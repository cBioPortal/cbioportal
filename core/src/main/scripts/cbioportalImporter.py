#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which imports portal data.
#
# ------------------------------------------------------------------------------

import os
import sys
import getopt
import argparse
import re
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
    args.append(IMPORT_CANCER_TYPE_CLASS)
    args.append(meta_filename)
    args.append("false") # don't clobber existing table
    run_java(*args)

def import_study(jvm_args, meta_filename):
    args = jvm_args.split(' ')
    args.append(IMPORT_STUDY_CLASS)
    args.append(meta_filename)
    run_java(*args)

def remove_study(jvm_args, meta_filename):
    args = jvm_args.split(' ')
    args.append(REMOVE_STUDY_CLASS)
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
    args.append(IMPORT_CASE_LIST_CLASS)
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

def process_directory(jvm_args, study_directory):
    study_files = [study_directory + '/' + x for x in os.listdir(study_directory)]
    study_meta = {}
    clinical_metafiles = []
    non_clinical_metafiles = []
    cancer_type_meta = {}

    for f in study_files:
        if re.search(r'(\b|_)meta(\b|_)', f):
            metadata = get_properties(f)
            if 'meta_study' in metadata.get('meta_file_type'):
                study_meta = metadata
                #First remove study if exists
                remove_study(jvm_args,f)
                #Then import study
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



    # First, import cancer type
    if cancer_type_meta != {}:
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
    print >> OUTPUT_FILE, ('cbioportalImporter.py --jar-path (path to core jar file) ' +
                           '--command [%s] --study_directory <path to directory> '
                           '--meta_filename <path to metafile>'
                           '--data_filename <path to datafile>' % (COMMANDS))

def check_args(command):
    if command not in COMMANDS:
        usage()
        sys.exit(2)


def check_files(meta_filename, data_filename):
    if meta_filename and not os.path.exists(meta_filename):
        print >> ERROR_FILE, 'meta-file cannot be found: ' + meta_filename
        sys.exit(2)
    if data_filename  and not os.path.exists(data_filename):
        print >> ERROR_FILE, 'data-file cannot be found:' + data_filename

def check_dir(study_directory):
    # check existence of directory
    if not os.path.exists(study_directory) and study_directory != '':
        print >> ERROR_FILE, 'Study cannot be found: ' + study_directory
        sys.exit(2)

def interface():
    parser = argparse.ArgumentParser(description='cBioPortal meta Importer')
    parser.add_argument('-c', '--command', type=str, required=True,
                        help='Command for import.')
    parser.add_argument('-s', '--study_directory',type=str, required=False,
                        help='Path to Study Directory')
    parser.add_argument('-jar', '--jar_path',type=str, required=True,
                        help='Path to core JAR file')
    parser.add_argument('-meta', '--meta_filename',type=str, required=False,
                        help='Path to meta file')
    parser.add_argument('-data', '--data_filename', type=str, required=False,
                        help='Path to Data file')

    parser = parser.parse_args()
    return parser


def main(args):

    # process the options
    jvm_args = "-Dspring.profiles.active=dbcp -cp " + args.jar_path
    study_directory = args.study_directory

    if study_directory != None:
        check_dir(study_directory)
        process_directory(jvm_args, study_directory)
    else:
        check_args(args.command)
        check_files(args.meta_filename, args.data_filename)
        process_command(jvm_args, args.command, args.meta_filename, args.data_filename)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    args = interface()
    main(args)
