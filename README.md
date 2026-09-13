# ps-archunit

Reusable ArchUnit rules for Java 21 hexagonal architecture.

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
com.acme.orders.domain.model..
com.acme.orders.domain.service..
com.acme.orders.application.service..
com.acme.orders.application.port.in..
com.acme.orders.application.port.out..
com.acme.orders.adapter.in..
com.acme.orders.adapter.out..
```

`basePackage` must be a concrete Java package name. It cannot contain
wildcards or a trailing dot.

## Use in tests

The consumer owns class importing and test execution:

```java
package com.acme.orders.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static io.github.psm8.archunit.HexagonalArchitectureRules.standard;

class ArchitectureTest {
    private static final String BASE_PACKAGE = "com.acme.orders";
    private static final JavaClasses CLASSES =
            new ClassFileImporter().importPackages(BASE_PACKAGE);

    @Test
    void hexagonal_architecture_holds() {
        standard(BASE_PACKAGE).check(CLASSES);
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

## Profiles

### `minimal(basePackage)`

Uses ArchUnit's built-in `onionArchitecture()`:

- Domain models and domain services form the core.
- Application services and ports depend inward.
- Adapters may depend inward.
- Adapters cannot depend on other adapters.
- Empty layers are allowed.
- Layer matching is rooted in the required base package.

This is the default starting point. Built-in rules reduce custom behavior and
match ArchUnit's community-documented onion/hexagonal model.

### `standard(basePackage)`

Adds:

- Built-in cycle-free nested package slices.
- No `org.springframework..`, `jakarta..`, or `javax..` dependencies from
  domain or port packages.
- Port packages contain interfaces only.
- Inbound port interfaces end in `UseCase`.
- Outbound port interfaces end in `Port`.
- Outbound classes ending in `Adapter` implement an outbound port.

`adapter.out` can also contain mappers, configuration, and helper classes.
Only `*Adapter` classes are checked as outbound adapters.

## Why two profiles?

Architecture rules are guardrails. More rules catch more drift but can also
reject an existing project's intentional structure. `minimal()` gives the
community-supported dependency boundary first. `standard()` adds the
Spring/hexagonal conventions that require narrow custom checks.

The framework ban is intentionally standard-only. It catches framework leakage
in the core and ports, but the broad `org.springframework..`, `jakarta..`, and
`javax..` namespaces can be too strict for some applications.

The cycle rule checks cycles between ArchUnit's matched package slices. It does
not prove that classes assigned to one slice have no internal cycle.
Classes under the base package but outside the documented layer trees are
intentionally not assigned to a profile; use a separate containment rule if
your application requires every package to be classified.

## Rule grounding

| Rule | Basis |
| --- | --- |
| Inward dependency direction | `clean-ddd-hexagonal/SKILL.md`: dependency rule |
| Domain and port framework isolation | `clean-ddd-hexagonal/references/HEXAGONAL.md`: port contracts; `clean-ddd-hexagonal/SKILL.md`: domain has zero external dependencies |
| Driving `UseCase` and driven `Port` suffixes | `clean-ddd-hexagonal/references/HEXAGONAL.md`: driving/driven port roles; project convention: suffix naming |
| Adapter implementations | `clean-ddd-hexagonal/references/HEXAGONAL.md`: adapters implement port interfaces |
| Architecture tests and cycles | `clean-ddd-hexagonal/references/TESTING.md`: Architecture Tests |
| Small public surface | Modular design guidance: package-private by default and minimal exports |
| Public seam tests | `tdd/SKILL.md`: test confirmed public seams |

No ArchUnit-specific installed skill was found. Built-in behavior follows the
official ArchUnit library documentation and examples.

## Maven Central release

The POM contains Java 21, source/Javadoc, license, SCM, and Maven Central
metadata. The current development version is `0.1.0-SNAPSHOT`; change it to a
non-SNAPSHOT release before publishing. Release signing, Central Portal
credentials, namespace ownership, and upload remain deliberate release-operator
steps.
