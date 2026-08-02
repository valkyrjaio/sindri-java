/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.generator.ast.container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.generator.ast.container.AstContainerDataFileGenerator;
import io.sindri.generator.enum_.GenerateStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public final class AstContainerDataFileGeneratorTest {

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
        publishers.put(
                "fixtures.service.UserService",
                new String[] {"fixtures.provider.UserProvider", "publishUserService"});

        generator.generateFile(tempDir.toString(), "AppContainerData", "test.data", publishers);

        String content = Files.readString(tempDir.resolve("AppContainerData.java"));
        assertTrue(content.contains("fixtures.service.UserService.class"));
        assertTrue(content.contains("fixtures.provider.UserProvider::publishUserService"));
    }

    @Test
    void generateFile_withTwoPublishers_writesCommaDelimited(@TempDir Path tempDir)
            throws IOException {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        publishers.put(
                "fixtures.service.ServiceA",
                new String[] {"fixtures.provider.ProviderA", "publishA"});
        publishers.put(
                "fixtures.service.ServiceB",
                new String[] {"fixtures.provider.ProviderB", "publishB"});

        generator.generateFile(tempDir.toString(), "AppContainerData", "test.data", publishers);

        String content = Files.readString(tempDir.resolve("AppContainerData.java"));
        assertTrue(
                content.contains(
                        "fixtures.service.ServiceA.class, fixtures.provider.ProviderA::publishA,"));
        assertTrue(
                content.contains(
                        "fixtures.service.ServiceB.class, fixtures.provider.ProviderB::publishB"));
    }

    @Test
    void generateClassContents_delegatesCorrectly() {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        publishers.put(
                "fixtures.service.UserService",
                new String[] {"fixtures.provider.UserProvider", "publish"});

        String result = generator.generateClassContents(publishers);

        assertTrue(result.contains("fixtures.service.UserService.class"));
        assertTrue(result.contains("fixtures.provider.UserProvider::publish"));
    }

    @Test
    void generateFile_withManyPublishers_usesMapOfEntries(@TempDir Path tempDir)
            throws IOException {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        for (int i = 1; i <= 11; i++) {
            publishers.put(
                    "fixtures.service.Service" + i,
                    new String[] {"fixtures.provider.Provider" + i, "publish" + i});
        }

        generator.generateFile(tempDir.toString(), "AppContainerData", "test.data", publishers);

        String content = Files.readString(tempDir.resolve("AppContainerData.java"));
        assertTrue(content.contains("Map.ofEntries("));
        assertTrue(content.contains("Map.entry(fixtures.service.Service1.class"));
    }

    @Test
    void generateFile_sameContent_returnsSkipped(@TempDir Path tempDir) throws IOException {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        publishers.put(
                "fixtures.service.UserService",
                new String[] {"fixtures.provider.UserProvider", "publish"});
        generator.generateFile(tempDir.toString(), "SkipTest", "test.data", publishers);

        GenerateStatus status =
                generator.generateFile(tempDir.toString(), "SkipTest", "test.data", publishers);

        assertEquals(GenerateStatus.SKIPPED, status);
    }

    @Test
    void generateFile_ioError_returnsFailure(@TempDir Path tempDir) throws IOException {
        Files.createDirectories(tempDir.resolve("FailTest.java"));

        GenerateStatus status =
                generator.generateFile(tempDir.toString(), "FailTest", "test.data", Map.of());

        assertEquals(GenerateStatus.FAILURE, status);
    }
}
