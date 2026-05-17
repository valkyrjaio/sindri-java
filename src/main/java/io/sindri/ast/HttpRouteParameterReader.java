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
import io.sindri.ast.contract.HttpRouteParameterReaderContract;
import io.sindri.ast.data.HttpParameterData;
import java.util.List;
import java.util.Map;

public class HttpRouteParameterReader extends AstReader
        implements HttpRouteParameterReaderContract {

    @Override
    public List<HttpParameterData> updateParameters(
            MethodDeclaration method, Map<String, String> importMap, String pkg) {
        return List.of();
    }
}
