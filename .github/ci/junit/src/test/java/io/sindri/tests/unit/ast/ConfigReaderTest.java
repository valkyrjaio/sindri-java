/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.ConfigReader;
import io.sindri.ast.data.result.ConfigResult;
import org.junit.jupiter.api.Test;

/** Test the {@link ConfigReader}. */
final class ConfigReaderTest {

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readsClassConfigWithProviders() {
        ConfigResult result =
                new ConfigReader().readFile(fixturePath("Config/TestConfigFixture.java"));

        assertEquals("io.sindri.tests", result.namespace());
        assertEquals("io.sindri.tests.data", result.dataNamespace());
        assertEquals(
                java.util.List.of(
                        "io.sindri.tests.fixtures.component.provider.TestComponentProviderFixture"),
                result.providers());
        assertTrue(result.dataPath().endsWith("/data"));
    }

    @Test
    void readsRecordConfigWithNonPrefixedDataNamespace() {
        ConfigResult result =
                new ConfigReader().readFile(fixturePath("Config/TestConfigRecordFixture.java"));

        assertEquals("io.sindri.tests", result.namespace());
        assertEquals("other.data", result.dataNamespace());
        assertTrue(result.providers().isEmpty());
        assertTrue(result.dataPath().endsWith("/other/data"));
    }

    @Test
    void ignoresProviderListWithoutObjectCreations() {
        ConfigResult result =
                new ConfigReader()
                        .readFile(fixturePath("Config/TestConfigStringProvidersFixture.java"));

        assertTrue(result.providers().isEmpty());
    }

    @Test
    void throwsWhenNoTypeIsPresent() {
        assertThrows(
                RuntimeException.class,
                () ->
                        new ConfigReader()
                                .readFile(fixturePath("Config/TestConfigNoTypeFixture.java")));
    }

    @Test
    void throwsWhenNoNoArgConstructor() {
        assertThrows(
                RuntimeException.class,
                () ->
                        new ConfigReader()
                                .readFile(fixturePath("Config/TestConfigNoCtorFixture.java")));
    }

    @Test
    void throwsWhenConstructorHasNoThisCall() {
        assertThrows(
                RuntimeException.class,
                () ->
                        new ConfigReader()
                                .readFile(fixturePath("Config/TestConfigNoThisCallFixture.java")));
    }

    @Test
    void skipsListItemsThatAreNotObjectCreations() {
        // A list arg containing a non-object-creation alongside a creation exercises both sides
        // of the `item.isObjectCreationExpr()` guard.
        var reader =
                new ConfigReader() {
                    @Override
                    protected java.util.List<com.github.javaparser.ast.expr.Expression>
                            extractListOfItems(com.github.javaparser.ast.expr.Expression expr) {
                        return java.util.List.of(
                                com.github.javaparser.StaticJavaParser.parseExpression("new Foo()"),
                                new com.github.javaparser.ast.expr.NameExpr("notACreation"));
                    }
                };

        // Only the object-creation item is collected; the NameExpr is skipped.
        assertEquals(
                1,
                reader.readFile(fixturePath("Config/TestConfigFixture.java")).providers().size());
    }

    @Test
    void readsConfigInDefaultPackageWithEmptyNamespace() {
        // No package declaration (empty file package) and an empty namespace literal exercise the
        // filePkg-empty / namespace-empty ternaries plus resolveClassName's empty-package arm.
        ConfigResult result =
                new ConfigReader()
                        .readFile(fixturePath("Config/TestConfigDefaultPackageFixture.java"));

        assertEquals("", result.namespace());
        assertEquals(java.util.List.of("SomeProvider"), result.providers());
    }

    @Test
    void skipsProvidersWhoseFqnResolvesEmpty() {
        var reader =
                new ConfigReader() {
                    @Override
                    protected String extractObjectCreationFqn(
                            com.github.javaparser.ast.expr.Expression expr,
                            java.util.Map<String, String> importMap,
                            String pkg) {
                        return "";
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Config/TestConfigFixture.java"))
                        .providers()
                        .isEmpty());
    }
}
