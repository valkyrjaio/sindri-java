/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

import io.sindri.tests.fixtures.grpc.handler.GreeterHandler;
import io.valkyrja.grpc.routing.attribute.Route;
import io.valkyrja.grpc.routing.attribute.Service;
import io.valkyrja.grpc.routing.attribute.route.Handler;

/** Fixture whose handler method name is a constant reference rather than a string literal. */
@Service(service = "pkg.NonLiteralHandler")
public class TestNonLiteralHandlerMethodGrpcControllerFixture {

    public static final String HANDLER = "memberHandler";

    @Route(name = "Ping")
    @Handler(handlerClass = GreeterHandler.class, handlerMethod = HANDLER)
    public Object ping(Object container, Object route) {
        return null;
    }
}
