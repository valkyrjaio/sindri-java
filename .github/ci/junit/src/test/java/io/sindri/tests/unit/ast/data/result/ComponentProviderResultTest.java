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

import io.sindri.ast.data.result.ComponentProviderResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test the {@link ComponentProviderResult}. */
final class ComponentProviderResultTest {

    @Test
    void noArgConstructorIsEmpty() {
        assertTrue(new ComponentProviderResult().componentProviders().isEmpty());
    }

    @Test
    void mergeUnionsEachListWithoutDuplicates() {
        var a =
                new ComponentProviderResult(
                        List.of("c1"),
                        List.of("s1"),
                        List.of("l1"),
                        List.of("cr1"),
                        List.of("hr1"),
                        List.of("gr1"));
        var b =
                new ComponentProviderResult(
                        List.of("c1", "c2"),
                        List.of("s2"),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of("gr2"));

        var merged = a.merge(b);

        assertEquals(List.of("c1", "c2"), merged.componentProviders());
        assertEquals(List.of("s1", "s2"), merged.serviceProviders());
        assertEquals(List.of("l1"), merged.listenerProviders());
        assertEquals(List.of("gr1", "gr2"), merged.grpcRouteProviders());
    }
}
