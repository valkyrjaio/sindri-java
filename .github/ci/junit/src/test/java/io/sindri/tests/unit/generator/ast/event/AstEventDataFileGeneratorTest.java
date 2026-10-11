/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.generator.ast.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import io.sindri.generator.ast.event.AstEventDataFileGenerator;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public final class AstEventDataFileGeneratorTest {

    private final AstEventDataFileGenerator generator = new AstEventDataFileGenerator();

    /** Parse generated source at the language level the generated record needs. */
    private static ParseResult<CompilationUnit> parse(String content) {
        return new JavaParser(
                        new ParserConfiguration()
                                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21))
                .parse(content);
    }

    @Test
    void generateFile_empty_writesEmptyMaps(@TempDir Path tempDir) throws IOException {
        generator.generateFile(tempDir.toString(), "AppEventData", "test.data", Map.of());

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        assertTrue(content.contains("package test.data;"));
        assertTrue(content.contains("public record AppEventData()"));
        assertTrue(content.contains("return Map.of();"));
    }

    @Test
    void generateFile_withListener_writesListenerEntry(@TempDir Path tempDir) throws IOException {
        generator.generateFile(
                tempDir.toString(),
                "AppEventData",
                "test.data",
                Map.of("user.created", "fixtures.handler.UserHandler::onUserCreated"));

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        assertTrue(content.contains("\"user.created\""));
        assertTrue(content.contains("fixtures.handler.UserHandler::onUserCreated"));
    }

    @Test
    void generateFile_withTwoListeners_writesCommaDelimited(@TempDir Path tempDir)
            throws IOException {
        Map<String, String> listeners = new LinkedHashMap<>();
        listeners.put("user.created", "fixtures.handler.UserHandler::onCreate");
        listeners.put("user.deleted", "fixtures.handler.UserHandler::onDelete");

        generator.generateFile(tempDir.toString(), "AppEventData", "test.data", listeners);

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        assertTrue(content.contains("\"user.created\", fixtures.handler.UserHandler::onCreate,"));
        assertTrue(content.contains("\"user.deleted\", fixtures.handler.UserHandler::onDelete"));
    }

    @Test
    void generateClassContents_delegatesCorrectly() {
        String result =
                generator.generateClassContents(
                        Map.of("user.created", "fixtures.handler.UserHandler::onCreate"));

        assertTrue(result.contains("\"user.created\""));
        assertTrue(result.contains("fixtures.handler.UserHandler::onCreate"));
    }

    @Test
    void generateFile_withManyListeners_usesMapOfEntries(@TempDir Path tempDir) throws IOException {
        Map<String, String> listeners = new LinkedHashMap<>();
        for (int i = 1; i <= 11; i++) {
            listeners.put("event." + i, "fixtures.handler.Handler" + i + "::onEvent" + i);
        }

        generator.generateFile(tempDir.toString(), "AppEventData", "test.data", listeners);

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        assertTrue(content.contains("Map.ofEntries("));
        assertTrue(content.contains("Map.entry(\"event.1\""));
    }

    @Test
    void generateFile_nameHoldingCharactersALiteralCannotHold_isEscaped(@TempDir Path tempDir)
            throws IOException {
        // Every escape the generator writes, including a line break, which ends a literal, and a
        // control character, which takes an octal escape because the compiler reads a unicode
        // escape before it reads the literal.
        String name = "a\\\"b\bc\td\ne\ff\rg\u0001h\u007Fi";

        generator.generateFile(
                tempDir.toString(),
                "AppEventData",
                "test.data",
                Map.of(name, "fixtures.handler.UserHandler::onCreate"));

        String content = Files.readString(tempDir.resolve("AppEventData.java"));
        CompilationUnit generated =
                parse(content)
                        .getResult()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                "the escaped name does not parse:\n" + content));

        assertTrue(
                parse(content).isSuccessful(),
                () -> "the escaped name does not parse:\n" + content);
        // The literal the generator wrote reads back as the name it was given, which is the
        // property the escaping has to hold.
        StringLiteralExpr literal =
                generated.findAll(StringLiteralExpr.class).stream()
                        .filter(found -> found.asString().equals(name))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                "the escaped name does not read back as itself:\n"
                                                        + content));

        // javac is the oracle, not javaparser: javaparser accepts a \\u escape inside a literal
        // and javac resolves it before the literal is lexed, so an escape table that reached for
        // \\u000a would pass every assertion above and break every real build.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assumeTrue(compiler != null, "this JVM ships no system Java compiler");

        Path probe = tempDir.resolve("EscapeProbe.java");
        Files.writeString(
                probe, "class EscapeProbe { String value = \"" + literal.getValue() + "\"; }");

        assertEquals(
                0,
                compiler.run(null, null, OutputStream.nullOutputStream(), probe.toString()),
                () -> "javac rejects the escaped literal: " + literal.getValue());
    }

}
