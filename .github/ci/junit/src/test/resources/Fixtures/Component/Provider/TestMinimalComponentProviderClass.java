package io.sindri.tests.fixtures.component.provider;

import io.sindri.tests.fixtures.container.provider.TestServiceProviderClass;
import io.valkyrja.application.kernel.contract.ApplicationContract;
import io.valkyrja.application.provider.contract.ComponentProviderContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.List;

public final class TestMinimalComponentProviderClass implements ComponentProviderContract {

    @Override
    public List<ServiceProviderContract> getContainerProviders(ApplicationContract app) {
        return List.of(new TestServiceProviderClass());
    }
}
