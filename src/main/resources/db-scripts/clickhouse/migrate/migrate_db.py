#!/usr/bin/env python3
"""ClickHouse-native schema migration runner for cBioPortal.

Parses migrate_schema.sql (in this same directory, unless overridden) into version-tagged
sections and applies any section newer than the target database's current db_schema_version,
strictly in ascending order. See the header of migrate_schema.sql for the section format.
Versions listed in VERSION_HANDLERS are applied by a Python handler instead of their (comment-only)
SQL section, for steps that must inspect table state to be safely resumable.

With --populate-derived-tables, also repopulates derived tables (populate_derived_tables.sql)
after a run that actually applied one or more migration sections. Off by default, since some
deployments may run derived-table population as a separate manual step. Add --force to repopulate
derived tables even when there were no pending migrations (e.g. to rebuild them on demand).

ClickHouse connection is configured via environment variables, matching the convention used by
cbioportal-core's rebuild_derived_tables.py:
    CLICKHOUSE_HOST, CLICKHOUSE_NATIVE_PORT, CLICKHOUSE_USER, CLICKHOUSE_PASSWORD, CLICKHOUSE_DB
Optionally, set CLICKHOUSE_SECURE=true to connect over TLS (required for ClickHouse Cloud, whose
native port — typically 9440 — is TLS-only). Unset/false for a plain local/self-hosted ClickHouse
on its default native port (9000).

Credentials are written to a temporary clickhouse-client config file (mode 0600) rather than
passed as command-line arguments, since subprocess argv is visible to other users on the host via
`ps aux` / `/proc/<pid>/cmdline`.
"""

import argparse
import os
import re
import subprocess
import sys
import tempfile
import time

RED = '\033[91m'
GREEN = '\033[92m'
END = '\033[0m'

SECTION_HEADER_RE = re.compile(r'^##\s*db_schema_version:\s*(\S+)\s*$')
DESCRIPTION_RE = re.compile(r'^##\s*description:\s*(.*)$')
DIRECTIVE_LINE_RE = re.compile(r'^##')

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DEFAULT_MIGRATE_SCHEMA_SQL = os.path.join(SCRIPT_DIR, 'migrate_schema.sql')
DEFAULT_POPULATE_DERIVED_TABLES_SQL = os.path.normpath(
    os.path.join(SCRIPT_DIR, os.pardir, 'populate_derived_tables.sql'))


class MigrationSection:
    def __init__(self, version, description, sql):
        self.version = version
        self.description = description
        self.sql = sql

    def version_tuple(self):
        return version_tuple(self.version)


def version_tuple(version):
    return tuple(int(part) for part in version.split('.'))


def parse_migrate_schema_sql(filepath):
    with open(filepath) as f:
        lines = f.readlines()

    sections = []
    current = None
    sql_lines = []

    def flush():
        if current is not None:
            sections.append(MigrationSection(
                current['version'], current['description'], ''.join(sql_lines).strip()))

    for line in lines:
        header_match = SECTION_HEADER_RE.match(line)
        if header_match:
            flush()
            current = {'version': header_match.group(1), 'description': ''}
            sql_lines = []
            continue
        if current is None:
            continue  # ignore file header / comments before the first section
        desc_match = DESCRIPTION_RE.match(line)
        if desc_match:
            current['description'] = desc_match.group(1).strip()
            continue
        if DIRECTIVE_LINE_RE.match(line):
            # A `##`-prefixed line inside a section that isn't a recognized directive is almost
            # certainly an authoring mistake (e.g. a multi-line description) — fail loudly instead
            # of silently folding it into the section's SQL, where it would be swallowed as a
            # harmless-looking comment sent to the server without anyone noticing the intended
            # content never made it into the description.
            raise RuntimeError(
                f"Unrecognized '##' directive line in section {current['version']}: {line!r}. "
                f"Descriptions must be a single line — put everything on the '## description:' "
                f"line itself.")
        sql_lines.append(line)
    flush()

    sections.sort(key=lambda s: s.version_tuple())
    return sections


