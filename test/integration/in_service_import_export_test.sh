#!/bin/bash

# exit when any of these fails
set -e

echo "Importing of the test study with API validation..."
# --update_generic_assay_entity True: force update of entity properties even when entities already
# exist in the DB (they may exist in the seed without the test-study-specific properties).
metaImport.py -v -u http://cbioportal-container:8080 -o -s /cbioportal/test/test_data/study_es_0_import_export/ \
  --update_generic_assay_entity True

# After importing, rebuild ClickHouse derived tables so the export endpoint can query them.
# This is the required post-import step in the ClickHouse-native import workflow: the importer
# writes study data to base tables, and clickhouse.sql computes the derived tables (genomic
# events, gene panels, clinical timelines, etc.) from those base tables via bulk INSERT...SELECT.
# Until cbioportal-core#<ISSUE> is resolved (auto-trigger from metaImport.py), this must be
# called explicitly after any import against a ClickHouse datasource.
# See: https://github.com/cBioPortal/cbioportal-core/blob/main/scripts/clickhouse_import_support/README.md
#
# NOTE: ClickHouse 24.5's HTTP API (port 8123) does not support the `multiquery` URL parameter
# (it returns UNKNOWN_SETTING, error code 115). The clickhouse-client CLI flag `--multiquery`
# works fine, but clickhouse-client is not installed in the cbioportal container. As a workaround,
# we use Python to split clickhouse.sql on semicolons and POST each statement individually.
# If a future ClickHouse version re-enables the `multiquery` HTTP parameter, this workaround can
# be replaced with a single curl call.
echo "Rebuilding ClickHouse derived tables (required post-import step)..."
CH_PROPS=/cbioportal/application.properties
CH_URL=$(grep '^spring.datasource.url=' "$CH_PROPS" | cut -d= -f2-)
CH_USER=$(grep '^spring.datasource.username=' "$CH_PROPS" | cut -d= -f2-)
CH_PASS=$(grep '^spring.datasource.password=' "$CH_PROPS" | cut -d= -f2-)
CH_HOST=$(echo "$CH_URL" | sed 's|jdbc:ch://||' | cut -d/ -f1)
CH_DB=$(echo "$CH_URL" | sed 's|.*://[^/]*/||' | sed 's/[|].*//')
python3 - "${CH_HOST}" "${CH_USER}" "${CH_PASS}" "${CH_DB}" \
         /cbioportal/src/main/resources/db-scripts/clickhouse/clickhouse.sql << 'PYEOF'
import sys, re, urllib.request, urllib.parse

host, user, password, database, sql_file = sys.argv[1:]
url = f"http://{host}/?user={urllib.parse.quote(user)}&password={urllib.parse.quote(password)}&database={urllib.parse.quote(database)}"

with open(sql_file) as f:
    sql = f.read()

# Strip single-line comments and split on semicolons
sql = re.sub(r'--[^\n]*', '', sql)
statements = [s.strip() for s in sql.split(';') if s.strip()]

for stmt in statements:
    data = stmt.encode()
    req = urllib.request.Request(url, data=data, method='POST')
    try:
        with urllib.request.urlopen(req) as resp:
            pass
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        print(f"ERROR in statement:\n{stmt[:200]}\nClickHouse response: {body}", file=sys.stderr)
        sys.exit(1)

print(f"Successfully executed {len(statements)} statements.")
PYEOF
echo "Derived tables rebuilt."

echo "Exporting of the test study."
curl -s http://cbioportal-container:8080/export/study/study_es_0_import_export.zip > study_es_0_import_export.zip \
&& unzip study_es_0_import_export.zip -d ./output_study_es_0_import_export

echo "Sort content of text files from both folders to make order during comparison unimportant."
./cbioportal/test/integration/copy_and_sort.sh /cbioportal/test/test_data/study_es_0_import_export/ ./input_study_es_0_import_export_sorted/
./cbioportal/test/integration/copy_and_sort.sh ./output_study_es_0_import_export/ ./output_study_es_0_import_export_sorted/

echo "Comparing the original and exported studies."
diff --recursive ./input_study_es_0_import_export_sorted/ ./output_study_es_0_import_export_sorted/

exit 0
