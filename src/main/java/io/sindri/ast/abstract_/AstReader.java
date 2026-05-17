/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.abstract_;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import io.sindri.ast.throwable.exception.AstFileReadException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AstReader {

    protected CompilationUnit parseFile(String path) {
        try {
            StaticJavaParser.getParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
            return StaticJavaParser.parse(new File(path));
        } catch (FileNotFoundException e) {
            throw new AstFileReadException("Could not parse file: " + path, e);
        }
    }

    protected Map<String, String> buildImportMap(CompilationUnit cu) {
        Map<String, String> importMap = new HashMap<>();
        for (ImportDeclaration importDecl : cu.getImports()) {
            if (!importDecl.isAsterisk() && !importDecl.isStatic()) {
                String fqn = importDecl.getNameAsString();
                String shortName = fqn.substring(fqn.lastIndexOf('.') + 1);
                importMap.put(shortName, fqn);
            }
        }
        return importMap;
    }

    protected String getPackageName(CompilationUnit cu) {
        return cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
    }

    protected Optional<ClassOrInterfaceDeclaration> findClass(CompilationUnit cu) {
        return cu.findFirst(ClassOrInterfaceDeclaration.class);
    }

    protected Optional<RecordDeclaration> findRecord(CompilationUnit cu) {
        return cu.findFirst(RecordDeclaration.class);
    }

    protected Optional<TypeDeclaration<?>> findType(CompilationUnit cu) {
        Optional<RecordDeclaration> rec = findRecord(cu);
        if (rec.isPresent()) {
            return Optional.of(rec.get());
        }
        return findClass(cu).map(c -> c);
    }

    protected Map<String, MethodDeclaration> indexMethods(TypeDeclaration<?> type) {
        Map<String, MethodDeclaration> methods = new HashMap<>();
        for (MethodDeclaration method : type.getMethods()) {
            methods.put(method.getNameAsString(), method);
        }
        return methods;
    }

    protected String resolveClassName(String name, Map<String, String> importMap, String pkg) {
        if (importMap.containsKey(name)) {
            return importMap.get(name);
        }
        if (name.contains(".")) {
            return name;
        }
        return pkg.isEmpty() ? name : pkg + "." + name;
    }

    protected String extractStringLiteral(Expression expr) {
        if (expr.isStringLiteralExpr()) {
            return expr.asStringLiteralExpr().asString();
        }
        return expr.toString();
    }

    protected String extractObjectCreationFqn(
            Expression expr, Map<String, String> importMap, String pkg) {
        if (!expr.isObjectCreationExpr()) {
            return "";
        }
        String typeName = expr.asObjectCreationExpr().getTypeAsString();
        return resolveClassName(typeName, importMap, pkg);
    }

    protected String extractClassExprFqn(
            Expression expr, Map<String, String> importMap, String pkg) {
        if (!expr.isClassExpr()) {
            return "";
        }
        String typeName = expr.asClassExpr().getTypeAsString();
        return resolveClassName(typeName, importMap, pkg);
    }

    protected List<Expression> extractListOfItems(Expression expr) {
        List<Expression> items = new ArrayList<>();
        if (!expr.isMethodCallExpr()) {
            return items;
        }
        MethodCallExpr call = expr.asMethodCallExpr();
        if (!call.getNameAsString().equals("of")) {
            return items;
        }
        boolean isListOf = call.getScope().map(s -> s.toString().equals("List")).orElse(false);
        if (!isListOf) {
            return items;
        }
        items.addAll(call.getArguments());
        return items;
    }

    protected Optional<Expression> findReturnExpr(MethodDeclaration method) {
        return method.getBody()
                .flatMap(
                        body ->
                                body.getStatements().stream()
                                        .filter(stmt -> stmt.isReturnStmt())
                                        .findFirst()
                                        .flatMap(stmt -> stmt.asReturnStmt().getExpression()));
    }

    protected Optional<NodeList<Expression>> findThisCallArgs(ConstructorDeclaration ctor) {
        return ctor.getBody().getStatements().stream()
                .filter(stmt -> stmt instanceof ExplicitConstructorInvocationStmt)
                .map(stmt -> (ExplicitConstructorInvocationStmt) stmt)
                .filter(ExplicitConstructorInvocationStmt::isThis)
                .findFirst()
                .map(ExplicitConstructorInvocationStmt::getArguments);
    }

    protected String fqnToFilePath(String fqn, String namespace, String srcDir) {
        String namespacePkg = namespace.toLowerCase(java.util.Locale.ROOT);
        if (fqn.startsWith(namespacePkg + ".")) {
            String relative = fqn.substring(namespacePkg.length() + 1).replace('.', '/');
            return srcDir + "/" + relative + ".java";
        }
        return "";
    }
}
