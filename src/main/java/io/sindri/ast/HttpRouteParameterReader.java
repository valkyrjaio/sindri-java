/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.HttpRouteParameterReaderContract;
import io.sindri.ast.data.HttpParameterData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Reads {@code @Parameter} / {@code @Parameters} annotations off an HTTP controller method into
 * {@link HttpParameterData}. A parameter's {@code regex} may be a string literal or a reference to a
 * {@code Regex} constant (e.g. {@code Regex.ALPHA}); the latter is resolved to its value by
 * reflecting the constant off the classpath, mirroring what the framework sees at runtime.
 */
public class HttpRouteParameterReader extends AstReader
        implements HttpRouteParameterReaderContract {

    @Override
    public List<HttpParameterData> updateParameters(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        List<HttpParameterData> parameters = new ArrayList<>();

        for (AnnotationExpr annotation : method.getAnnotations()) {
            switch (annotation.getNameAsString()) {
                case "Parameter" -> addParameter(parameters, annotation, importMap, pkg);
                case "Parameters" -> {
                    for (AnnotationExpr inner : extractParametersArray(annotation)) {
                        addParameter(parameters, inner, importMap, pkg);
                    }
                }
                default -> {}
            }
        }

        return parameters;
    }

    private void addParameter(
            List<HttpParameterData> parameters,
            AnnotationExpr annotation,
            Map<String, String> importMap,
            String pkg) {
        HttpParameterData parameter = parseParameter(annotation, importMap, pkg);
        if (parameter != null) {
            parameters.add(parameter);
        }
    }

    private @Nullable HttpParameterData parseParameter(
            AnnotationExpr annotation, Map<String, String> importMap, String pkg) {
        if (!(annotation instanceof NormalAnnotationExpr normal)) {
            return null;
        }

        String name = "";
        String regex = "";
        boolean isOptional = false;
        boolean shouldCapture = true;

        for (MemberValuePair pair : normal.getPairs()) {
            switch (pair.getNameAsString()) {
                case "name" -> name = extractStringLiteral(pair.getValue());
                case "regex" -> regex = resolveRegexValue(pair.getValue(), importMap, pkg);
                case "isOptional" -> isOptional = extractBoolean(pair.getValue());
                case "shouldCapture" -> shouldCapture = extractBoolean(pair.getValue());
                default -> {}
            }
        }

        if (name.isEmpty()) {
            return null;
        }

        return new HttpParameterData(name, regex, null, isOptional, shouldCapture);
    }

    /** Resolve a parameter regex: a string literal directly, or a {@code Regex.*} constant value. */
    protected String resolveRegexValue(
            Expression expr, Map<String, String> importMap, String pkg) {
        if (expr.isStringLiteralExpr()) {
            return expr.asStringLiteralExpr().getValue();
        }

        if (expr.isFieldAccessExpr()) {
            String fqn =
                    resolveClassName(expr.asFieldAccessExpr().getScope().toString(), importMap, pkg);
            return reflectStaticStringField(fqn, expr.asFieldAccessExpr().getNameAsString());
        }

        return "";
    }

    private String reflectStaticStringField(String classFqn, String fieldName) {
        try {
            Object value = Class.forName(classFqn).getField(fieldName).get(null);
            return value != null ? value.toString() : "";
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }

    private boolean extractBoolean(Expression expr) {
        return expr.isBooleanLiteralExpr() && expr.asBooleanLiteralExpr().getValue();
    }

    private List<AnnotationExpr> extractParametersArray(AnnotationExpr annotation) {
        Expression value = null;
        if (annotation instanceof SingleMemberAnnotationExpr single) {
            value = single.getMemberValue();
        } else if (annotation instanceof NormalAnnotationExpr normal) {
            for (MemberValuePair pair : normal.getPairs()) {
                if (pair.getNameAsString().equals("value")) {
                    value = pair.getValue();
                }
            }
        }

        List<AnnotationExpr> result = new ArrayList<>();
        if (value instanceof ArrayInitializerExpr array) {
            for (Expression item : array.getValues()) {
                if (item instanceof AnnotationExpr inner) {
                    result.add(inner);
                }
            }
        } else if (value instanceof AnnotationExpr inner) {
            result.add(inner);
        }

        return result;
    }
}
