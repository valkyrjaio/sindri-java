plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "sindri"

// The framework is resolved from Maven Central (io.valkyrja:valkyrja). To build
// against a local framework checkout instead, opt in with -PlocalFramework
// (e.g. ./gradlew build -PlocalFramework, or set localFramework=true in your
// local gradle.properties). Off by default, so CI and publishes never composite.
if (providers.gradleProperty("localFramework").isPresent) {
    includeBuild("../valkyrja")
}
