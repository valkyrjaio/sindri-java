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

import com.github.javaparser.ast.expr.NameExpr;
import io.sindri.ast.data.result.ListenerProviderResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test the {@link ListenerProviderResult}. */
final class ListenerProviderResultTest {

    @Test
    void noArgConstructorIsEmpty() {
        assertTrue(new ListenerProviderResult().listenerClasses().isEmpty());
    }

    @Test
    void mergeUnionsClassesAndConcatenatesListeners() {
        var a = new ListenerProviderResult(List.of("A"), List.of(new NameExpr("x")));
        var b = new ListenerProviderResult(List.of("A", "B"), List.of(new NameExpr("y")));

        var merged = a.merge(b);

        assertEquals(List.of("A", "B"), merged.listenerClasses());
        assertEquals(2, merged.listeners().size());
    }
}
