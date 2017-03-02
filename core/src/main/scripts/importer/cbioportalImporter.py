#!/usr/bin/env python2.7

# ------------------------------------------------------------------------------
# Script which imports portal data.
#
# ------------------------------------------------------------------------------

import os
import sys
import argparse
import logging
import re

import cbioportal_common
from cbioportal_common import OUTPUT_FILE
from cbioportal_common import ERROR_FILE
from cbioportal_common import MetaFileTypes
from cbioportal_common import IMPORTER_CLASSNAME_BY_META_TYPE
from cbioportal_common import IMPORTER_REQUIRES_METADATA
from cbioportal_common import IMPORT_CANCER_TYPE_CLASS
from cbioportal_common import IMPORT_STUDY_CLASS
from cbioportal_common import UPDATE_STUDY_STATUS_CLASS
from cbioportal_common import REMOVE_STUDY_CLASS
from cbioportal_common import IMPORT_CASE_LIST_CLASS
from cbioportal_common import ADD_CASE_LIST_CLASS
from cbioportal_common import VERSION_UTIL_CLASS
from cbioportal_common import run_java


# ------------------------------------------------------------------------------
# globals

LOGGER = None

# commands
IMPORT_CANCER_TYPE = "import-cancer-type"
IMPORT_STUDY = "import-study"
REMOVE_STUDY = "remove-study"
IMPORT_STUDY_DATA = "import-study-data"
IMPORT_CASE_LIST = "import-case-list"

COMMANDS = [IMPORT_CANCER_TYPE, IMPORT_STUDY, REMOVE_STUDY, IMPORT_STUDY_DATA, IMPORT_CASE_LIST]
PORTAL_HOME = "PORTAL_HOME"

# ------------------------------------------------------------------------------
# sub-routines

def import_cancer_type(jvm_args, data_filename):
    args = jvm_args.split(' ')
    args.append(IMPORT_CANCER_TYPE_CLASS)
    args.append(data_filename)
    args.append("false") # don't clobber existing table
    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)

def import_study(jvm_args, meta_filename):
    args = jvm_args.split(' ')
    args.append(IMPORT_STUDY_CLASS)
    args.append(meta_filename)
    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)

def update_study_status(jvm_args, study_id):
    args = jvm_args.split(' ')
    args.append(UPDATE_STUDY_STATUS_CLASS)
    args.append(study_id)
    args.append("AVAILABLE")
    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)

def remove_study(jvm_args, meta_filename):
    args = jvm_args.split(' ')
    args.append(REMOVE_STUDY_CLASS)
    meta_dict, meta_type = cbioportal_common.parse_metadata_file(
        meta_filename, logger=LOGGER)
    if meta_type != MetaFileTypes.STUDY:
        # invalid file, skip
        print >> ERROR_FILE, 'Not a study meta file: ' + meta_filename
        return
    args.append(meta_dict['cancer_study_identifier'])
    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)

def import_study_data(jvm_args, meta_filename, data_filename):

    args = jvm_args.split(' ')
    meta_file_dict, meta_file_type = cbioportal_common.parse_metadata_file(
        meta_filename, logger=LOGGER)
    if meta_file_type is None:
        # invalid file, skip
        return

    if not data_filename.endswith(meta_file_dict['data_filename']):
        print >> ERROR_FILE, ("'data_filename' in meta file contradicts "
                              "data filename in command, skipping file")
        return

    importer = IMPORTER_CLASSNAME_BY_META_TYPE[meta_file_type]

    args.append(importer)
    if IMPORTER_REQUIRES_METADATA[importer]:
        args.append("--meta")
        args.append(meta_filename)
        args.append("--loadMode")
        args.append("bulkload")
    if importer in ("org.mskcc.cbio.portal.scripts.ImportMutSigData", "org.mskcc.cbio.portal.scripts.ImportGisticData"):
        args.append("--data")
        args.append(data_filename)
        args.append("--study")
        args.append(meta_file_dict['cancer_study_identifier'])
    else:
        args.append("--data")
        args.append(data_filename)

    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)

def import_case_list(jvm_args, meta_filename):
    args = jvm_args.split(' ')
    args.append(IMPORT_CASE_LIST_CLASS)
    args.append(meta_filename)
    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)
    
def add_global_case_list(jvm_args, study_id):
    args = jvm_args.split(' ')
    args.append(ADD_CASE_LIST_CLASS)
    args.append(study_id)
    args.append("all")
    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)

