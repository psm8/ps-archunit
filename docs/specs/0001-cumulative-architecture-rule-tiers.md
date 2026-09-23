# Cumulative architecture rule tiers

## Problem Statement

The library currently exposes one bundled rule set under several profile names.
That makes the architecture hierarchy unclear:

- Consumers cannot select infrastructure-centric, domain-oriented, or
  dependency-inverted domain architecture independently.
- The current profiles imply that every application must pay for the full
  hexagonal rule set.
- Domain-oriented dependency boundaries are missing as an explicit middle
  tier.
- Package vocabulary mixes broad architecture concepts with technical
  configuration selectors.
- README, context, ADR, and architecture Markdown describe obsolete profile
  semantics.

This makes rule selection, migration, and shared architecture language harder
than necessary.

## Solution

Expose three cumulative architecture rule tiers:

1. `baseline`: Level 1, infrastructure-centric architecture.
2. `domainOriented`: Level 1 plus Level 2, domain-oriented architecture.
3. `hexagonal`: Level 1 plus Level 2 plus Level 3, dependency-inverted domain
   architecture.

Each tier accepts a base package or its matching typed layout:
`BaselineLayout`, `DomainOrientedLayout`, or `HexagonalLayout`.
Strict and lax hexagonal convenience profiles remain available as Level 3
factories. Package layout gains broad API and infrastructure groups.
Default domain, application, API, infrastructure, port, and adapter groups
include both direct and one-feature vertical variants. Configured `{base}`
tokens resolve exactly to the concrete base package, and `*` matches one
package segment.
Configuration stops being a logical package group. Documentation and ADR
content describe the same model.

## User Stories

1. As a library consumer, I want to select baseline rules, so that simple CRUD,
   configuration, translation, and stateless orchestration applications do not
   pay for hexagonal rules.
2. As a library consumer, I want to select domain-oriented rules, so that
   repeated business behavior gets explicit dependency boundaries without
   requiring full dependency inversion.
3. As a library consumer, I want to select hexagonal rules, so that volatile
   external mechanisms are kept outside the domain and application core.
4. As a library consumer, I want higher tiers to include lower tiers, so that a
   stronger profile never silently removes baseline protections.
5. As a library consumer, I want a base-package factory for each tier, so that
   common layouts need minimal configuration.
6. As a library consumer, I want a typed layout factory for each tier, so that
   feature-first and non-default package layouts remain configurable.
7. As a library consumer, I want empty package groups to produce no-op rules
   while unmatched classes under the configured base package fail at Levels 2
   and 3, so that partial applications remain checkable without silently
   ignoring architecture code.
8. As a library consumer, I want domain dependencies on application,
   API, and infrastructure rejected at Level 2, so that business meaning
   remains independent from outer concerns.
9. As a library consumer, I want application dependencies on API and
   infrastructure rejected at Level 2, so that use-case orchestration is not
   shaped by delivery or implementation mechanisms.
10. As a library consumer, I want API code allowed to depend on application
    and domain types but not infrastructure at Level 2, so that inbound
    adapters can translate external representations without coupling to
    technical mechanisms.
11. As a library consumer, I want infrastructure code prevented from depending
    on API code at Level 2, so that outbound mechanisms do not depend on
    inbound delivery details.
12. As a library consumer, I want infrastructure code to depend on domain and
    application code at Level 2, so that pragmatic implementations can use
    domain types and use-case contracts.
13. As a library consumer, I want selected model annotations allowed in domain
    code at Level 2, so that pragmatic JPA, validation, and Jackson models do
    not require artificial duplication.
14. As a library consumer, I want runtime and service framework dependencies
    rejected from domain code at Level 2, so that model convenience does not
    become framework coupling.
15. As a library consumer, I want to configure model-framework namespaces, so
    that the allowlist can match project policy without weakening all framework
    checks.
16. As a library consumer, I want model-framework allowances limited to model
    annotations, so that runtime clients and framework services cannot enter
    the domain through an allowlisted namespace.
17. As a library consumer, I want Level 3 onion direction checks, so that
    dependencies point inward through the domain and application core.
18. As a library consumer, I want Level 3 framework isolation, so that domain
    and application behavior can be tested without framework infrastructure.
