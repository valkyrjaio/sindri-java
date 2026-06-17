/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import io.sindri.ast.data.result.ListenerProviderResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListenerProviderReaderTest {

    private final ListenerProviderReader reader = new ListenerProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Classes/" + relative).getPath();
    }

    @Test
    void readFile_parsesListenerCount() {
        ListenerProviderResult result = reader.readFile(fixturePath("Event/Provider/TestListenerProviderClass.java"));

        assertEquals(1, result.listenerClasses().size());
        assertEquals(1, result.listeners().size());
    }

    @Test
    void readFile_parsesListenerClass() {
        ListenerProviderResult result = reader.readFile(fixturePath("Event/Provider/TestListenerProviderClass.java"));

        assertEquals("io.valkyrja.event.data.Listener", result.listenerClasses().get(0));
    }

    @Test
    void readFile_noGetListenersMethod_returnsEmpty() {
        ListenerProviderResult result = reader.readFile(fixturePath("Event/Provider/TestEmptyListenerProviderClass.java"));

        assertTrue(result.listenerClasses().isEmpty());
        assertTrue(result.listeners().isEmpty());
    }

    @Test
    void readFile_getListenersThrows_returnsEmpty() {
        ListenerProviderResult result = reader.readFile(fixturePath("Event/Provider/TestNoReturnListenerProviderClass.java"));

        assertTrue(result.listenerClasses().isEmpty());
        assertTrue(result.listeners().isEmpty());
    }
}
