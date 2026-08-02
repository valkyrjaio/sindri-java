/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast.data.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.data.result.ConfigResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test the {@link ConfigResult}. */
final class ConfigResultTest {

    @Test
    void canonicalConstructorExposesFields() {
        var result = new ConfigResult("ns", "dir", "data", "dataNs", List.of("P"));

        assertEquals("ns", result.namespace());
        assertEquals("dir", result.dir());
        assertEquals("data", result.dataPath());
        assertEquals("dataNs", result.dataNamespace());
        assertEquals(List.of("P"), result.providers());
    }

    @Test
    void noArgConstructorIsEmpty() {
        assertTrue(new ConfigResult().providers().isEmpty());
    }
}
