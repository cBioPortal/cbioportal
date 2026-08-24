---
name: qc-pr
description: QC a cBioPortal optimization PR by replaying real captured traffic (cbioportal_qc) against OLD vs NEW builds to verify response parity and measure memory/latency. Use when asked to QC, verify, benchmark, or "confirm the same result / faster / lighter" for a performance or optimization PR.
---

# QC a cBioPortal optimization PR

Replays real production requests against two builds (OLD = baseline, NEW = the PR) and
reports **parity** (identical responses?), **memory**, and **latency**. The tools live in
`dev/qc-harness/` (see its `README.md` for full detail and the gotcha list); this skill is
the playbook for driving them.

## Requires: the ClickHouse MCP

Corpus building and every bit of data exploration — discovering which endpoints have
traffic, finding the pathological request shape for a memory test, checking study/value
coverage, investigating a suspected regression — read the `cbioportal_qc` request-log
database and the data clone. **The read-only replay credential (`apitests`) cannot read
`cbioportal_qc`**, so do those queries through the ClickHouse MCP (`mcp-clickhouse`), whose
user has the needed grants.

Configure it as a project MCP server in `.mcp.json` at the repo root (an example is shipped
at `dev/qc-harness/.mcp.json.example`):

```json
{
  "mcpServers": {
    "mcp-clickhouse": {
      "command": "uv",
      "args": ["run", "--with", "mcp-clickhouse", "--python", "3.13", "mcp-clickhouse"],
      "env": {
        "CLICKHOUSE_HOST": "dl96orhu96.us-east-1.aws.clickhouse.cloud",
        "CLICKHOUSE_PORT": "8443",
        "CLICKHOUSE_SECURE": "true",
        "CLICKHOUSE_VERIFY": "true",
        "CLICKHOUSE_USER": "<user-with-SELECT-on-cbioportal_qc-and-the-clone>",
        "CLICKHOUSE_PASSWORD": "<password>",
        "CLICKHOUSE_DATABASE": "cbioportal_qc"
      }
    }
  }
}
```

The MCP user needs `SELECT` on `cbioportal_qc` **and** the clone DB (e.g.
`cbioportal_public_clone_*`). Never commit real credentials — keep `.mcp.json` out of git.
It exposes `list_databases`, `list_tables`, and `run_query`.

## Choose the approach first

- **Pure SQL / mapper change** (e.g. a `*Mapper.xml` rewrite): skip the app. Render both query
  versions with real params and measure straight from the ClickHouse HTTP
  `X-ClickHouse-Summary` header (`read_rows` / `read_bytes` / `elapsed`). Pattern:
  `dev/qc-harness/examples/coexp_sql_ab.py`. `read_rows`/`read_bytes` are cache-independent and
  are exactly any "N× fewer rows read" claim; parity = identical sorted result rows.
- **App behavior change** (controllers/services/serialization): full end-to-end via `qc.sh`.
- **Any memory claim**: a broad corpus often shows *zero* benefit — see traps below.

## Steps (end-to-end)

1. `cp dev/qc-harness/config.env.example dev/qc-harness/config.env` and fill in creds.
2. Find the PR's changed endpoint(s) and confirm there's traffic (MCP):
   `SELECT endpoint, count(), uniqExact(body) FROM cbioportal_qc.logged_requests
    WHERE endpoint LIKE '%...%' GROUP BY endpoint`.
3. Run it:
   `dev/qc-harness/qc.sh <old-ref> <new-ref> '<endpoint-LIKE>' <label> [count] [random|size]`
   (`random` = representative mix; `size` = heaviest requests).
4. Read the output: **parity is the gate**; latency as the OLD→NEW *delta*; peak heap / alloc.

## Interpreting + traps (the parts that bite)

- **Parity is the gate.** A single mismatch is a blocker — dump bodies with `--save-bodies`
  and investigate before trusting any speed/memory number.
- **Coverage trap (most important).** A passing parity run only covers what you *sampled*.
  For changes that **narrow** behavior (filters, projections, `IN`-lists, case-sensitive
  matches), the regression hides in the rows you didn't replay. Before declaring parity:
  query the clone for the values the change could drop and confirm they still appear. (A
  case-sensitive property-name `IN` filter regression sat behind a green broad run exactly
  this way — caught only by querying the clone for lower/mixed-case values.)
- **Memory is request-shape-dependent.** "Heavy by request-body size" ≠ "heap-stressing."
  The win usually needs a specific shape — a few rows of a huge entity, or one large response
  multiplied by concurrency (`replay.py --workers N`). Construct that shape (often from the DB
  via MCP) rather than assuming a big corpus exercises it.
- **Boot the A/B sequentially** — one `-Xmx2g` JVM fits this box; two do not.
- **Other gotchas** (the runnable jar is `cbioportal-exec.jar`; `-Dmaven.gitcommitid.skip=true`
  in worktrees; gzip request bodies are byte-corrupted in the log and unreplayable; preserve
  query strings): documented in `dev/qc-harness/README.md`.

## Reporting

Lead with the parity verdict, then memory and latency as OLD→NEW deltas. State the corpus
(endpoint, count, random/size) and call out coverage limits explicitly — what shapes were and
were not exercised.
