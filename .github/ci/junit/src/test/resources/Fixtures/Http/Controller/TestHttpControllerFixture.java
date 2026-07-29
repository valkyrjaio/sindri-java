package io.sindri.tests.fixtures.http.controller;

import io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderFixture;
import io.valkyrja.http.message.enum_.RequestMethod;
import io.valkyrja.http.routing.attribute.Route;
import io.valkyrja.http.routing.attribute.route.RouteHandler;

public class TestHttpControllerFixture {

    @Route(path = "/test", name = "test.get", requestMethods = {RequestMethod.GET})
    @Route(path = "/test", name = "test.head", requestMethods = {RequestMethod.HEAD})
    @RouteHandler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "getHandler")
    public static void get() {}

    @Route(path = "/test", name = "test.post", requestMethods = {RequestMethod.POST})
    @RouteHandler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "postHandler")
    public static void post() {}
}
