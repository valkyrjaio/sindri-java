package io.sindri.tests.fixtures.http.controller;

import io.valkyrja.http.message.enum_.RequestMethod;
import io.valkyrja.http.routing.attribute.Route;

public class TestNoRouteHandlerHttpControllerClass {

    @Route(path = "/no-handler", name = "no.handler", requestMethods = {RequestMethod.GET})
    public static void noHandler() {}
}
