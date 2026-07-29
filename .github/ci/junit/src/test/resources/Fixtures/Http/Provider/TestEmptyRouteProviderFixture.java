package io.sindri.tests.fixtures.http.provider;

import io.valkyrja.http.routing.data.contract.RouteContract;
import io.valkyrja.http.routing.provider.contract.HttpRouteProviderContract;

import java.util.List;

public final class TestEmptyRouteProviderFixture implements HttpRouteProviderContract {

    @Override
    public List<RouteContract> getRoutes() {
        return List.of();
    }
}
