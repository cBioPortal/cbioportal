#!/usr/bin/env python3
"""ClickHouse-native schema migration runner for cBioPortal.

Parses migrate_schema.sql (in this same directory, unless overridden) into version-tagged
sections and applies any section newer than the target database's current db_schema_version,
strictly in ascending order. See the header of migrate_schema.sql for the section format.

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


def apply_section(ch_props, section):
    print(f"Applying db_schema_version {section.version}: {section.description}")
    if section.sql:
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
