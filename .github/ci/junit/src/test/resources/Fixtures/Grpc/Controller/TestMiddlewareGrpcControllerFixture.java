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

/** Fixture whose handler carries repeatable {@code @Middleware} for stage classification. */
@Service(service = "pkg.Guarded")
public class TestMiddlewareGrpcControllerFixture {

    @Method(name = "Guarded")
    @Middleware(name = AuthMiddleware.class)
    @Middleware(name = AuditMiddleware.class)
    public ServiceResponseContract guarded(ContainerContract container, RouteContract route) {
        return ServiceResponse.ok();
    }
}
