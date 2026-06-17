package io.sindri.tests.classes.container.provider;

import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.Map;
import java.util.function.Consumer;

public final class TestNoReturnServiceProviderClass implements ServiceProviderContract {

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        throw new UnsupportedOperationException();
    }
}
