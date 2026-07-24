/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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
