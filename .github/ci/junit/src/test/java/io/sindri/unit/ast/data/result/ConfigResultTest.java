/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast.data.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import io.sindri.ast.data.result.ConfigResult;
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
