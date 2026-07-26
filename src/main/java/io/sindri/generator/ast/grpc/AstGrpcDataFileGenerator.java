/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generator.ast.grpc;

import io.sindri.generator.abstract_.AstFileGenerator;
import io.sindri.generator.enum_.GenerateStatus;
import io.sindri.generator.grpc.contract.GrpcDataFileGeneratorContract;
import java.util.Map;

public class AstGrpcDataFileGenerator extends AstFileGenerator
        implements GrpcDataFileGeneratorContract {

    @Override
    public GenerateStatus generateFile(
            String directory, String className, String namespace, Map<String, String> routes) {
        String contents =
                buildRoutesDataFile(
                        namespace,
                        className,
                        buildRoutesBody(routes),
                        "grpc",
                        "GrpcRoutingDataContract");
        return writeFile(directory, className, contents);
    }

    @Override
    public String generateClassContents(Map<String, String> routes) {
        return buildRoutesBody(routes);
    }
}
