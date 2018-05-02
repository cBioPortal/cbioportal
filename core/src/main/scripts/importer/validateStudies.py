#!/usr/bin/env python2.7

"""
Copyright (c) 2018 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
"""

import argparse
import os
import logging
import traceback
import tempfile
import datetime
import sys

import validateData


def main(args):
    """Process arguments and run validation"""

    # Set variables for directory and list of studies
    root_dir = args.root_directory
    studies = args.list_of_studies

    # If studies are filled in, create a list
    if studies is not None:
        list_studies = [str(study) for study in studies.split(',')]

    # Get all studies in root directory if no list of studies is defined
    if root_dir is not None and studies is None:
        list_studies = []
        for study_dir in os.listdir(root_dir):
            if os.path.isdir(os.path.join(root_dir, study_dir)):
                list_studies.append(os.path.join(root_dir, study_dir))

    # When both root directory and list of studies are given, create list of study files
    if root_dir is not None and studies is not None:
        list_studies = [os.path.join(root_dir, study) for study in list_studies]

    # Get current date and time to write to logfilename
    d_date = datetime.datetime.now()
    reg_format_date = d_date.strftime("%Y-%m-%d_%H:%M")
    only_logfilename = 'log-validate-studies-' + reg_format_date + '.txt'

    # When html folder is given as input, check if html folder exists, otherwise create folder
    # Also, when html folder is defined the logfile is written to this folder as well, otherwise to /tmp
    output_folder = args.html_folder
    if output_folder is not None:
        if not os.path.exists(output_folder):
            print "HTML output folder did not exist, so is created: %s" % output_folder
            os.makedirs(output_folder)
        logfilename = os.path.join(output_folder, only_logfilename)
    else:
        # Get systems temp directory and write log file
        logfilename = os.path.join(tempfile.gettempdir(), only_logfilename)

    # Inform user where logfile is written to
    print "\nLogfile is written to: %s" % logfilename

    # Set up logger, stdout from validation will be written to a log file
    logging.basicConfig(level=logging.DEBUG,
                        filename=logfilename,
                        filemode='w')
    console = logging.StreamHandler()
    console.setLevel(logging.CRITICAL)
    logging.getLogger('').addHandler(console)

    # Make dictionary of possible exit status from validateData and values that should be printed in the console
    # when these exit statuses occur
    possible_exit_status = {0: 'VALID', 1: 'INVALID', 2: 'INVALID (PROBLEMS OCCURRED)', 3: 'VALID (WITH WARNINGS)'}
    # This script can have two possible exit statuses 0 or 1. The validation exit status from this script
    # will become 1 when one of the studies fails validation. This will be when validateData has for at
    # least one of the studies validated exit status 1 or 2 or when another error occurred during the validation
    validation_exit_status = 0
    # Go through list of studies and run validation
    for study in list_studies:
        # Write to stdout and log file that we are validating this particular study
        print "\n=== Validating study %s" %study
        logging.info("Validation study: %s" %study)

        # Check which portal info variable is given as input, and set correctly in the arguments for validateData
        if args.portal_info_dir is not None:
            validator_args = ['--study_directory', study, '-p', args.portal_info_dir]
        elif args.no_portal_checks:
            validator_args = ['--study_directory', study, '-n']
        else:
            validator_args = ['--study_directory', study, '-u', args.url_server]

        # Append argument for portal properties file if given at input
        if args.portal_properties is not None:
            validator_args.append('-P')
            validator_args.append(args.portal_properties)
            
        # Append argument for strict mode when supplied by user
        if args.strict_mutation_checks is not False:
            validator_args.append('-m')

        # When HTML file is required, create html file name and add to arguments for validateData
        if output_folder is not None:
            try:
                html = ""
                # Look in meta_study file for study name to add to name of validation report
                with open(os.path.join(study, "meta_study.txt"), 'r') as meta_study:
                    for line in meta_study:
                        if 'cancer_study_identifier' in line:
                            study_identifier = line.split(': ')[1].strip('\n').strip('\r')
                            html = study_identifier + "-validation.html"
                # If in the meta_study file no cancer_study_identifier could be found append study name from input
                if html == "":
                    only_study = study.split("/")[-1]
                    html = only_study + "-validation.html"

                # Add path to HTML file name and add to arguments for validateData
                html_file = os.path.join(output_folder, html)
                validator_args.append('--html')
                validator_args.append(html_file)
                
            # Exception can be thrown when the supplied study folder does not exist, when there is no meta_study
            # file or when there is no cancer_study_identifier and it will not create the HTML file at all.
            except:
                var_traceback = traceback.format_exc()
                print '\x1b[31m' + "Error occurred during creating html file name:"
                print var_traceback
                print "Validation from study " + study + " will not be written to HTML file" + '\x1b[0m'

        # Try to run validateData with certain study and return exit status
        try:
            validator_parsed_args = validateData.interface(validator_args)
            exit_status_study = validateData.main_validate(validator_parsed_args)
            # Reset logger handlers to suppress stdout output, will be written to log file
            validator_logger = logging.getLogger(validateData.__name__)
            validator_logger.handlers = []
            # Check exit status and print result
            if exit_status_study == 1 or exit_status_study == 2:
                print '\x1b[0m' + "Result: " + '\x1b[31m' + possible_exit_status[exit_status_study] + '\x1b[0m'
                validation_exit_status = 1  # When invalid check the exit status to one, for failing circleCI
            else:
                print '\x1b[0m' + "Result: %s" % possible_exit_status[exit_status_study]
        except:
            # When an error occurred in validateData print the python error to stdout
            var_traceback = traceback.format_exc()
            print '\x1b[31m' + "Error occured during validation:"
            print var_traceback + '\x1b[0m'
            validation_exit_status = 1  # Here also set invalid validation exit status for failing circleCI
            # Reset logger handlers to suppress stdout output, will be written to log file
            validator_logger = logging.getLogger(validateData.__name__)
            validator_logger.handlers = []

    return validation_exit_status


