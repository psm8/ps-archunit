# Narrow composition-root exemptions

**Status:** Accepted

## Context

Spring applications commonly have a bootstrap class that assembles framework
objects, adapters, and application services. A strict hexagonal rule set sees
those assembly dependencies as ordinary application dependencies, even though
the composition root is the intended boundary between the application and its
external mechanisms.

The library must support Spring and Spring Boot composition roots without
adding a Spring runtime dependency or allowing framework coupling to spread
through domain and application code.

## Decision

Recognize a composition root when a class is directly or recursively
meta-annotated with one of these annotation names:

- `org.springframework.context.annotation.Configuration`;
- `org.springframework.boot.SpringBootConfiguration`;
- `org.springframework.boot.autoconfigure.SpringBootApplication`.

The implementation matches annotation names from ArchUnit metadata. It does
not compile against or load Spring.

The root exemption is source-local. A recognized root:

- is excluded from Level 2/3 classification completeness;
- may have outgoing dependencies that bypass domain direction and onion
  direction;
- may have outgoing dependencies ignored by package-cycle checks;
- may assemble framework objects without failing framework isolation.

Referenced targets remain subject to their own rules. The exemption is not
transitive through an application service, domain object, adapter, or other
ordinary class.

The exemption does not apply to explicit dependency bans, bean placement or
declared bean exposure, configuration visibility, `proxyBeanMethods`,
transaction placement, port signatures, or adapter rules. Direct
`@Configuration` policy checks therefore remain independent from composition
root recognition.

## Consequences

Standard Spring Boot bootstrap forms can wire adapters and framework objects
without false positives from dependency-direction, onion, cycle, or framework
isolation checks. The architecture still reports framework leakage from
ordinary core classes and still enforces explicit policy rules at the root.

The matching remains intentionally name-based and framework-free. Consumers
using another bootstrap annotation must configure a documented dependency
exception or use a supported annotation meta-structure.
