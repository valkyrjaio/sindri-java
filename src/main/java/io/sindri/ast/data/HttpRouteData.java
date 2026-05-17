/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.HttpRouteDataContract;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record HttpRouteData(
        String path,
        String name,
        @Nullable HandlerData handler,
        List<String> requestMethods,
        List<String> routeMatchedMiddleware,
        List<String> routeDispatchedMiddleware,
        List<String> throwableCaughtMiddleware,
        List<String> sendingResponseMiddleware,
        List<String> terminatedMiddleware,
        @Nullable String requestStruct,
        @Nullable String responseStruct,
        boolean isDynamic,
        List<HttpParameterData> parameters)
        implements HttpRouteDataContract {

    public HttpRouteData(String path, String name) {
        this(
                path, name, null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, false, List.of());
    }

    public HttpRouteData(String path, String name, List<String> requestMethods) {
        this(
                path,
                name,
                null,
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
    }
}
