#!/usr/bin/env python3
"""Replay the heavy QC corpus against one running backend.

For each request: POST the decoded JSON body to base_url+path (query string included),
record HTTP status, latency, response length, a raw SHA-256, and a canonical SHA-256
of the response (arrays/objects sorted so incidental ordering differences don't
register as mismatches).

Output: one JSONL line per request to --out. Pass --save-bodies <dir> to also dump
each raw response to <dir>/<idx>.json so a parity mismatch can be inspected.
"""
import argparse, base64, hashlib, json, sys, time, urllib.request, urllib.error
from concurrent.futures import ThreadPoolExecutor

def canonical(obj):
    """Stable serialization: sort object keys, and sort arrays by their own
    canonical form so element ordering is normalized."""
    if isinstance(obj, dict):
        return {k: canonical(obj[k]) for k in sorted(obj)}
    if isinstance(obj, list):
        norm = [canonical(x) for x in obj]
        try:
            return sorted(norm, key=lambda x: json.dumps(x, sort_keys=True))
        except Exception:
            return norm
    return obj

def canon_sha(text):
    try:
        obj = json.loads(text)
    except Exception:
        # non-JSON (error page etc) -> hash raw bytes
        return "raw:" + hashlib.sha256(text.encode("utf-8", "replace")).hexdigest()
    blob = json.dumps(canonical(obj), sort_keys=True, separators=(",", ":"))
    return "json:" + hashlib.sha256(blob.encode()).hexdigest()

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--corpus", required=True)
    ap.add_argument("--base-url", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("--save-bodies", help="dir to dump raw responses (optional)")
    ap.add_argument("--workers", type=int, default=1, help="concurrent requests")
    args = ap.parse_args()

    rows = [l.rstrip("\n").split("\t") for l in open(args.corpus) if l.strip()]
    out = open(args.out, "w")
    n_ok = n_err = 0
    if args.save_bodies:
        import os
        os.makedirs(args.save_bodies, exist_ok=True)

    def do_one(i, f):
        ep, path = f[0], f[1]
        qs = base64.b64decode(f[3]).decode() if len(f) > 3 and f[3] else ""
        body = base64.b64decode(f[5])  # raw JSON bytes
        url = args.base_url.rstrip("/") + path + (("?" + qs) if qs else "")
        req = urllib.request.Request(url, data=body, method="POST",
                                     headers={"Content-Type": "application/json",
                                              "Accept": "application/json"})
        t0 = time.perf_counter()
        status = 0; resp_text = ""; err = ""
        try:
            with urllib.request.urlopen(req, timeout=300) as r:
                status = r.status
                resp_text = r.read().decode("utf-8", "replace")
        except urllib.error.HTTPError as e:
            status = e.code
            resp_text = e.read().decode("utf-8", "replace")
            err = f"HTTP {e.code}"
        except Exception as e:
            err = repr(e)
        dt = (time.perf_counter() - t0) * 1000.0
        raw_sha = "raw:" + hashlib.sha256(resp_text.encode("utf-8", "replace")).hexdigest() if resp_text else ""
        # canonicalization reorders arrays (defends against benign ordering); skip for very
        # large bodies where the raw sha already settles parity cheaply.
        canon = canon_sha(resp_text) if (resp_text and len(resp_text) < 20_000_000) else "skipped"
        if args.save_bodies:
            with open(f"{args.save_bodies}/{i:05d}.json", "w") as bf:
                bf.write(resp_text)
        return {"idx": i, "ep": ep, "path": path, "req_sha": hashlib.sha256(body).hexdigest()[:16],
                "status": status, "latency_ms": round(dt, 1), "resp_len": len(resp_text),
                "resp_raw": raw_sha, "resp_canon": canon, "err": err}

    done = 0
    if args.workers <= 1:
        results = (do_one(i, f) for i, f in enumerate(rows))
    else:
        ex = ThreadPoolExecutor(max_workers=args.workers)
        results = ex.map(lambda p: do_one(*p), list(enumerate(rows)))
    for rec in results:
        out.write(json.dumps(rec) + "\n")
        done += 1
        if rec["err"]: n_err += 1
        else: n_ok += 1
        if done % 50 == 0:
            print(f"  {done}/{len(rows)} ok={n_ok} err={n_err}", file=sys.stderr, flush=True)
    out.close()
    print(f"DONE {args.base_url} (workers={args.workers}): {n_ok} ok, {n_err} err -> {args.out}", file=sys.stderr)

if __name__ == "__main__":
    main()
