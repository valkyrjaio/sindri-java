/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.cli.command;

import io.sindri.generate.abstract_.GenerateDataFromAst;
import io.sindri.generator.ast.cli.AstCliDataFileGenerator;
import io.sindri.generator.ast.container.AstContainerDataFileGenerator;
import io.sindri.generator.ast.event.AstEventDataFileGenerator;
import io.sindri.generator.ast.http.AstHttpDataFileGenerator;
import io.sindri.generator.cli.contract.CliDataFileGeneratorContract;
import io.sindri.generator.container.contract.ContainerDataFileGeneratorContract;
import io.sindri.generator.event.contract.EventDataFileGeneratorContract;
import io.sindri.generator.http.contract.HttpDataFileGeneratorContract;
import io.valkyrja.cli.interaction.message.Message;
import io.valkyrja.cli.interaction.output.contract.OutputContract;
import io.valkyrja.cli.interaction.output.factory.contract.OutputFactoryContract;
import io.valkyrja.cli.routing.data.contract.RouteContract;
import io.valkyrja.container.manager.contract.ContainerContract;

public class GenerateDataFromConfigCommand extends GenerateDataFromAst {

    private final ContainerContract container;
    private final RouteContract route;

    public GenerateDataFromConfigCommand(ContainerContract container, RouteContract route) {
        this.container = container;
        this.route = route;
    }

    @Override
    protected ContainerDataFileGeneratorContract getContainerDataFileGenerator() {
        return new AstContainerDataFileGenerator();
    }

    @Override
    protected EventDataFileGeneratorContract getEventDataFileGenerator() {
        return new AstEventDataFileGenerator();
    }

    @Override
    protected CliDataFileGeneratorContract getCliDataFileGenerator() {
        return new AstCliDataFileGenerator();
    }

    @Override
    protected HttpDataFileGeneratorContract getHttpDataFileGenerator() {
        return new AstHttpDataFileGenerator();
    }

    public OutputContract execute() {
        String configPath = route.getArgument("config").getFirstValue();

        OutputFactoryContract outputFactory = container.getSingleton(OutputFactoryContract.class);
        OutputContract output = outputFactory.createOutput();

        output =
                output.withAddedMessages(
                                new Message("Generating Container Data......................"))
                        .writeMessages();

        try {
            run(configPath);
            output = output.withAddedMessages(new Message("Success\n")).writeMessages();
        } catch (Exception e) {
            output =
                    output.withAddedMessages(
                                    new Message("Failed\n"),
                                    new Message(
                                            e.getMessage() != null
                                                    ? e.getMessage()
                                                    : e.getClass().getName()))
                            .writeMessages();
        }

        return output;
    }
}