def get_clickhouse_props():
    required_props = {
        'host': 'CLICKHOUSE_HOST',
        'port': 'CLICKHOUSE_NATIVE_PORT',
        'user': 'CLICKHOUSE_USER',
        'password': 'CLICKHOUSE_PASSWORD',
        'database': 'CLICKHOUSE_DB',
    }
    missing = []
    ch_props = {}
    for key, env_var in required_props.items():
        value = os.environ.get(env_var)
        if not value:
            missing.append(env_var)
        ch_props[key] = value
    if missing:
        raise RuntimeError(f"ClickHouse properties not set: {', '.join(missing)}")
    # Optional: required for ClickHouse Cloud, whose native port (typically 9440) is TLS-only.
    # Unset/false for a plain local/self-hosted ClickHouse on its default native port (9000).
    ch_props['secure'] = os.environ.get('CLICKHOUSE_SECURE', '').lower() in ('1', 'true', 'yes')
    return ch_props


def _yaml_double_quoted(value):
    """Escape a string for use inside a YAML double-quoted scalar."""
    return value.replace('\\', '\\\\').replace('"', '\\"')


def write_client_config(ch_props):
    """Write ClickHouse connection settings (including the password) to a temp YAML config file
    with 0600 permissions, for use with `clickhouse client --config-file`. Avoids passing the
    password as a subprocess argv, which would otherwise be visible to any other user on the
    host via `ps aux` / `/proc/<pid>/cmdline`. Caller is responsible for deleting the file."""
    fd, path = tempfile.mkstemp(prefix='ch_migrate_client_', suffix='.yaml')
    try:
        with os.fdopen(fd, 'w') as f:
            f.write(f"user: \"{_yaml_double_quoted(ch_props['user'])}\"\n")
            f.write(f"password: \"{_yaml_double_quoted(ch_props['password'])}\"\n")
            f.write(f"host: \"{_yaml_double_quoted(ch_props['host'])}\"\n")
            f.write(f"port: {ch_props['port']}\n")
            f.write(f"database: \"{_yaml_double_quoted(ch_props['database'])}\"\n")
            if ch_props['secure']:
                f.write("secure: true\n")
    except Exception:
        os.remove(path)
        raise
    os.chmod(path, 0o600)
    return path


def _base_cmd(ch_props):
    # mutations_sync=2 forces ALTER TABLE ... UPDATE/DELETE (mutations) to block until fully
    # applied (on all replicas, in a replicated setup) before the statement returns, instead of
    # ClickHouse's default fire-and-forget behavior. This closes a race within a single migration
    # section that has multiple statements: without it, a later statement in the same section
    # could run against data an earlier UPDATE/DELETE in that section hasn't actually applied yet.
    return ['clickhouse', 'client', '--config-file', ch_props['config_path'],
            '--mutations_sync', '2']


def _run_client(ch_props, extra_args, input_text=None):
    cmd = _base_cmd(ch_props) + extra_args
    try:
        result = subprocess.run(cmd, input=input_text, capture_output=True, text=True)
    except FileNotFoundError:
        raise RuntimeError(
            "clickhouse client not found. Install it with:\n"
            "  curl https://clickhouse.com/install | sh"
        )
    if result.returncode != 0:
        raise RuntimeError(f"clickhouse client failed (exit {result.returncode}):\n{result.stderr}")
    return result.stdout


def run_query(ch_props, query):
    """Run a single query via the clickhouse client and return trimmed stdout."""
    return _run_client(ch_props, ['--query', query]).strip()


def run_multiquery(ch_props, sql):
    """Run a block of SQL statements (piped via stdin) through the clickhouse client."""
    return _run_client(ch_props, ['--multiquery'], input_text=sql)


def get_current_db_schema_version(ch_props):
    version = run_query(ch_props, "SELECT db_schema_version FROM info LIMIT 1")
    if not version:
        raise RuntimeError(
            "Could not read db_schema_version from info table. Is the database initialized?")
    return version


