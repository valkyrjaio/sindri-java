/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.ListenerAttributeReader;
import org.junit.jupiter.api.Test;

/** Test the {@link ListenerAttributeReader}. */
final class ListenerAttributeReaderTest {

    @Test
    void readFileReturnsEmptyResult() {
        assertTrue(new ListenerAttributeReader().readFile("ignored").listeners().isEmpty());
    }
}
