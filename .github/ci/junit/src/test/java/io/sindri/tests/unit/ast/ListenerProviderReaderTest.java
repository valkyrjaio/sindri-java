/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.*;
import io.sindri.ast.data.result.ListenerProviderResult;
import io.sindri.ast.throwable.exception.AstFileReadException;
import io.sindri.ast.throwable.exception.UnreadableListenerException;
import org.junit.jupiter.api.Test;

public final class ListenerProviderReaderTest {

    private final ListenerProviderReader reader = new ListenerProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesListenerCount() {
        ListenerProviderResult result =
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderFixture.java"));

        assertEquals(1, result.listenerClasses().size());
        assertEquals(1, result.listeners().size());
    }

    @Test
    void readFile_keysEachListenerByItsName() {
        ListenerProviderResult result =
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderFixture.java"));

        assertTrue(result.listeners().containsKey("user.created"));
    }

    @Test
    void readFile_qualifiesEachListenerAgainstTheProvidersImports() {
        ListenerProviderResult result =
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderFixture.java"));

        String listener = result.listeners().get("user.created").toString();
        assertTrue(listener.contains("new io.valkyrja.event.data.Listener("));
        assertTrue(listener.contains("io.sindri.tests.fixtures.event.TestEventFixture.class"));
        assertTrue(
                listener.contains(
                        "io.sindri.tests.fixtures.event.TestListenerFixture::onUserCreated"));
    }

    @Test
    void readFile_leavesAQualifiedReferenceAsItIsRead() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestQualifiedListenerProviderFixture.java"));

