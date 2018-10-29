# R/Matlab

## CGDS-R Package

### Description

The CGDS-R package provides a basic set of functions for querying the Cancer
Genomic Data Server (CGDS) via the R platform for statistical computing.
Maintained by Anders Jacobsen at the Computational Biology Center, MSKCC.

### Documentation

- [CGDS-R Package on CRAN](http://cran.r-project.org/web/packages/cgdsr/index.html)
- [The CGDS-R reference manual](http://cran.r-project.org/web/packages/cgdsr/cgdsr.pdf)
- [The CGDS-R documentation vignette](http://cran.r-project.org/web/packages/cgdsr/vignettes/cgdsr.pdf)

### Installation

1. The CDGS-R package currently **only works with R Version 2.12 or higher**.
2. Then install the cgds-R package from within R: `install.packages('cgdsr')`

### Example usage
```
# Create CGDS object
mycgds = CGDS("http://www.cbioportal.org/public-portal/")

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
```

## MATLAB CGDS Cancer Genomics Toolbox

### Description
This toolbox provides direct access to cancer genomics data from within the
MATLAB environment. The toolbox will query the Cancer Genomics Data Server Web
API, and return data in a structured format.

Maintained by Erik Larsson at the Computational Biology Center, MSKCC.

A tutorial `('showdemo cgdstutorial')` makes it easy to get started.

### Download
[CGDS Toolbox @ MATLAB Central](http://www.mathworks.com/matlabcentral/fileexchange/31297-mskcc-cgds-cancer-genomics-toolbox)

### Documentation
[Complete CGDS Tutorial](http://www.mathworks.com/matlabcentral/fileexchange/31297-mskcc-cgds-cancer-genomics-toolbox/content/html/cgdstutorial.html)

### Example Usage
```
% Get started by adding the CGDS toolbox directory to the path (this will depend
% on install location) and setting the server URL
addpath('/MATLAB/cgds');
cgdsURL = 'http://www.cbioportal.org/public-portal';

% Show toolbox help ('helpwin cgds' will open in the Help window)
help cgds;

% Get list of cancer studies at server
cancerStudies = getcancerstudies(cgdsURL);

% Get available genetic profiles for a given cancer type (GBM below)
geneticProfiles = getgeneticprofiles(cgdsURL, cancerStudies.cancerStudyId{2});

% Get available case lists (collections of samples)
caseLists = getcaselists(cgdsURL, cancerStudies.cancerStudyId{2});

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
cancerStudies = getcancerstudies(cgdsURL, 'silent');
```
