# RFC: Freeze cbioportal-core and Reintegrate the Importer into cbioportal

Oct 30, 2026 (revised Nov 3, 2026) · @Angelica Ochoa

**Status:** Draft — seeking stakeholder input\
**Type:** Request for Comments\
**Repos affected:** `cbioportal-core`, `cbioportal`, `cbioportal-docker-compose`

## Summary

This RFC proposes freezing the `cbioportal-core` repository and moving its importer code (Java import classes, Python/Perl scripts and their resources) into the main `cbioportal` repository. The two repositories are tightly coupled today. The importer's tests download cbioportal's database schema from GitHub at run time. The Docker build clones `cbioportal-core` by branch name. Shared domain types are copied by hand and have already drifted apart. Contributors have to work across two codebases to follow one data flow.

After the merge, one repository would hold separate Maven modules for the web application, the importer and the types they share. It would have one build, one version and one CI setup that runs only the tests a change touches. `cbioportal-core` would be archived (read-only) once the migration is complete and downstream users have been updated.

## Hackathon Goals (In Scope)

**Done so far:** the importer code is in `importer/`, and its dependencies are aligned with the web application's (50 of the 52 version differences resolved). See [cbiohackathon-importer-drop.md](cbiohackathon-importer-drop.md).

The immediate goals for the hackathon are below. Goal 2 comes first, because the shared module it creates is where goal 1's merged types go. The importer half of goal 3 can run alongside it.

### Goal 1: Trim redundancies and consolidate the shared models

