/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.fixtures.http.controller;

import io.sindri.tests.fixtures.http.middleware.AuditMiddleware;
import io.sindri.tests.fixtures.http.middleware.AuthMiddleware;

public class TestMiddlewareHttpControllerFixture {

    @Route(path = "/guarded", name = "guarded")
    @Middleware(name = AuthMiddleware.class)
    @Middleware(name = AuditMiddleware.class)
    public void guarded() {}
}
