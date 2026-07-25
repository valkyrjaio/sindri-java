/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.fixtures.grpc.controller;

import io.sindri.tests.fixtures.grpc.middleware.AuditMiddleware;
import io.sindri.tests.fixtures.grpc.middleware.AuthMiddleware;

/** Fixture using the explicit {@code @Middlewares({...})} container form. */
@Service(service = "pkg.Wrapped")
public class TestMiddlewaresContainerGrpcControllerClass {

    @Method(name = "Wrapped")
    @Middlewares({@Middleware(name = AuthMiddleware.class), @Middleware(name = AuditMiddleware.class)})
    public ServiceResponseContract wrapped(ContainerContract container, RouteContract route) {
        return ServiceResponse.ok();
    }
}
