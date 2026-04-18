# Apache 2.0 Relicensing Dependency Audit (RFC86)

This document records dependency-license findings for the relicensing investigation tracked in RFC86.

## Scope

- Backend dependencies from `pom.xml`
- E2E JavaScript dependencies from `src/e2e/js/package.json`

## How this was checked

1. Attempted baseline Maven build/test:
   - `mvn install -DskipTests`
   - `mvn integration-test`
2. Generated effective POM:
   - `mvn -q help:effective-pom -Doutput=/tmp/cbioportal-effective-pom.xml`
3. Queried dependency POM metadata from Maven Central (`repo1.maven.org`) to inspect declared licenses.

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
