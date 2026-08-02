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
import io.sindri.ast.data.result.RouteProviderResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test the {@link RouteProviderResult}. */
final class RouteProviderResultTest {

    @Test
    void noArgConstructorIsEmpty() {
        assertTrue(new RouteProviderResult().controllerClasses().isEmpty());
    }

    @Test
    void mergeUnionsControllersAndConcatenatesRoutes() {
        var a = new RouteProviderResult(List.of("A"), List.of(new NameExpr("x")));
        var b = new RouteProviderResult(List.of("A", "B"), List.of(new NameExpr("y")));

        var merged = a.merge(b);

        assertEquals(List.of("A", "B"), merged.controllerClasses());
        assertEquals(2, merged.routes().size());
    }
}
