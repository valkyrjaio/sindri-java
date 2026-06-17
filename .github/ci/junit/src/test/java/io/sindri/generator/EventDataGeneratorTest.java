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
import java.util.LinkedHashMap;
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

    @Test
    void generateFile_withTwoListeners_writesCommaDelimited(@TempDir Path tempDir) throws IOException {
        Map<String, String> listeners = new LinkedHashMap<>();
        listeners.put("user.created", "fixtures.handler.UserHandler::onCreate");
        listeners.put("user.deleted", "fixtures.handler.UserHandler::onDelete");

        generator.generateFile(tempDir.toString(), "AppEventData", "test.data", listeners);

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        assertTrue(content.contains("\"user.created\", fixtures.handler.UserHandler::onCreate,"));
        assertTrue(content.contains("\"user.deleted\", fixtures.handler.UserHandler::onDelete"));
    }

    @Test
    void generateClassContents_delegatesCorrectly() {
        String result = generator.generateClassContents(
                Map.of("user.created", "fixtures.handler.UserHandler::onCreate"));

        assertTrue(result.contains("\"user.created\""));
        assertTrue(result.contains("fixtures.handler.UserHandler::onCreate"));
    }

    @Test
    void generateFile_withManyListeners_usesMapOfEntries(@TempDir Path tempDir) throws IOException {
        Map<String, String> listeners = new LinkedHashMap<>();
        for (int i = 1; i <= 11; i++) {
            listeners.put("event." + i, "fixtures.handler.Handler" + i + "::onEvent" + i);
        }

        generator.generateFile(tempDir.toString(), "AppEventData", "test.data", listeners);

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        assertTrue(content.contains("Map.ofEntries("));
        assertTrue(content.contains("Map.entry(\"event.1\""));
    }
}
