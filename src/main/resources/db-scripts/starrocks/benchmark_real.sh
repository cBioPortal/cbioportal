#!/usr/bin/env bash
# Head-to-head benchmark using REAL production queries from CH system.query_log
# Queries run identically on both engines (SR now has views matching CH derived table names)
#
# CH adaptations:  table names prefixed with cgds_public_staging.
# SR adaptations:  replaceOne() → REPLACE(); IN ([...]) → IN (...); no FORMAT clause
#
# Usage: CH_PASSWORD=... bash benchmark_real.sh

CH_HOST="${CH_HOST:-dl96orhu96.us-east-1.aws.clickhouse.cloud}"
CH_USER="${CH_USER:-onurs}"
CH_PASSWORD="${CH_PASSWORD:?Set CH_PASSWORD}"
DB="cgds_public_staging"
CH_URL="https://${CH_HOST}:8443"

SR_CONTAINER="starrocks"
SR_DB="cbioportal"

RUNS=3

# ── helpers ─────────────────────────────────────────────────────────────────

ch_time() {
    local query="$1"
    local total=0 start end ms
    for i in $(seq 1 $RUNS); do
        start=$(python3 -c "import time; print(int(time.time()*1000))")
        curl -sf "${CH_URL}/" \
            --user "${CH_USER}:${CH_PASSWORD}" \
            --data "$query FORMAT Null" > /dev/null
        end=$(python3 -c "import time; print(int(time.time()*1000))")
        total=$((total + end - start))
    done
    echo $((total / RUNS))
}

sr_time() {
    local query="$1"
    local total=0 start end
    for i in $(seq 1 $RUNS); do
        start=$(python3 -c "import time; print(int(time.time()*1000))")
        docker exec "$SR_CONTAINER" mysql -P 9030 -h 127.0.0.1 -u root \
            -e "USE ${SR_DB}; $query" > /dev/null 2>&1
        end=$(python3 -c "import time; print(int(time.time()*1000))")
        total=$((total + end - start))
    done
    echo $((total / RUNS))
}

ch_rows() {
    curl -sf "${CH_URL}/" --user "${CH_USER}:${CH_PASSWORD}" \
        --data "$1" 2>/dev/null | tail -1
}

sr_rows() {
    docker exec "$SR_CONTAINER" mysql -P 9030 -h 127.0.0.1 -u root -BN \
        -e "USE ${SR_DB}; $1" 2>/dev/null | tail -1
}

run_pair() {
    local label="$1" ch_sql="$2" sr_sql="$3"
    local count_ch="$4" count_sr="$5"
    printf "  %-52s" "$label"
    local ch_ms sr_ms ch_count sr_count speedup
    ch_ms=$(ch_time "$ch_sql")
    sr_ms=$(sr_time "$sr_sql")
    ch_count=$(ch_rows "$count_ch")
    sr_count=$(sr_rows "$count_sr")
    if (( sr_ms > 0 )); then
        speedup=$(python3 -c "print(f'{$ch_ms/$sr_ms:.2f}x')" 2>/dev/null || echo "?")
    else
        speedup="∞"
    fi
    printf "  CH:%5dms  SR:%5dms  SR_speedup:%6s  rows(CH/SR): %s / %s\n" \
        "$ch_ms" "$sr_ms" "$speedup" "$ch_count" "$sr_count"
}

hdr() { echo ""; echo "══════════════════════════════════════════════════════════════════════"; echo "  $1"; echo "══════════════════════════════════════════════════════════════════════"; }

# ════════════════════════════════════════════════════════════════════════════
# Q1 — CNA event fetch (sample_cna_event 6-table join)
#      Most-executed query shape in production (39K calls/day, 12.8B total ms)
#      Filters via sample_list subquery; returns raw CNA rows per sample/gene
# ════════════════════════════════════════════════════════════════════════════
hdr "Q1 — CNA event fetch via sample_cna_event (most frequent production query)"

CNA_COLS_BOTH='
    cna_event.entrez_gene_id as entrezGeneId,
    cna_event.alteration AS alteration,
    genetic_profile.stable_id AS molecularProfileId,
    sample.stable_id AS sampleId,
    patient.stable_id AS patientId,
    cancer_study.cancer_study_identifier AS studyId,
    alteration_driver_annotation.driver_filter AS driverFilter'

