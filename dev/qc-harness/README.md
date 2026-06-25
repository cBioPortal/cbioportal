# QC harness — replay real traffic to verify a PR

Tools to **replay real captured production traffic against two builds of cBioPortal**
and check that a change is correct (identical responses), and what it does to memory
and latency. Built for performance/optimization PRs where "does it still return the
same thing, and is it actually faster / lighter?" needs a quantitative answer.

It has been used to QC several PRs (gene-panel / generic-assay memory cut; the
copy-number-segments/mutations/clinical-data streaming change; the co-expression
single-scan rewrite).

## How it works

Real requests are captured in `cbioportal_qc.logged_requests` (method, path, query
string, headers, body, response status). The harness:

1. **builds two jars** from two git refs (OLD = baseline, NEW = the PR),
2. **extracts a corpus** of real requests for the endpoint(s) the PR touches,
3. **replays the corpus** against each backend (pointed at the public ClickHouse
   clone), recording status, latency, and a content hash of each response,
4. **compares** OLD vs NEW: parity (responses identical?), latency distribution, and
   peak heap / allocation from GC logs.

Backends are booted **one at a time** (`-Xmx2g`) — see gotchas.

## Setup

```bash
cp config.env.example config.env      # then set CH_PASSWORD (read-only staging cred)
```

`config.env` is gitignored. The defaults point at the same read-only public-staging
ClickHouse clone used by `.circleci/config.yml`.

## Quick start — full end-to-end A/B

```bash
# qc.sh <old-ref> <new-ref> <endpoint-LIKE> <label> [count] [order]
./qc.sh master my-pr-branch '%co-expression%' coexp 80 random
```

Prints a parity verdict, latency percentiles (OLD vs NEW), and peak heap / alloc.

## Quick start — pure-SQL change (no app build)

If the PR is only a mapper/SQL change, you can measure it directly against ClickHouse
via the HTTP `X-ClickHouse-Summary` header (read_rows / read_bytes / elapsed) without
building the app — see `examples/coexp_sql_ab.py`.

```bash
set -a; source config.env; set +a
python3 examples/coexp_sql_ab.py
```

## Pieces

| Path | What |
|---|---|
| `qc.sh` | orchestrator: build → extract → replay OLD → replay NEW → compare |
| `bin/build-ab.sh` | build OLD/NEW `cbioportal-exec.jar` from two refs (throwaway worktrees) |
| `bin/extract-corpus.sh` | pull a replay corpus for an endpoint from `cbioportal_qc` |
| `bin/run-backend.sh` | boot one jar vs the clone, clickhouse mode, GC-logged |
| `lib/replay.py` | replay a corpus, record status/latency/raw+canonical response hash; `--workers N` for concurrency |
| `lib/compare.py` | parity (raw or order-tolerant canonical) + latency diff of two result files |
| `lib/gcstats.py` | parse a `-Xlog:gc*` log → peak heap + allocated-bytes/request |
| `examples/coexp_sql_ab.py` | SQL-level A/B via X-ClickHouse-Summary (template) |

## Reading the results

- **Parity** is the gate. `compare.py` treats two responses as identical if the raw
  bytes match **or** their canonical forms match (arrays sorted) — endpoints without
  an `ORDER BY` legitimately return rows in different orders between builds.
- **Latency** is end-to-end HTTP. Absolute numbers are network/cache-sensitive
  (the clone is remote ClickHouse Cloud); the OLD-vs-NEW *delta* is the signal. The
  harness warms each backend before measuring.
- **Memory** = peak post-GC heap and allocated-bytes/request from the GC log.

## Gotchas (learned the hard way — don't rediscover these)

- **The runnable jar is `target/cbioportal-exec.jar`**, not `cbioportal.jar` (the
  latter is the thin, non-bootable jar).
- **`-Dmaven.gitcommitid.skip=true`** is required when building inside a git worktree
  (the git-commit-id plugin can't read a worktree's `.git` file and aborts the build).
- **`application.properties` is gitignored** — `build-ab.sh` seeds it so the build's
  resource-filtering step succeeds; the runtime datasource is overridden anyway.
- **Logged request bodies: the gzipped ones are byte-corrupted** in the table (a
  lossy-UTF-8 capture turned the gzip magic into U+FFFD). They are *not* replayable —
  `extract-corpus.sh` filters to clean JSON only. The corrupted bodies are usually the
  *largest* requests, so a clean corpus under-samples big responses (see next point).
- **Preserve query strings.** `projection`, `threshold`, `clinicalDataType`, etc. live
  in the query string and change the response. `replay.py` replays them; the corpus
  stores them.
- **Boot A/B sequentially.** One `-Xmx2g` JVM fits comfortably; two concurrently do
  not (~5 GB). `qc.sh` boots OLD, measures, shuts it down, then NEW.
- **Memory wins are request-shape-dependent.** "Heavy by request-body size" is *not*
  the same as "heap-stressing." Several optimizations only help a specific shape (e.g.
  a few samples from a 54k-sample profile, or a single very large response). A broad
  corpus can show *zero* memory benefit while the targeted shape shows a large one.
  When verifying a memory claim, **construct the pathological shape** (often from the
  DB, e.g. N samples of the biggest profile) and use **`--workers N` concurrency** to
  surface peak-heap differences — materializing 24 large responses at once is what
  pushes the heap, not one at a time.
- **For pure-SQL changes, skip the app.** Render both query versions and read
  `X-ClickHouse-Summary` — `read_rows`/`read_bytes` are cache-independent and are
  exactly the "N× fewer rows read" claim; latency there is cache-sensitive, so
  alternate runs and take medians.

## Corpus tips

`extract-corpus.sh <endpoint-LIKE> <label> <count> <out.tsv> [size|random]`

- `size` order → the largest requests (stress/heavy). `random` → representative mix.
- Corpus is TSV with every field base64-encoded (binary/quotes survive):
  `label  path  ct_b64  qs_b64  headers_b64  body_b64`.
- To find an endpoint's traffic volume / shape first:
  `SELECT endpoint, count(), uniqExact(body) FROM cbioportal_qc.logged_requests
   WHERE endpoint LIKE '%...%' GROUP BY endpoint`.
