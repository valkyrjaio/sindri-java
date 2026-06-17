package io.sindri.tests.classes.http.controller;

import io.sindri.tests.classes.http.provider.TestHttpRouteProviderClass;
import io.valkyrja.http.routing.attribute.Route;
import io.valkyrja.http.routing.attribute.route.RouteHandler;

public class TestStringRequestMethodHttpControllerClass {

    @Route(path = "/str", name = "str.get", requestMethods = "GET")
    @RouteHandler(handlerClass = TestHttpRouteProviderClass.class, handlerMethod = "getHandler")
    public static void get() {}
}
