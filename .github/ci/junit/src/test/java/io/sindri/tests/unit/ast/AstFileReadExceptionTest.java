/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sindri.ast.*;
import io.sindri.ast.throwable.exception.AstFileReadException;
import org.junit.jupiter.api.Test;

public final class AstFileReadExceptionTest {

    @Test
    void constructor_withMessage_storesMessage() {
        AstFileReadException ex = new AstFileReadException("error msg");

        assertEquals("error msg", ex.getMessage());
    }
}