CNA_JOINS_CH="
FROM sample_cna_event
INNER JOIN ${DB}.cna_event ON cna_event.cna_event_id = sample_cna_event.cna_event_id
INNER JOIN ${DB}.genetic_profile ON sample_cna_event.genetic_profile_id = genetic_profile.genetic_profile_id
INNER JOIN ${DB}.sample ON sample_cna_event.sample_id = sample.internal_id
INNER JOIN ${DB}.patient ON sample.patient_id = patient.internal_id
INNER JOIN ${DB}.cancer_study ON patient.cancer_study_id = cancer_study.cancer_study_id
LEFT JOIN ${DB}.alteration_driver_annotation ON
    sample_cna_event.genetic_profile_id = alteration_driver_annotation.genetic_profile_id
    AND sample_cna_event.sample_id = alteration_driver_annotation.sample_id
    AND sample_cna_event.cna_event_id = alteration_driver_annotation.alteration_event_id"

CNA_JOINS_SR="
FROM sample_cna_event
INNER JOIN cna_event ON cna_event.cna_event_id = sample_cna_event.cna_event_id
INNER JOIN genetic_profile ON sample_cna_event.genetic_profile_id = genetic_profile.genetic_profile_id
INNER JOIN sample ON sample_cna_event.sample_id = sample.internal_id
INNER JOIN patient ON sample.patient_id = patient.internal_id
INNER JOIN cancer_study ON patient.cancer_study_id = cancer_study.cancer_study_id
LEFT JOIN alteration_driver_annotation ON
    sample_cna_event.genetic_profile_id = alteration_driver_annotation.genetic_profile_id
    AND sample_cna_event.sample_id = alteration_driver_annotation.sample_id
    AND sample_cna_event.cna_event_id = alteration_driver_annotation.alteration_event_id"

for spec in \
    "luad_tcga [~500 samples, no gene filter]:luad_tcga_gistic:luad_tcga_all" \
    "laml_tcga [~200 samples, no gene filter]:laml_tcga_gistic:laml_tcga_all" \
    "brca_tcga_pan_can_atlas_2018 [~1000 samples]:brca_tcga_pan_can_atlas_2018_gistic:brca_tcga_pan_can_atlas_2018_all"; do

    LABEL="${spec%%:*}"
    PROFILE="${spec#*:}"; PROFILE="${PROFILE%%:*}"
    SLIST="${spec##*:}"

    WHERE_CH="WHERE sample_cna_event.genetic_profile_id = (
                SELECT genetic_profile_id FROM ${DB}.genetic_profile gp WHERE gp.stable_id = '${PROFILE}')
            AND sample_cna_event.sample_id IN (
                SELECT sample_list_list.sample_id FROM ${DB}.sample_list_list
                INNER JOIN ${DB}.sample_list ON sample_list_list.list_id = sample_list.list_id
                WHERE sample_list.stable_id = '${SLIST}')
            AND cna_event.alteration IN (-2,2)"

    WHERE_SR="WHERE sample_cna_event.genetic_profile_id = (
                SELECT genetic_profile_id FROM genetic_profile gp WHERE gp.stable_id = '${PROFILE}')
            AND sample_cna_event.sample_id IN (
                SELECT sample_list_list.sample_id FROM sample_list_list
                INNER JOIN sample_list ON sample_list_list.list_id = sample_list.list_id
                WHERE sample_list.stable_id = '${SLIST}')
            AND cna_event.alteration IN (-2,2)"

    run_pair "$LABEL" \
        "SELECT ${CNA_COLS_BOTH} ${CNA_JOINS_CH} ${WHERE_CH}" \
        "SELECT ${CNA_COLS_BOTH} ${CNA_JOINS_SR} ${WHERE_SR}" \
        "SELECT count() FROM ${DB}.sample_cna_event ${WHERE_CH}" \
        "SELECT count(*) FROM sample_cna_event ${WHERE_SR}"
done

