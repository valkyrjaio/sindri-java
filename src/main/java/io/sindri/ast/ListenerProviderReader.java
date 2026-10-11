/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.ListenerProviderReaderContract;
import io.sindri.ast.data.ListenerData;
import io.sindri.ast.data.result.ListenerProviderResult;
import io.sindri.ast.throwable.exception.AstFileReadException;
import io.sindri.ast.throwable.exception.UnreadableListenerException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public class ListenerProviderReader extends AstReader implements ListenerProviderReaderContract {

    /**
     * The builder calls this reader understands. A call outside this set can change the name the
     * listener registers under, or the event it listens for, and the cache must match what the
     * runtime collector receives, so an unknown call is a read failure rather than a listener
     * cached under a guessed name.
     */
    private static final Set<String> CHAINED_CALLS =
            Set.of("withEventId", "withName", "withHandler");

    /** The extension a Java source file carries. */
    private static final String JAVA_SUFFIX = ".java";

    @Override
    public ListenerProviderResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        Map<String, String> importMap = buildImportMap(cu);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, MethodDeclaration> methods = indexMethods(type);
        MethodDeclaration method = methods.get("getListeners");
        if (method == null) {
            return new ListenerProviderResult();
        }

        Expression returnExpr = findReturnExpr(method).orElse(null);
        if (returnExpr == null) {
            return new ListenerProviderResult();
        }

        // An inlined listener is written into a data file in another package, so every name in it
        // has to be fully qualified. A class the provider's own package declares carries no
        // import, so the map takes each of those too, from the provider's own directory.
        Map<String, String> qualifyMap = new HashMap<>(importMap);
        qualifyMap.putAll(getPackageClasses(filePath, pkg));

        List<String> listenerClasses = new ArrayList<>();
        Map<String, Expression> listeners = new LinkedHashMap<>();
        Map<String, ListenerData> listenerData = new LinkedHashMap<>();
        for (Expression item : extractListOfItems(returnExpr)) {
            List<MethodCallExpr> chain = getChainedCalls(item);
            Expression base = chain.isEmpty() ? item : chain.getLast().getScope().orElseThrow();
            if (base.isObjectCreationExpr()) {
                String fqn = extractObjectCreationFqn(base, qualifyMap, pkg);
                if (!fqn.isEmpty()) {
                    listenerClasses.add(fqn);
                }
                ListenerData data = readListener(item, base, chain, qualifyMap);
                listeners.put(data.name(), qualify(item, qualifyMap));
                listenerData.put(data.name(), data);
            }
        }
        return new ListenerProviderResult(listenerClasses, listeners, listenerData);
    }

    /**
     * Map each class the reading file's own package declares to its fully qualified name. A sibling
     * source file is the only way to tell a same-package class from one the compiler resolves
     * elsewhere, such as {@code String}, which must be left as it is read.
     *
     * @param filePath the file being read
     * @param pkg the file's package
     * @return the simple name to fully qualified name map for the package
     */
    private Map<String, String> getPackageClasses(String filePath, String pkg) {
        Map<String, String> classes = new HashMap<>();
        Path directory =
                Objects.requireNonNullElse(
                        Path.of(filePath).toAbsolutePath().getParent(), Path.of("."));

        try (Stream<Path> siblings = listFiles(directory)) {
            siblings.map(Path::getFileName)
                    .filter(Objects::nonNull)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(JAVA_SUFFIX))
                    .map(name -> name.substring(0, name.length() - JAVA_SUFFIX.length()))
                    .forEach(name -> classes.put(name, pkg.isEmpty() ? name : pkg + "." + name));
        } catch (IOException | UncheckedIOException e) {
            throw new AstFileReadException("Could not list the directory: " + directory, e);
        }

        return classes;
    }

    /**
     * List the files a directory holds.
     *
     * @param directory the directory to list
     * @return the directory's entries
     * @throws IOException when the directory cannot be listed
     */
    protected Stream<Path> listFiles(Path directory) throws IOException {
        return Files.list(directory);
    }

    /**
     * Collect the calls a builder chain makes, from the outermost call inward.
     *
     * @param listener the listener expression
     * @return the chain's calls, empty when the expression is not a chain
     */
    private List<MethodCallExpr> getChainedCalls(Expression listener) {
        List<MethodCallExpr> calls = new ArrayList<>();
        Expression current = listener;

        while (current.isMethodCallExpr() && current.asMethodCallExpr().getScope().isPresent()) {
            MethodCallExpr call = current.asMethodCallExpr();
            calls.add(call);
            current = call.getScope().orElseThrow();
        }

        return calls;
    }

    /**
     * Read the event a listener listens for and the name it registers under. Each one comes from
     * the construction's arguments, or from the builder chain over that construction when a call in
     * the chain replaces it.
     *
     * @param listener the listener expression
     * @param base the construction at the base of the listener expression
     * @param chain the builder chain over that construction, outermost call first
     * @param typeMap the simple name to fully qualified name map for the provider
     * @return the event id and the name the listener registers under
     */
    private ListenerData readListener(
            Expression listener,
            Expression base,
            List<MethodCallExpr> chain,
            Map<String, String> typeMap) {
        @Nullable Expression eventIdArgument = null;
        @Nullable Expression nameArgument = null;

        for (MethodCallExpr call : chain) {
            String called = call.getNameAsString();

            if (!CHAINED_CALLS.contains(called)) {
                throw new UnreadableListenerException(
                        "Could not read the listener through " + called + "(): " + listener);
            }

            // The framework applies the outermost call last, so the first withEventId and the
            // first withName this walk reaches hold the values every call under them replaces.
            if (eventIdArgument == null && called.equals("withEventId")) {
                eventIdArgument = getArgument(call.getArguments(), 0);
            }

            if (nameArgument == null && called.equals("withName")) {
                nameArgument = getArgument(call.getArguments(), 0);
            }
        }

        NodeList<Expression> arguments = base.asObjectCreationExpr().getArguments();

        if (eventIdArgument == null) {
            eventIdArgument = getArgument(arguments, 0);
        }

        if (nameArgument == null) {
            nameArgument = getArgument(arguments, 1);
        }

        String eventId = eventIdArgument == null ? "" : resolveEventId(eventIdArgument, typeMap);

        if (eventId.isEmpty()) {
            throw new UnreadableListenerException(
                    "Could not read the listener event id from: " + listener);
        }

        if (nameArgument == null || !nameArgument.isStringLiteralExpr()) {
            throw new UnreadableListenerException(
                    "Could not read the listener name from: " + listener);
        }

        return new ListenerData(eventId, nameArgument.asStringLiteralExpr().asString());
    }

    /**
     * Resolve the event class a {@code .class} argument names, against the imports and the classes
     * the provider's own package declares. A name neither of those knows stays as it is read,
     * because the inlined listener keeps that name too and the two have to name one class.
     *
     * @param argument the argument naming the event class
     * @param typeMap the simple name to fully qualified name map for the provider
     * @return the event class name, or an empty string when the argument names no event class
     */
    private String resolveEventId(Expression argument, Map<String, String> typeMap) {
        // An array type and a primitive type name no event, and the inlined listener qualifies an
        // array's component type, which the index cannot follow.
        if (!argument.isClassExpr() || !argument.asClassExpr().getType().isClassOrInterfaceType()) {
            return "";
        }

        String typeName = argument.asClassExpr().getTypeAsString();
        int nested = typeName.indexOf('.');
        String outer = nested < 0 ? typeName : typeName.substring(0, nested);
        String resolved = typeMap.get(outer);

        if (resolved == null) {
            return typeName;
        }

        return nested < 0 ? resolved : resolved + typeName.substring(nested);
    }

    /**
     * Read the argument at a position, or nothing when the argument list is shorter than that.
     *
     * @param arguments the argument list to read from
     * @param index the argument's position
     * @return the argument, or {@code null}
     */
    private @Nullable Expression getArgument(NodeList<Expression> arguments, int index) {
        return index < arguments.size() ? arguments.get(index) : null;
    }
}
