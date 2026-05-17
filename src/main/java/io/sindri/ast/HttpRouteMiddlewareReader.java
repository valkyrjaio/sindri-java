/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.HttpRouteMiddlewareReaderContract;
import java.util.List;
import java.util.Map;

public class HttpRouteMiddlewareReader extends AstReader
        implements HttpRouteMiddlewareReaderContract {

    @Override
    public List<String> updateRequestMethods(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }

    @Override
    public List<String> updateRouteMatchedMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }

    @Override
    public List<String> updateRouteDispatchedMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }

    @Override
    public List<String> updateThrowableCaughtMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }

    @Override
    public List<String> updateSendingResponseMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }

    @Override
    public List<String> updateTerminatedMiddleware(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }

    @Override
    public String updateRequestStruct(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return "";
    }

    @Override
    public String updateResponseStruct(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return "";
    }
}
