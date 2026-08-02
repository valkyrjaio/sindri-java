/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.provider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.sindri.provider.SindriComponentProvider;
import io.valkyrja.application.kernel.contract.ApplicationContract;
import io.valkyrja.container.manager.Container;
import org.junit.jupiter.api.Test;

/** Test the {@link SindriComponentProvider}. */
final class SindriComponentProviderTest {

    private final SindriComponentProvider provider = new SindriComponentProvider();
    private final ApplicationContract app = mock(ApplicationContract.class);

    @Test
    void exposesAllProviderLists() {
        assertNotNull(provider.getComponentProviders(app));
        assertNotNull(provider.getContainerProviders(app));
        assertNotNull(provider.getEventProviders(app));
        assertNotNull(provider.getCliProviders(app));
        assertNotNull(provider.getHttpProviders(app));
        assertNotNull(provider.getGrpcProviders(app));
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
