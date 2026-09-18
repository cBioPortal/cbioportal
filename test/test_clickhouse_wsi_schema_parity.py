#!/usr/bin/env python3
"""Keep fresh-install and version-migration WSI schemas identical."""

from __future__ import annotations

import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
FRESH = ROOT / "src/main/resources/db-scripts/clickhouse/init/schema.sql"
MIGRATION = ROOT / "src/main/resources/db-scripts/clickhouse/migrate/migrate_schema.sql"
TABLES = (
    "wsi_patient",
    "wsi_part",
    "wsi_block",
    "wsi_slide",
    "wsi_slide_placement",
    "wsi_slide_timing",
)


def definition(path: Path, table: str) -> str:
    sql = path.read_text(encoding="utf-8")
    matches = re.findall(
        rf"CREATE TABLE(?: IF NOT EXISTS)? {table}\s*\(.*?\) ENGINE = .*?;",
        sql,
        re.DOTALL,
    )
    if not matches:
        raise AssertionError(f"{path} does not define {table}")
    # A migration may rebuild a table over several versions. Its final CREATE
    # is the schema reached by an upgraded database and is what must match a
    # fresh install.
    value = re.sub(r"--[^\n]*", "", matches[-1])
    value = value.replace("CREATE TABLE IF NOT EXISTS", "CREATE TABLE")
    # Version 3.3.0 updates the existing WSI table constraints in place so
    # already-hydrated blue/green databases retain their rows.  Fold that
    # ALTER into the effective definition used for fresh/migrated parity.
    if path == MIGRATION and table == "wsi_slide":
        value = value.replace(
            "CONSTRAINT wsi_slide_type_valid CHECK slide_type IN ('H&E', 'IHC', 'Other')",
            "CONSTRAINT wsi_slide_type_valid CHECK slide_type IN ('H&E', 'IHC', 'Other', 'Unknown')",
        ).replace(
            "AND (slide_type != 'Other' OR NOT is_hne AND NOT is_ihc)",
            "AND (slide_type NOT IN ('Other', 'Unknown') OR NOT is_hne AND NOT is_ihc)",
        )
    return " ".join(value.split())


for table_name in TABLES:
    fresh = definition(FRESH, table_name)
    migrated = definition(MIGRATION, table_name)
    if fresh != migrated:
        raise AssertionError(
            f"fresh and migrated definitions differ for {table_name}\n"
            f"fresh: {fresh}\nmigrated: {migrated}"
        )

migration_sql = MIGRATION.read_text(encoding="utf-8")
section_31 = migration_sql.split("## db_schema_version: 3.1.0", 1)[1].split(
    "## db_schema_version: 3.2.0", 1
)[0]
section_32 = migration_sql.split("## db_schema_version: 3.2.0", 1)[1]
if "slide_type Nullable(String)" not in section_31:
    raise AssertionError("published 3.1.0 WSI migration was rewritten")
if "slide_type String" not in section_32:
    raise AssertionError("3.2.0 does not upgrade WSI to non-null slide_type")
section_33 = migration_sql.split("## db_schema_version: 3.3.0", 1)[1]
if "slide_type IN ('H&E', 'IHC', 'Other', 'Unknown')" not in section_33:
    raise AssertionError("3.3.0 does not publish the Unknown stain category")

slide = definition(FRESH, "wsi_slide")
for required in (
    "slide_type String",
    "CONSTRAINT wsi_slide_type_valid",
    "CONSTRAINT wsi_slide_stain_flags_valid",
):
    if required not in slide:
        raise AssertionError(f"wsi_slide is missing contract enforcement: {required}")
for forbidden in ("release_id", "procedure_date", "mrn", "diagnosis_date"):
    if forbidden in " ".join(definition(FRESH, table) for table in TABLES):
        raise AssertionError(f"WSI serving schema contains forbidden column {forbidden}")

print("ClickHouse fresh/migration WSI schema parity passed")
