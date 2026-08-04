/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

import io.valkyrja.spotless.CopyrightHeader

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("io.valkyrja:ci-spotless:26.1.2")
    }
}

plugins {
    id("com.diffplug.spotless") version "8.9.0"
    id("com.github.ben-manes.versions") version "0.59.0"
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
        licenseHeader(CopyrightHeader.block("Sindri"))
    }
}