def check_version(jvm_args):
    args = jvm_args.split(' ')
    args.append(VERSION_UTIL_CLASS)
    try:
        run_java(*args)
    except:
        print >> OUTPUT_FILE, 'Error, probably due to this version of the portal being out of sync with the database. Run the database migration script located at PORTAL_HOME/core/src/main/scripts/migrate_db.py before continuing.'
        raise

def process_case_lists(jvm_args, case_list_dir):
    for case_list in os.listdir(case_list_dir):
        # skip "temp"/backup files made by some text editors:
        if not (case_list.startswith('.') or case_list.endswith('~')):
            import_case_list(jvm_args, os.path.join(case_list_dir, case_list))

def process_command(jvm_args, command, meta_filename, data_filename):
    if command == IMPORT_CANCER_TYPE:
        import_cancer_type(jvm_args, data_filename)
    elif command == IMPORT_STUDY:
        import_study(jvm_args, meta_filename)
    elif command == REMOVE_STUDY:
        remove_study(jvm_args, meta_filename)
    elif command == IMPORT_STUDY_DATA:
        import_study_data(jvm_args, meta_filename, data_filename)
    elif command == IMPORT_CASE_LIST:
        import_case_list(jvm_args, meta_filename)

def process_directory(jvm_args, study_directory):

    """Import an entire study directory based on meta files found."""

    meta_filenames = (
        os.path.join(study_directory, f) for
        f in os.listdir(study_directory) if
        re.search(r'(\b|_)meta(\b|[_0-9])', f,
                  flags=re.IGNORECASE) and
        not (f.startswith('.') or f.endswith('~')))
    study_id = None
    study_metafile = None
    study_metadata = None
    cancer_type_filepairs = []
    sample_attr_filepair = None
    regular_filepairs = []
    gsva_score_filepair = None
    gsva_pvalue_filepair = None

    # read all meta files (excluding case lists) to determine what to import
    for f in meta_filenames:
        # parse meta file
        metadata, meta_file_type = cbioportal_common.parse_metadata_file(
            f, study_id=study_id, logger=LOGGER)
        if meta_file_type is None:
            # invalid meta file, let's die
            raise RuntimeError('Invalid meta file: ' + f)
        # remember study id to give an error in case any other file is referencing a different one
        if study_id is None and 'cancer_study_identifier' in metadata:
            study_id = metadata['cancer_study_identifier']

        if meta_file_type == MetaFileTypes.CANCER_TYPE:
            cancer_type_filepairs.append(
                (f, os.path.join(study_directory, metadata['data_filename'])))
        elif meta_file_type == MetaFileTypes.STUDY:
            if study_metafile is not None:
                raise RuntimeError(
                    'Multiple meta_study files found: {} and {}'.format(
                        study_metafile, f))
            study_metafile = f
            study_metadata = metadata
        elif meta_file_type == MetaFileTypes.SAMPLE_ATTRIBUTES:
            if sample_attr_filepair is not None:
                raise RuntimeError(
                    'Multiple sample attribute files found: {} and {}'.format(
                        sample_attr_filepair[0], f))
            sample_attr_filepair = (
                f, os.path.join(study_directory, metadata['data_filename']))
        elif meta_file_type == MetaFileTypes.GSVA_SCORES:
            gsva_score_filepair = (
                (f, os.path.join(study_directory, metadata['data_filename'])))
        elif meta_file_type == MetaFileTypes.GSVA_PVALUES:
            gsva_pvalue_filepair = (
                (f, os.path.join(study_directory, metadata['data_filename'])))
        else:
            regular_filepairs.append(
                (f, os.path.join(study_directory, metadata['data_filename'])))

    # First, import cancer types
    for meta_filename, data_filename in cancer_type_filepairs:
        import_cancer_type(jvm_args, data_filename)

    # Then define the study
    if study_metafile is None:
        raise RuntimeError('No meta_study file found')
    else:
        # First remove study if exists
        remove_study(jvm_args, study_metafile)
        import_study(jvm_args, study_metafile)

    # Next, we need to import sample definitions
    if sample_attr_filepair is None:
        raise RuntimeError('No sample attribute file found')
    else:
        meta_filename, data_filename = sample_attr_filepair
        import_study_data(jvm_args, meta_filename, data_filename)

    # Now, import everything else except gsva
    for meta_filename, data_filename in regular_filepairs:
        import_study_data(jvm_args, meta_filename, data_filename)

    # Now import gsva genetic profiles
    if gsva_score_filepair is not None:
        # First import the score data
        meta_filename, data_filename = gsva_score_filepair
        import_study_data(jvm_args, meta_filename, data_filename)
        #Second import the pvalue data
        meta_filename, data_filename = gsva_pvalue_filepair
        import_study_data(jvm_args, gsva_pvalue_filepair[0], gsva_pvalue_filepair[1])

    # do the case lists
    case_list_dirname = os.path.join(study_directory, 'case_lists')
    if os.path.isdir(case_list_dirname):
        process_case_lists(jvm_args, case_list_dirname)

    if study_metadata.get('add_global_case_list', 'false').lower() == 'true':
        add_global_case_list(jvm_args, study_id)
        
    # enable study
    update_study_status(jvm_args, study_id)


