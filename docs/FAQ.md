# cBioPortal FAQs


* [General Questions](#general-questions)
    * [What is the cBioPortal for Cancer Genomics?](#what-is-the-cbioportal-for-cancer-genomics)
    * [How do I get started?](#how-do-i-get-started)
    * [What data types are in the portal?](#what-data-types-are-in-the-portal)
    * [What is the process of data curation?](#what-is-the-process-of-data-curation)
    * [How do I get updates on new portal developments and new data sets?](#how-do-i-get-updates-on-new-portal-developments-and-new-data-sets)
    * [Does the portal work on all browsers and operating systems?](#does-the-portal-work-on-all-browsers-and-operating-systems)
    * [How do I cite the cBioPortal?](#how-do-i-cite-the-cbioportal)
    * [Can I use figures from the cBioPortal in my publications or presentations?](#can-i-use-figures-from-the-cbioportal-in-my-publications-or-presentations)
    * [Can I save or bookmark my results in cBioPortal?](#can-i-save-or-bookmark-my-results-in-cbioportal)
    * [How is the cBioPortal for Cancer Genomics different from the Genomic Data Commons (GDC)?](#how-is-the-cbioportal-for-cancer-genomics-different-from-the-genomic-data-commons-gdc)
    * [Does the cBioPortal provide a Web Service API? R interface? MATLAB interface?](#does-the-cbioportal-provide-a-web-service-api-r-interface-matlab-interface)
    * [Can I use cBioPortal with my own data?](#can-i-use-cbioportal-with-my-own-data)
    * [Can I create a local instance of cBioPortal to host my own data?](#can-i-create-a-local-instance-of-cbioportal-to-host-my-own-data)
    * [I'd like to contribute code to the cBioPortal. How do I get started?](#id-like-to-contribute-code-to-the-cbioportal-how-do-i-get-started)
    * [What is a Virtual Study?](#what-is-a-virtual-study)
    * [Is it necessary to log in to use virtual studies? If I do log in, what additional functionality do I gain?](#is-it-necessary-to-log-in-to-use-virtual-studies-if-i-do-log-in-what-additional-functionality-do-i-gain)
    * [What is Group Comparison?](#what-is-group-comparison)
    * [Where did the Network tab go?](#where-did-the-network-tab-go)
* [Data Questions](#data-questions)
    * [General Data](#general-data)
        * [Does the portal contain cancer study X?](#does-the-portal-contain-cancer-study-x)
        * [Which resources are integrated for variant annotation?](#which-resources-are-integrated-for-variant-annotation)
        * [What version of the human reference genome is being used in cBioPortal?](#what-version-of-the-human-reference-genome-is-being-used-in-cbioportal)
        * [How does cBioPortal handle duplicate samples or sample IDs across different studies?](#how-does-cbioportal-handle-duplicate-samples-or-sample-ids-across-different-studies)
        * [Are there any normal tissue samples available through cBioPortal?](#are-there-any-normal-tissue-samples-available-through-cbioportal)
        * [How can I find which studies have mRNA expression data (or any other specific data type)?](#how-can-i-find-which-studies-have-mrna-expression-data-or-any-other-specific-data-type)
        * [Can I download all data at once?](#can-i-download-all-data-at-once)
        * [The data today is different than the last time I looked. What happened?](#the-data-today-is-different-than-the-last-time-I-looked-what-happened)
        * [How do I access data from AACR Project GENIE?](#how-do-i-access-data-from-aacr-project-genie)
    * [TCGA](#tcga)
        * [How does TCGA data in cBioPortal compare to TCGA data in Genome Data Commons?](#how-does-tcga-data-in-cbioportal-compare-to-tcga-data-in-genome-data-commons)
        * [What happened to TCGA Provisional datasets?](#what-happened-to-tcga-provisional-datasets)
        * [What are TCGA Firehose Legacy datasets and how do they compare to the publication-associated datasets and the PanCancer Atlas datasets?](#what-are-tcga-firehose-legacy-datasets-and-how-do-they-compare-to-the-publication-associated-datasets-and-the-pancancer-atlas-datasets)
        * [Where do the thresholded copy number call in TCGA Firehose Legacy data come from?](#where-do-the-thresholded-copy-number-call-in-tcga-firehose-legacy-data-come-from)
        * [Which studies have MutSig and GISTIC results? How do these results compare to the data in the TCGA publications?](#which-studies-have-mutsig-and-gistic-results-how-do-these-results-compare-to-the-data-in-the-tcga-publications)
        * [How can I download the PanCancer Atlas data?](#how-can-i-download-the-pancancer-atlas-data)
    * [DNA (Mutations, Copy Number &amp; Fusions)](#dna-mutations-copy-number--fusions)
        * [Does the cBioPortal contain synonymous mutation data?](#does-the-cbioportal-contain-synonymous-mutation-data)
        * [What processing or filtering is applied to generate the mutation data?](#what-processing-or-filtering-is-applied-to-generate-the-mutation-data)
        * [What transcripts are used for annotating mutations?](#what-transcripts-are-used-for-annotating-mutations)
        * [How are protein domains in the mutational lollipop diagrams specified?](#how-are-protein-domains-in-the-mutational-lollipop-diagrams-specified)
        * [What is the difference between a “splice site” mutation and a “splice region” mutation?](#what-is-the-difference-between-a-splice-site-mutation-and-a-splice-region-mutation)
        * [What do “Amplification”, “Gain”, “Deep Deletion”, “Shallow Deletion” and "-2", "-1", "0", "1", and "2" mean in the copy-number data?](#what-do-amplification-gain-deep-deletion-shallow-deletion-and--2--1-0-1-and-2-mean-in-the-copy-number-data)
        * [What is GISTIC? What is RAE?](#what-is-gistic-what-is-rae)
    * [RNA](#rna)
        * [Does the portal store raw or probe-level data?](#does-the-portal-store-raw-or-probe-level-data)
        * [What are mRNA and microRNA Z-Scores?](#what-are-mrna-and-microrna-z-scores)
        * [Is there any normal RNA-seq data in cBioPortal?](#is-there-any-normal-rna-seq-data-in-cbioportal)
        * [How is TCGA RNASeqV2 processed? What units are used?](#how-is-tcga-rnaseqv2-processed-what-units-are-used)
        * [Is there microRNA data?](#is-there-microrna-data)
        * [How can I query microRNAs in the portal?](#how-can-i-query-micrornas-in-the-portal)
    * [Protein](#protein)
        * [How can I query phosphoprotein levels in the portal?](#how-can-i-query-phosphoprotein-levels-in-the-portal)
        * [Why isn’t there protein data for my gene of interest?](#why-isnt-there-protein-data-for-my-gene-of-interest)
    * [DNA Methylation](#dna-methylation)
        * [Which methylation probe is used for genes with multiple probes?](#which-methylation-probe-is-used-for-genes-with-multiple-probes)
    * [Clinical Data](#clinical-data)
        * [What kind of clinical data is stored in the portal?](#what-kind-of-clinical-data-is-stored-in-the-portal)
        * [What is the meaning of OS_STATUS / OS_MONTHS, and PFS_STATUS / PFS_MONTHS?](#what-is-the-meaning-of-os_status--os_months-and-pfs_status--pfs_months)
* [Analysis Questions](#analysis-questions)
    * [How can I query/explore a select subset of samples?](#how-can-i-queryexplore-a-select-subset-of-samples)
    * [How can I compare two or more subsets of samples?](#how-can-i-compare-two-or-more-subsets-of-samples)
    * [Is it possible to determine if a particular mutation is heterozygous or homozygous in a sample? When a sample has 2 mutations in one gene, is it possible to determine whether the mutations are in cis or in trans with each other?](#is-it-possible-to-determine-if-a-particular-mutation-is-heterozygous-or-homozygous-in-a-sample-when-a-sample-has-2-mutations-in-one-gene-is-it-possible-to-determine-whether-the-mutations-are-in-cis-or-in-trans-with-each-other)
    * [How can I query over/under expression of a gene?](#how-can-i-query-overunder-expression-of-a-gene)
    * [How can I compare outcomes in patients with high vs low expression of a gene?](#how-can-i-compare-outcomes-in-patients-with-high-vs-low-expression-of-a-gene)
* [Results View](#results-view)
    * [OncoPrint](#oncoprint)
        * [What are OncoPrints?](#what-are-oncoprints)
        * [Can I change the order of genes in the OncoPrint?](#can-i-change-the-order-of-genes-in-the-oncoprint)
        * [Can I visualize my own data within an OncoPrint?](#can-i-visualize-my-own-data-within-an-oncoprint)
        * [Why are some samples “Not profiled” for certain genes?](#why-are-some-samples-not-profiled-for-certain-genes)
    * [Other pages](#other-pages)
        * [Does the Mutual Exclusivity tab calculate its statistics using all samples/alterations or only a specific subset?](#does-the-mutual-exclusivity-tab-calculate-its-statistics-using-all-samplesalterations-or-only-a-specific-subset)
        * [What are the values of the box and whiskers in a boxplot?](#what-are-the-values-of-the-box-and-whiskers-in-a-boxplot)
* [Study View](#study-view)
    * [How to use filter in the URL of Study View page?](#how-to-use-filter-in-the-url-of-study-view-page)
* [What if I have other questions or comments?](#what-if-i-have-other-questions-or-comments)

## General Questions
#### What is the cBioPortal for Cancer Genomics?
The cBioPortal for Cancer Genomics is an open-access, open-source resource for interactive exploration of multidimensional cancer genomics data sets. The goal of cBioPortal is to significantly lower the barriers between complex genomic data and cancer researchers by providing rapid, intuitive, and high-quality access to molecular profiles and clinical attributes from large-scale cancer genomics projects, and therefore to empower researchers to translate these rich data sets into biologic insights and clinical applications.
#### How do I get started?
Check out our [tutorial slides](https://www.cbioportal.org/tutorials) to get started or go through our [tutorial paper](https://www.ncbi.nlm.nih.gov/pubmed/23550210).
#### What data types are in the portal?
The portal supports and stores non-synonymous mutations, DNA copy-number data (putative, discrete values per gene, e.g. "deeply deleted" or "amplified", as well as log2 or linear copy number data), mRNA and microRNA expression data, protein-level and phosphoprotein level data (RPPA or mass spectrometry based), DNA methylation data, and de-identified clinical data. For a complete breakdown of available data types per cancer study go to the [Data Sets Page](https://www.cbioportal.org/datasets). Note that for many studies, only somatic mutation data and limited clinical data are available. For TCGA studies, the other data types are also available. Germline mutations are supported by cBioPortal, but are, with a few exceptions, not available in the public instance.
#### What is the process of data curation?
The TCGA firehose legacy datasets are imported directly from the original TCGA Data Coordinating Center via the [Broad Firehose](https://gdac.broadinstitute.org/).

We are also actively curating datasets from the literature. Studies from the literature were curated from the data published with the manuscripts. We sometimes reach out to the investigators to acquire additional data, such as clinical attributes. All mutation calls (in VCF or MAF format) are processed through an internal pipeline to annotate the variant effects in a consistent way across studies. Please [contact us](mailto:cbioportal@googlegroups.com) to suggest additional public datasets to curate or view the list of studies suggested for curation in our [Datahub on Github](https://github.com/cBioPortal/datahub/issues?q=is%3Aissue+is%3Aopen+label%3A%22new+public+study%22).
#### How do I get updates on new portal developments and new data sets?
Please subscribe to our low-volume [news mailing list](https://groups.google.com/group/cbioportal-news) or follow [@cbioportal on Twitter](https://twitter.com/cbioportal).
#### Does the portal work on all browsers and operating systems?
We support and test on the following web browsers: Safari, Google Chrome, Firefox 3.0 and above, as well as Internet Explorer 11.0 and above. If you notice any incompatibilities, please let us know.
#### How do I cite the cBioPortal?
Please cite the following portal papers:
* Cerami et al. The cBio Cancer Genomics Portal: An Open Platform for Exploring Multidimensional Cancer Genomics Data. Cancer Discovery. May 2012 2; 401. [PubMed](https://www.ncbi.nlm.nih.gov/pubmed/22588877).
* Gao et al. Integrative analysis of complex cancer genomics and clinical profiles using the cBioPortal. Sci. Signal. 6, pl1 (2013). [PubMed](https://www.ncbi.nlm.nih.gov/pubmed/23550210).

Remember to also cite the source of the data if you are using a publicly available dataset.
#### Can I use figures from the cBioPortal in my publications or presentations?
Yes, you are free to use any of the figures from the portal in your publications or presentations (many are available in SVG or PDF format for easier scaling and editing). When you do, please cite Cerami et al., Cancer Discov. 2012, and Gao et al., Sci. Signal. 2013 (see the previous question for full citations).
#### Can I save or bookmark my results in cBioPortal?
You can bookmark your query results and share the URL with collaborators. We store all queries via Session IDs, and these are saved indefinitely. Use the bookmark tab to retrieve the full link, or generate a short link via the bit.ly link generator.
#### How is the cBioPortal for Cancer Genomics different from the Genomic Data Commons (GDC)?
The cBioPortal is an exploratory analysis tool for exploring large-scale cancer genomic data sets that hosts data from large consortium efforts, like [TCGA](https://cancergenome.nih.gov/) and [TARGET](https://ocg.cancer.gov/programs/target), as well as publications from individual labs. You can quickly view genomic alterations across a set of patients, across a set of cancer types, perform survival analysis and perform group comparisons. If you want to explore specific genes or a pathway of interest in one or more cancer types, the cBioPortal is probably where you want to start.

By contrast, the [Genomic Data Commons (GDC)](https://gdc.cancer.gov/) aims to be the definitive place for full-download and access to all data generated by TCGA and TARGET. If you want to download raw mRNA expression files or full segmented copy number files, the GDC is probably where you want to start.
#### Does the cBioPortal provide a Web Service API? R interface? MATLAB interface?
Yes, the cBioPortal provides a [Swagger API](https://www.cbioportal.org/api/swagger-ui.html), and [R/MATLAB interfaces](https://docs.cbioportal.org/6.-web-api-and-clients/api-and-api-clients).
#### Can I use cBioPortal with my own data?
cBioPortal provides several options for analyzing your own data. Visit our [Visualize Your Data](https://www.cbioportal.org/visualize) page to generate an OncoPrint or Lollipop Plot with your own data. To utilize the entire suite of analysis and visualization tools, you can also install your own instance of cBioPortal (see next question).
#### Can I create a local instance of cBioPortal to host my own data?
Yes, the cBioPortal is open-source, and available on [GitHub](https://github.com/cBioPortal/cbioportal). Our [documentation](https://docs.cbioportal.org) provides complete download and installation instructions.
#### I'd like to contribute code to the cBioPortal. How do I get started?
Great! We would love to have your contributions. To get started, head over to our GitHub repository and check out our page on [how to contribute](https://github.com/cBioPortal/cbioportal/blob/master/CONTRIBUTING.md).
#### What is a Virtual Study?
A virtual study is a custom study comprised of samples from one or more existing studies. The virtual study feature allows you to define a custom cohort of samples that fit your specific genomic or clinical criteria of interest. These samples can be a subset of the data available in an existing study, or result from the combination of multiple existing studies. This cohort of samples can then be queried or explored just like a traditional study, and can be returned to at a later date or shared with a collaborator. For more information and examples, see our [tutorial on virtual studies](https://www.cbioportal.org/tutorials).
#### Is it necessary to log in to use virtual studies? If I do log in, what additional functionality do I gain?
No. A user that has not logged in can create virtual studies and run queries in those studies (by using the query box on the study summary page). Links to virtual studies are permanent, so you can save the link on your computer and come back to it anytime, or share it with others.

If you log in, you gain the ability to save your virtual study to the list of existing studies on the homepage. This makes a virtual study functionally the same as any other study: you can access your virtual studies in the query builder and you can combine an existing virtual study with any other study to create a new virtual study.
#### What is Group Comparison?
Group Comparison is a suite of analysis features which allows a user to compare clinical or genomic features of user-defined groups of samples. These groups can be defined based on any clinical or genomic features. For an overview, see [our tutorial on group comparison](https://www.cbioportal.org/tutorials).
#### Where did the Network tab go?
The Network tab was retired on November 1, 2019. We will soon release a new feature for Pathway Analysis on the portal. In the interim, [PCViz](https://www.pathwaycommons.org/pcviz/) provides similar functionality to the old Network tab. 


## Data Questions
### General Data
#### Does the portal contain cancer study X?
Check out the [Data Sets Page](https://www.cbioportal.org/datasets) for the complete set of cancer studies currently stored in the portal. If you do not see your specific cancer study of interest, please [contact us](mailto:cbioportal@googlegroups.com), and we will let you know if it's in the queue.
#### Which resources are integrated for variant annotation?
cBioPortal supports the annotation of variants from several different databases. These databases provide information about the recurrence of, or prior knowledge about, specific amino acid changes. For each variant, the number of occurrences of mutations at the same amino acid position present in the COSMIC database are reported. Furthermore, variants are annotated as “hotspots” if the amino acid positions were found to be recurrent linear hotspots, as defined by the Cancer Hotspots method ([cancerhotspots.org](https://www.cancerhotspots.org/)), or three-dimensional hotspots, as defined by 3D Hotspots ([3dhotspots.org](https://www.3dhotspots.org/)). Prior knowledge about variants, including clinical actionability information, is provided from three different sources: OncoKB ([www.oncokb.org](https://www.oncokb.org/)), CIViC ([civicdb.org](https://civicdb.org/)), as well as My Cancer Genome ([mycancergenome.org](https://www.mycancergenome.org/)). For OncoKB, exact levels of clinical actionability are displayed in cBioPortal, as defined by [the OncoKB paper](https://ascopubs.org/doi/full/10.1200/PO.17.00011).
#### What version of the human reference genome is being used in cBioPortal?
The [public cBioPortal](https://www.cbioportal.org) is currently using hg19/GRCh37.
#### How does cBioPortal handle duplicate samples or sample IDs across different studies?
The cBioPortal generally assumes that samples or patients that have the same ID are actually the same. This is important for cross-cancer queries, where each sample should only be counted once. If a sample is part of multiple cancer cohorts, its alterations are only counted once in the Mutations tab (it will be listed multiple times in the table, but is only counted once in the lollipop plot). However, other tabs (including OncoPrint and Cancer Types Summary) will count the sample twice - for this reason, we advise against querying multiple studies that contain the same samples (e.g., TCGA PanCancer Atlas and TCGA Firehose Legacy).
#### Are there any normal tissue samples available through cBioPortal?
No, we currently do not store any normal tissue data in our system.
#### How can I find which studies have mRNA expression data (or any other specific data type)?
Check out the [Data Sets Page](https://www.cbioportal.org/datasets) where you can view the complete set of cancer studies and sort by the number of samples with data available for any data type.
#### Can I download all data at once?
You can download all data for individual studies on the [Data Sets Page](https://www.cbioportal.org/datasets) or the study view page for the study of interest. You can also download all studies from our [Data Hub](https://github.com/cBioPortal/datahub).
#### The data today is different than the last time I looked. What happened?
We do occasionally update existing datasets to provide the most up-to-date, accurate and consistent data possible. The data you see today is likely an improved version of what you have seen previously. However, if you suspect that there is an error in the current version, please let us know at [cbioportal@googlegroups.com](mailto:cbioportal@googlegroups.com).

If you need to reference an old version of a dataset, you can find previous versions in our [Datahub repository](https://github.com/cBioPortal/datahub/tree/master/public).
#### How do I access data from AACR Project GENIE?
Data from AACR Project GENIE are provided in a [dedicated instance of cBioPortal](https://www.cbioportal.org/genie/). You can also download GENIE data from the [Synapse Platform](https://synapse.org/genie). Note that you will need to register before accessing the data. Additional information about AACR Project GENIE can be found on the [AACR website](https://www.aacr.org/Research/Research/Pages/aacr-project-genie.aspx).

### TCGA
#### How does TCGA data in cBioPortal compare to TCGA data in Genome Data Commons?
We do not currently load the mutation data from the GDC. Instead, we have the original mutation data generated by the individual TCGA sequencing centers. The source of the data is the Broad Firehose (or the publication pages for data that matches a specific manuscript). These data are usually a combination of two mutation callers, but they differ by center (typically a variant caller like MuTect plus an indel caller), and sequencing centers have modified their mutation calling pipelines over time.
#### What happened to TCGA Provisional datasets?
We renamed TCGA Provisional datasets to TCGA Firehose Legacy to better reflect that this data comes from a legacy processing pipeline. The exact same data is now available in TCGA Firehose Legacy studies.
#### What are TCGA Firehose Legacy datasets and how do they compare to the publication-associated datasets and the PanCancer Atlas datasets?
The Firehose Legacy dataset (formerly Provisional datasets) for each TCGA cancer type contains all data available from the Broad Firehose. The publication datasets reflect the data that were used for each of the publications. The samples in a published dataset are usually a subset of the firehose legacy dataset, since manuscripts were often written before TCGA completed their goal of sequencing 500 tumors.

There can be differences between firehose legacy and published data. For example, the mutation data in the publication usually underwent more QC, and false positives might have been removed or, in rare cases, false negatives added. RNA-Seq and copy-number values may also differ slightly, as different versions of analysis pipelines could have been used. Additionally, due to additional curation during the publication process, the clinical data for the publication may be of higher quality or may contain a few more data elements, sometimes derived from the genomic data (e.g., genomic subtypes).

The TCGA PanCancer Atlas datasets derive from an effort to unify TCGA data across all tumor types. Publications resulting from this effort can be found at the [TCGA PanCancer Atlas site](https://www.cell.com/pb-assets/consortium/pancanceratlas/pancani3/index.html). In the cBioPortal, data from the PanCancer Atlas is divided by tumor type, but these studies have uniform clinical elements, consistent processing and normalization of mutations, copy number, mRNA data and are ideally processed for comparative analyses.
#### Where do the thresholded copy number call in TCGA Firehose Legacy data come from?
Thresholded copy number calls in the TCGA Firehouse Legacy datasets are generated by the GISTIC 2.0 algorithm and obtained from the Broad Firehose.
#### Which studies have MutSig and GISTIC results? How do these results compare to the data in the TCGA publications?
MutSig and GISTIC results about the statistical significance of recurrence of mutations and copy-number alterations in specific genes are available for many TCGA studies. The MutSig and GISTIC results reported in cBioPortal are based on the same mutations and copy number data reported in each TCGA publication, or the Broad Firehose for the firehose legacy data sets. However, the publication may or may not have included the complete MutSig and GISTIC output, and therefore there may be some discrepancies between the publication and the data in cBioPortal.
#### How can I download the PanCancer Atlas data?
PanCancer Atlas data can be downloaded on a study-by-study basis from cBioPortal through the [Datasets page](https://www.cbioportal.org/datasets) or our [DataHub](https://github.com/cBioPortal/datahub/tree/master/public). To download all cancer types together, try the [Genomic Data Commons PanCancer Atlas page](https://gdc.cancer.gov/about-data/publications/pancanatlas).

### DNA (Mutations, Copy Number & Fusions)
#### Does the cBioPortal contain synonymous mutation data?
No, the cBioPortal does not currently support synonymous mutations. This may change in the future, but we have no plans yet to add this feature.
#### What processing or filtering is applied to generate the mutation data?
Within cBioPortal, we utilize the mutation calls as provided by each publication. We do not perform any additional filtering. The only processing we do is to standardize the annotation of the mutations using [Genome Nexus](https://genomenexus.org) (which utilizes [VEP](https://useast.ensembl.org/info/docs/tools/vep/index.html) with the [canonical UniProt transcript](https://github.com/mskcc/vcf2maf/blob/master/data/isoform_overrides_uniprot)). For specifics of which tools were used to call mutations and filters that may have been applied, refer to the publication manuscript.
#### What transcripts are used for annotating mutations?
Prior to loading a study into cBioPortal, we run all mutation data through a standard pipeline (see above), which re-annotates all mutations to the [canonical UniProt transcript](https://github.com/mskcc/vcf2maf/blob/master/data/isoform_overrides_uniprot),
#### How are protein domains in the mutational lollipop diagrams specified?
Protein domain definitions come from [PFAM](https://pfam.xfam.org/).
#### What is the difference between a “splice site” mutation and a “splice region” mutation?
A “splice site” mutation occurs in an intron, in a splice acceptor or donor site (2bp into an intron adjacent to the intron/exon junction), defined by [Sequence Ontology](https://www.sequenceontology.org/browser/current_svn/term/SO:0001629). “Splice region” mutations are mutations that occur near the intron/exon junction, defined by [Sequence Ontology](https://www.sequenceontology.org/browser/current_svn/term/SO:0001630). While synonymous mutations are generally excluded from cBioPortal, these “splice region” synonymous mutations are included due to their potential impact on splicing.
#### What do “Amplification”, “Gain”, “Deep Deletion”, “Shallow Deletion” and "-2", "-1", "0", "1", and "2" mean in the copy-number data?
These levels are derived from copy-number analysis algorithms like GISTIC or RAE, and indicate the copy-number level per gene:
* -2 or Deep Deletion indicates a deep loss, possibly a homozygous deletion
* -1 or Shallow Deletion indicates a shallow loss, possibley a heterozygous deletion
* 0 is diploid
* 1 or Gain indicates a low-level gain (a few additional copies, often broad)
* 2 or Amplification indicate a high-level amplification (more copies, often focal)

Note that these calls are putative. We consider the deep deletions and amplifications as biologically relevant for individual genes by default. Note that these calls are usually not manually reviewed, and due to differences in purity and ploidy between samples, there may be false positives and false negatives.
#### What is GISTIC? What is RAE?
Copy number data sets within the portal are often generated by the [GISTIC](https://www.ncbi.nlm.nih.gov/sites/entrez?term=18077431) or [RAE](https://www.ncbi.nlm.nih.gov/sites/entrez?term=18784837) algorithms. Both algorithms attempt to identify significantly altered regions of amplification or deletion across sets of patients. Both algorithms also generate putative gene/patient copy number specific calls, which are then input into the portal.

For TCGA studies, the table in allthresholded.bygenes.txt (which is the part of the GISTIC output that is used to determine the copy-number status of each gene in each sample in cBioPortal) is obtained by applying both low- and high-level thresholds to to the gene copy levels of all the samples. The entries with value +/- 2 exceed the high-level thresholds for amplifications/deep deletions, and those with +/- 1 exceed the low-level thresholds but not the high-level thresholds. The low-level thresholds are just the 'ampthresh' and 'delthresh' noise threshold input values to GISTIC (typically 0.1 or 0.3) and are the same for every thresholds.

By contrast, the high-level thresholds are calculated on a sample-by-sample basis and are based on the maximum (or minimum) median arm-level amplification (or deletion) copy number found in the sample. The idea, for deletions anyway, is that this level is a good approximation for hemizygous losses given the purity and ploidy of the sample. The actual cutoffs used for each sample can be found in a table in the output file sample_cutoffs.txt. All GISTIC output files for TCGA are available at: gdac.broadinstitute.org.

### RNA
#### Does the portal store raw or probe-level data?
No, the portal only contains gene-level data. Data for different isoforms of a given gene are merged. Raw and probe-level data for data sets are available via [NCBI GEO](https://www.ncbi.nlm.nih.gov/geo/), [dbGaP](https://www.ncbi.nlm.nih.gov/gap/) or through the [GDC](https://portal.gdc.cancer.gov/). See the cancer type description on the main query page or refer to the original publication for links to the raw data.
#### What are mRNA and microRNA Z-Scores?
For mRNA and microRNA expression data, we typically compute the relative expression of an individual gene in a tumor sample to the gene's expression distribution in a reference population of samples. That reference population is all samples that are diploid for the gene in question (by default for mRNA), or normal samples (when specified, currently only in _Prostate Adenocarcinoma (MSKCC, Cancer Cell 2010)_), or all profiled samples . The returned value indicates the number of standard deviations away from the mean of expression in the reference population (Z-score). The normalization method is described [here](https://github.com/cBioPortal/cbioportal/blob/master/docs/Z-Score-normalization-script.md). Please note that the expression results by querying a gene with the default setting (z-score threshold of 2) oftentimes are not meaningful. Since the z-scores were usually calculated compared to other tumor samples, high or low expression does not necessarily mean that the gene is expressed irregularly in tumors. The data is useful for correlation analysis, for example, pick a threshold based on overall expression (using Plots tab) and compare survival data between expression high and low groups.
#### Is there any normal RNA-seq data in cBioPortal?
No, we currently do not store any expression data from normal tissue samples in our system.
#### How is TCGA RNASeqV2 processed? What units are used?
RNASeqV2 from TCGA is processed and normalized using [RSEM](https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-12-323). Specifically, the RNASeq V2 data in cBioPortal corresponds to the rsem.genes.normalized_results file from TCGA. A more detailed explanation of RSEM output can be found [here](https://www.biostars.org/p/106127/). cBioPortal then calculates z-scores as described above in [What are mRNA and microRNA Z-Scores?](#what-are-mrna-and-microrna-z-scores)
#### Is there microRNA data?
We have microRNA data for only a few studies and they are not up to date. To download more updated miRNA data, please go to either [Broad Firehose](https://gdac.broadinstitute.org/), or [GDC](https://portal.gdc.cancer.gov/).
#### How can I query microRNAs in the portal?
You can input either precursor or mature miRNA IDs. Since one precursor ID may correspond to multiple mature IDs and vise versa, the portal creates one internal ID for each pair of precursor ID and mature ID mapping. For example, an internal ID of MIR-29B-1/29B stands for precursor microRNA hsa-mir-29b-1 and mature microRNA hsa-miR-29b. After entering a precursor or mature ID, you will be asked to select one internal ID for query and that internal ID will also be displayed in the Oncoprint.

### Protein
#### How can I query phosphoprotein levels in the portal?
You need to input special IDs for each phosphoprotein/phopshosite such as AKT1_pS473 (which means AKT1 protein phosphorylated at serine residue at position 473). You could also input aliases such as phosphoAKT1 or phosphoprotein, and the portal will ask you to select the phosphoprotein/phosphosite of your interest. Note that phosphoprotein data is only available for select studies and for a limited number of proteins / phosphorylation sites.
#### Why isn’t there protein data for my gene of interest?
Most of the protein expression data in cBioPortal comes from assays like RPPA which only interrogate a subset of all proteins. TCGA ovarian, breast, and colorectal firehose legacy studies also have mass-spectrometry-based proteomics data from [CPTAC](https://proteomics.cancer.gov/programs/cptac) which cover more genes/proteins.
### DNA Methylation
#### Which methylation probe is used for genes with multiple probes?
For genes with multiple probes (usually from the Infinium arrays), we only include methylation data from the probe with the strongest negative correlation between the methylation signal and the gene's expression in the study (TCGA only).

### Clinical Data
#### What kind of clinical data is stored in the portal?
The portal currently stores de-identified clinical data, such as gender, age, tumor type, tumor grade, overall and disease-free survival data, when available. The available clinical data will differ from study to study.
#### What is the meaning of OS_STATUS / OS_MONTHS, and PFS_STATUS / PFS_MONTHS?
OS_STATUS means overall survival status ("0" -> "living" or "1" -> "deceased") and OS_MONTHS indicates the number of months from time of diagnosis to time of death or last follow up. PFS refers to “progression free survival”, indicating whether patient’s disease has recurred/progressed (PFS_STATUS), and at what time the disease recurred or the patient was last seen (PFS_MONTHS).

## Analysis Questions
#### How can I query/explore a select subset of samples?
cBioPortal allows you to run a query or explore study view using a user-specified list of samples/patients.

The first step is to define your sample set. There are two slightly different approaches you can take to defining your sample set, depending on whether you are selecting based on a positive criteria (samples with TP53 mutations) or a negative criteria (samples without a KRAS mutation).

Let’s take the positive criteria example first. Run a query for TP53 mutations using OQL (TP53: MUT) in your study of interest. Click over to the “Download” tab. In the table at the top, find the row that starts with “Samples affected”, and either Copy or Download that list. This is your list of samples that have a TP53 mutation.

Now for the negative criteria example. This also begins by using OQL to run a query for KRAS mutations (KRAS: MUT) in your study of interest. Click over to the “Download” tab. Look at the table at the top again, but this time find the row that starts with “Sample matrix”. Copy or download this data and open it in Excel. You will see a two column table that indicates whether a given sample is altered or not, indicated by 0 or 1. Sort by the second column and then copy all the sample IDs from the first column that have a 0 in the second column. This is your list of samples that do not have a KRAS mutation.

With a sample list in hand, you can now either run a query in just the selected samples (select “User-defined Case List” in the “Select Patient/Case Set:” dropdown) or explore this set of patients in study view (click “Select cases by IDs” and then create a Virtual Study restricted to just those samples).

For more information about OQL, see the [specification page](https://www.cbioportal.org/oql) or view the [tutorial slides](https://www.cbioportal.org/tutorials#oql). For more information about virtual studies, read [this FAQ](https://www.cbioportal.org/faq#what-is-a-virtual-study) or view the [tutorial slides](https://www.cbioportal.org/tutorials).
#### How can I compare two or more subsets of samples?
cBioPortal has a suite of analysis tools to enable comparisons between user-defined groups of samples/patients. For an overview of this functionality, see our [tutorial on group comparison](https://www.cbioportal.org/tutorials).
#### Is it possible to determine if a particular mutation is heterozygous or homozygous in a sample? When a sample has 2 mutations in one gene, is it possible to determine whether the mutations are in cis or in trans with each other?
There is currently no way to definitively determine whether a mutation is heterozygous/homozygous or in cis/trans with another mutation. However, you can try to infer the status of mutations by noting the copy number status of the gene and the variant allele frequency of the mutation(s) of interest relative to other mutations in the same sample. The cBioPortal patient/sample view can help you accomplish this.

Specifically in the case of TCGA samples with two mutations in the same gene, you can also obtain access to the aligned sequencing reads from the [GDC](https://portal.gdc.cancer.gov/) and check if the mutations are in cis or in trans (if the mutations are close enough to each other).
#### How can I query over/under expression of a gene?
cBioPortal supports Onco Query Language (OQL) which can be used to query over/under expression of a gene. When writing a query, select an mRNA expression profile. By default, samples with expression z-scores >2 or <-2 in any queried genes are considered altered. Alternate cut-offs can be defined using OQL, for example: "EGFR: EXP>2" will query for samples with an EGFR expression z-score >2. Review for the OQL [specification page](https://www.cbioportal.org/oql) or [tutorial slides](https://www.cbioportal.org/tutorials#oql) for more specifics and examples.
#### How can I compare outcomes in patients with high vs low expression of a gene?
To compare outcomes in patients with high vs low expression of a gene (excluding those patients with intermediate levels of expression), we will follow a 2 step process that builds on the approach described above in [How can I query/explore a select subset of samples?](#how-can-i-queryexplore-a-select-subset-of-samples), utilizing [OQL](https://www.cbioportal.org/oql) to first identify and then stratify that cases of interest.

First, identify the sample set using OQL. For example, to stratify patients based on expression of EGFR, add an mRNA profile to the query, and write "EGFR: EXP>2 EXP<-2" in the gene set box. After running the query, go to the Download tab and copy/download the “Samples affected” list.

Second, return to the homepage and paste the list of sample IDs from the previous step into the “User-defined Case List” in the “Select Patient/Case Set:” dropdown. This query will now only look at samples with high or low expression. To now stratify into high vs low for survival analysis, enter "EGFR: EXP>2" in the gene set box (don’t forget to select the same mRNA profile). Run the query and click over to the Survival tab. The “cases with alteration” are patients with high expression of EGFR and the cases without alteration are those with low expression of EGFR.

We use 2 and -2 as example thresholds above, but it is also a good idea to look at the distribution of expression data and select a threshold based on that. Plots tab can be useful for analyzing the expression distribution.
## Results View
### OncoPrint
#### What are OncoPrints?
OncoPrints are compact means of visualizing distinct genomic alterations, including somatic mutations, copy number alterations, and mRNA expression changes across a set of cases. They are extremely useful for visualizing gene set and pathway alterations across a set of cases, and for visually identifying trends, such as trends in mutual exclusivity or co-occurrence between gene pairs within a gene set. Individual genes are represented as rows, and individual cases or patients are represented as columns.

![image](https://cloud.githubusercontent.com/assets/840895/24209131/d34e33b2-0efb-11e7-945a-fd2b3c66d195.png)

#### Can I change the order of genes in the OncoPrint?
By default, the order of genes in the OncoPrint will be the same as in your query. You can change the order by (a) clicking on the gene name and dragging it up/down or (b) clicking on the three vertical dots next to the gene name to move the gene up/down.
#### Can I visualize my own data within an OncoPrint?
Yes, check out the OncoPrinter tool on our [Visualize Your Data page](https://www.cbioportal.org/visualize).
#### Why are some samples “Not profiled” for certain genes?
Some studies include data from one or more targeted sequencing platforms which do not include all genes. For samples sequenced on these smaller panels, cBioPortal will indicate that a particular gene was not included on the sequencing panel used for that sample. Alteration frequency calculations for each gene also take this information into account. Hover over a sample in OncoPrint to see the gene panel name, and click on that gene panel name to view a list of the genes included on that panel.
### Other pages
#### Does the Mutual Exclusivity tab calculate its statistics using all samples/alterations or only a specific subset?
The calculations on the Mutual Exclusivity tab are performed using all samples included in the query. A sample is defined as altered or unaltered for each gene based on the [OQL](https://www.cbioportal.org/oql) utilized in the query - by default, this will be non-synonymous mutations, fusions, amplifications and deep deletions.
#### What are the values of the box and whiskers in a boxplot?
In boxplots on cBioPortal, the box is drawn from the 25th percentile (Q1) to the 75th percentile (Q3), with the horizontal line in between representing the median. Whiskers are drawn independently above and below the box, and will extend to the maximum or minimum data values, unless there are outlier values, in which case the whisker will extend to 1.5 * IQR (interquartile range = Q3-Q1). Outliers are defined as values that extend beyond 1.5 * IQR.

## Study View
### How to use filter in the URL of Study View page?
You can filter the study based on values of one attribute in the URL. For example, <https://www.cbioportal.org/study/summary?id=msk_impact_2017#filterJson={"clinicalDataFilters":[{"attributeId":"CANCER_TYPE","values":[{"value":"Melanoma"}]}]}>

filterJson is set in the url hash string. Here are the allowed parameters and format for it in filterJson:
```
{
	"caseLists": [
		["string"]
	],
	"clinicalDataFilters": [{
		"attributeId": "string",
		"values": [{
			"end": 0,
			"start": 0,
			"value": "string"
		}]
	}],
	"geneFilters": [{
		"geneQueries": [
			["string"]
		],
		"molecularProfileIds": ["string"]
	}],
	"genomicDataFilters": [{
		"hugoGeneSymbol": "string",
		"profileType": "string",
		"values": [{
			"end": 0,
			"start": 0,
			"value": "string"
		}]
	}],
	"genomicProfiles": [
		["string"]
	],
	"sampleIdentifiers": [{
		"sampleId": "string",
		"studyId": "string"
	}],
	"studyIds": ["string"]
}
```

## What if I have other questions or comments?
Please contact us at [cbioportal@googlegroups.com](mailto:cbioportal@googlegroups.com). Previous discussions about cBioPortal are available on the [user discussion mailing list](https://groups.google.com/group/cbioportal).
