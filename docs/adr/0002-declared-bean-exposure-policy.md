# Declared bean exposure policy

**Status:** Accepted

## Context

Baseline architecture rules inspect Spring `@Bean` methods through their
declared return types. Concrete application types are straightforward, but an
interface return can expose either an external contract or an application
abstraction. A generic interface allowlist cannot distinguish those cases and
can accept package patterns that are broader than intended.

## Decision

Baseline bean exposure uses the declared return type:

- concrete return types pass;
- interfaces outside the application base package pass;
- application-owned interfaces annotated with `@FunctionalInterface` pass;
- application-owned interfaces fail by default;
- exact fully qualified application interface names may be allowed through
  `allowedApplicationInterfaceBeanTypes(...)`.

Application ownership uses an exact base-package boundary: the return type
package must equal `basePackage` or start with `basePackage + "."`. Allowlist
values must be exact fully qualified Java type names. Null, blank, simple,
wildcard, and package-pattern values are rejected. The rule does not inspect
method bodies or infer port or use-case semantics.

Configuration class visibility is a separate baseline policy. It is
unrestricted by default and can opt into package-private enforcement; the
`publicConfigurationClasses(...)` allowlist applies only to that opt-in policy.
`@ConfigurationProperties` visibility remains independently package-private by
default.

The allowlist is exposed by cumulative layout builders. The former
`interfaceBeanReturnTypes(...)` selector is replaced because the exception is
limited to application-owned interfaces.

## Consequences

External framework and library interfaces remain valid bean contracts.
Unlisted application-owned interfaces fail early. Consumers using the renamed
selector must update their layout builders. ADR 0001 remains unchanged because
this decision changes the bean policy without changing cumulative tier
ownership.
