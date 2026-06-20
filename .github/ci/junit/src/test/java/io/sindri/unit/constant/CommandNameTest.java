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

import io.sindri.constant.CommandName;
import org.junit.jupiter.api.Test;

/** Test the {@link CommandName} constant holder. */
final class CommandNameTest {

    @Test
    void exposesConstants() {
        assertEquals("data:generate", CommandName.DATA_GENERATE);
        assertNotNull(new CommandName());
    }
}
