/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast;
import io.sindri.ast.*;

import io.sindri.ast.data.result.CliRouteAttributeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CliRouteAttributeReaderTest {

    private final CliRouteAttributeReader reader = new CliRouteAttributeReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Classes/" + relative).getPath();
    }

    @Test
    void readFile_parsesRouteCount() {
        CliRouteAttributeResult result = reader.readFile(fixturePath("Cli/Controller/TestCliControllerClass.java"));

        assertEquals(1, result.routes().size());
    }

    @Test
    void readFile_parsesRouteName() {
        CliRouteAttributeResult result = reader.readFile(fixturePath("Cli/Controller/TestCliControllerClass.java"));

        assertTrue(result.routes().containsKey("greet"));
    }

    @Test
    void readFile_withNoRouteMethod_returnsEmpty() {
        CliRouteAttributeResult result = reader.readFile(fixturePath("Cli/Controller/TestNoRouteCliControllerClass.java"));

        assertTrue(result.routes().isEmpty());
    }

    @Test
    void readFile_markerRoute_isSkipped() {
        CliRouteAttributeResult result =
                reader.readFile(fixturePath("Cli/Controller/TestEdgeCliControllerClass.java"));

        assertTrue(result.routes().isEmpty());
    }

}
