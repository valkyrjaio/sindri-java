/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.HttpParameterDataContract;
import org.jspecify.annotations.Nullable;

public record HttpParameterData(
        String name, String regex, @Nullable String cast, boolean isOptional, boolean shouldCapture)
        implements HttpParameterDataContract {

    public HttpParameterData(String name, String regex) {
        this(name, regex, null, false, true);
    }
}
