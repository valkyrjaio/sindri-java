package io.sindri.tests.fixtures.component.provider;

import io.valkyrja.application.kernel.contract.ApplicationContract;
import io.valkyrja.application.provider.contract.ComponentProviderContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;

import java.util.List;

public final class TestNoReturnComponentProviderFixture implements ComponentProviderContract {

    @Override
    public List<ServiceProviderContract> getContainerProviders(ApplicationContract app) {
        throw new UnsupportedOperationException();
    }
}
