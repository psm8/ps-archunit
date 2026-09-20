# Logical application architecture hierarchy

Practical model for choosing architecture investment. Not a quality score.
Higher levels add structure, boundaries, tests, and terminology. They are worth
the cost only when they protect changes the system is likely to face.

## Scope

“System” means an independently deployable application or runtime:

- A monolith is one system.
- Each independently deployed service is one system.
- A repository is not automatically one system.

Choose a level for the system. A Level 1 system may still use a targeted port
or adapter around one volatile integration.

## Decision rule

Choose the lowest level that protects the system's likely changes:

1. Little business behavior and stable mechanisms: Level 1.
2. Repeated business rules, workflows, concepts, or boundaries: Level 2.
3. Level 2 plus multiple or volatile external mechanisms: Level 3.

Architecture investment is justified by concrete pressure, not by fashion,
team size, codebase size, traffic, or a desire to future-proof the system.

## Rule scope and classification

The tiers differ in how much package structure they enforce:

- Level 1 remains permissive about architecture categories. It does not reject
  an otherwise valid in-scope class merely because no domain, application, API,
  or infrastructure selector matches it.
- Levels 2 and 3 fail closed. Every imported class under `basePackage..` must
  match at least one configured architecture group. Level 3 classifies inbound
  and outbound ports as application/core, inbound adapters as effective API,
  and outbound or mixed adapters as effective infrastructure.
- Empty groups remain optional and rules targeting an empty group remain
  no-ops. A class may match multiple groups. Imported classes outside the base
  package are not part of classification.

This completeness check validates the consumer's imported scope. It does not
invent an infrastructure fallback for classes that the configured vocabulary
does not describe.

## Default package topology

The default Level 2 groups cover both direct and one-feature vertical layouts:

```text
{base}.domain..              {base}.*.domain..
{base}.application..         {base}.*.application..
{base}.api..                 {base}.*.api..
{base}.infrastructure..      {base}.*.infrastructure..
```

Level 3 adds the same direct and one-feature variants for inbound and outbound
ports under `application.port.in` and `application.port.out`, and for
directional adapters under `adapter.in` and `adapter.out`. The `laxHexagonal`
alias uses direct and one-feature `adapter..` roots for mixed adapters.

Here `{base}` is an exact substitution token, not an additional matcher.
After substitution, `*` matches exactly one package segment and `..` matches
descendants. A package such as `{base}.messaging.domain` therefore matches the
feature-local domain group without changing the architecture level.

## 1. Infrastructure-centric architecture

The system is primarily shaped by technical mechanisms such as frameworks,
transports, persistence, messaging, and external clients. Little business
behavior needs protection from those mechanisms.

Thin protocol translators, CRUD applications, configuration services, simple
integrations, and stateless orchestration can be appropriately
infrastructure-centric. Traditional layers such as controller, service, and
repository may be enough.

**Traditional layered architecture** is a technical structure, not a
guarantee of good boundaries. It is common at Level 1, but can also host a
Level 2 domain model.

**No intentional architecture** is a failure condition within this level. It
means structure emerged from framework and technology defaults without
consistent boundaries, useful documentation, or meaningful enforcement. It
does not mean the codebase has no packages or recurring structure.

## 2. Domain-oriented architecture (DDD-informed)

The system has business behavior worth protecting. Repeated rules, policies,
workflows, meaningful domain concepts, or business boundaries become explicit.
Business capabilities and shared language shape the design instead of
technical layers alone.

Domain-driven design (DDD) is a modeling approach, not a package layout. DDD
can be implemented with traditional layers. Level 2 does not imply dependency
inversion.

Level 2 costs more domain modeling, boundary decisions, shared language, and
tests. It pays off when business behavior changes often or mistakes in that
behavior are expensive.

## 3. Dependency-inverted domain architecture

Level 2 is also exposed to multiple or volatile external mechanisms, such as
transports, databases, message brokers, external clients, or frameworks.
Domain and application code become the stable center. External mechanisms
depend inward through explicit boundaries instead of driving the structure.

Common names:

- Hexagonal architecture
- Ports and adapters
- Onion architecture

These names emphasize related views of dependency inversion. They are not a
promise of rich DDD, and they are not interchangeable package layouts.

Level 3 costs ports, adapters, indirection, composition, and additional tests.
It pays off when mechanism replacement, mechanism proliferation, or
infrastructure coupling threatens domain stability or domain testing.

Level 3 does not require every inbound adapter to reach the domain through an
application port. Inbound adapters may use domain types for translation or
normalization, while remaining forbidden from depending on infrastructure
mechanisms.

## Selection guide

| Situation                                                      | Choose                                    | Reason                                                               |
|----------------------------------------------------------------|-------------------------------------------|----------------------------------------------------------------------|
| CRUD, configuration, translation, or simple orchestration      | Level 1                                   | Little domain behavior needs protection                              |
| Repeated business rules, workflows, concepts, or boundaries    | Level 2                                   | Business behavior needs an explicit model                            |
| Level 2 plus multiple or volatile external mechanisms          | Level 3                                   | Domain needs protection from mechanism change                        |
| Many integrations but little business logic                    | Level 1 + targeted ports/adapters         | Isolate volatile edges without paying for a full domain architecture |
| High scale or deployment complexity with simple business rules | Level 1 + performance/deployment patterns | Scale pressure does not create domain complexity                     |

## Cost and benefit

| Level   | Added cost                                           | Benefit                                               |
|---------|------------------------------------------------------|-------------------------------------------------------|
| Level 1 | Low upfront structure; more coupling accepted        | Fast delivery when change pressure is low             |
| Level 2 | Domain modeling, boundaries, language, and tests     | Business behavior stays understandable and consistent |
| Level 3 | Ports, adapters, indirection, composition, and tests | Domain remains stable when external mechanisms change |

Expected system lifetime and change rate affect the trade-off. A short-lived
system may not recover Level 2 or Level 3 investment. A long-lived system with
expensive business or mechanism changes is more likely to recover it.

## Examples and counterexamples

### Level 1 examples

- CRUD application with stable persistence
- Configuration service
- Stateless protocol translator
- Simple integration with little business behavior

### Level 2 example

An application implements pricing, eligibility, approval, or fulfillment
policies with interacting rules, but uses one stable technical mechanism.

### Level 3 example

The same kind of domain has HTTP and messaging entry points, persistent
storage, external clients, and a realistic chance that those mechanisms will
change independently.

### Counterexamples

- High traffic alone does not require DDD or hexagonal architecture.
- A large team alone does not require Level 2 or Level 3.
- Many integrations without meaningful domain behavior do not require Level 3;
  targeted adapters may be enough.
- DDD terminology alone does not prove domain-oriented architecture.

## Not additional levels

Modular monoliths, microservices, CQRS, event-driven design, deployment
topology, performance architecture, resilience patterns, and security
architecture are separate choices. They can be combined with any level.
