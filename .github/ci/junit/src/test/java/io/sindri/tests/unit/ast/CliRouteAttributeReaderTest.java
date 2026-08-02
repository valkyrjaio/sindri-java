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
import io.sindri.ast.data.result.CliRouteAttributeResult;
import org.junit.jupiter.api.Test;

public final class CliRouteAttributeReaderTest {

    private final CliRouteAttributeReader reader = new CliRouteAttributeReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesRouteCount() {
        CliRouteAttributeResult result =
                reader.readFile(fixturePath("Cli/Controller/TestCliControllerFixture.java"));

        assertEquals(1, result.routes().size());
    }

    @Test
    void readFile_parsesRouteName() {
        CliRouteAttributeResult result =
                reader.readFile(fixturePath("Cli/Controller/TestCliControllerFixture.java"));

        assertTrue(result.routes().containsKey("greet"));
    }

    @Test
    void readFile_withNoRouteMethod_returnsEmpty() {
        CliRouteAttributeResult result =
                reader.readFile(fixturePath("Cli/Controller/TestNoRouteCliControllerFixture.java"));

        assertTrue(result.routes().isEmpty());
    }

    @Test
    void readFile_markerRoute_isSkipped() {
        CliRouteAttributeResult result =
                reader.readFile(fixturePath("Cli/Controller/TestEdgeCliControllerFixture.java"));

        assertTrue(result.routes().isEmpty());
    }

    @Test
    void readFile_buildsRouteSupplierWithHandler() {
        CliRouteAttributeResult result =
                reader.readFile(fixturePath("Cli/Controller/TestCliControllerFixture.java"));

        String supplier = result.routes().get("greet").toString();
        assertTrue(supplier.contains("() -> new io.valkyrja.cli.routing.data.Route(\"greet\","));
        assertTrue(
                supplier.contains(
                        "io.sindri.tests.fixtures.cli.provider.TestCliRouteProviderFixture::greetHandler"));
    }

    @Test
    void readFile_edgeRoutes_handlerlessAndUnknownMembers() {
        CliRouteAttributeResult result =
                reader.readFile(
                        fixturePath("Cli/Controller/TestCliEdgeRoutesControllerFixture.java"));

        assertEquals(3, result.routes().size());
        // No @RouteHandler → the supplier uses a null handler.
        assertTrue(result.routes().get("nohandler").toString().contains(", null)"));
        assertTrue(result.routes().get("weird").toString().contains("greetHandler"));
        // Marker @RouteHandler (not a NormalAnnotationExpr) → null handler too.
        assertTrue(result.routes().get("marker").toString().contains(", null)"));
    }

    @Test
    void readFile_classifiesMiddlewareIntoStagesAndEmitsThemPreSorted() {
        // Auth is a RouteMatched stage; Audit is the terminal Exited stage.
        var middlewareSources =
                java.util.Map.of(
                        "io.sindri.tests.fixtures.cli.middleware.AuthMiddleware",
                        "package io.sindri.tests.fixtures.cli.middleware;"
                                + " import io.valkyrja.cli.middleware.contract.RouteMatchedMiddlewareContract;"
                                + " public class AuthMiddleware implements RouteMatchedMiddlewareContract {}",
                        "io.sindri.tests.fixtures.cli.middleware.AuditMiddleware",
                        "package io.sindri.tests.fixtures.cli.middleware;"
                                + " import io.valkyrja.cli.middleware.contract.ProcessExitingMiddlewareContract;"
                                + " public class AuditMiddleware implements ProcessExitingMiddlewareContract {}");
        var mwReader =
                new CliRouteAttributeReader(
                        fqn ->
                                java.util.Optional.ofNullable(middlewareSources.get(fqn))
                                        .map(com.github.javaparser.StaticJavaParser::parse));

        String supplier =
                mwReader.readFile(
                                fixturePath(
                                        "Cli/Controller/TestMiddlewareCliControllerFixture.java"))
                        .routes()
                        .get("guarded")
                        .toString();

        // routeMatched=[Auth], routeDispatched=[], throwableCaught=[], exited=[Audit].
        org.junit.jupiter.api.Assertions.assertTrue(
                supplier.contains(
                        "null, java.util.List.of(io.sindri.tests.fixtures.cli.middleware.AuthMiddleware.class),"
                                + " java.util.List.of(), java.util.List.of(),"
                                + " java.util.List.of(io.sindri.tests.fixtures.cli.middleware.AuditMiddleware.class),"
                                + " java.util.List.of(), java.util.List.of()"));
    }

