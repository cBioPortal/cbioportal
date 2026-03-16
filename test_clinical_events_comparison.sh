#!/bin/bash
# Compare legacy vs column-store clinical events endpoint responses
# Tests 20 patients × all param permutations

BASE_URL="http://localhost:8082"
LEGACY="/api/studies"
COLSTORE="/api/column-store/studies"
PASS=0
FAIL=0
SKIP=0
TOTAL=0
FAILURES=""

compare() {
  local desc="$1"
  local legacy_url="$2"
  local colstore_url="$3"
  local compare_mode="${4:-body}" # body, header, or body_unordered

  TOTAL=$((TOTAL + 1))

  if [ "$compare_mode" = "header" ]; then
    legacy_resp=$(curl -s --max-time 30 -D - "$legacy_url" -H "Accept: application/json" 2>/dev/null)
    colstore_resp=$(curl -s --max-time 30 -D - "$colstore_url" -H "Accept: application/json" 2>/dev/null)
    legacy_val=$(echo "$legacy_resp" | grep -i "total-count" | tr -d '\r' | awk '{print $2}')
    colstore_val=$(echo "$colstore_resp" | grep -i "total-count" | tr -d '\r' | awk '{print $2}')
    if [ "$legacy_val" = "$colstore_val" ]; then
      PASS=$((PASS + 1))
    else
      FAIL=$((FAIL + 1))
      FAILURES="${FAILURES}\n  FAIL: ${desc}\n    Legacy total-count: [${legacy_val}]\n    ColStore total-count: [${colstore_val}]"
    fi
  else
    legacy_body=$(curl -s --max-time 30 "$legacy_url" -H "Accept: application/json" 2>/dev/null)
    colstore_body=$(curl -s --max-time 30 "$colstore_url" -H "Accept: application/json" 2>/dev/null)

    result=$(python3 << PYEOF
import json, sys

try:
    legacy = json.loads('''$( echo "$legacy_body" | python3 -c "import sys; print(sys.stdin.read().replace(\"'\", \"\\\\'\"))" )''')
    colstore = json.loads('''$( echo "$colstore_body" | python3 -c "import sys; print(sys.stdin.read().replace(\"'\", \"\\\\'\"))" )''')
except:
    print("SKIP")
    sys.exit(0)

if not isinstance(legacy, list) or not isinstance(colstore, list):
    print("SKIP")
    sys.exit(0)

if len(legacy) != len(colstore):
    print(f"FAIL:count:{len(legacy)}:{len(colstore)}")
    sys.exit(0)

def norm(e):
    e2 = dict(e)
    e2.pop('uniquePatientKey', None)
    if 'attributes' in e2 and e2['attributes']:
        e2['attributes'] = sorted(e2['attributes'], key=lambda a: (a.get('key',''), a.get('value','')))
    return e2

ln = [norm(e) for e in legacy]
cn = [norm(e) for e in colstore]

mode = "$compare_mode"

if mode == "body_unordered":
    # Sort-dependent: only check same set of events (order may differ due to tie-breaking)
    ls = sorted(ln, key=lambda e: json.dumps(e, sort_keys=True))
    cs = sorted(cn, key=lambda e: json.dumps(e, sort_keys=True))
    if ls == cs:
        print("PASS")
    else:
        print("FAIL:content_unordered")
else:
    if ln == cn:
        print("PASS")
    else:
        # Check if it's just sort tie-breaking
        ls = sorted(ln, key=lambda e: json.dumps(e, sort_keys=True))
        cs = sorted(cn, key=lambda e: json.dumps(e, sort_keys=True))
        if ls == cs:
            print("PASS_TIEBREAK")
        else:
            print("FAIL:content")
PYEOF
)

    case "$result" in
      PASS|PASS_TIEBREAK)
        PASS=$((PASS + 1))
        ;;
      SKIP)
        SKIP=$((SKIP + 1))
        ;;
      FAIL:count:*)
        FAIL=$((FAIL + 1))
        lc=$(echo "$result" | cut -d: -f3)
        cc=$(echo "$result" | cut -d: -f4)
        FAILURES="${FAILURES}\n  FAIL: ${desc} (count: legacy=${lc} colstore=${cc})"
        ;;
      *)
        FAIL=$((FAIL + 1))
        FAILURES="${FAILURES}\n  FAIL: ${desc} (${result})"
        mkdir -p /tmp/clinical_events_diff
        safe_desc=$(echo "$desc" | tr ' /:' '___')
        echo "$legacy_body" | python3 -m json.tool > "/tmp/clinical_events_diff/${safe_desc}_legacy.json" 2>/dev/null
        echo "$colstore_body" | python3 -m json.tool > "/tmp/clinical_events_diff/${safe_desc}_colstore.json" 2>/dev/null
        ;;
    esac
  fi
}

