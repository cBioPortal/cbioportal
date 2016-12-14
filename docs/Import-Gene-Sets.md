# Import Gene Sets in cBioPortal

Gene sets are collections of genes that are grouped together based on higher level function or system characteristics, such as being part of the same molecular process or found to be co-regulated for example. Assessing gene sets in cBioPortal is useful when the user wants to visualize the number of mutations in sets of genes, or wants to see if all genes in a set are up- or down-regulated. To visualize gene set variation in a sample, the user can calculate scores per gene set per sample using the Gene Set Variation Analysis (GSVA) algorithm ([Hänzelmann, 2013](#references)). 

Before loading a study with gene set data, gene set definitions have to be added to the database. These can be custom user defined sets, or sets downloaded from external sources such as [MSigDB](#references). Additionally, a gene set hierarchy can be imported which is used on the cBioPortal Query page for selecting gene sets.

## Table of contents

- [Requirements for gene sets in cBioPortal](#requirements-for-gene-sets-in-cbioportal)
- [Import Gene Sets](#import-gene-sets)
	- [File formats](#file-formats)
	- [Run the gene set importer](#run-the-gene-set-importer)
- [Import Gene Set hierarchy](#import-gene-set-hierarchy)
	- [File format](#file-format)
	- [Running the gene set hierarchy importer](#running-the-gene-set-hierarchy-importer)
- [Import a study with gene set data](#import-a-study-with-gene-set-data)
	- [GSVA Score meta file](#gsva-score-meta-file)
	- [GSVA Score data file](#gsva-score-data-file)
	- [GSVA p-value meta file](#gsva-p-value-meta-file)
	- [GSVA p-value data file](#gsva-p-value-data-file)
- [Example to load complete test data](#example-to-load-complete-test-data)
- [References](#references)

## Requirements for gene sets in cBioPortal
Gene set functionality was added in cBioPortal x.x.x. Therefor please use this or a later version. In addition, because gene sets are stored in specific gene set tables, the database has to be updated to at least version 2.1.0. This can be done with the by running the python wrapper `migrate_db.py` for `migration.sql`. 

Updating the database is described in [here](https://github.com/cBioPortal/cbioportal/blob/master/docs/Updating-your-cBioPortal-installation.md#running-the-migration-script).

## Import Gene Sets

### File formats
Once you have initialized MySQL with cBioPortal database, it is possible to import gene sets. The format of the gene set data file is [the Gene Matrix Transposed file format (.gmt)](http://software.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats#GMT:_Gene_Matrix_Transposed_file_format_.28.2A.gmt.29). This format is also used by the [MSigDB](#references), which hosts several collections of gene sets on: [http://software.broadinstitute.org/gsea/msigdb/](http://software.broadinstitute.org/gsea/msigdb/)

Sample of .gmt file:
```
GLI1_UP.V1_DN	http://...	22818	143384
GLI1_UP.V1_UP	http://...	3489	3119
E2F1_UP.V1_DN	http://...	7041	6374	5460	
```

GMT files contain a row for every gene set. The first column contains the EXTERNAL_ID or `stable id` (MsigDB calls this "standard name"), e.g. GO_POTASSIUM_ION_TRANSPORT, not longer than 100 characters. The second column contains the REF_LINK. This is an optional URL linking to external information about this gene set. Column 3 to N contain the Entrez gene IDs that belong to this gene set.

Additional information can be loaded in a supplementary file. This file should be a .txt, containing columns for the `stable id`, the full name (max 100 characters) and description of the gene set (max 300 characters). 

Sample of supplementary .txt file:
```
GLI1_UP.V1_DN	GLI1 upregulated v1 down genes	Genes down-regulated in RK3E cells (kidney epithelium) over-expressing GLI1 [GeneID=2735].
GLI1_UP.V1_UP	GLI1 upregulated v1 up genes	Genes up-regulated in RK3E cells (kidney epithelium) over-expressing GLI1 [GeneID=2735].
E2F1_UP.V1_DN	E2F1 upregulated v1 down genes	Identification of E2F1-regulated genes that modulate the transition from quiescence into DNA synthesis, or have roles in apoptosis, signal transduction, membrane biology, and transcription repression.

```
### Run the gene set importer
The importer for gene sets is located at the following location and has the following arguments:
```
$PORTAL_HOME/core/src/main/java/org/mskcc/cbio/portal/scripts/ImportGenesetData.java

required:     --data <data_file.gmt>  
              --new-version <Version> OR --update-info
optional:     --supp <supp_file.txt>
```
When importing gene sets, it is required to add **a user defined version**. Later when the user imports genomic profiles with gene set data, it is also required to give this version in the meta files. This ensures that the data is generated with the same version of gene set definitions as is in the database. 

The `--new-version` argument with a `<Version>` parameter is used for loading new gene set definitions. It is not possible to add new gene sets or change the genes of current gene sets, without removing the old gene sets first. This is to prevent the user from having gene sets from different definitions and data from older definitions. The user can choose whatever version he wants, e.g. `v1.0` or `Oncogenic_2017`. Running the script with `--new-version` does **remove** all previous gene sets, gene set hierarchy and gene set score genetic profiles. A prompt is given to make sure the user wants to do this. Note that it is possible to give in the same version as the previous version, but previous data is removed nevertheless.

The `--update info` is used to update only the gene set name, description and reference URL.

## Import Gene Set hierarchy

When gene sets are imported, the user can import a gene set hierarchy that is used on the query page to select gene sets.

### File format
For gene set hierarchy files, we use the YAML format. This is common format to sturcture hierarchical data. 

Sample of format (note this is mock data):

```
Custom:
  Gene sets:
    - BCAT.100_UP.V1_DN
Cancer Gene sets from Broad:
  Gene sets:
    - AKT_UP.V1_DN
    - CYCLIN_D1_KE_.V1_UP
  Broad Subcategory 1:
    Gene sets:
      - HINATA_NFKB_MATRIX
  Broad Subcategory 2:
    Gene sets:
      - GLI1_UP.V1_UP
      - GLI1_UP.V1_DN
      - CYCLIN_D1_KE_.V1_UP

```

To make your own hierarchy, make sure every branchname ends with `:`. Every branch can contain new branches (which can be considered subcategories) or gene sets (which are designated by the `Gene sets:` statement). The gene set names are the `stable ids` imported by `ImportGenesetData.java` and should start with `-`.

### Running the gene set hierarchy importer
```
$PORTAL_HOME/core/src/main/java/org/mskcc/cbio/portal/scripts/ImportGenesetHierarchy.java

required:     --data <data_file.yaml>  
```

## Import a study with gene set data

Gene set data can be added to a study folder and subsequently import the whole study with metaImport.py. cBioPortal supports GSVA Scores and p-values (from bootstrapping) calculated by the Gene Set Variation Analysis (GSVA, [Hänzelmann, 2013](#references)) algorithm in R. To import the GSVA Scores and p-values we will need 2 sets of a meta and a data file, according to the cBioPortal data loading specifications for study data. One set for the GSVA scores per sample, and one for the respective GSVA p-values of these scores per sample.

It is important that the dimensions of the GSVA Score and p-value file are the same, and that they contain the gene sets and samples.

### GSVA Score meta file
The meta file will be similar to meta files of other genetic profiles, such as mRNA expression. These are the required fields: 

```
cancer_study_identifier: Same value as specified in study meta file
genetic_alteration_type: GENESET_SCORE
datatype: GSVA-SCORE
stable_id: Any unique identifier within the study
source_stable_id: Stable id of the genetic profile (in this same study) that was used as the input source for calculating the GSVA scores. Typically this will be one of the mRNA expression genetic profiles. 
profile_name: A name describing the analysis.
profile_description: A description of the data processing done.
data_filename: <your GSVA score datafile>
geneset_def_version: Version of the gene set definition this calculation was based on. 
```

Example:
```
cancer_study_identifier: study_es_0
genetic_alteration_type: GENESET_SCORE
datatype: GSVA-SCORE
stable_id: gsva_oncogenic_sets_scores
source_stable_id: rna_seq_v2_mrna
profile_name: GSVA scores on oncogenic signatures gene sets
profile_description: GSVA scores on oncogenic signatures gene sets using mRNA expression data calculated with GSVA version x with parameters x and y.
data_filename: data_gsva_scores.txt
geneset_def_version: 1
```

### GSVA Score data file
The data file will be a simple tab separated format, similar to the expression data  file: each sample is a column, each gene set a row, each cell represents the GSVA score for that sample x gene set combination.

The first column is the GENESET_ID. This contains the EXTERNAL_ID or "stable id" (MsigDB calls this "standard name") of the gene set. The other colums are the sample columns: An additional column for each sample in the dataset using the sample id as the column header.

The cells contain the GSVA score: which is real number, between -1.0 and 1.0, representing the GSVA score for the gene set in the respective sample, or  NA for when the GSVA score for the gene set in the respective sample could not be (or was not) calculated.
Example with 2 gene sets and 3 samples: 

| GENESET_ID                      | TCGA-AO-A0J | TCGA-A2-A0Y | TCGA-A2-A0S |
|---------------------------------|-------------|-------------|-------------|
| GO_POTASSIUM_ION_TRANSPOR       | -0.987      | 0.423       | -0.879      |
| GO_GLUCURONATE_METABOLIC_PROCES | 0.546       | 0.654       | 0.123       |
| ..                              |             |             |             |

### GSVA p-value meta file
The meta file will be similar to meta files of other genetic profiles, such as mRNA expression. These are the fields: 
```
cancer_study_identifier: Same value as specified in study meta file
genetic_alteration_type: GENESET_SCORE
datatype: P-VALUE
stable_id: Any unique identifier within the study
source_stable_id: Stable id of the GSVA-SCORES genetic profile (see above). 
profile_name: A name describing the analysis.
profile_description: A description of the data processing done.
data_filename: <your GSVA p-value datafile>
geneset_def_version: Version of the gene sets definition this calculation was based on. 
```
Example:
```
cancer_study_identifier: study_es_0
genetic_alteration_type: GENESET_SCOREGSVA-P-VALUES
datatype: P-VALUE
stable_id: gsva_oncogenic_sets_pvalues
source_stable_id: gsva_oncogenic_sets_scores
profile_name: GSVA p-values for GSVA scores on oncogenic signatures gene sets
profile_description: GSVA p-values for GSVA scores on oncogenic signatures gene sets using mRNA expression data calculated with the bootstrapping method in GSVA version x with parameters x and y.
data_filename: data_gsva_pvalues.txt
geneset_def_version: 1
```

### GSVA p-value data file
The data file will be a simple tab separated format, similar to the GSVA score file: each sample is a column, each gene set a row, each cell represents the GSVA p-value for the score found for that sample x gene set combination.

The first column is the GENESET_ID. This contains the EXTERNAL_ID or "stable id" (MsigDB calls this "standard name") of the gene set. The other colums are the sample columns: An additional column for each sample in the dataset using the sample id as the column header.

The cells contain the p-value for the GSVA score: A real number, between 0.0 and 1.0, representing the p-value for the GSVA score calculated for the gene set in the respective sample, or NA for when the GSVA score for the gene is also NA.
Example with 2 gene sets and 3 samples: 

| GENESET_ID                      | TCGA-AO-A0J | TCGA-A2-A0Y | TCGA-A2-A0S |
|---------------------------------|-------------|-------------|-------------|
| GO_POTASSIUM_ION_TRANSPOR       | 0.0811      | 0.0431      | 0.0087      |
| GO_GLUCURONATE_METABOLIC_PROCES | 0.6621      | 0.0031      | 1.52e-9     |
| ..                              |             |             |             |

## Example to load complete test data

Steps to load simple example files: 

1- go to
`cd $PORTAL_HOME/core/src/main/scripts`

2- Import genesets:
`./importGenesetData.pl --data ../../test/resources/genesets/genesets_test.txt --new-version 1 --supp ../../test/resources/genesets/supp-genesets.txt`

3- Import hierarchy info: 

`./importGenesetHierarchy.pl --data ../../test/resources/genesets/genesetshierarchy_test.yaml` 
 


## References
**GSVA: gene set variation analysis for microarray and RNA-Seq data**<br>
Sonja Hänzelmann, Robert Castelo and Justin Guinney, *BMC Bioinformatics, 2013*<br>
http://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-14-7 <br>
http://www.bioconductor.org/packages/release/bioc/html/GSVA.html

**Gene set enrichment analysis: A knowledge-based approach for interpreting genome-wide expression profiles**<br>
Aravind Subramanian, Pablo Tamayo, Vamsi K. Mootha, Sayan Mukherjee, Benjamin L. Ebert, Michael A. Gillette, Amanda Paulovich, Scott L. Pomeroy, Todd R. Golub, Eric S. Lander, and Jill P. Mesirov, *PNAS, 2005*<br>
http://www.pnas.org/content/102/43/15545 <br>
http://software.broadinstitute.org/gsea/msigdb
