# Domain modeling concepts

These terms are not the same category. The list mixes architecture styles,
DDD scope, and modeling patterns.

| Term                | Meaning                                                                           | Scope                      |
|---------------------|-----------------------------------------------------------------------------------|----------------------------|
| **CRUD/procedural** | Code organized around data operations or procedures: create, read, update, delete | Basic implementation style |
| **Domain-oriented** | Code organized around business capabilities, behavior, rules, and language        | Architecture direction     |
| **DDD concepts**    | Modeling toolbox for complex business domains                                     | Umbrella term              |
| **Bounded context** | Boundary where business terms and models have one precise meaning                 | Strategic DDD              |
| **Aggregate**       | Cluster of domain objects with one consistency boundary and aggregate root        | Tactical DDD               |
| **Policy**          | Business rule deciding or constraining behavior                                   | Domain behavior            |
| **Domain event**    | Immutable business fact that already happened                                     | Domain communication       |

## Relationship

```text
CRUD/procedural
    ↓
Domain-oriented architecture
    ↓
DDD-informed modeling
    ├── Bounded contexts
    ├── Aggregates
    ├── Policies
    └── Domain events
```

This is not a strict progression. Domain-oriented code does not require every
DDD concept.

## Example

Order system:

- **CRUD**: `OrderController → OrderService → OrderRepository`
- **Domain-oriented**: code organized around ordering behavior, not only tables
- **Bounded context**: Ordering and Billing contexts may define `Order`
  differently
- **Aggregate**: `Order` root controls order lines and status changes
- **Policy**: “Discount applies only to eligible customers”
- **Domain event**: `OrderPlaced`, `PaymentCaptured`

## Important distinctions

- Bounded context ≠ microservice. One context can live inside a monolith.
- Aggregate ≠ database table. Aggregate protects business invariants.
- Policy ≠ generic service. Policy expresses business decision logic.
- Domain event ≠ technical event. `OrderPlaced` is a business fact;
  `DatabaseUpdated` is a technical fact.
- Domain event ≠ event-driven architecture. Events can stay in-process.
- DDD ≠ hexagonal architecture. DDD models business; hexagonal controls
  dependencies.
- CRUD ≠ bad. It is correct for simple domains.
- DDD concepts pay off when rules, language, boundaries, and consistency
  matter.
