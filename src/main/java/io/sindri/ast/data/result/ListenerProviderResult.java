/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.result;

import com.github.javaparser.ast.expr.Expression;
import io.sindri.ast.data.ListenerData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;

public record ListenerProviderResult(
        List<String> listenerClasses,
        Map<String, Expression> listeners,
        Map<String, ListenerData> listenerData) {

    public ListenerProviderResult() {
        this(List.of(), Map.of(), Map.of());
    }

    public ListenerProviderResult merge(ListenerProviderResult other) {
        SequencedSet<String> classes = new LinkedHashSet<>(listenerClasses);
        classes.addAll(other.listenerClasses);

        Map<String, Expression> mergedListeners = new LinkedHashMap<>(listeners);
        mergedListeners.putAll(other.listeners);

        Map<String, ListenerData> mergedData = new LinkedHashMap<>(listenerData);
        mergedData.putAll(other.listenerData);

        return new ListenerProviderResult(new ArrayList<>(classes), mergedListeners, mergedData);
    }
}
