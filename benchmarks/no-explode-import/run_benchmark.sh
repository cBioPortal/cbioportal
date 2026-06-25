#!/usr/bin/env bash
# No-explode import benchmark: direct exploded load vs. packed + ARRAY JOIN derive.
# Loads a real datahub study (ccle_broad_2019: discrete CNA + continuous RPKM) two ways and
# compares latency, server memory, storage, and correctness.
#
# Requires: the `clickhouse` binary on PATH (client + local) and:
#   CH_HOST, CH_USER, CH_PASSWORD   -- connection (native protocol, secure port 9440)
#   CH_DB                           -- database holding the tables from ddl.sql (default: bench_noexplode)
set -euo pipefail

CH_DB="${CH_DB:-bench_noexplode}"
CL="clickhouse client --host ${CH_HOST} --port 9440 --secure --user ${CH_USER} --password ${CH_PASSWORD}"
M="https://media.githubusercontent.com/media/cBioPortal/datahub/master/public/ccle_broad_2019"

echo "## download source matrices"
[ -f ccle_cna.txt ]  || curl -s "$M/data_cna.txt"           -o ccle_cna.txt
[ -f ccle_rpkm.txt ] || curl -s "$M/data_mrna_seq_rpkm.txt" -o ccle_rpkm.txt

echo "## create tables"; $CL --multiquery < ddl.sql

# wide-grid -> exploded rows, streamed via native protocol. $1=file $2=profile_type $3=target_table
direct_load() {
  clickhouse local --query "
    SELECT sample_id, arr[1] AS hugo_gene_symbol, '$2' AS profile_type, if(value='',NULL,value) AS value
    FROM (SELECT splitByChar('\t', line) AS arr FROM file('$1', LineAsString) WHERE line NOT LIKE 'Hugo_Symbol%') AS body
    CROSS JOIN (SELECT arraySlice(splitByChar('\t', line),2) AS samples FROM file('$1', LineAsString) WHERE line LIKE 'Hugo_Symbol%') AS hdr
    ARRAY JOIN arraySlice(arr,2) AS value, samples AS sample_id
    WHERE value != 'NA' FORMAT Native" \
  | $CL --query "INSERT INTO ${CH_DB}.$3 FORMAT Native"
}

# wide-grid -> packed (one row/gene, comma-joined values) + per-profile sample order. $1=file $2=profile_type
packed_load() {
  clickhouse local --query "
    SELECT arr[1] AS hugo_gene_symbol, '$2' AS profile_type, arrayStringConcat(arraySlice(arr,2), ',') AS \`values\`
    FROM (SELECT splitByChar('\t', line) AS arr FROM file('$1', LineAsString) WHERE line NOT LIKE 'Hugo_Symbol%')
    FORMAT Native" \
  | $CL --query "INSERT INTO ${CH_DB}.packed FORMAT Native"
  clickhouse local --query "
    SELECT '$2' AS profile_type, arraySlice(splitByChar('\t', line),2) AS sample_ids
    FROM file('$1', LineAsString) WHERE line LIKE 'Hugo_Symbol%' FORMAT Native" \
  | $CL --query "INSERT INTO ${CH_DB}.sample_order FORMAT Native"
}

# server-side ARRAY JOIN derive for one profile. $1=profile_type
derive() {
  $CL --query "
    INSERT INTO ${CH_DB}.exploded_legacy
    SELECT sample_id, p.hugo_gene_symbol, p.profile_type, if(v='',NULL,v) AS value
    FROM ${CH_DB}.packed AS p
    INNER JOIN ${CH_DB}.sample_order AS so ON so.profile_type = p.profile_type
    ARRAY JOIN splitByChar(',', p.\`values\`) AS v, so.sample_ids AS sample_id
    WHERE p.profile_type='$1' AND v != 'NA'
    SETTINGS log_comment='bench_derive_$1'"
}

for prof in "ccle_cna.txt cna" "ccle_rpkm.txt rpkm"; do
  set -- $prof; file=$1; ptype=$2
  echo "## === $ptype ==="
  echo "[no-explode] direct load";        /usr/bin/time -f '  wall=%es rss=%MKB' bash -c "$(declare -f direct_load); CL='$CL' CH_DB='$CH_DB' direct_load $file $ptype exploded_direct"
  echo "[legacy] packed load";            /usr/bin/time -f '  wall=%es rss=%MKB' bash -c "$(declare -f packed_load); CL='$CL' CH_DB='$CH_DB' packed_load $file $ptype"
  echo "[legacy] ARRAY JOIN derive";      /usr/bin/time -f '  wall=%es'          bash -c "$(declare -f derive);      CL='$CL' CH_DB='$CH_DB' derive $ptype"
done

echo "## OPTIMIZE for storage measurement"
for t in packed sample_order exploded_direct exploded_legacy; do $CL --query "OPTIMIZE TABLE ${CH_DB}.$t FINAL"; done

echo "## correctness (direct vs legacy must match)"
$CL --query "
SELECT profile_type,
  sum(cityHash64(sample_id,hugo_gene_symbol,profile_type,ifNull(value,'N'))) FROM ${CH_DB}.exploded_direct GROUP BY profile_type ORDER BY profile_type"
$CL --query "
SELECT profile_type,
  sum(cityHash64(sample_id,hugo_gene_symbol,profile_type,ifNull(value,'N'))) FROM ${CH_DB}.exploded_legacy GROUP BY profile_type ORDER BY profile_type"

echo "## storage"
$CL --query "
SELECT table, formatReadableQuantity(sum(rows)) rows, formatReadableSize(sum(bytes_on_disk)) disk
FROM system.parts WHERE database='${CH_DB}' AND active GROUP BY table ORDER BY table"

echo "## server peak memory of derives"
$CL --query "SYSTEM FLUSH LOGS"
$CL --query "
SELECT log_comment, formatReadableSize(max(memory_usage)) peak_mem, max(query_duration_ms) dur_ms
FROM system.query_log WHERE log_comment LIKE 'bench_derive_%' AND type='QueryFinish' GROUP BY log_comment"
