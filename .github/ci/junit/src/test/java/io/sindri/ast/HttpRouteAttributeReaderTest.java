/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import io.sindri.ast.data.HttpRouteData;
import io.sindri.ast.data.result.HttpRouteAttributeResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpRouteAttributeReaderTest {

    private final HttpRouteAttributeReader reader = new HttpRouteAttributeReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Classes/" + relative).getPath();
    }

    @Test
    void readFile_parsesAllRoutes() {
        HttpRouteAttributeResult result = reader.readFile(fixturePath("Http/Controller/TestHttpControllerClass.java"));

        assertEquals(3, result.routeData().size());
    }

    @Test
    void readFile_parsesGetRoute() {
        HttpRouteAttributeResult result = reader.readFile(fixturePath("Http/Controller/TestHttpControllerClass.java"));

        HttpRouteData getRoute = result.routeData().get("test.get");
        assertNotNull(getRoute);
        assertEquals("/test", getRoute.path());
        assertEquals(List.of("GET"), getRoute.requestMethods());
        assertNotNull(getRoute.handler());
        assertEquals("io.sindri.tests.classes.http.provider.TestHttpRouteProviderClass", getRoute.handler().handlerClass());
        assertEquals("getHandler", getRoute.handler().method());
    }

    @Test
    void readFile_parsesMultipleRoutesOnOneMethod() {
        HttpRouteAttributeResult result = reader.readFile(fixturePath("Http/Controller/TestHttpControllerClass.java"));

        long getMethodRoutes = result.routeData().values().stream()
                .filter(r -> r.handler() != null && r.handler().method().equals("getHandler"))
                .count();
        assertEquals(2, getMethodRoutes);
    }

    @Test
    void readFile_parsesPostRoute() {
        HttpRouteAttributeResult result = reader.readFile(fixturePath("Http/Controller/TestHttpControllerClass.java"));

        HttpRouteData postRoute = result.routeData().get("test.post");
        assertNotNull(postRoute);
        assertEquals("/test", postRoute.path());
        assertTrue(postRoute.requestMethods().contains("POST"));
        assertNotNull(postRoute.handler());
        assertEquals("postHandler", postRoute.handler().method());
    }
}