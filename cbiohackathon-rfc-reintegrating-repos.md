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

- **One copy of each shared type: done (Nov 4, 2026).** The new `shared-model` module (`io.github.cbioportal:shared-model`, package `org.cbioportal.shared.model`) holds the only definitions of `EntityType`, `ResourceType`, `GenericAssayMeta`, `GenericEntityProperty`, `GeneticEntity`, `MolecularAlterationType` and `MolecularProfileDataType`, plus, after the audit below, `User`, `UserAuthorities`, `ReferenceGenome` and `CancerStudyTags`. Both the web module and the importer depend on it.
  - **Naming (decided Nov 4):**
    - The new package keeps these types out of `legacy/model`, as AGENTS.md asks.
    - The alteration enum takes the API field's name, `MolecularAlterationType`, so the importer's `GeneticAlterationType` was renamed (whole word only: methods such as `getGeneticAlterationType()` are unchanged).
    - `MolecularProfileDataType` stays a class of `String` constants, because the `datatype` column holds many values neither side models. The web app's one-value `MolecularProfile.DataType` and `ImportType` enums were removed.
  - **Values unchanged:** the enum values were already identical on both sides, so nothing stored in the database or returned by the API changes.
  - **Changes:**
    - 61 web files and 34 importer files now import from `org.cbioportal.shared.model`, and both MyBatis `GenericAssayMeta` mappers use the new class name.
    - `MolecularProfile` lost its nested enums.
    - The old copies, including `importer/.../model/shared/`, are deleted.
  - **Guard:** `SharedModelDuplicationTest` (web tests) fails if the web or importer sources declare any of these type names again, including as nested types, apart from the two file-export classes. It reads source files, like `ResourceDataMapperStudyFilterTest`, because the web tests run before the importer is compiled. A copy under a new name isn't caught.
  - **After deploy:** cached Redis values that contain a `MolecularProfile` were serialized with the old nested enum class, so they won't deserialize. `CustomRedisCache` logs that and reloads the value, so expect a burst of cache misses rather than errors, or flush the cache on deploy.
- **Duplicates within cbioportal: done.** The `legacy/model` copies of `GeneticEntity` and `GenericEntityProperty` were unused, and have been deleted. The `application/file/model` classes with the same names aren't copies: they're smaller, export-specific classes used by the file-export MyBatis mapper, so they stay.
- **Other parallel models: audited (Nov 4, 2026); four merged.** 21 of the importer's 36 model classes share a name with a web class. A field-by-field comparison found four real duplicates, which are now in `shared-model` (web version kept, with these fixes):

  | Class | Difference found | Result |
  | --- | --- | --- |
  | `User` | Same fields. The web `equals` compared strings with `==` (identity, a bug) | Merged; `equals` uses `Objects.equals`, as the importer's did |
  | `UserAuthorities` | Same fields; web `List` vs importer `Collection`, plus a no-arg constructor on the web side | Merged; the importer already passed `List`s |
  | `ReferenceGenome` | Same fields. The web `equals` threw on a null field | Merged; `equals` is null-safe, as the importer's was |
  | `CancerStudyTags` | The web class was a superset (`Integer` id plus `studyId`) | Merged |

  These classes moved to `org.cbioportal.shared.model`. The `SecurityMapper.xml` and `StudyMapper.xml` result types were updated, and `SharedModelDuplicationTest` now covers them too.

  The other 17 share a name only. They are not copies:
  - **Read side vs write side.** The importer's classes are mutable objects for writing, keyed by internal ids. The web side has read models, export row classes, and records in `domain/` (`Sample`, `Patient`, `ClinicalData`, `ClinicalAttribute`, `TypeOfCancer`).
  - **Low overlap.** Most pairs share well under half their fields, and often with different types. Examples: `GenePanel` holds `Set<CanonicalGene>` vs `List<GenePanelToGene>`; `ClinicalEvent` a `Map` of event data vs a list of attributes; `SampleList`, `Geneset`, `Gistic` and `MutSig` use different ids and fields.
  - **Closest candidate.** `StructuralVariant` comes closest (44 of 52 field names shared), but the two sides use different id fields and types (`geneticProfileId` and internal ids vs `molecularProfileId`/`patientId`/`studyId`). Merging it would mean designing one model for both reading and writing, not removing a copy.
  - **Recommendation:** keep the 17 as separate read and write models.
