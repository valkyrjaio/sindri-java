package io.sindri.tests.fixtures.container.provider;

import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.Map;
import java.util.function.Consumer;

public final class TestMapOtherServiceProviderClass implements ServiceProviderContract {

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        // Map-scoped, but neither of() nor ofEntries().
        return Map.copyOf(Map.of());
    }
}
