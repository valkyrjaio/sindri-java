/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.generator;

import io.sindri.generator.ast.cli.AstCliDataFileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CliRoutingDataGeneratorTest {

    private final AstCliDataFileGenerator generator = new AstCliDataFileGenerator();

    @Test
    void generateFile_empty_writesEmptyMap(@TempDir Path tempDir) throws IOException {
        generator.generateFile(tempDir.toString(), "AppCliRoutingData", "test.data", Map.of());

        String content = Files.readString(tempDir.resolve("AppCliRoutingData.java"));
        assertTrue(content.contains("package test.data;"));
        assertTrue(content.contains("public record AppCliRoutingData()"));
        assertTrue(content.contains("return Map.of();"));
    }

    @Test
    void generateFile_withRoute_writesRouteEntry(@TempDir Path tempDir) throws IOException {
        generator.generateFile(
                tempDir.toString(),
                "AppCliRoutingData",
                "test.data",
                Map.of("greet", "fixtures.command.GreetCommand::handle"));

        String content = Files.readString(tempDir.resolve("AppCliRoutingData.java"));
        assertTrue(content.contains("\"greet\""));
        assertTrue(content.contains("fixtures.command.GreetCommand::handle"));
    }

    @Test
    void generateFile_withTwoRoutes_writesCommaDelimited(@TempDir Path tempDir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("greet", "fixtures.command.GreetCommand::handle");
        routes.put("farewell", "fixtures.command.FarewellCommand::handle");

        generator.generateFile(tempDir.toString(), "AppCliRoutingData", "test.data", routes);

        String content = Files.readString(tempDir.resolve("AppCliRoutingData.java"));
        assertTrue(content.contains("\"greet\", fixtures.command.GreetCommand::handle,"));
        assertTrue(content.contains("\"farewell\", fixtures.command.FarewellCommand::handle"));
    }

    @Test
    void generateClassContents_delegatesCorrectly() {
        String result = generator.generateClassContents(
                Map.of("greet", "fixtures.command.GreetCommand::handle"));

        assertTrue(result.contains("\"greet\""));
        assertTrue(result.contains("fixtures.command.GreetCommand::handle"));
    }

    @Test
    void generateFile_withManyRoutes_usesMapOfEntries(@TempDir Path tempDir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        for (int i = 1; i <= 11; i++) {
            routes.put("command." + i, "fixtures.command.Command" + i + "::handle");
        }

        generator.generateFile(tempDir.toString(), "AppCliRoutingData", "test.data", routes);

        String content = Files.readString(tempDir.resolve("AppCliRoutingData.java"));
        assertTrue(content.contains("Map.ofEntries("));
        assertTrue(content.contains("Map.entry(\"command.1\""));
    }
}
