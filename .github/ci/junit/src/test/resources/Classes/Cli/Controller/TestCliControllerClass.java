package io.sindri.tests.classes.cli.controller;

import io.sindri.tests.classes.cli.provider.TestCliRouteProviderClass;
import io.valkyrja.cli.routing.attribute.Route;
import io.valkyrja.cli.routing.attribute.route.RouteHandler;
import io.valkyrja.cli.routing.enum_.ArgumentMode;
import io.valkyrja.cli.routing.enum_.ArgumentValueMode;
import io.valkyrja.cli.routing.attribute.ArgumentParameter;

public class TestCliControllerClass {

    @Route(name = "greet", description = "Greet a user")
    @RouteHandler(handlerClass = TestCliRouteProviderClass.class, handlerMethod = "greetHandler")
    @ArgumentParameter(name = "name", description = "The name to greet", mode = ArgumentMode.REQUIRED, valueMode = ArgumentValueMode.DEFAULT)
    public static void greet() {}
}
