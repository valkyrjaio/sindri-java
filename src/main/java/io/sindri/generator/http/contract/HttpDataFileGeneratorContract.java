/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.generator.http.contract;

import io.sindri.ast.data.HttpRouteData;
import io.sindri.generator.enum_.GenerateStatus;
import java.util.Map;

public interface HttpDataFileGeneratorContract {

    GenerateStatus generateFile(
            String directory,
            String className,
            String namespace,
            Map<String, String> routes,
            Map<String, HttpRouteData> routeData);

    String generateClassContents(Map<String, String> routes, Map<String, HttpRouteData> routeData);
}
