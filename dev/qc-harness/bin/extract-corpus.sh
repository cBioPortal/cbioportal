#!/usr/bin/env bash
# Build a replay corpus from real captured traffic (cbioportal_qc.logged_requests)
# for one endpoint pattern. Emits TSV lines: ep<TAB>path<TAB>ct_b64<TAB>qs_b64<TAB>
# headers_b64<TAB>body_b64  (everything base64 so binary/quotes survive).
#
# Usage: extract-corpus.sh <endpoint-LIKE> <label> <count> <out.tsv> [size|random]
#   e.g. extract-corpus.sh '%co-expression%' coexp 80 corpus.tsv random
#        extract-corpus.sh '/api/gene-panel-data/fetch' gpd 150 corpus.tsv size
#
# Filters applied (learned the hard way):
#  - response_status=200 only
#  - clean JSON bodies only: starts {/[ and ends }/], and NO U+FFFD replacement char
#    (the largest logged bodies are gzipped and were byte-corrupted on capture —
#     they are NOT replayable, so we exclude them)
#  - dedup by (query_string, body) — query strings carry projection/threshold/etc.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${HERE}/config.env"

LIKE="$1"; LABEL="$2"; COUNT="$3"; OUT="$4"; ORDER="${5:-size}"
case "$ORDER" in
  size)   ORD="length(body) DESC" ;;
  random) ORD="cityHash64(query_string, body)" ;;
  *) echo "order must be size|random" >&2; exit 1 ;;
esac

SQL="
WITH ranked AS (
  SELECT path, query_string, content_type, headers, body,
         row_number() OVER (ORDER BY ${ORD}) AS rn
  FROM (
    SELECT DISTINCT path, query_string, content_type, headers, body
    FROM ${QC_DB}.${QC_TABLE}
    WHERE method='POST' AND response_status=200 AND endpoint LIKE '${LIKE}'
      AND ((startsWith(body,'{') AND endsWith(body,'}'))
        OR (startsWith(body,'[') AND endsWith(body,']')))
      AND position(body, unhex('EFBFBD'))=0 AND length(body) > 2
  )
)
SELECT arrayStringConcat(groupArray(
  concat('${LABEL}','\t', path, '\t', base64Encode(content_type), '\t',
         base64Encode(query_string), '\t', base64Encode(headers), '\t',
         base64Encode(body))), '\n')
FROM ranked WHERE rn <= ${COUNT}
FORMAT TSVRaw"

# Uses the QC-log cred (SELECT on cbioportal_qc), which is distinct from the
# clone-replay cred (CH_USER). See config.env.
curl -s -u "${QC_CH_USER}:${QC_CH_PASSWORD}" "${QC_CH_HTTP_URL}/?database=${QC_DB}" \
  --data-binary "$SQL" > "$OUT"
if head -c 40 "$OUT" | grep -q 'DB::Exception'; then
  echo "ERROR from ClickHouse:" >&2; head -c 300 "$OUT" >&2; echo >&2; exit 1
fi
echo "wrote $(wc -l < "$OUT") requests -> $OUT"
awk -F'\t' 'NF!=6{bad++} END{if(bad) print "WARNING: "bad" malformed lines"}' "$OUT"
