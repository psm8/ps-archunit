# Polish Architecture Presentation Compared with `ps-archunit`

## Scope and method

This report summarizes the supplied Polish architecture presentation and
compares its recommendations with the current repository documentation and
executable ArchUnit rules. It is an architecture review, not a proposal to
raise every consumer to the highest rule tier.

Evidence used:

- the machine-generated transcript at
  `C:\Users\admin\AppData\Local\Temp\ps-archunit-video-analysis\transcript-faster.txt`;
- [`README.md`](../../README.md);
- [`ARCHITECTURE-HIERARCHY.md`](../../ARCHITECTURE-HIERARCHY.md);
- [`DDD-CONCEPTS.md`](../../DDD-CONCEPTS.md);
- [`ARCHITECTURE-AXES.md`](../../ARCHITECTURE-AXES.md);
- [`CONTEXT.md`](../../CONTEXT.md);
- the cumulative-tier [specification](../specs/0001-cumulative-architecture-rule-tiers.md);
- the corresponding [ADR](../adr/0001-cumulative-architecture-rule-tiers.md);
- the public rule factories, layouts, and fixture-level tests.

The transcript is a machine transcription of Polish speech. Some technical
terms and examples are garbled, so the comparison uses repeated themes and
context rather than treating every sentence as exact source text.

## Executive summary

The presentation and this repository agree strongly on the following:

1. **Use the simplest architecture that protects the likely changes.**
   Hexagonal Architecture, CQRS, and Event Sourcing are tools for specific
   pressures, not default badges of quality.
2. **Keep business code independent from volatile mechanisms.** External
   frameworks, databases, transports, and clients should not determine the
   domain model.
3. **Make boundaries explicit.** Ports, adapters, application orchestration,
   package-private implementations, and small public contracts reduce
   accidental coupling.
4. **Prefer testable composition.** Dependencies should be supplied at the
   boundary, while the application and domain remain usable without a real
   database or transport.
5. **Model business behavior deliberately.** Names, value objects, aggregate
   boundaries, and ubiquitous language matter when the domain is complex.

The repository already enforces much of the structural part of this advice.
Its Level 1, Level 2, and Level 3 factories cover dependency direction,
framework isolation, ports, adapters, composition mechanics, visibility, and
classification completeness.

The presentation also discusses concerns that are semantic, operational, or
context-specific: aggregate integrity, anemic versus behavior-rich models,
bounded contexts, event meaning, transaction and outbox guarantees, CQRS
read/write divergence, Event Sourcing evolution, and negotiation with the
business. These are mostly not suitable as generic ArchUnit rules. The
repository documents that boundary intentionally rather than claiming that
package structure proves good DDD.

## Presentation outline

The timestamps below are approximate ten-minute markers from the transcript,
not chapter boundaries.

| Approximate time | Main themes |
| --- | --- |
| 0:00-0:10 | Framing architecture buzzwords pragmatically; introducing the example and the cost of over-architecting. |
| 0:10-0:20 | Package and module organization, encapsulation, and why a more elaborate structure should have a reason. |
| 0:20-0:30 | Explicit Spring configuration, lightweight configuration, `@Bean` declarations, and avoiding unnecessary framework magic. |
| 0:30-0:50 | Dependency injection, repository abstractions, test doubles, ports, and keeping the core usable without a real database. |
| 0:50-1:00 | Public versus private repository contracts, implementation hiding, and minimizing the exposed API. |
| 1:00-1:10 | Application-layer orchestration across domain operations and repositories; transaction boundaries at the use-case level. |
| 1:10-1:20 | Commands and command handlers as an explicit application orchestration style, used when it improves clarity. |
| 1:20-1:40 | Event-driven ideas and Event Sourcing: storing facts, rebuilding state, replay, optimistic concurrency, and persistence concerns. |
| 1:40-1:50 | Domain model versus ORM/persistence model, mapping, framework annotations, and the cost of model pollution. |
| 1:50-2:10 | Domain events versus technical or incoming persistence events, ubiquitous language, business behavior, and distinguishing business code from technical code. |
| 2:10-2:20 | Bounded contexts, aggregates, Event Storming, modeling decisions, CRUD-first evolution, refactoring pressure, and the business cost of large redesigns. |

## Areas of strong agreement

### Choose architecture by change pressure

The presentation repeatedly warns against adopting Hexagonal Architecture,
CQRS, or Event Sourcing simply because they are fashionable. It recommends
starting with the business problem and adding structure when change, testing,
or mechanism volatility justifies the cost.