- **Unused dependencies and leftover files: done.** `dom4j`, `commons-text`, `opencsv`, `commons-io` and the unused `commons_cli.version` property are removed. `importer/.github/` was ported to the root workflows and removed, `importer/LICENSE` was removed, and `importer/README.md` was rewritten. `importer/Dockerfile` stays, because the ported workflow still publishes `cbioportal/cbioportal-core` from it.
- **Done when:** `org.mskcc.cbio.portal.model.shared` is empty, a check keeps it that way, and both modules build. **Met (Nov 4)** for the shared types. The other parallel models are a separate, larger item.

### Goal 2: Consolidate the root POM and `importer/pom.xml`

**Status: done (Nov 4, 2026), including the Docker image.** The layout is a reactor that doesn't move `src/`, a variant of Option A in [Build Structure](#build-structure-and-file-organization).

```
cbioportal/
  pom.xml            ← io.github.cbioportal:cbioportal-parent (<packaging>pom</packaging>), parent
                       spring-boot-starter-parent 4.1.0: every version property, dependencyManagement,
                       Spotless and the pre-commit hook (root only), test-skip profiles,
                       Maven Central and third-party-report profiles
  web/pom.xml        ← io.github.cbioportal:cbioportal, the web application. Builds ../src in place
                       into the root's target/, so target/cbioportal-exec.jar keeps its path
  src/               ← unchanged
  importer/pom.xml   ← org.mskcc.cbio:core, parent = ../pom.xml, declares no versions
  shared-model/      ← io.github.cbioportal:shared-model, the 11 shared types (goal 1)
```

**What was decided:**
- **Artifact coordinates (Nov 3):** the web module keeps `io.github.cbioportal:cbioportal`, so consumers of the published web artifact on Maven Central and JitPack see no change. The root becomes `io.github.cbioportal:cbioportal-parent`. The parent POM, and later `shared-model`, must also be published, because the published web artifact references them. Whether the importer is published is still open; for now the publish workflow deploys only `-pl web -am`.
- **Importer version (Nov 3):** the importer inherits the parent's version, so it builds `importer/core-v7.2.5-SNAPSHOT.jar`. The `core-*.jar` name is kept, so the Docker and Docker Compose paths still work. `mvn versions:set` updates every module at once.
- **ClickHouse JDBC driver (Nov 3):** 0.6.2 with the `http` classifier is one entry in the parent's `dependencyManagement`. 0.6.4 was tried and reverted, because it breaks 21 ClickHouse mapper tests (see [Upgrade the ClickHouse JDBC driver](#upgrade-the-clickhouse-jdbc-driver)).
- **Testcontainers ClickHouse:** aligned to 1.21.4 through `testcontainers-bom`. The separate 1.19.7 pin is gone.

**How the web module keeps working from `web/`:**
- `sourceDirectory`, `testSourceDirectory`, the resource filter files, `resources`/`testResources` and the build-helper source folders all point at `../src`.
- The build directory is `../target`.
- `git-commit-id` reads `../.git`.
- Surefire and Failsafe run with the repository root as their working directory, because some tests (for example `SchemaParityTest` and `DtoOpenApiSchemaTest`) read files by paths relative to it.

**How the importer handles what it inherits from `spring-boot-starter-parent`:**
- Its shade execution is named `default`, so it merges with the parent's instead of shading twice.
- Its transformers use `combine.self="override"`, so Spring Boot's transformers aren't added.
- `start-class` is set to empty, so the JAR manifest has no `Main-Class`.

**Verified:**
- `mvn package` at the root builds both JARs.
- The web module resolves exactly the same dependencies as before, apart from the intended Testcontainers ClickHouse change. The importer's are unchanged.
- The web boot JAR matches a clean build of the previous commit: all 1,315 classes are byte-identical, and the libraries and resources match. (Two git-ignored local files in `src/main/resources`, `log4j.properties` and `portal.properties`, also end up in the JAR. That was already true before.)
- Web suite: 1,580 tests, 0 failures, 0 errors, 5 skipped (UTC, Docker).
- `mvn spotless:check` still checks the root `src/` and still catches violations.

