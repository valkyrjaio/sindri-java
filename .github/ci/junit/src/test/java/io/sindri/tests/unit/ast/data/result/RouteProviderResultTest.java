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
        assertTrue(new RouteProviderResult().routes().isEmpty());
        assertTrue(new RouteProviderResult().chainedRoutes().isEmpty());
    }

    @Test
    void mergeUnionsControllersAndConcatenatesRoutes() {
        var a =
                new RouteProviderResult(
                        List.of("A"), List.of(new NameExpr("x")), List.of(new NameExpr("cx")));
        var b =
                new RouteProviderResult(
                        List.of("A", "B"), List.of(new NameExpr("y")), List.of(new NameExpr("cy")));

        var merged = a.merge(b);

        assertEquals(List.of("A", "B"), merged.controllerClasses());
        assertEquals(List.of(new NameExpr("x"), new NameExpr("y")), merged.routes());
        assertEquals(List.of(new NameExpr("cx"), new NameExpr("cy")), merged.chainedRoutes());
    }
}
