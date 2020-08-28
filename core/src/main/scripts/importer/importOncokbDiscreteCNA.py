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

import argparse
import importlib
import logging.handlers
import os
import requests
import sys
from os import path
from pathlib import Path

# configure relative imports if running as a script; see PEP 366
# it might passed as empty string by certain tooling to mark a top level module
if __name__ == "__main__" and (__package__ is None or __package__ == ''):
    # replace the script's location in the Python search path by the main
    # scripts/ folder, above it, so that the importer package folder is in
    # scope and *not* directly in sys.path; see PEP 395
    sys.path[0] = str(Path(sys.path[0]).resolve().parent)
    __package__ = 'importer'
    # explicitly import the package, which is needed on CPython 3.4 because it
    # doesn't include https://github.com/python/cpython/pull/2639
    importlib.import_module(__package__)

from . import cbioportal_common
from . import libImportOncokb
from . import validateData

required_cna_columns = ['Hugo_Symbol']


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
    check_required_columns(libImportOncokb.get_first_line(open_cna_file(cna_file_path)).rstrip('\n').split('\t'))

    global portal_instance
    if hasattr(args, 'portal_info_dir') and args.portal_info_dir is not None:
        portal_instance = validateData.load_portal_info(args.portal_info_dir, logger,
                                           offline=True)
    else:
        portal_instance = validateData.load_portal_info(server_url, logger)

    features = get_features(cna_file_path)
    id_to_annotation = fetch_oncokb_annotations(features)
    for feature in features:
        feature['oncogenic'] = id_to_annotation[feature['id']]['oncogenic']

    print("Updating study files ...", end = '')
    write_annotations_to_file(features, pd_file_path)
    update_cna_metafile(meta_cna_file_path, pd_file_name)
    print(" DONE")

    logger.info('Import complete')

    return exit_status_handler.get_exit_status()


def open_cna_file(file_name):
    """Open CNA file and handle exception when not found."""
    try:
        file = open(file_name)
    except FileNotFoundError:
        raise FilenotFoundError("Could not open discrete CNA file at path '" + file_name + "'")
    return file


def get_features(cna_file_path):
    """Extract CNA events from CNA data file."""
    header_elements = libImportOncokb.get_first_line(open_cna_file(cna_file_path)).rstrip('\n').split('\t')
    header_indexes = {}
    for required_column in required_cna_columns + ['Entrez_Gene_Id']:
        header_indexes[required_column] = header_elements.index(required_column)
    sample_ids = [i for j, i in enumerate(header_elements) if j not in header_indexes.values()]
    sample_indexes = {}
    for sample_id in sample_ids:
        sample_indexes[sample_id] = header_elements.index(sample_id)

    features = []
    cna_file = open_cna_file(cna_file_path)
    print("Reading features from file ...", end = '')
    for line in cna_file:
        if line == '\n' or line.startswith('#') or line.startswith(header_elements[0]):
            continue  # skip comment and header line
        line_elements = line.rstrip('\n').split('\t')
        for sample_id in sample_ids:
            feature = {}
            feature['sample_id'] = sample_id
            feature['alteration'] = int(line_elements[sample_indexes[sample_id]])
            # cna value 0 (no CNA) is skipped
            if feature['alteration'] == 0:
                continue
            feature['copyNameAlterationType'] = list(cna_alteration_types.keys())[
                list(cna_alteration_types.values()).index(feature['alteration'])]
            for column_name, index in header_indexes.items():
                value = line_elements[index]
                if value != '':
                    feature[column_name] = value
                elif column_name != 'Entrez_Gene_Id':
                    print(Color.RED + "Empty value encounterd in column '" + column_name + "' in row " + str(
                        row_counter) + ". OncoKb annotations cannot be imported. Please fix and rerun." + Color.END,
                          file=sys.stderr)
                    print("!" * 71, file=sys.stderr)
                    sys.exit(1)

            # resolve gene symbols to Entrez Ids if needed
            if 'Entrez_Gene_Id' in feature and feature['Entrez_Gene_Id'] is not None and feature[
                'Entrez_Gene_Id'] != '':
                entrez_gene_ids = [feature['Entrez_Gene_Id']]
            else:
                entrez_gene_ids = portal_instance.hugo_entrez_map[feature['Hugo_Symbol']]

            if len(entrez_gene_ids) > 1:
                logger.error("Multiple Entrez gene ids were found for a gene." \
                             "OncoKb annotations will not be imported for this gene." \
                             "Please fix and rerun.",
                             extra={'symbol': feature['Hugo_Symbol']})
                feature['Entrez_Gene_Id'] = None
            elif len(entrez_gene_ids) == 0:
                logger.error("Could not find the Entrez gene id for a gene." \
                             "OncoKb annotations will not be imported for this gene." \
                             "Please fix and rerun.",
                             extra={'symbol': feature['Hugo_Symbol']})
                feature['Entrez_Gene_Id'] = None
            else:
                feature['Entrez_Gene_Id'] = str(entrez_gene_ids[0])
                feature['id'] = "_".join([feature['Entrez_Gene_Id'], feature['copyNameAlterationType']])

            features.append(feature)
    cna_file.close()
    print(" DONE")
    return features


def fetch_oncokb_annotations(features):
    """Submit CNA events to OncoKB.org and return OncoKB annotations."""
    id_to_annotation = {}
    payload_list = create_request_payload(features)
    annotations = libImportOncokb.fetch_oncokb_annotations(payload_list, "https://demo.oncokb.org/api/v1/annotate/copyNumberAlterations")
    for annotation in annotations:
        id = annotation['query']['id']
        id_to_annotation[id] = annotation
    return id_to_annotation


def create_request_payload(features):
    """Translate CNA events into JSON for message body."""
    elements = {}
    for feature in features:
        elements[feature[
            'id']] = '{ "copyNameAlterationType":"%s", "gene":{"entrezGeneId":%s}, "id":"%s", "tumorType":null} ' \
                     % (feature['copyNameAlterationType'], feature['Entrez_Gene_Id'], feature['id'])
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
        line = "\t".join(
            [feature['sample_id'], feature['Entrez_Gene_Id'], libImportOncokb.evaluate_driver_passenger(feature['oncogenic']),
             feature['oncogenic'], '', '']) + "\n"
        new_file.write(line)
    new_file.close()


def check_required_columns(header_elements):
    missing_columns = []
    for required_column in required_cna_columns:
        if not required_column in header_elements:
            missing_columns.append(required_column)
    if len(missing_columns) > 0:
        raise RuntimeError("One or more required columns for OncoKb import are missing from the discrete CNA file. " \
                           "Missing column(s): [" + ", ".join(missing_columns) + "]")


def interface():
    parser = argparse.ArgumentParser(description='cBioPortal OncoKB annotation importer')
    parser.add_argument('-u', '--url_server',
                        type=str,
                        default='http://localhost:8080',
                        help='URL to cBioPortal server. You can '
                             'set this if your URL is not '
                             'http://localhost:8080')
    parser.add_argument('-m', '--study_directory', type=str, required=True,
                        help='path to study directory.')
    parser = parser.parse_args()
    return parser


if __name__ == '__main__':
    try:
        parsed_args = interface()
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
