/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.throwable.exception.abstract_;

import io.sindri.throwable.contract.SindriThrowable;
import io.valkyrja.throwable.exception.RuntimeException;

public abstract class SindriRuntimeException extends RuntimeException implements SindriThrowable {

    protected SindriRuntimeException(String message) {
        super(message);
    }

    protected SindriRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
