/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.contract;

import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.List;
import java.util.Map;

public interface HttpRouteMiddlewareReaderContract {

    List<String> updateRequestMethods(
            MethodDeclaration method, Map<String, String> importMap, String pkg);

    List<String> updateRouteMatchedMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg);

    List<String> updateRouteDispatchedMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg);

    List<String> updateThrowableCaughtMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg);

    List<String> updateSendingResponseMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg);

    List<String> updateResponseSentMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg);

    String updateRequestStruct(MethodDeclaration method, Map<String, String> importMap, String pkg);

    String updateResponseStruct(
            MethodDeclaration method, Map<String, String> importMap, String pkg);
}
