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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

public record RouteProviderResult(List<String> controllerClasses, List<Expression> routes) {

    public RouteProviderResult() {
        this(List.of(), List.of());
    }

    public RouteProviderResult merge(RouteProviderResult other) {
        SequencedSet<String> classes = new LinkedHashSet<>(controllerClasses);
        classes.addAll(other.controllerClasses);

        List<Expression> merged = new ArrayList<>(routes);
        merged.addAll(other.routes);

        return new RouteProviderResult(new ArrayList<>(classes), merged);
    }
}
