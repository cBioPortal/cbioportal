To facilitate the loading of new studies into its database, cBioPortal [provides a set of staging files formats](File-Formats.md) for the various data types. To validate your files you can use the dataset validator script. 

## Running the validator

To run the validator first go to the importer folder
`<your_cbioportal_dir>/core/src/main/scripts/importer` 
and then run the following command:
```bash
./validateData.py --help
```
This will tell you the parameters you can use: 
```console
usage: validateData.py [-h] -s STUDY_DIRECTORY
                       [-u URL_SERVER | -p PORTAL_INFO_DIR | -n]
                       [-html HTML_TABLE] [-e ERROR_FILE] [-v]

cBioPortal study validator

optional arguments:
  -h, --help            show this help message and exit
  -s STUDY_DIRECTORY, --study_directory STUDY_DIRECTORY
                        path to directory.
  -u URL_SERVER, --url_server URL_SERVER
                        URL to cBioPortal server. You can set this if your URL
                        is not http://localhost/cbioportal
  -p PORTAL_INFO_DIR, --portal_info_dir PORTAL_INFO_DIR
                        Path to a directory of cBioPortal info files to be
                        used instead of contacting a server
  -n, --no_portal_checks
                        Skip tests requiring information from the cBioPortal
                        installation
  -P PORTAL_PROPERTIES, --portal_properties PORTAL_PROPERTIES
                        portal.properties file path (default: assumed hg19)
  -html HTML_TABLE, --html_table HTML_TABLE
                        path to html report output file
  -e ERROR_FILE, --error_file ERROR_FILE
                        File to which to write line numbers on which errors
                        were found, for scripts
  -v, --verbose         report status info messages in addition to errors and
                        warnings
```

