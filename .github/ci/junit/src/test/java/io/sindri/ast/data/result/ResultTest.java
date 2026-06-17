/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.ast.data.result;

import com.github.javaparser.ast.expr.NameExpr;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResultTest {

    @Test
    void componentProviderResult_noArgConstructor_isEmpty() {
        ComponentProviderResult result = new ComponentProviderResult();

        assertTrue(result.componentProviders().isEmpty());
        assertTrue(result.serviceProviders().isEmpty());
        assertTrue(result.listenerProviders().isEmpty());
        assertTrue(result.cliRouteProviders().isEmpty());
        assertTrue(result.httpRouteProviders().isEmpty());
    }

    @Test
    void componentProviderResult_merge_combinesLists() {
        ComponentProviderResult a = new ComponentProviderResult(
                List.of("com.example.ComponentA"),
                List.of("com.example.ServiceA"),
                List.of("com.example.ListenerA"),
                List.of("com.example.CliA"),
                List.of("com.example.HttpA"));
        ComponentProviderResult b = new ComponentProviderResult(
                List.of("com.example.ComponentB"),
                List.of("com.example.ServiceB"),
                List.of("com.example.ListenerB"),
                List.of("com.example.CliB"),
                List.of("com.example.HttpB"));

        ComponentProviderResult merged = a.merge(b);

        assertEquals(2, merged.componentProviders().size());
        assertEquals(2, merged.serviceProviders().size());
        assertEquals(2, merged.listenerProviders().size());
        assertEquals(2, merged.cliRouteProviders().size());
        assertEquals(2, merged.httpRouteProviders().size());
        assertTrue(merged.serviceProviders().contains("com.example.ServiceA"));
        assertTrue(merged.serviceProviders().contains("com.example.ServiceB"));
    }

    @Test
    void componentProviderResult_merge_deduplicates() {
        ComponentProviderResult a = new ComponentProviderResult(
                List.of(),
                List.of("com.example.ServiceA"),
                List.of(),
                List.of(),
                List.of());
        ComponentProviderResult b = new ComponentProviderResult(
                List.of(),
                List.of("com.example.ServiceA"),
                List.of(),
                List.of(),
                List.of());

        ComponentProviderResult merged = a.merge(b);

        assertEquals(1, merged.serviceProviders().size());
    }

    @Test
    void serviceProviderResult_noArgConstructor_isEmpty() {
        ServiceProviderResult result = new ServiceProviderResult();

        assertTrue(result.serviceClasses().isEmpty());
        assertTrue(result.publishers().isEmpty());
    }

    @Test
    void serviceProviderResult_merge_combinesEntries() {
        ServiceProviderResult a = new ServiceProviderResult(
                List.of("com.example.ServiceA"),
                Map.of("com.example.ServiceA", new String[]{"com.example.ProviderA", "publishA"}));
        ServiceProviderResult b = new ServiceProviderResult(
                List.of("com.example.ServiceB"),
                Map.of("com.example.ServiceB", new String[]{"com.example.ProviderB", "publishB"}));

        ServiceProviderResult merged = a.merge(b);

        assertEquals(2, merged.serviceClasses().size());
        assertEquals(2, merged.publishers().size());
        assertTrue(merged.publishers().containsKey("com.example.ServiceA"));
        assertTrue(merged.publishers().containsKey("com.example.ServiceB"));
    }

    @Test
    void listenerProviderResult_noArgConstructor_isEmpty() {
        ListenerProviderResult result = new ListenerProviderResult();

        assertTrue(result.listenerClasses().isEmpty());
        assertTrue(result.listeners().isEmpty());
    }

    @Test
    void listenerProviderResult_merge_combinesEntries() {
        NameExpr exprA = new NameExpr("listenerA");
        NameExpr exprB = new NameExpr("listenerB");

        ListenerProviderResult a = new ListenerProviderResult(
                List.of("com.example.ListenerA"),
                List.of(exprA));
        ListenerProviderResult b = new ListenerProviderResult(
                List.of("com.example.ListenerB"),
                List.of(exprB));

        ListenerProviderResult merged = a.merge(b);

        assertEquals(2, merged.listenerClasses().size());
        assertEquals(2, merged.listeners().size());
    }

    @Test
    void routeProviderResult_noArgConstructor_isEmpty() {
        RouteProviderResult result = new RouteProviderResult();

        assertTrue(result.controllerClasses().isEmpty());
        assertTrue(result.routes().isEmpty());
    }

    @Test
    void routeProviderResult_merge_combinesEntries() {
        NameExpr exprA = new NameExpr("routeA");
        NameExpr exprB = new NameExpr("routeB");

        RouteProviderResult a = new RouteProviderResult(
                List.of("com.example.ControllerA"),
                List.of(exprA));
        RouteProviderResult b = new RouteProviderResult(
                List.of("com.example.ControllerB"),
                List.of(exprB));

        RouteProviderResult merged = a.merge(b);

        assertEquals(2, merged.controllerClasses().size());
        assertEquals(2, merged.routes().size());
    }

    @Test
    void httpRouteAttributeResult_noArgConstructor_isEmpty() {
        HttpRouteAttributeResult result = new HttpRouteAttributeResult();

        assertTrue(result.routes().isEmpty());
        assertTrue(result.routeData().isEmpty());
    }

    @Test
    void cliRouteAttributeResult_noArgConstructor_isEmpty() {
        CliRouteAttributeResult result = new CliRouteAttributeResult();

        assertTrue(result.routes().isEmpty());
    }
}
