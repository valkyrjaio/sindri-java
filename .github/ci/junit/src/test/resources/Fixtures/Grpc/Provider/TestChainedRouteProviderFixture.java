package io.sindri.tests.fixtures.grpc.provider;

import io.valkyrja.grpc.routing.data.Route;
import java.util.List;

/** A route provider that declares a streaming method as a builder chain. */
public final class TestChainedRouteProviderFixture {

    public List<Object> getRoutes() {
        return List.of(
                new Route("/pkg.Ping/Ping", TestChainedRouteProviderFixture::ping),
                new Route("/pkg.Ping/Fanout", TestChainedRouteProviderFixture::fanout)
                        .withServerStreaming(true),
                new Route("/pkg.Ping/Echo", TestChainedRouteProviderFixture::echo)
                        .withClientStreaming(true)
                        .withServerStreaming(true),
                makeRoute(),
                makeRoute().withServerStreaming(true));
    }
}