**Other files updated:**
- `docker/web-and-data/Dockerfile`: copies the module POMs and builds every module (see Docker below).
- `backend-test.yml`, `integration-test.yml` and `sonarcloud.yml`: run with `-pl web -am`, because the importer's tests don't pass yet.
- `maven-package-build.yml`: deploys `-pl web -am`.
- `third-party-report-check.yml` and `scripts/generate-third-party-report.sh`: also cover `web/pom.xml`, and run with `-pl web -am`.
- `importer-maven.yml`: its cache key includes the root `pom.xml`.
- `AGENTS.md`: the build and test commands.

**Remaining:**
- **Docker: done (Nov 4).** `docker/web-and-data/Dockerfile` builds every module from this repository.
  - **Removed:** the `build-core` stage, the `cbioportal-core` clone, the GitHub API call and `CORE_BRANCH`.
  - **What the image gets from `importer/`:** the JAR (as `/core/core-IMPORTER.jar`), `scripts/` (as `/core/scripts`) and `requirements.txt`, so the `/core` paths Docker Compose uses are unchanged.
  - **Caching:** the dependency layer copies all four POMs.
  - **`.dockerignore`:** leaves out `importer/tests` (9.6 MB of Python test data) and locally built importer JARs.
  - **Checked with a local build:** the image builds; inside it, `ImportTypesOfCancers` and `metaImport.py -h` run, and the web app's start class is in place. Not yet checked end to end with Docker Compose.
- **Trade-off:** the web module's sources live outside its own folder (`../src`). That's less conventional, but it avoided the mass rename. Moving `src/` to `backend/` can still happen later. The IDE check was skipped; if an IDE has trouble importing the project, that move is the fix.

### Goal 3: Fix the test environments for each module

**Importer**

