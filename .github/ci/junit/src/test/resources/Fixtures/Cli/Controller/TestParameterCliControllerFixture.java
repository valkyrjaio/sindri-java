package io.sindri.tests.fixtures.cli.controller;

import io.sindri.tests.fixtures.cli.provider.TestCliRouteProviderFixture;
import io.valkyrja.cli.routing.attribute.ArgumentParameter;
import io.valkyrja.cli.routing.attribute.OptionParameter;
import io.valkyrja.cli.routing.attribute.Route;
import io.valkyrja.cli.routing.attribute.route.RouteHandler;
import io.valkyrja.cli.routing.enum_.ArgumentMode;
import io.valkyrja.cli.routing.enum_.ArgumentValueMode;
import io.valkyrja.cli.routing.enum_.OptionMode;
import io.valkyrja.cli.routing.enum_.OptionValueMode;

public class TestParameterCliControllerFixture {

    @Route(name = "build", description = "Build it")
    @RouteHandler(handlerClass = TestCliRouteProviderFixture.class, handlerMethod = "greetHandler")
    @ArgumentParameter(
            name = "target",
            description = "The target",
            mode = ArgumentMode.REQUIRED,
            valueMode = ArgumentValueMode.DEFAULT)
    @ArgumentParameter(
            name = "rest",
            description = "The rest",
            mode = ArgumentMode.OPTIONAL,
            valueMode = ArgumentValueMode.ARRAY)
    @OptionParameter(
            name = "format",
            description = "The format",
            valueDisplayName = "fmt",
            defaultValue = "json",
            shortNames = {"f"},
            validValues = {"json", "xml"},
            mode = OptionMode.REQUIRED,
            valueMode = OptionValueMode.DEFAULT)
    @OptionParameter(
            name = "flag",
            description = "A flag",
            mode = OptionMode.OPTIONAL,
            valueMode = OptionValueMode.NONE)
    public static void build() {}
}
