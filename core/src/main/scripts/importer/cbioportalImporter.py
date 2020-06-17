#!/usr/bin/env python3

# ------------------------------------------------------------------------------
# Script which imports portal data.
#
# ------------------------------------------------------------------------------

import os
import sys
import importlib
import argparse
import logging
import re
from pathlib import Path

# configure relative imports if running as a script; see PEP 366
# it might passed as empty string by certain tooling to mark a top level module
if __name__ == "__main__" and (__package__ is None or __package__ == ''):
    # replace the script's location in the Python search path by the main
    # scripts/ folder, above it, so that the importer package folder is in
    # scope and *not* directly in sys.path; see PEP 395
    sys.path[0] = str(Path(sys.path[0]).resolve().parent)
    __package__ = 'importer'
    # explicitly load the package, which is needed on CPython 3.4 because it
    # doesn't include https://github.com/python/cpython/pull/2639
    importlib.import_module(__package__)

from . import cbioportal_common
from .cbioportal_common import OUTPUT_FILE
from .cbioportal_common import ERROR_FILE
from .cbioportal_common import MetaFileTypes
from .cbioportal_common import IMPORTER_CLASSNAME_BY_META_TYPE
from .cbioportal_common import IMPORTER_REQUIRES_METADATA
from .cbioportal_common import IMPORT_CANCER_TYPE_CLASS
from .cbioportal_common import IMPORT_STUDY_CLASS
from .cbioportal_common import UPDATE_STUDY_STATUS_CLASS
from .cbioportal_common import REMOVE_STUDY_CLASS
from .cbioportal_common import IMPORT_CASE_LIST_CLASS
from .cbioportal_common import ADD_CASE_LIST_CLASS
from .cbioportal_common import VERSION_UTIL_CLASS
from .cbioportal_common import run_java


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

def remove_study_meta(jvm_args, meta_filename):
    args = jvm_args.split(' ')
    args.append(REMOVE_STUDY_CLASS)
    meta_dictionary = cbioportal_common.parse_metadata_file(
        meta_filename, logger=LOGGER)
    if meta_dictionary['meta_file_type'] != MetaFileTypes.STUDY:
        # invalid file, skip
        print('Not a study meta file: ' + meta_filename, file=ERROR_FILE)
        return
    args.append(meta_dictionary['cancer_study_identifier'])
    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)

def remove_study_id(jvm_args, study_id):
    args = jvm_args.split(' ')
    args.append(REMOVE_STUDY_CLASS)
    args.append(study_id)
    args.append("--noprogress") # don't report memory usage and % progress
    run_java(*args)


def import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, meta_file_dictionary = None):
    args = jvm_args.split(' ')

    # In case the meta file is already parsed in a previous function, it is not
    # necessary to parse it again
    if meta_file_dictionary is None:
        meta_file_dictionary = cbioportal_common.parse_metadata_file(
        meta_filename, logger=LOGGER)

    # Retrieve meta file type
    meta_file_type = meta_file_dictionary['meta_file_type']

    # Do not update entities by default
    shouldUpdateGenericAssayEntities = False
    if update_generic_assay_entity != None and update_generic_assay_entity.casefold() == "True".casefold():
        shouldUpdateGenericAssayEntities = True

    # invalid file, skip
    if meta_file_type is None:
        print(("Unrecognized meta file type '%s', skipping file"
                              % (meta_file_type)), file=ERROR_FILE)
        return

    if not data_filename.endswith(meta_file_dictionary['data_filename']):
        print(("'data_filename' in meta file contradicts "
                              "data filename in command, skipping file"), file=ERROR_FILE)
        return

    importer = IMPORTER_CLASSNAME_BY_META_TYPE[meta_file_type]

    args.append(importer)
    if IMPORTER_REQUIRES_METADATA[importer]:
        args.append("--meta")
        args.append(meta_filename)
        args.append("--loadMode")
        args.append("bulkload")
    if importer == "org.mskcc.cbio.portal.scripts.ImportProfileData" and shouldUpdateGenericAssayEntities:
        args.append("--update-info")
        args.append("True")
    elif importer == "org.mskcc.cbio.portal.scripts.ImportProfileData" and not shouldUpdateGenericAssayEntities:
        args.append("--update-info")
        args.append("False")
    if importer in ("org.mskcc.cbio.portal.scripts.ImportMutSigData", "org.mskcc.cbio.portal.scripts.ImportGisticData"):
        args.append("--data")
        args.append(data_filename)
        args.append("--study")
        args.append(meta_file_dictionary['cancer_study_identifier'])
    elif importer == "org.mskcc.cbio.portal.scripts.ImportGenePanelProfileMap":
        args.append("--meta")
        args.append(meta_filename)
        args.append("--data")
        args.append(data_filename)
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
        print(
            'Error, probably due to this version of the portal '
            'being out of sync with the database. '
            'Run the database migration script located at '
            'CBIOPORTAL_SRC/core/src/main/scripts/migrate_db.py '
            'before continuing.',
            file=OUTPUT_FILE)
        raise

