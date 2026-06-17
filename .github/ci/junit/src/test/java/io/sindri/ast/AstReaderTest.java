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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.data.result.ServiceProviderResult;
import io.sindri.ast.throwable.exception.AstFileReadException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AstReaderTest {

    private final ServiceProviderReader reader = new ServiceProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Classes/" + relative).getPath();
    }

    @Test
    void readFile_withRecordType_returnsEmpty() {
        ServiceProviderResult result = reader.readFile(fixturePath("Data/TestDataRecord.java"));

        assertTrue(result.publishers().isEmpty());
    }

    @Test
    void fqnToFilePath_matchingNamespace_returnsRelativePath() {
        TestableAstReader astReader = new TestableAstReader();

        String path = astReader.testFqnToFilePath("app.service.UserService", "app", "/src");

        assertEquals("/src/service/UserService.java", path);
    }

    @Test
    void fqnToFilePath_nonMatchingNamespace_returnsEmpty() {
        TestableAstReader astReader = new TestableAstReader();

        String path = astReader.testFqnToFilePath("other.service.UserService", "app", "/src");

        assertEquals("", path);
    }

    @Test
    void findThisCallArgs_withThisCall_returnsArgs() {
        TestableAstReader astReader = new TestableAstReader();
        CompilationUnit cu = astReader.testParseFile(fixturePath("Common/TestClassWithThisCall.java"));
        ClassOrInterfaceDeclaration clazz = astReader.testFindClass(cu).orElseThrow();
        ConstructorDeclaration ctor = clazz.getConstructors().stream()
                .filter(c -> c.getParameters().isEmpty())
                .findFirst()
                .orElseThrow();

        Optional<NodeList<Expression>> args = astReader.testFindThisCallArgs(ctor);

        assertTrue(args.isPresent());
        assertEquals(1, args.get().size());
        assertEquals("\"default\"", args.get().get(0).toString());
    }

    @Test
    void parseFile_withNonExistentPath_throwsException() {
        TestableAstReader astReader = new TestableAstReader();

        assertThrows(AstFileReadException.class,
                () -> astReader.testParseFile("/nonexistent/path/to/File.java"));
    }

    @Test
    void resolveClassName_withFqnContainingDot_returnsSameName() {
        TestableAstReader astReader = new TestableAstReader();

        String result = astReader.testResolveClassName("io.sindri.SomeClass", Map.of(), "io.sindri");

        assertEquals("io.sindri.SomeClass", result);
    }

    @Test
    void extractStringLiteral_withNonStringExpr_returnsToString() {
        TestableAstReader astReader = new TestableAstReader();

        String result = astReader.testExtractStringLiteral(new NameExpr("someVar"));

        assertEquals("someVar", result);
    }

    @Test
    void extractObjectCreationFqn_withNonObjectCreationExpr_returnsEmpty() {
        TestableAstReader astReader = new TestableAstReader();

        String result = astReader.testExtractObjectCreationFqn(
                new NameExpr("someVar"), Map.of(), "io.sindri");

        assertEquals("", result);
    }

    @Test
    void extractListOfItems_withNonMethodCallExpr_returnsEmpty() {
        TestableAstReader astReader = new TestableAstReader();

        List<Expression> result = astReader.testExtractListOfItems(new NameExpr("someVar"));

        assertTrue(result.isEmpty());
    }

    @Test
    void extractListOfItems_withNonOfMethodName_returnsEmpty() {
        TestableAstReader astReader = new TestableAstReader();

        List<Expression> result = astReader.testExtractListOfItems(new MethodCallExpr("notOf"));

        assertTrue(result.isEmpty());
    }

    @Test
    void extractListOfItems_withNonListScope_returnsEmpty() {
        TestableAstReader astReader = new TestableAstReader();
        MethodCallExpr expr = new MethodCallExpr(new NameExpr("Map"), "of", new NodeList<>());

        List<Expression> result = astReader.testExtractListOfItems(expr);

        assertTrue(result.isEmpty());
    }

    private static class TestableAstReader extends AstReader {
        CompilationUnit testParseFile(String path) {
            return parseFile(path);
        }

        Optional<ClassOrInterfaceDeclaration> testFindClass(CompilationUnit cu) {
            return findClass(cu);
        }

        Optional<NodeList<Expression>> testFindThisCallArgs(ConstructorDeclaration ctor) {
            return findThisCallArgs(ctor);
        }

        String testResolveClassName(String name, Map<String, String> importMap, String pkg) {
            return resolveClassName(name, importMap, pkg);
        }

        String testExtractStringLiteral(Expression expr) {
            return extractStringLiteral(expr);
        }

        String testExtractObjectCreationFqn(
                Expression expr, Map<String, String> importMap, String pkg) {
            return extractObjectCreationFqn(expr, importMap, pkg);
        }

        List<Expression> testExtractListOfItems(Expression expr) {
            return extractListOfItems(expr);
        }

        String testFqnToFilePath(String fqn, String namespace, String srcDir) {
            return fqnToFilePath(fqn, namespace, srcDir);
        }
    }
}
