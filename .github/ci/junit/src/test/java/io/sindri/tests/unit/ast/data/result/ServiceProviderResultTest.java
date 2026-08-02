/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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
