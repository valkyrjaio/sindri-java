package io.sindri.tests.fixtures.grpc.controller;

import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.grpc.message.response.contract.ServiceResponseContract;
import io.valkyrja.grpc.routing.attribute.GrpcMethod;
import io.valkyrja.grpc.routing.attribute.GrpcService;
import io.valkyrja.grpc.routing.data.contract.RouteContract;

@Deprecated
@GrpcService(service = "pkg.Greeter")
public class TestGrpcControllerClass {

    @Deprecated
    @GrpcMethod(name = "SayHello")
    public ServiceResponseContract sayHello(ContainerContract container, RouteContract route) {
        return null;
    }

    @GrpcMethod(name = "StreamHellos", clientStreaming = true, serverStreaming = true)
    public ServiceResponseContract streamHellos(ContainerContract container, RouteContract route) {
        return null;
    }

    public ServiceResponseContract notAnRpc(ContainerContract container, RouteContract route) {
        return null;
    }
}
