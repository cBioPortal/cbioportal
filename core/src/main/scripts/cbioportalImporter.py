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

COMMANDS = [IMPORT_STUDY, REMOVE_STUDY]


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


def create_cancer_type_file(study_meta, study_directory):
    # this needs to be better defined
    cancer_type_file = open(study_directory + '/' + 'cancer_type.txt','w')
    cancer_type_file.write(study_meta.get('type_of_cancer') + '\t' + study_meta.get('type_of_cancer') + '\t' + study_meta.get('type_of_cancer') + '\t' + study_meta.get('dedicated_color') + '\t' + study_meta.get('type_of_cancer'))
    cancer_type_file.close()

def process_case_lists(jvm_args,study_files):
    for f in study_files:
        if 'case_lists' in f:
            if os.path.isdir(f):
                case_list_files = [f + '/' + x for x in os.listdir(f)]
                for case_list in case_list_files:
                    import_case_list(jvm_args,case_list)

def process(jvm_args, study_directory, command):
    study_files = [study_directory + '/' + x for x in os.listdir(study_directory)]
    meta_filename = ''
    study_meta = {}

    for f in study_files:
        if 'meta' in f:
            metadata = get_properties(f);
            if 'meta_study' in metadata.values():
                study_meta = metadata
                meta_filename = f


    if len(study_meta) == 0:
        print >> ERROR_FILE, 'No meta_study file found'
        sys.exit(1)

    if command == REMOVE_STUDY:
        remove_study(jvm_args, meta_filename)
        return

    create_cancer_type_file(study_meta, study_directory)

    # First, import cancer type
    import_cancer_type(jvm_args,study_directory + '/' + 'cancer_type.txt')

    non_clinical_metafiles = []
    files_found = []

    # Next, we need to import clinical files
    for f in study_files:
        if 'meta' in f:
            metadata = get_properties(f)
            if metadata.get('meta_file_type') == 'meta_clinical':
                import_study_data(jvm_args, f, metadata.get('data_file_path'))
            else:
                non_clinical_metafiles.append(f)

    # Now, import everything else
    for f in non_clinical_metafiles:
        meta_file = open(f, 'r')
        metadata = get_properties(f)
        files_found.append
        import_study_data(jvm_args, f, metadata.get('data_file_path'))

    # do the case lists
    process_case_lists(jvm_args,study_files)



def usage():
    print >> OUTPUT_FILE, ('cbioportalImporter.py --jvm-args (args to jvm) ' +
							'--command [%s] --study-directory <path to directory> ' % (COMMANDS))

def check_args(command, jvm_args, study_directory):
    if (jvm_args == '' or command not in COMMANDS or study_directory == ''):
        usage()
        sys.exit(2)

def check_files(study_directory):
    # check existence of directory
    if not os.path.exists(study_directory):
        print >> ERROR_FILE, 'Study cannot be found: ' + study_directory
        sys.exit(2)

def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['command=', 'jvm-args=','study-directory='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

    # process the options
    jvm_args = ''
    study_directory = ''
    command = ''

    for o, a in opts:
        if o == '--jvm-args':
            jvm_args = a
        elif o == '--study-directory':
            study_directory = a
        elif o == '--command':
            command = a

    check_args(command, jvm_args, study_directory)
    check_files(study_directory)
    process(jvm_args, study_directory, command)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
