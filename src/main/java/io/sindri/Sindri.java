/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri;

import io.sindri.provider.SindriComponentProvider;
import io.valkyrja.application.data.CliConfig;
import io.valkyrja.application.entry.Cli;
import java.util.List;

public class Sindri extends Cli {

    public static void main(String[] args) {
        run(
                new CliConfig(
                        "Sindri",
                        System.getProperty("user.dir"),
                        "26.0.0",
                        "production",
                        true,
                        "UTC",
                        "sindri_secret_key",
                        "io/sindri/provider/data",
                        "io.sindri.provider.data",
                        "sindri",
                        "generate",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new SindriComponentProvider()),
                        List.of(SindriComponentProvider::publish)),
                args);
    }
}
