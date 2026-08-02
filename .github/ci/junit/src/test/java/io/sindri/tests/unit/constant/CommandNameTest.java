/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.sindri.constant.CommandName;
import org.junit.jupiter.api.Test;

/** Test the {@link CommandName} constant holder. */
final class CommandNameTest {

    @Test
    void exposesConstants() {
        assertEquals("data:generate", CommandName.DATA_GENERATE);
        assertNotNull(new CommandName());
    }
}