def usage():
    # TODO : replace this by usage string from interface()
    print >> OUTPUT_FILE, ('cbioportalImporter.py --jar-path (path to scripts jar file) ' +
                           '--command [%s] --study_directory <path to directory> '
                           '--meta_filename <path to metafile>'
                           '--data_filename <path to datafile>'
                           '--properties-filename <path to properties file> ' % (COMMANDS))

def check_args(command):
    if command not in COMMANDS:
        usage()
        sys.exit(2)


def check_files(meta_filename, data_filename):
    if meta_filename and not os.path.exists(meta_filename):
        print >> ERROR_FILE, 'meta-file cannot be found: ' + meta_filename
        sys.exit(2)
    if data_filename  and not os.path.exists(data_filename):
        print >> ERROR_FILE, 'data-file cannot be found:' + data_filename
        sys.exit(2)
        
def check_dir(study_directory):
    # check existence of directory
    if not os.path.exists(study_directory) and study_directory != '':
        print >> ERROR_FILE, 'Study cannot be found: ' + study_directory
        sys.exit(2)

def interface():
    parser = argparse.ArgumentParser(description='cBioPortal meta Importer')
    parser.add_argument('-c', '--command', type=str, required=False,
                        help='Command for import. Allowed commands: import-cancer-type, '
                             'import-study, import-study-data, import-case-list or '
                             'remove-study')
    parser.add_argument('-s', '--study_directory',type=str, required=False,
                        help='Path to Study Directory')
    parser.add_argument('-jar', '--jar_path',type=str, required=False,
                        help='Path to scripts JAR file')
    parser.add_argument('-meta', '--meta_filename',type=str, required=False,
                        help='Path to meta file')
    parser.add_argument('-data', '--data_filename', type=str, required=False,
                        help='Path to Data file')
    # TODO - add same argument to metaimporter
    # TODO - harmonize on - and _

    parser = parser.parse_args()
    return parser


def main(args):

    global LOGGER

    # get the logger with a handler to print logged error messages to stderr
    module_logger = logging.getLogger(__name__)
    error_handler = logging.StreamHandler(sys.stderr)
    error_handler.setFormatter(cbioportal_common.LogfileStyleFormatter(
            os.getcwd()))
    error_handler.setLevel(logging.ERROR)
    module_logger.addHandler(error_handler)
    LOGGER = module_logger

    # jar_path is optional. If not set, try to make it up based on PORTAL_HOME
    if args.jar_path is None:
        portal_home = os.environ.get('PORTAL_HOME', None)
        if portal_home is None:
            # PORTAL_HOME also not set...quit trying with error: 
            print 'Error: either --jar_path needs to be given or environment variable PORTAL_HOME needs to be set'
            sys.exit(2)
        else: 
            #find jar files in lib folder and add them to classpath:
            import glob
            jars = glob.glob(portal_home + "/scripts/target/scripts-*.jar")
            if len(jars) != 1:
                print 'Expected to find 1 scripts-*.jar, but found: ' + str(len(jars))
                sys.exit(2)
            args.jar_path = jars[0]
            print 'Data loading step using: {}\n'.format(args.jar_path)
        
    # process the options
    jvm_args = "-Dspring.profiles.active=dbcp -cp " + args.jar_path
    study_directory = args.study_directory

    # check if DB version and application version are in sync
    check_version(jvm_args)

    if study_directory != None:
        check_dir(study_directory)
        process_directory(jvm_args, study_directory)
    else:
        check_args(args.command)
        check_files(args.meta_filename, args.data_filename)
        process_command(jvm_args, args.command, args.meta_filename, args.data_filename)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    parsed_args = interface()
    main(parsed_args)
