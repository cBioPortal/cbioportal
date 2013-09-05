# September 5, 2013:

* [New standalone oncoprint tool](tools.jsp)
* Updated COSMIC data (v66 Release)
* Improved / interactive visualization on the "Protein changes" tab
* Enhanced mutation diagrams: color-coding by mutation time and syncing with table filters
* Addition of DNA cytoband information in the patient view of copy-number changes
* OncoPrints now allow the display of an optional track with clinical annotation (Endometrial cancer example below)
<center>![Oncoprint with clinical track](images/previews/oncoprint_clinical_track.png)</center>

# June 4, 2013

* All TCGA data updated to the April Firehose run (April 21, 2012).

# April 28, 2013

* All TCGA data updated to the March Firehose run (March 26, 2012).
* mRNA percentiles for altered genes shown in patient view.

# April 2, 2013

* New study added: Breast Cancer (British Columbia, Nature 2012).
* All TCGA data updated to the February Firehose run (February 22, 2012).
* __Sequencing read counts and frequencies__ are now shown in the Mutation Details table when available.
* Improved OncoPrints, resulting in performance improvements.
* Major new feature: Users can now visualize __genomic alterations and clinical data of individual tumors__, including:
    - Summary of __mutations__ and __copy-number alterations__ of interest
    - __Clinical trial__ information
    - TCGA __Pathology Reports__
* New __cancer summary view__ (Example [Endometrial Cancer](study.do?cancer_study_id=ucec_tcga))
* __Updated drug data__ from KEGG DRUG and NCI Cancer Drugs (aggregated by [PiHelper](https://bitbucket.org/armish/pihelper))

# October 10, 2012

* New studies added: Breast cancer sequencing studies from the Broad and the Sanger, Cancer Cell Line Encyclopedia (CCLE), and cervical cancer TCGA.
* All data updated to the __Broad Firehose__ run from July 25, 2012.
* __COSMIC data__ added to Mutation Details (via Oncotator)
* All predicted functional impact scores are updated to __Mutation Assessor 2.0__
* Users can now base queries on genes in recurrent regions of copy-number alteration (from __GISTIC__ via Firehose).
* The [Onco Query Language (OQL)](onco_query_lang_desc.jsp) now supports queries for specific mutations or mutation types.
* __Drug information__ added to the network view (via Drugbank).
* Extended __cross-cancer__ functionality, enabling users to query across all cancer studies in our database.
* Users can now base queries on frequently mutated genes (from __MutSig__ via Firehose).
* New __"build a case"__ functionality, enabling users to generate custom case sets, based on one or more clinical attributes.
* New OncoPrint features, including more __compact OncoPrints__, and support for __RPPA__ visualization.
* New feature: __Mutation Diagrams__ that show mutations in the context of protein domains.
<center>![TP53 Mutations in Ovarian Cancer](images/previews/tp53_mutations.png)</center>

# January 27, 2011

* __Updated TCGA data sets__ for Breast Cancer, Ovarian, and Uterine Corpus Endometrioid Carcinoma.
* New __gene symbol validation__ service.  You can now use gene aliases and/or Entrez Gene IDs within your gene sets.
* New and __improved mutation details__, with sorting and filtering capabilities.
* In collaboration with Bilkent University, we have added a __new Network tab__ to our results pages.  The network tab enables users to visualize, analyze and filter cancer genomic data in the context of pathways and interaction networks derived from [Pathway Commons](http://www.pathwaycommons.org).

<center>![GBM Network](images/previews/ova_network.png)</center>

# September 23, 2011

* Updated data for breast and endometrial cancer from TCGA, including 499 fully sequenced breast cancer samples.
* You can now query across different cancer studies (feature available directly from the home page).
* The code for the cBio Portal has now been fully open sourced, and made available at [Google Code](http://code.google.com/p/cbio-cancer-genomics-portal/).  If you would like to join our open source efforts and make the portal even better, drop us an email.

# April 1, 2011

This is the initial release of the cBio SU2C Cancer Genomics Portal. The portal contains genomic data from the TCGA projects on serous ovarian cancer, breast cancer, and uterine cancer, as well as limited protein data from TCGA ovarian cancer samples. 
