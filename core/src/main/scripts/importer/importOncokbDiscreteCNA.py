#!/usr/bin/env python3

#
# Copyright (c) 2020 The Hyve B.V.
# This code is licensed under the GNU Affero General Public License (AGPL),
# version 3, or (at your option) any later version.
#

#
# This file is part of cBioPortal.
#
# cBioPortal is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

"""OncoKB annotation import script for discrete CNA files.
"""

import importlib
import logging.handlers
import os
import sys
from os import path
from pathlib import Path

""" Configure relative imports if running as a script; see PEP 366
    It might passed as empty string by certain tooling to mark a top level module. """
if __name__ == "__main__" and (__package__ is None or __package__ == ''):
    """ Replace the script's location in the Python search path by the main
        scripts/ folder, above it, so that the importer package folder is in
        scope and *not* directly in sys.path; see PEP 395. """
    sys.path[0] = str(Path(sys.path[0]).resolve().parent)
    __package__ = 'importer'
    """ Explicitly import the package, which is needed on CPython 3.4 because it
        doesn't include https://github.com/python/cpython/pull/2639. """
    importlib.import_module(__package__)

from . import cbioportal_common
from . import libImportOncokb
from . import validateData

ENTREZ_ID = 'Entrez_Gene_Id'
HUGO_SYMBOL = 'Hugo_Symbol'
CNA_TYPE = 'copyNameAlterationType'
ALTERATION = 'alteration'
SAMPLE_ID = 'sample_id'
INTERNAL_ID = 'id'
ONCOGENIC = libImportOncokb.ONCOKB_JSON_ONCOGENIC_FIELD

required_cna_columns = [HUGO_SYMBOL]

# from: cbioportal-frontend file CopyNumberUtils.ts
cna_alteration_types = {
    "DELETION": -2,
    "LOSS": -1,
    "GAIN": 1,
    "AMPLIFICATION": 2,
}


def main_import(args):

    study_dir = args.study_directory
    server_url = args.url_server

    # get a logger to emit messages
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.INFO)
    exit_status_handler = validateData.MaxLevelTrackingHandler()
    logger.addHandler(exit_status_handler)

    # set default message handler
    text_handler = logging.StreamHandler(sys.stdout)
    text_handler.setFormatter(
        cbioportal_common.LogfileStyleFormatter(study_dir))
    collapsing_text_handler = cbioportal_common.CollapsingLogMessageHandler(
        capacity=5e5,
        flushLevel=logging.CRITICAL,
        target=text_handler)
    collapsing_text_handler.setLevel(logging.INFO)
    logger.addHandler(collapsing_text_handler)

    meta_cna_file_path = libImportOncokb.find_meta_file_by_fields(study_dir, {'genetic_alteration_type': 'COPY_NUMBER_ALTERATION', 'datatype': 'DISCRETE'})
    cna_file_path = os.path.join(study_dir, libImportOncokb.find_data_file_from_meta_file(meta_cna_file_path))
    pd_file_name = 'data_cna_pd_annotations.txt'
    pd_file_path = os.path.join(study_dir, pd_file_name)
    meta_dict = libImportOncokb.read_meta_file(meta_cna_file_path)
    if 'pd_annotations_filename' in meta_dict:
        raise RuntimeError( 
            "Custom driver annotations filename already specified in discrete CNA meta file. Please remove and rerun.")
    if path.exists(pd_file_path):
        raise RuntimeError(
            "Custom driver annotations file '" + pd_file_path + "' for discrete CNA already exists . Please remove and rerun.")
    libImportOncokb.check_required_columns(libImportOncokb.get_first_line_cells(libImportOncokb.open_file(cna_file_path), '\t'), required_cna_columns)

    global portal_instance
    if hasattr(args, 'portal_info_dir') and args.portal_info_dir is not None:
        portal_instance = validateData.load_portal_info(args.portal_info_dir, logger,
                                           offline=True)
    else:
        portal_instance = validateData.load_portal_info(server_url, logger)

    cna_events = get_cna_events(cna_file_path)
    id_to_annotation = fetch_and_map_oncokb_annotations(cna_events)
    for cna_event in cna_events:
        if cna_event[INTERNAL_ID] in id_to_annotation:
            cna_event[ONCOGENIC] = id_to_annotation[cna_event[INTERNAL_ID]][ONCOGENIC]

    print("Updating study files ...", end = '')
    write_annotations_to_file(cna_events, pd_file_path)
    update_cna_metafile(meta_cna_file_path, pd_file_name)
    print(" DONE")

    logger.info('Import complete')

    return exit_status_handler.get_exit_status()

