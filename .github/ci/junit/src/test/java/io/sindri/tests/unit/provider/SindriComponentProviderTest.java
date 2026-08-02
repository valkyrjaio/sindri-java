/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.sindri.provider.SindriAstServiceProvider;
import io.sindri.provider.SindriCliRouteProvider;
import io.sindri.provider.SindriCommandServiceProvider;
import io.sindri.provider.SindriComponentProvider;
import io.valkyrja.application.kernel.contract.ApplicationContract;
import io.valkyrja.application.provider.CliApplicationComponentProvider;
import io.valkyrja.container.manager.Container;
import org.junit.jupiter.api.Test;

/** Test the {@link SindriComponentProvider}. */
final class SindriComponentProviderTest {

    private final SindriComponentProvider provider = new SindriComponentProvider();
    private final ApplicationContract app = mock(ApplicationContract.class);

    @Test
    void getComponentProvidersReturnsTheComponentProviders() {
        var providers = provider.getComponentProviders(app);

        assertEquals(1, providers.size());
        assertInstanceOf(CliApplicationComponentProvider.class, providers.get(0));
    }

    @Test
    void getContainerProvidersReturnsTheServiceProviders() {
        var providers = provider.getContainerProviders(app);

        assertEquals(2, providers.size());
        assertInstanceOf(SindriAstServiceProvider.class, providers.get(0));
        assertInstanceOf(SindriCommandServiceProvider.class, providers.get(1));
    }

    @Test
    void getEventProvidersIsEmpty() {
        assertTrue(provider.getEventProviders(app).isEmpty());
    }

    @Test
    void getCliProvidersReturnsTheCliRouteProviders() {
        var providers = provider.getCliProviders(app);

        assertEquals(1, providers.size());
        assertInstanceOf(SindriCliRouteProvider.class, providers.get(0));
    }

    @Test
    void getHttpProvidersIsEmpty() {
        assertTrue(provider.getHttpProviders(app).isEmpty());
    }

    @Test
    void getGrpcProvidersIsEmpty() {
        assertTrue(provider.getGrpcProviders(app).isEmpty());
    }

    @Test
    void publishSkipsInDebugMode() {
        when(app.getDebugMode()).thenReturn(true);

        SindriComponentProvider.publish(app);
    }

    @Test
    void publishRegistersContainerDataWhenNotDebug() {
        var container = new Container();
        when(app.getDebugMode()).thenReturn(false);
        when(app.getContainer()).thenReturn(container);

        SindriComponentProvider.publish(app);

        assertNotNull(container.getSingleton(io.sindri.ast.contract.ConfigReaderContract.class));
    }
}
