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

var PlotsCustomMenu = (function(){

    var content = {
        plots_type_list : {
            "mrna" : { value : "mrna", name :  "mRNA Expression" },
            "copy_no" : { value : "copy_no", name :  "Copy Number Alteration" },
            "methylation" : { value : "methylation", name :  "DNA Methylation" },
            "rppa" : { value : "rppa", name :  "RPPA Protein Level" }
        },
        geneX_genetic_profiles: {
            genetic_profile_mutations : [],
            genetic_profile_mrna : [],
            genetic_profile_copy_no : [],
            genetic_profile_rppa : [],
            genetic_profile_dna_methylation : []
        },
        geneY_genetic_profiles: {
            genetic_profile_mutations : [],
            genetic_profile_mrna : [],
            genetic_profile_copy_no : [],
            genetic_profile_rppa : [],
            genetic_profile_dna_methylation : []
        }
    };

    function fetchFrameData(geneX, geneY) {
        //geneX
        content.geneX_genetic_profiles.genetic_profile_mutations = Plots.getGeneticProfiles(geneX).genetic_profile_mutations;
        content.geneX_genetic_profiles.genetic_profile_mrna = Plots.getGeneticProfiles(geneX).genetic_profile_mrna;
        content.geneX_genetic_profiles.genetic_profile_copy_no = Plots.getGeneticProfiles(geneX).genetic_profile_copy_no;
        content.geneX_genetic_profiles.genetic_profile_dna_methylation = Plots.getGeneticProfiles(geneX).genetic_profile_dna_methylation;
        content.geneX_genetic_profiles.genetic_profile_rppa = Plots.getGeneticProfiles(geneX).genetic_profile_rppa;
        //geneY
        content.geneY_genetic_profiles.genetic_profile_mutations = Plots.getGeneticProfiles(geneY).genetic_profile_mutations;
        content.geneY_genetic_profiles.genetic_profile_mrna = Plots.getGeneticProfiles(geneY).genetic_profile_mrna;
        content.geneY_genetic_profiles.genetic_profile_copy_no = Plots.getGeneticProfiles(geneY).genetic_profile_copy_no;
        content.geneY_genetic_profiles.genetic_profile_dna_methylation = Plots.getGeneticProfiles(geneY).genetic_profile_dna_methylation;
        content.geneY_genetic_profiles.genetic_profile_rppa = Plots.getGeneticProfiles(geneY).genetic_profile_rppa;
    }

    function appendDropDown(divId, value, text) {
        $(divId).append("<option value='" + value + "'>" + text + "</option>");
    }

    function dataIsDiscretized(profileText) {
        if (profileText.indexOf("GISTIC") !== -1 ||
            profileText.indexOf("RAE") !== -1 ||
            profileText.indexOf("discretization") !== -1) {
            return true;
        }
        return false;
    }

    function generateList(selectId, options) {
        var select = document.getElementById(selectId);
        options.forEach(function(option){
            var el = document.createElement("option");
            el.textContent = option;
            el.value = option;
            select.appendChild(el);
        });
    }

    function setPlatFormDefaultSelection(plotsTypeId, platformId) {
        //----mRNA Priority List: RNA Seq V2, RNA Seq, Z-scores
        //TODO: Changed hard coded html value
        if ($(plotsTypeId).val() === "mrna") {
            $(platformId + " > option").each(function() {
                if (this.text.toLowerCase().indexOf("z-scores")){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $(platformId + " > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $(platformId + " > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
        }

        //----dna methylation priority list: hm450, hm27
        if ($(plotsTypeId).val() === "methylation") {
            $(platformId + " > option").each(function() {
                if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                    $(this).prop('selected', true);
                    return false;
                }
            });
        }
    }

    function updateXselection() {
        $("#custom_platform_select_div_x").empty();
        $("#custom_platform_select_div_x").append(
            "<select id='custom_platform_x' onchange='PlotsCustomView.init();PlotsCustomMenu.updateLogScaleCheckBoxX();' class='plots-select'>");
        if($("#custom_plots_type_x").val() === "mrna"){
            content.geneX_genetic_profiles.genetic_profile_mrna.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection("#custom_plots_type_x", "#custom_platform_x");
        } else if($("#custom_plots_type_x").val() === "copy_no"){
            content.geneX_genetic_profiles.genetic_profile_copy_no.forEach (function (profile) {
                if (!dataIsDiscretized(profile[1])) {  //No listing of discretized data type (profile)
                    $("#custom_platform_x")
                        .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
                }
            });
        } else if($("#custom_plots_type_x").val() === "methylation"){
            content.geneX_genetic_profiles.genetic_profile_dna_methylation.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection("#custom_plots_type_x", "#custom_platform_x");
        } else if($("#custom_plots_type_x").val() === "rppa"){
            content.geneX_genetic_profiles.genetic_profile_rppa.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
        }
    }

    function updateYselection() {
        $("#custom_platform_select_div_y").empty();
        $("#custom_platform_select_div_y").append(
            "<select id='custom_platform_y' onchange='PlotsCustomView.init();PlotsCustomMenu.updateLogScaleCheckBoxY();' class='plots-select'>");

        if($("#custom_plots_type_y").val() === "mrna"){
            content.geneY_genetic_profiles.genetic_profile_mrna.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection("#custom_plots_type_y", "#custom_platform_y");
        } else if($("#custom_plots_type_y").val() === "copy_no"){
            content.geneY_genetic_profiles.genetic_profile_copy_no.forEach (function (profile) {
                if (!dataIsDiscretized(profile[1])) {  //No listing of discretized data type (profile)
                    $("#custom_platform_y")
                        .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
                }
            });
        } else if($("#custom_plots_type_y").val() === "methylation"){
            content.geneY_genetic_profiles.genetic_profile_dna_methylation.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection("#custom_plots_type_y", "#custom_platform_y");
        } else if($("#custom_plots_type_y").val() === "rppa"){
            content.geneY_genetic_profiles.genetic_profile_rppa.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
        }
    }

    function updateLogScaleCheckBoxX() {
        var _str_x = "<input type='checkbox' id='custom_genes_log_scale_option_x' checked onchange='PlotsCustomView.updateLogScaleX();' /> log scale";
        $("#custom_genes_apply_log_scale_div_x").empty();
        if (($("#custom_plots_type_x").val() === "mrna" &&
            $("#custom_platform_x option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
            $("#custom_platform_x option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1)) {
               $("#custom_genes_apply_log_scale_div_x").append(_str_x);
            }
    }

    function updateLogScaleCheckBoxY() {
        var _str_y = "<input type='checkbox' id='custom_genes_log_scale_option_y' checked onchange='PlotsCustomView.updateLogScaleY();' /> log scale";
        $("#custom_genes_apply_log_scale_div_y").empty();
        if (($("#custom_plots_type_y").val() === "mrna" &&
            $("#custom_platform_y option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
            $("#custom_platform_y option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1)) {
            $("#custom_genes_apply_log_scale_div_y").append(_str_y);
        }
    }

    function generateGeneList() {
        generateList("custom_geneX", gene_list);
        var tmp_gene_list = jQuery.extend(true, [], gene_list);
        var tmp_gene_holder = tmp_gene_list.pop();
        tmp_gene_list.unshift(tmp_gene_holder);
        generateList("custom_geneY", tmp_gene_list);
    }

    function generatePlotsTypeList() {
        appendDropDown("#custom_plots_type_x", content.plots_type_list.mrna.value, content.plots_type_list.mrna.name);
        appendDropDown("#custom_plots_type_y", content.plots_type_list.mrna.value, content.plots_type_list.mrna.name);
        if (content.geneX_genetic_profiles.genetic_profile_copy_no.length !== 0) {
            var _flag = false;
            $.each(content.geneX_genetic_profiles.genetic_profile_copy_no, function(index, val) {
                if (!dataIsDiscretized(val[1])) {
                    _flag = true;
                }
            });     //If contains continuous data type
            if (_flag) {
                appendDropDown("#custom_plots_type_x", content.plots_type_list.copy_no.value, content.plots_type_list.copy_no.name);
            }
        }
        if (content.geneY_genetic_profiles.genetic_profile_copy_no.length !== 0) {
            var _flag = false;
            $.each(content.geneX_genetic_profiles.genetic_profile_copy_no, function(index, val) {
                if (!dataIsDiscretized(val[1])) {
                    _flag = true;
                }
            });     //If contains continuous data type
            if (_flag) {
                appendDropDown("#custom_plots_type_y", content.plots_type_list.copy_no.value, content.plots_type_list.copy_no.name);
            }
        }
        if (content.geneX_genetic_profiles.genetic_profile_dna_methylation.length !== 0) {
            appendDropDown("#custom_plots_type_x", content.plots_type_list.methylation.value, content.plots_type_list.methylation.name);
        }
        if (content.geneY_genetic_profiles.genetic_profile_dna_methylation.length !== 0) {
            appendDropDown("#custom_plots_type_y", content.plots_type_list.methylation.value, content.plots_type_list.methylation.name);
        }
        if (content.geneX_genetic_profiles.genetic_profile_rppa.length !== 0) {
            appendDropDown("#custom_plots_type_x", content.plots_type_list.rppa.value, content.plots_type_list.rppa.name);
        }
        if (content.geneY_genetic_profiles.genetic_profile_rppa.length !== 0) {
            appendDropDown("#custom_plots_type_y", content.plots_type_list.rppa.value, content.plots_type_list.rppa.name);
        }
    }

    return {
        init: function() {
            generateGeneList();
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            generatePlotsTypeList();
            updateXselection();
            updateYselection();
            updateLogScaleCheckBoxX();
            updateLogScaleCheckBoxY();
        },
        updateX: function(){
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            updateXselection();
            updateLogScaleCheckBoxX();
        },
        updateY: function(){
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            updateYselection();
            updateLogScaleCheckBoxY();
        },
        updateLogScaleCheckBoxX: updateLogScaleCheckBoxX,
        updateLogScaleCheckBoxY: updateLogScaleCheckBoxY

    };
}());   //Closing PlotsCustomMenu

var PlotsCustomView = (function() {

    //Extracted data from JSON for plotting
    //Dots collection
    var pData = {
            case_set_length : 0,
            dotsData : []
        },
    //Data Set Status (empty)
        errStatus = {
            xHasData : false,
            yHasData : false
        },
    //The template for creating dot unit
        singleDot = {
            case_id : "",
            x_value : "",
            y_value : "",
            annotation: ""  //Mutation (for now)
        },
    //Current Selection from the menu
        menu = {
            geneX : "",
            geneY : "",
            plots_type_x: "",
            plots_type_y: "",
            genetic_profile_id_x: "",
            genetic_profile_id_y: ""
        },
    //Canvas Settings
        settings = {
            canvas_width: 722,
            canvas_height: 600
        },
    //DOMs
        elem = {
            svg : "",
            xScale : "",
            yScale : "",
            xAxis : "",
            yAxis : "",
            dotsGroup : "",
            axisTitleGroup: ""
        },
        style = {
            geneX_mut : {
                fill : "#DBA901",
                stroke : "#886A08",
                text : "GeneX mutated"
            },
            geneY_mut : {
                fill : "#F5A9F2",
                stroke : "#F7819F",
                text : "GeneY mutated"
            },
            both_mut : {
                fill : "#FF0000",
                stroke : "#B40404",
                text : "Both mutated"
            },
            non_mut : {
                fill : "#00AAF8",
                stroke : "#0089C6",
                text : "Neither mutated"
            }
        };

    function isEmpty(inputVal) {
        if (inputVal !== "NaN" && inputVal !== "NA") {
            return false;
        }
        return true;
    }

    function pDataInit(result) {
        var tmp_singleDot = {
            case_id : "",
            value: "",
            annotation: ""
        };
        var tmp_pDataX = [];
        var tmp_pDataY = [];
        pData.dotsData.length = 0;
        pData.case_set_length = 0;
        if (menu.geneX === menu.geneY) {    //same gene situation
            for (var gene in result) {
                var geneObj = result[gene];
                for (var case_id in geneObj) {
                    var obj = geneObj[case_id];
                    var new_tmp_singleDot_x = jQuery.extend(true, {}, tmp_singleDot);
                    var new_tmp_singleDot_y = jQuery.extend(true, {}, tmp_singleDot);
                    new_tmp_singleDot_x.case_id = case_id;
                    new_tmp_singleDot_y.case_id = case_id;
                    for (var i = 0; i < Object.keys(obj).length; i++) {
                        if (Object.keys(obj)[i] === menu.genetic_profile_id_x) {
                            var tmp_profile_x_index = i;
                        }
                    }
                    for (var i = 0; i < Object.keys(obj).length; i++) {
                        if (Object.keys(obj)[i] === menu.genetic_profile_id_y) {
                            var tmp_profile_y_index = i;
                        }
                    }
                    var tmp_profile_annotation_index = Object.keys(obj).length - 1;
                    new_tmp_singleDot_x.value = obj[Object.keys(obj)[tmp_profile_x_index]];
                    new_tmp_singleDot_x.annotation = obj[Object.keys(obj)[tmp_profile_annotation_index]];//mutation
                    new_tmp_singleDot_y.value = obj[Object.keys(obj)[tmp_profile_y_index]];
                    new_tmp_singleDot_y.annotation = obj[Object.keys(obj)[tmp_profile_annotation_index]];//mutation
                    tmp_pDataX.push(new_tmp_singleDot_x);
                    tmp_pDataY.push(new_tmp_singleDot_y);
                }
            }
        } else {
            for (var gene in result) {
                if (gene === menu.geneX) {
                    var geneObj = result[gene];
                    for (var case_id in geneObj) {
                        var obj = geneObj[case_id];
                        var new_tmp_singleDot = jQuery.extend(true, {}, tmp_singleDot);
                        new_tmp_singleDot.case_id = case_id;
                        for (var i = 0; i < Object.keys(obj).length; i++) {
                            if (Object.keys(obj)[i] === menu.genetic_profile_id_x) {
                                var tmp_profile_x_index = i;
                            }
                        }
                        new_tmp_singleDot.value = obj[Object.keys(obj)[tmp_profile_x_index]];
                        if (obj.hasOwnProperty(cancer_study_id + "_mutations")) { //mutation
                            var tmp_profile_annotation_index = Object.keys(obj).indexOf(cancer_study_id + "_mutations");
                            new_tmp_singleDot.annotation = obj[Object.keys(obj)[tmp_profile_annotation_index]];
                        } else {
                            new_tmp_singleDot.annotation = "NaN";
                        }
                        tmp_pDataX.push(new_tmp_singleDot);
                    }
                } else if (gene === menu.geneY) {
                    var geneObj = result[gene];
                    for (var case_id in geneObj) {
                        var obj = geneObj[case_id];
                        var new_tmp_singleDot = jQuery.extend(true, {}, tmp_singleDot);
                        new_tmp_singleDot.case_id = case_id;
                        for (var i = 0; i < Object.keys(obj).length; i++) {
                            if (Object.keys(obj)[i] === menu.genetic_profile_id_y) {
                                var tmp_profile_y_index = i;
                            }
                        }
                        new_tmp_singleDot.value = obj[Object.keys(obj)[tmp_profile_y_index]];//profile id
                        if (obj.hasOwnProperty(cancer_study_id + "_mutations")) { //mutation
                            var tmp_profile_annotation_index = Object.keys(obj).indexOf(cancer_study_id + "_mutations");
                            new_tmp_singleDot.annotation = obj[Object.keys(obj)[tmp_profile_annotation_index]];
                        } else {
                            new_tmp_singleDot.annotation = "NaN";
                        }
                        tmp_pDataY.push(new_tmp_singleDot);
                    }
                }
            }
        }

        //Error Handle: spot empty dataset
        errStatus.xHasData = false;
        errStatus.yHasData = false;
        $.each(tmp_pDataX, function(key, obj) {
            if (!isEmpty(obj.value)) {
                errStatus.xHasData = true;
            }
        });
        $.each(tmp_pDataY, function(key, obj) {
            if (!isEmpty(obj.value)) {
                errStatus.yHasData = true;
            }
        });

        //merge tmp_pDataX, tmp_pDataY, and filter empty data
        for (var i = 0; i < tmp_pDataY.length; i++) {
            if (!isEmpty(tmp_pDataX[i].value) && !isEmpty(tmp_pDataY[i].value)) {
                pData.case_set_length += 1;

                var new_singleDot = jQuery.extend(true, {}, singleDot);
                new_singleDot.case_id = tmp_pDataX[i].case_id;
                new_singleDot.x_value = tmp_pDataX[i].value;
                new_singleDot.y_value = tmp_pDataY[i].value;

                var tmp_annotation_str = "";
                if (!isEmpty(tmp_pDataX[i].annotation)) {
                    tmp_annotation_str +=
                        menu.geneX + ": " + tmp_pDataX[i].annotation + "&nbsp;&nbsp;";
                }
                if (!isEmpty(tmp_pDataY[i].annotation)) {
                    tmp_annotation_str +=
                        menu.geneY + ": " + tmp_pDataY[i].annotation;
                }

                //handle same gene situation
                if (menu.geneX === menu.geneY) {
                    tmp_annotation_str = tmp_annotation_str.substring(0, tmp_annotation_str.length/2);
                }

                new_singleDot.annotation = tmp_annotation_str.trim();
                pData.dotsData.push(new_singleDot);
            }
        }
    }

    function initCanvas() {
        $('#plots_box').empty();
        elem.svg = d3.select("#plots_box")
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);
        elem.dotsGroup = elem.svg.append("svg:g");
        elem.axisTitleGroup = elem.svg.append("svg:g").attr("class", "axis");
    }

    function getUserSelection() {
        menu.geneX = document.getElementById("custom_geneX").value;
        menu.geneY = document.getElementById("custom_geneY").value;
        menu.plots_type_x = document.getElementById("custom_plots_type_x").value;
        menu.plots_type_y = document.getElementById("custom_plots_type_y").value;
        menu.genetic_profile_id_x = document.getElementById("custom_platform_x").value.split("|")[0];
        menu.genetic_profile_id_y = document.getElementById("custom_platform_y").value.split("|")[0];
    }

    function generatePlots() {
        getProfileData();
    }

    function getProfileData() {
        Plots.getProfileData(
            menu.geneX + " " + menu.geneY,
            menu.genetic_profile_id_x + " " + menu.genetic_profile_id_y + " " + cancer_study_id + "_mutations",
            case_set_id,
            case_ids_key,
            getProfileDataCallBack
        );
    }

    function analyseData() {
        var tmp_xData = [];
        var tmp_xIndex = 0;
        var tmp_yData = [];
        var tmp_yIndex = 0;
        for (var j = 0; j< pData.case_set_length; j++){
            if (!isEmpty(pData.dotsData[j].x_value) && !isEmpty(pData.dotsData[j].y_value)) {
                tmp_xData[tmp_xIndex] = pData.dotsData[j].x_value;
                tmp_xIndex += 1;
                tmp_yData[tmp_yIndex] = pData.dotsData[j].y_value;
                tmp_yIndex += 1;
            }
        }
        var min_x = Math.min.apply(Math, tmp_xData);
        var max_x = Math.max.apply(Math, tmp_xData);
        var min_y = Math.min.apply(Math, tmp_yData);
        var max_y = Math.max.apply(Math, tmp_yData);
        return {
            min_x: min_x,
            max_x: max_x,
            min_y: min_y,
            max_y: max_y
        };
    }

    function initXAxis(applyLogScale) {
        var analyseResult = analyseData();
        if (applyLogScale) {
            if (analyseResult.min_x <= (Plots.getLogScaleThreshold())) {
                var min_x = Math.log(Plots.getLogScaleThreshold()) / Math.log(2);
            } else {
                var min_x = Math.log(analyseResult.min_x) / Math.log(2);
            }
            var max_x = Math.log(analyseResult.max_x) / Math.log(2);
        } else {
            var min_x = analyseResult.min_x;
            var max_x = analyseResult.max_x;
        }
        var edge_x = (max_x - min_x) * 0.2;
        var rangeXmin = min_x - edge_x;
        var rangeXmax = max_x + edge_x;
        if (!applyLogScale) {
            if (menu.plots_type_x === "methylation") { //Fix the range for methylation data
                rangeXmin = -0.02;
                rangeXmax = 1.02;
            }
        }
        elem.xScale = d3.scale.linear()
            .domain([rangeXmin, rangeXmax])
            .range([100, 600]);
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom")
            .tickSize(6, 0, 0)
            .tickPadding([8]);
    }

    function initYAxis(applyLogScale) {
        var analyseResult = analyseData();
        if (applyLogScale) {
            if (analyseResult.min_y <= (Plots.getLogScaleThreshold())) {
                var min_y = Math.log(Plots.getLogScaleThreshold()) / Math.log(2);
            } else {
                var min_y = Math.log(analyseResult.min_y) / Math.log(2);
            }
            var max_y = Math.log(analyseResult.max_y) / Math.log(2);
        } else {
            var min_y = analyseResult.min_y;
            var max_y = analyseResult.max_y;
        }
        var edge_y = (max_y - min_y) * 0.1;
        var rangeYmin = min_y - edge_y;
        var rangeYmax = max_y + edge_y;
        if (!applyLogScale) {
            if (menu.plots_type_y === "methylation") {
                rangeYmin = -0.02;
                rangeYmax = 1.02;
            }
        }
        elem.yScale = d3.scale.linear()
            .domain([rangeYmin, rangeYmax])
            .range([520, 20]);
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .orient("left")
            .tickSize(6, 0, 0)
            .tickPadding([8]);
    }

    function drawAxisX() {
        d3.select("#plots_box").select(".plots-x-axis-class").remove();
        elem.svg.append("g")
            .style("stroke-width", 1.5)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 520)")
            .attr("class", "plots-x-axis-class")
            .call(elem.xAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "12px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 1.5)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 20)")
            .call(elem.xAxis.orient("bottom").ticks(0));
    }

    function drawAxisY() {
        d3.select("#plots_box").select(".plots-y-axis-class").remove();
        elem.svg.append("g")
            .style("stroke-width", 1.5)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(100, 0)")
            .attr("class", "plots-y-axis-class")
            .call(elem.yAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "12px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 1.5)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(600, 0)")
            .call(elem.yAxis.orient("left").ticks(0));
    }

    function drawPlots() {
        //sort DotsData
        var tmp_dotsData = pData.dotsData;
        var nonMutatedData = [];
        var mutatedData= [];
        var dataBuffer = [];

        tmp_dotsData.forEach (function(entry) {
            if (entry.annotation !== "") {
                mutatedData.push(entry);
            } else {
                nonMutatedData.push(entry);
            }
        });

        nonMutatedData.forEach (function(entry) {
            dataBuffer.push(entry);
        });
        mutatedData.forEach (function(entry) {
            dataBuffer.push(entry);
        });
        tmp_dotsData = dataBuffer;

        elem.dotsGroup.selectAll("path").remove();
        var showMutation = document.getElementById("show_mutation_custom_view").checked;
        elem.dotsGroup.selectAll("path")
            .data(tmp_dotsData)
            .enter()
            .append("svg:path")
            .attr("transform", function(d){
                $(this).attr("x_pos", elem.xScale(d.x_value));
                $(this).attr("y_pos", elem.yScale(d.y_value));
                $(this).attr("x_val", d.x_value);
                $(this).attr("y_val", d.y_value);
                $(this).attr("size", 20);
                return "translate(" + elem.xScale(d.x_value) + ", " + elem.yScale(d.y_value) + ")";
            })
            .attr("d", d3.svg.symbol()
                .size(20)
                .type("circle"))
            .attr("fill", function(d) {
                if (showMutation) {
                    if (d.annotation === "") {
                        return style.non_mut.fill;
                    } else {
                        var count = d.annotation.split(":").length - 1;
                        if (count === 1) { //single mut
                            if (d.annotation.indexOf(menu.geneX) !== -1) {
                                return style.geneX_mut.fill;
                            } else if (d.annotation.indexOf(menu.geneY) !== -1) {
                                return style.geneY_mut.fill;
                            }
                        } else if (count === 2) { //both mut
                            return style.both_mut.fill;
                        }
                    }
                } else {
                    return style.non_mut.fill;
                }
            })
            .attr("stroke", function(d) {
                if (showMutation) {
                    if (d.annotation === "") {
                        return style.non_mut.stroke;
                    } else {
                        var count = d.annotation.split(":").length - 1;
                        if (count === 1) { //single mut
                            if (d.annotation.indexOf(menu.geneX) !== -1) {
                                return style.geneX_mut.stroke;
                            } else if (d.annotation.indexOf(menu.geneY) !== -1) {
                                return style.geneY_mut.stroke;
                            }
                        } else if (count === 2) { //both mut
                            return style.both_mut.stroke;
                        }
                    }
                } else {
                    return style.non_mut.stroke;
                }
            })
            .attr("stroke-width", function(d) {
                return "1.2";
            })
            .attr("class", function(d) { return d.case_id; });
    }

    function updatePlotsLogScale(axis, applyLogScale) {
        elem.dotsGroup.selectAll("path")
            .transition().duration(300)
            .attr("transform", function() {
                if (applyLogScale) {
                    if (axis === "x") {
                        if (parseFloat(d3.select(this).attr("x_val")) <= (Plots.getLogScaleThreshold())) {
                            var _post_x = elem.xScale(Math.log(Plots.getLogScaleThreshold()) / Math.log(2));
                        } else {
                            var _post_x = elem.xScale(Math.log(d3.select(this).attr("x_val")) / Math.log(2));
                        }
                        var _post_y = d3.select(this).attr("y_pos");
                    } else if (axis === "y") {
                        var _post_x = d3.select(this).attr("x_pos");
                        if (parseFloat(d3.select(this).attr("y_val")) <= (Plots.getLogScaleThreshold())) {
                            var _post_y = elem.yScale(Math.log(Plots.getLogScaleThreshold()) / Math.log(2));
                        } else {
                            var _post_y = elem.yScale(Math.log(d3.select(this).attr("y_val")) / Math.log(2));
                        }

                    }
                    d3.select(this).attr("x_pos", _post_x);
                    d3.select(this).attr("y_pos", _post_y);
                    return "translate(" + _post_x + ", " + _post_y + ")";
                } else {
                    if (axis === "x") {
                        var _post_x = elem.xScale(d3.select(this).attr("x_val"));
                        var _post_y = d3.select(this).attr("y_pos");
                    } else if (axis === "y") {
                        var _post_x = d3.select(this).attr("x_pos");
                        var _post_y = elem.yScale(d3.select(this).attr("y_val"));
                    }
                    d3.select(this).attr("x_pos", _post_x);
                    d3.select(this).attr("y_pos", _post_y);
                    return "translate(" + _post_x + ", " + _post_y + ")";
                }
            });
    }

    function drawLegends() {
        var showMutation = document.getElementById("show_mutation_custom_view").checked;
        if (showMutation) {
            var twoGenesStyleArr = [];
            twoGenesStyleArr.splice(0, twoGenesStyleArr.length);
            twoGenesStyleArr.length = 0;
            for (var key in style) {
                var obj = style[key];
                twoGenesStyleArr.push(obj);
            }

            //Only show glyphs "mutated" and "non mutated" for same gene situation
            if (menu.geneX === menu.geneY) {
                twoGenesStyleArr.splice(1, 1);
                twoGenesStyleArr.splice(1, 1);
            }

            var legend = elem.svg.selectAll(".legend")
                .data(twoGenesStyleArr)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) {
                    return "translate(610, " + (30 + i * 15) + ")";
                })

            legend.append("path")
                .attr("width", 18)
                .attr("height", 18)
                .attr("d", d3.svg.symbol()
                    .size(30)
                    .type(function(d) { return "circle"; }))
                .attr("fill", function (d) { return d.fill; })
                .attr("stroke", function (d) { return d.stroke; })
                .attr("stroke-width", 1.1);

            legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d) {
                    if (d.text.indexOf("GeneX") !== -1) {
                        var tmp_legend = d.text.replace("GeneX", menu.geneX);
                    } else if (d.text.indexOf("GeneY") !== -1) {
                        var tmp_legend = d.text.replace("GeneY", menu.geneY);
                    } else if (d.text.indexOf("Neither") !== -1) {
                        if (menu.geneX === menu.geneY) {
                            var tmp_legend = "No mutation";
                        } else {
                            var tmp_legend = d.text;
                        }
                    } else {
                        var tmp_legend = d.text;
                    }
                    return tmp_legend;
                });
        } else {
            var legend = elem.svg.selectAll("g.legend").remove();
        }
    }

    function drawAxisXTitle(applyLogScale) {
        d3.select("#plots_box").select(".custom-label-x").remove();
        d3.select("#plots_box").select(".x-title-help").remove();
        var elt_x = document.getElementById("custom_platform_x");
        var titleText_x = elt_x.options[elt_x.selectedIndex].text;
        var x_titleHelp = elt_x.options[elt_x.selectedIndex].value.split("|")[1];
        if (applyLogScale) {
            var xTitle = menu.geneX + ", " + titleText_x + "(log2)";
        } else {
            var xTitle = menu.geneX + ", " + titleText_x;
        }
        elem.axisTitleGroup.append("text")
            .attr("class", "custom-label-x")
            .attr("x", 350)
            .attr("y", 580)
            .style("text-anchor", "middle")
            .style("font-weight","bold")
            .text(xTitle);
        Plots.addxAxisHelp(
            elem.svg,
            elem.axisTitleGroup,
            xTitle,
            "x-title-help",
            x_titleHelp
        );
    }

    function drawAxisYTitle(applyLogScale) {
        d3.select("#plots_box").select(".custom-label-y").remove();
        d3.select("#plots_box").select(".y-title-help").remove();
        var elt_y = document.getElementById("custom_platform_y");
        var titleText_y = elt_y.options[elt_y.selectedIndex].text;
        var y_titleHelp = elt_y.options[elt_y.selectedIndex].value.split("|")[1];
        if (applyLogScale) {
            var yTitle = menu.geneY + ", " + titleText_y + "(log2)";
        } else {
            var yTitle = menu.geneY + ", " + titleText_y;
        }
        elem.axisTitleGroup.append("text")
            .attr("class", "custom-label-y")
            .attr("transform", "rotate(-90)")
            .attr("x", -270)
            .attr("y", 45)
            .style("text-anchor", "middle")
            .style("font-weight","bold")
            .text(yTitle);
        Plots.addyAxisHelp(
            elem.svg,
            elem.axisTitleGroup,
            yTitle,
            "y-title-help",
            y_titleHelp
        );
    }

    function addQtips() {
        elem.dotsGroup.selectAll('path').each(
            function(d) {
                var content = "<font size='2'>";
                content += "Case ID: " + "<strong><a href='tumormap.do?case_id=" + d.case_id +
                    "&cancer_study_id=" + cancer_study_id + "' target = '_blank'>" + d.case_id + "</a></strong><br>";
                if (menu.geneX === menu.geneY) {
                    content += "x-Val: <strong>" + parseFloat(d.x_value).toFixed(3) + "</strong><br>" +
                        "y-Val: <strong>" + parseFloat(d.y_value).toFixed(3) + "</strong><br>";
                } else {
                    content += menu.geneX + ": <strong>" + parseFloat(d.x_value).toFixed(3) + "</strong><br>" +
                        menu.geneY + ": <strong>" + parseFloat(d.y_value).toFixed(3) + "</strong><br>";
                }
                if (d.annotation !== "") {
                    if (menu.geneX === menu.geneY) {
                        var tmp_anno_str = d.annotation.substring(d.annotation.indexOf(":") + 1, d.annotation.length).replace(/,/g, ", ");
                    } else {
                        var tmp_anno_str = d.annotation.replace(/,/g, ", ");
                    }
                    content += "Mutation: <strong>" + tmp_anno_str + "</strong>";
                }
                content = content + "</font>";

                $(this).qtip(
                    {
                        content: {text: content},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
	                    hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right'}
                    }
                );

                var mouseOn = function() {
                    var dot = d3.select(this);
                    dot.transition()
                        .ease("elastic")
                        .duration(600)
                        .delay(100)
                        .attr("d", d3.svg.symbol().size(200).type("circle"));
                };

                var mouseOff = function() {
                    var dot = d3.select(this);
                    dot.transition()
                        .ease("elastic")
                        .duration(600)
                        .delay(100)
                        .attr("d", d3.svg.symbol().size(20).type("circle"));
                };

                elem.dotsGroup.selectAll("path").on("mouseover", mouseOn);
                elem.dotsGroup.selectAll("path").on("mouseout", mouseOff);
            }
        );
    }

    function drawImgConverter() {
        $('#view_title').empty();
        $('#view_title').append("Custom View : " + menu.geneX + " vs. " + menu.geneY);

        var pdfConverterForm =
            "<form style='display:inline-block' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='correlation_plots-" + menu.geneX + "_" + menu.geneY + ".pdf'>" +
                "<input type='submit' value='PDF'></form>";
        $('#view_title').append(pdfConverterForm);

        var svgConverterForm =
            "<form style='display:inline-block' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' name='filename' value='correlation_plots-" + menu.geneX + "_" + menu.geneY + ".svg'>" +
                "<input type='submit' value='SVG'></form>";
        $('#view_title').append(svgConverterForm);
    }

    function drawErrorMsg() {
        $('#view_title').empty();
        elem.svg.empty();

        var _line1 = "";
        var _line2 = " in the selected cancer study.";
        var _line3 = "";
        if (!errStatus.xHasData && errStatus.yHasData) {
            _line1 = "There is no " + $("#custom_platform_x option:selected").html() + " data for";
            _line2 = menu.geneX + _line2;
        } else if (!errStatus.yHasData && errStatus.xHasData) {
            _line1 = "There is no " + $("#custom_platform_y option:selected").html() + " data for";
            _line2 = menu.geneY + _line2;
        } else if (!errStatus.yHasData && !errStatus.xHasData) {
            _line1 = "There is no " + $("#custom_platform_x option:selected").html() + " data for " + menu.geneX;
            _line3 = _line2;
            _line2 = "and no " + $("#custom_platform_y option:selected").html() + " data for " + menu.geneY;
        }

        elem.svg.append("text")
            .attr("x", 350)
            .attr("y", 55)
            .attr("text-anchor", "middle")
            .attr("fill", "#DF3A01")
            .text(_line1)
        elem.svg.append("text")
            .attr("x", 350)
            .attr("y", 70)
            .attr("text-anchor", "middle")
            .attr("fill", "#DF3A01")
            .text(_line2)
        elem.svg.append("text")
            .attr("x", 350)
            .attr("y", 85)
            .attr("text-anchor", "middle")
            .attr("fill", "#DF3A01")
            .text(_line3)
        elem.svg.append("rect")
            .attr("x", 150)
            .attr("y", 30)
            .attr("width", 400)
            .attr("height", 70)
            .attr("fill", "none")
            .attr("stroke-width", 1)
            .attr("stroke", "#BDBDBD");

    }

    function updateMutationDisplay() {
        drawPlots();
        drawLegends();
        addQtips();
        var applyLogScale_x = false;
        var applyLogScale_y = false;
        if (document.getElementById("custom_genes_log_scale_option_x") !== null) {
            applyLogScale_x = document.getElementById("custom_genes_log_scale_option_x").checked;
        }
        if (document.getElementById("custom_genes_log_scale_option_y") !== null) {
            applyLogScale_y = document.getElementById("custom_genes_log_scale_option_y").checked;
        }
        updatePlotsLogScale("x", applyLogScale_x);
        updatePlotsLogScale("y", applyLogScale_y);
    }

    function getProfileDataCallBack(result) {
        pDataInit(result);
        initCanvas();
        if (pData.dotsData.length !== 0) {
            $("#show_mutation_custom_view").attr("disabled", false);
            $('#view_title').show();
            $('#plots_box').show();
            $('#loading-image').hide();
            var applyLogScale_x = false;
            var applyLogScale_y = false;
            if (document.getElementById("custom_genes_log_scale_option_x") !== null) {
                applyLogScale_x = true;
            }
            if (document.getElementById("custom_genes_log_scale_option_y") !== null) {
                applyLogScale_y = true;
            }
            initXAxis(applyLogScale_x);
            initYAxis(applyLogScale_y);
            drawAxisX();
            drawAxisY();
            drawPlots();
            drawLegends();
            drawAxisXTitle(applyLogScale_x);
            drawAxisYTitle(applyLogScale_y);
            addQtips();
            drawImgConverter();
            updatePlotsLogScale("x", applyLogScale_x);
            updatePlotsLogScale("y", applyLogScale_y);
        } else {
            $("#show_mutation_custom_view").attr("disabled", true);
            $('#view_title').show();
            $('#plots_box').show();
            $('#loading-image').hide();
            drawErrorMsg();
        }
    }

    return {
        init : function() {
            $('#view_title').empty();
            $('#plots_box').empty();
            $('#loading-image').show();
            $('#view_title').hide();
            $('#plots_box').hide();

            getUserSelection();
            //Contains a series of chained function
            //Including data fetching and drawing
            generatePlots();
        },
        update : function() {
            //TODO: using cache
        },
        updateMutationDisplay : updateMutationDisplay,
        updateLogScaleX: function() { //axis --> indicate x or y
            var applyLogScale = document.getElementById("custom_genes_log_scale_option_x").checked;
            initXAxis(applyLogScale);
            drawAxisX();
            drawAxisXTitle(applyLogScale);
            updatePlotsLogScale("x", applyLogScale);
        },
        updateLogScaleY: function() {  //axis --> indicate x or y
            var applyLogScale = document.getElementById("custom_genes_log_scale_option_y").checked;
            initYAxis(applyLogScale);
            drawAxisY();
            drawAxisYTitle(applyLogScale);
            updatePlotsLogScale("y", applyLogScale);
        }
    };
}());  //Closing PlotsCustomView