package io.sindri.tests.classes.container.provider;

import io.sindri.tests.classes.container.TestService;

import java.util.Map;

public final class TestServiceProviderClass {

    public Map<Class<?>, Object> publishers() {
        return Map.of(TestService.class, TestServiceProviderClass::publishTestService);
    }

    public static void publishTestService(Object container) {}
}
