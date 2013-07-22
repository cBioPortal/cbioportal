function profileData = getprofiledata(cgdsURL, caseListId, geneticProfileId, geneList, toNumeric, varargin)
%GETPROFILEDATA Get genomic profile data from the cBio CGDS portal.
%    A = GETPROFILEDATA(cgdsURL, caseListId, geneticProfileId, geneList, toNumeric)
%    loads genomic profile data into A. cdgsURL points to the CGDS web API,
%    typically 'http://cbio.mskcc.org/cgds-public/'. caseListId is a case
%    list ID, as returned by the getcaselists function. geneticProfileId is
%    a cell array of genetic profile IDs, as returned by
%    getgeneticprofiles. geneList is a cell array of HUGO gene symbols or
%    Entrez Gene IDs. If toNumeric is true, data will be returned as a
%    numeric matrix (convenient e.g. for mRNA expression data).
%
%    This function can be called in two different ways:
%
%    * Specificy multiple genes (cell array of strings, or single string
%    with symbols separated by , or +) and a single genetic profile ID.
%    Returns a struct array with the following fields: geneId (Entrez Gene
%    IDs), common (HUGO gene symbols), data (data matrix), caseId (column
%    labels for the data matrix).
%
%    * Specificy a single gene and multiple genetic profile IDs (cell
%    array of strings, or separated by , or +). Returns a struct array with
%    the following fields: geneticProfileId, alterationType, geneId (Entrez
%    Gene ID), common (HUGO gene symbol), data (data matrix).
%
%    Field names follow column names as returned by the web API.
%
%    A = GETPROFILEDATA(cgdsURL, caseListId, geneticProfileId, geneList, toNumeric, 'silent')
%    runs the function in non-verbose mode, supressing status and warning
%    messages from the cBio CGDS web API. Any string or numerical
%    (e.g. 'non-verbose' or 0) will have this effect. Error messages are
%    always printed, as these indicate an unrecoverable problem.
%
%    See also getcancertypes, getgeneticprofiles, getcaselists,
%    getclinicaldata.

verbose = isempty(varargin);

cells  = urlgetcells([cgdsURL 'webservice.do?cmd=getProfileData&case_set_id=' caseListId ...
                      '&genetic_profile_id=' cellarraytostr(geneticProfileId) ...
                      '&gene_list=' cellarraytostr(geneList)], verbose);

% determine format
if strcmp(cells(1,1), 'GENE_ID')
    % multiple genes, single genetic profile ID
    profileData.geneId = cells(2:end, 1);    
    profileData.common = cells(2:end, 2);
    profileData.caseId = cells(1, 3:end)';
    if toNumeric
        profileData.data = str2double(cells(2:end, 3:end));
    else
        profileData.data = cells(2:end, 3:end);
    end
else
    % single gene, multiple genetic profile IDs
    profileData.geneticProfileId = cells(2:end, 1);    
    profileData.alterationType = cells(2:end, 2);
    profileData.geneId = cells(2:end, 3);
    profileData.common = cells(2:end, 4);
    profileData.caseId = cells(1, 5:end)';
    if toNumeric
        profileData.data = str2double(cells(2:end, 5:end));
    else
        profileData.data = cells(2:end, 5:end);
    end    
end


function s = cellarraytostr(sArray)
% converts a cell array of strings into a single string where elements
% are separated by '+'. in case sArray is a string rather than cell array,
% it is simply passed through to s.

if isstr(sArray)
    s = sArray;
else
    s = sArray{1};
    for i = 2:length(sArray),
        s = [s '+' sArray{i}];
    end
end
