#!/usr/bin/env bash
# StarRocks vs ClickHouse benchmark
# Usage: bash benchmark.sh
# Requires: CH_HOST, CH_USER, CH_PASSWORD env vars

set -euo pipefail

CH_HOST="${CH_HOST:-dl96orhu96.us-east-1.aws.clickhouse.cloud}"
CH_USER="${CH_USER:-onurs}"
CH_PASSWORD="${CH_PASSWORD:?Set CH_PASSWORD}"
CH_DB="cgds_public_staging"
CH_URL="https://${CH_HOST}:8443"

SR_CONTAINER="starrocks"
SR_DB="cbioportal"

STUDIES_SMALL="'laml_tcga'"
STUDIES_MEDIUM="'brca_tcga'"
STUDIES_LARGE="'msk_impact_2017'"
STUDIES_MULTI="'laml_tcga','brca_tcga','msk_impact_2017'"

RUNS=3  # warm runs per query

# ─────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────

ch_time() {
    # Returns elapsed_ms for a ClickHouse query
    local query="$1"
    local total=0
    for i in $(seq 1 $RUNS); do
        local ms
        ms=$(curl -s "${CH_URL}/" \
            --user "${CH_USER}:${CH_PASSWORD}" \
            --data "SELECT elapsed FROM system.query_log WHERE query_id = (
                SELECT query_id FROM system.query_log
                WHERE query LIKE $(printf '%s' "$query" | python3 -c "import sys; s=sys.stdin.read().strip(); print(\"'%\" + s[:40].replace(\"'\",\"''\") + \"%'\")")
                ORDER BY event_time DESC LIMIT 1
            ) LIMIT 1" 2>/dev/null || true)
        # Simpler: just time the HTTP call
        local start end
        start=$(python3 -c "import time; print(int(time.time()*1000))")
        curl -s "${CH_URL}/" \
            --user "${CH_USER}:${CH_PASSWORD}" \
            --data "$query FORMAT Null" > /dev/null 2>&1
        end=$(python3 -c "import time; print(int(time.time()*1000))")
        total=$((total + end - start))
    done
    echo $((total / RUNS))
}

sr_time() {
    local query="$1"
    local total=0
    for i in $(seq 1 $RUNS); do
        local start end
        start=$(python3 -c "import time; print(int(time.time()*1000))")
        docker exec "$SR_CONTAINER" mysql -P 9030 -h 127.0.0.1 -u root \
            -e "USE ${SR_DB}; $query" > /dev/null 2>&1
        end=$(python3 -c "import time; print(int(time.time()*1000))")
        total=$((total + end - start))
    done
    echo $((total / RUNS))
}

ch_rows() {
    curl -s "${CH_URL}/" \
        --user "${CH_USER}:${CH_PASSWORD}" \
        --data "$1" 2>/dev/null | tail -1
}

sr_rows() {
    docker exec "$SR_CONTAINER" mysql -P 9030 -h 127.0.0.1 -u root -BN \
        -e "USE ${SR_DB}; $1" 2>/dev/null | tail -1
}

print_header() {
    echo ""
    echo "════════════════════════════════════════════════════════════════"
    echo "  $1"
    echo "════════════════════════════════════════════════════════════════"
}

run_pair() {
    local label="$1"
    local ch_sql="$2"
    local sr_sql="$3"
    local count_sql_ch="$4"   # a COUNT(*) version for row verification
    local count_sql_sr="$5"

    printf "  %-45s" "$label"

    local ch_ms sr_ms ch_count sr_count
    ch_ms=$(ch_time "$ch_sql")
    sr_ms=$(sr_time "$sr_sql")
    ch_count=$(ch_rows "$count_sql_ch")
    sr_count=$(sr_rows "$count_sql_sr")

    local speedup
    if (( ch_ms > 0 )); then
        speedup=$(python3 -c "print(f'{$ch_ms/$sr_ms:.1f}x')" 2>/dev/null || echo "?")
    else
        speedup="N/A"
    fi

    printf "  CH: %5dms  SR: %5dms  speedup: %6s  rows(CH/SR): %s/%s\n" \
        "$ch_ms" "$sr_ms" "$speedup" "$ch_count" "$sr_count"
}

# ─────────────────────────────────────────────
# B1: Sample count / list by study
# CH uses sample_derived; SR uses sample JOIN patient JOIN cancer_study
# ─────────────────────────────────────────────
print_header "B1 — Sample lookup (sample_derived vs JOIN)"