19. As a library consumer, I want direct or meta-annotated Spring composition
    roots recognized without a Spring runtime dependency, so that standard
    Boot bootstrap classes do not create false positives.
20. As a library consumer, I want composition-root exemptions limited to the
    root source and selected dependency-based checks, so that they do not
    spread transitively through the core or weaken explicit policy rules.
21. As a library consumer, I want driving and driven port rules at Level 3, so
    that application-owned contracts remain visible, named, and framework
    independent.
22. As a library consumer, I want outbound adapter implementations checked
    against outbound ports, so that adapters cannot silently bypass contracts.
23. As a library consumer, I want adapter visibility and containment checked,
    so that adapter implementations do not become accidental public API.
24. As a library consumer, I want component annotations allowed in outer
    adapter, API, and infrastructure code, so they do not become accidental
    package selectors.
25. As a library consumer, I want existing strict directional adapter behavior
    preserved, so that migration does not change established Level 3 checks.
26. As a library consumer, I want existing lax mixed-adapter behavior preserved,
    so that applications without directional adapter packages can migrate.
27. As a library consumer, I want configuration rules to remain available
    without a configuration package selector, so that configuration is treated
    as infrastructure rather than a separate architecture layer.
28. As a library consumer, I want configuration classes and bean methods found
    from their annotations, so that technical configuration can live in the
    infrastructure layout chosen by the application.
29. As a library consumer, I want configuration class visibility unrestricted
    by default with an opt-in package-private policy, so existing public
    configuration remains valid while stricter applications can enforce it.
30. As a library consumer, I want selector replacement and append semantics
    consistent across package groups, so that custom layouts are predictable.
31. As a library maintainer, I want each rule group assigned to one tier, so
    that future changes preserve cumulative semantics.
32. As a library maintainer, I want public factory names to reflect architecture
    meaning, so that consumers do not infer false differences from legacy
    profile names.
33. As a library maintainer, I want obsolete strict/lax terminology marked as
    compatibility API, so that new documentation teaches the tier model.
34. As a library maintainer, I want tests to exercise public `ArchRule` seams,
    so that behavior remains validated without coupling tests to private helper
    methods.
35. As a library maintainer, I want fixture packages representing valid and
    invalid tier behavior, so that dependency direction and framework policy
    regressions are visible.
36. As a documentation reader, I want the architecture hierarchy, glossary,
    README, axes, DDD concepts, and ADR to agree, so that one vocabulary is
    used across design and code.
37. As a documentation reader, I want the ADR history to explain why the
    cumulative tiers exist, so that the public rule behavior is not surprising.
38. As a contributor, I want stale `minimal`, `standard`, and full-rule-set
    terminology removed from current documentation, so that old semantics are
    not reintroduced.
39. As a contributor, I want validation to include tests, verification,
    documentation searches, and diff checks, so that code and written contracts
    stay synchronized.
40. As a library consumer, I want to opt into transaction-annotation placement
    checks at Level 2, so that transaction markers stay on application
    boundaries without making a framework annotation mandatory for every
    consumer.
41. As a library consumer, I want the transaction annotation name and allowed
    package patterns configured explicitly, so that the generic library does
    not depend on Spring or assume one transaction framework.
42. As a library consumer, I want framework and adapter annotations on port
    parameter declarations rejected, so that parameter metadata cannot bypass
    the framework-free port contract.

## Implementation Decisions

- The architecture scope is one independently deployable application or
  runtime. A repository is not automatically one system.
- The three levels are cumulative:
    - Level 1 is infrastructure-centric.
    - Level 2 is domain-oriented and DDD-informed, without requiring dependency
      inversion.
    - Level 3 is dependency-inverted domain architecture using ports and
      adapters.
- Add `baseline` and `domainOriented` factories for both base-package and
  explicit-layout use. Keep `hexagonal` as the Level 3 factory.
- Organize factories by tier owner: `BaselineArchitectureRules`,
  `DomainOrientedArchitectureRules`, and `HexagonalArchitectureRules`.
- Keep `strictHexagonal` and `laxHexagonal` as Level 3 convenience factories.
  Strict uses separate inbound and outbound adapter defaults. Lax uses a
  direction-neutral mixed adapter group.
