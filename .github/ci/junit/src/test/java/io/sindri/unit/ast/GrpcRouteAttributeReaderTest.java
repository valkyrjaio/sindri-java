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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.GrpcRouteAttributeReader;
import io.sindri.ast.data.GrpcRouteData;
import io.sindri.ast.data.result.GrpcRouteAttributeResult;
import org.junit.jupiter.api.Test;

public class GrpcRouteAttributeReaderTest {

    private final GrpcRouteAttributeReader reader = new GrpcRouteAttributeReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    private GrpcRouteAttributeResult read(String fixture) {
        return reader.readFile(fixturePath("Grpc/Controller/" + fixture));
    }

    @Test
    void readFile_parsesRouteCount() {
        GrpcRouteAttributeResult result = read("TestGrpcControllerClass.java");
        assertEquals(2, result.routes().size());
    }

    @Test
    void readFile_keysRoutesByFullyQualifiedMethod() {
        GrpcRouteAttributeResult result = read("TestGrpcControllerClass.java");
        assertTrue(result.routes().containsKey("/pkg.Greeter/SayHello"));
        assertTrue(result.routes().containsKey("/pkg.Greeter/StreamHellos"));
    }

    @Test
    void readFile_buildsRouteSupplierWithReflectiveHandler() {
        String supplier = read("TestGrpcControllerClass.java").routes().get("/pkg.Greeter/SayHello").toString();
        assertTrue(
                supplier.contains(
                        "() -> new io.valkyrja.grpc.routing.data.Route(\"/pkg.Greeter/SayHello\","));
        assertTrue(
                supplier.contains(
                        "(c, r) -> new io.sindri.tests.fixtures.grpc.controller.TestGrpcControllerClass().sayHello(c, r)"));
    }

    @Test
    void readFile_unaryRouteHasNoStreamingFlags() {
        String supplier = read("TestGrpcControllerClass.java").routes().get("/pkg.Greeter/SayHello").toString();
        assertFalse(supplier.contains("withClientStreaming"));
        assertFalse(supplier.contains("withServerStreaming"));
    }

    @Test
    void readFile_streamingRouteCarriesBothFlags() {
        String supplier = read("TestGrpcControllerClass.java").routes().get("/pkg.Greeter/StreamHellos").toString();
        assertTrue(supplier.contains(".withClientStreaming(true)"));
        assertTrue(supplier.contains(".withServerStreaming(true)"));
    }

    @Test
    void readFile_populatesRouteData() {
        GrpcRouteData data = read("TestGrpcControllerClass.java").routeData().get("/pkg.Greeter/StreamHellos");
        assertEquals("/pkg.Greeter/StreamHellos", data.method());
        assertEquals("pkg.Greeter", data.service());
        assertEquals("StreamHellos", data.methodName());
        assertTrue(data.clientStreaming());
        assertTrue(data.serverStreaming());
        assertEquals("streamHellos", data.handler().method());
    }

    @Test
    void readFile_withMarkerGrpcService_returnsEmpty() {
        GrpcRouteAttributeResult result = read("TestNoServiceGrpcControllerClass.java");
        assertTrue(result.routes().isEmpty());
        assertTrue(result.routeData().isEmpty());
    }

    @Test
    void readFile_skipsMarkerAndUnnamedMethods() {
        GrpcRouteAttributeResult result = read("TestGrpcEdgeControllerClass.java");
        assertEquals(1, result.routes().size());
        assertTrue(result.routes().containsKey("/pkg.Edge/Valid"));
    }

