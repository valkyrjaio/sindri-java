package io.sindri.tests.classes.http.provider;

import io.valkyrja.http.routing.data.contract.RouteContract;
import io.valkyrja.http.routing.provider.contract.HttpRouteProviderContract;

import java.util.List;

public final class TestEmptyRouteProviderClass implements HttpRouteProviderContract {

    @Override
    public List<RouteContract> getRoutes() {
        return List.of();
    }
}
