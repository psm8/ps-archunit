# ps-archunit

Reusable ArchUnit rules for Java 21 applications using layered, domain-oriented,
or ports-and-adapters architecture.

## Architecture tiers

The public factories are cumulative:

| Factory          | Level | Protects                                                                                          |
|------------------|-------|---------------------------------------------------------------------------------------------------|
| `baseline`       | 1     | cycles, explicit dependency bans, configuration, beans, and boundary outputs                      |
| `domainOriented` | 2     | Level 1 plus domain/application/API/infrastructure direction and model-only framework annotations |
| `hexagonal`      | 3     | Level 2 plus onion direction, framework isolation, ports, and adapters                           |

Choose the lowest tier that protects the system's likely changes. A higher
tier includes every lower-tier rule.

```java
import static io.github.psm8.archunit.DomainOrientedArchitectureRules.domainOriented;

domainOriented("com.acme.orders").check(CLASSES);
```

`strictHexagonal(String)` and `laxHexagonal(String)` remain available on
`HexagonalArchitectureRules` as Level 3 convenience factories. New code can
use `hexagonal` with either a base package or a configured `HexagonalLayout`.

## Install

Artifact coordinates:

```xml
<dependency>
    <groupId>io.github.psm8</groupId>
    <artifactId>ps-archunit</artifactId>
    <version>0.2.1</version>
    <scope>test</scope>
</dependency>
```

The library returns ArchUnit `ArchRule` objects. ArchUnit is therefore a
compile-visible dependency of the library and is available to the consumer's
architecture tests.

The repository also publishes `ps-archunit-cli`. The CLI owns YAML parsing;
SnakeYAML is not a dependency of the core `ps-archunit` JAR.
CLI coordinates: `io.github.psm8:ps-archunit-cli:0.2.1`.

## Architecture verification CLI

Repository contributors can build the executable shaded artifact:

```shell
mvn -B -pl ps-archunit-cli -am package
```

Example `architecture.yml`:

```yaml
schemaVersion: 1
tier: hexagonal
basePackage: com.acme.orders
hexagonal:
  adapterMode: strict
  replace:
    outputs: ["{base}.."]
    domain: ["{base}.domain.."]
    applicationPackages: ["{base}.application.."]
    apiPackages: ["{base}.api.."]
    infrastructurePackages: ["{base}.infrastructure.."]
    # configurationVisibility: packagePrivate
  append:
    dependencyDirectionIgnores:
      - source: "{base}.application.LegacyBridge"
        target: "{base}.api.LegacyController"
```

Run it against compiled bytecode:

```shell
java -jar ps-archunit-cli/target/ps-archunit-cli-0.2.1-all.jar \
  --config architecture.yml \
  --classes target/classes \
  --classpath dependency-a.jar \
  --classpath dependency-b.jar \
  --format text \
  --output architecture-report.txt
```

`--config` is required except with `--version`. `--classes` accepts directories
or JARs; it may be repeated and accepts comma-separated paths. Without it, the
runner uses existing `target/classes` and `target/test-classes`. Repeat
`--classpath` for additional JAR or directory inputs, or use
`--classpath-file` with one path per line. Every `--classes`, `--classpath`,
and `--classpath-file` entry is imported and checked as part of the effective
architecture input; these options are not resolution-only. `--format` defaults
to `text`; `json` emits stable
`version`, `tier`, `input`, `pass`, and `violations` fields. Reports go to
stdout unless `--output` names a file. `--version` reads Maven artifact
metadata.

The schema accepts `baseline`, `domainOriented`, and `hexagonal` tiers. Every
layout builder option is available under explicit `replace` and `append`
sections, including dependency-ban scoped exceptions and transaction policy.
`@Configuration` class visibility is unrestricted by default. Set
`replace.configurationVisibility` to the exact value `packagePrivate` to
enforce package-private configuration classes; `unrestricted` disables that
check. `@ConfigurationProperties` visibility remains package-private by
default and uses `publicConfigurationProperties` for exact exceptions.
Hexagonal `adapterMode` is `strict` or `lax`; lax mode clears directional
adapter defaults and uses mixed `adapter..` roots.

