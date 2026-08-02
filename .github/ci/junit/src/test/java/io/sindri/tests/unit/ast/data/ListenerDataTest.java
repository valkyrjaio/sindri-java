/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast.data;

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
