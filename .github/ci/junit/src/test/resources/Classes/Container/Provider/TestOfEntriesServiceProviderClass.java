package io.sindri.tests.classes.container.provider;

import io.sindri.tests.classes.container.TestService;
import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.*;
import static java.util.Map.entry;
import java.util.Map;
import java.util.function.Consumer;

public final class TestOfEntriesServiceProviderClass implements ServiceProviderContract {

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        return Map.ofEntries(
            Map.entry(TestService.class, TestOfEntriesServiceProviderClass::publish)
        );
    }

    public static void publish(ContainerContract container) {}
}
