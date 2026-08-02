/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

plugins {
    id("com.diffplug.spotless") version "8.9.0"
    id("com.github.ben-manes.versions") version "0.58.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
}

group = "io.sindri"
version = "26.0.0"

repositories {
    mavenCentral()
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf { isNonStable(candidate.version) }
}

spotless {
    java {
        // The JUnit and ArchUnit builds hold the repo's other Java source trees; format them too.
        // Scoped to `src/test/java` on purpose — `src/test/resources` holds .java files that are
        // AST *input samples*, parsed as text by the readers under test. Formatting them (or
        // injecting a license header) rewrites the very source those tests assert on.
        target(
            "src/**/*.java",
            ".github/ci/junit/src/test/java/**/*.java",
            ".github/ci/archunit/src/test/java/**/*.java",
        )
        googleJavaFormat("1.27.0").aosp()
        licenseHeader(
            """/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

"""
        )
    }
}
