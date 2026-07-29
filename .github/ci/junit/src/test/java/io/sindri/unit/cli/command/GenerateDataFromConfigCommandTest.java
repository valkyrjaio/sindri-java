/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.cli.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.sindri.cli.command.GenerateDataFromConfigCommand;
import io.sindri.generator.container.contract.ContainerDataFileGeneratorContract;
import io.sindri.generator.enum_.GenerateStatus;
import io.sindri.generator.throwable.exception.DataFileWriteException;
import io.valkyrja.cli.interaction.output.factory.OutputFactory;
import io.valkyrja.cli.interaction.output.factory.contract.OutputFactoryContract;
import io.valkyrja.cli.routing.data.contract.ArgumentParameterContract;
import io.valkyrja.cli.routing.data.contract.RouteContract;
import io.valkyrja.container.manager.Container;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Drives {@link GenerateDataFromConfigCommand} end-to-end against a self-consistent app tree. */
final class GenerateDataFromConfigCommandTest {

    /**
     * The full regex the framework Processor computes for {@code /show/{value}} with a
     * {@code Regex.ALPHA} parameter, exactly as it is escaped into the generated Java source. Pinned
     * whole rather than by its {@code (?<value>…)} fragment so a change to the framework's regex
     * framing — anchors, or the PCRE {@code /…/} delimiters it once carried — fails here instead of
     * silently changing every generated cache.
     */
    private static final String EXPECTED_SHOW_REGEX = "\"^\\\\/show\\\\/(?<value>[a-zA-Z]+)$\"";

    private static void writeFixture(Path appDir, String name, String contents) throws IOException {
        Files.writeString(appDir.resolve(name), contents, StandardCharsets.UTF_8);
    }

