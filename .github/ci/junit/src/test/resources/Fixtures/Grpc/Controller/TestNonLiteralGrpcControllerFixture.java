/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

/**
 * Fixture whose service name is a constant reference rather than a string literal, which sindri
 * cannot resolve syntactically.
 */
@Service(service = SERVICE_NAME)
public class TestNonLiteralGrpcControllerFixture {

    public static final String SERVICE_NAME = "pkg.NonLiteral";

    @Method(name = "Ping")
    public ServiceResponseContract ping(ContainerContract container, RouteContract route) {
        return ServiceResponse.ok();
    }
}
