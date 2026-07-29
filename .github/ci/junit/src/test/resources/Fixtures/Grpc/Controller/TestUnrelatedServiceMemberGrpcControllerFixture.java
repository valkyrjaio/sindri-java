package io.sindri.tests.fixtures.grpc.controller;

import io.valkyrja.grpc.routing.attribute.GrpcMethod;
import io.valkyrja.grpc.routing.attribute.GrpcService;

// A @Service carrying members but no `service` member is treated as absent. Where the marker
// fixture never enters the member loop at all, this one walks it to exhaustion and falls out the
// bottom — the only way to reach the loop's exit branch, since a real `service` member returns
// from inside it.
@Service(name = "pkg.Unrelated")
public class TestUnrelatedServiceMemberGrpcControllerFixture {

    @Method(name = "Ignored")
    public Object ignored(Object container, Object route) {
        return null;
    }
}
