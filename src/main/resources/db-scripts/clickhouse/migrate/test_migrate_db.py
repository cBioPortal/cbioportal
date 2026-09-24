#!/usr/bin/env python3
"""Unit tests for migrate_db.py's Python version handlers.

Run from this directory with:
    python3 -m unittest test_migrate_db -v

FakeClickHouseClient models just enough of ClickHouse (table existence, sorting keys and the
RESOURCE_DATA_ID values of each row) to drive the resumable 3.6.0 state machine without a server.
"""

import os
import re
import unittest
from unittest import mock

import migrate_db
from migrate_db import (
    MigrationSection,
    MigrationStateError,
    RESOURCE_DATA_PREVIOUS_TABLE as PREVIOUS,
    RESOURCE_DATA_STAGING_TABLE as STAGING,
    RESOURCE_DATA_TABLE as RESOURCE_DATA,
    RESOURCE_DATA_TARGET_SORTING_KEY as TARGET_KEY,
)

OLD_KEY = 'CANCER_STUDY_ID, RESOURCE_ID, RESOURCE_DATA_ID'


class Interrupted(Exception):
    pass


class FakeClickHouseClient:
    def __init__(self, tables, interrupt_on=None, lose_row_on_insert=False):
        # tables: {name: {'key': sorting_key, 'rows': [RESOURCE_DATA_ID, ...]}}
        self.tables = {name: {'key': t['key'], 'rows': list(t['rows'])}
                       for name, t in tables.items()}
        self.interrupt_on = interrupt_on
        self.lose_row_on_insert = lose_row_on_insert
        self.statements = []

    def query(self, sql):
        if 'FROM system.tables' in sql:
            names = re.findall(r"'([^']+)'", sql)
            return '\n'.join(f"{name}\t{self.tables[name]['key']}"
                             for name in sorted(names) if name in self.tables)
        match = re.search(r'SELECT count\(\), uniqExact\(RESOURCE_DATA_ID\) FROM (\w+)', sql)
        if match:
            rows = self._table(match.group(1))['rows']
            return f"{len(rows)}\t{len(set(rows))}"
        raise AssertionError(f"Unexpected query: {sql}")

    def execute(self, sql):
        self.statements.append(sql)
        if self.interrupt_on and re.search(self.interrupt_on, sql):
            self.interrupt_on = None
            raise Interrupted(sql)
        match = re.match(r'CREATE TABLE (\w+)', sql)
        if match:
            name = match.group(1)
            if name in self.tables:
                raise RuntimeError(f"Table {name} already exists")
            key = re.search(r'ORDER BY \(([^)]*)\)', sql).group(1)
            self.tables[name] = {'key': key, 'rows': []}
            return
        match = re.match(r'INSERT INTO (\w+) .* FROM (\w+);', sql)
        if match:
            rows = list(self._table(match.group(2))['rows'])
            if self.lose_row_on_insert:
                rows = rows[1:]
            self._table(match.group(1))['rows'].extend(rows)
            return
        match = re.match(r'RENAME TABLE (.*);', sql)
        if match:
            for pair in match.group(1).split(','):
                source, target = pair.split(' TO ')
                source, target = source.strip(), target.strip()
                if target in self.tables:
                    raise RuntimeError(f"Table {target} already exists")
                self.tables[target] = self._table(source)
                del self.tables[source]
            return
        match = re.match(r'DROP TABLE IF EXISTS (\w+) SYNC;', sql)
        if match:
            self.tables.pop(match.group(1), None)
            return
        raise AssertionError(f"Unexpected statement: {sql}")

    def _table(self, name):
        if name not in self.tables:
            raise RuntimeError(f"Table {name} does not exist")
        return self.tables[name]


ROWS = [101, 102, 103, 104]


def run_handler(client):
    with mock.patch('builtins.print'):
        migrate_db.migrate_resource_data_patient_order(client)


