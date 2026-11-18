package io.sindri.tests.fixtures.http.controller;

import io.sindri.tests.fixtures.http.provider.TestHttpRouteProviderFixture;
import io.valkyrja.http.routing.attribute.DynamicRoute;
import io.valkyrja.http.routing.attribute.Parameter;
import io.valkyrja.http.routing.attribute.route.Handler;
import io.valkyrja.http.routing.constant.Regex;

public class TestDynamicRouteHttpControllerFixture {

    @DynamicRoute(
            path = "/users/{id}",
            name = "users.show",
            parameters = {@Parameter(name = "id", regex = Regex.NUM)})
    @Handler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "getHandler")
    public static void show() {}

    @DynamicRoute(
            path = "/users/{id}/{slug}",
            name = "users.slug",
            parameters = {
                @Parameter(name = "id", regex = Regex.NUM),
                @Parameter(name = "slug", regex = Regex.SLUG)
            })
    @Handler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "getHandler")
    public static void slug() {}

    @DynamicRoute(
            path = "/nc/{value}",
            name = "users.nonCapture",
            parameters = {@Parameter(name = "value", regex = Regex.ALPHA, shouldCapture = false)})
    @Handler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "getHandler")
    public static void nonCapture() {}

    @DynamicRoute(
            path = "/opt/{value?}",
            name = "users.optional",
            parameters = {@Parameter(name = "value", regex = Regex.ALPHA, isOptional = true)})
    @Handler(handlerClass = TestHttpRouteProviderFixture.class, handlerMethod = "getHandler")
    public static void optional() {}
}