def process_case_lists(jvm_args, case_list_dir):
    for case_list in os.listdir(case_list_dir):
        # skip "temp"/backup files made by some text editors:
        if not (case_list.startswith('.') or case_list.endswith('~')):
            import_case_list(jvm_args, os.path.join(case_list_dir, case_list))

def process_command(jvm_args, command, meta_filename, data_filename, study_ids, update_generic_assay_entity = None):
    if command == IMPORT_CANCER_TYPE:
        import_cancer_type(jvm_args, data_filename)
    elif command == IMPORT_STUDY:
        import_study(jvm_args, meta_filename)
    elif command == REMOVE_STUDY:
        if study_ids == None:
            remove_study_meta(jvm_args, meta_filename)
        elif meta_filename == None:
            study_ids = study_ids.split(",")
            for study_id in study_ids:
                remove_study_id(jvm_args, study_id)
        else:
            raise RuntimeError('Your command uses both -id and -meta. Please, use only one of the two parameters.')
    elif command == IMPORT_STUDY_DATA:
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity)
    elif command == IMPORT_CASE_LIST:
        import_case_list(jvm_args, meta_filename)

def process_directory(jvm_args, study_directory, update_generic_assay_entity = None):
    """
    Import an entire study directory based on meta files found.

    1. Determine meta files in study directory.
    2. Read all meta files and determine file types.
    3. Import data files in specific order by file type.
    """

    study_id = None
    study_meta_filename = None
    study_meta_dictionary = {}
    cancer_type_filepairs = []
    sample_attr_filepair = None
    sample_resource_filepair = None
    resource_definition_filepair = None
    regular_filepairs = []
    gene_panel_matrix_filepair = None
    zscore_filepairs = []
    gsva_score_filepair = None
    gsva_pvalue_filepair = None
    fusion_filepair = None

    # Determine meta filenames in study directory
    meta_filenames = (
        os.path.join(study_directory, meta_filename) for
        meta_filename in os.listdir(study_directory) if
        re.search(r'(\b|_)meta(\b|[_0-9])', meta_filename,
                  flags=re.IGNORECASE) and
        not (meta_filename.startswith('.') or meta_filename.endswith('~')))

    # Read all meta files (excluding case lists) to determine what to import
    for meta_filename in meta_filenames:

        # Parse meta file
        meta_dictionary = cbioportal_common.parse_metadata_file(
            meta_filename, study_id=study_id, logger=LOGGER)

        # Save meta dictionary in study meta dictionary
        study_meta_dictionary[meta_filename] = meta_dictionary

        # Retrieve meta file type
        meta_file_type = meta_dictionary['meta_file_type']
        if meta_file_type is None:
            # invalid meta file, let's die
            raise RuntimeError('Invalid meta file: ' + meta_filename)

        # remember study id to give an error in case any other file is referencing a different one
        if study_id is None and 'cancer_study_identifier' in meta_dictionary:
            study_id = meta_dictionary['cancer_study_identifier']

        # Check the type of metafile. It is to know which metafile types the
        # study contains because at a later stage we want to import in a
        # specific order.

        # Check for cancer type file
        if meta_file_type == MetaFileTypes.CANCER_TYPE:
            cancer_type_filepairs.append(
                (meta_filename, os.path.join(study_directory, meta_dictionary['data_filename'])))
        # Check for meta study file
        elif meta_file_type == MetaFileTypes.STUDY:
            if study_meta_filename is not None:
                raise RuntimeError(
                    'Multiple meta_study files found: {} and {}'.format(
                        study_meta_filename, meta_filename))
            # Determine the study meta filename
            study_meta_filename = meta_filename
            study_meta_dictionary[study_meta_filename] = meta_dictionary
        # Check for resource definitions
        elif meta_file_type == MetaFileTypes.RESOURCES_DEFINITION:
            if resource_definition_filepair is not None:
                raise RuntimeError(
                    'Multiple resource definition files found: {} and {}'.format(
                        resource_definition_filepair[0], meta_filename))   # pylint: disable=unsubscriptable-object
            resource_definition_filepair = (
                meta_filename, os.path.join(study_directory, meta_dictionary['data_filename']))            
        # Check for sample attributes
        elif meta_file_type == MetaFileTypes.SAMPLE_ATTRIBUTES:
            if sample_attr_filepair is not None:
                raise RuntimeError(
                    'Multiple sample attribute files found: {} and {}'.format(
                        sample_attr_filepair[0], meta_filename))   # pylint: disable=unsubscriptable-object
            sample_attr_filepair = (
                meta_filename, os.path.join(study_directory, meta_dictionary['data_filename']))
        elif meta_file_type == MetaFileTypes.SAMPLE_RESOURCES:
            if sample_resource_filepair is not None:
                raise RuntimeError(
                    'Multiple sample resource files found: {} and {}'.format(
                        sample_resource_filepair[0], meta_filename))   # pylint: disable=unsubscriptable-object
            sample_resource_filepair = (
                meta_filename, os.path.join(study_directory, meta_dictionary['data_filename']))
        # Check for gene panel matrix
        elif meta_file_type == MetaFileTypes.GENE_PANEL_MATRIX:
            gene_panel_matrix_filepair = (
                (meta_filename, os.path.join(study_directory, meta_dictionary['data_filename'])))
        # Check for z-score exression files
        elif meta_file_type == MetaFileTypes.EXPRESSION and meta_dictionary['datatype'] == "Z-SCORE":
            zscore_filepairs.append(
                (meta_filename, os.path.join(study_directory, meta_dictionary['data_filename'])))
        # Check for GSVA scores
        elif meta_file_type == MetaFileTypes.GSVA_SCORES:
            gsva_score_filepair = (
                (meta_filename, os.path.join(study_directory, meta_dictionary['data_filename'])))
        # Check for GSVA p-values
        elif meta_file_type == MetaFileTypes.GSVA_PVALUES:
            gsva_pvalue_filepair = (
                (meta_filename, os.path.join(study_directory, meta_dictionary['data_filename'])))
        # Check for fusion data
        elif meta_file_type == MetaFileTypes.FUSION:
            fusion_filepair = (
                (meta_filename, os.path.join(study_directory, meta_dictionary['data_filename'])))
        # Add all other types of data
        else:
            regular_filepairs.append(
                (meta_filename, os.path.join(study_directory, meta_dictionary['data_filename'])))

    # First, import cancer types
    for meta_filename, data_filename in cancer_type_filepairs:
        import_cancer_type(jvm_args, data_filename)

    # Then define the study
    if study_meta_filename is None:
        raise RuntimeError('No meta_study file found')
    else:
        # First remove study if exists
        remove_study_meta(jvm_args, study_meta_filename)
        import_study(jvm_args, study_meta_filename)

    # Next, we need to import sample definitions
    if sample_attr_filepair is None:
        raise RuntimeError('No sample attribute file found')
    else:
        meta_filename, data_filename = sample_attr_filepair
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

    # Next, we need to import resource definitions for resource data
    if resource_definition_filepair is not None:
        meta_filename, data_filename = resource_definition_filepair
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

    # Next, we need to import sample definitions for resource data
    if sample_resource_filepair is not None:
        meta_filename, data_filename = sample_resource_filepair
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

    # Next, import everything else except gene panel, fusion data, GSVA and
    # z-score expression. If in the future more types refer to each other, (like
    # in a tree structure) this could be programmed in a recursive fashion.
    for meta_filename, data_filename in regular_filepairs:
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

    # Import fusion data (after mutation)
    if fusion_filepair is not None:
        meta_filename, data_filename = fusion_filepair
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

    # Import expression z-score (after expression)
    for meta_filename, data_filename in zscore_filepairs:
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

    # Import GSVA genetic profiles (after expression and z-scores)
    if gsva_score_filepair is not None:

        # First import the GSVA score data
        meta_filename, data_filename = gsva_score_filepair
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

        # Second import the GSVA p-value data
        meta_filename, data_filename = gsva_pvalue_filepair
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

    if gene_panel_matrix_filepair is not None:
        meta_filename, data_filename = gene_panel_matrix_filepair
        import_study_data(jvm_args, meta_filename, data_filename, update_generic_assay_entity, study_meta_dictionary[meta_filename])

    # Import the case lists
    case_list_dirname = os.path.join(study_directory, 'case_lists')
    if os.path.isdir(case_list_dirname):
        process_case_lists(jvm_args, case_list_dirname)

    if study_meta_dictionary[study_meta_filename].get('add_global_case_list', 'false').lower() == 'true':
        add_global_case_list(jvm_args, study_id)

    # enable study
    update_study_status(jvm_args, study_id)


