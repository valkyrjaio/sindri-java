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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.*;
import io.sindri.ast.data.result.RouteProviderResult;
import org.junit.jupiter.api.Test;

public class RouteProviderReaderTest {

    private final RouteProviderReader reader = new RouteProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesControllerClasses() {
        RouteProviderResult result =
                reader.readFile(fixturePath("Http/Provider/TestHttpRouteProviderClass.java"));

        assertEquals(1, result.controllerClasses().size());
        assertEquals(
                "io.sindri.tests.fixtures.http.controller.TestHttpControllerClass",
                result.controllerClasses().get(0));
    }

    @Test
    void readFile_noGetControllerClassesMethod_returnsEmpty() {
        RouteProviderResult result =
                reader.readFile(fixturePath("Http/Provider/TestEmptyRouteProviderClass.java"));

        assertTrue(result.controllerClasses().isEmpty());
        assertTrue(result.routes().isEmpty());
    }

    @Test
    void readFile_getControllerClassesThrows_returnsEmpty() {
        RouteProviderResult result =
                reader.readFile(fixturePath("Http/Provider/TestNoReturnRouteProviderClass.java"));

        assertTrue(result.controllerClasses().isEmpty());
        assertTrue(result.routes().isEmpty());
    }

    @Test
    void readFile_emptyFqn_isSkipped() {
        var reader =
                new RouteProviderReader() {
                    @Override
                    protected String extractClassExprFqn(
                            com.github.javaparser.ast.expr.Expression expr,
                            java.util.Map<String, String> importMap,
                            String pkg) {
                        return "";
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Http/Provider/TestHttpRouteProviderClass.java"))
                        .controllerClasses()
                        .isEmpty());
    }

    @Test
    void readFile_readsAndQualifiesProviderRoutes() {
        RouteProviderResult result =
                reader.readFile(fixturePath("Http/Provider/TestRoutesRouteProviderClass.java"));

        assertEquals(4, result.routes().size());
        String first = result.routes().get(0).toString();
        assertTrue(first.contains("new io.valkyrja.http.routing.data.Route("));
        assertTrue(
                first.contains(
                        "io.sindri.tests.fixtures.http.provider.TestRoutesRouteProviderClass::h"));
        assertTrue(first.contains("io.valkyrja.http.message.enum_.RequestMethod.GET"));
        assertTrue(result.routes().get(2).toString().contains("Unmapped.VALUE"));
        assertTrue(result.routes().get(3).toString().contains("new UnmappedRoute("));
    }

    @Test
    void readFile_providerWithoutGetRoutes_hasNoRoutes() {
        RouteProviderResult result =
                reader.readFile(
                        fixturePath("Http/Provider/TestControllersOnlyRouteProviderClass.java"));

        assertTrue(result.routes().isEmpty());
    }

    @Test
    void readFile_noPackageProvider_qualifiesSelfClassWithoutPackage() {
        RouteProviderResult result =
                reader.readFile(fixturePath("Http/Provider/TestNoPackageRouteProviderClass.java"));

        assertEquals(1, result.routes().size());
        assertTrue(
                result.routes().get(0).toString().contains("TestNoPackageRouteProviderClass::h"));
    }

    @Test
    void readFile_nonClassExprItem_isSkipped() {
        var reader =
                new RouteProviderReader() {
                    @Override
                    protected java.util.List<com.github.javaparser.ast.expr.Expression>
                            extractListOfItems(com.github.javaparser.ast.expr.Expression expr) {
                        return java.util.List.of(new com.github.javaparser.ast.expr.NameExpr("x"));
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Http/Provider/TestHttpRouteProviderClass.java"))
                        .controllerClasses()
                        .isEmpty());
    }

    @Test
    void readFile_noTypeDeclaration_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        reader.readFile(
                                fixturePath("Http/Provider/TestNoTypeRouteProviderFile.java")));
    }
}