For more information on the `--portal_info_dir` option, see [Offline validation](#offline-validation) below. If your cBioPortal is not using `hg19`, you must use the `--portal_properties` option. For more information, see [Validation of non-human data](#validation-of-non-human-data).

### Example 1
As an example, you can try the validator with one of the test studies found in  `<your_cbioportal_dir>/core/src/test/scripts/test_data`. Example, assuming port 8080 and using -v option to also see the progress:
```bash
./validateData.py -s ../../../test/scripts/test_data/study_es_0/ -u http://localhost:8080/cbioportal -v
```
Results in:
```console
DEBUG: -: Requesting cancertypes from portal at 'http://localhost:8080/cbioportal'
DEBUG: -: Requesting clinicalattributes/patients from portal at 'http://localhost:8080/cbioportal'
DEBUG: -: Requesting clinicalattributes/samples from portal at 'http://localhost:8080/cbioportal'
DEBUG: -: Requesting genes from portal at 'http://localhost:8080/cbioportal'
DEBUG: -: Requesting genesaliases from portal at 'http://localhost:8080/cbioportal'

DEBUG: meta_CNA.txt: Starting validation of meta file
INFO: meta_CNA.txt: Validation of meta file complete

DEBUG: brca_tcga_meta_cna_hg19_seg.txt: Starting validation of meta file
INFO: brca_tcga_meta_cna_hg19_seg.txt: Validation of meta file complete

DEBUG: -: Retrieving chromosome lengths from 'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips/hg19.chrom.sizes'

DEBUG: meta_patients.txt: Starting validation of meta file
INFO: meta_patients.txt: Validation of meta file complete

DEBUG: meta_samples.txt: Starting validation of meta file
INFO: meta_samples.txt: Validation of meta file complete

DEBUG: meta_fusions.txt: Starting validation of meta file
INFO: meta_fusions.txt: Validation of meta file complete

DEBUG: meta_log2CNA.txt: Starting validation of meta file
INFO: meta_log2CNA.txt: Validation of meta file complete

DEBUG: meta_study.txt: Starting validation of meta file
INFO: meta_study.txt: Validation of meta file complete

DEBUG: meta_methylation_hm27.txt: Starting validation of meta file
INFO: meta_methylation_hm27.txt: Validation of meta file complete

DEBUG: meta_expression_median.txt: Starting validation of meta file
INFO: meta_expression_median.txt: Validation of meta file complete

DEBUG: meta_gistic_genes_amp.txt: Starting validation of meta file
INFO: meta_gistic_genes_amp.txt: Validation of meta file complete

DEBUG: meta_mutations_extended.txt: Starting validation of meta file
INFO: meta_mutations_extended.txt: Validation of meta file complete

DEBUG: data_samples.txt: Starting validation of file
INFO: data_samples.txt: Validation of file complete
INFO: data_samples.txt: Read 831 lines. Lines with warning: 0. Lines with error: 0

DEBUG: brca_tcga_data_cna_hg19.seg: Starting validation of file
INFO: brca_tcga_data_cna_hg19.seg: Validation of file complete
INFO: brca_tcga_data_cna_hg19.seg: Read 10 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_methylation_hm27.txt: Starting validation of file
INFO: data_methylation_hm27.txt: Validation of file complete
INFO: data_methylation_hm27.txt: Read 9 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_fusions.txt: Starting validation of file
INFO: data_fusions.txt: Validation of file complete
INFO: data_fusions.txt: Read 4 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_expression_median.txt: Starting validation of file
INFO: data_expression_median.txt: Validation of file complete
INFO: data_expression_median.txt: Read 7 lines. Lines with warning: 0. Lines with error: 0

DEBUG: brca_tcga_pub.maf: Starting validation of file
INFO: brca_tcga_pub.maf: lines [7, 9, 14]: Validation of line skipped due to cBioPortal's filtering. Filtered types: [Silent, Intron, 3'UTR, 3'Flank, 5'UTR, 5'Flank, IGR, RNA]; value encountered: 'Silent'
INFO: brca_tcga_pub.maf: Validation of file complete
INFO: brca_tcga_pub.maf: Read 15 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_CNA.txt: Starting validation of file
INFO: data_CNA.txt: Validation of file complete
INFO: data_CNA.txt: Read 8 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_log2CNA.txt: Starting validation of file
INFO: data_log2CNA.txt: Validation of file complete
INFO: data_log2CNA.txt: Read 8 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_patients.txt: Starting validation of file
INFO: data_patients.txt: Validation of file complete
INFO: data_patients.txt: Read 830 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_gistic_genes_amp.txt: Starting validation of file
INFO: data_gistic_genes_amp.txt: Validation of file complete
INFO: data_gistic_genes_amp.txt: Read 28 lines. Lines with warning: 0. Lines with error: 0

DEBUG: -: Validating case lists

DEBUG: case_lists/cases_custom.txt: Starting validation of meta file
INFO: case_lists/cases_custom.txt: Validation of meta file complete

INFO: -: Validation of case lists complete
INFO: -: Validation complete
Validation of study succeeded.
```

When using the `-html` option, a report will be generated, which looks like this for the previous example:
![Screenshot of a successful validation report](images/scripts/report.png)

### Example 2
More test studies for trying the validator (`study_es_1` and `study_es_3`) are available in  `<your_cbioportal_dir>/core/src/test/scripts/test_data`. Example, assuming port 8080 and using -v option:
```bash
./validateData.py -s ../../../test/scripts/test_data/study_es_1/ -u http://localhost:8080/cbioportal -v
```
Results in:
```console
DEBUG: -: Requesting cancertypes from portal at 'http://localhost:8080/cbioportal'
DEBUG: -: Requesting clinicalattributes/patients from portal at 'http://localhost:8080/cbioportal'
DEBUG: -: Requesting clinicalattributes/samples from portal at 'http://localhost:8080/cbioportal'
DEBUG: -: Requesting genes from portal at 'http://localhost:8080/cbioportal'
DEBUG: -: Requesting genesaliases from portal at 'http://localhost:8080/cbioportal'

DEBUG: meta_samples.txt: Starting validation of meta file
WARNING: meta_samples.txt: Unrecognized field in meta file; values encountered: ['show_profile_in_analysis_tab', 'profile_name', 'profile_description']
INFO: meta_samples.txt: Validation of meta file complete

DEBUG: meta_study.txt: Starting validation of meta file
INFO: meta_study.txt: Validation of meta file complete

DEBUG: meta_expression_median.txt: Starting validation of meta file
ERROR: meta_expression_median.txt: Invalid stable id for genetic_alteration_type 'MRNA_EXPRESSION', data_type 'Z-SCORE'; expected one of [mrna_U133_Zscores, rna_seq_mrna_median_Zscores, mrna_median_Zscores, rna_seq_v2_mrna_median_Zscores, mirna_median_Zscores, mrna_merged_median_Zscores, mrna_zbynorm, rna_seq_mrna_capture_Zscores]; value encountered: 'mrna'

DEBUG: data_samples.txt: Starting validation of file
WARNING: data_samples.txt: line 3: column 4: datatype definition for attribute 'DAYS_TO_COLLECTION' does not match the portal, and will be loaded as 'NUMBER'; value encountered: 'STRING'
ERROR: data_samples.txt: line 8: column 4: According to portal, attribute should be loaded as NUMBER. Value of attribute to be loaded as NUMBER is not a real number; value encountered: 'spam'
INFO: data_samples.txt: Validation of file complete
INFO: data_samples.txt: Read 831 lines. Lines with warning: 1. Lines with error: 1

DEBUG: -: Validating case lists

DEBUG: case_lists/cases_all.txt: Starting validation of meta file
INFO: case_lists/cases_all.txt: Validation of meta file complete
ERROR: case_lists/cases_all.txt: Sample id not defined in clinical file; value encountered: 'INVALID-A2-A0T2-01'

INFO: -: Validation of case lists complete
INFO: -: Validation complete
Validation of study failed.
```

And respective HTML report:
![Screenshot of an unsuccessful validation report](images/scripts/report1.png)

## Offline validation ##
The validation script can be used offline, without connecting to a cBioPortal server. The tests that depend on information specific to the portal (which clinical attributes and cancer types have been previously defined, and which Entrez gene identifiers and corresponding symbols are supported), will instead be read from a folder with .json files generated from the portal.

### Example 3: validation with a portal info folder ###
To run the validator with a folder of portal information files, add the `-p/--portal_info_dir` option to the command line, followed by the path to the folder:
```bash
./validateData.py -s ../../../test/scripts/test_data/study_es_0/ -p ../../../test/scripts/test_data/api_json_system_tests/ -v
```
```console
DEBUG: -: Reading portal information from ../../../test/scripts/test_data/api_json_system_tests/cancertypes.json
DEBUG: -: Reading portal information from ../../../test/scripts/test_data/api_json_system_tests/clinicalattributes_patients.json
DEBUG: -: Reading portal information from ../../../test/scripts/test_data/api_json_system_tests/clinicalattributes_samples.json
DEBUG: -: Reading portal information from ../../../test/scripts/test_data/api_json_system_tests/genes.json
DEBUG: -: Reading portal information from ../../../test/scripts/test_data/api_json_system_tests/genesaliases.json

DEBUG: meta_CNA.txt: Starting validation of meta file
INFO: meta_CNA.txt: Validation of meta file complete

DEBUG: brca_tcga_meta_cna_hg19_seg.txt: Starting validation of meta file
INFO: brca_tcga_meta_cna_hg19_seg.txt: Validation of meta file complete

DEBUG: -: Retrieving chromosome lengths from 'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips/hg19.chrom.sizes'

DEBUG: meta_patients.txt: Starting validation of meta file
INFO: meta_patients.txt: Validation of meta file complete

DEBUG: meta_samples.txt: Starting validation of meta file
INFO: meta_samples.txt: Validation of meta file complete

DEBUG: meta_fusions.txt: Starting validation of meta file
INFO: meta_fusions.txt: Validation of meta file complete

DEBUG: meta_log2CNA.txt: Starting validation of meta file
INFO: meta_log2CNA.txt: Validation of meta file complete

DEBUG: meta_study.txt: Starting validation of meta file
INFO: meta_study.txt: Validation of meta file complete

DEBUG: meta_methylation_hm27.txt: Starting validation of meta file
INFO: meta_methylation_hm27.txt: Validation of meta file complete

DEBUG: meta_expression_median.txt: Starting validation of meta file
INFO: meta_expression_median.txt: Validation of meta file complete

DEBUG: meta_gistic_genes_amp.txt: Starting validation of meta file
INFO: meta_gistic_genes_amp.txt: Validation of meta file complete

DEBUG: meta_mutations_extended.txt: Starting validation of meta file
INFO: meta_mutations_extended.txt: Validation of meta file complete

DEBUG: data_samples.txt: Starting validation of file
INFO: data_samples.txt: Validation of file complete
INFO: data_samples.txt: Read 831 lines. Lines with warning: 0. Lines with error: 0

DEBUG: brca_tcga_data_cna_hg19.seg: Starting validation of file
INFO: brca_tcga_data_cna_hg19.seg: Validation of file complete
INFO: brca_tcga_data_cna_hg19.seg: Read 10 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_methylation_hm27.txt: Starting validation of file
INFO: data_methylation_hm27.txt: Validation of file complete
INFO: data_methylation_hm27.txt: Read 9 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_fusions.txt: Starting validation of file
INFO: data_fusions.txt: Validation of file complete
INFO: data_fusions.txt: Read 4 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_expression_median.txt: Starting validation of file
INFO: data_expression_median.txt: Validation of file complete
INFO: data_expression_median.txt: Read 7 lines. Lines with warning: 0. Lines with error: 0

DEBUG: brca_tcga_pub.maf: Starting validation of file
INFO: brca_tcga_pub.maf: lines [7, 9, 14]: Validation of line skipped due to cBioPortal's filtering. Filtered types: [Silent, Intron, 3'UTR, 3'Flank, 5'UTR, 5'Flank, IGR, RNA]; value encountered: 'Silent'
INFO: brca_tcga_pub.maf: Validation of file complete
INFO: brca_tcga_pub.maf: Read 15 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_CNA.txt: Starting validation of file
INFO: data_CNA.txt: Validation of file complete
INFO: data_CNA.txt: Read 8 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_log2CNA.txt: Starting validation of file
INFO: data_log2CNA.txt: Validation of file complete
INFO: data_log2CNA.txt: Read 8 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_patients.txt: Starting validation of file
INFO: data_patients.txt: Validation of file complete
INFO: data_patients.txt: Read 830 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_gistic_genes_amp.txt: Starting validation of file
INFO: data_gistic_genes_amp.txt: Validation of file complete
INFO: data_gistic_genes_amp.txt: Read 28 lines. Lines with warning: 0. Lines with error: 0

DEBUG: -: Validating case lists

DEBUG: case_lists/cases_custom.txt: Starting validation of meta file
INFO: case_lists/cases_custom.txt: Validation of meta file complete

INFO: -: Validation of case lists complete
INFO: -: Validation complete
Validation of study succeeded.
```

### Example 4: generating the portal info folder ###
The portal information files can be generated on the server, using the dumpPortalInfo script. Go to `<your cbioportal dir>/core/src/main/scripts`, make sure the environment variables `$JAVA_HOME` and `$PORTAL_HOME` are set, and run dumpPortalInfo.pl with the name of the directory you want to create:
```bash
export JAVA_HOME='/usr/lib/jvm/java-7-openjdk-amd64'
export PORTAL_HOME='../../../..'
./dumpPortalInfo.pl /home/johndoe/my_portal_info_folder/
```

### Example 5: validating without portal-specific information ###
Alternatively, you can run the validation script with the `-n/--no_portal_checks` flag to entirely skip checks relating to installation-specific metadata. Be warned that files succeeding this validation may still fail to load (correctly).

```bash
./validateData.py -s ../../../test/scripts/test_data/study_es_0/ -n -v
```
```console
WARNING: -: Skipping validations relating to cancer types defined in the portal
WARNING: -: Skipping validations relating to clinical attributes defined in the portal
WARNING: -: Skipping validations relating to gene identifiers and aliases defined in the portal

DEBUG: meta_CNA.txt: Starting validation of meta file
INFO: meta_CNA.txt: Validation of meta file complete

DEBUG: brca_tcga_meta_cna_hg19_seg.txt: Starting validation of meta file
INFO: brca_tcga_meta_cna_hg19_seg.txt: Validation of meta file complete

DEBUG: -: Retrieving chromosome lengths from 'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips/hg19.chrom.sizes'

DEBUG: meta_patients.txt: Starting validation of meta file
INFO: meta_patients.txt: Validation of meta file complete

DEBUG: meta_samples.txt: Starting validation of meta file
INFO: meta_samples.txt: Validation of meta file complete

DEBUG: meta_fusions.txt: Starting validation of meta file
INFO: meta_fusions.txt: Validation of meta file complete

DEBUG: meta_log2CNA.txt: Starting validation of meta file
INFO: meta_log2CNA.txt: Validation of meta file complete

DEBUG: meta_study.txt: Starting validation of meta file
INFO: meta_study.txt: Validation of meta file complete

DEBUG: meta_methylation_hm27.txt: Starting validation of meta file
INFO: meta_methylation_hm27.txt: Validation of meta file complete

DEBUG: meta_expression_median.txt: Starting validation of meta file
INFO: meta_expression_median.txt: Validation of meta file complete

DEBUG: meta_gistic_genes_amp.txt: Starting validation of meta file
INFO: meta_gistic_genes_amp.txt: Validation of meta file complete

DEBUG: meta_mutations_extended.txt: Starting validation of meta file
INFO: meta_mutations_extended.txt: Validation of meta file complete

DEBUG: data_samples.txt: Starting validation of file
INFO: data_samples.txt: Validation of file complete
INFO: data_samples.txt: Read 831 lines. Lines with warning: 0. Lines with error: 0

DEBUG: brca_tcga_data_cna_hg19.seg: Starting validation of file
INFO: brca_tcga_data_cna_hg19.seg: Validation of file complete
INFO: brca_tcga_data_cna_hg19.seg: Read 10 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_methylation_hm27.txt: Starting validation of file
INFO: data_methylation_hm27.txt: Validation of file complete
INFO: data_methylation_hm27.txt: Read 9 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_fusions.txt: Starting validation of file
INFO: data_fusions.txt: Validation of file complete
INFO: data_fusions.txt: Read 4 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_expression_median.txt: Starting validation of file
INFO: data_expression_median.txt: Validation of file complete
INFO: data_expression_median.txt: Read 7 lines. Lines with warning: 0. Lines with error: 0

DEBUG: brca_tcga_pub.maf: Starting validation of file
INFO: brca_tcga_pub.maf: lines [7, 9, 14]: Validation of line skipped due to cBioPortal's filtering. Filtered types: [Silent, Intron, 3'UTR, 3'Flank, 5'UTR, 5'Flank, IGR, RNA]; value encountered: 'Silent'
INFO: brca_tcga_pub.maf: Validation of file complete
INFO: brca_tcga_pub.maf: Read 15 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_CNA.txt: Starting validation of file
INFO: data_CNA.txt: Validation of file complete
INFO: data_CNA.txt: Read 8 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_log2CNA.txt: Starting validation of file
INFO: data_log2CNA.txt: Validation of file complete
INFO: data_log2CNA.txt: Read 8 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_patients.txt: Starting validation of file
INFO: data_patients.txt: Validation of file complete
INFO: data_patients.txt: Read 830 lines. Lines with warning: 0. Lines with error: 0

DEBUG: data_gistic_genes_amp.txt: Starting validation of file
INFO: data_gistic_genes_amp.txt: Validation of file complete
INFO: data_gistic_genes_amp.txt: Read 28 lines. Lines with warning: 0. Lines with error: 0

DEBUG: -: Validating case lists

DEBUG: case_lists/cases_custom.txt: Starting validation of meta file
INFO: case_lists/cases_custom.txt: Validation of meta file complete

INFO: -: Validation of case lists complete
INFO: -: Validation complete
Validation of study succeeded with warnings.
```

## Validation of non-human data ##
When importing a study, the validator assumes by default that the following parameters from `portal.properties` are set to:
```
species=human
ncbi.build=37
ucsc.build=hg19
```

cBioPortal is gradually introducing support for mouse. If you want to load mouse studies and you have [set up your database for mouse](Import-the-Seed-Database.md#download-the-cbioportal-database), you should set the previous parameters to:
```
species=mouse
ncbi.build=38
ucsc.build=mm10
```

If your `portal.properties` does not have the default (human) settings, you should introduce a new parameter `-P` in your command. This parameter should point to either `portal.properties` or a file which contains the new global variables. 

As an example, the command for the "Example 1" listed above incorporating the `-P` parameter is given:
```
./validateData.py -s ../../../test/scripts/test_data/study_es_0/ -P ../../../../../src/main/resources/portal.properties -u http://localhost:8080/cbioportal -v
```
