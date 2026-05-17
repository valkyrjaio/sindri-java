/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import io.sindri.ast.data.result.ComponentProviderResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentProviderReaderTest {

    private final ComponentProviderReader reader = new ComponentProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Classes/" + relative).getPath();
    }

    @Test
    void readFile_parsesServiceProviders() {
        ComponentProviderResult data = reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertEquals(1, data.serviceProviders().size());
        assertEquals("io.sindri.tests.classes.container.provider.TestServiceProviderClass", data.serviceProviders().get(0));
    }

    @Test
    void readFile_parsesListenerProviders() {
        ComponentProviderResult data = reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertEquals(1, data.listenerProviders().size());
        assertEquals("io.sindri.tests.classes.event.provider.TestListenerProviderClass", data.listenerProviders().get(0));
    }

    @Test
    void readFile_parsesHttpRouteProviders() {
        ComponentProviderResult data = reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertEquals(1, data.httpRouteProviders().size());
        assertEquals("io.sindri.tests.classes.http.provider.TestHttpRouteProviderClass", data.httpRouteProviders().get(0));
    }

    @Test
    void readFile_emptyComponentAndCliRouteProviders() {
        ComponentProviderResult data = reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertTrue(data.componentProviders().isEmpty());
        assertTrue(data.cliRouteProviders().isEmpty());
    }
}