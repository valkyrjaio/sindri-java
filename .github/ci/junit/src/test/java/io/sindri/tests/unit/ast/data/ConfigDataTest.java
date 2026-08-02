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

import io.sindri.ast.data.ConfigData;
import org.junit.jupiter.api.Test;

/** Test the {@link ConfigData}. */
final class ConfigDataTest {

    @Test
    void shortConstructorDefaultsProvidersToEmpty() {
        var data = new ConfigData("ns", "dir", "dataPath", "dataNs");

        assertEquals("ns", data.namespace());
        assertEquals("dir", data.dir());
        assertEquals("dataPath", data.dataPath());
        assertEquals("dataNs", data.dataNamespace());
        assertTrue(data.providers().isEmpty());
    }
}
