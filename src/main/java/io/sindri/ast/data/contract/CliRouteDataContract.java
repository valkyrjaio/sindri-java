/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data.contract;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface CliRouteDataContract {

    String name();

    String description();

    @Nullable HandlerDataContract handler();

    @Nullable HandlerDataContract helpText();

    List<String> routeMatchedMiddleware();

    List<String> routeDispatchedMiddleware();

    List<String> throwableCaughtMiddleware();

    List<String> exitedMiddleware();

    List<? extends CliArgumentParameterDataContract> arguments();

    List<? extends CliOptionParameterDataContract> options();
}
