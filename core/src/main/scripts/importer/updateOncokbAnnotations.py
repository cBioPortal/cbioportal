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

"""Script to update OncoKB annotation for the provided study.
"""

import argparse
import importlib
import logging.handlers
import os
import requests
import sys
import MySQLdb
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

ERROR_FILE = sys.stderr
DATABASE_HOST = 'db.host'
DATABASE_NAME = 'db.portal_db_name'
DATABASE_USER = 'db.user'
DATABASE_PW = 'db.password'
REQUIRED_PROPERTIES = [DATABASE_HOST, DATABASE_NAME, DATABASE_USER, DATABASE_PW]
REFERENCE_GENOME = {'hg19': 'GRCh37', 'hg38': 'GRCh38'}

# from: cbioportal-frontend file CopyNumberUtils.ts
cna_alteration_types = {
    "DELETION": -2,
    "LOSS": -1,
    "GAIN": 1,
    "AMPLIFICATION": 2,
}

class PortalProperties(object):
    """ Properties object class, just has fields for db conn """

    def __init__(self, database_host, database_name, database_user, database_pw):
        # default port:
        self.database_port = 3306
        # if there is a port added to the host name, split and use this one:
        if ':' in database_host:
            host_and_port = database_host.split(':')
            self.database_host = host_and_port[0]
            if self.database_host.strip() == 'localhost':
                print(
                    "Invalid host config '" + database_host + "' in properties file. If you want to specify a port on local host use '127.0.0.1' instead of 'localhost'",
                    file=ERROR_FILE)
                sys.exit(1)
            self.database_port = int(host_and_port[1])
        else:
            self.database_host = database_host
        self.database_name = database_name
        self.database_user = database_user
        self.database_pw = database_pw

def get_portal_properties(properties_filename):
    """ Returns a properties object """
    properties = {}
    with open(properties_filename, 'r') as properties_file:
        for line in properties_file:
            line = line.strip()
            # skip line if its blank or a comment
            if len(line) == 0 or line.startswith('#'):
                continue
            try:
                name, value = line.split('=', maxsplit=1)
            except ValueError:
                print(
                    'Skipping invalid entry in property file: %s' % (line),
                    file=ERROR_FILE)
                continue
            properties[name] = value.strip()
    missing_properties = []
    for required_property in REQUIRED_PROPERTIES:
        if required_property not in properties or len(properties[required_property]) == 0:
            missing_properties.append(required_property)
    if missing_properties:
        print(
            'Missing required properties : (%s)' % (', '.join(missing_properties)),
            file=ERROR_FILE)
        return None
    # return an instance of PortalProperties
    return PortalProperties(properties[DATABASE_HOST],
                            properties[DATABASE_NAME],
                            properties[DATABASE_USER],
                            properties[DATABASE_PW])

def get_db_cursor(portal_properties):
    """ Establishes a MySQL connection """
    try:
        connection = MySQLdb.connect(host=portal_properties.database_host,
            port = portal_properties.database_port,
            user = portal_properties.database_user,
            passwd = portal_properties.database_pw,
            db = portal_properties.database_name)
    except MySQLdb.Error as exception:
        print(exception, file=ERROR_FILE)
        port_info = ''
        if portal_properties.database_host.strip() != 'localhost':
            # only add port info if host is != localhost (since with localhost apparently sockets are used and not the given port) TODO - perhaps this applies for all names vs ips?
            port_info = " on port " + str(portal_properties.database_port)
        message = (
            "--> Error connecting to server "
            + portal_properties.database_host
            + port_info)
        print(message, file=ERROR_FILE)
        raise ConnectionError(message) from exception
    if connection is not None:
        return connection, connection.cursor()

def get_current_mutation_data(study_id, cursor, cancer_genes):
    """ Get mutation data from the current study.
        Returns an array of dictionaries, with the following keys:
        id, geneticProfileId, entrezGeneId, alteration, and consequence
    """
    mutations = []
    try:
        cursor.execute('SELECT genetic_profile.GENETIC_PROFILE_ID, mutation_event.ENTREZ_GENE_ID, PROTEIN_CHANGE as ALTERATION, ' +
        'MUTATION_TYPE as CONSEQUENCE, mutation.MUTATION_EVENT_ID, mutation.SAMPLE_ID FROM cbioportal.mutation_event ' +
        'inner join mutation on mutation.MUTATION_EVENT_ID = mutation_event.MUTATION_EVENT_ID ' +
        'inner join genetic_profile on genetic_profile.GENETIC_PROFILE_ID = mutation.GENETIC_PROFILE_ID ' +
        'inner join cancer_study on cancer_study.CANCER_STUDY_ID = genetic_profile.CANCER_STUDY_ID ' +
        'WHERE cancer_study.CANCER_STUDY_IDENTIFIER = "'+study_id +'"')
        for row in cursor.fetchall():
            if row[1] in cancer_genes:
                mutations += [{ "id": "_".join([str(row[4]), str(row[0]), str(row[5])]), "geneticProfileId": row[0], "entrezGeneId": row[1],
                                "alteration": row[2], "consequence": row[3]}]
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        return None
    return mutations

