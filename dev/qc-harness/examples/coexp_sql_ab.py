#!/usr/bin/env python3
"""SQL-level A/B example (PR #12187 co-expression rewrite).

Run with config.env in the environment:
  set -a; source ../config.env; set +a; python3 coexp_sql_ab.py

Measures a pure-SQL change directly via the ClickHouse HTTP X-ClickHouse-Summary
header (read_rows / read_bytes / elapsed) — no app build needed, since the
endpoint just serializes the query result.

QC for PR #12187 — co-expression single-scan SQL rewrite.

For each scenario, render the OLD (3-scan, master) and NEW (1-scan, PR) query,
run each several times against the ClickHouse clone, and capture:
  - parity: sorted result rows must be identical (PR claims byte-identical)
  - cost:   read_rows / read_bytes / elapsed from X-ClickHouse-Summary (the PR's claim)

The optional MyBatis sampleUniqueIds filter is omitted: real traffic uses *_all
sample lists (the whole profile), matching the PR's own full-dataset verification.
"""
import json, subprocess, statistics, sys

import os
CH = os.environ["CH_HTTP_URL"]
U, P = os.environ["CH_USER"], os.environ["CH_PASSWORD"]
DB = os.environ["CH_DB"]

OLD = """
WITH ref_values AS (
  SELECT sample_unique_id, toFloat64OrNull(alteration_value) AS ref_val
  FROM genetic_alteration_derived
  WHERE cancer_study_identifier = '{sa}' AND profile_type = '{pa}'
    AND hugo_gene_symbol = '{gene}'
    AND alteration_value != '' AND alteration_value != 'NA'
    AND toFloat64OrNull(alteration_value) IS NOT NULL
),
non_constant_genes AS (
  SELECT hugo_gene_symbol FROM genetic_alteration_derived
  WHERE cancer_study_identifier = '{sb}' AND profile_type = '{pb}'
    AND hugo_gene_symbol != '{gene}'
    AND alteration_value != '' AND alteration_value != 'NA'
    AND toFloat64OrNull(alteration_value) IS NOT NULL
  GROUP BY hugo_gene_symbol HAVING uniqExact(toFloat64OrNull(alteration_value)) > 1
)
SELECT g.entrez_gene_id AS entrezGeneId,
  rankCorr(r.ref_val, toFloat64OrNull(d.alteration_value)) AS spearmansCorrelation,
  count(*) AS numSamples
FROM genetic_alteration_derived d
INNER JOIN ref_values r ON d.sample_unique_id = r.sample_unique_id
INNER JOIN gene g ON d.hugo_gene_symbol = g.hugo_gene_symbol
WHERE d.cancer_study_identifier = '{sb}' AND d.profile_type = '{pb}'
  AND d.hugo_gene_symbol IN (SELECT hugo_gene_symbol FROM non_constant_genes)
  AND d.alteration_value != '' AND d.alteration_value != 'NA'
  AND toFloat64OrNull(d.alteration_value) IS NOT NULL
GROUP BY g.entrez_gene_id
HAVING count(*) >= 3 AND uniq(toFloat64OrNull(d.alteration_value)) > count(*) / 2
   AND isFinite(spearmansCorrelation) AND abs(spearmansCorrelation) >= {thr}
UNION ALL
SELECT g.entrez_gene_id AS entrezGeneId, NULL AS spearmansCorrelation, count(*) AS numSamples
FROM genetic_alteration_derived d
INNER JOIN ref_values r ON d.sample_unique_id = r.sample_unique_id
INNER JOIN gene g ON d.hugo_gene_symbol = g.hugo_gene_symbol
WHERE d.cancer_study_identifier = '{sb}' AND d.profile_type = '{pb}'
  AND d.hugo_gene_symbol != '{gene}'
  AND d.alteration_value != '' AND d.alteration_value != 'NA'
  AND toFloat64OrNull(d.alteration_value) IS NOT NULL
GROUP BY g.entrez_gene_id
HAVING count(*) < 3 OR uniqExact(toFloat64OrNull(d.alteration_value)) = 1
    OR uniq(toFloat64OrNull(d.alteration_value)) <= count(*) / 2
SETTINGS max_bytes_before_external_group_by = 2000000000
"""

