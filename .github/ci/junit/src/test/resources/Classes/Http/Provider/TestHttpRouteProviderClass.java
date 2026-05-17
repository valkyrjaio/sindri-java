package io.sindri.tests.classes.http.provider;

import io.sindri.tests.classes.http.controller.TestHttpControllerClass;

import java.util.List;

public final class TestHttpRouteProviderClass {

    public List<Class<?>> getControllerClasses() {
        return List.of(TestHttpControllerClass.class);
    }

    public List<Object> getRoutes() {
        return List.of();
    }

    public static void getHandler(Object container, Object route) {}

    public static void postHandler(Object container, Object route) {}
}