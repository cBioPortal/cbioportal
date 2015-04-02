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


/*
 * Generate the header section of a regular plot, includes the title and the pdf/svg download button
 *
 * @author: yichao S
 * @date: Jan 2014
 *
 */

var PlotsHeader = (function() {

    var headerDivId = "",
        plotsDivId = "",
        logScaleXDivId = "",
        logScaleYDivId = "",
        showMutationsDivId = "",
        downloadPdfDivId = "",
        downloadSvgDivId = "",
        title = "",
        fileName = "",
        enableLogScale = false,
        plotsObj = {};

    function appendTitle() {
        $("#" + headerDivId).append("<b>" + title + "</b>");
    }

    function appendSvgConverter() {
        $("#" + headerDivId).append("<br><br><br><button id='" + downloadSvgDivId + "'>SVG</button>");
    }

    function appendPdfConverter() {
        $("#" + headerDivId).append("<button id='" + downloadPdfDivId + "'>PDF</button>&nbsp;&nbsp;");
    }

    function appendControlPanel() {
        $("#" + headerDivId).append(
            "<input type='checkbox' id='" + showMutationsDivId + "' checked />Show Mutations&nbsp;&nbsp;&nbsp;");
        if (enableLogScale) {
            $("#" + headerDivId).append(
                "<input type='checkbox' id='" + logScaleXDivId + "' />Log Scale X&nbsp;&nbsp;&nbsp;");
            $("#" + headerDivId).append(
                "<input type='checkbox' id='" + logScaleYDivId + "' />Log Scale Y&nbsp;&nbsp;&nbsp;");
        }
    }

    return {
        init: function(_names, _title, _fileName, _enableLogScale, _plots_obj) { //log scale x/y on/off, 
            headerDivId = _names.header;
            logScaleXDivId = _names.log_scale_x;
            logScaleYDivId = _names.log_scale_y;
            showMutationsDivId = _names.show_mutations;
            downloadPdfDivId = _names.download_pdf;
            downloadSvgDivId = _names.download_svg;
            plotsDivId = _names.body; //The actual svg div name 
            enableLogScale = _enableLogScale;
            plotsObj = _plots_obj;
            title = _title;
            fileName = _fileName;
            appendTitle();
            appendSvgConverter();
            appendPdfConverter();
            appendControlPanel();
        }
    };

}());