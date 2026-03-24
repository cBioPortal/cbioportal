#!/usr/bin/env bash
# ingest.sh — Migrate cgds_public_staging (ClickHouse) → cbioportal (StarRocks)
#
# Strategy:
#   • All tables: stream ClickHouse HTTP export → StarRocks Stream Load (no temp files)
#   • genetic_alteration (10.3B rows): chunked per study to bound memory
#   • generic_assay_data (410M rows): chunked per profile_type
#   • All other tables: single-shot bulk load
#
# Requirements:
#   • curl
#   • StarRocks running locally via Docker
#   • Environment variables: CH_HOST, CH_USER, CH_PASSWORD
#
# Usage:
#   export CH_HOST="dl96orhu96.us-east-1.aws.clickhouse.cloud"
#   export CH_USER="onurs"
#   export CH_PASSWORD="..."
#   bash ingest.sh [table_name]   # optional: run only one table

set -euo pipefail

# ── Configuration ────────────────────────────────────────────────────────────
CH_HOST="${CH_HOST:?Set CH_HOST to your ClickHouse host}"
CH_USER="${CH_USER:?Set CH_USER}"
CH_PASSWORD="${CH_PASSWORD:?Set CH_PASSWORD}"
CH_DB="cgds_public_staging"
CH_URL="https://${CH_HOST}:8443"

SR_FE_HOST="localhost"
SR_FE_PORT="8040"   # Send directly to BE to avoid FE→BE redirect (curl can't re-stream stdin on redirect)
SR_DB="cbioportal"
SR_USER="root"
SR_PASSWORD=""

ONLY_TABLE="${1:-}"

# ── Helpers ──────────────────────────────────────────────────────────────────

# ch_query QUERY → stdout (CSV with header; --compressed decompresses gzip on the fly)
ch_query() {
    local query="$1"
    curl --fail --silent --compressed \
         --user "${CH_USER}:${CH_PASSWORD}" \
         --get \
         --data-urlencode "query=${query} FORMAT CSVWithNames" \
         "${CH_URL}/"
}

