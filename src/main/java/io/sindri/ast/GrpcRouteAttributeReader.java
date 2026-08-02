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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.GrpcRouteAttributeReaderContract;
import io.sindri.ast.data.GrpcRouteData;
import io.sindri.ast.data.HandlerData;
import io.sindri.ast.data.result.GrpcRouteAttributeResult;
import io.sindri.ast.throwable.exception.NonLiteralAttributeValueException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GrpcRouteAttributeReader extends AstReader
        implements GrpcRouteAttributeReaderContract {

    /**
     * The gRPC middleware stages, in {@link GrpcRouteData} constructor order. Each pairs a stage
     * contract (matched against a middleware's ancestry) with the {@code Route} builder call that
     * registers a class under it in the generated cache.
     */
    private enum Stage {
        ROUTE_MATCHED(
                "io.valkyrja.grpc.middleware.contract.RouteMatchedMiddlewareContract",
                "withAddedRouteMatchedMiddleware"),
        ROUTE_DISPATCHED(
                "io.valkyrja.grpc.middleware.contract.RouteDispatchedMiddlewareContract",
                "withAddedRouteDispatchedMiddleware"),
        THROWABLE_CAUGHT(
                "io.valkyrja.grpc.middleware.contract.ThrowableCaughtMiddlewareContract",
                "withAddedThrowableCaughtMiddleware"),
        SENDING_RESPONSE(
                "io.valkyrja.grpc.middleware.contract.SendingResponseMiddlewareContract",
                "withAddedSendingResponseMiddleware"),
        RESPONSE_SENT(
                "io.valkyrja.grpc.middleware.contract.ResponseSentMiddlewareContract",
                "withAddedResponseSentMiddleware");

        private final String contractFqn;
        private final String addMethod;

        Stage(String contractFqn, String addMethod) {
            this.contractFqn = contractFqn;
            this.addMethod = addMethod;
        }
    }

    private static final Set<String> STAGE_CONTRACTS = stageContracts();

    private final MiddlewareClassifier classifier = new MiddlewareClassifier();
    private final MiddlewareClassifier.SourceResolver resolver;

    /** No-op resolver: without a way to resolve middleware sources, no middleware is classified. */
    public GrpcRouteAttributeReader() {
        this(fqn -> Optional.empty());
    }

    public GrpcRouteAttributeReader(MiddlewareClassifier.SourceResolver resolver) {
        this.resolver = resolver;
    }

    private static Set<String> stageContracts() {
        Set<String> contracts = new java.util.HashSet<>();
        for (Stage stage : Stage.values()) {
            contracts.add(stage.contractFqn);
        }
        return contracts;
    }

    @Override
    public GrpcRouteAttributeResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        String pkg = getPackageName(cu);
        Map<String, String> imports = buildImportMap(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, Expression> routeMap = new LinkedHashMap<>();
        Map<String, GrpcRouteData> routeDataMap = new LinkedHashMap<>();

        String service = readServiceName(type);
        if (service.isEmpty()) {
            return new GrpcRouteAttributeResult(routeMap, routeDataMap);
        }

        String controllerFqn =
                pkg.isEmpty() ? type.getNameAsString() : pkg + "." + type.getNameAsString();

        for (MethodDeclaration method : type.getMethods()) {
            NormalAnnotationExpr methodAttribute = findMethodAttribute(method);
            if (methodAttribute == null) {
                continue;
            }

            String name = "";
            boolean clientStreaming = false;
            boolean serverStreaming = false;
            for (MemberValuePair pair : methodAttribute.getPairs()) {
                switch (pair.getNameAsString()) {
                    case "name" ->
                            name =
                                    requireStringLiteral(
                                            pair.getValue(),
                                            "@Method(name)",
                                            method.getNameAsString());
                    case "clientStreaming" -> clientStreaming = extractBoolean(pair.getValue());
                    case "serverStreaming" -> serverStreaming = extractBoolean(pair.getValue());
                    default -> {}
                }
            }
            if (name.isEmpty()) {
                continue;
            }

            String fullMethod = "/" + service + "/" + name;
            String methodName = method.getNameAsString();
            Map<Stage, List<String>> middleware = classifyMiddleware(method, imports, pkg);

            GrpcRouteData data =
                    new GrpcRouteData(
                            fullMethod,
                            service,
                            name,
                            new HandlerData(controllerFqn, methodName),
                            clientStreaming,
                            serverStreaming,
                            stageList(middleware, Stage.ROUTE_MATCHED),
                            stageList(middleware, Stage.ROUTE_DISPATCHED),
                            stageList(middleware, Stage.THROWABLE_CAUGHT),
                            stageList(middleware, Stage.SENDING_RESPONSE),
                            stageList(middleware, Stage.RESPONSE_SENT));
            routeDataMap.put(fullMethod, data);
            routeMap.put(
                    fullMethod,
                    buildRouteValue(
                            fullMethod,
                            controllerFqn,
                            methodName,
                            clientStreaming,
                            serverStreaming,
                            middleware));
        }

        return new GrpcRouteAttributeResult(routeMap, routeDataMap);
    }

    /** Read the {@code service} value from the class-level {@code @Service}, or "" if absent. */
    private String readServiceName(TypeDeclaration<?> type) {
        for (AnnotationExpr annotation : type.getAnnotations()) {
            if (annotation.getNameAsString().equals("Service")
                    && annotation instanceof NormalAnnotationExpr normal) {
                for (MemberValuePair pair : normal.getPairs()) {
                    if (pair.getNameAsString().equals("service")) {
                        return requireStringLiteral(
                                pair.getValue(), "@Service(service)", type.getNameAsString());
                    }
                }
            }
        }
        return "";
    }

    /**
     * Resolve an annotation value that must be a string literal.
     *
     * <p>Sindri parses source syntactically, so a non-literal (a constant reference, a
     * concatenation) cannot be evaluated here. Falling back to the expression's source text would
     * bake a route key like {@code /SERVICE_NAME/Method} into the cache — a route that silently
     * answers {@code UNIMPLEMENTED}, and only when the cache is enabled. Fail at generation time
     * instead, where the developer can see it.
     *
     * @param expr the annotation value
     * @param what the annotation member being read, for the error message
     * @param where the declaring type or method, for the error message
     * @return the literal value
     */
    private String requireStringLiteral(Expression expr, String what, String where) {
        if (!expr.isStringLiteralExpr()) {
            throw new NonLiteralAttributeValueException(
                    "%s must be a string literal to be cached, but %s uses '%s'. Inline the literal or disable the gRPC route cache."
                            .formatted(what, where, expr));
        }

        return expr.asStringLiteralExpr().asString();
    }

    private @org.jspecify.annotations.Nullable NormalAnnotationExpr findMethodAttribute(
            MethodDeclaration method) {
        return method.getAnnotations().stream()
                .filter(a -> a.getNameAsString().equals("Method"))
                .filter(a -> a instanceof NormalAnnotationExpr)
                .map(a -> (NormalAnnotationExpr) a)
                .findFirst()
                .orElse(null);
    }

    /**
     * Resolve every {@code @Middleware} on the method and bucket it by the stages it implements.
     *
     * <p>The annotation names only the class, so each is classified by walking its type hierarchy —
     * the generation-time equivalent of the runtime collector's {@code isAssignableFrom} cascade. A
     * class implementing several stage contracts is registered under every one of them.
     *
     * @param method the handler method
     * @param imports the controller's imports, for resolving the middleware class names
     * @param pkg the controller's package, for same-package middleware
     * @return every stage mapped to its (possibly empty) middleware list
     */
    private Map<Stage, List<String>> classifyMiddleware(
            MethodDeclaration method, Map<String, String> imports, String pkg) {
        Map<String, List<String>> byContract =
                classifier.classifyMethod(method, imports, pkg, resolver, STAGE_CONTRACTS);

        Map<Stage, List<String>> byStage = new java.util.EnumMap<>(Stage.class);
        for (Stage stage : Stage.values()) {
            byStage.put(stage, byContract.getOrDefault(stage.contractFqn, List.of()));
        }

        return byStage;
    }

    /** The (always-present) middleware list for a stage. */
    private static List<String> stageList(Map<Stage, List<String>> byStage, Stage stage) {
        return java.util.Objects.requireNonNull(byStage.get(stage));
    }

    /**
     * Build the {@code Supplier<RouteContract>} expression stored as a route's value, e.g. {@code
     * () -> new Route("/pkg.Greeter/SayHello", (c, r) -> new pkg.Greeter().sayHello(c, r))}. Fully
     * qualified names are used so the generated file needs no extra imports. Middleware is emitted
     * pre-classified, so the cached route needs no stage discovery at runtime.
     */
    private Expression buildRouteValue(
            String fullMethod,
            String controllerFqn,
            String methodName,
            boolean clientStreaming,
            boolean serverStreaming,
            Map<Stage, List<String>> middleware) {
        StringBuilder supplier =
                new StringBuilder(
                        "() -> new io.valkyrja.grpc.routing.data.Route(\""
                                + escapeJava(fullMethod)
                                + "\", (c, r) -> new "
                                + controllerFqn
                                + "()."
                                + methodName
                                + "(c, r))");
        if (clientStreaming) {
            supplier.append(".withClientStreaming(true)");
        }
        if (serverStreaming) {
            supplier.append(".withServerStreaming(true)");
        }
        for (Stage stage : Stage.values()) {
            List<String> classes = stageList(middleware, stage);
            if (classes.isEmpty()) {
                continue;
            }
            supplier.append(".")
                    .append(stage.addMethod)
                    .append("(java.util.List.of(")
                    .append(String.join(".class, ", classes))
                    .append(".class))");
        }

        return StaticJavaParser.parseExpression(supplier.toString());
    }

    private boolean extractBoolean(Expression expr) {
        return expr.isBooleanLiteralExpr() && expr.asBooleanLiteralExpr().getValue();
    }

    private String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
