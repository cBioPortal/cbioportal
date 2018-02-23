# Add new reference genome to the database
This manual is intended to guide you to load new reference genome(s) to the database. 

### What is **Reference Genome** (also know as reference assembly)?
A digital nucleic acid sequence database assembled by scientists as a representative example of a species' set of genes. Reference genomes can be accessed online using dedicated browsers such as Ensembl or UCSC Genome Browser. 

### Why does reference genomes matter?
Raw reads from sample tissue are often aligned to a particular reference genome. Choose the same reference genome used by the alignment help us accurately identify the chromosomal location of the gene and the position of cyto-genetic bands within a chromosome. 

## How to load reference genome(s) to the database?

##1. prepare a tab delimited txt file with the following fields:
1. species: the name of organic species e.g human
2. name: the name of reference genome eg. hg38
3. build_name: the name of genome assembly e.g GRCh38
4. nonN_bases: the total number of non N bases
5. URL: the URL to download the reference genome 
6. release_date: the date when reference genome released, in a format of yyyy-mm-dd hh:mm:ss

here is a sample reference genome file:
```
#species	name	build_name	nonN_bases	URL	release_date
human	hg19	GRCh37	2897310462	http://hgdownload.cse.ucsc.edu/goldenPath/hg19	2009-02-01 00:00:00
human	hg38	GRCh38	3049315783	http://hgdownload.cse.ucsc.edu/goldenPath/hg38	2013-12-24 00:00:00
mouse	mm10	GRCm38	2652783500	http://hgdownload.cse.ucsc.edu//goldenPath/mm10	2011-12-01 00:00:00
```
##2. Using import script to load reference genome(s) data to the database:
```
 cd <your_cbioportal_dir>/core/src/main/scripts
 export PORTAL_HOME=<your_cbioportal_dir>
./importReferenceGenome.pl --ref-genome <your_reference_genome_file>
```
##3. output of the script runs successfully
```
$ ./importReferenceGenome.pl --ref-genome ~/myspace/cbioportal/core/src/test/resources/reference_genomes.txt
   Reading reference genome from:  /Users/kelsyzhu/myspace/cbioportal/core/src/test/resources/reference_genomes.txt
    --> total number of lines:  3
   
   Done. Restart tomcat to make sure the cache is replaced with the new data.
   
   Warnings / Errors:
   -------------------
   0.  New reference genome added; 3x
   Done.
   Total time:  3432 ms

```
