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
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.ConfigReaderContract;
import io.sindri.ast.data.result.ConfigResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigReader extends AstReader implements ConfigReaderContract {

    @Override
    public ConfigResult readFile(String configFilePath) {
        CompilationUnit cu = parseFile(configFilePath);
        Map<String, String> importMap = buildImportMap(cu);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(
                                () -> new RuntimeException("No type found in: " + configFilePath));

        ConstructorDeclaration noArgCtor =
                findNoArgConstructor(type)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "No no-arg constructor found in: "
                                                        + configFilePath));

        NodeList<Expression> args =
                findThisCallArgs(noArgCtor)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "No this(...) call found in constructor in: "
                                                        + configFilePath));

        String namespace = extractStringLiteral(args.get(0));

        String dataNamespace = extractStringLiteral(args.get(8));

        List<String> providers = extractProviders(args, importMap, pkg);

        String srcDir = computeSrcDir(configFilePath, pkg, namespace);

        String absoluteDataPath = resolveDataPath(srcDir, dataNamespace, namespace);

        return new ConfigResult(namespace, srcDir, absoluteDataPath, dataNamespace, providers);
    }

    private Optional<ConstructorDeclaration> findNoArgConstructor(TypeDeclaration<?> type) {
        if (type instanceof RecordDeclaration record) {
            return record.getConstructors().stream()
                    .filter(ctor -> ctor.getParameters().isEmpty())
                    .findFirst();
        }
        return type.asClassOrInterfaceDeclaration().getConstructors().stream()
                .filter(ctor -> ctor.getParameters().isEmpty())
                .findFirst();
    }

    private List<String> extractProviders(
            NodeList<Expression> args, Map<String, String> importMap, String pkg) {
        List<String> providers = new ArrayList<>();
        for (Expression arg : args) {
            List<Expression> items = extractListOfItems(arg);
            if (items.isEmpty()) {
                continue;
            }
            boolean hasObjectCreations = items.stream().anyMatch(Expression::isObjectCreationExpr);
            if (!hasObjectCreations) {
                continue;
            }
            for (Expression item : items) {
                if (item.isObjectCreationExpr()) {
                    String fqn = extractObjectCreationFqn(item, importMap, pkg);
                    if (!fqn.isEmpty()) {
                        providers.add(fqn);
                    }
                }
            }
            break;
        }
        return providers;
    }

    private String computeSrcDir(String configFilePath, String filePkg, String namespace) {
        File fileDir = new File(configFilePath).getParentFile();
        int filePackageDepth = filePkg.isEmpty() ? 0 : filePkg.split("\\.").length;
        String namespacePkg = namespace.toLowerCase(java.util.Locale.ROOT);
        int namespaceDepth = namespacePkg.isEmpty() ? 0 : namespacePkg.split("\\.").length;
        int levels = filePackageDepth - namespaceDepth;

        File srcDir = fileDir;
        for (int i = 0; i < levels; i++) {
            srcDir = srcDir.getParentFile();
        }
        return srcDir.getAbsolutePath();
    }

    private String resolveDataPath(String srcDir, String dataNamespace, String namespace) {
        String namespacePkg = namespace.toLowerCase(java.util.Locale.ROOT);
        String relative =
                dataNamespace.startsWith(namespacePkg + ".")
                        ? dataNamespace.substring(namespacePkg.length() + 1).replace('.', '/')
                        : dataNamespace.replace('.', '/');
        return srcDir + "/" + relative;
    }
}
