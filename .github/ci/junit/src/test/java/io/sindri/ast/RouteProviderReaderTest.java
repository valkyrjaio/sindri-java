/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import io.sindri.ast.data.result.RouteProviderResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteProviderReaderTest {

    private final RouteProviderReader reader = new RouteProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Classes/" + relative).getPath();
    }

    @Test
    void readFile_parsesControllerClasses() {
        RouteProviderResult result = reader.readFile(fixturePath("Http/Provider/TestHttpRouteProviderClass.java"));

        assertEquals(1, result.controllerClasses().size());
        assertEquals("io.sindri.tests.classes.http.controller.TestHttpControllerClass", result.controllerClasses().get(0));
    }
}
