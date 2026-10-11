package io.sindri.tests.fixtures.event.provider;

import io.sindri.tests.fixtures.event.TestEventFixture;
import io.sindri.tests.fixtures.event.TestListenerFixture;
import io.valkyrja.event.data.Listener;
import io.valkyrja.event.data.contract.ListenerContract;
import io.valkyrja.event.provider.contract.ListenerProviderContract;

import java.util.List;

public final class TestChainedListenerProviderFixture implements ListenerProviderContract {

    @Override
    public List<Class<?>> getListenerClasses() {
        return List.of(TestListenerFixture.class);
    }

    @Override
    public List<ListenerContract> getListeners() {
        return List.of(
            new Listener(TestEventFixture.class, "ctor.name", TestListenerFixture::onUserCreated)
                .withName("first")
                .withName("last"),
            new Listener(TestEventFixture.class, "handler.only", TestListenerFixture::onUserCreated)
                .withHandler(TestListenerFixture::onUserDeleted),
            makeListener()
        );
    }

    private ListenerContract makeListener() {
        return new Listener(
            TestEventFixture.class, "factory.made", TestListenerFixture::onUserCreated);
    }
}
