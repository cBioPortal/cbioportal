# September 26, 2014
* All TCGA data updated to the Firehose run of July 15, 2014

# May 21, 2014
* All TCGA data updated to the Firehose run of April 16, 2014

# May 12, 2014
* Improved study summary page including survival analysis based on clinical attributes
<br/>e.g. [TCGA Endometrial Cancer cohort](study.do?cancer_study_id=ucec_tcga_pub)
<br/>![Study view](images/previews/study_view.png)

# March 27, 2014
* New features:
    * Visualizing of mutations mapped on 3D structures (individual or multiple mutations, directly in the browser)
    * Gene expression correlation analysis (find all genes with expression correlation to your query genes)
    * The Patient-Centric View now displays mutation frequencies across all cohorts in cBioPortal for each mutation
    * The Mutation Details Tab and the Patient-Centric View now display the copy-number status of each mutation
<br/>![3D viewer & Co-expression](images/previews/news_3d_coexp.png)

# March 18, 2014
* All TCGA data updated to the Firehose run of January 15, 2014
* Updated to the latest COSMIC data (v68)
* Added two new provisional TCGA studies
    * Adrenocortical Carcinoma
    * Uterine Carcinosarcoma
* Added genomic data from multiple studies from the literature

# November 8, 2013

* All TCGA data updated to the Firehose run of September 23, 2013.
* Updated to the latest COSMIC data (v67).

# October 18, 2013
 
* Improved interface for survival plots, including information on individual samples via mouse-over
* New fusion glyph in OncoPrints &nbsp;&nbsp;&nbsp;[![FGFR3 fusions in head and neck carcinoma](images/previews/fusion-in-oncoprint.png)](index.do?cancer_study_id=hnsc_tcga_pub&genetic_profile_ids_PROFILE_MUTATION_EXTENDED=hnsc_tcga_pub_mutations&genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION=hnsc_tcga_pub_gistic&Z_SCORE_THRESHOLD=2.0&RPPA_SCORE_THRESHOLD=2.0&data_priority=0&case_set_id=hnsc_tcga_pub_sequenced&case_ids=&gene_set_choice=user-defined-list&gene_list=FGFR3%3A+AMP+MUT%3B%0D%0A%0D%0A%0D%0A%0D%0A%0D%0A%0D%0A&clinical_param_selection=null&tab_index=tab_visualize&Action=Submit)
* Improved cross-cancer query: new alteration frequency histogram (example below - query gene: CDKN2A) and mutation diagram
<center>![Cross Cancer Query](images/previews/cross_cancer.png)</center>

# September 5, 2013:

* Updated COSMIC data (v66 Release)
* Improved / interactive visualization on the "Protein changes" tab
* Enhanced mutation diagrams: color-coding by mutation time and syncing with table filters
* Addition of DNA cytoband information in the patient view of copy-number changes
* OncoPrints now allow the display of an optional track with clinical annotation (Endometrial cancer example below)
<center>![Oncoprint with clinical track](images/previews/oncoprint_clinical_track.png)</center>

# July 23, 2013