# ════════════════════════════════════════════════════════════════════════════
# Q2 — Sample lookup via sample_derived view
#      Returns sample + patient metadata with sequenced/cnv flags.
#      In CH: reads pre-computed derived table.
#      In SR: view expands to sample JOIN patient JOIN cancer_study + subqueries.
# ════════════════════════════════════════════════════════════════════════════
hdr "Q2 — Sample metadata via sample_derived view"

SD_COLS='sample_derived.internal_id as internalId,
    sample_stable_id as stableId, patient_stable_id as patientStableId,
    sample_derived.cancer_study_identifier as cancerStudyIdentifier,
    sample_unique_id_base64 as uniqueSampleKey,
    patient_unique_id_base64 as uniquePatientKey,
    sample_type as sampleType, sequenced, copy_number_segment_present'

for spec in \
    "laml_tcga [200 samples]:laml_tcga_all" \
    "brca_tcga [1108 samples]:brca_tcga_all" \
    "msk_impact_2017 [10945 samples]:msk_impact_2017_all"; do

    LABEL="${spec%%:*}"
    SLIST="${spec##*:}"

    CH_Q="SELECT ${SD_COLS}
        FROM ${DB}.sample_derived
        WHERE sample_derived.internal_id IN (
            SELECT sample_list_list.sample_id FROM ${DB}.sample_list_list
            INNER JOIN ${DB}.sample_list ON sample_list_list.list_id = sample_list.list_id
            WHERE sample_list.stable_id IN (['${SLIST}']))"

    SR_Q="SELECT ${SD_COLS}
        FROM sample_derived
        WHERE sample_derived.internal_id IN (
            SELECT sample_list_list.sample_id FROM sample_list_list
            INNER JOIN sample_list ON sample_list_list.list_id = sample_list.list_id
            WHERE sample_list.stable_id IN ('${SLIST}'))"

    run_pair "$LABEL" "$CH_Q" "$SR_Q" \
        "SELECT count() FROM ${DB}.sample_derived WHERE sample_derived.internal_id IN (SELECT sample_list_list.sample_id FROM ${DB}.sample_list_list INNER JOIN ${DB}.sample_list ON sample_list_list.list_id = sample_list.list_id WHERE sample_list.stable_id IN (['${SLIST}']))" \
        "SELECT count(*) FROM sample_derived WHERE sample_derived.internal_id IN (SELECT sample_list_list.sample_id FROM sample_list_list INNER JOIN sample_list ON sample_list_list.list_id = sample_list.list_id WHERE sample_list.stable_id IN ('${SLIST}'))"
done

# ════════════════════════════════════════════════════════════════════════════
# Q3 — Mutation frequency per gene (genomic_event_derived)
#      Counts altered samples per gene for the mutation frequency table.
#      In CH: pre-computed flat table with all join columns materialized.
#      In SR: view expands to mutation+mutation_event+sample_profile+...+gene JOINs.
# ════════════════════════════════════════════════════════════════════════════
hdr "Q3 — Mutation frequency per gene (genomic_event_derived, variant_type=mutation)"

MUT_FREQ_COLS='hugo_gene_symbol as hugoGeneSymbol, entrez_gene_id as entrezGeneId,
    COUNT(DISTINCT sample_unique_id) as numberOfAlteredCases,
    COUNT(DISTINCT CASE WHEN off_panel = 0 THEN sample_unique_id END) as numberOfAlteredCasesOnPanel,
    COUNT(*) as totalCount'

