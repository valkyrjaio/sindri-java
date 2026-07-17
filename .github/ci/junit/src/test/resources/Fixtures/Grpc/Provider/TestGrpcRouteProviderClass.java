package io.sindri.tests.fixtures.grpc.provider;

import io.sindri.tests.fixtures.grpc.controller.TestGrpcControllerClass;

import java.util.List;

public final class TestGrpcRouteProviderClass {

    public List<Class<?>> getControllerClasses() {
        return List.of(TestGrpcControllerClass.class);
    }

    public List<Object> getRoutes() {
        return List.of();
    }
}
