/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.generator.throwable.exception;

import io.sindri.generator.throwable.exception.abstract_.GeneratorRuntimeException;

public class GeneratorUnreachableException extends GeneratorRuntimeException {

    public GeneratorUnreachableException(String message) {
        super(message);
    }

    public GeneratorUnreachableException(String message, Throwable cause) {
        super(message, cause);
    }
}
