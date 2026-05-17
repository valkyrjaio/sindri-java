/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generate.abstract_;

import io.sindri.ast.CliRouteAttributeReader;
import io.sindri.ast.ComponentProviderReader;
import io.sindri.ast.ConfigReader;
import io.sindri.ast.HttpRouteAttributeReader;
import io.sindri.ast.ListenerProviderReader;
import io.sindri.ast.RouteProviderReader;
import io.sindri.ast.ServiceProviderReader;
import io.sindri.ast.data.HttpRouteData;
import io.sindri.ast.data.result.CliRouteAttributeResult;
import io.sindri.ast.data.result.ComponentProviderResult;
import io.sindri.ast.data.result.ConfigResult;
import io.sindri.ast.data.result.HttpRouteAttributeResult;
import io.sindri.ast.data.result.RouteProviderResult;
import io.sindri.generator.cli.contract.CliDataFileGeneratorContract;
import io.sindri.generator.container.contract.ContainerDataFileGeneratorContract;
import io.sindri.generator.event.contract.EventDataFileGeneratorContract;
import io.sindri.generator.http.contract.HttpDataFileGeneratorContract;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class GenerateDataFromAst {

    private final ConfigReader configReader = new ConfigReader();
    private final ComponentProviderReader componentProviderReader = new ComponentProviderReader();
    private final ServiceProviderReader serviceProviderReader = new ServiceProviderReader();
    private final RouteProviderReader routeProviderReader = new RouteProviderReader();
    private final ListenerProviderReader listenerProviderReader = new ListenerProviderReader();
    private final CliRouteAttributeReader cliRouteAttributeReader = new CliRouteAttributeReader();
    private final HttpRouteAttributeReader httpRouteAttributeReader =
            new HttpRouteAttributeReader();

    protected abstract ContainerDataFileGeneratorContract getContainerDataFileGenerator();

    protected abstract EventDataFileGeneratorContract getEventDataFileGenerator();

    protected abstract CliDataFileGeneratorContract getCliDataFileGenerator();

    protected abstract HttpDataFileGeneratorContract getHttpDataFileGenerator();

    public void run(String configFilePath) {
        ConfigResult config = configReader.readFile(configFilePath);

        ComponentProviderResult allProviderData = collectProviderData(config);

        Map<String, String[]> publishers =
                collectPublishers(allProviderData.serviceProviders(), config);

        Map<String, String> listeners =
                collectListeners(allProviderData.listenerProviders(), config);

        Map<String, String> cliRoutes =
                collectCliRoutes(allProviderData.cliRouteProviders(), config);

        Map<String, String> httpRoutes = new LinkedHashMap<>();
        Map<String, HttpRouteData> httpRouteData = new LinkedHashMap<>();
        collectHttpRoutes(allProviderData.httpRouteProviders(), config, httpRoutes, httpRouteData);

        String dataClassName = "AppContainerData";
        String dataDir = config.dataPath();
        String dataNamespace = config.dataNamespace();

        getContainerDataFileGenerator()
                .generateFile(dataDir, dataClassName, dataNamespace, publishers);
        getEventDataFileGenerator().generateFile(dataDir, "AppEventData", dataNamespace, listeners);
        getCliDataFileGenerator()
                .generateFile(dataDir, "AppCliRoutingData", dataNamespace, cliRoutes);
        getHttpDataFileGenerator()
                .generateFile(
                        dataDir, "AppHttpRoutingData", dataNamespace, httpRoutes, httpRouteData);
    }

    private ComponentProviderResult collectProviderData(ConfigResult config) {
        ComponentProviderResult combined = new ComponentProviderResult();

        for (String providerFqn : config.providers()) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            ComponentProviderResult data = componentProviderReader.readFile(filePath);
            combined = combined.merge(data);

            for (String nestedFqn : data.componentProviders()) {
                String nestedPath = fqnToFilePath(nestedFqn, config.namespace(), config.dir());
                if (!nestedPath.isEmpty()) {
                    ComponentProviderResult nestedData =
                            componentProviderReader.readFile(nestedPath);
                    combined = combined.merge(nestedData);
                }
            }
        }

        return combined;
    }

    private Map<String, String[]> collectPublishers(
            java.util.List<String> serviceProviders, ConfigResult config) {
        Map<String, String[]> publishers = new LinkedHashMap<>();
        for (String providerFqn : serviceProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            publishers.putAll(serviceProviderReader.readFile(filePath).publishers());
        }
        return publishers;
    }

    private Map<String, String> collectListeners(
            java.util.List<String> listenerProviders, ConfigResult config) {
        Map<String, String> listeners = new LinkedHashMap<>();
        for (String providerFqn : listenerProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            listenerProviderReader
                    .readFile(filePath)
                    .listeners()
                    .forEach(expr -> listeners.put(expr.toString(), expr.toString()));
        }
        return listeners;
    }

    private Map<String, String> collectCliRoutes(
            java.util.List<String> cliRouteProviders, ConfigResult config) {
        Map<String, String> routes = new LinkedHashMap<>();
        for (String providerFqn : cliRouteProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            RouteProviderResult routeProvider = routeProviderReader.readFile(filePath);
            for (String controllerFqn : routeProvider.controllerClasses()) {
                String controllerPath =
                        fqnToFilePath(controllerFqn, config.namespace(), config.dir());
                if (!controllerPath.isEmpty()) {
                    CliRouteAttributeResult result =
                            cliRouteAttributeReader.readFile(controllerPath);
                    result.routes().forEach((name, expr) -> routes.put(name, expr.toString()));
                }
            }
        }
        return routes;
    }

    private void collectHttpRoutes(
            java.util.List<String> httpRouteProviders,
            ConfigResult config,
            Map<String, String> httpRoutes,
            Map<String, HttpRouteData> httpRouteData) {
        for (String providerFqn : httpRouteProviders) {
            String filePath = fqnToFilePath(providerFqn, config.namespace(), config.dir());
            if (filePath.isEmpty()) {
                continue;
            }
            RouteProviderResult routeProvider = routeProviderReader.readFile(filePath);
            for (String controllerFqn : routeProvider.controllerClasses()) {
                String controllerPath =
                        fqnToFilePath(controllerFqn, config.namespace(), config.dir());
                if (!controllerPath.isEmpty()) {
                    HttpRouteAttributeResult result =
                            httpRouteAttributeReader.readFile(controllerPath);
                    result.routes().forEach((name, expr) -> httpRoutes.put(name, expr.toString()));
                    httpRouteData.putAll(result.routeData());
                }
            }
        }
    }

    private String fqnToFilePath(String fqn, String namespace, String srcDir) {
        String namespacePkg = namespace.toLowerCase(java.util.Locale.ROOT);
        if (fqn.startsWith(namespacePkg + ".")) {
            String relative = fqn.substring(namespacePkg.length() + 1).replace('.', '/');
            return srcDir + "/" + relative + ".java";
        }
        return "";
    }
}