def get_current_cna_data(study_id, cursor, cancer_genes):
    """ Get cna data from the current study.
        Returns an array of dictionaries, with the following keys:
        id, geneticProfileId, entrezGeneId, and alteration
    """
    cna = []
    try:
        cursor.execute('SELECT genetic_profile.GENETIC_PROFILE_ID, '+ 'cna_event.ENTREZ_GENE_ID, ALTERATION, '+
        'sample_cna_event.CNA_EVENT_ID, sample_cna_event.SAMPLE_ID from cbioportal.cna_event ' +
        'inner join sample_cna_event on sample_cna_event.CNA_EVENT_ID = cna_event.CNA_EVENT_ID '+
        'inner join genetic_profile on genetic_profile.GENETIC_PROFILE_ID = sample_cna_event.GENETIC_PROFILE_ID '+
        'inner join cancer_study on cancer_study.CANCER_STUDY_ID = genetic_profile.CANCER_STUDY_ID '+
        'WHERE cancer_study.CANCER_STUDY_IDENTIFIER = "'+study_id +'"')
        for row in cursor.fetchall():
            if row[1] in cancer_genes:
                alteration = list(cna_alteration_types.keys())[
                    list(cna_alteration_types.values()).index(row[2])]
                cna += [{"id": "_".join([str(row[2]), str(row[0]), str(row[3])]), "geneticProfileId": row[0], "entrezGeneId": row[1],
                        "alteration": alteration}]
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        return None
    return cna

def get_reference_genome(study_id, cursor):
    """ Get reference genome from the study """
    ref_genome = []
    try:
        cursor.execute('SELECT reference_genome.NAME FROM reference_genome ' +
        'inner join cancer_study ON cancer_study.reference_genome_id = reference_genome.reference_genome_id ' +
        'WHERE cancer_study.CANCER_STUDY_IDENTIFIER = "'+study_id +'"')
        for row in cursor.fetchall():
            ref_genome += [row[0]]
        if len(ref_genome) == 1:
            return REFERENCE_GENOME[ref_genome[0]]
        else:
            raise ValueError("There is an error when retrieving the reference genome, as multiple values have been retrieved: "+ref_genome)
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)

def fetch_oncokb_mutation_annotations(mutation_data, ref_genome):
    request_url = "https://demo.oncokb.org/api/v1/annotate/mutations/byProteinChange"
    request_headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}
    request_payload = create_mutation_request_payload(mutation_data, ref_genome)
    request = requests.post(url=request_url, headers=request_headers, data=request_payload)

    row_number_to_annotation = {}
    if request.ok:
        return request.json()
    else:
        if request.status_code == 404:
            raise ConnectionError(
                "An error occurred when trying to connect to OncoKB for retrieving of mutation annotations.")
        else:
            request.raise_for_status()

def create_mutation_request_payload(mutation_data, ref_genome):
    elements = {}
    for mutation in mutation_data:
        elements[mutation["id"]] = '{"alteration": "' + mutation["alteration"] + '", "consequence": "'+mutation["consequence"] + \
                            '", "gene": {"entrezGeneId": '+ str(mutation["entrezGeneId"]) +'}, "id": "'+mutation["id"] + \
                            '", "referenceGenome": "'+ref_genome+'"}'

    # normalize for alteration id since same alteration is represented in multiple samples
    payload = '[' + ', '.join(elements.values()) + ']'
    return payload

def fetch_oncokb_copy_number_annotations(copy_number_data, ref_genome):
    request_url = "https://demo.oncokb.org/api/v1/annotate/copyNumberAlterations"
    request_headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}
    request_payload = create_copy_number_request_payload(copy_number_data, ref_genome)
    print(request_payload)
    request = requests.post(url=request_url, headers=request_headers, data=request_payload)

    if request.ok:
        # Parse transcripts and exons from JSON
        return request.json()
    else:
        if request.status_code == 404:
            raise ConnectionError(
                "An error occurred when trying to connect to OncoKB for retrieving of mutation annotations.")
            sys.exit(1)
        else:
            request.raise_for_status()


