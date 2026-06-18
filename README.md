<p align="center"><a href="https://valkyrja.io" target="_blank">
    <img src="https://raw.githubusercontent.com/valkyrjaio/art/refs/heads/master/long-banner/orange/java.png" width="100%">
</a></p>

# Sindri

[Sindri][github sindri] is the code generator and application creator for the
[Valkyrja][Valkyrja url] Java framework.

Sindri generates the compiled data classes that let your application skip
discovery work at runtime — parsing configuration and source into container,
event, and routing data ahead of time. Named after the dwarven smith in Norse
mythology who forged Mjölnir and other divine artifacts, Sindri does for your
Valkyrja app what his namesake did for the gods: crafts the tools and artifacts
that make it all work faster and better.

<p>
    <a href="https://central.sonatype.com/artifact/io.sindri/sindri"><img src="https://img.shields.io/maven-central/v/io.sindri/sindri?label=Maven%20Central" alt="Latest Stable Version"></a>
    <a href="https://github.com/valkyrjaio/sindri-java"><img src="https://img.shields.io/badge/Java-21--25-orange" alt="Java Version"></a>
    <a href="https://github.com/valkyrjaio/sindri-java/blob/26.x/LICENSE.md"><img src="https://img.shields.io/badge/license-MIT-blue" alt="License"></a>
    <a href="https://github.com/valkyrjaio/sindri-java/actions/workflows/ci.yml?query=branch%3A26.x"><img src="https://github.com/valkyrjaio/sindri-java/actions/workflows/ci.yml/badge.svg?branch=26.x" alt="CI Status"></a>
    <a href="https://coveralls.io/github/valkyrjaio/sindri-java?branch=26.x"><img src="https://coveralls.io/repos/github/valkyrjaio/sindri-java/badge.svg?branch=26.x" alt="Coverage Status"></a>
    <a href="https://sonarcloud.io/summary/new_code?id=valkyrjaio_sindri-java"><img src="https://sonarcloud.io/api/project_badges/measure?project=valkyrjaio_sindri-java&metric=sqale_rating" alt="Maintainability Rating"></a>
</p>

What Sindri Does
----------------

- **Generates compiled data classes** — parses configuration and source into
  container, event, and routing data so your app skips discovery at runtime
- **Reads source via AST** — analyzes your application's classes and attributes
  to produce accurate, type-safe data files
- **Builds artifacts** — prepares deployable outputs optimized for production
  runtimes
- **Handles upgrades** — assists with migrations between major Valkyrja
  versions

Installation
------------

### Via Gradle / Maven

Add Sindri to an existing Valkyrja project's build:

```kotlin
// Gradle (Kotlin DSL)
implementation("io.sindri:sindri:26.0.0")
```

```xml
<!-- Maven -->
<dependency>
    <groupId>io.sindri</groupId>
    <artifactId>sindri</artifactId>
    <version>26.0.0</version>
</dependency>
```

### Runnable Jar

Build the standalone jar and run it directly:

```
./gradlew build
java -jar build/libs/sindri.jar list
```

Getting Started
---------------

### Generating Data Files

Sindri's primary job is generating the compiled data classes Valkyrja loads at
boot — container, event, and routing data derived from your configuration:

```
java -jar build/libs/sindri.jar generate
```

`generate` is the default command. It reads your application's configuration
and source, then writes the corresponding data classes.

### Listing Available Commands

```
java -jar build/libs/sindri.jar list
```

See the [Sindri documentation][docs url] for the full list of generation
commands and options.

Documentation
-------------

Full Valkyrja [documentation][docs url] is baked into the framework
repository so you can browse it offline.

For framework-level questions about Valkyrja itself, see the
[Valkyrja framework repository][framework url].

Versioning and Release Process
------------------------------

Sindri follows [semantic versioning][semantic versioning url] with a major
release every year, and support for each major version for 2 years from the
date of release.

For more information see our
[Versioning and Release Process documentation][Versioning and Release Process url].

### Supported Versions

Bug fixes are provided until 3 months after the next major release. Security
fixes are provided for 2 years after the initial release.

| Version | Java    | Release        | Bug Fixes Until | Security Fixes Until |
|:--------|:--------|:---------------|:----------------|:---------------------|
| 26      | 21 – 25 | March 31, 2026 | Q2 2027         | Q1 2028              |
| 27      | 23 – 25 | Q1 2027        | Q2 2028         | Q1 2029              |
| 28      | 25+     | Q1 2028        | Q2 2029         | Q1 2030              |

Contributing
------------

Sindri is an open-source, community-driven project. Thank you for your
interest in helping develop, maintain, and release it.

See [`CONTRIBUTING.md`][contributing url] for the submission process and
[`VOCABULARY.md`][vocabulary url] for the terminology used across Valkyrja.

Security Issues
---------------

If you discover a security vulnerability within Sindri, please follow our
[disclosure procedure][security vulnerabilities url].

License
-------

Sindri is open-source software licensed under the
[MIT license][MIT license url]. See [`LICENSE.md`](./LICENSE.md).

[Valkyrja url]: https://valkyrja.io

[framework url]: https://github.com/valkyrjaio/valkyrja-java

[github sindri]: https://github.com/valkyrjaio/sindri-java

[docs url]: https://github.com/valkyrjaio/valkyrja-java/blob/26.x/src/main/java/io/valkyrja/README.md

[Versioning and Release Process url]: https://github.com/valkyrjaio/valkyrja-java/blob/26.x/src/main/java/io/valkyrja/VERSIONING_AND_RELEASE_PROCESS.md

[contributing url]: https://github.com/valkyrjaio/.github/blob/master/CONTRIBUTING.md

[vocabulary url]: https://github.com/valkyrjaio/.github/blob/master/VOCABULARY.md

[security vulnerabilities url]: https://github.com/valkyrjaio/.github/blob/master/SECURITY.md

[semantic versioning url]: https://semver.org/

[MIT license url]: https://opensource.org/licenses/MIT

[license url]: ./LICENSE.md
