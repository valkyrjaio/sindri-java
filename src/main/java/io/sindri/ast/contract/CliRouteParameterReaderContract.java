/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.contract;

import com.github.javaparser.ast.body.MethodDeclaration;
import io.sindri.ast.data.CliArgumentParameterData;
import io.sindri.ast.data.CliOptionParameterData;
import java.util.List;
import java.util.Map;

public interface CliRouteParameterReaderContract {

    List<CliArgumentParameterData> updateArguments(
            MethodDeclaration method, Map<String, String> importMap, String pkg);

    List<CliOptionParameterData> updateOptions(
            MethodDeclaration method, Map<String, String> importMap, String pkg);
}
