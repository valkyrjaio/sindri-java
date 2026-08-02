/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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
