/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast.data;

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
