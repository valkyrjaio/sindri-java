/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast.data.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.expr.NameExpr;
import java.util.Map;
import io.sindri.ast.data.result.CliRouteAttributeResult;
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
