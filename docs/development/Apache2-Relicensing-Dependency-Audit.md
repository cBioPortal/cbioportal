# Apache 2.0 Relicensing Dependency Audit (RFC86)

This document records dependency-license findings for the relicensing investigation tracked in RFC86.

## Scope

- Backend dependencies from `pom.xml`
- E2E JavaScript dependencies from `src/e2e/js/package.json`
- `cbioportal-frontend` dependencies at tag `v7.0.0-rc.2` (matching `<frontend.version>` in `pom.xml`)

## How this was checked

1. Attempted baseline Maven build/test:
   - `mvn install -DskipTests`
   - `mvn integration-test`
2. Generated effective POM:
   - `mvn -q help:effective-pom -Doutput=/tmp/cbioportal-effective-pom.xml`
3. Queried dependency POM metadata from Maven Central (`repo1.maven.org`) to inspect declared licenses.
4. Queried `cbioportal-frontend` package metadata and npm registry license fields for `dependencies` and `devDependencies`.

## Findings

### 1) Clear blocker for Apache-2.0-only distribution

- **`com.mysql:mysql-connector-j:8.2.0`** (direct dependency in `pom.xml`) declares:
  - **GPL-2.0 with Universal FOSS Exception 1.0**
- This is not a standard permissive Apache-2.0 dependency and is the main licensing risk for an Apache-2.0 relicensing path.

### 2) Additional dependencies that need legal confirmation

The scan identified several dependencies with multi-license declarations that include copyleft options (for example EPL/GPL dual licensing, or LGPL/MPL alternatives). Examples include Jakarta API artifacts and some Jersey/RabbitMQ-related transitive artifacts.

These are often consumable under permissive terms (depending on the selected license path), but should be confirmed by legal review before finalizing relicensing.

### 3) E2E JavaScript package

`/src/e2e/js/package.json` declares package license `ISC`; dependencies are common permissive packages (`axios`, `lodash`, `chai`, `mocha`, etc.). No immediate Apache-2.0 compatibility concern was identified in this subset.

### 4) cbioportal-frontend repository audit (tag `v7.0.0-rc.2`)

- `cbioportal-frontend` itself declares **`AGPL-3.0-or-later`** (in `LICENSE` and root `package.json`).
- This is a direct incompatibility for an Apache-2.0-only relicensing target unless the frontend is relicensed or strictly separated from Apache-2.0 distribution scope.
- The frontend dependency scan found multiple copyleft-licensed packages (notably AGPL/GPL-family internal cBioPortal/OncoKB packages and a few GPL-licensed npm dependencies such as `react-column-resizer`, `react-json-to-table`, and dual-licensed `jszip`).
- A few dependencies are git/url based and require manual license confirmation (`svg2pdf.js`, `swagger-js-codegen`, `webpack-raphael`).

## Environment limitation during audit

Full Maven dependency resolution in this environment is currently blocked by unreachable Shibboleth-hosted OpenSAML artifact metadata (`build.shibboleth.net`).

Because of that, this audit is based on:
- direct dependency declarations,
- effective POM analysis,
- and Maven Central license metadata for resolvable artifacts.

## Recommended next steps

1. Decide policy for JDBC driver licensing under Apache-2.0 relicensing:
   - replace `mysql-connector-j`, or
   - make it externally provided with clear distribution boundaries.
2. Run a complete SBOM/license scan in CI from a network environment that can fully resolve all artifacts.
3. Have legal counsel review dual-licensed transitive dependencies and document accepted license choices.
4. For `cbioportal-frontend`, define relicensing strategy (relicense, dependency replacement, or distribution boundary) before any Apache-2.0 migration decision.
