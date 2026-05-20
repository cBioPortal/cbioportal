#!/usr/bin/env bash
#
# compare_coexpression_endpoints.sh
#
# Compares legacy co-expression endpoint with the new ClickHouse-backed endpoint.
# Discovers test cases dynamically from the running cBioPortal instance.
#
# Usage: bash scripts/compare_coexpression_endpoints.sh [BASE_URL]
#   BASE_URL defaults to http://localhost:8082

set -euo pipefail

BASE_URL="${1:-http://localhost:8082}"
LEGACY_PATH="/api/molecular-profiles/co-expressions/fetch"
NEW_PATH="/api/column-store/molecular-profiles/co-expressions/fetch"

# Output files
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_DIR="coexpression_comparison_${TIMESTAMP}"
mkdir -p "$REPORT_DIR"
CSV_FILE="${REPORT_DIR}/results.csv"
SUMMARY_FILE="${REPORT_DIR}/summary.txt"
LOG_FILE="${REPORT_DIR}/run.log"

# Reference genes: name and entrezGeneId (parallel arrays)
GENE_NAMES=(TP53 BRCA1 CDKN2A EGFR KRAS MYC PIK3CA PTEN RB1 APC)
GENE_IDS=(7157 672 1029 1956 3845 4609 5290 5728 5925 324)

# Counters
TOTAL=0
EXACT=0
APPROX=0
MISMATCH=0
ERRORS=0
TOTAL_LEGACY_MS=0
TOTAL_NEW_MS=0
MAX_CORR_DELTA=0
MAX_CORR_DELTA_CASE=""
SUM_CORR_DELTA=0
CORR_DELTA_COUNT=0

log() {
  echo "[$(date '+%H:%M:%S')] $*" | tee -a "$LOG_FILE"
}

# Write CSV header
echo "test_id,profile_a,profile_b,sample_list,gene_name,entrez_id,legacy_count,new_count,overlap_count,overlap_pct,max_corr_delta,mean_corr_delta,median_corr_delta,max_pval_delta,legacy_ms,new_ms,status" > "$CSV_FILE"

log "Starting co-expression endpoint comparison"
log "Base URL: $BASE_URL"
log "Report directory: $REPORT_DIR"

# ------------------------------------------------------------------
# Step 1: Discover molecular profiles
# ------------------------------------------------------------------
log "Step 1: Discovering molecular profiles..."
PROFILES_JSON=$(curl -sf "${BASE_URL}/api/molecular-profiles?direction=ASC&pageSize=10000" \
  -H "Accept: application/json")

if [ -z "$PROFILES_JSON" ]; then
  log "ERROR: Failed to fetch molecular profiles"
  exit 1
fi

# Filter to MRNA_EXPRESSION and PROTEIN_LEVEL profiles
EXPRESSION_PROFILES=$(echo "$PROFILES_JSON" | jq -r '[.[] | select(.molecularAlterationType == "MRNA_EXPRESSION" or .molecularAlterationType == "PROTEIN_LEVEL")]')
PROFILE_COUNT=$(echo "$EXPRESSION_PROFILES" | jq 'length')
log "Found $PROFILE_COUNT expression/protein profiles"

if [ "$PROFILE_COUNT" -eq 0 ]; then
  log "ERROR: No expression profiles found"
  exit 1
fi

# Extract unique study IDs
STUDY_IDS=$(echo "$EXPRESSION_PROFILES" | jq -r '.[].studyId' | sort -u)
STUDY_COUNT=$(echo "$STUDY_IDS" | wc -l | tr -d ' ')
log "Across $STUDY_COUNT studies"

# ------------------------------------------------------------------
# Step 2: Build test cases
# ------------------------------------------------------------------
log "Step 2: Building test cases..."

# We'll store test cases as lines: profileA|profileB|sampleListId|geneName|entrezId
TEST_CASES_FILE="${REPORT_DIR}/test_cases.txt"
> "$TEST_CASES_FILE"

CASE_COUNT=0
MAX_CASES=40

# Rotate through genes across studies for diversity
GENE_INDEX=0

