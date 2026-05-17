/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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
