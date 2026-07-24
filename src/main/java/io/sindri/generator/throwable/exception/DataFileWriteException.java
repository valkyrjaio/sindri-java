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

/**
 * Thrown when a generated data file could not be written, so generation does not silently report
 * success while leaving a stale (or missing) data class on disk.
 */
public class DataFileWriteException extends GeneratorRuntimeException {

    public DataFileWriteException(String message) {
        super(message);
    }
}