def get_cna_events(cna_file_path):
    """Extract CNA events from CNA data file."""
    header_elements = libImportOncokb.get_first_line_cells(libImportOncokb.open_file(cna_file_path), '\t')
    header_indexes = {}
    for required_column in required_cna_columns + [ENTREZ_ID]:
        header_indexes[required_column] = header_elements.index(required_column)
    sample_ids = [i for j, i in enumerate(header_elements) if j not in header_indexes.values()]
    sample_indexes = {}
    for sample_id in sample_ids:
        sample_indexes[sample_id] = header_elements.index(sample_id)

    features = []
    cna_file = libImportOncokb.open_file(cna_file_path)
    print("Reading features from file ...", end = '')
    for line in cna_file:
        if line == '\n' or line.startswith('#') or line.startswith(header_elements[0]):
            continue  # skip comment and header line
        line_elements = line.rstrip('\n').split('\t')
        for sample_id in sample_ids:
            feature = {}
            feature[SAMPLE_ID] = sample_id
            feature[ALTERATION] = int(line_elements[sample_indexes[sample_id]])
            # CNA value 0 (no CNA) is skipped
            if not feature[ALTERATION] in cna_alteration_types.values():
                continue
            feature[CNA_TYPE] = list(cna_alteration_types.keys())[
                list(cna_alteration_types.values()).index(feature[ALTERATION])]
            for column_name, index in header_indexes.items():
                value = line_elements[index]
                if value != '':
                    feature[column_name] = value
                elif column_name != ENTREZ_ID:
                    print(Color.RED + "Empty value encounterd in column '" + column_name + "' in row " + str(
                        row_counter) + ". OncoKB annotations cannot be imported. Please fix and rerun." + Color.END,
                          file=sys.stderr)
                    print("!" * 71, file=sys.stderr)
                    sys.exit(1)

            # resolve gene symbols to Entrez Ids if needed
            if ENTREZ_ID in feature and feature[ENTREZ_ID] is not None and feature[
                ENTREZ_ID] != '':
                entrez_gene_ids = [feature[ENTREZ_ID]]
            elif feature[HUGO_SYMBOL] in portal_instance.hugo_entrez_map:
                entrez_gene_ids = portal_instance.hugo_entrez_map[feature[HUGO_SYMBOL]]

            if len(entrez_gene_ids) > 1:
                logger.error(""" Multiple Entrez gene ids were found for a gene.
                                 OncoKB annotations will not be imported for this gene.
                                 Please fix and rerun. """,
                             extra={'symbol': feature[HUGO_SYMBOL]})
                feature[ENTREZ_ID] = None
            elif len(entrez_gene_ids) == 0:
                logger.error(""" Could not find the Entrez gene id for a gene.
                                 OncoKB annotations will not be imported for this gene.
                                 Please fix and rerun. """,
                             extra={'symbol': feature[HUGO_SYMBOL]})
                feature[ENTREZ_ID] = None
            else:
                feature[ENTREZ_ID] = str(entrez_gene_ids[0])
                feature[INTERNAL_ID] = "_".join([feature[ENTREZ_ID], feature[CNA_TYPE]])

            features.append(feature)
    cna_file.close()
    # FIXME this should not occur, right?
    # Remove duplicate entrez_gene_id/sample_id occurrences.
    non_redundant_features_dict = {x[ENTREZ_ID]+x[SAMPLE_ID]:x for x in features}
    print(" DONE")
    return non_redundant_features_dict.values();


def fetch_and_map_oncokb_annotations(features):
    """Submit CNA events to OncoKB.org and return OncoKB annotations."""
    id_to_annotation = {}
    payload_list = create_request_payload(features)
    annotations = libImportOncokb.fetch_oncokb_annotations(payload_list, libImportOncokb.DEFAULT_ONCOKB_URL + "/annotate/copyNumberAlterations")
    for annotation in annotations:
        id = annotation[libImportOncokb.ONCOKB_JSON_QUERY_FIELD][libImportOncokb.ONCOKB_JSON_ID_FIELD]
        id_to_annotation[id] = annotation
    return id_to_annotation


def create_request_payload(features):
    """Translate CNA events into JSON for message body."""
    elements = {}
    for feature in features:
        elements[feature[INTERNAL_ID]] = '{ "copyNameAlterationType":"%s", "gene":{"entrezGeneId":%s}, "id":"%s", "tumorType":null} ' \
                     % (feature[CNA_TYPE], feature[ENTREZ_ID], feature[INTERNAL_ID])
    # normalize for alteration id since same alteration is represented in multiple samples
    return list(elements.values())


def update_cna_metafile(meta_cna_file_path, pd_file_name):
    """Add reference to pd annotation file to CNA meta file."""
    meta_file = open(meta_cna_file_path, "r")
    lines = meta_file.readlines()
    meta_file.close()
    meta_file_name = os.path.basename(meta_cna_file_path)
    dir = os.path.dirname(meta_cna_file_path)
    os.rename(meta_cna_file_path, os.path.join(dir, 'ONCOKB_IMPORT_BACKUP_' + meta_file_name))
    if lines[-1] == '\n':
        lines = lines[:-1]
    meta_file = open(meta_cna_file_path, "w")
    for line in lines:
        meta_file.write(line)
    meta_file.write('pd_annotations_filename: ' + pd_file_name + '\n')
    meta_file.close()


def write_annotations_to_file(features, pd_file_name):
    """Write CNA pd annotations to data file."""
    new_file = open(pd_file_name, "w")
    new_file.write("SAMPLE_ID\tEntrez_Gene_Id\tcbp_driver\tcbp_driver_annotation\tcbp_driver_tiers\tcbp_driver_tiers_annotation\n")
    for feature in features:
        if ONCOGENIC in feature:
            oncokb_annotation = feature[ONCOGENIC]
            line = feature[SAMPLE_ID] + '\t' + feature[ENTREZ_ID] + libImportOncokb.get_annotation_cells(oncokb_annotation)
            new_file.write(line)
    new_file.close()


if __name__ == '__main__':
    try:
        parsed_args = libImportOncokb.interface()
        exit_status = main_import(parsed_args)
    finally:
        logging.shutdown()
        del logging._handlerList[:]  # workaround for harmless exceptions on exit
    print(('Import of OncoKB annotations for discrete CNA {status}.'.format(
        status={0: 'succeeded',
                1: 'failed',
                2: 'not performed as problems occurred',
                3: 'succeeded with warnings'}.get(exit_status, 'unknown'))), file=sys.stderr)
    sys.exit(exit_status)