def usage():
    # TODO : replace this by usage string from interface()
    print(('cbioportalImporter.py --jar-path (path to scripts jar file) ' +
                           '--command [%s] --study_directory <path to directory> '
                           '--meta_filename <path to metafile>'
                           '--data_filename <path to datafile>'
                           '--study_ids <cancer study ids for remove-study command, comma separated>'
                           '--properties-filename <path to properties file> ' % (COMMANDS)), file=OUTPUT_FILE)

def check_args(command):
    if command not in COMMANDS:
        usage()
        sys.exit(2)


def check_files(meta_filename, data_filename):
    if meta_filename and not os.path.exists(meta_filename):
        print('meta-file cannot be found: ' + meta_filename, file=ERROR_FILE)
        sys.exit(2)
    if data_filename  and not os.path.exists(data_filename):
        print('data-file cannot be found:' + data_filename, file=ERROR_FILE)
        sys.exit(2)

def check_dir(study_directory):
    # check existence of directory
    if not os.path.exists(study_directory) and study_directory != '':
        print('Study cannot be found: ' + study_directory, file=ERROR_FILE)
        sys.exit(2)

def add_parser_args(parser):
    parser.add_argument('-s', '--study_directory', type=str, required=False,
                        help='Path to Study Directory')
    parser.add_argument('-jar', '--jar_path', type=str, required=False,
                        help='Path to scripts JAR file')
    parser.add_argument('-meta', '--meta_filename', type=str, required=False,
                        help='Path to meta file')
    parser.add_argument('-data', '--data_filename', type=str, required=False,
                        help='Path to Data file')

