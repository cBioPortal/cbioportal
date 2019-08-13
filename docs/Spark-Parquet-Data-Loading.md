# Study View Customization with Spark & Parquet

To better support large datasets (over 200k samples), a Spark/Parquet implementation of the Study View web service has been implemented.
As a prerequisite, all [supported filetypes](https://github.com/cBioPortal/cbioportal/blob/af549ac4726e47d7e142f148cf6d3057f232da8c/docs/File-Formats.md)
 need to be converted to Parquet columnar storage format, using the ParquetWriter utility, organized and named in a specific way. 
More information about this organization and naming specification as well as instructions for running the utility are found below.

## Organization of Parquet Files

Parquet files are used in persistence-spark code in order to load data, and should follow the naming convention as documented.
All files should be placed in PARQUET_DIR `${data.parquet.folder}` property specified in the [properties](portal.properties-Reference.md#spark-parquet) with **case_lists**, **gene_panels**, **studies** as the top level directories.

### Example of Organization
 
* PARQUET_DIR
    * **case_lists**
        * msk_impact_2017_cna
    * **gene_panels**
        * impact341
    * **studies**
        * msk_impact_2017
            * clinical_sample
            * mutations

### Parquet File Name

| TSV file                         	                                     | Parquet file                                                           	        | Parquet Type  |
|------------------------------------------------------------------------|----------------------------------------------------------------------------------|---------------|
| data_clinical_patient*.txt                                             | studies/{study_id}/clinical_patient                                              | data          |
| data_clinical_sample*.txt                                              | studies/{study_id}/clinical_sample                                               | data          |
| data_cna_*.seg<br>(e.g. data_cna_hg19.seg)                             | studies/{study_id}/data_cna_*.seg<br>(e.g. studies/{study_id}/data_cna_hg19.seg) | data          |
| data_mutations*.txt                                                    | studies/{study_id}/mutations                                                     | data          |
| data_fusions*.txt                                                      | studies/{study_id}/fusions                                                       | data          |
| data_CNA*.txt                                                          | studies/{study_id}/data_CNA                                                      | cna           |
| meta*.txt                                                              | studies/{study_id}/meta                                                          | meta          |
| case_list/*.txt<br>(e.g. cases_cna.txt)                                | case_lists/{stable_id}<br>(e.g. case_list/msk_impact_2017_cna)                   | case          |
| data_gene_panel_{panel_id}.txt <br>(e.g. data_gene_panel_impact341.txt) | gene_panels/{panel_id}<br>(e.g. gene_panels/impact341)                           | panel         |

**Note:** *.seg* files are kept in separate folders in order to preserve `reference_genome_id`.
 
## Writing Parquet Files

ParquetWriter takes arguments:<br>
**--input-file** `<path to the TSV file>`<br>
**--output-file** `<path to write Parquet file>`<br>
**--input-file-type** `<type of input file - see previous Parquet File Name table>`<br>

Below are examples illustrating how to run the ParquetWriter utility. The following path variables in the examples 
below should be substituted with path values from your environment.

**$HOME** - the path to your cbioportal project<br>
**$TSV_LOCATION** - the path to your TSV files<br>
**$PARQUET_DATA** - the path to Parquet data that is the same as `${data.parquet.folder}` property.<br>

#### Example 1: Data file (all data_* files with the exception of data_CNA, data_gene_panel_*)
```console
$JAVA_HOME/bin/java -cp $HOME/cbioportal/scripts/target/scripts*.jar org.cbioportal.persistence.spark.util.ParquetWriter
--input-file $TSV_LOCATION/msk_impact_2017/data_clinical_sample.txt
--output-file $PARQUET_DATA/studies/msk_impact_2017/clinical_sample
--type data
```

#### Example 2: Meta file
```console
$JAVA_HOME/bin/java -cp $HOME/cbioportal/scripts/target/scripts*.jar org.cbioportal.persistence.spark.util.ParquetWriter
--input-file $TSV_LOCATION/msk_impact_2017/meta_mutsig.txt
--output-file $PARQUET_DATA/studies/msk_impact_2017/meta
--type meta
```

#### Example 3: Case list file
```console
$JAVA_HOME/bin/java -cp $HOME/cbioportal/scripts/target/scripts*.jar org.cbioportal.persistence.spark.util.ParquetWriter
--input-file $TSV_LOCATION/msk_impact_2017/case_lists/cases_cna.txt
--output-file $PARQUET_DATA/case_lists/msk_impact_2017_cna
--type case
```

#### Example 4: Gene panel file
```console
$JAVA_HOME/bin/java -cp $HOME/cbioportal/scripts/target/scripts*.jar org.cbioportal.persistence.spark.util.ParquetWriter
--input-file $TSV_LOCATION/msk_impact_2017/data_gene_panel_impact341.txt
--output-file $PARQUET_DATA/gene_panels/impact341
--type panel
```

#### Example 5: data CNA file
```console
$JAVA_HOME/bin/java -cp $HOME/cbioportal/scripts/target/scripts*.jar org.cbioportal.persistence.spark.util.ParquetWriter
--input-file $TSV_LOCATION/msk_impact_2017/data_CNA.txt
--output-file $PARQUET_DATA/gene_panels/data_CNA
--type cna
```