import io.valkyrja.http.routing.data.Route;
import java.util.List;

public class TestNoPackageRouteProviderFixture {

    public List<Object> getRoutes() {
        return List.of(new Route("/np", "np", TestNoPackageRouteProviderFixture::h));
    }
}
