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
