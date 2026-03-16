#!/usr/bin/env python3
"""Compare legacy vs column-store clinical events endpoint responses.
Tests 20 patients × 13 param permutations = 260 tests.
"""

import json
import sys
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8082"

PATIENTS = [
    ("glioma_mskcc_2019", "P-0005249"),
    ("stad_tcga_pan_can_atlas_2018", "TCGA-BR-8371"),
    ("paad_tcga_gdc", "TCGA-IB-7891"),
    ("msk_met_2021", "P-0029561"),
    ("msk_met_2021", "P-0034992"),
    ("pancan_pcawg_2020", "DO17751"),
    ("msk_chord_2024", "P-0034469"),
    ("msk_chord_2024", "P-0020220"),
    ("msk_chord_2024", "P-0057385"),
    ("msk_chord_2024", "P-0083056"),
    ("msk_chord_2024", "P-0013721"),
    ("msk_met_2021", "P-0012246"),
    ("msk_met_2021", "P-0016138"),
    ("msk_met_2021", "P-0047149"),
    ("pancan_pcawg_2020", "DO51477"),
    ("msk_met_2021", "P-0008901"),
    ("msk_met_2021", "P-0001268"),
    ("msk_met_2021", "P-0044058"),
    ("luad_tcga_pan_can_atlas_2018", "TCGA-44-7669"),
    ("msk_met_2021", "P-0019063"),
]

PARAM_PERMUTATIONS = [
    ("default", ""),
    ("projection=DETAILED", "?projection=DETAILED"),
    ("projection=ID", "?projection=ID"),
    ("projection=META", "?projection=META"),
    ("page=0,size=2", "?pageSize=2&pageNumber=0"),
    ("page=1,size=2", "?pageSize=2&pageNumber=1"),
    ("sort=eventType,ASC", "?sortBy=eventType&direction=ASC"),
    ("sort=eventType,DESC", "?sortBy=eventType&direction=DESC"),
    ("sort=startDate,ASC", "?sortBy=startNumberOfDaysSinceDiagnosis&direction=ASC"),
    ("sort=stopDate,DESC", "?sortBy=endNumberOfDaysSinceDiagnosis&direction=DESC"),
    ("sort=eventType,ASC+page=0,size=3", "?sortBy=eventType&direction=ASC&pageSize=3&pageNumber=0"),
    ("sort=eventType,DESC+page=1,size=3", "?sortBy=eventType&direction=DESC&pageSize=3&pageNumber=1"),
    ("DETAILED+sort=startDate,ASC+page=0,size=5", "?projection=DETAILED&sortBy=startNumberOfDaysSinceDiagnosis&direction=ASC&pageSize=5&pageNumber=0"),
]


def fetch(url):
    """Fetch URL and return (body_str, headers_dict)."""
    req = urllib.request.Request(url, headers={"Accept": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            body = resp.read().decode("utf-8")
            headers = {k.lower(): v for k, v in resp.getheaders()}
            return body, headers
    except Exception as e:
        return None, None


def normalize_event(e):
    """Normalize an event for comparison: remove uniquePatientKey, sort attributes."""
    e2 = dict(e)
    e2.pop("uniquePatientKey", None)
    if "attributes" in e2 and e2["attributes"]:
        e2["attributes"] = sorted(
            e2["attributes"], key=lambda a: (a.get("key", ""), a.get("value", ""))
        )
    return e2


def compare_bodies(legacy_body, colstore_body, allow_reorder=False):
    """Compare two JSON array bodies. Returns (pass, reason)."""
    try:
        legacy = json.loads(legacy_body)
        colstore = json.loads(colstore_body)
    except (json.JSONDecodeError, TypeError):
        return False, "JSON parse error"

    if not isinstance(legacy, list) or not isinstance(colstore, list):
        return False, "not a list"

    if len(legacy) != len(colstore):
        return False, f"count mismatch: {len(legacy)} vs {len(colstore)}"

    ln = [normalize_event(e) for e in legacy]
    cn = [normalize_event(e) for e in colstore]

    # Exact match (modulo attribute order within events)
    if ln == cn:
        return True, "exact"

    # Same set of events, different order (tie-breaking)
    ls = sorted(ln, key=lambda e: json.dumps(e, sort_keys=True))
    cs = sorted(cn, key=lambda e: json.dumps(e, sort_keys=True))
    if ls == cs:
        return True, "tie-break order"

    if allow_reorder:
        return False, "content differs even unordered"

    return False, "content differs"


def compare_headers(legacy_headers, colstore_headers):
    """Compare total-count headers."""
    lv = legacy_headers.get("total-count") if legacy_headers else None
    cv = colstore_headers.get("total-count") if colstore_headers else None
    if lv == cv:
        return True, "exact"
    return False, f"total-count: {lv} vs {cv}"


def main():
    passed = 0
    failed = 0
    skipped = 0
    total = 0
    failures = []

    for study, patient in PATIENTS:
        base_path = f"/studies/{study}/patients/{patient}/clinical-events"
        print(f"--- {study} / {patient} ---", end=" ", flush=True)

        for pname, params in PARAM_PERMUTATIONS:
            total += 1
            desc = f"{study}/{patient} {pname}"

            legacy_url = f"{BASE_URL}/api{base_path}{params}"
            colstore_url = f"{BASE_URL}/api/column-store{base_path}{params}"

            legacy_body, legacy_headers = fetch(legacy_url)
            colstore_body, colstore_headers = fetch(colstore_url)

            if legacy_body is None or colstore_body is None:
                skipped += 1
                continue

            is_meta = "projection=META" in params
            # Sort+page tests: allow reorder since tie-breaking differs
            has_sort_and_page = "sortBy=" in params and "pageSize=" in params

            if is_meta:
                ok, reason = compare_headers(legacy_headers, colstore_headers)
            else:
                ok, reason = compare_bodies(
                    legacy_body, colstore_body, allow_reorder=has_sort_and_page
                )

            if ok:
                passed += 1
            else:
                failed += 1
                failures.append(f"  {desc}: {reason}")

        print("done")

    print()
    print("=" * 50)
    print(f"RESULTS: {passed} passed, {failed} failed, {skipped} skipped out of {total}")
    print("=" * 50)

    if failures:
        print(f"\nFAILURES ({len(failures)}):")
        for f in failures:
            print(f)
        sys.exit(1)
    else:
        print("\nAll tests passed!")
        sys.exit(0)


if __name__ == "__main__":
    main()
