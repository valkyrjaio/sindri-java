/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

/** Fixture whose method name is a constant reference rather than a string literal. */
@Service(service = "pkg.Svc")
public class TestNonLiteralMethodGrpcControllerFixture {

    public static final String METHOD_NAME = "Ping";

    @Method(name = METHOD_NAME)
    public ServiceResponseContract ping(ContainerContract container, RouteContract route) {
        return ServiceResponse.ok();
    }
}
