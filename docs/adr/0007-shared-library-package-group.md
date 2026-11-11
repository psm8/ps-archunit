# Shared library package group

**Status:** Accepted

## Context

Systems often have technical code that both sides of the outer ring use:
request correlation, structured log fields, context propagation, clock
helpers. It is not use-case orchestration and it connects no external
mechanism, so neither the application group nor an adapter group describes it.

Level 2 lets API code depend on application and domain but not on
infrastructure, and Level 3 applies the same direction to inbound adapters.
Code shared by inbound and outbound adapters therefore passes only when it is
classified as application. That puts technical, framework-dependent code
inside the core, where domain and application code may depend on it.

## Decision

Add a shared library package group from Level 2, inherited by Level 3:

- API, inbound adapter, infrastructure, outbound adapter, and mixed adapter
  code, and composition roots, may depend on it.
- Domain, application, and port code may not depend on it. The core obtains
  technical capabilities through driven ports.
- It is a leaf: its classes may depend only on the group itself and on code
  outside the base package. Composition roots inside it keep the source-local
  exemption of ADR 0004.
- It is outside the core, so framework isolation and the interface default
  method rule do not apply to it.
- Its default selectors are `{base}.lib..` and `{base}.*.lib..`, like the
  defaults of the other groups. The layout and the CLI configure it as
  `sharedLibPackages`.
- The new checks report through the existing tier rules; their descriptions
  stay unchanged (ADR 0006).

## Considered options

- Keep classifying such code as application. Rejected: it lets domain and
  application code depend on technical, framework-dependent code, and it
  misnames the group.
- A shared group that the core may also use (a shared kernel). Deferred:
  business concepts shared between features are a different concern, with
  framework isolation, and no system needs it yet.
- No default selectors. Rejected for consistency with the other groups; the
  leaf rule and the _Avoid_ note in the glossary guard against a catch-all.

## Consequences

A consumer with an existing `lib` or `*.lib` package under the base package
gets the leaf and dependent rules for it after upgrading, which may report new
violations. The release that introduces the group is a minor release and calls
this out.
