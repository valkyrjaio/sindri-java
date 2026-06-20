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
import static org.junit.jupiter.api.Assertions.assertNull;
import io.sindri.ast.data.ListenerData;
import org.junit.jupiter.api.Test;

/** Test the {@link ListenerData}. */
final class ListenerDataTest {

    @Test
    void shortConstructorDefaultsHandlerToNull() {
        var data = new ListenerData("event.id", "name");

        assertEquals("event.id", data.eventId());
        assertEquals("name", data.name());
        assertNull(data.handler());
    }
}
