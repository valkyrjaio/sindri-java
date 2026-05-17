/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generator;

import io.sindri.ast.data.HttpRouteData;
import io.sindri.generator.ast.http.AstHttpDataFileGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpRoutingDataGeneratorTest {

    private final AstHttpDataFileGenerator generator = new AstHttpDataFileGenerator();

    @Test
    void generateFile_empty_writesEmptyMaps(@TempDir Path tempDir) throws IOException {
        generator.generateFile(tempDir.toString(), "AppHttpRoutingData", "test.data", Map.of(), Map.of());

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("package test.data;"));
        assertTrue(content.contains("public record AppHttpRoutingData()"));
        assertTrue(content.contains("return Map.of();"));
    }

    @Test
    void generateFile_withRoute_writesRouteEntry(@TempDir Path tempDir) throws IOException {
        var routes = Map.of("users.index", "fixtures.provider.UserProvider::indexHandler");
        var routeData = Map.of("users.index", new HttpRouteData("/users", "users.index"));

        generator.generateFile(tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("\"users.index\""));
        assertTrue(content.contains("fixtures.provider.UserProvider::indexHandler"));
    }

    @Test
    void generateFile_withRoute_writesPathsIndex(@TempDir Path tempDir) throws IOException {
        var routes = Map.of("users.index", "fixtures.provider.UserProvider::indexHandler");
        var routeData = Map.of("users.index", new HttpRouteData("/users", "users.index", List.of("GET")));

        generator.generateFile(tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("\"/users\""));
        assertTrue(content.contains("\"users.index\""));
    }

    @Test
    void generateFile_withDynamicRoute_writesDynamicPathsIndex(@TempDir Path tempDir) throws IOException {
        var routes = Map.of("users.show", "fixtures.provider.UserProvider::showHandler");
        var routeData = Map.of("users.show", new HttpRouteData("/users/{id}", "users.show", List.of("GET")));

        generator.generateFile(tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("\"/users/{id}\""));
        assertTrue(content.contains("\"users.show\""));
    }
}