for STUDIES_LABEL in "small:${STUDIES_SMALL}" "medium:${STUDIES_MEDIUM}" "large:${STUDIES_LARGE}" "multi:${STUDIES_MULTI}"; do
    LABEL="${STUDIES_LABEL%%:*}"
    STUDIES="${STUDIES_LABEL##*:}"

    CH_COUNT="SELECT count() FROM ${CH_DB}.sample_derived WHERE cancer_study_identifier IN (${STUDIES})"
    SR_COUNT="SELECT count(*) FROM sample s JOIN patient p ON s.patient_id = p.internal_id JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id WHERE cs.cancer_study_identifier IN (${STUDIES})"

    run_pair "count [${LABEL}]" \
        "$CH_COUNT" "$SR_COUNT" "$CH_COUNT" "$SR_COUNT"

    CH_LIST="SELECT internal_id, sample_stable_id, patient_stable_id, cancer_study_identifier FROM ${CH_DB}.sample_derived WHERE cancer_study_identifier IN (${STUDIES}) ORDER BY sample_stable_id"
    SR_LIST="SELECT s.internal_id, s.stable_id, p.stable_id, cs.cancer_study_identifier FROM sample s JOIN patient p ON s.patient_id = p.internal_id JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id WHERE cs.cancer_study_identifier IN (${STUDIES}) ORDER BY s.stable_id"

    run_pair "list  [${LABEL}]" \
        "$CH_LIST" "$SR_LIST" \
        "SELECT count() FROM ${CH_DB}.sample_derived WHERE cancer_study_identifier IN (${STUDIES})" \
        "SELECT count(*) FROM sample s JOIN patient p ON s.patient_id = p.internal_id JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id WHERE cs.cancer_study_identifier IN (${STUDIES})"
done

# ─────────────────────────────────────────────
# B2: Clinical data fetch
# CH uses clinical_data_derived; SR uses UNION of clinical_sample + clinical_patient with joins
# Patient attrs are expanded per-sample (matching clinical_data_derived semantics)
# ─────────────────────────────────────────────
print_header "B2 — Clinical data (clinical_data_derived vs JOIN)"

for STUDIES_LABEL in "small:${STUDIES_SMALL}" "medium:${STUDIES_MEDIUM}" "large:${STUDIES_LARGE}" "multi:${STUDIES_MULTI}"; do
    LABEL="${STUDIES_LABEL%%:*}"
    STUDIES="${STUDIES_LABEL##*:}"

    CH_Q="SELECT cancer_study_identifier, type, attribute_name, sample_unique_id, patient_unique_id, attribute_value FROM ${CH_DB}.clinical_data_derived WHERE cancer_study_identifier IN (${STUDIES})"

    # Sample attributes: one row per (sample, attr)
    # Patient attributes: one row per (sample, patient_attr) — expanded per sample, matching CH semantics
    SR_Q="SELECT cs.cancer_study_identifier, 'SAMPLE' AS type, csam.attr_id AS attribute_name,
        CONCAT(cs.cancer_study_identifier,'_',s.stable_id) AS sample_unique_id,
        CONCAT(cs.cancer_study_identifier,'_',p.stable_id) AS patient_unique_id,
        csam.attr_value AS attribute_value
    FROM clinical_sample csam
    JOIN sample s ON csam.internal_id = s.internal_id
    JOIN patient p ON s.patient_id = p.internal_id
    JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
    WHERE cs.cancer_study_identifier IN (${STUDIES})
    UNION ALL
    SELECT cs.cancer_study_identifier, 'PATIENT' AS type, cpat.attr_id AS attribute_name,
        CONCAT(cs.cancer_study_identifier,'_',s.stable_id) AS sample_unique_id,
        CONCAT(cs.cancer_study_identifier,'_',p.stable_id) AS patient_unique_id,
        cpat.attr_value AS attribute_value
    FROM clinical_patient cpat
    JOIN patient p ON cpat.internal_id = p.internal_id
    JOIN sample s ON s.patient_id = p.internal_id
    JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
    WHERE cs.cancer_study_identifier IN (${STUDIES})"

    CH_COUNT="SELECT count() FROM ${CH_DB}.clinical_data_derived WHERE cancer_study_identifier IN (${STUDIES})"
    SR_COUNT="SELECT count(*) FROM (${SR_Q}) t"

    run_pair "all attrs [${LABEL}]" "$CH_Q" "$SR_Q" "$CH_COUNT" "$SR_COUNT"
done

# ─────────────────────────────────────────────
# B3: Mutated genes per study
# CH uses genomic_event_derived; SR joins mutation + mutation_event + gene + genetic_profile + sample + patient + cancer_study
# ─────────────────────────────────────────────
print_header "B3 — Mutated genes (genomic_event_derived vs JOIN)"

