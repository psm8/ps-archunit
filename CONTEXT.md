# ArchUnit Hexagonal Rules

Reusable architecture rules for Java applications that use ports and adapters.

## Architecture vocabulary

**Architecture level**:
One of three system-wide choices for architecture investment: infrastructure-centric, domain-oriented, or dependency-inverted domain architecture.
_Avoid_: Treating a higher level as universally better.

**System**:
An independently deployable application or runtime to which one architecture level applies.
_Avoid_: Assuming a repository or every module is a system.

**Architecture investment**:
The added modeling, boundaries, indirection, and tests used to protect behavior or change.
_Avoid_: Adding structure only to future-proof a system without concrete pressure.

**Core**:
The conceptual inside of a system, containing domain and application code.
`Core` is an umbrella term, not a package name.
_Avoid_: Using `core` as a catch-all package for configuration, filters, errors, or utilities.

**Domain layer**:
Business concepts, rules, invariants, policies, and domain events.
It expresses what the business means, not how external mechanisms work.

**Application layer**:
Use-case orchestration that coordinates domain behavior, transactions, and ports.
It belongs to the core but is distinct from the domain model.

**Application service**:
A use-case coordinator in the application layer. It is a role, not a required
package or naming convention.
_Avoid_: Assuming every application needs a `service` package.

**API layer**:
Inbound adapters and transport-facing code that invokes application use cases.
It is outside the core.

**Infrastructure layer**:
Outbound adapters, persistence, external clients, messaging, configuration,
and framework integration.
It is outside the core.

**Feature-first package layout**:
A layout that groups code by business capability first, then separates domain, application, API, and infrastructure concerns inside each feature.
_Avoid_: Treating feature-first layout as proof of domain-oriented design.

**Pragmatic domain-driven design**:
DDD-informed design that keeps business behavior explicit while allowing selected framework dependencies where their cost is justified.
_Avoid_: Treating framework independence as the definition of DDD.

**Infrastructure-centric architecture**:
An application whose structure is primarily shaped by technical mechanisms such
as frameworks, transports, persistence, messaging, and external clients rather
than by business capabilities and domain boundaries. It is appropriate for
thin protocol translators, CRUD applications, configuration services, simple
integrations, and stateless orchestration where there is little or no domain
to model.

**Traditional layered architecture**:
A technical organization such as controller, service, and repository layers.
It describes visible structure, not boundary quality or domain complexity.
It is often infrastructure-centric, but can also contain a domain-oriented
model.

**No intentional architecture**:
An informal, deliberately harsh label for an infrastructure-centric codebase
whose structure emerged from framework and technology defaults without explicit
domain boundaries, dependency rules, consistent enforcement, or useful
documentation. “No architecture” is a mocking alias, not a claim that the
codebase has no packages or recurring structure.

**Domain-oriented architecture**:
An architecture where business capabilities, shared language, domain concepts,
rules, workflows, and boundaries shape the design.
_Avoid_: Assuming domain-oriented architecture requires dependency inversion.

**Domain-driven design (DDD)**:
A modeling approach for understanding and shaping complex business domains.
DDD does not prescribe a package layout or guarantee dependency inversion.
_Avoid_: “DDD architecture” as a synonym for hexagonal architecture.

**Dependency-inverted domain architecture**:
A domain-oriented architecture where domain and application code form the
stable center and external mechanisms depend inward through explicit
boundaries.
_Avoid_: Treating dependency inversion as mandatory for every domain model.

**External mechanism**:
A technical means by which the system is invoked or provides capabilities, such
as a transport, database, message broker, framework, or external client.

**Mechanism volatility**:
The likelihood that external mechanisms will multiply, be replaced, or change
independently of domain behavior.

**Hexagonal architecture**:
The project's public name for dependency-inverted domain architecture using
ports and adapters. It emphasizes external mechanisms depending on the
application and domain core.

**Ports and adapters**:
A name for the dependency-inversion approach in which application-owned ports
define boundaries and adapters connect external mechanisms to those ports.

**Onion architecture**:
A related dependency-inversion approach organized around concentric inward
dependencies. It is the implementation model used by this project's Level 3
rules, not a claim that all hexagonal package layouts are identical.

**Driving port**:
An application capability invoked by something outside the application core.
In this project, driving port interfaces use the `UseCase` suffix.

**Driven port**:
An application-owned contract for a capability provided by something outside
the application core. In this project, driven port interfaces use the `Port`
suffix.

**Adapter**:
A boundary implementation that connects an external mechanism to a driving or
driven port.

**Architecture rule**:
An executable constraint evaluated against imported Java classes.

**Baseline**:
The Level 1 rule tier. It protects package cycles, explicit dependency bans,
configuration, bean declarations, and immutable boundary outputs.

**Domain-oriented**:
The cumulative Level 2 rule tier. It adds domain, application, API, and
infrastructure dependency direction and allows only configured model annotations
in domain classes.

**Hexagonal**:
The cumulative Level 3 rule tier. It adds onion direction, framework
isolation, ports, adapters, and component-scan rules.

**API package group**:
Inbound delivery code that invokes application capabilities. By default it
includes `basePackage.api..` and inbound adapter groups.

**Infrastructure package group**:
Outbound adapters, persistence, clients, messaging, configuration, and
framework integration. By default it includes `basePackage.infrastructure..`,
outbound adapter groups, and mixed adapter groups.

**Model-framework package group**:
Configured namespaces whose annotation types may be used as domain model
metadata. Allowing a namespace does not allow runtime or service types from
that namespace.

**Strict and lax aliases**:
Level 3 convenience factories on `HexagonalArchitectureRules`.
Both delegate to Level 3; strict keeps directional adapter defaults, while lax
uses one direction-neutral mixed adapter group.

**Tier rule classes**:
`BaselineArchitectureRules` owns Level 1, `DomainOrientedArchitectureRules`
owns cumulative Levels 1–2, and `HexagonalArchitectureRules` owns cumulative
Levels 1–3.

**Package layout**:
`PackageLayout` is an immutable vocabulary snapshot. Its builder's package
group methods replace defaults, while corresponding `add...` methods append
patterns. Configuration is discovered from annotations, not represented as a
package group.
