/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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
