/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures;

/** Fixture exposing a null static String constant, to exercise the null-value reflection path. */
public final class NullRegexConstantFixture {

    public static final String VALUE = null;

    private NullRegexConstantFixture() {}
}
