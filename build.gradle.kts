import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    application
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.valkyrja"
// Sourced from VERSION.md so the release pipeline (which bumps VERSION.md) drives the
// version that gets published. The leading "v" is stripped for Maven compatibility.
version = file("VERSION.md").readText().trim().removePrefix("v")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.valkyrja:valkyrja:26.2.0")
    // Framework sources jar on the runtime classpath: Sindri resolves framework provider `.java`
    // from the classpath during generation (GenerateDataFromAst.resolveSourceFromClasspath) so
    // their publishers()/nested providers are scanned, the way PHP uses ReflectionClass::getFileName().
    runtimeOnly("io.valkyrja:valkyrja:26.2.0:sources")
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "sindri", version.toString())

    pom {
        name.set("Sindri")
        description.set("The Sindri Java Code Generator.")
        url.set("https://github.com/valkyrjaio/sindri-java")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("melechmizrachi")
                name.set("Melech Mizrachi")
                email.set("melechmizrachi@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/valkyrjaio/sindri-java.git")
            developerConnection.set("scm:git:ssh://github.com/valkyrjaio/sindri-java.git")
            url.set("https://github.com/valkyrjaio/sindri-java")
        }
    }
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
