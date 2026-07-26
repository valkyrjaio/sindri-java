/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.MiddlewareClassifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Test the {@link MiddlewareClassifier}. */
final class MiddlewareClassifierTest {

    private static final String MATCHED = "v.RouteMatchedMiddlewareContract";
    private static final String TERMINATED = "v.ResponseSentMiddlewareContract";
    private static final Set<String> TARGETS = Set.of(MATCHED, TERMINATED);

    private final MiddlewareClassifier classifier = new MiddlewareClassifier();

    /** Resolver backed by in-memory source, so the test needs no fixture files. */
    private MiddlewareClassifier.SourceResolver resolver(Map<String, String> sources) {
        return fqn -> Optional.ofNullable(sources.get(fqn)).map(StaticJavaParser::parse);
    }

    @Test
    void classifiesADirectImplementer() {
        var sources =
                Map.of(
                        "p.Direct",
                        "package p; import v.RouteMatchedMiddlewareContract;"
                                + " class Direct implements RouteMatchedMiddlewareContract {}");

        assertEquals(Set.of(MATCHED), classifier.classify("p.Direct", resolver(sources), TARGETS));
    }

    @Test
    void classifiesAClassIntoEveryStageItImplements() {
        var sources =
                Map.of(
                        "p.Multi",
                        "package p;"
                                + " import v.RouteMatchedMiddlewareContract;"
                                + " import v.ResponseSentMiddlewareContract;"
                                + " class Multi implements RouteMatchedMiddlewareContract,"
                                + " ResponseSentMiddlewareContract {}");

        assertEquals(TARGETS, classifier.classify("p.Multi", resolver(sources), TARGETS));
    }

    @Test
    void resolvesAStageInheritedFromAnAbstractBase() {
        var sources =
                Map.of(
                        "p.Child", "package p; class Child extends p.Base {}",
                        "p.Base",
                                "package p; import v.RouteMatchedMiddlewareContract;"
                                        + " abstract class Base implements"
                                        + " RouteMatchedMiddlewareContract {}");

        assertEquals(Set.of(MATCHED), classifier.classify("p.Child", resolver(sources), TARGETS));
    }

    @Test
    void resolvesAStageThroughACustomSubContract() {
        var sources =
                Map.of(
                        "p.Custom",
                        "package p; import v.ResponseSentMiddlewareContract;"
                                + " interface Custom extends ResponseSentMiddlewareContract {}",
                        "p.Uses", "package p; class Uses implements Custom {}");

        assertEquals(Set.of(TERMINATED), classifier.classify("p.Uses", resolver(sources), TARGETS));
    }

    @Test
    void classifiesAStageContractReachedThroughAWildcardImport() {
        var sources =
                Map.of(
                        "p.Wild",
                        "package p; import v.*;"
                                + " class Wild implements RouteMatchedMiddlewareContract {}");

        assertEquals(Set.of(MATCHED), classifier.classify("p.Wild", resolver(sources), TARGETS));
    }

    @Test
    void aSamePackageClassShadowsAWildcardImportedStageContract() {
        // Wild has `import v.*;` but its own package also defines a RouteMatchedMiddlewareContract
        // that implements no stage. Java resolves the bare name to the same-package type, so the
        // framework contract must NOT be matched despite the wildcard import.
        var sources =
                Map.of(
                        "p.Wild",
                        "package p; import v.*;"
                                + " class Wild implements RouteMatchedMiddlewareContract {}",
                        "p.RouteMatchedMiddlewareContract",
                        "package p; class RouteMatchedMiddlewareContract {}");

        assertTrue(classifier.classify("p.Wild", resolver(sources), TARGETS).isEmpty());
    }

    @Test
    void classifiesAClassThatImplementsNoStageAsEmpty() {
        var sources =
                Map.of("p.None", "package p; class None implements java.io.Serializable {}");

        assertTrue(classifier.classify("p.None", resolver(sources), TARGETS).isEmpty());
    }

    @Test
    void stopsWithoutErrorAtAnUnresolvableAncestor() {
        var sources = Map.of("p.Opaque", "package p; class Opaque extends some.Unknown {}");

        assertTrue(classifier.classify("p.Opaque", resolver(sources), TARGETS).isEmpty());
    }

