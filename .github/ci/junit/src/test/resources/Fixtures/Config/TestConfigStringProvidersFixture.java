package io.sindri.tests.fixtures.config;

import java.util.List;

public final class TestConfigStringProvidersFixture {

    public TestConfigStringProvidersFixture() {
        this(
                "io.sindri.tests",
                "ud",
                "v",
                "env",
                true,
                "UTC",
                "secret",
                "io/sindri/tests/data",
                "io.sindri.tests.data",
                List.of("notAProvider"),
                List.of());
    }

    public TestConfigStringProvidersFixture(
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
