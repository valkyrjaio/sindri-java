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
                routeMap.put(name, buildRouteValue(name, description, handlerClass, handlerMethod));
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
            String name, String description, String handlerClass, String handlerMethod) {
        String handlerRef = !handlerClass.isEmpty() ? handlerClass + "::" + handlerMethod : "null";

        String supplier =
                "() -> new io.valkyrja.cli.routing.data.Route(\""
                        + escapeJava(name)
                        + "\", \""
                        + escapeJava(description)
                        + "\", "
                        + handlerRef
                        + ")";

        return StaticJavaParser.parseExpression(supplier);
    }

    private String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
