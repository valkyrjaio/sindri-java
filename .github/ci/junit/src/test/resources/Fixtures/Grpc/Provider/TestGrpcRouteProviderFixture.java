package io.sindri.tests.fixtures.grpc.provider;

import io.sindri.tests.fixtures.grpc.controller.TestGrpcControllerFixture;

import java.util.List;

public final class TestGrpcRouteProviderFixture {

    public List<Class<?>> getControllerClasses() {
        return List.of(TestGrpcControllerFixture.class);
    }

    public List<Object> getRoutes() {
        return List.of();
    }
}
