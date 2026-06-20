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
