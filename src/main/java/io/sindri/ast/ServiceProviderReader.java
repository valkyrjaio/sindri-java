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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.ServiceProviderReaderContract;
import io.sindri.ast.data.result.ServiceProviderResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceProviderReader extends AstReader implements ServiceProviderReaderContract {

    /**
     * Reads the publishers() method from a ServiceProvider file.
     *
     * @return result with serviceClasses list and map of serviceFqn -> [providerFqn, methodName]
     */
    @Override
    public ServiceProviderResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        Map<String, String> importMap = buildImportMap(cu);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, MethodDeclaration> methods = indexMethods(type);
        MethodDeclaration publishersMethod = methods.get("publishers");
        if (publishersMethod == null) {
            return new ServiceProviderResult();
        }

        Expression returnExpr = findReturnExpr(publishersMethod).orElse(null);
        if (returnExpr == null) {
            return new ServiceProviderResult();
        }

        Map<String, String[]> publishers = extractPublishers(returnExpr, importMap, pkg);
        List<String> serviceClasses = new ArrayList<>(publishers.keySet());

        return new ServiceProviderResult(serviceClasses, publishers);
    }

    private Map<String, String[]> extractPublishers(
            Expression expr, Map<String, String> importMap, String pkg) {
        Map<String, String[]> result = new LinkedHashMap<>();
        if (!expr.isMethodCallExpr()) {
            return result;
        }
        MethodCallExpr call = expr.asMethodCallExpr();
        String callName = call.getNameAsString();
        String scope = call.getScope().map(Expression::toString).orElse("");

        if (scope.equals("Map") && callName.equals("of")) {
            extractFromMapOf(call.getArguments(), importMap, pkg, result);
        } else if (scope.equals("Map") && callName.equals("ofEntries")) {
            extractFromMapOfEntries(call.getArguments(), importMap, pkg, result);
        }

        return result;
    }

    private void extractFromMapOf(
            NodeList<Expression> args,
            Map<String, String> importMap,
            String pkg,
            Map<String, String[]> result) {
        for (int i = 0; i + 1 < args.size(); i += 2) {
            Expression keyExpr = args.get(i);
            Expression valueExpr = args.get(i + 1);
            processEntry(keyExpr, valueExpr, importMap, pkg, result);
        }
    }

    private void extractFromMapOfEntries(
            NodeList<Expression> args,
            Map<String, String> importMap,
            String pkg,
            Map<String, String[]> result) {
        for (Expression arg : args) {
            if (!arg.isMethodCallExpr()) {
                continue;
            }
            MethodCallExpr entryCall = arg.asMethodCallExpr();
            if (!entryCall.getNameAsString().equals("entry")) {
                continue;
            }
            NodeList<Expression> entryArgs = entryCall.getArguments();
            if (entryArgs.size() < 2) {
                continue;
            }
            processEntry(entryArgs.get(0), entryArgs.get(1), importMap, pkg, result);
        }
    }

    private void processEntry(
            Expression keyExpr,
            Expression valueExpr,
            Map<String, String> importMap,
            String pkg,
            Map<String, String[]> result) {
        String serviceFqn = extractClassExprFqn(keyExpr, importMap, pkg);
        if (serviceFqn.isEmpty()) {
            return;
        }
        if (!valueExpr.isMethodReferenceExpr()) {
            return;
        }
        MethodReferenceExpr methodRef = valueExpr.asMethodReferenceExpr();
        String scopeName = methodRef.getScope().toString();
        String methodName = methodRef.getIdentifier();
        String providerFqn = resolveClassName(scopeName, importMap, pkg);
        result.put(serviceFqn, new String[] {providerFqn, methodName});
    }
}
