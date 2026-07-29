package io.sindri.tests.fixtures.event.provider;

import io.valkyrja.event.provider.contract.ListenerProviderContract;

import java.util.List;

public final class TestEmptyListenerProviderFixture implements ListenerProviderContract {

    @Override
    public List<Class<?>> getListenerClasses() {
        return List.of();
    }
}
