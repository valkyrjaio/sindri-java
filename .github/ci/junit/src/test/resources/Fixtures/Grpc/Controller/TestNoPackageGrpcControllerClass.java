import io.valkyrja.grpc.routing.attribute.GrpcMethod;
import io.valkyrja.grpc.routing.attribute.GrpcService;

@GrpcService(service = "pkg.NoPkg")
public class TestNoPackageGrpcControllerClass {

    @GrpcMethod(name = "Ping")
    public Object ping(Object container, Object route) {
        return null;
    }
}
