#!/usr/bin/env bash
# Boot one cbioportal jar against the configured ClickHouse clone, in clickhouse
# (column-store) mode, with GC logging for the memory measurement.
#
# Usage: run-backend.sh <jar> <label> <port>
# Writes <WORK_DIR>/{app,gc}-<label>.log and <WORK_DIR>/backend-<label>.pid
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${HERE}/config.env"

JAR="$1"; LABEL="$2"; PORT="${3:-8080}"
mkdir -p "$WORK_DIR"
GCLOG="$WORK_DIR/gc-$LABEL.log"; APPLOG="$WORK_DIR/app-$LABEL.log"
rm -f "$GCLOG"

# shellcheck disable=SC2086
java $JVM_OPTS \
  -Xlog:gc*:file="$GCLOG":time,uptime,level,tags \
  -jar "$JAR" \
  --server.port="$PORT" \
  --authenticate=false \
  --clickhouse_mode=true \
  --spring.profiles.active=clickhouse \
  --spring.datasource.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver \
  --spring.datasource.url="$CH_JDBC_URL" \
  --spring.datasource.username="$CH_USER" \
  --spring.datasource.password="$CH_PASSWORD" \
  --spring.datasource.clickhouse.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver \
  --spring.datasource.clickhouse.url="$CH_JDBC_URL" \
  --spring.datasource.clickhouse.username="$CH_USER" \
  --spring.datasource.clickhouse.password="$CH_PASSWORD" \
  > "$APPLOG" 2>&1 &
echo $! > "$WORK_DIR/backend-$LABEL.pid"
echo "started $LABEL pid=$(cat "$WORK_DIR/backend-$LABEL.pid") port=$PORT"

# Wait for readiness (cancer-types is cheap and always present)
for _ in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:$PORT/api/cancer-types?pageSize=1" 2>/dev/null || echo 000)
  [ "$code" = "200" ] && { echo "$LABEL ready on $PORT"; exit 0; }
  sleep 5
done
echo "ERROR: $LABEL did not become ready — see $APPLOG" >&2; exit 1
