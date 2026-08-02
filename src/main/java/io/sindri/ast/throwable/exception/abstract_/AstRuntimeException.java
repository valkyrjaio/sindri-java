/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.throwable.exception.abstract_;

import io.sindri.ast.throwable.contract.AstThrowable;
import io.sindri.throwable.exception.abstract_.SindriRuntimeException;

public abstract class AstRuntimeException extends SindriRuntimeException implements AstThrowable {

    protected AstRuntimeException(String message) {
        super(message);
    }

    protected AstRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
