/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.HttpRouteAttributeReaderContract;
import io.sindri.ast.data.HandlerData;
import io.sindri.ast.data.HttpParameterData;
import io.sindri.ast.data.HttpRouteData;
import io.sindri.ast.data.result.HttpRouteAttributeResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public class HttpRouteAttributeReader extends AstReader
        implements HttpRouteAttributeReaderContract {

    /** The route-level middleware stage contracts, in {@code HttpRouteData} constructor order. */
    private static final List<String> STAGE_CONTRACTS =
            List.of(
                    "io.valkyrja.http.middleware.contract.RouteMatchedMiddlewareContract",
                    "io.valkyrja.http.middleware.contract.RouteDispatchedMiddlewareContract",
                    "io.valkyrja.http.middleware.contract.ThrowableCaughtMiddlewareContract",
                    "io.valkyrja.http.middleware.contract.SendingResponseMiddlewareContract",
                    "io.valkyrja.http.middleware.contract.ResponseSentMiddlewareContract");

    private final HttpRouteParameterReader parameterReader = new HttpRouteParameterReader();
    private final MiddlewareClassifier classifier = new MiddlewareClassifier();
    private final MiddlewareClassifier.SourceResolver resolver;

    /** No-op resolver: without a way to resolve middleware sources, no middleware is classified. */
    public HttpRouteAttributeReader() {
        this(fqn -> Optional.empty());
    }

    public HttpRouteAttributeReader(MiddlewareClassifier.SourceResolver resolver) {
        this.resolver = resolver;
    }

    /** Classify the method's {@code @Middleware} into the stage lists, in constructor order. */
    private List<List<String>> classifyMiddleware(
            MethodDeclaration method, Map<String, String> imports, String pkg) {
        Map<String, List<String>> byContract =
                classifier.classifyMethod(
                        method, imports, pkg, resolver, Set.copyOf(STAGE_CONTRACTS));
        List<List<String>> lists = new ArrayList<>();
        for (String contract : STAGE_CONTRACTS) {
            lists.add(byContract.getOrDefault(contract, List.of()));
        }
        return lists;
    }

    /** Emit the five stage middleware lists as positional {@code List.of(...)} constructor args. */
    private String emitMiddlewareLists(List<List<String>> middleware) {
        List<String> parts = new ArrayList<>();
        for (List<String> classes : middleware) {
            parts.add(
                    classes.isEmpty()
                            ? "java.util.List.of()"
                            : "java.util.List.of(" + String.join(".class, ", classes) + ".class)");
        }
        return String.join(", ", parts);
    }

    @Override
    public HttpRouteAttributeResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        Map<String, String> importMap = buildImportMap(cu);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, Expression> routeMap = new LinkedHashMap<>();
        Map<String, HttpRouteData> routeDataMap = new LinkedHashMap<>();

        for (MethodDeclaration method : type.getMethods()) {
            List<AnnotationExpr> routeAnnotations =
                    method.getAnnotations().stream()
                            .filter(
                                    a ->
                                            a.getNameAsString().equals("Route")
                                                    || a.getNameAsString().equals("DynamicRoute"))
                            .toList();

            if (routeAnnotations.isEmpty()) {
                continue;
            }

            Optional<AnnotationExpr> handlerAnnotation =
                    method.getAnnotations().stream()
                            .filter(a -> a.getNameAsString().equals("RouteHandler"))
                            .findFirst();

            String handlerClass = "";
            String handlerMethod = "";
            if (handlerAnnotation.isPresent()
                    && handlerAnnotation.get() instanceof NormalAnnotationExpr handler) {
                for (MemberValuePair pair : handler.getPairs()) {
                    if (pair.getNameAsString().equals("handlerClass")) {
                        handlerClass = extractClassExprFqn(pair.getValue(), importMap, pkg);
                    } else if (pair.getNameAsString().equals("handlerMethod")) {
                        handlerMethod = extractStringLiteral(pair.getValue());
                    }
                }
            }

            for (AnnotationExpr routeAnnotation : routeAnnotations) {
                if (!(routeAnnotation instanceof NormalAnnotationExpr normalRoute)) {
                    continue;
                }
                String path = "";
                String name = "";
                List<String> requestMethods = List.of("HEAD", "GET");
                for (MemberValuePair pair : normalRoute.getPairs()) {
                    switch (pair.getNameAsString()) {
                        case "path" -> path = extractStringLiteral(pair.getValue());
                        case "name" -> name = extractStringLiteral(pair.getValue());
                        case "requestMethods" ->
                                requestMethods = extractRequestMethods(pair.getValue());
                        default -> {}
                    }
                }
                HandlerData handler =
                        !handlerClass.isEmpty()
                                ? new HandlerData(handlerClass, handlerMethod)
                                : null;

                // A path containing a {placeholder} is a dynamic route: collect its parameters and
                // precompute the matching regex (so the cached data needs no runtime processing).
                boolean isDynamic = path.contains("{");
                List<HttpParameterData> parameters =
                        isDynamic
                                ? parameterReader.updateParameters(method, importMap, pkg)
                                : List.of();
                String regex = isDynamic ? computeRegex(path, name, parameters) : "";
                List<List<String>> middleware = classifyMiddleware(method, importMap, pkg);

                HttpRouteData data =
                        new HttpRouteData(
                                path,
                                name,
                                handler,
                                requestMethods,
                                middleware.get(0),
                                middleware.get(1),
                                middleware.get(2),
                                middleware.get(3),
                                middleware.get(4),
                                null,
                                null,
                                isDynamic,
                                parameters,
                                regex);
                routeDataMap.put(name, data);
                routeMap.put(
                        name,
                        buildRouteValue(
                                path,
                                name,
                                handler,
                                requestMethods,
                                isDynamic,
                                parameters,
                                regex,
                                middleware));
            }
        }
        return new HttpRouteAttributeResult(routeMap, routeDataMap);
    }

    /**
     * Build the {@code Supplier<RouteContract>} expression stored as a route's value, e.g. {@code
     * () -> new Route("/path", "name", Handler::method, List.of(RequestMethod.GET))}. Fully
     * qualified names are used so the generated file needs no extra imports.
     */
    private Expression buildRouteValue(
            String path,
            String name,
            @Nullable HandlerData handler,
            List<String> requestMethods,
            boolean isDynamic,
            List<HttpParameterData> parameters,
            String regex,
            List<List<String>> middleware) {
        String handlerRef =
                handler != null ? handler.handlerClass() + "::" + handler.method() : "null";

        // The HEAD+GET default with no middleware is exactly what the short Route/DynamicRoute
        // constructors apply, so it needs no explicit arguments; any other set — or any middleware
        // —
        // needs the full constructor (otherwise the middleware would be silently dropped).
        boolean hasMiddleware = middleware.stream().anyMatch(list -> !list.isEmpty());
        String tail =
                requestMethods.equals(List.of("HEAD", "GET")) && !hasMiddleware
                        ? ")"
                        : ", "
                                + buildRequestMethodsList(requestMethods)
                                + ", "
                                + emitMiddlewareLists(middleware)
                                + ", null, null)";

        String head;
        if (isDynamic) {
            head =
                    "() -> new io.valkyrja.http.routing.data.DynamicRoute(\""
                            + path
                            + "\", \""
                            + name
                            + "\", \""
                            + escapeJava(regex)
                            + "\", "
                            + buildParameterList(parameters)
                            + ", "
                            + handlerRef;
        } else {
            head =
                    "() -> new io.valkyrja.http.routing.data.Route(\""
                            + path
                            + "\", \""
                            + name
                            + "\", "
                            + handlerRef;
        }

        return StaticJavaParser.parseExpression(head + tail);
    }

    /**
     * Emit {@code List.of(new Parameter("name", "regex", ...), ...)} mirroring the framework
     * collector, carrying each parameter's optional and capture flags so the cached route binds
     * exactly as a collected one does.
     */
    private String buildParameterList(List<HttpParameterData> parameters) {
        StringBuilder sb = new StringBuilder("java.util.List.of(");
        for (int i = 0; i < parameters.size(); i++) {
            HttpParameterData parameter = parameters.get(i);
            sb.append("new io.valkyrja.http.routing.data.Parameter(\"")
                    .append(parameter.name())
                    .append("\", \"")
                    .append(escapeJava(parameter.regex()))
                    .append("\", null, ")
                    .append(parameter.isOptional())
                    .append(", ")
                    .append(parameter.shouldCapture())
                    .append(", null, null)");
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Precompute a dynamic route's match regex by running the real framework {@link
     * io.valkyrja.http.routing.processor.Processor}, so the cached regex is always identical to
     * what the framework would compute at runtime.
     */
    @SuppressWarnings(
            "NullAway") // throwaway route used only to compute the regex; handler is unused
    private String computeRegex(String path, String name, List<HttpParameterData> parameters) {
        List<io.valkyrja.http.routing.data.contract.ParameterContract> dataParameters =
                new java.util.ArrayList<>();
        for (HttpParameterData parameter : parameters) {
            dataParameters.add(
                    new io.valkyrja.http.routing.data.Parameter(
                            parameter.name(),
                            parameter.regex(),
                            null,
                            parameter.isOptional(),
                            parameter.shouldCapture(),
                            null,
                            null));
        }

        try {
            io.valkyrja.http.routing.data.DynamicRoute route =
                    new io.valkyrja.http.routing.data.DynamicRoute(
                            path, name, "", dataParameters, null);
            io.valkyrja.http.routing.data.contract.RouteContract processed =
                    new io.valkyrja.http.routing.processor.Processor().route(route);

            return ((io.valkyrja.http.routing.data.contract.DynamicRouteContract) processed)
                    .getRegex();
        } catch (RuntimeException e) {
            // Malformed dynamic route (e.g. a parameter with no matching placeholder) — skip it.
            return "";
        }
    }

    private String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String buildRequestMethodsList(List<String> methods) {
        StringBuilder sb = new StringBuilder("java.util.List.of(");
        for (int i = 0; i < methods.size(); i++) {
            // Tolerate string-literal request methods (e.g. "GET") by stripping the quotes so the
            // emitted RequestMethod constant stays a valid identifier.
            String method = methods.get(i).replace("\"", "");
            sb.append("io.valkyrja.http.message.enum_.RequestMethod.").append(method);
            if (i < methods.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private List<String> extractRequestMethods(Expression expr) {
        List<String> methods = new ArrayList<>();
        if (expr.isArrayInitializerExpr()) {
            ArrayInitializerExpr array = expr.asArrayInitializerExpr();
            for (Expression value : array.getValues()) {
                methods.add(extractEnumName(value));
            }
        } else {
            methods.add(extractEnumName(expr));
        }
        return methods;
    }

    private String extractEnumName(Expression expr) {
        if (expr.isFieldAccessExpr()) {
            return expr.asFieldAccessExpr().getNameAsString();
        }
        if (expr.isNameExpr()) {
            return expr.asNameExpr().getNameAsString();
        }
        return expr.toString();
    }
}
