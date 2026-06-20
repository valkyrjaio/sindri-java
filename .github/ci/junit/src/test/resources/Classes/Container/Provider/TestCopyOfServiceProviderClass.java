package io.sindri.tests.classes.container.provider;

import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public final class TestCopyOfServiceProviderClass implements ServiceProviderContract {

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        // A non-Map-scoped call exercises the `scope.equals("Map")` false arm.
        return Collections.emptyMap();
    }
}
