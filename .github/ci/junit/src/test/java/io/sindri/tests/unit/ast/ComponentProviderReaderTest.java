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
import io.sindri.ast.data.result.ComponentProviderResult;
import org.junit.jupiter.api.Test;

public class ComponentProviderReaderTest {

    private final ComponentProviderReader reader = new ComponentProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesServiceProviders() {
        ComponentProviderResult data =
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertEquals(1, data.serviceProviders().size());
        assertEquals(
                "io.sindri.tests.fixtures.container.provider.TestServiceProviderClass",
                data.serviceProviders().get(0));
    }

    @Test
    void readFile_parsesListenerProviders() {
        ComponentProviderResult data =
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertEquals(1, data.listenerProviders().size());
        assertEquals(
                "io.sindri.tests.fixtures.event.provider.TestListenerProviderClass",
                data.listenerProviders().get(0));
    }

    @Test
    void readFile_parsesHttpRouteProviders() {
        ComponentProviderResult data =
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertEquals(1, data.httpRouteProviders().size());
        assertEquals(
                "io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderClass",
                data.httpRouteProviders().get(0));
    }

    @Test
    void readFile_parsesGrpcRouteProviders() {
        ComponentProviderResult data =
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertEquals(1, data.grpcRouteProviders().size());
        assertEquals(
                "io.sindri.tests.fixtures.grpc.provider.TestGrpcRouteProviderClass",
                data.grpcRouteProviders().get(0));
    }

    @Test
    void readFile_emptyComponentAndCliRouteProviders() {
        ComponentProviderResult data =
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"));

        assertTrue(data.componentProviders().isEmpty());
        assertTrue(data.cliRouteProviders().isEmpty());
    }

    @Test
    void readFile_missingMethods_returnsEmptyForMissingOnes() {
        ComponentProviderResult data =
                reader.readFile(
                        fixturePath("Component/Provider/TestMinimalComponentProviderClass.java"));

        assertTrue(data.componentProviders().isEmpty());
        assertTrue(data.listenerProviders().isEmpty());
        assertTrue(data.cliRouteProviders().isEmpty());
        assertTrue(data.httpRouteProviders().isEmpty());
        assertEquals(1, data.serviceProviders().size());
    }

    @Test
    void readFile_methodWithNoReturn_returnsEmpty() {
        ComponentProviderResult data =
                reader.readFile(
                        fixturePath("Component/Provider/TestNoReturnComponentProviderClass.java"));

        assertTrue(data.serviceProviders().isEmpty());
    }

    @Test
    void readFile_emptyFqn_isSkipped() {
        var reader =
                new ComponentProviderReader() {
                    @Override
                    protected String extractObjectCreationFqn(
                            com.github.javaparser.ast.expr.Expression expr,
                            java.util.Map<String, String> importMap,
                            String pkg) {
                        return "";
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"))
                        .serviceProviders()
                        .isEmpty());
    }

    @Test
    void readFile_nonObjectCreationItem_isSkipped() {
        var reader =
                new ComponentProviderReader() {
                    @Override
                    protected java.util.List<com.github.javaparser.ast.expr.Expression>
                            extractListOfItems(com.github.javaparser.ast.expr.Expression expr) {
                        return java.util.List.of(new com.github.javaparser.ast.expr.NameExpr("x"));
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderClass.java"))
                        .serviceProviders()
                        .isEmpty());
    }

    @Test
    void readFile_noTypeDeclaration_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Component/Provider/TestNoTypeComponentProviderFile.java")));
    }
}
