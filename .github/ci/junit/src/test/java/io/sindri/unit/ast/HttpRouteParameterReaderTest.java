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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.HttpRouteParameterReader;
import io.sindri.ast.data.HttpParameterData;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test the {@link HttpRouteParameterReader}. */
final class HttpRouteParameterReaderTest {

    private static final Map<String, String> IMPORTS =
            Map.of("Regex", "io.valkyrja.http.routing.constant.Regex");

    private final HttpRouteParameterReader reader = new HttpRouteParameterReader();

    private List<HttpParameterData> read(String annotations) {
        MethodDeclaration method =
                StaticJavaParser.parse("class C { " + annotations + " void m() {} }")
                        .findFirst(MethodDeclaration.class)
                        .orElseThrow();
        return reader.updateParameters(method, IMPORTS, "app");
    }

    @Test
    void readsStringLiteralRegex() {
        List<HttpParameterData> params = read("@Parameter(name = \"id\", regex = \"[0-9]+\")");

        assertEquals(1, params.size());
        assertEquals("id", params.get(0).name());
        assertEquals("[0-9]+", params.get(0).regex());
        assertFalse(params.get(0).isOptional());
        assertTrue(params.get(0).shouldCapture());
    }

    @Test
    void resolvesRegexConstantViaReflection() {
        List<HttpParameterData> params = read("@Parameter(name = \"value\", regex = Regex.ALPHA)");

        assertEquals("[a-zA-Z]+", params.get(0).regex());
    }

    @Test
    void readsOptionalAndNonCapturingFlags() {
        List<HttpParameterData> params =
                read("@Parameter(name = \"x\", isOptional = true, shouldCapture = false)");

        assertTrue(params.get(0).isOptional());
        assertFalse(params.get(0).shouldCapture());
    }

    @Test
    void noRegexDefaultsToEmpty() {
        assertEquals("", read("@Parameter(name = \"x\")").get(0).regex());
    }

    @Test
    void unknownRegexConstantResolvesToEmpty() {
        assertEquals("", read("@Parameter(name = \"x\", regex = Regex.NOPE)").get(0).regex());
    }

    @Test
    void nonLiteralNonConstantRegexResolvesToEmpty() {
        assertEquals("", read("@Parameter(name = \"x\", regex = SOMECONST)").get(0).regex());
    }

    @Test
    void skipsParameterWithoutName() {
        assertTrue(read("@Parameter(regex = \"[0-9]+\")").isEmpty());
    }

    @Test
    void skipsMarkerParameterAnnotation() {
        assertTrue(read("@Parameter").isEmpty());
    }

    @Test
    void ignoresUnrelatedAnnotations() {
        assertTrue(read("@Override @Deprecated").isEmpty());
    }

    @Test
    void readsParametersContainerArray() {
        List<HttpParameterData> params =
                read(
                        "@Parameters({@Parameter(name = \"a\", regex = \"1\"),"
                                + " @Parameter(name = \"b\", regex = \"2\")})");

        assertEquals(2, params.size());
        assertEquals("a", params.get(0).name());
        assertEquals("b", params.get(1).name());
    }

    @Test
    void readsParametersContainerWithValueMember() {
        List<HttpParameterData> params =
                read("@Parameters(value = {@Parameter(name = \"a\", regex = \"1\")})");

        assertEquals(1, params.size());
        assertEquals("a", params.get(0).name());
    }

    @Test
    void readsParametersContainerWithSingleAnnotation() {
        List<HttpParameterData> params =
                read("@Parameters(@Parameter(name = \"only\", regex = \"1\"))");

        assertEquals(1, params.size());
        assertEquals("only", params.get(0).name());
    }
}