for spec in \
    "laml_tcga [200 samples]:laml_tcga" \
    "cesc_tcga [~200 samples]:cesc_tcga" \
    "brca_tcga [1108 samples]:brca_tcga"; do

    LABEL="${spec%%:*}"
    STUDY="${spec##*:}"

    SUBQ_CH="SELECT sample_unique_id FROM ${DB}.sample_derived WHERE cancer_study_identifier IN ('${STUDY}')"
    SUBQ_SR="SELECT sample_unique_id FROM sample_derived WHERE cancer_study_identifier IN ('${STUDY}')"

    CH_Q="SELECT ${MUT_FREQ_COLS}
        FROM ${DB}.genomic_event_derived
        WHERE variant_type = 'mutation' AND mutation_status != 'UNCALLED'
          AND sample_unique_id IN (${SUBQ_CH})
        GROUP BY entrez_gene_id, hugo_gene_symbol
        ORDER BY totalCount DESC, hugo_gene_symbol ASC"

    SR_Q="SELECT ${MUT_FREQ_COLS}
        FROM genomic_event_derived
        WHERE variant_type = 'mutation' AND mutation_status != 'UNCALLED'
          AND sample_unique_id IN (${SUBQ_SR})
        GROUP BY entrez_gene_id, hugo_gene_symbol
        ORDER BY totalCount DESC, hugo_gene_symbol ASC"

    run_pair "$LABEL" "$CH_Q" "$SR_Q" \
        "SELECT count() FROM ${DB}.genomic_event_derived WHERE variant_type='mutation' AND sample_unique_id IN (${SUBQ_CH})" \
        "SELECT count(*) FROM genomic_event_derived WHERE variant_type='mutation' AND sample_unique_id IN (${SUBQ_SR})"
done

# ════════════════════════════════════════════════════════════════════════════
# Q4 — CNA frequency per gene with cytoband (genomic_event_derived)
#      Powers the CNA frequency bar chart in study view.
# ════════════════════════════════════════════════════════════════════════════
hdr "Q4 — CNA frequency per gene with cytoband (genomic_event_derived, variant_type=cna)"

CNA_FREQ_COLS='hugo_gene_symbol as hugoGeneSymbol, entrez_gene_id as entrezGeneId,
    cna_alteration as alteration, cna_cytoband as cytoband,
    COUNT(DISTINCT sample_unique_id) as numberOfAlteredCases,
    COUNT(DISTINCT CASE WHEN off_panel = 0 THEN sample_unique_id END) as numberOfAlteredCasesOnPanel,
    COUNT(*) as totalCount'

for spec in \
    "laml_tcga [200 samples]:laml_tcga" \
    "brca_tcga [1108 samples]:brca_tcga" \
    "ccle_broad_2019 [~1700 samples]:ccle_broad_2019"; do

    LABEL="${spec%%:*}"
    STUDY="${spec##*:}"

    SUBQ_CH="SELECT sample_unique_id FROM ${DB}.sample_derived WHERE cancer_study_identifier IN ('${STUDY}')"
    SUBQ_SR="SELECT sample_unique_id FROM sample_derived WHERE cancer_study_identifier IN ('${STUDY}')"

    CH_Q="SELECT ${CNA_FREQ_COLS}
        FROM ${DB}.genomic_event_derived
        WHERE variant_type = 'cna'
          AND sample_unique_id IN (${SUBQ_CH})
        GROUP BY entrez_gene_id, hugo_gene_symbol, alteration, cytoband
        ORDER BY totalCount DESC, hugo_gene_symbol ASC"

    SR_Q="SELECT ${CNA_FREQ_COLS}
        FROM genomic_event_derived
        WHERE variant_type = 'cna'
          AND sample_unique_id IN (${SUBQ_SR})
        GROUP BY entrez_gene_id, hugo_gene_symbol, alteration, cytoband
        ORDER BY totalCount DESC, hugo_gene_symbol ASC"

    run_pair "$LABEL" "$CH_Q" "$SR_Q" \
        "SELECT count() FROM ${DB}.genomic_event_derived WHERE variant_type='cna' AND sample_unique_id IN (${SUBQ_CH})" \
        "SELECT count(*) FROM genomic_event_derived WHERE variant_type='cna' AND sample_unique_id IN (${SUBQ_SR})"
done

# ════════════════════════════════════════════════════════════════════════════
# Q4b — Study-view queries with real production patterns
#       Two variants seen in query_log:
#         (a) multi-study IN list (33 brain cancer studies) — large cross-study scan
#         (b) profile-filtered INTERSECT (samples with both mrna+mutations profiles)
# ════════════════════════════════════════════════════════════════════════════
hdr "Q4b — Study view: multi-study CNA genes (33 brain studies, ~real production load)"

