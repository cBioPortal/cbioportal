#!/usr/bin/env python3
"""ClickHouse-native schema migration runner for cBioPortal.

Parses migrate_schema.sql (in this same directory, unless overridden) into version-tagged
sections and applies any section newer than the target database's current db_schema_version,
strictly in ascending order. See the header of migrate_schema.sql for the section format.

With --populate-derived-tables, also repopulates derived tables (populate_derived_tables.sql)
after a run that actually applied one or more migration sections. Off by default, since some
deployments may run derived-table population as a separate manual step.

ClickHouse connection is configured via environment variables, matching the convention used by
cbioportal-core's rebuild_derived_tables.py:
    CLICKHOUSE_HOST, CLICKHOUSE_NATIVE_PORT, CLICKHOUSE_USER, CLICKHOUSE_PASSWORD, CLICKHOUSE_DB

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
    except Exception:
        os.remove(path)
        raise
    os.chmod(path, 0o600)
    return path


def _base_cmd(ch_props):
    return ['clickhouse', 'client', '--config-file', ch_props['config_path']]


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

    ALTER TABLE ... UPDATE/DELETE are async mutations in ClickHouse — a section isn't
    actually done just because the client returned.
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


def apply_section(ch_props, section):
    print(f"Applying db_schema_version {section.version}: {section.description}")
    if section.sql:
        run_multiquery(ch_props, section.sql)
    run_multiquery(ch_props, f"ALTER TABLE info UPDATE db_schema_version = '{section.version}' WHERE 1;")
    wait_for_mutations(ch_props)
    print(GREEN + f"Applied db_schema_version {section.version}" + END)


def populate_derived_tables(ch_props, populate_derived_tables_sql_filepath=None):
    """Repopulate derived tables by running populate_derived_tables.sql. Safe to call any time —
    it clears and rebuilds derived table data from scratch, and doesn't touch table structure."""
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
                    populate_derived_tables_sql_filepath=None):
    """Apply any pending migrations (and optionally repopulate derived tables if anything was
    applied). Returns True on success, False on failure."""
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
        pending = [s for s in sections if s.version_tuple() > current_tuple]

        if not pending:
            print(f"Database is already at db_schema_version {current_version}. Nothing to do.")
        else:
            print(f"Current db_schema_version: {current_version}. "
                  f"{len(pending)} migration(s) to apply.")
            for section in pending:
                apply_section(ch_props, section)

        if populate_derived_tables_flag and pending:
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
    args = parser.parse_args()

    success = run_migrations(
        args.migrate_schema_sql,
        populate_derived_tables_flag=args.populate_derived_tables,
        populate_derived_tables_sql_filepath=args.populate_derived_tables_sql)
    sys.exit(0 if success else 1)


if __name__ == '__main__':
    main()
