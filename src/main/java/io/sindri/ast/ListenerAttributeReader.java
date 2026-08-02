/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast;

import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.ListenerAttributeReaderContract;
import io.sindri.ast.data.result.ListenerAttributeResult;
import java.util.Map;

public class ListenerAttributeReader extends AstReader implements ListenerAttributeReaderContract {

    @Override
    public ListenerAttributeResult readFile(String filePath) {
        return new ListenerAttributeResult(Map.of());
    }
}
