/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.ConfigDataContract;
import java.util.List;

public record ConfigData(
        String namespace, String dir, String dataPath, String dataNamespace, List<String> providers)
        implements ConfigDataContract {

    public ConfigData(String namespace, String dir, String dataPath, String dataNamespace) {
        this(namespace, dir, dataPath, dataNamespace, List.of());
    }
}