NEW = """
WITH ref_values AS (
  SELECT sample_unique_id, toFloat64OrNull(alteration_value) AS ref_val
  FROM genetic_alteration_derived
  WHERE cancer_study_identifier = '{sa}' AND profile_type = '{pa}'
    AND hugo_gene_symbol = '{gene}'
    AND alteration_value != '' AND alteration_value != 'NA'
    AND toFloat64OrNull(alteration_value) IS NOT NULL
),
per_gene AS (
  SELECT g.entrez_gene_id AS entrezGeneId, count(*) AS numSamples,
    uniqExact(toFloat64OrNull(d.alteration_value)) AS exactDistinct,
    uniq(toFloat64OrNull(d.alteration_value)) AS approxDistinct,
    groupArray(r.ref_val) AS refValues,
    groupArray(toFloat64OrNull(d.alteration_value)) AS geneValues
  FROM genetic_alteration_derived d
  INNER JOIN ref_values r ON d.sample_unique_id = r.sample_unique_id
  INNER JOIN gene g ON d.hugo_gene_symbol = g.hugo_gene_symbol
  WHERE d.cancer_study_identifier = '{sb}' AND d.profile_type = '{pb}'
    AND d.hugo_gene_symbol != '{gene}'
    AND d.alteration_value != '' AND d.alteration_value != 'NA'
    AND toFloat64OrNull(d.alteration_value) IS NOT NULL
  GROUP BY g.entrez_gene_id
),
scored AS (
  SELECT entrezGeneId, numSamples,
    (numSamples >= 3 AND exactDistinct > 1 AND approxDistinct > numSamples / 2) AS validNumeric,
    arrayReduce('rankCorr',
      if(exactDistinct > 1, refValues, [0., 1.]),
      if(exactDistinct > 1, geneValues, [0., 1.])) AS corr
  FROM per_gene
)
SELECT entrezGeneId,
  if(validNumeric AND isFinite(corr), corr, NULL) AS spearmansCorrelation, numSamples
FROM scored
WHERE (validNumeric AND isFinite(corr) AND abs(corr) >= {thr}) OR (NOT validNumeric)
SETTINGS max_bytes_before_external_group_by = 2000000000
"""

SCEN = [
  dict(name="skcm_TNFRSF8_thr0.3(PR)", sa="skcm_tcga_pan_can_atlas_2018", pa="rna_seq_v2_mrna",
       sb="skcm_tcga_pan_can_atlas_2018", pb="rna_seq_v2_mrna", gene="TNFRSF8", thr=0.3),
  dict(name="ov_AHR_thr0", sa="ov_tcga", pa="rna_seq_v2_mrna", sb="ov_tcga", pb="rna_seq_v2_mrna",
       gene="AHR", thr=0.0),
  dict(name="ov_TP53_thr0.3", sa="ov_tcga", pa="rna_seq_v2_mrna", sb="ov_tcga", pb="rna_seq_v2_mrna",
       gene="TP53", thr=0.3),
  dict(name="ccle_EGFR_thr0", sa="ccle_broad_2025", pa="rna_seq_mrna", sb="ccle_broad_2025",
       pb="rna_seq_mrna", gene="EGFR", thr=0.0),
  dict(name="ov_mrnaU133_TP53_thr0.3", sa="ov_tcga", pa="mrna_U133", sb="ov_tcga", pb="mrna_U133",
       gene="TP53", thr=0.3),
]

def run(sql):
    """Return (sorted_result_text, read_rows, read_bytes, elapsed_s)."""
    r = subprocess.run(["curl","-s","-u",f"{U}:{P}","-D","/tmp/chh.txt",
                        f"{CH}/?database={DB}&default_format=TSV","--data-binary",sql],
                       capture_output=True, text=True, timeout=600)
    body = r.stdout
    if "DB::Exception" in body:
        return ("ERROR: "+body[:200], None, None, None)
    summ = {}
    for line in open("/tmp/chh.txt"):
        if line.lower().startswith("x-clickhouse-summary"):
            summ = json.loads(line.split(":",1)[1].strip())
    rows = sorted(body.strip().split("\n")) if body.strip() else []
    return ("\n".join(rows), int(summ.get("read_rows",0)), int(summ.get("read_bytes",0)),
            int(summ.get("elapsed_ns",0))/1e9)

ROUNDS = 3
print(f"{'scenario':<28}{'parity':<10}{'rows OLD→NEW':<26}{'bytesGB O→N':<18}{'med s O→N':<16}{'speedup'}")
allpass = True
for s in SCEN:
    old_sql = OLD.format(**s); new_sql = NEW.format(**s)
    res_o = res_n = None; to=[]; tn=[]; ro=rn=bo=bn=None
    for i in range(ROUNDS):  # alternate to share cache fairly; drop round 0
        o = run(old_sql); n = run(new_sql)
        if i == 0: res_o, res_n = o[0], n[0]; ro,bo = o[1],o[2]; rn,bn = n[1],n[2]
        else: to.append(o[3]); tn.append(n[3])
    if (res_o or "").startswith("ERROR") or (res_n or "").startswith("ERROR"):
        print(f"{s['name']:<28}ERROR     {(res_o or res_n)[:80]}"); allpass=False; continue
    parity = "IDENT" if res_o == res_n else "DIFFER"
    if parity=="DIFFER": allpass=False
    mo, mn = statistics.median(to), statistics.median(tn)
    spd = f"{mo/mn:.2f}x" if mn else "n/a"
    nrows = len(res_o.split("\n")) if res_o else 0
    print(f"{s['name']:<28}{parity:<10}{ro/1e6:.1f}M→{rn/1e6:.1f}M{'':<6}"
          f"{bo/1e9:.2f}→{bn/1e9:.2f}{'':<8}{mo:.2f}→{mn:.2f}{'':<7}{spd}  ({nrows} rows)")
print("\nPARITY VERDICT:", "PASS — all scenarios identical" if allpass else "FAIL — see DIFFER above")
