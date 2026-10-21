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

The following controllers have known authorization gaps that are tracked as TODOs:
- `MutationCountController` — returns aggregate mutation counts across all studies with no study IDs in the request or response; `@PostFilter` is not applicable; authorization approach needs further investigation
- `MskEntityTranslationController` — accesses study-specific data but lacks `@PreAuthorize`; currently allowed in the ArchUnit test to avoid breaking the build
- `GenericAssayController` — accesses study-specific data; `@PreAuthorize` was removed for performance reasons; currently allowed in the ArchUnit test to avoid breaking the build
- `ColumnStoreGenericAssayController` — accesses study-specific data; `@PreAuthorize` was removed for performance reasons; currently allowed in the ArchUnit test to avoid breaking the build
- `ColumnStoreStudyController` — serves study data without per-study authorization; currently allowed in the ArchUnit test to avoid breaking the build

Any new exception must be documented here with a justification.

### Enforcement

An ArchUnit test (`EndpointAuthorizationArchTest`) verifies that all `@RequestMapping` methods in `@RestController` classes have `@PreAuthorize`, unless the controller or method is in a documented exceptions list. If you add a new endpoint, the test will fail unless you either:
1. Add `@PreAuthorize` to the method, OR
2. Add the controller to `AUTHORIZED_EXCEPTIONS` (or the method to `METHOD_EXCEPTIONS`) with a justification comment

## Study Filtering in Mappers (Security-Critical)

`@PreAuthorize` checks that the user may read the studies named in the request. It cannot check
what the query then reads. A query that does not restrict its results to those studies will hand a
user who is legitimately authorized for one study the rows of every other study -- authorization
passes and the data still leaks.

So a mapper that reads study-specific data must constrain `cancer_study_identifier` (or
`cancer_study_id`) to the list the caller supplied, in **every** select. Use a shared `<sql>`
fragment where the selects take the same parameter object, and repeat the condition inline where
they do not.

`ResourceDataMapperStudyFilterTest` enforces this for `ResourceDataMapper.xml`: a select there that
neither includes `ResourceTableBaseFilters` nor filters on `cancer_study_identifier IN` fails the
build. No other ClickHouse mapper is covered by a test. Nearly all of their selects carry the same
condition; the few that do not read reference data, or are scoped by sample or profile identifiers
that the request was already authorized for, rather than by study. That has not been audited.

The check is textual. It catches a select added or copied without the filter; it does not prove the
filter is reachable in every branch of a select that has one.

## Code Conventions

- Follow existing patterns in the codebase when adding new endpoints or services
- New features should use the new persistence stack (domain/infrastructure layers), not the legacy service layer
- PRs should include test coverage for new functionality
