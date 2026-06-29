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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.RouteProviderReaderContract;
import io.sindri.ast.data.result.RouteProviderResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads a route provider's {@code getControllerClasses()} (annotated controller classes to scan)
 * and {@code getRoutes()} (manually-defined {@code Route}/{@code DynamicRoute} objects). The latter
 * are fully qualified against the provider's imports so they can be inlined into the generated data
 * file, letting the cached data hold every route and avoid any runtime provider iteration.
 */
public class RouteProviderReader extends AstReader implements RouteProviderReaderContract {

    @Override
    public RouteProviderResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        Map<String, String> importMap = buildImportMap(cu);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, MethodDeclaration> methods = indexMethods(type);

        List<String> controllerClasses = new ArrayList<>();
        MethodDeclaration controllerMethod = methods.get("getControllerClasses");
        if (controllerMethod != null) {
            findReturnExpr(controllerMethod)
                    .ifPresent(
                            returnExpr -> {
                                for (Expression item : extractListOfItems(returnExpr)) {
                                    if (item.isClassExpr()) {
                                        String fqn = extractClassExprFqn(item, importMap, pkg);
                                        if (!fqn.isEmpty()) {
                                            controllerClasses.add(fqn);
                                        }
                                    }
                                }
                            });
        }

        // Routes often reference the provider itself (e.g. Provider::handler), which is not in the
        // import map, so add the current class to the qualification map.
        Map<String, String> qualifyMap = new java.util.HashMap<>(importMap);
        String className = type.getNameAsString();
        qualifyMap.put(className, pkg.isEmpty() ? className : pkg + "." + className);

        List<Expression> routes = new ArrayList<>();
        MethodDeclaration routesMethod = methods.get("getRoutes");
        if (routesMethod != null) {
            findReturnExpr(routesMethod)
                    .ifPresent(
                            returnExpr -> {
                                for (Expression item : extractListOfItems(returnExpr)) {
                                    if (item.isObjectCreationExpr()) {
                                        routes.add(qualify(item, qualifyMap));
                                    }
                                }
                            });
        }

        return new RouteProviderResult(controllerClasses, routes);
    }

    /**
     * Fully qualify every simple type/name reference in an expression against the file's import
     * map, so the result is self-contained when inlined into a generated file with different
     * imports.
     */
    protected Expression qualify(Expression expr, Map<String, String> importMap) {
        Expression clone = expr.clone();

        for (NameExpr name : clone.findAll(NameExpr.class)) {
            String fqn = importMap.get(name.getNameAsString());
            if (fqn != null) {
                name.replace(StaticJavaParser.parseExpression(fqn));
            }
        }

        for (ClassOrInterfaceType simpleType : clone.findAll(ClassOrInterfaceType.class)) {
            if (simpleType.getScope().isEmpty()) {
                String fqn = importMap.get(simpleType.getNameAsString());
                if (fqn != null) {
                    simpleType.replace(StaticJavaParser.parseClassOrInterfaceType(fqn));
                }
            }
        }

        return clone;
    }
}
