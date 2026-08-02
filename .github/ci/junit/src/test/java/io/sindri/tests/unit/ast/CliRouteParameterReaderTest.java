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

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.CliRouteParameterReader;
import io.sindri.ast.data.CliArgumentParameterData;
import io.sindri.ast.data.CliOptionParameterData;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link CliRouteParameterReader}. */
final class CliRouteParameterReaderTest {

    private final CliRouteParameterReader reader = new CliRouteParameterReader();

    private MethodDeclaration method(String fixture, String name) throws Exception {
        var path =
                Path.of(
                        getClass()
                                .getClassLoader()
                                .getResource("Fixtures/Cli/Controller/" + fixture)
                                .toURI());

        return StaticJavaParser.parse(path).findAll(MethodDeclaration.class).stream()
                .filter(m -> m.getNameAsString().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void readsNoParametersFromAMethodThatDeclaresNone() {
        var method = new MethodDeclaration();

        assertTrue(reader.updateArguments(method, Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateOptions(method, Map.of(), "pkg").isEmpty());
    }

    @Test
    void readsEveryArgumentWithItsModes() throws Exception {
        List<CliArgumentParameterData> arguments =
                reader.updateArguments(
                        method("TestParameterCliControllerFixture.java", "build"), Map.of(), "pkg");

        assertEquals(2, arguments.size());

        assertEquals("target", arguments.get(0).name());
        assertEquals("The target", arguments.get(0).description());
        assertEquals("REQUIRED", arguments.get(0).mode());
        assertEquals("DEFAULT", arguments.get(0).valueMode());

        assertEquals("rest", arguments.get(1).name());
        assertEquals("OPTIONAL", arguments.get(1).mode());
        assertEquals("ARRAY", arguments.get(1).valueMode());
    }

    @Test
    void readsEveryOptionWithItsModesAndMetadata() throws Exception {
        List<CliOptionParameterData> options =
                reader.updateOptions(
                        method("TestParameterCliControllerFixture.java", "build"), Map.of(), "pkg");

        assertEquals(2, options.size());

        var format = options.get(0);
        assertEquals("format", format.name());
        assertEquals("The format", format.description());
        assertEquals("fmt", format.valueDisplayName());
        assertEquals("json", format.defaultValue());
        assertEquals(List.of("f"), format.shortNames());
        assertEquals(List.of("json", "xml"), format.validValues());
        assertEquals("REQUIRED", format.mode());
        assertEquals("DEFAULT", format.valueMode());

        var flag = options.get(1);
        assertEquals("flag", flag.name());
        // An option naming no short names or valid values keeps them empty.
        assertTrue(flag.shortNames().isEmpty());
        assertTrue(flag.validValues().isEmpty());
        assertEquals("NONE", flag.valueMode());
    }

    @Test
    void readsAnArgumentDeclaredOnItsOwn() throws Exception {
        List<CliArgumentParameterData> arguments =
                reader.updateArguments(
                        method("TestCliControllerFixture.java", "greet"), Map.of(), "pkg");

        assertEquals(1, arguments.size());
        assertEquals("name", arguments.get(0).name());
        assertEquals("REQUIRED", arguments.get(0).mode());
    }

    /** Parse a throwaway method carrying the given annotation source. */
    private MethodDeclaration annotated(String annotations) {
        return StaticJavaParser.parseBodyDeclaration(annotations + " public static void run() {}")
                .asMethodDeclaration();
    }

    @Test
    void readsParametersDeclaredThroughTheirContainerAnnotation() {
        var method =
                annotated(
                        """
                        @ArgumentParameters({
                            @ArgumentParameter(name = "a", description = "A",
                                mode = ArgumentMode.REQUIRED,
                                valueMode = ArgumentValueMode.DEFAULT),
                            @ArgumentParameter(name = "b", description = "B",
                                mode = ArgumentMode.OPTIONAL,
                                valueMode = ArgumentValueMode.ARRAY)
                        })
                        @OptionParameters({
                            @OptionParameter(name = "o", description = "O",
                                mode = OptionMode.OPTIONAL,
                                valueMode = OptionValueMode.NONE)
                        })
                        """);

        List<CliArgumentParameterData> arguments = reader.updateArguments(method, Map.of(), "pkg");
        assertEquals(2, arguments.size());
        assertEquals("a", arguments.get(0).name());
        assertEquals("ARRAY", arguments.get(1).valueMode());

        List<CliOptionParameterData> options = reader.updateOptions(method, Map.of(), "pkg");
        assertEquals(1, options.size());
        assertEquals("o", options.get(0).name());
    }

    @Test
    void readsAContainerAnnotationWrittenWithAnExplicitValueMember() {
        var method =
                annotated(
                        """
                        @ArgumentParameters(unknown = 1, value = {
                            @ArgumentParameter(name = "a", description = "A")
                        })
                        """);

        List<CliArgumentParameterData> arguments = reader.updateArguments(method, Map.of(), "pkg");

        assertEquals(1, arguments.size());
        assertEquals("a", arguments.get(0).name());
    }

    @Test
    void readsASingleContainedAnnotationNotWrappedInAnArray() {
        var method =
                annotated("@OptionParameters(@OptionParameter(name = \"o\", description = \"O\"))");

        List<CliOptionParameterData> options = reader.updateOptions(method, Map.of(), "pkg");

        assertEquals(1, options.size());
        assertEquals("o", options.get(0).name());
    }

    @Test
    void readsNothingFromAContainerAnnotationWithNothingToUnwrap() {
        // A marker container names no value at all, and an array holding something that is not an
        // annotation has nothing to contribute.
        assertTrue(
                reader.updateArguments(annotated("@ArgumentParameters"), Map.of(), "pkg")
                        .isEmpty());
        assertTrue(
                reader.updateOptions(annotated("@OptionParameters({1})"), Map.of(), "pkg")
                        .isEmpty());
    }

    @Test
    void readsNothingFromAParameterAnnotationThatNamesNoMembers() {
        assertTrue(
                reader.updateArguments(annotated("@ArgumentParameter"), Map.of(), "pkg").isEmpty());
        assertTrue(reader.updateOptions(annotated("@OptionParameter"), Map.of(), "pkg").isEmpty());
    }

    @Test
    void readsNothingFromAParameterAnnotationThatNamesNoName() {
        // An unrecognized member is ignored, and without a name there is no parameter to build.
        assertTrue(
                reader.updateArguments(
                                annotated("@ArgumentParameter(description = \"d\", unknown = 1)"),
                                Map.of(),
                                "pkg")
                        .isEmpty());
        assertTrue(
                reader.updateOptions(
                                annotated("@OptionParameter(description = \"d\", unknown = 1)"),
                                Map.of(),
                                "pkg")
                        .isEmpty());
    }

    @Test
    void readsShortNamesAndValidValuesWrittenAsASingleString() {
        var method =
                annotated(
                        "@OptionParameter(name = \"o\", description = \"O\", shortNames = \"f\","
                                + " validValues = \"json\")");

        CliOptionParameterData option = reader.updateOptions(method, Map.of(), "pkg").get(0);

        assertEquals(List.of("f"), option.shortNames());
        assertEquals(List.of("json"), option.validValues());
    }

    @Test
    void readsEnumMembersWrittenWithoutTheirType() {
        // A statically imported enum constant parses as a bare name rather than a field access.
        var method =
                annotated(
                        "@ArgumentParameter(name = \"a\", description = \"A\", mode = REQUIRED,"
                                + " valueMode = ARRAY)");

        CliArgumentParameterData argument = reader.updateArguments(method, Map.of(), "pkg").get(0);

        assertEquals("REQUIRED", argument.mode());
        assertEquals("ARRAY", argument.valueMode());
    }

    @Test
    void fallsBackToAnEnumMembersSourceWhenItIsNeitherANameNorAFieldAccess() {
        var method =
                annotated("@ArgumentParameter(name = \"a\", description = \"A\", mode = modeOf())");

        CliArgumentParameterData argument = reader.updateArguments(method, Map.of(), "pkg").get(0);

        assertEquals("modeOf()", argument.mode());
    }
}
