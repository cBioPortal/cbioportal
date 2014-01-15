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

/**
 *
 * JS functions for generating the detailed plots view for selected row
 * under co-expression view, mostly init related components (header and body).
 *
 * @author: yichao S
 * @date: Jan 2014
 *
 */

var CoexpPlotsView = (function() {

    var options = {
        style: "",
        canvas: "",
        elem: "",
        names: {
            div: "",
            header: "",
            body: ""
        },
        text: {
            xTitle: "",
            yTitle: "",
            title: "",
            fileName: "",
        }
    };

    function settings(_divName, _geneX, _geneY, _dataAttr) {
        //css style
        options.style = jQuery.extend(true, {}, PlotsBoilerplate.style);
        //positions
        options.canvas = jQuery.extend(true, {}, PlotsBoilerplate.canvas);
        //svg elements
        options.elem = jQuery.extend(true, {}, PlotsBoilerplate.elem);
        //div ids
        options.names = jQuery.extend(true, {}, PlotsBoilerplate.names);
        options.names.div = _divName;
        options.names.header = _divName + options.names.header;
        options.names.body = _divName + options.names.body;   //the actual svg plots
        options.names.loading_img = _divName + options.names.loading_img;
        //construct axis titles
        options.text.xTitle = _geneX + ", " + _dataAttr.profile_name;
        options.text.yTitle = _geneY + ", " + _dataAttr.profile_name;
        options.text.title = "Co-expression in mRNA Expression: " + _geneX + " vs. " + _geneY + "  ";
        options.text.fileName = "co_expression_result-" + _geneX + "-" + _geneY;

    }

    function layout(_divName) {
        $("#" + _divName).empty();
        $("#" + options.names.div).append(
            "<div id='" + options.names.header + 
            "' style='padding-left: " + options.canvas.xLeft + "px; padding-top: 20px;'>" + 
            "</div>");
        $("#" + options.names.div).append("<div id='" + options.names.body + "'></div>");
    }

    function show(_dataArr, _dataAttr) {
        PlotsHeader.init(options.names.header, options.text.title, options.text.fileName, options.names.body);
        ScatterPlots.init(options, _dataArr, _dataAttr);
    }

    function update() {
       // ScatterPlots.update();
    }

    return {
        init: function(_divName, _geneX, _geneY, _dataArr, _dataAttr) {
            settings(_divName, _geneX, _geneY, _dataAttr);
            layout(_divName);
            show(_dataArr, _dataAttr);
        },
        show: show,
        update: update
    }

}());