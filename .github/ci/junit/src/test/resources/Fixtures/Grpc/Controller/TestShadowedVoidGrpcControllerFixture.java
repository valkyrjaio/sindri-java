/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

import io.sindri.tests.fixtures.grpc.message.Void;
import io.valkyrja.grpc.routing.attribute.Route;
import io.valkyrja.grpc.routing.attribute.Service;

/** Fixture importing a class of the sentinel's own simple name, which shadows the sentinel. */
@Service(service = "pkg.Shadowed")
public class TestShadowedVoidGrpcControllerFixture {

    @Route(name = "Shadowed", requestType = Void.class)
    public Object shadowed(Object container, Object route) {
        return null;
    }
}
