package io.sindri.tests.fixtures.container.provider;

import io.sindri.tests.fixtures.container.TestService;
import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.Map;
import java.util.function.Consumer;

public final class TestBadOfEntriesServiceProviderClass implements ServiceProviderContract {

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        return Map.ofEntries(
            notAnEntry,
            Map.of(TestService.class),
            Map.entry(TestService.class)
        );
    }
}