That is the central design decision in the repository. The public factories
are cumulative:

| Tier | Repository position |
| --- | --- |
| Level 1, `baseline` | Use for simple CRUD, configuration, translation, or orchestration. Protect basic boundaries without requiring a domain model. |
| Level 2, `domainOriented` | Use when repeated business rules, workflows, concepts, or boundaries need protection. |
| Level 3, `hexagonal` | Use when a domain-oriented system also has multiple or volatile external mechanisms. |

The [architecture hierarchy](../../ARCHITECTURE-HIERARCHY.md) explicitly says
that high traffic, a large team, or DDD terminology alone does not justify a
higher tier. This is an especially good match for the presentation's
CRUD-first and refactor-when-justified advice.

### Dependency inversion and framework isolation

The presentation recommends keeping domain code free of Spring and, where
the benefit warrants it, free of ORM details. The repository implements this
structurally:

- Level 2 enforces domain, application, API, and infrastructure direction.
- Level 2 permits only configured model-framework annotations on domain
  classes; it does not permit arbitrary runtime framework types.
- Level 3 adds onion direction and framework isolation for domain and
  application/core classes.
- Level 3 allows a narrow, explicit composition-root exception for assembling
  framework objects. The exception is not transitive.
- Port signatures must not expose framework or adapter types, including
  declaration annotations on parameters. Whole-port exceptions remain
  available for documented legacy boundaries.
- Level 2 can opt into placement checks for one exact transaction annotation
  in explicit package groups. Level 3 inherits that policy and permits the
  configured annotation as a narrow framework-isolation exception.

This captures the presentation's most portable recommendation: technical
mechanisms may be used at the edges, but they should not silently become
business dependencies.

### Ports, adapters, and testable composition

The presentation shows external repositories and other mechanisms behind
interfaces, with test implementations supplied separately from production
implementations. It also emphasizes constructor-supplied dependencies and
application code that can be tested without infrastructure.

Level 3 provides executable support for the boundary mechanics:

- inbound and outbound ports are interfaces with configurable naming
  conventions;
- ports are public contracts;
- port signatures are framework-free and adapter-free;
- directional outbound adapters implement an outbound port;
- adapter implementations are package-private by default;
- adapter implementations must remain in configured adapter groups;
- API and inbound adapters may translate into domain/application types but may
  not depend on infrastructure;
- outbound and mixed adapters are treated as infrastructure for lower-tier
  checks;
- ports are treated as application/core for lower-tier checks.

These rules do not prove that a consumer chose the right port granularity or
that dependency injection uses constructors everywhere. They do protect the
most important accidental-coupling failures.

### Encapsulation and small public APIs

The presentation recommends hiding implementation classes and exposing only
the contracts needed by callers. The repository supports this through
package-private adapter implementations, public ports, output checks, and
dependency direction. The public API of the library itself also separates
configuration concepts by tier instead of exposing one undifferentiated
selector surface.

This is a strong example of what ArchUnit can check well: visibility,
location, naming, and dependency relationships are observable in imported
bytecode.

### Explicit distinction between DDD and Hexagonal Architecture

The presentation connects DDD modeling with Hexagonal Architecture but does
not treat them as identical. The repository makes the distinction explicit:

- [`DDD-CONCEPTS.md`](../../DDD-CONCEPTS.md) describes bounded contexts,
  aggregates, policies, and domain events as modeling concepts.
- [`CONTEXT.md`](../../CONTEXT.md) says that DDD is not a package layout and
  does not automatically imply dependency inversion.
- [`ARCHITECTURE-HIERARCHY.md`](../../ARCHITECTURE-HIERARCHY.md) defines Level
  3 as dependency-inverted domain architecture, not as a guarantee of rich
  DDD.

This prevents a common category error: passing an onion rule does not mean
that the domain has good aggregates or ubiquitous language.

## What the executable rules already cover

### Level 1: baseline structural safety

The `baseline` factory covers:

- package-slice cycle checks;
- explicit, configurable dependency bans;
- unrestricted `@Configuration` classes by default, with opt-in package-private
  enforcement;
- package-private internal `@ConfigurationProperties` classes;
- lightweight Spring configuration, including `proxyBeanMethods = false`;
- `@Bean` placement and declared return-type exposure policy;
- record or sealed-interface boundary outputs.

This supports the presentation's preference for simple, explicit wiring even
when a full domain architecture is unnecessary.

### Level 2: domain-oriented direction

The `domainOriented` factory adds:

- domain code cannot depend on application, API, or infrastructure;
- application code cannot depend on API or infrastructure;
- API code may depend on application and domain, but not infrastructure;
- infrastructure cannot depend on API;
- infrastructure may depend on domain and application;
- only configured model annotations are allowed in domain classes;
- every imported class under the base package must belong to at least one
  configured domain, application, API, or infrastructure group.

The completeness rule is important. It prevents a new class from silently
escaping the architecture merely because it was placed in an unrecognized
package. Empty configured groups remain no-ops, overlapping groups are
allowed, and classes outside the base package are ignored.

### Level 3: dependency-inverted domain architecture

The `hexagonal` factory retains Levels 1 and 2 and adds:

- onion dependency direction;
- framework isolation for domain and application/core code;
- explicit composition-root handling;
- framework-free public port signatures;
- `UseCase` and `Port` naming and interface checks;
- public port contracts;
- outbound adapter implementation of outbound ports;
- package-private adapter implementations;
- adapter containment in configured adapter groups;
- effective classification of ports as application/core and adapters as API or
  infrastructure where appropriate.

The implementation is therefore strong at enforcing architecture mechanics
that can be observed from class dependencies and metadata. The fixture tests
also cover valid and invalid examples for classification, framework
isolation, composition roots, ports, adapters, layout promotion, and
cumulative tier behavior.

## Partial coverage and documentation-only guidance

The following recommendations are represented in repository documentation or
are indirectly supported, but are not fully enforced by generic rules.

| Presentation recommendation | Current repository position | Assessment |
| --- | --- | --- |
| Keep domain and application code independent from frameworks. | Directly enforced at Level 2/3, with a narrow composition-root exception and configured model-annotation allowlists. | Strong executable coverage. |
| Use explicit, lightweight Spring configuration. | Level 1 checks opt-in `@Configuration` visibility, default package-private `@ConfigurationProperties` visibility, `@Bean` placement, declared return-type exposure, and `proxyBeanMethods = false`. The library intentionally has no Spring runtime dependency. | Strong for the selected Spring conventions; configuration class visibility is unrestricted unless enabled, and the rules do not validate the whole application bootstrap. |
| Inject external dependencies at the boundary. | Ports, adapter contracts, framework-free signatures, and direction rules protect the boundary. | Structural support, but constructor injection itself is not a universal rule. |
| Use value objects and strong types instead of primitive-heavy models. | DDD concepts and hierarchy documents support explicit business modeling. | Guidance only; ArchUnit cannot reliably infer semantic type strength. |
| Put transaction orchestration at the application/use-case boundary. | Level 2 can opt into exact transaction-annotation placement in explicit package groups. Level 3 inherits the policy and keeps the configured annotation as a narrow framework-isolation exception. | Placement is structurally enforced when configured; transaction semantics, atomicity, and runtime behavior remain outside generic rules. |
| Use commands and handlers when orchestration benefits from them. | Application and port groups can contain such classes, but the repository does not require a command/handler layout. | Intentionally optional. |
| Separate ORM models from domain models when ORM coupling becomes costly. | Framework isolation and model-annotation rules support the direction, but no rule requires a separate persistence model or mapper. | Partially protected, intentionally flexible. |
| Distinguish domain events from technical or persistence events. | `DDD-CONCEPTS.md` and `ARCHITECTURE-AXES.md` document the distinction. | Documentation only. |
| Use ubiquitous language in methods, types, and events. | Domain-oriented naming is encouraged by the documentation, but generic rules do not understand business vocabulary. | Documentation only. |
| Test business behavior more heavily than trivial mapping code. | The repository tests its own public rule behavior and fixture boundaries. Consumer domain-test strategy is outside the library's scope. | Guidance, not a consumer test policy. |

## Important gaps, and why they should not become generic rules

### Aggregate integrity and behavior-rich models

The presentation discusses aggregate roots, consistency boundaries, and
keeping business decisions inside the domain model rather than in anemic
application services. The repository can identify a package or a class name,
but it cannot determine from bytecode alone:

- whether a class is the correct aggregate root;
- whether all invariant-changing operations pass through that root;
- whether a child entity is exposed incorrectly;
- whether two objects belong in one aggregate or two;
- whether a method expresses a business decision or merely moves data;
- whether a value object is semantically stronger than a primitive.

These are modeling decisions that require domain knowledge. A generic rule
that requires `Aggregate`, `Entity`, or `ValueObject` names would create
ceremony without proving the model is correct.

### Bounded contexts and ubiquitous language