def create_copy_number_request_payload(copy_number_data, ref_genome):
    elements = {}
    for copy_number in copy_number_data:
        elements[copy_number["id"]] = '{"copyNameAlterationType":"'+ copy_number["alteration"]+'", "gene":{"entrezGeneId":'+str(copy_number["entrezGeneId"])+ \
            '}, "id":"'+copy_number["id"]+'", "referenceGenome": "'+ref_genome+'"}'
    
    # normalize for alteration id since same alteration is represented in multiple samples
    payload = '[' + ', '.join(elements.values()) + ']'
    return payload

def get_oncokb_cancer_genes():
    request_url = "https://demo.oncokb.org/api/v1/utils/cancerGeneList"
    request = requests.get(url=request_url)

    if request.ok:
        return request.json()
    else:
        if request.status_code == 404:
            raise ConnectionError(
                "An error occurred when trying to connect to OncoKB for retrieving of mutation annotations.")
        else:
            request.raise_for_status()

def get_oncokb_cancer_genes_by_entrezId():
    cancer_genes = get_oncokb_cancer_genes()
    entrez_ids = []
    for gene in cancer_genes:
        entrez_ids += [gene["entrezGeneId"]]
    return entrez_ids

def update_annotations(result, cursor):
    for entry in result:
        parsed_id = entry["query"]["id"].split("_")
        event_id = parsed_id[0]
        genetic_profile_id = parsed_id[1]
        sample_id = parsed_id[2]
        oncogenic = libImportOncokb.evaluate_driver_passenger(entry["oncogenic"])
        try:
            cursor.execute('UPDATE cbioportal.alteration_driver_annotation'+
            ' SET DRIVER_FILTER = "'+ oncogenic + '"' +
            ' WHERE alteration_driver_annotation.ALTERATION_EVENT_ID = '+ event_id +
            ' and alteration_driver_annotation.GENETIC_PROFILE_ID = '+ genetic_profile_id +
            ' and alteration_driver_annotation.SAMPLE_ID = '+ sample_id)
        except MySQLdb.Error as msg:
            print(msg, file=ERROR_FILE)
        
