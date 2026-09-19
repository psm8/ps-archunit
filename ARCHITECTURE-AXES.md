# Architecture axes

## Core distinction

Three architecture levels answer:

> How much domain structure and dependency protection does the system need?

Other choices answer different questions.

| Choice                   | Question answered                                        |
|--------------------------|----------------------------------------------------------|
| Modular monolith         | How is code modularized and deployed?                    |
| Microservices            | How is the system split across deployables?              |
| CQRS                     | How do commands and queries use models?                  |
| Event-driven design      | How do components communicate?                           |
| Deployment topology      | Where and how do runtimes execute?                       |
| Performance architecture | How does the system meet latency and throughput goals?   |
| Resilience patterns      | How does the system handle failure?                      |
| Security architecture    | How does the system protect assets and trust boundaries? |

## Modular monolith

One deployable runtime with strong internal module boundaries.

Combinations:

- Level 1: controller, service, and repository modules.
- Level 2: modules organized around business capabilities.
- Level 3: domain modules with ports and adapters.
- CQRS: separate command and query modules.
- Events: in-process domain or integration events.
- Performance: caches, batching, and read replicas.
- Resilience: transaction boundaries and retries around external calls.
- Security: module authorization and one shared trust boundary.

Often the best starting point before microservices.

## Microservices

Multiple independently deployable runtimes. Each owns some capability, data,
or operational responsibility.

Microservices do not automatically mean:

- DDD
- Good boundaries
- Hexagonal architecture
- Event-driven communication

Combinations:

- Level 1: technically split services, which can become a distributed mess.
- Level 2: services aligned with business capabilities.
- Level 3: each service protects its domain through ports and adapters.
- CQRS: separate read and write services or models.
- Events: asynchronous service communication.
- Deployment topology: containers, regions, clusters, or serverless.
- Resilience: cross-service timeouts, retries, breakers, and idempotency.
- Security: service identity, authorization, and network trust boundaries.

Microservices add operational cost. They need independent deployment, scaling,
ownership, or failure isolation to justify that cost.

## CQRS

Command Query Responsibility Segregation:

- Commands change state.
- Queries read state.
- Write and read models may differ.
- Separate services are not required.
- Event sourcing is not required.
- Events are not required.

Combinations:

- Level 1: separate handlers for complex reads and writes.
- Level 2: commands enforce domain rules; queries use optimized projections.
- Level 3: command and query sides connect through ports.
- Event-driven: events update read projections.
- Microservices: separate read and write deployables when justified.

Costs: duplicated models, synchronization, eventual consistency, and more
tests.

## Event-driven design

Components communicate by publishing and consuming events instead of relying
only on direct calls.

Event types include:

- Notification: “something happened.”
- Event-carried state: event includes data consumers need.
- Domain event: business-relevant fact.
- Event sourcing: events become the primary state history.

Event-driven design does not automatically mean DDD. Technical events can
exist in Level 1 systems.

Combinations:

- Level 1: integration events around simple processing.
- Level 2: domain events represent business facts.
- Level 3: the message broker is an adapter behind a port.
- CQRS: events update query projections.
- Microservices: events decouple service communication.

Costs: eventual consistency, ordering, duplication, replay, schema evolution,
and harder debugging.

## Deployment topology

Runtime arrangement:

- One process
- Containers
- Multiple services
- Multiple regions
- Serverless
- Edge deployment
- Active-active or active-passive

Deployment topology can combine with any architecture level:

- Level 2 modular monolith in one container.
- Level 3 modular monolith in one process.
- Level 2 microservices across regions.
- Level 1 serverless functions.

Deployment topology does not define code boundaries.

## Performance architecture

Structures used for latency, throughput, capacity, or resource efficiency:

- Caching
- Batching
- Replication
- Partitioning
- Indexing
- Asynchronous processing
- Read projections
- Connection pooling
- Load balancing

Performance architecture can combine with any level. High traffic alone does
not justify DDD or hexagonal architecture.

## Resilience patterns

Structures for handling failure:

- Timeouts
- Retry budgets
- Circuit breakers
- Bulkheads
- Rate limits
- Idempotency
- Dead-letter queues
- Outbox pattern
- Sagas
- Fallbacks
- Health checks

Microservices and event-driven systems usually require more resilience design.
Level 3 places these concerns in adapters and application boundaries. Level 1
can use them around external calls without adopting Level 3.

## Security architecture

Protection of identities, assets, data, and trust boundaries:

- Authentication
- Authorization
- Least privilege
- Encryption
- Secret management
- Network segmentation
- Audit logging
- Tenant isolation
- Threat modeling
- Secure defaults

Security is cross-cutting and applies at every level.

Hexagonal boundaries can help isolate security-sensitive adapters, but
hexagonal architecture does not provide security automatically.

## Example combinations

### Simple CRUD

- Level 1
- Modular monolith
- Single deployment
- Basic caching
- Basic resilience
- Standard security

### Complex business application

- Level 2
- Modular monolith
- Selective CQRS
- Domain events
- Performance-specific read models
- Resilience around integrations
- Strong authorization

### Complex, integration-heavy platform

- Level 3
- Modular monolith or microservices
- Ports and adapters
- CQRS where read and write needs differ
- Event-driven integration
- Strong resilience patterns
- Explicit deployment and security architecture

## Selection rule

Pick domain level from business complexity and mechanism volatility.

Pick other patterns from deployment, data flow, performance, failure, and
security pressures.
