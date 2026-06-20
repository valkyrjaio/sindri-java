/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.cli.command;

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
                        return List.of(new AppNestedComponentProvider());
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
                        return List.of(AppCliController.class);
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
                        return List.of(AppHttpController.class);
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
                import io.valkyrja.http.routing.attribute.Route;
                import io.valkyrja.http.routing.attribute.route.RouteHandler;

                public class AppHttpController {
                    @Route(path = "/test", name = "test.get", requestMethods = {RequestMethod.GET})
                    @RouteHandler(handlerClass = AppHttpRouteProvider.class, handlerMethod = "getHandler")
                    public static void get() {}
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
}
