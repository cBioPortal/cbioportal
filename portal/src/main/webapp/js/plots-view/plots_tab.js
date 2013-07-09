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

var PlotsMenu = (function () {
    var content = {
            plots_type : {
                "mrna_copyNo" : {
                    value : "mrna_vs_copy_no",
                    text : "mRNA vs. Copy Number"
                },
                "mrna_methylation" : {
                    value : "mrna_vs_dna_methylation",
                    text : "mRNA vs. DNA Methylation"

                },
                "rppa_mrna" : {
                    value : "rppa_protein_level_vs_mrna",
                    text : "RPPA Protein Level vs. mRNA"
                }
            },
            data_type : {
                "mrna" : {
                    label: "- mRNA -",
                    value: "data_type_mrna",
                    genetic_profile : []
                },
                "copy_no" : {
                    label: "- Copy Number -",
                    value: "data_type_copy_no",
                    genetic_profile : []
                },
                "dna_methylation" : {
                    label: "- DNA Methylation -",
                    value: "data_type_dna_methylation",
                    genetic_profile : []

                },
                "rppa" : {
                    label: "- RPPA Protein Level -",
                    value: "data_type_rppa",
                    genetic_profile : []

                },
                "mutation" : { //hidden field
                    label: "",
                    value: "data_type_mutation",
                    genetic_profile : [],
                }
            }
        },
        status = {
            has_mrna : false,
            has_dna_methylation : false,
            has_rppa : false,
            has_copy_no : false
        };

    var Util = (function() {
        function appendDropDown(divId, value, text) {
            $(divId).append("<option value='" + value + "'>" + text + "</option>");
        }

        function toggleVisibility(elemId, switchToStatus) {
            var e = document.getElementById(elemId);
            if (switchToStatus === "show") {
                e.style.display = 'block';
            } else if (switchToStatus === "hide") {
                e.style.display = 'none';
            }
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

        return {
            appendDropDown: appendDropDown,
            toggleVisibility: toggleVisibility,
            generateList: generateList,
        };

    }());

    function drawMenu() {
        //Gene List
        Util.generateList("genes", gene_list);

        //Plots Type field
        if (status.has_mrna && status.has_copy_no) {
            Util.appendDropDown(
                '#plots_type',
                content.plots_type.mrna_copyNo.value,
                content.plots_type.mrna_copyNo.text
            );
        }
        if (status.has_mrna && status.has_dna_methylation) {
            Util.appendDropDown(
                '#plots_type',
                content.plots_type.mrna_methylation.value,
                content.plots_type.mrna_methylation.text
            );
        }
        if (status.has_mrna && status.has_rppa) {
            Util.appendDropDown(
                '#plots_type',
                content.plots_type.rppa_mrna.value,
                content.plots_type.rppa_mrna.text
            );
        }

        //Data Type Field : profile
        for (var key in content.data_type) {
            var singleDataTypeObj = content.data_type[key];
            $("#one_gene_platform_select_div").append(
                "<div id='" + singleDataTypeObj.value + "_dropdown' style='padding:5px;'>" +
                    "<label for='" + singleDataTypeObj.value + "'>" + singleDataTypeObj.label + "</label><br>" +
                    "<select id='" + singleDataTypeObj.value + "' onchange='PlotsView.init()'></select></div>"
            );
            for (var index in singleDataTypeObj.genetic_profile) { //genetic_profile is ARRAY!
                var item_profile = singleDataTypeObj.genetic_profile[index];
                $("#" + singleDataTypeObj.value).append(
                    "<option value='" + item_profile[0] + "'>" + item_profile[1] + "</option>");
            }
        }

    }

    function setDefaultCopyNoSelection() {
        //-----Priority: discretized(gistic, rae), continuous
        //TODO: refactor
        $("#data_type_copy_no > option").each(function() {
            if (this.text.toLowerCase().indexOf("(rae)") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_copy_no > option").each(function() {
            if (this.text.toLowerCase().indexOf("gistic") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
        var userSelectedCopyNoProfile = "";
        $.each(geneticProfiles.split(/\s+/), function(index, value){
            if (value.indexOf("cna") !== -1 || value.indexOf("log2") !== -1 ||
                value.indexOf("CNA")!== -1 || value.indexOf("gistic") !== -1) {
                userSelectedCopyNoProfile = value;
                return false;
            }
        });
        $("#data_type_copy_no > option").each(function() {
            if (this.value === userSelectedCopyNoProfile){
                $(this).prop('selected', true);
                return false;
            }
        });
    }

    function setDefaultMrnaSelection() {
        var userSelectedMrnaProfile = "";  //from main query
        //geneticProfiles --> global variable, passing user selected profile IDs
        $.each(geneticProfiles.split(/\s+/), function(index, value){
            if (value.indexOf("mrna") !== -1) {
                userSelectedMrnaProfile = value;
                return false;
            }
        });

        //----Priority List: User selection, RNA Seq V2, RNA Seq, Z-scores
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("z-scores") !== -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                this.text.toLowerCase().indexOf("z-scores") === -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                this.text.toLowerCase().indexOf("z-scores") === -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.value === userSelectedMrnaProfile){
                $(this).prop('selected', true);
                return false;
            }
        });
    }

    function updateVisibility() {
        //Dynamically show only the plots type related drop div
        var currentPlotsType = $('#plots_type').val();

        if (currentPlotsType.indexOf("copy_no") !== -1) {

            Util.toggleVisibility("data_type_mrna_dropdown", "show");
            Util.toggleVisibility("data_type_copy_no_dropdown", "show");
            Util.toggleVisibility("data_type_dna_methylation_dropdown", "hide");
            Util.toggleVisibility("data_type_rppa_dropdown", "hide");
            Util.toggleVisibility("data_type_mutation_dropdown", "hide");

        } else if (currentPlotsType.indexOf("dna_methylation") !== -1) {

            Util.toggleVisibility("data_type_mrna_dropdown", "show");
            Util.toggleVisibility("data_type_copy_no_dropdown", "hide");
            Util.toggleVisibility("data_type_dna_methylation_dropdown", "show");
            Util.toggleVisibility("data_type_rppa_dropdown", "hide");
            Util.toggleVisibility("data_type_mutation_dropdown", "hide");

        } else if (currentPlotsType.indexOf("rppa") !== -1) {

            Util.toggleVisibility("data_type_mrna_dropdown", "show");
            Util.toggleVisibility("data_type_copy_no_dropdown", "hide");
            Util.toggleVisibility("data_type_dna_methylation_dropdown", "hide");
            Util.toggleVisibility("data_type_rppa_dropdown", "show");
            Util.toggleVisibility("data_type_mutation_dropdown", "hide");
        }

    }

    function fetchFrameContent() {
        content.data_type.mutation.genetic_profile = Plots.getGeneticProfiles().genetic_profile_mutations;
        content.data_type.mrna.genetic_profile = Plots.getGeneticProfiles().genetic_profile_mrna;
        content.data_type.copy_no.genetic_profile = Plots.getGeneticProfiles().genetic_profile_copy_no;
        content.data_type.dna_methylation.genetic_profile = Plots.getGeneticProfiles().genetic_profile_dna_methylation;
        content.data_type.rppa.genetic_profile = Plots.getGeneticProfiles().genetic_profile_rppa;
        status.has_mrna = (content.data_type.mrna.genetic_profile.length !== 0);
        status.has_copy_no = (content.data_type.copy_no.genetic_profile.length !== 0);
        status.has_dna_methylation = (content.data_type.dna_methylation.genetic_profile.length !== 0);
        status.has_rppa = (content.data_type.rppa.genetic_profile.length !== 0);
    }

    return {
        init: function () {
            fetchFrameContent();
            drawMenu();
        },
        update: function() {
            setDefaultMrnaSelection();
            setDefaultCopyNoSelection();
            updateVisibility();
            PlotsView.init();
        }
    };
}()); //Closing PlotsMenu

var PlotsView = (function () {
    var elem = {
            svg : "",
            xScale : "",
            yScale : "",
            xAxis : "",
            yAxis : "",
            dotsGroup : ""
        },   //DOM elements
        pData = {
            gene : "",
            case_set : [],
            gisticType : [],
            mutation_type : [], //Mutation Types
            mutation_detail : [], //Mutation Detail (id)
            copy_no : [],
            mrna : [],
            rppa : [],
            dna_methylation : [],
        },   //Related Profile data (later translate into datum object)
        text = {
            xTitle : "",
            yTitle : "",
            mutations_alias : {
                frameshift : "frameshift",
                in_frame : "in_frame",
                missense : "missense",
                nonsense : "nonsense",
                splice : "splice",
                nonstop : "nonstop",
                nonstart : "nonstart",
                non : "non"
            },
            gistic_txt_val : {
                "-2": "Homdel",
                "-1": "Hetloss",
                "0": "Diploid",
                "1": "Gain",
                "2": "Amp"
            }
        },   //Text for the general view
        mutationStyle = {
            frameshift : {
                symbol : "triangle-down",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "frameshift"
            },
            nonsense : {
                symbol : "diamond",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "nonsense"
            },
            splice : {
                symbol : "triangle-up",
                fill : "#A4A4A4",
                stroke : "#B40404",
                legendText : "splice"
            },
            in_frame : {
                symbol : "square",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "in_frame"
            },
            nonstart : {
                symbol : "cross",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "nonstart"
            },
            nonstop : {
                symbol : "triangle-up",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "nonstop"
            },
            missense : {
                symbol : "circle",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "missense"
            },
            non : {
                symbol : "circle",
                fill : "#00AAF8",
                stroke : "#0089C6",
                legendText : "none"
            }
        },
        gisticStyle = {
            Amp : {
                stroke : "#FF0000",
                fill : "none",
                symbol : "circle",
                legendText : "Amp"
            },
            Gain : {
                stroke : "#FF69B4",
                fill : "none",
                symbol : "circle",
                legendText : "Gain"
            },
            Diploid : {
                stroke : "#000000",
                fill : "none",
                symbol : "circle",
                legendText : "Diploid"
            },
            Hetloss : {
                stroke : "#00BFFF",
                fill : "none",
                symbol : "circle",
                legendText : "Hetloss"
            },
            Homdel : {
                stroke : "#00008B",
                fill : "none",
                symbol : "circle",
                legendText : "Homdel"
            },
        },
        setting = {
            canvas_width: 700,
            canvas_height: 600
        },   //basic d3 canvas settings
        userSelection = {
            plots_type: "",
            mutation_type : "",
            copy_no_type : "",
            mrna_type : "",
            dna_methylation_type : "",
            rppa_type : "",
        },   //current user selection from the side menu
        tmpDataSet = [],   //Fileter/form multi-demention Dataset for D3 to use
        datum = {
            caseId : "",
            xVal : "",
            yVal : "",
            mutationDetail : "",  //Mutation ID
            mutationType : "",
            gisticType : "", //Discretized(GISTIC/RAE) Annotation
        };   //Template for single dot

    var Util = (function() {

        function isEmpty(inputVal) {
            if (inputVal !== "NaN" && inputVal !== "NA") {
                return false;
            }
            return true;
        }

        function dataIsAvailable() {
            var xHasData = false;
            var yHasData = false;
            var combineHasData = false;

            if (plotsTypeIsCopyNo()) {
                xData = pData.copy_no;
                yData = pData.mrna;
            } else if (plotsTypeIsMethylation()) {
                xData = pData.dna_methylation;
                yData = pData.mrna;
            } else if (plotsTypeIsRPPA()) {
                xData = pData.mrna;
                yData = pData.rppa;
            }

            for (var i = 0; i < pData.case_set.length; i++) {
                if (!isEmpty(xData[i])) {
                    xHasData = true;
                }
                if (!isEmpty(yData[i])) {
                    yHasData = true;
                }
                if (!isEmpty(xData[i]) && !isEmpty(yData[i])) {
                    combineHasData = true;
                }
            }

            if ((!yHasData) ||
                (!xHasData) ||
                (!combineHasData)) {
                var errorTxt2 = pData.gene + " in the selected cancer study.";
                if (yHasData == false) {
                    errorTxt1 = "There is no " +
                        text.yTitle.substring(pData.gene.length+1) +
                        " data for";
                } else if (xHasData == false) {
                    errorTxt1 = "There is no " +
                        text.xTitle.substring(pData.gene.length+1) +
                        " data for";
                } else if (combineHasData == false) {
                    errorTxt1 = "There is no data for the selected data type combination for";
                }
                //Get rid of "RPPA" in error message for rppa view
                errorTxt1 = errorTxt1.replace("RPPA", "");

                elem.svg.append("text")
                    .attr("x", 250)
                    .attr("y", 50)
                    .attr("text-anchor", "middle")
                    .attr("fill", "#DF3A01")
                    .text(errorTxt1)
                elem.svg.append("text")
                    .attr("x", 250)
                    .attr("y", 70)
                    .attr("text-anchor", "middle")
                    .attr("fill", "#DF3A01")
                    .text(errorTxt2)
                elem.svg.append("rect")
                    .attr("x", 50)
                    .attr("y", 30)
                    .attr("width", 400)
                    .attr("height", 50)
                    .attr("fill", "none")
                    .attr("stroke-width", 1)
                    .attr("stroke", "#BDBDBD");

                return false;
            }
            return true;
        };

        function copyData(desArray, srcArray) {
            desArray.length = 0;
            var desArrayIndex = 0;
            for (var tmpIndex = 0; tmpIndex < srcArray.length; tmpIndex ++ ){
                if (srcArray[tmpIndex] !== "" && srcArray[tmpIndex] !== null ) {
                    desArray[desArrayIndex] = srcArray[tmpIndex];
                    desArrayIndex += 1;
                }
            }
        }

        function dataIsDiscretized() {
            return userSelection.plots_type.indexOf("copy_no") !== -1 &&
                userSelection.copy_no_type.indexOf("log2") === -1 &&
                (userSelection.copy_no_type.indexOf("gistic") !== -1 ||
                    userSelection.copy_no_type.indexOf("cna") !== -1 ||
                    userSelection.copy_no_type.indexOf("CNA") !== -1);
        }

        function analyseData(xData, yData) {
            var tmp_xData = [];
            var tmp_xIndex = 0;
            var tmp_yData = [];
            var tmp_yIndex = 0;
            for (var j=0; j< pData.case_set.length; j++){
                if (!isEmpty(xData[j]) && !isEmpty(yData[j])) {
                    tmp_xData[tmp_xIndex] = xData[j];
                    tmp_xIndex += 1;
                    tmp_yData[tmp_yIndex] = yData[j];
                    tmp_yIndex += 1;
                }
            }
            var min_x = Math.min.apply(Math, tmp_xData);
            var max_x = Math.max.apply(Math, tmp_xData);
            var edge_x = (max_x - min_x) * 0.2;
            var min_y = Math.min.apply(Math, tmp_yData);
            var max_y = Math.max.apply(Math, tmp_yData);
            var edge_y = (max_y - min_y) * 0.1;
            return {
                min_x: min_x,
                max_x: max_x,
                edge_x: edge_x,
                min_y: min_y,
                max_y: max_y,
                edge_y: edge_y
            };
        }

        function searchIndexBottom(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
                if (parseFloat(ele) > parseFloat(arr[i])) {
                    continue ;
                } else if (parseFloat(ele) == parseFloat(arr[i])) {
                    return i;
                } else {
                    return i - 1;
                }
            }
            return arr.length - 1 ;
        };

        function searchIndexTop(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
                if (ele <= arr[i]) {
                    return i;
                } else {
                    continue;
                }
            }
            return arr.length - 1;
        };

        function plotsTypeIsCopyNo() {
            return userSelection.plots_type.indexOf("copy_no") !== -1;
        };

        function plotsTypeIsMethylation() {
            return userSelection.plots_type.indexOf("methylation") !== -1;
        };

        function plotsTypeIsRPPA() {
            return userSelection.plots_type.indexOf("rppa") !== -1;
        };

        return {
            dataIsAvailable: dataIsAvailable,
            copyData: copyData,
            dataIsDiscretized: dataIsDiscretized,
            analyseData: analyseData,
            searchIndexBottom: searchIndexBottom,
            searchIndexTop: searchIndexTop,
            plotsTypeIsCopyNo: plotsTypeIsCopyNo,
            plotsTypeIsMethylation: plotsTypeIsMethylation,
            plotsTypeIsRPPA: plotsTypeIsRPPA,
            isEmpty: isEmpty
        };

    }());

    var Data = (function() {

        function fetchPlotsData() {
            var result_set = new Array(5);
            for (var i = 0; i< result_set.length; i++) {
                result_set[i] = new Array();
            }
            //Calling web APIs
            var url_base = "";
            if (case_set_id === "-1") {
                url_base = "webservice.do?cmd=getProfileData&case_ids_key=" +
                    case_ids_key + "&gene_list=" + pData.gene + "&genetic_profile_id=";
            } else {
                url_base = "webservice.do?cmd=getProfileData&case_set_id=" +
                    case_set_id + "&gene_list=" + pData.gene + "&genetic_profile_id=";
            }
            var types = [
                userSelection.mutation_type,
                userSelection.copy_no_type,
                userSelection.mrna_type,
                userSelection.rppa_type,
                userSelection.dna_methylation_type
            ];
            for (var i = 0; i < types.length; i++) {
                if (types[i]) {
                    $.ajax({
                        url: url_base + types[i],
                        type: 'get',
                        dataType: 'text',
                        async: false,
                        success: function (data) {
                            if (data !== "") {
                                if (data.indexOf("No genetic profile available") === -1) {  //Profile Exists
                                    var tmp_sections = data.split(/\n/);
                                    //Get Case Set
                                    var tmp_case_set = String(tmp_sections[3]).trim().split(/\s+/);
                                    for (var j = 0; j < tmp_case_set.length - 2; j++) {
                                        pData.case_set[j] = tmp_case_set[j+2];
                                    }
                                    //Get profile data (Filter the headers)
                                    var profile_data = [];
                                    var tmp_profile_data = String(tmp_sections[4]).trim().split(/\s+/);
                                    for (var profileDataIndex = 0; profileDataIndex < tmp_profile_data.length; profileDataIndex++) {
                                        profile_data[profileDataIndex] = tmp_profile_data[profileDataIndex + 2];
                                    }
                                    //Fill in Result Data Set
                                    Util.copyData(result_set[i], profile_data);
                                }
                            } else {
                                alert("ERROR Fetching Data.");
                            }
                        }
                    });
                }
            }
            Util.copyData(pData.mutation_detail, result_set[0]);
            Util.copyData(pData.copy_no, result_set[1]);
            Util.copyData(pData.mrna, result_set[2]);
            Util.copyData(pData.rppa, result_set[3]);
            Util.copyData(pData.dna_methylation, result_set[4]);
        }

        function fetchMutationType() {
            var mutationMap = {};
            for (var i = 0; i < pData.case_set.length; i++) {
                mutationMap[pData.case_set[i]] = "non";
            }
            var url = "";
            if (case_set_id === "-1") {
                url = "webservice.do?cmd=getMutationData&case_ids_key=" +
                    case_ids_key + "&gene_list=" + pData.gene +
                    "&genetic_profile_id=" + cancer_study_id + "_mutations";
            } else {
                url = "webservice.do?cmd=getMutationData&case_set_id="  +
                    case_set_id + "&gene_list=" + pData.gene +
                    "&genetic_profile_id=" + cancer_study_id + "_mutations";
            }
            $.ajax({
                url: url,
                type: 'get',
                dataType: 'text',
                async: false,
                success: function (data) {
                    if (data != "") {
                        if (data.indexOf("No genetic profile available") === -1) {  //Profile Exists
                            tmp_lines = data.split(/\n/);
                            for (var j = 3; j < tmp_lines.length; j++) {   //Skip first two lines
                                var tmp_columns = tmp_lines[j].split(/\t/);
                                //Column2 is case id, column 5 is mutation detail
                                mutationMap[tmp_columns[2]] = tmp_columns[5];
                            }
                        }
                    } else {
                        //TODO: Hander Mutation Detail Fetching Error
                    }
                }
            });
            for (var k = 0; k < pData.case_set.length; k++) {
                var item = mutationMap[pData.case_set[k]];
                if (item === "Frame_Shift_Del" || item === "Frame_Shift_Ins") {
                    item = text.mutations_alias.frameshift;
                } else if ((item === "In_Frame_Del") || (item === "In_Frame_Ins")) {
                    item = text.mutations_alias.in_frame;
                } else if ((item === "Missense_Mutation") || (item === "Missense")) {
                    item = text.mutations_alias.missense;
                } else if ((item === "Nonsense_Mutation") || (item === "Nonsense")) {
                    item = text.mutations_alias.nonsense;
                } else if ((item === "Splice_Site") || (item === "Splice_Site_SNP")) {
                    item = text.mutations_alias.splice;
                } else if (item === "NonStop_Mutation") {
                    item = text.mutations_alias.nonstop;
                } else if (item === "Translation_Start_Site") {
                    item = text.mutations_alias.nonstart;
                } else {
                    item = text.mutations_alias.non;
                }
                pData.mutation_type[k] = item;
            }
        }

        function translateGisticType() {
            var items = []; // tmp container for gistic values.
            var url;
            if (case_set_id === "-1") {
                url = "webservice.do?cmd=getProfileData&case_ids_key=" +
                    case_ids_key + "&gene_list=" +
                    pData.gene + "&genetic_profile_id=" +
                    cancer_study_id + "_gistic";
            } else {
                url = "webservice.do?cmd=getProfileData&case_set_id=" +
                    case_set_id + "&gene_list=" +
                    pData.gene + "&genetic_profile_id=" +
                    cancer_study_id + "_gistic";

            }
            $.ajax({
                url: url,
                type: 'get',
                dataType: 'text',
                async: false,
                success: function (data) {
                    if (data != "") {
                        if (data.indexOf("No genetic profile available") === -1) {  //Profile Exists
                            tmp_lines = data.split(/\n/);
                            items = tmp_lines[4].split(/\t/);
                            items.splice(0,2);
                        }
                    } else {
                        //TODO: Hander Mutation Detail Fetching Error
                    }
                }
            });
            for (var i = 0; i < pData.case_set.length; i++) {
                pData.gisticType[i] = text.gistic_txt_val[items[i]];
            }
        }

        function formDataSet() {
            tmpDataSet.length = 0;
            for (var i = 0; i < pData.case_set.length; i++) {
                var tmpObj = jQuery.extend(true, {}, datum);
                tmpObj.caseId = pData.case_set[i];
                tmpObj.gisticType = pData.gisticType[i];
                tmpObj.mutationType = pData.mutation_type[i];
                tmpObj.mutationDetail = pData.mutation_detail[i];
                if (userSelection.plots_type.indexOf("copy_no") !== -1) { // mrna vs. copy_no
                    if (!(Util.isEmpty(pData.copy_no[i])) &&
                        !(Util.isEmpty(pData.mrna[i]))) {
                        tmpObj.xVal = pData.copy_no[i];
                        tmpObj.yVal = pData.mrna[i];
                        tmpDataSet.push(tmpObj);
                    }
                } else if (userSelection.plots_type.indexOf("dna_methylation") !== -1) {
                    if (!(Util.isEmpty(pData.dna_methylation[i])) &&
                        !(Util.isEmpty(pData.mrna[i]))) {
                        tmpObj.xVal = pData.dna_methylation[i];
                        tmpObj.yVal = pData.mrna[i];
                        tmpDataSet.push(tmpObj);
                    }
                } else if (userSelection.plots_type.indexOf("rppa") !== -1) {
                    if (!(Util.isEmpty(pData.rppa[i])) &&
                        !(Util.isEmpty(pData.mrna[i]))) {
                        tmpObj.xVal = pData.mrna[i];
                        tmpObj.yVal = pData.rppa[i];
                        tmpDataSet.push(tmpObj);
                    }
                } else {
                    //TODO: Error Handler
                }
            }
        }

        function reorderMutations() {
            var nonMutatedData = [];
            var mutatedData= [];
            var dataBuffer = [];
            tmpDataSet.forEach (function(entry) {
                if (entry.mutationType != "non") {
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
            tmpDataSet = dataBuffer;
        }

        return {
            fetchPlotsData: fetchPlotsData,
            fetchMutationType: fetchMutationType,
            translateGisticType: translateGisticType,
            formDataSet: formDataSet,
            reorderMutations: reorderMutations

        };

    }());

    var Text = (function() {

        function getDataTypes() {
            userSelection.plots_type = document.getElementById("plots_type").value;
            userSelection.mutation_type = document.getElementById("data_type_mutation").value;
            userSelection.copy_no_type = document.getElementById("data_type_copy_no").value;
            userSelection.mrna_type = document.getElementById("data_type_mrna").value;
            userSelection.rppa_type = document.getElementById("data_type_rppa").value;
            userSelection.dna_methylation_type = document.getElementById("data_type_dna_methylation").value;
        }

        function getAxisTitles() {
            if (userSelection.plots_type.indexOf("copy_no") !== -1) {
                var e = document.getElementById("data_type_copy_no");
                text.xTitle = pData.gene + ", " + e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_mrna");
                text.yTitle = pData.gene + ", " + e.options[e.selectedIndex].text;
            } else if (userSelection.plots_type.indexOf("dna_methylation") !== -1) {
                var e = document.getElementById("data_type_dna_methylation");
                text.xTitle = pData.gene + ", " + e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_mrna");
                text.yTitle = pData.gene + ", " + e.options[e.selectedIndex].text;
            } else if (userSelection.plots_type.indexOf("rppa") !== -1) {
                var e = document.getElementById("data_type_mrna");
                text.xTitle = pData.gene + ", " + e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_rppa");
                text.yTitle = pData.gene + ", " + e.options[e.selectedIndex].text;
            }
        }

        return {
            getDataTypes : getDataTypes,
            getAxisTitles : getAxisTitles
        };
    }());

    var View = (function() {

        function initView() {
            $('#view_title').empty();
            $('#plots_box').empty();
            elem.svg = d3.select("#plots_box")
                .append("svg")
                .attr("width", setting.canvas_width)
                .attr("height", setting.canvas_height);
        }

        function drawImgConverter() {
            $('#view_title').empty();
            if (Util.plotsTypeIsCopyNo()) {
                $('#view_title').append(pData.gene + ": mRNA Expression v. CNA ");
            } else if (Util.plotsTypeIsMethylation()) {
                $('#view_title').append(pData.gene + ": mRNA Expression v. DNA Methylation ");
            } else if (Util.plotsTypeIsRPPA()) {
                $('#view_title').append(pData.gene + ": RPPA protein level v. mRNA Expression ");
            }
            var pdfConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=loadSVG('plots_box');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='plots.pdf'>" +
                "<input type='submit' value='PDF'></form>";
            $('#view_title').append(pdfConverterForm);
            var svgConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=loadSVG('plots_box');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' name='filename' value='plots.svg'>" +
                "<input type='submit' value='SVG'></form>";
            $('#view_title').append(svgConverterForm);
        }

        function initContinuousAxis() {
            var analyseResult = {};
            if (userSelection.plots_type.indexOf("copy_no") !== -1) {
                analyseResult = Util.analyseData(pData.copy_no, pData.mrna);
            } else if (userSelection.plots_type.indexOf("dna_methylation") !== -1) {
                analyseResult = Util.analyseData(pData.dna_methylation, pData.mrna);
            } else if (userSelection.plots_type.indexOf("rppa") !== -1) {
                analyseResult = Util.analyseData(pData.mrna, pData.rppa);
            }
            var min_x = analyseResult.min_x;
            var max_x = analyseResult.max_x;
            var edge_x = analyseResult.edge_x;
            var min_y = analyseResult.min_y;
            var max_y = analyseResult.max_y;
            var edge_y = analyseResult.edge_y;

            if (userSelection.plots_type.indexOf("methylation") !== -1 &&
                userSelection.dna_methylation_type.indexOf("hm27") !== -1 ){
                //Range for DNA Methylation HM27 need to be fixed as from 0 to 1.
                elem.xScale = d3. scale.linear()
                    .domain([-0.02, 1])
                    .range([100,600]);
            } else {
                elem.xScale = d3.scale.linear()
                    .domain([min_x - edge_x, max_x + edge_x])
                    .range([100, 600]);
            }
            elem.yScale = d3.scale.linear()
                .domain([min_y - edge_y, max_y + edge_y])
                .range([520, 20]);
            elem.xAxis = d3.svg.axis()
                .scale(elem.xScale)
                .orient("bottom")
            elem.yAxis = d3.svg.axis()
                .scale(elem.yScale)
                .orient("left");
        }

        function initDiscretizedAxis() {
            var analyseResult = Util.analyseData(pData.copy_no, pData.mrna);
            var min_y = analyseResult.min_y;
            var max_y = analyseResult.max_y;
            var edge_y = analyseResult.edge_y;

            //reset max_x as the range of slots
            // -- Not real max x value for scaling!!
            var slotsCnt = 0;
            var tmp_copy_no = [];
            $.each(tmpDataSet, function(index, value) {
                tmp_copy_no.push(value.xVal);
            })
            for (var j = -2; j < 3; j++) {
                if (tmp_copy_no.indexOf(j.toString()) !== -1) {
                    slotsCnt += 1;
                }
            }

            //Set the domain range for different cases
            var new_min_x, new_max_x;
            if (slotsCnt === 1) {
                new_min_x = -0.5;
                new_max_x = 0.5;
            } else if (slotsCnt === 2) {
                new_min_x = -0.8;
                new_max_x = 1.8;
            } else if (slotsCnt === 3) {
                new_min_x = -0.8;
                new_max_x = 2.8;
            } else if (slotsCnt === 4) {
                new_min_x = -0.6;
                new_max_x = 3.6;
            } else if (slotsCnt === 5) {
                new_min_x = -0.6;
                new_max_x = 4.6;
            }

            //Define the axis
            elem.xScale = d3.scale.linear()
                .domain([new_min_x, new_max_x])
                .range([100, 600]);
            elem.yScale = d3.scale.linear()
                .domain([min_y - edge_y, max_y + edge_y])
                .range([520, 20]);

            elem.xAxis = d3.svg.axis()
                .scale(elem.xScale)
                .orient("bottom")
            elem.yAxis = d3.svg.axis()
                .scale(elem.yScale)
                .orient("left");
        }

        function drawDiscretizedAxis() {
            var textSet = [];
            var svg = elem.svg;

            var tmp_copy_no = [];
            $.each(tmpDataSet, function(index, value) {
                tmp_copy_no.push(value.xVal);
            })
            for (var j = -2; j < 3; j++) {
                if (tmp_copy_no.indexOf(j.toString()) !== -1) {
                    textSet.push(text.gistic_txt_val[j.toString()]);
                }
            }

            svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 520)")
                .call(elem.xAxis.ticks(textSet.length))
                .selectAll("text")
                .data(textSet)
                .style("font-family", "sans-serif")
                .style("font-size", "11px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black")
                .text(function(d){return d});
            svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 20)")
                .call(elem.xAxis.orient("bottom").ticks(0));
            svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(100, 0)")
                .call(elem.yAxis)
                .selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "11px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black");
            svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(600, 0)")
                .call(elem.yAxis.orient("left").ticks(0));
        }

        function drawContinuousAxis() {
            var svg = elem.svg;
            svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 520)")
                .call(elem.xAxis)
                .selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "11px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black");
            svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 20)")
                .call(elem.xAxis.orient("bottom").ticks(0));
            svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(100, 0)")
                .call(elem.yAxis)
                .selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "11px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black");
            svg.append("g")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(600, 0)")
                .call(elem.yAxis.orient("left").ticks(0));
        }

        function drawDiscretizedPlots() { //GISTIC, RAE view
            elem.dotsGroup = elem.svg.append("svg:g");
            var ramRatio = 20;  //Noise

            var subDataSet = {
                Homdel : [],
                Hetloss : [],
                Diploid : [],
                Gain : [],
                Amp : []
            };
            $.each(tmpDataSet, function(index, value) {
                if (value.gisticType === "Homdel") {
                    subDataSet.Homdel.push(value);
                } else if (value.gisticType === "Hetloss") {
                    subDataSet.Hetloss.push(value);
                } else if (value.gisticType === "Diploid") {
                    subDataSet.Diploid.push(value);
                } else if (value.gisticType === "Gain") {
                    subDataSet.Gain.push(value);
                } else if (value.gisticType === "Amp") {
                    subDataSet.Amp.push(value);
                }
            });
            //Remove empty data set
            $.each(subDataSet, function(key, value) {
                if (subDataSet[key].length === 0) {
                    delete subDataSet[key];
                }
            });

            var posVal = 0;
            $.each(subDataSet, function(key, value) {
                var subDotsGrp = elem.dotsGroup.append("svg:g");
                subDotsGrp.selectAll("path")
                    .data(value)
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d){
                        return "translate(" +
                            (elem.xScale(posVal) + (Math.random() * ramRatio - ramRatio/2)) +
                            ", " +
                            elem.yScale(d.yVal) + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(function(d){
                            switch (d.mutationType) {
                                case "non" : return 15;
                                default : return 25;
                            }
                        })
                        .type(function(d){
                            return mutationStyle[d.mutationType].symbol;
                        })
                    )
                    .attr("fill", function(d){
                        return mutationStyle[d.mutationType].fill;
                    })
                    .attr("stroke", function(d){
                        return mutationStyle[d.mutationType].stroke;
                    })
                    .attr("stroke-width", 1.2);
                posVal += 1;
            });
        }

        function drawLog2Plots() {
            elem.dotsGroup = elem.svg.append("svg:g");
            elem.dotsGroup.selectAll("path")
                .data(tmpDataSet)
                .enter()
                .append("svg:path")
                .attr("transform", function(d) {
                    return "translate(" + elem.xScale(d.xVal) + ", " + elem.yScale(d.yVal) + ")";
                })
                .attr("d", d3.svg.symbol()
                    .size(function(d){
                        switch (d.mutationTypes){
                            case "non" : return 15;
                            default : return 25;
                        }
                    })
                    .type(function(d){
                        return mutationStyle[d.mutationType].symbol;
                    })
                )
                .attr("fill", function(d){
                    return mutationStyle[d.mutationType].fill;
                })
                .attr("stroke", function(d){
                    return mutationStyle[d.mutationType].stroke;
                })
                .attr("stroke-width", 1.2);
        }

        function drawBoxPlots(){
            var xData = pData.copy_no;
            var yData = pData.mrna;

            var tmp_data_result = Util.analyseData(xData, yData);
            var min_x = tmp_data_result.min_x;
            var max_x = tmp_data_result.max_x;

            //position Indicator
            //Not using real x value for discretized data
            var pos = 0;

            for (var i = min_x ; i < max_x + 1; i++) {
                //Divide data set into sub group based on x(gistic) value
                var top;
                var bottom;
                var quan1;
                var quan2;
                var mean;
                var IQR;
                var scaled_y_arr=[];
                var tmp_y_arr = [];

                //Find the middle (vertical) line for one box plot
                var midLine = elem.xScale(pos);

                //Find the max/min y value with certain x value;
                var index_tmp_y_data_array = 0;
                for (var j = 0; j < yData.length; j++) {
                    if (!(Util.isEmpty(yData[j])) &&
                        !(Util.isEmpty(xData[j])) &&
                        (xData[j] === i.toString())) {
                        tmp_y_arr[index_tmp_y_data_array] = parseFloat(yData[j]);
                        index_tmp_y_data_array += 1;
                    }
                }
                tmp_y_arr.sort(function(a,b) { return a-b });
                if (tmp_y_arr.length === 0) {
                    //TODO: error handle (empty dataset)
                } else if (tmp_y_arr.length === 1) {
                    mean = elem.yScale(tmp_y_arr[0]);
                    elem.svg.append("line")
                        .attr("x1", midLine-30)
                        .attr("x2", midLine+30)
                        .attr("y1", mean)
                        .attr("y2", mean)
                        .attr("stroke-width", 1)
                        .attr("stroke", "grey");
                    pos += 1;
                } else {
                    if (tmp_y_arr.length === 2) {
                        mean = elem.yScale((tmp_y_arr[0] + tmp_y_arr[1]) / 2);
                        quan1 = bottom = elem.yScale(tmp_y_arr[0]);
                        quan2 = top = elem.yScale(tmp_y_arr[1]);
                        IQR = Math.abs(quan2 - quan1);
                        pos += 1;
                    } else {
                        var yl = tmp_y_arr.length;
                        if (yl % 2 === 0) {
                            mean = elem.yScale((tmp_y_arr[(yl / 2)-1] + tmp_y_arr[yl / 2]) / 2);
                            if (yl % 4 === 0) {
                                quan1 = elem.yScale((tmp_y_arr[(yl / 4)-1] + tmp_y_arr[yl / 4]) / 2);
                                quan2 = elem.yScale((tmp_y_arr[(3*yl / 4)-1] + tmp_y_arr[3 * yl / 4]) / 2);
                            } else {
                                quan1 = elem.yScale(tmp_y_arr[Math.floor(yl / 4)]);
                                quan2 = elem.yScale(tmp_y_arr[Math.floor(3 * yl / 4)]);
                            }
                        } else {
                            mean = elem.yScale(tmp_y_arr[Math.floor(yl / 2)]);
                            var tmp_yl = Math.floor(yl / 2) + 1;
                            if (tmp_yl % 2 === 0) {
                                quan1 = elem.yScale((tmp_y_arr[tmp_yl / 2 - 1] + tmp_y_arr[tmp_yl / 2]) / 2);
                                quan2 = elem.yScale((tmp_y_arr[(3 * tmp_yl / 2) - 2] + tmp_y_arr[(3 * tmp_yl / 2) - 1]) / 2);
                            } else {
                                quan1 = elem.yScale(tmp_y_arr[Math.floor(tmp_yl / 2)]);
                                quan2 = elem.yScale(tmp_y_arr[tmp_yl - 1 + Math.floor(tmp_yl / 2)]);
                            }
                        }
                        for (var k = 0 ; k < tmp_y_arr.length ; k++) {
                            scaled_y_arr[k] = parseFloat(elem.yScale(tmp_y_arr[k]));
                        }
                        scaled_y_arr.sort(function(a,b) { return a-b });
                        IQR = Math.abs(quan2 - quan1);
                        var index_top = Util.searchIndexTop(scaled_y_arr, (quan2 - 1.5 * IQR));
                        top = scaled_y_arr[index_top];
                        var index_bottom = Util.searchIndexBottom(scaled_y_arr, (quan1 + 1.5 * IQR));
                        bottom = scaled_y_arr[index_bottom];

                        pos += 1;
                    }

                    //D3 Drawing
                    var boxPlotsGroup = elem.svg.append("svg:g");
                    boxPlotsGroup.append("rect")
                        .attr("x", midLine-40)
                        .attr("y", quan2)
                        .attr("width", 80)
                        .attr("height", IQR)
                        .attr("fill", "none")
                        .attr("stroke-width", 1)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsGroup.append("line")
                        .attr("x1", midLine-40)
                        .attr("x2", midLine+40)
                        .attr("y1", mean)
                        .attr("y2", mean)
                        .attr("stroke-width", 1)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsGroup.append("line")
                        .attr("x1", midLine-30)
                        .attr("x2", midLine+30)
                        .attr("y1", top)
                        .attr("y2", top)
                        .attr("stroke-width", 1)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsGroup.append("line")
                        .attr("x1", midLine-30)
                        .attr("x2", midLine+30)
                        .attr("y1", bottom)
                        .attr("y2", bottom)
                        .attr("stroke", "#BDBDBD")
                        .style("stroke-width", 1);
                    boxPlotsGroup.append("line")
                        .attr("x1", midLine)
                        .attr("x2", midLine)
                        .attr("y1", quan1)
                        .attr("y2", bottom)
                        .attr("stroke", "#BDBDBD")
                        .attr("stroke-width", 1);
                    boxPlotsGroup.append("line")
                        .attr("x1", midLine)
                        .attr("x2", midLine)
                        .attr("y1", quan2)
                        .attr("y2", top)
                        .attr("stroke", "#BDBDBD")
                        .style("stroke-width", 1);
                }
            }
        }

        function drawContinuousPlots() {  //RPPA, DNA Methylation Views
            elem.dotsGroup = elem.svg.append("svg:g");
            elem.dotsGroup.selectAll("path")
                .data(tmpDataSet)
                .enter()
                .append("svg:path")
                .attr("transform", function(d){
                    return "translate(" + elem.xScale(d.xVal) + ", " + elem.yScale(d.yVal) + ")";
                })
                .attr("d", d3.svg.symbol()
                    .size(35)
                    .type("circle"))
                .attr("fill", function(d) {
                    switch (d.mutationType) {
                        case "non" : return "white";
                        default: return "orange";
                    }
                })
                .attr("fill-opacity", function(d) {
                    switch (d.mutationType) {
                        case "non" : return 0.0;
                        default : return 1.0;
                    }
                })
                .attr("stroke", function(d) {
                    return gisticStyle[d.gisticType].stroke;
                })
                .attr("stroke-width", function(d) {
                    switch (d.mutationType) {
                        case "non" : return "1";
                        default : return "1.1";
                    }
                });
        }

        function drawAxisTitle() {
            var axisTitleGroup = elem.svg.append("svg:g");
            axisTitleGroup.append("text")
                .attr("class", "label")
                .attr("x", 350)
                .attr("y", 580)
                .style("text-anchor", "middle")
                .style("font-weight","bold")
                .text(text.xTitle);
            axisTitleGroup.append("text")
                .attr("class", "label")
                .attr("transform", "rotate(-90)")
                .attr("x", -270)
                .attr("y", 45)
                .style("text-anchor", "middle")
                .style("font-weight","bold")
                .text(text.yTitle);
        }

        function drawCopyNoViewLegends() {
            //Only show glyphs whose mutation type
            //appeared in the current individual case
            var _appearedMutationTypes = [];
            $.each(tmpDataSet, function(index, value) {
                _appearedMutationTypes.push(value.mutationType);
            });

            //Convert object to array
            var mutationStyleArr = [];
            for (var key in mutationStyle) {
                var obj = mutationStyle[key];
                if (_appearedMutationTypes.indexOf(key) !== -1) {
                    mutationStyleArr.push(obj);
                }
            }

            var legend = elem.svg.selectAll(".legend")
                .data(mutationStyleArr)
                .enter().append("svg:g")
                .attr("transform", function(d, i) {
                    return "translate(610, " + (30 + i * 15) + ")";
                });

            legend.append("path")
                .data(mutationStyleArr)
                .attr("width", 18)
                .attr("height", 16)
                .attr("d", d3.svg.symbol().size(30)
                    .type(function(d) {return d.symbol;}))
                .attr("fill", function(d) {return d.fill;})
                .attr("stroke", function(d){return d.stroke;})
                .attr("stroke-width", 1.2);

            legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d){return d.legendText;});
        }

        function drawOtherViewLegends() {
            var gisticStyleArr = [];
            for (var key in gisticStyle) {
                var obj = gisticStyle[key];
                gisticStyleArr.push(obj);
            }

            var mutatedStyle = {
                stroke : "none",
                symbol : "circle",
                fill : "orange",
                legendText : "Mutated",
            }
            gisticStyleArr.push(mutatedStyle);

            var legend = elem.svg.selectAll(".legend")
                .data(gisticStyleArr)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) {
                    return "translate(610, " + (30 + i * 15) + ")";
                })

            legend.append("path")
                .attr("width", 18)
                .attr("height", 18)
                .attr("d", d3.svg.symbol()
                    .size(40)
                    .type(function(d) { return d.symbol; }))
                .attr("fill", function (d) { return d.fill; })
                .attr("stroke", function (d) { return d.stroke; })
                .attr("stroke-width", 1.1);

            legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d) { return d.legendText; })
        }

        var Qtips = (function() {

            function confContent(viewType, d) {
                var content = "<font size='2'>";
                if (viewType === "CopyNo") {
                    if (Util.dataIsDiscretized()) {
                        content += "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    } else {
                        content += "CNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                            "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='tumormap.do?case_id=" + d.caseId +
                        "&cancer_study_id=" + cancer_study_id + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail + "<br>";
                    }
                } else if (viewType === "Methylation") {
                    content += "Methylation: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                        "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid") {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='tumormap.do?case_id=" + d.caseId +
                        "&cancer_study_id=" + cancer_study_id + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail + "<br>";
                    }
                } else if (viewType === "RPPA") {
                    content += "mRNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                        "RPPA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid") {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='tumormap.do?case_id=" + d.caseId +
                        "&cancer_study_id=" + cancer_study_id + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail + "<br>";
                    }
                }
                content = content + "</font>";
                return content;
            }

            return {
                init: function(viewType){
                    elem.dotsGroup.selectAll("path").each(
                        function(d) {
                            var content = confContent(viewType, d);
                            $(this).qtip(
                                {
                                    content: {text: content},
                                    style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                                    hide: { fixed:true, delay: 100},
                                    position: {my:'left bottom',at:'top right'}
                                }
                            );
                            if (viewType === "CopyNo") {    //Handle special symbols
                                var mouseOn = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(200)
                                            .type(function(d){
                                                return mutationStyle[d.mutationType].symbol;
                                            })
                                        )
                                        .attr("fill", function(d){
                                            return mutationStyle[d.mutationType].fill;
                                        })
                                        .attr("stroke", function(d){
                                            return mutationStyle[d.mutationType].stroke;
                                        })
                                        .attr("stroke-width", 1.2);
                                };
                                var mouseOff = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol()
                                            .size(function(d){
                                                switch (d.mutationType) {
                                                    case "non" : return 15;
                                                    default : return 25;
                                                }
                                            })
                                            .type(function(d){
                                                return mutationStyle[d.mutationType].symbol;
                                            })
                                        )
                                        .attr("fill", function(d){
                                            return mutationStyle[d.mutationType].fill;
                                        })
                                        .attr("stroke", function(d){
                                            return mutationStyle[d.mutationType].stroke;
                                        })
                                        .attr("stroke-width", 1.2);
                                };
                                elem.dotsGroup.selectAll("path").on("mouseover", mouseOn);
                                elem.dotsGroup.selectAll("path").on("mouseout", mouseOff);
                            } else {
                                var mouseOn = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(200));
                                };
                                var mouseOff = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(35));
                                };
                                elem.dotsGroup.selectAll("path").on("mouseover", mouseOn);
                                elem.dotsGroup.selectAll("path").on("mouseout", mouseOff);
                            }
                        }
                    );
                }
            };

        }());

        return {
            initView: initView,
            drawImgConverter: drawImgConverter,
            initContinuousAxis: initContinuousAxis,
            initDiscretizedAxis: initDiscretizedAxis,
            drawDiscretizedAxis: drawDiscretizedAxis,
            drawContinuousAxis: drawContinuousAxis,
            drawDiscretizedPlots: drawDiscretizedPlots,
            drawLog2Plots: drawLog2Plots,
            drawBoxPlots: drawBoxPlots,
            drawContinuousPlots: drawContinuousPlots,
            drawAxisTitle: drawAxisTitle,
            drawCopyNoViewLegends: drawCopyNoViewLegends,
            drawOtherViewLegends: drawOtherViewLegends,
            Qtips: Qtips
        };

    }());

    return {
        init: function(){
            $('#loading-image').show();
            $('#view_title').hide();
            $('#plots_box').hide();

            //Data Processing
            pData.gene = document.getElementById("genes").value;
            Text.getDataTypes();
            Text.getAxisTitles();
            Data.fetchPlotsData();
            Data.translateGisticType();
            Data.fetchMutationType();
            //TODO: error handle
            Data.formDataSet();    //Build the tmp data set for d3 to use
            Data.reorderMutations();

            //View Construction
            View.initView();
            if (Util.dataIsAvailable()) {
                View.drawImgConverter();  //PDF, SVG download buttons
                if (Util.plotsTypeIsCopyNo()) {
                    if (Util.dataIsDiscretized()) {
                        View.initDiscretizedAxis();
                        View.drawDiscretizedAxis();
                        View.drawBoxPlots();
                        View.drawDiscretizedPlots();
                    } else {
                        View.initContinuousAxis();
                        View.drawContinuousAxis();
                        View.drawLog2Plots();
                    }
                    View.drawCopyNoViewLegends();
                    View.Qtips.init("CopyNo");
                } else if (Util.plotsTypeIsMethylation()) { //RPPA and GISTIC view
                    View.initContinuousAxis();
                    View.drawContinuousAxis();
                    View.drawContinuousPlots();
                    View.drawOtherViewLegends();
                    View.Qtips.init("Methylation");
                } else if (Util.plotsTypeIsRPPA()) {
                    View.initContinuousAxis();
                    View.drawContinuousAxis();
                    View.drawContinuousPlots();
                    View.drawOtherViewLegends();
                    View.Qtips.init("RPPA");
                }
                View.drawAxisTitle();
                //Img Center: PDF and SVG button
            }

            setTimeout(
                function() {
                    $('#loading-image').hide();
                    $('#view_title').show();
                    $('#plots_box').show();
                },
                500
            );

        }
    };
}()); //Closing PlotsView