/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.sindri.ast.abstract_.AstReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
            String pkg = getPackageName(cu.get());
            for (ClassOrInterfaceType ancestor : type.get().getImplementedTypes()) {
                pending.push(resolveName(ancestor, importMap, pkg));
            }
            for (ClassOrInterfaceType ancestor : type.get().getExtendedTypes()) {
                pending.push(resolveName(ancestor, importMap, pkg));
            }
        }

        return matched;
    }

    /**
     * Resolve a written {@code implements}/{@code extends} type to a fully-qualified name: an
     * already-qualified reference is taken as-is; a simple name is resolved through the file's
     * imports, falling back to the same package.
     */
    private String resolveName(
            ClassOrInterfaceType type, Map<String, String> importMap, String pkg) {
        String written =
                type.getScope().map(scope -> scope.asString() + ".").orElse("")
                        + type.getNameAsString();
        if (written.contains(".")) {
            return written;
        }
        if (importMap.containsKey(written)) {
            return importMap.get(written);
        }
        return pkg.isEmpty() ? written : pkg + "." + written;
    }
}
