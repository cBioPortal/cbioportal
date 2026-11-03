# Importer drop: cbioportal-core → cbioportal/importer

Nov 3, 2026 · branch `cbio-hackathon-repo-int` · companion to [cbiohackathon-rfc-reintegrating-repos.md](cbiohackathon-rfc-reintegrating-repos.md)

This is the first step of the RFC: the importer code now lives in this repository, and its dependencies come from the same versions the web application uses. The test environments were deliberately left alone.

## What was done

1. **Copied the importer in.** All 906 tracked files from `cbioportal-core` at `6146cdbf916e30ba8d0a9a5cced224d715af28d5` are now under `importer/`. They were exported with `git archive`, so untracked and ignored files (local `CLAUDE.md`, built JARs, `target/`) were left behind. This is a plain copy: the importer's git history isn't carried over.
2. **Moved the ignore rules into the root `.gitignore`.** The root rule `*/**/.gitignore` ignores nested `.gitignore` files, so `importer/.gitignore` would never have been committed. Its rules that matter here (built `*.jar`, `application.properties`, `maven.properties`, `__pycache__/`, `.venv/`) are now an `importer/` block in the root `.gitignore`, and the nested file was removed.
3. **Rewrote `importer/pom.xml` to use the web application's versions.** The importer stays a separate Maven project, because the root POM uses `jar` packaging and can't be a parent or reactor (RFC Option B). It now imports the same Spring Boot BOM as the root's `spring-boot-starter-parent` (4.1.0), plus the root's version overrides. Explicit versions were removed wherever the BOM manages the artifact. Coordinates (`org.mskcc.cbio:core:1.0.16`), the shade packaging and the plugins are unchanged, so the build still produces `core-1.0.16.jar`.
4. **Verified the build.** `mvn clean package -DskipTests` in `importer/` succeeds with **no Java source changes**: main and test sources compile against Spring Framework 7.0.8. The shaded JAR contains `scripts/` and `requirements.txt`, with Jackson 2.21.6 inside. A smoke run of `ImportTypesOfCancers` prints its usage message. `ImportProfileData` fails on the missing `application.properties`, exactly as the original JAR does.

The root build is unchanged; `mvn install` at the root doesn't build `importer/`.

## Dependency discrepancies

The comparison uses the resolved dependency lists (`mvn dependency:list`, all scopes) of the root project and the importer. The two builds shared 81 artifacts, and **52 of them resolved to different versions**. After the change, 50 match and 2 remain open.