for STUDY_ID in $STUDY_IDS; do
  if [ "$CASE_COUNT" -ge "$MAX_CASES" ]; then
    break
  fi

  # Get profiles for this study (pick first one only for speed)
  STUDY_PROFILES=$(echo "$EXPRESSION_PROFILES" | jq -r --arg sid "$STUDY_ID" '[.[] | select(.studyId == $sid)]')
  STUDY_PROFILE_ID=$(echo "$STUDY_PROFILES" | jq -r '.[0].molecularProfileId')

  # Get sample lists for this study
  SAMPLE_LISTS_JSON=$(curl -sf "${BASE_URL}/api/studies/${STUDY_ID}/sample-lists" \
    -H "Accept: application/json" 2>/dev/null || echo "[]")

  if [ "$SAMPLE_LISTS_JSON" = "[]" ] || [ -z "$SAMPLE_LISTS_JSON" ]; then
    log "  Skipping study $STUDY_ID: no sample lists"
    continue
  fi

  # Pick one sample list: prefer _all, then any match
  SAMPLE_LIST_ID=$(echo "$SAMPLE_LISTS_JSON" | jq -r '.[].sampleListId' | \
    grep -E '(_all$|rna_seq|mrna)' | head -1)

  if [ -z "$SAMPLE_LIST_ID" ]; then
    SAMPLE_LIST_ID=$(echo "$SAMPLE_LISTS_JSON" | jq -r '.[0].sampleListId')
  fi

  # One gene per study, rotating through the list
  GENE_NAME="${GENE_NAMES[$GENE_INDEX]}"
  ENTREZ_ID="${GENE_IDS[$GENE_INDEX]}"
  GENE_INDEX=$(( (GENE_INDEX + 1) % ${#GENE_NAMES[@]} ))

  echo "${STUDY_PROFILE_ID}|${STUDY_PROFILE_ID}|${SAMPLE_LIST_ID}|${GENE_NAME}|${ENTREZ_ID}" >> "$TEST_CASES_FILE"
  CASE_COUNT=$((CASE_COUNT + 1))
done

log "Generated $CASE_COUNT test cases"

# ------------------------------------------------------------------
# Step 3: Execute test cases and compare
# ------------------------------------------------------------------
log "Step 3: Executing test cases..."

# Helper: compute stats using awk
compute_stats() {
  # reads lines of numbers from stdin, outputs max|mean|median
  sort -g | awk '
  {
    vals[NR] = $1
    sum += $1
    if ($1 > max) max = $1
    n = NR
  }
  END {
    if (n == 0) { print "0|0|0"; exit }
    mean = sum / n
    if (n % 2 == 1) median = vals[int(n/2)+1]
    else median = (vals[n/2] + vals[n/2+1]) / 2
    printf "%.8f|%.8f|%.8f\n", max, mean, median
  }'
}

TEST_ID=0
while IFS='|' read -r PROFILE_A PROFILE_B SAMPLE_LIST GENE_NAME ENTREZ_ID; do
  TEST_ID=$((TEST_ID + 1))
  TOTAL=$((TOTAL + 1))

  # Build request body
  BODY=$(jq -n --arg sampleListId "$SAMPLE_LIST" --argjson entrezGeneId "$ENTREZ_ID" \
    '{sampleListId: $sampleListId, entrezGeneId: $entrezGeneId}')

  PARAMS="molecularProfileIdA=${PROFILE_A}&molecularProfileIdB=${PROFILE_B}&threshold=0"

  # Call legacy endpoint
  LEGACY_START=$(python3 -c 'import time; print(int(time.time()*1000))')
  LEGACY_RESPONSE=$(curl -sf -w '\n%{http_code}' \
    "${BASE_URL}${LEGACY_PATH}?${PARAMS}" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d "$BODY" 2>/dev/null || echo -e '\n000')
  LEGACY_END=$(python3 -c 'import time; print(int(time.time()*1000))')
  LEGACY_MS=$((LEGACY_END - LEGACY_START))

  LEGACY_HTTP=$(echo "$LEGACY_RESPONSE" | tail -1)
  LEGACY_JSON=$(echo "$LEGACY_RESPONSE" | sed '$d')

  # Call new endpoint
  NEW_START=$(python3 -c 'import time; print(int(time.time()*1000))')
  NEW_RESPONSE=$(curl -sf -w '\n%{http_code}' \
    "${BASE_URL}${NEW_PATH}?${PARAMS}" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d "$BODY" 2>/dev/null || echo -e '\n000')
  NEW_END=$(python3 -c 'import time; print(int(time.time()*1000))')
  NEW_MS=$((NEW_END - NEW_START))

  NEW_HTTP=$(echo "$NEW_RESPONSE" | tail -1)
  NEW_JSON=$(echo "$NEW_RESPONSE" | sed '$d')

  TOTAL_LEGACY_MS=$((TOTAL_LEGACY_MS + LEGACY_MS))
  TOTAL_NEW_MS=$((TOTAL_NEW_MS + NEW_MS))

  # Check for HTTP errors
  if [ "$LEGACY_HTTP" != "200" ] || [ "$NEW_HTTP" != "200" ]; then
    ERRORS=$((ERRORS + 1))
    echo "${TEST_ID},${PROFILE_A},${PROFILE_B},${SAMPLE_LIST},${GENE_NAME},${ENTREZ_ID},ERR(${LEGACY_HTTP}),ERR(${NEW_HTTP}),,,,,,,${LEGACY_MS},${NEW_MS},HTTP_ERROR" >> "$CSV_FILE"
    log "  [${TEST_ID}/${CASE_COUNT}] ${PROFILE_A} / ${GENE_NAME}: HTTP error (legacy=${LEGACY_HTTP}, new=${NEW_HTTP})"
    continue
  fi

  # Count results
  LEGACY_COUNT=$(echo "$LEGACY_JSON" | jq 'length' 2>/dev/null || echo 0)
  NEW_COUNT=$(echo "$NEW_JSON" | jq 'length' 2>/dev/null || echo 0)

  # Handle empty results
  if [ "$LEGACY_COUNT" -eq 0 ] && [ "$NEW_COUNT" -eq 0 ]; then
    EXACT=$((EXACT + 1))
    echo "${TEST_ID},${PROFILE_A},${PROFILE_B},${SAMPLE_LIST},${GENE_NAME},${ENTREZ_ID},0,0,0,100,0,0,0,0,${LEGACY_MS},${NEW_MS},EXACT_EMPTY" >> "$CSV_FILE"
    log "  [${TEST_ID}/${CASE_COUNT}] ${PROFILE_A} / ${GENE_NAME}: both empty (${LEGACY_MS}ms / ${NEW_MS}ms)"
    continue
  fi

  # Write responses to temp files to avoid shell quoting issues with large JSON
  LEGACY_FILE="${REPORT_DIR}/tmp_legacy_${TEST_ID}.json"
  NEW_FILE="${REPORT_DIR}/tmp_new_${TEST_ID}.json"
  echo "$LEGACY_JSON" > "$LEGACY_FILE"
  echo "$NEW_JSON" > "$NEW_FILE"

  # Compare results using python for numerical precision
  COMPARISON=$(python3 -c "
import json, sys, statistics

with open('$LEGACY_FILE') as f: legacy = json.load(f)
with open('$NEW_FILE') as f: new = json.load(f)

legacy_map = {item['geneticEntityId']: item for item in legacy}
new_map = {item['geneticEntityId']: item for item in new}

legacy_ids = set(legacy_map.keys())
new_ids = set(new_map.keys())
overlap_ids = legacy_ids & new_ids

overlap_count = len(overlap_ids)
total_unique = len(legacy_ids | new_ids)
overlap_pct = (overlap_count / total_unique * 100) if total_unique > 0 else 0

corr_deltas = []
pval_deltas = []

for gid in overlap_ids:
    l = legacy_map[gid]
    n = new_map[gid]
    corr_delta = abs(float(l['spearmansCorrelation']) - float(n['spearmansCorrelation']))
    corr_deltas.append(corr_delta)
    pval_delta = abs(float(l['pValue']) - float(n['pValue']))
    pval_deltas.append(pval_delta)

if corr_deltas:
    max_corr = max(corr_deltas)
    mean_corr = statistics.mean(corr_deltas)
    median_corr = statistics.median(corr_deltas)
    max_pval = max(pval_deltas)
else:
    max_corr = mean_corr = median_corr = max_pval = 0

print(f'{overlap_count}|{overlap_pct:.1f}|{max_corr:.8f}|{mean_corr:.8f}|{median_corr:.8f}|{max_pval:.8f}')
" 2>/dev/null || echo "ERROR")

  # Clean up temp files
  rm -f "$LEGACY_FILE" "$NEW_FILE"

  if [ "$COMPARISON" = "ERROR" ]; then
    ERRORS=$((ERRORS + 1))
    echo "${TEST_ID},${PROFILE_A},${PROFILE_B},${SAMPLE_LIST},${GENE_NAME},${ENTREZ_ID},${LEGACY_COUNT},${NEW_COUNT},,,,,,,${LEGACY_MS},${NEW_MS},PARSE_ERROR" >> "$CSV_FILE"
    log "  [${TEST_ID}/${CASE_COUNT}] ${PROFILE_A} / ${GENE_NAME}: parse error"
    continue
  fi

  IFS='|' read -r OVERLAP_COUNT OVERLAP_PCT MAX_CORR_D MEAN_CORR_D MEDIAN_CORR_D MAX_PVAL_D <<< "$COMPARISON"

  # Accumulate aggregate stats
  SUM_CORR_DELTA=$(python3 -c "print(${SUM_CORR_DELTA} + ${MEAN_CORR_D})")
  CORR_DELTA_COUNT=$((CORR_DELTA_COUNT + 1))

  # Determine status
  STATUS="APPROX"
  if [ "$LEGACY_COUNT" -eq "$NEW_COUNT" ]; then
    # Check if max correlation delta is effectively zero
    IS_EXACT=$(python3 -c "print('yes' if ${MAX_CORR_D} < 1e-10 else 'no')")
    if [ "$IS_EXACT" = "yes" ]; then
      STATUS="EXACT"
      EXACT=$((EXACT + 1))
    else
      APPROX=$((APPROX + 1))
    fi
  else
    # Different counts - check if correlation deltas are still small
    IS_CLOSE=$(python3 -c "print('yes' if ${MAX_CORR_D} < 0.05 else 'no')")
    if [ "$IS_CLOSE" = "yes" ]; then
      APPROX=$((APPROX + 1))
    else
      STATUS="MISMATCH"
      MISMATCH=$((MISMATCH + 1))
    fi
  fi

  # Track worst case
  IS_WORSE=$(python3 -c "print('yes' if ${MAX_CORR_D} > ${MAX_CORR_DELTA} else 'no')")
  if [ "$IS_WORSE" = "yes" ]; then
    MAX_CORR_DELTA="$MAX_CORR_D"
    MAX_CORR_DELTA_CASE="${PROFILE_A}/${GENE_NAME}"
  fi

  echo "${TEST_ID},${PROFILE_A},${PROFILE_B},${SAMPLE_LIST},${GENE_NAME},${ENTREZ_ID},${LEGACY_COUNT},${NEW_COUNT},${OVERLAP_COUNT},${OVERLAP_PCT},${MAX_CORR_D},${MEAN_CORR_D},${MEDIAN_CORR_D},${MAX_PVAL_D},${LEGACY_MS},${NEW_MS},${STATUS}" >> "$CSV_FILE"

  log "  [${TEST_ID}/${CASE_COUNT}] ${PROFILE_A} / ${GENE_NAME}: ${STATUS} (legacy=${LEGACY_COUNT}, new=${NEW_COUNT}, overlap=${OVERLAP_PCT}%, maxΔ=${MAX_CORR_D}, ${LEGACY_MS}ms/${NEW_MS}ms)"

done < "$TEST_CASES_FILE"

# ------------------------------------------------------------------
# Step 4: Generate summary report
# ------------------------------------------------------------------
log ""
log "===== SUMMARY ====="

AVG_CORR_DELTA="0"
if [ "$CORR_DELTA_COUNT" -gt 0 ]; then
  AVG_CORR_DELTA=$(python3 -c "print(f'{${SUM_CORR_DELTA} / ${CORR_DELTA_COUNT}:.8f}')")
fi

AVG_LEGACY_MS=0
AVG_NEW_MS=0
LATENCY_RATIO="N/A"
if [ "$TOTAL" -gt 0 ]; then
  AVG_LEGACY_MS=$((TOTAL_LEGACY_MS / TOTAL))
  AVG_NEW_MS=$((TOTAL_NEW_MS / TOTAL))
  if [ "$AVG_LEGACY_MS" -gt 0 ]; then
    LATENCY_RATIO=$(python3 -c "print(f'{${AVG_NEW_MS} / ${AVG_LEGACY_MS}:.2f}x')")
  fi
fi

{
  echo "Co-Expression Endpoint Comparison Report"
  echo "========================================="
  echo "Date: $(date)"
  echo "Base URL: $BASE_URL"
  echo ""
  echo "Test Cases"
  echo "----------"
  echo "Total:           $TOTAL"
  echo "Exact matches:   $EXACT"
  echo "Approx matches:  $APPROX"
  echo "Mismatches:      $MISMATCH"
  echo "Errors:          $ERRORS"
  echo ""
  echo "Correlation Deltas (overlapping genes)"
  echo "---------------------------------------"
  echo "Mean delta:      $AVG_CORR_DELTA"
  echo "Worst delta:     $MAX_CORR_DELTA"
  echo "Worst case:      $MAX_CORR_DELTA_CASE"
  echo ""
  echo "Latency"
  echo "-------"
  echo "Avg legacy:      ${AVG_LEGACY_MS}ms"
  echo "Avg new:         ${AVG_NEW_MS}ms"
  echo "Ratio (new/old): $LATENCY_RATIO"
  echo ""
  echo "Detailed results: $CSV_FILE"
} | tee "$SUMMARY_FILE" | tee -a "$LOG_FILE"

# Flag cases with large deltas
FLAGGED=$(awk -F',' 'NR>1 && $11+0 > 0.05 {print $0}' "$CSV_FILE")
if [ -n "$FLAGGED" ]; then
  echo "" | tee -a "$SUMMARY_FILE" "$LOG_FILE"
  echo "WARNING: Cases with correlation delta > 0.05:" | tee -a "$SUMMARY_FILE" "$LOG_FILE"
  echo "$FLAGGED" | tee -a "$SUMMARY_FILE" "$LOG_FILE"
fi

log ""
log "Done. Results saved to $REPORT_DIR/"
