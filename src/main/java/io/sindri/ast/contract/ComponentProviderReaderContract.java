/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.contract;

import io.sindri.ast.data.result.ComponentProviderResult;

public interface ComponentProviderReaderContract {

    ComponentProviderResult readFile(String filePath);
}