- **One copy of each shared type.** Merge the five `org.mskcc.cbio.portal.model.shared` classes that have cbioportal counterparts (see [Duplicated types](#duplicated-types)) into `shared-model`. This includes reconciling the values of `GeneticAlterationType` with `MolecularAlterationType`, and of `MolecularProfileDataType` with `MolecularProfile.DataType` / `ImportType`.
- **Duplicates within cbioportal.** `GeneticEntity` and `GenericEntityProperty` each exist in both `legacy/model` and `application/file/model`.
- **Other parallel models.** Audit the importer's `org.mskcc.cbio.portal.model` package against `legacy/model` (for example `Sample` and `CancerStudy`), and merge the ones that represent the same thing.
- **Unused dependencies and leftover files: done.** `dom4j`, `commons-text`, `opencsv`, `commons-io` and the unused `commons_cli.version` property are removed. `importer/.github/` was ported to the root workflows and removed, `importer/LICENSE` was removed, and `importer/README.md` was rewritten. `importer/Dockerfile` stays, because the ported workflow still publishes `cbioportal/cbioportal-core` from it.
- **Done when:** `org.mskcc.cbio.portal.model.shared` is empty, an ArchUnit rule keeps it that way, and both modules build.

### Goal 2: Consolidate the root POM and `importer/pom.xml`

Proposed layout for the hackathon: a reactor that doesn't move `src/`. It's a variant of Option A in [Build Structure](#build-structure-and-file-organization).

```
cbioportal/
  pom.xml            ← parent/aggregator (<packaging>pom</packaging>): Spring Boot parent,
                       all version properties, dependencyManagement, pluginManagement
  web/pom.xml        ← the web application; builds ../src in place (Spring Boot JAR)
  src/               ← unchanged
  shared-model/      ← the merged shared types (goal 1)
  importer/pom.xml   ← parent = ../pom.xml; declares no versions of its own
```

- Every version is declared once, in the root, which ends the "keep in sync with `../pom.xml`" comments.
- One ClickHouse JDBC version and classifier for both modules, and one Testcontainers ClickHouse version.
- Web-only build settings (frontend dependency, git-commit-id, Spring Boot repackage, JaCoCo/Sonar paths, Spotless includes) move to `web/pom.xml`.
- The importer must override the shade transformers it would otherwise inherit from `spring-boot-starter-parent`'s pluginManagement.
- The Dockerfile builds the importer from this repository; the `build-core` stage that clones `cbioportal-core` goes away.
- **Trade-off:** the web module's sources live outside its own folder (`../src`). That's less conventional, but it avoids the mass rename. The `backend/` move can still happen later.
- **Done when:** `mvn package` at the root builds both JARs, versions appear only in the root POM, and the Docker image builds without cloning `cbioportal-core`.

### Goal 3: Fix the test environments for each module

**Importer**

- Use the Surefire version Spring Boot manages instead of the pinned 2.21.0. Then add the JUnit Vintage engine, or migrate the 76 JUnit 4 classes to JUnit Jupiter.
- Have `IntegrationTestBase` read `clickhouse_cgds.sql` from the repository instead of downloading it from GitHub, and drop the stale `cbioportal.version` (v6.4.1) property.
- Make the Python tests (`test_scripts.sh` / `tests/`) runnable from `importer/`.
- ~~Port `maven.yml` and `validate-python.yml` into the root `.github/workflows/`~~ Done: `importer-maven.yml` and `importer-validate-python.yml`. The Python suite passes from `importer/` (192 tests). The Java suite still needs the fixes above.

**Web application**

- Confirm the backend unit and integration tests still pass after the POM restructure.
- ~~Rename `core-test.yml`~~ Done: it's now `backend-test.yml` ("Backend tests"). Update branch protection if the old check was required.
- Fix the root's internal Testcontainers mismatch (ClickHouse module 1.19.7 vs everything else at 1.21.4).

**Done when:** `mvn verify` at the root runs both modules' unit and integration tests, and CI runs them.

## Current State

| | `cbioportal` | `cbioportal-core` |
| --- | --- | --- |
| Version | 7.2.x | 1.0.16 |
| Default branch | `master` | `main` |
| Build | Single Maven module, `<packaging>jar</packaging>`, Spring Boot fat JAR | Single Maven module, `maven-shade-plugin` producing `core-*.jar` |
| Framework | Spring Boot 4.1 / Spring Framework 7 / Spring Security 7 | Plain Java with `spring-context`, `spring-jdbc` and `spring-tx` 6.2.x (not a Spring Boot app) |
| Tests | JUnit 5 | 82 Java test files, 76 of them on JUnit 4; one Python system test (`tests/system_tests_import_data.py`) |
| Test database | MySQL 5.7 service in `core-test.yml`; ClickHouse test resources in `src/test/resources/` | ClickHouse 24.3 via one shared, static Testcontainers instance (`IntegrationTestBase`) |
| CI workflows | `core-test.yml` (despite its name, it tests the web app), `integration-test.yml`, `dockerimage.yml` and others | `maven.yml`, `validate-python.yml`, `docker-image.yml` |

## Motivation

**The importer's tests depend on cbioportal through GitHub.** `IntegrationTestBase` downloads `src/test/resources/clickhouse_cgds.sql` from `raw.githubusercontent.com/cBioPortal/cbioportal/<version>/…`, where the version comes from a `cbioportal.version` property pinned in the core POM. So every importer test run needs GitHub to be reachable and a version pin to be kept current by hand. A schema change in cbioportal reaches the importer's tests only when someone bumps that pin. In one repository, the importer's tests would read the schema from the same commit.

**The Docker build couples the two repos.** `docker/web-and-data/Dockerfile` has a `build-core` stage that clones `cbioportal-core` at a `CORE_BRANCH` build argument (default `main`). It busts the Docker cache with `ADD https://api.github.com/repos/cBioPortal/cbioportal-core/commits/${CORE_BRANCH}`. Every image build therefore depends on the GitHub API being available, and the image can pair any cbioportal commit with whatever the core branch holds at build time. In one repository, both JARs would come from the same commit.

**The shared types are copied by hand and have already drifted.** Five of the seven classes in core's `model/shared` package have counterparts in cbioportal (see [Shared Code](#shared-code)). They're kept in sync by memory alone. `GeneticAlterationType.java` says it is copied to `org.cbioportal.legacy.model.MolecularProfile.GeneticAlterationType` and warns the reader to update both. That type is now named `MolecularAlterationType`, so even the reminder is out of date. Two of these types are also duplicated within cbioportal itself, in `legacy/model` and `application/file/model`.

**Mismatches only show up at run time.** The importer writes `genetic_alteration_type` and `datatype` values, and the web API reads them back. Both are stored as strings, so a difference between the two enums causes errors or empty results only once real data flows through, never at compile time. With one shared definition, the compiler catches it.

**Contributors have to work across two repos.** To follow a study from upload to API response today, a contributor clones two repos, learns two builds and traces calls across a JAR boundary. After the merge, it's one clone and one build, and an IDE can follow calls across modules.

**Releases need coordinating across two version lines.** Core (1.0.x) and cbioportal (7.2.x) are tagged separately. The Docker image picks up core by branch, not by version, so no release records which importer version shipped in it. One version tag would fix that.

## Build Structure and File Organization

A Maven project that declares modules must use `pom` packaging, so the root `pom.xml` can't stay the web application's JAR *and* act as the reactor. There are two workable layouts.

**Option A (recommended): a full reactor.** The root becomes an aggregator POM, and the web application moves into its own module.

```
cbioportal/
  pom.xml                         ← aggregator/parent POM (<packaging>pom</packaging>)
  backend/                        ← existing web app, moved from src/ (name TBD)
    pom.xml                       ← Spring Boot fat JAR, as today
    src/
  shared-model/                   ← types used by both backend and importer
    pom.xml                       ← plain JAR, no Spring Boot
    src/main/java/org/cbioportal/...
  importer/                       ← moved from cbioportal-core
    pom.xml                       ← maven-shade-plugin, produces core-*.jar
    src/main/java/org/mskcc/cbio/portal/...   (package name TBD)
    src/main/resources/
    src/test/java/
    scripts/                      ← importer/, clickhouse_import_support/, Perl scripts
    tests/                        ← system_tests_import_data.py
    requirements.txt
  docker/web-and-data/Dockerfile  ← single build stage; no longer clones cbioportal-core
```

- `mvn package` from the root builds both deployable JARs.
- Both the backend and the importer depend on `shared-model`. The importer must **not** depend on the backend module: it's a repackaged Spring Boot fat JAR and would pull the whole web application into the importer.
- The cost is moving `src/` into `backend/`. That one large, mechanical PR touches every path, so it breaks open PRs and anything that hardcodes paths (Sonar, the Dockerfile's `COPY` lines, workflows, docs). Doing it with `git mv` in a single commit keeps `git log --follow` and blame working.

**Option B (fallback): no reactor.** The root stays the backend JAR. `shared-model/` and `importer/` are standalone Maven projects in the same repository, built in order by a script or CI step (`mvn -f shared-model install`, then the root, then `mvn -f importer package`). This avoids moving `src/`, but gives up the single build command, needs build ordering, and means an IDE imports three projects.

**Note on history.** Earlier versions of cbioportal were a multi-module Maven project (`core/`, `model/`, `persistence/`, `service/`, `web/`) before being consolidated into one module. Option A is deliberately smaller: three modules, split by deployable artifact rather than by architectural layer. Reviewers who remember the earlier layout should weigh in on whether it brought problems this plan should avoid.

**Packaging.** The importer keeps `maven-shade-plugin` and keeps producing a self-contained `core-*.jar`. It doesn't need Spring Boot. The Docker image keeps installing it at `/core/core-IMPORTER.jar`, because `cbioportal-docker-compose/entrypoint.sh` uses that path.

## Dependency Alignment

The two repos are on different framework generations. Core uses Spring 6.2.x and mostly JUnit 4. cbioportal just moved to Spring Boot 4.1 / Spring Framework 7. If the importer inherits the parent POM's dependency management, its dependencies will be upgraded under it.

**Status: done as part of the drop.** `importer/pom.xml` now uses the same Spring Boot 4.1.0 BOM and version overrides as the root, and the importer compiles against Spring 7 with no source changes. Two differences remain open: the ClickHouse JDBC driver and the Testcontainers ClickHouse module. The JUnit 4 tests still need a Vintage engine or a migration; that's part of hackathon goal 3. Details are in [cbiohackathon-importer-drop.md](cbiohackathon-importer-drop.md).

## Shared Code

### Duplicated types

| `cbioportal-core` (`org.mskcc.cbio.portal.model.shared`) | `cbioportal` counterpart | Notes |
| --- | --- | --- |
| `GeneticAlterationType` | `legacy/model/MolecularProfile.MolecularAlterationType` | Renamed in cbioportal; the "copied to" comment in core is out of date |
| `MolecularProfileDataType` | None as a class; closest are `MolecularProfile.DataType` / `ImportType` | Values need to be reconciled, not just deduplicated |
| `EntityType` | `legacy/model/EntityType` | |
| `GenericAssayMeta` | `legacy/model/meta/GenericAssayMeta` | |
| `GenericEntityProperty` | `legacy/model/GenericEntityProperty`, `application/file/model/GenericEntityProperty` | Also duplicated within cbioportal |
| `GeneticEntity` | `legacy/model/GeneticEntity`, `application/file/model/GeneticEntity` | Also duplicated within cbioportal |
| `ResourceType` | `legacy/model/ResourceType` | |

Other models, such as `Sample` and `CancerStudy`, are reported to differ in field names and constructors. Phase 4 should check the full list.

The single definition of each shared type goes in the new `shared-model` module, in an `org.cbioportal` package. It doesn't go in `legacy/model`: AGENTS.md directs new work to the domain/infrastructure layers rather than the legacy stack.

### Schema

cbioportal has no Liquibase migrations. The ClickHouse schema is plain SQL:

- `src/main/resources/db-scripts/clickhouse/init/schema.sql` and `migrate/migrate_schema.sql` set up and upgrade deployments.
- `src/test/resources/clickhouse_cgds.sql` (and related files) is what the tests load, including the importer's tests through the download described above.

As part of this work, decide which file is the source of truth and how the test schema stays in sync with it. After the move, the importer's tests should read the schema from the repository, not from GitHub.

### Standards for shared code

These apply to any type, constant or resource that both the importer and the backend use:

1. **One definition.** A shared type lives in exactly one file, in `shared-model`. Enforce this with ArchUnit rules that check something concrete: no classes left in `org.mskcc.cbio.portal.model.shared`, and no dependency from `importer` on backend packages. Copies that a rule can't detect are caught in code review.
2. **Explicit dependencies.** Modules use shared code only through a declared Maven dependency on `shared-model`.
3. **Backward compatibility.** Changes to shared types follow a deprecation cycle: mark the old form `@Deprecated` and keep it for at least one release, unless the change is part of a major version bump.
4. **Importer-only code stays in the importer.** Write-path DAO helpers, MAF parsing models and similar code belong in `importer/` and aren't visible to the backend.
5. **The schema has one source of truth**, chosen under [Schema](#schema). The importer doesn't keep or download a copy of its own.

## Test Organization and CI

### Test suites

| Suite | Location after merge | Notes |
| --- | --- | --- |
| Importer Java tests | `importer/src/test/java` | 82 files; integration tests use one shared static ClickHouse Testcontainers instance |
| Importer Python tests | `importer/tests/`, plus the `validate-python.yml` checks | Validator and import-script tests |
| Backend tests | `backend/src/test` (Option A) or `src/test` (Option B) | Unit and integration tests |
| End-to-end tests | Not in either repo today | The e2e step in `core-test.yml` is commented out; out of scope for this RFC |

### Workflows

- Done: core's `maven.yml`, `validate-python.yml` and `docker-image.yml` are now `importer-maven.yml`, `importer-validate-python.yml` and `importer-docker-image.yml` in the root, each limited to `importer/**`.
- Done: cbioportal's `core-test.yml` is now `backend-test.yml`.

### Path-based triggers

| Change touches | Suites that run |
| --- | --- |
| `importer/**` | Importer Java and Python tests, Docker image build |
| `backend/**` (or `src/**`) | Backend tests, Docker image build |
| `shared-model/**`, root `pom.xml`, schema files | All suites |
| `docker/**` | Docker image build, plus an importer smoke test inside the image |

GitHub has a catch here: if a required status check comes from a workflow that was skipped by a path filter, the check never reports and the PR can't merge. Use a gate job that always runs and passes or fails based on whether the relevant suites ran and succeeded, and make that the required check.

### Reducing importer test time

The integration tests already share one ClickHouse container across the suite, so the remaining options are:

1. Run test classes in parallel (Surefire `forkCount` or JUnit 5 parallel execution) once the JUnit 5 upgrade is done.
2. Split fast unit tests from the container-based integration tests (Surefire vs Failsafe, or a profile), so `mvn test` stays fast.
3. Cache the ClickHouse image in CI.

## Migration Mechanics

- **Git history: not carried over (decided).** The importer is copied in as a snapshot of `cbioportal-core@6146cdb`. `git log` and blame for `importer/` start at the drop commit; earlier history stays in the archived `cbioportal-core` repository.
- **Open PRs and issues.** Before the freeze, merge or close open `cbioportal-core` PRs, and use GitHub's issue transfer to move open issues to `cbioportal`.
- **Freeze, then archive.** Announce a freeze date. After it, `cbioportal-core` accepts only critical fixes, which are also applied in `cbioportal/importer`, until the archive phase.
- **Rollback.** Until the archive phase, `cbioportal-core` keeps building and publishing. If the migration stalls, the Dockerfile can go back to the `build-core` stage.
- **Maven coordinates.** Decide what happens to the `core` artifact and its 1.0.x version line: keep publishing it under the new version, or retire it. Integrators who depend on it need notice.

## Downstream Updates

| Consumer | Dependency on cbioportal-core | Required update |
| --- | --- | --- |
| `cbioportal` Docker image | `build-core` stage clones core at `CORE_BRANCH` | Remove the stage; copy the importer JAR, scripts and `requirements.txt` from the main build. Keep the `/core/...` install paths. |
| `cbioportal-docker-compose` | `entrypoint.sh` runs `/core/core-IMPORTER.jar`; no `CORE_BRANCH` reference found | None if the image keeps its paths; test the new image end-to-end |
| `validateData.py` users | Scripts come from core | Same scripts, shipped from the cbioportal image; update docs that link to the core repo |
| Institutional pipelines and forks | Some may build core themselves or depend on its Maven artifact or packages | Survey before the freeze; provide a migration note |
| Documentation (`docs.cbioportal.org`) | Import how-tos link to the core repo and its script paths | Update repository links and script paths |
| `io.github.cbioportal:maf` | Used by core for MAF parsing | Check it resolves from the merged build's repositories |

## Proposed Timeline

Durations are rough and assume 2–3 engineers. Each phase must finish before the next starts.

| Phase | Description | Est. duration | Exit criteria |
| --- | --- | --- | --- |
| 0: RFC | Circulate; decide layout (A/B), module names, package name, CI rules | 2 weeks | RFC approved by maintainers |
| 1: Importer dependency upgrade | In `cbioportal-core`: Spring 7, JUnit 5 (or Vintage), match the parent's library versions | 1–2 weeks | Core CI passes on the upgraded stack |
| 2: Build restructure | Aggregator POM; empty `shared-model`; under Option A, move `src/` to `backend/` | 1 week | `mvn package` from root produces the backend JAR; backend tests pass |
| 3: Importer move | Copy core into `importer/` (no history); move workflows; importer tests read the schema from the repo | 1–2 weeks | Importer Java and Python tests pass in cbioportal CI; no GitHub schema download |
| 4: Shared type consolidation | Move the duplicated types into `shared-model`; reconcile the enums; add ArchUnit rules | 1–2 weeks | `org.mskcc.cbio.portal.model.shared` is empty; ArchUnit rules pass |
| 5: Docker and CI | Remove the `build-core` stage; path filters; required gate job | 1 week | Image builds from one commit; importer suites run only on relevant changes |
| 6: Downstream and docs | Docker Compose test, documentation, community notice | 1–2 weeks | Docker Compose deployment tested end-to-end with the new image |
| 7: Archive `cbioportal-core` | Archive notice in README; archive the repo on GitHub | 1 day | Repo archived; README points to `cbioportal/importer/` |

**Total estimate:** 9–14 weeks from RFC approval to archive.

## Future Goals (Out of Scope)

These are goals for after the reintegration. They are out of scope for the hackathon and for the phases above.

### Harmonize logging

The web application and the importer write logs in different ways:

| | Web application | Importer (Java) | Importer scripts (Python) |
| --- | --- | --- | --- |
| Logging API | SLF4J through Spring Boot (`spring-boot-starter-logging`) | SLF4J in 13 classes; 51 classes report through the custom `ProgressMonitor`; 17 classes print directly with `System.out` / `System.err` | Python `logging` module (7 files) |
| Backend | Logback | None in the shaded JAR (logback is test-only), so SLF4J falls back to no-op and those messages are lost | Python handlers |
| Configuration | `logging.*` in `application.properties`; `logback.xml.EXAMPLE`, `log4j.properties.EXAMPLE` and `logging.properties` templates in `src/main/resources` | `logback.xml` in test resources only | Per-script |

The goal is one approach for both: the same API and backend (SLF4J and Logback), one configuration format, consistent log levels and message format, and output that a deployment can collect the same way from both the web app and the importer. That would likely involve:

- Shipping a logging backend in the importer JAR, so its SLF4J messages are actually written.
- Replacing direct `System.out` / `System.err` calls with a logger, and deciding whether `ProgressMonitor` becomes a thin layer over SLF4J or stays a separate user-facing progress channel.
- Deciding how the Python wrappers (`metaImport.py`, `cbioportalImporter.py`, `validateData.py`) and the Java importer they call share log levels and output format.
- Retiring the logging config templates that are no longer used.

## Open Questions

Please comment on this document or open a GitHub discussion thread.

**Layout and naming**

- [ ] Option A (full reactor, move `src/` to `backend/`) or Option B (no reactor)? If A, when should the move land to disrupt the fewest open PRs?
- [ ] Module names: `backend/`, `shared-model/`, `importer/`? (Alternatives: `web/`, `model/`, `data-import/`.)
- [ ] Keep the Java package `org.mskcc.cbio.portal` or rename it to `org.cbioportal.importer`? Renaming is clearer but adds churn for external integrators.
- [ ] Keep the Python/Perl scripts under `importer/scripts/` or move them to the top-level `scripts/`?

**Build and release**

- [ ] Upgrade the importer to the parent's dependency versions (recommended), or allow overrides?
- [ ] Should the importer's version always match the parent version?
- [ ] What happens to the `core` Maven artifact and the 1.0.x version line?

**Tests and CI**

- [ ] What is the current wall-clock time of the importer test suite, and is there a target for PR CI time?
- [ ] When only `shared-model` or the schema changes, which suites must pass before merge?
- [ ] Which schema file is the source of truth for both production and tests?

**Shared code**

- [ ] Who owns reconciling `GeneticAlterationType` / `MolecularAlterationType` and `MolecularProfileDataType` / `DataType`? Deprecation cycle, or a breaking change in a major version?

**Downstream and community**

- [ ] Who owns the `cbioportal-docker-compose` update, and how does it line up with that repo's releases?
- [ ] Which institutional deployments or forks build `cbioportal-core` themselves or depend on its packages? Is a public survey needed?
- [ ] How will the freeze be announced (blog post, GitHub discussion, release notes), and what is the freeze date?

## Changes from the Previous Draft

- Removed the duplicated section headings.
- Added the GitHub schema download in the importer's tests as a motivation.
- Replaced `<module>.</module>` with two workable layouts, and added the `shared-model` module so the importer doesn't depend on the backend's fat JAR.
- Added the dependency alignment phase (Spring 6.2 → 7, JUnit 4 → 5).
- Corrected the shared-types table (five counterparts, not seven; `MolecularAlterationType`; duplicates within cbioportal).
- Removed the Liquibase references; described the actual ClickHouse SQL schema files.
- Corrected the importer packaging (`maven-shade-plugin`, not Spring Boot) and kept the `/core/core-IMPORTER.jar` path that Docker Compose relies on.
- Corrected the test inventory: there's no `e2e/` directory, the Testcontainers instance is already shared, and the branch is `master`.
- Added migration mechanics (git history, open PRs and issues, rollback, Maven coordinates) and the GitHub path-filter catch with required status checks.
- Combined the stakeholder questions into one list and dropped the ones the codebase already answers.
