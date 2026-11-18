package io.sindri.tests.fixtures.http.controller;

public class TestEdgeHttpControllerFixture {

    // Marker @Route is not a NormalAnnotationExpr (skipped); @Handler with only a
    // handlerMethod pair exercises the non-handlerClass branch.
    @Route
    @Handler(handlerMethod = "h")
    public static void markerRoute() {}

    // @Handler as a marker is present but not a NormalAnnotationExpr.
    @Route(name = "marker.handler")
    @Handler
    public static void markerHandler() {}

    // @Handler with a member that is neither handlerClass nor handlerMethod.
    @Route(name = "extra.handler")
    @Handler(handlerClass = Foo.class, handlerMethod = "m", extra = "z")
    public static void extraHandler() {}

    // Normal @Route with an unknown member (switch default) and no @Handler.
    @Route(path = "/x", name = "edge", unknown = "z")
    public static void extraMember() {}
}
