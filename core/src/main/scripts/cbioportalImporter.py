#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which imports portal data.
#
# ------------------------------------------------------------------------------

import os
import sys
import getopt
from cbioportal_common import *

# ------------------------------------------------------------------------------
# globals

# commands
IMPORT_CANCER_TYPE = "import-cancer-type"
IMPORT_STUDY = "import-study"
REMOVE_STUDY = "remove-study"
IMPORT_STUDY_DATA = "import-study-data"
IMPORT_CASE_LIST = "import-case-list"

COMMANDS = [IMPORT_CANCER_TYPE, IMPORT_STUDY, REMOVE_STUDY, IMPORT_STUDY_DATA, IMPORT_CASE_LIST]

# ------------------------------------------------------------------------------
# sub-routines

def import_cancer_type(jvm_args, meta_filename):
	args = jvm_args.split(' ')
	args.append(IMPORT_CANCER_TYPE_CLASS);
	args.append(meta_filename)
	args.append("false") # don't clobber existing table
	run_java(*args)

def import_study(jvm_args, meta_filename):
	args = jvm_args.split(' ')
	args.append(IMPORT_STUDY_CLASS);
	args.append(meta_filename)
	run_java(*args)

def remove_study(jvm_args, meta_filename):
	args = jvm_args.split(' ')
	args.append(REMOVE_STUDY_CLASS);
	metastudy_properties = get_metastudy_properties(meta_filename)
	args.append(metastudy_properties.cancer_study_identifier)
	run_java(*args)
	
def import_study_data(jvm_args, meta_filename, data_filename):
    args = jvm_args.split(' ')
    metafile_properties = get_metafile_properties(meta_filename)
    importer = IMPORTER_CLASSNAME_BY_ALTERATION_TYPE[metafile_properties.genetic_alteration_type]
    args.append(importer)
    if IMPORTER_REQUIRES_METADATA[importer]:
        args.append("--meta")
        args.append(meta_filename)
        args.append("--loadMode")
        args.append("bulkload")
    if metafile_properties.genetic_alteration_type == 'CLINICAL':
        args.append(data_filename)
        args.append(metafile_properties.cancer_study_identifier)
    else:
        args.append("--data")
        args.append(data_filename)
    run_java(*args)

def import_case_list(jvm_args, meta_filename):
	args = jvm_args.split(' ')
	args.append(IMPORT_CASE_LIST_CLASS);
	args.append(meta_filename)
	run_java(*args)

def process_command(jvm_args, command, meta_filename, data_filename):
	if command == IMPORT_CANCER_TYPE:
		import_cancer_type(jvm_args, meta_filename)
	elif command == IMPORT_STUDY:
		import_study(jvm_args, meta_filename)
	elif command == REMOVE_STUDY:
		remove_study(jvm_args, meta_filename)
	elif command == IMPORT_STUDY_DATA:
		import_study_data(jvm_args, meta_filename, data_filename)
	elif command == IMPORT_CASE_LIST:
		import_case_list(jvm_args, meta_filename)

def usage():
    print >> OUTPUT_FILE, ('cbioportalImporter.py --jvm-args (args to jvm) ' +
							'--command [%s] --meta-filename <path to metafile> ' +
							'--data-filename <path to datafile>' % (COMMANDS))

def check_args(command, jvm_args, meta_filename, data_filename):
    if (jvm_args == '' or command not in COMMANDS or meta_filename == ''):
        usage()
        sys.exit(2)
    if (command == IMPORT_STUDY_DATA and data_filename == ''):
        usage()
        sys.exit(2)

def check_files(meta_filename, data_filename):
    # check existence of file
    if len(meta_filename) > 0 and not os.path.exists(meta_filename):
        print >> ERROR_FILE, 'meta-file cannot be found: ' + meta_filename
        sys.exit(2)
    if len(data_filename) > 0 and not os.path.exists(data_filename):
        print >> ERROR_FILE, 'data-file cannot be found: ' + data_filename
        sys.exit(2)

def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['jvm-args=', 'command=', 'meta-filename=', 'data-filename='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

    # process the options
    command = ''
    jvm_args = ''
    meta_filename = ''
    data_filename = ''

    for o, a in opts:
        if o == '--jvm-args':
            jvm_args = a
        elif o == '--command':
            command = a
        elif o == '--meta-filename':
            meta_filename = a
        elif o == '--data-filename':
            data_filename = a

    check_args(command, jvm_args, meta_filename, data_filename)
    check_files(meta_filename, data_filename)
    process_command(jvm_args, command, meta_filename, data_filename)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
