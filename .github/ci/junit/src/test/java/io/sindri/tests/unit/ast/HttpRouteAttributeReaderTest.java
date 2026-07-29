/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.*;
import io.sindri.ast.data.HttpRouteData;
import io.sindri.ast.data.result.HttpRouteAttributeResult;
import java.util.List;
import org.junit.jupiter.api.Test;

public final class HttpRouteAttributeReaderTest {

    private final HttpRouteAttributeReader reader = new HttpRouteAttributeReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesAllRoutes() {
        HttpRouteAttributeResult result =
                reader.readFile(fixturePath("Http/Controller/TestHttpControllerClass.java"));

        assertEquals(3, result.routeData().size());
    }

    @Test
    void readFile_parsesGetRoute() {
        HttpRouteAttributeResult result =
                reader.readFile(fixturePath("Http/Controller/TestHttpControllerClass.java"));

        HttpRouteData getRoute = result.routeData().get("test.get");
        assertNotNull(getRoute);
        assertEquals("/test", getRoute.path());
        assertEquals(List.of("GET"), getRoute.requestMethods());
        assertNotNull(getRoute.handler());
        assertEquals(
                "io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderClass",
                getRoute.handler().handlerClass());
        assertEquals("getHandler", getRoute.handler().method());
    }

    @Test
    void readFile_parsesMultipleRoutesOnOneMethod() {
        HttpRouteAttributeResult result =
                reader.readFile(fixturePath("Http/Controller/TestHttpControllerClass.java"));

        long getMethodRoutes =
                result.routeData().values().stream()
                        .filter(
                                r -> {
                                    var handler = r.handler();

                                    return handler != null && handler.method().equals("getHandler");
                                })
                        .count();
        assertEquals(2, getMethodRoutes);
    }

    @Test
    void readFile_parsesPostRoute() {
        HttpRouteAttributeResult result =
                reader.readFile(fixturePath("Http/Controller/TestHttpControllerClass.java"));

        HttpRouteData postRoute = result.routeData().get("test.post");
        assertNotNull(postRoute);
        assertEquals("/test", postRoute.path());
        assertTrue(postRoute.requestMethods().contains("POST"));
        assertNotNull(postRoute.handler());
        assertEquals("postHandler", postRoute.handler().method());
    }

    @Test
    void readFile_withNoRouteMethod_returnsEmpty() {
        HttpRouteAttributeResult result =
                reader.readFile(fixturePath("Http/Controller/TestNoRouteHttpControllerClass.java"));

        assertTrue(result.routeData().isEmpty());
    }

    @Test
    void readFile_withRouteButNoRouteHandler_handlerIsNull() {
        HttpRouteAttributeResult result =
                reader.readFile(
                        fixturePath("Http/Controller/TestNoRouteHandlerHttpControllerClass.java"));

        assertEquals(1, result.routeData().size());
        HttpRouteData route = result.routeData().get("no.handler");
        assertNotNull(route);
        assertNull(route.handler());
    }

    @Test
    void readFile_withSingleRequestMethod_parsesCorrectly() {
        HttpRouteAttributeResult result =
                reader.readFile(
                        fixturePath(
                                "Http/Controller/TestSingleRequestMethodHttpControllerClass.java"));

        assertEquals(1, result.routeData().size());
        HttpRouteData route = result.routeData().get("single.get");
        assertNotNull(route);
        assertEquals(List.of("GET"), route.requestMethods());
    }

    @Test
    void readFile_withNoTypeDeclaration_throwsException() {
        String path = fixturePath("Http/Controller/TestNoTypeDeclHttpControllerClass.java");

        assertThrows(RuntimeException.class, () -> reader.readFile(path));
    }

    @Test
    void readFile_withStringRequestMethod_usesToStringFallback() {
        HttpRouteAttributeResult result =
                reader.readFile(
                        fixturePath(
                                "Http/Controller/TestStringRequestMethodHttpControllerClass.java"));

        assertEquals(1, result.routeData().size());
        HttpRouteData route = result.routeData().get("str.get");
        assertNotNull(route);
        assertEquals(List.of("\"GET\""), route.requestMethods());
    }

    @Test
    void readFile_edgeAnnotations_handlesMarkerHandlerAndDefaults() {
        HttpRouteAttributeResult result =
                reader.readFile(fixturePath("Http/Controller/TestEdgeHttpControllerClass.java"));

        assertTrue(result.routes().containsKey("edge"));
    }

