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

"""OncoKB annotation import script for MAF files.
"""

import argparse
import importlib
import logging.handlers
import os
import requests
import sys
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

required_mutation_columns = ['Hugo_Symbol', 'HGVSp_Short', 'Variant_Classification', 'Protein_position']
disallowed_mutation_columns = ['cbp_driver', 'cbp_driver_annotation', 'cbp_driver_tiers', 'cbp_driver_tiers_annotation']
portal_instance = None


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

    meta_file_mutation = libImportOncokb.find_meta_file_by_fields(study_dir, {'genetic_alteration_type': 'MUTATION_EXTENDED'})
    mutation_file_path = os.path.join(study_dir, libImportOncokb.find_data_file_from_meta_file(meta_file_mutation))
    check_required_columns(libImportOncokb.get_first_line(open_mutations_file(mutation_file_path)).rstrip('\n').split('\t'))
    check_disallowed_columns(libImportOncokb.get_first_line(open_mutations_file(mutation_file_path)).rstrip('\n').split('\t'))

    global portal_instance
    if hasattr(args, 'portal_info_dir') and args.portal_info_dir is not None:
        portal_instance = validateData.load_portal_info(args.portal_info_dir, logger,
                                           offline=True)
    else:
        portal_instance = validateData.load_portal_info(server_url, logger)

    row_number_to_feature = get_features(mutation_file_path)
    row_number_to_annotation = fetch_oncokb_annotations(row_number_to_feature)
    write_annotations_to_file(row_number_to_annotation, mutation_file_path)

    logger.info('Import complete')

    return exit_status_handler.get_exit_status()


def open_mutations_file(file_name):
    """Open MAF file and handle exception when not found."""
    try:
        file = open(file_name)
    except FileNotFoundError:
        raise FilenotFoundError("Could not open MAF file at path '" + file_name + "'")
    return file


def check_disallowed_columns(header_elements):
    disallowed_columns = []
    for disallowed_column in disallowed_mutation_columns:
        if disallowed_column in header_elements:
            disallowed_columns.append(disallowed_column)
    if len(disallowed_columns) > 0:
        raise RuntimeError("One or more disallowed columns for OncoKb import are present in the MAF file. " \
                           "Disallowed column(s): [" + ", ".join(disallowed_columns) + "]")


def get_features(mutation_file_path):
    """Extract Mutation events from MAF data file."""
    header_elements = libImportOncokb.get_first_line(open_mutations_file(mutation_file_path)).rstrip().split('\t')
    header_indexes = {}
    for required_column in required_mutation_columns + ['Entrez_Gene_Id']:
        header_indexes[required_column] = header_elements.index(required_column)
    row_number_to_feature = {}
    row_counter = 0
    mutation_file = open_mutations_file(mutation_file_path)
    for line in mutation_file:
        row_counter += 1
        if line == '\n' or line.startswith('#') or line.startswith(header_elements[0]):
            continue  # skip comment and header line
        line_elements = line.rstrip().split('\t')
        feature = {}
        for column_name, index in header_indexes.items():
            value = line_elements[index]
            if value != '':
                if column_name == 'HGVSp_Short':
                    value = value.replace('p.', '')
                feature[column_name] = value
            elif column_name != 'Entrez_Gene_Id' and column_name != 'Protein_position':
                raise RuntimeError("Empty value encounterd in column '" +
                                   column_name + "' in row " + str(row_counter) + "." \
                                                                                  "OncoKb annotations cannot be imported. Please fix and rerun.")

        # resolve gene symbols to Entrez Ids if needed
        if 'Entrez_Gene_Id' in feature and feature['Entrez_Gene_Id'] is not None and feature['Entrez_Gene_Id'] != '':
            entrez_gene_ids = [feature['Entrez_Gene_Id']]
        else:
            entrez_gene_ids = portal_instance.hugo_entrez_map[feature['Hugo_Symbol']]

        if len(entrez_gene_ids) > 1:
            logger.error("Multiple Entrez gene ids were found for a gene." \
                         "OncoKb annotations will not be imported for this gene." \
                         "Please fix and rerun.",
                         extra={'symbol': feature['Hugo_Symbol'], 'row': str(row_counter)})
            feature['Entrez_Gene_Id'] = None
        elif len(entrez_gene_ids) == 0:
            logger.error("Could not find the Entrez gene id for a gene." \
                         "OncoKb annotations will not be imported for this gene." \
                         "Please fix and rerun.",
                         extra={'symbol': feature['Hugo_Symbol'], 'row': str(row_counter)})
            feature['Entrez_Gene_Id'] = None
        else:
            feature['Entrez_Gene_Id'] = str(entrez_gene_ids[0])
            feature['id'] = "_".join(
                [feature['Entrez_Gene_Id'], feature['HGVSp_Short'], feature['Variant_Classification']])

        row_number_to_feature[row_counter] = feature
    mutation_file.close()
    return row_number_to_feature


