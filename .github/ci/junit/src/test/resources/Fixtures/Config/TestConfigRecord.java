package io.sindri.tests.fixtures.config;

import java.util.List;

public record TestConfigRecord(String namespace, String dataNamespace) {

    public TestConfigRecord() {
        this(
                "io.sindri.tests",
                "user-dir",
                "version",
                "env",
                true,
                "UTC",
                "secret",
                "io/sindri/tests/data",
                "other.data",
                List.of(),
                List.of());
    }

    public TestConfigRecord(
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
            List<Object> publishers) {
        this(namespace, dataNamespace);
    }
}