    @Test
    void readFile_classifiesMiddlewareIntoStagesAndEmitsThemPreSorted() {
        var middlewareSources =
                java.util.Map.of(
                        "io.sindri.tests.fixtures.http.middleware.AuthMiddleware",
                        "package io.sindri.tests.fixtures.http.middleware;"
                                + " import io.valkyrja.http.middleware.contract.RouteMatchedMiddlewareContract;"
                                + " public class AuthMiddleware implements RouteMatchedMiddlewareContract {}",
                        "io.sindri.tests.fixtures.http.middleware.AuditMiddleware",
                        "package io.sindri.tests.fixtures.http.middleware;"
                                + " import io.valkyrja.http.middleware.contract.ResponseSentMiddlewareContract;"
                                + " public class AuditMiddleware implements ResponseSentMiddlewareContract {}");
        var mwReader =
                new HttpRouteAttributeReader(
                        fqn ->
                                java.util.Optional.ofNullable(middlewareSources.get(fqn))
                                        .map(com.github.javaparser.StaticJavaParser::parse));

        var result =
                mwReader.readFile(
                        fixturePath("Http/Controller/TestMiddlewareHttpControllerClass.java"));
        HttpRouteData data = result.routeData().get("guarded");
        String supplier = result.routes().get("guarded").toString();

        assertEquals(
                List.of("io.sindri.tests.fixtures.http.middleware.AuthMiddleware"),
                data.routeMatchedMiddleware());
        assertEquals(
                List.of("io.sindri.tests.fixtures.http.middleware.AuditMiddleware"),
                data.responseSentMiddleware());
        assertTrue(data.routeDispatchedMiddleware().isEmpty());
        assertTrue(
                supplier.contains(
                        "java.util.List.of(io.sindri.tests.fixtures.http.middleware.AuthMiddleware.class)"));
        assertTrue(
                supplier.contains(
                        "java.util.List.of(io.sindri.tests.fixtures.http.middleware.AuditMiddleware.class)"));
    }

    @Test
    void readFile_withoutAResolverClassifiesNoHttpMiddleware() {
        HttpRouteAttributeResult result =
                reader.readFile(
                        fixturePath("Http/Controller/TestMiddlewareHttpControllerClass.java"));

        assertTrue(result.routeData().get("guarded").routeMatchedMiddleware().isEmpty());
    }

    @Test
    void readFile_parsesDynamicRouteAnnotations() {
        HttpRouteAttributeResult result = reader.readFile(fixturePath("Http/Controller/TestDynamicRouteHttpControllerClass.java"));

        assertEquals(4, result.routeData().size());

        HttpRouteData show = result.routeData().get("users.show");
        assertNotNull(show);
        assertEquals("/users/{id}", show.path());
        // A dynamic route declares its parameters inline, and they must be read from there.
        assertEquals(1, show.parameters().size());
        assertEquals("id", show.parameters().get(0).name());
        assertFalse(show.regex().isEmpty());
    }

    @Test
    void readFile_parsesEveryInlineParameterOfADynamicRoute() {
        HttpRouteAttributeResult result = reader.readFile(fixturePath("Http/Controller/TestDynamicRouteHttpControllerClass.java"));

        HttpRouteData slug = result.routeData().get("users.slug");
        assertNotNull(slug);
        assertEquals(2, slug.parameters().size());
        assertEquals("id", slug.parameters().get(0).name());
        assertEquals("slug", slug.parameters().get(1).name());
    }

    @Test
    void readFile_carriesTheCaptureAndOptionalFlagsIntoTheComputedRegex() {
        HttpRouteAttributeResult result = reader.readFile(fixturePath("Http/Controller/TestDynamicRouteHttpControllerClass.java"));

        // A parameter declared as not captured must produce a group without a name.
        HttpRouteData nonCapture = result.routeData().get("users.nonCapture");
        assertNotNull(nonCapture);
        assertFalse(nonCapture.parameters().get(0).shouldCapture());
        assertTrue(nonCapture.regex().contains("(?:"));
        assertFalse(nonCapture.regex().contains("(?<value>"));

        // An optional parameter must make the preceding slash optional too.
        HttpRouteData optional = result.routeData().get("users.optional");
        assertNotNull(optional);
        assertTrue(optional.parameters().get(0).isOptional());
        assertTrue(optional.regex().contains(")?"));
    }
}
