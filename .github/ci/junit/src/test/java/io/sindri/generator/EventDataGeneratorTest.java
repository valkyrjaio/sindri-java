/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generator;

import io.sindri.generator.ast.event.AstEventDataFileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventDataGeneratorTest {

    private final AstEventDataFileGenerator generator = new AstEventDataFileGenerator();

    @Test
    void generateFile_empty_writesEmptyMaps(@TempDir Path tempDir) throws IOException {
        generator.generateFile(tempDir.toString(), "AppEventData", "test.data", Map.of());

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        assertTrue(content.contains("package test.data;"));
        assertTrue(content.contains("public record AppEventData()"));
        assertTrue(content.contains("return Map.of();"));
    }

    @Test
    void generateFile_withListener_writesListenerEntry(@TempDir Path tempDir) throws IOException {
        generator.generateFile(
                tempDir.toString(),
                "AppEventData",
                "test.data",
                Map.of("user.created", "fixtures.handler.UserHandler::onUserCreated"));

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        assertTrue(content.contains("\"user.created\""));
        assertTrue(content.contains("fixtures.handler.UserHandler::onUserCreated"));
    }
}
