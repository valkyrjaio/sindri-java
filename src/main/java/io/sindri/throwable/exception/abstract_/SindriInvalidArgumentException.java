/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.throwable.exception.abstract_;

import io.sindri.throwable.contract.SindriThrowable;
import io.valkyrja.throwable.exception.InvalidArgumentException;

public abstract class SindriInvalidArgumentException extends InvalidArgumentException
        implements SindriThrowable {

    protected SindriInvalidArgumentException(String message) {
        super(message);
    }

    protected SindriInvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
