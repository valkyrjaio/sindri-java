package io.sindri.tests.fixtures.event.provider;

import io.sindri.tests.fixtures.event.TestEventFixture;
import io.valkyrja.event.data.Listener;
import io.valkyrja.event.data.contract.ListenerContract;
import io.valkyrja.event.provider.contract.ListenerProviderContract;

import java.util.List;

public final class TestSamePackageListenerProviderFixture implements ListenerProviderContract {

    @Override
    public List<Class<?>> getListenerClasses() {
        return List.of();
    }

    @Override
    public List<ListenerContract> getListeners() {
        return List.of(
            new Listener(
                TestEventFixture.class,
                "same.package.handler",
                TestEmptyListenerProviderFixture::handle),
            new Listener(
                TestEventFixture.class,
                "own.class.handler",
                TestSamePackageListenerProviderFixture::handle)
        );
    }

    public static Object handle(Object container, Object arguments) {
        return null;
    }
}
