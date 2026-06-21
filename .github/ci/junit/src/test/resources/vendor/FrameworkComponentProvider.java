package vendor;

import java.util.List;

/**
 * A stand-in for a framework component provider that lives OUTSIDE the app namespace. It is loaded
 * by Sindri from the classpath (this file is a test resource), exercising the sources-jar fallback
 * in fqnToFilePath, and it nests a further framework provider to prove deep recursion.
 */
public final class FrameworkComponentProvider {
    public List<Object> getComponentProviders(Object app) {
        return List.of(new FrameworkNestedComponentProvider());
    }

    public List<Object> getContainerProviders(Object app) {
        return List.of(new FrameworkServiceProvider());
    }
}
