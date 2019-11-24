* [Introduction](#introduction)
* [How is it supposed to work?](#workflow)

# [Introduction](introduction)
From release 3.1, cBioportal supports multiple reference genomes. This means that the hg19 datasets can co-exist with the hg38 ones in the same portal database. We also support mouse genomes (mm10), but it is not recommended to have both human and mouse data in the same instance of cBioPortal.

This document outlines the steps to support multiple reference genomes in your instance of cBioPortal.

# [How is it supposed to work?](workflow)
1. [Import Reference Genome](Import-reference-genome) into your cBioPortal database unless you launch a new instance of cBioPortal and restore your database from a seed database. If the version of your portal instance is lower than 3.1, you will have to [update your cBioPortal installation](Updating-your-cBioPortal-installation) 
and migrate your database schema to the latest version. The migrartion script by default will add three reference genomes (hg19, hg38, mm10) to the database. 
2. [Update the reference genome gene database table](Updating-gene-and-gene_alias-tables) to include the genes from the reference genome of interest to the database. You will also need to update both gene and gene alias tables in order to support other species such as a mouse.
3. [Import Cancer Study](import-cancer-study) of a given reference genome into the portal database. You will need to add a new propertity of **reference genome** to your meta_study file. You must use **ucsc** and **ncbi** options 
if your study is profiled by a reference geome other than a default genome value listed in your portal properties file.

**Important Note**
* Add the following default values to your portal.properties file. Those default genome values will be used by the validation script when importing a new study.
```# species and genomic information
   species=human
   ncbi.build=GRCh37
   ucsc.build=hg19
```
* You can also overwrite default genome values by using the following options of the importer script. _**ucsc**_ and _**ncbi**_ are both required when importing a dataset profiled with a reference genome different from a default genome value listed in your portal properties file.
```
  -species SPECIES, --species SPECIES
                        species information (default: assumed human)
  -ucsc UCSC_BUILD_NAME, --ucsc_build_name UCSC_BUILD_NAME
                        UCSC reference genome assembly name (default: assumed
                        hg19)
  -ncbi NCBI_BUILD_NUMBER, --ncbi_build_number NCBI_BUILD_NUMBER
                        NCBI reference genome build number (default: assumed
                        GRCh37 for UCSC reference genome build hg19)
```
For instance, to load a hg38 dataset:
```
core/src/main/scripts/importer/metaImport.py  -s /path/to/your/study -jar scripts.jar -ucsc hg38 -ncbi GRCh38 -n -o

```