def interface():
    parent_parser = argparse.ArgumentParser(description='cBioPortal meta Importer')
    add_parser_args(parent_parser)
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(title='subcommands', dest='subcommand',
                          help='Command for import. Allowed commands: import-cancer-type, '
                          'import-study, import-study-data, import-case-list or '
                          'remove-study')
    import_cancer_type = subparsers.add_parser('import-cancer-type', parents=[parent_parser], add_help=False)
    import_study = subparsers.add_parser('import-study', parents=[parent_parser], add_help=False)
    import_study_data = subparsers.add_parser('import-study-data', parents=[parent_parser], add_help=False)
    import_case_list = subparsers.add_parser('import-case-list', parents=[parent_parser], add_help=False)
    remove_study = subparsers.add_parser('remove-study', parents=[parent_parser], add_help=False)
    
    remove_study.add_argument('-id', '--study_ids', type=str, required=False,
                        help='Cancer Study IDs for `remove-study` command, comma separated')
    parser.add_argument('-c', '--command', type=str, required=False, 
                        help='This argument is outdated. Please use the listed subcommands, without the -c flag. '
                        'Command for import. Allowed commands: import-cancer-type, '
                        'import-study, import-study-data, import-case-list or '
                        'remove-study')
    add_parser_args(parser)
    parser.add_argument('-id', '--study_ids', type=str, required=False,
                        help='Cancer Study IDs for `remove-study` command, comma separated')
    
    parser.add_argument('-update', '--update_generic_assay_entity', type=str, required=False,
                        help='Set as True to update the existing generic assay entities, set as False to keep the existing generic assay entities for generic assay')
    # TODO - add same argument to metaimporter
    # TODO - harmonize on - and _

    parser = parser.parse_args()
    if parser.command is not None and parser.subcommand is not None:
        print('Cannot call multiple commands')
        sys.exit(2)
    elif parser.subcommand is not None:
        parser.command = parser.subcommand
    return parser


def locate_jar():
    """Locate the scripts jar file relative to this script.

    Throws a FileNotFoundError with a message if the jar file couldn't be
    identified.
    """
    # get the directory name of the currently running script,
    # resolving any symlinks
    script_dir = Path(__file__).resolve().parent
    # go up from cbioportal/core/src/main/scripts/importer/ to cbioportal/
    src_root = script_dir.parent.parent.parent.parent.parent
    jars = list((src_root / 'scripts' / 'target').glob('scripts-*.jar'))
    if len(jars) != 1:
        raise FileNotFoundError(
            'Expected to find 1 scripts-*.jar, but found ' + str(len(jars)))
    return str(jars[0])


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

    # jar_path is optional. If not set, try to find it relative to this script
    if args.jar_path is None:
        try:
            args.jar_path = locate_jar()
        except FileNotFoundError as e:
            print(e)
            sys.exit(2)
        print('Data loading step using', args.jar_path)
        print()

    # process the options
    jvm_args = "-Dspring.profiles.active=dbcp -cp " + args.jar_path
    study_directory = args.study_directory

    # check if DB version and application version are in sync
    check_version(jvm_args)

    if study_directory != None:
        check_dir(study_directory)
        process_directory(jvm_args, study_directory, args.update_generic_assay_entity)
    else:
        check_args(args.command)
        check_files(args.meta_filename, args.data_filename)
        process_command(jvm_args, args.command, args.meta_filename, args.data_filename, args.study_ids, args.update_generic_assay_entity)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    parsed_args = interface()
    main(parsed_args)