def interface(args=None):
    """Parse arguments from input"""

    parser = argparse.ArgumentParser(description='Wrapper where cBioPortal study validator is run for multiple studies')
    parser.add_argument('-d', '--root-directory',
                        type=str, help='Path to directory with all studies that should be validated')
    parser.add_argument('-l', '--list-of-studies',
                        type=str, help='List with paths of studies which should be validated')
    parser.add_argument('-html', '--html-folder',
                        type=str, help='Path to folder for output HTML reports')
    # Only one of the portal arguments can be given, therefore set as mutually exclusive group
    portal_mode_group = parser.add_mutually_exclusive_group()
    portal_mode_group.add_argument('-u', '--url_server',
                                   type=str,
                                   default='http://localhost:8080/cbioportal',
                                   help='URL to cBioPortal server. You can '
                                        'set this if your URL is not '
                                        'http://localhost:8080/cbioportal')
    portal_mode_group.add_argument('-p', '--portal_info_dir',
                                   type=str,
                                   help='Path to a directory of cBioPortal '
                                        'info files to be used instead of '
                                        'contacting a server')
    portal_mode_group.add_argument('-n', '--no_portal_checks',
                                   action='store_true',
                                   help='Skip tests requiring information '
                                        'from the cBioPortal installation')
    parser.add_argument('-P', '--portal_properties', type=str,
                        help='portal.properties file path (default: assumed hg19)',
                        required=False)

    parser.add_argument('-m', '--strict_mutation_checks', required=False,
                        action='store_true', default=False,
                        help='Option to enable strict mode for validator when '
                             'validating mutation data')

    args = parser.parse_args(args)

    # Check if -d or -l was given as input, otherwise let the parser give an error and stop
    if not (args.root_directory or args.list_of_studies):
        parser.error('No action requested, add -d or -l')

    return args


if __name__ == '__main__':
    parsed_args = interface()
    exit_status = main(parsed_args)

    print "\n\nOverall exit status: %s" %exit_status
    sys.exit(exit_status)

