#!/usr/bin/env python3

# ClickHouse-compatible replacement for updateOncokbAnnotations.py.
#
# The original script uses pymysql/MySQLdb which speaks the MySQL wire protocol.
# ClickHouse uses a different protocol (HTTP on port 8123), so we connect via
# Python's built-in urllib and send SQL over the ClickHouse HTTP interface.
#
# SQL differences vs the MySQL original:
#   - Column/table names are lowercase (ClickHouse is case-sensitive)
#   - String literals use single quotes (ClickHouse rejects double-quoted strings)
#   - UPDATE uses ClickHouse's "ALTER TABLE ... UPDATE ... WHERE ..." syntax
#     (standard SQL UPDATE is not supported on MergeTree tables)
#   - No transaction/commit needed; ClickHouse is auto-commit
#   - After all ALTER TABLE UPDATEs we wait for background mutations to finish

import argparse
import importlib
import logging.handlers
import sys
import urllib.request
import urllib.parse
import urllib.error
import time
from pathlib import Path
from cbioportal_common import get_database_properties
import libImportOncokb

if __name__ == "__main__" and (__package__ is None or __package__ == ''):
    sys.path[0] = str(Path(sys.path[0]).resolve().parent)
    __package__ = 'importer'
    importlib.import_module(__package__)

ERROR_FILE = sys.stderr
REFERENCE_GENOME = {'hg19': 'GRCh37', 'hg38': 'GRCh38'}

cna_alteration_types = {
    "DELETION": -2,
    "LOSS": -1,
    "GAIN": 1,
    "AMPLIFICATION": 2,
}


def ch_query(base_url, sql, params=None):
    """Execute a SQL statement against ClickHouse HTTP API. Returns response text."""
    url = base_url
    if params:
        url += '&' + urllib.parse.urlencode(params)
    data = sql.encode('utf-8')
    req = urllib.request.Request(url, data=data, method='POST')
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        raise RuntimeError(f"ClickHouse error for SQL:\n{sql[:300]}\nResponse: {body}") from e


def build_base_url(portal_properties):
    """Parse jdbc:ch://host:port/db from spring.datasource.url and build HTTP base URL."""
    url = portal_properties.database_url  # e.g. jdbc:ch://host:8123/cbioportal
    url = url.replace('jdbc:ch://', '')
    url = url.split('|')[0]              # strip any trailing pipe (JDBC extras)
    host_port, database = url.split('/', 1)
    user = urllib.parse.quote(portal_properties.database_user)
    pw   = urllib.parse.quote(portal_properties.database_pw)
    db   = urllib.parse.quote(database)
    return f"http://{host_port}/?user={user}&password={pw}&database={db}"


def fetchall(base_url, sql):
    """Run a SELECT and return list of rows (each row is a list of strings)."""
    result = ch_query(base_url, sql + ' FORMAT TSV')
    rows = []
    for line in result.strip().splitlines():
        if line:
            rows.append(line.split('\t'))
    return rows


def get_current_mutation_data(study_id, base_url):
    sql = (
        "SELECT genetic_profile.genetic_profile_id, mutation_event.entrez_gene_id, "
        "mutation_event.protein_change AS alteration, "
        "mutation_event.mutation_type AS consequence, "
        "mutation.mutation_event_id, mutation.sample_id "
        "FROM mutation_event "
        "INNER JOIN mutation ON mutation.mutation_event_id = mutation_event.mutation_event_id "
        "INNER JOIN genetic_profile ON genetic_profile.genetic_profile_id = mutation.genetic_profile_id "
        "INNER JOIN cancer_study ON cancer_study.cancer_study_id = genetic_profile.cancer_study_id "
        f"WHERE cancer_study.cancer_study_identifier = '{study_id}'"
    )
    rows = fetchall(base_url, sql)
    return [
        {"id": "_".join([r[4], r[0], r[5]]),
         "geneticProfileId": r[0], "entrezGeneId": r[1],
         "alteration": r[2], "consequence": r[3]}
        for r in rows
    ]


