
function validGeneSet(downloadDataParameters)
{
    var geneIds = "";

    jQuery.each(downloadDataParameters, function(i, parameter) {
        if (parameter.name == "gene_list") {
            geneIds = parameter.value;
        }
    });

    return !(geneIds == "");
}

function validCaseSet(downloadDataParameters)
{
    var caseIds = "";
    var caseSetId = "";
    var missingCaseSetId = true;
    var USER_DEFINED_CASELIST = "-1";

    jQuery.each(downloadDataParameters, function(i, parameter) {
        if (parameter.name == "case_set_id") {
            missingCaseSetId = false;
            caseSetId = parameter.value;
        }
        else if (parameter.name == "case_ids") {
            caseIds = parameter.value;
        }
    });

    return !(missingCaseSetId || (caseSetId == USER_DEFINED_CASELIST && caseIds == ""));
}

function validGenomicProfile(downloadDataParameters)
{
    var foundGeneticProfileIds = false;

    jQuery.each(downloadDataParameters, function(i, parameter) {
        if (parameter.name == "genetic_profile_ids") {
            foundGeneticProfileIds = true;
        }
    });

    return foundGeneticProfileIds;
}

function validDownloadDataForm(downloadDataParameters)
{
    if (!validGenomicProfile(downloadDataParameters)) {
        alert("Invalid Genomic Profile Selected.");
        return false;
    }

    if (!validCaseSet(downloadDataParameters)) {
        alert("Invalid Patient/Case Set.");
        return false;
    }

    if (!validGeneSet(downloadDataParameters)) {
        alert("Invalid Gene Set.");
        return false;
    }

    return true;
}
