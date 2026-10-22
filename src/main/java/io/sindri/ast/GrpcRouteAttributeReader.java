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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public class GrpcRouteAttributeReader extends AstReader
        implements GrpcRouteAttributeReaderContract {

    /**
     * The gRPC middleware stages, in {@link GrpcRouteData} constructor order. Each pairs a stage
     * contract (matched against a middleware's ancestry) with the {@code @Route} member that names
     * the stage's middleware and the {@code Route} builder call that registers a class under it in
     * the generated cache.
     */
    private enum Stage {
        ROUTE_MATCHED(
                "io.valkyrja.grpc.middleware.contract.RouteMatchedMiddlewareContract",
                "routeMatchedMiddleware",
                "withAddedRouteMatchedMiddleware"),
        ROUTE_DISPATCHED(
                "io.valkyrja.grpc.middleware.contract.RouteDispatchedMiddlewareContract",
                "routeDispatchedMiddleware",
                "withAddedRouteDispatchedMiddleware"),
        THROWABLE_CAUGHT(
                "io.valkyrja.grpc.middleware.contract.ThrowableCaughtMiddlewareContract",
                "throwableCaughtMiddleware",
                "withAddedThrowableCaughtMiddleware"),
        SENDING_RESPONSE(
                "io.valkyrja.grpc.middleware.contract.SendingResponseMiddlewareContract",
                "sendingResponseMiddleware",
                "withAddedSendingResponseMiddleware"),
        RESPONSE_SENT(
                "io.valkyrja.grpc.middleware.contract.ResponseSentMiddlewareContract",
                "responseSentMiddleware",
                "withAddedResponseSentMiddleware");

        private final String contractFqn;
        private final String member;
        private final String addMethod;

        Stage(String contractFqn, String member, String addMethod) {
            this.contractFqn = contractFqn;
            this.member = member;
            this.addMethod = addMethod;
        }

        private static @Nullable Stage forMember(String member) {
            for (Stage stage : values()) {
                if (stage.member.equals(member)) {
                    return stage;
                }
            }

            return null;
        }
    }

    /** The class an optional {@code Class<?>} member names when it is left at its default. */
    private static final String ABSENT_CLASS = "java.lang.Void";

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
            NormalAnnotationExpr routeAttribute = findRouteAttribute(method);
            if (routeAttribute == null) {
                continue;
            }

            String name = "";
            boolean clientStreaming = false;
            boolean serverStreaming = false;
            String requestType = "";
            String responseType = "";
            HandlerData handler = null;
            Map<Stage, List<String>> named = new EnumMap<>(Stage.class);

            for (MemberValuePair pair : routeAttribute.getPairs()) {
                String member = pair.getNameAsString();
                Stage stage = Stage.forMember(member);

                if (stage != null) {
                    named.put(stage, extractClassList(pair.getValue(), imports, pkg));
                    continue;
                }

                switch (member) {
                    case "name" ->
                            name =
                                    requireStringLiteral(
                                            pair.getValue(),
                                            "@Route(name)",
                                            method.getNameAsString());
                    case "clientStreaming" -> clientStreaming = extractBoolean(pair.getValue());
                    case "serverStreaming" -> serverStreaming = extractBoolean(pair.getValue());
                    case "requestType" ->
                            requestType = extractOptionalClass(pair.getValue(), imports, pkg);
                    case "responseType" ->
                            responseType = extractOptionalClass(pair.getValue(), imports, pkg);
                    case "handler" ->
                            handler =
                                    readHandler(
                                            pair.getValue(),
                                            imports,
                                            pkg,
                                            method.getNameAsString());
                    default -> {}
                }
            }
            if (name.isEmpty()) {
                continue;
            }

            // The convenience attribute is a second spelling of the one field, and a single-valued
            // field takes the convenience attribute's value, so it wins over the member.
            HandlerData annotated = readHandlerAttribute(method, imports, pkg);
            if (annotated != null) {
                handler = annotated;
            }

            String fullMethod = "/" + service + "/" + name;
            String methodName = method.getNameAsString();
            Map<Stage, List<String>> middleware =
                    middlewareByStage(named, classifyMiddleware(method, imports, pkg));

            GrpcRouteData data =
                    new GrpcRouteData(
                            fullMethod,
                            service,
                            name,
                            handler != null ? handler : new HandlerData(controllerFqn, methodName),
                            requestType.isEmpty() ? null : requestType,
                            responseType.isEmpty() ? null : responseType,
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
                            handlerExpression(handler, controllerFqn, methodName),
                            requestType,
                            responseType,
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

    private @Nullable NormalAnnotationExpr findRouteAttribute(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .filter(a -> a.getNameAsString().equals("Route"))
                .filter(a -> a instanceof NormalAnnotationExpr)
                .map(a -> (NormalAnnotationExpr) a)
                .findFirst()
                .orElse(null);
    }

    /**
     * Read the method-level {@code @RouteHandler}, the second spelling of the handler field.
     *
     * @param method the handler method
     * @param imports the controller's imports, for resolving the handler class name
     * @param pkg the controller's package, for a same-package handler class
     * @return the handler it names, or null where the method carries none
     */
    private @Nullable HandlerData readHandlerAttribute(
            MethodDeclaration method, Map<String, String> imports, String pkg) {
        for (AnnotationExpr annotation : method.getAnnotations()) {
            if (annotation.getNameAsString().equals("RouteHandler")) {
                return readHandler(annotation, imports, pkg, method.getNameAsString());
            }
        }

        return null;
    }

    /**
     * Read a {@code @RouteHandler}, whether it stands on the method or fills the {@code handler}
     * member. A Java annotation member holds a constant and no callable, so the handler is the
     * class and the method name.
     *
     * @param expr the annotation
     * @param imports the controller's imports, for resolving the handler class name
     * @param pkg the controller's package, for a same-package handler class
     * @param where the declaring method, for the error message
     * @return the handler it names, or null where it names the absent type
     */
    private @Nullable HandlerData readHandler(
            Expression expr, Map<String, String> imports, String pkg, String where) {
        if (!(expr instanceof NormalAnnotationExpr annotation)) {
            return null;
        }

        String handlerClass = "";
        String handlerMethod = "";

        for (MemberValuePair pair : annotation.getPairs()) {
            switch (pair.getNameAsString()) {
                case "handlerClass" ->
                        handlerClass = extractOptionalClass(pair.getValue(), imports, pkg);
                case "handlerMethod" ->
                        handlerMethod =
                                requireStringLiteral(
                                        pair.getValue(), "@RouteHandler(handlerMethod)", where);
                default -> {}
            }
        }

        return handlerClass.isEmpty() ? null : new HandlerData(handlerClass, handlerMethod);
    }

    /**
     * Resolve an optional {@code Class<?>} member, where {@code Void.class} means the member was
     * left at its default and names no class.
     *
     * @param expr the annotation value
     * @param imports the controller's imports, for resolving the class name
     * @param pkg the controller's package, for a same-package class
     * @return the fully-qualified class name, or "" where the member names none
     */
    private String extractOptionalClass(Expression expr, Map<String, String> imports, String pkg) {
        if (!expr.isClassExpr()) {
            return "";
        }

        String written = expr.asClassExpr().getTypeAsString();

        // The sentinel resolves through java.lang, which a file never imports, so neither the
        // import map nor the package resolves it. An import of a type of the same simple name
        // shadows it, and that type is a class the member names.
        if (!imports.containsKey(written)
                && ("Void".equals(written) || ABSENT_CLASS.equals(written))) {
            return "";
        }

        return extractClassExprFqn(expr, imports, pkg);
    }

    /**
     * Resolve a {@code Class<?>[]} member, which a single value may also spell without the braces.
     *
     * @param expr the annotation value
     * @param imports the controller's imports, for resolving each class name
     * @param pkg the controller's package, for a same-package class
     * @return the fully-qualified name of each class the member names
     */
    private List<String> extractClassList(
            Expression expr, Map<String, String> imports, String pkg) {
        List<String> classes = new ArrayList<>();
        List<Expression> values =
                expr.isArrayInitializerExpr()
                        ? expr.asArrayInitializerExpr().getValues()
                        : List.of(expr);

        for (Expression value : values) {
            String fqn = extractClassExprFqn(value, imports, pkg);
            if (!fqn.isEmpty()) {
                classes.add(fqn);
            }
        }

        return classes;
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

        Map<Stage, List<String>> byStage = new EnumMap<>(Stage.class);
        for (Stage stage : Stage.values()) {
            byStage.put(stage, byContract.getOrDefault(stage.contractFqn, List.of()));
        }

        return byStage;
    }

    /**
     * Join what the route attribute names with what the convenience attribute adds. A list field
     * adds the convenience attribute's value, so the attribute's own order comes first.
     *
     * @param named the middleware each stage member names
     * @param classified the middleware the {@code @Middleware} attributes name
     * @return every stage mapped to its (possibly empty) middleware list
     */
    private Map<Stage, List<String>> middlewareByStage(
            Map<Stage, List<String>> named, Map<Stage, List<String>> classified) {
        Map<Stage, List<String>> byStage = new EnumMap<>(Stage.class);

        for (Stage stage : Stage.values()) {
            List<String> classes = new ArrayList<>(named.getOrDefault(stage, List.of()));
            classes.addAll(stageList(classified, stage));
            byStage.put(stage, List.copyOf(classes));
        }

        return byStage;
    }

    /** The (always-present) middleware list for a stage. */
    private static List<String> stageList(Map<Stage, List<String>> byStage, Stage stage) {
        return Objects.requireNonNull(byStage.get(stage));
    }

    /**
     * Build the handler expression the cached route holds: a method reference to the static handler
     * the route names, or a call that resolves the controller from the container, exactly as the
     * runtime collector resolves it.
     *
     * @param handler the handler the route names, or null where it names none
     * @param controllerFqn the controller class
     * @param methodName the annotated method
     * @return the handler expression
     */
    private String handlerExpression(
            @Nullable HandlerData handler, String controllerFqn, String methodName) {
        if (handler != null) {
            return handler.handlerClass() + "::" + handler.method();
        }

        return "(c, r) -> c.get(" + controllerFqn + ".class)." + methodName + "(c, r)";
    }

    /**
     * Build the {@code Supplier<RouteContract>} expression stored as a route's value, e.g. {@code
     * () -> new Route("/pkg.Greeter/SayHello", (c, r) -> c.get(pkg.Greeter.class).sayHello(c, r))}.
     * Fully qualified names are used so the generated file needs no extra imports. Middleware is
     * emitted pre-classified, so the cached route needs no stage discovery at runtime.
     */
    private Expression buildRouteValue(
            String fullMethod,
            String handlerExpression,
            String requestType,
            String responseType,
            boolean clientStreaming,
            boolean serverStreaming,
            Map<Stage, List<String>> middleware) {
        StringBuilder supplier =
                new StringBuilder(
                        "() -> new io.valkyrja.grpc.routing.data.Route(\""
                                + escapeJava(fullMethod)
                                + "\", "
                                + handlerExpression
                                + ")");
        if (!requestType.isEmpty()) {
            supplier.append(".withRequestType(").append(requestType).append(".class)");
        }
        if (!responseType.isEmpty()) {
            supplier.append(".withResponseType(").append(responseType).append(".class)");
        }
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
