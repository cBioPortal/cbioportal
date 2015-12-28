__author__ = 'priti'
#======================================================================================
#Import
#======================================================================================

from import_data_validator import validateData
import cbioportalImporter
import sys
import argparse


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
    parser.add_argument('-hugo', '--hugo_entrez_map', type=str, required=True,
                        help='path to Hugo gene Symbol')
    parser.add_argument('-html', '--html_table', type=str, required=False,
                        help='path to html report')
    parser.add_argument('-v', '--verbose', required=False, action="store_true",
                        help='list warnings in addition to fatal errors')
    parser.add_argument('-o', '--override_warning', type=int, required=False, default=0,
                        help='override warnings and continue importing')
    parser.add_argument('-jar', '--jar_path', type=str, required=True,
                        help='path to core jar file.')


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


