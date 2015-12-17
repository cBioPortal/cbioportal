__author__ = 'priti'
#======================================================================================
#Import
#======================================================================================

from import_data_validator import validateData
import cbioportalImporter
import sys
import argparse
import os
import subprocess


#======================================================================================
#Global variables
#======================================================================================


class color:
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

#======================================================================================
#Functions
#======================================================================================


def checkWarningFile(warningfile):
    status = '0'
    fh = open(warningfile, 'rU')
    for line in fh:
        status = line.strip()
    return status


def interface():
    parser = argparse.ArgumentParser(description='cBioPortal meta Importer')
    parser.add_argument('-s', '--study_directory', type=str, required=True,
                        help='path to directory.')
    parser.add_argument('-hugo', '--hugo_entrez_map',type=str, required=True,
                        help='Path to Hugo gene Symbol')
    parser.add_argument('-html', '--html_table',type=str, required=False,
                        help='Path to html report')
    parser.add_argument('-html_simple', '--html_simple_table',type=str, required=False,
                        help='Path to html report')
    parser.add_argument('-v', '--validate', required=True,action="store_true",
                        help='Validate')
    parser.add_argument('-f', '--fix', required=False,action="store_true",
                        help='Fix files')
    parser.add_argument('-o', '--override_warning',type=int, required=False,default=0,
                        help='Fix files')
    parser.add_argument('-c', '--command', type=str, required=True,
                        help='Command for import.')
    parser.add_argument('-jvm', '--jvm_args',type=str, required=True,
                        help='JVM arguments')
    parser.add_argument('-meta', '--meta_filename',type=str, required=False,
                        help='Path to meta file')
    parser.add_argument('-data', '--data_filename', type=str, required=False,
                        help='Path to Data file')

    parser = parser.parse_args()
    return parser

#======================================================================================
#Main
#======================================================================================


if __name__ == '__main__':
    ####Parse user input
    args = interface()
    study_dir = args.study_directory

    ###Validate the study directory.
    print >> sys.stderr ,'Starting Validation....\n\n\n'
    exitcode = validateData.main_validate(args)

    print >> sys.stderr , "#######################################################################"

    if exitcode == 1:
        print >> sys.stderr , color.BOLD + "Errors. Please fix your files before importing" + color.END
        print >> sys.stderr, "#######################################################################\n\n"
    elif exitcode == 3:
        if args.override_warning:
            print >> sys.stderr ,  color.BOLD + "Overriding Warnings. Importing Files now" + color.END
            print >> sys.stderr, "#######################################################################\n\n"
            cbioportalImporter.main(args)
        else:
            print >> sys.stderr, color.BOLD + "Warnings. Please fix your files or import with override warning option" + color.END
            print >> sys.stderr, "#######################################################################\n\n"


    elif exitcode == 0:
        print  >> sys.stderr, color.BOLD + "Everything looks good. Study importing now" + color.END
        print >> sys.stderr, "#######################################################################\n\n"
        cbioportalImporter.main(args)


