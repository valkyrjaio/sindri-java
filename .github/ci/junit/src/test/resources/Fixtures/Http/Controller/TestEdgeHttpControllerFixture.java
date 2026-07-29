package io.sindri.tests.fixtures.http.controller;

public class TestEdgeHttpControllerFixture {

    // Marker @Route is not a NormalAnnotationExpr (skipped); @RouteHandler with only a
    // handlerMethod pair exercises the non-handlerClass branch.
    @Route
    @RouteHandler(handlerMethod = "h")
    public static void markerRoute() {}

    // @RouteHandler as a marker is present but not a NormalAnnotationExpr.
    @Route(name = "marker.handler")
    @RouteHandler
    public static void markerHandler() {}

    // @RouteHandler with a member that is neither handlerClass nor handlerMethod.
    @Route(name = "extra.handler")
    @RouteHandler(handlerClass = Foo.class, handlerMethod = "m", extra = "z")
    public static void extraHandler() {}

    // Normal @Route with an unknown member (switch default) and no @RouteHandler.
    @Route(path = "/x", name = "edge", unknown = "z")
    public static void extraMember() {}
}
