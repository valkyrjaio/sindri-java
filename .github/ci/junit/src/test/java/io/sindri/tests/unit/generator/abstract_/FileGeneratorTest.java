/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.tests.unit.generator.abstract_;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sindri.generator.abstract_.FileGenerator;
import io.sindri.generator.enum_.GenerateStatus;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Test the {@link FileGenerator}. */
final class FileGeneratorTest {

    private static final class FixtureGenerator extends FileGenerator {
        private final String contents;

        FixtureGenerator(String filePath, String contents) {
            super(filePath);
            this.contents = contents;
        }

        @Override
        public String generateFileContents() {
            return contents;
        }
    }

    @Test
    void writesSkipsAndFailsAppropriately(@TempDir Path dir) {
        var target = dir.resolve("sub/Out.java");
        var generator = new FixtureGenerator(target.toString(), "contents");

        assertEquals(GenerateStatus.SUCCESS, generator.generateFile());
        // Re-running with identical contents is skipped.
        assertEquals(GenerateStatus.SKIPPED, generator.generateFile());

        // Writing where the path is a directory fails.
        assertEquals(
                GenerateStatus.FAILURE, new FixtureGenerator(dir.toString(), "x").generateFile());
    }
}
