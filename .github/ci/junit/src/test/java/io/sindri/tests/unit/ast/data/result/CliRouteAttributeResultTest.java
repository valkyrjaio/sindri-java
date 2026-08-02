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
import io.sindri.ast.data.result.CliRouteAttributeResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link CliRouteAttributeResult}. */
final class CliRouteAttributeResultTest {

    @Test
    void exposesRoutesAndDefaultsEmpty() {
        var result = new CliRouteAttributeResult(Map.of("r", new NameExpr("x")));

        assertEquals(1, result.routes().size());
        assertTrue(new CliRouteAttributeResult().routes().isEmpty());
    }
}
