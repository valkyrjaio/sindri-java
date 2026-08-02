/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.data.CliRouteData;
import org.junit.jupiter.api.Test;

/** Test the {@link CliRouteData}. */
final class CliRouteDataTest {

    @Test
    void shortConstructorAppliesDefaults() {
        var data = new CliRouteData("name", "desc");

        assertEquals("name", data.name());
        assertEquals("desc", data.description());
        assertNull(data.handler());
        assertTrue(data.arguments().isEmpty());
        assertTrue(data.options().isEmpty());
    }
}
