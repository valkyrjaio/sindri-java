package io.sindri.tests.fixtures.cli.controller;

import io.sindri.tests.fixtures.cli.provider.TestCliRouteProviderFixture;
import io.valkyrja.cli.routing.attribute.Route;
import io.valkyrja.cli.routing.attribute.route.RouteHandler;
import io.valkyrja.cli.routing.enum_.ArgumentMode;
import io.valkyrja.cli.routing.enum_.ArgumentValueMode;
import io.valkyrja.cli.routing.attribute.ArgumentParameter;

public class TestCliControllerFixture {

    @Route(name = "greet", description = "Greet a user")
    @RouteHandler(handlerClass = TestCliRouteProviderFixture.class, handlerMethod = "greetHandler")
    @ArgumentParameter(name = "name", description = "The name to greet", mode = ArgumentMode.REQUIRED, valueMode = ArgumentValueMode.DEFAULT)
    public static void greet() {}
}
