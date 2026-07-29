package io.sindri.tests.fixtures.http.controller;

import io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderFixture;
import io.valkyrja.http.routing.attribute.Route;
import io.valkyrja.http.routing.attribute.route.RouteHandler;

import static io.valkyrja.http.message.enum_.RequestMethod.GET;

public class TestSingleRequestMethodHttpControllerFixture {

    @Route(path = "/single", name = "single.get", requestMethods = GET)
    @RouteHandler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "getHandler")
    public static void get() {}
}
