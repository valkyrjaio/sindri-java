/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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