    /** Lays out a minimal, package-consistent Valkyrja app under {@code <tmp>/app}. */
    private static Path writeApp(Path tmp) throws IOException {
        Path appDir = Files.createDirectories(tmp.resolve("app"));

        writeFixture(
                appDir,
                "AppConfig.java",
                """
                package app;

                import java.util.List;

                public final class AppConfig {
                    public AppConfig() {
                        this(
                                "app", "ud", "v", "env", true, "UTC", "secret",
                                "app/data", "app.data",
                                List.of(new AppComponentProvider(), new ext.Ext()), List.of());
                    }

                    public AppConfig(
                            String namespace, String userDir, String version, String env,
                            boolean debug, String timezone, String secret, String dataPath,
                            String dataNamespace, List<Object> providers, List<Object> publishers) {}
                }
                """);

        writeFixture(
                appDir,
                "AppComponentProvider.java",
                """
                package app;

                import java.util.List;

                public final class AppComponentProvider {
                    public List<Object> getComponentProviders(Object app) {
                        return List.of(
                                new AppNestedComponentProvider(),
                                new vendor.FrameworkComponentProvider(),
                                new ext.Ext());
                    }

                    public List<Object> getContainerProviders(Object app) {
                        return List.of(new AppServiceProvider(), new ext.Ext());
                    }

                    public List<Object> getEventProviders(Object app) {
                        return List.of(new AppListenerProvider(), new ext.Ext());
                    }

                    public List<Object> getCliProviders(Object app) {
                        return List.of(new AppCliRouteProvider(), new ext.Ext());
                    }

                    public List<Object> getHttpProviders(Object app) {
                        return List.of(new AppHttpRouteProvider(), new ext.Ext());
                    }

                    public List<Object> getGrpcProviders(Object app) {
                        return List.of(new AppGrpcRouteProvider(), new ext.Ext());
                    }
                }
                """);

        writeFixture(
                appDir,
                "AppNestedComponentProvider.java",
                """
                package app;

                import java.util.List;

                public final class AppNestedComponentProvider {
                    public List<Object> getContainerProviders(Object app) {
                        return List.of(new AppServiceProvider());
                    }
                }
                """);

        writeFixture(
                appDir,
                "AppServiceProvider.java",
                """
                package app;

                import java.util.Map;

                public final class AppServiceProvider {
                    public Map<Class<?>, Object> publishers() {
                        return Map.of(AppService.class, AppServiceProvider::publishAppService);
                    }

                    public static void publishAppService(Object container) {}
                }
                """);

        writeFixture(
                appDir,
                "AppListenerProvider.java",
                """
                package app;

                import io.valkyrja.event.data.Listener;
                import java.util.List;

                public final class AppListenerProvider {
                    public List<Class<?>> getListenerClasses() {
                        return List.of(AppListener.class);
                    }

                    public List<Object> getListeners() {
                        return List.of(new Listener(AppEvent.class, "app.event", AppListener::on));
                    }
                }
                """);

        writeFixture(
                appDir,
                "AppCliRouteProvider.java",
                """
                package app;

                import java.util.List;

                import io.valkyrja.cli.routing.data.Route;

                public final class AppCliRouteProvider {
                    public List<Class<?>> getControllerClasses() {
                        return List.of(AppCliController.class, ext.Ext.class);
                    }

                    public List<Object> getRoutes() {
                        return List.of(
                                new Route("manual-cli", "Manual", AppCliRouteProvider::run),
                                // A non-string name (arg 0) cannot be keyed, so this is skipped.
                                new Route(NoName.VALUE, "Skip", AppCliRouteProvider::run));
                    }

                    public static void run(Object container, Object route) {}
                }
                """);

        writeFixture(
                appDir,
                "AppHttpRouteProvider.java",
                """
                package app;

                import java.util.List;

                import io.valkyrja.http.message.enum_.RequestMethod;
                import io.valkyrja.http.routing.data.DynamicRoute;
                import io.valkyrja.http.routing.data.Route;

                public final class AppHttpRouteProvider {
                    public List<Class<?>> getControllerClasses() {
                        return List.of(AppHttpController.class, ext.Ext.class);
                    }

                    public List<Object> getRoutes() {
                        return List.of(
                                new Route("/manual", "manual", AppHttpRouteProvider::getHandler,
                                        List.of(RequestMethod.PUT), List.of(), List.of(), List.of(),
                                        List.of(), List.of(), null, null),
                                // A hand-written regex is passed through verbatim, so it uses the
                                // Java-native anchored form the framework itself now emits.
                                new DynamicRoute("/manual/{id}", "manual.show",
                                        "^\\\\/manual\\\\/(?<id>\\\\d+)$", List.of(),
                                        AppHttpRouteProvider::getHandler),
                                // A non-string name (arg 1) cannot be keyed, so this is skipped.
                                new Route("/skip", NoName.VALUE, AppHttpRouteProvider::getHandler),
                                // Too few arguments to read a name — also skipped.
                                new Route("/one"));
                    }

                    public static void getHandler(Object container, Object route) {}
                }
                """);

        writeFixture(
                appDir,
                "AppCliController.java",
                """
                package app;

                import io.valkyrja.cli.routing.attribute.Route;
                import io.valkyrja.cli.routing.attribute.route.RouteHandler;

                public class AppCliController {
                    @Route(name = "greet", description = "Greet")
                    @RouteHandler(handlerClass = AppCliRouteProvider.class, handlerMethod = "greet")
                    public static void greet() {}
                }
                """);

        writeFixture(
                appDir,
                "AppHttpController.java",
                """
                package app;

                import io.valkyrja.http.message.enum_.RequestMethod;
                import io.valkyrja.http.routing.attribute.Parameter;
                import io.valkyrja.http.routing.attribute.Route;
                import io.valkyrja.http.routing.attribute.route.Middleware;
                import io.valkyrja.http.routing.attribute.route.RouteHandler;
                import io.valkyrja.http.routing.constant.Regex;

                public class AppHttpController {
                    @Route(path = "/test", name = "test.get", requestMethods = {RequestMethod.GET})
                    @RouteHandler(handlerClass = AppHttpRouteProvider.class, handlerMethod = "getHandler")
                    public static void get() {}

                    @Route(path = "/welcome", name = "welcome")
                    @RouteHandler(handlerClass = AppHttpRouteProvider.class, handlerMethod = "getHandler")
                    public static void welcome() {}

                    @Route(path = "/show/{value}", name = "show")
                    @Parameter(name = "value", regex = Regex.ALPHA)
                    @RouteHandler(handlerClass = AppHttpRouteProvider.class, handlerMethod = "getHandler")
                    public static void show() {}

                    @Route(path = "/pair/{a}/{b}", name = "pair")
                    @Parameter(name = "a", regex = Regex.ALPHA)
                    @Parameter(name = "b", regex = Regex.NUM)
                    @RouteHandler(handlerClass = AppHttpRouteProvider.class, handlerMethod = "getHandler")
                    public static void pair() {}

                    @Route(path = "/multi", name = "multi",
                            requestMethods = {RequestMethod.GET, RequestMethod.POST})
                    @RouteHandler(handlerClass = AppHttpRouteProvider.class, handlerMethod = "getHandler")
                    public static void multi() {}

                    @Route(path = "/bad/{x}", name = "bad")
                    @Parameter(name = "y", regex = Regex.NUM)
                    @RouteHandler(handlerClass = AppHttpRouteProvider.class, handlerMethod = "getHandler")
                    public static void bad() {}

                    // Two @Middleware whose classes cannot be resolved: MissingMiddleware is under
                    // the app namespace but has no source file (parse throws FileNotFoundException),
                    // and ext.NoMiddleware is off both the app tree and the classpath (empty path).
                    // Both exercise the middleware source resolver's unresolvable branches; neither
                    // reaches a stage, so the route stays short-form.
                    @Route(path = "/guarded", name = "guarded")
                    @Middleware(name = AppGuardMiddleware.class)
                    @Middleware(name = MissingMiddleware.class)
                    @Middleware(name = ext.NoMiddleware.class)
                    @RouteHandler(handlerClass = AppHttpRouteProvider.class, handlerMethod = "getHandler")
                    public static void guarded() {}
                }
                """);

        writeFixture(
                appDir,
                "AppGrpcRouteProvider.java",
                """
                package app;

                import java.util.List;

                import io.valkyrja.grpc.routing.data.Route;

                public final class AppGrpcRouteProvider {
                    public List<Class<?>> getControllerClasses() {
                        return List.of(AppGrpcController.class, ext.Ext.class);
                    }

                    public List<Object> getRoutes() {
                        return List.of(
                                new Route("/app.Manual/Do", AppGrpcRouteProvider::run),
                                // A non-string method (arg 0) cannot be keyed, so this is skipped.
                                new Route(NoName.VALUE, AppGrpcRouteProvider::run));
                    }

                    public static Object run(Object container, Object route) {
                        return null;
                    }
                }
                """);

        writeFixture(
                appDir,
                "AppGrpcController.java",
                """
                package app;

                import io.valkyrja.grpc.routing.attribute.GrpcMethod;
                import io.valkyrja.grpc.routing.attribute.GrpcService;

                @Service(service = "app.Greeter")
                public class AppGrpcController {
                    @Method(name = "SayHello")
                    public Object sayHello(Object container, Object route) {
                        return null;
                    }

                    @Method(name = "StreamHellos", serverStreaming = true)
                    public Object streamHellos(Object container, Object route) {
                        return null;
                    }
                }
                """);

        // A resolvable @Middleware whose source parses cleanly but reaches no stage contract — it
        // exercises the resolver's successful-parse branch without changing the guarded route shape.
        writeFixture(
                appDir,
                "AppGuardMiddleware.java",
                """
                package app;

                public final class AppGuardMiddleware {}
                """);

        return appDir;
    }

