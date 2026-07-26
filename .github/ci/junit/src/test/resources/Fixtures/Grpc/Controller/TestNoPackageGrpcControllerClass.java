import io.valkyrja.grpc.routing.attribute.GrpcMethod;
import io.valkyrja.grpc.routing.attribute.GrpcService;

@Service(service = "pkg.NoPkg")
public class TestNoPackageGrpcControllerClass {

    @Method(name = "Ping")
    public Object ping(Object container, Object route) {
        return null;
    }
}
