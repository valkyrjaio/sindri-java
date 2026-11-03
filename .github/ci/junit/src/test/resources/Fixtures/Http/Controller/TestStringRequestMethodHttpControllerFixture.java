package io.sindri.tests.fixtures.http.controller;

import io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderFixture;
import io.valkyrja.http.routing.attribute.Route;
import io.valkyrja.http.routing.attribute.route.Handler;

public class TestStringRequestMethodHttpControllerFixture {

    @Route(path = "/str", name = "str.get", requestMethods = "GET")
    @Handler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "getHandler")
    public static void get() {}
}
