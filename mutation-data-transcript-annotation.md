This document describes how each mutation in cBioPortal gets annotated with a specific gene symbol + protein change. 

### Biological Background 

This section explains the concepts of protein isoforms and transcripts. 

#### What is an isoform? 

From a single gene (string of nucleotides) multiple protein sequences can be formed (string of amino acids). For 
example: parts of the gene that code for proteins (exons) can be included or excluded through a process known as [alternative splicing](https://en.wikipedia.org/wiki/Alternative_splicing). Each of the different resulting proteins is called an [isoform](https://en.wikipedia.org/wiki/Protein_isoform). A single mutation can impact the isoforms differently. E.g. in one isoform
it might change a P to a T, but in the other isoform that particular exon does not get included and it is therefore not 
changing the amino acid sequence at all. In cBioPortal for convenience sake we assign a single gene symbol + protein change to each mutation. For most cases this works well because there is only one protein isoform relevant in a clinical setting. There are of course exceptions and we are 
therefore working on improving this representation. An explanation of the relation between transcripts and protein 
isoforms can be found in the next section. 

#### What is a transcript? 

DNA is transcribed to a pre-mRNA transcript which includes intron and exon regions. Splicing and other processes then take place to form the resulting mature mRNA transcript that only contains exons, which subsequently can be translated to a protein sequence. An mRNA transcript can thus be associated with a specific protein isoform. The Ensembl database assigns ids for these transcript with names like `ENSTxxx`. You can see this on e.g. the Ensembl 
website for the [BRAF gene](https://grch37.ensembl.org/Homo_sapiens/Gene/Summary?g=ENSG00000157764;r=7:140719327-140924929#): 

<img width="995" alt="Screen Shot 2022-10-05 at 9 35 55 AM" src="https://user-images.githubusercontent.com/1334004/194073821-9a43cab2-3d31-40ab-b47e-517f1ce8bca3.png">

The transcript `ENST00000288602.6` is 2480 base pairs long (nucleotides ACGT) and the associated protein isoform is 766 
amino acids (V/P/etc). You can see we are showing that same transcript and protein isoform on [cBioPortal](https://bit.ly/3vaclXN): 

<img width="1052" alt="Screen Shot 2022-10-05 at 9 37 32 AM" src="https://user-images.githubusercontent.com/1334004/194074248-c3e205b4-c2e4-4e19-a541-17517dc07913.png"> 

For each gene name in cBioPortal a canonical/default transcript is assigned. These assignments are stored in [Genome 
Nexus](https://www.genomenexus.org/) and explained below. Although cBioPortal does not store changes to different 
transcripts/isoforms for each mutation in the database itself, it does allow viewing them on the [Mutations Tab](https://bit.ly/39hVtDd) by re-annotating the mutations on the fly through [Genome Nexus](https://www.genomenexus.org/) whenever a user clicks on the transcript dropdown. 

### Transcript Assignment 

The cBioPortal database stores one gene + protein change annotation for each mutation event in the database. To allow 
comparing mutation data across studies it is important to annotate the mutation data (be it in MAF or VCF format) in 
the same way, otherwise the gene + protein changes can mean entirely different things. For all public studies stored 
in [datahub](https://github.com/cBioPortal/datahub/tree/master/public) we leverage [Genome Nexus](https://www.genomenexus.org) to do so. 
Genome Nexus assigns one canonical Ensembl Transcript + gene name + protein change for 
each mutation. You can find the mapping of hugo symbol to transcript id [here](https://github.com/genome-nexus/genome-nexus-importer/blob/master/data/grch37_ensembl92/export/ensembl_biomart_canonical_transcripts_per_hgnc.txt). There are 
two sets of default transcripts: `uniprot` and `mskcc`. We recommend to use the `mskcc` set of transcripts when 
starting from scratch, since these are more up to date and correspond to transcripts that were chosen as relevant for 
clinical sequencing at MSKCC. The `uniprot` set of transcripts was constructed several years ago, but we are no longer 
certain about the logic on how to reconstruct them hence they are not being kept up to date. One can see the 
differences between the two in [this file](https://github.com/cBioPortal/cbioportal-frontend/files/9498680/genes_with_different_uniprot_mskcc_isoforms.txt). For the public cBioPortal (https:
//www.cbioportal.org) and [datahub](https://github.com/cBioPortal/datahub/tree/master/public) we are using `mskcc`, for the GENIE cBioPortal (https://genie.cbioportal.org) we still use `uniprot`. As of cBioPortal v5 the default is `mskcc` for local installations. Prior to v5 it was `uniprot`. We recommend that people upgrading to v5 consider migrating to `mskcc` as well (see [migration guide](https://docs.cbioportal.org/migration-guide/) and [the properties reference docs](https://docs.cbioportal.org/deployment/customization/application.properties-reference/#properties)).

#### How default transcript assignment affects the Mutations Tab 

The [Mutations Tab](https://bit.ly/39hVtDd) shows the full protein sequence. The one shown by default is the canonical 
transcript (`mskcc` or `uniprot` depending on configuration). The mutations are drawn on the lollipop based on the 
protein position found in the cBioPortal database. For the [public cBioPortal](https://cbioportal.org) all mutation 
data in MAF format are annotated using [Genome Nexus](https://www.genomenexus.org) to add the gene and protein change 
columns. This is then imported into the cBioPortal database. Whether you choose to use the set of `uniprot` or `mskcc` 
transcripts, make sure to indicate it in the [Genome Nexus Annotation Pipeline](https://github.com/genome-nexus/genome-
nexus-annotation-pipeline#maf-annotation)(`--isoform-override <mskcc or uniprot>`) when annotating as well as in [the properties file](https://docs.cbioportal.org/deployment/customization/application.properties-reference/#properties)
of cBioPortal. That way the [Mutations Tab](https://bit.ly/39hVtDd) will show the correct canonical 
transcript. Note that whenever somebody uses the dropdown on the Mutations Tab to change the displayed transcript, 
Genome Neuxs re-annotates all mutations on the fly. The browser sends over the genomic location (chrom,start,end,ref,
alt) to get the protein change information for each transcript. Since many of the annotations are for the canonical transcripts
only we are currently hiding annotations for non-canonical transcripts.

#### Plans for default transcripts 

We are planning to move to a single set of default transcripts over time. Prior to v5 `uniprot` was used for the public 
facing portals and local installations. Our plan is to use `mskcc` everywhere and eventually we will most likely move to [MANE](https://www.ensembl.org/info/genome/genebuild/mane.html). MANE is only 
available for grch38 and since most of our data is for grch37 this is currently not feasible. Whichever set of 
transcripts you choose to use, make sure to indicate so in the [Genome Nexus Annotation Pipeline](https://github.com/genome-nexus/genome-nexus-annotation-pipeline#maf-annotation) (`--isoform-override <mskcc or uniprot>`) and put the same 
set of transcripts in [the properties file](https://docs.cbioportal.org/deployment/customization/application.properties-reference/#properties) of cBioPortal, such that the [Mutations Tab](https://bit.ly/39hVtDd) will show the correct canonical transcript (currently defaults to `mskcc`). The re-annotation of mutations only happens once a user clicks to change the transcript, which is why it's important that the protein change in the database is for the specific transcript displayed first. 
