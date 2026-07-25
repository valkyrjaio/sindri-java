/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.fixtures.grpc.controller;

/** Fixture whose method name is a constant reference rather than a string literal. */
@Service(service = "pkg.Svc")
public class TestNonLiteralMethodGrpcControllerClass {

    public static final String METHOD_NAME = "Ping";

    @Method(name = METHOD_NAME)
    public ServiceResponseContract ping(ContainerContract container, RouteContract route) {
        return ServiceResponse.ok();
    }
}
