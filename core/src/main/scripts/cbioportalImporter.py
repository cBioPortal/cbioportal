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
    if metafile_properties.meta_file_type != '':
        importer = IMPORTER_CLASSNAME_BY_ALTERATION_TYPE[metafile_properties.meta_file_type]
    elif metafile_properties.genetic_alteration_type != '':
        importer = IMPORTER_CLASSNAME_BY_ALTERATION_TYPE[metafile_properties.genetic_alteration_type]
    else:
        print >> ERROR_FILE, 'Missing meta_file_type in metafile: ' + meta_filename
        return

    args.append(importer)
    if IMPORTER_REQUIRES_METADATA[importer]:
        args.append("--meta")
        args.append(meta_filename)
        args.append("--loadMode")
        args.append("bulkload")
    if metafile_properties.genetic_alteration_type == 'CLINICAL' or metafile_properties.meta_file_type == 'meta_clinical':
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

def process_case_lists(jvm_args, study_files):
    for f in study_files:
        if 'case_lists' in f:
            if os.path.isdir(f):
                case_list_files = [f + '/' + x for x in os.listdir(f)]
                for case_list in case_list_files:
                    import_case_list(jvm_args,case_list)

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

def process_directory(jvm_args, command, study_directory):
    study_files = [study_directory + '/' + x for x in os.listdir(study_directory)]
    meta_study_filename = ''
    study_meta = {}
    clinical_metafiles = []
    non_clinical_metafiles = []

    for f in study_files:
        if 'meta_' in f:
            metadata = get_properties(f)
            if 'meta_study' in metadata.get('meta_file_type'):
                study_meta = metadata
                meta_study_filename = f
                import_study(jvm_args,f)
            elif 'meta_cancer_type' in metadata.get('meta_file_type'):
                cancer_type_meta = metadata
            elif 'meta_clinical' in metadata.get('meta_file_type'):
                clinical_metafiles.append(f)
            else:
                non_clinical_metafiles.append(f)


    if len(study_meta) == 0:
        print >> ERROR_FILE, 'No meta_study file found'
        sys.exit(1)

    if command == REMOVE_STUDY:
        remove_study(jvm_args, meta_study_filename)
        return

    # First, import cancer type
    import_cancer_type(jvm_args, cancer_type_meta.get('data_file_path'))

    # Next, we need to import clinical files
    for f in clinical_metafiles:
        metadata = get_properties(f)
        import_study_data(jvm_args, f, os.path.join(study_directory,metadata.get('data_file_path')))

    # Now, import everything else
    for f in non_clinical_metafiles:
        metadata = get_properties(f)
        import_study_data(jvm_args, f, os.path.join(study_directory,metadata.get('data_file_path')))

    # do the case lists
    process_case_lists(jvm_args,study_files)



def usage():
    print >> OUTPUT_FILE, ('cbioportalImporter.py --jvm-args (args to jvm) ' +
							'--command [%s] --study-directory <path to directory> --meta-filename <path to metafile> --data-filename <path to datafile>' % (COMMANDS))

def check_args(command, jvm_args, study_directory, meta_filename, data_filename):
    if jvm_args == '' or command not in COMMANDS or (study_directory == '' and data_filename == '') or (study_directory != '' and data_filename != '') or (study_directory != '' and command != 'import-study' and command != 'remove-study'):
        usage()
        sys.exit(2)

def check_files(study_directory, meta_filename, data_filename):
    # check existence of directory
    if not os.path.exists(study_directory) and study_directory != '':
        print >> ERROR_FILE, 'Study cannot be found: ' + study_directory
        sys.exit(2)
    if len(meta_filename) > 0 and not os.path.exists(meta_filename):
        print >> ERROR_FILE, 'meta-file cannot be found: ' + meta_filename
        sys.exit(2)
    if len(data_filename) > 0 and not os.path.exists(data_filename):
        print >> ERROR_FILE, 'data-file cannot be found:' + data_filename

def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['command=', 'jvm-args=', 'study-directory=', 'meta-filename=', 'data-filename='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

    # process the options
    jvm_args = ''
    study_directory = ''
    command = ''
    meta_filename = ''
    data_filename = ''

    for o, a in opts:
        if o == '--jvm-args':
            jvm_args = a
        elif o == '--study-directory':
            study_directory = a
        elif o == '--command':
            command = a
        elif o == '--meta-filename':
            meta_filename = a
        elif o == '--data-filename':
            data_filename = a

    check_args(command, jvm_args, study_directory, meta_filename, data_filename)
    check_files(study_directory, meta_filename, data_filename)
    if study_directory != '':
        process_directory(jvm_args, command, study_directory)
    else:
        process_command(jvm_args, command, meta_filename, data_filename)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
