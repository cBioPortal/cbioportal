#!/usr/bin/env python3

__author__ = 'priti'

# ----------------------------------------------------------------------------
# Import
# ----------------------------------------------------------------------------


import sys
import os
import importlib
import argparse
import logging
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

from . import validateData
from . import cbioportalImporter
from . import importOncokbMutation
from . import importOncokbDiscreteCNA
from . import libImportOncokb
from . import rebuild_derived_tables


# ----------------------------------------------------------------------------
# Global variables
# ----------------------------------------------------------------------------

class Color(object):
    PURPLE = '\033[95m'
    CYAN = '\033[96m'
    DARKCYAN = '\033[36m'
    BLUE = '\033[94m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'
    END = '\033[0m'


# ----------------------------------------------------------------------------
# Functions
# ----------------------------------------------------------------------------

def interface():
    parser = argparse.ArgumentParser(description='cBioPortal meta Importer')
    data_source_group = parser.add_mutually_exclusive_group()
    data_source_group.add_argument('-s', '--study_directory',
                                   type=str, help='path to study directory.')
    data_source_group.add_argument('-d', '--data_directory',
                                   type=str, help='path to data directory for incremental upload.')
    portal_mode_group = parser.add_mutually_exclusive_group()
    portal_mode_group.add_argument('-u', '--url_server',
                                   type=str,
                                   default='http://localhost:8080',
                                   help='URL to cBioPortal server. You can '
                                        'set this if your URL is not '
                                        'http://localhost:8080')
    portal_mode_group.add_argument('-p', '--portal_info_dir',
                                   type=str,
                                   help='Path to a directory of cBioPortal '
                                        'info files to be used instead of '
                                        'contacting the web API')
    #  temporary workaround to simplify import process when no web-server is running. TODO: replace by solution for #1466
    portal_mode_group.add_argument('-n', '--no_portal_checks', default=False,
                                       action='store_true',
                                       help='Skip tests requiring information '
                                            'from the cBioPortal installation')
    parser.add_argument('-jvo', '--java_opts', type=str, default=os.environ.get('JAVA_OPTS'),
                        help='Path to specify JAVA_OPTS for the importer. \
                        (default: gets the JAVA_OPTS from the environment)')
    parser.add_argument('-jar', '--jar_path', type=str, required=False,
                        help='Path to scripts JAR file (default: locate it '
                             'relative to the import script)')
    parser.add_argument('-html', '--html_table', type=str,
                        help='path to html report')
    parser.add_argument('-v', '--verbose', action='store_true',
                        help='report status info messages while validating')
    parser.add_argument('-o', '--override_warning', action='store_true',
                        help='override warnings and continue importing')
    parser.add_argument('-r', '--relaxed_clinical_definitions', required=False,
                        action='store_true', default=False,
                        help='Option to enable relaxed mode for validator when '
                             'validating clinical data without header definitions')
    parser.add_argument('-m', '--strict_maf_checks', required=False,
                        action='store_true', default=False,
                        help='Option to enable strict mode for validator when '
                             'validating mutation data')
    parser.add_argument('-update', '--update_generic_assay_entity', type=str, required=False, default="False",
                        help='Set as True to update the existing generic assay entities, set as False to keep the existing generic assay entities for generic assay')
    parser.add_argument('-oncokb', '--import_oncokb', action='store_true',
                        help='Set as True to download OncoKB annotations for Mutations and CNA and load as custom driver annotations')
    parser.add_argument('-skipimport', '--skip_db_import', action='store_true',
                        help='Perform validation and OncoKB download but do not import study into database.')
    derive_tables_group = parser.add_mutually_exclusive_group()
    derive_tables_group.add_argument('--no-derive-tables', action='store_true',
                        help='Skip derived table construction after import.')
    derive_tables_group.add_argument('--derived-table-sql', type=str, metavar='PATH',
                        help='Path to SQL file used for derived table construction.')
    parser = parser.parse_args()
    return parser


# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

