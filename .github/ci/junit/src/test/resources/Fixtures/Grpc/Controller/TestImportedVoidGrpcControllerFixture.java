/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

import io.valkyrja.grpc.routing.attribute.Route;
import io.valkyrja.grpc.routing.attribute.Service;
import java.lang.Void;

/** Fixture importing the sentinel itself, which Java permits and which names the sentinel. */
@Service(service = "pkg.ImportedVoid")
public class TestImportedVoidGrpcControllerFixture {

    @Route(name = "ImportedVoid", requestType = Void.class)
    public Object importedVoid(Object container, Object route) {
        return null;
    }
}
