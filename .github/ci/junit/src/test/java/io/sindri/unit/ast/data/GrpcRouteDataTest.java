/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.data.GrpcRouteData;
import org.junit.jupiter.api.Test;

/** Test the {@link GrpcRouteData}. */
final class GrpcRouteDataTest {

    @Test
    void shortConstructorAppliesDefaults() {
        var data = new GrpcRouteData("/pkg.Greeter/SayHello", "pkg.Greeter", "SayHello");

        assertEquals("/pkg.Greeter/SayHello", data.method());
        assertEquals("pkg.Greeter", data.service());
        assertEquals("SayHello", data.methodName());
        assertNull(data.handler());
        assertFalse(data.clientStreaming());
        assertFalse(data.serverStreaming());
        assertTrue(data.routeMatchedMiddleware().isEmpty());
        assertTrue(data.responseSentMiddleware().isEmpty());
    }
}
