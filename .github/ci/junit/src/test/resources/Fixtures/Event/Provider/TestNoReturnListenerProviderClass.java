package io.sindri.tests.fixtures.event.provider;

import io.valkyrja.event.data.contract.ListenerContract;
import io.valkyrja.event.provider.contract.ListenerProviderContract;

import java.util.List;

public final class TestNoReturnListenerProviderClass implements ListenerProviderContract {

    @Override
    public List<Class<?>> getListenerClasses() {
        return List.of();
    }

    @Override
    public List<ListenerContract> getListeners() {
        throw new UnsupportedOperationException();
    }
}