Exit codes:

- `0`: architecture passes;
- `1`: architecture violations;
- `2`: configuration, input, import, runtime, or reporting failure.

Consumer CI downloads the shaded artifact from Maven Central using the
coordinates and `all` classifier:

```shell
mkdir -p target/ps-archunit
mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy \
  -Dartifact=io.github.psm8:ps-archunit-cli:0.2.1:jar:all \
  -DoutputDirectory=target/ps-archunit
java -jar target/ps-archunit/ps-archunit-cli-0.2.1-all.jar \
  --config architecture.yml
```

GitHub Actions example (add to a consumer workflow):

```yaml
- name: Verify architecture
  run: |
    mkdir -p target/ps-archunit
    mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy \
      -Dartifact=io.github.psm8:ps-archunit-cli:0.2.1:jar:all \
      -DoutputDirectory=target/ps-archunit
    java -jar target/ps-archunit/ps-archunit-cli-0.2.1-all.jar \
      --config architecture.yml
```

GitLab CI example:

```yaml
architecture:
  image: maven:3.9.9-eclipse-temurin-21
  script:
    - mkdir -p target/ps-archunit
    - mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy -Dartifact=io.github.psm8:ps-archunit-cli:0.2.1:jar:all -DoutputDirectory=target/ps-archunit
    - java -jar target/ps-archunit/ps-archunit-cli-0.2.1-all.jar --config architecture.yml
```

The snippets are consumer-side examples. Release publishing is handled by
`.github/workflows/publish-release.yml` when a GitHub Release is published
with a `v0.2.1` tag.

## Consumer package contract

Replace `com.acme.orders` with the consumer's root package:

```text
com.acme.orders.domain..
com.acme.orders.application..
com.acme.orders.api..
com.acme.orders.infrastructure..
com.acme.orders.application.port.in..
com.acme.orders.application.port.out..
com.acme.orders.adapter.in..
com.acme.orders.adapter.out..
```

The default selectors also include one-feature vertical variants:

```text
com.acme.orders.*.domain..
com.acme.orders.*.application..
com.acme.orders.*.api..
com.acme.orders.*.infrastructure..
com.acme.orders.*.application.port.in..
com.acme.orders.*.application.port.out..
com.acme.orders.*.adapter.in..
com.acme.orders.*.adapter.out..
```

This supports layouts such as
`com.acme.orders.messaging.domain` and
`com.acme.orders.messaging.adapter.out`. The `*` matcher represents exactly
one package segment; `..` matches descendants below that point.

`basePackage` must be a concrete Java package name. It cannot contain
wildcards or a trailing dot. Configured package patterns support the `{base}`
macro. It is replaced exactly with the configured base package when the layout
is built, so `{base}.*.application..` resolves to
`com.acme.orders.*.application..` for the base package above.

Level 2 declares API and infrastructure groups independently. Level 3
effectively treats inbound adapters as API code and outbound or mixed adapters
as infrastructure, and treats inbound and outbound ports as application/core.
Configuration is also infrastructure, but configuration rules find
`@Configuration`, `@ConfigurationProperties`, and `@Bean` declarations by
annotation rather than by a configuration package selector. `@Configuration`
visibility is unrestricted by default and can opt into package-private
enforcement; `@ConfigurationProperties` remains package-private by default.
API and inbound adapter code may depend on application and domain types, but
not on infrastructure types. This permits translation adapters to normalize
external representations without coupling delivery code to technical
mechanisms.

Bean exposure uses each `@Bean` method's declared return type. Concrete
application-owned types, external interfaces, and interfaces annotated with
`@FunctionalInterface` pass. Other application-owned interfaces fail unless
their exact fully qualified name is configured with
`allowedApplicationInterfaceBeanTypes(...)`. Selector values reject null, blank,
simple, wildcard, and package-pattern forms.

## Configured layouts

Each tier has its own immutable cumulative snapshot and mutable builder:

