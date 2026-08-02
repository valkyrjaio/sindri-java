/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.data.CliOptionParameterData;
import org.junit.jupiter.api.Test;

/** Test the {@link CliOptionParameterData}. */
final class CliOptionParameterDataTest {

    @Test
    void shortConstructorAppliesDefaults() {
        var data = new CliOptionParameterData("name", "desc");

        assertEquals("name", data.name());
        assertEquals("", data.valueDisplayName());
        assertEquals("OPTIONAL", data.mode());
        assertTrue(data.shortNames().isEmpty());
    }
}
