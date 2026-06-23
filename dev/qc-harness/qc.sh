#!/usr/bin/env bash
# End-to-end A/B QC for a cbioportal PR: build two jars, replay real captured
# traffic for an endpoint against each (sequentially — one -Xmx2g JVM at a time),
# and report parity + latency + peak heap.
#
# Usage: qc.sh <old-ref> <new-ref> <endpoint-LIKE> <label> [count] [order]
#   e.g. qc.sh master coexpression-single-scan-query '%co-expression%' coexp 80 random
#
# Prereqs: cp config.env.example config.env  (and fill in CH_PASSWORD)
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${HERE}/config.env"
OLD_REF="$1"; NEW_REF="$2"; LIKE="$3"; LABEL="$4"; COUNT="${5:-80}"; ORDER="${6:-random}"
mkdir -p "$WORK_DIR"; cd "$WORK_DIR"

echo "== 1/5 build jars =="
"$HERE/bin/build-ab.sh" "$OLD_REF" "$NEW_REF"

echo "== 2/5 extract corpus =="
"$HERE/bin/extract-corpus.sh" "$LIKE" "$LABEL" "$COUNT" "$WORK_DIR/corpus.tsv" "$ORDER"

replay_side() {  # $1=OLD|NEW  $2=jar  $3=port
  "$HERE/bin/run-backend.sh" "$2" "$1" "$3"
  # warmup pass (warms JIT + ClickHouse cache), then the measured pass
  python3 "$HERE/lib/replay.py" --corpus "$WORK_DIR/corpus.tsv" \
      --base-url "http://localhost:$3" --out /tmp/warm-$1.jsonl 2>/dev/null || true
  python3 "$HERE/lib/replay.py" --corpus "$WORK_DIR/corpus.tsv" \
      --base-url "http://localhost:$3" --out "$WORK_DIR/results-$1.jsonl"
  python3 "$HERE/lib/gcstats.py" "$WORK_DIR/gc-$1.log" "$((COUNT*2))" > "$WORK_DIR/mem-$1.txt" || true
  kill "$(cat "$WORK_DIR/backend-$1.pid")" 2>/dev/null || true
  while kill -0 "$(cat "$WORK_DIR/backend-$1.pid")" 2>/dev/null; do sleep 1; done
}

echo "== 3/5 replay OLD =="; replay_side OLD "$WORK_DIR/cbioportal-OLD.jar" 8111
echo "== 4/5 replay NEW =="; replay_side NEW "$WORK_DIR/cbioportal-NEW.jar" 8112

echo "== 5/5 compare =="
python3 "$HERE/lib/compare.py" "$WORK_DIR/results-OLD.jsonl" "$WORK_DIR/results-NEW.jsonl"
echo; echo "--- peak heap / alloc (OLD) ---"; cat "$WORK_DIR/mem-OLD.txt"
echo "--- peak heap / alloc (NEW) ---"; cat "$WORK_DIR/mem-NEW.txt"
echo; echo "artifacts in $WORK_DIR"
