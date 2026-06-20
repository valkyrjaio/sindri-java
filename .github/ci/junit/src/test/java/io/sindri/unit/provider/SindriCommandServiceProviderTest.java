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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.valkyrja.container.manager.Container;
import io.sindri.provider.SindriCommandServiceProvider;
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
