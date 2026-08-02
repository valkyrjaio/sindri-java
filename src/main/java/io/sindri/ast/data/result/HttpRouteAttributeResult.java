/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.result;

import com.github.javaparser.ast.expr.Expression;
import io.sindri.ast.data.HttpRouteData;
import java.util.Map;

public record HttpRouteAttributeResult(
        Map<String, Expression> routes, Map<String, HttpRouteData> routeData) {

    public HttpRouteAttributeResult() {
        this(Map.of(), Map.of());
    }
}
