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
import io.sindri.ast.contract.ComponentProviderReaderContract;
import io.sindri.ast.data.result.ComponentProviderResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComponentProviderReader extends AstReader implements ComponentProviderReaderContract {

    @Override
    public ComponentProviderResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        Map<String, String> importMap = buildImportMap(cu);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, MethodDeclaration> methods = indexMethods(type);

        List<String> componentProviders =
                extractObjectCreationFqns(methods, "getComponentProviders", importMap, pkg);
        List<String> serviceProviders =
                extractObjectCreationFqns(methods, "getContainerProviders", importMap, pkg);
        List<String> listenerProviders =
                extractObjectCreationFqns(methods, "getEventProviders", importMap, pkg);
        List<String> cliRouteProviders =
                extractObjectCreationFqns(methods, "getCliProviders", importMap, pkg);
        List<String> httpRouteProviders =
                extractObjectCreationFqns(methods, "getHttpProviders", importMap, pkg);

        return new ComponentProviderResult(
                componentProviders,
                serviceProviders,
                listenerProviders,
                cliRouteProviders,
                httpRouteProviders);
    }

    private List<String> extractObjectCreationFqns(
            Map<String, MethodDeclaration> methods,
            String methodName,
            Map<String, String> importMap,
            String pkg) {
        List<String> fqns = new ArrayList<>();
        MethodDeclaration method = methods.get(methodName);
        if (method == null) {
            return fqns;
        }
        Expression returnExpr = findReturnExpr(method).orElse(null);
        if (returnExpr == null) {
            return fqns;
        }
        List<Expression> items = extractListOfItems(returnExpr);
        for (Expression item : items) {
            if (item.isObjectCreationExpr()) {
                String fqn = extractObjectCreationFqn(item, importMap, pkg);
                if (!fqn.isEmpty()) {
                    fqns.add(fqn);
                }
            }
        }
        return fqns;
    }
}
