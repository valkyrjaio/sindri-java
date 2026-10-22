/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

import io.sindri.tests.fixtures.grpc.handler.GreeterHandler;
import io.sindri.tests.fixtures.grpc.message.HelloReply;
import io.sindri.tests.fixtures.grpc.message.HelloRequest;
import io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware;
import io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware;
import io.valkyrja.grpc.routing.attribute.Route;
import io.valkyrja.grpc.routing.attribute.Service;
import io.valkyrja.grpc.routing.attribute.route.RouteHandler;

/** Fixture covering each way a route names its handler, its message types and its middleware. */
@Service(service = "pkg.Handled")
public class TestHandlerGrpcControllerFixture {

    // The handler member names the handler, the message types name real classes, and two stage
    // members name middleware (one as an array, one as a bare value).
    @Route(
            name = "Member",
            handler =
                    @RouteHandler(
                            handlerClass = GreeterHandler.class,
                            handlerMethod = "memberHandler"),
            requestType = HelloRequest.class,
            responseType = HelloReply.class,
            routeMatchedMiddleware = {AuthMiddleware.class, AuditMiddleware.class},
            responseSentMiddleware = AuditMiddleware.class)
    public Object member(Object container, Object route) {
        return null;
    }

    // Both spellings name a handler, and the convenience attribute wins.
    @Route(
            name = "Attribute",
            handler =
                    @RouteHandler(
                            handlerClass = GreeterHandler.class,
                            handlerMethod = "memberHandler"))
    @RouteHandler(handlerClass = GreeterHandler.class, handlerMethod = "attributeHandler")
    public Object attribute(Object container, Object route) {
        return null;
    }

    // Every optional member spelled at its default names nothing, and an unknown member of the
    // handler hits its default switch arm.
    @Route(
            name = "Defaults",
            handler = @RouteHandler(handlerClass = Void.class, unknown = "x"),
            requestType = Void.class,
            responseType = Void.class,
            routeMatchedMiddleware = {})
    public Object defaults(Object container, Object route) {
        return null;
    }

    // A marker handler names nothing, a non-literal message type is no type, and a stage member
    // that names something other than a class holds nothing.
    @Route(
            name = "Marker",
            handler = @RouteHandler,
            requestType = REQUEST,
            routeMatchedMiddleware = {OTHER})
    public Object marker(Object container, Object route) {
        return null;
    }
}
