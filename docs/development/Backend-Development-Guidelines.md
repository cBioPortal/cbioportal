# cBioPortal Backend Development Guidelines

All new backend development targets `master`, which is the v7 development
branch. v7 is ClickHouse-only, so new data-access work should use the current
ClickHouse repository and mapper patterns unless an issue explicitly concerns
legacy compatibility.

## Architecture

Use the package-based clean architecture stack for new features:

1. Put business models, use cases, repository interfaces, and domain utilities
   in `org.cbioportal.domain.<feature>`.
2. Put REST controllers, request/response DTOs, and API mappers in
   `org.cbioportal.application.rest`.
3. Put ClickHouse repository implementations and MyBatis mapper interfaces in
   `org.cbioportal.infrastructure.repository.clickhouse.<feature>`.
4. Put ClickHouse SQL mapper XML in
   `src/main/resources/mappers/clickhouse/<feature>`.

Do not add new feature code to the legacy stack unless the change is
specifically maintaining an existing legacy API or migration path.

## REST APIs

- Keep controllers thin. They should validate input, enforce authorization,
  select the appropriate use case, and map responses.
- Do not put business rules or SQL access in controller methods.
- Use DTOs for API responses instead of exposing persistence objects directly.
- Keep endpoint paths, projections, request filters, and response headers
  consistent with neighboring controllers.
- Every endpoint that accesses study-specific data must have a suitable
  `@PreAuthorize` annotation. See `AGENTS.md` and the
  `EndpointAuthorizationArchTest` conventions before adding or changing
  endpoints.
- If an endpoint intentionally serves only public reference data, document the
  exception in the same place as the authorization test exception.

## Domain Logic

- Put business decisions in use cases or focused domain utilities.
- Keep domain code independent of Spring MVC, MyBatis mapper interfaces, and
  API DTOs.
- Prefer immutable domain records or simple value objects when they make the
  behavior easier to test.
- Avoid duplicating logic across use cases; extract a domain utility only when
  it is shared by real behavior.

## Data Access

- Controllers should not call repositories directly. Route requests through a
  domain use case or, for legacy behavior, an existing service boundary.
- Domain packages should define repository interfaces; infrastructure packages
  should implement them.
- Keep ClickHouse-specific SQL and mapper details in the infrastructure layer.
- Return empty results for empty input collections before reaching the mapper
  when that is the established pattern for the repository.
- Follow existing mapper XML style for filters, projections, ordering, and
  result mappings.

## Legacy Code

The `org.cbioportal.legacy` packages remain part of the product. When a bug is
in a legacy path, fix it with the smallest compatible change and add tests near
the existing behavior.

When adding new behavior, avoid expanding legacy web, service, or persistence
patterns. If a clean-architecture implementation needs to interoperate with
legacy models or filters, keep the adaptation at the boundary and make the
domain use case own the business rule.

## Tests

- Add focused unit tests for new domain logic.
- Add Spring MVC tests for controller behavior, request validation, response
  mapping, and authorization-sensitive paths.
- Add ClickHouse mapper or integration tests when correctness depends on SQL,
  joins, aggregation, filtering, or ordering.
- Add API E2E coverage when the issue is about an endpoint contract or behavior
  that is difficult to validate with unit tests alone.
- Keep test data small and targeted. Broaden coverage when a change touches a
  shared mapper, shared filter, or public API.

## Configuration

- Runtime configuration belongs in `application.properties` and related
  Spring Boot configuration classes.
- Document user-facing configuration in the customization docs when behavior or
  accepted values change.
- Keep defaults aligned across `application.properties.EXAMPLE`, documentation,
  and any frontend-facing configuration exposed by the backend.

## Code Style

Follow `CODE_STYLE.md` and the formatting enforced by Spotless. Match the
surrounding code's naming, import order, test style, and use of JUnit 4 or
JUnit 5.

Use descriptive commit messages and keep commits focused. Documentation-only
changes do not need runtime tests, but the PR should state that clearly.
