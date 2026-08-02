/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.constant;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.sindri.constant.SindriInfo;
import org.junit.jupiter.api.Test;

/** Test the {@link SindriInfo} constant holder. */
final class SindriInfoTest {

    @Test
    void exposesConstants() {
        assertNotNull(SindriInfo.VERSION);
        assertNotNull(new SindriInfo());
    }
}
