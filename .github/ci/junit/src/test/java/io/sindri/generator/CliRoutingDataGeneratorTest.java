/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generator;

import io.sindri.generator.ast.cli.AstCliDataFileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}