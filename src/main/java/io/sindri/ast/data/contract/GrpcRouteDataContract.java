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

public interface GrpcRouteDataContract {

    String method();

    String service();

    String methodName();

    @Nullable HandlerDataContract handler();

    boolean clientStreaming();

    boolean serverStreaming();

    List<String> routeMatchedMiddleware();

    List<String> routeDispatchedMiddleware();

    List<String> throwableCaughtMiddleware();

    List<String> sendingResponseMiddleware();

    List<String> terminatedMiddleware();
}
