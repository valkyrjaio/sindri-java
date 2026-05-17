/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.HttpParameterDataContract;
import org.jspecify.annotations.Nullable;

public record HttpParameterData(
        String name, String regex, @Nullable String cast, boolean isOptional, boolean shouldCapture)
        implements HttpParameterDataContract {

    public HttpParameterData(String name, String regex) {
        this(name, regex, null, false, true);
    }
}