    @Test
    void visitsAnAncestorReachedByTwoPathsOnlyOnce() {
        // Diamond: Both IA and IB extend Mid, so Mid is queued twice — the second pop is skipped.
        var sources =
                Map.of(
                        "p.Diamond", "package p; class Diamond implements p.IA, p.IB {}",
                        "p.IA", "package p; interface IA extends p.Mid {}",
                        "p.IB", "package p; interface IB extends p.Mid {}",
                        "p.Mid",
                                "package p; import v.RouteMatchedMiddlewareContract;"
                                        + " interface Mid extends RouteMatchedMiddlewareContract {}");

        assertEquals(Set.of(MATCHED), classifier.classify("p.Diamond", resolver(sources), TARGETS));
    }

    @Test
    void stopsAtAResolvedSourceWithNoClassOrInterfaceDeclaration() {
        // The ancestor resolves to a source, but it declares an enum, not a class/interface.
        var sources =
                Map.of(
                        "p.Uses", "package p; class Uses extends p.E {}",
                        "p.E", "package p; enum E {}");

        assertTrue(classifier.classify("p.Uses", resolver(sources), TARGETS).isEmpty());
    }

    @Test
    void classifiesAnAncestorInTheDefaultPackage() {
        // No package on the class, and a bare ancestor name — exercises the empty-package branch.
        var sources = Map.of("Default", "class Default implements RouteMatchedMiddlewareContract {}");

        assertTrue(classifier.classify("Default", resolver(sources), TARGETS).isEmpty());
    }

    @Test
    void ignoresAMarkerMiddlewareAnnotationWithoutArguments() {
        MethodDeclaration method =
                StaticJavaParser.parse("package p; class C { @Middleware void m() {} }")
                        .findFirst(MethodDeclaration.class)
                        .orElseThrow();

        assertTrue(
                classifier
                        .classifyMethod(method, Map.of(), "p", resolver(Map.of()), TARGETS)
                        .isEmpty());
    }

    @Test
    void ignoresAMiddlewarePairThatIsNotAClassNamedName() {
        // Two pairs cover both guard operands: a non-"name" pair, and a "name" pair whose value is
        // not a class literal.
        MethodDeclaration method =
                StaticJavaParser.parse(
                                "package p; class C {"
                                        + " @Middleware(name = \"notAClass\", other = Foo.class)"
                                        + " void m() {} }")
                        .findFirst(MethodDeclaration.class)
                        .orElseThrow();

        assertTrue(
                classifier
                        .classifyMethod(method, Map.of(), "p", resolver(Map.of()), TARGETS)
                        .isEmpty());
    }

    @Test
    void ignoresStaticWildcardImportsWhenGatheringWildcardPackages() {
        // A static wildcard import is not a type-name wildcard, so it must not be treated as one;
        // the class still resolves its stage through the regular single-type import.
        var sources =
                Map.of(
                        "p.Wild",
                        "package p; import static v.Helpers.*;"
                                + " import v.RouteMatchedMiddlewareContract;"
                                + " class Wild implements RouteMatchedMiddlewareContract {}");

        assertEquals(Set.of(MATCHED), classifier.classify("p.Wild", resolver(sources), TARGETS));
    }

    @Test
    void ignoresNonAnnotationEntriesInAMiddlewaresContainer() {
        // A @Middlewares array element that is not itself an annotation is skipped.
        MethodDeclaration method =
                StaticJavaParser.parse(
                                "package p; class C { @Middlewares({\"notAnAnnotation\"}) void m() {}"
                                        + " }")
                        .findFirst(MethodDeclaration.class)
                        .orElseThrow();

        assertTrue(
                classifier
                        .classifyMethod(method, Map.of(), "p", resolver(Map.of()), TARGETS)
                        .isEmpty());
    }

    @Test
    void classifiesADefaultPackageMiddlewareClassByItsSimpleName() {
        // No package (pkg == "") and a simple-name middleware class exercises the empty-package
        // branch in the annotation reader; the class is unresolvable so no stage is matched.
        MethodDeclaration method =
                StaticJavaParser.parse("class C { @Middleware(name = Simple.class) void m() {} }")
                        .findFirst(MethodDeclaration.class)
                        .orElseThrow();

        List<String> matched =
                classifier
                        .classifyMethod(method, Map.of(), "", resolver(Map.of()), TARGETS)
                        .getOrDefault(MATCHED, List.of());
        assertTrue(matched.isEmpty());
    }
}