BRAIN_STUDIES="'odg_msk_2017','lgg_tcga','lgg_tcga_pan_can_atlas_2018','gbm_mayo_pdx_sarkaria_2019','brain_cptac_gdc','difg_glass','difg_glass_2019','difg_tcga_gdc','gbm_cptac_2021','gbm_columbia_2019','gbm_iatlas_prins_2019','gbm_tcga_pub2013','gbm_tcga_pub','gbm_tcga_gdc','gbm_tcga','gbm_tcga_pan_can_atlas_2018','glioma_mskcc_2019','glioma_msk_2018','difg_msk_2023','lgg_ucsf_2014','mbl_broad_2012','mbl_dkfz_2017','mbl_icgc','mbl_pcgp','mbl_sickkids_2016','mng_utoronto_2021','lgggbm_tcga_pub','mnet_tcga_gdc','brain_cptac_2020','lgg_ctf_synodos_2025','pcpg_tcga','past_dkfz_heidelberg_2013','ptad_msk_2024'"

SUBQ_CH_BRAIN="SELECT sample_unique_id FROM ${DB}.sample_derived WHERE cancer_study_identifier IN (${BRAIN_STUDIES})"
SUBQ_SR_BRAIN="SELECT sample_unique_id FROM sample_derived WHERE cancer_study_identifier IN (${BRAIN_STUDIES})"

run_pair "cna-genes [33 brain studies]" \
    "SELECT ${CNA_FREQ_COLS} FROM ${DB}.genomic_event_derived WHERE variant_type = 'cna' AND sample_unique_id IN (${SUBQ_CH_BRAIN}) GROUP BY entrez_gene_id, hugo_gene_symbol, alteration, cytoband ORDER BY totalCount DESC, hugo_gene_symbol ASC" \
    "SELECT ${CNA_FREQ_COLS} FROM genomic_event_derived WHERE variant_type = 'cna' AND sample_unique_id IN (${SUBQ_SR_BRAIN}) GROUP BY entrez_gene_id, hugo_gene_symbol, alteration, cytoband ORDER BY totalCount DESC, hugo_gene_symbol ASC" \
    "SELECT count() FROM ${DB}.genomic_event_derived WHERE variant_type='cna' AND sample_unique_id IN (${SUBQ_CH_BRAIN})" \
    "SELECT count(*) FROM genomic_event_derived WHERE variant_type='cna' AND sample_unique_id IN (${SUBQ_SR_BRAIN})"

run_pair "mutated-genes [33 brain studies]" \
    "SELECT ${MUT_FREQ_COLS} FROM ${DB}.genomic_event_derived WHERE variant_type = 'mutation' AND mutation_status != 'UNCALLED' AND sample_unique_id IN (${SUBQ_CH_BRAIN}) GROUP BY entrez_gene_id, hugo_gene_symbol ORDER BY totalCount DESC, hugo_gene_symbol ASC" \
    "SELECT ${MUT_FREQ_COLS} FROM genomic_event_derived WHERE variant_type = 'mutation' AND mutation_status != 'UNCALLED' AND sample_unique_id IN (${SUBQ_SR_BRAIN}) GROUP BY entrez_gene_id, hugo_gene_symbol ORDER BY totalCount DESC, hugo_gene_symbol ASC" \
    "SELECT count() FROM ${DB}.genomic_event_derived WHERE variant_type='mutation' AND sample_unique_id IN (${SUBQ_CH_BRAIN})" \
    "SELECT count(*) FROM genomic_event_derived WHERE variant_type='mutation' AND sample_unique_id IN (${SUBQ_SR_BRAIN})"

hdr "Q4c — Study view: profile-filtered INTERSECT (brca_tcga, samples with mrna+mutations)"

INTERSECT_BRCA_CH="SELECT sample_unique_id FROM ${DB}.sample_derived WHERE cancer_study_identifier IN ('brca_tcga')
    INTERSECT
    SELECT sample_derived.sample_unique_id FROM ${DB}.sample_profile
    JOIN ${DB}.genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
    JOIN ${DB}.sample_derived ON sample_profile.sample_id = sample_derived.internal_id
    WHERE sample_derived.cancer_study_identifier IN ('brca_tcga') AND gp.stable_id = 'brca_tcga_mrna'
    INTERSECT
    SELECT sample_derived.sample_unique_id FROM ${DB}.sample_profile
    JOIN ${DB}.genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
    JOIN ${DB}.sample_derived ON sample_profile.sample_id = sample_derived.internal_id
    WHERE sample_derived.cancer_study_identifier IN ('brca_tcga') AND gp.stable_id = 'brca_tcga_mutations'"