def fetch_oncokb_annotations(row_number_to_feature):
    """Submit mutation events to OncoKB.org and return OncoKB annotations."""
    id_to_rownumber = {}
    for row_number, feature in row_number_to_feature.items():
        id_to_rownumber[feature['id']] = row_number

    request_url = "https://demo.oncokb.org/api/v1/annotate/mutations/byProteinChange"
    request_headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}
    request_payload = create_request_payload(row_number_to_feature)
    request = requests.post(url=request_url, headers=request_headers, data=request_payload)
    row_number_to_annotation = {}
    if request.ok:
        result_json = request.json()
        id_to_annotation = {annotation['query']['id']: annotation for annotation in result_json}
        for row_number, feature in row_number_to_feature.items():
            row_number_to_annotation[row_number] = id_to_annotation[feature['id']]
    else:
        if request.status_code == 404:
            raise ConnectionError(
                "An error occurred when trying to connect to OncoKB for retrieving of mutation annotations.")
        else:
            request.raise_for_status()

    return row_number_to_annotation


def create_request_payload(row_number_to_feature):
    """Translate mutation events into JSON for message body."""    
    elements = {}
    for row_number, feature in row_number_to_feature.items():
        protein_position = feature['Protein_position'] if 'Protein_position' in feature and feature[
            'Protein_position'] != 'NA' else None
        protein_change = feature['HGVSp_Short'] if 'HGVSp_Short' in feature and feature['HGVSp_Short'] != 'NA' else None
        proteinStart = libImportOncokb.get_protein_pos_start(protein_position, protein_change)
        proteinEnd = libImportOncokb.get_protein_pos_end(protein_position, protein_change)
        if proteinEnd == -1:
            proteinEnd = proteinStart
        if proteinStart != -1:
            elements[feature[
                'id']] = '{ "alteration":"%s", "consequence":"%s", "gene":{"entrezGeneId":%s}, "id":"%s", "proteinStart":%s, "proteinEnd":%s, "tumorType":null} ' \
                         % (feature['HGVSp_Short'], feature['Variant_Classification'], feature['Entrez_Gene_Id'],
                            feature['id'], proteinStart, proteinEnd)
        else:
            elements[feature[
                'id']] = '{ "alteration":"%s", "consequence":"%s", "gene":{"entrezGeneId":%s}, "id":"%s", "tumorType":null} ' \
                         % (feature['HGVSp_Short'], feature['Variant_Classification'], feature['Entrez_Gene_Id'],
                            feature['id'])

    # normalize for alteration id since same alteration is represented in multiple samples
    payload = '[' + ', '.join(elements.values()) + ']'
    return payload


def write_annotations_to_file(row_number_to_annotation, mutations_file_path):
    """Add pd annotation columns and data fields to MAF file."""
    meta_cna_file_name = os.path.basename(mutations_file_path)
    dir = os.path.dirname(mutations_file_path)
    backup_file_name = 'ONCOKB_IMPORT_BACKUP_' + meta_cna_file_name
    backup_file_path = os.path.join(dir, backup_file_name)
    try:
        new_file = open(mutations_file_path + '_temp', "x")
        header_updated = False
        row_counter = 0
        mutations_file = open_mutations_file(mutations_file_path)
        for line in mutations_file:
            row_counter += 1
            if not line.startswith('#'):
                if not header_updated:
                    line = line.rstrip(
                        '\n') + '\tcbp_driver\tcbp_driver_annotation' + '\n'  # add custom driver columns to header
                    header_updated = True
                else:
                    if row_counter in row_number_to_annotation:
                        oncokb_annotation = row_number_to_annotation[row_counter]
                        line = line.rstrip('\n') + '\t' + libImportOncokb.evaluate_driver_passenger(
                            oncokb_annotation['oncogenic']) + '\t' + oncokb_annotation['oncogenic'] + '\n'
                    else:
                        line = line.rstrip('\n') + '\t\t\n'
            new_file.write(line)
    except FileExistsError:
        raise FileExistsError("Backup MAF file that does not contain OncoKB annotations does already exist. " \
                              "Please remove file '" + backup_file_name + "' and try again.")
    finally:
        mutations_file.close()
        new_file.close()
    os.rename(mutations_file_path, backup_file_path)
    os.rename(mutations_file_path + '_temp', mutations_file_path)
    return


def check_required_columns(header_elements):
    missing_columns = []
    for required_column in required_mutation_columns:
        if not required_column in header_elements:
            missing_columns.append(required_column)
    if len(missing_columns) > 0:
        raise RuntimeError("One or more required columns for OncoKb import are missing from the MAF file. " \
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
    print(('Import of OncoKB annotations for mutations {status}.'.format(
        status={0: 'succeeded',
                1: 'failed',
                2: 'not performed as problems occurred',
                3: 'succeeded with warnings'}.get(exit_status, 'unknown'))), file=sys.stderr)
    sys.exit(exit_status)
