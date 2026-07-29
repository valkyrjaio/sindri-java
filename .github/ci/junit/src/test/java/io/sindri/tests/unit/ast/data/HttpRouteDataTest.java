/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.data.HttpRouteData;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test the {@link HttpRouteData}. */
final class HttpRouteDataTest {

    @Test
    void shortConstructorAppliesDefaults() {
        var data = new HttpRouteData("/path", "name");

        assertEquals("/path", data.path());
        assertEquals("name", data.name());
        assertNull(data.handler());
        assertFalse(data.isDynamic());
        assertTrue(data.requestMethods().isEmpty());
    }

    @Test
    void requestMethodsConstructorKeepsMethods() {
        var data = new HttpRouteData("/p", "n", List.of("GET", "POST"));

        assertEquals(List.of("GET", "POST"), data.requestMethods());
    }
}
