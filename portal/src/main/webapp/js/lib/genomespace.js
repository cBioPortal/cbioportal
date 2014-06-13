/** Copyright (c) 2007 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander, Benjamin Gross
 ** Modified by Jim Robinson for use with IGV
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

function uploadUrlByGetToGS(dataURL, filename)
{
    var dest = "";
    var onComplete = function(savePath){
        alert('outer Saved to GenomeSpace as ' + savePath);
    };
    var onError = function(savePath){
        alert('outer ERROR saving to GenomeSpace as ' + savePath);
    };
    gsUploadByGet({
        url: dataURL,
        filename: filename,
        successCallback: onComplete,
        errorCallback: onError
    });
    return false;
};

function getDataFilenameSuffixByProfile(genomicProfile)
{
    if (genomicProfile == 'PROFILE_COPY_NUMBER_ALTERATION') {
        return 'cna.txt';
    }
    else if (genomicProfile == 'PROFILE_MUTATION_EXTENDED') {
        return 'mutations.txt';
    }
    else if (genomicProfile == 'PROFILE_MRNA_EXPRESSION') {
        return 'mrna.txt';
    }
    else if (genomicProfile == 'PROFILE_METHYLATION') {
        return 'methylation.txt';
    }
    else if (genomicProfile == 'PROFILE_RPPA') {
        return 'rppa.txt';
    }
}

function getDataFilename(cancerStudy, genomicProfiles)
{
    var dataFilename = "cbioportal-" + cancerStudy + "-";
    $(genomicProfiles).find('input:radio').each(function() {
        if ($(this).attr('checked')) {
            dataFilename = dataFilename + getDataFilenameSuffixByProfile($(this).attr('class'));
        }
    });
    return dataFilename;
}

function getOrigin()
{
    var location = new String(window.location);
    return location.substring(0, location.indexOf("?")+1);
}

function getFormParametersString(downloadDataParameters)
{
    var parameters = "";
    jQuery.each(downloadDataParameters, function(i, parameter) {
        parameters += parameter.name + "=" + encodeURIComponent(parameter.value) + "&";
    });
    parameters += "Action=Submit";

    return parameters;
}



function prepGSLaunch(form, cancerStudy, genomicProfiles)
{
    var downloadDataParameters = $(form).serializeArray();
    if (validDownloadDataForm(downloadDataParameters)) {
        var urlToDownloadData = getOrigin() + getFormParametersString(downloadDataParameters);
        var dataFilename = getDataFilename(cancerStudy, genomicProfiles)
        uploadUrlByGetToGS(urlToDownloadData, dataFilename);
    }
}