def get_current_cna_data(study_id, base_url):
    sql = (
        "SELECT genetic_profile.genetic_profile_id, cna_event.entrez_gene_id, cna_event.alteration, "
        "sample_cna_event.cna_event_id, sample_cna_event.sample_id "
        "FROM cna_event "
        "INNER JOIN sample_cna_event ON sample_cna_event.cna_event_id = cna_event.cna_event_id "
        "INNER JOIN genetic_profile ON genetic_profile.genetic_profile_id = sample_cna_event.genetic_profile_id "
        "INNER JOIN cancer_study ON cancer_study.cancer_study_id = genetic_profile.cancer_study_id "
        f"WHERE cancer_study.cancer_study_identifier = '{study_id}'"
    )
    rows = fetchall(base_url, sql)
    result = []
    for r in rows:
        alteration = list(cna_alteration_types.keys())[
            list(cna_alteration_types.values()).index(int(r[2]))]
        result.append({"id": "_".join([r[3], r[0], r[4]]),
                        "geneticProfileId": r[0], "entrezGeneId": r[1],
                        "alteration": alteration})
    return result


def get_current_sv_data(study_id, base_url):
    sql = (
        "SELECT genetic_profile.genetic_profile_id, structural_variant.site1_entrez_gene_id, "
        "structural_variant.site2_entrez_gene_id, structural_variant.event_info, "
        "structural_variant.internal_id, structural_variant.sample_id "
        "FROM structural_variant "
        "INNER JOIN genetic_profile ON genetic_profile.genetic_profile_id = structural_variant.genetic_profile_id "
        "INNER JOIN cancer_study ON cancer_study.cancer_study_id = genetic_profile.cancer_study_id "
        f"WHERE cancer_study.cancer_study_identifier = '{study_id}'"
    )
    rows = fetchall(base_url, sql)
    return [
        {"id": "_".join([r[4], r[0], r[5]]),
         "geneticProfileId": r[0], "entrezGeneIdA": r[1],
         "entrezGeneIdB": r[2], "structuralVariantType": r[3]}
        for r in rows
    ]


def get_reference_genome(study_id, base_url):
    sql = (
        "SELECT reference_genome.name FROM reference_genome "
        "INNER JOIN cancer_study ON cancer_study.reference_genome_id = reference_genome.reference_genome_id "
        f"WHERE cancer_study.cancer_study_identifier = '{study_id}'"
    )
    rows = fetchall(base_url, sql)
    if len(rows) == 1:
        return REFERENCE_GENOME[rows[0][0]]
    raise ValueError(f"Unexpected reference genome rows: {rows}")


def get_current_annotation_data(study_id, base_url):
    sql = (
        "SELECT alteration_driver_annotation.alteration_event_id, "
        "alteration_driver_annotation.genetic_profile_id, "
        "alteration_driver_annotation.sample_id "
        "FROM alteration_driver_annotation "
        "INNER JOIN genetic_profile ON genetic_profile.genetic_profile_id = alteration_driver_annotation.genetic_profile_id "
        "INNER JOIN cancer_study ON cancer_study.cancer_study_id = genetic_profile.cancer_study_id "
        f"WHERE cancer_study.cancer_study_identifier = '{study_id}'"
    )
    rows = fetchall(base_url, sql)
    return set("_".join([r[0], r[1], r[2]]) for r in rows)


def wait_for_mutations(base_url, table='alteration_driver_annotation', timeout=60):
    """Wait for all ClickHouse ALTER TABLE UPDATE mutations on the given table to finish."""
    deadline = time.time() + timeout
    while time.time() < deadline:
        rows = fetchall(base_url,
            f"SELECT count() FROM system.mutations WHERE table = '{table}' AND is_done = 0")
        if rows and rows[0][0] == '0':
            return
        time.sleep(1)
    raise TimeoutError(f"ClickHouse mutations on '{table}' did not finish within {timeout}s")


def update_annotations(result, study_id, base_url):
    current_annotation_data = get_current_annotation_data(study_id, base_url)
    for entry in result:
        parsed_id = entry["query"]["id"].split("_")
        event_id, genetic_profile_id, sample_id = parsed_id[0], parsed_id[1], parsed_id[2]
        oncogenic = libImportOncokb.evaluate_driver_passenger(entry["oncogenic"])
        key = "_".join([event_id, genetic_profile_id, sample_id])
        if key in current_annotation_data:
            # ClickHouse MergeTree does not support standard UPDATE; use ALTER TABLE UPDATE
            sql = (
                f"ALTER TABLE alteration_driver_annotation "
                f"UPDATE driver_filter = '{oncogenic}' "
                f"WHERE alteration_event_id = {event_id} "
                f"AND genetic_profile_id = {genetic_profile_id} "
                f"AND sample_id = {sample_id}"
            )
        else:
            sql = (
                f"INSERT INTO alteration_driver_annotation "
                f"(alteration_event_id, genetic_profile_id, sample_id, driver_filter, "
                f"driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) "
                f"VALUES ({event_id}, {genetic_profile_id}, {sample_id}, '{oncogenic}', '', '', '')"
            )
        ch_query(base_url, sql)

    # Wait for any ALTER TABLE UPDATE mutations to complete before returning
    wait_for_mutations(base_url)


