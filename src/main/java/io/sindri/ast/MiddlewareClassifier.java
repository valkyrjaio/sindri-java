/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.sindri.ast.abstract_.AstReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Classifies a middleware class by which stage contracts it implements — the syntactic equivalent
 * of the runtime collector's {@code isAssignableFrom} cascade, for the route-data cache.
 *
 * <p>A {@code @Middleware} annotation names only the class, not its stage; the stage is the set of
 * stage contracts the class implements, directly or through an abstract base or a sub-contract.
 * This walks the class's full {@code implements}/{@code extends} ancestry (resolving names through
 * each source's imports) and reports which of the given target contracts it reaches. Targets are
 * matched by fully-qualified name and never parsed, so the framework contracts need not be
 * resolvable — which keeps the cache generator working even before the target module has been
 * published.
 */
public class MiddlewareClassifier extends AstReader {

    /** Resolves a fully-qualified type name to its parsed source, or empty when unresolvable. */
    public interface SourceResolver {

        Optional<CompilationUnit> resolve(String fqn);
    }

    /**
     * Return the subset of {@code targetFqns} the middleware reaches through its type hierarchy.
     *
     * @param middlewareFqn the middleware class
     * @param resolver resolves an app/framework class to its source (empty stops that branch)
     * @param targetFqns the stage-contract fully-qualified names to match against
     * @return the matched target FQNs (a class may match several stages)
     */
    public Set<String> classify(
            String middlewareFqn, SourceResolver resolver, Set<String> targetFqns) {
        Set<String> matched = new LinkedHashSet<>();
        Set<String> visited = new HashSet<>();
        Deque<String> pending = new ArrayDeque<>();
        pending.push(middlewareFqn);

        while (!pending.isEmpty()) {
            String fqn = pending.pop();
            if (!visited.add(fqn)) {
                continue;
            }
            if (targetFqns.contains(fqn)) {
                // A target is terminal: record it, and don't parse it (it may be unresolvable).
                matched.add(fqn);
                continue;
            }

            Optional<CompilationUnit> cu = resolver.resolve(fqn);
            if (cu.isEmpty()) {
                // Unresolvable ancestor (a non-target framework or JDK type) — stop this branch.
                continue;
            }
            Optional<ClassOrInterfaceDeclaration> type = findClass(cu.get());
            if (type.isEmpty()) {
                continue;
            }

            Map<String, String> importMap = buildImportMap(cu.get());
            List<String> wildcards = wildcardPackages(cu.get());
            String pkg = getPackageName(cu.get());
            for (ClassOrInterfaceType ancestor : type.get().getImplementedTypes()) {
                pending.addAll(
                        resolveNameCandidates(ancestor, importMap, wildcards, pkg, resolver));
            }
            for (ClassOrInterfaceType ancestor : type.get().getExtendedTypes()) {
                pending.addAll(
                        resolveNameCandidates(ancestor, importMap, wildcards, pkg, resolver));
            }
        }

        return matched;
    }

    /** The packages of the file's wildcard imports ({@code import pkg.*;}). */
    private List<String> wildcardPackages(CompilationUnit cu) {
        List<String> packages = new ArrayList<>();
        for (var importDeclaration : cu.getImports()) {
            if (importDeclaration.isAsterisk() && !importDeclaration.isStatic()) {
                packages.add(importDeclaration.getNameAsString());
            }
        }
        return packages;
    }

    /**
     * Read every {@code @Middleware} on a method and group each by the stage contracts it reaches.
     *
     * <p>Shared by the gRPC, HTTP, and CLI route readers — the {@code @Middleware} attribute and
     * the classification are identical across protocols; only the set of stage contracts differs.
     *
     * @param method the handler method
     * @param imports the declaring file's imports, for resolving the middleware class names
     * @param pkg the declaring file's package, for same-package middleware
     * @param resolver resolves a middleware/base class to its source
     * @param targetFqns the protocol's stage-contract fully-qualified names
     * @return each matched target contract mapped to the middleware classes assigned to it, in
     *     order
     */
    public Map<String, List<String>> classifyMethod(
            MethodDeclaration method,
            Map<String, String> imports,
            String pkg,
            SourceResolver resolver,
            Set<String> targetFqns) {
        Map<String, List<String>> byTarget = new LinkedHashMap<>();
        for (String middlewareFqn : middlewareClasses(method, imports, pkg)) {
            for (String matched : classify(middlewareFqn, resolver, targetFqns)) {
                byTarget.computeIfAbsent(matched, key -> new ArrayList<>()).add(middlewareFqn);
            }
        }
        return byTarget;
    }

