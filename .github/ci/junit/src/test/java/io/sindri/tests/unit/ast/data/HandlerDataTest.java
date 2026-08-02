/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sindri.ast.data.HandlerData;
import org.junit.jupiter.api.Test;

/** Test the {@link HandlerData}. */
final class HandlerDataTest {

    @Test
    void exposesHandlerClassAndMethod() {
        var data = new HandlerData("App\\Controller", "index");

        assertEquals("App\\Controller", data.handlerClass());
        assertEquals("index", data.method());
    }
}
