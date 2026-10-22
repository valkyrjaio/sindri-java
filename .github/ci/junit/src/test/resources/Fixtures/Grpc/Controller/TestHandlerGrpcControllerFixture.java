/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.fixtures.grpc.controller;

import io.sindri.tests.fixtures.grpc.handler.GreeterHandler;
import io.sindri.tests.fixtures.grpc.message.Hello;
import io.sindri.tests.fixtures.grpc.message.HelloReply;
import io.sindri.tests.fixtures.grpc.message.HelloRequest;
import io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware;
import io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware;
import io.sindri.tests.fixtures.grpc.middleware.CaughtMiddleware;
import io.sindri.tests.fixtures.grpc.middleware.DispatchedMiddleware;
import io.sindri.tests.fixtures.grpc.middleware.MatchedMiddleware;
import io.sindri.tests.fixtures.grpc.middleware.SendingMiddleware;
import io.valkyrja.grpc.routing.attribute.Route;
import io.valkyrja.grpc.routing.attribute.Service;
import io.valkyrja.grpc.routing.attribute.route.Middleware;
import io.valkyrja.grpc.routing.attribute.route.RouteHandler;

/** Fixture covering each way a route names its handler, its message types and its middleware. */
@Service(service = "pkg.Handled")
public class TestHandlerGrpcControllerFixture {

    // The handler member names the handler, the message types name real classes, and every one of
    // the five stage members names its own stage's middleware (one as an array, four bare).
    @Route(
            name = "Member",
            handler =
                    @RouteHandler(
                            handlerClass = GreeterHandler.class,
                            handlerMethod = "memberHandler"),
            requestType = HelloRequest.class,
            responseType = HelloReply.class,
            routeMatchedMiddleware = {AuthMiddleware.class, MatchedMiddleware.class},
            routeDispatchedMiddleware = DispatchedMiddleware.class,
            throwableCaughtMiddleware = CaughtMiddleware.class,
            sendingResponseMiddleware = SendingMiddleware.class,
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

    // A stage member and the convenience attribute both name the same stage, so the stage holds
    // the member's class first and the attribute's after it.
    @Route(name = "Combined", routeMatchedMiddleware = MatchedMiddleware.class)
    @Middleware(name = AuthMiddleware.class)
    public Object combined(Object container, Object route) {
        return null;
    }

    // Protobuf nests every message in an outer class unless the schema sets java_multiple_files,
    // so a message type is written `Outer.Inner` and the file imports the outer class alone.
    @Route(
            name = "Nested",
            requestType = Hello.HelloRequest.class,
            responseType = Hello.HelloReply.class)
    public Object nested(Object container, Object route) {
        return null;
    }

    // Every optional member spelled at its default names nothing, an unknown member of the
    // handler hits its default switch arm, and the sentinel's qualified spelling means the same.
    @Route(
            name = "Defaults",
            handler = @RouteHandler(handlerClass = Void.class, unknown = "x"),
            requestType = Void.class,
            responseType = java.lang.Void.class,
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
