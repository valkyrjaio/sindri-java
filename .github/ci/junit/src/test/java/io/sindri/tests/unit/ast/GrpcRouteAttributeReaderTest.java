/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.GrpcRouteAttributeReader;
import io.sindri.ast.data.GrpcRouteData;
import io.sindri.ast.data.result.GrpcRouteAttributeResult;
import org.junit.jupiter.api.Test;

public final class GrpcRouteAttributeReaderTest {

    private static final String MIDDLEWARE = "io.sindri.tests.fixtures.grpc.middleware.";

    private final GrpcRouteAttributeReader reader = new GrpcRouteAttributeReader();

    /** A reader that resolves a middleware source from the given map, keyed by its class name. */
    private GrpcRouteAttributeReader resolverReader(java.util.Map<String, String> sources) {
        return new GrpcRouteAttributeReader(
                fqn ->
                        java.util.Optional.ofNullable(sources.get(fqn))
                                .map(com.github.javaparser.StaticJavaParser::parse));
    }

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    private GrpcRouteAttributeResult read(String fixture) {
        return reader.readFile(fixturePath("Grpc/Controller/" + fixture));
    }

    @Test
    void readFile_parsesRouteCount() {
        GrpcRouteAttributeResult result = read("TestGrpcControllerFixture.java");
        assertEquals(2, result.routes().size());
    }

    @Test
    void readFile_keysRoutesByFullyQualifiedMethod() {
        GrpcRouteAttributeResult result = read("TestGrpcControllerFixture.java");
        assertTrue(result.routes().containsKey("/pkg.Greeter/SayHello"));
        assertTrue(result.routes().containsKey("/pkg.Greeter/StreamHellos"));
    }

    @Test
    void readFile_buildsRouteSupplierResolvingTheControllerFromTheContainer() {
        String supplier =
                read("TestGrpcControllerFixture.java")
                        .routes()
                        .get("/pkg.Greeter/SayHello")
                        .toString();
        assertTrue(
                supplier.contains(
                        "() -> new io.valkyrja.grpc.routing.data.Route(\"/pkg.Greeter/SayHello\","));
        // The cached route resolves the controller exactly as the runtime collector does, so a
        // controller with constructor dependencies is reached from the cache too.
        assertTrue(
                supplier.contains(
                        "(c, r) -> c.get(io.sindri.tests.fixtures.grpc.controller.TestGrpcControllerFixture.class).sayHello(c, r)"));
    }

    @Test
    void readFile_unaryRouteHasNoStreamingFlags() {
        String supplier =
                read("TestGrpcControllerFixture.java")
                        .routes()
                        .get("/pkg.Greeter/SayHello")
                        .toString();
        assertFalse(supplier.contains("withClientStreaming"));
        assertFalse(supplier.contains("withServerStreaming"));
    }

    @Test
    void readFile_streamingRouteCarriesBothFlags() {
        String supplier =
                read("TestGrpcControllerFixture.java")
                        .routes()
                        .get("/pkg.Greeter/StreamHellos")
                        .toString();
        assertTrue(supplier.contains(".withClientStreaming(true)"));
        assertTrue(supplier.contains(".withServerStreaming(true)"));
    }

    @Test
    void readFile_populatesRouteData() {
        GrpcRouteData data =
                read("TestGrpcControllerFixture.java").routeData().get("/pkg.Greeter/StreamHellos");
        assertEquals("/pkg.Greeter/StreamHellos", data.method());
        assertEquals("pkg.Greeter", data.service());
        assertEquals("StreamHellos", data.methodName());
        assertTrue(data.clientStreaming());
        assertTrue(data.serverStreaming());
        assertEquals("streamHellos", data.handler().method());
    }

    @Test
    void readFile_withMarkerGrpcService_returnsEmpty() {
        GrpcRouteAttributeResult result = read("TestNoServiceGrpcControllerFixture.java");
        assertTrue(result.routes().isEmpty());
        assertTrue(result.routeData().isEmpty());
    }

    @Test
    void readFile_withGrpcServiceCarryingNoServiceMember_returnsEmpty() {
        // The marker fixture above never enters the member loop; this one walks it to exhaustion
        // and falls out the bottom, which is the only way to reach the loop's exit branch — a real
        // `service` member returns from inside it.
        GrpcRouteAttributeResult result =
                read("TestUnrelatedServiceMemberGrpcControllerFixture.java");
        assertTrue(result.routes().isEmpty());
        assertTrue(result.routeData().isEmpty());
    }

