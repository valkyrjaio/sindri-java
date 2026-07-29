package io.sindri.tests.fixtures.http.controller;

import io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderFixture;
import io.valkyrja.http.routing.attribute.Route;
import io.valkyrja.http.routing.attribute.route.RouteHandler;

public class TestStringRequestMethodHttpControllerFixture {

    @Route(path = "/str", name = "str.get", requestMethods = "GET")
    @RouteHandler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "getHandler")
    public static void get() {}
}
