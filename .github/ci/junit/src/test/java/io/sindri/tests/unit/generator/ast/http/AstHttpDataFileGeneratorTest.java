/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.generator.ast.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.data.HttpRouteData;
import io.sindri.generator.ast.http.AstHttpDataFileGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AstHttpDataFileGeneratorTest {

    private final AstHttpDataFileGenerator generator = new AstHttpDataFileGenerator();

    @Test
    void generateFile_empty_writesEmptyMaps(@TempDir Path tempDir) throws IOException {
        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", Map.of(), Map.of());

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("package test.data;"));
        assertTrue(content.contains("public record AppHttpRoutingData()"));
        assertTrue(content.contains("return Map.of();"));
    }

    @Test
    void generateFile_withRoute_writesRouteEntry(@TempDir Path tempDir) throws IOException {
        var routes = Map.of("users.index", "fixtures.provider.UserProvider::indexHandler");
        var routeData = Map.of("users.index", new HttpRouteData("/users", "users.index"));

        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("\"users.index\""));
        assertTrue(content.contains("fixtures.provider.UserProvider::indexHandler"));
    }

    @Test
    void generateFile_withRoute_writesPathsIndex(@TempDir Path tempDir) throws IOException {
        var routes = Map.of("users.index", "fixtures.provider.UserProvider::indexHandler");
        var routeData =
                Map.of("users.index", new HttpRouteData("/users", "users.index", List.of("GET")));

        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("\"/users\""));
        assertTrue(content.contains("\"users.index\""));
    }

    @Test
    void generateFile_withDynamicRoute_writesDynamicPathsIndex(@TempDir Path tempDir)
            throws IOException {
        var routes = Map.of("users.show", "fixtures.provider.UserProvider::showHandler");
        var routeData =
                Map.of(
                        "users.show",
                        new HttpRouteData("/users/{id}", "users.show", List.of("GET")));

        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("\"/users/{id}\""));
        assertTrue(content.contains("\"users.show\""));
    }

    @Test
    void generateFile_withTwoRoutes_writesCommaDelimited(@TempDir Path tempDir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("users.index", "fixtures.provider.UserProvider::indexHandler");
        routes.put("users.show", "fixtures.provider.UserProvider::showHandler");
        Map<String, HttpRouteData> routeData = new LinkedHashMap<>();
        routeData.put("users.index", new HttpRouteData("/users", "users.index", List.of("GET")));
        routeData.put("users.show", new HttpRouteData("/users/1", "users.show", List.of("GET")));

        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(
                content.contains("\"users.index\", fixtures.provider.UserProvider::indexHandler,"));
        assertTrue(content.contains("\"/users\", \"users.index\", \"/users/1\", \"users.show\""));
    }

    @Test
    void generateClassContents_delegatesCorrectly() {
        var routes = Map.of("users.index", "fixtures.provider.UserProvider::indexHandler");
        var routeData =
                Map.of("users.index", new HttpRouteData("/users", "users.index", List.of("GET")));

        String result = generator.generateClassContents(routes, routeData);

        assertTrue(result.contains("\"users.index\""));
        assertTrue(result.contains("fixtures.provider.UserProvider::indexHandler"));
    }

    @Test
    void generateFile_withManyRoutes_usesMapOfEntries(@TempDir Path tempDir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        Map<String, HttpRouteData> routeData = new LinkedHashMap<>();
        for (int i = 1; i <= 11; i++) {
            String name = "route." + i;
            routes.put(name, "fixtures.provider.Provider" + i + "::handler");
            routeData.put(name, new HttpRouteData("/path/" + i, name, List.of("GET")));
        }

        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("Map.ofEntries("));
        assertTrue(content.contains("Map.entry(\"route.1\""));
    }

    @Test
    void generateFile_withHeadRoute_includesInPaths(@TempDir Path tempDir) throws IOException {
        var routes = Map.of("head.only", "fixtures.provider.Provider::headHandler");
        var routeData =
                Map.of("head.only", new HttpRouteData("/head-only", "head.only", List.of("HEAD")));

        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        // HEAD paths are included (matches PHP output), not dropped.
        assertTrue(content.contains("\"/head-only\""));
        assertTrue(content.contains("Map.entry(\"HEAD\""));
    }

    @Test
    void generateFile_withManyPathsSameMethod_usesInnerMapOfEntries(@TempDir Path tempDir)
            throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        Map<String, HttpRouteData> routeData = new LinkedHashMap<>();
        for (int i = 1; i <= 11; i++) {
            String name = "users." + i;
            routes.put(name, "fixtures.provider.Provider::handler" + i);
            routeData.put(name, new HttpRouteData("/users/" + i, name, List.of("GET")));
        }

        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("Map.ofEntries(Map.entry("));
    }

    @Test
    void generateFile_withMultipleRequestMethods_writesCommasBetweenMethods(@TempDir Path tempDir)
            throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        Map<String, HttpRouteData> routeData = new LinkedHashMap<>();
        routes.put("users.index", "fixtures.provider.UserProvider::indexHandler");
        routes.put("users.create", "fixtures.provider.UserProvider::createHandler");
        routeData.put("users.index", new HttpRouteData("/users", "users.index", List.of("GET")));
        routeData.put("users.create", new HttpRouteData("/users", "users.create", List.of("POST")));

        generator.generateFile(
                tempDir.toString(), "AppHttpRoutingData", "test.data", routes, routeData);

        String content = Files.readString(tempDir.resolve("AppHttpRoutingData.java"));
        assertTrue(content.contains("Map.entry(\"GET\","));
        assertTrue(content.contains("Map.entry(\"POST\","));
    }

    @Test
    void buildInnerPathMap_withEmptyMap_returnsMapOfEmpty() {
        TestableGenerator gen = new TestableGenerator();

        String result = gen.testBuildInnerPathMap(Map.of());

        assertEquals("Map.of()", result);
    }

    private static class TestableGenerator extends AstHttpDataFileGenerator {
        String testBuildInnerPathMap(Map<String, String> pathMap) {
            return buildInnerPathMap(pathMap);
        }
    }
}
