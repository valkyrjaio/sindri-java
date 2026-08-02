/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.contract;

import org.jspecify.annotations.Nullable;

public interface HttpParameterDataContract {

    String name();

    String regex();

    @Nullable String cast();

    boolean isOptional();

    boolean shouldCapture();
}
