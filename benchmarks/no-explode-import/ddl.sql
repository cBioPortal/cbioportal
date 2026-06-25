-- Benchmark tables for: no-explode direct load vs. packed + ARRAY JOIN derive.
-- Run against a database where the benchmark user has CREATE/INSERT/SELECT/ALTER/OPTIMIZE/DROP.

-- Exploded target written by the NO-EXPLODE pipeline (importer emits rows directly).
CREATE TABLE IF NOT EXISTS exploded_direct
( sample_id String, hugo_gene_symbol String, profile_type LowCardinality(String), value Nullable(String) )
ENGINE = MergeTree ORDER BY (profile_type, hugo_gene_symbol, sample_id);

-- Exploded target written by the LEGACY pipeline (server-side ARRAY JOIN derive). Same schema.
CREATE TABLE IF NOT EXISTS exploded_legacy
( sample_id String, hugo_gene_symbol String, profile_type LowCardinality(String), value Nullable(String) )
ENGINE = MergeTree ORDER BY (profile_type, hugo_gene_symbol, sample_id);

-- Legacy packed base table: one row per gene, comma-joined per-sample values (mirrors genetic_alteration).
CREATE TABLE IF NOT EXISTS packed
( hugo_gene_symbol String, profile_type LowCardinality(String), `values` String )
ENGINE = MergeTree ORDER BY (profile_type, hugo_gene_symbol);

-- Per-profile sample order (mirrors genetic_profile_samples.ordered_sample_list), needed by the derive.
CREATE TABLE IF NOT EXISTS sample_order
( profile_type LowCardinality(String), sample_ids Array(String) )
ENGINE = MergeTree ORDER BY profile_type;
