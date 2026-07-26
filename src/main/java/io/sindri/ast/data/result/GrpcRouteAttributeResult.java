/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data.result;

import com.github.javaparser.ast.expr.Expression;
import io.sindri.ast.data.GrpcRouteData;
import java.util.Map;

public record GrpcRouteAttributeResult(
        Map<String, Expression> routes, Map<String, GrpcRouteData> routeData) {

    public GrpcRouteAttributeResult() {
        this(Map.of(), Map.of());
    }
}
