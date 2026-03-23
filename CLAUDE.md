# StarRocks Migration Plan

## Context

cBioPortal has fully migrated from MySQL to ClickHouse as its sole database. We are evaluating migrating to StarRocks for better update support. The main pain point is frequent updates to patients/samples (many times a day), which is difficult because:

1. ClickHouse is append-only — no efficient row-level updates
2. The schema uses 10+ denormalized "derived tables" (built in `src/main/resources/db-scripts/clickhouse/clickhouse.sql`) that must be fully rebuilt (DROP + CREATE + INSERT) on every data change
3. A single patient/sample edit fans out across all derived tables

## Why StarRocks

- **Primary Key tables** with real upserts (row-store delta + column-store base)
- **CBO + broadcast joins** that would eliminate most derived tables (dimension tables are small)
- **Incremental materialized views** for the 2 large exploded tables
- MySQL wire protocol compatibility

## Data Scale (from ClickHouse cgds_public_staging)

- 535 studies, 372K patients, 395K samples, 44.8K genes, 2,487 genetic profiles
- Total: 77 GB on disk (compressed), 798 GB uncompressed
- Largest table: `genetic_alteration_derived` — 10.3B rows, ~30GB on disk
- Second largest: `generic_assay_data_derived` — 410M rows
- `genomic_event_derived` — 50M rows (8-way join)
- `mutation_derived` — 18.5M rows (7-way join)

## Derived Tables to Eliminate in StarRocks

These are pre-computed joins against small dimension tables — StarRocks CBO handles them at query time:
- `sample_derived` (395K rows)
- `sample_to_gene_panel_derived` (1.4M rows)
- `gene_panel_to_gene_derived` (71K rows)
- `clinical_data_derived` (14M rows)
- `clinical_event_derived` (2.6M rows)
- `mutation_derived` (18.5M rows)

## Tables to Keep as Native Format

The ARRAY JOIN explosion of packed CSV data — adopt exploded form as native format in ETL:
- `genetic_alteration_derived` (10.3B rows) → becomes `genetic_alteration` in StarRocks
- `generic_assay_data_derived` (410M rows) → becomes `generic_assay_data` in StarRocks

## Migration Steps

1. Install StarRocks locally (Docker or binary)
2. Configure MCP access: MySQL MCP server at 127.0.0.1:9030 + ClickHouse MCP for source data
3. Create StarRocks schema (Primary Key tables for dimensions, Duplicate Key for facts)
4. Migrate data from ClickHouse via MCP (SELECT from CH → INSERT into SR)
5. For 10B-row table: use StarRocks ClickHouse catalog connector for server-to-server transfer
6. Benchmark key joins live against normalized tables
7. Port 14 MyBatis mapper XMLs from ClickHouse SQL dialect to StarRocks

## Application Architecture Notes

- Spring Boot + MyBatis, 14 mapper XMLs in `src/main/resources/mappers/clickhouse/`
- ClickHouse-specific SQL in mappers: `arrayMap`, `splitByString`, `ARRAY JOIN`, `LowCardinality`, `EXCHANGE TABLES`, `base64Encode`
- JDBC driver: `com.clickhouse:clickhouse-jdbc:0.6.2`
- Config: `ClickhouseMyBatisConfig.java`
- Zero UPDATE/DELETE in app code — all INSERT + derived table rebuilds
