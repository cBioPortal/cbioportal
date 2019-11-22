# [Introduction](introduction)
From release 2.2 cBioportal supports multiple reference genomes. This means you can have the hg19 datasets coexist with the hg38 ones in the same portal database. It is also possible to reconfigure the existing cBioPortal instance so that human and mouse data can be imported to the same 
portal instance and shown in the UI.
The document outlines the steps you need to follow to have cBioPortal support multiple reference genomes.

# [How is it supposed to work?](workflow)
1. [Import Reference Genome](Import-reference-genome) into your cBioPortal database. If the version of your portal instance is lower than 2.2, you will have to [update your cBioPortal installation](Updating-your-cBioPortal-installation) 
and migrate your database schema to the latest. The migrartion script by default will add three reference genomes (hg19, hg38, mm10) to the database.
2. [Update the reference genome gene database table](Updating-gene-and-gene_alias-tables) to include the genes from the reference genome of interest to the database. You will also need to update both gene and gene alias tables in order to support other species such as mouse.
3. [Import Cancer Study](#import-cancer-study) of a given reference genome into the portal database. You will need to add a new propertity of **reference genome** to your meta_study file. You must use **ucsc** and **ncbi** options 
if your study is profiled by a reference geome other than a default genome value listed in your portal properties file.
* You can use the portal.properties file to give a default genome value as the following:
```# species and genomic information
 species=human
 ncbi.build=GRCh37
 ucsc.build=hg19
 ```
* You can also overwrite default genome values by using the following options of the importer script:
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
The **species** by default is human. You do not need to supply it unless you try to add datasets from other species such as a mouse. **ucsc** and **ncbi** are both required 
when loading hg38 datasets into the portal database. For instance, to load hg38 dataset:
```
core/src/main/scripts/importer/metaImport.py  -s /path/to/your/study -jar scripts.jar -ucsc hg38 -ncbi GRCh38 -n -o

```

