# Architecture verification runner

**Status:** Accepted

## Context

The library's public API returns ArchUnit `ArchRule` objects. Consumers
currently need to write a JUnit test, import their classes, configure a layout
through Java builders, and rely on the test process exit status. That makes the
architecture policy harder to run from GitHub Actions, GitLab CI, or a local
shell without adding consumer test code.

The repository must support all existing layout configuration without coupling
the core library to a CI platform, Spring, a YAML parser, or application
startup. The runner also needs a stable failure contract for automation and a
version that can be inspected independently of the consumer project.

## Decision

Add a separate `ps-archunit-cli` Maven module in the same repository.

The CLI:

- is distributed as an executable shaded JAR;
- uses `java -jar` and supports `--version`;
- consumes compiled consumer bytecode, not source code;
- accepts class directories and JARs plus dependency classpath entries, with
  every effective `--classes`, `--classpath`, and `--classpath-file` entry
  imported and checked as an architecture subject;
- never starts consumer application code, Spring, tests, databases, or network
  clients;
- requires an external, versioned verification configuration;
- maps every current public layout-builder option, including nested dependency
  bans, scoped exceptions, configuration visibility policy, transaction policy,
  port and adapter settings, and replacement/append semantics;
- supports explicit `baseline`, `domainOriented`, and `hexagonal` tiers;
- supports strict and lax Hexagonal adapter defaults through configuration;
- rejects unknown configuration keys and invalid combinations before rule
  evaluation;
- emits human-readable text by default and structured JSON on request;
- returns exit code `0` for a passing architecture, `1` for architecture
  violations, and `2` for configuration, input, or runner failures;
- treats empty effective class input as a runner failure rather than a pass.

Configuration uses a versioned nested schema with explicit `replace` and
`append` sections. The CLI owns YAML parsing and validation. The core
`ps-archunit` artifact remains framework-free and keeps its Java API.

The project version moves from `0.1.0-SNAPSHOT` to `0.2.0-SNAPSHOT`. The CLI
and core use the same Maven version, while runtime version output is read from
artifact metadata rather than maintained as a second source of truth.

README documentation provides Maven artifact download commands and copy-paste
GitHub Actions and GitLab CI examples.
Publishing credentials, package-repository automation, and active workflows are
outside this decision.

## Alternatives considered

### Consumer-owned architecture tests

Rejected as the only integration path. The Java API remains available, but
requiring every consumer to write test glue prevents direct CI use and
duplicates classpath and reporting setup.

### Maven plugin

Deferred. A plugin could integrate tightly with Maven lifecycle phases, but
would not provide the same neutral entry point for Gradle, GitHub Actions,
GitLab CI, or shell use. The CLI can be wrapped by a plugin later.

### YAML support in the core library

Rejected. YAML parsing would add configuration and dependency weight to the
reusable rule artifact. The CLI is the boundary that needs external
configuration.

## Consequences

Consumers must compile their code before architecture verification and provide
the resulting class paths. They no longer need to create architecture unit
tests for CI execution.

The CLI introduces a second artifact and a schema that must evolve
compatibly. Strict validation makes configuration errors visible early but
requires schema updates when new layout options are added.

The existing Java builder API remains the source vocabulary for architecture
rules. The CLI is an adapter from external verification configuration to those
builders, so rule behavior is not duplicated.
