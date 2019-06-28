#!/usr/bin/env python3

__author__ = 'priti'

# ----------------------------------------------------------------------------
# Import
# ----------------------------------------------------------------------------


import sys
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
    parser.add_argument('-s', '--study_directory', type=str, required=True,
                        help='path to directory.')
    portal_mode_group = parser.add_mutually_exclusive_group()
    portal_mode_group.add_argument('-u', '--url_server',
                                   type=str,
                                   default='http://localhost/cbioportal',
                                   help='URL to cBioPortal server. You can '
                                        'set this if your URL is not '
                                        'http://localhost/cbioportal')
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
    parser.add_argument('-species', '--species', type=str, default='human',
                        help='species information (default: assumed human)',
                        required=False)
    parser.add_argument('-ucsc', '--ucsc_build_name', type=str, default='hg19',
                        help='UCSC reference genome assembly name (default: assumed hg19)',
                        required=False)   
    parser.add_argument('-ncbi', '--ncbi_build_number', type=str, default='37',
                         help='NCBI reference genome build number (default: assumed 37 for UCSC reference genome build hg19)',
                         required=False)                                                                  
    parser.add_argument('-jar', '--jar_path', type=str, required=False,
                        help=(
                            'Path to scripts JAR file (default: locate it '
                            'relative to the import script)'))
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
    parser.add_argument('-a', '--max_reported_values', required=False,
                        type=int, default=3,
                        help='Cutoff in report for the maximum number of line numbers '
                             'and values encountered to report for each message in the HTML '
                             'report. For example, set this to a high number to '
                             'report all genes that could not be loaded, instead '
                             'of reporting "(GeneA, GeneB, GeneC, 213 more)".')
    parser = parser.parse_args()
    return parser


# ----------------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------------

if __name__ == '__main__':
    # Parse user input
    args = interface()
    # supply parameters that the validation script expects to have parsed
    args.error_file = False

    study_dir = args.study_directory

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

    # Depending on validation results, load the study or notify the user
    try:
        print("\n")
        print("#" * 71, file=sys.stderr)
        if exitcode == 1:
            print(Color.RED + "One or more errors reported above. Please fix your files accordingly" + Color.END, file=sys.stderr)
            print("!" * 71, file=sys.stderr)
        elif exitcode == 3:
            if args.override_warning:
                print(Color.BOLD + "Overriding Warnings. Importing study now" + Color.END, file=sys.stderr)
                print("#" * 71 + "\n", file=sys.stderr)
                cbioportalImporter.main(args)
                exitcode = 0
            else:
                print(Color.BOLD + "Warnings. Please fix your files or import with override warning option" + Color.END, file=sys.stderr)
                print("#" * 71, file=sys.stderr)
        elif exitcode == 0:
            print(Color.BOLD + "Everything looks good. Importing study now" + Color.END, file=sys.stderr)
            print("#" * 71 + "\n", file=sys.stderr)
            cbioportalImporter.main(args)
    except KeyboardInterrupt:
        print(Color.BOLD + "\nProcess interrupted. You will have to run this again to make sure study is completely loaded." + Color.END, file=sys.stderr)
        print("#" * 71, file=sys.stderr)
        raise
    except:
        print("!" * 71, file=sys.stderr)
        print(Color.RED + "Error occurred during data loading step. Please fix the problem and run this again to make sure study is completely loaded." + Color.END, file=sys.stderr)
        raise
    sys.exit(exitcode)
