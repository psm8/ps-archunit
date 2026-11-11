# Bean creation rule replaces bean placement

**Status:** Accepted

## Context

Baseline required every `@Bean` method to be declared in a class directly
annotated with `@Configuration`. It reported false positives for `@Bean`
methods in classes whose annotation only builds on `@Configuration`, such as a
`@SpringBootConfiguration` bootstrap class or an `@AutoConfiguration` class.

The concern behind it is different: beans should be created explicitly, so
that adding a module never activates anything silently. Component scanning
does the opposite. It registers every class annotated with `@Component` or
`@Service` in the scanned packages.

## Decision

Remove the bean placement check. Keep the bean exposure policy (ADR 0002).

Add a switchable bean creation rule (`beanCreationMode`,
`beanCreationExceptions`) at Baseline. It reports classes directly annotated
with:

- `@Component` or `@Service`: register the class with a `@Bean` method in a
  `@Configuration` class;
- `@ComponentScan` or `@ComponentScans`: register beans with `@Bean` methods
  or `@Import`;
- `@SpringBootApplication`: use `@SpringBootConfiguration` with
  `@EnableAutoConfiguration`.

Only direct annotations match. `@Configuration`, `@RestController`,
`@Controller`, `@ControllerAdvice`, `@Repository`, and a system's own
stereotypes are not reported, although they build on `@Component`.

Its default mode depends on the level: off at Level 1, advisory at Level 2,
and fail at Level 3. Explicit bean creation is a design investment that grows
with the architecture level.

## Considered options

- Ban every annotation that builds on `@Component`, with an allowlist.
  Rejected: a system that defines its own stereotype did so on purpose, and the
  allowlist would have to track every Spring stereotype.
- One default mode at every level. Rejected: fail at Level 1 would break most
  Spring applications on upgrade, and advisory at Level 3 would not protect a
  hexagonal system's explicit wiring.

## Consequences

Most Spring Boot applications at Level 3 use `@SpringBootApplication` and fail
once after upgrading until the bootstrap class is changed or listed in
`beanCreationExceptions`. The release notes call this out.

A system's own stereotype built on `@Component` is still found by component
scanning and is not reported.

Removing the placement check removes violations; frozen entries for them drop
out of a violation store automatically.
