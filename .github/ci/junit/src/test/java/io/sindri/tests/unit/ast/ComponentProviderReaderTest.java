/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.*;
import io.sindri.ast.data.result.ComponentProviderResult;
import org.junit.jupiter.api.Test;

public final class ComponentProviderReaderTest {

    private final ComponentProviderReader reader = new ComponentProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesServiceProviders() {
        ComponentProviderResult data =
                reader.readFile(
                        fixturePath("Component/Provider/TestComponentProviderFixture.java"));

        assertEquals(1, data.serviceProviders().size());
        assertEquals(
                "io.sindri.tests.fixtures.container.provider.TestServiceProviderFixture",
                data.serviceProviders().get(0));
    }

    @Test
    void readFile_parsesListenerProviders() {
        ComponentProviderResult data =
                reader.readFile(
                        fixturePath("Component/Provider/TestComponentProviderFixture.java"));

        assertEquals(1, data.listenerProviders().size());
        assertEquals(
                "io.sindri.tests.fixtures.event.provider.TestListenerProviderFixture",
                data.listenerProviders().get(0));
    }

    @Test
    void readFile_parsesHttpRouteProviders() {
        ComponentProviderResult data =
                reader.readFile(
                        fixturePath("Component/Provider/TestComponentProviderFixture.java"));

        assertEquals(1, data.httpRouteProviders().size());
        assertEquals(
                "io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderFixture",
                data.httpRouteProviders().get(0));
    }

    @Test
    void readFile_parsesGrpcRouteProviders() {
        ComponentProviderResult data =
                reader.readFile(
                        fixturePath("Component/Provider/TestComponentProviderFixture.java"));

        assertEquals(1, data.grpcRouteProviders().size());
        assertEquals(
                "io.sindri.tests.fixtures.grpc.provider.TestGrpcRouteProviderFixture",
                data.grpcRouteProviders().get(0));
    }

    @Test
    void readFile_emptyComponentAndCliRouteProviders() {
        ComponentProviderResult data =
                reader.readFile(
                        fixturePath("Component/Provider/TestComponentProviderFixture.java"));

        assertTrue(data.componentProviders().isEmpty());
        assertTrue(data.cliRouteProviders().isEmpty());
    }

    @Test
    void readFile_missingMethods_returnsEmptyForMissingOnes() {
        ComponentProviderResult data =
                reader.readFile(
                        fixturePath("Component/Provider/TestMinimalComponentProviderFixture.java"));

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
                        fixturePath(
                                "Component/Provider/TestNoReturnComponentProviderFixture.java"));

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
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderFixture.java"))
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
                reader.readFile(fixturePath("Component/Provider/TestComponentProviderFixture.java"))
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
                                        "Component/Provider/TestNoTypeComponentProviderFileFixture.java")));
    }
}
