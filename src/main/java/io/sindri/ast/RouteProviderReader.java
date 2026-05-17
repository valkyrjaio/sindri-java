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
import com.github.javaparser.ast.expr.Expression;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.RouteProviderReaderContract;
import io.sindri.ast.data.result.RouteProviderResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        MethodDeclaration method = methods.get("getControllerClasses");
        if (method == null) {
            return new RouteProviderResult();
        }

        Expression returnExpr = findReturnExpr(method).orElse(null);
        if (returnExpr == null) {
            return new RouteProviderResult();
        }

        List<String> controllerClasses = new ArrayList<>();
        List<Expression> routes = new ArrayList<>();
        for (Expression item : extractListOfItems(returnExpr)) {
            if (item.isClassExpr()) {
                String fqn = extractClassExprFqn(item, importMap, pkg);
                if (!fqn.isEmpty()) {
                    controllerClasses.add(fqn);
                    routes.add(item);
                }
            }
        }
        return new RouteProviderResult(controllerClasses, routes);
    }
}
