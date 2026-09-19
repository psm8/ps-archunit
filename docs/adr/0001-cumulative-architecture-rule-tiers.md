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
  onion direction, framework isolation, ports, adapters, and component
  scanning.

Each tier exposes `String` and `PackageLayout` factories where applicable.
Higher tiers compose lower tiers and never remove lower-tier protections.

`strictHexagonal(String)` and `laxHexagonal(String)` remain Level 3
convenience factories on `HexagonalArchitectureRules`. Strict uses separate
inbound and outbound adapter defaults. Lax uses a direction-neutral mixed
adapter group.

Shared ArchUnit conditions, predicates, matching, validation, and composition
helpers are package-private implementation support. They are not public
architecture tiers.

`PackageLayout` exposes API, infrastructure, and domain-model-framework
groups. Replacement methods replace defaults; `add...` methods append.
Configuration is discovered from annotations instead of being represented as a
package group.

## Consequences

Consumers can select the lowest tier that protects the system's likely
changes. The class names reveal which rules belong to each tier, and stronger
tiers retain all lower-tier checks.

The old single-class implementation and superseded ADR content are removed.
Callers use the tier owner that defines the desired factory.
