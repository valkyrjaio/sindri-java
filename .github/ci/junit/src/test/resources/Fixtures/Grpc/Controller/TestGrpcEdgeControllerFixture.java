package io.sindri.tests.fixtures.grpc.controller;

import io.valkyrja.grpc.routing.attribute.Route;
import io.valkyrja.grpc.routing.attribute.Service;

// A non-"service" member (ordered first) exercises the service-name lookup's skip branch.
@Service(unused = "x", service = "pkg.Edge")
public class TestGrpcEdgeControllerFixture {

    // Marker @Route (not a NormalAnnotationExpr) is skipped.
    @Route
    public Object marker(Object container, Object route) {
        return null;
    }

    // @Route with no name is skipped.
    @Route(clientStreaming = true)
    public Object noName(Object container, Object route) {
        return null;
    }

    // An unknown member hits the default switch arm; an explicit false literal and a non-literal
    // streaming value exercise both boolean-extraction branches.
    @Route(name = "Valid", clientStreaming = false, serverStreaming = OTHER, unknown = "y")
    public Object valid(Object container, Object route) {
        return null;
    }
}