for STUDIES_LABEL in "small:${STUDIES_SMALL}" "medium:${STUDIES_MEDIUM}" "large:${STUDIES_LARGE}" "multi:${STUDIES_MULTI}"; do
    LABEL="${STUDIES_LABEL%%:*}"
    STUDIES="${STUDIES_LABEL##*:}"

    CH_Q="SELECT hugo_gene_symbol, count(distinct sample_unique_id) AS n_samples
        FROM ${CH_DB}.genomic_event_derived
        WHERE cancer_study_identifier IN (${STUDIES})
          AND variant_type = 'mutation'
        GROUP BY hugo_gene_symbol
        ORDER BY n_samples DESC
        LIMIT 25"

    SR_Q="SELECT g.hugo_gene_symbol, count(distinct s.internal_id) AS n_samples
        FROM mutation m
        JOIN genetic_profile gp ON m.genetic_profile_id = gp.genetic_profile_id
        JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
        JOIN sample s ON m.sample_id = s.internal_id
        JOIN gene g ON m.entrez_gene_id = g.entrez_gene_id
        WHERE cs.cancer_study_identifier IN (${STUDIES})
        GROUP BY g.hugo_gene_symbol
        ORDER BY n_samples DESC
        LIMIT 25"

    CH_COUNT="SELECT count(distinct hugo_gene_symbol) FROM ${CH_DB}.genomic_event_derived WHERE cancer_study_identifier IN (${STUDIES}) AND variant_type='mutation'"
    SR_COUNT="SELECT count(distinct g.hugo_gene_symbol) FROM mutation m JOIN genetic_profile gp ON m.genetic_profile_id = gp.genetic_profile_id JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id JOIN gene g ON m.entrez_gene_id = g.entrez_gene_id WHERE cs.cancer_study_identifier IN (${STUDIES})"

    run_pair "top25 genes [${LABEL}]" "$CH_Q" "$SR_Q" "$CH_COUNT" "$SR_COUNT"
done

# ─────────────────────────────────────────────
# B4: CNA genes per study
# CH uses genomic_event_derived; SR joins sample_cna_event + cna_event + gene + genetic_profile + sample + patient + cancer_study
# ─────────────────────────────────────────────
print_header "B4 — CNA genes (genomic_event_derived vs JOIN)"

for STUDIES_LABEL in "small:${STUDIES_SMALL}" "medium:${STUDIES_MEDIUM}" "large:${STUDIES_LARGE}" "multi:${STUDIES_MULTI}"; do
    LABEL="${STUDIES_LABEL%%:*}"
    STUDIES="${STUDIES_LABEL##*:}"

    CH_Q="SELECT hugo_gene_symbol, cna_alteration, count(distinct sample_unique_id) AS n_samples
        FROM ${CH_DB}.genomic_event_derived
        WHERE cancer_study_identifier IN (${STUDIES})
          AND variant_type = 'cna'
        GROUP BY hugo_gene_symbol, cna_alteration
        ORDER BY n_samples DESC
        LIMIT 25"

    SR_Q="SELECT g.hugo_gene_symbol, ce.alteration AS cna_alteration, count(distinct s.internal_id) AS n_samples
        FROM sample_cna_event sce
        JOIN cna_event ce ON sce.cna_event_id = ce.cna_event_id
        JOIN genetic_profile gp ON sce.genetic_profile_id = gp.genetic_profile_id
        JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
        JOIN sample s ON sce.sample_id = s.internal_id
        JOIN gene g ON ce.entrez_gene_id = g.entrez_gene_id
        WHERE cs.cancer_study_identifier IN (${STUDIES})
        GROUP BY g.hugo_gene_symbol, ce.alteration
        ORDER BY n_samples DESC
        LIMIT 25"

    CH_COUNT="SELECT count(distinct hugo_gene_symbol) FROM ${CH_DB}.genomic_event_derived WHERE cancer_study_identifier IN (${STUDIES}) AND variant_type='cna'"
    SR_COUNT="SELECT count(distinct g.hugo_gene_symbol) FROM sample_cna_event sce JOIN cna_event ce ON sce.cna_event_id = ce.cna_event_id JOIN genetic_profile gp ON sce.genetic_profile_id = gp.genetic_profile_id JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id JOIN gene g ON ce.entrez_gene_id = g.entrez_gene_id WHERE cs.cancer_study_identifier IN (${STUDIES})"

    run_pair "top25 CNA genes [${LABEL}]" "$CH_Q" "$SR_Q" "$CH_COUNT" "$SR_COUNT"
done

# ─────────────────────────────────────────────
# B5: Gene panel profiling counts
# CH uses sample_to_gene_panel_derived + gene_panel_to_gene_derived
# SR uses sample_profile + gene_panel + gene_panel_list + gene
# ─────────────────────────────────────────────
print_header "B5 — Gene panel profiling (derived vs JOIN)"