| Artifact | Importer before | Importer after | cbioportal | Scope (importer) | Status |
| --- | --- | --- | --- | --- | --- |
| `org.springframework:spring-aop` | 6.2.11 | 7.0.8 | 7.0.8 | compile | aligned |
| `org.springframework:spring-beans` | 6.2.11 | 7.0.8 | 7.0.8 | compile | aligned |
| `org.springframework:spring-context` | 6.2.11 | 7.0.8 | 7.0.8 | compile | aligned |
| `org.springframework:spring-core` | 6.2.11 | 7.0.8 | 7.0.8 | compile | aligned |
| `org.springframework:spring-expression` | 6.2.11 | 7.0.8 | 7.0.8 | compile | aligned |
| `org.springframework:spring-jdbc` | 6.2.0 | 7.0.8 | 7.0.8 | compile | aligned |
| `org.springframework:spring-tx` | 6.2.11 | 7.0.8 | 7.0.8 | compile | aligned |
| `org.springframework:spring-test` | 6.2.7 | 7.0.8 | 7.0.8 | test | aligned |
| `org.springframework.boot:spring-boot` | 3.5.0 | 4.1.0 | 4.1.0 | test | aligned |
| `org.springframework.boot:spring-boot-autoconfigure` | 3.5.0 | 4.1.0 | 4.1.0 | test | aligned |
| `org.springframework.boot:spring-boot-starter` | 3.5.0 | 4.1.0 | 4.1.0 | test | aligned |
| `org.springframework.boot:spring-boot-starter-logging` | 3.5.0 | 4.1.0 | 4.1.0 | test | aligned |
| `org.springframework.boot:spring-boot-starter-test` | 3.5.0 | 4.1.0 | 4.1.0 | test | aligned |
| `org.springframework.boot:spring-boot-test` | 3.5.0 | 4.1.0 | 4.1.0 | test | aligned |
| `org.springframework.boot:spring-boot-test-autoconfigure` | 3.5.0 | 4.1.0 | 4.1.0 | test | aligned |
| `com.fasterxml.jackson.core:jackson-annotations` | 2.15.2 | 2.21 | 2.21 | compile | aligned |
| `com.fasterxml.jackson.core:jackson-core` | 2.15.2 | 2.21.6 | 2.21.6 | compile | aligned |
| `com.fasterxml.jackson.core:jackson-databind` | 2.15.2 | 2.21.6 | 2.21.6 | compile | aligned |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | 2.15.2 | 2.21.6 | 2.21.6 | compile | aligned |
| `org.slf4j:slf4j-api` | 2.0.17 | 2.0.18 | 2.0.18 | compile | aligned |
| `org.slf4j:jul-to-slf4j` | 2.0.17 | 2.0.18 | 2.0.18 | test | aligned |
| `ch.qos.logback:logback-classic` | 1.5.18 | 1.5.34 | 1.5.34 | test | aligned |
| `ch.qos.logback:logback-core` | 1.5.18 | 1.5.34 | 1.5.34 | test | aligned |
| `org.apache.logging.log4j:log4j-api` | 2.24.3 | 2.25.5 | 2.25.5 | test | aligned |
| `org.apache.logging.log4j:log4j-to-slf4j` | 2.24.3 | 2.25.5 | 2.25.5 | test | aligned |
| `commons-logging:commons-logging` | 1.2 | 1.3.6 | 1.3.6 | compile | aligned |
| `org.apache.commons:commons-lang3` | 3.13.0 | 3.18.0 | 3.18.0 | compile | aligned |
| `net.bytebuddy:byte-buddy` | 1.18.3 | 1.18.10 | 1.18.10 | compile | aligned |
| `net.bytebuddy:byte-buddy-agent` | 1.17.7 | 1.18.10 | 1.18.10 | test | aligned |
| `org.yaml:snakeyaml` | 2.4 | 2.6 | 2.6 | compile | aligned |
| `io.micrometer:micrometer-commons` | 1.14.11 | 1.17.0 | 1.17.0 | compile | aligned |
| `io.micrometer:micrometer-observation` | 1.14.11 | 1.17.0 | 1.17.0 | compile | aligned |
| `jakarta.activation:jakarta.activation-api` | 2.1.3 | 2.1.4 | 2.1.4 | test | aligned |
| `jakarta.annotation:jakarta.annotation-api` | 2.1.1 | 3.0.0 | 3.0.0 | test | aligned |
| `jakarta.transaction:jakarta.transaction-api` | 1.3.1 | 2.0.1 | 2.0.1 | compile | aligned |
| `jakarta.ws.rs:jakarta.ws.rs-api` | 3.1.0 | 4.0.0 | 4.0.0 | test | aligned |
| `jakarta.xml.bind:jakarta.xml.bind-api` | 4.0.2 | 4.0.5 | 4.0.5 | test | aligned |
| `org.junit.jupiter:junit-jupiter` | 5.12.2 | 6.0.3 | 6.0.3 | test | aligned |
| `org.junit.jupiter:junit-jupiter-api` | 5.12.2 | 6.0.3 | 6.0.3 | test | aligned |
| `org.junit.jupiter:junit-jupiter-engine` | 5.12.2 | 6.0.3 | 6.0.3 | test | aligned |
| `org.junit.jupiter:junit-jupiter-params` | 5.12.2 | 6.0.3 | 6.0.3 | test | aligned |
| `org.junit.platform:junit-platform-commons` | 1.12.2 | 6.0.3 | 6.0.3 | test | aligned |
| `org.junit.platform:junit-platform-engine` | 1.12.2 | 6.0.3 | 6.0.3 | test | aligned |
| `org.mockito:mockito-junit-jupiter` | 5.17.0 | 5.23.0 | 5.23.0 | test | aligned |
| `org.assertj:assertj-core` | 3.27.3 | 3.27.7 | 3.27.7 | test | aligned |
| `org.hamcrest:hamcrest-core` | 1.3 | 3.0 | 3.0 | test | aligned |
| `com.jayway.jsonpath:json-path` | 2.9.0 | 2.10.0 | 2.10.0 | test | aligned |
| `net.minidev:json-smart` | 2.5.2 | 2.6.0 | 2.6.0 | test | aligned |
| `net.minidev:accessors-smart` | 2.5.2 | 2.6.0 | 2.6.0 | test | aligned |
| `org.xmlunit:xmlunit-core` | 2.10.1 | 2.11.0 | 2.11.0 | test | aligned |
| `com.clickhouse:clickhouse-jdbc` | 0.6.4 | 0.6.4 | 0.6.2 (`http` classifier) | compile | **open** |
| `org.testcontainers:clickhouse` | 1.21.4 | 1.21.4 | 1.19.7 | test | **open** |

Artifacts that only the importer uses, and that changed as a side effect:

| Artifact | Before | After | Why |
| --- | --- | --- | --- |
| `org.apache.commons:commons-dbcp2` | 2.10.0 | 2.14.0 | Now managed by the Spring Boot BOM, like everything else it manages |
| `org.apache.commons:commons-pool2` | 2.11.1 | 2.13.1 | Pulled in by dbcp2; managed by the BOM |
| `org.springframework:spring-jcl` | 6.2.11 | removed | Spring 7 dropped `spring-jcl` in favour of `commons-logging` 1.3. The old JAR's "please remove commons-logging.jar" warning is gone. |

