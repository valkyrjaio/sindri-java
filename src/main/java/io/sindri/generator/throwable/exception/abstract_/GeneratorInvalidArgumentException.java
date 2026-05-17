/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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
