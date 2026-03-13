# AI Agent Guidelines for cBioPortal

This file contains rules and conventions that AI coding agents (Claude Code, Copilot, Cursor, etc.) must follow when contributing to this project.

## Build & Test

- Build: `mvn install -DskipTests`
- Run all tests: `mvn integration-test`
- All new development targets the `master` branch (v7)

## Endpoint Authorization (Security-Critical)

Every REST controller endpoint that accesses study-specific data **must** have a `@PreAuthorize` annotation. Forgetting this allows unauthorized data access.

### Patterns

- **GET endpoints with `studyId`:**
  ```java
  @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  ```

- **POST fetch endpoints with filters (study collection):**
  ```java
  @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  ```
  These also require the `InvolvedCancerStudyExtractorInterceptor` (in `WebAppConfig`) to handle the endpoint path.

### Exceptions (no @PreAuthorize needed)

Controllers serving only public/reference data do not need authorization. These include:
- `CancerTypeController` — public reference data
- `GeneController` — public reference data
- `GenePanelController` — public reference data
- `GenesetController` — public reference data
- `ReferenceGenomeGeneController` — public reference data
- `InfoController` — server metadata
- `ServerStatusController` — health check
- `CacheController` / `CacheStatsController` — operational
- `IndexPageController` / `LoginPageController` — UI pages
- `PublicVirtualStudiesController` — explicitly public

Any new exception must be documented here with a justification.

### Enforcement

An ArchUnit test should verify that all `@RequestMapping` methods in `@RestController` classes have `@PreAuthorize`, unless the controller is in the documented exceptions list. See the endpoint authorization architecture test for details.

## Code Conventions

- Follow existing patterns in the codebase when adding new endpoints or services
- New features should use the new persistence stack (domain/infrastructure layers), not the legacy service layer
- PRs should include test coverage for new functionality
