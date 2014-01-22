/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

/*
 * Generate the header section of a regular plot, includes the title and the pdf/svg download button
 *
 * @author: yichao S
 * @date: Jan 2014
 *
 */

var PlotsHeader = (function() {

    var divName = "", 
        title = "",
        fileName = "",
        targetDiv = "",
        controlPanelDiv = "";

    function appendTitle() {
        $("#" + divName).append("<b>" + title + "</b>");
    }

    function appendPdfConverter() {
        var pdfConverterForm =
            "<form style='display:inline-block' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=PlotsHeader.loadSvg('" + targetDiv + "');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='" + fileName + ".pdf'>" +
                "<input type='submit' value='PDF'></form>";
        $("#" + divName).append(pdfConverterForm);
    }

    function appendSvgConverter() {
        var svgConverterForm =
            "<form style='display:inline-block' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=PlotsHeader.loadSvg('" + targetDiv + "');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' name='filename' value='" + fileName + ".svg'>" +
                "<input type='submit' value='SVG'></form>";
        $("#" + divName).append(svgConverterForm);
    }

    function appendControlPanel(_log_x, _log_y) {
        $("#" + divName).append(
            "<div id='" + controlPanelDiv + "' " +
            "style='margin-top: 15px; margin-bottom: 0px; margin-right: 53px; float:right;'></div>");
        $("#" + controlPanelDiv).append(
            "<input type='checkbox' id='" + controlPanelDiv + "_show_mutation' checked/>" +
            "Show Mutations &nbsp;&nbsp;&nbsp;" 
        );
        if (_log_x) {
            $("#" + controlPanelDiv).append(
                "<input type='checkbox' id='" + controlPanelDiv + "_x' " + 
                "onchange=\"ScatterPlots.updateScaleX('" + controlPanelDiv + "_x');\" />" + 
                "Log Scale X &nbsp;&nbsp;&nbsp;");
        }
        if (_log_y) {
            $("#" + controlPanelDiv).append(
                "<input type='checkbox' id='" + controlPanelDiv + "_y' " +
                "onchange=\"ScatterPlots.updateScaleY('" + controlPanelDiv + "_y');\" />" +
                "Log Scale Y &nbsp;&nbsp;&nbsp;"); 
        }
    }

    function addEventListener(_plotsDiv) {
        var _svgElement = document.getElementById(_plotsDiv);
        _svgElement.addEventListener("mouseover", function() { 
            $("#" + controlPanelDiv).css("opacity", "1.0");
        });
        _svgElement.addEventListener("mouseout", function() {
            $("#" + controlPanelDiv).css("opacity", "0.3");
        });
        $("#" + controlPanelDiv).css("opacity", "0.3");
    }

    return {
        init: function(_plotsDiv, _divName, _title, _fileName, _targetDiv, _controlPanelDiv, _log_x, _log_y) { //log scale x/y on/off, 
            divName = _divName;
            title = _title;
            fileName = _fileName;
            targetDiv = _targetDiv; //The actual svg div name 
            controlPanelDiv = _controlPanelDiv;
            appendTitle();
            appendPdfConverter();
            appendSvgConverter();
            appendControlPanel(_log_x, _log_y);
            //addEventListener(_plotsDiv);
        },
        loadSvg: function(_divName) {
            return $("#" + _divName).html();
        }
    }

}());