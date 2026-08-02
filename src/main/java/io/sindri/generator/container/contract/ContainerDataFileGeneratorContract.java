/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

package io.sindri.generator.container.contract;

import io.sindri.generator.enum_.GenerateStatus;
import java.util.Map;

public interface ContainerDataFileGeneratorContract {

    GenerateStatus generateFile(
            String directory, String className, String namespace, Map<String, String[]> publishers);

    String generateClassContents(Map<String, String[]> publishers);
}
