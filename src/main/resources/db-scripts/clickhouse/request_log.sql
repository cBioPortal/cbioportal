-- Schema for the optional HTTP request log used for endpoint QC.
--
-- This is independent of the main derived-table schema in clickhouse.sql: it is
-- only needed when the backend is run with request-logging.enabled=true, in
-- which case every matching HTTP request is captured and inserted here through
-- the application's primary ClickHouse datasource (see
-- org.cbioportal.infrastructure.requestlog.*). The table is NOT created by the
-- application at startup; provision it out-of-band with this script.
--
-- The engine is a ReplacingMergeTree ordered by `id` (a SHA-256 of
-- method + path + query + body), so repeated observations of the same logical
-- request collapse to a single row during background merges. Read the
-- deduplicated set with FINAL (or GROUP BY id). There is intentionally no TTL:
-- rows persist until explicitly deleted.
--
-- The default target is cbioportal_qc.logged_requests; override with
-- request-logging.table. The runtime datasource user needs INSERT on this
-- table (plus SELECT/OPTIMIZE to query and compact it), e.g.:
--   GRANT SELECT, INSERT, OPTIMIZE ON cbioportal_qc.* TO <app_user>;

CREATE DATABASE IF NOT EXISTS cbioportal_qc;

CREATE TABLE IF NOT EXISTS cbioportal_qc.logged_requests
(
    id String,                              -- SHA-256(method + path + query + body); dedup key
    method LowCardinality(String),
    path String,                            -- request path without the query string
    endpoint String,                        -- matched controller route, e.g. /api/studies/{studyId}
    query_string String,
    server_name LowCardinality(String),     -- host the request was addressed to
    url String,                             -- fully reconstructed URL incl. scheme, host, query
    headers String,                         -- JSON array of {name, value}
    content_type String,
    body String,
    body_truncated UInt8,                   -- 1 when the body exceeded the capture cap
    response_status UInt16,
    seen DateTime,                          -- when this observation was captured
    git_commit LowCardinality(String),      -- backend build commit (suffixed -dirty if uncommitted)
    INDEX idx_endpoint endpoint TYPE bloom_filter GRANULARITY 4
)
ENGINE = ReplacingMergeTree(seen)
ORDER BY id;
