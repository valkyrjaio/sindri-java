import java.util.List;

public final class TestConfigDefaultPackageFixture {

    public TestConfigDefaultPackageFixture() {
        this(
                "",
                "ud",
                "v",
                "env",
                true,
                "UTC",
                "secret",
                "data",
                "data",
                List.of(new SomeProvider()),
                List.of());
    }

    public TestConfigDefaultPackageFixture(
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
