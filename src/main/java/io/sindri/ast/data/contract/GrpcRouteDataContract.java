/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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

    List<String> responseSentMiddleware();
}