* Multi-gene correlation plots.
* Variant allele frequency distribution plots for individual tumor samples.
* Tissue images for TCGA samples in the patient view, via [Digital Slide Archive](http://cancer.digitalslidearchive.net/). [Example](case.do?cancer_study_id=ucec_tcga&case_id=TCGA-BK-A0CC#images).

# July 16, 2013

* All TCGA data updated to the May Firehose run (May 23, 2012).
    * TCGA Pancreatic Cancer study (provisional) added.

# July 4, 2013

* Improved rendering of mutation diagrams, including ability to download in PDF format.
* Improved home page: Searchable cancer study & gene set selectors, data sets selector.

# June 13, 2013

* Improved interface for correlation plots, including information on individual samples via mouse-over.
* Gene Details from Biogene are now available in the Network view.

# June 4, 2013

* All TCGA data updated to the April Firehose run (April 21, 2012).

# April 28, 2013

* All TCGA data updated to the March Firehose run (March 26, 2012).

# April 2, 2013

* All TCGA data updated to the February Firehose run (February 22, 2012).
* mRNA percentiles for altered genes shown in patient view.

# March 26, 2013

* All TCGA data updated to the __January Firehose run__ (January 16, 2012).

# January 21, 2013

* All data updated to the __October Firehose run__ (October 24, 2012), plus updated mutation data for many tumor types.
* __Sequencing read counts and frequencies__ are now shown in the Mutation Details table when available.
* __Support for gene fusions__ in lung adenocarcinoma (RET/ROS1/ALK fusions are shown as mutations, other tumor types coming soon).
* Improved visualization of drugs and clinical trials for individual patients
* Improved OncoPrints, resulting in performance improvements

# November 21, 2012

* Major new feature: Users can now visualize __genomic alterations and clinical data of individual tumors__, including:
    - Summary of __mutations__ and __copy-number alterations__ of interest
    - __Clinical trial__ information
    - TCGA __Pathology Reports__
* New __cancer summary view__ (Example [Endometrial Cancer](study.do?cancer_study_id=ucec_tcga))
* __Updated drug data__ from KEGG DRUG and NCI Cancer Drugs (aggregated by [PiHelper](https://bitbucket.org/armish/pihelper))

# October 10, 2012

* All data updated to the __Broad Firehose__ run from July 25, 2012.
* __COSMIC data__ added to Mutation Details (via Oncotator)
* All predicted functional impact scores are updated to __Mutation Assessor 2.0__
* Users can now base queries on genes in recurrent regions of copy-number alteration (from __GISTIC__ via Firehose).
* New studies added: Cancer Cell Line Encyclopedia (CCLE) and Lung Adenocarcinoma from the Broad Institute.
* The [Onco Query Language (OQL)](onco_query_lang_desc.jsp) now supports queries for specific mutations or mutation types.

# July 18, 2012

* All data updated to the __latest Broad Firehose run__ (May 25, 2012).
* __Drug information__ added to the network view (via Drugbank).
* __Improved cross-cancer__ queries: Option to select data types, export of summary graphs.
* Users can now base queries on frequently mutated genes (from __MutSig__ via Firehose).

# May 16, 2012

* All data updated to the __latest Broad Firehose run__ (March 21, 2012).
* Extended __cross-cancer__ functionality, enabling users to query across all cancer studies in our database.
* New __"build a case"__ functionality, enabling users to generate custom case sets, based on one or more clinical attributes.
* New OncoPrint features, including more __compact OncoPrints__, and support for __RPPA__ visualization.

# February 27, 2012

* All data updated to the __latest Firehose run__ (January 24).
* Validated mutation data for colorectal cancer.
* Updated mutation data for several TCGA studies.
* New feature: __Mutation Diagrams__ that show mutations in the context of protein domains.
<center>![TP53 Mutations in Ovarian Cancer](images/previews/tp53_mutations.png)</center>

# January 26, 2012

* All data updated to the __latest Firehose run__ (December 30).
* __New RNA-Seq expression data__ for many tumor types.
* __New mutation data__: All new bladder cancer data and small updates for lung squamous carcinoma.

# January 5, 2012

* Now __19 tumor types__, all data updated to the __latest Firehose run__ (November 28).
* Support for __microRNA__ expression and copy-number (including multiple loci).
* New __gene symbol validation__ service.  You can now use gene aliases and/or Entrez Gene IDs within your gene sets.
* Improved __links to IGV__ for visualization of DNA copy-number changes.
* Background information from the [Sanger Cancer Gene Census](http://www.sanger.ac.uk/genetics/CGP/Census/).
* Two __new [Tutorials](tutorial.jsp)__ to get you quickly started in using the portal.

# December 5, 2011

New data types and updated data:

* Added support for RNA-Seq expression data (COADREAD, KIRC, LUSC).
* Added support for log2 copy-number data (available on the Plots tab).
* Updated LAML and LUSC MAF files.
* Updated LUSC case lists (reflecting the manuscript sample set).

# November 15, 2011

Another major update, with the following new features and data:

* In collaboration with Bilkent University, we have added a new *network* tab to our results pages.  The network tab enables users to visualize, analyze and filter cancer genomic data in the context of pathways and interaction networks derived from [Pathway Commons](http://www.pathwaycommons.org).
* New and improved mutation details, with sorting and filtering capabilities.
* Survival analysis - assess survival differences between altered and non-altered patient sets.
* Links to the [Integrative Genomics Viewer (IGV)](http://www.broadinstitute.org/igv/) to view copy-number alterations.
* All data updated to the October 2011 TCGA Broad Firehose run.
* RPPA data is now available for 2154 samples in six tumor types.
* Many new and updated MAF files - now 9 tumor types with mutation data.

![GBM Network](images/previews/ova_network.png)

# October 18, 2011

This release adds the following new data:

* Based on the September Firehose run with new tumor types (Bladder, Liver, Low grade glioma).
* Latest MAF files from the COADREAD and LUSC analysis working groups.
* Mutation Assessor links for all missense mutations.
* RPPA data for kidney clear cell and endometrial cancer.
* GBM case lists for transcriptional subtypes and CIMP.

# September 12, 2011

This is a major update, which includes the following main changes:

* New tumor types and data: Now ~3500 samples from 12 tumor types, including mutation data for 8 tumor types.
* RPPA data support - the Portal currently has data for ovarian and breast cancer.
* Access control via [OpenID](http://openid.net/).
* The code for the cBio Portal has now been fully open sourced, and made available at [Google Code](http://code.google.com/p/cbio-cancer-genomics-portal/).  If you would like to join our open source efforts and make the portal even better, drop us an email.

# April 26, 2011

This is the initial release of the cBio TCGA Cancer Genomics Portal. The portal contains data from all completed and ongoing TCGA projects. All data is automatically imported from the output of the Broad Firehose.

This release includes data on 2531 samples from 11 tumor types.