- ~~Use the Surefire version Spring Boot manages instead of the pinned 2.21.0. Then add the JUnit Vintage engine, or migrate the 76 JUnit 4 classes to JUnit Jupiter.~~ Done (Nov 5): Surefire 3.5.6 and `junit-vintage-engine` 6.0.3. The JUnit 4 tests run unchanged on the JUnit Platform; migrating them to Jupiter is optional.
- ~~Have `IntegrationTestBase` read `clickhouse_cgds.sql` from the repository instead of downloading it from GitHub, and drop the stale `cbioportal.version` (v6.4.1) property.~~ Done (Nov 5). The download was already commented out; the dead code and the property are now removed. The tests read the importer's own copy, `importer/src/test/resources/clickhouse_cgds.sql`, which has drifted from production's `schema.sql` (see [Planned Future Work](#planned-future-work)).
- **Result (Nov 5):** `mvn -pl importer -am integration-test` passes: 115 unit tests, and 200 integration tests with 0 failures, 0 errors and 3 skipped. The integration tests took about 61 minutes, because `IntegrationTestBase` started a new ClickHouse container after every test method; now about 3 minutes (see [Planned Future Work](#planned-future-work)).
- `importer-maven.yml` now runs from the repository root (`-pl importer -am`), with a 30-minute timeout, and also runs for `shared-model/**` and `pom.xml` changes. Run from `importer/` alone, it couldn't resolve `shared-model`.
- ~~Make the Python tests (`test_scripts.sh` / `tests/`) runnable from `importer/`.~~ Done (see below).
- ~~Port `maven.yml` and `validate-python.yml` into the root `.github/workflows/`~~ Done: `importer-maven.yml` and `importer-validate-python.yml`. The Python suite passes from `importer/` (192 tests). The Java suite still needs the fixes above.

**Web application**

- ~~Confirm the backend unit and integration tests still pass after the POM restructure.~~ Done: 1,581 tests, 0 failures, 0 errors, 5 skipped (Nov 4, with UTC and Docker).
- ~~Rename `core-test.yml`~~ Done: it's now `backend-test.yml` ("Backend tests"). Update branch protection if the old check was required.
- ~~Fix the root's internal Testcontainers mismatch (ClickHouse module 1.19.7 vs everything else at 1.21.4).~~ Done: all at 1.21.4 through `testcontainers-bom`.

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

**Status: done as part of the drop.** `importer/pom.xml` now uses the same Spring Boot 4.1.0 BOM and version overrides as the root, and the importer compiles against Spring 7 with no source changes. One difference remains open, the Testcontainers ClickHouse module; the ClickHouse JDBC driver was aligned afterwards at 0.6.2 `http` (0.6.4 breaks web app tests). The JUnit 4 tests still need a Vintage engine or a migration; that's part of hackathon goal 3. Details are in [cbiohackathon-importer-drop.md](cbiohackathon-importer-drop.md).

## Shared Code

### Duplicated types

| `cbioportal-core` (`org.mskcc.cbio.portal.model.shared`) | `cbioportal` counterpart | Notes |
| --- | --- | --- |
| `GeneticAlterationType` | `legacy/model/MolecularProfile.MolecularAlterationType` | Renamed in cbioportal; the "copied to" comment in core is out of date |
| `MolecularProfileDataType` | None as a class; closest are `MolecularProfile.DataType` / `ImportType` | Values need to be reconciled, not just deduplicated |
| `EntityType` | `legacy/model/EntityType` | |
| `GenericAssayMeta` | `legacy/model/meta/GenericAssayMeta` | |
| `GenericEntityProperty` | ~~`legacy/model/GenericEntityProperty`~~ (unused; deleted) | The importer copy becomes the shared one; the export-only `application/file/model/GenericEntityProperty` is a different class |
| `GeneticEntity` | ~~`legacy/model/GeneticEntity`~~ (unused; deleted) | The importer copy becomes the shared one; the export-only `application/file/model/GeneticEntity` is a different class |
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

1. **One definition.** A shared type lives in exactly one file, in `shared-model`. `SharedModelDuplicationTest` enforces this by failing if the web or importer sources declare a shared type's name anywhere else. It's a source-text check (the web tests run before the importer compiles), so a copy under a new name is caught only in code review.
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

## Planned Future Work

Status as of Nov 5, 2026. Goals 1 and 2 are done; the repository builds the web app and the importer from one reactor, and the Docker image no longer clones `cbioportal-core`. What's left is below, in the order it should happen. Nothing in it changes the decisions recorded above.

### 1. Finish the hackathon goals

| Step | Goal | Why it matters | Effort |
| --- | --- | --- | --- |
| ~~**Fix the `cbioportal/cbioportal-core` image build.**~~ **Done (Nov 5).** `importer/Dockerfile` built `importer/pom.xml` on its own (build context `importer/`), which no longer works now that the POM inherits from the root and needs `shared-model`. It now builds from the repository root with `mvn -pl importer -am package` and copies `web/pom.xml` only because Maven reads every listed module. `importer-docker-image.yml` uses context `.` and also runs for `shared-model/**` and `pom.xml` changes. Checked with a local build: the JAR includes the shared types, and the Java importer and `metaImport.py -h` run. The multi-platform publish only runs in CI. | 2 | It was a regression introduced by the reactor | Small |
| ~~**Make the importer's Java tests run.**~~ **Done (Nov 5).** Surefire 3.5.6 with the JUnit Vintage engine; the dead GitHub download and `cbioportal.version` are removed; the importer CI workflow builds from the root. 115 unit and 200 integration tests pass (3 skipped). | 3 | The importer's integration tests had never run in this repository | Done |
| ~~**Make the importer's integration tests fast enough for every PR.**~~ **Done (Nov 5): about 61 minutes down to 3¼ minutes** for `mvn -pl importer -am integration-test`, all tests still passing (also in two random class orders). `IntegrationTestBase` now rebuilds the test database inside one container (drop, schema, seed: under a second) instead of starting a new container after every test method, and all integration test classes share one JVM (`reuseForks=true`). The shared JVM needed the importer's static state reset before each test, because six test classes leave bulk-load mode on and the gene, gene panel and reference genome caches outlived the database; small public `reset()`/`reCache()` hooks were added to `ClickHouseBulkLoader`, `ClickHouseBulkDeleter`, `DaoGenePanel` and `DaoReferenceGenome`. The JVM-per-class setup had also hidden a local cost: the ClickHouse driver resolves every local network address on start-up, which took 40 seconds per class on the test Mac. | 3 | An hour per run was too long to require on PRs | Done |
| **Run every module in CI.** Now that the importer's tests take minutes, switch `backend-test.yml`, `integration-test.yml` and `sonarcloud.yml` from `-pl web -am` to the full reactor, so `mvn verify` at the root is the single check. | 3 | Meets goal 3's "done when" | Small |
| **Test the importer against the production schema.** Its `clickhouse_cgds.sql` still creates the old `resource_sample`, `resource_patient` and `resource_study` tables, which production replaced with `resource`, and lacks five derived tables that production's `schema.sql` creates. Point `IntegrationTestBase` at `src/main/resources/db-scripts/clickhouse/init/schema.sql`, fix what breaks, and delete the copy. | 3 | Today the importer is tested against a schema production doesn't use | Medium |
| **Test the new image with Docker Compose,** end to end: start the stack and import a study through `/core/core-IMPORTER.jar`. | 2 | The image was only checked by a local build and a few commands inside it | Small |
| **Check the IDE import** of the `web/` module whose sources live in `../src`. If IntelliJ or VS Code mishandles it, move `src/` to `backend/` (Option A) in one `git mv` commit. | 2 | The layout is unusual and untested in an IDE | Small to check; medium if the move is needed |
| **Update branch protection** if "Core tests" was a required check; the workflow is now "Backend tests". | 3 | A required check that never reports blocks merges | Trivial |

### 2. Clean up after the merge

- **Stop bundling the scripts in the importer JAR.** `importer/pom.xml` still copies `scripts/` into the JAR because the old Dockerfile extracted them from it. Both Dockerfiles now copy them from `importer/` directly, so check for other users before removing this.
- **Update the docs that describe two repositories:** `docs/Inter-Repository-Dependencies.md` (now obsolete; replace it with a short description of `shared-model`) and `docs/Architecture-Overview.md` (validator links point to `cbioportal-core`). The import how-tos on docs.cbioportal.org also link to the core repository and its script paths.
- **Path filters and a gate job** (Phase 5): run the importer suites only for `importer/**`, run everything for `shared-model/**`, the root POM or schema files, and make one always-running gate job the required check (see [Path-based triggers](#path-based-triggers)).
- **Choose the schema's source of truth** for both production (`db-scripts/clickhouse/`) and tests (`clickhouse_cgds.sql`), and add a check that keeps them in sync.
- **Plan the deploy** of the shared-model change: flush the Redis cache, or expect a burst of cache misses for values that contain a `MolecularProfile` (see goal 1).
- **Speed up the importer tests** once they run on JUnit 5: parallel execution, a unit/integration split and a cached ClickHouse image (see [Reducing importer test time](#reducing-importer-test-time)).

### 3. Decide, then retire `cbioportal-core`

These need maintainers rather than code (see [Open Questions](#open-questions)):

- **Publishing:** whether the importer module is published to Maven Central, and what happens to the `org.mskcc.cbio:core` artifact, its 1.0.x version line and the `cbioportal/cbioportal-core` Docker image.
- **Naming:** whether to rename the importer's `org.mskcc.cbio.portal` package, and whether its scripts stay under `importer/scripts/`.
- **Migration** (Phases 6–7): survey institutional users and forks; announce a freeze date; merge or close open `cbioportal-core` PRs and transfer its issues; update `cbioportal-docker-compose` if needed; then archive `cbioportal-core` with a README that points to `cbioportal/importer/`.

### 4. Later improvements (out of scope for the hackathon)

- **Harmonize logging** across the web app, the importer and its Python scripts. See [Harmonize logging](#harmonize-logging).
- **Upgrade the ClickHouse JDBC driver** from 0.6.2 to 0.11.0: the web app first (test-base time zone option, transaction manager), then port the importer's three bulk-load classes to `client-v2`. See [Upgrade the ClickHouse JDBC driver](#upgrade-the-clickhouse-jdbc-driver).
- **`StructuralVariant`:** the one same-name model pair close enough to consider merging, but only as a deliberate design for both reading and writing. The other 16 pairs stay separate (see goal 1).
- **Shared test data:** if the web app and the importer later need the same fixtures, consider one dataset (for example Parquet files loaded into the test ClickHouse) instead of each module's own SQL.

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

### Upgrade the ClickHouse JDBC driver

Both modules are pinned to `clickhouse-jdbc` 0.6.2 with the `http` classifier (see [Goal 2](#goal-2-consolidate-the-root-pom-and-importerpomxml)). The latest release is 0.11.0. Findings from Nov 3, 2026, for whoever picks this up:

**Why 0.6.4 was reverted.** The 0.6.3 and 0.6.4 release notes don't mention it, but 0.6.4 changed what the legacy (V1) JDBC driver does with statements it can't classify. 0.6.2 treats them as queries and returns their rows. 0.6.4 treats them as returning nothing: the server sends the rows, and the driver discards them ("N bytes skipped before closing input stream"). A JDBC probe against ClickHouse 24.5 confirmed two triggers, both used by the web app's ClickHouse mappers:

| Query shape | 0.6.2 | 0.6.4 | 0.11.0 | Where it's used |
| --- | --- | --- | --- | --- |
| Statement starting with `(`, e.g. `(SELECT 1)`, `(WITH … SELECT …)`, `(SELECT …) UNION ALL (SELECT …)` | Rows returned | **No result set** | Rows returned | `getClinicalDataCountsByStudyViewFilter`: 12 failing tests in `ClickhouseClinicalDataMapperTest` |
| `?` placeholder in a `SETTINGS` clause, e.g. `… SETTINGS max_memory_usage = ?` (the driver's own parser can't handle it, so it logs a parse error) | Rows returned | **No result set** | Rows returned | `getResourceTableMetadataKeyStats` in `ResourceDataMapper.xml`: 9 failing tests in `ClickhouseResourceDataMapperTest` / `ClickhouseResourceDataRepositoryTest` |
| Plain `SELECT …`, `WITH … SELECT …`, `LIMIT ?`, string literal `'{}'` | Rows returned | Rows returned | Rows returned | — |

If anyone wants to stay on the 0.6.x line but move past 0.6.2, the mapper-side workarounds are: wrap parenthesized statements as `SELECT * FROM ( … )`, and put the `SETTINGS` value inline instead of binding it as a parameter.

**What moving to 0.11.0 involves.** 0.11.0 isn't an incremental upgrade. Since 0.8.0 the default driver is a rewritten implementation (`jdbc-v2`, built on `client-v2`), and since 0.9.0 the `http`, `shaded` and `shaded-all` classifiers are no longer published.

- **Web application: moderate work.**
  - Both query shapes above work on 0.11.0.
  - The web app imports nothing from `com.clickhouse` directly; it uses the driver through MyBatis and Spring. Its custom type handlers (`ClickhouseMapTypeHandler`, `ClickhouseJsonMapTypeHandler`) and the `ArrayTypeHandler` parameters still need to be covered by the test suite.
  - Packaging gets simpler. The plain 0.11.0 JAR depends on `httpclient5` 5.6.4 / `httpcore5` 5.4.3, exactly the versions the root pins for GHSA-hf6x-8p5f-cgmf / GHSA-v3jc-474w-2wm6. The `http` classifier and the explicit `httpclient5` dependency can go.
  - **Full web test suite on 0.11.0 (Nov 3, 2026):** 1,580 tests: 0 failures, **571 errors**, 5 skipped. All 43 database-backed test classes fail before any query runs, for two reasons:
    1. **Test setup (29 legacy MyBatis test classes).** `AbstractLegacyTestcontainers` adds the V1 option `use_server_time_zone=false` to the JDBC URL. V2 then refuses to connect ("Nor server timezone nor specific timezone is set"). The fix is in that test base: drop the option, or also set a time zone. Exactly which V2 option to use still needs checking.
    2. **Transactions (14 ClickHouse repository test classes, and production code).** V2 throws `SQLFeatureNotSupportedException: setAutoCommit(false) is not supported` instead of simulating transactions as V1 did. Spring's `DataSourceTransactionManager`, configured in `ClickhouseMyBatisConfig`, calls it whenever a transaction begins. That affects the tests and also the seven `@Transactional(readOnly = true)` methods in `ExpressionEnrichmentServiceImpl`, `CoExpressionServiceImpl` and `ExportService`, which would fail at run time. Options: remove those annotations, or stop using a transaction manager for the ClickHouse datasource. Also check whether V2 has a setting that ignores unsupported calls.

    So the web app's upgrade is more than a version bump. It needs those two fixes, then another full test run to find whatever the 571 errors are currently hiding.
- **Importer: real porting work.**
  - The importer's bulk-load path doesn't work on 0.11.0. `ClickHouseBulkLoader`, `ClickHouseBulkDeleter` and `ClickHouseBulkUploader` send data as `INSERT INTO … FORMAT TSV(WithNames)` with the payload bound through `PreparedStatement.setBinaryStream`. That's a V1-only "stream-based PreparedStatement" feature, which V2 removed. A probe of exactly this pattern inserts 3 rows on 0.6.2; on 0.11.0 it throws `ArrayIndexOutOfBoundsException` and inserts nothing.
  - These three classes would need to move to the `client-v2` insert API (an insert from an `InputStream` with a TSV format) or to a different insert approach.
  - V2 also drops JDBC transactions, which V1 only simulated; it throws on `setAutoCommit(false)` (confirmed in the web suite above). That affects the importer's Spring `TransactionTemplate` usage (`TransactionalScriptRunner`) and `DaoGenericAssay`'s `setAutoCommit(false)`. Check whether either relies on rollback.
- **Dependency conflicts.**
  - With today's root pins, Maven resolves three of the driver's dependencies to *older* versions than 0.11.0 declares: Guava 32.1.3-jre (wants 33.7.2-jre), commons-lang3 3.18.0 (wants 3.20.0) and commons-compress 1.24.0 (wants 1.28.0). Each needs checking or raising.
  - 0.11.0 also brings in `at.yawk.lz4:lz4-java`, a fork that provides the same `net.jpountz` classes as the importer's `org.lz4:lz4-java` 1.8.0. Keep only one.
- **Other V2 behavior changes to review:**
  - No multi-statement SQL and no named parameters.
  - Since 0.11.0: no default socket buffer sizes; `TIMEOUT_EXCEEDED` is no longer retried; `DatabaseMetaData.getTables` returns `null` remarks; and a new `User-Agent` format.
  - A `migration-helpers` module converts V1 configuration properties and URLs to the V2 format.

**Suggested order:**
1. Upgrade the web app first. It can move as soon as its test suite passes on 0.11.0.
2. Port the importer's three bulk classes to `client-v2`.
3. Return both modules to a single driver version, as the reactor's one dependency entry.

## Open Questions

Please comment on this document or open a GitHub discussion thread.

**Layout and naming**

- [ ] Option A (full reactor, move `src/` to `backend/`) or Option B (no reactor)? If A, when should the move land to disrupt the fewest open PRs?
- [ ] Module names: `backend/`, `shared-model/`, `importer/`? (Alternatives: `web/`, `model/`, `data-import/`.)
- [ ] Keep the Java package `org.mskcc.cbio.portal` or rename it to `org.cbioportal.importer`? Renaming is clearer but adds churn for external integrators.
- [ ] Keep the Python/Perl scripts under `importer/scripts/` or move them to the top-level `scripts/`?

**Build and release**

- [x] ~~Who keeps the `io.github.cbioportal:cbioportal` coordinates?~~ Decided: the web module keeps them; the root becomes `cbioportal-parent`; the parent POM and `shared-model` are published.
- [ ] Should the importer module be published to Maven Central too, or skipped (`maven.deploy.skip`)? If published, it has to pass the `maven-central` profile's javadoc, sources and GPG steps.

- [ ] Upgrade the importer to the parent's dependency versions (recommended), or allow overrides?
- [x] ~~Should the importer's version always match the parent version?~~ Decided: yes, modules inherit the parent's version.
- [x] ~~Which ClickHouse JDBC driver?~~ Decided: 0.6.2 with the `http` classifier, in both modules. 0.6.4 was reverted after it broke 21 web app tests.
- [x] ~~What in 0.6.4 breaks the ClickHouse mapper queries?~~ Answered: statements starting with `(` and `SETTINGS … = ?` placeholders return no result set on 0.6.4. See [Upgrade the ClickHouse JDBC driver](#upgrade-the-clickhouse-jdbc-driver).
- [ ] When should the driver upgrade to 0.11.0 (a future goal) be scheduled, and who ports the importer's bulk-load classes to `client-v2`?
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
