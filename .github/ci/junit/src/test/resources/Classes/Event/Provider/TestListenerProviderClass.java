package io.sindri.tests.classes.event.provider;

import io.sindri.tests.classes.event.TestEventClass;
import io.sindri.tests.classes.event.TestListenerClass;
import io.valkyrja.event.data.Listener;
import io.valkyrja.event.data.contract.ListenerContract;
import io.valkyrja.event.provider.contract.ListenerProviderContract;

import java.util.List;

public final class TestListenerProviderClass implements ListenerProviderContract {

    @Override
    public List<Class<?>> getListenerClasses() {
        return List.of(TestListenerClass.class);
    }

    @Override
    public List<ListenerContract> getListeners() {
        return List.of(
            new Listener(TestEventClass.class, "user.created", TestListenerClass::onUserCreated)
        );
    }
}