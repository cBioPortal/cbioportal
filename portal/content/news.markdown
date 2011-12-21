# December 22, 2011

* Fourteen new [TCGA](http://cancergenome.nih.gov/) cancer studies:  This includes complete data for TCGA Colorectal Carcinoma
and provisional data for thirteen other cancer types in the TCGA production pipeline.  Please note that data from these
thirteen new cancer types are provisional, not final and do not yet include mutation data.
As per NCI guidelines, preliminary mutation data cannot be redistributed until they have been validated.  

![TCGA](http://cancergenome.nih.gov/PublishedContent/Images/SharedItems/Images/tcga_logo.png)

* Four new data types:
    * Reverse-phase protein array (RPPA) data.
    * microRNA expression and copy-number (including support for multiple loci)
    * RNA-Seq based expression data.
    * log2 copy-number data.
* Updated TCGA GBM copy-number, expression, and methylation data.
* New gene validation service.  You can now use gene aliases and/or Entrez Gene IDs within your gene sets.
* Links to IGV for visualization of DNA copy-number changes
* Two new [Tutorials](tutorial.jsp) to get you quickly started in using the portal.

# November 14, 2011

* New and improved mutation details, with sorting and filtering capabilities.
* In collaboration with Bilkent University, we have added a new *network* tab to our results pages.  The network tab enables users to visualize, analyze and filter cancer genomic data in the context of pathways and interaction networks derived from [Pathway Commons](http://www.pathwaycommons.org).

![GBM Network](images/previews/ova_network.png)

# September 3, 2011

* You can now query across different cancer studies (feature available directly from the home page).
* Our [MATLAB CGDS Cancer Genomics Toolbox](cgds_r.jsp) is now available.  The toolbox enables you to download data from the cBio Portal, and import it directly into MATLAB.
* The code for the cBio Portal has now been fully open sourced, and made available at [Google Code](http://code.google.com/p/cbio-cancer-genomics-portal/).  If you would like to join our open source efforts and make the portal even better, drop us an email.

![Cross Cancer Query](images/previews/cross_cancer.png)

# March 2, 2011

New plotting features and other improvements:

* Correlation plots that show the relationship between different data types for individual genes.
* Survival analysis - assess survival differences between altered and non-altered patient sets.
* Updated [R Package](cgds_r.jsp) with support for correlation plots and general improvements for retrieving and accessing data in R data frames.
* The [Web Interface](web_api.jsp) now supports basic clinical data, e.g. survival data.
* [Networks](networks.jsp) for pathway analysis are now available for download.

![Survival Analysis](images/previews/ova_survival_60_percent.png)

# December 15, 2010

Several new features, including:

* Redesigned and streamlined user interface, based on user feedback and usability testing.
* Advanced support for gene-specific alterations.  For example, users
can now view mutations within TP53, and ignore copy number alterations, or
only view amplifications of EGFR, and ignore deletions.
* Improved performance.
* [Frequently Asked Questions](faq.jsp) document released.
* Updated [Video Tutorial](video.jsp).

# November 4, 2010

* Enhanced [Oncoprints](faq.jsp#what-are-oncoprints), enabling users to quickly visualize genomic alterations across many cases.  Oncoprints now also work in all major browsers, including Firefox, Chrome, Safari, and Internet Explorer.
* Official release of our [Web Interface](web_api.jsp), enabling programmatic access to all data.
* Official release of our [R Package](cgds_r.jsp), enabling programmatic access to all data from the R platform for statistical computing.

![OncoPrints](images/previews/gbm_oncoprint.png)