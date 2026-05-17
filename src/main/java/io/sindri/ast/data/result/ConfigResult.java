/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data.result;

import java.util.List;

public record ConfigResult(
        String namespace,
        String dir,
        String dataPath,
        String dataNamespace,
        List<String> providers) {

    public ConfigResult() {
        this("", "", "", "", List.of());
    }
}
