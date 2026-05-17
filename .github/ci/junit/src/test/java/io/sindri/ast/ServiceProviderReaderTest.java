/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import io.sindri.ast.data.result.ServiceProviderResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceProviderReaderTest {

    private final ServiceProviderReader reader = new ServiceProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Classes/" + relative).getPath();
    }

    @Test
    void readFile_parsesPublishers() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestServiceProviderClass.java"));

        assertEquals(1, result.publishers().size());
        assertTrue(result.publishers().containsKey("io.sindri.tests.classes.container.TestService"));
    }

    @Test
    void readFile_parsesProviderAndMethod() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestServiceProviderClass.java"));

        String[] ref = result.publishers().get("io.sindri.tests.classes.container.TestService");
        assertArrayEquals(
                new String[]{"io.sindri.tests.classes.container.provider.TestServiceProviderClass", "publishTestService"},
                ref);
    }
}