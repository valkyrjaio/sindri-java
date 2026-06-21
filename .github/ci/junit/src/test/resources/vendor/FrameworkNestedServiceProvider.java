package vendor;

import java.util.Map;

public final class FrameworkNestedServiceProvider {
    public Map<Class<?>, Object> publishers() {
        return Map.of(
                FrameworkNestedService.class,
                FrameworkNestedServiceProvider::publishFrameworkNestedService);
    }

    public static void publishFrameworkNestedService(Object container) {}
}
