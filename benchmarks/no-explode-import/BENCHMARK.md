# No-explode import benchmark: direct exploded load vs. packed + `ARRAY JOIN` derive

## Question

cBioPortal stores gene×sample matrix profiles (CNA, expression, …) in a **packed** base
table `genetic_alteration` (one row per gene, a comma-joined `values` string), then builds
the denormalized `genetic_alteration_derived` (one row per gene×sample) with a server-side
`ARRAY JOIN` **derive** step that is *fully rebuilt from scratch on every import*.

Can we instead have the importer write the **exploded** rows directly — no packed `values`,
no `ARRAY JOIN`, no derive step — and is that strategy correct and faster/cheaper?

## Method

- **Source:** a real datahub study, **`ccle_broad_2019`**, two genuine matrix files:
  - `data_cna.txt` — 23,312 genes × 1,030 samples = **24,011,360 cells** (discrete; avg value 1 char)
  - `data_mrna_seq_rpkm.txt` — 56,318 genes × 1,156 samples = **65,103,608 cells** (continuous; avg value 7.1 chars)
  - Total **89,114,968 cells** across two profiles.
- **Two pipelines**, same source file, into the same ClickHouse (Cloud, 26.2), measured to "queryable":
  - **Legacy** — load packed `genetic_alteration`-shaped rows, then a **scoped** server-side `ARRAY JOIN` derive into the exploded table.
  - **No-explode** — `clickhouse-local` reads the wide grid and streams the exploded `(sample, gene, profile, value)` rows directly into the exploded table (native protocol).
- **Correctness:** `cityHash64` checksum of the two exploded tables compared against **each other** (self-contained — no dependency on any pre-existing table).
- **Caveat on the baseline:** the Legacy pipeline here derives **only this study's slice**. Production `metaImport.py` does **not** do this — it *fully rebuilds the entire derived corpus on every import* ("Derived tables cannot be incrementally updated — they are always fully rebuilt from scratch"). So the Legacy numbers below are a *generous, hypothetical "incremental" lower bound* for legacy, not what production actually pays.
- Client: single box (6 cores), native protocol to ClickHouse Cloud. Absolute times reflect that link; the **ratios** generalize.

## Results

### Correctness — ✅ confirmed
Both profiles are **byte-identical** between the two pipelines:

| profile | rows | checksum (cityHash64 sum) |
|---|---|---|
| cna | 24,011,360 | 12187620144933403365 |
| rpkm | 65,103,608 | 8001463874369123421 |

The no-explode strategy produces exactly the table the legacy explode produces.

### Latency (to queryable)

| step | CNA | RPKM | total |
|---|---|---|---|
| Legacy — packed load | 1.11s | 4.67s | 5.78s |
| Legacy — `ARRAY JOIN` derive (scoped) | 3.22s | 7.98s | 11.20s |
| **Legacy total (scoped)** | **4.33s** | **12.65s** | **16.98s** |
| **No-explode — direct load** | **10.48s** | **30.23s** | **40.71s** |

### Server peak memory & storage

| | Legacy | No-explode |
|---|---|---|
| Server peak memory (derive) | **1.85 GiB** (CNA), **4.78 GiB** (RPKM) | none — insert-level (MB-scale) |
| Client peak RSS | ~768 MiB | 803 MiB (CNA), 1131 MiB (RPKM) |
| Persisted storage | **319.26 MiB** (packed 122.64 + exploded 196.60 + order) | **197.89 MiB** (exploded only) |

## Findings

1. **No-explode is correct** — byte-identical to the legacy explode for both discrete and continuous profiles.
2. **No-explode uses ~38% less storage** (198 vs 319 MiB) — it does not persist the redundant packed copy, which is non-trivial (123 MiB), especially for continuous data whose `values` strings compress poorly.
3. **No-explode has no server-memory spike.** The `ARRAY JOIN` derive peaked at **1.85 GiB (CNA) / 4.78 GiB (RPKM) for a single study**; the direct load stays at insert-level. At full-corpus scale this is the difference between a multi-GiB OOM-prone rebuild and bounded memory.
4. **No-explode is ~2.4× slower in wall-clock _for a remote client_** (40.7s vs 17.0s). This is **not** the explosion cost — the explosion is cheap. It is the cost of shipping the large exploded rows over the network. Legacy ships the tiny packed data and explodes server-side, so it moves far fewer bytes. The gap widens for continuous data (more bytes per cell). **For a co-located importer (same DC as ClickHouse), this network cost shrinks and the picture inverts toward no-explode.**

## Interpretation

The real trade is **where the explode runs and what crosses the network**, not whether to "explode":

- **No-explode / direct** (explode client-side, ship exploded rows): bounded server memory, less storage, immediately queryable, inherently per-study — but network-bound for a *remote* client.
- **Scoped server-side derive** (ship packed, `ARRAY JOIN` per study): network-light, but a multi-GiB server memory spike and it keeps a redundant packed staging table.
- **Production legacy** (ship packed, then **full-rebuild the entire corpus every import**): the worst of both — re-explodes ~11.8B rows (tens of minutes, multi-GiB) on every single import.

**Versus production legacy, no-explode wins decisively** (seconds-per-study + bounded memory + less storage vs. a full corpus rebuild). Versus a *hypothetical* scoped derive, no-explode trades remote-client wall-clock for memory and storage — and that wall-clock gap is a network artifact that disappears when the importer is co-located with ClickHouse.

## Reproduce

```
./run_benchmark.sh        # downloads ccle_broad_2019, creates tables, runs both pipelines, prints metrics
```
Requires the `clickhouse` binary and env vars `CH_HOST`, `CH_USER`, `CH_PASSWORD` for a database
with create/insert/select/optimize grants (see `ddl.sql`).
