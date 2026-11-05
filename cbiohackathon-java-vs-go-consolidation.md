# Consolidation options: Java repo merge vs. the Go monorepo

Nov 4, 2026 · @Angelica Ochoa · companion to [cbiohackathon-rfc-reintegrating-repos.md](cbiohackathon-rfc-reintegrating-repos.md) and [cbiohackathon-importer-drop.md](cbiohackathon-importer-drop.md)

**Purpose.** We're deciding whether to continue merging `cbioportal-core` into `cbioportal` (Java), or stop and adopt [cBioPortal/cbioportal-go](https://github.com/cBioPortal/cbioportal-go), a Go rewrite of both the REST API and the importer in one repository. This document lists the work still open on each path, so the two can be compared side by side. It doesn't make the decision.

**Sources and their limits.**
- **Java path:** our own work on branch `cbio-hackathon-repo-int` (Nov 3–4, 2026). Its results were checked by builds and test runs.
- **Go path:** taken from `cbioportal-go`'s own documentation and code at commit `f9aec2c` (Nov 2, 2026). Its speed, cost, endpoint coverage and parity figures are **as reported by that project; we haven't reproduced them**.

## At a glance

| | Java consolidation (`cbioportal` + `importer/`) | Go monorepo (`cbioportal-go`) |
| --- | --- | --- |
| Status | Importer copied in; dependencies aligned; builds; web suite passes (1,580 tests) | Experimental, not an official release. Runs at go.cbioportal.org on a mirror of production data |
| How backend–importer dependencies are resolved | Being resolved: one shared version list, one shared-types module, a Maven reactor (all still to do) | Resolved by design: one Go module and one `go.mod`, with shared code as `internal/` packages that both programs import |
| Who owns the database schema | Split across `db-scripts`, test SQL files and the importer's copies | The importer: its `transform` step builds every table the API reads |
| Test environments | Three separate setups (MySQL 5.7 in `backend-test.yml`, Testcontainers in the web app, Testcontainers plus a GitHub schema download in the importer). Importer Java tests don't run yet | One ClickHouse for everything; the API tests run on data loaded by the importer itself; golden-table tests for the importer |
| Feature coverage | Complete; it is the current product | 179 of 192 endpoints; some login methods missing (see below) |
| Remaining effort to finish the consolidation | About 2–3 weeks for one engineer for the hackathon goals, plus 4–7 weeks of follow-up work | Mostly closing gaps and validating at scale; no merge work. Not estimated here |
| Main risk | Doing the merge work while carrying over the Java stack's existing debt | Moving production and every deployment to a new implementation and language |

## Java path: remaining issues

From the RFC's hackathon goals and the Nov 3 recap, in priority order.

| Priority | Issue | Difficulty | Effort |
| --- | --- | --- | --- |
| P1 | **No import has been run with the new importer JAR yet.** It's the only check on Spring 7, the switch to Apache HttpClient 5, and the removed libraries | Medium, unknown | 1–2 days |
| P1 | **Reactor decisions:** whether IDEs handle the no-move layout (the alternative is moving `src/` to `backend/`), and whether the importer is published to Maven Central | Low | ~Half a day |
| P1 | **Build the reactor (goal 2):** split the 1,058-line root POM, fix `${project.basedir}` and `target/` paths, override the shade settings the importer would inherit, and remove the duplicated version pins | Medium–high | 2–4 days |
| P2 | **Shared types (goal 1, steps 2–6):** create a `shared-model` module and switch both sides to it; then check the API docs for changes and the Redis cache behaviour after deploy | Medium | 1.5–3 days |
| P2 | **Importer test environment (goal 3):** read the schema from the repo instead of GitHub; switch to Boot's Surefire; run or migrate 76 JUnit 4 classes; get 82 test classes passing | High, unknown | 2–5 days |
| P2 | **Small test fixes:** Testcontainers ClickHouse 1.19.7 → 1.21.4; `StudyControllerTest` only passes in UTC | Low | Hours |
| P3 | **Docker:** build the importer from `importer/`, drop the `build-core` stage, keep `importer/tests` out of the build context | Medium–low | ~1 day |
| P3 | **CI:** limit the backend workflows to the web module, add a required gate job for path-filtered checks, and rename the "Core tests" required check (needs repo admin) | Medium–low | 1–2 days |
| Later | **Merge the other parallel models** (21 classes with the same name on both sides) | Very high | 2–4+ weeks |
| Future | **ClickHouse driver 0.6.2 → 0.11.0:** the web app needs transaction and test-setup fixes; the importer's 3 bulk-load classes need porting to `client-v2` | Medium–high | ~1–2 weeks |
| Future | **One logging setup** for the web app and the importer | Medium | ~1 week |

**Known side effects already in place:**
- **Failing workflow:** `importer-maven.yml` fails until the importer tests are fixed.
- **Image tags change:** the `cbioportal/cbioportal-core` image tags change when the ported Docker workflow runs. Branch builds are tagged `master`, and release tags publish the importer under cbioportal's version.
- **Separate schema setups remain:** even after the reactor, the importer and web tests load the schema separately until someone changes that deliberately.

**What finishing gets us:**
- One repository, one build, one version, and no duplicated shared types.
- The same Java stack, team skills, deployments and extension points as today.
- It doesn't change the existing technical debt: the legacy and new model layers, Java's memory use, and the old driver.

## Go path: remaining issues

From `cbioportal-go`'s README ("What is still missing"), its API coverage page, Java-quirks page and benchmark page. Reported, not verified.

