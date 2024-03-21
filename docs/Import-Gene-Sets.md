# Import Gene Sets in cBioPortal

Gene sets are collections of genes that are grouped together based on higher level function or system characteristics, such as being part of the same molecular process or found to be co-regulated for example. Assessing gene sets in cBioPortal is useful when the user wants to visualize the number of mutations in sets of genes, or wants to see if all genes in a set are up- or down-regulated. To visualize gene set variation in a sample, the user can calculate scores per gene set per sample using the Gene Set Variation Analysis (GSVA) algorithm ([Hänzelmann, 2013](#references)).

Before loading a study with gene set data, gene set definitions have to be added to the database. These can be custom user-defined sets, or sets downloaded from external sources such as [MSigDB](#references). Additionally, a gene set hierarchy can be imported which is used on the cBioPortal Query page for selecting gene sets.

## Quick example
This example shows how the process of importing gene set data using test data.

1. Navigate to scripts folder:

```
cd <cbioportal_source_folder>/core/src/main/scripts
```

2. Import gene sets and supplementary data:
Note: This removes existing gene set, gene set hierarchy and gene set genetic profile data.

```
./importGenesetData.pl \
	--data ../../test/resources/genesets/study_es_0_genesets.gmt \
	--new-version msigdb_6.1 \
	--supp ../../test/resources/genesets/study_es_0_supp-genesets.txt
```

3. Import gene set hierarchy data:

```
./importGenesetHierarchy.pl \
	--data ../../test/resources/genesets/study_es_0_tree.yaml
```

4. Restart Tomcat if you have it running or call the `/api/cache` endpoint with a `DELETE` http-request
   (see [here](/deployment/customization/application.properties-Reference.md#evict-caches-with-the-apicache-endpoint) for more information).


5. Import study (replace argument after `-u` with local cBioPortal and `-html` with preferred location for html report):

```
./importer/metaImport.py \
	-s ../../test/scripts/test_data/study_es_0 \
	-u http://cbioportal:8080/cbioportal \
	-html report_study_es_0.html \
	-v \
	-o
```

## Requirements for gene sets in cBioPortal
Gene set functionality was added in cBioPortal 1.7.0. Please use this or a later version. In addition, the database has to be updated to version 2.3.0 or higher, depending on the cBioPortal version. This can be done by running the python wrapper `migrate_db.py` for `migration.sql`.

Updating the database is described [here](https://github.com/cBioPortal/cbioportal/blob/master/docs/Updating-your-cBioPortal-installation.md#running-the-migration-script).

## Import Gene Sets

### File formats
Once you have initialized MySQL with cBioPortal database, it is possible to import gene sets. The format of the gene set data file is [the Gene Matrix Transposed file format (.gmt)](https://software.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats#GMT:_Gene_Matrix_Transposed_file_format_.28.2A.gmt.29). This format is also used by the [MSigDB](#references), which hosts several collections of gene sets on: [https://software.broadinstitute.org/gsea/msigdb/](https://software.broadinstitute.org/gsea/msigdb/)

Sample of .gmt file:

```
GLI1_UP.V1_DN<TAB>https://...<TAB>22818<TAB>143384
GLI1_UP.V1_UP<TAB>https://...<TAB>3489<TAB>3119
E2F1_UP.V1_DN<TAB>https://...<TAB>7041<TAB>6374<TAB>5460
```

GMT files contain a row for every gene set. The first column contains the EXTERNAL_ID or `stable id` (MsigDB calls this "standard name"), e.g. GO_POTASSIUM_ION_TRANSPORT, not longer than 100 characters. The second column contains the REF_LINK. This is an optional URL linking to external information about this gene set. Column 3 to N contain the Entrez gene IDs that belong to this gene set.

Additional information can be placed in a supplementary file. This file should be a .txt, containing columns for the `stable id`, the long name (max 100 characters) and description of the gene set (max 300 characters).

Sample of supplementary .txt file:

```
GLI1_UP.V1_DN<TAB>GLI1 upregulated v1 down genes<TAB>Genes down-regulated in RK3E cells (kidney epithelium) over-expressing GLI1 [GeneID=2735].
GLI1_UP.V1_UP<TAB>GLI1 upregulated v1 up genes<TAB>Genes up-regulated in RK3E cells (kidney epithelium) over-expressing GLI1 [GeneID=2735].
E2F1_UP.V1_DN<TAB>E2F1 upregulated v1 down genes<TAB>Identification of E2F1-regulated genes that modulate the transition from quiescence into DNA synthesis, or have roles in apoptosis, signal transduction, membrane biology, and transcription repression.

```
### Run the gene set importer
The importer for gene sets can be run with a perl wrapper, which is located at the following location and requires the following arguments:

```
cd <cbioportal_source_folder>/core/src/main/scripts
perl importGenesetData.pl

required:     --data <data_file.gmt>
              --new-version <Version> OR --update-info
optional:     --supp <supp_file.txt>
```
The `--new-version` argument with a `<Version>` parameter is used for loading new gene set definitions. It is not possible to add new gene sets or change the genes of current gene sets, without removing the old gene sets first. This is to prevent the user from having gene sets from different definitions and data from older definitions. The user can choose the name or number of the `<Version>` as he likes, e.g. `msigdb_6.1` or `Oncogenic_2017`. Running the script with `--new-version` **removes all previous gene sets, gene set hierarchy and gene set genetic profiles.** A prompt is given to make sure the user wants to do this. Note that it is possible enter the same version as the previous version, but previous data is removed nevertheless.

The `--update info` can be used only to update only the long name, description and reference URL.

## Import Gene Set hierarchy

After importing gene sets, you can import a gene set hierarchy that is used on the query page to select gene sets.

### File format
For gene set hierarchy files, we use the YAML format. This is common format to structure hierarchical data.

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
cd <cbioportal_source_folder>/core/src/main/scripts
perl importGenesetHierarchy.pl

required:     --data <data_file.yaml>
```

## Import a study with gene set data

Gene set data can be added to a study folder and subsequently import the whole study with `metaImport.py`. cBioPortal supports GSVA Scores and p-values (from bootstrapping) calculated using Gene Set Variation Analysis (GSVA, [Hänzelmann, 2013](#references)).
A description of GSVA study data can be found in the [cBioPortal File Formats documentation](File-Formats.md#gene-set-data).

## References
**GSVA: gene set variation analysis for microarray and RNA-Seq data**<br>
Sonja Hänzelmann, Robert Castelo and Justin Guinney, *BMC Bioinformatics, 2013*<br>
https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-14-7 <br>
https://www.bioconductor.org/packages/release/bioc/html/GSVA.html

**Gene set enrichment analysis: A knowledge-based approach for interpreting genome-wide expression profiles**<br>
Aravind Subramanian, Pablo Tamayo, Vamsi K. Mootha, Sayan Mukherjee, Benjamin L. Ebert, Michael A. Gillette, Amanda Paulovich, Scott L. Pomeroy, Todd R. Golub, Eric S. Lander, and Jill P. Mesirov, *PNAS, 2005*<br>
https://www.pnas.org/content/102/43/15545 <br>
https://software.broadinstitute.org/gsea/msigdb