INTERSECT_BRCA_SR="SELECT sample_unique_id FROM sample_derived WHERE cancer_study_identifier IN ('brca_tcga')
    INTERSECT
    SELECT sample_derived.sample_unique_id FROM sample_profile
    JOIN genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
    JOIN sample_derived ON sample_profile.sample_id = sample_derived.internal_id
    WHERE sample_derived.cancer_study_identifier IN ('brca_tcga') AND gp.stable_id = 'brca_tcga_mrna'
    INTERSECT
    SELECT sample_derived.sample_unique_id FROM sample_profile
    JOIN genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
    JOIN sample_derived ON sample_profile.sample_id = sample_derived.internal_id
    WHERE sample_derived.cancer_study_identifier IN ('brca_tcga') AND gp.stable_id = 'brca_tcga_mutations'"

run_pair "cna-genes [brca_tcga, mrna∩mutations filter]" \
    "SELECT ${CNA_FREQ_COLS} FROM ${DB}.genomic_event_derived WHERE variant_type = 'cna' AND sample_unique_id IN (${INTERSECT_BRCA_CH}) GROUP BY entrez_gene_id, hugo_gene_symbol, alteration, cytoband ORDER BY totalCount DESC, hugo_gene_symbol ASC" \
    "SELECT ${CNA_FREQ_COLS} FROM genomic_event_derived WHERE variant_type = 'cna' AND sample_unique_id IN (${INTERSECT_BRCA_SR}) GROUP BY entrez_gene_id, hugo_gene_symbol, alteration, cytoband ORDER BY totalCount DESC, hugo_gene_symbol ASC" \
    "SELECT count() FROM ${DB}.genomic_event_derived WHERE variant_type='cna' AND sample_unique_id IN (${INTERSECT_BRCA_CH})" \
    "SELECT count(*) FROM genomic_event_derived WHERE variant_type='cna' AND sample_unique_id IN (${INTERSECT_BRCA_SR})"

run_pair "mutated-genes [brca_tcga, mrna∩mutations filter]" \
    "SELECT ${MUT_FREQ_COLS} FROM ${DB}.genomic_event_derived WHERE variant_type = 'mutation' AND mutation_status != 'UNCALLED' AND sample_unique_id IN (${INTERSECT_BRCA_CH}) GROUP BY entrez_gene_id, hugo_gene_symbol ORDER BY totalCount DESC, hugo_gene_symbol ASC" \
    "SELECT ${MUT_FREQ_COLS} FROM genomic_event_derived WHERE variant_type = 'mutation' AND mutation_status != 'UNCALLED' AND sample_unique_id IN (${INTERSECT_BRCA_SR}) GROUP BY entrez_gene_id, hugo_gene_symbol ORDER BY totalCount DESC, hugo_gene_symbol ASC" \
    "SELECT count() FROM ${DB}.genomic_event_derived WHERE variant_type='mutation' AND sample_unique_id IN (${INTERSECT_BRCA_CH})" \
    "SELECT count(*) FROM genomic_event_derived WHERE variant_type='mutation' AND sample_unique_id IN (${INTERSECT_BRCA_SR})"

# ════════════════════════════════════════════════════════════════════════════
# Q5 — Clinical sample attributes for specific attrs (clinical_data_derived)
#      Powers scatter plots and clinical attribute filtering in study view.
#      Both CH (flat derived table) and SR (view with attr cross-join + LEFT JOIN)
#      run the same WHERE sample_unique_id IN (...) pattern.
# ════════════════════════════════════════════════════════════════════════════
hdr "Q5 — Clinical sample attributes (clinical_data_derived, FRACTION_GENOME_ALTERED + MUTATION_COUNT)"