class ResourceDataPatientOrderTest(unittest.TestCase):

    def assertMigrated(self, client, rows=ROWS):
        self.assertEqual({RESOURCE_DATA}, set(client.tables))
        self.assertEqual(TARGET_KEY, client.tables[RESOURCE_DATA]['key'])
        self.assertEqual(sorted(rows), sorted(client.tables[RESOURCE_DATA]['rows']))

    def test_reads_tables_and_sorting_keys(self):
        client = FakeClickHouseClient({
            RESOURCE_DATA: {'key': OLD_KEY, 'rows': ROWS},
            STAGING: {'key': TARGET_KEY, 'rows': []},
        })
        self.assertEqual({RESOURCE_DATA: OLD_KEY, STAGING: TARGET_KEY},
                         migrate_db.get_resource_data_tables(client))

    def test_skips_when_target_key_present_without_leftovers(self):
        client = FakeClickHouseClient({RESOURCE_DATA: {'key': TARGET_KEY, 'rows': ROWS}})
        run_handler(client)
        self.assertEqual([], client.statements)
        self.assertMigrated(client)

    def test_rebuilds_old_key_layout(self):
        client = FakeClickHouseClient({RESOURCE_DATA: {'key': OLD_KEY, 'rows': ROWS}})
        run_handler(client)
        self.assertMigrated(client)
        kinds = [statement.split()[0] for statement in client.statements]
        self.assertEqual(['CREATE', 'INSERT', 'RENAME', 'DROP'], kinds)
        self.assertIn(f'DROP TABLE IF EXISTS {PREVIOUS} SYNC;', client.statements[-1])

    def test_rebuilds_empty_table(self):
        client = FakeClickHouseClient({RESOURCE_DATA: {'key': OLD_KEY, 'rows': []}})
        run_handler(client)
        self.assertMigrated(client, rows=[])

    def test_drops_partial_staging_and_rebuilds(self):
        client = FakeClickHouseClient({
            RESOURCE_DATA: {'key': OLD_KEY, 'rows': ROWS},
            STAGING: {'key': TARGET_KEY, 'rows': ROWS[:2]},
        })
        run_handler(client)
        self.assertMigrated(client)
        self.assertEqual(f'DROP TABLE IF EXISTS {STAGING} SYNC;', client.statements[0])

    def test_drops_stale_staging_when_target_key_already_present(self):
        client = FakeClickHouseClient({
            RESOURCE_DATA: {'key': TARGET_KEY, 'rows': ROWS},
            STAGING: {'key': TARGET_KEY, 'rows': ROWS},
        })
        run_handler(client)
        self.assertMigrated(client)
        self.assertEqual([f'DROP TABLE IF EXISTS {STAGING} SYNC;'], client.statements)

    def test_finishes_interrupted_swap_when_counts_match(self):
        client = FakeClickHouseClient({
            RESOURCE_DATA: {'key': TARGET_KEY, 'rows': ROWS},
            PREVIOUS: {'key': OLD_KEY, 'rows': ROWS},
        })
        run_handler(client)
        self.assertMigrated(client)
        self.assertEqual([f'DROP TABLE IF EXISTS {PREVIOUS} SYNC;'], client.statements)

    def test_refuses_to_drop_previous_when_counts_differ(self):
        client = FakeClickHouseClient({
            RESOURCE_DATA: {'key': TARGET_KEY, 'rows': ROWS + [105]},
            PREVIOUS: {'key': OLD_KEY, 'rows': ROWS},
        })
        with self.assertRaises(MigrationStateError):
            run_handler(client)
        self.assertEqual({RESOURCE_DATA, PREVIOUS}, set(client.tables))
        self.assertEqual([], client.statements)

    def test_refuses_previous_next_to_old_key_resource_data(self):
        client = FakeClickHouseClient({
            RESOURCE_DATA: {'key': OLD_KEY, 'rows': ROWS},
            PREVIOUS: {'key': OLD_KEY, 'rows': ROWS},
        })
        with self.assertRaises(MigrationStateError):
            run_handler(client)
        self.assertEqual([], client.statements)

    def test_restores_previous_and_rebuilds_when_resource_data_missing(self):
        client = FakeClickHouseClient({PREVIOUS: {'key': OLD_KEY, 'rows': ROWS}})
        run_handler(client)
        self.assertMigrated(client)
        self.assertEqual(f'RENAME TABLE {PREVIOUS} TO {RESOURCE_DATA};', client.statements[0])

    def test_drops_staging_restores_previous_after_partial_rename(self):
        client = FakeClickHouseClient({
            PREVIOUS: {'key': OLD_KEY, 'rows': ROWS},
            STAGING: {'key': TARGET_KEY, 'rows': ROWS},
        })
        run_handler(client)
        self.assertMigrated(client)
        self.assertEqual(f'DROP TABLE IF EXISTS {STAGING} SYNC;', client.statements[0])
        self.assertEqual(f'RENAME TABLE {PREVIOUS} TO {RESOURCE_DATA};', client.statements[1])

    def test_refuses_when_all_three_tables_exist(self):
        client = FakeClickHouseClient({
            RESOURCE_DATA: {'key': TARGET_KEY, 'rows': ROWS},
            STAGING: {'key': TARGET_KEY, 'rows': ROWS},
            PREVIOUS: {'key': OLD_KEY, 'rows': ROWS},
        })
        with self.assertRaises(MigrationStateError):
            run_handler(client)
        self.assertEqual([], client.statements)
        self.assertEqual(3, len(client.tables))

    def test_refuses_when_only_staging_exists(self):
        client = FakeClickHouseClient({STAGING: {'key': TARGET_KEY, 'rows': ROWS}})
        with self.assertRaisesRegex(MigrationStateError, 'may be incomplete'):
            run_handler(client)
        self.assertEqual([], client.statements)

    def test_refuses_when_resource_data_missing(self):
        client = FakeClickHouseClient({})
        with self.assertRaises(MigrationStateError):
            run_handler(client)

    def test_copy_mismatch_stops_before_swap(self):
        client = FakeClickHouseClient({RESOURCE_DATA: {'key': OLD_KEY, 'rows': ROWS}},
                                      lose_row_on_insert=True)
        with self.assertRaises(MigrationStateError):
            run_handler(client)
        self.assertFalse(any(s.startswith('RENAME') for s in client.statements))
        self.assertEqual(OLD_KEY, client.tables[RESOURCE_DATA]['key'])
        self.assertEqual(ROWS, client.tables[RESOURCE_DATA]['rows'])

    def test_resumes_after_interruption_at_each_step(self):
        for step in ('^INSERT', '^RENAME', f'^DROP TABLE IF EXISTS {PREVIOUS}'):
            with self.subTest(interrupted_at=step):
                client = FakeClickHouseClient({RESOURCE_DATA: {'key': OLD_KEY, 'rows': ROWS}},
                                              interrupt_on=step)
                with self.assertRaises(Interrupted):
                    run_handler(client)
                run_handler(client)
                self.assertMigrated(client)


