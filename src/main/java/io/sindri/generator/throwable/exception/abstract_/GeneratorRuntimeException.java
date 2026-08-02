/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.generator.throwable.exception.abstract_;

import io.sindri.generator.throwable.contract.GeneratorThrowable;
import io.sindri.throwable.exception.abstract_.SindriRuntimeException;

public abstract class GeneratorRuntimeException extends SindriRuntimeException
        implements GeneratorThrowable {

    protected GeneratorRuntimeException(String message) {
        super(message);
    }

    protected GeneratorRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
