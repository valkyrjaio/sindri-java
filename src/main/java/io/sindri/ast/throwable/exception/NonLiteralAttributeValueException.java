/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.ast.throwable.exception;

import io.sindri.ast.throwable.exception.abstract_.AstInvalidArgumentException;

/**
 * Thrown when an attribute value that must be a string literal to be cached is a non-literal
 * expression (a constant reference, a concatenation) that sindri cannot evaluate syntactically.
 */
public class NonLiteralAttributeValueException extends AstInvalidArgumentException {

    public NonLiteralAttributeValueException(String message) {
        super(message);
    }
}
