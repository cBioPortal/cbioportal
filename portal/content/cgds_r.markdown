[TOC]

# About

The CGDS-R package provides a basic set of functions for querying the Cancer Genomic
Data Server (CGDS) via the [R platform for statistical computing](http://www.r-project.org/).

# Documentation

* [CGDS-R Package on CRAN](http://cran.r-project.org/web/packages/cgdsr/index.html).
* The [CGDS-R reference manual](http://cran.r-project.org/web/packages/cgdsr/cgdsr.pdf).
* The [CGDS-R documentation vignette](http://cran.r-project.org/web/packages/cgdsr/vignettes/cgdsr.pdf).

# Installation

1.  The CDGS-R package currently **only works with R Version 2.12 or higher**.

2.  Then install the cgds-R package from within R:

     install.packages('cgdsr')

# Example usage

	library('cgdsr')
	
	mycgds = CGDS("http://cbio.mskcc.org/cgds-public/")

	# basic server API tests
	test(mycgds) 

	# get list of cancer types at server
	getCancerTypes(mycgds)

	# get available case lists (collection of samples) for a given cancer type  
	mycancertype = getCancerTypes(mycgds)[1,1]
	mycaselist = getCaseLists(mycgds,mycancertype)[1,1]

	# get available genetic profiles
	mygeneticprofile = getGeneticProfiles(mycgds,mycancertype)[4,1]

	# get data for a specified list of genes, datatypes and case list
	getProfileData(mycgds,c('BRCA1','BRCA2'),mygeneticprofile,mycaselist)

	# documentation
	help('cgdsr')
	help('CGDS')
