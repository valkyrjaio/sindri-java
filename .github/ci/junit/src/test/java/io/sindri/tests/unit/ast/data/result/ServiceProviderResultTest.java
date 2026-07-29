/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast.data.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.data.result.ServiceProviderResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link ServiceProviderResult}. */
final class ServiceProviderResultTest {

    @Test
    void noArgConstructorIsEmpty() {
        assertTrue(new ServiceProviderResult().serviceClasses().isEmpty());
    }

    @Test
    void mergeUnionsClassesAndCombinesPublishers() {
        var a = new ServiceProviderResult(List.of("A"), Map.of("k1", new String[] {"v1"}));
        var b = new ServiceProviderResult(List.of("A", "B"), Map.of("k2", new String[] {"v2"}));

        var merged = a.merge(b);

        assertEquals(List.of("A", "B"), merged.serviceClasses());
        assertEquals(2, merged.publishers().size());
    }
}
