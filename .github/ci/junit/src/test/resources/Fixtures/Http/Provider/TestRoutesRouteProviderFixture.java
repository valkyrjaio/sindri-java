package io.sindri.tests.fixtures.http.provider;

import io.valkyrja.http.message.enum_.RequestMethod;
import io.valkyrja.http.routing.data.Route;
import java.util.List;

public class TestRoutesRouteProviderFixture {

    public List<Object> getRoutes() {
        return List.of(
                // Mapped types (Route, RequestMethod, List) + a self-class reference.
                new Route("/a", "a", TestRoutesRouteProviderFixture::h, List.of(RequestMethod.GET)),
                // Already-fully-qualified type (scope present, left alone).
                new io.valkyrja.http.routing.data.Route("/b", "b", TestRoutesRouteProviderFixture::h),
                // Unmapped name reference (left as-is).
                new Route("/c", "c", Unmapped.VALUE),
                // Unmapped simple type (no FQN available, left as-is).
                new UnmappedRoute("/e", "e"));
    }
}
