package vendor;

import java.util.Map;

public final class FrameworkServiceProvider {
    public Map<Class<?>, Object> publishers() {
        return Map.of(FrameworkService.class, FrameworkServiceProvider::publishFrameworkService);
    }

    public static void publishFrameworkService(Object container) {}
}
