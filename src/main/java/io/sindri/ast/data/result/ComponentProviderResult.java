/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.result;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

public record ComponentProviderResult(
        List<String> componentProviders,
        List<String> serviceProviders,
        List<String> listenerProviders,
        List<String> cliRouteProviders,
        List<String> httpRouteProviders,
        List<String> grpcRouteProviders) {

    public ComponentProviderResult() {
        this(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public ComponentProviderResult merge(ComponentProviderResult other) {
        return new ComponentProviderResult(
                mergeUnique(componentProviders, other.componentProviders),
                mergeUnique(serviceProviders, other.serviceProviders),
                mergeUnique(listenerProviders, other.listenerProviders),
                mergeUnique(cliRouteProviders, other.cliRouteProviders),
                mergeUnique(httpRouteProviders, other.httpRouteProviders),
                mergeUnique(grpcRouteProviders, other.grpcRouteProviders));
    }

    private static List<String> mergeUnique(List<String> a, List<String> b) {
        SequencedSet<String> set = new LinkedHashSet<>(a);
        set.addAll(b);
        return new ArrayList<>(set);
    }
}
