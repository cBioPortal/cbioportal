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
# case lists structure

CASE_LISTS = [
    {'CASE_LIST_FILENAME' : 'cases_all.txt', 'STAGING_FILENAME' : 'data_CNA.txt|data_RNA_Seq_mRNA_median_Zscores.txt|data_RNA_Seq_v2_mRNA_median_Zscores.txt|data_mRNA_median_Zscores.txt|data_mutations_extended.txt|data_methylation_27.txt|data_methylation_hm450.txt|data_rppa.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_all', 'META_CASE_LIST_CATEGORY' : 'all_cases_in_study', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'All Tumors', 'META_CASE_LIST_DESCRIPTION' : 'All tumor samples (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_cna.txt', 'STAGING_FILENAME' : 'data_CNA.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_cna', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_cna_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors CNA', 'META_CASE_LIST_DESCRIPTION' : 'All tumors with CNA data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_cnaseq.txt', 'STAGING_FILENAME' : 'data_CNA.txt&data_mutations_extended.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_cnaseq', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mutation_and_cna_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with sequencing and CNA data', 'META_CASE_LIST_DESCRIPTION' : 'All tumor samples that have CNA and sequencing data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_complete.txt', 'STAGING_FILENAME' : 'data_RNA_Seq_v2_mRNA_median_Zscores.txt&data_CNA.txt&data_mutations_extended.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_3way_complete', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mutation_and_cna_and_mrna_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'All Complete Tumors', 'META_CASE_LIST_DESCRIPTION' : 'All tumor samples that have mRNA, CNA and sequencing data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_complete.txt', 'STAGING_FILENAME' : 'data_RNA_Seq_expression_median.txt&data_CNA.txt&data_mutations_extended.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_3way_complete', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mutation_and_cna_and_mrna_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'All Complete Tumors', 'META_CASE_LIST_DESCRIPTION' : 'All tumor samples that have mRNA, CNA and sequencing data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_complete.txt', 'STAGING_FILENAME' : 'data_mRNA_median_Zscores.txt&data_CNA.txt&data_mutations_extended.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_3way_complete', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mutation_and_cna_and_mrna_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'All Complete Tumors', 'META_CASE_LIST_DESCRIPTION' : 'All tumor samples that have mRNA, CNA and sequencing data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_complete.txt', 'STAGING_FILENAME' : 'data_expression_Zscores.txt&data_CNA.txt&data_mutations_extended.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_3way_complete', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mutation_and_cna_and_mrna_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'All Complete Tumors', 'META_CASE_LIST_DESCRIPTION' : 'All tumor samples that have mRNA, CNA and sequencing data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_log2CNA.txt', 'STAGING_FILENAME' : 'data_log2CNA.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_log2CNA', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_log2_cna_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors log2 copy-number', 'META_CASE_LIST_DESCRIPTION' : 'All tumors with log2 copy-number data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_methylation_hm27.txt', 'STAGING_FILENAME' : 'data_methylation_hm27.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_methylation_hm27', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_methylation_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with methylation data (HM27)', 'META_CASE_LIST_DESCRIPTION' : 'All samples with methylation (HM27) data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_methylation_hm450.txt', 'STAGING_FILENAME' : 'data_methylation_hm450.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_methylation_hm450', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_methylation_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with methylation data (HM450)', 'META_CASE_LIST_DESCRIPTION' : 'All samples with methylation (HM450) data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_miRNA.txt', 'STAGING_FILENAME' : 'data_miRNA_median_Zscores.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_microrna', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_microrna_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with microRNA data (microRNA-Seq)', 'META_CASE_LIST_DESCRIPTION' : 'All samples with microRNA data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_mRNA_U133.txt', 'STAGING_FILENAME' : 'data_expression_Zscores.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_mrna_U133', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mrna_array_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with mRNA data (U133 microarray only)', 'META_CASE_LIST_DESCRIPTION' : 'All samples with mRNA expression data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_mRNA.txt', 'STAGING_FILENAME' : 'data_mRNA_median_Zscores.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_mrna', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mrna_array_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with mRNA data (Agilent microarray)', 'META_CASE_LIST_DESCRIPTION' : 'All samples with mRNA expression data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_RNA_Seq_mRNA.txt', 'STAGING_FILENAME' : 'data_RNA_Seq_expression_median.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_rna_seq_mrna', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mrna_rnaseq_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with mRNA data (RNA Seq)', 'META_CASE_LIST_DESCRIPTION' : 'All samples with mRNA expression data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_RNA_Seq_v2_mRNA.txt', 'STAGING_FILENAME' : 'data_RNA_Seq_v2_mRNA_median_Zscores.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_rna_seq_v2_mrna', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mrna_rnaseq_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with mRNA data (RNA Seq V2)', 'META_CASE_LIST_DESCRIPTION' : 'All samples with mRNA expression data (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_rppa.txt', 'STAGING_FILENAME' : 'data_rppa.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_rppa', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_rppa_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Tumors with RPPA data', 'META_CASE_LIST_DESCRIPTION' : 'Tumors with reverse phase protein array (RPPA) data for about 200 antibodies (<NUM_CASES> samples)'},
    {'CASE_LIST_FILENAME' : 'cases_sequenced.txt', 'STAGING_FILENAME' : 'data_mutations_extended.txt', 'META_STABLE_ID' : '<CANCER_STUDY>_sequenced', 'META_CASE_LIST_CATEGORY' : 'all_cases_with_mutation_data', 'META_CANCER_STUDY_ID' : '<CANCER_STUDY>', 'META_CASE_LIST_NAME' : 'Sequenced Tumors', 'META_CASE_LIST_DESCRIPTION' : 'All (Next-Gen) sequenced samples (<NUM_CASES> samples)'},
]

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

def read_meta(meta_file):
    meta = {}
    for line in meta_file:
        line = line.strip()
        line_data = line.split(':')
        meta[line_data[0]] = line_data[1]
    return meta

def create_cancer_type_file(study_meta, study_directory):
    cancer_type_file = open(study_directory + '/' + 'cancer_type.txt','w')
    cancer_type_file.wrtie(study_meta.get('type_of_cancer') + '\t' + study_meta.get('type_of_cancer') + '\t' + study_meta.get('type_of_cancer') + '\t' + study_meta.get('dedicated_color') + '\t' + study_meta.get('type_of_cancer'))
    cancer_type_file.close()

def process_case_lists(jvm_args,study_files):
    case_list_files = []
    # see if there are any case lists first
    for f in study_files:
        if 'case_lists' in f:
            if os.path.isdir(f):
                case_list_files = [f + '/' + x for x in os.listdir(f)]
                for case_list in case_list_files:
                    import_case_list(jvm_args,case_list)

    # now, lets go through the other case lists we can create based on data files available
    for case in CASE_LISTS:
        case_list_filename = case['CASE_LIST_FILENAME']
        for case_list in case_list_files:
            if case_list_filename not in case_list:
                # we should make it if possible!
                create_case_list(study_files,case)

def create_case_list(study_files, case_properties):
    if can_create_case(study_files,case_properties):
        return
        

def can_create_case(study_files, case_properties):
    staging_files_needed = case_properties['STAGING_FILENAME'].split('|')
    for staging_file in staging_files_needed:
        found = False
        for f in study_files:
            if staging_file in f:
                found = True
        if not found:
            return found
    return found

def process(jvm_args, study_directory, command):
    study_files = [study_directory + '/' + x for x in os.listdir(study_directory)]
    meta_filename = study_directory + '/' + "meta_study.txt"
    study_meta = {}

    if meta_filename in study_files:
        meta_file = open(meta_filename,'r')
        study_meta = read_meta(meta_file)
    else:
        print >> ERROR_FILE, 'meta_study.txt cannot be found'
        sys.exit(2)

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
            meta_file = open(f, 'r')
            metadata = read_meta(meta_file)
            if metadata.get('meta_file_type') == 'meta_clinical':
                import_study_data(jvm_args, f, metadata.get('data_file_path'))
            else:
                non_clinical_metafiles.add(f)

    # Now, import everything else
    for f in non_clinical_metafiles:
        meta_file = open(f, 'r')
        metadata = read_meta(meta_file)
        files_found.append
        import_study_data(jvm_args, f, metadata.get('data_file_path'))

    # Lets add some caselists! Not yet ready!!! But Soon!
    # process_case_lists(jvm_args,study_files)

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
