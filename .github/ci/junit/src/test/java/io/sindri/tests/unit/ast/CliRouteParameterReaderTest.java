/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.CliRouteParameterReader;
import io.sindri.ast.data.CliArgumentParameterData;
import io.sindri.ast.data.CliOptionParameterData;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link CliRouteParameterReader}. */
final class CliRouteParameterReaderTest {

    private final CliRouteParameterReader reader = new CliRouteParameterReader();

    private MethodDeclaration method(String fixture, String name) throws Exception {
        var path =
                Path.of(
                        getClass()
                                .getClassLoader()
                                .getResource("Fixtures/Cli/Controller/" + fixture)
                                .toURI());

        return StaticJavaParser.parse(path).findAll(MethodDeclaration.class).stream()
                .filter(m -> m.getNameAsString().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void readsNoParametersFromAMethodThatDeclaresNone() {
        var method = new MethodDeclaration();

        assertTrue(reader.updateArguments(method, Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateOptions(method, Map.of(), "pkg").isEmpty());
    }

    @Test
    void readsEveryArgumentWithItsModes() throws Exception {
        List<CliArgumentParameterData> arguments =
                reader.updateArguments(
                        method("TestParameterCliControllerFixture.java", "build"), Map.of(), "pkg");

        assertEquals(2, arguments.size());

        assertEquals("target", arguments.get(0).name());
        assertEquals("The target", arguments.get(0).description());
        assertEquals("REQUIRED", arguments.get(0).mode());
        assertEquals("DEFAULT", arguments.get(0).valueMode());

        assertEquals("rest", arguments.get(1).name());
        assertEquals("OPTIONAL", arguments.get(1).mode());
        assertEquals("ARRAY", arguments.get(1).valueMode());
    }

    @Test
    void readsEveryOptionWithItsModesAndMetadata() throws Exception {
        List<CliOptionParameterData> options =
                reader.updateOptions(
                        method("TestParameterCliControllerFixture.java", "build"), Map.of(), "pkg");

        assertEquals(2, options.size());

        var format = options.get(0);
        assertEquals("format", format.name());
        assertEquals("The format", format.description());
        assertEquals("fmt", format.valueDisplayName());
        assertEquals("json", format.defaultValue());
        assertEquals(List.of("f"), format.shortNames());
        assertEquals(List.of("json", "xml"), format.validValues());
        assertEquals("REQUIRED", format.mode());
        assertEquals("DEFAULT", format.valueMode());

        var flag = options.get(1);
        assertEquals("flag", flag.name());
        // An option naming no short names or valid values keeps them empty.
        assertTrue(flag.shortNames().isEmpty());
        assertTrue(flag.validValues().isEmpty());
        assertEquals("NONE", flag.valueMode());
    }

    @Test
    void readsAnArgumentDeclaredOnItsOwn() throws Exception {
        List<CliArgumentParameterData> arguments =
                reader.updateArguments(
                        method("TestCliControllerFixture.java", "greet"), Map.of(), "pkg");

        assertEquals(1, arguments.size());
        assertEquals("name", arguments.get(0).name());
        assertEquals("REQUIRED", arguments.get(0).mode());
    }
}
