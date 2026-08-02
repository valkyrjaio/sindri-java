/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.data.contract;

import org.jspecify.annotations.Nullable;

public interface ListenerDataContract {

    String eventId();

    String name();

    @Nullable HandlerDataContract handler();
}
