plugins {
    java
    application
}

group = "io.sindri"
version = "26.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.valkyrja:valkyrja:26.0.0")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.26.4")
    compileOnly("org.jspecify:jspecify:1.0.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("io.sindri.Sindri")
}

tasks.jar {
    archiveFileName.set("sindri.jar")
    manifest {
        attributes("Main-Class" to "io.sindri.Sindri")
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// CI tasks — run from the project root without cd-ing into each CI directory

tasks.register<GradleBuild>("spotlessCheck") {
    group = "CI"
    description = "Check code formatting via Spotless"
    dir = file(".github/ci/spotless")
    tasks = listOf("spotlessCheck")
}

tasks.register<GradleBuild>("spotlessApply") {
    group = "CI"
    description = "Apply code formatting via Spotless"
    dir = file(".github/ci/spotless")
    tasks = listOf("spotlessApply")
}

tasks.register<GradleBuild>("archunit") {
    group = "CI"
    description = "Run ArchUnit architecture tests"
    dir = file(".github/ci/archunit")
    tasks = listOf("test")
}

tasks.register<GradleBuild>("errorprone") {
    group = "CI"
    description = "Run Error Prone static analysis"
    dir = file(".github/ci/errorprone")
    tasks = listOf("build")
}

tasks.register<GradleBuild>("spotbugs") {
    group = "CI"
    description = "Run SpotBugs static analysis"
    dir = file(".github/ci/spotbugs")
    tasks = listOf("check")
}

tasks.register<GradleBuild>("junit") {
    group = "CI"
    description = "Run JUnit unit tests"
    dir = file(".github/ci/junit")
    tasks = listOf("test")
}

listOf("spotless", "archunit", "errorprone", "spotbugs", "junit").forEach { ci ->
    tasks.register<GradleBuild>("${ci}OutdatedCheck") {
        group = "CI"
        description = "Check $ci dependencies for available updates"
        dir = file(".github/ci/$ci")
        tasks = listOf("dependencyUpdates")
    }
}

tasks.register("outdatedCheck") {
    group = "CI"
    description = "Check all CI dependencies for available updates"
    dependsOn("spotlessOutdatedCheck", "archunitOutdatedCheck", "errorproneOutdatedCheck", "spotbugsOutdatedCheck", "junitOutdatedCheck")
}

tasks.register("ci") {
    group = "CI"
    description = "Run all CI checks"
    dependsOn("spotlessCheck", "archunit", "errorprone", "spotbugs", "junit")
}
