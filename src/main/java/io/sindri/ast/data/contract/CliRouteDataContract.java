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

public interface CliRouteDataContract {

    String name();

    String description();

    @Nullable HandlerDataContract handler();

    @Nullable HandlerDataContract helpText();

    List<String> routeMatchedMiddleware();

    List<String> routeDispatchedMiddleware();

    List<String> throwableCaughtMiddleware();

    List<String> processExitingMiddleware();

    List<? extends CliArgumentParameterDataContract> arguments();

    List<? extends CliOptionParameterDataContract> options();
}
