import io.valkyrja.http.routing.data.Route;
import java.util.List;

public class TestNoPackageRouteProviderClass {

    public List<Object> getRoutes() {
        return List.of(new Route("/np", "np", TestNoPackageRouteProviderClass::h));
    }
}
