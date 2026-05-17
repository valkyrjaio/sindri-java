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
import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.CliRouteParameterReaderContract;
import io.sindri.ast.data.CliArgumentParameterData;
import io.sindri.ast.data.CliOptionParameterData;
import java.util.List;
import java.util.Map;

public class CliRouteParameterReader extends AstReader implements CliRouteParameterReaderContract {

    @Override
    public List<CliArgumentParameterData> updateArguments(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }

    @Override
    public List<CliOptionParameterData> updateOptions(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }
}
