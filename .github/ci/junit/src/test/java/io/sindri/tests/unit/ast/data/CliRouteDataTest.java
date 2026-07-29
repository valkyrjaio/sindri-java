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
