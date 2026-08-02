/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
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
