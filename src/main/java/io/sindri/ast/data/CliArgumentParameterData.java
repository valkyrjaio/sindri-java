/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.CliArgumentParameterDataContract;
import org.jspecify.annotations.Nullable;

public record CliArgumentParameterData(
        String name, String description, @Nullable String cast, String mode, String valueMode)
        implements CliArgumentParameterDataContract {

    public CliArgumentParameterData(String name, String description) {
        this(name, description, null, "OPTIONAL", "DEFAULT");
    }

    public CliArgumentParameterData(
            String name, String description, String mode, String valueMode) {
        this(name, description, null, mode, valueMode);
    }
}
