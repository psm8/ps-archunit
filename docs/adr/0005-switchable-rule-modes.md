# Switchable rule modes and architecture advisories

**Status:** Accepted

## Context

Until now every rule either passed or failed verification. Some useful
constraints, such as the ban on setters, are matters of design judgement:
a team may want them enforced, reported as guidance, or not checked at all.
ArchUnit itself has no notion of severity.

## Decision

Selected rules are switchable. Each one has a rule mode: `FAIL`, `ADVISORY`
or `OFF`. In fail mode, findings are architecture violations. In advisory
mode, findings are architecture advisories: the library exposes them as a
separate rule set, and the CLI reports them in a separate `advisories` list
that never changes `pass` or the exit code. Switchable rules accept rule
exceptions as exact fully qualified class names or package patterns.

Existing rules stay always-on. Only new rules are switchable unless a later
decision widens this.

## Considered options

- Configurable failing rules with no advisory mode. Rejected: it does not
  give teams a way to report guidance without failing builds.
- Freezing as a substitute for warnings. Rejected: freezing tolerates known
  violations, which is a different concern from severity (see ADR 0006).

## Consequences

Consumers using JUnit must evaluate advisory rules separately and decide what
to do with the findings, since a JUnit assertion cannot "warn".
