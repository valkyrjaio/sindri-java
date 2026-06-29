/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.unit.ast;
import io.sindri.ast.*;

import io.sindri.ast.data.result.ServiceProviderResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceProviderReaderTest {

    private final ServiceProviderReader reader = new ServiceProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesPublishers() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestServiceProviderClass.java"));

        assertEquals(1, result.publishers().size());
        assertTrue(result.publishers().containsKey("io.sindri.tests.fixtures.container.TestService"));
    }

    @Test
    void readFile_parsesProviderAndMethod() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestServiceProviderClass.java"));

        String[] ref = result.publishers().get("io.sindri.tests.fixtures.container.TestService");
        assertArrayEquals(
                new String[]{"io.sindri.tests.fixtures.container.provider.TestServiceProviderClass", "publishTestService"},
                ref);
    }

    @Test
    void readFile_noPublishersMethod_returnsEmpty() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestEmptyServiceProviderClass.java"));

        assertTrue(result.publishers().isEmpty());
        assertTrue(result.serviceClasses().isEmpty());
    }

    @Test
    void readFile_publishersThrows_returnsEmpty() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestNoReturnServiceProviderClass.java"));

        assertTrue(result.publishers().isEmpty());
        assertTrue(result.serviceClasses().isEmpty());
    }

    @Test
    void readFile_nonCallReturnExpr_returnsEmpty() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestFieldPublishersServiceProviderClass.java"));

        assertTrue(result.publishers().isEmpty());
        assertTrue(result.serviceClasses().isEmpty());
    }

    @Test
    void readFile_mixedEntries_skipsInvalidKeyAndNonMethodRef() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestMixedPublishersServiceProviderClass.java"));

        assertTrue(result.publishers().isEmpty());
    }

    @Test
    void readFile_mapOfEntriesPublishers_parsesEntries() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestOfEntriesServiceProviderClass.java"));

        assertEquals(1, result.publishers().size());
        assertTrue(result.publishers().containsKey("io.sindri.tests.fixtures.container.TestService"));
        String[] ref = result.publishers().get("io.sindri.tests.fixtures.container.TestService");
        assertArrayEquals(
                new String[]{"io.sindri.tests.fixtures.container.provider.TestOfEntriesServiceProviderClass", "publish"},
                ref);
    }

    @Test
    void readFile_badOfEntriesEntries_skipsInvalidArgs() {
        ServiceProviderResult result = reader.readFile(fixturePath("Container/Provider/TestBadOfEntriesServiceProviderClass.java"));

        assertTrue(result.publishers().isEmpty());
    }

    @Test
    void readFile_mapOfEntries_isParsed() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestOfEntriesServiceProviderClass.java"));

        assertEquals(1, result.publishers().size());
    }

    @Test
    void readFile_neitherMapOfNorOfEntries_returnsEmpty() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestCopyOfServiceProviderClass.java"));

        assertTrue(result.publishers().isEmpty());
    }


    @Test
    void readFile_mapCallOtherThanOfOrOfEntries_returnsEmpty() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestMapOtherServiceProviderClass.java"));

        assertTrue(result.publishers().isEmpty());
    }

}