```java
BaselineLayout baseline = BaselineLayout.builder("com.acme.orders")
    .outputs("com.acme.orders.api..")
    .build();

DomainOrientedLayout domain = DomainOrientedLayout.builder(baseline)
    .applicationPackages("com.acme.orders.orders..") // replaces default
    .addApplicationPackages("com.acme.orders.shared..")
    .apiPackages("com.acme.orders.http..")
    .infrastructurePackages("com.acme.orders.persistence..")
    .domainModelFrameworkPackages(
        "jakarta.persistence..",
        "jakarta.validation..",
        "com.fasterxml.jackson.annotation..")
    .transactionAnnotation(
        "org.springframework.transaction.annotation.Transactional")
    .transactionPackages("com.acme.orders.application..")
    .dependencyBans(BaselineLayout.DependencyBan.of(
        "com.acme.orders.application..",
        "com.acme.orders.legacy.."))
    .build();

HexagonalLayout layout = HexagonalLayout.builder(domain)
    .inboundAdapterPackages("com.acme.orders.http.adapter..")
    .outboundAdapterPackages("com.acme.orders.persistence.adapter..")
    .build();

HexagonalArchitectureRules.hexagonal(layout).check(CLASSES);
```

API package customization belongs to `DomainOrientedLayout`. A hexagonal layout
inherits that snapshot through promotion and uses it internally, but does not
expose `apiPackages(...)` methods on its public API.

Level 1 defaults:

- Outputs: `basePackage..`
- Cycle pattern: `basePackage.(**)`

Standalone Level 2 defaults:

- Domain: `basePackage.domain..`, `basePackage.*.domain..`
- Application: `basePackage.application..`, `basePackage.*.application..`
- API: `basePackage.api..`, `basePackage.*.api..`
- Infrastructure: `basePackage.infrastructure..`,
  `basePackage.*.infrastructure..`
- Model framework namespaces: JPA, Jakarta/Javax validation, and Jackson annotations

Standalone Level 3 adds:

- Inbound ports: `basePackage.application.port.in..`,
  `basePackage.*.application.port.in..`
- Outbound ports: `basePackage.application.port.out..`,
  `basePackage.*.application.port.out..`
- Inbound adapters: `basePackage.adapter.in..`,
  `basePackage.*.adapter.in..`
- Outbound adapters: `basePackage.adapter.out..`,
  `basePackage.*.adapter.out..`

The `laxHexagonal` convenience factory uses
`basePackage.adapter..` and `basePackage.*.adapter..` as mixed adapter roots.

Each configurable package group has replacement and append semantics. For
example, domain-oriented API configuration uses `apiPackages(...)` to replace
the group and `addApiPackages(...)` to append paths.
The same pattern applies to `infrastructurePackages(...)`,
`domainModelFrameworkPackages(...)`, application groups, adapter groups,
`domain(...)`, and `outputs(...)`. Builders accepting a lower-tier layout copy
that snapshot; `toBuilder()` starts an independent builder.

Level 3 lower-layer checks use effective groups internally: declared API plus
inbound adapters; declared infrastructure plus outbound and mixed adapters;
and declared application plus inbound and outbound ports as application/core.
The hexagonal layout exposes the inherited API boundary through rule behavior,
not through a repeated public API selector.

For Level 2 and Level 3, every imported class in `basePackage..` must belong
to at least one configured domain, application/core, API, or infrastructure
group. An empty group remains a no-op when no in-scope class uses it. Level 1
does not apply this completeness check. Group overlap is allowed, and imported
classes outside `basePackage..` are ignored by classification.

Model-framework allowances apply only to annotation types used as metadata on
domain classes. A runtime service, client, or other non-annotation type from
an allowlisted namespace is still rejected by `domainOriented` and
`hexagonal`.

Default framework isolation includes all `jakarta..` packages and explicit
legacy Java EE roots under `javax`, such as `javax.persistence..`,
`javax.validation..`, `javax.servlet..`, and `javax.transaction`. It does not
classify all `javax..` packages as frameworks because Java SE also provides
packages such as `javax.sql`, `javax.naming`, and `javax.lang.model`.

