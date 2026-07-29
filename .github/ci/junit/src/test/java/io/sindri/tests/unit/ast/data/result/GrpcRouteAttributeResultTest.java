/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast.data.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.expr.NameExpr;
import io.sindri.ast.data.GrpcRouteData;
import io.sindri.ast.data.result.GrpcRouteAttributeResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link GrpcRouteAttributeResult}. */
final class GrpcRouteAttributeResultTest {

    @Test
    void exposesRoutesAndRouteDataAndDefaultsEmpty() {
        var result =
                new GrpcRouteAttributeResult(
                        Map.of("/pkg.A/M", new NameExpr("x")),
                        Map.of("/pkg.A/M", new GrpcRouteData("/pkg.A/M", "pkg.A", "M")));

        assertEquals(1, result.routes().size());
        assertEquals(1, result.routeData().size());

        var empty = new GrpcRouteAttributeResult();
        assertTrue(empty.routes().isEmpty());
        assertTrue(empty.routeData().isEmpty());
    }
}
