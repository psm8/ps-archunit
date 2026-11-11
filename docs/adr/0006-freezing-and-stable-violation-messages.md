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

JUnit consumers wrap the library's rules in `FreezingArchRule` themselves. The
README documents how; the library adds no freezing API.

Violation message text is a compatibility contract. Changing the wording of a
violation message is a breaking change and must be called out in the release
notes.

## Consequences

Rewording a message, even to fix a typo, makes every matching frozen
violation look new in consumer builds. Message improvements therefore need
the same care as public API changes.
