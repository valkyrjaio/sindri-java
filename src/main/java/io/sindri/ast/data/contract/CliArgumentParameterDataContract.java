/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data.contract;

import org.jspecify.annotations.Nullable;

public interface CliArgumentParameterDataContract {

    String name();

    String description();

    @Nullable String cast();

    String mode();

    String valueMode();
}
