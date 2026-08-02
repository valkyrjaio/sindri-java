/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.generator.enum_;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.sindri.generator.enum_.GenerateStatus;
import org.junit.jupiter.api.Test;

/** Test the {@link GenerateStatus}. */
final class GenerateStatusTest {

    @Test
    void exposesAllConstants() {
        assertEquals(3, GenerateStatus.values().length);
        assertSame(GenerateStatus.SUCCESS, GenerateStatus.valueOf("SUCCESS"));
        assertSame(GenerateStatus.FAILURE, GenerateStatus.valueOf("FAILURE"));
        assertSame(GenerateStatus.SKIPPED, GenerateStatus.valueOf("SKIPPED"));
    }
}
