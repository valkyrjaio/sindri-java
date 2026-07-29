/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sindri.ast.*;
import io.sindri.ast.throwable.exception.AstFileReadException;
import org.junit.jupiter.api.Test;

public class AstFileReadExceptionTest {

    @Test
    void constructor_withMessage_storesMessage() {
        AstFileReadException ex = new AstFileReadException("error msg");

        assertEquals("error msg", ex.getMessage());
    }
}
