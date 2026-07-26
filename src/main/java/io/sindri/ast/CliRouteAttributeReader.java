/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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
import io.sindri.ast.contract.CliRouteAttributeReaderContract;
import io.sindri.ast.data.result.CliRouteAttributeResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CliRouteAttributeReader extends AstReader implements CliRouteAttributeReaderContract {

    /** The route-level middleware stage contracts, in {@code cli.routing.data.Route} order. */
    private static final List<String> STAGE_CONTRACTS =
            List.of(
                    "io.valkyrja.cli.middleware.contract.RouteMatchedMiddlewareContract",
                    "io.valkyrja.cli.middleware.contract.RouteDispatchedMiddlewareContract",
                    "io.valkyrja.cli.middleware.contract.ThrowableCaughtMiddlewareContract",
                    "io.valkyrja.cli.middleware.contract.ProcessExitingMiddlewareContract");

    private final MiddlewareClassifier classifier = new MiddlewareClassifier();
    private final MiddlewareClassifier.SourceResolver resolver;

    /** No-op resolver: without a way to resolve middleware sources, no middleware is classified. */
    public CliRouteAttributeReader() {
        this(fqn -> Optional.empty());
    }

    public CliRouteAttributeReader(MiddlewareClassifier.SourceResolver resolver) {
        this.resolver = resolver;
    }

    /** Classify the method's {@code @Middleware} into the stage lists, in constructor order. */
    private List<List<String>> classifyMiddleware(
            MethodDeclaration method, Map<String, String> imports, String pkg) {
        Map<String, List<String>> byContract =
                classifier.classifyMethod(
                        method, imports, pkg, resolver, java.util.Set.copyOf(STAGE_CONTRACTS));
        List<List<String>> lists = new java.util.ArrayList<>();
        for (String contract : STAGE_CONTRACTS) {
            lists.add(byContract.getOrDefault(contract, List.of()));
        }
        return lists;
    }

    /** Emit the four stage middleware lists as positional {@code List.of(...)} constructor args. */
    private String emitMiddlewareLists(List<List<String>> middleware) {
        List<String> parts = new java.util.ArrayList<>();
        for (List<String> classes : middleware) {
            parts.add(
                    classes.isEmpty()
                            ? "java.util.List.of()"
                            : "java.util.List.of(" + String.join(".class, ", classes) + ".class)");
        }
        return String.join(", ", parts);
    }

    @Override
    public CliRouteAttributeResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        Map<String, String> importMap = buildImportMap(cu);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, Expression> routeMap = new LinkedHashMap<>();
        for (MethodDeclaration method : type.getMethods()) {
            List<AnnotationExpr> routeAnnotations =
                    method.getAnnotations().stream()
                            .filter(a -> a.getNameAsString().equals("Route"))
                            .toList();

            if (routeAnnotations.isEmpty()) {
                continue;
            }

            String handlerClass = "";
            String handlerMethod = "";
            Optional<AnnotationExpr> handlerAnnotation =
                    method.getAnnotations().stream()
                            .filter(a -> a.getNameAsString().equals("RouteHandler"))
                            .findFirst();
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
                String name = "";
                String description = "";
                for (MemberValuePair pair : normalRoute.getPairs()) {
                    switch (pair.getNameAsString()) {
                        case "name" -> name = extractStringLiteral(pair.getValue());
                        case "description" -> description = extractStringLiteral(pair.getValue());
                        default -> {}
                    }
                }
                List<List<String>> middleware = classifyMiddleware(method, importMap, pkg);
                routeMap.put(
                        name,
                        buildRouteValue(
                                name, description, handlerClass, handlerMethod, middleware));
            }
        }
        return new CliRouteAttributeResult(routeMap);
    }

    /**
     * Build the {@code Supplier<RouteContract>} expression stored as a route's value, e.g. {@code
     * () -> new Route("name", "description", Handler::method)}. Fully qualified names are used so
     * the generated file needs no extra imports. (Arguments/options are a follow-up — see TODO.)
     */
    private Expression buildRouteValue(
            String name,
            String description,
            String handlerClass,
            String handlerMethod,
            List<List<String>> middleware) {
        String handlerRef = !handlerClass.isEmpty() ? handlerClass + "::" + handlerMethod : "null";

        StringBuilder supplier =
                new StringBuilder(
                        "() -> new io.valkyrja.cli.routing.data.Route(\""
                                + escapeJava(name)
                                + "\", \""
                                + escapeJava(description)
                                + "\", "
                                + handlerRef);
        // The short constructor covers a route with no middleware; any middleware needs the full
        // constructor, whose null helpText and empty arguments/options match the short defaults
        // (arguments/options are a follow-up — see TODO). Otherwise the middleware would be
        // dropped.
        if (middleware.stream().anyMatch(list -> !list.isEmpty())) {
            supplier.append(", null, ")
                    .append(emitMiddlewareLists(middleware))
                    .append(", java.util.List.of(), java.util.List.of()");
        }
        supplier.append(")");

        return StaticJavaParser.parseExpression(supplier.toString());
    }

    private String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
