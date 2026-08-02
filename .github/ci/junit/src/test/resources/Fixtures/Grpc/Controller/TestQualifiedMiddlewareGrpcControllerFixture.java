/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

import io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware;

/** Fixture mixing a fully-qualified @Middleware name with a duplicate simple one (both appended). */
@Service(service = "pkg.Qualified")
public class TestQualifiedMiddlewareGrpcControllerFixture {

    @Method(name = "Qualified")
    @Middleware(name = io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware.class)
    @Middleware(name = AuthMiddleware.class)
    public ServiceResponseContract qualified(ContainerContract container, RouteContract route) {
        return ServiceResponse.ok();
    }
}