## Discrepancies addressed, by category

| Category | Artifacts | How it was aligned |
| --- | --- | --- |
| **Spring Framework 6.2 → 7.0** | 8 (`spring-aop`, `-beans`, `-context`, `-core`, `-expression`, `-jdbc`, `-tx`, `-test`) | Spring Boot 4.1.0 BOM. Before, the importer mixed 6.2.0, 6.2.7 and 6.2.11. |
| **Spring Boot test stack 3.5 → 4.1** | 7 (`spring-boot*`, all test scope) | Spring Boot 4.1.0 BOM |
| **Jackson 2.15 → 2.21** | 4 | `jackson-bom` 2.21.6, the root's CVE override (`jackson-2-bom.version`) |
| **Logging** | 7 (slf4j ×2, logback ×2, log4j ×2, commons-logging) | BOM, plus `log4j-bom` 2.25.5 (the root's CVE override `log4j2.version`) |
| **Apache Commons** | 1 (`commons-lang3` 3.13.0 → 3.18.0) | Pinned to the root's CVE override `commons-lang3.version` |
| **Runtime utilities** | 4 (`byte-buddy`, `snakeyaml`, `micrometer-commons`, `micrometer-observation`) | BOM; explicit versions removed |
| **Jakarta APIs** | 5 (activation, annotation, transaction, ws.rs, xml.bind) | BOM; the explicit `jakarta.ws.rs-api` 3.1.0 removed |
| **JUnit / test libraries** | 14 (JUnit Jupiter/Platform 5.12 → 6.0, mockito-junit-jupiter, byte-buddy-agent, assertj, hamcrest, json-path, json-smart, accessors-smart, xmlunit) | BOM; explicit `junit`, `mockito-core` and `jsonassert` versions removed (the BOM gives the same versions the root resolves) |
| **Already equal; now from one source** | `guava` 32.1.3-jre, `commons-collections4` 4.4, `testcontainers` 1.21.4 | Pinned with the same property names and values as the root, with a "keep in sync with ../pom.xml" comment |

**Total:** 50 of 52 discrepancies addressed.

## Remaining issues

### Dependencies

1. **ClickHouse JDBC driver (open).** The importer uses `clickhouse-jdbc` 0.6.4 (no classifier). The web application uses 0.6.2 with the `http` classifier and pins `httpclient5` 5.6.4 / `httpcore5` 5.4.3 for CVE reasons (see the comment in `../pom.xml`). Neither JAR bundles `httpcore5`. Moving the importer down to 0.6.2 might undo fixes it relies on, and moving the web app up to 0.6.4 needs backend testing. Pick one driver version and classifier for both.
2. **ClickHouse Testcontainers module (open, test only).** The importer uses 1.21.4, matching the root's own `testcontainers.version`. The root pins `org.testcontainers:clickhouse` separately at `clickhouse_testcontainer.version` 1.19.7, so the root isn't consistent with itself. The fix is probably on the root side.
3. **Versions are still declared in two POMs.** Because the root can't be a parent, `importer/pom.xml` repeats `spring-boot.version` and the root's CVE overrides (`jackson-2-bom.version`, `log4j2.version`, `commons-lang3.version`, guava, collections4). They match today, but nothing enforces it. Option A in the RFC (a root aggregator POM) removes this; until then, a CI check comparing the two would.
4. **Libraries the code doesn't reference: resolved.** `dom4j` 1.6.1, `commons-text` 1.9, `opencsv` 2.4 and `commons-io` 2.14.0 were removed from `importer/pom.xml`; none of them, nor `xml-apis` (pulled in by dom4j), is still on the classpath. The build passes and the shaded JAR shrank from 22.7 MB to 21.6 MB. `commons-text` 1.9 had been in the CVE-2022-42889 range, and `dom4j` 1.6.1 had XXE issues (CVE-2018-1000632, CVE-2020-10683). Still to confirm with an import run (issue 6). (`lz4-java` is never imported either, but the ClickHouse driver uses it at run time for compression, so it stays.)
5. **Unused property: resolved.** `commons_cli.version` was removed.

### Not yet tested at runtime

6. **Spring 7 at run time.** The importer compiles, but no import has been run against a database. The code paths most exposed to the Spring 6 → 7 change are the XML application context (`FileSystemXmlApplicationContext`), `TransactionTemplate` and `JdbcTemplate`. Before going further, load a study, for example `study_es_0`, with the new JAR.
7. **No SLF4J provider in the shaded JAR.** The JAR logs "No SLF4J providers were found" and falls back to no-op logging, because logback is only on the test classpath. This was already the case before the drop. Fixing it is part of the logging harmonization future goal in the RFC, which is out of scope for the hackathon.

### Build, packaging and repo integration

8. **Not in the root build.** `mvn install` at the root doesn't build `importer/`; it has to be built with `mvn -f importer/pom.xml package`. Decide between Option A and Option B (see the RFC).
9. **Docker still clones `cbioportal-core`.** `docker/web-and-data/Dockerfile` still has the `build-core` stage, which clones the core repo by branch name, so the image doesn't use `importer/` yet. Also, the web build stage runs `COPY . .`, so `importer/` (about 60 MB, mostly `tests/` data) is now sent in the Docker build context. Add `importer/tests` to `.dockerignore`, or rewrite the `build-core` stage to build from `importer/`.
10. **Maven coordinates and version.** The importer is still `org.mskcc.cbio:core:1.0.16`, while the web app is `io.github.cbioportal:cbioportal:v7.2.5-SNAPSHOT`. The JAR name `core-*.jar` was kept on purpose, because the Dockerfile copies `core-*.jar` to `/core/core-IMPORTER.jar` and `cbioportal-docker-compose/entrypoint.sh` uses that path.
11. **Leftover files from the core repo: resolved.**
    - `importer/.github/workflows/` was ported to the root as `importer-maven.yml`, `importer-validate-python.yml` and `importer-docker-image.yml`, each limited to `importer/**` and its own file. `importer/.github/` was removed.
    - `importer/Dockerfile` stays: the ported Docker workflow still builds it and publishes `cbioportal/cbioportal-core`.
    - `importer/LICENSE` (identical to the root's) was removed. `importer/README.md` was rewritten for the importer's new home and keeps the AGPL-3.0 licensing note.
    - The `maven-resources-plugin` FIXME was replaced with a comment explaining why the scripts are still bundled: the root Dockerfile's `build-core` stage extracts them from the JAR.
    - The root `core-test.yml` was renamed `backend-test.yml` ("Backend tests").
    - **Still open:**
      - If "Core tests" is a required status check in branch protection, it has to be updated there.
      - The importer image's tags change. Branch builds are now tagged `master` instead of `main`. Every cbioportal release tag (for example `v7.2.5`) now also publishes `cbioportal/cbioportal-core:7.2.5`, so the image moves from the 1.0.x version line to cbioportal's.
      - `importer-maven.yml` runs the importer's Java tests, which are expected to fail until goal 3 is done.
12. **Git history: resolved, not needed.** The files were copied, not merged, so `git log` and blame for `importer/` start at the drop commit. That's the decided approach; earlier history stays in the archived `cbioportal-core` repository.
13. **Code style.** The root's Spotless configuration only covers `src/main/java` and `src/test/java`. The importer uses its own formatting (tabs in the POM, different Java style) and isn't checked.

### Shared code (unchanged by this step)

14. **Duplicated domain types.** Five of the seven classes in `importer/src/main/java/org/mskcc/cbio/portal/model/shared/` still have separate counterparts under `src/main/java/org/cbioportal/legacy/model/`. Now that both copies are in one repo, the duplication is easier to see. RFC Phase 4 covers these.
15. **Schema location and version.** `db.version` (3.1.0) is declared in both POMs. The comment in `importer/pom.xml` now points to `../pom.xml` and `../src/main/resources/db-scripts/clickhouse/init/schema.sql`.

### Test environments (now hackathon goal 3 in the RFC)

16. **Schema download.** `IntegrationTestBase` still downloads `clickhouse_cgds.sql` from GitHub at `cbioportal.version` v6.4.1, which is stale. It could now read `../src/test/resources/clickhouse_cgds.sql` directly.
17. **Old Surefire with JUnit 4 tests.** Surefire is still pinned at 2.21.0, and 76 of 82 test classes use JUnit 4. `spring-boot-starter-test` 4.1 brings in JUnit Jupiter 6, and there's no Vintage engine. Expect test discovery to need attention before the suites run.
18. **Importer tests not in CI.** The root workflows don't run the importer's tests.

## Files changed

- `importer/`: new; 905 files from `cbioportal-core@6146cdb` (its `.gitignore` removed).
- `importer/pom.xml`: Spring Boot 4.1.0 BOM import and root overrides; explicit versions removed where managed; `db.version` comment paths updated.
- `.gitignore`: new `importer/` block.
- `importer/pom.xml` (cleanups): removed `dom4j`, `commons-text`, `opencsv`, `commons-io` and the unused version properties; FIXME comment replaced.
- `importer/LICENSE`, `importer/.github/`: removed. `importer/README.md`: rewritten.
- `.github/workflows/core-test.yml` → `backend-test.yml`, renamed "Backend tests".
- `.github/workflows/importer-maven.yml`, `importer-validate-python.yml`, `importer-docker-image.yml`: new, ported from `cbioportal-core`.
