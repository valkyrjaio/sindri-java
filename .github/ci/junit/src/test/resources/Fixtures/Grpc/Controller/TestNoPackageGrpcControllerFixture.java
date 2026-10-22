import io.valkyrja.grpc.routing.attribute.Route;
import io.valkyrja.grpc.routing.attribute.Service;

@Service(service = "pkg.NoPkg")
public class TestNoPackageGrpcControllerFixture {

    @Route(name = "Ping")
    public Object ping(Object container, Object route) {
        return null;
    }
}
