
import os
import subprocess
import sys


def rebuild_derived_tables(derived_table_sql_filepath=None):
    """Rebuild ClickHouse derived tables after a database-mutating operation.

    Returns True on success, False on failure.
    """
    RED = '\033[91m'
    END = '\033[0m'
    try:
        if not derived_table_sql_filepath:
            portal_home = os.environ.get('PORTAL_HOME', '')
            if not portal_home:
                raise RuntimeError("PORTAL_HOME not set, could not locate derived table script")
            derived_table_sql_filepath = os.path.join(
                portal_home, 'db-scripts', 'clickhouse', 'populate_derived_tables.sql')
            if not os.path.exists(derived_table_sql_filepath):
                raise RuntimeError(f"Could not find derived table script at {derived_table_sql_filepath}")

        required_props = {
            'host': 'CLICKHOUSE_HOST',
            'port': 'CLICKHOUSE_NATIVE_PORT', # the clickhouse-client binary communicates over the native port for maximum efficiency
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
            raise RuntimeError(
                f"ClickHouse properties not set: {', '.join(missing)}")
        
        ch_props['optimize_backoff_secs'] = os.environ.get('CLICKHOUSE_OPTIMIZE_BACKOFF_SECS', '0')

        execute_clickhouse_sql(derived_table_sql_filepath, ch_props)
        return True
    except Exception as e:
        print(RED + f"Derived table construction failed: {e}" + END, file=sys.stderr)
        return False

def execute_clickhouse_sql(sql_filepath, ch_props):
    """Execute a SQL file via the clickhouse client using --multiquery."""
    cmd = [
        'clickhouse', 'client',
        '--host', ch_props['host'],
        '--port', ch_props['port'],
        '--user', ch_props['user'],
        '--password', ch_props['password'],
        '--database', ch_props['database'],
        '--multiquery',
        '--queries-file', sql_filepath,
        '--param_optimize_backoff_secs', ch_props['optimize_backoff_secs'],
        '--echo',
        '--send_logs_level=information'
    ]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True)
    except FileNotFoundError:
        raise RuntimeError(
            "clickhouse client not found. Install it with:\n"
            "  curl https://clickhouse.com/install | sh"
        )
    if result.returncode != 0:
        raise RuntimeError(
            f"clickhouse client failed (exit {result.returncode}):\n{result.stderr}"
        )
