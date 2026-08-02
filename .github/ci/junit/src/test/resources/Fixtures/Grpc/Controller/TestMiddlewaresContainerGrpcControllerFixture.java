/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

import io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware;
import io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware;

/** Fixture using the explicit {@code @Middlewares({...})} container form. */
@Service(service = "pkg.Wrapped")
public class TestMiddlewaresContainerGrpcControllerFixture {

    @Method(name = "Wrapped")
    @Middlewares({@Middleware(name = AuthMiddleware.class), @Middleware(name = AuditMiddleware.class)})
    public ServiceResponseContract wrapped(ContainerContract container, RouteContract route) {
        return ServiceResponse.ok();
    }
}
