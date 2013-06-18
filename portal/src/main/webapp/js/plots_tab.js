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
    return {
        init: function () {
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
            //Re-generate the plots-view
            PlotsView.init();
        }
    };
}());

var PlotsView = (function () {
    var elem = {
            svg: "",
            xScale: "",
            yScale: "",
            xAxis: "",
            yAxis: ""
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
		mutationStyle = [
			frameshift : {
				symbol : "triangle-down",
				fill : "#1C1C1C",	
				stroke : "#B40404"
			},
			nonsense : {
				symbol : "diamond",
				fill : "#1C1C1C",
				stroke : "#B40404"
			},
			splice : {
				symbol : "triangle-up",
				fill : "#A4A4A4",
				stroke : "#B40404"
			},
			in_frame: {
				symbol : "square",
				fill : "#DF7401",
				stroke : "#B40404"
			},
			nonstart : {
				symbol : "cross",
				fill : "#DF7401",
				stroke : "#B40404"
			},
			nonstop : {
				symbol : "triangle-up",
				fill : "#1C1C1C",
				stroke : "#B40404" 
			},
			missense : {
				symbol : "circle",
				fill : "#DF7401",
				stroke "#B40404"
			},
			non : {
				symbol : "circle",
				fill : "#00AAF8",
				stroke : "#0089C6"
			}
		],
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

    //Functions for Retrieving Texts for Markups
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

    //Functions for Data Fetching/Formatting
    function fetchPlotsData() {
        var result_set = new Array(5);
        for ( var i = 0 ; i< result_set.length; i++) {
            result_set[i] = new Array();
        }
        var url_base = "webservice.do?cmd=getProfileData&case_set_id=" +
                        case_set_id + "&gene_list=" +
                        pData.gene + "&genetic_profile_id=";
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
        $.ajax({
            url: "webservice.do?cmd=getMutationData&case_set_id=" + case_set_id +
                 "&gene_list=" + pData.gene +
                 "&genetic_profile_id=" + cancer_study_id + "_mutations",
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
    function initSVG() {
        $('#plots_box').empty();
        elem.svg = d3.select("#plots_box")
            .append("svg")
            .attr("width", setting.canvas_width)
            .attr("height", setting.canvas_height);
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
        console.log(analyseResult);

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
        //Define Axis
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
        for (var j = -2; j < 3; j++) { // j: -2, -1, 0, 1, 2
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
		var dotsGroup = elemsvg.append("svg:g");
    	dotsGroup.selectAll("path")
        	.data(pData.tmpDataSet)
        	.enter()
        	.append("svg:path")
        	.attr("transform", function(d) { return "translate(" + elem.xScale(d.xVal) + ", " + elem.yScale(d.yVal) + ")";})
        	.attr("d", d3.svg.symbol()
            			.size(function(d) {
                			switch (d.mutationType) {
                    			case "non" : return 15;
                    			default : return 25;
                			}
            			})
            			.type(function(d) {
							return mutationStyle[d.mutationType].symbol;
                		})
        	.attr("fill", function(d) {
				return mutationStyle[d.mutationType].fill;
        	})
        	.attr("stroke", function(d) {
				return mutationStyle[d.mutationType].stroke;
        	})
        	.attr("stroke-width", 1.2);
    }
    function drawBoxPlots() { 

    }
    function drawLog2Plots() { //Log2 Copy Number Alteration View
		dotsGroup = elem.svg.append("svg:g");
    	dotsGroup.selectAll("path")
        	.data(tmpDataSet)
        	.enter()
        	.append("svg:path")
        	.attr("transform", function(d) { return "translate(" + elem.xScale(d.xVal) + ", " + elem.yScale(d.yVal) + ")";})
        	.attr("d", d3.svg.symbol()
            	.size(function(d) {
                	switch (d[2]) {
                    	case "non" : return 15;
                    	default : return 25;
                	}
            	})
            	.type(function (d) {
					return mutationStyle[d.mutationType].symbol;
            	})
        	)
        	.attr("fill", function(d) {
            	return mutationStyle[d.mutationType].fill;
        	})
        	.attr("stroke", function(d) {
        		return mutationStyle[d.mutationType].stroke;
			})
       	 	.attr("stroke-width", 1.2);
	}
    function drawContinuousPlots() { //DNA Methylation, RPPA view

    }
    function drawQtips() {

    }
    function drawAxisTitles() {

    }

    return {
        init: function() {

            //Data Processing
            pData.gene = document.getElementById("genes").value;
            getDataTypes();
            getAxisTitles();
            fetchPlotsData();
            fetchMutationType();
            formDataSet();

            //View Construction
            initSVG();
            initAxis();
            if (userSelection.plots_type.indexOf("copy_no") !== -1) {
                if (dataIsDiscretized()) {
                    drawDiscretizedAxis();
                    drawDiscretizedPlots();
                    drawBoxPlots();
                } else {
                    drawContinuousAxis();
                    drawLog2Plots();
                }
            } else {
                drawContinuousAxis();
                drawContinuousPlots();
            }
            drawQtips();
            drawGlyphs();
            drawAxisTitles();
        }
    };
}());