        String listener = result.listeners().get("already.qualified").toString();
        assertTrue(listener.contains("new io.valkyrja.event.data.Listener("));
        assertTrue(listener.contains("io.sindri.tests.fixtures.event.TestListenerFixture.HANDLER"));
    }

    @Test
    void readFile_leavesANameItCannotResolveAsItIsRead() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestQualifiedListenerProviderFixture.java"));

        String listener = result.listeners().get("same.package").toString();
        assertTrue(listener.contains("TestSamePackageEventFixture.class"));
        assertTrue(listener.contains("HANDLER"));
        // The index names the same class the inlined listener does, rather than guessing a
        // package for it.
        assertEquals(
                "TestSamePackageEventFixture", result.listenerData().get("same.package").eventId());
    }

    @Test
    void readFile_nestedEventClass_resolvesThroughItsOuterClass() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath(
                                "Event/Provider/TestNestedEventIdListenerProviderFixture.java"));

        assertEquals(
                "io.sindri.tests.fixtures.event.TestEventFixture.Inner",
                result.listenerData().get("nested").eventId());
    }

    @Test
    void readFile_eventClassTheProviderQualifiesItself_isLeftAsItIsRead() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath(
                                "Event/Provider/TestNestedEventIdListenerProviderFixture.java"));

        assertEquals("java.lang.String", result.listenerData().get("already.qualified").eventId());
    }

    @Test
    void readFile_qualifiesAClassTheProvidersOwnPackageDeclares() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestSamePackageListenerProviderFixture.java"));

        assertTrue(
                result.listeners()
                        .get("same.package.handler")
                        .toString()
                        .contains(
                                "io.sindri.tests.fixtures.event.provider"
                                        + ".TestEmptyListenerProviderFixture::handle"));
    }

    @Test
    void readFile_qualifiesTheProvidersOwnClass() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestSamePackageListenerProviderFixture.java"));

        assertTrue(
                result.listeners()
                        .get("own.class.handler")
                        .toString()
                        .contains(
                                "io.sindri.tests.fixtures.event.provider"
                                        + ".TestSamePackageListenerProviderFixture::handle"));
    }

    @Test
    void readFile_arrayEventClass_throws() {
        assertThrows(
                UnreadableListenerException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestArrayEventIdListenerProviderFixture.java")));
    }

    @Test
    void readFile_directoryListingThatFailsDuringTraversal_throws() {
        var reader =
                new ListenerProviderReader() {
                    @Override
                    protected java.util.stream.Stream<java.nio.file.Path> listFiles(
                            java.nio.file.Path directory) {
                        return java.util.stream.Stream.generate(
                                () -> {
                                    throw new java.io.UncheckedIOException(
                                            new java.io.IOException("mid-traversal"));
                                });
                    }
                };

        assertThrows(
                AstFileReadException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestSamePackageListenerProviderFixture.java")));
    }

    @Test
    void readFile_directoryItCannotList_throws() {
        var reader =
                new ListenerProviderReader() {
                    @Override
                    protected java.util.stream.Stream<java.nio.file.Path> listFiles(
                            java.nio.file.Path directory) throws java.io.IOException {
                        throw new java.io.IOException("unreadable");
                    }
                };

        assertThrows(
                AstFileReadException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestSamePackageListenerProviderFixture.java")));
    }

    @Test
    void readFile_chainedListener_isKeyedByTheNameTheChainApplies() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestChainedListenerProviderFixture.java"));

        assertTrue(result.listeners().containsKey("last"));
        assertTrue(result.listeners().get("last").toString().contains(".withName(\"last\")"));
    }

    @Test
    void readFile_chainThatKeepsTheName_isKeyedByTheConstructionsName() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestChainedListenerProviderFixture.java"));

        assertTrue(result.listeners().containsKey("handler.only"));
    }

    @Test
    void readFile_itemWithNoConstructionToReadFrom_isSkipped() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestChainedListenerProviderFixture.java"));

        assertEquals(4, result.listeners().size());
    }

    @Test
    void readFile_indexesEachListenerUnderTheEventItsConstructionNames() {
        ListenerProviderResult result =
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderFixture.java"));

        assertEquals(
                "io.sindri.tests.fixtures.event.TestEventFixture",
                result.listenerData().get("user.created").eventId());
    }

    @Test
    void readFile_chainedEventId_replacesTheConstructions() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestChainedListenerProviderFixture.java"));

        assertEquals(
                "io.sindri.tests.fixtures.event.TestOtherEventFixture",
                result.listenerData().get("rebound").eventId());
    }

    @Test
    void readFile_chainThatReboundsTwice_takesTheOutermostEventId() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestChainedListenerProviderFixture.java"));

        assertEquals(
                "io.sindri.tests.fixtures.event.TestOtherEventFixture",
                result.listenerData().get("rebound.twice").eventId());
    }

    @Test
    void readFile_providerInTheDefaultPackage_isRead() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestNoPackageListenerProviderFixture.java"));

        assertTrue(result.listeners().containsKey("user.created"));
    }

    @Test
    void readFile_noEventIdArgument_throws() {
        assertThrows(
                UnreadableListenerException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestNoEventIdListenerProviderFixture.java")));
    }

    @Test
    void readFile_nonClassEventId_throws() {
        assertThrows(
                UnreadableListenerException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestNonClassEventIdListenerProviderFixture.java")));
    }

    @Test
    void readFile_chainCallItCannotRead_throws() {
        assertThrows(
                UnreadableListenerException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestUnknownChainListenerProviderFixture.java")));
    }

    @Test
    void readFile_nonLiteralChainedName_throws() {
        assertThrows(
                UnreadableListenerException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestNonLiteralChainNameListenerProviderFixture.java")));
    }

    @Test
    void readFile_nonLiteralName_throws() {
        assertThrows(
                UnreadableListenerException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestNonLiteralNameListenerProviderFixture.java")));
    }

    @Test
    void readFile_noNameArgument_throws() {
        assertThrows(
                UnreadableListenerException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestNoNameListenerProviderFixture.java")));
    }

    @Test
    void readFile_parsesListenerClass() {
        ListenerProviderResult result =
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderFixture.java"));

        assertEquals("io.valkyrja.event.data.Listener", result.listenerClasses().get(0));
    }

    @Test
    void readFile_noGetListenersMethod_returnsEmpty() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestEmptyListenerProviderFixture.java"));

        assertTrue(result.listenerClasses().isEmpty());
        assertTrue(result.listeners().isEmpty());
    }

    @Test
    void readFile_getListenersThrows_returnsEmpty() {
        ListenerProviderResult result =
                reader.readFile(
                        fixturePath("Event/Provider/TestNoReturnListenerProviderFixture.java"));

        assertTrue(result.listenerClasses().isEmpty());
        assertTrue(result.listeners().isEmpty());
    }

    @Test
    void readFile_emptyFqn_isSkipped() {
        var reader =
                new ListenerProviderReader() {
                    @Override
                    protected String extractObjectCreationFqn(
                            com.github.javaparser.ast.expr.Expression expr,
                            java.util.Map<String, String> importMap,
                            String pkg) {
                        return "";
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderFixture.java"))
                        .listenerClasses()
                        .isEmpty());
    }

    @Test
    void readFile_nonObjectCreationItem_isSkipped() {
        var reader =
                new ListenerProviderReader() {
                    @Override
                    protected java.util.List<com.github.javaparser.ast.expr.Expression>
                            extractListOfItems(com.github.javaparser.ast.expr.Expression expr) {
                        return java.util.List.of(new com.github.javaparser.ast.expr.NameExpr("x"));
                    }
                };

        assertTrue(
                reader.readFile(fixturePath("Event/Provider/TestListenerProviderFixture.java"))
                        .listenerClasses()
                        .isEmpty());
    }

    @Test
    void readFile_noTypeDeclaration_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Event/Provider/TestNoTypeListenerProviderFileFixture.java")));
    }
}
