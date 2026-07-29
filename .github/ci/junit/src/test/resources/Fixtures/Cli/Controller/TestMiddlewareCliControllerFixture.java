/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.fixtures.cli.controller;

import io.sindri.tests.fixtures.cli.middleware.AuditMiddleware;
import io.sindri.tests.fixtures.cli.middleware.AuthMiddleware;

public class TestMiddlewareCliControllerFixture {

    @Route(name = "guarded", description = "guarded command")
    @Middleware(name = AuthMiddleware.class)
    @Middleware(name = AuditMiddleware.class)
    public void guarded() {}
}
