%% CGDS toolbox examples ('showdemo cgdstutorial')
% The CGDS toolbox provides a set of functions for retrieving data from the
% cBio Cancer Genomics Data Portal web API. Get started by adding the CGDS
% toolbox directory to the path and setting the server URL.

% modify path to make toolbox functions globally available in matlab
% this will depend on install location, and is only necessary if you want
% to make the functions available from any directory
addpath('/Users/Erik/Documents/MATLAB/cgds');

% Set web API URL (including trailing '/' but excluding 'webservice.do')
cgdsURL = 'http://www.cbioportal.org/public-portal/';

%% Show toolbox help
% Use 'helpwin cgds' if you prefer to display it in the Help window.
help cgds;

%% Get list of available cancer types
cancerStudies = getcancerstudies(cgdsURL)

%% Get available genetic profiles for a given cancer type
% This example retreives available profiles for glioblastoma (GBM).
geneticProfiles = getgeneticprofiles(cgdsURL, 'gbm_tcga')

%% Get available case lists (collections of samples) for a given cancer type  
caseLists = getcaselists(cgdsURL, 'gbm_tcga')

%% Get multiple types of genetic profile data for a specific gene
% This fetches both mRNA expression and copy number status for P53 in GBM.
% The last argument causes data to be returned as a numeric matrix. Set to
% false when fetching non-numeric data, e.g. mutations. 'gbm_mrna' and
% 'gbm_gistic' are genetic profile IDs in geneticProfiles.geneticProfileID.
% 'gbm_all' is a case list ID from caseLists.caseListId.
profileData = getprofiledata(cgdsURL, 'gbm_tcga_all', ...
                             {'gbm_tcga_mrna' 'gbm_tcga_gistic'}, ...
                             'TP53', true)

%% Plot mRNA levels as a function of copy number status
boxplot(profileData.data(1,:),profileData.data(2,:));
title('TP53'); xlabel('CNA'); ylabel('mRNA level');

%% Get genetic profile data for multiple specified genes
% This fetches mutation data for five different genes. Only one genetic
% profile ID is allowed in this case. Note that genes may be returned in a
% different order than requested.
profileData = getprofiledata(cgdsURL, 'gbm_tcga_sequenced', ...
                             'gbm_tcga_mutations', ...
                             {'TP53' 'NF1' 'EGFR' 'PTEN' 'IDH1'}, false)

%% Get clinical data for all patients in a given case list
clinicalData = getclinicaldata(cgdsURL, 'gbm_tcga_sequenced')

%% Survival plots for patients with and without IDH1 mutations
isMutated = ismember(clinicalData.caseId, profileData.caseId(~strcmp(profileData.data(2,:), 'NaN')));
isCensored = strcmp(clinicalData.overallSurvivalStatus, 'LIVING');
ecdf(clinicalData.overallSurvivalMonths(isMutated), ...
     'censoring',isCensored(isMutated),'function','survivor');
set(get(gca,'Children'), 'Color', [1 0 0]); hold on;
ecdf(clinicalData.overallSurvivalMonths(~isMutated), ...
     'censoring',isCensored(~isMutated),'function','survivor');
xlabel('Overall survival (months)'); ylabel('Proportion surviving');
legend({'IDH1 mutated' 'IDH1 wild type'});

%% Run a function in non-verbose mode
cancerStudies = getcancerstudies(cgdsURL, 'silent');
