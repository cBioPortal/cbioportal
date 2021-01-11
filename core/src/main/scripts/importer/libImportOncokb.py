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

"""OncoKB annotation import script.

Run with the command line option --help for usage information.
"""

import re
import os
import requests
import json
import argparse

BATCH_SIZE = 200
DEFAULT_ONCOKB_URL = "https://public.api.oncokb.org/api/v1"
ONCOKB_JSON_QUERY_FIELD = 'query'
ONCOKB_JSON_ID_FIELD = 'id'
ONCOKB_JSON_ONCOGENIC_FIELD = 'oncogenic'

def load_portal_genes(server_url):
    parsed_json = request_from_portal_api(server_url, "genes", logging.getLogger(__name__))
    return transform_symbol_entrez_map(parsed_json, 'hugoGeneSymbol')


# after: getProteinStart() in ExtendedMutationUtil.java
def get_protein_pos_start(protein_position, protein_change):
    start = -1
    if protein_position is not None:
        start_end = protein_position.split('/')[0].split('-')
        if len(start_end) > 0:
            start = start_end[0]
    if start == -1 and protein_change is not None:
        start_stop = annotate_protein_change(protein_change)
        start = str(start_stop['start'])
    return start


# after: getProteinPosEnd() in ExtendedMutationUtil.java
def get_protein_pos_end(protein_position, protein_change):
    end = -1
    if protein_position is not None:
        start_end = protein_position.split('/')[0].split('-')
        if len(start_end) > 1:
            end = start_end[1]
    if end == -1 and protein_change is not None:
        start_stop = annotate_protein_change(protein_change)
        end = str(start_stop['stop'])
    return end


# after: annotateProteinChange() in ExtendedMutationUtil.java
def annotate_protein_change(protein_change):
    start = -1
    end = -1
    match = re.findall(r'^([A-Z\*]+)([0-9]+)([A-Z\*\?]*)$', protein_change)
    if len(match) > 0:
        ref = match[0][0]
        var = match[0][2]
        refL = len(ref)
        start = int(match[0][1])
        if ref == var or ref == '*' or var == '*' or start == 1 or var == '?':
            end = start
        else:
            end = start + refL - 1
    else:
        match = re.findall(r'[A-Z]?([0-9]+)(_[A-Z]?([0-9]+))?(delins|ins)([A-Z]+)', protein_change)
        if len(match) > 0:
            start = int(match[0][0])
            if match[0][2] is not None and match[0][2] != '':
                end = int(match[0][2])
            else:
                end = start
        else:
            match = re.findall(r'[A-Z]?([0-9]+)(_[A-Z]?([0-9]+))?(_)?splice', protein_change)
            if len(match) > 0:
                start = int(match[0][0])
                if match[0][2] is not None and match[0][2] != '':
                    end = int(match[0][2])
                else:
                    end = start
            else:
                match = re.findall(r'[A-Z]?([0-9]+)_[A-Z]?([0-9]+)(.+)', protein_change)
                if len(match) > 0:
                    start = int(match[0][0])
                    end = int(match[0][1])
                else:
                    match = re.findall(r'([A-Z\*])([0-9]+)[A-Z]?fs.*', protein_change)
                    if len(match) > 0:
                        start = int(match[0][1])
                        end = start
                    else:
                        match = re.findall(r'([A-Z]+)?([0-9]+)((ins)|(del)|(dup))', protein_change)
                        if len(match) > 0:
                            start = int(match[0][1])
                            end = start
    return {"start": start, "stop": end}


def evaluate_driver_passenger(oncogenic):
    """Translate the OncoKB 'oncogenic' value to Putative_Driver' or Putative_Passenger'"""
    custom_annotation = 'Putative_Passenger'
    if oncogenic.lower() in ['oncogenic', 'likely oncogenic', 'predicted oncogenic']:
        custom_annotation = 'Putative_Driver'
    return custom_annotation


def get_first_line(file_handle):
    """Read the first line of a file ignoring comment lines."""
    while True:
        first_line = file_handle.readline()
        if not first_line.startswith('#'):
            file_handle.close()
            return first_line.rstrip('\n')
        
def get_first_line_cells(file_handle, separator = '\t'):
    return get_first_line(file_handle).split(separator);

def find_meta_file_by_fields(study_dir, field_dict):
    """In the study directory find the meta file that holds specified field values."""
    filenames = os.listdir(study_dir)
    meta_files = [ file for file in filenames if file.startswith('meta_')]
    meta_files = [ study_dir + "/" + file for file in meta_files ]
    for meta_file in meta_files:
        fields = read_meta_file(meta_file)
        match_found = True
        for field_name, field_value in field_dict.items():
            if field_name not in fields or fields[field_name] != field_value:
                match_found = False
        if match_found:
            return os.path.join(study_dir, meta_file)
    return None


