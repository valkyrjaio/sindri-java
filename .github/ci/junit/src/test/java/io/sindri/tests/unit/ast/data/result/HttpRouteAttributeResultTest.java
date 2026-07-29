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
import io.sindri.ast.data.HttpRouteData;
import io.sindri.ast.data.result.HttpRouteAttributeResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link HttpRouteAttributeResult}. */
final class HttpRouteAttributeResultTest {

    @Test
    void exposesRoutesAndRouteDataAndDefaultsEmpty() {
        var result =
                new HttpRouteAttributeResult(
                        Map.of("r", new NameExpr("x")), Map.of("r", new HttpRouteData("/p", "n")));

        assertEquals(1, result.routes().size());
        assertEquals(1, result.routeData().size());
        assertTrue(new HttpRouteAttributeResult().routes().isEmpty());
    }
}
