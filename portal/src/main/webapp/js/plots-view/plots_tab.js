var PlotsMenu = (function () {
    var content = {
            plots_type_list : [
                ["mrna_vs_copy_no", "mRNA vs. Copy Number"],
                ["mrna_vs_dna_methylation", "mRNA vs. DNA Methylation"],
                ["rppa_protein_level_vs_mrna", "RPPA Protein Level vs. mRNA"]
            ],
            genetic_profile_mutations : [],
            genetic_profile_mrna : [],
            genetic_profile_copy_no : [],
            genetic_profile_rppa : [],
            genetic_profile_dna_methylation : []
        },
        status = {
            //General Profile Info
            has_mrna : false,
            has_dna_methylation : false,
            has_rppa : false,
            has_copy_no : false,

            //User Selection Info

        };
    function fetchFrameData() {
        $.ajax({
            url: "webservice.do?cmd=getGeneticProfiles&cancer_study_id=" + cancer_study_id,
            type: 'get',
            dataType: 'text',
            async: false,
            success: function (data) {
                if (data !== "") {
                    tmp_lines = data.split(/\n/);
                    for (var j = 2; j < tmp_lines.length; j++) {
                        if (tmp_lines[j] !== "") {
                            tmp_columns = tmp_lines[j].split(/\t/);
                            if (tmp_columns[4] === "MUTATION_EXTENDED") {
                                tmpArr = [tmp_columns[0], tmp_columns[1]];
                                content.genetic_profile_mutations.push(tmpArr);
                            } else if (tmp_columns[4] === "MRNA_EXPRESSION") {
                                tmpArr = [tmp_columns[0], tmp_columns[1]];
                                content.genetic_profile_mrna.push(tmpArr);
                                status.has_mrna = true;
                            } else if (tmp_columns[4] === "COPY_NUMBER_ALTERATION") {
                                tmpArr = [tmp_columns[0], tmp_columns[1]];
                                content.genetic_profile_copy_no.push(tmpArr);
                                status.has_copy_no = true;
                            } else if (tmp_columns[4] === "METHYLATION") {
                                tmpArr = [tmp_columns[0], tmp_columns[1]];
                                content.genetic_profile_dna_methylation.push(tmpArr);
                                status.has_dna_methylation = true;
                            } else if (tmp_columns[4] === "PROTEIN_ARRAY_PROTEIN_LEVEL") {
                                tmpArr = [tmp_columns[0], tmp_columns[1]];
                                content.genetic_profile_rppa.push(tmpArr);
                                status.has_rppa = true;
                            }
                        }
                    }
                } else {
                    alert("ERROR(Ajax Call) Fetching Plots Frame Data.");
                }
            }
        });
    }
    function appendDropDown(divId, value, text) {
        $(divId).append("<option value='" + value + "'>" + text + "</option>");
    }
    function toggleVisibilityShow(elemId) {
        var e = document.getElementById(elemId);
        e.style.display = 'block';
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
        init: function () {
            generateList("genes", gene_list);
            fetchFrameData();
            if ( status.has_mrna && status.has_copy_no) {
                appendDropDown('#plots_type', content.plots_type_list[0][0], content.plots_type_list[0][1]);
            }
            if ( status.has_mrna && status.has_dna_methylation) {
                appendDropDown('#plots_type', content.plots_type_list[1][0], content.plots_type_list[1][1]);
            }
            if ( status.has_mrna && status.has_rppa) {
                appendDropDown('#plots_type', content.plots_type_list[2][0], content.plots_type_list[2][1]);
            }
            //Hidden Mutation Field
            for (var j = 0; j < content.genetic_profile_mutations.length; j++ ) {
                $('#data_type_mutation').append("<option value='" +
                    content.genetic_profile_mutations[j][0] + "'>" +
                    content.genetic_profile_mutations[j][1] + "</option>");
            }
            //Display All Datatypes first.
            for (var j = 0; j < content.genetic_profile_copy_no.length; j++ ) {
                $('#data_type_copy_no').append("<option value='" +
                    content.genetic_profile_copy_no[j][0] + "'>" +
                    content.genetic_profile_copy_no[j][1] + "</option>");
            }
            for ( var j = 0; j < content.genetic_profile_mrna.length; j++ ) {
                $('#data_type_mrna').append("<option value='" +
                    content.genetic_profile_mrna[j][0] + "'>" +
                    content.genetic_profile_mrna[j][1] + "</option>");
            }
            for ( var j = 0; j < content.genetic_profile_dna_methylation.length; j++ ) {
                $('#data_type_dna_methylation').append("<option value='" +
                    content.genetic_profile_dna_methylation[j][0] + "'>" +
                    content.genetic_profile_dna_methylation[j][1] + "</option>");
            }
            for ( j = 0; j < content.genetic_profile_rppa.length; j++ ) {
                $('#data_type_rppa').append("<option value='" +
                    content.genetic_profile_rppa[j][0] + "'>" +
                    content.genetic_profile_rppa[j][1] + "</option>");
            }
        },
        update: function() {
            //Adjust the menu content
            var currentPlotsType = $('#plots_type').val();
            if (currentPlotsType.indexOf("copy_no") !== -1) {
                toggleVisibilityShow("mrna_dropdown");
                toggleVisibilityShow("copy_no_dropdown");
                toggleVisibilityHide("dna_methylation_dropdown");
                toggleVisibilityHide("rppa_dropdown");
            } else if (currentPlotsType.indexOf("dna_methylation") !== -1) {
                toggleVisibilityShow("mrna_dropdown");
                toggleVisibilityHide("copy_no_dropdown");
                toggleVisibilityShow("dna_methylation_dropdown");
                toggleVisibilityHide("rppa_dropdown");
            } else if (currentPlotsType.indexOf("rppa") !== -1) {
                toggleVisibilityShow("mrna_dropdown");
                toggleVisibilityHide("copy_no_dropdown");
                toggleVisibilityHide("dna_methylation_dropdown");
                toggleVisibilityShow("rppa_dropdown");
            }
            //Set Default mRNA Selection
            //----Priority List: User selection, RNA Seq V2, RNA Seq, Z-scores
            $("#data_type_mrna > option").each(function() {
                if (this.text.toLowerCase().indexOf("z-scores")){
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
            var userSelectedMrnaProfile = "";
            $.each(geneticProfiles.split(/\s+/), function(index, value){ //geneticProfiles --> global variable, passing user selected profile IDs
                if (value.indexOf("mrna") !== -1) {
                    userSelectedMrnaProfile = value;
                    return false;
                }
            });
            $("#data_type_mrna > option").each(function() {
                if (this.value === userSelectedMrnaProfile){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            //Re-generate the plots-view
            PlotsView.init();
        }
    };
}());

var PlotsView = (function () {
    var elem = {
            svg : "",
            xScale : "",
            yScale : "",
            xAxis : "",
            yAxis : "",
            dotsGroup : ""
        },
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
        },
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
        },
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
                legendText : "non"
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
        },
        userSelection = {
            plots_type: "",
            mutation_type : "",
            copy_no_type : "",
            mrna_type : "",
            dna_methylation_type : "",
            rppa_type : "",
        },
    //Fileter/form multi-demention Dataset for D3 to use
        tmpDataSet = [],
        datum = {
            caseId : "",
            xVal : "",
            yVal : "",
            mutationDetail : "",  //Mutation ID
            mutationType : "",
            gisticType : "", //Discretized(GISTIC/RAE) Annotation
        };

    //Util Functions
    function copyData(desArray, oriArray) {
        desArray.length = 0;
        var desArrayIndex = 0;
        for ( var tmpIndex = 0; tmpIndex < oriArray.length; tmpIndex ++ ){
            if (oriArray[tmpIndex] !== "" && oriArray[tmpIndex] !== null ) {
                desArray[desArrayIndex] = oriArray[tmpIndex];
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
            if (xData[j] !== "NaN" && yData[j] !== "NaN" && xData[j] !== "NA" && yData[j] !== "NA") {
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
        for( var i = 0; i < arr.length; i++) {
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
        for( var i = 0; i < arr.length; i++) {
            if (ele <= arr[i]) {
                return i;
            } else {
                continue;
            }
        }
        return arr.length - 1;
    };
    function PlotsTypeIsCopyNo() {
        return userSelection.plots_type.indexOf("copy_no") !== -1;
    };
    function PlotsTypeIsMethylation() {
        return userSelection.plots_type.indexOf("methylation") !== -1;
    };
    function PlotsTypeIsRPPA() {
        return userSelection.plots_type.indexOf("rppa") !== -1;
    };

    //Functions for Retrieving Texts for Markups
    function getDataTypes() {
        userSelection.plots_type = document.getElementById("plots_type").value;
        userSelection.mutation_type = document.getElementById("data_type_mutation").value;
        userSelection.copy_no_type = document.getElementById("data_type_copy_no").value;
        userSelection.mrna_type = document.getElementById("data_type_mrna").value;
        userSelection.rppa_type = document.getElementById("data_type_rppa").value;
        userSelection.dna_methylation_type = document.getElementById("data_type_dna_methylation").value;
    };
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

    //Functions for Data Fetching/Formatting
    function fetchPlotsData() {
        var result_set = new Array(5);
        for ( var i = 0 ; i< result_set.length; i++) {
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
                                copyData(result_set[i], profile_data);
                            }
                        } else {
                            alert("ERROR Fetching Data.");
                        }
                    }
                });
            }
        }
        copyData(pData.mutation_detail, result_set[0]);
        copyData(pData.copy_no, result_set[1]);
        copyData(pData.mrna, result_set[2]);
        copyData(pData.rppa, result_set[3]);
        copyData(pData.dna_methylation, result_set[4]);
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
            } else if ((item === "In_Frame_Del")||(item === "In_Frame_Ins")) {
                item = text.mutations_alias.in_frame;
            } else if ((item === "Missense_Mutation")||(item === "Missense")) {
                item = text.mutations_alias.missense;
            } else if ((item === "Nonsense_Mutation")||(item === "Nonsense")) {
                item = text.mutations_alias.nonsense;
            } else if ((item === "Splice_Site")||(item === "Splice_Site_SNP")) {
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
        for (var i = 0 ; i < pData.case_set.length; i++) {
            var tmpObj = jQuery.extend(true, {}, datum);
            tmpObj.caseId = pData.case_set[i];
            tmpObj.gisticType = pData.gisticType[i];
            tmpObj.mutationType = pData.mutation_type[i];
            tmpObj.mutationDetail = pData.mutation_detail[i];
            if (userSelection.plots_type.indexOf("copy_no") !== -1) { // mrna vs. copy_no
                if ((pData.copy_no[i] !== "NaN") &&
                    (pData.copy_no[i] !== "NA") &&
                    (pData.mrna[i] !== "NaN") &&
                    (pData.mrna[i] !== "NA")){
                    tmpObj.xVal = pData.copy_no[i];
                    tmpObj.yVal = pData.mrna[i];
                    tmpDataSet.push(tmpObj);
                }
            } else if (userSelection.plots_type.indexOf("dna_methylation") !== -1) {
                if ((pData.dna_methylation[i] !== "NaN") &&
                    (pData.dna_methylation[i] !== "NA") &&
                    (pData.mrna[i] !== "NaN") &&
                    (pData.mrna[i] !== "NA")){
                    tmpObj.xVal = pData.dna_methylation[i];
                    tmpObj.yVal = pData.mrna[i];
                    tmpDataSet.push(tmpObj);
                }
            } else if (userSelection.plots_type.indexOf("rppa") !== -1) {
                if ((pData.mrna[i] !== "NaN") &&
                    (pData.mrna[i] !== "NA") &&
                    (pData.rppa[i] !== "NaN") &&
                    (pData.rppa[i] !== "NA")){
                    tmpObj.xVal = pData.mrna[i];
                    tmpObj.yVal = pData.rppa[i];
                    tmpDataSet.push(tmpObj);
                }
            } else {
                //TODO: Error Handler
            }
        }
    }

    //Functions for D3 Drawing
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
        if (PlotsTypeIsCopyNo()) {
            $('#view_title').append(pData.gene + ": mRNA Expression v. CNA ");
        } else if (PlotsTypeIsMethylation()) {
            $('#view_title').append(pData.gene + ": mRNA Expression v. DNA Methylation ");
        } else if (PlotsTypeIsRPPA()) {
            $('#view_title').append(pData.gene + ": RPPA protein level v. mRNA Expression ");
        }
        var pdfConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' onsubmit=\"this.elements['svgelement'].value=loadSVGforPDF();\">" +
            "<input type='hidden' name='svgelement'>" +
            "<input type='hidden' name='filetype' value='pdf'>" +
            "<input type='submit' value='PDF'></form>";
        $('#view_title').append(pdfConverterForm);
        var svgConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' onsubmit=\"this.elements['svgelement'].value=loadSVGforSVG();\">" +
            "<input type='hidden' name='svgelement'>" +
            "<input type='hidden' name='filetype' value='svg'>" +
            "<input type='submit' value='SVG'></form>";
        $('#view_title').append(svgConverterForm);
    }



    function initAxis() {
        var analyseResult = {};
        if (userSelection.plots_type.indexOf("copy_no") !== -1) {
            analyseResult = analyseData(pData.copy_no, pData.mrna);
        } else if (userSelection.plots_type.indexOf("dna_methylation") !== -1) {
            analyseResult = analyseData(pData.dna_methylation, pData.mrna);
        } else if (userSelection.plots_type.indexOf("rppa") !== -1) {
            analyseResult = analyseData(pData.mrna, pData.rppa);
        }
        var min_x = analyseResult.min_x;
        var max_x = analyseResult.max_x;
        var edge_x = analyseResult.edge_x;
        var min_y = analyseResult.min_y;
        var max_y = analyseResult.max_y;
        var edge_y = analyseResult.edge_y;

        if ( userSelection.plots_type.indexOf("methylation") !== -1 &&
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

        var analyseResult = {};
        analyseResult = analyseData(pData.copy_no, pData.mrna);
        var min_x = analyseResult.min_x;
        var max_x = analyseResult.max_x;
        var edge_x = analyseResult.edge_x;
        var min_y = analyseResult.min_y;
        var max_y = analyseResult.max_y;
        var edge_y = analyseResult.edge_y;

        //reset max_x as the range of slots
        // -- Not real max x value for scaling!!
        slotsCnt = 0;
        for (var j = min_x; j < max_x + 1; j++) {
            if (pData.copy_no.indexOf(j.toString()) !== -1) {
                slotsCnt += 1;
            }
        }
        var new_min_x = 0;
        var new_max_x = slotsCnt - 1;

        elem.xScale = d3.scale.linear()
            .domain([new_min_x - edge_x, new_max_x + edge_x])
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

        for (var j = -2; j < 3; j++) {
            if (pData.copy_no.indexOf(j.toString()) !== -1) {
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

        var tmp_data_result = analyseData(xData, yData);
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
                if (yData[j] != "NaN" && xData[j] != "NaN" && yData[j] != "NA" && xData[j] != "NA" && xData[j] == i) {
                    tmp_y_arr[index_tmp_y_data_array] = parseFloat(yData[j]);
                    index_tmp_y_data_array += 1;
                }
            }
            tmp_y_arr.sort(function(a,b){return a-b});
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
                if (tmp_y_arr.length == 2) {
                    mean = elem.yScale((tmp_y_arr[0] + tmp_y_arr[1])/2);
                    quan1 = bottom = elem.yScale(tmp_y_arr[0]);
                    quan2 = top = elem.yScale(tmp_y_arr[1]);
                    IQR = Math.abs(quan2 - quan1);
                    pos += 1;
                } else {
                    var yl = tmp_y_arr.length;
                    if (yl % 2 == 0) {
                        mean = elem.yScale((tmp_y_arr[(yl/2)-1] + tmp_y_arr[yl/2])/2);
                        if (yl % 4 == 0) {
                            quan1 = elem.yScale((tmp_y_arr[(yl/4)-1] + tmp_y_arr[yl/4])/2);
                            quan2 = elem.yScale((tmp_y_arr[(3*yl/4)-1] + tmp_y_arr[3*yl/4])/2);
                        } else {
                            quan1 = elem.yScale(tmp_y_arr[Math.floor(yl/4)]);
                            quan2 = elem.yScale(tmp_y_arr[Math.floor(3*yl/4)]);
                        }
                    } else {
                        mean = elem.yScale(tmp_y_arr[Math.floor(yl/2)]);
                        var tmp_yl = Math.floor(yl/2) + 1;
                        if ( tmp_yl % 2 == 0) {
                            quan1 = elem.yScale((tmp_y_arr[tmp_yl/2 - 1] + tmp_y_arr[tmp_yl/2])/2);
                            quan2 = elem.yScale((tmp_y_arr[(3 * tmp_yl/2) - 2] + tmp_y_arr[(3*tmp_yl/2)-1])/2);
                        } else {
                            quan1 = elem.yScale(tmp_y_arr[Math.floor(tmp_yl/2)]);
                            quan2 = elem.yScale(tmp_y_arr[tmp_yl - 1 + Math.floor(tmp_yl/2)]);
                        }
                    }
                    for (var k = 0 ; k < tmp_y_arr.length ; k++) {
                        scaled_y_arr[k] = parseFloat(elem.yScale(tmp_y_arr[k]));
                    }
                    scaled_y_arr.sort(function(a,b){return a-b});
                    IQR = Math.abs(quan2 - quan1);
                    var index_top = searchIndexTop(scaled_y_arr, (quan2-1.5*IQR));
                    top = scaled_y_arr[index_top];
                    var index_bottom = searchIndexBottom(scaled_y_arr, (quan1+1.5*IQR));
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

        //Convert object to array
        var mutationStyleArr = [];
        for (var key in mutationStyle) {
            var obj = mutationStyle[key];
            mutationStyleArr.push(obj);
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

    function drawQtips() {
        elem.dotsGroup.selectAll('path').each(function(d) {
            $(this).qtip({
                content: {text: 'qtip failed'},
                events: {
                    render: function(event, api) {
                        var content = "<font size='2'>";

                        if (PlotsTypeIsCopyNo()) {
                            if (dataIsDiscretized()) {
                                content += "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                            } else {
                                content += "CNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                                    "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                            }
                        } else if (PlotsTypeIsMethylation()) {    //mrna vs. dna methylation
                            content += "Methylation: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                                "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                            if (d.gisticType !== "Diploid") {
                                content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                            }
                        } else if (PlotsTypeIsRPPA()) { //rppa vs. mrna
                            content += "mRNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                                "RPPA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                            if (d.gisticType !== "Diploid") {
                                content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                            }
                        }

                        content += "Case ID: <strong><a href='tumormap.do?case_id=" + d.caseId +
                            "&cancer_study_id=" + cancer_study_id + "'>" + d.caseId +
                            "</a></strong><br>";

                        if (d.mutationType !== 'non') {
                            content = content + "Mutation: " + "<strong>" + d.mutationDetail + "<br>";
                        }

                        content = content + "</font>";
                        api.set('content.text', content);
                    }
                },
                show: 'mouseover',
                hide: { fixed:true, delay: 100},
                style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                position: {my:'left bottom',at:'top right'}
            });
        });
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

    function dataIsAvailable() {

        var xHasData = false;
        var yHasData = false;
        var combineHasData = false;

        if (PlotsTypeIsCopyNo()) {
            xData = pData.copy_no;
            yData = pData.mrna;
        } else if (PlotsTypeIsMethylation()) {
            xData = pData.dna_methylation;
            yData = pData.mrna;
        } else if (PlotsTypeIsRPPA()) {
            xData = pData.mrna;
            yData = pData.rppa;
        }

        for (var j = 0; j < pData.case_set.length; j++) {
            if (xData[j] !== "NaN" || xData[j] === "NA") {
                xHasData = true;
            }
        }
        for (var k = 0; k < pData.case_set.length; k++) {
            if (yData[k] !== "NaN" || yData[k] === "NA") {
                yHasData = true;
            }
        }
        for (var m = 0; m < xData.length; m++) {
            if ((xData[m] !== "NaN" || xData[m] === "NA") &&
                (yData[m] !== "NaN" || yData[m] === "NA")) {
                combineHasData = true;
            }
        }

        if ((yHasData === false) ||
            (xHasData === false) ||
            (combineHasData === false)) {
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
    }

    return {
        init: function(){

            $('#loading-image').show();
            $('#view_title').hide();
            $('#plots_box').hide();

            //Data Processing
            pData.gene = document.getElementById("genes").value;
            getDataTypes();
            getAxisTitles();
            fetchPlotsData();
            translateGisticType();
            fetchMutationType();
            //TODO: error handle
            formDataSet();    //Build the tmp data set for d3 to use
            reorderMutations();

            //View Construction
            initView();
            if (dataIsAvailable()) {
                drawImgConverter();  //PDF, SVG download buttons
                if (PlotsTypeIsCopyNo()) {
                    if (dataIsDiscretized()) {
                        initDiscretizedAxis();
                        drawDiscretizedAxis();
                        drawBoxPlots();
                        drawDiscretizedPlots();
                    } else {
                        initAxis();
                        drawContinuousAxis();
                        drawLog2Plots();
                    }
                    drawCopyNoViewLegends();
                } else if (PlotsTypeIsMethylation()) { //RPPA and GISTIC view
                    initAxis();
                    drawContinuousAxis();
                    drawContinuousPlots();
                    drawOtherViewLegends();
                } else if (PlotsTypeIsRPPA()) {
                    initAxis();
                    drawContinuousAxis();
                    drawContinuousPlots();
                    drawOtherViewLegends();
                }
                drawAxisTitle();
                drawQtips();
                //Img Center: PDF and SVG button
            }

            setTimeout(function() {
                $('#loading-image').hide();
                $('#view_title').show();
                $('#plots_box').show();
            }, 500);


        }
    };
}());