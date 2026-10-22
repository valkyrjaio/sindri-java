package io.sindri.tests.fixtures.grpc.controller;

import io.valkyrja.grpc.routing.attribute.Route;
import io.valkyrja.grpc.routing.attribute.Service;

// A marker @Service (no service value) is treated as absent. The unrelated @Deprecated exercises
// the reader's skip of a non-@Service type annotation.
@Deprecated
@Service
public class TestNoServiceGrpcControllerFixture {

    @Route(name = "Ignored")
    public Object ignored(Object container, Object route) {
        return null;
    }
}
