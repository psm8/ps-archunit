# Cumulative architecture rule tiers

**Status:** Accepted

## Context

Architecture rules need to express three levels of architecture investment:

1. infrastructure-centric applications;
2. domain-oriented applications;
3. dependency-inverted domain applications.

The implementation must make ownership of each level visible instead of
concentrating unrelated rule groups in one class.

## Decision

Split the public rule factories across three tier classes:

- `BaselineArchitectureRules` owns Level 1:
  cycles, dependency bans, configuration, beans, and boundary outputs;
- `DomainOrientedArchitectureRules` owns cumulative Levels 1 and 2:
  domain/application/API/infrastructure direction and model-only framework
  annotations;
- `HexagonalArchitectureRules` owns cumulative Levels 1, 2, and 3:
  onion direction, framework isolation, ports, and adapters.

Each tier exposes `String` and matching typed-layout factories:
`BaselineLayout`, `DomainOrientedLayout`, and `HexagonalLayout`.
Higher tiers compose lower tiers and never remove lower-tier protections.

`strictHexagonal(String)` and `laxHexagonal(String)` remain Level 3
convenience factories on `HexagonalArchitectureRules`. Strict uses separate
inbound and outbound adapter defaults. Lax uses a direction-neutral mixed
adapter group.

Shared ArchUnit conditions, predicates, matching, validation, and composition
helpers are package-private implementation support. They are not public
architecture tiers.

The three immutable cumulative layouts expose only the selectors owned by their
tier. Higher builders copy lower snapshots. Level 2 defaults include domain,
application, base API, base infrastructure, and model-framework groups; they
do not include adapter defaults. Level 3 adds ports and adapters. Replacement
methods replace defaults; `add...` methods append. Level 3 uses declared API
plus inbound adapters and declared infrastructure plus outbound/mixed adapters
as internal effective groups. API package customization remains on the
domain-oriented layout; the hexagonal layout inherits it through promotion and
does not repeat the API selector on its public surface. Component annotations
are not package selectors. Configuration is discovered from annotations instead
of being represented as a package group.


Level 2 API direction bans only dependencies on infrastructure. API packages
and inbound adapters may depend directly on application and domain types. This
keeps dependency direction distinct from the optional project policy that
requires every delivery adapter to enter through an application use case, and
supports translation adapters that normalize external representations into
domain types.

Levels 2 and 3 also enforce classification completeness within the consumer's
scope: every imported class under `basePackage..` must match at least one
configured domain, application/core, API, or infrastructure group. Level 1
remains permissive. Empty groups remain no-ops, overlapping matches are
allowed, classes outside the base package are ignored, and Level 3 classifies
inbound and outbound ports as application/core while folding adapters into
effective API or infrastructure groups.
## Consequences

Consumers can select the lowest tier that protects the system's likely
changes. The class names reveal which rules belong to each tier, and stronger
tiers retain all lower-tier checks.

The old single-class implementation and superseded ADR content are removed.
Callers use the tier owner that defines the desired factory.
