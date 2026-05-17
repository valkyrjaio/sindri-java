/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data;

import io.sindri.ast.data.contract.ListenerDataContract;
import org.jspecify.annotations.Nullable;

public record ListenerData(String eventId, String name, @Nullable HandlerData handler)
        implements ListenerDataContract {

    public ListenerData(String eventId, String name) {
        this(eventId, name, null);
    }
}
