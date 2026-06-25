#!/usr/bin/env python3
"""Compare two replay result files (OLD vs NEW) for parity + latency."""
import json, sys, statistics

def load(p):
    return {r["idx"]: r for r in (json.loads(l) for l in open(p))}

old = load(sys.argv[1]); new = load(sys.argv[2])
idxs = sorted(set(old) & set(new))

status_mismatch=[]; parity_mismatch=[]; errors=[]
lat_old=[]; lat_new=[]
by_ep = {}
for i in idxs:
    o, n = old[i], new[i]
    ep = o["ep"]; by_ep.setdefault(ep, {"n":0,"parity_ok":0})
    by_ep[ep]["n"] += 1
    if o["err"] or n["err"]:
        errors.append((i, ep, o["err"], n["err"]))
    if o["status"] != n["status"]:
        status_mismatch.append((i, ep, o["status"], n["status"]))
    # identical if raw bytes match, OR (ordering-tolerant) canonical forms match
    raw_eq = o.get("resp_raw","") == n.get("resp_raw","")
    canon_eq = (o["resp_canon"] == n["resp_canon"]) and o["resp_canon"] not in ("", "skipped")
    if raw_eq or canon_eq:
        by_ep[ep]["parity_ok"] += 1
    else:
        parity_mismatch.append((i, ep, o["path"], o.get("resp_raw","")[:18]+"/"+o["resp_canon"][:14],
                                n.get("resp_raw","")[:18]+"/"+n["resp_canon"][:14]))
    if o["status"]==200: lat_old.append(o["latency_ms"])
    if n["status"]==200: lat_new.append(n["latency_ms"])

def pct(v,q):
    return round(statistics.quantiles(v, n=100)[q-1],1) if len(v)>=2 else (v[0] if v else 0)

print("="*64)
print(f"PARITY  (n={len(idxs)} requests compared)")
for ep,d in sorted(by_ep.items()):
    print(f"  {ep}: {d['parity_ok']}/{d['n']} identical")
print(f"  status mismatches : {len(status_mismatch)}")
print(f"  parity mismatches : {len(parity_mismatch)}")
print(f"  errors            : {len(errors)}")
if status_mismatch[:10]:
    print("  -- status mismatches --")
    for i,ep,a,b in status_mismatch[:10]: print(f"    idx{i} {ep}: OLD={a} NEW={b}")
if parity_mismatch[:10]:
    print("  -- response mismatches (idx, ep, path) --")
    for i,ep,path,a,b in parity_mismatch[:10]: print(f"    idx{i} {ep} {path}\n       OLD {a}\n       NEW {b}")
if errors[:10]:
    print("  -- errors --")
    for i,ep,oe,ne in errors[:10]: print(f"    idx{i} {ep}: OLD={oe!r} NEW={ne!r}")
print("="*64)
print("LATENCY (200-OK requests, ms)")
print(f"            {'OLD':>10} {'NEW':>10}   delta")
for label,q in [("p50",50),("p90",90),("p95",95),("p99",99)]:
    o=pct(lat_old,q); n=pct(lat_new,q)
    d = f"{(n-o)/o*100:+.1f}%" if o else "n/a"
    print(f"  {label:>5}    {o:>10} {n:>10}   {d}")
print(f"  mean     {round(statistics.mean(lat_old),1):>10} {round(statistics.mean(lat_new),1):>10}")
print(f"  sum(s)   {round(sum(lat_old)/1000,1):>10} {round(sum(lat_new)/1000,1):>10}")
print("="*64)
verdict = "PASS" if not status_mismatch and not parity_mismatch and not errors else "FAIL"
print(f"PARITY VERDICT: {verdict}")
