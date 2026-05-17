/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.CliArgumentParameterDataContract;
import org.jspecify.annotations.Nullable;

public record CliArgumentParameterData(
        String name, String description, @Nullable String cast, String mode, String valueMode)
        implements CliArgumentParameterDataContract {

    public CliArgumentParameterData(String name, String description) {
        this(name, description, null, "OPTIONAL", "DEFAULT");
    }

    public CliArgumentParameterData(
            String name, String description, String mode, String valueMode) {
        this(name, description, null, mode, valueMode);
    }
}
