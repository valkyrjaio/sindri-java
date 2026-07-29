/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sindri.ast.data.HandlerData;
import org.junit.jupiter.api.Test;

/** Test the {@link HandlerData}. */
final class HandlerDataTest {

    @Test
    void exposesHandlerClassAndMethod() {
        var data = new HandlerData("App\\Controller", "index");

        assertEquals("App\\Controller", data.handlerClass());
        assertEquals("index", data.method());
    }
}
