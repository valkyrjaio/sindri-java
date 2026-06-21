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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.sindri.cli.command.GenerateDataFromConfigCommand;
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

                public final class AppCliRouteProvider {
                    public List<Class<?>> getControllerClasses() {
                        return List.of(AppCliController.class, ext.Ext.class);
                    }

                    public List<Object> getRoutes() {
                        return List.of();
                    }
                }
                """);

        writeFixture(
                appDir,
                "AppHttpRouteProvider.java",
                """
                package app;

                import java.util.List;

                public final class AppHttpRouteProvider {
                    public List<Class<?>> getControllerClasses() {
                        return List.of(AppHttpController.class, ext.Ext.class);
                    }

                    public List<Object> getRoutes() {
                        return List.of();
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
                }
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
    void generatesAllFourDataFilesFromConfig(@TempDir Path tmp) throws IOException {
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
        // ...with its Parameter regex resolved from the Regex.ALPHA constant ([a-zA-Z]+)...
        assertTrue(
                http.contains(
                        "new io.valkyrja.http.routing.data.Parameter(\"value\", \"[a-zA-Z]+\")"),
                () -> "parameter regex not resolved from Regex constant:\n" + http);
        // ...and the full match regex precomputed by the framework Processor.
        assertTrue(
                http.contains("(?<value>[a-zA-Z]+)"),
                () -> "computed route regex missing:\n" + http);

        // dynamicPaths() maps the dynamic path to its route name.
        assertTrue(
                http.contains("\"/show/{value}\", \"show\""),
                () -> "dynamicPaths entry missing:\n" + http);

        // regexes() must be populated (not the empty Map.of() default). It is the last method, so
        // everything from its declaration to EOF is its body.
        String regexesBlock = http.substring(http.indexOf("regexes()"));
        assertTrue(
                regexesBlock.contains("(?<value>[a-zA-Z]+)"),
                () -> "regexes() not populated:\n" + http);

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
}