    @Test
    void readFile_skipsMarkerAndUnnamedMethods() {
        GrpcRouteAttributeResult result = read("TestGrpcEdgeControllerFixture.java");
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
                        () -> read("TestNonLiteralGrpcControllerFixture.java"));
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
                reader.readFile(
                        fixturePath("Grpc/Controller/TestMiddlewareGrpcControllerFixture.java"));
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
                mwReader.readFile(
                                fixturePath(
                                        "Grpc/Controller/TestMiddlewaresContainerGrpcControllerFixture.java"))
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
                mwReader.readFile(
                                fixturePath(
                                        "Grpc/Controller/TestQualifiedMiddlewareGrpcControllerFixture.java"))
                        .routeData()
                        .get("/pkg.Qualified/Qualified")
                        .routeMatchedMiddleware());
    }

    @Test
    void readFile_withNonLiteralMethodName_throws() {
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> read("TestNonLiteralMethodGrpcControllerFixture.java"));
        assertTrue(thrown.getMessage().contains("@Route(name)"));
    }

    @Test
    void readFile_withoutAResolverClassifiesNoMiddleware() {
        // The default reader cannot resolve middleware sources, so it emits none rather than
        // guessing a stage.
        String supplier =
                read("TestMiddlewareGrpcControllerFixture.java")
                        .routes()
                        .get("/pkg.Guarded/Guarded")
                        .toString();

        assertFalse(supplier.contains("withAdded"));
    }

    @Test
    void readFile_withNoTypeInFile_throws() {
        assertThrows(
                RuntimeException.class,
                () ->
                        reader.readFile(
                                fixturePath("Grpc/Controller/TestNoTypeGrpcFileFixture.java")));
    }

    @Test
    void readFile_noPackageController_omitsPackagePrefix() {
        String supplier =
                read("TestNoPackageGrpcControllerFixture.java")
                        .routes()
                        .get("/pkg.NoPkg/Ping")
                        .toString();
        assertTrue(
                supplier.contains(
                        "(c, r) -> c.get(TestNoPackageGrpcControllerFixture.class).ping(c, r)"));
    }

    @Test
    void readFile_handlerMemberNamesTheHandlerAndTheMessageTypes() {
        var result = read("TestHandlerGrpcControllerFixture.java");
        String supplier = result.routes().get("/pkg.Handled/Member").toString();
        GrpcRouteData data = result.routeData().get("/pkg.Handled/Member");

        assertTrue(
                supplier.contains(
                        "io.sindri.tests.fixtures.grpc.handler.GreeterHandler::memberHandler"));
        assertTrue(
                supplier.contains(
                        ".withRequestType(io.sindri.tests.fixtures.grpc.message.HelloRequest.class)"));
        assertTrue(
                supplier.contains(
                        ".withResponseType(io.sindri.tests.fixtures.grpc.message.HelloReply.class)"));
        assertEquals(
                "io.sindri.tests.fixtures.grpc.handler.GreeterHandler",
                data.handler().handlerClass());
        assertEquals("memberHandler", data.handler().method());
        assertEquals("io.sindri.tests.fixtures.grpc.message.HelloRequest", data.requestType());
        assertEquals("io.sindri.tests.fixtures.grpc.message.HelloReply", data.responseType());
    }

    @Test
    void readFile_routeAttributeNamesTheMiddlewareOfEveryStage() {
        GrpcRouteData data =
                read("TestHandlerGrpcControllerFixture.java")
                        .routeData()
                        .get("/pkg.Handled/Member");

        // A stage member this reader spells wrong drops that stage's middleware from the cache
        // in silence, so every one of the five is read here.
        assertEquals(
                java.util.List.of(MIDDLEWARE + "AuthMiddleware", MIDDLEWARE + "MatchedMiddleware"),
                data.routeMatchedMiddleware());
        assertEquals(
                java.util.List.of(MIDDLEWARE + "DispatchedMiddleware"),
                data.routeDispatchedMiddleware());
        assertEquals(
                java.util.List.of(MIDDLEWARE + "CaughtMiddleware"),
                data.throwableCaughtMiddleware());
        assertEquals(
                java.util.List.of(MIDDLEWARE + "SendingMiddleware"),
                data.sendingResponseMiddleware());
        assertEquals(
                java.util.List.of(MIDDLEWARE + "AuditMiddleware"), data.responseSentMiddleware());
    }

    @Test
    void readFile_theMiddlewareAttributeAddsToTheStageTheRouteAttributeNames() {
        // The stage member names one class and the convenience attribute names another, and a
        // list field adds the convenience attribute's value, so the member's class comes first.
        GrpcRouteData data =
                resolverReader(
                                java.util.Map.of(
                                        MIDDLEWARE + "AuthMiddleware",
                                        "package io.sindri.tests.fixtures.grpc.middleware;"
                                                + " import io.valkyrja.grpc.middleware.contract.RouteMatchedMiddlewareContract;"
                                                + " public class AuthMiddleware implements"
                                                + " RouteMatchedMiddlewareContract {}"))
                        .readFile(
                                fixturePath(
                                        "Grpc/Controller/TestHandlerGrpcControllerFixture.java"))
                        .routeData()
                        .get("/pkg.Handled/Combined");

        assertEquals(
                java.util.List.of(MIDDLEWARE + "MatchedMiddleware", MIDDLEWARE + "AuthMiddleware"),
                data.routeMatchedMiddleware());
    }

    @Test
    void readFile_emitsTheAddCallOfEveryStage() {
        String supplier =
                read("TestHandlerGrpcControllerFixture.java")
                        .routes()
                        .get("/pkg.Handled/Member")
                        .toString();

        // The emitted builder call is the other half of each stage row. A wrong call name parses
        // cleanly here and lands in a cache file that does not compile.
        assertTrue(
                supplier.contains(
                        ".withAddedRouteMatchedMiddleware(java.util.List.of("
                                + MIDDLEWARE
                                + "AuthMiddleware.class, "
                                + MIDDLEWARE
                                + "MatchedMiddleware.class))"));
        assertTrue(
                supplier.contains(
                        ".withAddedRouteDispatchedMiddleware(java.util.List.of("
                                + MIDDLEWARE
                                + "DispatchedMiddleware.class))"));
        assertTrue(
                supplier.contains(
                        ".withAddedThrowableCaughtMiddleware(java.util.List.of("
                                + MIDDLEWARE
                                + "CaughtMiddleware.class))"));
        assertTrue(
                supplier.contains(
                        ".withAddedSendingResponseMiddleware(java.util.List.of("
                                + MIDDLEWARE
                                + "SendingMiddleware.class))"));
        assertTrue(
                supplier.contains(
                        ".withAddedResponseSentMiddleware(java.util.List.of("
                                + MIDDLEWARE
                                + "AuditMiddleware.class))"));
    }

    @Test
    void readFile_anImportOfTheSentinelItselfStillNamesTheSentinel() {
        // Java permits a redundant import of a java.lang class, and importing the sentinel does
        // not turn it into a type the member names.
        assertNull(
                read("TestImportedVoidGrpcControllerFixture.java")
                        .routeData()
                        .get("/pkg.ImportedVoid/ImportedVoid")
                        .requestType());
    }

    @Test
    void readFile_anImportOfTheSentinelsOwnNameShadowsTheSentinel() {
        // The sentinel is a class, not a spelling, so a class an application imports under the
        // same simple name is a type the member names.
        assertEquals(
                "io.sindri.tests.fixtures.grpc.message.Void",
                read("TestShadowedVoidGrpcControllerFixture.java")
                        .routeData()
                        .get("/pkg.Shadowed/Shadowed")
                        .requestType());
    }

    @Test
    void readFile_resolvesANestedMessageTypeThroughTheOuterClassImport() {
        var result = read("TestHandlerGrpcControllerFixture.java");
        GrpcRouteData data = result.routeData().get("/pkg.Handled/Nested");
        String supplier = result.routes().get("/pkg.Handled/Nested").toString();

        // Emitting `Hello.HelloRequest` as written names a class the generated file cannot see,
        // because it imports neither the outer class nor the nested one.
        assertEquals(
                "io.sindri.tests.fixtures.grpc.message.Hello.HelloRequest", data.requestType());
        assertEquals("io.sindri.tests.fixtures.grpc.message.Hello.HelloReply", data.responseType());
        assertTrue(
                supplier.contains(
                        ".withRequestType(io.sindri.tests.fixtures.grpc.message.Hello.HelloRequest.class)"));
    }

    @Test
    void readFile_withNonLiteralHandlerMethod_throwsRatherThanCachingAGarbageHandler() {
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> read("TestNonLiteralHandlerMethodGrpcControllerFixture.java"));
        assertTrue(thrown.getMessage().contains("@Handler(handlerMethod)"));
        assertTrue(thrown.getMessage().contains("string literal"));
    }

    @Test
    void readFile_handlerAttributeWinsOverTheHandlerMember() {
        GrpcRouteData data =
                read("TestHandlerGrpcControllerFixture.java")
                        .routeData()
                        .get("/pkg.Handled/Attribute");

        assertEquals("attributeHandler", data.handler().method());
    }

    @Test
    void readFile_optionalMembersAtTheirDefaultsNameNothing() {
        var result = read("TestHandlerGrpcControllerFixture.java");
        String supplier = result.routes().get("/pkg.Handled/Defaults").toString();
        GrpcRouteData data = result.routeData().get("/pkg.Handled/Defaults");

        // `Void.class` and `java.lang.Void.class` are the same class, so both mean absent.
        assertNull(data.requestType());
        assertNull(data.responseType());
        assertFalse(supplier.contains("withRequestType"));
        assertFalse(supplier.contains("withResponseType"));
        assertFalse(supplier.contains("withAdded"));
        // A handler member naming no class leaves the controller method as the handler.
        assertEquals(
                "io.sindri.tests.fixtures.grpc.controller.TestHandlerGrpcControllerFixture",
                data.handler().handlerClass());
        assertEquals("defaults", data.handler().method());
    }

    @Test
    void readFile_markerHandlerAndNonClassMembersNameNothing() {
        var result = read("TestHandlerGrpcControllerFixture.java");
        GrpcRouteData data = result.routeData().get("/pkg.Handled/Marker");

        assertEquals("marker", data.handler().method());
        assertNull(data.requestType());
        assertTrue(data.routeMatchedMiddleware().isEmpty());
    }
}
