/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.contract;

import java.util.List;

public interface ConfigDataContract {

    String namespace();

    String dir();

    String dataPath();

    String dataNamespace();

    List<String> providers();
}
