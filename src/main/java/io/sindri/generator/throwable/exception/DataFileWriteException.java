/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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
