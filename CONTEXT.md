# ArchUnit Hexagonal Rules

Reusable architecture rules for Java applications that use ports and adapters.

## Architecture vocabulary

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

**Minimal profile**:
The low-friction rule set based on ArchUnit's built-in onion architecture.

**Standard profile**:
The minimal profile plus framework isolation, port conventions, cycle checks,
and outbound adapter checks.
