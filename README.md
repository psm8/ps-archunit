# ps-archunit

Reusable ArchUnit rules for Java 21 applications using layered, domain-oriented,
or ports-and-adapters architecture.

## Architecture tiers

The public factories are cumulative:

| Factory          | Level | Protects                                                                                          |
|------------------|-------|---------------------------------------------------------------------------------------------------|
| `baseline`       | 1     | cycles, explicit dependency bans, configuration, beans, and boundary outputs                      |
| `domainOriented` | 2     | Level 1 plus domain/application/API/infrastructure direction and model-only framework annotations |
| `hexagonal`      | 3     | Level 2 plus onion direction, framework isolation, ports, adapters, and component-scan rules      |

Choose the lowest tier that protects the system's likely changes. A higher
tier includes every lower-tier rule.

```java
import static io.github.psm8.archunit.DomainOrientedArchitectureRules.domainOriented;

domainOriented("com.acme.orders").check(CLASSES);
```

`strictHexagonal(String)` and `laxHexagonal(String)` remain available on
`HexagonalArchitectureRules` as Level 3 convenience factories. New code can
use `hexagonal` with either a base package or a configured `PackageLayout`.

## Install

Artifact coordinates:

```xml
<dependency>
    <groupId>io.github.psm8</groupId>
    <artifactId>ps-archunit</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

The library returns ArchUnit `ArchRule` objects. ArchUnit is therefore a
compile-visible dependency of the library and is available to the consumer's
architecture tests.

## Consumer package contract

Replace `com.acme.orders` with the consumer's root package:

```text
com.acme.orders.domain..
com.acme.orders.application..
com.acme.orders.application.port.in..
com.acme.orders.application.port.out..
com.acme.orders.api..
com.acme.orders.infrastructure..
com.acme.orders.adapter.in..
com.acme.orders.adapter.out..
```

`basePackage` must be a concrete Java package name. It cannot contain
wildcards or a trailing dot.

The default vocabulary treats inbound adapters as API code and outbound or
mixed adapters as infrastructure. Configuration is also infrastructure, but
configuration rules find `@Configuration`, `@ConfigurationProperties`, and
`@Bean` declarations by annotation rather than by a configuration package
selector.

## Configured layouts

`PackageLayout` is an immutable snapshot. Build one with its mutable builder:

```java
PackageLayout layout = PackageLayout.builder("com.acme.orders")
    .applicationPackages("com.acme.orders.orders..") // replaces default
    .addApplicationPackages("com.acme.orders.shared..")
    .apiPackages("com.acme.orders.http..")
    .infrastructurePackages("com.acme.orders.persistence..")
    .domainModelFrameworkPackages(
        "jakarta.persistence..",
        "jakarta.validation..",
        "com.fasterxml.jackson.annotation..")
    .inboundAdapterPackages("com.acme.orders.http.adapter..")
    .outboundAdapterPackages("com.acme.orders.persistence.adapter..")
    .componentScanPackages("com.acme.orders.mapping..")
    .componentScanExceptions(
        "com.acme.orders.mapping.OrderMapper",
        "com.acme.orders.mapping.OrderMapping")
    .dependencyBans(PackageLayout.DependencyBan.of(
        "com.acme.orders.application..",
        "com.acme.orders.legacy.."))
    .build();

