package io.sindri.tests.classes.component.provider;

import io.sindri.tests.classes.container.provider.TestServiceProviderClass;
import io.sindri.tests.classes.event.provider.TestListenerProviderClass;
import io.sindri.tests.classes.http.provider.TestHttpRouteProviderClass;

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