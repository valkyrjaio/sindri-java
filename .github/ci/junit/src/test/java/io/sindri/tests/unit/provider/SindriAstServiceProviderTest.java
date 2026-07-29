/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.sindri.ast.contract.ConfigReaderContract;
import io.sindri.provider.SindriAstServiceProvider;
import io.valkyrja.container.manager.Container;
import org.junit.jupiter.api.Test;

/** Test the {@link SindriAstServiceProvider}. */
final class SindriAstServiceProviderTest {

    @Test
    void publishersExposesAllReaderAndGeneratorBindings() {
        assertEquals(15, new SindriAstServiceProvider().publishers().size());
    }

    @Test
    void publishContainerDataBindsEveryService() {
        var container = new Container();

        SindriAstServiceProvider.publishContainerData(container);

        assertNotNull(container.getSingleton(ConfigReaderContract.class));
    }
}
