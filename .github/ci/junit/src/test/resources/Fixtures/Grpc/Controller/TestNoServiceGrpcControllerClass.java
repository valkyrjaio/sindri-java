package io.sindri.tests.fixtures.grpc.controller;

import io.valkyrja.grpc.routing.attribute.GrpcMethod;
import io.valkyrja.grpc.routing.attribute.GrpcService;

// A marker @Service (no service value) is treated as absent. The unrelated @Deprecated exercises
// the reader's skip of a non-@Service type annotation.
@Deprecated
@Service
public class TestNoServiceGrpcControllerClass {

    @Method(name = "Ignored")
    public Object ignored(Object container, Object route) {
        return null;
    }
}
