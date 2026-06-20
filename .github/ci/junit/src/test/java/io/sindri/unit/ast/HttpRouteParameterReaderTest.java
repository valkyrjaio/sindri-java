/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.Map;
import io.sindri.ast.HttpRouteParameterReader;
import org.junit.jupiter.api.Test;

/** Test the {@link HttpRouteParameterReader}. */
final class HttpRouteParameterReaderTest {

    @Test
    void returnsEmptyParameters() {
        assertTrue(
                new HttpRouteParameterReader()
                        .updateParameters(new MethodDeclaration(), Map.of(), "pkg")
                        .isEmpty());
    }
}
