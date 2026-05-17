/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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

    List<String> updateTerminatedMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg);

    String updateRequestStruct(MethodDeclaration method, Map<String, String> importMap, String pkg);

    String updateResponseStruct(
            MethodDeclaration method, Map<String, String> importMap, String pkg);
}
