package io.sindri.tests.fixtures.container.provider;

import io.sindri.tests.fixtures.container.TestService;

import java.util.Map;

public final class TestServiceProviderFixture {

    public Map<Class<?>, Object> publishers() {
        return Map.of(TestService.class, TestServiceProviderFixture::publishTestService);
    }

    public static void publishTestService(Object container) {}
}
