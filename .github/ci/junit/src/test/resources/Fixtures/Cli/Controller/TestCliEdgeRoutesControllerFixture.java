package io.sindri.tests.fixtures.cli.controller;

import io.sindri.tests.fixtures.cli.provider.TestCliRouteProviderFixture;
import io.valkyrja.cli.routing.attribute.Route;
import io.valkyrja.cli.routing.attribute.route.RouteHandler;

public class TestCliEdgeRoutesControllerFixture {

    // No @RouteHandler → null handler; an unknown @Route member exercises the switch default.
    @Route(name = "nohandler", description = "No handler", bogus = "x")
    public static void noHandler() {}

    // An unknown @RouteHandler member exercises the else-if-false path.
    @Route(name = "weird", description = "Weird")
    @RouteHandler(
            handlerClass = TestCliRouteProviderFixture.class,
            handlerMethod = "greetHandler",
            bogus = "y")
    public static void weird() {}

    // A marker @RouteHandler is present but not a NormalAnnotationExpr → null handler.
    @Route(name = "marker", description = "Marker")
    @RouteHandler
    public static void markerHandler() {}
}