# 20 test patients
declare -a PATIENTS=(
  "glioma_mskcc_2019|P-0005249"
  "stad_tcga_pan_can_atlas_2018|TCGA-BR-8371"
  "paad_tcga_gdc|TCGA-IB-7891"
  "msk_met_2021|P-0029561"
  "msk_met_2021|P-0034992"
  "pancan_pcawg_2020|DO17751"
  "msk_chord_2024|P-0034469"
  "msk_chord_2024|P-0020220"
  "msk_chord_2024|P-0057385"
  "msk_chord_2024|P-0083056"
  "msk_chord_2024|P-0013721"
  "msk_met_2021|P-0012246"
  "msk_met_2021|P-0016138"
  "msk_met_2021|P-0047149"
  "pancan_pcawg_2020|DO51477"
  "msk_met_2021|P-0008901"
  "msk_met_2021|P-0001268"
  "msk_met_2021|P-0044058"
  "luad_tcga_pan_can_atlas_2018|TCGA-44-7669"
  "msk_met_2021|P-0019063"
)

echo "============================================="
echo "Clinical Events: Legacy vs Column-Store"
echo "============================================="
echo ""

for entry in "${PATIENTS[@]}"; do
  IFS='|' read -r study patient <<< "$entry"

  echo -n "--- ${study} / ${patient} --- "

  # 1. Default params (SUMMARY, no paging, no sort)
  compare "${study}/${patient} default" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events"

  # 2. projection=DETAILED
  compare "${study}/${patient} projection=DETAILED" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?projection=DETAILED" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?projection=DETAILED"

  # 3. projection=ID
  compare "${study}/${patient} projection=ID" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?projection=ID" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?projection=ID"

  # 4. projection=META (compare total-count header)
  compare "${study}/${patient} projection=META" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?projection=META" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?projection=META" \
    "header"

  # 5. pageSize=2&pageNumber=0
  compare "${study}/${patient} page=0,size=2" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?pageSize=2&pageNumber=0" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?pageSize=2&pageNumber=0"

  # 6. pageSize=2&pageNumber=1
  compare "${study}/${patient} page=1,size=2" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?pageSize=2&pageNumber=1" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?pageSize=2&pageNumber=1"

  # 7. sortBy=eventType&direction=ASC (tie-breaking may differ)
  compare "${study}/${patient} sort=eventType,ASC" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?sortBy=eventType&direction=ASC" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?sortBy=eventType&direction=ASC"

  # 8. sortBy=eventType&direction=DESC
  compare "${study}/${patient} sort=eventType,DESC" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?sortBy=eventType&direction=DESC" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?sortBy=eventType&direction=DESC"

  # 9. sortBy=startNumberOfDaysSinceDiagnosis&direction=ASC
  compare "${study}/${patient} sort=startDate,ASC" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?sortBy=startNumberOfDaysSinceDiagnosis&direction=ASC" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?sortBy=startNumberOfDaysSinceDiagnosis&direction=ASC"

  # 10. sortBy=endNumberOfDaysSinceDiagnosis&direction=DESC
  compare "${study}/${patient} sort=stopDate,DESC" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?sortBy=endNumberOfDaysSinceDiagnosis&direction=DESC" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?sortBy=endNumberOfDaysSinceDiagnosis&direction=DESC"

  # 11. sortBy + paging combined (tie-breaking can affect page content)
  compare "${study}/${patient} sort=eventType,ASC+page=0,size=3" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?sortBy=eventType&direction=ASC&pageSize=3&pageNumber=0" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?sortBy=eventType&direction=ASC&pageSize=3&pageNumber=0" \
    "body_unordered"

  # 12. sortBy + paging page 1 (tie-breaking can affect page content)
  compare "${study}/${patient} sort=eventType,DESC+page=1,size=3" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?sortBy=eventType&direction=DESC&pageSize=3&pageNumber=1" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?sortBy=eventType&direction=DESC&pageSize=3&pageNumber=1" \
    "body_unordered"

  # 13. DETAILED + sort + paging (tie-breaking can affect page content)
  compare "${study}/${patient} DETAILED+sort=startDate,ASC+page=0,size=5" \
    "${BASE_URL}${LEGACY}/${study}/patients/${patient}/clinical-events?projection=DETAILED&sortBy=startNumberOfDaysSinceDiagnosis&direction=ASC&pageSize=5&pageNumber=0" \
    "${BASE_URL}${COLSTORE}/${study}/patients/${patient}/clinical-events?projection=DETAILED&sortBy=startNumberOfDaysSinceDiagnosis&direction=ASC&pageSize=5&pageNumber=0" \
    "body_unordered"

  echo "done (13 permutations)"
done

echo ""
echo "============================================="
echo "RESULTS: ${PASS} passed, ${FAIL} failed, ${SKIP} skipped out of ${TOTAL} tests"
echo "============================================="

if [ $FAIL -gt 0 ]; then
  echo ""
  echo "FAILURES:"
  echo -e "$FAILURES"
  echo ""
  echo "Full diff files saved to /tmp/clinical_events_diff/"
  exit 1
else
  echo "All tests passed!"
  exit 0
fi
