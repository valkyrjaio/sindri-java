/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generator.abstract_;

import io.sindri.generator.contract.FileGeneratorContract;
import io.sindri.generator.enum_.GenerateStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public abstract class FileGenerator implements FileGeneratorContract {

    protected String filePath;

    public FileGenerator(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public GenerateStatus generateFile() {
        String contents = generateFileContents();
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(Objects.requireNonNullElse(path.getParent(), Paths.get(".")));
            String existing =
                    Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8) : null;
            if (contents.equals(existing)) {
                return GenerateStatus.SKIPPED;
            }
            Files.writeString(path, contents, StandardCharsets.UTF_8);
            return GenerateStatus.SUCCESS;
        } catch (IOException e) {
            return GenerateStatus.FAILURE;
        }
    }
}
