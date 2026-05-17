/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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