- Level 1 owns cycle checks, explicit dependency bans, opt-in package-private
  configuration visibility, default package-private configuration-properties
  visibility, configuration proxy mode, bean placement and return-type checks,
  and immutable boundary output shape.
- Level 2 owns coarse dependency direction between domain, application, API,
  and infrastructure groups. Domain is inward of application. API is the
  inbound-facing group and may depend on application and domain, but not
  infrastructure. In Level 3, inbound adapters join its effective boundary.
  Infrastructure contains outbound adapters, persistence, clients, messaging,
  configuration, and framework integration.
- Level 2 permits infrastructure dependencies on domain and application.
  Level 2 does not require ports for infrastructure dependencies.
- Level 2 model-framework allowances are restricted to annotation metadata on
  domain model classes. Default namespaces cover JPA persistence annotations,
  Jakarta/Javax validation annotations, and Jackson annotations. Non-model
  framework types, runtime clients, Spring types, and service APIs remain
  violations.
- Add configurable API, infrastructure, and domain-model-framework package
  groups. Replacement methods replace defaults. `add...` methods append.
- Keep API package customization on `DomainOrientedLayout`. `HexagonalLayout`
  inherits a promoted domain-oriented snapshot and uses the API boundary
  internally without repeating API selector methods on its public surface.
- Standalone Level 2 domain, application, API, and infrastructure defaults
  include direct and one-feature vertical groups. Level 3 port and directional
  adapter defaults use the same direct and one-feature shape. Lax mixed
  adapter defaults use direct and one-feature `adapter..` roots.
- Resolve `{base}` exactly in configured package selectors, dependency-ban
  package groups, and pattern-pair endpoints before rules evaluate them.
  Preserve `*` as a one-package-segment matcher.
- Level 3 lower-tier checks add inbound adapters to the effective API group
  and outbound/mixed adapters to the effective infrastructure group. Level 3
  treats declared inbound and outbound port groups as application/core.
- Keep directional and mixed adapter selectors needed by Level 3 wiring and
  containment rules.
- Keep configuration and configuration-properties as annotation-discovered
  technical concerns rather than package selectors. Configuration class
  visibility is unrestricted by default, with opt-in package-private
  enforcement and the existing exact `publicConfigurationClasses(...)`
  exception. Configuration-properties visibility remains package-private by
  default with its independent exact `publicConfigurationProperties(...)`
  exception.
- Level 3 adds onion direction, strict framework isolation, framework-free port
  contracts, port naming and visibility, outbound adapter wiring, adapter
  visibility, and adapter containment.
- Level 2 transaction placement is opt-in. A configured exact annotation type
  and explicit package patterns are required together. The rule checks direct
  declaration annotations on classes and methods and enforces placement only;
  it does not infer transaction semantics or atomicity. Level 3 inherits this
  configuration.
- Level 3 framework isolation permits the exact configured transaction
  annotation only when the source class is in a configured transaction
  package. Other framework dependencies remain violations.
- Level 3 port signature checks include method parameter declaration
  annotations. Whole-port signature exceptions continue to skip the complete
  signature check. Type-use annotations are outside this change.
- Level 3 framework isolation applies to domain and application classes.
  Classes directly or recursively meta-annotated with `@Configuration`,
  `@SpringBootConfiguration`, or `@SpringBootApplication` are recognized as
  composition roots by annotation name. Their source-local assembly
  dependencies are exempt from framework isolation, classification
  completeness, domain direction, onion direction, and package-cycle checks.
  Referenced targets remain checked, and the exception is not transitive to
  other domain or application classes.
- Explicit dependency bans, bean placement and exposure, configuration
  visibility, `proxyBeanMethods`, transaction placement, and port/adapter
  rules remain active for composition roots.
- Package groups remain optional. Rules targeting empty groups are no-ops.
  Levels 2 and 3 additionally require every imported class under
  `basePackage..` to match at least one configured domain, application/core,
  API, or infrastructure group. Level 1 remains permissive. Overlapping group
  matches are valid, and imported classes outside `basePackage..` are not
  classified.
