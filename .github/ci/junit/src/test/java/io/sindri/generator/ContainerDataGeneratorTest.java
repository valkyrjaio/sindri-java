/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generator;

import io.sindri.generator.ast.container.AstContainerDataFileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerDataGeneratorTest {

    private final AstContainerDataFileGenerator generator = new AstContainerDataFileGenerator();

    @Test
    void generateFile_empty_writesEmptyCallbacks(@TempDir Path tempDir) throws IOException {
        generator.generateFile(tempDir.toString(), "AppContainerData", "test.data", Map.of());

        String content = Files.readString(tempDir.resolve("AppContainerData.java"));
        assertTrue(content.contains("package test.data;"));
        assertTrue(content.contains("public record AppContainerData()"));
        assertTrue(content.contains("return Map.of();"));
    }

    @Test
    void generateFile_withPublisher_writesCallbackEntry(@TempDir Path tempDir) throws IOException {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        publishers.put("fixtures.service.UserService", new String[]{"fixtures.provider.UserProvider", "publishUserService"});

        generator.generateFile(tempDir.toString(), "AppContainerData", "test.data", publishers);

        String content = Files.readString(tempDir.resolve("AppContainerData.java"));
        assertTrue(content.contains("fixtures.service.UserService.class"));
        assertTrue(content.contains("fixtures.provider.UserProvider::publishUserService"));
    }
}