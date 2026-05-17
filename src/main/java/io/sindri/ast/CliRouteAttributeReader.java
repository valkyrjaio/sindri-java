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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.CliRouteAttributeReaderContract;
import io.sindri.ast.data.result.CliRouteAttributeResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CliRouteAttributeReader extends AstReader implements CliRouteAttributeReaderContract {

    @Override
    public CliRouteAttributeResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, Expression> routeMap = new LinkedHashMap<>();
        for (MethodDeclaration method : type.getMethods()) {
            List<AnnotationExpr> routeAnnotations =
                    method.getAnnotations().stream()
                            .filter(a -> a.getNameAsString().equals("Route"))
                            .toList();

            if (routeAnnotations.isEmpty()) {
                continue;
            }

            for (AnnotationExpr routeAnnotation : routeAnnotations) {
                if (!(routeAnnotation instanceof NormalAnnotationExpr normalRoute)) {
                    continue;
                }
                String name = "";
                for (MemberValuePair pair : normalRoute.getPairs()) {
                    if (pair.getNameAsString().equals("name")) {
                        name = extractStringLiteral(pair.getValue());
                    }
                }
                routeMap.put(name, new NameExpr(name));
            }
        }
        return new CliRouteAttributeResult(routeMap);
    }
}
