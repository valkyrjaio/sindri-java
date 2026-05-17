/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generator.abstract_;

import io.sindri.generator.enum_.GenerateStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AstFileGenerator {

    protected GenerateStatus writeFile(String directory, String className, String data) {
        String filePath = directory.replaceAll("/+$", "") + "/" + className + ".java";
        Path path = Paths.get(filePath);
        try {
            Path parent = path.getParent();
            Files.createDirectories(parent != null ? parent : Paths.get("."));
            String existing =
                    Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8) : null;
            if (data.equals(existing)) {
                return GenerateStatus.SKIPPED;
            }
            Files.writeString(path, data, StandardCharsets.UTF_8);
            return GenerateStatus.SUCCESS;
        } catch (IOException e) {
            return GenerateStatus.FAILURE;
        }
    }
}
