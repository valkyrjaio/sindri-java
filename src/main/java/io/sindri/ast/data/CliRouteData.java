/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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
        List<String> processExitingMiddleware,
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
