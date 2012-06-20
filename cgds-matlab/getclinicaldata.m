function clinicalData = getclinicaldata(cgdsURL, caseListId, varargin)
%GETCLINICALDATA Get clinical data from the cBio CGDS portal.
%    A = getclinicaldata(cgdsURL, caseListId) loads clinical data into A.
%    cdgsURL points to the CGDS web API, typically
%    'http://cbio.mskcc.org/cgds-public/'. caseListId is a case list ID, as
%    returned by the getcaselists function.
%
%    Returns a struct array with the following fields: caseId,
%    overallSurvivalMonths, overallSurvivalStatus,
%    diseaseFreeSurvivalMonths, diseaseFreeSurvivalStatus, ageAtDiagnosis.
%
%    Field names follow column names as returned by the web API.
%
%    A = getclinicaldata(cgdsURL, caseListId, 'silent')
%    runs the function in non-verbose mode, supressing status and warning
%    messages from the cBio CGDS web API. Any string or numerical
%    (e.g. 'non-verbose' or 0) will have this effect. Error messages are
%    always printed, as these indicate an unrecoverable problem.
%
%    See also getcancertypes, getgeneticprofiles, getcaselists,
%    getprofiledata.

verbose = isempty(varargin);

cells  = urlgetcells([cgdsURL 'webservice.do?cmd=getClinicalData&case_set_id=' caseListId], verbose);

clinicalData.caseId = cells(2:end, 1);
clinicalData.overallSurvivalMonths = str2double(cells(2:end, 2));
clinicalData.overallSurvivalStatus = cells(2:end, 3);
clinicalData.diseaseFreeSurvivalMonths = str2double(cells(2:end, 4));
clinicalData.diseaseFreeSurvivalStatus = cells(2:end, 5);
clinicalData.ageAtDiagnosis = str2double(cells(2:end, 6));
