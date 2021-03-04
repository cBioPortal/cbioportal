/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


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
