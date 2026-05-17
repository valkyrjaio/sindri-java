package io.sindri.tests.classes.http.controller;

import io.sindri.tests.classes.http.provider.TestHttpRouteProviderClass;
import io.valkyrja.http.message.enum_.RequestMethod;
import io.valkyrja.http.routing.attribute.Route;
import io.valkyrja.http.routing.attribute.route.RouteHandler;

public class TestHttpControllerClass {

    @Route(path = "/test", name = "test.get", requestMethods = {RequestMethod.GET})
    @Route(path = "/test", name = "test.head", requestMethods = {RequestMethod.HEAD})
    @RouteHandler(handlerClass = TestHttpRouteProviderClass.class, handlerMethod = "getHandler")
    public static void get() {}

    @Route(path = "/test", name = "test.post", requestMethods = {RequestMethod.POST})
    @RouteHandler(handlerClass = TestHttpRouteProviderClass.class, handlerMethod = "postHandler")
    public static void post() {}
}