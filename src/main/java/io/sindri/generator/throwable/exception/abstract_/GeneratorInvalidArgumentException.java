/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.generator.throwable.exception.abstract_;

import io.sindri.generator.throwable.contract.GeneratorThrowable;
import io.sindri.throwable.exception.abstract_.SindriInvalidArgumentException;

public abstract class GeneratorInvalidArgumentException extends SindriInvalidArgumentException
        implements GeneratorThrowable {

    protected GeneratorInvalidArgumentException(String message) {
        super(message);
    }

    protected GeneratorInvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
