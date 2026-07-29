/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.*;
import io.sindri.ast.data.result.ListenerProviderResult;
import org.junit.jupiter.api.Test;

public final class ListenerProviderReaderTest {

    private final ListenerProviderReader reader = new ListenerProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesListenerCount() {
        ListenerProviderResult result =
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderClass.java"));

        assertEquals(1, result.listenerClasses().size());
        assertEquals(1, result.listeners().size());
    }

    @Test
    void readFile_parsesListenerClass() {
        ListenerProviderResult result =
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderClass.java"));

        assertEquals("io.valkyrja.event.data.Listener", result.listenerClasses().get(0));
    }

    @Test
    void readFile_noGetListenersMethod_returnsEmpty() {
        ListenerProviderResult result =
                reader.readFile(fixturePath("Event/Provider/TestEmptyListenerProviderClass.java"));

        assertTrue(result.listenerClasses().isEmpty());
        assertTrue(result.listeners().isEmpty());
    }

    @Test
    void readFile_getListenersThrows_returnsEmpty() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestNoReturnListenerProviderClass.java"));

        assertTrue(result.listenerClasses().isEmpty());
        assertTrue(result.listeners().isEmpty());
    }

    @Test
    void readFile_emptyFqn_isSkipped() {
        var reader =
                new ListenerProviderReader() {
                    @Override
                    protected String extractObjectCreationFqn(
                            com.github.javaparser.ast.expr.Expression expr,
                            java.util.Map<String, String> importMap,
                            String pkg) {
                        return "";
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderClass.java"))
                        .listenerClasses()
                        .isEmpty());
    }

    @Test
    void readFile_nonObjectCreationItem_isSkipped() {
        var reader =
                new ListenerProviderReader() {
                    @Override
                    protected java.util.List<com.github.javaparser.ast.expr.Expression>
                            extractListOfItems(com.github.javaparser.ast.expr.Expression expr) {
                        return java.util.List.of(new com.github.javaparser.ast.expr.NameExpr("x"));
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderClass.java"))
                        .listenerClasses()
                        .isEmpty());
    }

    @Test
    void readFile_noTypeDeclaration_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        reader.readFile(
                                fixturePath("Event/Provider/TestNoTypeListenerProviderFile.java")));
    }
}
