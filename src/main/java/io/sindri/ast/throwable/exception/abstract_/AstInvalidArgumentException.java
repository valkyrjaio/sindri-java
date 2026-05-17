/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.throwable.exception.abstract_;

import io.sindri.ast.throwable.contract.AstThrowable;
import io.sindri.throwable.exception.abstract_.SindriInvalidArgumentException;

public abstract class AstInvalidArgumentException extends SindriInvalidArgumentException
        implements AstThrowable {

    protected AstInvalidArgumentException(String message) {
        super(message);
    }

    protected AstInvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
