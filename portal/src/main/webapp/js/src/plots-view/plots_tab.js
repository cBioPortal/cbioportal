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

        function toggleVisibilityX(elemId) {
            var e = document.getElementById(elemId);
            e.style.display = 'block';
            $("#" + elemId).append("<div id='one_gene_log_scale_x_div'></div>");
        }

        function toggleVisibilityY(elemId) {
            var e = document.getElementById(elemId);
            e.style.display = 'block';
            $("#" + elemId).append("<div id='one_gene_log_scale_y_div'></div>");
        }

        function toggleVisibilityHide(elemId) {
            var e = document.getElementById(elemId);
            e.style.display = 'none';
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
            toggleVisibilityX: toggleVisibilityX,
            toggleVisibilityY: toggleVisibilityY,
            toggleVisibilityHide: toggleVisibilityHide,
            generateList: generateList
        };

    }());

    function drawMenu() {

        $("#one_gene_type_specification").show();
        $("#plots_type").empty();
        $("#one_gene_platform_select_div").empty();
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
                    "<select id='" + singleDataTypeObj.value + "' onchange='PlotsView.init();PlotsMenu.updateLogScaleOption();' class='plots-select'></select></div>"
            );
            for (var index in singleDataTypeObj.genetic_profile) { //genetic_profile is ARRAY!
                if (index.length === 1) { //TODO: this is temp solution
                    var item_profile = singleDataTypeObj.genetic_profile[index];
                    $("#" + singleDataTypeObj.value).append(
                        "<option value='" + item_profile[0] + "|" + item_profile[2] + "'>" + item_profile[1] + "</option>");
                }
            }
        }
    }

    function drawErrMsgs() {
        $("#one_gene_type_specification").hide();
        $("#menu_err_msg").append("<h5>Profile data missing for generating this view.</h5>");
    }

    function setDefaultCopyNoSelection() {
        //-----Priority: discretized(gistic, rae), continuous
        //TODO: refactor
        $('#data_type_copy_no > option').each(function() {
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

    function setDefaultMethylationSelection() {
        $('#data_type_dna_methylation > option').each(function() {
            if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
    }

    function updateVisibility() {
        $("#one_gene_log_scale_x_div").remove();
        $("#one_gene_log_scale_y_div").remove();
        var currentPlotsType = $('#plots_type').val();
        if (currentPlotsType.indexOf("copy_no") !== -1) {
            Util.toggleVisibilityX("data_type_copy_no_dropdown");
            Util.toggleVisibilityY("data_type_mrna_dropdown");
            Util.toggleVisibilityHide("data_type_dna_methylation_dropdown");
            Util.toggleVisibilityHide("data_type_rppa_dropdown");
        } else if (currentPlotsType.indexOf("dna_methylation") !== -1) {
            Util.toggleVisibilityX("data_type_dna_methylation_dropdown");
            Util.toggleVisibilityY("data_type_mrna_dropdown");
            Util.toggleVisibilityHide("data_type_copy_no_dropdown");
            Util.toggleVisibilityHide("data_type_rppa_dropdown");
        } else if (currentPlotsType.indexOf("rppa") !== -1) {
            Util.toggleVisibilityX("data_type_mrna_dropdown");
            Util.toggleVisibilityY("data_type_rppa_dropdown");
            Util.toggleVisibilityHide("data_type_copy_no_dropdown");
            Util.toggleVisibilityHide("data_type_dna_methylation_dropdown");
        }
        updateLogScaleOption();
    }

    function updateLogScaleOption() {
        $("#one_gene_log_scale_x_div").empty();
        $("#one_gene_log_scale_y_div").empty();
        var _str_x = "<input type='checkbox' id='log_scale_option_x' checked onchange='PlotsView.applyLogScaleX();'/> log scale";
        var _str_y = "<input type='checkbox' id='log_scale_option_y' checked onchange='PlotsView.applyLogScaleY();'/> log scale";
        if ($("#plots_type").val() === content.plots_type.mrna_copyNo.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_y_div").append(_str_y);
            }
        } else if ($("#plots_type").val() === content.plots_type.mrna_methylation.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_y_div").append(_str_y);
            }
        } else if ($("#plots_type").val() === content.plots_type.rppa_mrna.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_x_div").append(_str_x);
            }
        }
    }

    function fetchFrameContent(selectedGene) {
        content.data_type.mrna.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_mrna;
        content.data_type.copy_no.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_copy_no;
        content.data_type.dna_methylation.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_dna_methylation;
        content.data_type.rppa.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_rppa;
        status.has_mrna = (content.data_type.mrna.genetic_profile.length !== 0);
        status.has_copy_no = (content.data_type.copy_no.genetic_profile.length !== 0);
        status.has_dna_methylation = (content.data_type.dna_methylation.genetic_profile.length !== 0);
        status.has_rppa = (content.data_type.rppa.genetic_profile.length !== 0);
    }

    return {
        init: function () {
            $("#menu_err_msg").empty();
            fetchFrameContent(gene_list[0]);
            Util.generateList("gene", gene_list);
            if(status.has_mrna && (status.has_copy_no || status.has_dna_methylation || status.has_rppa)) {
                drawMenu();
                setDefaultMrnaSelection();
                setDefaultCopyNoSelection();
                setDefaultMethylationSelection();
                updateVisibility();
            } else {
                drawErrMsgs();
            }
        },
        updateMenu: function() {
            $("#menu_err_msg").empty();
            fetchFrameContent(document.getElementById("gene").value);
            if(status.has_mrna && (status.has_copy_no || status.has_dna_methylation || status.has_rppa)) {
                drawMenu();
                setDefaultMrnaSelection();
                setDefaultCopyNoSelection();
                setDefaultMethylationSelection();
                updateVisibility();
            } else {
                drawErrMsgs();
            }
        },
        updateDataType: function() {
            setDefaultMrnaSelection();
            setDefaultCopyNoSelection();
            setDefaultMethylationSelection();
            updateVisibility();
        },
        updateLogScaleOption: updateLogScaleOption,
        getStatus: function() {
            return status;
        }
    };
}()); //Closing PlotsMenu

