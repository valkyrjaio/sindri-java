/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.result;

import com.github.javaparser.ast.expr.Expression;
import java.util.Map;

public record ListenerAttributeResult(Map<String, Expression> listeners) {

    public ListenerAttributeResult() {
        this(Map.of());
    }
}
