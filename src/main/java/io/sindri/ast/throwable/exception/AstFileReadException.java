/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.throwable.exception;

import io.sindri.ast.throwable.exception.abstract_.AstRuntimeException;

public class AstFileReadException extends AstRuntimeException {

    public AstFileReadException(String message) {
        super(message);
    }

    public AstFileReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
