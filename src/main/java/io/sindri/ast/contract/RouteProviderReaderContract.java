/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.contract;

import io.sindri.ast.data.result.RouteProviderResult;

public interface RouteProviderReaderContract {

    RouteProviderResult readFile(String filePath);
}
