function cancerStudies = getcancerstudies(cgdsURL, varargin)
%GETCANCERSTUDIES Get cancer studies from the cBio CGDS portal.
%    A = GETCANCERSTUDIES(cgdsURL) loads a list of available cancer types
%    into A. cdgsURL points to the CGDS web API, typically
%    'http://cbio.mskcc.org/cgds-public/'.
%
%    The function returns a struct array with the following fields:
%    cancerTypeId, name, description.
%
%    Field names follow column names as returned by the web API.
%
%    A = GETcancerStudies(cgdsURL, 'silent')
%    runs the function in non-verbose mode, supressing status and warning
%    messages from the cBio CGDS web API. Any string or numerical
%    (e.g. 'non-verbose' or 0) will have this effect. Error messages are
%    always printed, as these indicate an unrecoverable problem.
%
%    See also getgeneticprofiles, getcaselists, getprofiledata,
%    getclinicaldata.

verbose = isempty(varargin);

cells  = urlgetcells([cgdsURL 'webservice.do?cmd=getCancerStudies'], verbose);

cancerStudies.cancerTypeId = cells(2:end, 1);
cancerStudies.name = cells(2:end, 2);
cancerStudies.description = cells(2:end, 3);
