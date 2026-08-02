/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.ConfigDataContract;
import java.util.List;

public record ConfigData(
        String namespace, String dir, String dataPath, String dataNamespace, List<String> providers)
        implements ConfigDataContract {

    public ConfigData(String namespace, String dir, String dataPath, String dataNamespace) {
        this(namespace, dir, dataPath, dataNamespace, List.of());
    }
}
