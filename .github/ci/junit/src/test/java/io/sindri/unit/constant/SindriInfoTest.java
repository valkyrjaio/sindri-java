/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.sindri.constant.SindriInfo;
import org.junit.jupiter.api.Test;

/** Test the {@link SindriInfo} constant holder. */
final class SindriInfoTest {

    @Test
    void exposesConstants() {
        assertNotNull(SindriInfo.VERSION);
        assertNotNull(new SindriInfo());
    }
}