    @Test
    void readFile_withNonLiteralServiceName_throwsRatherThanCachingAGarbageKey() {
        // Falling back to the expression source would cache "/SERVICE_NAME/Ping", a route that
        // answers UNIMPLEMENTED only when the cache is enabled.
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> read("TestNonLiteralGrpcControllerClass.java"));
        assertTrue(thrown.getMessage().contains("@Service(service)"));
        assertTrue(thrown.getMessage().contains("string literal"));
    }

    @Test
    void readFile_classifiesMiddlewareIntoItsStagesAndEmitsThemPreSorted() {
        // The @Middleware attribute names only the class, so each is classified by walking its
        // hierarchy — here Auth is a RouteMatched stage and Audit a ResponseSent stage.
        var middlewareSources =
                java.util.Map.of(
                        "io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware",
                        "package io.sindri.tests.fixtures.grpc.middleware;"
                                + " import io.valkyrja.grpc.middleware.contract.RouteMatchedMiddlewareContract;"
                                + " public class AuthMiddleware implements"
                                + " RouteMatchedMiddlewareContract {}",
                        "io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware",
                        "package io.sindri.tests.fixtures.grpc.middleware;"
                                + " import io.valkyrja.grpc.middleware.contract.ResponseSentMiddlewareContract;"
                                + " public class AuditMiddleware implements"
                                + " ResponseSentMiddlewareContract {}");
        var reader =
                new GrpcRouteAttributeReader(
                        fqn ->
                                java.util.Optional.ofNullable(middlewareSources.get(fqn))
                                        .map(com.github.javaparser.StaticJavaParser::parse));

        var result =
                reader.readFile(fixturePath("Grpc/Controller/TestMiddlewareGrpcControllerClass.java"));
        String supplier = result.routes().get("/pkg.Guarded/Guarded").toString();
        GrpcRouteData data = result.routeData().get("/pkg.Guarded/Guarded");

        assertTrue(
                supplier.contains(
                        ".withAddedRouteMatchedMiddleware(java.util.List.of(io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware.class))"));
        assertTrue(
                supplier.contains(
                        ".withAddedResponseSentMiddleware(java.util.List.of(io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware.class))"));
        assertEquals(
                java.util.List.of("io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware"),
                data.routeMatchedMiddleware());
        assertEquals(
                java.util.List.of("io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware"),
                data.responseSentMiddleware());
        assertTrue(data.routeDispatchedMiddleware().isEmpty());
    }

    @Test
    void readFile_classifiesMiddlewareFromTheExplicitMiddlewaresContainer() {
        var middlewareSources =
                java.util.Map.of(
                        "io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware",
                        "package io.sindri.tests.fixtures.grpc.middleware;"
                                + " import io.valkyrja.grpc.middleware.contract.RouteMatchedMiddlewareContract;"
                                + " public class AuthMiddleware implements RouteMatchedMiddlewareContract {}",
                        "io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware",
                        "package io.sindri.tests.fixtures.grpc.middleware;"
                                + " import io.valkyrja.grpc.middleware.contract.ResponseSentMiddlewareContract;"
                                + " public class AuditMiddleware implements ResponseSentMiddlewareContract {}");
        var mwReader =
                new GrpcRouteAttributeReader(
                        fqn ->
                                java.util.Optional.ofNullable(middlewareSources.get(fqn))
                                        .map(com.github.javaparser.StaticJavaParser::parse));

        GrpcRouteData data =
                mwReader
                        .readFile(
                                fixturePath(
                                        "Grpc/Controller/TestMiddlewaresContainerGrpcControllerClass.java"))
                        .routeData()
                        .get("/pkg.Wrapped/Wrapped");

        assertEquals(
                java.util.List.of("io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware"),
                data.routeMatchedMiddleware());
        assertEquals(
                java.util.List.of("io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware"),
                data.responseSentMiddleware());
    }

    @Test
    void readFile_appendsAQualifiedAndSimpleMiddlewareNameWithoutDeduping() {
        var middlewareSources =
                java.util.Map.of(
                        "io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware",
                        "package io.sindri.tests.fixtures.grpc.middleware;"
                                + " import io.valkyrja.grpc.middleware.contract.RouteMatchedMiddlewareContract;"
                                + " public class AuthMiddleware implements RouteMatchedMiddlewareContract {}");
        var mwReader =
                new GrpcRouteAttributeReader(
                        fqn ->
                                java.util.Optional.ofNullable(middlewareSources.get(fqn))
                                        .map(com.github.javaparser.StaticJavaParser::parse));

        // The qualified and simple forms resolve to the same class, but the reader appends both —
        // it never deduplicates, mirroring the runtime collector. A duplicate is the dev's concern.
        assertEquals(
                java.util.List.of(
                        "io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware",
                        "io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware"),
                mwReader
                        .readFile(
                                fixturePath(
                                        "Grpc/Controller/TestQualifiedMiddlewareGrpcControllerClass.java"))
                        .routeData()
                        .get("/pkg.Qualified/Qualified")
                        .routeMatchedMiddleware());
    }

    @Test
    void readFile_withNonLiteralMethodName_throws() {
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> read("TestNonLiteralMethodGrpcControllerClass.java"));
        assertTrue(thrown.getMessage().contains("@Method(name)"));
    }

    @Test
    void readFile_withoutAResolverClassifiesNoMiddleware() {
        // The default reader cannot resolve middleware sources, so it emits none rather than
        // guessing a stage.
        String supplier =
                read("TestMiddlewareGrpcControllerClass.java")
                        .routes()
                        .get("/pkg.Guarded/Guarded")
                        .toString();

        assertFalse(supplier.contains("withAdded"));
    }

    @Test
    void readFile_withNoTypeInFile_throws() {
        assertThrows(
                RuntimeException.class,
                () -> reader.readFile(fixturePath("Grpc/Controller/TestNoTypeGrpcFile.java")));
    }

    @Test
    void readFile_noPackageController_omitsPackagePrefix() {
        String supplier = read("TestNoPackageGrpcControllerClass.java").routes().get("/pkg.NoPkg/Ping").toString();
        assertTrue(
                supplier.contains("(c, r) -> new TestNoPackageGrpcControllerClass().ping(c, r)"));
    }
}
