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
import static org.junit.jupiter.api.Assertions.assertNull;

import io.sindri.ast.data.CliArgumentParameterData;
import org.junit.jupiter.api.Test;

/** Test the {@link CliArgumentParameterData}. */
final class CliArgumentParameterDataTest {

    @Test
    void shortConstructorAppliesDefaults() {
        var data = new CliArgumentParameterData("name", "desc");

        assertEquals("name", data.name());
        assertEquals("desc", data.description());
        assertNull(data.cast());
        assertEquals("OPTIONAL", data.mode());
        assertEquals("DEFAULT", data.valueMode());
    }

    @Test
    void modeConstructorOverridesModes() {
        var data = new CliArgumentParameterData("n", "d", "REQUIRED", "ARRAY");

        assertEquals("REQUIRED", data.mode());
        assertEquals("ARRAY", data.valueMode());
    }
}
