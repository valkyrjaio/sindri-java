/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.classes;

/** Fixture exposing a null static String constant, to exercise the null-value reflection path. */
public final class NullRegexConstant {

    public static final String VALUE = null;

    private NullRegexConstant() {}
}
