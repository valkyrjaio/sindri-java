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
import io.sindri.ast.data.CliArgumentParameterData;
import io.sindri.ast.data.CliOptionParameterData;
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
    private final CliRouteParameterReader parameterReader = new CliRouteParameterReader();
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
                List<CliArgumentParameterData> arguments =
                        parameterReader.updateArguments(method, importMap, pkg);
                List<CliOptionParameterData> options =
                        parameterReader.updateOptions(method, importMap, pkg);
                routeMap.put(
                        name,
                        buildRouteValue(
                                name,
                                description,
                                handlerClass,
                                handlerMethod,
                                middleware,
                                arguments,
                                options));
            }
        }
        return new CliRouteAttributeResult(routeMap);
    }

    /**
     * Build the {@code Supplier<RouteContract>} expression stored as a route's value, e.g. {@code
     * () -> new Route("name", "description", Handler::method)}. Fully qualified names are used so
     * the generated file needs no extra imports.
     */
    private Expression buildRouteValue(
            String name,
            String description,
            String handlerClass,
            String handlerMethod,
            List<List<String>> middleware,
            List<CliArgumentParameterData> arguments,
            List<CliOptionParameterData> options) {
        String handlerRef = !handlerClass.isEmpty() ? handlerClass + "::" + handlerMethod : "null";

        StringBuilder supplier =
                new StringBuilder(
                        "() -> new io.valkyrja.cli.routing.data.Route(\""
                                + escapeJava(name)
                                + "\", \""
                                + escapeJava(description)
                                + "\", "
                                + handlerRef);
        // The short constructor covers a route with no middleware, arguments or options; anything
        // more needs the full constructor, whose null helpText matches the short default.
        // Otherwise the middleware, arguments or options would be dropped.
        if (middleware.stream().anyMatch(list -> !list.isEmpty())
                || !arguments.isEmpty()
                || !options.isEmpty()) {
            supplier.append(", null, ")
                    .append(emitMiddlewareLists(middleware))
                    .append(", ")
                    .append(emitArguments(arguments))
                    .append(", ")
                    .append(emitOptions(options));
        }
        supplier.append(")");

        return StaticJavaParser.parseExpression(supplier.toString());
    }

    /** Emit the argument parameters a command declares. */
    private String emitArguments(List<CliArgumentParameterData> arguments) {
        StringBuilder sb = new StringBuilder("java.util.List.of(");

        for (int i = 0; i < arguments.size(); i++) {
            CliArgumentParameterData argument = arguments.get(i);
            sb.append("new io.valkyrja.cli.routing.data.ArgumentParameter(\"")
                    .append(escapeJava(argument.name()))
                    .append("\", \"")
                    .append(escapeJava(argument.description()))
                    .append("\", io.valkyrja.cli.routing.enum_.ArgumentMode.")
                    .append(argument.mode())
                    .append(", io.valkyrja.cli.routing.enum_.ArgumentValueMode.")
                    .append(argument.valueMode())
                    .append(", java.util.List.of())");

            if (i < arguments.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.append(")").toString();
    }

    /** Emit the option parameters a command declares. */
    private String emitOptions(List<CliOptionParameterData> options) {
        StringBuilder sb = new StringBuilder("java.util.List.of(");

        for (int i = 0; i < options.size(); i++) {
            CliOptionParameterData option = options.get(i);
            sb.append("new io.valkyrja.cli.routing.data.OptionParameter(\"")
                    .append(escapeJava(option.name()))
                    .append("\", \"")
                    .append(escapeJava(option.description()))
                    .append("\", \"")
                    .append(escapeJava(option.valueDisplayName()))
                    .append("\", \"")
                    .append(escapeJava(option.defaultValue()))
                    .append("\", ")
                    .append(emitStringList(option.shortNames()))
                    .append(", ")
                    .append(emitStringList(option.validValues()))
                    .append(", java.util.List.of(), io.valkyrja.cli.routing.enum_.OptionMode.")
                    .append(option.mode())
                    .append(", io.valkyrja.cli.routing.enum_.OptionValueMode.")
                    .append(option.valueMode())
                    .append(")");

            if (i < options.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.append(")").toString();
    }

    /** Emit a list of string literals. */
    private String emitStringList(List<String> values) {
        StringBuilder sb = new StringBuilder("java.util.List.of(");

        for (int i = 0; i < values.size(); i++) {
            sb.append("\"").append(escapeJava(values.get(i))).append("\"");

            if (i < values.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.append(")").toString();
    }

    private String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