def wait_for_mutations(ch_props, timeout_secs=300, poll_interval_secs=2):
    """Block until all ClickHouse mutations in this database have completed.

    ALTER TABLE ... UPDATE/DELETE are async mutations in ClickHouse by default. _base_cmd()
    already sets mutations_sync=2, which forces our own UPDATE/DELETE statements to block until
    applied before returning — so this is now a secondary safety net (e.g. against a mutation
    left pending by something outside this run) rather than the primary synchronization
    mechanism.
    """
    deadline = time.time() + timeout_secs
    while time.time() < deadline:
        pending = run_query(
            ch_props,
            "SELECT count() FROM system.mutations "
            f"WHERE database = '{ch_props['database']}' AND is_done = 0",
        )
        if pending == '0':
            return
        time.sleep(poll_interval_secs)
    raise TimeoutError(f"Mutations did not complete within {timeout_secs}s")


class ClickHouseClient:
    """Minimal query/execute interface used by the Python version handlers, so their state
    machines can be unit-tested against a fake client (see test_migrate_db.py)."""

    def __init__(self, ch_props):
        self.ch_props = ch_props

    def query(self, sql):
        return run_query(self.ch_props, sql)

    def execute(self, sql):
        run_multiquery(self.ch_props, sql)


class MigrationStateError(RuntimeError):
    """Raised when a version handler finds table state it cannot safely resolve on its own."""


# --- 3.6.0: rebuild resource_data with the patient-inclusive sort key -----------------------

RESOURCE_DATA_TABLE = 'resource_data'
RESOURCE_DATA_STAGING_TABLE = 'resource_data_patient_order'
RESOURCE_DATA_PREVIOUS_TABLE = 'resource_data_previous_order'
RESOURCE_DATA_TARGET_SORTING_KEY = 'CANCER_STUDY_ID, RESOURCE_ID, PATIENT_ID, RESOURCE_DATA_ID'
RESOURCE_DATA_COLUMNS = (
    'RESOURCE_DATA_ID', 'RESOURCE_ID', 'CANCER_STUDY_ID', 'ENTITY_TYPE', 'PATIENT_ID',
    'SAMPLE_ID', 'URL', 'DISPLAY_NAME', 'TYPE', 'METADATA',
)
RESOURCE_DATA_STAGING_DDL = f"""CREATE TABLE {RESOURCE_DATA_STAGING_TABLE}
(
    `RESOURCE_DATA_ID` Int64,
    `RESOURCE_ID`      String,
    `CANCER_STUDY_ID`  Int32,
    `ENTITY_TYPE`      String,
    `PATIENT_ID`       Nullable(String),
    `SAMPLE_ID`        Nullable(String),
    `URL`              String,
    `DISPLAY_NAME`     Nullable(String),
    `TYPE`             Nullable(String),
    `METADATA`         Nullable(String)
) ENGINE = MergeTree ORDER BY ({RESOURCE_DATA_TARGET_SORTING_KEY})
SETTINGS allow_nullable_key = 1"""


def get_resource_data_tables(client):
    """Return {table_name: sorting_key} for the resource_data tables present in the current
    database (resource_data, the staging table and the previous-order backup)."""
    names = ', '.join(f"'{name}'" for name in (
        RESOURCE_DATA_TABLE, RESOURCE_DATA_STAGING_TABLE, RESOURCE_DATA_PREVIOUS_TABLE))
    output = client.query(
        "SELECT name, sorting_key FROM system.tables "
        f"WHERE database = currentDatabase() AND name IN ({names}) "
        "ORDER BY name FORMAT TabSeparated")
    tables = {}
    for line in output.splitlines():
        if not line.strip():
            continue
        name, _, sorting_key = line.partition('\t')
        tables[name] = sorting_key.strip()
    return tables


def get_resource_data_counts(client, table):
    """Return (row count, distinct RESOURCE_DATA_ID count) for a resource_data-shaped table."""
    output = client.query(
        f"SELECT count(), uniqExact(RESOURCE_DATA_ID) FROM {table} FORMAT TabSeparated")
    parts = output.split()
    if len(parts) != 2:
        raise MigrationStateError(f"Unexpected count output for {table}: {output!r}")
    return int(parts[0]), int(parts[1])


