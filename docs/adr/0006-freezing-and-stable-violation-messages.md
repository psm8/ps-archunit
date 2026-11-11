# Freezing violations and stable violation messages

**Status:** Accepted

## Context

Adopting a stricter architecture level in an existing system produces many
violations at once. ArchUnit's freezing records the existing violations in a
violation store and fails only on new ones. It identifies a frozen violation
by its message text.

## Decision

The CLI supports freezing with `--freeze-store <dir>`. Only architecture
violations are frozen; advisories are never frozen. The consumer commits the
store to their repository. A missing store fails the run instead of being
recreated silently; re-recording requires an explicit `--refreeze`.

The store has one entry per tier rule. Checks that a release adds to a tier
rule report new violations under the existing entry. A rule without an entry
(after changing `tier` or `basePackage`, or a rule that a release adds
separately) has no frozen violations: all of its violations are new and fail
the run, and the store is not changed. The run does not stop with a
configuration error; the consumer fixes the new violations or accepts them
with `--refreeze`. ArchUnit's default, recording the
current violations of an unknown rule and passing, is not used, because it
hides the violations of a new rule. Stopping the run until `--refreeze` is not
used either, because `--refreeze` re-records every rule and would also accept
new violations of the existing rules.

JUnit consumers wrap the library's rules in `FreezingArchRule` themselves. The
README documents how; the library adds no freezing API.

Violation message text and the tier rule descriptions (the `as(...)` and
`because(...)` text of `baseline`, `domainOriented`, and `hexagonal`) are a
compatibility contract. The store is keyed by rule description, so changing it
makes all of its frozen violations look new; changing a violation message
makes the matching frozen violations look new. Both are breaking changes and
must be called out in the release notes.

## Consequences

Rewording a message, even to fix a typo, makes every matching frozen
violation look new in consumer builds. Message improvements therefore need
the same care as public API changes.
