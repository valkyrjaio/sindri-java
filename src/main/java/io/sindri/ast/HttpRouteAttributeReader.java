/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.HttpRouteAttributeReaderContract;
import io.sindri.ast.data.HandlerData;
import io.sindri.ast.data.HttpRouteData;
import io.sindri.ast.data.result.HttpRouteAttributeResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpRouteAttributeReader extends AstReader
        implements HttpRouteAttributeReaderContract {

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
                            .filter(a -> a.getNameAsString().equals("Route"))
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
                HttpRouteData data =
                        new HttpRouteData(
                                path,
                                name,
                                handler,
                                requestMethods,
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                null,
                                null,
                                false,
                                List.of());
                routeDataMap.put(name, data);
                routeMap.put(name, new NameExpr(name));
            }
        }
        return new HttpRouteAttributeResult(routeMap, routeDataMap);
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
