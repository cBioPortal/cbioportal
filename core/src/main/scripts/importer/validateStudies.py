#!/usr/bin/env python3

"""
Copyright (c) 2018 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
"""

import argparse
import os
import traceback
import tempfile
import datetime
import sys
import subprocess

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
            print("HTML output folder did not exist, so is created: %s" % output_folder)
            os.makedirs(output_folder)
        logfilename = os.path.join(output_folder, only_logfilename)
    else:
        # Get systems temp directory and write log file
        logfilename = os.path.join(tempfile.gettempdir(), only_logfilename)
    print('\nWriting validation logs to: {}'.format(logfilename))

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
        print("\n=== Validating study %s" %study)

        # Check which portal info variable is given as input, and set correctly in the arguments for validateData
        if args.portal_info_dir is not None:
            validator_args = ['-v', '--study_directory', study, '-p', args.portal_info_dir]
        elif args.no_portal_checks:
            validator_args = ['-v', '--study_directory', study, '-n']
        else:
            validator_args = ['-v', '--study_directory', study, '-u', args.url_server]

        # Append argument for portal properties file if given at input
        if args.portal_properties is not None:
            validator_args.append('-P')
            validator_args.append(args.portal_properties)
            
        # Append argument for strict mode when supplied by user
        if args.strict_maf_checks is not False:
            validator_args.append('-m')

        # Append argument for maximum reported line numbers and encountered values in HTML
        if args.max_reported_values != 3:
            validator_args.append('-a %s' % args.max_reported_values)

        # When HTML file is required, create html file name and add to arguments for validateData
        if output_folder is not None:
            try:
                html = ""
                # Look in meta_study file for study name to add to name of validation report
                with open(os.path.join(study, "meta_study.txt"), 'r') as meta_study:
                    for line in meta_study:
                        if 'cancer_study_identifier' in line:
                            study_identifier = line.split(':')[1].strip()
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
                print('\x1b[31m' + "Error occurred during creating html file name:")
                print(var_traceback)
                print("Validation from study " + study + " will not be written to HTML file" + '\x1b[0m')

        # Get the path to the validator , in the same directory as this script
        validation_script = os.path.join(
            os.path.dirname(os.path.realpath(__file__)),
            'validateData.py')
        # Run validateData on this study and get the exit status
        try:
            with open(logfilename, 'a') as log_file:
                log_file.write('=== Validation study: {}\n'.format(study))
                log_file.flush()
                exit_status_study = subprocess.call(
                    [sys.executable, '--', validation_script] + validator_args,
                    stdout=log_file)
        # If opening the log file or executing the script failed,
        except OSError:
            # Output the Python stack trace for the error
            traceback.print_exc(file=sys.stdout)
            # And mark this run as not validated
            exit_status_study = 2

        # Check exit status and print result
        exit_status_message = possible_exit_status.get(exit_status_study, 'Unknown status: {}'.format(exit_status_study))
        if exit_status_study == 0 or exit_status_study == 3:
            print('\x1b[0m' + "Result: %s" % exit_status_message)
        else:
            print('\x1b[0m' + "Result: " + '\x1b[31m' + exit_status_message + '\x1b[0m')
            validation_exit_status = 1  # When invalid check the exit status to one, for failing circleCI

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

    parser.add_argument('-m', '--strict_maf_checks', required=False,
                        action='store_true', default=False,
                        help='Option to enable strict mode for validator when '
                             'validating mutation data')
    parser.add_argument('-a', '--max_reported_values', required=False,
                        type=int, default = 3,
                        help='Cutoff in HTML report for the maximum number of line numbers '
                             'and values encountered to report for each message. '
                             'For example, set this to a high number to '
                             'report all genes that could not be loaded, instead '
                             'of reporting "GeneA, GeneB, GeneC, 213 more"')

    args = parser.parse_args(args)

    # Check if -d or -l was given as input, otherwise let the parser give an error and stop
    if not (args.root_directory or args.list_of_studies):
        parser.error('No action requested, add -d or -l')

    return args


if __name__ == '__main__':
    parsed_args = interface()
    exit_status = main(parsed_args)

    print("\n\nOverall exit status: %s" %exit_status)
    sys.exit(exit_status)

