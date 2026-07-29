/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.generator.ast.golden;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sindri.ast.data.HttpParameterData;
import io.sindri.ast.data.HttpRouteData;
import io.sindri.generator.ast.cli.AstCliDataFileGenerator;
import io.sindri.generator.ast.container.AstContainerDataFileGenerator;
import io.sindri.generator.ast.event.AstEventDataFileGenerator;
import io.sindri.generator.ast.grpc.AstGrpcDataFileGenerator;
import io.sindri.generator.ast.http.AstHttpDataFileGenerator;
import io.valkyrja.http.routing.data.DynamicRoute;
import io.valkyrja.http.routing.data.Parameter;
import io.valkyrja.http.routing.data.contract.DynamicRouteContract;
import io.valkyrja.http.routing.data.contract.ParameterContract;
import io.valkyrja.http.routing.processor.Processor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Full-output golden/snapshot tests for the Ast data-file generators. Unlike the per-generator unit
 * tests (which assert individual substrings), these pin the ENTIRE emitted source against a
 * committed golden file, so any change to the generated shape — spacing, ordering, imports,
 * wrappers — is caught and must be an intentional golden update.
 *
 * <p>The HTTP inputs exercise the meaningful structure: a GET/POST split plus a dynamic {@code
 * /users/{id}} route carrying a parameter, so {@code routes}, {@code paths}, {@code dynamicPaths}
 * and {@code regexes} all populate. The dynamic route's regex is computed by the real framework
 * {@link Processor} — exactly as {@code HttpRouteAttributeReader} does in production — so the
 * golden pins the regex the framework actually emits and a change to its format (for example
 * dropping the PCRE delimiters around {@code ^...$}) fails here instead of silently changing every
 * generated cache.
 *
 * <p>To refresh the goldens after an intentional generator change, run this suite with {@code
 * GOLDEN_UPDATE=1} set — each {@code src/test/resources/golden/*.golden} is rewritten from the
 * matching generator output — then review and commit the new snapshots.
 */
final class GoldenSnapshotTest {

    private static final String PKG = "app.data";

    @Test
    void httpRoutingDataMatchesGolden(@TempDir Path dir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("users.index", "fixtures.provider.UserProvider::index");
        routes.put("users.show", "fixtures.provider.UserProvider::show");
        routes.put("users.store", "fixtures.provider.UserProvider::store");

        Map<String, HttpRouteData> routeData = new LinkedHashMap<>();
        routeData.put("users.index", new HttpRouteData("/users", "users.index", List.of("GET")));
        routeData.put(
                "users.show",
                dynamicRouteData(
                        "/users/{id}",
                        "users.show",
                        List.of("GET"),
                        new HttpParameterData("id", "[0-9]+")));
        routeData.put("users.store", new HttpRouteData("/users", "users.store", List.of("POST")));

        new AstHttpDataFileGenerator()
                .generateFile(dir.toString(), "AppHttpRoutingData", PKG, routes, routeData);

        assertGolden(dir.resolve("AppHttpRoutingData.java"), "AppHttpRoutingData.golden");
    }

    @Test
    void cliRoutingDataMatchesGolden(@TempDir Path dir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("greet", "fixtures.command.GreetCommand::handle");
        routes.put("farewell", "fixtures.command.FarewellCommand::handle");

        new AstCliDataFileGenerator()
                .generateFile(dir.toString(), "AppCliRoutingData", PKG, routes);

        assertGolden(dir.resolve("AppCliRoutingData.java"), "AppCliRoutingData.golden");
    }

    @Test
    void containerDataMatchesGolden(@TempDir Path dir) throws IOException {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        publishers.put(
                "fixtures.service.ServiceA",
                new String[] {"fixtures.provider.ProviderA", "publishA"});
        publishers.put(
                "fixtures.service.ServiceB",
                new String[] {"fixtures.provider.ProviderB", "publishB"});

        new AstContainerDataFileGenerator()
                .generateFile(dir.toString(), "AppContainerData", PKG, publishers);

        assertGolden(dir.resolve("AppContainerData.java"), "AppContainerData.golden");
    }

    @Test
    void eventDataMatchesGolden(@TempDir Path dir) throws IOException {
        Map<String, String> listeners = new LinkedHashMap<>();
        listeners.put("user.created", "fixtures.handler.UserHandler::onCreate");
        listeners.put("user.deleted", "fixtures.handler.UserHandler::onDelete");

        new AstEventDataFileGenerator()
                .generateFile(dir.toString(), "AppEventData", PKG, listeners);

        assertGolden(dir.resolve("AppEventData.java"), "AppEventData.golden");
    }

    @Test
    void grpcRoutingDataMatchesGolden(@TempDir Path dir) throws IOException {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("/app.Greeter/SayHello", "fixtures.service.GreeterService::sayHello");
        routes.put("/app.Greeter/SayGoodbye", "fixtures.service.GreeterService::sayGoodbye");

        new AstGrpcDataFileGenerator()
                .generateFile(dir.toString(), "AppGrpcRoutingData", PKG, routes);

        assertGolden(dir.resolve("AppGrpcRoutingData.java"), "AppGrpcRoutingData.golden");
    }

    /**
     * Build a dynamic route whose match regex is precomputed by the framework {@link Processor},
     * mirroring what {@code HttpRouteAttributeReader} stores for an annotated dynamic route.
     */
    private static HttpRouteData dynamicRouteData(
            String path,
            String name,
            List<String> requestMethods,
            HttpParameterData... parameters) {
        List<ParameterContract> frameworkParameters = new ArrayList<>();
        for (HttpParameterData parameter : parameters) {
            frameworkParameters.add(new Parameter(parameter.name(), parameter.regex()));
        }

        // The handler is never invoked — the Processor only rewrites the path into a regex.
        DynamicRoute route =
                new DynamicRoute(
                        path,
                        name,
                        "",
                        frameworkParameters,
                        (container, dispatched) -> {
                            throw new AssertionError("unreachable");
                        });
        String regex = ((DynamicRouteContract) new Processor().route(route)).getRegex();

        return new HttpRouteData(
                path,
                name,
                null,
                requestMethods,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                true,
                List.of(parameters),
                regex);
    }

    /**
     * Compare the generated source against the committed golden, refreshing it first when {@code
     * GOLDEN_UPDATE=1} is set (mirroring the TypeScript port's snapshot switch).
     */
    private static void assertGolden(Path generated, String goldenName) throws IOException {
        String actual = Files.readString(generated);
        Path golden = Path.of("src/test/resources/golden", goldenName);

        if ("1".equals(System.getenv("GOLDEN_UPDATE"))) {
            Files.writeString(golden, actual);
        }

        assertEquals(Files.readString(golden), actual);
    }
}
