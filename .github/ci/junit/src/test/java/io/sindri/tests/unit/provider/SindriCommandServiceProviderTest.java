/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.provider;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sindri.provider.SindriCommandServiceProvider;
import io.valkyrja.container.manager.Container;
import org.junit.jupiter.api.Test;

/** Test the {@link SindriCommandServiceProvider}. */
final class SindriCommandServiceProviderTest {

    @Test
    void publishersExposesCommand() {
        assertEquals(1, new SindriCommandServiceProvider().publishers().size());
    }

    @Test
    void publishIsANoOp() {
        assertDoesNotThrow(
                () ->
                        SindriCommandServiceProvider.publishGenerateDataFromConfigCommand(
                                new Container()));
    }
}
