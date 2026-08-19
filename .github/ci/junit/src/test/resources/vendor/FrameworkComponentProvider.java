package vendor;

import java.util.List;

/**
 * A component provider outside the app namespace, loaded by Sindri from the classpath.
 */
public final class FrameworkComponentProvider {
    public List<Object> getComponentProviders(Object app) {
        return List.of(new FrameworkNestedComponentProvider());
    }

    public List<Object> getContainerProviders(Object app) {
        return List.of(new FrameworkServiceProvider());
    }
}