def main_import(study_id, properties_filename):    
    # 1. check mutation file present
    # 2. check no custom driver columns present
    # 3. check entrez gene id column is present and values are not empty for any gene
    # 4. check columns below are present
    # 5. extract features (gene symbol, mutation id) from columns
    #   entrez gene id --> Entrez_Gene_Id
    #   alteration --> HGVSp_Short
    #   consequence --> Variant_Classification
    #   proteinStart --> extract from Protein_position (see getProteinStart() in ExtendedMutationUtil.java)
    #   proteinEnd --> extract from Protein_position (see getProteinPosEnd() in ExtendedMutationUtil.java)
    # create a map indexed by line number
    # 6. retrieve oncokb annotations batch wise; keep value 'oncogenic'
    # 7. loop over every line in the MAF file and add Driver_Filter and Driver_Filter_Annotation columns
    # Driver_Filter becomes 'Putative_Driver' values are 'Likely Oncogenic', 'Oncogenic', and 'Predicted Oncogenic'
    # Driver_Filter_Annotation becomes the value of the 'oncogenic' return value
    # 8. Rename the original MAF file to <filename>_orig.maf, and use updated MAF file at study loading.

    # # get a logger to emit messages
    # logger = logging.getLogger(__name__)
    # logger.setLevel(logging.INFO)
    # exit_status_handler = validateData.MaxLevelTrackingHandler()
    # logger.addHandler(exit_status_handler)

    # # set default message handler
    # text_handler = logging.StreamHandler(sys.stdout)
    # text_handler.setFormatter(
    #     cbioportal_common.LogfileStyleFormatter(study_dir))
    # collapsing_text_handler = cbioportal_common.CollapsingLogMessageHandler(
    #     capacity=5e5,
    #     flushLevel=logging.CRITICAL,
    #     target=text_handler)
    # collapsing_text_handler.setLevel(logging.INFO)
    # logger.addHandler(collapsing_text_handler)

    # meta_file_mutation = libImportOncokb.find_meta_file_by_fields(study_dir, {'genetic_alteration_type': 'MUTATION_EXTENDED'})
    # mutation_file_path = os.path.join(study_dir, libImportOncokb.find_data_file_from_meta_file(meta_file_mutation))
    # check_required_columns(libImportOncokb.get_first_line(open_mutations_file(mutation_file_path)).rstrip('\n').split('\t'))
    # check_disallowed_columns(libImportOncokb.get_first_line(open_mutations_file(mutation_file_path)).rstrip('\n').split('\t'))

    # global portal_instance
    # if hasattr(args, 'portal_info_dir') and args.portal_info_dir is not None:
    #     portal_instance = validateData.load_portal_info(args.portal_info_dir, logger,
    #                                        offline=True)
    # else:
    #     portal_instance = validateData.load_portal_info(server_url, logger)

    # row_number_to_feature = get_mutation_features(mutation_file_path)
    # row_number_to_annotation = fetch_oncokb_annotations(row_number_to_feature)
    # write_annotations_to_file(row_number_to_annotation, mutation_file_path)

    ############################
    # Parse portal.properties
    # check existence of properties file and sql file
    if not os.path.exists(properties_filename):
        print('properties file %s cannot be found' % (properties_filename), file=ERROR_FILE)
        sys.exit(2)

    # parse properties file
    portal_properties = get_portal_properties(properties_filename)
    if portal_properties is None:
        print('failure reading properties file (%s)' % (properties_filename), file=ERROR_FILE)
        sys.exit(1)

    # Connect to the database
    # TODO: must be a unique transaction
    connection, cursor = get_db_cursor(portal_properties)
    if cursor is None:
        print('failure connecting to sql database', file=ERROR_FILE)
        sys.exit(1)

    #0. Get OncoKB cancer genes
    cancer_genes = get_oncokb_cancer_genes_by_entrezId()

    # 1. Query DB to get mutation data of the study
    mutation_study_data = get_current_mutation_data(study_id, cursor, cancer_genes)

    # 2. Query DB to get cna data of the study
    cna_study_data = get_current_cna_data(study_id, cursor, cancer_genes)

    # 3. Call oncokb to get annotations for the mutation data retrieved
    ref_genome = get_reference_genome(study_id, cursor)
    mutation_result = fetch_oncokb_mutation_annotations(mutation_study_data, ref_genome)
    # print(mutation_result)
    
    # 4. Call oncokb to get annotations for the cna data retrieved
    # cna_result = fetch_oncokb_copy_number_annotations(cna_study_data, ref_genome)
    
    # 5. Query DB to clean alteration_driver_annotation table data for the study requested // go one by one and modify record
    update_annotations(mutation_result, cursor)

    # 6. Query DB to add retrieved oncokb data in alteration_driver_annotation table

    print('Update complete')

    return 0


# def open_mutations_file(file_name):
#     try:
#         file = open(file_name)
#     except FileNotFoundError:
#         raise FilenotFoundError("Could not open MAF file at path '" + file_name + "'")
#     return file


# def check_required_columns(header_elements):
#     missing_columns = []
#     for required_column in required_mutation_columns:
#         if not required_column in header_elements:
#             missing_columns.append(required_column)
#     if len(missing_columns) > 0:
#         raise RuntimeError("One or more required columns for OncoKb import are missing from the MAF file. " \
#                            "Missing column(s): [" + ", ".join(missing_columns) + "]")


# def check_disallowed_columns(header_elements):
#     disallowed_columns = []
#     for disallowed_column in disallowed_mutation_columns:
#         if disallowed_column in header_elements:
#             disallowed_columns.append(disallowed_column)
#     if len(disallowed_columns) > 0:
#         raise RuntimeError("One or more disallowed columns for OncoKb import are present in the MAF file. " \
#                            "Disallowed column(s): [" + ", ".join(disallowed_columns) + "]")


# def get_mutation_features(mutation_file_path):
#     header_elements = libImportOncokb.get_first_line(open_mutations_file(mutation_file_path)).rstrip().split('\t')
#     header_indexes = {}
#     for required_column in required_mutation_columns + ['Entrez_Gene_Id']:
#         header_indexes[required_column] = header_elements.index(required_column)
#     row_number_to_feature = {}
#     row_counter = 0
#     mutation_file = open_mutations_file(mutation_file_path)
#     for line in mutation_file:
#         row_counter += 1
#         if line == '\n' or line.startswith('#') or line.startswith(header_elements[0]):
#             continue  # skip comment and header line
#         line_elements = line.rstrip().split('\t')
#         feature = {}
#         for column_name, index in header_indexes.items():
#             value = line_elements[index]
#             if value != '':
#                 if column_name == 'HGVSp_Short':
#                     value = value.replace('p.', '')
#                 feature[column_name] = value
#             elif column_name != 'Entrez_Gene_Id' and column_name != 'Protein_position':
#                 raise RuntimeError("Empty value encounterd in column '" +
#                                    column_name + "' in row " + str(row_counter) + "." \
#                                                                                   "OncoKb annotations cannot be imported. Please fix and rerun.")