def verify_resource_data_copy(client, source, target):
    source_counts = get_resource_data_counts(client, source)
    target_counts = get_resource_data_counts(client, target)
    if source_counts != target_counts:
        raise MigrationStateError(
            f"Row counts differ between {source} (rows, distinct RESOURCE_DATA_ID = "
            f"{source_counts}) and {target} ({target_counts}). Were imports paused? "
            f"Leaving both tables in place for inspection.")
    return target_counts


def verify_resource_data_sorting_key(client, table):
    sorting_key = get_resource_data_tables(client).get(table)
    if sorting_key != RESOURCE_DATA_TARGET_SORTING_KEY:
        raise MigrationStateError(
            f"{table} has sorting key {sorting_key!r}, expected "
            f"{RESOURCE_DATA_TARGET_SORTING_KEY!r}.")


def drop_table(client, table):
    client.execute(f"DROP TABLE IF EXISTS {table} SYNC;")


def rebuild_resource_data(client):
    """Copy resource_data into a staging table with the target key, verify the copy, swap the
    tables and verify again before dropping the previous-order table."""
    columns = ', '.join(RESOURCE_DATA_COLUMNS)
    print(f"Creating {RESOURCE_DATA_STAGING_TABLE} with ORDER BY "
          f"({RESOURCE_DATA_TARGET_SORTING_KEY})")
    client.execute(RESOURCE_DATA_STAGING_DDL + ';')
    print(f"Copying {RESOURCE_DATA_TABLE} into {RESOURCE_DATA_STAGING_TABLE}")
    client.execute(
        f"INSERT INTO {RESOURCE_DATA_STAGING_TABLE} ({columns}) "
        f"SELECT {columns} FROM {RESOURCE_DATA_TABLE};")
    verify_resource_data_sorting_key(client, RESOURCE_DATA_STAGING_TABLE)
    counts = verify_resource_data_copy(client, RESOURCE_DATA_TABLE, RESOURCE_DATA_STAGING_TABLE)
    print(f"Copy verified: {counts[0]} rows, {counts[1]} distinct RESOURCE_DATA_ID values")
    client.execute(
        f"RENAME TABLE {RESOURCE_DATA_TABLE} TO {RESOURCE_DATA_PREVIOUS_TABLE}, "
        f"{RESOURCE_DATA_STAGING_TABLE} TO {RESOURCE_DATA_TABLE};")
    finish_resource_data_swap(client)


def finish_resource_data_swap(client):
    """resource_data is the rebuilt table and resource_data_previous_order holds the original
    rows: verify the rebuilt table and drop the previous one."""
    verify_resource_data_sorting_key(client, RESOURCE_DATA_TABLE)
    verify_resource_data_copy(client, RESOURCE_DATA_PREVIOUS_TABLE, RESOURCE_DATA_TABLE)
    print(f"Swap verified; dropping {RESOURCE_DATA_PREVIOUS_TABLE}")
    drop_table(client, RESOURCE_DATA_PREVIOUS_TABLE)


