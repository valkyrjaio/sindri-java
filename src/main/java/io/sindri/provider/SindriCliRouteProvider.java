/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.provider;

import io.sindri.cli.command.GenerateDataFromConfigCommand;
import io.valkyrja.cli.interaction.output.contract.OutputContract;
import io.valkyrja.cli.routing.data.ArgumentParameter;
import io.valkyrja.cli.routing.data.Route;
import io.valkyrja.cli.routing.data.contract.RouteContract;
import io.valkyrja.cli.routing.enum_.ArgumentMode;
import io.valkyrja.cli.routing.enum_.ArgumentValueMode;
import io.valkyrja.cli.routing.provider.contract.CliRouteProviderContract;
import io.valkyrja.container.manager.contract.ContainerContract;
import java.util.ArrayList;
import java.util.List;

public final class SindriCliRouteProvider implements CliRouteProviderContract {

    public static OutputContract generateHandler(ContainerContract container, RouteContract route) {
        return new GenerateDataFromConfigCommand(container, route).execute();
    }

    @Override
    public List<Class<?>> getControllerClasses() {
        return List.of(GenerateDataFromConfigCommand.class);
    }

    @Override
    public List<RouteContract> getRoutes() {
        return List.of(
                new Route(
                                "generate",
                                "Generate data from config",
                                SindriCliRouteProvider::generateHandler)
                        .withAddedArguments(
                                new ArgumentParameter(
                                        "config",
                                        "The path to the application config file",
                                        ArgumentMode.REQUIRED,
                                        ArgumentValueMode.DEFAULT,
                                        new ArrayList<>())));
    }
}
