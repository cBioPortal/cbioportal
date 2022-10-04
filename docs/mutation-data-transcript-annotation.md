## Mutation Data Transcript Annotation
This document describes how each mutation in cBioPortal gets assigned to a specific gene symbol + protein change.

### Biological Background
From a single gene (string of nucleotides) multiple protein sequences can be formed (string of amino acids). For example: parts of the gene that code for proteins (exons) 
can be included or excluded. Each of these different proteins is called an isoform. A single mutation can thus change the protein structure for each of 
these isoforms differently. In cBioPortal for convenience sake we assign a single gene symbol + protein change to each mutation. In many cases this works
well because there is only one protein isoform relevant in a clinical setting.

### Transcript Assignment
The cBioPortal database stores one gene + protein change annotation for each mutation event in the database. To allow comparing mutation data across studies
it is important to annotate the mutation data (be it in MAF or VCF format) in the same way. For all public studies stored in 
[datahub](https://github.com/cBioPortal/datahub/tree/master/public) we leverage  [Genome Nexus](https://www.genomenexus.org) to do so. Genome Nexus
assigns one canonical Ensembl Transcript + gene name + protein change for each mutation. You can find the mapping of hugo symbol to transcript id
[here](https://github.com/genome-nexus/genome-nexus-importer/blob/master/data/grch37_ensembl92/export/ensembl_biomart_canonical_transcripts_per_hgnc.txt).
For the public cBioPortal (https://www.cbioportal.org) we are using the ids in the `mskcc_canonical_transcript` column. For local installations and
the GENIE cBioPortal (https://genie.cbioportal.org) we are using the `uniprot_canonical_transcript` column. TODO: explain differences