# OncoKB fetch helpers (unchanged logic from original)
def fetch_oncokb_mutation_annotations(mutation_data, ref_genome):
    request_url = libImportOncokb.DEFAULT_ONCOKB_URL + "/annotate/mutations/byProteinChange"
    elements = {}
    for m in mutation_data:
        elements[m["id"]] = (
            '{"alteration": "' + m["alteration"] + '", "consequence": "' + m["consequence"] +
            '", "gene": {"entrezGeneId": ' + str(m["entrezGeneId"]) +
            '}, "id": "' + m["id"] + '", "referenceGenome": "' + ref_genome + '"}'
        )
    return libImportOncokb.fetch_oncokb_annotations(list(elements.values()), request_url)


def fetch_oncokb_copy_number_annotations(copy_number_data, ref_genome):
    request_url = libImportOncokb.DEFAULT_ONCOKB_URL + "/annotate/copyNumberAlterations"
    elements = {}
    for c in copy_number_data:
        elements[c["id"]] = (
            '{"copyNameAlterationType":"' + c["alteration"] +
            '", "gene":{"entrezGeneId":' + str(c["entrezGeneId"]) +
            '}, "id":"' + c["id"] + '", "referenceGenome": "' + ref_genome + '"}'
        )
    return libImportOncokb.fetch_oncokb_annotations(list(elements.values()), request_url)


def fetch_oncokb_sv_annotations(sv_data, ref_genome):
    request_url = libImportOncokb.DEFAULT_ONCOKB_URL + "/annotate/structuralVariants"
    elements = {}
    for sv in sv_data:
        elements[sv["id"]] = (
            '{"structuralVariantType":"' + sv["structuralVariantType"].upper() +
            '", "geneA":{"entrezGeneId":' + str(sv["entrezGeneIdA"]) +
            '}, "geneB":{"entrezGeneId":' + str(sv["entrezGeneIdB"]) +
            '}, "id":"' + sv["id"] + '", "referenceGenome": "' + ref_genome + '"}'
        )
    return libImportOncokb.fetch_oncokb_annotations(list(elements.values()), request_url, sv=True)


def main_import(study_id, properties_filename):
    portal_properties = get_database_properties(properties_filename)
    if portal_properties is None:
        print('failure reading properties file (%s)' % properties_filename, file=ERROR_FILE)
        sys.exit(1)

    base_url = build_base_url(portal_properties)

    mutation_study_data = get_current_mutation_data(study_id, base_url)
    cna_study_data      = get_current_cna_data(study_id, base_url)
    sv_study_data       = get_current_sv_data(study_id, base_url)
    ref_genome          = get_reference_genome(study_id, base_url)

    mutation_result = fetch_oncokb_mutation_annotations(mutation_study_data, ref_genome)
    cna_result      = fetch_oncokb_copy_number_annotations(cna_study_data, ref_genome)
    sv_result       = fetch_oncokb_sv_annotations(sv_study_data, ref_genome)
    all_results     = mutation_result + cna_result + sv_result

    update_annotations(all_results, study_id, base_url)
    print('Update complete')
    return 0


def interface():
    parser = argparse.ArgumentParser(description='cBioPortal OncoKB annotation updater (ClickHouse)')
    parser.add_argument('-s', '--study_id', type=str, required=True)
    parser.add_argument('-p', '--portal_properties', type=str, required=True)
    return parser.parse_args()


if __name__ == '__main__':
    try:
        parsed_args = interface()
        exit_status = main_import(parsed_args.study_id, parsed_args.portal_properties)
    finally:
        logging.shutdown()
        del logging._handlerList[:]
    print(('Update of OncoKB annotations for mutations {status}.'.format(
        status={0: 'succeeded', 1: 'failed', 2: 'not performed as problems occurred',
                3: 'succeeded with warnings'}.get(exit_status, 'unknown'))), file=sys.stderr)
    sys.exit(exit_status)
