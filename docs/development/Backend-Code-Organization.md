# cBioPortal Backend Code Organization

The cBioPortal backend is a single-module Spring Boot application built with
Maven. The current codebase uses package-based boundaries instead of the older
multi-module layout.

New backend work should follow the clean architecture structure under
`src/main/java/org/cbioportal`. Legacy packages are still active for existing
APIs and migration work, but new feature development should prefer the
application/domain/infrastructure stack.

## Runtime Stack

The backend connects to ClickHouse and serves the REST API used by
`cbioportal-frontend`. The frontend is maintained in the
[cbioportal-frontend](https://github.com/cBioPortal/cbioportal-frontend)
repository and is packaged into the backend build as a Maven dependency.

The session service is a separate application used for saved sessions, virtual
studies, groups, and related user state. cBioPortal proxies session-service
requests from the backend so the frontend does not need to call the session
service directly.

## Main Packages

### `org.cbioportal.application`

The application layer contains Spring MVC controllers, request and response
DTOs, API mappers, endpoint security helpers, file/export code, and proxy code.

Typical responsibilities:

- Accept HTTP requests and validate request shape.
- Enforce authorization with `@PreAuthorize` on endpoints that access
  study-specific data.
- Convert between API DTOs and domain objects.
- Delegate business behavior to domain use cases or legacy services.

Controllers should not contain database queries or substantial business logic.

### `org.cbioportal.domain`

The domain layer contains feature-oriented business objects, repository
interfaces, use cases, and pure domain utilities. Packages are organized by
functional area, for example:

- `alteration`
- `cancerstudy`
- `clinical_attributes`
- `clinical_data`
- `clinical_data_enrichment`
- `clinical_event`
- `coexpression`
- `generic_assay`
- `genomic_data`
- `mutation`
- `patient`
- `sample`
- `studyview`
- `treatment`

Domain use cases coordinate business rules and call repository interfaces. They
should not depend on ClickHouse mapper classes, Spring MVC controllers, or API
DTOs.

### `org.cbioportal.infrastructure`

The infrastructure layer contains technical implementations for domain
interfaces and external systems. Most data access code lives under
`infrastructure.repository.clickhouse`.

Typical responsibilities:

- Implement domain repository interfaces.
- Declare MyBatis mapper interfaces.
- Keep ClickHouse-specific details out of controllers and use cases.
- Hold infrastructure configuration and service adapters.

The matching ClickHouse SQL mapper XML files live in
`src/main/resources/mappers/clickhouse`.

### `org.cbioportal.legacy`

The legacy layer contains the older model, persistence, service, web, security,
configuration, and utility code that still supports existing behavior.

This code is not dead code. Many endpoints and compatibility paths still depend
on it. When changing legacy behavior, keep the change local and preserve the
existing public API unless the issue or migration explicitly requires a
contract change.

For new features, prefer the clean architecture stack. If a feature must touch
legacy code, keep the boundary clear and avoid expanding legacy patterns into
new code.

### `org.cbioportal.shared`

The shared package contains cross-cutting enums and small utilities that are
used by more than one layer. Keep this package narrow; feature-specific logic
usually belongs in the relevant domain package.

## Resources

Important runtime resources are under `src/main/resources`:

- `application.properties.EXAMPLE` contains example Spring Boot configuration.
- `mappers/clickhouse` contains current ClickHouse MyBatis SQL mappers.
- `mappers/export` contains export SQL mappers.
- `db-scripts/clickhouse` contains ClickHouse schema and migration resources.
- `templates` and `webapp` contain server-rendered and packaged web assets.

Older mapper locations under `src/main/resources/org/cbioportal` still support
legacy code paths.

## Tests

The repository uses separate test layers:

- `src/test/java` contains unit and focused Spring MVC tests.
- `src/integration/java` contains integration tests that need database-backed
  Spring components.
- `src/e2e/js` contains API E2E tests that call a running cBioPortal instance.

ClickHouse mapper tests commonly use Testcontainers fixtures from
`src/test/resources`.

## Adding a New Backend Feature

A typical clean-architecture feature has this shape:

1. Add or reuse domain model classes and repository interfaces in
   `org.cbioportal.domain.<feature>`.
2. Implement business behavior in a use case in the same domain package.
3. Implement repository interfaces in
   `org.cbioportal.infrastructure.repository.clickhouse.<feature>`.
4. Add or update MyBatis mapper XML under
   `src/main/resources/mappers/clickhouse/<feature>`.
5. Add REST DTOs, MapStruct mappers, and controller methods under
   `org.cbioportal.application.rest`.
6. Add unit tests for use cases and controller behavior. Add mapper,
   integration, or E2E coverage when the behavior depends on SQL or the HTTP
   contract.

## Dependency Direction

Keep dependencies flowing inward:

- Controllers depend on DTO mappers and domain use cases.
- Domain use cases depend on domain repository interfaces.
- Infrastructure implements domain repository interfaces and owns SQL details.
- Legacy code can be adapted at the boundary, but new domain logic should not
  depend on legacy web controllers.

This direction keeps business behavior testable and lets ClickHouse-specific
queries evolve without leaking persistence details into the REST layer.
