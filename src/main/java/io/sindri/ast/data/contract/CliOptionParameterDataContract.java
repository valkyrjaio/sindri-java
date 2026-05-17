/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data.contract;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface CliOptionParameterDataContract {

    String name();

    String description();

    String valueDisplayName();

    @Nullable String cast();

    String defaultValue();

    List<String> shortNames();

    List<String> validValues();

    String mode();

    String valueMode();
}
