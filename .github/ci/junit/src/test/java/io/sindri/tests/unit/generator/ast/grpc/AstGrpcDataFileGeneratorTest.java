/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.generator.ast.grpc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.generator.ast.grpc.AstGrpcDataFileGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public final class AstGrpcDataFileGeneratorTest {

    private final AstGrpcDataFileGenerator generator = new AstGrpcDataFileGenerator();

    @Test
    void generateFile_empty_writesEmptyMap(@TempDir Path tempDir) throws IOException {
        generator.generateFile(tempDir.toString(), "AppGrpcRoutingData", "test.data", Map.of());

        String content = Files.readString(tempDir.resolve("AppGrpcRoutingData.java"));
        assertTrue(content.contains("package test.data;"));
        assertTrue(content.contains("implements GrpcRoutingDataContract"));
        assertTrue(content.contains("public record AppGrpcRoutingData()"));
        assertTrue(content.contains("return Map.of();"));
    }

    @Test
    void generateFile_withRoute_writesRouteEntry(@TempDir Path tempDir) throws IOException {
        generator.generateFile(
                tempDir.toString(),
                "AppGrpcRoutingData",
                "test.data",
                Map.of("/pkg.Greeter/SayHello", "() -> new Route(...)"));

        String content = Files.readString(tempDir.resolve("AppGrpcRoutingData.java"));
        assertTrue(content.contains("\"/pkg.Greeter/SayHello\""));
        assertTrue(content.contains("() -> new Route(...)"));
    }

    @Test
    void generateFile_withTwoRoutes_writesCommaDelimited(@TempDir Path tempDir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("/pkg.A/M1", "() -> a");
        routes.put("/pkg.A/M2", "() -> b");

        generator.generateFile(tempDir.toString(), "AppGrpcRoutingData", "test.data", routes);

        String content = Files.readString(tempDir.resolve("AppGrpcRoutingData.java"));
        assertTrue(content.contains("\"/pkg.A/M1\", () -> a,"));
        assertTrue(content.contains("\"/pkg.A/M2\", () -> b"));
    }

    @Test
    void generateClassContents_delegatesCorrectly() {
        String result = generator.generateClassContents(Map.of("/pkg.A/M", "() -> x"));
        assertTrue(result.contains("\"/pkg.A/M\""));
        assertTrue(result.contains("() -> x"));
    }

    @Test
    void generateFile_withManyRoutes_usesMapOfEntries(@TempDir Path tempDir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        for (int i = 1; i <= 11; i++) {
            routes.put("/pkg.A/M" + i, "() -> r" + i);
        }

        generator.generateFile(tempDir.toString(), "AppGrpcRoutingData", "test.data", routes);

        String content = Files.readString(tempDir.resolve("AppGrpcRoutingData.java"));
        assertTrue(content.contains("Map.ofEntries("));
        assertTrue(content.contains("Map.entry(\"/pkg.A/M1\""));
    }
}
