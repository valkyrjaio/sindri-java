package io.sindri.tests.classes.container.provider;

import io.sindri.tests.classes.container.TestService;
import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.Map;
import java.util.function.Consumer;

public final class TestMixedPublishersServiceProviderClass implements ServiceProviderContract {

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        return Map.of(
            "not.a.class.expr", TestMixedPublishersServiceProviderClass::publish,
            TestService.class, (ContainerContract c) -> {}
        );
    }

    public static void publish(ContainerContract container) {}
}
