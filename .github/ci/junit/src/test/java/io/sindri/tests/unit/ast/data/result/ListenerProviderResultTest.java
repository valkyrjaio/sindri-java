/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast.data.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.expr.NameExpr;
import io.sindri.ast.data.result.ListenerProviderResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link ListenerProviderResult}. */
final class ListenerProviderResultTest {

    @Test
    void noArgConstructorIsEmpty() {
        assertTrue(new ListenerProviderResult().listenerClasses().isEmpty());
        assertTrue(new ListenerProviderResult().listeners().isEmpty());
    }

    @Test
    void mergeUnionsClassesAndListeners() {
        var a = result("A", "x");
        var b = new ListenerProviderResult(List.of("A", "B"), Map.of());

        var merged = a.merge(b);

        assertEquals(List.of("A", "B"), merged.listenerClasses());
        assertEquals(1, merged.listeners().size());
    }

    @Test
    void mergeKeepsTheLastListenerRegisteredUnderAName() {
        var a = result("A", "x");
        var b = result("B", "x");

        var merged = a.merge(b);

        assertEquals("B", merged.listeners().get("x").toString());
    }

    private static ListenerProviderResult result(String eventId, String name) {
        return new ListenerProviderResult(List.of(eventId), Map.of(name, new NameExpr(eventId)));
    }
}
