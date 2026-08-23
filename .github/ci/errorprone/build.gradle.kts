/*
 * This file is part of the Sindri package.
 *
 * Copyright (c) 2016-present Melech Mizrachi
 *
 * Released under the MIT License. See LICENSE.md for details.
 */

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("net.ltgt.errorprone") version "5.1.0"
    id("com.github.ben-manes.versions") version "0.61.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.19"
}

group = "io.sindri"
version = "26.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

sourceSets {
    main {
        java {
            srcDirs("../../../src/main/java")
        }
    }
    // The JUnit build's tests are the repo's other Java source tree; analyze them too.
    test {
        java {
            srcDirs("../junit/src/test/java")
        }
    }
}

dependencies {
    implementation("io.valkyrja:valkyrja:26.9.10")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.28.2")
    compileOnly("org.jspecify:jspecify:1.0.1")
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    errorprone("com.uber.nullaway:nullaway:0.14.0")

    // Mirrors the JUnit build's test classpath — needed only so the tests compile here.
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("org.jspecify:jspecify:1.0.1")
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

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.errorprone {
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:AnnotatedPackages", "io.sindri")
    }
}

// Compiling the tests is the point — running them is the JUnit build's job, so `build` compiles
// the test sources (Error Prone runs as part of that) without executing the suite twice.
tasks.test {
    enabled = false
}

tasks.named("check") {
    dependsOn(tasks.compileTestJava)
}

// NullAway enforces a nullness contract on the tool's own API. Tests deliberately break it to reach
// the defensive guards those methods exist to provide, and the canonical guide requires synthetic
// inputs to cover guards normal input cannot. Enforcing it here would mean deleting the tests that
// hold branch coverage at 100%, so it is scoped to `src`; every other Error Prone check still
// applies to the test tree.
tasks.compileTestJava {
    options.errorprone {
        check("NullAway", CheckSeverity.OFF)
    }
}
