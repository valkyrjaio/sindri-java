/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.sindri.provider.SindriCliRouteProvider;
import io.valkyrja.cli.interaction.output.factory.OutputFactory;
import io.valkyrja.cli.interaction.output.factory.contract.OutputFactoryContract;
import io.valkyrja.cli.routing.data.contract.ArgumentParameterContract;
import io.valkyrja.cli.routing.data.contract.RouteContract;
import io.valkyrja.container.manager.Container;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Test the {@link SindriCliRouteProvider}. */
final class SindriCliRouteProviderTest {

    @Test
    void exposesCommandControllerAndRoute() {
        var provider = new SindriCliRouteProvider();

        assertEquals(1, provider.getControllerClasses().size());
        assertFalse(provider.getRoutes().isEmpty());
    }

    @Test
    void generateHandlerDelegatesToCommand() {
        var container = new Container();
        container.setSingleton(OutputFactoryContract.class, new OutputFactory());
        var route = mock(RouteContract.class);
        var argument = mock(ArgumentParameterContract.class);
        when(route.getArgument("config")).thenReturn(argument);
        when(argument.getFirstValue()).thenReturn("/does/not/exist/Config.java");

        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            assertNotNull(SindriCliRouteProvider.generateHandler(container, route));
        } finally {
            System.setOut(original);
        }
    }
}
