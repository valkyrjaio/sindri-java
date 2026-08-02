/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.contract;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface CliOptionParameterDataContract {

    String name();

    String description();

    String valueDisplayName();

    @Nullable String cast();

    String defaultValue();

    List<String> shortNames();

    List<String> validValues();

    String mode();

    String valueMode();
}
