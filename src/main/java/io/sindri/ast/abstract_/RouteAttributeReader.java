/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.abstract_;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.data.HandlerData;
import java.util.Map;

public abstract class RouteAttributeReader extends AstReader {

    protected HandlerData updateHandler(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        String handlerClass =
                pkg.isEmpty()
                        ? method.findAncestor(ClassOrInterfaceDeclaration.class)
                                .map(c -> c.getNameAsString())
                                .orElse("")
                        : pkg
                                + "."
                                + method.findAncestor(ClassOrInterfaceDeclaration.class)
                                        .map(c -> c.getNameAsString())
                                        .orElse("");
        return new HandlerData(handlerClass, method.getNameAsString());
    }

    protected abstract String getRouteHandlerAnnotationClass();
}
