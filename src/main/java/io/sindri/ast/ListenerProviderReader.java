/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.ListenerProviderReaderContract;
import io.sindri.ast.data.result.ListenerProviderResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListenerProviderReader extends AstReader implements ListenerProviderReaderContract {

    @Override
    public ListenerProviderResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        Map<String, String> importMap = buildImportMap(cu);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, MethodDeclaration> methods = indexMethods(type);
        MethodDeclaration method = methods.get("getListeners");
        if (method == null) {
            return new ListenerProviderResult();
        }

        Expression returnExpr = findReturnExpr(method).orElse(null);
        if (returnExpr == null) {
            return new ListenerProviderResult();
        }

        List<String> listenerClasses = new ArrayList<>();
        List<Expression> listeners = new ArrayList<>();
        for (Expression item : extractListOfItems(returnExpr)) {
            if (item.isObjectCreationExpr()) {
                String fqn = extractObjectCreationFqn(item, importMap, pkg);
                if (!fqn.isEmpty()) {
                    listenerClasses.add(fqn);
                }
                listeners.add(item);
            }
        }
        return new ListenerProviderResult(listenerClasses, listeners);
    }
}