- Existing output suffix, dependency-ban, cycle-ignore, port-signature,
  adapter exception, bean exposure, and configuration visibility customization
  remains supported unless removed explicitly above. Configuration class
  visibility is unrestricted unless explicitly set to package-private.
- Baseline bean exposure is based on the declared `@Bean` return type:
  concrete application-owned types and interfaces outside the application
  base package pass; application-owned interfaces annotated with
  `@FunctionalInterface` also pass. Other application-owned interface types
  fail by default. `allowedApplicationInterfaceBeanTypes(...)` allows exact
  fully qualified application-owned interface names only. Null, blank, simple,
  wildcard, and package-pattern values are invalid. The rule does not inspect
  method bodies or infer ports and use cases.
- Record the bean exposure decision in ADR 0002.
- Update current architecture Markdown, glossary, README, and ADR content to
  use the same tier and package vocabulary.

## Testing Decisions

- Test the public factory seam: each factory returns an `ArchRule` evaluated
  against imported Java fixture classes.
- Prefer fixture-level behavior tests over private helper tests.
- Verify Level 1 rules independently, Level 2 includes Level 1, and Level 3
  includes both lower tiers.
- Add valid and invalid fixtures for domain/application/API/infrastructure
  direction.
- Include valid API and inbound-adapter fixtures that depend directly on
  domain types, plus an invalid API-to-infrastructure fixture.
- Add valid model classes using default model annotations and invalid model
  classes using Spring or runtime framework types.
- Add valid and invalid composition-root fixtures for direct and
  meta-annotated Boot roots, root-to-adapter wiring, root-only cycles, ordinary
  non-root cycles, dependency-ban preservation, and non-transitive framework
  leakage.
- Verify typed cumulative layouts, custom API, infrastructure, and
  model-framework package selectors, replacement behavior, append behavior,
  promotion isolation, HexagonalLayout API-surface hiding, effective Level 3
  groups, application/core port derivation, classification completeness,
  external imported classes, overlapping groups, and empty-group no-op
  behavior. Verify direct and feature-local vertical fixtures, `{base}`
  substitution, and one-segment `*` matching.
- Preserve regression coverage for strict and lax aliases, onion direction,
  cycles, ports, adapter wiring and containment, component annotations,
  outputs, beans, configuration behavior, and dependency bans. Bean tests must
  cover concrete application returns, external interfaces, rejected local
  interfaces, exact allowlisting, invalid selector values, and package-prefix
  ownership boundaries.
- Add valid and invalid transaction-placement fixtures, partial-configuration
  failures, inherited Level 3 configuration, and framework-isolation coverage.
  Add a port fixture with a framework annotation on a parameter declaration
  and preserve whole-port signature exception coverage.
- Validate external behavior only: rule pass/fail results, layout snapshots,
  factory validation, and public compatibility behavior.
- Run the project test suite, full Maven verification, whitespace/diff checks,
  and stale-documentation searches.

## Out of Scope

- Adding new architecture levels.
- Making DDD mandatory for every application.
- Enforcing internal `entity`, `dto`, `message`, `service`, or similar
  subpackages.
- Making `core` a required package. `core` remains a conceptual umbrella for
  domain and application.
- Requiring every Level 2 application to define ports or adapters.
- Designing modular-monolith, microservice, CQRS, event-driven, deployment,
  performance, resilience, or security rules as part of these tiers.
- Adding Spring, JPA, Validation, or Jackson as library runtime dependencies.
- Changing ArchUnit's class-importing responsibility owned by consumers.
- Publishing commits, opening pull requests, or changing remote repositories.
- Publishing to an external issue tracker until tracker and triage-label
  configuration is available.

## Further Notes

- Highest test seam is the public factory plus imported fixture classes.
- `BaselineLayout`, `DomainOrientedLayout`, and `HexagonalLayout` are immutable
  cumulative snapshots with mutable builders. Higher builders copy lower
  snapshots; they do not inherit from them.
- The hard removal of configuration selectors is an intentional public API
  change. It prevents configuration from being mistaken for a logical
  architecture layer.
- Local spec saved in the project. Issue-tracker publication remains blocked
  because no issue-tracker configuration, triage vocabulary, or connected app
  is available in this session.
