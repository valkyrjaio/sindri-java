package io.sindri.tests.fixtures.event.provider;

import io.sindri.tests.fixtures.event.TestEventFixture;
import io.sindri.tests.fixtures.event.TestListenerFixture;
import io.valkyrja.event.data.Listener;
import io.valkyrja.event.data.contract.ListenerContract;
import io.valkyrja.event.provider.contract.ListenerProviderContract;

import java.util.List;

public final class TestQualifiedListenerProviderFixture implements ListenerProviderContract {

    private static final Object HANDLER = null;

    @Override
    public List<Class<?>> getListenerClasses() {
        return List.of(TestListenerFixture.class);
    }

    @Override
    public List<ListenerContract> getListeners() {
        return List.of(
            new io.valkyrja.event.data.Listener(
                TestEventFixture.class, "already.qualified", TestListenerFixture.HANDLER),
            new Listener(TestSamePackageEventFixture.class, "same.package", HANDLER)
        );
    }
}
