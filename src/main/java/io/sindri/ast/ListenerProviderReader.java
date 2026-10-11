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
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.ListenerProviderReaderContract;
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
import java.util.stream.Stream;

public class ListenerProviderReader extends AstReader implements ListenerProviderReaderContract {

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
        for (Expression item : extractListOfItems(returnExpr)) {
            if (item.isObjectCreationExpr()) {
                String fqn = extractObjectCreationFqn(item, qualifyMap, pkg);
                if (!fqn.isEmpty()) {
                    listenerClasses.add(fqn);
                }
                listeners.put(readName(item), qualify(item, qualifyMap));
            }
        }
        return new ListenerProviderResult(listenerClasses, listeners);
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
     * Read the name a listener registers under, which is its construction's second argument.
     *
     * @param listener the listener construction
     * @return the listener name
     */
    private String readName(Expression listener) {
        NodeList<Expression> arguments = listener.asObjectCreationExpr().getArguments();

        if (arguments.size() > 1 && arguments.get(1).isStringLiteralExpr()) {
            return arguments.get(1).asStringLiteralExpr().asString();
        }

        throw new UnreadableListenerException(
                "Could not read the listener name from: " + listener);
    }
}