for STUDIES_LABEL in "large:${STUDIES_LARGE}" "multi:${STUDIES_MULTI}"; do
    LABEL="${STUDIES_LABEL%%:*}"
    STUDIES="${STUDIES_LABEL##*:}"

    CH_Q="SELECT gptg.entrez_gene_id, count(distinct stgp.sample_unique_id) AS n_profiled
        FROM ${CH_DB}.sample_to_gene_panel_derived stgp
        JOIN ${CH_DB}.gene_panel_to_gene_derived gptg ON stgp.gene_panel_stable_id = gptg.gene_panel_id
        WHERE stgp.cancer_study_identifier IN (${STUDIES})
        GROUP BY gptg.entrez_gene_id
        ORDER BY n_profiled DESC
        LIMIT 25"

    SR_Q="SELECT gl.gene_id AS entrez_gene_id, count(distinct sp.sample_id) AS n_profiled
        FROM sample_profile sp
        JOIN gene_panel gp ON sp.panel_id = gp.internal_id
        JOIN gene_panel_list gl ON gp.internal_id = gl.internal_id
        JOIN genetic_profile prof ON sp.genetic_profile_id = prof.genetic_profile_id
        JOIN cancer_study cs ON prof.cancer_study_id = cs.cancer_study_id
        WHERE cs.cancer_study_identifier IN (${STUDIES})
          AND sp.panel_id IS NOT NULL
        GROUP BY gl.gene_id
        ORDER BY n_profiled DESC
        LIMIT 25"

    CH_COUNT="SELECT count(distinct stgp.sample_unique_id) FROM ${CH_DB}.sample_to_gene_panel_derived stgp WHERE stgp.cancer_study_identifier IN (${STUDIES})"
    SR_COUNT="SELECT count(distinct sp.sample_id) FROM sample_profile sp JOIN genetic_profile prof ON sp.genetic_profile_id = prof.genetic_profile_id JOIN cancer_study cs ON prof.cancer_study_id = cs.cancer_study_id WHERE cs.cancer_study_identifier IN (${STUDIES}) AND sp.panel_id IS NOT NULL"

    run_pair "profiled counts [${LABEL}]" "$CH_Q" "$SR_Q" "$CH_COUNT" "$SR_COUNT"
done

# ─────────────────────────────────────────────
# B6: Generic assay data counts
# Both sides use the exploded table (generic_assay_data / generic_assay_data_derived)
# ─────────────────────────────────────────────
print_header "B6 — Generic assay data counts (exploded table, both sides)"

# brca_aurora_2023 is the study with methylation_hm450 data
METHYL_PROFILE="'brca_aurora_2023_methylation_hm450'"

CH_Q6="SELECT entity_stable_id, count(distinct sample_unique_id) AS n_samples
    FROM ${CH_DB}.generic_assay_data_derived
    WHERE profile_type = 'methylation_hm450'
      AND profile_stable_id IN (${METHYL_PROFILE})
    GROUP BY entity_stable_id
    ORDER BY n_samples DESC
    LIMIT 25"

SR_Q6="SELECT entity_stable_id, count(distinct sample_unique_id) AS n_samples
    FROM generic_assay_data
    WHERE profile_type = 'methylation_hm450'
      AND profile_stable_id IN (${METHYL_PROFILE})
    GROUP BY entity_stable_id
    ORDER BY n_samples DESC
    LIMIT 25"

CH_COUNT6="SELECT count() FROM ${CH_DB}.generic_assay_data_derived WHERE profile_type='methylation_hm450' AND profile_stable_id IN (${METHYL_PROFILE})"
SR_COUNT6="SELECT count(*) FROM generic_assay_data WHERE profile_type='methylation_hm450' AND profile_stable_id IN (${METHYL_PROFILE})"

run_pair "methylation_hm450 [brca_aurora_2023]" "$CH_Q6" "$SR_Q6" "$CH_COUNT6" "$SR_COUNT6"

CH_Q6b="SELECT entity_stable_id, count(distinct sample_unique_id) AS n_samples
    FROM ${CH_DB}.generic_assay_data_derived
    WHERE profile_type = 'phosphoproteome'
    GROUP BY entity_stable_id
    ORDER BY n_samples DESC
    LIMIT 25"

SR_Q6b="SELECT entity_stable_id, count(distinct sample_unique_id) AS n_samples
    FROM generic_assay_data
    WHERE profile_type = 'phosphoproteome'
    GROUP BY entity_stable_id
    ORDER BY n_samples DESC
    LIMIT 25"

CH_COUNT6b="SELECT count() FROM ${CH_DB}.generic_assay_data_derived WHERE profile_type='phosphoproteome'"
SR_COUNT6b="SELECT count(*) FROM generic_assay_data WHERE profile_type='phosphoproteome'"

run_pair "phosphoproteome [all studies]" "$CH_Q6b" "$SR_Q6b" "$CH_COUNT6b" "$SR_COUNT6b"

echo ""
echo "Done."
