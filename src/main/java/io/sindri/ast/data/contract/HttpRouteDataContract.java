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

public interface HttpRouteDataContract {

    String path();

    String name();

    @Nullable HandlerDataContract handler();

    List<String> requestMethods();

    List<String> routeMatchedMiddleware();

    List<String> routeDispatchedMiddleware();

    List<String> throwableCaughtMiddleware();

    List<String> sendingResponseMiddleware();

    List<String> responseSentMiddleware();

    @Nullable String requestStruct();

    @Nullable String responseStruct();

    boolean isDynamic();

    List<? extends HttpParameterDataContract> parameters();

    String regex();
}
