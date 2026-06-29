package io.sindri.tests.fixtures.container.provider;

import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.Map;
import java.util.function.Consumer;

public final class TestFieldPublishersServiceProviderClass implements ServiceProviderContract {

    private static final Map<Class<?>, Consumer<ContainerContract>> PUBLISHERS = Map.of();

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        return PUBLISHERS;
    }
}
