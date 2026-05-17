/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.generator.event.contract;

import io.sindri.generator.enum_.GenerateStatus;
import java.util.Map;

public interface EventDataFileGeneratorContract {

    GenerateStatus generateFile(
            String directory, String className, String namespace, Map<String, String> listeners);

    String generateClassContents(Map<String, String> listeners);
}
