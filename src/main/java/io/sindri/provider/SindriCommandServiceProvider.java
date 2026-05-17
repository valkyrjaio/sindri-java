/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.provider;

import io.sindri.cli.command.GenerateDataFromConfigCommand;
import io.valkyrja.container.manager.contract.ContainerContract;
import io.valkyrja.container.provider.contract.ServiceProviderContract;
import java.util.Map;
import java.util.function.Consumer;

public class SindriCommandServiceProvider implements ServiceProviderContract {

    public static void publishGenerateDataFromConfigCommand(ContainerContract container) {
        // Command is registered as a singleton for route attribute scanning
    }

    @Override
    public Map<Class<?>, Consumer<ContainerContract>> publishers() {
        return Map.of(
                GenerateDataFromConfigCommand.class,
                SindriCommandServiceProvider::publishGenerateDataFromConfigCommand);
    }
}
