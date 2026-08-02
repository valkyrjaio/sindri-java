/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.result;

import com.github.javaparser.ast.expr.Expression;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

public record ListenerProviderResult(List<String> listenerClasses, List<Expression> listeners) {

    public ListenerProviderResult() {
        this(List.of(), List.of());
    }

    public ListenerProviderResult merge(ListenerProviderResult other) {
        SequencedSet<String> classes = new LinkedHashSet<>(listenerClasses);
        classes.addAll(other.listenerClasses);

        List<Expression> merged = new ArrayList<>(listeners);
        merged.addAll(other.listeners);

        return new ListenerProviderResult(new ArrayList<>(classes), merged);
    }
}
