function caseLists = getcaselists(cgdsURL, cancerTypeId, varargin)
%GETCASELISTS Get case lists from the cBio CGDS portal.
%    A = GETCASELISTS(cgdsURL, cancerTypeId) loads available case lists for
%    a specific cancer type into A. cdgsURL points to the CGDS web API,
%    typically 'http://cbio.mskcc.org/cgds-public/'. cancerTypeId is the
%    cancer type ID, as returned by the getcancertypes function.
%
%    Variable names follow column names returned by the web API.
%
%    The function returns a struct array with the following fields:
%    caseListId, caseListName, caseListDescription, cancerTypeId, caseIds.
%    Each element of caseIds contains a cell array of strings.
%
%    Field names follow column names as returned by the web API.
%
%    A = GETCASELISTS(cgdsURL, cancerTypeId, 'silent')
%    runs the function in non-verbose mode, supressing status and warning
%    messages from the cBio CGDS web API. Any string or numerical
%    (e.g. 'non-verbose' or 0) will have this effect. Error messages are
%    always printed, as these indicate an unrecoverable problem.
%
%    See also getcancertypes, getgeneticprofiles, getprofiledata,
%    getclinicaldata.

verbose = isempty(varargin);

cells  = urlgetcells([cgdsURL 'webservice.do?cmd=getCaseLists&cancer_type_id=' cancerTypeId], verbose);

caseLists.caseListId = cells(2:end, 1);
caseLists.caseListName = cells(2:end, 2);
caseLists.caseListDescription = cells(2:end, 3);
caseLists.cancerTypeId = cells(2:end, 4);

% tokenize each case id list
for i = 2:size(cells, 1),
    thisCaseIds = textscan(cells{i, 5}, '%s', 'delimiter', ' ');
    caseLists.caseIds{i - 1, 1} = thisCaseIds{1};
end
