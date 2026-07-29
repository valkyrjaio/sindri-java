/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.generator.enum_;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.sindri.generator.enum_.GenerateStatus;
import org.junit.jupiter.api.Test;

/** Test the {@link GenerateStatus}. */
final class GenerateStatusTest {

    @Test
    void exposesAllConstants() {
        assertEquals(3, GenerateStatus.values().length);
        assertSame(GenerateStatus.SUCCESS, GenerateStatus.valueOf("SUCCESS"));
        assertSame(GenerateStatus.FAILURE, GenerateStatus.valueOf("FAILURE"));
        assertSame(GenerateStatus.SKIPPED, GenerateStatus.valueOf("SKIPPED"));
    }
}
