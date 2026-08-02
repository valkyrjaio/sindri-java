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
import io.sindri.ast.data.result.ListenerAttributeResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link ListenerAttributeResult}. */
final class ListenerAttributeResultTest {

    @Test
    void exposesListenersAndDefaultsEmpty() {
        var result = new ListenerAttributeResult(Map.of("l", new NameExpr("x")));

        assertEquals(1, result.listeners().size());
        assertTrue(new ListenerAttributeResult().listeners().isEmpty());
    }
}
