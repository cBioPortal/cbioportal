function clinicalData = getclinicaldata(cgdsURL, caseListId, varargin)
%GETCLINICALDATA Get clinical data from the cBio CGDS portal.
%    A = getclinicaldata(cgdsURL, caseListId) loads clinical data into A.
%    cdgsURL points to the CGDS web API, typically
%    http://www.cbioportal.org/public-portal/. caseListId is a case list
%    ID, as returned by the getcaselists function.
%
%    Returns a struct array with the following fields: data (data matrix),
%    caseId (row labels for data matrix), clinVariable (column labels for
%    data matrix).
%
%    Since data returned by this function can be of mixed types, everything
%    is given as strings. Use str2double() to convert to numeric format
%    when appropriate.
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
if ~strcmp(cgdsURL(end), '/') cgdsURL(end + 1) = '/'; end

cells  = urlgetcells([cgdsURL 'webservice.do?cmd=getClinicalData&case_set_id=' caseListId], verbose);

clinicalData.caseId = cells(2:end, 1);
clinicalData.clinVariable = cells(1, 2:end)';
clinicalData.data = cells(2:end, 2:end);
