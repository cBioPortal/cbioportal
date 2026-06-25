#!/usr/bin/env python3
"""Parse an -Xlog:gc* ParallelGC log: peak heap + total allocated (freed) bytes.
Allocation proxy = sum over collections of (used_before - used_after); for a
workload returning to baseline this approximates total bytes allocated."""
import re, sys

unit = {'B':1,'K':1024,'M':1024**2,'G':1024**3}
def tobytes(v,u): return float(v)*unit[u]

# matches e.g. "1234M->567M(2048M)"
pat = re.compile(r'(\d+(?:\.\d+)?)([BKMG])->(\d+(?:\.\d+)?)([BKMG])\((\d+(?:\.\d+)?)([BKMG])\)')
peak=0.0; freed=0.0; n=0
for line in open(sys.argv[1]):
    m = pat.search(line)
    if not m: continue
    before = tobytes(m.group(1),m.group(2))
    after  = tobytes(m.group(3),m.group(4))
    peak = max(peak, before)
    if before>after: freed += (before-after)
    n+=1
reqs = int(sys.argv[2]) if len(sys.argv)>2 else 0
print(f"collections      : {n}")
print(f"peak heap used   : {peak/1024**2:.0f} MB")
print(f"total allocated  : {freed/1024**2:.0f} MB  (~{freed/1024**3:.2f} GB)")
if reqs:
    print(f"alloc per request: {freed/reqs/1024**2:.2f} MB/req  (over {reqs} reqs)")
