# Study View Customization with Spark & Parquet

A Spark implementation of data retrieval is provided for Study View for larger datasets.
Spark loads data from Parquet files that use a columnar storage format.

## Writing Parquet Files

A utility is provided to write TSV files into Parquet files.
The naming convention for Parquet files are detailed [here](#organization-of-parquet-files).
 
### Running ParquetWriter with Arguments
--input-file `<path to the TSV file>`
--output-file `path to write Parquet file>`
--type `<type of file (case, dna, meta, panel, data by default)>`

Below are examples of writing Parquet files where below should be substituted with your paths.
**$HOME** - the path to your cbioportal project
**$TSV_LOCATION** - the path to your TSV files
**$PARQUET_DATA** - the path to Parquet data that is the same as `${data.parquet.folder}` property.

#### Example 1: Data file
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

## Organization of Parquet Files

Parquet files are used in persistence-spark code in order to load data, and should follow the convention as documented.
`org.cbioportal.persistence.spark.util.ParquetConstants.java` contains all file names and directories for Parquet files. 

All files should be placed in `${data.parquet.folder}` specified in the [properties](portal.properties-Reference.md#spark-parquet) with **case_lists**, **gene_panels**, **studies** as the top level directories.

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

### Parquet File Names

| TSV file                         	                                     | Parquet file                                                           	        |
|------------------------------------------------------------------------|----------------------------------------------------------------------------------|	
| data_clinical_patient*.txt                                             | studies/{study_id}/clinical_patient                                              |
| data_clinical_sample*.txt                                              | studies/{study_id}/clinical_sample                                               |
| data_cna_*.seg<br>(e.g. data_cna_hg19.seg)                             | studies/{study_id}/data_cna_*.seg<br>(e.g. studies/{study_id}/data_cna_hg19.seg) | 
| data_CNA*.txt                                                          | studies/{study_id}/data_CNA                                                      |
| data_mutations*.txt                                                    | studies/{study_id}/mutations                                                     |
| data_fusions*.txt                                                      | studies/{study_id}/fusions                                                       |
| meta*.txt                                                              | studies/{study_id}/meta                                                          |
| case_list/*.txt<br>(e.g. cases_cna.txt)                                | case_lists/{stable_id}<br>(e.g. case_list/msk_impact_2017_cna)                   |
| data_gene_panel_{panel_id}.txt <br>(e.g. data_gene_panel_impact341.txt) | gene_panels/{panel_id}<br>(e.g. gene_panels/impact341)                           |

**Note:** *.seg* files are kept in separate folders in order to preserve `reference_genome_id`.