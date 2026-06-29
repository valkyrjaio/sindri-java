package io.sindri.tests.fixtures.config;

import java.util.List;

public final class TestConfigClass {

    public TestConfigClass() {
        this(
                "io.sindri.tests",
                "user-dir",
                "version",
                "env",
                true,
                "UTC",
                "secret",
                "io/sindri/tests/data",
                "io.sindri.tests.data",
                List.of(
                        new io.sindri.tests.fixtures.component.provider.TestComponentProviderClass()),
                List.of());
    }

    public TestConfigClass(
            String namespace,
            String userDir,
            String version,
            String env,
            boolean debug,
            String timezone,
            String secret,
            String dataPath,
            String dataNamespace,
            List<Object> providers,
            List<Object> publishers) {}
}