var PlotsView = (function () {

    var text = {
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
        mutationStyle = {  //Key and "typeName" are always identical
            frameshift : {
                typeName : "frameshift",
                symbol : "triangle-down",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Frameshift"
            },
            nonsense : {
                typeName: "nonsense",
                symbol : "diamond",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Nonsense"
            },
            splice : {
                typeName : "splice",
                symbol : "triangle-up",
                fill : "#A4A4A4",
                stroke : "#B40404",
                legendText : "Splice"
            },
            in_frame : {
                typeName : "in_frame",
                symbol : "square",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "In_frame"
            },
            nonstart : {
                typeName : "nonstart",
                symbol : "cross",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "Nonstart"
            },
            nonstop : {
                typeName : "nonstop",
                symbol : "triangle-up",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Nonstop"
            },
            missense : {
                typeName : "missense",
                symbol : "circle",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "Missense"
            },
            other: {
                typeName: "other",
                symbol: "square",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Other"
            },
            non : {
                typeName : "non",
                symbol : "circle",
                fill : "#00AAF8",
                stroke : "#0089C6",
                legendText : "No mutation"
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
            Unknown : {
                stroke : "#A8A8A8",
                fill : "none",
                symbol : "circle",
                legendText : "No CNA data"
            }
        },
        userSelection = {
            gene: "",
            plots_type: "",
            copy_no_type : "",
            mrna_type : "",
            dna_methylation_type : "",
            rppa_type : ""
        };   //current user selection from the side menu

    var discretizedDataTypeIndicator = "";

    var Util = (function() {
        
        function hasCopyNumberData() {
            var result = false;
            $.each(PlotsData.getDotsGroup(), function(index, obj) {
                if (!isEmpty(obj.gisticType)) {
                    result = true;
                    return false;
                }
            });
            return result;
        }

        function isEmpty(inputVal) {
            if (inputVal !== "NaN" && inputVal !== "NA" && (typeof inputVal !== "undefined")) {
                return false;
            }
            return true;
        }

        function plotsTypeIsCopyNo() {
            return userSelection.plots_type === "mrna_vs_copy_no";
        };

        function plotsTypeIsMethylation() {
            return userSelection.plots_type === "mrna_vs_dna_methylation";
        };

        function plotsTypeIsRPPA() {
            return userSelection.plots_type === "rppa_protein_level_vs_mrna";
        };

        function plotsIsDiscretized() {
            return userSelection.plots_type.indexOf("copy_no") !== -1 &&
                userSelection.copy_no_type.indexOf("log2") === -1 &&
                (userSelection.copy_no_type.indexOf("gistic") !== -1 ||
                    userSelection.copy_no_type.indexOf("cna") !== -1 ||
                    userSelection.copy_no_type.indexOf("CNA") !== -1);
        }

        function analyseData(inputArr) {
            var tmp_xData = [];
            var tmp_xIndex = 0;
            var tmp_yData = [];
            var tmp_yIndex = 0;
            for (var j = 0; j< inputArr.length; j++){
                if (!isEmpty(inputArr[j].xVal) && !isEmpty(inputArr[j].yVal)) {
                    tmp_xData[tmp_xIndex] = inputArr[j].xVal;
                    tmp_xIndex += 1;
                    tmp_yData[tmp_yIndex] = inputArr[j].yVal;
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

        function searchIndexBottom(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
                if (parseFloat(ele) > parseFloat(arr[i])) {
                    continue ;
                } else if (parseFloat(ele) === parseFloat(arr[i])) {
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

        return {
            plotsTypeIsCopyNo: plotsTypeIsCopyNo,
            plotsTypeIsMethylation: plotsTypeIsMethylation,
            plotsTypeIsRPPA: plotsTypeIsRPPA,
            isEmpty: isEmpty,
            copyData: copyData,
            plotsIsDiscretized: plotsIsDiscretized,
            analyseData: analyseData,
            searchIndexBottom: searchIndexBottom,
            searchIndexTop: searchIndexTop,
            hasCopyNumberData: hasCopyNumberData
        };

    }());

    var PlotsData = (function() {

        var caseSetLength = 0,
            dotsGroup = [],
            singleDot = {
                caseId : "",
                xVal : "",
                yVal : "",
                mutationDetail : "",  //Mutation ID
                mutationType : "",
                gisticType : "" //Discretized(GISTIC/RAE) Annotation
            },   //Template for single dot
            status = {
                xHasData: false,
                yHasData: false,
                combineHasData: false
            },
            attr = {
                min_x: 0,
                max_x: 0,
                min_y: 0,
                max_y: 0,
                pearson: 0,
                spearman: 0
            };

        function fetchPlotsData(profileDataResult) {

            var resultObj = profileDataResult[userSelection.gene];
            for (var key in resultObj) {  //key is case id
                caseSetLength += 1;
                var _obj = resultObj[key];
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.caseId = key;
                //TODO: remove hard-coded menu content
                if (Util.plotsTypeIsCopyNo()) {
                    _singleDot.xVal = _obj[userSelection.copy_no_type];
                    _singleDot.yVal = _obj[userSelection.mrna_type];
                } else if (Util.plotsTypeIsMethylation()) {
                    _singleDot.xVal = _obj[userSelection.dna_methylation_type];
                    _singleDot.yVal = _obj[userSelection.mrna_type];
                } else if (Util.plotsTypeIsRPPA()) {
                    _singleDot.xVal = _obj[userSelection.mrna_type];
                    _singleDot.yVal = _obj[userSelection.rppa_type];
                }
                if (_obj.hasOwnProperty(cancer_study_id + "_mutations")) {
                    _singleDot.mutationDetail = _obj[cancer_study_id + "_mutations"];
                    _singleDot.mutationType = _obj[cancer_study_id + "_mutations"]; //Translate into type later
                } else {
                    _singleDot.mutationType = "non";
                }
                if (!Util.isEmpty(_obj[discretizedDataTypeIndicator])) {
                    _singleDot.gisticType = text.gistic_txt_val[_obj[discretizedDataTypeIndicator]];
                } else {
                    _singleDot.gisticType = "NaN";
                }
                //Set Data Status
                if (!Util.isEmpty(_singleDot.xVal)) {
                    status.xHasData = true;
                }
                if (!Util.isEmpty(_singleDot.yVal)) {
                    status.yHasData = true;
                }
                //Push into the dots array
                if (!Util.isEmpty(_singleDot.xVal) &&
                    !Util.isEmpty(_singleDot.yVal)) {
                    dotsGroup.push(_singleDot);
                    status.combineHasData = true;
                }
            }
        }

        function translateMutationType(mutationTypeResult) {
            //Map mutation type for each individual cases
            var mutationDetailsUtil =
                new MutationDetailsUtil(new MutationCollection(mutationTypeResult));
            var mutationMap = mutationDetailsUtil.getMutationCaseMap();
            $.each(dotsGroup, function(index, dot) {
                if (!mutationMap.hasOwnProperty(dot.caseId.toLowerCase())) {
                    dot.mutationType = mutationStyle.non.typeName;
                } else {
                    var _mutationTypes = []; //one case can have multi-mutations
                    $.each(mutationMap[dot.caseId.toLowerCase()], function (index, val) {
                        if ((val.mutationType === "Frame_Shift_Del")||(val.mutationType === "Frame_Shift_Ins")) {
                            _mutationTypes.push(mutationStyle.frameshift.typeName);
                        } else if ((val.mutationType === "In_Frame_Del")||(val.mutationType === "In_Frame_Ins")) {
                            _mutationTypes.push(mutationStyle.in_frame.typeName);
                        } else if ((val.mutationType === "Missense_Mutation")||(val.mutationType === "Missense")) {
                            _mutationTypes.push(mutationStyle.missense.typeName);
                        } else if ((val.mutationType === "Nonsense_Mutation")||(val.mutationType === "Nonsense")) {
                            _mutationTypes.push(mutationStyle.nonsense.typeName);
                        } else if ((val.mutationType === "Splice_Site")||(val.mutationType === "Splice_Site_SNP")) {
                            _mutationTypes.push(mutationStyle.splice.typeName);
                        } else if (val.mutationType === "NonStop_Mutation") {
                            _mutationTypes.push(mutationStyle.nonstop.typeName);
                        } else if (val.mutationType === "Translation_Start_Site") {
                            _mutationTypes.push(mutationStyle.nonstart.typeName);
                        } else { //Fusion etc. new mutation types
                            _mutationTypes.push(mutationStyle.other.typeName);
                        }
                    });
                    //Re-order mutations in one case based on priority list
                    var mutationPriorityList = [];
                    mutationPriorityList[mutationStyle.frameshift.typeName] = "0";
                    mutationPriorityList[mutationStyle.in_frame.typeName] = "1";
                    mutationPriorityList[mutationStyle.missense.typeName] = "2";
                    mutationPriorityList[mutationStyle.nonsense.typeName] = "3";
                    mutationPriorityList[mutationStyle.splice.typeName] = "4";
                    mutationPriorityList[mutationStyle.nonstop.typeName] = "5";
                    mutationPriorityList[mutationStyle.nonstart.typeName] = "6";
                    mutationPriorityList[mutationStyle.other.typeName] = "7";
                    mutationPriorityList[mutationStyle.non.typeName] = "8";
                    var _primaryMutation = _mutationTypes[0];
                    $.each(_mutationTypes, function(index, val) {
                        if (mutationPriorityList[_primaryMutation] > mutationPriorityList[val]) {
                            _primaryMutation = val;
                        }
                    });
                    dot.mutationType = _primaryMutation;
                }
            });
        }

        function prioritizeMutatedCases() {
            var nonMutatedData = [];
            var mutatedData= [];
            var dataBuffer = [];
            dotsGroup.forEach (function(entry) {
                if (!Util.isEmpty(entry.mutationDetail)) {
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
            dotsGroup = dataBuffer;
        }

        function analyseData() {
            var tmp_xData = [];
            var tmp_xIndex = 0;
            var tmp_yData = [];
            var tmp_yIndex = 0;
            for (var j = 0; j < dotsGroup.length; j++){
                if (!Util.isEmpty(dotsGroup[j].xVal) &&
                    !Util.isEmpty(dotsGroup[j].yVal)) {
                    tmp_xData[tmp_xIndex] = dotsGroup[j].xVal;
                    tmp_xIndex += 1;
                    tmp_yData[tmp_yIndex] = dotsGroup[j].yVal;
                    tmp_yIndex += 1;
                }
            }
            attr.min_x = Math.min.apply(Math, tmp_xData);
            attr.max_x = Math.max.apply(Math, tmp_xData);
            attr.min_y = Math.min.apply(Math, tmp_yData);
            attr.max_y = Math.max.apply(Math, tmp_yData);

            //Calculate the co-express/correlation scores
            //(When data is discretized)
            if (!Util.plotsIsDiscretized()) {
                var tmpGeneXcoExpStr = "",
                    tmpGeneYcoExpStr = "";
                $.each(PlotsData.getDotsGroup(), function(index, obj) {
                    tmpGeneXcoExpStr += obj.xVal + " ";
                    tmpGeneYcoExpStr += obj.yVal + " ";
                });
                var paramsCalcCoexp = {
                    gene_x : tmpGeneXcoExpStr,
                    gene_y : tmpGeneYcoExpStr
                };
                $.post("calcCoExp.do", paramsCalcCoexp, getCalcCoExpCallBack, "json");
            } else {
                $('#view_title').show();
                $('#plots_box').show();
                $('#loading-image').hide();
                View.init();                
            }
        }

        function getCalcCoExpCallBack(result) {
            //Parse the coexp scoring result
            var tmpArrCoexpScores = result.split(" ");
            attr.pearson = parseFloat(tmpArrCoexpScores[0]).toFixed(3);
            attr.spearman = parseFloat(tmpArrCoexpScores[1]).toFixed(3);
            $('#view_title').show();
            $('#plots_box').show();
            $('#loading-image').hide();
            View.init();
        }

        return {
            init: function(profileDataResult, mutationTypeResult) {
                status.xHasData = false;
                status.yHasData = false;
                status.combineHasData = false;
                caseSetLength = 0;
                dotsGroup.length = 0;
                fetchPlotsData(profileDataResult);
                if (mutationTypeResult !== "") {
                    translateMutationType(mutationTypeResult);
                    prioritizeMutatedCases();
                }
                analyseData();
            },
            getDotsGroup: function() { return dotsGroup; },
            getDataStatus: function() { return status; },
            getDataAttr: function() { return attr; }
        };

    }());

    var View = (function() {

        var elem = {
                svg : "",
                elemDotsGroup : "",
                boxPlots: ""
            },   //DOM elements
            settings = {
                canvas_width: 750,
                canvas_height: 600
            },   //basic d3 canvas settings
            attr = {
                xScale : "",
                yScale : ""
            };

        var Axis = (function() {

            var xAxis = "",
                yAxis = "",
                xTitle = "",
                yTitle = "",
                xTitleHelp = "",
                yTitleHelp = "";

            function getAxisTitles() {
                //TODO: Change hard-coded menu items value
                if (Util.plotsTypeIsCopyNo()) {
                    var e = document.getElementById("data_type_copy_no");
                    xTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_mrna");
                    yTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                } else if (Util.plotsTypeIsMethylation()) {
                    var e = document.getElementById("data_type_dna_methylation");
                    xTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_mrna");
                    yTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                } else if (Util.plotsTypeIsRPPA()) {
                    var e = document.getElementById("data_type_mrna");
                    xTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_rppa");
                    yTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                }
            }

            function initDiscretizedAxis() {
                var _dataAttr = PlotsData.getDataAttr();
                var min_y = _dataAttr.min_y;
                var max_y = _dataAttr.max_y;
                //reset max_x as the range of slots
                // -- Not real max x value for scaling!!
                var slotsCnt = 0;
                var tmp_copy_no = [];
                $.each(PlotsData.getDotsGroup(), function(index, value) {
                    tmp_copy_no.push(value.xVal);
                });
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
                var edge_y = (max_y - min_y) * 0.1;
                //Define the axis
                attr.xScale = d3.scale.linear()
                    .domain([new_min_x, new_max_x])
                    .range([100, 600]);
                attr.yScale = d3.scale.linear()
                    .domain([min_y - edge_y, max_y + edge_y])
                    .range([520, 20]);
                xAxis = d3.svg.axis()
                    .scale(attr.xScale)
                    .orient("bottom")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
                yAxis = d3.svg.axis()
                    .scale(attr.yScale)
                    .orient("left")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function drawDiscretizedAxis() {
                var textSet = [];
                var svg = elem.svg;
                var tmp_copy_no = [];
                $.each(PlotsData.getDotsGroup(), function(index, value) {
                    tmp_copy_no.push(value.xVal);
                });
                for (var j = -2; j < 3; j++) {
                    if (tmp_copy_no.indexOf(j.toString()) !== -1) {
                        textSet.push(text.gistic_txt_val[j.toString()]);
                    }
                }
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 520)")
                    .attr("class", "plots-x-axis-class")
                    .call(xAxis.ticks(textSet.length))
                    .selectAll("text")
                    .data(textSet)
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black")
                    .text(function(d){return d;});
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 20)")
                    .call(xAxis.orient("bottom").ticks(0));
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(100, 0)")
                    .attr("class", "plots-y-axis-class")
                    .call(yAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(600, 0)")
                    .call(yAxis.orient("left").ticks(0));
            }

            function initContinuousAxisX() {
                var _dataAttr = PlotsData.getDataAttr();
                var min_x = _dataAttr.min_x;
                var max_x = _dataAttr.max_x;
                var edge_x = (max_x - min_x) * 0.2;
                if (Util.plotsTypeIsMethylation()){
                    //Range for DNA Methylation Data Type
                    //Need to be fixed as from 0 to 1.
                    attr.xScale = d3. scale.linear()
                        .domain([-0.02, 1.02])
                        .range([100,600]);
                } else {
                    attr.xScale = d3.scale.linear()
                        .domain([min_x - edge_x, max_x + edge_x])
                        .range([100, 600]);
                }
                xAxis = d3.svg.axis()
                    .scale(attr.xScale)
                    .orient("bottom")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function initContinuousAxisY() {
                var _dataAttr = PlotsData.getDataAttr();
                var min_y = _dataAttr.min_y;
                var max_y = _dataAttr.max_y;
                var edge_y = (max_y - min_y) * 0.1;
                attr.yScale = d3.scale.linear()
                    .domain([min_y - edge_y, max_y + edge_y])
                    .range([520, 20]);
                yAxis = d3.svg.axis()
                    .scale(attr.yScale)
                    .orient("left")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function drawContinuousAxisMainX() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 520)")
                    .attr("class", "plots-x-axis-class")
                    .call(xAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
            }

            function drawContinuousAxisEdgeX() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 20)")
                    .call(xAxis.orient("bottom").ticks(0));
            }

            function drawContinuousAxisMainY() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(100, 0)")
                    .attr("class", "plots-y-axis-class")
                    .call(yAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
            }

            function drawContinuousAxisEdgeY() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(600, 0)")
                    .call(yAxis.orient("left").ticks(0));
            }

            function addXaxisTitle(axisTitleGroup, xTitle) {
                axisTitleGroup.append("text")
                    .attr("class", "x-axis-title")
                    .attr("x", 350)
                    .attr("y", 580)
                    .style("text-anchor", "middle")
                    .style("font-weight","bold")
                    .text(xTitle);

            }

            function addYaxisTitle(axisTitleGroup, yTitle) {
                axisTitleGroup.append("text")
                    .attr("class", "y-axis-title")
                    .attr("transform", "rotate(-90)")
                    .attr("x", -270)
                    .attr("y", 45)
                    .style("text-anchor", "middle")
                    .style("font-weight","bold")
                    .text(yTitle);

            }

            function addxAxisHelp(axisTitleGroup, _xTitle) {
                Plots.addxAxisHelp(
                    elem.svg,
                    axisTitleGroup,
                    _xTitle,
                    "x-title-help",
                    xTitleHelp
                );
            }

            function addyAxisHelp(axisTitleGroup, _yTitle) {
                Plots.addyAxisHelp(
                    elem.svg,
                    axisTitleGroup,
                    _yTitle,
                    "y-title-help",
                    yTitleHelp
                );

            }

            return {
                init: function() {
                    getAxisTitles();
                    if (Util.plotsIsDiscretized()) {
                        initDiscretizedAxis();
                        drawDiscretizedAxis();
                    } else {
                        initContinuousAxisX();
                        initContinuousAxisY();
                        drawContinuousAxisMainX();
                        drawContinuousAxisEdgeX();
                        drawContinuousAxisMainY();
                        drawContinuousAxisEdgeY();
                    }
                    var axisTitleGroup = elem.svg.append("svg:g")
                        .attr("class", "axis");
                    addXaxisTitle(axisTitleGroup, xTitle);
                    addYaxisTitle(axisTitleGroup, yTitle);
                    addxAxisHelp(axisTitleGroup, xTitle);
                    addyAxisHelp(axisTitleGroup, yTitle);  
                },
                getXHelp: function() {
                    return xTitleHelp;
                },
                getYHelp: function() {
                    return yTitleHelp;
                },
                updateLogScaleX: function(applyLogScale) {
                    d3.select("#plots_box").select(".plots-x-axis-class").remove();
                    d3.select("#plots_box").select(".x-axis-title").remove();
                    d3.select("#plots_box").select(".x-title-help").remove();
                    var _dataAttr = PlotsData.getDataAttr();
                    if (applyLogScale) {
                        if (_dataAttr.min_x <= (Plots.getLogScaleThresholdDown())) {
                            var min_x = Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2);
                        } else {
                            var min_x = Math.log(_dataAttr.min_x) / Math.log(2);
                        }
                        if (_dataAttr.max_x >= (Plots.getLogScaleThresholdUp())) {
                            var max_x = Math.log(Plots.getLogScaleThresholdUp()) / Math.log(2);
                        } else {
                            var max_x = Math.log(_dataAttr.max_x) / Math.log(2);
                        }
                        var edge_x = (max_x - min_x) * 0.2;
                        attr.xScale = d3.scale.linear()
                            .domain([min_x - edge_x, max_x + edge_x])
                            .range([100, 600]);
                        xAxis = d3.svg.axis()
                            .scale(attr.xScale)
                            .orient("bottom")
                            .tickSize(6, 0, 0)
                            .tickPadding([8]);
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addXaxisTitle(axisTitleGroup, xTitle + "(log2)");
                        addxAxisHelp(axisTitleGroup, xTitle + "(log2)");
                    } else {
                        initContinuousAxisX();
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addXaxisTitle(axisTitleGroup, xTitle);
                        addxAxisHelp(axisTitleGroup, xTitle);
                    }
                    drawContinuousAxisMainX();
                },
                updateLogScaleY: function(applyLogScale) {
                    d3.select("#plots_box").select(".plots-y-axis-class").remove();
                    d3.select("#plots_box").select(".y-axis-title").remove();
                    d3.select("#plots_box").select(".y-title-help").remove();
                    var _dataAttr = PlotsData.getDataAttr();
                    if (applyLogScale) {
                        if (_dataAttr.min_y <= (Plots.getLogScaleThresholdDown())) {
                            var min_y = Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2);
                        } else {
                            var min_y = Math.log(_dataAttr.min_y) / Math.log(2);
                        }
                        if (_dataAttr.max_y >= (Plots.getLogScaleThresholdUp())) {
                            var max_y = Math.log(Plots.getLogScaleThresholdUp()) / Math.log(2);
                        } else {
                            var max_y = Math.log(_dataAttr.max_y) / Math.log(2);
                        }
                        var edge_y = (max_y - min_y) * 0.1;
                        attr.yScale = d3.scale.linear()
                            .domain([min_y - edge_y, max_y + edge_y])
                            .range([520, 20]);
                        yAxis = d3.svg.axis()
                            .scale(attr.yScale)
                            .orient("left")
                            .tickSize(6, 0, 0)
                            .tickPadding([8]);
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addYaxisTitle(axisTitleGroup, yTitle + "(log2)");
                        addyAxisHelp(axisTitleGroup, yTitle + "(log2)");
                    } else {
                        initContinuousAxisY();
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addYaxisTitle(axisTitleGroup, yTitle);
                        addyAxisHelp(axisTitleGroup, yTitle);
                    }
                    drawContinuousAxisMainY();
                }
            };
        }());

        var Qtips = (function() {

            function confContent(d) {
                var content = "<font size='2'>";
                if (Util.plotsTypeIsCopyNo()) {
                    if (Util.plotsIsDiscretized()) {
                        content += "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    } else {
                        content += "CNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                            "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"+
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            +"' target = '_blank'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                } else if (Util.plotsTypeIsMethylation()) {
                    content += "Methylation: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                        "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid" && !Util.isEmpty(d.gisticType)) {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                } else if (Util.plotsTypeIsRPPA()) {
                    content += "mRNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                        "RPPA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid" && !Util.isEmpty(d.gisticType)) {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                }
                content = content + "</font>";
                return content;
            }

            return {
                init: function(){
                    elem.elemDotsGroup.selectAll("path").each(
                        function(d) {
                            var content = confContent(d);
                            $(this).qtip(
                                {
                                    content: {text: content},
                                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                                    show: {event: "mouseover"},
                                    hide: {fixed:true, delay: 100, event: "mouseout"},
                                    position: {my:'left bottom',at:'top right', viewport: $(window)}
                                }
                            );
                            if (Util.plotsTypeIsCopyNo()) {    //Handle special symbols
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
                                            .size(20)
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
                                elem.elemDotsGroup.selectAll("path").on("mouseover", mouseOn);
                                elem.elemDotsGroup.selectAll("path").on("mouseout", mouseOff);
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
                                elem.elemDotsGroup.selectAll("path").on("mouseover", mouseOn);
                                elem.elemDotsGroup.selectAll("path").on("mouseout", mouseOff);
                            }
                        }
                    );
                }
            };
        }());

        var ScatterPlots = (function() {

            function drawDiscretizedPlots() { //GISTIC, RAE view
                var ramRatio = 30;  //Noise
                //Divide Data Set by Gistic Type
                var subDataSet = {
                    Homdel : [],
                    Hetloss : [],
                    Diploid : [],
                    Gain : [],
                    Amp : []
                };
                $.each(PlotsData.getDotsGroup(), function(index, value) {
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
                var posVal = 0;    //Index for Positioning
                $.each(subDataSet, function(key, value) {
                    var subDotsGrp = elem.elemDotsGroup.append("svg:g");
                    subDotsGrp.selectAll("path")
                        .data(value)
                        .enter()
                        .append("svg:path")
                        .attr("class", function(d){ return d.caseId;})
                        .attr("transform", function(d){
                            var _x = attr.xScale(posVal) + (Math.random() * ramRatio - ramRatio/2);
                            var _y = attr.yScale(d.yVal);
                            $(this).attr("x_pos", _x);
                            $(this).attr("y_pos", _y);
                            $(this).attr("x_val", d.xVal);
                            $(this).attr("y_val", d.yVal);
                            $(this).attr("size", 20);
                            return "translate(" + _x + "," + _y + ")";
                        })
                        .attr("d", d3.svg.symbol()
                            .size(20)
                            .type(function(d){
                                $(this).attr("symbol", mutationStyle[d.mutationType].symbol);
                                return mutationStyle[d.mutationType].symbol;
                            })
                        )
                        .attr("fill", function(d){
                            $(this).attr("fill", mutationStyle[d.mutationType].fill);
                            return mutationStyle[d.mutationType].fill;
                        })
                        .attr("stroke", function(d){
                            $(this).attr("stroke", mutationStyle[d.mutationType].stroke);
                            return mutationStyle[d.mutationType].stroke;
                        })
                        .attr("stroke-width", 1.2);
                    posVal += 1;
                });
            }

            function drawBoxPlots(applyLogScale){
                d3.select("#plots_box").select(".box_plots").remove();
                var boxPlotsElem = elem.boxPlots.append("svg:g").attr("class", "box_plots");
                var _dotsGroup = [];
                _dotsGroup.length = 0;
                _dotsGroup = jQuery.extend(true, {}, PlotsData.getDotsGroup());
                if (applyLogScale) {
                    $.each(_dotsGroup, function(index, value) {
                        if (value.yVal <= (Plots.getLogScaleThresholdDown())) {
                            value.yVal = Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2);
                        } else if (value.yVal >= (Plots.getLogScaleThresholdUp())) {
                            value.yVal = Math.log(Plots.getLogScaleThresholdUp() / Math.log(2));
                        } else {
                            value.yVal = Math.log(value.yVal) / Math.log(2);
                        }
                    });
                }

                var min_x = PlotsData.getDataAttr().min_x;
                var max_x = PlotsData.getDataAttr().max_x;

                //Not using real x value for positioning discretized data
                var pos = 0;   //position Indicator
                for (var i = min_x ; i < max_x + 1; i++) {
                    var top;
                    var bottom;
                    var quan1;
                    var quan2;
                    var mean;
                    var IQR;
                    var scaled_y_arr=[];
                    var tmp_y_arr = [];
                    //Find the middle (vertical) line for one box plot
                    var midLine = attr.xScale(pos);
                    //Find the max/min y value with certain x value;
                    $.each(_dotsGroup, function(index, value) {
                        if (value.xVal === i.toString()) {
                            tmp_y_arr.push(parseFloat(value.yVal));
                        }
                    });
                    tmp_y_arr.sort(function(a, b) { return (a - b); });
                    if (tmp_y_arr.length === 0) {
                        //Do nothing: DO NOT MOVE POSITION INDEX (pos)
                    } else if (tmp_y_arr.length === 1) {
                        mean = attr.yScale(tmp_y_arr[0]);
                        boxPlotsElem.append("line")
                            .attr("x1", midLine-30)
                            .attr("x2", midLine+30)
                            .attr("y1", mean)
                            .attr("y2", mean)
                            .attr("stroke-width", 2)
                            .attr("stroke", "grey");
                        pos += 1;
                    } else {
                        if (tmp_y_arr.length === 2) {
                            mean = attr.yScale((tmp_y_arr[0] + tmp_y_arr[1]) / 2);
                            quan1 = bottom = attr.yScale(tmp_y_arr[0]);
                            quan2 = top = attr.yScale(tmp_y_arr[1]);
                            IQR = Math.abs(quan2 - quan1);
                            pos += 1;
                        } else {
                            var yl = tmp_y_arr.length;
                            if (yl % 2 === 0) {
                                mean = attr.yScale((tmp_y_arr[(yl / 2)-1] + tmp_y_arr[yl / 2]) / 2);
                                if (yl % 4 === 0) {
                                    quan1 = attr.yScale((tmp_y_arr[(yl / 4)-1] + tmp_y_arr[yl / 4]) / 2);
                                    quan2 = attr.yScale((tmp_y_arr[(3*yl / 4)-1] + tmp_y_arr[3 * yl / 4]) / 2);
                                } else {
                                    quan1 = attr.yScale(tmp_y_arr[Math.floor(yl / 4)]);
                                    quan2 = attr.yScale(tmp_y_arr[Math.floor(3 * yl / 4)]);
                                }
                            } else {
                                mean = attr.yScale(tmp_y_arr[Math.floor(yl / 2)]);
                                var tmp_yl = Math.floor(yl / 2) + 1;
                                if (tmp_yl % 2 === 0) {
                                    quan1 = attr.yScale((tmp_y_arr[tmp_yl / 2 - 1] + tmp_y_arr[tmp_yl / 2]) / 2);
                                    quan2 = attr.yScale((tmp_y_arr[(3 * tmp_yl / 2) - 2] + tmp_y_arr[(3 * tmp_yl / 2) - 1]) / 2);
                                } else {
                                    quan1 = attr.yScale(tmp_y_arr[Math.floor(tmp_yl / 2)]);
                                    quan2 = attr.yScale(tmp_y_arr[tmp_yl - 1 + Math.floor(tmp_yl / 2)]);
                                }
                            }
                            for (var k = 0 ; k < tmp_y_arr.length ; k++) {
                                scaled_y_arr[k] = parseFloat(attr.yScale(tmp_y_arr[k]));
                            }
                            scaled_y_arr.sort(function(a,b) { return (a - b); });
                            IQR = Math.abs(quan2 - quan1);
                            var index_top = Util.searchIndexTop(scaled_y_arr, (quan2 - 1.5 * IQR));
                            top = scaled_y_arr[index_top];
                            var index_bottom = Util.searchIndexBottom(scaled_y_arr, (quan1 + 1.5 * IQR));
                            bottom = scaled_y_arr[index_bottom];
                            pos += 1;
                        }

                        //D3 Drawing
                        boxPlotsElem.append("rect")
                            .attr("x", midLine-40)
                            .attr("y", quan2)
                            .attr("width", 80)
                            .attr("height", IQR)
                            .attr("fill", "none")
                            .attr("stroke-width", 1)
                            .attr("stroke", "#BDBDBD");
                        boxPlotsElem.append("line")
                            .attr("x1", midLine-40)
                            .attr("x2", midLine+40)
                            .attr("y1", mean)
                            .attr("y2", mean)
                            .attr("stroke-width", 2)
                            .attr("stroke", "#BDBDBD");
                        boxPlotsElem.append("line")
                            .attr("x1", midLine-30)
                            .attr("x2", midLine+30)
                            .attr("y1", top)
                            .attr("y2", top)
                            .attr("stroke-width", 1)
                            .attr("stroke", "#BDBDBD");
                        boxPlotsElem.append("line")
                            .attr("x1", midLine-30)
                            .attr("x2", midLine+30)
                            .attr("y1", bottom)
                            .attr("y2", bottom)
                            .attr("stroke", "#BDBDBD")
                            .style("stroke-width", 1);
                        boxPlotsElem.append("line")
                            .attr("x1", midLine)
                            .attr("x2", midLine)
                            .attr("y1", quan1)
                            .attr("y2", bottom)
                            .attr("stroke", "#BDBDBD")
                            .attr("stroke-width", 1);
                        boxPlotsElem.append("line")
                            .attr("x1", midLine)
                            .attr("x2", midLine)
                            .attr("y1", quan2)
                            .attr("y2", top)
                            .attr("stroke", "#BDBDBD")
                            .style("stroke-width", 1);
                    }
                }
            }

            function drawLog2Plots() {
                elem.elemDotsGroup.selectAll("path")
                    .attr("class", "dots")
                    .data(PlotsData.getDotsGroup())
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d) {
                        var _x = attr.xScale(d.xVal);
                        var _y = attr.yScale(d.yVal);
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("symbol", "circle");
                        $(this).attr("size", 20);
                        return "translate(" + _x + ", " + _y + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(20)
                        .type(function(d){
                            return mutationStyle[d.mutationType].symbol;
                        })
                    )
                    .attr("fill", function(d){
                        $(this).attr("fill", mutationStyle[d.mutationType].fill);
                        return mutationStyle[d.mutationType].fill;
                    })
                    .attr("stroke", function(d){
                        $(this).attr("stroke", mutationStyle[d.mutationType].stroke);
                        return mutationStyle[d.mutationType].stroke;
                    })
                    .attr("stroke-width", 1.2)
                    .attr("class", function(d) { return d.caseId; });
            }

            function drawContinuousPlots() {  //RPPA, DNA Methylation Views
                elem.elemDotsGroup.selectAll("path")
                    .data(PlotsData.getDotsGroup())
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d){
                        var _x = attr.xScale(d.xVal);
                        var _y = attr.yScale(d.yVal);
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("symbol", "circle");
                        $(this).attr("size", 35);
                        return "translate(" + attr.xScale(d.xVal) + ", " + attr.yScale(d.yVal) + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(35)
                        .type("circle"))
                    .attr("fill", function(d) {
                        switch (d.mutationType) {
                            case "non" : {$(this).attr("fill", "white");return "white";}
                            default: {$(this).attr("fill", "orange");return "orange";}
                        }
                    })
                    .attr("fill-opacity", function(d) {
                        switch (d.mutationType) {
                            case "non" : return 0.0;
                            default : return 1.0;
                        }
                    })
                    .attr("stroke", function(d) {
                        if (Util.hasCopyNumberData()) {
                            if (Util.isEmpty(d.gisticType)) {
                                return gisticStyle.Unknown.stroke;
                            } else {
                                return gisticStyle[d.gisticType].stroke;
                            }
                        } else {
                            return "black";
                        }
                    })
                    .attr("stroke-width", 1.2)
                    .attr("class", function(d) { return d.caseId; });
            }

            return {
                init: function() {
                    elem.boxPlots = elem.svg.append("svg:g");
                    elem.elemDotsGroup = elem.svg.append("svg:g");
                    if (Util.plotsTypeIsCopyNo()) {
                        if (Util.plotsIsDiscretized()) {    //Gistic, RAE...
                            drawBoxPlots(false);
                            drawDiscretizedPlots();
                        } else {   //Log2
                            drawLog2Plots();
                        }
                    } else {  //Methylation, RPPA
                        drawContinuousPlots();
                    }
                },
                updateLogScaleX: function(applyLogScale) {
                    elem.elemDotsGroup.selectAll("path")
                        .transition().duration(300)
                        .attr("transform", function() {
                            if (applyLogScale) {
                                if(d3.select(this).attr("x_val") <= (Plots.getLogScaleThresholdDown())) {
                                    var _post_x = attr.xScale(Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2));
                                } else if (d3.select(this).attr("x_val") >= (Plots.getLogScaleThresholdUp())) {
                                    var _post_x = attr.xScale(Math.log(Plots.getLogScaleThresholdUp()) / Math.log(2));
                                } else {
                                    var _post_x = attr.xScale(Math.log(d3.select(this).attr("x_val")) / Math.log(2));
                                }
                            } else {
                                var _post_x = attr.xScale(d3.select(this).attr("x_val"));
                            }
                            var _pre_y = d3.select(this).attr("y_pos");
                            d3.select(this).attr("x_pos", _post_x);
                            return "translate(" + _post_x + ", " + _pre_y + ")";
                        });
                },
                updateLogScaleY: function(applyLogScale) {
                    elem.elemDotsGroup.selectAll("path")
                        .transition().duration(300)
                        .attr("transform", function() {
                            var _pre_x = d3.select(this).attr("x_pos");
                            if (applyLogScale) {
                                if (parseFloat(d3.select(this).attr("y_val")) <= (Plots.getLogScaleThresholdDown())) {
                                    var _post_y = attr.yScale(Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2));
                                } else if (parseFloat(d3.select(this).attr("y_val")) >= (Plots.getLogScaleThresholdUp())) {
                                    var _post_y = attr.yScale(Math.log(Plots.getLogScaleThresholdUp()) / Math.log(2));
                                } else {
                                    var _post_y = attr.yScale(Math.log(d3.select(this).attr("y_val")) / Math.log(2));
                                }
                            } else {
                                var _post_y = attr.yScale(d3.select(this).attr("y_val"));
                            }
                            d3.select(this).attr("y_pos", _post_y);
                            return "translate(" + _pre_x + ", " + _post_y + ")";
                        });
                    if (Util.plotsIsDiscretized()) {
                        drawBoxPlots(applyLogScale);
                    }
                }
            };
        }());

        var Legends = (function() {

            function drawCopyNoViewLegends() {
                //Only show glyphs whose mutation type
                //appeared in the current individual case
                var _appearedMutationTypes = [];
                _appearedMutationTypes.length = 0;
                $.each(PlotsData.getDotsGroup(), function(index, value) {
                    _appearedMutationTypes.push(value.mutationType);
                });

                //Convert object to array
                var mutationStyleArr = [];
                mutationStyleArr.length = 0;
                for (var key in mutationStyle) {
                    var obj = mutationStyle[key];
                    if (_appearedMutationTypes.indexOf(key) !== -1) {
                        mutationStyleArr.push(obj);
                    }
                }
                //If only contain non mutation cases, remove the glyphs completely
                if (mutationStyleArr.length === 1 &&
                    mutationStyleArr[0].legendText === mutationStyle["non"].legendText) {
                    mutationStyleArr.length = 0;
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
                
                if (Util.hasCopyNumberData()) {
                    for (var key in gisticStyle) {
                        var obj = gisticStyle[key];
                        gisticStyleArr.push(obj);
                    }
                }
                
                var mutatedStyle = {
                        stroke : "none",
                        symbol : "circle",
                        fill : "orange",
                        legendText : "Mutated"
                    };
                gisticStyleArr.push(mutatedStyle);

                var legend = elem.svg.selectAll(".legend")
                    .data(gisticStyleArr)
                    .enter().append("g")
                    .attr("class", "legend")
                    .attr("transform", function(d, i) {
                        return "translate(610, " + (30 + i * 15) + ")";
                    });

                legend.append("path")
                    .attr("width", 18)
                    .attr("height", 18)
                    .attr("d", d3.svg.symbol()
                        .size(40)
                        .type(function(d) { return d.symbol; }))
                    .attr("fill", function (d) { return d.fill; })
                    .attr("stroke", function (d) { return d.stroke; })
                    .attr("stroke-width", 1.2);

                legend.append("text")
                    .attr("dx", ".75em")
                    .attr("dy", ".35em")
                    .style("text-anchor", "front")
                    .text(function(d) { return d.legendText; });    
            }

            return {
                init: function() {
                    if (Util.plotsTypeIsCopyNo()) {
                        drawCopyNoViewLegends();
                    } else {
                        drawOtherViewLegends();
                    }
                    if (!Util.plotsIsDiscretized()) {
                        var tmpDataAttr = PlotsData.getDataAttr();
                        var tmpPearson = "Pearson: " + tmpDataAttr.pearson,
                            tmpSpearman = "Spearman: " + tmpDataAttr.spearman;
                        var coExpLegend = elem.svg.selectAll(".coexp_legend")
                            .data(["Correlation", tmpPearson, tmpSpearman])
                            .enter().append("g")
                            .attr("class", "coexp_legend")
                            .attr("transform", function(d, i) {
                                return "translate(600, " + (150 + i * 15) + ")";
                            });
                        coExpLegend.append("text")
                                .attr("dx", ".75em")
                                .attr("dy", ".35em")
                                .style("text-anchor", "front")
                                .text(function(d) {
                                    return d;
                                });                      
                    }
                }
            };
        }());

        function initCanvas() {
            elem.svg = d3.select("#plots_box")
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
        }

        function drawErrMsgs() {
            var _xDataType = "",
                _yDataType = "";
            if (Util.plotsTypeIsCopyNo()) {
                var e = document.getElementById("data_type_copy_no");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_mrna");
                _yDataType = e.options[e.selectedIndex].text;
            } else if (Util.plotsTypeIsMethylation()) {
                var e = document.getElementById("data_type_dna_methylation");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_mrna");
                _yDataType = e.options[e.selectedIndex].text;
            } else if (Util.plotsTypeIsRPPA()) {
                var e = document.getElementById("data_type_mrna");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_rppa");
                _yDataType = e.options[e.selectedIndex].text;
            }

            var err_line1 = "There is no UNAVAILABLE_DATA_TYPE data";
            var err_line2 = "for " + userSelection.gene + " in the selected cancer study.";
            var _dataStatus = PlotsData.getDataStatus();
            if (!_dataStatus.xHasData && _dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", _xDataType);
            } else if (_dataStatus.xHasData && !_dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", _yDataType);
            } else if (!_dataStatus.xHasData && !_dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", "both selected data types");
            } else if (_dataStatus.xHasData &&_dataStatus.yHasData &&
                !_dataStatus.combineHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", "combined data types");
            }

            elem.svg.append("text")
                .attr("x", 350)
                .attr("y", 50)
                .attr("text-anchor", "middle")
                .attr("fill", "#DF3A01")
                .text(err_line1);
            elem.svg.append("text")
                .attr("x", 350)
                .attr("y", 70)
                .attr("text-anchor", "middle")
                .attr("fill", "#DF3A01")
                .text(err_line2);
            elem.svg.append("rect")
                .attr("x", 150)
                .attr("y", 30)
                .attr("width", 400)
                .attr("height", 50)
                .attr("fill", "none")
                .attr("stroke-width", 1)
                .attr("stroke", "#BDBDBD");
        }

        function drawImgConverter() {
            $('#view_title').empty();
            if (Util.plotsTypeIsCopyNo()) {
                $('#view_title').append(userSelection.gene + ": mRNA Expression v. CNA ");
            } else if (Util.plotsTypeIsMethylation()) {
                $('#view_title').append(userSelection.gene + ": mRNA Expression v. DNA Methylation ");
            } else if (Util.plotsTypeIsRPPA()) {
                $('#view_title').append(userSelection.gene + ": RPPA protein level v. mRNA Expression ");
            }
            var pdfConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank' " +
                "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='correlation_plot-" + userSelection.gene + ".pdf'>" +
                "<input type='submit' value='PDF'></form>";
            $('#view_title').append(pdfConverterForm);
            var svgConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank'" +
                "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' name='filename' value='correlation_plot-" + userSelection.gene + ".svg'>" +
                "<input type='submit' value='SVG'></form>";
            $('#view_title').append(svgConverterForm);
        }

        function applyLogScaleX(applyLogScale) {
            //Update the axis
            Axis.updateLogScaleX(applyLogScale);
            //Update the position of the dots
            ScatterPlots.updateLogScaleX(applyLogScale);
        }

        function applyLogScaleY(applyLogScale) {
            //Update the axis
            Axis.updateLogScaleY(applyLogScale);
            //Update the position of the dots
            ScatterPlots.updateLogScaleY(applyLogScale);
        }

        return {
            init: function() {
                initCanvas();
                if (PlotsData.getDotsGroup().length !== 0) {
                    drawImgConverter();
                    Axis.init();
                    ScatterPlots.init();
                    Legends.init();
                    Qtips.init();
                    if (document.getElementById("log_scale_option_x") !== null) {
                        var _applyLogScaleX = document.getElementById("log_scale_option_x").checked;
                        applyLogScaleX(_applyLogScaleX);
                    }
                    if (document.getElementById("log_scale_option_y") !== null) {
                        var _applyLogScaleY = document.getElementById("log_scale_option_y").checked;
                        applyLogScaleY(_applyLogScaleY);
                    }
                } else { //No available data
                    drawErrMsgs();
                }
            },
            applyLogScaleX: applyLogScaleX,
            applyLogScaleY: applyLogScaleY
        };
    }());

    function getUserSelection() {
        userSelection.gene = document.getElementById("gene").value;
        userSelection.plots_type = document.getElementById("plots_type").value;
        userSelection.copy_no_type = document.getElementById("data_type_copy_no").value.split("|")[0];
        userSelection.mrna_type = document.getElementById("data_type_mrna").value.split("|")[0];
        userSelection.rppa_type = document.getElementById("data_type_rppa").value.split("|")[0];
        userSelection.dna_methylation_type = document.getElementById("data_type_dna_methylation").value.split("|")[0];
    }

    function generatePlots() {
        getProfileData();
    }

    function getProfileData() {
        var sel = document.getElementById("data_type_copy_no");
        var vals = [];
        for (var i = 0; i < sel.children.length; ++i) {
            var child = sel.children[i];
            if (child.tagName === 'OPTION') vals.push(child.value.split("|")[0]);
        }
        if (vals.indexOf(cancer_study_id + "_gistic") !== -1) {
            discretizedDataTypeIndicator = cancer_study_id + "_gistic";
        } else if (vals.indexOf(cancer_study_id + "_cna") !== -1) {
            discretizedDataTypeIndicator = cancer_study_id + "_cna";
        } else if (vals.indexOf(cancer_study_id + "_CNA") !== -1) {
            discretizedDataTypeIndicator = cancer_study_id + "_CNA";
        } else if (vals.indexOf(cancer_study_id + "_cna_rae") !== -1) {
            discretizedDataTypeIndicator = cancer_study_id + "_cna_rae";
        }
        var _profileIdsStr = cancer_study_id + "_mutations" + " " +
            discretizedDataTypeIndicator + " " +
            userSelection.copy_no_type + " " +
            userSelection.mrna_type + " " +
            userSelection.rppa_type + " " +
            userSelection.dna_methylation_type;
        Plots.getProfileData(
            userSelection.gene,
            _profileIdsStr,
            patient_set_id,
            patient_ids_key,
            getProfileDataCallBack
        );
    }

    function getProfileDataCallBack(profileDataResult) {
        //TODO: error handle should be get Mutation servlet
        var resultObj = profileDataResult[userSelection.gene];
        var _hasMutationProfile = true;
        for (var key in resultObj) {  //key is case id
            var _obj = resultObj[key];
            if (!_obj.hasOwnProperty(cancer_study_id + "_mutations")) {
                _hasMutationProfile = false;
            } else {
                _hasMutationProfile = true;
            }
        }

        if (_hasMutationProfile) {
            Plots.getMutationType(
                userSelection.gene,
                cancer_study_id + "_mutations",
                patient_set_id,
                patient_ids_key,
                getMutationTypeCallBack
            );
        } else {
            PlotsData.init(profileDataResult, "");
        }

        function getMutationTypeCallBack(mutationTypeResult) {
            PlotsData.init(profileDataResult, mutationTypeResult);

        }
    }

    return {
        init: function(){
            $('#view_title').empty();
            $('#plots_box').empty();
            $('#loading-image').show();
            $('#view_title').hide();
            $('#plots_box').hide();

            var _status = PlotsMenu.getStatus();
            if (_status.has_mrna && (_status.has_copy_no || _status.has_dna_methylation || _status.has_rppa)) {
                getUserSelection();
                generatePlots();
            } else {
                $('#loading-image').hide();
            }
        },
        applyLogScaleX: function() {
            var applyLogScale = document.getElementById("log_scale_option_x").checked;
            View.applyLogScaleX(applyLogScale);
        },
        applyLogScaleY: function() {
            var applyLogScale = document.getElementById("log_scale_option_y").checked;
            View.applyLogScaleY(applyLogScale);
        }
    };

}());//Closing PlotsView
