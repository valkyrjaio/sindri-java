/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.provider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.valkyrja.application.kernel.contract.ApplicationContract;
import io.valkyrja.container.manager.Container;
import io.sindri.provider.SindriComponentProvider;
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

        assertNotNull(
                container.getSingleton(io.sindri.ast.contract.ConfigReaderContract.class));
    }
}
