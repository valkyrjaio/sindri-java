/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.provider;

import io.sindri.ast.CliRouteAttributeReader;
import io.sindri.ast.CliRouteParameterReader;
import io.sindri.ast.ComponentProviderReader;
import io.sindri.ast.ConfigReader;
import io.sindri.ast.HttpRouteAttributeReader;
import io.sindri.ast.HttpRouteMiddlewareReader;
import io.sindri.ast.HttpRouteParameterReader;
import io.sindri.ast.ListenerAttributeReader;
import io.sindri.ast.ListenerProviderReader;
import io.sindri.ast.RouteProviderReader;
import io.sindri.ast.ServiceProviderReader;
import io.sindri.ast.contract.CliRouteAttributeReaderContract;
import io.sindri.ast.contract.CliRouteParameterReaderContract;
import io.sindri.ast.contract.ComponentProviderReaderContract;
import io.sindri.ast.contract.ConfigReaderContract;
import io.sindri.ast.contract.HttpRouteAttributeReaderContract;
import io.sindri.ast.contract.HttpRouteMiddlewareReaderContract;
import io.sindri.ast.contract.HttpRouteParameterReaderContract;
import io.sindri.ast.contract.ListenerAttributeReaderContract;
import io.sindri.ast.contract.ListenerProviderReaderContract;
import io.sindri.ast.contract.RouteProviderReaderContract;
import io.sindri.ast.contract.ServiceProviderReaderContract;
import io.sindri.generator.ast.cli.AstCliDataFileGenerator;
import io.sindri.generator.ast.container.AstContainerDataFileGenerator;
import io.sindri.generator.ast.event.AstEventDataFileGenerator;
import io.sindri.generator.ast.http.AstHttpDataFileGenerator;
import io.sindri.generator.cli.contract.CliDataFileGeneratorContract;
import io.sindri.generator.container.contract.ContainerDataFileGeneratorContract;
import io.sindri.generator.event.contract.EventDataFileGeneratorContract;
import io.sindri.generator.http.contract.HttpDataFileGeneratorContract;
import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;
import java.util.Map;
import java.util.function.Consumer;

public final class SindriAstServiceProvider implements ServiceProviderContract {

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        return Map.ofEntries(
                Map.entry(
                        ConfigReaderContract.class, SindriAstServiceProvider::publishConfigReader),
                Map.entry(
                        ComponentProviderReaderContract.class,
                        SindriAstServiceProvider::publishComponentProviderReader),
                Map.entry(
                        ServiceProviderReaderContract.class,
                        SindriAstServiceProvider::publishServiceProviderReader),
                Map.entry(
                        RouteProviderReaderContract.class,
                        SindriAstServiceProvider::publishRouteProviderReader),
                Map.entry(
                        ListenerProviderReaderContract.class,
                        SindriAstServiceProvider::publishListenerProviderReader),
                Map.entry(
                        CliRouteAttributeReaderContract.class,
                        SindriAstServiceProvider::publishCliRouteAttributeReader),
                Map.entry(
                        HttpRouteAttributeReaderContract.class,
                        SindriAstServiceProvider::publishHttpRouteAttributeReader),
                Map.entry(
                        ListenerAttributeReaderContract.class,
                        SindriAstServiceProvider::publishListenerAttributeReader),
                Map.entry(
                        CliRouteParameterReaderContract.class,
                        SindriAstServiceProvider::publishCliRouteParameterReader),
                Map.entry(
                        HttpRouteMiddlewareReaderContract.class,
                        SindriAstServiceProvider::publishHttpRouteMiddlewareReader),
                Map.entry(
                        HttpRouteParameterReaderContract.class,
                        SindriAstServiceProvider::publishHttpRouteParameterReader),
                Map.entry(
                        ContainerDataFileGeneratorContract.class,
                        SindriAstServiceProvider::publishContainerDataFileGenerator),
                Map.entry(
                        EventDataFileGeneratorContract.class,
                        SindriAstServiceProvider::publishEventDataFileGenerator),
                Map.entry(
                        CliDataFileGeneratorContract.class,
                        SindriAstServiceProvider::publishCliDataFileGenerator),
                Map.entry(
                        HttpDataFileGeneratorContract.class,
                        SindriAstServiceProvider::publishHttpDataFileGenerator));
    }

    public static void publishContainerData(ContainerContract container) {
        publishConfigReader(container);
        publishComponentProviderReader(container);
        publishServiceProviderReader(container);
        publishRouteProviderReader(container);
        publishListenerProviderReader(container);
        publishCliRouteAttributeReader(container);
        publishHttpRouteAttributeReader(container);
        publishListenerAttributeReader(container);
        publishCliRouteParameterReader(container);
        publishHttpRouteMiddlewareReader(container);
        publishHttpRouteParameterReader(container);
        publishContainerDataFileGenerator(container);
        publishEventDataFileGenerator(container);
        publishCliDataFileGenerator(container);
        publishHttpDataFileGenerator(container);
    }

    public static void publishConfigReader(ContainerContract container) {
        container.setSingleton(ConfigReaderContract.class, new ConfigReader());
    }

    public static void publishComponentProviderReader(ContainerContract container) {
        container.setSingleton(
                ComponentProviderReaderContract.class, new ComponentProviderReader());
    }

    public static void publishServiceProviderReader(ContainerContract container) {
        container.setSingleton(ServiceProviderReaderContract.class, new ServiceProviderReader());
    }

    public static void publishRouteProviderReader(ContainerContract container) {
        container.setSingleton(RouteProviderReaderContract.class, new RouteProviderReader());
    }

    public static void publishListenerProviderReader(ContainerContract container) {
        container.setSingleton(ListenerProviderReaderContract.class, new ListenerProviderReader());
    }

    public static void publishCliRouteAttributeReader(ContainerContract container) {
        container.setSingleton(
                CliRouteAttributeReaderContract.class, new CliRouteAttributeReader());
    }

    public static void publishHttpRouteAttributeReader(ContainerContract container) {
        container.setSingleton(
                HttpRouteAttributeReaderContract.class, new HttpRouteAttributeReader());
    }

    public static void publishListenerAttributeReader(ContainerContract container) {
        container.setSingleton(
                ListenerAttributeReaderContract.class, new ListenerAttributeReader());
    }

    public static void publishCliRouteParameterReader(ContainerContract container) {
        container.setSingleton(
                CliRouteParameterReaderContract.class, new CliRouteParameterReader());
    }

    public static void publishHttpRouteMiddlewareReader(ContainerContract container) {
        container.setSingleton(
                HttpRouteMiddlewareReaderContract.class, new HttpRouteMiddlewareReader());
    }

    public static void publishHttpRouteParameterReader(ContainerContract container) {
        container.setSingleton(
                HttpRouteParameterReaderContract.class, new HttpRouteParameterReader());
    }

    public static void publishContainerDataFileGenerator(ContainerContract container) {
        container.setSingleton(
                ContainerDataFileGeneratorContract.class, new AstContainerDataFileGenerator());
    }

    public static void publishEventDataFileGenerator(ContainerContract container) {
        container.setSingleton(
                EventDataFileGeneratorContract.class, new AstEventDataFileGenerator());
    }

    public static void publishCliDataFileGenerator(ContainerContract container) {
        container.setSingleton(CliDataFileGeneratorContract.class, new AstCliDataFileGenerator());
    }

    public static void publishHttpDataFileGenerator(ContainerContract container) {
        container.setSingleton(HttpDataFileGeneratorContract.class, new AstHttpDataFileGenerator());
    }
}