CLIN_SAMPLE_COLS="internal_id as internalId,
    REPLACE(sample_unique_id, CONCAT(cancer_study_identifier, '_'), '') as sampleId,
    REPLACE(patient_unique_id, CONCAT(cancer_study_identifier, '_'), '') as patientId,
    attribute_name as attrId, attribute_value as attrValue,
    cancer_study_identifier as studyId"

# CH uses replaceOne; SR uses REPLACE (same result when prefix appears once)
CLIN_SAMPLE_COLS_CH="internal_id as internalId,
    replaceOne(sample_unique_id, concat(cancer_study_identifier, '_'), '') as sampleId,
    replaceOne(patient_unique_id, concat(cancer_study_identifier, '_'), '') as patientId,
    attribute_name as attrId, attribute_value as attrValue,
    cancer_study_identifier as studyId"

for spec in \
    "laml_tcga [200 samples]:laml_tcga" \
    "brca_tcga [1108 samples]:brca_tcga" \
    "msk_impact_2017 [10945 samples]:msk_impact_2017"; do

    LABEL="${spec%%:*}"
    STUDY="${spec##*:}"

    SUBQ_CH="SELECT sample_unique_id FROM ${DB}.sample_derived WHERE cancer_study_identifier IN ('${STUDY}')"
    SUBQ_SR="SELECT sample_unique_id FROM sample_derived WHERE cancer_study_identifier IN ('${STUDY}')"

    CH_Q="SELECT ${CLIN_SAMPLE_COLS_CH}
        FROM ${DB}.clinical_data_derived
        WHERE sample_unique_id IN (${SUBQ_CH})
          AND cancer_study_identifier IN ('${STUDY}')
          AND attribute_name IN ('FRACTION_GENOME_ALTERED', 'MUTATION_COUNT')
          AND type = 'sample'"

    SR_Q="SELECT ${CLIN_SAMPLE_COLS}
        FROM clinical_data_derived
        WHERE sample_unique_id IN (${SUBQ_SR})
          AND cancer_study_identifier IN ('${STUDY}')
          AND attribute_name IN ('FRACTION_GENOME_ALTERED', 'MUTATION_COUNT')
          AND type = 'sample'"

    run_pair "$LABEL" "$CH_Q" "$SR_Q" \
        "SELECT count() FROM ${DB}.clinical_data_derived WHERE sample_unique_id IN (${SUBQ_CH}) AND cancer_study_identifier IN ('${STUDY}') AND attribute_name IN ('FRACTION_GENOME_ALTERED','MUTATION_COUNT') AND type='sample'" \
        "SELECT count(*) FROM clinical_data_derived WHERE sample_unique_id IN (${SUBQ_SR}) AND cancer_study_identifier IN ('${STUDY}') AND attribute_name IN ('FRACTION_GENOME_ALTERED','MUTATION_COUNT') AND type='sample'"
done

# ════════════════════════════════════════════════════════════════════════════
# Q6 — Clinical data with INTERSECT sample filter
#      More complex: sample set is filtered by both study AND presence in two
#      genetic profiles (mutations + gistic). Uses INTERSECT — tests SR's
#      set-operation pushdown vs CH's native INTERSECT support.
# ════════════════════════════════════════════════════════════════════════════
hdr "Q6 — Clinical data with INTERSECT sample filter (laml_tcga, 2-profile intersection)"

INTERSECT_FILTER_CH() {
    local STUDY="$1"
    echo "SELECT sample_unique_id FROM ${DB}.sample_derived WHERE cancer_study_identifier IN ('${STUDY}')
    INTERSECT
    SELECT sample_derived.sample_unique_id
    FROM ${DB}.sample_profile
    JOIN ${DB}.genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
    JOIN ${DB}.sample_derived ON sample_profile.sample_id = sample_derived.internal_id
    WHERE sample_derived.cancer_study_identifier IN ('${STUDY}')
      AND gp.stable_id = concat('${STUDY}', '_', 'mutations')
    INTERSECT
    SELECT sample_derived.sample_unique_id
    FROM ${DB}.sample_profile
    JOIN ${DB}.genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
    JOIN ${DB}.sample_derived ON sample_profile.sample_id = sample_derived.internal_id
    WHERE sample_derived.cancer_study_identifier IN ('${STUDY}')
      AND gp.stable_id = concat('${STUDY}', '_', 'gistic')"
}