def find_data_file_from_meta_file(metafile_path):
    """From specified meta file return the value of the 'data_filename' field."""
    fields = read_meta_file(metafile_path)
    return fields['data_filename']


def read_meta_file(metafile_path):
    """Read fields of specified meta into a dict."""
    fields = {}
    with open(metafile_path) as metafile:
        for line in metafile:
            if line == '\n':
                continue
            match = re.findall(r'\s*(\S+)\s*:\s*(\S+)', line)[0]
            fields[match[0]] = match[1]
    return fields

def get_oncokb_curated_genes():
    """Do a call to OncoKB to retrieve the Curated Gene List. """
    request_url = DEFAULT_ONCOKB_URL + "/utils/allCuratedGenes"
    request = requests.get(url=request_url)

    if request.ok:
        return request.json()
    else:
        if request.status_code == 404:
            raise ConnectionError(
                "An error occurred when trying to connect to OncoKB for retrieving of mutation annotations.")
        else:
            request.raise_for_status()

def get_annotated_oncokb_curated_genes_by_entrezId():
    """ Get a list with the Entrez Gene IDs of the OncoKB Curated Gene List, only the genes that have
        annotations. """
    curated_genes = get_oncokb_curated_genes()
    entrez_ids = []
    for gene in curated_genes:
        entrez_ids += [gene["entrezGeneId"]]
    return entrez_ids

def filter_payload(payload_list, sv):
    """ Remove the genes that are not an OncoKB Curated Gene List from the payload. """
    curated_genes = get_annotated_oncokb_curated_genes_by_entrezId()
    filtered_payload = []
    for element in payload_list:
        parsed_element = json.loads(element)
        if sv:
            if parsed_element["geneA"]["entrezGeneId"] in curated_genes and parsed_element["geneB"]["entrezGeneId"] in curated_genes:
                filtered_payload += [element]
        else:
            if parsed_element["gene"]["entrezGeneId"] in curated_genes:
                filtered_payload += [element]
    return filtered_payload

def fetch_oncokb_annotations(payload_list, request_url, sv=False):
    """Submit alterations to OncoKB.org and return OncoKB annotations."""
    annotations = []
    request_headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}
    filtered_payload = filter_payload(payload_list, sv)
    payload_batches = partition_list(filtered_payload, BATCH_SIZE)
    for payload_batch in payload_batches:
        payload = '['+ ', '.join(payload_batch) + ']'
        print("Fetching batch of " + str(len(payload_batch)) + " annotations ...", end = '')
        request = requests.post(url=request_url, headers=request_headers, data=payload)
        if request.ok:
            print(" DONE")
            # Parse transcripts and exons from JSON
            result_json = request.json()
            annotations = annotations + result_json
        else:
            if request.status_code == 404:
                print(
                    Color.RED + 'An error occurred when trying to connect to OncoKB for retrieving of mutation annotations' + Color.END,
                    file=sys.stderr)
                sys.exit(1)
            else:
                request.raise_for_status()
    return annotations

def partition_list(list, n):
    """Yield successive n-sized chunks from list."""
    for i in range(0, len(list), n):
        yield list[i:i + n]


def check_required_columns(header_elements, required_columns):
    missing_columns = []
    for required_column in required_columns:
        if not required_column in header_elements:
            missing_columns.append(required_column)
    if len(missing_columns) > 0:
        raise RuntimeError("One or more required columns for OncoKB import are missing in the input file. " \
                           "Missing column(s): [" + ", ".join(missing_columns) + "]")

def open_file(file_name):
    """Open file and handle exception when not found."""
    try:
        file = open(file_name)
    except FileNotFoundError:
        raise FilenotFoundError("Could not open file at path '" + file_name + "'")
    return file


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

def get_annotation_cells(oncokb_annotation):
    oncogenicity = evaluate_driver_passenger(oncokb_annotation)
    driver_field_annotation = ''
    tier_field_annotation = ''
    if oncogenicity != '':
        driver_field_annotation = 'Annotation imported from OncoKB.org'
    if oncokb_annotation != '':
        tier_field_annotation = 'Annotation imported from OncoKB.org'
    return '\t%s\t%s\t%s\t%s\n' % (oncogenicity, driver_field_annotation, oncokb_annotation, tier_field_annotation)
