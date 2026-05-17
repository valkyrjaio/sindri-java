/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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
