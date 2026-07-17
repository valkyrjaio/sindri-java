package io.sindri.tests.fixtures.grpc.controller;

import io.valkyrja.grpc.routing.attribute.GrpcMethod;
import io.valkyrja.grpc.routing.attribute.GrpcService;

// A marker @GrpcService (no service value) is treated as absent.
@GrpcService
public class TestNoServiceGrpcControllerClass {

    @GrpcMethod(name = "Ignored")
    public Object ignored(Object container, Object route) {
        return null;
    }
}
