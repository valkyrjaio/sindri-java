/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.generator.ast.golden;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sindri.ast.data.HttpRouteData;
import io.sindri.generator.ast.cli.AstCliDataFileGenerator;
import io.sindri.generator.ast.container.AstContainerDataFileGenerator;
import io.sindri.generator.ast.event.AstEventDataFileGenerator;
import io.sindri.generator.ast.http.AstHttpDataFileGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Full-output golden/snapshot tests for the Ast data-file generators. Unlike the per-generator unit
 * tests (which assert individual substrings), these pin the ENTIRE emitted source against a committed
 * golden file, so any change to the generated shape — spacing, ordering, imports, wrappers — is caught
 * and must be an intentional golden update.
 *
 * <p>To refresh the goldens after an intentional generator change, temporarily re-add the
 * {@code Files.writeString(golden, actual)} line in {@link #assertGolden} (see git history), run the
 * suite once, then remove it again.
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
        routeData.put("users.show", new HttpRouteData("/users/{id}", "users.show", List.of("GET")));
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

        new AstCliDataFileGenerator().generateFile(dir.toString(), "AppCliRoutingData", PKG, routes);

        assertGolden(dir.resolve("AppCliRoutingData.java"), "AppCliRoutingData.golden");
    }

    @Test
    void containerDataMatchesGolden(@TempDir Path dir) throws IOException {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        publishers.put(
                "fixtures.service.ServiceA", new String[] {"fixtures.provider.ProviderA", "publishA"});
        publishers.put(
                "fixtures.service.ServiceB", new String[] {"fixtures.provider.ProviderB", "publishB"});

        new AstContainerDataFileGenerator()
                .generateFile(dir.toString(), "AppContainerData", PKG, publishers);

        assertGolden(dir.resolve("AppContainerData.java"), "AppContainerData.golden");
    }

    @Test
    void eventDataMatchesGolden(@TempDir Path dir) throws IOException {
        Map<String, String> listeners = new LinkedHashMap<>();
        listeners.put("user.created", "fixtures.handler.UserHandler::onCreate");
        listeners.put("user.deleted", "fixtures.handler.UserHandler::onDelete");

        new AstEventDataFileGenerator().generateFile(dir.toString(), "AppEventData", PKG, listeners);

        assertGolden(dir.resolve("AppEventData.java"), "AppEventData.golden");
    }

    private static void assertGolden(Path generated, String goldenName) throws IOException {
        String actual = Files.readString(generated);
        Path golden = Path.of("src/test/resources/golden", goldenName);
        assertEquals(Files.readString(golden), actual);
    }
}