class ApplySectionTest(unittest.TestCase):

    def setUp(self):
        patches = [
            mock.patch.object(migrate_db, 'run_multiquery'),
            mock.patch.object(migrate_db, 'wait_for_mutations'),
            mock.patch('builtins.print'),
        ]
        self.run_multiquery = patches[0].start()
        for patch in patches[1:]:
            patch.start()
        for patch in patches:
            self.addCleanup(patch.stop)

    def test_records_version_after_handler_succeeds(self):
        client = FakeClickHouseClient({RESOURCE_DATA: {'key': OLD_KEY, 'rows': ROWS}})
        migrate_db.apply_section({}, MigrationSection('3.6.0', 'reorder', '-- marker'), client)
        self.assertEqual(TARGET_KEY, client.tables[RESOURCE_DATA]['key'])
        self.run_multiquery.assert_called_once()
        self.assertIn("db_schema_version = '3.6.0'", self.run_multiquery.call_args[0][1])

    def test_does_not_record_version_when_handler_fails(self):
        client = FakeClickHouseClient({})
        with self.assertRaises(MigrationStateError):
            migrate_db.apply_section({}, MigrationSection('3.6.0', 'reorder', ''), client)
        self.run_multiquery.assert_not_called()

    def test_rejects_sql_in_handler_section(self):
        client = FakeClickHouseClient({RESOURCE_DATA: {'key': OLD_KEY, 'rows': ROWS}})
        section = MigrationSection('3.6.0', 'reorder', '-- marker\nDROP TABLE resource_data;')
        with self.assertRaises(RuntimeError):
            migrate_db.apply_section({}, section, client)
        self.assertEqual([], client.statements)
        self.run_multiquery.assert_not_called()


class MigrateSchemaSqlTest(unittest.TestCase):

    def test_handler_sections_are_comment_only_markers(self):
        sections = migrate_db.parse_migrate_schema_sql(migrate_db.DEFAULT_MIGRATE_SCHEMA_SQL)
        migrate_db.validate_version_handlers(sections)
        by_version = {section.version: section for section in sections}
        for version in migrate_db.VERSION_HANDLERS:
            self.assertEqual('', migrate_db.strip_sql_comments(by_version[version].sql))

    def test_rejects_handler_without_section(self):
        with self.assertRaises(RuntimeError):
            migrate_db.validate_version_handlers([MigrationSection('3.5.0', '', 'SELECT 1')])


if __name__ == '__main__':
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    unittest.main()
