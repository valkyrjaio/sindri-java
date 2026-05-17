/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.CliRouteDataContract;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record CliRouteData(
        String name,
        String description,
        @Nullable HandlerData handler,
        @Nullable HandlerData helpText,
        List<String> routeMatchedMiddleware,
        List<String> routeDispatchedMiddleware,
        List<String> throwableCaughtMiddleware,
        List<String> exitedMiddleware,
        List<CliArgumentParameterData> arguments,
        List<CliOptionParameterData> options)
        implements CliRouteDataContract {

    public CliRouteData(String name, String description) {
        this(
                name,
                description,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }
}
