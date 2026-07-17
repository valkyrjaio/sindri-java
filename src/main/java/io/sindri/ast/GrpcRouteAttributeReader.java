/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.GrpcRouteAttributeReaderContract;
import io.sindri.ast.data.GrpcRouteData;
import io.sindri.ast.data.HandlerData;
import io.sindri.ast.data.result.GrpcRouteAttributeResult;
import java.util.LinkedHashMap;
import java.util.Map;

public class GrpcRouteAttributeReader extends AstReader
        implements GrpcRouteAttributeReaderContract {

    @Override
    public GrpcRouteAttributeResult readFile(String filePath) {
        CompilationUnit cu = parseFile(filePath);
        String pkg = getPackageName(cu);

        TypeDeclaration<?> type =
                findType(cu)
                        .orElseThrow(() -> new RuntimeException("No type found in: " + filePath));

        Map<String, Expression> routeMap = new LinkedHashMap<>();
        Map<String, GrpcRouteData> routeDataMap = new LinkedHashMap<>();

        String service = readServiceName(type);
        if (service.isEmpty()) {
            return new GrpcRouteAttributeResult(routeMap, routeDataMap);
        }

        String controllerFqn =
                pkg.isEmpty() ? type.getNameAsString() : pkg + "." + type.getNameAsString();

        for (MethodDeclaration method : type.getMethods()) {
            NormalAnnotationExpr grpcMethod = findGrpcMethod(method);
            if (grpcMethod == null) {
                continue;
            }

            String name = "";
            boolean clientStreaming = false;
            boolean serverStreaming = false;
            for (MemberValuePair pair : grpcMethod.getPairs()) {
                switch (pair.getNameAsString()) {
                    case "name" -> name = extractStringLiteral(pair.getValue());
                    case "clientStreaming" -> clientStreaming = extractBoolean(pair.getValue());
                    case "serverStreaming" -> serverStreaming = extractBoolean(pair.getValue());
                    default -> {}
                }
            }
            if (name.isEmpty()) {
                continue;
            }

            String fullMethod = "/" + service + "/" + name;
            String methodName = method.getNameAsString();

            GrpcRouteData data =
                    new GrpcRouteData(
                            fullMethod,
                            service,
                            name,
                            new HandlerData(controllerFqn, methodName),
                            clientStreaming,
                            serverStreaming,
                            java.util.List.of(),
                            java.util.List.of(),
                            java.util.List.of(),
                            java.util.List.of(),
                            java.util.List.of());
            routeDataMap.put(fullMethod, data);
            routeMap.put(
                    fullMethod,
                    buildRouteValue(
                            fullMethod,
                            controllerFqn,
                            methodName,
                            clientStreaming,
                            serverStreaming));
        }

        return new GrpcRouteAttributeResult(routeMap, routeDataMap);
    }

    /**
     * Read the {@code service} value from the class-level {@code @GrpcService}, or "" if absent.
     */
    private String readServiceName(TypeDeclaration<?> type) {
        for (AnnotationExpr annotation : type.getAnnotations()) {
            if (annotation.getNameAsString().equals("GrpcService")
                    && annotation instanceof NormalAnnotationExpr normal) {
                for (MemberValuePair pair : normal.getPairs()) {
                    if (pair.getNameAsString().equals("service")) {
                        return extractStringLiteral(pair.getValue());
                    }
                }
            }
        }
        return "";
    }

    private @org.jspecify.annotations.Nullable NormalAnnotationExpr findGrpcMethod(
            MethodDeclaration method) {
        return method.getAnnotations().stream()
                .filter(a -> a.getNameAsString().equals("GrpcMethod"))
                .filter(a -> a instanceof NormalAnnotationExpr)
                .map(a -> (NormalAnnotationExpr) a)
                .findFirst()
                .orElse(null);
    }

    /**
     * Build the {@code Supplier<RouteContract>} expression stored as a route's value, e.g. {@code
     * () -> new Route("/pkg.Greeter/SayHello", (c, r) -> new pkg.Greeter().sayHello(c, r))}. Fully
     * qualified names are used so the generated file needs no extra imports. Middleware dispatch is
     * a follow-up, mirroring the CLI/HTTP readers.
     */
    private Expression buildRouteValue(
            String fullMethod,
            String controllerFqn,
            String methodName,
            boolean clientStreaming,
            boolean serverStreaming) {
        StringBuilder supplier =
                new StringBuilder(
                        "() -> new io.valkyrja.grpc.routing.data.Route(\""
                                + escapeJava(fullMethod)
                                + "\", (c, r) -> new "
                                + controllerFqn
                                + "()."
                                + methodName
                                + "(c, r))");
        if (clientStreaming) {
            supplier.append(".withClientStreaming(true)");
        }
        if (serverStreaming) {
            supplier.append(".withServerStreaming(true)");
        }

        return StaticJavaParser.parseExpression(supplier.toString());
    }

    private boolean extractBoolean(Expression expr) {
        return expr.isBooleanLiteralExpr() && expr.asBooleanLiteralExpr().getValue();
    }

    private String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
