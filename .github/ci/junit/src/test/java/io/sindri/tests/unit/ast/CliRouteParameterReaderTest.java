/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.CliRouteParameterReader;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link CliRouteParameterReader}. */
final class CliRouteParameterReaderTest {

    @Test
    void returnsEmptyArgumentsAndOptions() {
        var reader = new CliRouteParameterReader();
        var method = new MethodDeclaration();

        assertTrue(reader.updateArguments(method, Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateOptions(method, Map.of(), "pkg").isEmpty());
    }
}
