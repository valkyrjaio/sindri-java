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
import io.sindri.ast.MiddlewareClassifier;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Test the {@link MiddlewareClassifier}. */
final class MiddlewareClassifierTest {

    private static final String MATCHED = "v.RouteMatchedMiddlewareContract";
    private static final String TERMINATED = "v.TerminatedMiddlewareContract";
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
                                + " import v.TerminatedMiddlewareContract;"
                                + " class Multi implements RouteMatchedMiddlewareContract,"
                                + " TerminatedMiddlewareContract {}");

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
                        "package p; import v.TerminatedMiddlewareContract;"
                                + " interface Custom extends TerminatedMiddlewareContract {}",
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
}
