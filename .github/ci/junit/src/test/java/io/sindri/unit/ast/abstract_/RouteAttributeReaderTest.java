/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast.abstract_;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.data.HandlerData;
import java.util.Map;
import io.sindri.ast.abstract_.RouteAttributeReader;
import org.junit.jupiter.api.Test;

/** Test the {@link RouteAttributeReader}. */
final class RouteAttributeReaderTest {

    private static final class TestReader extends RouteAttributeReader {
        @Override
        protected String getRouteHandlerAnnotationClass() {
            return "App\\Route";
        }

        HandlerData handler(MethodDeclaration method, String pkg) {
            return updateHandler(method, Map.of(), pkg);
        }
    }

    private static MethodDeclaration parseMethod() {
        return StaticJavaParser.parse("class Controller { void index() {} }")
                .findFirst(MethodDeclaration.class)
                .orElseThrow();
    }

    @Test
    void buildsHandlerWithPackagePrefix() {
        var data = new TestReader().handler(parseMethod(), "app.controller");

        assertEquals("app.controller.Controller", data.handlerClass());
        assertEquals("index", data.method());
    }

    @Test
    void buildsHandlerWithoutPackage() {
        var data = new TestReader().handler(parseMethod(), "");

        assertEquals("Controller", data.handlerClass());
    }
}
