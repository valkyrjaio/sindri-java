package vendor;

import java.util.List;

/** A second-level framework provider, reachable only via full recursion. */
public final class FrameworkNestedComponentProvider {
    public List<Object> getContainerProviders(Object app) {
        return List.of(new FrameworkNestedServiceProvider());
    }
}
