package io.sindri.tests.fixtures.grpc.controller;

import io.valkyrja.grpc.routing.attribute.GrpcMethod;
import io.valkyrja.grpc.routing.attribute.GrpcService;

// A non-"service" member (ordered first) exercises the service-name lookup's skip branch.
@Service(unused = "x", service = "pkg.Edge")
public class TestGrpcEdgeControllerClass {

    // Marker @Method (not a NormalAnnotationExpr) is skipped.
    @Method
    public Object marker(Object container, Object route) {
        return null;
    }

    // @Method with no name is skipped.
    @Method(clientStreaming = true)
    public Object noName(Object container, Object route) {
        return null;
    }

    // An unknown member hits the default switch arm; an explicit false literal and a non-literal
    // streaming value exercise both boolean-extraction branches.
    @Method(name = "Valid", clientStreaming = false, serverStreaming = OTHER, unknown = "y")
    public Object valid(Object container, Object route) {
        return null;
    }
}
