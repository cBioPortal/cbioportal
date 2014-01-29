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
        names: "",
        text: "",
        legends: []
    };

    function settings(_divName, _geneX, _geneY, _dataAttr) {
        //css style
        options.style = jQuery.extend(true, {}, PlotsBoilerplate.style);
        options.style["mutations"] = {
            gene_x_mutate_fill: "#DBA901",
            gene_x_mutate_stroke: "#886A08",
            gene_y_mutate_fill: "#F5A9F2",
            gene_y_mutate_stroke: "#F7819F",
            gene_both_mutate_fill: "#FF0000",
            gene_both_mutate_stroke: "#B40404"
        };
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
        options.names.control_panel = _divName + options.names.control_panel;
        //construct axis titles
        options.text = jQuery.extend(true, {}, PlotsBoilerplate.text);
        options.text.xTitle = _geneX + ", " + _dataAttr.profile_name;
        options.text.yTitle = _geneY + ", " + _dataAttr.profile_name;
        options.text.xTitleHelp = _dataAttr.profile_description;
        options.text.yTitleHelp = _dataAttr.profile_description;
        options.text.title = "Co-expression in mRNA Expression: " + _geneX + " vs. " + _geneY + "  ";
        options.text.fileName = "co_expression_result-" + _geneX + "-" + _geneY;
        //construct legend items
        options.legends.length = 0;
        if (_dataAttr.mut_x) {
            var _tmp_obj = {};
            _tmp_obj["fill"] = options.style.mutations.gene_x_mutate_fill;
            _tmp_obj["stroke"] = options.style.mutations.gene_x_mutate_stroke;
            //TODO: set the info for individual cases
            _tmp_obj["size"] = options.style.size;
            _tmp_obj["shape"] = options.style.shape;
            _tmp_obj["stroke_width"] = options.style.stroke_width;
            _tmp_obj["text"] = options.text.legends.gene_x_mut.replace("gene_x", _geneX);
            options.legends.push(_tmp_obj);
        }
        if (_dataAttr.mut_y) {
            var _tmp_obj = {};
            _tmp_obj["fill"] = options.style.mutations.gene_y_mutate_fill;
            _tmp_obj["stroke"] = options.style.mutations.gene_y_mutate_stroke;
            _tmp_obj["size"] = options.style.size;
            _tmp_obj["shape"] = options.style.shape;
            _tmp_obj["stroke_width"] = options.style.stroke_width;
            _tmp_obj["text"] = options.text.legends.gene_y_mut.replace("gene_y", _geneY);
            options.legends.push(_tmp_obj);
        }
        if (_dataAttr.mut_both) {
            var _tmp_obj = {};
            _tmp_obj["fill"] = options.style.mutations.gene_both_mutate_fill;
            _tmp_obj["stroke"] = options.style.mutations.gene_both_mutate_stroke;
            _tmp_obj["size"] = options.style.size;
            _tmp_obj["shape"] = options.style.shape;
            _tmp_obj["stroke_width"] = options.style.stroke_width;
            _tmp_obj["text"] = options.text.legends.gene_both_mut;
            options.legends.push(_tmp_obj);
        }
        var _tmp_obj = {};
        _tmp_obj["fill"] = options.style.fill;
        _tmp_obj["stroke"] = options.style.stroke;
        _tmp_obj["size"] = options.style.size;
        _tmp_obj["shape"] = options.style.shape;
        _tmp_obj["stroke_width"] = options.style.stroke_width;
        _tmp_obj["text"] = options.text.legends.non_mut;
        options.legends.push(_tmp_obj);
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
        var enable_log_scale = false;
        if (_dataAttr.profile_name.indexOf("RNA Seq") !== -1) {
            enable_log_scale = true;
        }
        PlotsHeader.init(
            options.names.div,
            options.names.header, 
            options.text.title, 
            options.text.fileName, 
            options.names.body, 
            options.names.control_panel,
            enable_log_scale, 
            enable_log_scale);
        ScatterPlots.init(options, _dataArr, _dataAttr);
    }

    function styleMutatedCases(_dataArr, _geneX, _geneY) { //style the mutated cases based on this specific scenario
        $.each(_dataArr, function(index, obj) {
            if (obj.hasOwnProperty("mutation")) {
                if (obj["mutation"].hasOwnProperty(_geneX) &&
                    obj["mutation"].hasOwnProperty(_geneY)) {
                    obj.stroke = options.style.mutations.gene_both_mutate_stroke;
                    obj.fill = options.style.mutations.gene_both_mutate_fill;
                } else if (obj["mutation"].hasOwnProperty(_geneX) &&
                    !obj["mutation"].hasOwnProperty(_geneY)) {
                    obj.stroke = options.style.mutations.gene_x_mutate_stroke;
                    obj.fill = options.style.mutations.gene_x_mutate_fill;
                } else if (!obj["mutation"].hasOwnProperty(_geneX) &&
                    obj["mutation"].hasOwnProperty(_geneY)) {
                    obj.stroke = options.style.mutations.gene_y_mutate_stroke;
                    obj.fill = options.style.mutations.gene_y_mutate_fill;
                } 
            } else {
                obj.stroke = options.style.stroke;
                obj.fill = options.style.fill;
            }
        });
    }

    function update() {
       // ScatterPlots.update();
    }

    return {
        init: function(_divName, _geneX, _geneY, _dataArr, _dataAttr) {
            settings(_divName, _geneX, _geneY, _dataAttr);
            styleMutatedCases(_dataArr, _geneX, _geneY);
            layout(_divName);
            show(_dataArr, _dataAttr);
        },
        show: show,
        update: update
    }

}());