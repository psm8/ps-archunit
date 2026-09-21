# Adversarial Review -- Concerns Summary

**Scope:** Local uncommitted CLI/module changes, focused on design decisions
and alternatives.

**Intent:** Add a standalone shaded YAML-driven architecture verifier while
preserving the reusable Java API, analyzing compiled bytecode only, and
providing CI-friendly reports and exit codes.

| # | Severity | Category | Concern | Reference | Status |
|---|---|---|---|---|---|
| 1 | Notable | Design Decisions | Consumer CI snippets originally built a module that exists only in this repository, so they were not runnable from an ordinary consumer checkout. | `README.md`, Architecture verification CLI | Addressed in working tree |
| 2 | Notable | Design Decisions | `--classpath` is treated as another imported architecture subject rather than a resolution-only classpath. Dependency classes can therefore be checked and appear in reports. | `ps-archunit-cli/src/main/java/io/github/psm8/archunit/cli/Main.java`, `Main.validateInputs` | Accepted by user; documented and tested |
| 3 | Minor | Design Decisions | The root artifact changes from the library artifact to a parent POM. The core coordinates remain intact, but release/source/javadoc tooling targeting the root project needs multi-module handling. | `pom.xml`, `ps-archunit/pom.xml` | Accepted by user |

### Critical Concerns

None identified.

### Notable Concerns

1. Consumer workflows now download the shaded `all` classifier using Maven
   Dependency Plugin commands. The repository-local build command remains
   documented separately for contributors.
2. The user selected analyzed-input semantics for `--classpath`. The README
   and ADR now state that `--classes`, `--classpath`, and
   `--classpath-file` entries are all imported and checked. A focused CLI test
   verifies that repeated classpath entries remain in the effective report
   input.

### What Looks Sound

- Keeping SnakeYAML and the external schema in the CLI preserves the core
  library's framework-free Java API.
- Using compiled bytecode and never starting consumer code matches the safety
  boundary.
- Explicit exit codes and the shaded executable provide a useful CI
  integration point.
- The moved core sources match the tracked originals byte-for-byte.
- Packaged pass/fail smoke tests cover the basic CLI contract.
