#!/usr/bin/env bash
# Rehearse a cBioPortal ClickHouse schema migration on an isolated clone of
# the active production database. This script never writes to SOURCE_DATABASE.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MIGRATOR="$ROOT_DIR/src/main/resources/db-scripts/clickhouse/migrate/migrate_db.py"
MIGRATION_SQL="$ROOT_DIR/src/main/resources/db-scripts/clickhouse/migrate/migrate_schema.sql"

usage() {
  cat <<'USAGE'
Usage:
  CLICKHOUSE_HOST=... CLICKHOUSE_NATIVE_PORT=9440 CLICKHOUSE_USER=... \
  CLICKHOUSE_PASSWORD=... CLICKHOUSE_SECURE=true \
  SOURCE_DATABASE=cbioportal_msk_green \
  REHEARSAL_DATABASE=cbioportal_msk_migration_rehearsal_<candidate_sha> \
  CANDIDATE_IMAGE_DIGEST=cbioportal/cbioportal@sha256:... \
  scripts/rehearse_clickhouse_production_clone.sh

Alternatively resolve SOURCE_DATABASE from the update-management database:
  UPDATE_MANAGEMENT_DATABASE=mskcc_update_management_database \
  PORTAL_DATABASE=msk SOURCE_DATABASE_PREFIX=cbioportal_msk_

Safety rules:
  * REHEARSAL_DATABASE must contain "migration_rehearsal" and must differ
    from SOURCE_DATABASE.
  * An existing non-empty rehearsal database is refused unless
    ALLOW_REPLACE_REHEARSAL=1 is explicitly set.
  * Derived tables and cbioportal_sequence_state are cloned as schema only;
    derived tables are rebuilt by migrate_db.py after migrations complete.
USAGE
}

case "${1:-}" in
  -h|--help) usage; exit 0 ;;
esac

require() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "missing required environment variable: $name" >&2
    exit 2
  fi
}

identifier() {
  [[ "$1" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]]
}

sql_literal() {
  printf "%s" "$1" | sed "s/'/''/g"
}

