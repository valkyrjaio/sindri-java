/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.generator.throwable.exception.abstract_;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.sindri.generator.throwable.exception.abstract_.GeneratorInvalidArgumentException;
import org.junit.jupiter.api.Test;

/** Test the {@link GeneratorInvalidArgumentException}. */
final class GeneratorInvalidArgumentExceptionTest {

    @Test
    void messageConstructor() {
        var exception = new GeneratorInvalidArgumentException("message") {};

        assertEquals("message", exception.getMessage());
    }

    @Test
    void messageAndCauseConstructor() {
        var cause = new IllegalStateException("cause");
        var exception = new GeneratorInvalidArgumentException("message", cause) {};

        assertEquals("message", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
