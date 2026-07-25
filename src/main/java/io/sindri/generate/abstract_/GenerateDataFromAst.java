/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generate.abstract_;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import io.sindri.ast.CliRouteAttributeReader;
import io.sindri.ast.ComponentProviderReader;
import io.sindri.ast.ConfigReader;
import io.sindri.ast.GrpcRouteAttributeReader;
import io.sindri.ast.HttpRouteAttributeReader;
import io.sindri.ast.ListenerProviderReader;
import io.sindri.ast.MiddlewareClassifier;
import io.sindri.ast.RouteProviderReader;
import io.sindri.ast.ServiceProviderReader;
import io.sindri.ast.data.HttpRouteData;
import io.sindri.ast.data.result.CliRouteAttributeResult;
import io.sindri.ast.data.result.ComponentProviderResult;
import io.sindri.ast.data.result.ConfigResult;
import io.sindri.ast.data.result.GrpcRouteAttributeResult;
import io.sindri.ast.data.result.HttpRouteAttributeResult;
import io.sindri.ast.data.result.RouteProviderResult;
import io.sindri.generator.cli.contract.CliDataFileGeneratorContract;
import io.sindri.generator.container.contract.ContainerDataFileGeneratorContract;
import io.sindri.generator.enum_.GenerateStatus;
import io.sindri.generator.event.contract.EventDataFileGeneratorContract;
import io.sindri.generator.grpc.contract.GrpcDataFileGeneratorContract;
import io.sindri.generator.http.contract.HttpDataFileGeneratorContract;
import io.sindri.generator.throwable.exception.DataFileWriteException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class GenerateDataFromAst {

    private final ConfigReader configReader = new ConfigReader();
    private final ComponentProviderReader componentProviderReader = new ComponentProviderReader();
    private final ServiceProviderReader serviceProviderReader = new ServiceProviderReader();
    private final RouteProviderReader routeProviderReader = new RouteProviderReader();
    private final ListenerProviderReader listenerProviderReader = new ListenerProviderReader();

    /**
     * Resolve a class to its parsed source for middleware classification: app classes from the
     * source tree, framework classes from the sources jar on the classpath. An unresolvable class
     * simply stops that branch of the hierarchy walk.
     *
     * @param config the generation config
     * @return the resolver
     */
    private MiddlewareClassifier.SourceResolver middlewareSourceResolver(ConfigResult config) {
        return fqn -> {
            String path = fqnToFilePath(fqn, config.namespace(), config.dir());
            if (path.isEmpty()) {
                return java.util.Optional.empty();
            }
            try {
                return java.util.Optional.of(
                        com.github.javaparser.StaticJavaParser.parse(new java.io.File(path)));
            } catch (java.io.FileNotFoundException e) {
                return java.util.Optional.empty();
            }
        };
    }

    /** FQN → staged temp source path (or "" when unresolvable) for classpath-resolved sources. */
    private final Map<String, String> classpathSourceCache = new java.util.HashMap<>();

    protected abstract ContainerDataFileGeneratorContract getContainerDataFileGenerator();

    protected abstract EventDataFileGeneratorContract getEventDataFileGenerator();

    protected abstract CliDataFileGeneratorContract getCliDataFileGenerator();

    protected abstract HttpDataFileGeneratorContract getHttpDataFileGenerator();

    protected abstract GrpcDataFileGeneratorContract getGrpcDataFileGenerator();

    public void run(String configFilePath) {
        ConfigResult config = configReader.readFile(configFilePath);

        ComponentProviderResult allProviderData = collectProviderData(config);

        Map<String, String[]> publishers =
                collectPublishers(allProviderData.serviceProviders(), config);

        Map<String, String> listeners =
                collectListeners(allProviderData.listenerProviders(), config);

        Map<String, String> cliRoutes =
                collectCliRoutes(allProviderData.cliRouteProviders(), config);

        Map<String, String> httpRoutes = new LinkedHashMap<>();
        Map<String, HttpRouteData> httpRouteData = new LinkedHashMap<>();
        collectHttpRoutes(allProviderData.httpRouteProviders(), config, httpRoutes, httpRouteData);

        Map<String, String> grpcRoutes =
                collectGrpcRoutes(allProviderData.grpcRouteProviders(), config);

        String dataClassName = "AppContainerData";
        String dataDir = config.dataPath();
        String dataNamespace = config.dataNamespace();

        requireWritten(
                dataClassName,
                getContainerDataFileGenerator()
                        .generateFile(dataDir, dataClassName, dataNamespace, publishers));
        requireWritten(
                "AppEventData",
                getEventDataFileGenerator()
                        .generateFile(dataDir, "AppEventData", dataNamespace, listeners));
        requireWritten(
                "AppCliRoutingData",
                getCliDataFileGenerator()
                        .generateFile(dataDir, "AppCliRoutingData", dataNamespace, cliRoutes));
        requireWritten(
                "AppHttpRoutingData",
                getHttpDataFileGenerator()
                        .generateFile(
                                dataDir,
                                "AppHttpRoutingData",
                                dataNamespace,
                                httpRoutes,
                                httpRouteData));
        requireWritten(
                "AppGrpcRoutingData",
                getGrpcDataFileGenerator()
                        .generateFile(dataDir, "AppGrpcRoutingData", dataNamespace, grpcRoutes));
    }

    /**
     * Fail loudly when a data file could not be written.
     *
     * <p>A discarded {@link GenerateStatus#FAILURE} leaves the previous generation's data class on
     * disk (or none at all) while the build reports success — the application then boots against a
     * stale route map. Surface it instead.
     *
     * @param dataClassName the data class being written, for the error message
     * @param status the generator's result
     */
    private void requireWritten(String dataClassName, GenerateStatus status) {
        if (status == GenerateStatus.FAILURE) {
            throw new DataFileWriteException("Failed to write " + dataClassName + ".");
        }
    }

    private ComponentProviderResult collectProviderData(ConfigResult config) {
        ComponentProviderResult combined = new ComponentProviderResult();

        // Walk the full component-provider graph breadth-first. Each provider can nest further
        // component providers (e.g. an app provider pulling in framework providers, which pull in
        // their own), so recurse to any depth, guarding against cycles with a visited set.
        java.util.Deque<String> queue = new java.util.ArrayDeque<>(config.providers());
        java.util.Set<String> visited = new java.util.HashSet<>();

        while (!queue.isEmpty()) {
            String providerFqn = queue.poll();
            if (!visited.add(providerFqn)) {
                continue;
            }

            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }

            ComponentProviderResult data = componentProviderReader.readFile(filePath);
            combined = combined.merge(data);
            queue.addAll(data.componentProviders());
        }

        return combined;
    }

    private Map<String, String[]> collectPublishers(
            java.util.List<String> serviceProviders, ConfigResult config) {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        for (String providerFqn : serviceProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            publishers.putAll(serviceProviderReader.readFile(filePath).publishers());
        }
        return publishers;
    }

    private Map<String, String> collectListeners(
            java.util.List<String> listenerProviders, ConfigResult config) {
        Map<String, String> listeners = new LinkedHashMap<>();
        for (String providerFqn : listenerProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            listenerProviderReader
                    .readFile(filePath)
                    .listeners()
                    .forEach(expr -> listeners.put(expr.toString(), expr.toString()));
        }
        return listeners;
    }

    private Map<String, String> collectCliRoutes(
            java.util.List<String> cliRouteProviders, ConfigResult config) {
        // Built per config so the reader can resolve each @Middleware class's source and classify
        // it
        // into its stages before the route is cached.
        CliRouteAttributeReader cliRouteAttributeReader =
                new CliRouteAttributeReader(middlewareSourceResolver(config));
        Map<String, String> routes = new LinkedHashMap<>();
        for (String providerFqn : cliRouteProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            RouteProviderResult routeProvider = routeProviderReader.readFile(filePath);
            for (String controllerFqn : routeProvider.controllerClasses()) {
                String controllerPath =
                        fqnToFilePath(controllerFqn, config.namespace(), config.dir());
                if (!controllerPath.isEmpty()) {
                    CliRouteAttributeResult result =
                            cliRouteAttributeReader.readFile(controllerPath);
                    result.routes().forEach((name, expr) -> routes.put(name, expr.toString()));
                }
            }

            // Manually-defined provider routes (getRoutes()) — the CLI Route's name is its first
            // constructor argument.
            for (Expression routeExpr : routeProvider.routes()) {
                String name = extractRouteArgString(routeExpr, 0);
                if (!name.isEmpty()) {
                    routes.put(name, "() -> " + routeExpr);
                }
            }
        }
        return routes;
    }

    private Map<String, String> collectGrpcRoutes(
            java.util.List<String> grpcRouteProviders, ConfigResult config) {
        Map<String, String> routes = new LinkedHashMap<>();
        for (String providerFqn : grpcRouteProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            // Built per config so the reader can resolve each @Middleware class's source and
            // classify it into its stages before the route is cached.
            GrpcRouteAttributeReader grpcRouteAttributeReader =
                    new GrpcRouteAttributeReader(middlewareSourceResolver(config));
            RouteProviderResult routeProvider = routeProviderReader.readFile(filePath);
            for (String controllerFqn : routeProvider.controllerClasses()) {
                String controllerPath =
                        fqnToFilePath(controllerFqn, config.namespace(), config.dir());
                if (!controllerPath.isEmpty()) {
                    GrpcRouteAttributeResult result =
                            grpcRouteAttributeReader.readFile(controllerPath);
                    result.routes().forEach((method, expr) -> routes.put(method, expr.toString()));
                }
            }

            // Manually-defined provider routes (getRoutes()) — the gRPC Route's fully-qualified
            // method is its first constructor argument.
            for (Expression routeExpr : routeProvider.routes()) {
                String method = extractRouteArgString(routeExpr, 0);
                if (!method.isEmpty()) {
                    routes.put(method, "() -> " + routeExpr);
                }
            }
        }
        return routes;
    }

    private void collectHttpRoutes(
            java.util.List<String> httpRouteProviders,
            ConfigResult config,
            Map<String, String> httpRoutes,
            Map<String, HttpRouteData> httpRouteData) {
        // Built per config so the reader can resolve each @Middleware class's source and classify
        // it
        // into its stages before the route is cached.
        HttpRouteAttributeReader httpRouteAttributeReader =
                new HttpRouteAttributeReader(middlewareSourceResolver(config));
        for (String providerFqn : httpRouteProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            RouteProviderResult routeProvider = routeProviderReader.readFile(filePath);
            for (String controllerFqn : routeProvider.controllerClasses()) {
                String controllerPath =
                        fqnToFilePath(controllerFqn, config.namespace(), config.dir());
                if (!controllerPath.isEmpty()) {
                    HttpRouteAttributeResult result =
                            httpRouteAttributeReader.readFile(controllerPath);
                    result.routes().forEach((name, expr) -> httpRoutes.put(name, expr.toString()));
                    httpRouteData.putAll(result.routeData());
                }
            }

            // Manually-defined provider routes (getRoutes()) are inlined too, so the cached data
            // holds every route and the runtime never has to iterate providers.
            for (Expression routeExpr : routeProvider.routes()) {
                String name = extractRouteArgString(routeExpr, 1);
                if (name.isEmpty()) {
                    continue;
                }
                httpRoutes.put(name, "() -> " + routeExpr);
                httpRouteData.put(name, buildHttpRouteDataFromExpr(routeExpr, name));
            }
        }
    }

    private String extractRouteArgString(Expression routeExpr, int index) {
        // Callers only pass object-creation route expressions (RouteProviderReader filters them).
        var arguments = routeExpr.asObjectCreationExpr().getArguments();
        if (index < arguments.size() && arguments.get(index).isStringLiteralExpr()) {
            return arguments.get(index).asStringLiteralExpr().getValue();
        }
        return "";
    }

    private HttpRouteData buildHttpRouteDataFromExpr(Expression routeExpr, String name) {
        String path = extractRouteArgString(routeExpr, 0);
        boolean isDynamic = path.contains("{");
        String regex = isDynamic ? extractRouteArgString(routeExpr, 2) : "";

        return new HttpRouteData(
                path,
                name,
                null,
                extractRequestMethodsFromExpr(routeExpr),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                null,
                null,
                isDynamic,
                java.util.List.of(),
                regex);
    }

    /** Pull RequestMethod constants out of a route expression, defaulting to HEAD + GET. */
    private java.util.List<String> extractRequestMethodsFromExpr(Expression routeExpr) {
        java.util.List<String> methods = new java.util.ArrayList<>();
        for (FieldAccessExpr fieldAccess : routeExpr.findAll(FieldAccessExpr.class)) {
            if (fieldAccess.getScope().toString().endsWith("RequestMethod")) {
                methods.add(fieldAccess.getNameAsString());
            }
        }
        return methods.isEmpty() ? java.util.List.of("HEAD", "GET") : methods;
    }

    private String fqnToFilePath(String fqn, String namespace, String srcDir) {
        String namespacePkg = namespace.toLowerCase(java.util.Locale.ROOT);
        if (fqn.startsWith(namespacePkg + ".")) {
            String relative = fqn.substring(namespacePkg.length() + 1).replace('.', '/');
            return srcDir + "/" + relative + ".java";
        }

        // Framework/vendor classes live outside the app source tree. Resolve their .java from the
        // sources jar on the classpath (the portable equivalent of PHP's
        // ReflectionClass::getFileName()) so their publishers()/providers can be scanned too.
        return resolveSourceFromClasspath(fqn);
    }

    /**
     * Resolve a class's {@code .java} source from a sources jar on the classpath and stage it as a
     * temp file (the AST readers accept only file paths). Returns {@code ""} when not found.
     */
    protected String resolveSourceFromClasspath(String fqn) {
        String cached = classpathSourceCache.get(fqn);
        if (cached != null) {
            return cached;
        }

        String resource = fqn.replace('.', '/') + ".java";
        java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            classpathSourceCache.put(fqn, "");
            return "";
        }

        // A plain try/catch (rather than try-with-resources) keeps both outcomes fully reachable;
        // stageSource owns closing the stream.
        try {
            String path = stageSource(in);
            classpathSourceCache.put(fqn, path);

            return path;
        } catch (java.io.IOException e) {
            classpathSourceCache.put(fqn, "");
            return "";
        }
    }

    /** Copy a resolved source stream to a temp file, closing the stream, and return its path. */
    protected String stageSource(java.io.InputStream in) throws java.io.IOException {
        java.nio.file.Path temp = java.nio.file.Files.createTempFile("sindri-src-", ".java");
        temp.toFile().deleteOnExit();
        java.nio.file.Files.copy(in, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        in.close();

        return temp.toString();
    }
}
