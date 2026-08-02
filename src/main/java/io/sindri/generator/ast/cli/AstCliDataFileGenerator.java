/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.generator.ast.cli;

import io.sindri.generator.abstract_.AstFileGenerator;
import io.sindri.generator.cli.contract.CliDataFileGeneratorContract;
import io.sindri.generator.enum_.GenerateStatus;
import java.util.Map;

public class AstCliDataFileGenerator extends AstFileGenerator
        implements CliDataFileGeneratorContract {

    @Override
    public GenerateStatus generateFile(
            String directory, String className, String namespace, Map<String, String> routes) {
        String contents =
                buildRoutesDataFile(
                        namespace,
                        className,
                        buildRoutesBody(routes),
                        "cli",
                        "CliRoutingDataContract");
        return writeFile(directory, className, contents);
    }

    @Override
    public String generateClassContents(Map<String, String> routes) {
        return buildRoutesBody(routes);
    }
}
