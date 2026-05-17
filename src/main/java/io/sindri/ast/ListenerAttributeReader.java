/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast;

import io.sindri.ast.abstract_.AstReader;
import io.sindri.ast.contract.ListenerAttributeReaderContract;
import io.sindri.ast.data.result.ListenerAttributeResult;
import java.util.Map;

public class ListenerAttributeReader extends AstReader implements ListenerAttributeReaderContract {

    @Override
    public ListenerAttributeResult readFile(String filePath) {
        return new ListenerAttributeResult(Map.of());
    }
}
