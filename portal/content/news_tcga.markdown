# May 16, 2012

* All data updated to the __latest Broad Firehose run__ (March 21, 2012).
* Extended cross-cancer functionality, enabling users to query across all cancer studies in our database.
* New "build a case" functionality, enabling users to generate custom case sets, based on one or more clinical attributes.
* New OncoPrint features, including more compact OncoPrints, and support for RPPA visualization.

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
