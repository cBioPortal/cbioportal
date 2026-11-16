#!/usr/bin/env bash

set -euo pipefail

repo_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
cd "$repo_root"

maven_bin=${MAVEN_BIN:-mvn}
license_report=target/generated-resources/licenses/THIRD-PARTY.txt
sbom=target/generated-resources/sbom/bom.json
rendered_report=$(mktemp)
trap 'rm -f "$rendered_report"' EXIT

"$maven_bin" --batch-mode -q -Pthird-party-report -DskipTests \
  license:add-third-party cyclonedx:makeAggregateBom

test -s "$license_report"
test -s "$sbom"

{
  printf '%s\n' \
    'Open Source Used in cBioPortal Backend' \
    '' \
    'This file is generated from the compile and runtime Maven dependency graph.' \
    'Regenerate it together with bom.json by running:' \
    '' \
    '    scripts/generate-third-party-report.sh' \
    '' \
    'License names come from dependency POM metadata and require review before' \
    'being used as a legal attribution record. Bundled static assets and the' \
    'separately packaged cBioPortal frontend are outside this Maven report.' \
    '' \
    '------------------------------------------------------------------------' \
    ''
  sed '/./,$!d' "$license_report"
} > "$rendered_report"

cp "$rendered_report" OPEN-SOURCE-DOCUMENTATION
cp "$sbom" bom.json
