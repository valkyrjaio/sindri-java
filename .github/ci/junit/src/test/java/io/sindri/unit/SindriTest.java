/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.sindri.Sindri;
import io.valkyrja.cli.server.support.Exiter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Test the {@link Sindri} entry point. */
final class SindriTest {

    @Test
    void mainBootsTheCliWithoutExitingTheJvm() {
        var originalOut = System.out;
        var originalIn = System.in;
        Exiter.freeze();
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        System.setIn(new ByteArrayInputStream(new byte[0]));
        try {
            assertDoesNotThrow(() -> Sindri.main(new String[] {"list"}));
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
            Exiter.unfreeze();
        }
    }
}
