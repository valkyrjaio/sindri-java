package io.sindri.tests.fixtures.event.provider;

import io.sindri.tests.fixtures.event.TestListenerFixture;
import io.valkyrja.event.data.Listener;
import io.valkyrja.event.data.contract.ListenerContract;
import io.valkyrja.event.provider.contract.ListenerProviderContract;

import java.util.List;

public final class TestNonClassEventIdListenerProviderFixture
    implements ListenerProviderContract {

    private static final Class<?> EVENT = Object.class;

    @Override
    public List<Class<?>> getListenerClasses() {
        return List.of(TestListenerFixture.class);
    }

    @Override
    public List<ListenerContract> getListeners() {
        return List.of(
            new Listener(EVENT, "user.created", TestListenerFixture::onUserCreated)
        );
    }
}