def migrate_resource_data_patient_order(client):
    """3.6.0 handler. Resumable: inspects which of resource_data, the staging table and the
    previous-order backup exist (and resource_data's sorting key) and continues from there.

    State (R = resource_data, S = staging, P = previous-order backup):
      R with target key, no S/P   -> nothing to do
      R, S, no P                  -> copy was interrupted: drop S, then continue from R
      R with old key, no S/P      -> rebuild
      R and P, no S               -> interrupted after the swap: verify counts, drop P
      P, no R                     -> interrupted mid-swap: drop S, rename P back, rebuild
      anything else               -> refuse; needs manual inspection
    """
    print(RED + "3.6.0 rebuilds resource_data. Resource imports and study deletions against "
          "this database must be paused until the migration finishes." + END)
    tables = get_resource_data_tables(client)
    has_r = RESOURCE_DATA_TABLE in tables
    has_s = RESOURCE_DATA_STAGING_TABLE in tables
    has_p = RESOURCE_DATA_PREVIOUS_TABLE in tables

    if has_p and has_r and has_s:
        raise MigrationStateError(
            f"{RESOURCE_DATA_TABLE}, {RESOURCE_DATA_STAGING_TABLE} and "
            f"{RESOURCE_DATA_PREVIOUS_TABLE} all exist; refusing to guess which holds the "
            f"authoritative rows. Inspect them and remove the stale tables manually.")
    if has_p and has_r:
        print(f"Found {RESOURCE_DATA_PREVIOUS_TABLE} next to {RESOURCE_DATA_TABLE}: resuming "
              f"after an interrupted swap")
        finish_resource_data_swap(client)
        return
    if has_p:
        if has_s:
            print(f"Dropping partial {RESOURCE_DATA_STAGING_TABLE} left by an interrupted swap")
            drop_table(client, RESOURCE_DATA_STAGING_TABLE)
        print(f"Restoring {RESOURCE_DATA_PREVIOUS_TABLE} to {RESOURCE_DATA_TABLE}")
        client.execute(
            f"RENAME TABLE {RESOURCE_DATA_PREVIOUS_TABLE} TO {RESOURCE_DATA_TABLE};")
        rebuild_resource_data(client)
        return
    if not has_r:
        detail = (f" Only {RESOURCE_DATA_STAGING_TABLE} exists and may be incomplete."
                  if has_s else "")
        raise MigrationStateError(
            f"{RESOURCE_DATA_TABLE} does not exist; cannot apply 3.6.0.{detail}")
    if has_s:
        print(f"Dropping partial {RESOURCE_DATA_STAGING_TABLE} left by an interrupted copy")
        drop_table(client, RESOURCE_DATA_STAGING_TABLE)
    if tables[RESOURCE_DATA_TABLE] == RESOURCE_DATA_TARGET_SORTING_KEY:
        print(f"{RESOURCE_DATA_TABLE} already has ORDER BY "
              f"({RESOURCE_DATA_TARGET_SORTING_KEY}); no rebuild needed")
        return
    rebuild_resource_data(client)


# Versions whose migration is implemented in Python instead of SQL. Their migrate_schema.sql
# section must contain only comments: it stays in the file so the version is still part of the
# ordered history, and the handler runs in its place.
VERSION_HANDLERS = {
    '3.6.0': migrate_resource_data_patient_order,
}


def strip_sql_comments(sql):
    """Return sql with blank lines and full-line `--` comments removed."""
    return '\n'.join(line for line in sql.splitlines()
                     if line.strip() and not line.strip().startswith('--'))


def validate_version_handlers(sections):
    """Every Python handler must have a (comment-only) marker section in migrate_schema.sql, so
    it runs in version order."""
    section_versions = {section.version for section in sections}
    missing = sorted(set(VERSION_HANDLERS) - section_versions, key=version_tuple)
    if missing:
        raise RuntimeError(
            f"migrate_db.py has handlers for versions with no migrate_schema.sql section: "
            f"{', '.join(missing)}")


def apply_section(ch_props, section, client=None):
    print(f"Applying db_schema_version {section.version}: {section.description}")
    handler = VERSION_HANDLERS.get(section.version)
    if handler is not None:
        if strip_sql_comments(section.sql):
            raise RuntimeError(
                f"Section {section.version} is implemented in migrate_db.py and must contain "
                f"only comments in migrate_schema.sql")
        handler(client or ClickHouseClient(ch_props))
    elif section.sql:
        run_multiquery(ch_props, section.sql)
    run_multiquery(ch_props, f"ALTER TABLE info UPDATE db_schema_version = '{section.version}' WHERE 1;")
    wait_for_mutations(ch_props)
    print(GREEN + f"Applied db_schema_version {section.version}" + END)


