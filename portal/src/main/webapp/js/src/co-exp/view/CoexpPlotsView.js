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

var CoexpPlotsView = function() {

    //params
    var plotsDivId = "",
        geneX = "",
        geneY = "",
        dataArr = [],
        dataAttr = [];
    //Settings that needs to be construct
    var plotsOpts = {
            style: "",
            canvas: "",
            elem: "",
            names: "",
            text: "",
            legends: []
        },
        settings = {
            enable_log_scale: false
        };


    function importDefaultSettings() {
        //Import default settings from boilerplate
        plotsOpts.style = jQuery.extend(true, {}, PlotsBoilerplate.style);
        plotsOpts.canvas = jQuery.extend(true, {}, PlotsBoilerplate.canvas);
        plotsOpts.elem = jQuery.extend(true, {}, PlotsBoilerplate.elem);      
        plotsOpts.text = jQuery.extend(true, {}, PlotsBoilerplate.text);
        plotsOpts.names = jQuery.extend(true, {}, PlotsBoilerplate.names);
    }

    function configGeneralSettings() {
        //div ids
        plotsOpts.names.div = plotsDivId;
        plotsOpts.names.header = plotsDivId + plotsOpts.names.header;
        plotsOpts.names.body = plotsDivId + plotsOpts.names.body;   //the actual svg plots
        plotsOpts.names.loading_img = plotsDivId + plotsOpts.names.loading_img;
        plotsOpts.names.control_panel = plotsDivId + plotsOpts.names.control_panel;       
        plotsOpts.names.log_scale_x = plotsDivId + plotsOpts.names.log_scale_x;       
        plotsOpts.names.log_scale_y = plotsDivId + plotsOpts.names.log_scale_y;       
        plotsOpts.names.show_mutations = plotsDivId + plotsOpts.names.show_mutations;
        plotsOpts.names.download_pdf = plotsDivId + plotsOpts.names.download_pdf;
        plotsOpts.names.download_svg = plotsDivId + plotsOpts.names.download_svg;       
    }

    function configPlotsSettings() {
        //css style
        plotsOpts.style["mutations"] = {
            gene_x_mutate_fill: "#DBA901",
            gene_x_mutate_stroke: "#886A08",
            gene_y_mutate_fill: "#F5A9F2",
            gene_y_mutate_stroke: "#F7819F",
            gene_both_mutate_fill: "#FF0000",
            gene_both_mutate_stroke: "#B40404"
        };  
        //construct axis titles
        plotsOpts.text.xTitle = geneX + ", " + dataAttr.profile_name;
        plotsOpts.text.yTitle = geneY + ", " + dataAttr.profile_name;
        plotsOpts.text.xTitleHelp = dataAttr.profile_description;
        plotsOpts.text.yTitleHelp = dataAttr.profile_description;
        //construct legend items
        plotsOpts.legends.length = 0;
        if (dataAttr.mut_x) {
            var _tmp_obj = {};
            _tmp_obj["fill"] = plotsOpts.style.mutations.gene_x_mutate_fill;
            _tmp_obj["stroke"] = plotsOpts.style.mutations.gene_x_mutate_stroke;
            //TODO: set the info for individual cases
            _tmp_obj["size"] = plotsOpts.style.size;
            _tmp_obj["shape"] = plotsOpts.style.shape;
            _tmp_obj["stroke_width"] = plotsOpts.style.stroke_width;
            _tmp_obj["text"] = plotsOpts.text.legends.gene_x_mut.replace("gene_x", geneX);
            plotsOpts.legends.push(_tmp_obj);
        }
        if (dataAttr.mut_y) {
            var _tmp_obj = {};
            _tmp_obj["fill"] = plotsOpts.style.mutations.gene_y_mutate_fill;
            _tmp_obj["stroke"] = plotsOpts.style.mutations.gene_y_mutate_stroke;
            _tmp_obj["size"] = plotsOpts.style.size;
            _tmp_obj["shape"] = plotsOpts.style.shape;
            _tmp_obj["stroke_width"] = plotsOpts.style.stroke_width;
            _tmp_obj["text"] = plotsOpts.text.legends.gene_y_mut.replace("gene_y", geneY);
            plotsOpts.legends.push(_tmp_obj);
        }
        if (dataAttr.mut_both) {
            var _tmp_obj = {};
            _tmp_obj["fill"] = 

        plotsOpts.style.mutations.gene_both_mutate_fill;
            _tmp_obj["stroke"] = plotsOpts.style.mutations.gene_both_mutate_stroke;
            _tmp_obj["size"] = plotsOpts.style.size;
            _tmp_obj["shape"] = plotsOpts.style.shape;
            _tmp_obj["stroke_width"] = plotsOpts.style.stroke_width;
            _tmp_obj["text"] = plotsOpts.text.legends.gene_both_mut;
            plotsOpts.legends.push(_tmp_obj);
        }
        var _tmp_obj = {};
        _tmp_obj["fill"] = plotsOpts.style.fill;
        _tmp_obj["stroke"] = plotsOpts.style.stroke;
        _tmp_obj["size"] = plotsOpts.style.size;
        _tmp_obj["shape"] = plotsOpts.style.shape;
        _tmp_obj["stroke_width"] = plotsOpts.style.stroke_width;
        _tmp_obj["text"] = plotsOpts.text.legends.non_mut;
        plotsOpts.legends.push(_tmp_obj);
        //Update Mutated Cases' Styles
        $.each(dataArr, function(index, obj) {
            if (obj.hasOwnProperty("mutation")) {
                if (obj["mutation"].hasOwnProperty(geneX) &&
                    obj["mutation"].hasOwnProperty(geneY)) {
                    obj.stroke = plotsOpts.style.mutations.gene_both_mutate_stroke;
                    obj.fill = plotsOpts.style.mutations.gene_both_mutate_fill;
                } else if (obj["mutation"].hasOwnProperty(geneX) &&
                    !obj["mutation"].hasOwnProperty(geneY)) {
                    obj.stroke = plotsOpts.style.mutations.gene_x_mutate_stroke;
                    obj.fill = plotsOpts.style.mutations.gene_x_mutate_fill;
                } else if (!obj["mutation"].hasOwnProperty(geneX) &&
                    obj["mutation"].hasOwnProperty(geneY)) {
                    obj.stroke = plotsOpts.style.mutations.gene_y_mutate_stroke;
                    obj.fill = plotsOpts.style.mutations.gene_y_mutate_fill;
                } 
            } else {
                obj.stroke = plotsOpts.style.stroke;
                obj.fill = plotsOpts.style.fill;
            }
        });

    }

    function configHeaderSettings() {
        plotsOpts.text.title = "mRNA co-expression: " + geneX + " vs. " + geneY + "  ";
        plotsOpts.text.fileName = "co_expression_result-" + geneX + "-" + geneY;
        //determine if turn on log scale 
        if (dataAttr.profile_name.indexOf("RNA Seq") !== -1) {
            settings.enable_log_scale = true;
        } 
    }

    function initDivs(_divName) {
        $("#" + plotsDivId).empty();
        $("#" + plotsDivId).append(
            "<div id='" + plotsOpts.names.header + 
            "' style='margin-left: " + (plotsOpts.canvas.xLeft) + "px;" +  
            "width: " + (plotsOpts.canvas.xRight - plotsOpts.canvas.xLeft) + "px;" + 
            "margin-top: 40px; margin-bottom: -10px; text-align: center;'>" + 
            "</div>");
        $("#" + plotsDivId).append("<div id='" + plotsOpts.names.body + "'></div>");
    }

    function initPlots() {
        //Init Plots
        var coexpPlots = new ScatterPlots();
        coexpPlots.init(plotsOpts, dataArr, dataAttr, false);
        PlotsHeader.init(
            plotsOpts.names, 
            plotsOpts.text.title, 
            plotsOpts.text.fileName, 
            settings.enable_log_scale,
            coexpPlots.loadSvg
        );
        //Bind event listeners
        $("#" + plotsOpts.names.show_mutations).change(function() {
            coexpPlots.updateMutations(
                plotsOpts.names.show_mutations,
                plotsOpts.names.log_scale_x,
                plotsOpts.names.log_scale_y
            );
        });
        if (settings.enable_log_scale) {
            $("#" + plotsOpts.names.log_scale_x).change(function() {
                coexpPlots.updateScaleX(plotsOpts.names.log_scale_x);
            });
            $("#" + plotsOpts.names.log_scale_y).change(function() {
                coexpPlots.updateScaleY(plotsOpts.names.log_scale_y);
            });
        }
        $("#" + plotsOpts.names.download_pdf).submit(function() { loadSvg("pdf"); });
        $("#" + plotsOpts.names.download_svg).submit(function() { loadSvg("svg"); });
    }

    function loadSvg(type) {
        //Remove the help icons
        $("#" + plotsOpts.names.body + " .plots-title-x-help").remove();
        $("#" + plotsOpts.names.body + " .plots-title-y-help").remove();
        //extract the "clean" svg
        var result = $("#" + plotsOpts.names.body).html();
        if (type === "pdf") {
            $("#" + plotsOpts.names.download_pdf + " :input").each(
                function() {
                    if (this.name === "svgelement") {
                        this.value = result;
                    }
                }
            );
        } if (type === "svg") {
            $("#" + plotsOpts.names.download_svg + " :input").each(
                function() {
                    if (this.name === "svgelement") {
                        this.value = result;
                    }
                }
            );
        }
        //Add help icons back on
        coexpPlots.updateTitleHelp(plotsOpts.names.log_scale_x, plotsOpts.names.log_scale_y);      
    }

    return {
        init: function(_divName, _geneX, _geneY, _dataArr, _dataAttr) {
            //set params
            plotsDivId = _divName,
            geneX = _geneX,
            geneY = _geneY,
            dataArr = jQuery.extend(true, [], _dataArr);
            dataAttr = jQuery.extend(true, [], _dataAttr);
            //
            importDefaultSettings();
            configGeneralSettings();
            configPlotsSettings();
            configHeaderSettings();
            initDivs();
            initPlots();
        }
    }

}