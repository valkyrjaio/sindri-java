package io.sindri.tests.fixtures.component.provider;

import io.sindri.tests.fixtures.container.provider.TestServiceProviderFixture;
import io.sindri.tests.fixtures.event.provider.TestListenerProviderFixture;
import io.sindri.tests.fixtures.grpc.provider.TestGrpcRouteProviderFixture;
import io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderFixture;

import java.util.List;

public final class TestComponentProviderFixture {

    public List<Object> getComponentProviders(Object app) {
        return List.of();
    }

    public List<Object> getContainerProviders(Object app) {
        return List.of(new TestServiceProviderFixture());
    }

    public List<Object> getEventProviders(Object app) {
        return List.of(new TestListenerProviderFixture());
    }

    public List<Object> getCliProviders(Object app) {
        return List.of();
    }

    public List<Object> getHttpProviders(Object app) {
        return List.of(new TestHttpRouteProviderFixture());
    }

    public List<Object> getGrpcProviders(Object app) {
        return List.of(new TestGrpcRouteProviderFixture());
    }
}