INTERSECT_FILTER_SR() {
    local STUDY="$1"
    echo "SELECT sample_unique_id FROM sample_derived WHERE cancer_study_identifier IN ('${STUDY}')
    INTERSECT
    SELECT sample_derived.sample_unique_id
    FROM sample_profile
    JOIN genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
    JOIN sample_derived ON sample_profile.sample_id = sample_derived.internal_id
    WHERE sample_derived.cancer_study_identifier IN ('${STUDY}')
      AND gp.stable_id = CONCAT('${STUDY}', '_', 'mutations')
    INTERSECT
    SELECT sample_derived.sample_unique_id
    FROM sample_profile
    JOIN genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
    JOIN sample_derived ON sample_profile.sample_id = sample_derived.internal_id
    WHERE sample_derived.cancer_study_identifier IN ('${STUDY}')
      AND gp.stable_id = CONCAT('${STUDY}', '_', 'gistic')"
}

for spec in \
    "laml_tcga (mutation+gistic intersection) [sample attrs]:laml_tcga:sample" \
    "laml_tcga (mutation+gistic intersection) [patient attrs]:laml_tcga:patient"; do

    LABEL="${spec%%:*}"
    STUDY="${spec#*:}"; STUDY="${STUDY%%:*}"
    ATYPE="${spec##*:}"

    FILTER_CH=$(INTERSECT_FILTER_CH "$STUDY")
    FILTER_SR=$(INTERSECT_FILTER_SR "$STUDY")

    if [ "$ATYPE" = "sample" ]; then
        CH_Q="SELECT ${CLIN_SAMPLE_COLS_CH}
            FROM ${DB}.clinical_data_derived
            WHERE sample_unique_id IN (${FILTER_CH})
              AND cancer_study_identifier IN ('${STUDY}')
              AND attribute_name IN ('FRACTION_GENOME_ALTERED','MUTATION_COUNT')
              AND type = 'sample'"
        SR_Q="SELECT ${CLIN_SAMPLE_COLS}
            FROM clinical_data_derived
            WHERE sample_unique_id IN (${FILTER_SR})
              AND cancer_study_identifier IN ('${STUDY}')
              AND attribute_name IN ('FRACTION_GENOME_ALTERED','MUTATION_COUNT')
              AND type = 'sample'"
    else
        CH_Q="SELECT ${CLIN_SAMPLE_COLS_CH}
            FROM ${DB}.clinical_data_derived
            WHERE patient_unique_id IN (
                SELECT patient_unique_id FROM ${DB}.sample_derived
                WHERE sample_unique_id IN (${FILTER_CH}))
              AND cancer_study_identifier IN ('${STUDY}')
              AND attribute_name IN ('FRACTION_GENOME_ALTERED','MUTATION_COUNT')
              AND type = 'patient'"
        SR_Q="SELECT ${CLIN_SAMPLE_COLS}
            FROM clinical_data_derived
            WHERE patient_unique_id IN (
                SELECT patient_unique_id FROM sample_derived
                WHERE sample_unique_id IN (${FILTER_SR}))
              AND cancer_study_identifier IN ('${STUDY}')
              AND attribute_name IN ('FRACTION_GENOME_ALTERED','MUTATION_COUNT')
              AND type = 'patient'"
    fi

    run_pair "$LABEL [$ATYPE]" "$CH_Q" "$SR_Q" \
        "SELECT count() FROM ${DB}.clinical_data_derived WHERE cancer_study_identifier IN ('${STUDY}') AND attribute_name IN ('FRACTION_GENOME_ALTERED','MUTATION_COUNT') AND type='${ATYPE}'" \
        "SELECT count(*) FROM clinical_data_derived WHERE cancer_study_identifier IN ('${STUDY}') AND attribute_name IN ('FRACTION_GENOME_ALTERED','MUTATION_COUNT') AND type='${ATYPE}'"
done

echo ""
echo "Done."