    private static GenerateDataFromConfigCommand command(String configPath) {
        var container = new Container();
        container.setSingleton(OutputFactoryContract.class, new OutputFactory());
        var route = mock(RouteContract.class);
        var argument = mock(ArgumentParameterContract.class);
        when(route.getArgument("config")).thenReturn(argument);
        when(argument.getFirstValue()).thenReturn(configPath);

        return new GenerateDataFromConfigCommand(container, route);
    }

    @Test
    void generatesAllDataFilesFromConfig(@TempDir Path tmp) throws IOException {
        Path appDir = writeApp(tmp);
        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            command(appDir.resolve("AppConfig.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        Path dataDir = appDir.resolve("data");
        assertTrue(Files.exists(dataDir.resolve("AppContainerData.java")));
        assertTrue(Files.exists(dataDir.resolve("AppEventData.java")));
        assertTrue(Files.exists(dataDir.resolve("AppCliRoutingData.java")));
        assertTrue(Files.exists(dataDir.resolve("AppHttpRoutingData.java")));
        assertTrue(Files.exists(dataDir.resolve("AppGrpcRoutingData.java")));
    }

    @Test
    void generatesExpectedGrpcRoutingContent(@TempDir Path tmp) throws IOException {
        Path appDir = writeApp(tmp);
        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            command(appDir.resolve("AppConfig.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        String grpc = Files.readString(appDir.resolve("data").resolve("AppGrpcRoutingData.java"));

        assertTrue(grpc.contains("implements GrpcRoutingDataContract"), () -> grpc);
        // Controller-annotation gRPC route: keyed by /service/method, reflective handler.
        assertTrue(
                grpc.contains(
                        "\"/app.Greeter/SayHello\", () -> new io.valkyrja.grpc.routing.data.Route(\"/app.Greeter/SayHello\","
                                + " (c, r) -> new app.AppGrpcController().sayHello(c, r))"),
                () -> "controller gRPC route missing:\n" + grpc);
        // Server-streaming flag carried through.
        assertTrue(
                grpc.contains(
                        "(c, r) -> new app.AppGrpcController().streamHellos(c, r)).withServerStreaming(true)"),
                () -> "streaming gRPC route missing/incorrect:\n" + grpc);
        // Manually-defined provider getRoutes() gRPC route, combined and fully qualified.
        assertTrue(
                grpc.contains(
                        "\"/app.Manual/Do\", () -> new io.valkyrja.grpc.routing.data.Route(\"/app.Manual/Do\","
                                + " app.AppGrpcRouteProvider::run)"),
                () -> "provider gRPC route not combined:\n" + grpc);
        assertDoesNotThrow(() -> com.github.javaparser.StaticJavaParser.parse(grpc));
    }

    @Test
    void generatesExpectedHttpRoutingContent(@TempDir Path tmp) throws IOException {
        Path appDir = writeApp(tmp);
        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            command(appDir.resolve("AppConfig.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        String http =
                Files.readString(appDir.resolve("data").resolve("AppHttpRoutingData.java"));

        // routes() must build a real Supplier<RouteContract> — a Route constructor with the
        // handler method reference — not a bare route-name placeholder (the original bug).
        assertTrue(
                http.contains(
                        "\"test.get\", () -> new io.valkyrja.http.routing.data.Route(\"/test\","
                                + " \"test.get\", app.AppHttpRouteProvider::getHandler,"),
                () -> "GET route supplier missing/incorrect:\n" + http);
        assertTrue(
                http.contains("io.valkyrja.http.message.enum_.RequestMethod.GET"),
                () -> "GET request method missing:\n" + http);
        // A route with no requestMethods uses the three-arg Route constructor (HEAD + GET default).
        assertTrue(
                http.contains(
                        "\"welcome\", () -> new io.valkyrja.http.routing.data.Route(\"/welcome\","
                                + " \"welcome\", app.AppHttpRouteProvider::getHandler)"),
                () -> "default-methods route supplier missing/incorrect:\n" + http);
        // The bare-identifier placeholder must never appear as a route value.
        assertFalse(
                http.contains("\"test.get\", test.get"),
                () -> "route value is still a bare name placeholder:\n" + http);

        // paths() must include HEAD for the default-methods route (matches PHP), not drop it.
        assertTrue(http.contains("Map.entry(\"HEAD\""), () -> "HEAD path entry missing:\n" + http);
        assertTrue(http.contains("Map.entry(\"GET\""), () -> "GET path entry missing:\n" + http);
        assertTrue(
                http.contains("\"/welcome\", \"welcome\""),
                () -> "welcome path mapping missing:\n" + http);

        // A manually-defined provider getRoutes() route must be inlined (fully qualified) alongside
        // the controller-annotation routes, and registered in paths() under its request method.
        assertTrue(
                http.contains(
                        "\"manual\", () -> new io.valkyrja.http.routing.data.Route(\"/manual\","
                                + " \"manual\", app.AppHttpRouteProvider::getHandler,"),
                () -> "provider route not combined into routes():\n" + http);
        assertTrue(
                http.contains("io.valkyrja.http.message.enum_.RequestMethod.PUT"),
                () -> "provider route methods not qualified:\n" + http);
        assertTrue(
                http.contains("Map.entry(\"PUT\", Map.of(\"/manual\", \"manual\"))"),
                () -> "provider route not registered in paths():\n" + http);
    }

    @Test
    void generatesExpectedDynamicRouteContent(@TempDir Path tmp) throws IOException {
        Path appDir = writeApp(tmp);
        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            command(appDir.resolve("AppConfig.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        String http =
                Files.readString(appDir.resolve("data").resolve("AppHttpRoutingData.java"));

        // A {placeholder} route becomes a DynamicRoute supplier...
        assertTrue(
                http.contains(
                        "\"show\", () -> new io.valkyrja.http.routing.data.DynamicRoute(\"/show/{value}\","
                                + " \"show\","),
                () -> "dynamic route supplier missing/incorrect:\n" + http);
        // ...with its Parameter regex resolved from the Regex.ALPHA constant ([a-zA-Z]+), and the
        // optional and capture flags it was declared with carried through...
        assertTrue(
                http.contains(
                        "new io.valkyrja.http.routing.data.Parameter(\"value\", \"[a-zA-Z]+\", null,"
                                + " false, true, null, null)"),
                () -> "parameter regex not resolved from Regex constant:\n" + http);
        // ...and the full match regex precomputed by the framework Processor.
        assertTrue(
                http.contains(EXPECTED_SHOW_REGEX),
                () -> "computed route regex missing/incorrect:\n" + http);

        // dynamicPaths() maps the dynamic path to its route name.
        assertTrue(
                http.contains("\"/show/{value}\", \"show\""),
                () -> "dynamicPaths entry missing:\n" + http);

        // regexes() must be populated (not the empty Map.of() default). It is the last method, so
        // everything from its declaration to EOF is its body.
        String regexesBlock = http.substring(http.indexOf("regexes()"));
        assertTrue(
                regexesBlock.contains(EXPECTED_SHOW_REGEX),
                () -> "regexes() not populated with the computed regex:\n" + http);

        // The whole file must be syntactically valid Java — catches malformed suppliers or bad
        // regex/string escaping that substring checks would miss.
        assertDoesNotThrow(() -> com.github.javaparser.StaticJavaParser.parse(http));
    }

    @Test
    void generatesExpectedContainerContent(@TempDir Path tmp) throws IOException {
        Path appDir = writeApp(tmp);
        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            command(appDir.resolve("AppConfig.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        String container =
                Files.readString(appDir.resolve("data").resolve("AppContainerData.java"));

        // The app service provider's publisher must be collected into callbacks().
        assertTrue(
                container.contains("app.AppServiceProvider::publishAppService"),
                () -> "app service publisher missing from callbacks:\n" + container);
    }

    @Test
    void generatesExpectedCliRoutingContent(@TempDir Path tmp) throws IOException {
        Path appDir = writeApp(tmp);
        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            command(appDir.resolve("AppConfig.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        String cli = Files.readString(appDir.resolve("data").resolve("AppCliRoutingData.java"));

        // Controller-annotation CLI route.
        assertTrue(
                cli.contains("\"greet\", () -> new io.valkyrja.cli.routing.data.Route(\"greet\","),
                () -> "controller CLI route missing:\n" + cli);
        // Manually-defined provider getRoutes() CLI route, combined and fully qualified.
        assertTrue(
                cli.contains(
                        "\"manual-cli\", () -> new io.valkyrja.cli.routing.data.Route(\"manual-cli\","
                                + " \"Manual\", app.AppCliRouteProvider::run)"),
                () -> "provider CLI route not combined:\n" + cli);
        assertDoesNotThrow(() -> com.github.javaparser.StaticJavaParser.parse(cli));
    }

    @Test
    void resolveSourceFromClasspathReturnsEmptyOnIoError() {
        var container = new Container();
        container.setSingleton(OutputFactoryContract.class, new OutputFactory());
        var route = mock(RouteContract.class);
        var command =
                new GenerateDataFromConfigCommand(container, route) {
                    @Override
                    protected String stageSource(java.io.InputStream in) throws java.io.IOException {
                        throw new java.io.IOException("boom");
                    }

                    String resolve(String fqn) {
                        return resolveSourceFromClasspath(fqn);
                    }
                };

        // The resource resolves from the classpath, but staging it to a temp file fails.
        assertEquals("", command.resolve("vendor.FrameworkComponentProvider"));
    }

    @Test
    void resolvesFrameworkProvidersFromClasspath(@TempDir Path tmp) throws IOException {
        Path appDir = writeApp(tmp);
        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            command(appDir.resolve("AppConfig.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        String container =
                Files.readString(appDir.resolve("data").resolve("AppContainerData.java"));

        // The framework component provider lives outside the app namespace and is resolved from the
        // classpath (sources-jar fallback); its publisher must end up in callbacks().
        assertTrue(
                container.contains("vendor.FrameworkServiceProvider::publishFrameworkService"),
                () -> "framework provider publisher missing (classpath resolution failed):\n"
                        + container);
        // ...and a second-level framework provider proves the recursion goes deeper than one level.
        assertTrue(
                container.contains(
                        "vendor.FrameworkNestedServiceProvider::publishFrameworkNestedService"),
                () -> "deeply-nested framework provider publisher missing (recursion too shallow):\n"
                        + container);
    }

    @Test
    void applicationPublishersWinOverTheFrameworkForTheSameService(@TempDir Path tmp)
            throws IOException {
        Path appDir = writeAppOverridingAFrameworkService(tmp);
        var original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            command(appDir.resolve("AppConfig.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        String container =
                Files.readString(appDir.resolve("data").resolve("AppContainerData.java"));

        // The application and the framework both publish vendor.FrameworkService. Publishers merge
        // last-wins, and a provider is merged after everything it nests, so the application's entry
        // must be the one that survives. Letting the framework win here silently discards the
        // generated cache the application meant to use.
        assertTrue(
                container.contains("app.AppServiceProvider::publishOverriddenService"),
                () -> "the application publisher lost to the framework's:\n" + container);
        assertFalse(
                container.contains("vendor.FrameworkServiceProvider::publishFrameworkService"),
                () -> "the framework publisher shadowed the application's:\n" + container);
    }

    /** An app whose service provider publishes the very service the framework also publishes. */
    private static Path writeAppOverridingAFrameworkService(Path tmp) throws IOException {
        Path appDir = Files.createDirectories(tmp.resolve("app"));

        writeFixture(
                appDir,
                "AppConfig.java",
                """
                package app;

                import java.util.List;

                public final class AppConfig {
                    public AppConfig() {
                        this(
                                "app", "ud", "v", "env", true, "UTC", "secret",
                                "app/data", "app.data",
                                List.of(new AppComponentProvider()), List.of());
                    }

                    public AppConfig(
                            String namespace, String userDir, String version, String env,
                            boolean debug, String timezone, String secret, String dataPath,
                            String dataNamespace, List<Object> providers, List<Object> publishers) {}
                }
                """);

        writeFixture(
                appDir,
                "AppComponentProvider.java",
                """
                package app;

                import java.util.List;

                public final class AppComponentProvider {
                    public List<Object> getComponentProviders(Object app) {
                        return List.of(new vendor.FrameworkComponentProvider());
                    }

                    public List<Object> getContainerProviders(Object app) {
                        return List.of(new AppServiceProvider());
                    }
                }
                """);

        writeFixture(
                appDir,
                "AppServiceProvider.java",
                """
                package app;

                import java.util.Map;

                public final class AppServiceProvider {
                    public Map<Class<?>, Object> publishers() {
                        return Map.of(
                                vendor.FrameworkService.class,
                                AppServiceProvider::publishOverriddenService);
                    }

                    public static void publishOverriddenService(Object container) {}
                }
                """);

        return appDir;
    }

    @Test
    void reportsFailureWhenConfigCannotBeRead(@TempDir Path tmp) {
        var original = System.out;
        var buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            command(tmp.resolve("missing/Nope.java").toString()).execute();
        } finally {
            System.setOut(original);
        }

        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("Failed"));
    }

    @Test
    void reportsExceptionClassNameWhenMessageIsNull() {
        var container = new Container();
        container.setSingleton(OutputFactoryContract.class, new OutputFactory());
        var route = mock(RouteContract.class);
        var argument = mock(ArgumentParameterContract.class);
        when(route.getArgument("config")).thenReturn(argument);
        when(argument.getFirstValue()).thenReturn("ignored");
        // A thrown exception with a null message exercises the message-null arm of the report.
        var command =
                new GenerateDataFromConfigCommand(container, route) {
                    @Override
                    public void run(String configFilePath) {
                        throw new RuntimeException();
                    }
                };

        var original = System.out;
        var buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            command.execute();
        } finally {
            System.setOut(original);
        }

        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains(RuntimeException.class.getName()));
    }

    @Test
    void runThrowsWhenADataFileFailsToWrite(@TempDir Path tmp) throws IOException {
        Path appDir = writeApp(tmp);
        var container = new Container();
        container.setSingleton(OutputFactoryContract.class, new OutputFactory());
        var route = mock(RouteContract.class);
        // The container data generator reports FAILURE, so run() must surface it rather than
        // silently leaving a stale data class on disk.
        var command =
                new GenerateDataFromConfigCommand(container, route) {
                    @Override
                    protected ContainerDataFileGeneratorContract getContainerDataFileGenerator() {
                        var generator = mock(ContainerDataFileGeneratorContract.class);
                        when(generator.generateFile(any(), any(), any(), any()))
                                .thenReturn(GenerateStatus.FAILURE);

                        return generator;
                    }
                };

        assertThrows(
                DataFileWriteException.class,
                () -> command.run(appDir.resolve("AppConfig.java").toString()));
    }
}