| Area | Gap |
| --- | --- |
| Status | Labelled **experimental**: "not an official cBioPortal release… do not use it for research or clinical decisions" |
| Login and authorization | Not supported: SAML login; `authorization=true` (users listed in the database); login providers without OpenID Connect (GitHub, Facebook); data access tokens other than `dat.method=oauth2`. Supported: OAuth2/OIDC (Keycloak, Google, Microsoft), Redis login sessions, role-based study access |
| Endpoints | 179 of 192. Not ported: SAML login pages, MatchMiner, the URL shortener, old `/api/proxy/*` endpoints, and the MSK-internal sample id translation |
| Same answers as Java | 613 of 618 logged production requests matched (the other 5 differ for known reasons). The nightly check's first run found **65 unexplained differences in 5,009 requests**, tracked in cbioportal-go#1. The main remaining difference is the order of rows that tie in a sort |
| Sessions | Reading one virtual study or custom data set by id returns more fields than Java does, and leaves out the `uniqueStudyIds` Java adds |
| Importer | "Tested on a handful of studies"; not yet tested against all of datahub, or against cbioportal-core's validator checks |
| Validator | Not ported. The hub's import job still runs **cbioportal-core's Python `validateData.py`**, copied from a fixed commit. So `cbioportal-core` (or at least its Python scripts) still has to live somewhere |
| Database load | Over three days, it used **1.47×** Java's ClickHouse CPU per request on www's traffic (0.92× on api's), because it has no response cache yet |
| Performance gaps | Pages sorted with `sortBy`, and sample, sample-list and profile lists, are built in memory. Whole-profile reads of very large studies take seconds |
| Data migration | It uses its own database layout: three databases (`cbioportal_raw`, `cbioportal_ref`, `cbioportal`). Today go.cbioportal.org reads a mirror of production through `transform --from-prod`, which the docs describe as needed "only while go replaces the Java cBioPortal". Production would have to switch to the Go importer |

**What it reports in its favour** (from its benchmark page, also unverified):
- **Latency:** about the same median latency as Java (31 vs 34 ms), and much better slow requests (p99 1.4 s vs 6.0 s).
- **Stability and memory:** no 5xx errors (Java had 60) and no restarts (Java had 7). A pod averages 48 MiB of memory, vs 1,838 MiB for Java.
- **Cost:** an estimated ~$160 a month in servers, vs ~$1,500 for the Java backend pods with their Redis caches.
- **Import speed:** studies load 2.6–3.6× faster. MSK-IMPACT takes 80 s instead of 289 s.
- **Code size:** about 38,100 lines of main code, vs about 113,000 for the Java backend and `cbioportal-core` combined.

## Issues that exist on both paths

- **Python validator and scripts.** Both paths still depend on `validateData.py` and its Python tooling. The Java path keeps it in `importer/scripts/`; the Go path copies it from `cbioportal-core`. Either way, it needs one permanent home and an owner.
- **Frontend.** Neither path changes `cbioportal-frontend`. The Go repo pins it as a git submodule and runs its Playwright suite weekly.
- **Downstream users.**
  - `cbioportal-docker-compose` (which runs `/core/core-IMPORTER.jar`), the docs and institutional deployments all need updating on either path.
  - The Go path also changes the server image, how it's configured (it reads an existing `portal.properties`), and the import commands.
- **Matching Java's behaviour.** The Java path keeps current behaviour automatically. The Go path has to keep proving it matches through its contract tests, Java oracle and nightly production replay, and some of Java's behaviour can't be matched (for example, results that change from one request to the next).

## Comparison by concern

| Concern | Java consolidation | Go monorepo |
| --- | --- | --- |
| Backend–importer dependencies | Requires a reactor and a shared module. Versions are still declared in two POMs until the reactor exists | Already handled by its structure |
| Test environments | Goal 3 work is outstanding; three different database setups | One database and one CI file. Every API test also tests the importer |
| Feature completeness | Complete | Gaps in login (SAML, database authorization), 13 endpoints, and the validator |
| Proof that results match | Same code, so nothing to prove | Strong tooling, but 65 open differences after the first nightly run |
| Run cost and performance | As today | Reported to be much lower and faster, though with higher ClickHouse CPU until it has a cache |
| Team and community | Existing Java skills, Spring extension points and forks keep working | New language and codebase; Java-based customizations by forks and institutions don't carry over |
| Reversibility | Low risk: the importer can still be built on its own | Can run alongside Java for a while (as go.cbioportal.org does now), but switching production is a large, one-way change |

## Questions that would settle it

1. **Deployments needing SAML or database authorization.** Do any production or institutional deployments need SAML, `authorization=true`, or the 13 unported endpoints? If so, who builds them in Go, and when?
2. **Long-term direction.** Is the Go rewrite the project's committed future? If so, the Java consolidation's later items (parallel models, the driver upgrade, logging) aren't worth doing, and the effort should stop after the parts that pay off immediately.
3. **Acceptable remaining differences.** What level of unexplained parity differences is acceptable before switching production? The first nightly run had 65 in 5,009.
4. **The validator's home.** Where should the Python validator and scripts live long-term, given both paths keep them?
5. **Interim value.** If the decision is Go but the switch is months away, which Java items are still worth doing in the meantime?
   - Likely worth it: the import run (P1), the importer test environment (P2), and the Docker change (P3).
   - Likely not: the reactor and shared types (P1/P2).
