[TOC]

# CGDS-R Package

## Description

The CGDS-R package provides a basic set of functions for querying the Cancer Genomic Data Server (CGDS) via the [R platform for statistical computing](http://www.r-project.org/).  

Maintained by [Anders Jacobsen](http://cbio.mskcc.org/people/info/anders_jacobsen.html) at the Computational Biology Center, MSKCC.

## Documentation

* [CGDS-R Package on CRAN](http://cran.r-project.org/web/packages/cgdsr/index.html).
* The [CGDS-R reference manual](http://cran.r-project.org/web/packages/cgdsr/cgdsr.pdf).
* The [CGDS-R documentation vignette](http://cran.r-project.org/web/packages/cgdsr/vignettes/cgdsr.pdf).

## Installation

1.  The CDGS-R package currently **only works with R Version 2.12 or higher**.

2.  Then install the cgds-R package from within R:

     install.packages('cgdsr')

## Example usage

	# Create CGDS object                                                                                                                                                            
     mycgds = CGDS("http://www.cbioportal.org/public-portal/")                                                                                                                       
                                                                                                                                                                                     
     # Test the CGDS endpoint URL using a few simple API tests                                                                                                                       
     test(mycgds)                                                                                                                                                                    
                                                                                                                                                                                     
     # Get list of cancer studies at server                                                                                                                                          
     getCancerStudies(mycgds)                                                                                                                                                        
                                                                                                                                                                                     
     # Get available case lists (collection of samples) for a given cancer study                                                                                                     
     mycancerstudy = getCancerStudies(mycgds)[2,1]                                                                                                                                   
     mycaselist = getCaseLists(mycgds,mycancerstudy)[1,1]                                                                                                                            
                                                                                                                                                                                     
     # Get available genetic profiles                                                                                                                                                
     mygeneticprofile = getGeneticProfiles(mycgds,mycancerstudy)[4,1]                                                                                                                
                                                                                                                                                                                     
     # Get data slices for a specified list of genes, genetic profile and case list                                                                                                  
     getProfileData(mycgds,c('BRCA1','BRCA2'),mygeneticprofile,mycaselist)                                                                                                           
                                                                                                                                                                                     
     # Get clinical data for the case list                                                                                                                                           
     myclinicaldata = getClinicalData(mycgds,mycaselist) 

	# documentation
	help('cgdsr')
	help('CGDS')

# MATLAB CGDS Cancer Genomics Toolbox

## Description

This toolbox provides direct access to cancer genomics data from within the MATLAB environment. The toolbox will query the Cancer Genomics Data Server Web API, and return data in a structured format.

Maintained by [Erik Larsson](http://cbio.mskcc.org/people/info/erik_larsson.html) at the Computational Biology Center, MSKCC.

A tutorial ('showdemo cgdstutorial') makes it easy to get started. 

## Download

[CGDS Toolbox @ MATLAB Central](http://www.mathworks.com/matlabcentral/fileexchange/31297-mskcc-cgds-cancer-genomics-toolbox)

## Documentation

[Complete CGDS Tutorial](http://www.mathworks.com/matlabcentral/fileexchange/31297-mskcc-cgds-cancer-genomics-toolbox/content/html/cgdstutorial.html)

## Example Usage

Like the R package, the CGDS MATLAB toolbox provides a set of functions for direct retrieval of portal data from within the MATLAB (MathWorks Inc.) environment. Each toolbox function has a direct counterpart in the CGDS Web API. Data is returned as structured arrays, in a format that is easy to interpret and ready for subsequent visualization and statistical analysis. 

An included tutorial ("showdemo cgdstutorial") shows how to use all the functions, as well as how to make basic plots.

	% Get started by adding the CGDS toolbox directory to the path (this will depend
	% on install location) and setting the server URL
	addpath('/MATLAB/cgds');
	cgdsURL = 'http://www.cbioportal.org/public-portal';

	% Show toolbox help ('helpwin cgds' will open in the Help window)
	help cgds;

	% Get list of cancer types at server
	cancerTypes = getcancertypes(cgdsURL);

	% Get available genetic profiles for a given cancer type (GBM below)
	geneticProfiles = getgeneticprofiles(cgdsURL, cancerTypes.cancerTypeId{2});

	% Get available case lists (collections of samples)
	caseLists = getcaselists(cgdsURL, cancerTypes.cancerTypeId{2});

	% Get multiple types of genetic profile data for p53 in GBM
	profileData = getprofiledata(cgdsURL, caseLists.caseListId{2}, ...
	                            geneticProfiles.geneticProfileId([3 4]), ...
	                            'TP53', true);

	% Plot mRNA levels as a function of copy number status
	boxplot(profileData.data(2,:),profileData.data(1,:));
	title('TP53'); xlabel('Copy-number status'); ylabel('mRNA level');

	% Get clinical data for all patients in a given case list
	clinicalData = getclinicaldata(cgdsURL, caseLists.caseListId{2});

	% Run a function in non-verbose mode
	cancerTypes = getcancertypes(cgdsURL, 'silent');