def populate_derived_tables(ch_props, populate_derived_tables_sql_filepath=None):
    """Repopulate derived tables by running populate_derived_tables.sql. Clears and rebuilds
    derived table data from scratch (doesn't touch table structure), so it's safe from a database
    consistency standpoint to call any time — but only call this while no backend web service is
    connected to the database in production, since queries against the empty/partially-rebuilt
    derived tables mid-run will error."""
    filepath = populate_derived_tables_sql_filepath or DEFAULT_POPULATE_DERIVED_TABLES_SQL
    if not os.path.exists(filepath):
        raise RuntimeError(f"Could not find populate_derived_tables.sql at {filepath}")

    print("Populating derived tables...")
    optimize_backoff_secs = os.environ.get('CLICKHOUSE_OPTIMIZE_BACKOFF_SECS', '0')
    _run_client(ch_props, [
        '--multiquery',
        '--queries-file', filepath,
        '--param_optimize_backoff_secs', optimize_backoff_secs,
    ])
    print(GREEN + "Derived tables populated" + END)


def run_migrations(migrate_schema_sql_filepath=None, populate_derived_tables_flag=False,
                    populate_derived_tables_sql_filepath=None, force=False):
    """Apply any pending migrations (and optionally repopulate derived tables if anything was
    applied, or unconditionally if force=True). Returns True on success, False on failure."""
    config_path = None
    try:
        filepath = migrate_schema_sql_filepath or DEFAULT_MIGRATE_SCHEMA_SQL
        if not os.path.exists(filepath):
            raise RuntimeError(f"Could not find migrate_schema.sql at {filepath}")

        ch_props = get_clickhouse_props()
        config_path = write_client_config(ch_props)
        ch_props['config_path'] = config_path

        current_version = get_current_db_schema_version(ch_props)
        current_tuple = version_tuple(current_version)

        sections = parse_migrate_schema_sql(filepath)
        validate_version_handlers(sections)
        pending = [s for s in sections if s.version_tuple() > current_tuple]

        if not pending:
            print(f"Database is already at db_schema_version {current_version}. Nothing to do.")
        else:
            print(f"Current db_schema_version: {current_version}. "
                  f"{len(pending)} migration(s) to apply.")
            for section in pending:
                apply_section(ch_props, section)

        if populate_derived_tables_flag:
            if pending:
                populate_derived_tables(ch_props, populate_derived_tables_sql_filepath)
            elif force:
                print("No pending migrations, but --force was passed — repopulating derived "
                      "tables anyway.")
                populate_derived_tables(ch_props, populate_derived_tables_sql_filepath)

        return True
    except Exception as e:
        print(RED + f"Migration failed: {e}" + END, file=sys.stderr)
        return False
    finally:
        if config_path and os.path.exists(config_path):
            os.remove(config_path)


def main():
    parser = argparse.ArgumentParser(description="Apply pending ClickHouse schema migrations.")
    parser.add_argument(
        '--migrate-schema-sql',
        default=None,
        help="Path to migrate_schema.sql (defaults to the file next to this script)")
    parser.add_argument(
        '--populate-derived-tables',
        action='store_true',
        help="After applying migrations, also repopulate derived tables if this run actually "
             "applied one or more migration sections. Off by default: deployments that run "
             "derived-table population as a separate manual step should leave this unset.")
    parser.add_argument(
        '--populate-derived-tables-sql',
        default=None,
        help="Path to populate_derived_tables.sql (defaults to the file next to this script's "
             "parent directory); only used with --populate-derived-tables")
    parser.add_argument(
        '--force',
        action='store_true',
        help="With --populate-derived-tables, repopulate derived tables even if there were no "
             "pending migrations to apply. Useful for rebuilding derived tables on demand "
             "without a real schema change to trigger it. No effect without "
             "--populate-derived-tables, and no effect on which migrate_schema.sql sections get "
             "applied — migrations are always applied strictly based on db_schema_version.")
    args = parser.parse_args()

    success = run_migrations(
        args.migrate_schema_sql,
        populate_derived_tables_flag=args.populate_derived_tables,
        populate_derived_tables_sql_filepath=args.populate_derived_tables_sql,
        force=args.force)
    sys.exit(0 if success else 1)


if __name__ == '__main__':
    main()