    /**
     * Collect the fully-qualified middleware classes named by {@code @Middleware} on the method,
     * expanding the repeatable container when the source spells it out explicitly. Classes are
     * appended in source order and never deduplicated — the generated cache mirrors the runtime
     * collector, which likewise appends, so a duplicate runs as many times as it is declared. Which
     * copy to drop and whether order matters are the developer's call, not the framework's.
     */
    private List<String> middlewareClasses(
            MethodDeclaration method, Map<String, String> imports, String pkg) {
        List<String> classes = new ArrayList<>();

        for (AnnotationExpr annotation : method.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (annotationName.equals("Middleware")) {
                addMiddlewareClass(annotation, imports, pkg, classes);
            } else if (annotationName.equals("Middlewares")) {
                annotation
                        .findAll(ArrayInitializerExpr.class)
                        .forEach(
                                array ->
                                        array.getValues()
                                                .forEach(
                                                        value -> {
                                                            if (value
                                                                    instanceof
                                                                    AnnotationExpr nested) {
                                                                addMiddlewareClass(
                                                                        nested, imports, pkg,
                                                                        classes);
                                                            }
                                                        }));
            }
        }

        return classes;
    }

    private void addMiddlewareClass(
            AnnotationExpr annotation, Map<String, String> imports, String pkg, List<String> into) {
        if (!(annotation instanceof NormalAnnotationExpr normal)) {
            return;
        }

        for (MemberValuePair pair : normal.getPairs()) {
            if (!pair.getNameAsString().equals("name") || !pair.getValue().isClassExpr()) {
                continue;
            }

            String written = pair.getValue().asClassExpr().getType().asString();
            String fqn =
                    written.contains(".")
                            ? written
                            : imports.getOrDefault(
                                    written, pkg.isEmpty() ? written : pkg + "." + written);
            into.add(fqn);
        }
    }

    /**
     * Resolve a written {@code implements}/{@code extends} type to its candidate fully-qualified
     * names, following Java's name-resolution precedence: an already-qualified reference or an
     * explicit single-type import resolves to exactly one; a bare simple name resolves to the
     * same-package type if one actually exists (a same-package type shadows every wildcard import),
     * and only otherwise fans out to the wildcard-import packages. Returning the wildcard
     * candidates only when the same-package name does not resolve keeps a genuine wildcard-imported
     * stage contract from being dropped, while stopping a local class that merely shares a
     * contract's simple name from being misclassified as that contract.
     */
    private List<String> resolveNameCandidates(
            ClassOrInterfaceType type,
            Map<String, String> importMap,
            List<String> wildcards,
            String pkg,
            SourceResolver resolver) {
        String written =
                type.getScope().map(scope -> scope.asString() + ".").orElse("")
                        + type.getNameAsString();
        if (written.contains(".")) {
            return List.of(written);
        }
        if (importMap.containsKey(written)) {
            return List.of(importMap.get(written));
        }

        String samePackage = pkg.isEmpty() ? written : pkg + "." + written;
        if (resolver.resolve(samePackage).isPresent()) {
            // A same-package type shadows any wildcard import (JLS §6.5.5.1), so it is the only
            // candidate — don't also try the wildcard packages and risk a false target match.
            return List.of(samePackage);
        }

        List<String> candidates = new ArrayList<>();
        candidates.add(samePackage);
        for (String wildcard : wildcards) {
            candidates.add(wildcard + "." + written);
        }
        return candidates;
    }
}