yaml_value() {
  printf "%s" "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

for variable in CLICKHOUSE_HOST CLICKHOUSE_NATIVE_PORT CLICKHOUSE_USER CLICKHOUSE_PASSWORD \
  REHEARSAL_DATABASE CANDIDATE_IMAGE_DIGEST; do
  require "$variable"
done

if [[ ! -f "$MIGRATOR" || ! -f "$MIGRATION_SQL" ]]; then
  echo "candidate migration files are missing from $ROOT_DIR" >&2
  exit 2
fi

if [[ -z "${SOURCE_DATABASE:-}" ]]; then
  for variable in UPDATE_MANAGEMENT_DATABASE PORTAL_DATABASE SOURCE_DATABASE_PREFIX; do
    require "$variable"
  done
  for value in "$UPDATE_MANAGEMENT_DATABASE" "$SOURCE_DATABASE_PREFIX"; do
    if ! identifier "${value%_}"; then
      echo "unsafe database identifier component: $value" >&2
      exit 2
    fi
  done
fi

if ! identifier "$REHEARSAL_DATABASE" || [[ "$REHEARSAL_DATABASE" != *migration_rehearsal* ]]; then
  echo "REHEARSAL_DATABASE must be a safe identifier containing migration_rehearsal" >&2
  exit 2
fi

config_file="$(mktemp)"
cleanup() {
  rm -f -- "$config_file"
}
trap cleanup EXIT
chmod 600 "$config_file"
{
  printf 'host: "%s"\n' "$(yaml_value "$CLICKHOUSE_HOST")"
  printf 'port: %s\n' "$CLICKHOUSE_NATIVE_PORT"
  printf 'user: "%s"\n' "$(yaml_value "$CLICKHOUSE_USER")"
  printf 'password: "%s"\n' "$(yaml_value "$CLICKHOUSE_PASSWORD")"
  if [[ "${CLICKHOUSE_SECURE:-false}" =~ ^(1|true|yes)$ ]]; then
    printf 'secure: true\n'
  fi
} > "$config_file"

ch() {
  clickhouse client --config-file "$config_file" --mutations_sync 2 --query "$1"
}

if [[ -z "${SOURCE_DATABASE:-}" ]]; then
  management_db_escaped="$(sql_literal "$UPDATE_MANAGEMENT_DATABASE")"
  portal_db_escaped="$(sql_literal "$PORTAL_DATABASE")"
  active_color="$(ch "SELECT current_database_in_production FROM \`${management_db_escaped}\`.update_status WHERE portal_database = '${portal_db_escaped}' LIMIT 1 FORMAT TSVRaw")"
  if [[ "$active_color" != blue && "$active_color" != green ]]; then
    echo "unexpected active production color: $active_color" >&2
    exit 1
  fi
  SOURCE_DATABASE="${SOURCE_DATABASE_PREFIX}${active_color}"
fi

if ! identifier "$SOURCE_DATABASE" || [[ "$SOURCE_DATABASE" == "$REHEARSAL_DATABASE" ]]; then
  echo "SOURCE_DATABASE must be a safe identifier distinct from REHEARSAL_DATABASE" >&2
  exit 2
fi

source_exists="$(ch "SELECT count() FROM system.databases WHERE name = '$(sql_literal "$SOURCE_DATABASE")'")"
if [[ "$source_exists" != 1 ]]; then
  echo "source database does not exist: $SOURCE_DATABASE" >&2
  exit 1
fi

rehearsal_exists="$(ch "SELECT count() FROM system.databases WHERE name = '$(sql_literal "$REHEARSAL_DATABASE")'")"
if [[ "$rehearsal_exists" == 1 ]]; then
  rehearsal_tables="$(ch "SELECT count() FROM system.tables WHERE database = '$(sql_literal "$REHEARSAL_DATABASE")'")"
  if [[ "$rehearsal_tables" != 0 ]]; then
    if [[ "${ALLOW_REPLACE_REHEARSAL:-0}" != 1 ]]; then
      echo "rehearsal database is non-empty; set ALLOW_REPLACE_REHEARSAL=1 to replace it" >&2
      exit 1
    fi
    ch "DROP DATABASE \`${REHEARSAL_DATABASE}\` SYNC"
  fi
fi
ch "CREATE DATABASE IF NOT EXISTS \`${REHEARSAL_DATABASE}\`"

unsupported="$(ch "SELECT concat(name, ':', engine) FROM system.tables WHERE database = '$(sql_literal "$SOURCE_DATABASE")' AND (engine IN ('View', 'MaterializedView', 'LiveView', 'Dictionary') OR is_temporary) ORDER BY name FORMAT TSVRaw")"
if [[ -n "$unsupported" ]]; then
  echo "unsupported source table engines for clone rehearsal:" >&2
  printf '%s\n' "$unsupported" >&2
  exit 1
fi

echo "cloning $SOURCE_DATABASE into $REHEARSAL_DATABASE for $CANDIDATE_IMAGE_DIGEST" >&2
while IFS=$'\t' read -r table_name engine; do
  [[ -n "$table_name" ]] || continue
  if ! identifier "$table_name"; then
    echo "unsafe table name returned by ClickHouse: $table_name" >&2
    exit 1
  fi
  if [[ "$table_name" == cbioportal_sequence_state || "$table_name" == *_derived ]]; then
    ch "CREATE TABLE \`${REHEARSAL_DATABASE}\`.\`${table_name}\` AS \`${SOURCE_DATABASE}\`.\`${table_name}\`"
    continue
  fi
  ch "CREATE TABLE \`${REHEARSAL_DATABASE}\`.\`${table_name}\` CLONE AS \`${SOURCE_DATABASE}\`.\`${table_name}\`"
  final_clause=""
  if [[ "$engine" == *ReplacingMergeTree* ]]; then
    final_clause=" FINAL"
  fi
  source_count="$(ch "SELECT count() FROM \`${SOURCE_DATABASE}\`.\`${table_name}\`${final_clause} SETTINGS select_sequential_consistency = 1")"
  rehearsal_count="$(ch "SELECT count() FROM \`${REHEARSAL_DATABASE}\`.\`${table_name}\`${final_clause} SETTINGS select_sequential_consistency = 1")"
  if [[ "$source_count" != "$rehearsal_count" ]]; then
    echo "count mismatch after cloning $table_name: source=$source_count rehearsal=$rehearsal_count" >&2
    exit 1
  fi
done < <(ch "SELECT name, engine FROM system.tables WHERE database = '$(sql_literal "$SOURCE_DATABASE")' ORDER BY name FORMAT TSVRaw")

export CLICKHOUSE_DB="$REHEARSAL_DATABASE"
export CLICKHOUSE_SECURE="${CLICKHOUSE_SECURE:-false}"
echo "running candidate migration against $REHEARSAL_DATABASE" >&2
python3 "$MIGRATOR" --migrate-schema-sql "$MIGRATION_SQL" --populate-derived-tables --force

expected_version="$(sed -n 's/.*<db.version>\([^<]*\)<\/db.version>.*/\1/p' "$ROOT_DIR/pom.xml" | head -1)"
actual_version="$(ch "SELECT db_schema_version FROM \`${REHEARSAL_DATABASE}\`.info LIMIT 1 FORMAT TSVRaw")"
if [[ "$actual_version" != "$expected_version" ]]; then
  echo "schema version mismatch after rehearsal: expected=$expected_version actual=$actual_version" >&2
  exit 1
fi

forbidden_columns="$(ch "SELECT count() FROM system.columns WHERE database = '$(sql_literal "$REHEARSAL_DATABASE")' AND table IN ('wsi_slide', 'wsi_slide_placement') AND name IN ('release_id', 'mrn', 'diagnosis_date', 'procedure_date')")"
if [[ "$forbidden_columns" != 0 ]]; then
  echo "rehearsal WSI schema retains forbidden portal-serving columns" >&2
  exit 1
fi

derived_tables="$(ch "SELECT count() FROM system.tables WHERE database = '$(sql_literal "$REHEARSAL_DATABASE")' AND endsWith(name, '_derived')")"
if [[ "$derived_tables" == 0 ]]; then
  echo "no derived tables found after migration" >&2
  exit 1
fi

printf 'rehearsal succeeded: source=%s target=%s image=%s schema=%s\n' \
  "$SOURCE_DATABASE" "$REHEARSAL_DATABASE" "$CANDIDATE_IMAGE_DIGEST" "$actual_version"