The presentation emphasizes discovering boundaries with domain experts,
Event Storming, and choosing terms that have one meaning within a bounded
context. A package rule can enforce that classes stay in a package, but it
cannot determine whether:

- the context boundary reflects a real business capability;
- the same word has conflicting meanings;
- an event name describes a business fact rather than a database action;
- a model is incorrectly shared between contexts.

The repository should continue to document these concepts, but should not
pretend that package naming is semantic validation.

### Transaction, consistency, and delivery guarantees

The presentation discusses application-level orchestration, persistence,
domain events, and the practical consequences of asynchronous work. When a
consumer opts into the transaction placement policy, the repository verifies
only the configured annotation's class/method placement. It does not verify:

- that one use case has the intended transaction boundary beyond annotation
  placement;
- that multiple writes commit atomically;
- optimistic locking or aggregate version checks;
- outbox publication;
- idempotency, retries, ordering, or eventual-consistency behavior;
- whether an incoming persistence event is safely translated into a domain
  event.

These require runtime behavior, database configuration, integration tests, or
consumer-specific annotations. They are not general dependency rules.

### CQRS and Event Sourcing

The presentation presents CQRS and Event Sourcing as choices that can solve
specific read/write or historical-state problems, while also adding
duplication, synchronization, replay, schema-evolution, and migration costs.

The repository intentionally does not require:

- separate command and query models;
- separate read and write packages;
- event handlers or projections;
- an event store;
- replay or snapshot mechanisms;
- event versioning or migration code.

[`ARCHITECTURE-AXES.md`](../../ARCHITECTURE-AXES.md) correctly treats CQRS,
event-driven design, and Event Sourcing as separate axes that can be combined
with any architecture level. Adding generic rules for them would conflate
orthogonal decisions and encourage adoption for the wrong reason.

### Refactoring and organizational constraints

The closing discussion stresses that poor early modeling can be expensive to
repair and that refactoring competes with visible business work. ArchUnit can
prevent selected regressions once a boundary is chosen. It cannot decide
whether the business will fund a redesign, whether a migration is worth its
cost, or whether a model change is safe across deployed consumers.

Those concerns belong in product planning, domain workshops, migration
design, and operational validation rather than in the generic rule tiers.

## Deliberate non-goals that are appropriate

The repository should continue to avoid turning these into universal Level 1,
2, or 3 rules:

- mandatory DDD for every application;
- mandatory `core`, `entity`, `dto`, `service`, or similar subpackages;
- mandatory ports for every Level 2 application;
- mandatory CQRS, event-driven design, or Event Sourcing;
- deployment topology or microservice boundaries;
- performance, resilience, security, or operational policy;
- a requirement that all ORM and domain models be separate;
- a requirement that all domain events use one event bus or persistence
  mechanism;
- assumptions about one universal transaction annotation or framework.

Consumers can add project-specific ArchUnit rules for these conventions when
they have a concrete local policy. The generic library should remain focused
on stable structural contracts and configurable package groups.

## Recommended follow-up work

No Java implementation change is required by this comparison. The current
rules already cover the structural recommendations that are both valuable and
reliably observable.

If this repository needs further support, the highest-value additions would be
documentation and extension seams rather than new mandatory tiers:

1. **Add a consumer guidance section** showing how to combine the tiers with
   optional project-specific rules for commands, aggregate roots, domain
   events, or transaction annotations.
2. **Document the boundary between structural and semantic checks** with
   examples of rules that are safe to customize and concepts that require
   domain review.
3. **Provide optional examples, not defaults,** for:
   - controller-to-use-case-only access;
   - aggregate-root visibility conventions;
   - domain-event naming and placement;
   - persistence mapper placement;
   - application-handler transaction conventions.
4. **Keep CQRS, Event Sourcing, outbox, and migration guidance in separate
   architecture documentation** so that consumers can opt in based on actual
   requirements.
5. **Consider consumer-supplied predicates or fixtures** if repeated users
   need semantic conventions. A consumer that knows its domain can provide
   better predicates than a generic library can infer.

## Conclusion

The presentation is a good fit for the repository's pragmatic architecture
position. It supports the repository's strongest rules: inward dependency
direction, framework isolation, explicit composition, ports and adapters,
encapsulation, and incremental adoption based on change pressure.

The presentation also reinforces the repository's restraint. Good aggregates,
ubiquitous language, bounded contexts, event contracts, transaction
guarantees, and migration strategies are not consequences of package
structure. They require modeling decisions, runtime guarantees, and business
collaboration. The current rule tiers should protect the mechanical
preconditions for those decisions without claiming to enforce the decisions
themselves.
