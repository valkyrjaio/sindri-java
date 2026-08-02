/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.HttpRouteMiddlewareReader;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link HttpRouteMiddlewareReader}. */
final class HttpRouteMiddlewareReaderTest {

    @Test
    void allUpdatersReturnEmptyDefaults() {
        var reader = new HttpRouteMiddlewareReader();
        var method = new MethodDeclaration();

        assertTrue(reader.updateRequestMethods(method, Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateRouteMatchedMiddleware(method, Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateRouteDispatchedMiddleware(method, Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateThrowableCaughtMiddleware(method, Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateSendingResponseMiddleware(method, Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateResponseSentMiddleware(method, Map.of(), "pkg").isEmpty());
        assertEquals("", reader.updateRequestStruct(method, Map.of(), "pkg"));
        assertEquals("", reader.updateResponseStruct(method, Map.of(), "pkg"));
    }
}
