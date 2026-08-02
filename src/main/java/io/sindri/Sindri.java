/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri;

import io.sindri.constant.SindriInfo;
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
                        SindriInfo.VERSION,
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
