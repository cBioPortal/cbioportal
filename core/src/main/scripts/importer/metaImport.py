#!/usr/bin/env python2.7

__author__ = 'priti'

# ----------------------------------------------------------------------------
# Import
# ----------------------------------------------------------------------------


import sys
import argparse
import logging

import validateData
import cbioportalImporter


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
    parser.add_argument('-jar', '--jar_path', type=str, required=True,
                        help='path to core jar file.')
    parser.add_argument('-html', '--html_table', type=str,
                        help='path to html report')
    parser.add_argument('-v', '--verbose', action='store_true',
                        help='report status info messages while validating')
    parser.add_argument('-o', '--override_warning', action='store_true',
                        help='override warnings and continue importing')

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
    args.no_portal_checks = False

    study_dir = args.study_directory

    # Validate the study directory.
    print >> sys.stderr, "Starting validation...\n"
    try:
        exitcode = validateData.main_validate(args)
    finally:
        # make sure all log messages are flushed
        validator_logger = logging.getLogger(validateData.__name__)
        for log_handler in validator_logger.handlers:
            log_handler.close()
        validator_logger.handlers = []

    # Depending on validation results, load the study or notify the user
    print >> sys.stderr, "#" * 71
    if exitcode == 1:
        print >> sys.stderr, Color.BOLD + "Errors. Please fix your files before importing" + Color.END
        print >> sys.stderr, "#" * 71 + "\n\n"
    elif exitcode == 3:
        if args.override_warning:
            print >> sys.stderr, Color.BOLD + "Overriding Warnings. Importing study now" + Color.END
            print >> sys.stderr, "#" * 71 + "\n\n"
            cbioportalImporter.main(args)
        else:
            print >> sys.stderr, Color.BOLD + "Warnings. Please fix your files or import with override warning option" + Color.END
            print >> sys.stderr, "#" * 71 + "\n\n"
    elif exitcode == 0:
        print >> sys.stderr, Color.BOLD + "Everything looks good. Importing study now" + Color.END
        print >> sys.stderr, "#" * 71 + "\n\n"
        cbioportalImporter.main(args)