Transaction placement is an opt-in Level 2 policy. Configure both an exact
annotation type name with `transactionAnnotation(...)` and explicit package
patterns with `transactionPackages(...)`. The builder rejects partial
configuration. The rule checks declaration annotations on classes and methods
and only enforces placement; it does not infer transaction boundaries or
prove atomicity. Package patterns support the `{base}` macro.

Level 3 inherits the transaction policy from `DomainOrientedLayout`. When it
is configured, the exact transaction annotation is the only additional
framework annotation allowed on class or method declarations in the configured
packages for domain and application/core framework isolation. All other
framework dependencies remain violations.

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

BaselineLayout.DependencyBan ban = BaselineLayout.DependencyBan
    .of(
        List.of("com.acme.orders.feature..", "com.acme.orders.workflow.."),
        List.of("com.acme.orders.legacy.."))
    .ignoring(
        "com.acme.orders.feature.LegacyBridge",
        "com.acme.orders.legacy.LegacyType");

BaselineLayout layout = BaselineLayout.builder("com.acme.orders")
    .dependencyBans(ban)
    .build();
```

## Rules by tier

### Level 1: `baseline`

- package-slice cycle checks;
- explicit dependency bans;
- unrestricted `@Configuration` classes by default, with opt-in package-private
  enforcement;
- package-private internal `@ConfigurationProperties` classes;
- lite Spring configuration (`proxyBeanMethods = false`);
- `@Bean` placement and declared return-type exposure policy;
- record or sealed-interface boundary outputs.

### Level 2: `domainOriented`

- domain does not depend on application, API, or infrastructure;
- application does not depend on API or infrastructure;
- API may depend on application and domain, but not infrastructure;
- infrastructure does not depend on API;
- infrastructure may depend on domain and application;
- domain may use configured model annotations, but not runtime framework types.
- optionally, the configured transaction annotation may be used only on
  classes and methods in the configured transaction packages.
- every class under the base package belongs to at least one configured
  architecture group.

### Level 3: `hexagonal`

- all Level 1 and Level 2 rules;
- onion dependency direction;
- framework isolation for domain and application code;
- inherited opt-in transaction placement from `DomainOrientedLayout`;
- direct or recursively meta-annotated `@Configuration`,
  `@SpringBootConfiguration`, and `@SpringBootApplication` classes are
  recognized as composition roots;
- composition-root exemptions are source-local for classification completeness,
  domain direction, onion direction, package cycles, and framework assembly;
  referenced targets and all non-root classes remain checked;
- explicit dependency bans, bean placement and exposure, configuration
  visibility, `proxyBeanMethods`, transaction placement, and port/adapter rules
  remain active;
- framework-free public port signatures, including parameter declaration
  annotations. Type-use annotations are outside this check;
- `UseCase` and `Port` naming and visibility;
- outbound adapter wiring, visibility, and containment;
- the composition-root exception is not transitive. It applies to the
  recognized root source, not to arbitrary domain or application classes it
  calls.
- inbound and outbound ports are classified as application/core;
- every class under the base package belongs to at least one effective
  architecture group, including declared groups and adapter groups.

Component annotations are not package-layout selectors and do not trigger a
separate selector rule.

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
metadata. Release profile `release` signs every artifact with GPG and publishes
through the Sonatype Central Portal.

The current release is `0.2.1`. Publish it by creating a GitHub Release with
tag `v0.2.1`. The publishing workflow requires these repository secrets:

- `MAVEN_CENTRAL_USERNAME`: Central Portal user-token username;
- `MAVEN_CENTRAL_TOKEN`: Central Portal user-token password;
- `MAVEN_GPG_PRIVATE_KEY`: ASCII-armored private signing key;
- `MAVEN_GPG_PASSPHRASE`: signing-key passphrase.

Publish the corresponding public GPG key to a public keyserver before creating
the release.

The workflow validates that the release tag matches the Maven project version
before running `mvn -Prelease deploy`.
