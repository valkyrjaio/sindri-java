/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;

public record ServiceProviderResult(List<String> serviceClasses, Map<String, String[]> publishers) {

    public ServiceProviderResult() {
        this(List.of(), Map.of());
    }

    public ServiceProviderResult merge(ServiceProviderResult other) {
        SequencedSet<String> classes = new LinkedHashSet<>(serviceClasses);
        classes.addAll(other.serviceClasses);

        Map<String, String[]> merged = new LinkedHashMap<>(publishers);
        merged.putAll(other.publishers);

        return new ServiceProviderResult(new ArrayList<>(classes), merged);
    }
}
