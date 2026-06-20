/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.ConfigReader;
import io.sindri.ast.data.result.ConfigResult;
import org.junit.jupiter.api.Test;

/** Test the {@link ConfigReader}. */
final class ConfigReaderTest {

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Classes/" + relative).getPath();
    }

    @Test
    void readsClassConfigWithProviders() {
        ConfigResult result =
                new ConfigReader().readFile(fixturePath("Config/TestConfigClass.java"));

        assertEquals("io.sindri.tests", result.namespace());
        assertEquals("io.sindri.tests.data", result.dataNamespace());
        assertEquals(
                java.util.List.of(
                        "io.sindri.tests.classes.component.provider.TestComponentProviderClass"),
                result.providers());
        assertTrue(result.dataPath().endsWith("/data"));
    }

    @Test
    void readsRecordConfigWithNonPrefixedDataNamespace() {
        ConfigResult result =
                new ConfigReader().readFile(fixturePath("Config/TestConfigRecord.java"));

        assertEquals("io.sindri.tests", result.namespace());
        assertEquals("other.data", result.dataNamespace());
        assertTrue(result.providers().isEmpty());
        assertTrue(result.dataPath().endsWith("/other/data"));
    }

    @Test
    void ignoresProviderListWithoutObjectCreations() {
        ConfigResult result =
                new ConfigReader().readFile(fixturePath("Config/TestConfigStringProviders.java"));

        assertTrue(result.providers().isEmpty());
    }

    @Test
    void throwsWhenNoTypeIsPresent() {
        assertThrows(
                RuntimeException.class,
                () -> new ConfigReader().readFile(fixturePath("Config/TestConfigNoType.java")));
    }

    @Test
    void throwsWhenNoNoArgConstructor() {
        assertThrows(
                RuntimeException.class,
                () -> new ConfigReader().readFile(fixturePath("Config/TestConfigNoCtor.java")));
    }

    @Test
    void throwsWhenConstructorHasNoThisCall() {
        assertThrows(
                RuntimeException.class,
                () -> new ConfigReader().readFile(fixturePath("Config/TestConfigNoThisCall.java")));
    }
}