# sr_load TABLE LABEL ← stdin (CSV with header)
# Sends directly to BE (port 8040) to avoid FE→BE redirect which breaks large stdin pipes.
# enclose=" handles CSV fields containing commas (e.g. study descriptions).
sr_load() {
    local table="$1"
    local label="${2:-${table}_$(date +%s)}"
    local filter_ratio="${3:-0.0}"
    local response
    response=$(curl --fail --silent \
         -X PUT \
         -H "label: ${label}" \
         -H "format: CSV" \
         -H "column_separator: ," \
         -H 'enclose: "' \
         -H 'escape: \' \
         -H "skip_header: 1" \
         -H "max_filter_ratio: ${filter_ratio}" \
         -u "${SR_USER}:${SR_PASSWORD}" \
         "http://${SR_FE_HOST}:${SR_FE_PORT}/api/${SR_DB}/${table}/_stream_load" \
         -T -)
    local status
    status=$(echo "$response" | python3 -c "import sys,json; print(json.load(sys.stdin).get('Status','UNKNOWN'))" 2>/dev/null || echo "UNKNOWN")
    if [[ "$status" == "Label Already Exists" ]]; then
        echo "  SKIP — label=${label} already finished, skipping"
        cat > /dev/null  # drain stdin so the cat pipe doesn't get SIGPIPE
        return 0
    fi
    if [[ "$status" != "Success" ]]; then
        echo "  ERROR loading ${table} (label=${label}): $response" >&2
        return 1
    fi
    local rows
    rows=$(echo "$response" | python3 -c "import sys,json; print(json.load(sys.stdin).get('NumberLoadedRows',0))" 2>/dev/null || echo "?")
    echo "  OK — loaded ${rows} rows into ${table}"
}

# ch_load_chunked TABLE SELECT_COLS SOURCE_TABLE [WHERE_CLAUSE] [LABEL_PREFIX]
# Loads a table in 1M-row LIMIT/OFFSET chunks to avoid BE connection timeout on large bodies.
# LABEL_PREFIX defaults to TABLE; pass a unique value when loading the same table multiple times.
ch_load_chunked() {
    local table="$1"
    local select_cols="$2"
    local source_table="$3"
    local where_clause="${4:-}"
    local label_prefix="${5:-${table}}"
    local chunk=1000000
    local offset=0
    local total=0
    local batch=1

    while true; do
        local where_part=""
        [[ -n "$where_clause" ]] && where_part="WHERE ${where_clause} "
        local query="SELECT ${select_cols} FROM ${CH_DB}.${source_table} ${where_part}LIMIT ${chunk} OFFSET ${offset}"
        local label="${label_prefix}_b${batch}"

        # Fast-skip already-finished batches without re-downloading from ClickHouse
        if sr_label_done "$label"; then
            echo "  SKIP — label=${label} already finished"
            total=$((total + chunk))
            offset=$((offset + chunk))
            batch=$((batch + 1))
            continue
        fi

        # Count rows in this chunk by checking if we got any output beyond header
        local tmpfile
        tmpfile=$(mktemp)
        ch_query "$query" > "$tmpfile"
        local nlines
        nlines=$(wc -l < "$tmpfile")

        if (( nlines <= 1 )); then
            rm -f "$tmpfile"
            break  # only header or empty — done
        fi

        cat "$tmpfile" | sr_load "${table}" "${label}"
        rm -f "$tmpfile"

        total=$((total + nlines - 1))
        offset=$((offset + chunk))
        batch=$((batch + 1))
    done
    echo "  Total for ${table}: ${total} rows loaded"
}

# sr_label_done LABEL — returns 0 (true) if label is already FINISHED in StarRocks
sr_label_done() {
    local lbl="$1"
    local state
    state=$(docker exec starrocks mysql -P 9030 -h 127.0.0.1 -u "${SR_USER}" -BN \
        -e "SELECT STATE FROM information_schema.loads WHERE DB_NAME='${SR_DB}' AND LABEL='${lbl}' LIMIT 1;" 2>/dev/null)
    [[ "$state" == "FINISHED" ]]
}

should_run() {
    [[ -z "$ONLY_TABLE" || "$ONLY_TABLE" == "$1" ]]
}

echo "=== StarRocks ingest from ClickHouse cgds_public_staging ==="
echo "    CH: ${CH_URL}"
echo "    SR: http://${SR_FE_HOST}:${SR_FE_PORT}/${SR_DB}"
echo ""

# ── Step 0: Recreate schema (skipped when targeting a single table) ───────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -z "$ONLY_TABLE" ]]; then
    echo "[0/9] Recreating schema..."
    docker exec starrocks mysql -P 9030 -h 127.0.0.1 -u "${SR_USER}" \
        -e "DROP DATABASE IF EXISTS ${SR_DB}; CREATE DATABASE ${SR_DB};"
    docker exec -i starrocks mysql -P 9030 -h 127.0.0.1 -u "${SR_USER}" \
        < "${SCRIPT_DIR}/starrocks.sql"
    docker exec starrocks mysql -P 9030 -h 127.0.0.1 -u "${SR_USER}" \
        -e "ADMIN SET FRONTEND CONFIG ('empty_load_as_error' = 'false');"
    echo "  Schema created."
    echo ""
fi

# ── Step 1: Simple dimension tables (column order matches CH exactly) ─────────
echo "[1/9] Simple dimension tables..."

SIMPLE_TABLES=(
    reference_genome
    cancer_study
    cancer_study_tags
    patient
    sample
    gene
    gene_alias
    genetic_entity
    reference_genome_gene
    genetic_profile
    genetic_profile_link
    genetic_profile_samples
    gene_panel
    gene_panel_list
    sample_profile
    sample_list
    sample_list_list
    geneset
    geneset_gene
    geneset_hierarchy_node
    geneset_hierarchy_leaf
    gistic
    gistic_to_gene
    mut_sig
    resource_patient
    resource_sample
    resource_study
    info
    authorities
    data_access_tokens
    copy_number_seg_file
)

for tbl in "${SIMPLE_TABLES[@]}"; do
    if should_run "$tbl"; then
        echo "  Loading ${tbl}..."
        ch_query "SELECT * FROM ${CH_DB}.${tbl}" \
            | sr_load "${tbl}" "${tbl}_full"
    fi
done

# Tables with column reorder (key columns must come first in SR DDL)
if should_run "clinical_attribute_meta"; then
    echo "  Loading clinical_attribute_meta (reordered)..."
    ch_query "SELECT attr_id, cancer_study_id,
                     display_name, description, datatype, patient_attribute, priority
              FROM ${CH_DB}.clinical_attribute_meta" \
        | sr_load "clinical_attribute_meta" "clinical_attribute_meta_full"
fi

if should_run "resource_definition"; then
    echo "  Loading resource_definition (reordered)..."
    ch_query "SELECT resource_id, cancer_study_id,
                     display_name, description, resource_type, open_by_default, priority, custom_metadata
              FROM ${CH_DB}.resource_definition" \
        | sr_load "resource_definition" "resource_definition_full"
fi

# ── Step 2: Clinical EAV tables ───────────────────────────────────────────────
echo ""
echo "[2/9] Clinical EAV tables..."
for tbl in clinical_patient clinical_sample clinical_event clinical_event_data; do
    if should_run "$tbl"; then
        echo "  Loading ${tbl}..."
        ch_query "SELECT * FROM ${CH_DB}.${tbl}" \
            | sr_load "${tbl}" "${tbl}_full"
    fi
done

# ── Step 3: CNA tables ────────────────────────────────────────────────────────
echo ""
echo "[3/9] CNA tables..."
for tbl in cna_event sample_cna_event copy_number_seg; do
    if should_run "$tbl"; then
        echo "  Loading ${tbl}..."
        ch_query "SELECT * FROM ${CH_DB}.${tbl}" \
            | sr_load "${tbl}" "${tbl}_full"
    fi
done

# ── Step 4: Mutation tables ───────────────────────────────────────────────────
echo ""
echo "[4/9] Mutation tables..."
# mutation_event (9.1M rows), mutation (18.5M), mutation_count_by_keyword (13M): chunked
for tbl in allele_specific_copy_number alteration_driver_annotation; do
    if should_run "$tbl"; then
        echo "  Loading ${tbl}..."
        ch_query "SELECT * FROM ${CH_DB}.${tbl}" \
            | sr_load "${tbl}" "${tbl}_full"
    fi
done
if should_run "mutation_event"; then
    echo "  Loading mutation_event (chunked)..."
    ch_load_chunked "mutation_event" "*" "mutation_event"
fi
if should_run "mutation"; then
    echo "  Loading mutation (chunked)..."
    ch_load_chunked "mutation" "*" "mutation"
fi
if should_run "mutation_count_by_keyword"; then
    echo "  Loading mutation_count_by_keyword (chunked)..."
    ch_load_chunked "mutation_count_by_keyword" "*" "mutation_count_by_keyword"
fi

# ── Step 5: Generic assay / derived dimension tables ─────────────────────────
echo ""
echo "[5/9] Generic assay / derived dimension tables..."
for tbl in gene_panel_to_gene_derived generic_assay_profile_entity_derived; do
    if should_run "$tbl"; then
        echo "  Loading ${tbl}..."
        ch_query "SELECT * FROM ${CH_DB}.${tbl}" \
            | sr_load "${tbl}" "${tbl}_full"
    fi
done
# generic_entity_properties (2.5M rows): chunked
if should_run "generic_entity_properties"; then
    echo "  Loading generic_entity_properties (chunked)..."
    ch_load_chunked "generic_entity_properties" "*" "generic_entity_properties"
fi

# generic_assay_meta_derived: properties is Map(String,String) in CH, exported as JSON string
# StarRocks column is JSON type; toJSONString converts the map to {"k":"v",...} format
if should_run "generic_assay_meta_derived"; then
    echo "  Loading generic_assay_meta_derived..."
    # filter_ratio=0.001: ClickHouse CSV double-quotes JSON strings (""k"":""v"") which is incompatible
    # with SR escape:\; allow up to 0.1% filtered rows (empirically ~1 row in 221K)
    ch_query "SELECT entity_stable_id, entity_type, toJSONString(properties) AS properties
              FROM ${CH_DB}.generic_assay_meta_derived" \
        | sr_load "generic_assay_meta_derived" "generic_assay_meta_derived_full" "0.001"
fi

# ── Step 6: Clinical derived tables (columns reordered to match SR key prefix) ─
echo ""
echo "[6/9] Clinical derived tables..."

# clinical_data_derived (13.9M rows) and clinical_event_data_derived (14.5M): chunked
if should_run "clinical_data_derived"; then
    echo "  Loading clinical_data_derived (chunked, reordered)..."
    ch_load_chunked "clinical_data_derived" \
        "cancer_study_identifier, type, attribute_name, sample_unique_id, internal_id, patient_unique_id, attribute_value" \
        "clinical_data_derived"
fi

if should_run "clinical_event_data_derived"; then
    echo "  Loading clinical_event_data_derived (chunked, reordered)..."
    ch_load_chunked "clinical_event_data_derived" \
        "cancer_study_identifier, event_type, patient_unique_id, key, value, start_date, stop_date" \
        "clinical_event_data_derived"
fi

if should_run "clinical_event_derived"; then
    echo "  Loading clinical_event_derived (reordered)..."
    ch_query "SELECT cancer_study_identifier, event_type, clinical_event_id,
                     patient_id, patient_stable_id, start_date, stop_date
              FROM ${CH_DB}.clinical_event_derived" \
        | sr_load "clinical_event_derived" "clinical_event_derived_full"
fi

# ── Step 7: genomic_event_derived (50M rows) — chunked, reordered ─────────────
echo ""
echo "[7/9] genomic_event_derived (50M rows, chunked)..."
if should_run "genomic_event_derived"; then
    ch_load_chunked "genomic_event_derived" \
        "genetic_profile_stable_id, cancer_study_identifier, variant_type, entrez_gene_id,
         hugo_gene_symbol, sample_unique_id, gene_panel_stable_id, mutation_variant,
         mutation_type, mutation_status, driver_filter, driver_filter_annotation,
         driver_tiers_filter, driver_tiers_filter_annotation, cna_alteration, cna_cytoband,
         sv_event_info, patient_unique_id, off_panel" \
        "genomic_event_derived"
fi

# ── Step 8: mutation_derived (18.5M rows, chunked) ───────────────────────────
# Dotted CH column names aliased; entrezGeneId moved to position 3 (SR key prefix)
MUTATION_DERIVED_COLS='molecularProfileId, sampleId, entrezGeneId, sampleInternalId, patientId,
    studyId, center, mutationStatus, validationStatus, tumorAltCount, tumorRefCount,
    normalAltCount, normalRefCount, aminoAcidChange, chr, startPosition, endPosition,
    referenceAllele, tumorSeqAllele, proteinChange, mutationType, ncbiBuild, variantType,
    refseqMrnaId, proteinPosStart, proteinPosEnd, keyword, annotationJSON,
    driverFilter, driverFilterAnnotation, driverTiersFilter, driverTiersFilterAnnotation,
    `GENE.entrezGeneId` AS gene_entrezGeneId,
    `GENE.hugoGeneSymbol` AS gene_hugoGeneSymbol,
    `GENE.type` AS gene_type,
    `alleleSpecificCopyNumber.ascnIntegerCopyNumber` AS ascn_integerCopyNumber,
    `alleleSpecificCopyNumber.ascnMethod` AS ascn_method,
    `alleleSpecificCopyNumber.ccfExpectedCopiesUpper` AS ascn_ccfExpectedCopiesUpper,
    `alleleSpecificCopyNumber.ccfExpectedCopies` AS ascn_ccfExpectedCopies,
    `alleleSpecificCopyNumber.clonal` AS ascn_clonal,
    `alleleSpecificCopyNumber.minorCopyNumber` AS ascn_minorCopyNumber,
    `alleleSpecificCopyNumber.expectedAltCopies` AS ascn_expectedAltCopies,
    `alleleSpecificCopyNumber.totalCopyNumber` AS ascn_totalCopyNumber'
echo ""
echo "[8/9] mutation_derived (18.5M rows, chunked)..."
if should_run "mutation_derived"; then
    ch_load_chunked "mutation_derived" "$MUTATION_DERIVED_COLS" "mutation_derived"
fi

# ── Step 9a: generic_assay_data (410M rows) — chunked by profile_type ─────────
echo ""
echo "[9a/9] generic_assay_data (410M rows, chunked by profile_type)..."
if should_run "generic_assay_data"; then
    PROFILE_TYPES=()
    while IFS= read -r line; do PROFILE_TYPES+=("$line"); done < <(
        ch_query "SELECT DISTINCT profile_type FROM ${CH_DB}.generic_assay_data_derived" \
        | tail -n +2 | tr -d '"'
    )
    echo "  Found ${#PROFILE_TYPES[@]} profile_types"
    for pt in "${PROFILE_TYPES[@]}"; do
        echo "  Loading profile_type='${pt}'..."
        pt_label="gad_$(echo "$pt" | tr '[:upper:]/ ' '[:lower:]__')"
        # Use chunked load to avoid BE connection timeout on large profile types (methylation = 242M rows)
        ch_load_chunked "generic_assay_data" \
            "profile_type, entity_stable_id, patient_unique_id, sample_unique_id,
             genetic_entity_id, value, generic_assay_type, profile_stable_id,
             datatype, toInt8(patient_level) AS patient_level" \
            "generic_assay_data_derived" \
            "profile_type = '${pt}'" \
            "${pt_label}"
    done
fi

# ── Step 9b: genetic_alteration (10.3B rows) — chunked per study ──────────────
echo ""
echo "[9b/9] genetic_alteration (10.3B rows, chunked by study)..."
if should_run "genetic_alteration"; then
    STUDIES=()
    while IFS= read -r line; do STUDIES+=("$line"); done < <(
        ch_query "SELECT cancer_study_identifier FROM ${CH_DB}.cancer_study" \
        | tail -n +2 | tr -d '"'
    )
    echo "  Found ${#STUDIES[@]} studies"
    DONE=0
    for study in "${STUDIES[@]}"; do
        DONE=$((DONE + 1))
        echo "  [${DONE}/${#STUDIES[@]}] Loading study '${study}'..."
        ch_query "SELECT
            cancer_study_identifier, hugo_gene_symbol, profile_type,
            sample_unique_id, alteration_value
        FROM ${CH_DB}.genetic_alteration_derived
        WHERE cancer_study_identifier = '${study}'" \
            | sr_load "genetic_alteration" "ga_$(echo "$study" | tr '[:upper:]/' '[:lower:]_')_${DONE}"
    done
fi

# ── Verification ──────────────────────────────────────────────────────────────
echo ""
echo "=== Row count verification ==="
VERIFY_TABLES=(
    reference_genome cancer_study patient sample gene genetic_profile
    clinical_attribute_meta resource_definition
    clinical_patient clinical_sample clinical_event clinical_event_data
    cna_event sample_cna_event copy_number_seg
    mutation_event mutation mutation_count_by_keyword
    clinical_data_derived clinical_event_data_derived clinical_event_derived
    genomic_event_derived mutation_derived
    generic_assay_meta_derived generic_assay_profile_entity_derived
    generic_assay_data genetic_alteration
)
for tbl in "${VERIFY_TABLES[@]}"; do
    SR_COUNT=$(docker exec starrocks mysql -P 9030 -h 127.0.0.1 -u "${SR_USER}" \
        -BN -e "SELECT COUNT(*) FROM ${SR_DB}.${tbl};" 2>/dev/null || echo "ERROR")
    echo "  ${tbl}: ${SR_COUNT} rows"
done

echo ""
echo "Done."