    @Test
    void readFile_withoutAResolverEmitsTheShortConstructor() {
        String supplier =
                reader.readFile(
                                fixturePath(
                                        "Cli/Controller/TestMiddlewareCliControllerFixture.java"))
                        .routes()
                        .get("guarded")
                        .toString();

        org.junit.jupiter.api.Assertions.assertFalse(supplier.contains("java.util.List.of("));
    }

    @Test
    void readFile_noTypeDeclaration_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Cli/Controller/TestNoTypeCliControllerFileFixture.java")));
    }

    @Test
    void readFile_emitsTheArgumentAndOptionParametersACommandDeclares() {
        CliRouteAttributeResult result =
                reader.readFile(
                        fixturePath("Cli/Controller/TestParameterCliControllerFixture.java"));

        String supplier = result.routes().get("build").toString();

        // Declaring parameters forces the full constructor, whose null helpText and empty
        // middleware match the short constructor's defaults.
        assertTrue(supplier.contains(", null, "));

        assertTrue(
                supplier.contains(
                        "new io.valkyrja.cli.routing.data.ArgumentParameter(\"target\", \"The"
                                + " target\", io.valkyrja.cli.routing.enum_.ArgumentMode.REQUIRED,"
                                + " io.valkyrja.cli.routing.enum_.ArgumentValueMode.DEFAULT,"
                                + " java.util.List.of())"));
        assertTrue(
                supplier.contains(
                        "new io.valkyrja.cli.routing.data.ArgumentParameter(\"rest\", \"The"
                                + " rest\", io.valkyrja.cli.routing.enum_.ArgumentMode.OPTIONAL,"
                                + " io.valkyrja.cli.routing.enum_.ArgumentValueMode.ARRAY,"
                                + " java.util.List.of())"));

        // The fully populated option carries its display name, default, short names and valid
        // values; the bare flag emits empty lists for the two it does not name.
        assertTrue(
                supplier.contains(
                        "new io.valkyrja.cli.routing.data.OptionParameter(\"format\", \"The"
                                + " format\", \"fmt\", \"json\", java.util.List.of(\"f\"),"
                                + " java.util.List.of(\"json\", \"xml\"), java.util.List.of(),"
                                + " io.valkyrja.cli.routing.enum_.OptionMode.REQUIRED,"
                                + " io.valkyrja.cli.routing.enum_.OptionValueMode.DEFAULT)"));
        assertTrue(
                supplier.contains(
                        "new io.valkyrja.cli.routing.data.OptionParameter(\"flag\", \"A flag\","
                                + " \"\", \"\", java.util.List.of(), java.util.List.of(),"
                                + " java.util.List.of(),"
                                + " io.valkyrja.cli.routing.enum_.OptionMode.OPTIONAL,"
                                + " io.valkyrja.cli.routing.enum_.OptionValueMode.NONE)"));
    }

    @Test
    void readFile_emitsTheFullConstructorForACommandDeclaringOnlyAnOption() {
        CliRouteAttributeResult result =
                reader.readFile(
                        fixturePath("Cli/Controller/TestParameterCliControllerFixture.java"));

        String supplier = result.routes().get("flag-only").toString();

        // Options alone force the full constructor, so the empty argument list is emitted too.
        assertTrue(supplier.contains(", null, "));
        assertTrue(
                supplier.contains(
                        "java.util.List.of(), java.util.List.of(new io.valkyrja.cli.routing.data.OptionParameter(\"flag\""));
    }
}
