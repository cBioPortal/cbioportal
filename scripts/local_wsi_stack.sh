#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")"/.. && pwd)"
COMPOSE_DIR="${ROOT_DIR}/../cbioportal-docker-compose"
FRONTEND_DIR="${ROOT_DIR}/../cbioportal-frontend"
TILE_DIR="${ROOT_DIR}/../cbioportal-tile-server"
STUDY_ID="msk_spectrum_tme_2022"
EXPECTED_SAMPLE_IDS="P-0055908-T01-IM6"
COMPOSE_FILES=(-f docker-compose.yml -f docker-compose.wsi-local-dev.yml)
COMPOSE_ENV=(
  CBIOPORTAL_SOURCE_DIR="${ROOT_DIR}"
  FRONTEND_SOURCE_DIR="${FRONTEND_DIR}"
  TILE_SOURCE_DIR="${TILE_DIR}"
)

check_port() {
  local url="$1"
  local retries="${2:-60}"
  local sleep_secs="${3:-2}"
  local i
  for i in $(seq 1 "${retries}"); do
    if curl -fsS "${url}" >/dev/null 2>&1; then
      return 0
    fi
    sleep "${sleep_secs}"
  done
  return 1
}

compose_base() {
  (
    cd "${COMPOSE_DIR}"
    env "${COMPOSE_ENV[@]}" docker compose "${COMPOSE_FILES[@]}" "$@"
  )
}

loaded_sample_ids() {
  docker exec cbioportal-database-container clickhouse-client --query "
    SELECT arrayStringConcat(arraySort(groupArray(s.stable_id)), ',')
    FROM cbioportal.sample AS s
    JOIN cbioportal.patient AS p ON s.patient_id = p.internal_id
    JOIN cbioportal.cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
    WHERE cs.cancer_study_identifier = '${STUDY_ID}'
    FORMAT TabSeparatedRaw
  " 2>/dev/null || true
}

import_minimal_wsi_study() {
  echo "Importing ${STUDY_ID} from docker compose fixture..."
  compose_base run --rm cbioportal \
    metaImport.py -s "/study/${STUDY_ID}" -n -o
}

normalize_minimal_wsi_study() {
  local stale_ids
  stale_ids="$(docker exec cbioportal-database-container clickhouse-client --query "
    SELECT arrayStringConcat(groupArray(toString(s.internal_id)), ',')
    FROM cbioportal.sample AS s
    JOIN cbioportal.patient AS p ON s.patient_id = p.internal_id
    JOIN cbioportal.cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
    WHERE cs.cancer_study_identifier = '${STUDY_ID}'
      AND s.stable_id NOT IN ('P-0055908-T01-IM6')
    FORMAT TabSeparatedRaw
  " 2>/dev/null || true)"

  if [[ -z "${stale_ids}" ]]; then
    return 0
  fi

  echo "Removing stale ${STUDY_ID} sample rows: ${stale_ids}"

  docker exec cbioportal-database-container clickhouse-client --query \
    "ALTER TABLE cbioportal.sample_list_list DELETE WHERE sample_id IN (${stale_ids})"
  docker exec cbioportal-database-container clickhouse-client --query \
    "ALTER TABLE cbioportal.clinical_sample DELETE WHERE internal_id IN (${stale_ids})"
  docker exec cbioportal-database-container clickhouse-client --query \
    "ALTER TABLE cbioportal.sample_derived DELETE WHERE internal_id IN (${stale_ids})"
  docker exec cbioportal-database-container clickhouse-client --query \
    "ALTER TABLE cbioportal.sample DELETE WHERE internal_id IN (${stale_ids})"

  docker exec cbioportal-database-container clickhouse-client --query \
    "OPTIMIZE TABLE cbioportal.sample_list_list FINAL"
  docker exec cbioportal-database-container clickhouse-client --query \
    "OPTIMIZE TABLE cbioportal.clinical_sample FINAL"
  docker exec cbioportal-database-container clickhouse-client --query \
    "OPTIMIZE TABLE cbioportal.sample_derived FINAL"
  docker exec cbioportal-database-container clickhouse-client --query \
    "OPTIMIZE TABLE cbioportal.sample FINAL"
}

reconcile_minimal_wsi_study() {
  local actual_sample_ids
  actual_sample_ids="$(loaded_sample_ids)"

  if [[ -z "${actual_sample_ids}" ]]; then
    import_minimal_wsi_study
    actual_sample_ids="$(loaded_sample_ids)"
  fi

  if [[ "${actual_sample_ids}" != "${EXPECTED_SAMPLE_IDS}" ]]; then
    normalize_minimal_wsi_study
    actual_sample_ids="$(loaded_sample_ids)"
  fi

  if [[ "${actual_sample_ids}" != "${EXPECTED_SAMPLE_IDS}" ]]; then
    echo "Expected ${STUDY_ID} samples ${EXPECTED_SAMPLE_IDS}, got: ${actual_sample_ids:-<none>}" >&2
    return 1
  fi

  echo "${STUDY_ID} fixture ready with samples: ${actual_sample_ids}"
}

ensure_compose_stack_running() {
  echo "Starting all-in-one local WSI compose stack..."
  compose_base up -d --build \
    cbioportal-database \
    cbioportal-session-database \
    cbioportal-session \
    cbioportal \
    redis \
    tile-server \
    frontend

  check_port http://localhost:8080/api/info 120 2
  check_port http://localhost:8081/health 120 2
  check_port http://localhost:3000 180 2
  reconcile_minimal_wsi_study
  echo "Compose stack ready"
}

stop_legacy_local_processes() {
  pkill -f "/Users/rlim/repos/cbioportal/target/cbioportal-exec.jar" >/dev/null 2>&1 || true
  pkill -f "/Users/rlim/repos/cbioportal-frontend/node_modules/.bin/cross-env NODE_ENV=development rspack serve -c rspack.config.js" >/dev/null 2>&1 || true
}

stop_legacy_tile_compose_stack() {
  (
    cd "${TILE_DIR}"
    docker compose down >/dev/null 2>&1 || true
  )
}

status() {
  echo "Frontend (:3000): $(curl -fsS http://localhost:3000 >/dev/null 2>&1 && echo up || echo down)"
  echo "Backend  (:8080): $(curl -fsS http://localhost:8080/api/info >/dev/null 2>&1 && echo up || echo down)"
  echo "Tile     (:8081): $(curl -fsS http://localhost:8081/health >/dev/null 2>&1 && echo up || echo down)"
  echo
  compose_base ps
}

start() {
  stop_legacy_tile_compose_stack
  stop_legacy_local_processes
  ensure_compose_stack_running
  status
}

stop() {
  echo "Stopping compose stack..."
  compose_base down
  stop_legacy_tile_compose_stack
  stop_legacy_local_processes
  status
}

case "${1:-start}" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    stop || true
    start
    ;;
  status)
    status
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}" >&2
    exit 1
    ;;
esac
