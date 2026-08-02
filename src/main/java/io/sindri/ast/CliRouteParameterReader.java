/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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
import io.sindri.ast.contract.CliRouteParameterReaderContract;
import io.sindri.ast.data.CliArgumentParameterData;
import io.sindri.ast.data.CliOptionParameterData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Reads the argument and option parameters a CLI command declares.
 *
 * <p>Each is declared by annotating the command method, and both annotations are repeatable, so a
 * command may carry several of either directly or wrapped in their container annotation.
 */
public class CliRouteParameterReader extends AstReader implements CliRouteParameterReaderContract {

    @Override
    public List<CliArgumentParameterData> updateArguments(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        List<CliArgumentParameterData> arguments = new ArrayList<>();

        for (AnnotationExpr annotation : method.getAnnotations()) {
            switch (annotation.getNameAsString()) {
                case "ArgumentParameter" -> addArgument(arguments, annotation);
                case "ArgumentParameters" -> {
                    for (AnnotationExpr inner : extractAnnotationArray(annotation)) {
                        addArgument(arguments, inner);
                    }
                }
                default -> {}
            }
        }

        return arguments;
    }

    @Override
    public List<CliOptionParameterData> updateOptions(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        List<CliOptionParameterData> options = new ArrayList<>();

        for (AnnotationExpr annotation : method.getAnnotations()) {
            switch (annotation.getNameAsString()) {
                case "OptionParameter" -> addOption(options, annotation);
                case "OptionParameters" -> {
                    for (AnnotationExpr inner : extractAnnotationArray(annotation)) {
                        addOption(options, inner);
                    }
                }
                default -> {}
            }
        }

        return options;
    }

    private void addArgument(List<CliArgumentParameterData> arguments, AnnotationExpr annotation) {
        CliArgumentParameterData argument = parseArgument(annotation);

        if (argument != null) {
            arguments.add(argument);
        }
    }

    private void addOption(List<CliOptionParameterData> options, AnnotationExpr annotation) {
        CliOptionParameterData option = parseOption(annotation);

        if (option != null) {
            options.add(option);
        }
    }

    private @Nullable CliArgumentParameterData parseArgument(AnnotationExpr annotation) {
        if (!(annotation instanceof NormalAnnotationExpr normal)) {
            return null;
        }

        String name = "";
        String description = "";
        String mode = "OPTIONAL";
        String valueMode = "DEFAULT";

        for (MemberValuePair pair : normal.getPairs()) {
            switch (pair.getNameAsString()) {
                case "name" -> name = extractStringLiteral(pair.getValue());
                case "description" -> description = extractStringLiteral(pair.getValue());
                case "mode" -> mode = extractEnumName(pair.getValue());
                case "valueMode" -> valueMode = extractEnumName(pair.getValue());
                default -> {}
            }
        }

        if (name.isEmpty()) {
            return null;
        }

        return new CliArgumentParameterData(name, description, null, mode, valueMode);
    }

    private @Nullable CliOptionParameterData parseOption(AnnotationExpr annotation) {
        if (!(annotation instanceof NormalAnnotationExpr normal)) {
            return null;
        }

        String name = "";
        String description = "";
        String valueDisplayName = "";
        String defaultValue = "";
        List<String> shortNames = List.of();
        List<String> validValues = List.of();
        String mode = "OPTIONAL";
        String valueMode = "DEFAULT";

        for (MemberValuePair pair : normal.getPairs()) {
            switch (pair.getNameAsString()) {
                case "name" -> name = extractStringLiteral(pair.getValue());
                case "description" -> description = extractStringLiteral(pair.getValue());
                case "valueDisplayName" -> valueDisplayName = extractStringLiteral(pair.getValue());
                case "defaultValue" -> defaultValue = extractStringLiteral(pair.getValue());
                case "shortNames" -> shortNames = extractStringArray(pair.getValue());
                case "validValues" -> validValues = extractStringArray(pair.getValue());
                case "mode" -> mode = extractEnumName(pair.getValue());
                case "valueMode" -> valueMode = extractEnumName(pair.getValue());
                default -> {}
            }
        }

        if (name.isEmpty()) {
            return null;
        }

        return new CliOptionParameterData(
                name,
                description,
                valueDisplayName,
                null,
                defaultValue,
                shortNames,
                validValues,
                mode,
                valueMode);
    }

    /** Extract the annotations a container annotation wraps in its {@code value} member. */
    private List<AnnotationExpr> extractAnnotationArray(AnnotationExpr annotation) {
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

    /** Extract a string array member, which may also be written as a single string. */
    private List<String> extractStringArray(Expression expr) {
        List<String> values = new ArrayList<>();

        if (expr.isArrayInitializerExpr()) {
            for (Expression value : expr.asArrayInitializerExpr().getValues()) {
                values.add(extractStringLiteral(value));
            }

            return values;
        }

        values.add(extractStringLiteral(expr));

        return values;
    }

    /** Extract an enum member's case name, e.g. {@code REQUIRED} from {@code Mode.REQUIRED}. */
    private String extractEnumName(Expression expr) {
        if (expr.isFieldAccessExpr()) {
            return expr.asFieldAccessExpr().getNameAsString();
        }

        if (expr.isNameExpr()) {
            return expr.asNameExpr().getNameAsString();
        }

        return expr.toString();
    }
}