HexagonalArchitectureRules.hexagonal(layout).check(CLASSES);
```

Defaults:

- Domain: `basePackage.domain..`
- Application: `basePackage.application..`
- API: `basePackage.api..` and inbound adapter packages
- Infrastructure: `basePackage.infrastructure..`, outbound adapters, and mixed adapters
- Inbound ports: `basePackage.application.port.in..`
- Outbound ports: `basePackage.application.port.out..`
- Model framework namespaces: JPA, Jakarta/Javax validation, and Jackson annotations

Every package group has replacement and append semantics. For example,
`apiPackages(...)` replaces the group and `addApiPackages(...)` appends paths.
The same pattern applies to `infrastructurePackages(...)`,
`domainModelFrameworkPackages(...)`, application groups, adapter groups,
`domain(...)`, and `outputs(...)`. `toBuilder()` starts an independent builder
from an existing snapshot.

Model-framework allowances apply only to annotation types used as metadata on
domain classes. A runtime service, client, or other non-annotation type from
an allowlisted namespace is still rejected by `domainOriented` and
`hexagonal`.

Adapters are split into `inboundAdapterPackages(...)`,
`outboundAdapterPackages(...)`, and the direction-neutral third layer
`mixedAdapterPackages(...)`. Directional outbound adapters must implement an
outbound port. Mixed adapters preserve the lax compatibility behavior and do
not require an outbound port.

The library has no Spring, JPA, Validation, or Jackson runtime dependency.
Framework names are matched from imported ArchUnit classes.

Dependency bans accept arbitrary source groups and scoped exceptions:

```java
import java.util.List;

PackageLayout.DependencyBan ban = PackageLayout.DependencyBan
    .of(
        List.of("com.acme.orders.feature..", "com.acme.orders.workflow.."),
        List.of("com.acme.orders.legacy.."))
    .ignoring(
        "com.acme.orders.feature.LegacyBridge",
        "com.acme.orders.legacy.LegacyType");

PackageLayout layout = PackageLayout.builder("com.acme.orders")
    .dependencyBans(ban)
    .build();
```

## Rules by tier

### Level 1: `baseline`

- package-slice cycle checks;
- explicit dependency bans;
- package-private internal configuration and configuration-properties classes;
- lite Spring configuration (`proxyBeanMethods = false`);
- `@Bean` placement and concrete return types;
- record or sealed-interface boundary outputs.

### Level 2: `domainOriented`

- domain does not depend on application, API, or infrastructure;
- application does not depend on API or infrastructure;
- API does not depend on domain or infrastructure;
- infrastructure does not depend on API;
- infrastructure may depend on domain and application;
- domain may use configured model annotations, but not runtime framework types.

### Level 3: `hexagonal`

- all Level 1 and Level 2 rules;
- onion dependency direction;
- framework isolation for domain and application code;
- explicit `@Configuration` composition roots may assemble framework objects;
- framework-free public port signatures;
- `UseCase` and `Port` naming and visibility;
- outbound adapter wiring, visibility, and containment;
- component-scan restrictions.

The composition-root exception is not transitive. It applies to the explicit
configuration class, not to arbitrary domain or application classes it calls.

## Use in tests

The consumer owns class importing and test execution:

```java
package com.acme.orders.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static io.github.psm8.archunit.HexagonalArchitectureRules.hexagonal;

class ArchitectureTest {
    private static final String BASE_PACKAGE = "com.acme.orders";
    private static final JavaClasses CLASSES =
            new ClassFileImporter().importPackages(BASE_PACKAGE);

    @Test
    void hexagonal_architecture_holds() {
        hexagonal(BASE_PACKAGE).check(CLASSES);
    }
}
```

Run in GitLab CI with the project's normal Maven test command:

```yaml
architecture:
  image: maven:3.9.9-eclipse-temurin-21
  script:
    - mvn -B verify
```

No CI file is included in this library.

## Architecture vocabulary

The tier owners are `BaselineArchitectureRules`,
`DomainOrientedArchitectureRules`, and `HexagonalArchitectureRules`. See
[ARCHITECTURE-HIERARCHY.md](ARCHITECTURE-HIERARCHY.md) for choosing among the
three architecture levels, [CONTEXT.md](CONTEXT.md) for the shared glossary,
and [docs/adr/0001-cumulative-architecture-rule-tiers.md](docs/adr/0001-cumulative-architecture-rule-tiers.md)
for the public API decision.

## Maven Central release

The POM contains Java 21, source/Javadoc, license, SCM, and Maven Central
metadata. The current development version is `0.1.0-SNAPSHOT`; change it to a
non-SNAPSHOT release before publishing. Release signing, Central Portal
credentials, namespace ownership, and upload remain deliberate release-operator
steps.
