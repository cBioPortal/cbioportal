function geneticProfiles = getgeneticprofiles(cgdsURL, cancerTypeId, varargin)
%GETGENETICPROFILES Get genetic profiles from the cBio CGDS portal.
%    A = GETGENETICPROFILES(cgdsURL, cancerTypeId) loads a list of
%    available genetic profiles into A. cdgsURL points to the CGDS web API,
%    typically 'http://cbio.mskcc.org/cgds-public/'. cancerTypeId is the
%    cancer type ID, as returned by the getcancertypes function.
%
%    The function returns a struct array with the following fields:
%    geneticProfileId, geneticProfileName, geneticProfileDescription,
%    cancerTypeId, geneticAlterationType.
%
%    Field names follow column names returned by the web API.
%
%    A = GETGENETICPROFILES(cgdsURL, cancerTypeId, 'silent')
%    runs the function in non-verbose mode, supressing status and warning
%    messages from the cBio CGDS web API. Any string or numerical
%    (e.g. 'non-verbose' or 0) will have this effect. Error messages are
%    always printed, as these indicate an unrecoverable problem.
%
%    See also getcancertypes, getcaselists, getprofiledata,
%    getclinicaldata.

verbose = isempty(varargin);

cells  = urlgetcells([cgdsURL 'webservice.do?cmd=getGeneticProfiles&cancer_type_id=' cancerTypeId], verbose);

geneticProfiles.geneticProfileId = cells(2:end, 1);
geneticProfiles.geneticProfileName = cells(2:end, 2);
geneticProfiles.geneticProfileDescription = cells(2:end, 3);
geneticProfiles.cancerTypeId = cells(2:end, 4);
geneticProfiles.geneticAlterationType = cells(2:end, 5);
