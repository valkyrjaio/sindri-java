/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast;

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
