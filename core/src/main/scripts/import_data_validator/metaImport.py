__author__ = 'priti'
#======================================================================================
#Import
#======================================================================================

import validateData
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

def run_import(study_dir):
    cmd = "$JAVA_HOME/bin/java  -ea -Dspring.profiles.active=dbcp -Djava.io.tmpdir=/data/tmp/ " \
          "-cp /data/zack/pipelines/importer/target/cbioportal-importer.jar org.mskcc.cbio.importer.Admin " \
          "-import_data_new " + study_dir
    #subprocess.call(cmd)
    #print (cmd)

def interface():
    parser = argparse.ArgumentParser(description='cBioPortal meta Importer')
    parser.add_argument('-d', '--directory', type=str, required=True,
                        help='path to directory.')
    parser.add_argument('-hugo', '--hugo_entrez_map',type=str, required=True,
                        help='Path to Hugo gene Symbol')
    parser.add_argument('-html', '--html_table',type=str, required=False,
                        help='Path to html report')
    parser.add_argument('-html_simple', '--html_simple_table',type=str, required=False,
                        help='Path to html report')
    parser.add_argument('-v', '--validate', required=True,action="store_true",
                        help='Validate')
    parser.add_argument('-c', '--fix', required=False,action="store_true",
                        help='Fix files')
    parser.add_argument('-o', '--override_warning',type=int, required=False,default=0,
                        help='Fix files')

    parser = parser.parse_args()
    return parser

#======================================================================================
#Main
#======================================================================================


if __name__ == '__main__':
    ####Parse user input
    args = interface()
    study_dir = args.directory

    ###Validate the study directory.
    print >> sys.stderr ,'Starting Validation....\n\n\n'
    exitcode = validateData.main_validate(args)

    print >> sys.stderr , "#######################################################################"

    if exitcode == 1:
        print >> sys.stderr , color.BOLD + "Errors. Please fix your files before importing" + color.END
    elif exitcode == 3:
        if args.override_warning:
            print >> sys.stderr ,  color.BOLD + "Overriding Warnings. Importing Files now" + color.END
            run_import(study_dir)
        else:
            print >> sys.stderr, color.BOLD + "Warnings. Please fix your files or import with override warning option" + color.END


    elif exitcode == 0:
        print  >> sys.stderr, color.BOLD + "Everything looks good. Study importing now" + color.END
        run_import(study_dir)

    print >> sys.stderr, "#######################################################################\n\n"
