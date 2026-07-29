/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.tests.unit.ast;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sindri.ast.*;
import io.sindri.ast.data.result.ServiceProviderResult;
import org.junit.jupiter.api.Test;

public final class ServiceProviderReaderTest {

    private final ServiceProviderReader reader = new ServiceProviderReader();

    private String fixturePath(String relative) {
        return getClass().getClassLoader().getResource("Fixtures/" + relative).getPath();
    }

    @Test
    void readFile_parsesPublishers() {
        ServiceProviderResult result =
                reader.readFile(fixturePath("Container/Provider/TestServiceProviderFixture.java"));

        assertEquals(1, result.publishers().size());
        assertTrue(
                result.publishers().containsKey("io.sindri.tests.fixtures.container.TestService"));
    }

    @Test
    void readFile_parsesProviderAndMethod() {
        ServiceProviderResult result =
                reader.readFile(fixturePath("Container/Provider/TestServiceProviderFixture.java"));

        String[] ref = result.publishers().get("io.sindri.tests.fixtures.container.TestService");
        assertArrayEquals(
                new String[] {
                    "io.sindri.tests.fixtures.container.provider.TestServiceProviderFixture",
                    "publishTestService"
                },
                ref);
    }

    @Test
    void readFile_noPublishersMethod_returnsEmpty() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestEmptyServiceProviderFixture.java"));

        assertTrue(result.publishers().isEmpty());
        assertTrue(result.serviceClasses().isEmpty());
    }

    @Test
    void readFile_publishersThrows_returnsEmpty() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestNoReturnServiceProviderFixture.java"));

        assertTrue(result.publishers().isEmpty());
        assertTrue(result.serviceClasses().isEmpty());
    }

    @Test
    void readFile_nonCallReturnExpr_returnsEmpty() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath(
                                "Container/Provider/TestFieldPublishersServiceProviderFixture.java"));

        assertTrue(result.publishers().isEmpty());
        assertTrue(result.serviceClasses().isEmpty());
    }

    @Test
    void readFile_mixedEntries_skipsInvalidKeyAndNonMethodRef() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath(
                                "Container/Provider/TestMixedPublishersServiceProviderFixture.java"));

        assertTrue(result.publishers().isEmpty());
    }

    @Test
    void readFile_mapOfEntriesPublishers_parsesEntries() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestOfEntriesServiceProviderFixture.java"));

        assertEquals(1, result.publishers().size());
        assertTrue(
                result.publishers().containsKey("io.sindri.tests.fixtures.container.TestService"));
        String[] ref = result.publishers().get("io.sindri.tests.fixtures.container.TestService");
        assertArrayEquals(
                new String[] {
                    "io.sindri.tests.fixtures.container.provider.TestOfEntriesServiceProviderFixture",
                    "publish"
                },
                ref);
    }

    @Test
    void readFile_badOfEntriesEntries_skipsInvalidArgs() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath(
                                "Container/Provider/TestBadOfEntriesServiceProviderFixture.java"));

        assertTrue(result.publishers().isEmpty());
    }

    @Test
    void readFile_mapOfEntries_isParsed() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestOfEntriesServiceProviderFixture.java"));

        assertEquals(1, result.publishers().size());
    }

    @Test
    void readFile_neitherMapOfNorOfEntries_returnsEmpty() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestCopyOfServiceProviderFixture.java"));

        assertTrue(result.publishers().isEmpty());
    }

    @Test
    void readFile_mapCallOtherThanOfOrOfEntries_returnsEmpty() {
        ServiceProviderResult result =
                reader.readFile(
                        fixturePath("Container/Provider/TestMapOtherServiceProviderFixture.java"));

        assertTrue(result.publishers().isEmpty());
    }

    @Test
    void readFile_noTypeDeclaration_throws() {
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        reader.readFile(
                                fixturePath(
                                        "Container/Provider/TestNoTypeServiceProviderFileFixture.java")));
    }
}