#         # resolve gene symbols to Entrez Ids if needed
#         if 'Entrez_Gene_Id' in feature and feature['Entrez_Gene_Id'] is not None and feature['Entrez_Gene_Id'] != '':
#             entrez_gene_ids = [feature['Entrez_Gene_Id']]
#         else:
#             entrez_gene_ids = portal_instance.hugo_entrez_map[feature['Hugo_Symbol']]

#         if len(entrez_gene_ids) > 1:
#             logger.error("Multiple Entrez gene ids were found for a gene." \
#                          "OncoKb annotations will not be imported for this gene." \
#                          "Please fix and rerun.",
#                          extra={'symbol': feature['Hugo_Symbol'], 'row': str(row_counter)})
#             feature['Entrez_Gene_Id'] = None
#         elif len(entrez_gene_ids) == 0:
#             logger.error("Could not find the Entrez gene id for a gene." \
#                          "OncoKb annotations will not be imported for this gene." \
#                          "Please fix and rerun.",
#                          extra={'symbol': feature['Hugo_Symbol'], 'row': str(row_counter)})
#             feature['Entrez_Gene_Id'] = None
#         else:
#             feature['Entrez_Gene_Id'] = str(entrez_gene_ids[0])
#             feature['id'] = "_".join(
#                 [feature['Entrez_Gene_Id'], feature['HGVSp_Short'], feature['Variant_Classification']])

#         row_number_to_feature[row_counter] = feature
#     mutation_file.close()
#     return row_number_to_feature


# def write_annotations_to_file(row_number_to_annotation, mutations_file_path):
#     meta_cna_file_name = os.path.basename(mutations_file_path)
#     dir = os.path.dirname(mutations_file_path)
#     backup_file_name = 'ONCOKB_IMPORT_BACKUP_' + meta_cna_file_name
#     backup_file_path = os.path.join(dir, backup_file_name)
#     try:
#         new_file = open(mutations_file_path + '_temp', "x")
#         header_updated = False
#         row_counter = 0
#         mutations_file = open_mutations_file(mutations_file_path)
#         for line in mutations_file:
#             row_counter += 1
#             if not line.startswith('#'):
#                 if not header_updated:
#                     line = line.rstrip(
#                         '\n') + '\tcbp_driver\tcbp_driver_annotation' + '\n'  # add custom driver columns to header
#                     header_updated = True
#                 else:
#                     if row_counter in row_number_to_annotation:
#                         oncokb_annotation = row_number_to_annotation[row_counter]
#                         line = line.rstrip('\n') + '\t' + libImportOncokb.evaluate_driver_passenger(
#                             oncokb_annotation['oncogenic']) + '\t' + oncokb_annotation['oncogenic'] + '\n'
#                     else:
#                         line = line.rstrip('\n') + '\t\t\n'
#             new_file.write(line)
#     except FileExistsError:
#         raise FileExistsError("Backup MAF file that does not contain OncoKB annotations does already exist. " \
#                               "Please remove file '" + backup_file_name + "' and try again.")
#     finally:
#         mutations_file.close()
#         new_file.close()
#     os.rename(mutations_file_path, backup_file_path)
#     os.rename(mutations_file_path + '_temp', mutations_file_path)
#     return


def interface():
    parser = argparse.ArgumentParser(description='cBioPortal OncoKB annotation updater')
    parser.add_argument('-s', '--study_id',
                        type=str,
                        required=True,
                        help='Study Identifier')
    parser.add_argument('-p', '--portal_properties', type=str, required=True,
                        help='portal.properties of cBioPortal')
    parser = parser.parse_args()
    return parser


if __name__ == '__main__':
    try:
        parsed_args = interface()
        exit_status = main_import(parsed_args.study_id, parsed_args.portal_properties)
    finally:
        logging.shutdown()
        del logging._handlerList[:]  # workaround for harmless exceptions on exit
    print(('Import of OncoKB annotations for mutations {status}.'.format(
        status={0: 'succeeded',
                1: 'failed',
                2: 'not performed as problems occurred',
                3: 'succeeded with warnings'}.get(exit_status, 'unknown'))), file=sys.stderr)
    sys.exit(exit_status)