def _print_need_to_update_derived_tables_warning():
    print(
        Color.BOLD +
        'The database has been altered. It is now necessary to reconstitute\n'
        'the derived tables before using the database with the cBioPortal\n'
        'web application. Run:\n'
        '    metaImport.py derive-tables\n' +
        Color.END,
        file=sys.stderr,
    )

def _print_need_to_make_study_available_message(study_id):
    print(
        Color.BOLD +
        'The study stays UNAVAILABLE until the derived tables are rebuilt. Then run:\n'
        '    cbioportalImporter.py make-studies-available -ids ' + study_id + '\n' +
        Color.END,
        file=sys.stderr,
    )

if __name__ == '__main__':
    derive_tables_only = len(sys.argv) > 1 and sys.argv[1] == 'derive-tables'
    if derive_tables_only:
        sys.argv.pop(1)

    # Parse user input
    args = interface()

    if derive_tables_only:
        sys.exit(0 if rebuild_derived_tables.rebuild_derived_tables(args.derived_table_sql) else 1)
    # supply parameters that the validation script expects to have parsed
    args.error_file = False

    data_dir = args.data_directory if args.data_directory is not None else args.study_directory

    # Validate the study directory.
    print("Starting validation...\n", file=sys.stderr)
    try:
        exitcode = validateData.main_validate(args)
    except KeyboardInterrupt:
        print(Color.BOLD + "\nProcess interrupted. " + Color.END, file=sys.stderr)
        print("#" * 71 + "\n", file=sys.stderr)
        raise
    except:
        print("!" * 71, file=sys.stderr)
        print(Color.RED + "Error occurred during validation step:" + Color.END, file=sys.stderr)
        raise
    finally:
        # make sure all log messages are flushed
        validator_logger = logging.getLogger(validateData.__name__)
        for log_handler in validator_logger.handlers:
            log_handler.close()
        validator_logger.handlers = []

    # Import OncoKB annotations when asked, and there are no validation warnings or warnings are overruled
    study_is_valid = exitcode == 0 or (exitcode == 3 and args.override_warning)
    if study_is_valid and args.import_oncokb:
        mutation_meta_file_path = libImportOncokb.find_meta_file_by_fields(data_dir, {'genetic_alteration_type': 'MUTATION_EXTENDED'})
        mutation_data_file_name = libImportOncokb.find_data_file_from_meta_file(mutation_meta_file_path)
        mutation_data_file_path = os.path.join(data_dir, mutation_data_file_name)
        study_is_modified = False
        print("\n")
        if os.path.exists(mutation_data_file_path):
            print("Starting import of OncoKB annotations for mutations file ...\n", file=sys.stderr)
            try:
                exitcode = importOncokbMutation.main_import(args)
                study_is_modified = True
            except KeyboardInterrupt:
                print(Color.BOLD + "\nProcess interrupted. " + Color.END, file=sys.stderr)
                print("#" * 71 + "\n", file=sys.stderr)
                raise
            except:
                print("!" * 71, file=sys.stderr)
                print(Color.RED + "Error occurred during import of OncoKB for mutations file:" + Color.END, file=sys.stderr)
                raise
            finally:
                # make sure all log messages are flushed
                validator_logger = logging.getLogger(importOncokbMutation.__name__)
                for log_handler in validator_logger.handlers:
                    log_handler.close()
                validator_logger.handlers = []
        cna_meta_file_path = libImportOncokb.find_meta_file_by_fields(data_dir, {'genetic_alteration_type': 'COPY_NUMBER_ALTERATION', 'datatype': 'DISCRETE'})
        cna_data_file_name = libImportOncokb.find_data_file_from_meta_file(cna_meta_file_path)
        cna_data_file_path = os.path.join(data_dir, cna_data_file_name)
        if os.path.exists(cna_data_file_path):
            print("Starting import of OncoKB annotations for discrete CNA file ...\n", file=sys.stderr)
            try:
                exitcode = importOncokbDiscreteCNA.main_import(args)
                study_is_modified = True
            except KeyboardInterrupt:
                print(Color.BOLD + "\nProcess interrupted. " + Color.END, file=sys.stderr)
                print("#" * 71 + "\n", file=sys.stderr)
                raise
            except:
                print("!" * 71, file=sys.stderr)
                print(Color.RED + "Error occurred during import of OncoKB for discrete CNA file:" + Color.END, file=sys.stderr)
                raise
            finally:
                # make sure all log messages are flushed
                validator_logger = logging.getLogger(importOncokbDiscreteCNA.__name__)
                for log_handler in validator_logger.handlers:
                    log_handler.close()
                validator_logger.handlers = []
        # Revalidate when custom annotations were added
        if study_is_modified:
            print("Starting re-validation of study with OncoKB annotations ...\n", file=sys.stderr)
            exitcode = validateData.main_validate(args)

    # Depending on validation results, load the study or notify the user
    try:
        print("\n")
        print("#" * 71, file=sys.stderr)
        if exitcode == 1:
            print(Color.RED + "One or more errors reported above. Please fix your files accordingly" + Color.END, file=sys.stderr)
            print("!" * 71, file=sys.stderr)
        elif exitcode == 3:
            if args.override_warning and not args.skip_db_import:
                print(Color.BOLD + "Overriding Warnings. Importing study now" + Color.END, file=sys.stderr)
                print("#" * 71 + "\n", file=sys.stderr)
                study_id = cbioportalImporter.main(args)
                exitcode = 0
                # Rebuild derived tables after database-mutating operation
                if not getattr(args, 'no_derive_tables', False):
                    print("\n")
                    print("#" * 71, file=sys.stderr)
                    print(Color.BOLD +
                          "Rebuilding ClickHouse derived tables..." +
                          Color.END, file=sys.stderr)
                    if not rebuild_derived_tables.rebuild_derived_tables(args.derived_table_sql):
                        print(Color.RED +
                              "Derived table construction failed. "
                              "The database may be in an inconsistent state." +
                              Color.END, file=sys.stderr)
                        exitcode = 1
                        _print_need_to_make_study_available_message(study_id)
                    else:
                        cbioportalImporter.make_study_available(args, study_id)
                else:
                    _print_need_to_update_derived_tables_warning()
                    _print_need_to_make_study_available_message(study_id)
            else:
                print(Color.BOLD + "Warnings. Please fix your files or import with override warning option" + Color.END, file=sys.stderr)
                print("#" * 71, file=sys.stderr)
        elif exitcode == 0 and not args.skip_db_import:
            print(Color.BOLD + "Everything looks good. Importing study now" + Color.END, file=sys.stderr)
            print("#" * 71 + "\n", file=sys.stderr)
            study_id = cbioportalImporter.main(args)
            # Rebuild derived tables after database-mutating operation
            if not getattr(args, 'no_derive_tables', False):
                print("\n")
                print("#" * 71, file=sys.stderr)
                print(Color.BOLD +
                      "Rebuilding ClickHouse derived tables..." +
                      Color.END, file=sys.stderr)
                if not rebuild_derived_tables.rebuild_derived_tables(args.derived_table_sql):
                    print(Color.RED +
                          "Derived table construction failed. "
                          "The database may be in an inconsistent state." +
                          Color.END, file=sys.stderr)
                    exitcode = 1
                    _print_need_to_make_study_available_message(study_id)
                else:
                    cbioportalImporter.make_study_available(args, study_id)
            else:
                _print_need_to_update_derived_tables_warning()
                _print_need_to_make_study_available_message(study_id)
    except KeyboardInterrupt:
        print(Color.BOLD + "\nProcess interrupted. You will have to run this again to make sure study is completely loaded." + Color.END, file=sys.stderr)
        print("#" * 71, file=sys.stderr)
        raise
    except:
        print("!" * 71, file=sys.stderr)
        print(Color.RED + "Error occurred during data loading step. Please fix the problem and run this again to make sure study is completely loaded." + Color.END, file=sys.stderr)
        raise
    sys.exit(exitcode)
