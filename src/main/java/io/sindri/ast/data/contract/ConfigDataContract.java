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

public interface ConfigDataContract {

    String namespace();

    String dir();

    String dataPath();

    String dataNamespace();

    List<String> providers();
}
