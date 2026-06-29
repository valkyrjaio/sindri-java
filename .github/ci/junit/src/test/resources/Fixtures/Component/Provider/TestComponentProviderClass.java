package io.sindri.tests.fixtures.component.provider;

import io.sindri.tests.fixtures.container.provider.TestServiceProviderClass;
import io.sindri.tests.fixtures.event.provider.TestListenerProviderClass;
import io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderClass;

import java.util.List;

public final class TestComponentProviderClass {

    public List<Object> getComponentProviders(Object app) {
        return List.of();
    }

    public List<Object> getContainerProviders(Object app) {
        return List.of(new TestServiceProviderClass());
    }

    public List<Object> getEventProviders(Object app) {
        return List.of(new TestListenerProviderClass());
    }

    public List<Object> getCliProviders(Object app) {
        return List.of();
    }

    public List<Object> getHttpProviders(Object app) {
        return List.of(new TestHttpRouteProviderClass());
    }
}
