/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.data.HttpParameterData;
import org.junit.jupiter.api.Test;

/** Test the {@link HttpParameterData}. */
final class HttpParameterDataTest {

    @Test
    void shortConstructorAppliesDefaults() {
        var data = new HttpParameterData("id", "\\d+");

        assertEquals("id", data.name());
        assertEquals("\\d+", data.regex());
        assertNull(data.cast());
        assertFalse(data.isOptional());
        assertTrue(data.shouldCapture());
    }
}
