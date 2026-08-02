/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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
