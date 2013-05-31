//Global Variables
var gene,
    case_set = [],
    mutations = [],
    mutations_id = [],
    copy_no = [],
    mrna = [],
    rppa = [],
    dna_methylation = [],
    genetic_profile_mutations = [],
    genetic_profile_mrna = [],
    genetic_profile_copy_no = [],
    genetic_profile_rppa = [],
    genetic_profile_dna_methylation = [];

var plot_type_list = [
    ["mrna_vs_copy_no", "mRNA vs. Copy Number"],
    ["mrna_vs_dna_mythelation", "mRNA vs. DNA Methylation"],
    ["rppa_protein_level_vs_mrna", "RPPA Protein Level vs. mRNA"]
];

var data_type_copy_no, data_type_mrna, data_type_dna_methylation, data_type_rppa, data_type_mutations;

//Utils Functions
function findIndex(Str, Exp) {
    for (var i = 0; i< Exp.length; i++) {
        if ( Str == Exp[i][0] ) {
            return i;
        }
    }
    return -1;
}
function seachIndexBottom(arr, ele) {
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
}
function seachIndexTop(arr, ele) {
    for( var i = 0; i < arr.length; i++) {
        if (ele <= arr[i]) {
            return i;
        } else {
            continue;
        }
    }
    return arr.length - 1;
}
function loadSVG() {
    var mySVG = document.getElementById("plots_tab");
    var svgDoc = mySVG.getElementsByTagName("svg");
    var tmp1 = new XMLSerializer();
    var tmp2 = tmp1.serializeToString(svgDoc[0]);
    return tmp2;
}
function copyData(desArray, oriArray) {
    var desArrayIndex = 0;
    for ( var tmpIndex = 0; tmpIndex < oriArray.length; tmpIndex ++ ){
        if (oriArray[tmpIndex] != "" && oriArray[tmpIndex] != null ) {
            desArray[desArrayIndex] = oriArray[tmpIndex];
            desArrayIndex += 1;
        }
    }
}

//View Controllers
function initView() {
    fetchFrameData();
    drawSideBar();
    fetchPlotsData();
    var tmp_axis_title_result = fetchAxisTitle();
    $('#plots_tab').empty();
    drawScatterPlots(copy_no, mrna, mutations, tmp_axis_title_result[0], tmp_axis_title_result[1], 1, mutations, mutations_id, case_set);
    $('#img_center').empty();
    $('#img_center').append(gene + ": mRNA Expression v. CNA ");
}
function updateScatterPlots() {
    fetchPlotsData();
    var axisTitles = fetchAxisTitle();
    var xLegend = axisTitles[0];
    var yLegend = axisTitles[1];
    var tmp_plot_type = document.getElementById("plot_type").value;
    if (tmp_plot_type == plot_type_list[0][0]) {    //"mrna_vs_copy_no"
        drawScatterPlots(copy_no, mrna, mutations, xLegend, yLegend, 1, mutations, mutations_id, case_set);
        $('#img_center').empty();
        $('#img_center').append(gene + ": mRNA Expression v. CNA ");
    } else if (tmp_plot_type == plot_type_list[1][0]) {  //"mrna_vs_dna_mythelation"
        drawScatterPlots(dna_methylation, mrna, copy_no, xLegend, yLegend, 2, mutations, mutations_id, case_set);
        $('#img_center').empty();
        $('#img_center').append(gene + ": mRNA Expression v. DNA Methylation ");
    } else if (tmp_plot_type == plot_type_list[2][0]) {  //"rppa_protein_level_vs_mrna"
        drawScatterPlots(mrna, rppa, copy_no, xLegend, yLegend, 3, mutations, mutations_id, case_set);
        $('#img_center').empty();
        $('#img_center').append(gene + ": RPPA protein level v. mRNA Expression ");
    }
}

//Data Retriving Functions
function fetchFrameData() {
    var tmpArr = [];
    var tmp_lines = [];
    var tmp_columns = [];
    $.ajax({
        url: "webservice.do?cmd=getGeneticProfiles&cancer_study_id=" + cancer_study_id,
        type: 'get',
        dataType: 'text',
        async: false,
        success: function (data) {
            if (data != "") {
                tmp_lines = data.split(/\n/);
                for (var j = 2; j < tmp_lines.length; j++) {
                    if (tmp_lines[j] != "") {
                        tmp_columns = tmp_lines[j].split(/\t/);
                        if (tmp_columns[4] == "MUTATION_EXTENDED") {
                            tmpArr = [tmp_columns[0], tmp_columns[1]];
                            genetic_profile_mutations.push(tmpArr);
                        } else if (tmp_columns[4] == "MRNA_EXPRESSION") {
                            tmpArr = [tmp_columns[0], tmp_columns[1]];
                            genetic_profile_mrna.push(tmpArr);
                        } else if (tmp_columns[4] == "COPY_NUMBER_ALTERATION") {
                            tmpArr = [tmp_columns[0], tmp_columns[1]];
                            genetic_profile_copy_no.push(tmpArr);
                        } else if (tmp_columns[4] == "METHYLATION") {
                            tmpArr = [tmp_columns[0], tmp_columns[1]];
                            genetic_profile_dna_methylation.push(tmpArr);
                        } else if (tmp_columns[4] == "PROTEIN_ARRAY_PROTEIN_LEVEL") {
                            tmpArr = [tmp_columns[0], tmp_columns[1]];
                            genetic_profile_rppa.push(tmpArr);
                        }
                    }
                }
            } else {
                alert("ERROR Fetching Data.");
            }
        }
    });
}
function fetchPlotsData() {
    var result_set = new Array(5);
    for ( var i = 0 ; i< result_set.length; i++) {
        result_set[i] = new Array();
    }
    //Get relevant parameters
    gene = document.getElementById("genes").value;
    data_type_copy_no = document.getElementById("data_type_copy_no").value;
    data_type_mrna = document.getElementById("data_type_mrna").value;
    if (genetic_profile_mutations != "") {
        data_type_mutations = genetic_profile_mutations[0][0];
    }
    if (genetic_profile_rppa != "") {
        data_type_rppa = genetic_profile_rppa[0][0];
    }
    if (genetic_profile_dna_methylation != "") {
        data_type_dna_methylation = genetic_profile_dna_methylation[0][0];
    }

    //Calling web APIs
    var urls;
    var url_base = "webservice.do?cmd=getProfileData&case_set_id=" + case_set_id + "&gene_list=" + gene + "&genetic_profile_id=";
    urls = [
        url_base + data_type_mutations, //mutations
        url_base + data_type_copy_no, //copy no
        url_base + data_type_mrna, //mrna
        url_base + data_type_rppa, //rppa
        url_base + data_type_dna_methylation //dna methylation
    ];
    for (var i = 0; i < urls.length; i++) {
        $.ajax({
            url: urls[i],
            type: 'get',
            dataType: 'text',
            async: false,
            success: function (data) {
                if (data != "") {
                    if (data.indexOf("No genetic profile available") == -1) {  //Profile Exists
                        tmp_sections = data.split(/\n/);
                        //Get Case Set
                        tmp_case_set = String(tmp_sections[3]).trim().split(/\s+/);
                        for (var j = 0; j < tmp_case_set.length - 2; j++) {
                            case_set[j] = tmp_case_set[j + 2];
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

    //Distributing Data
    copyData(mutations, result_set[0]);
    copyData(copy_no, result_set[1]);
    copyData(mrna, result_set[2]);
    copyData(rppa, result_set[3]);
    copyData(dna_methylation, result_set[4]);

    //Map Mutations --- replace mutation ID w/ mutations type
    var mutationMap = {};
    for (var i = 0; i < case_set.length; i++) {
        mutationMap[case_set[i]] = "non";
    }
    $.ajax({
        url: "webservice.do?cmd=getMutationData&case_set_id=" + case_set_id + "&gene_list=" + gene + "&genetic_profile_id=" + cancer_study_id + "_mutations",
        type: 'get',
        dataType: 'text',
        async: false,
        success: function (data) {
            if (data != "") {
                if (data.indexOf("No genetic profile available") == -1) {  //Profile Exists
                    tmp_lines = data.split(/\n/);
                    //Over-write mutation type
                    for (var j = 3; j < tmp_lines.length; j++) {
                        var tmp_columns = tmp_lines[j].split(/\t/);
                        mutationMap[tmp_columns[2]] = tmp_columns[5];
                    }
                }
            } else {
                alert("ERROR Fetching Extended Mutation Data.");
            }
        }
    });
    for (var k = 0; k < case_set.length; k++) {
        mutations_id[k] = mutations[k]; //Keep mutations ID info
        mutations[k] = mutationMap[case_set[k]];
    }
}
function fetchAxisTitle() {
    var xLegend = "";
    var yLegend = "";
    var tmp_plot_type = document.getElementById("plot_type").value;
    if (tmp_plot_type == plot_type_list[0][0]) {    //"mrna_vs_copy_no"
        var tmp_xLegend_index = findIndex(document.getElementById("data_type_copy_no").value, genetic_profile_copy_no);
        var tmp_yLegend_index = findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);
        xLegend = gene + "," + genetic_profile_copy_no[tmp_xLegend_index][1];
        yLegend = gene + "," + genetic_profile_mrna[tmp_yLegend_index][1];
    } else if (tmp_plot_type == plot_type_list[1][0]) {  //"mrna_vs_dna_mythelation"
        var tmp_yLegend_index = findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);
        if (genetic_profile_dna_methylation != "") {
            xLegend = gene + "," + genetic_profile_dna_methylation[0][1];
        }
        yLegend = gene + "," + genetic_profile_mrna[tmp_yLegend_index][1];
    } else if (tmp_plot_type == plot_type_list[2][0]) {  //"rppa_protein_level_vs_mrna"
        var tmp_xLegend_index = findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);
        xLegend = gene + "," + genetic_profile_mrna[tmp_xLegend_index][1];
        if (genetic_profile_rppa != "") {
            yLegend = gene + "," + genetic_profile_rppa[0][1];
        }
    }
    return [ xLegend, yLegend ];
}

//Utils functions for Plotting
function prepDataSet(type, xData, yData, zData, mutations, case_set, mutations_id, dataset) {
    //TODO : First 4 elements always got skipped
    dataset.length = 0;
    var index = 4;
    if (type == 1) {
        for( var i = 0; i<xData.length; i++) {
            if ((xData[i] == "NaN") || (yData[i] == "NaN") || (xData[i] == "NA") || (yData[i] == "NA")){
                continue;
            }
            //TODO: Mutations Mapping
            if ((zData[i] == "Frame_Shift_Del")||(zData[i] == "Frame_Shift_Ins")) {
                zData[i] = "frameshift";
            } else if ((zData[i] == "In_Frame_Del")||(zData[i] == "In_Frame_Ins")) {
                zData[i] = "in_frame";
            } else if ((zData[i] == "Missense_Mutation")||(zData[i] == "Missense")) {
                zData[i] = "missense";
            } else if ((zData[i] == "Nonsense_Mutation")||(zData[i] == "Nonsense")) {
                zData[i] = "nonsense";
            } else if ((zData[i] == "Splice_Site")||(zData[i] == "Splice_Site_SNP")) {
                zData[i] = "splice";
            } else if (zData[i] == "NonStop_Mutation") {
                zData[i] = "nonstop";
            } else if (zData[i] == "Translation_Start_Site") {
                zData[i] = "nonstart";
            } else {
                zData[i] = "non";
            }
            dataset[index] = [xData[i], yData[i], zData[i], mutations[i], case_set[i], mutations_id[i]];
            index += 1;
        }
    } else if (type == 2) {
        for( var i = 0; i<xData.length; i++) {
            if ((xData[i] == "NaN") || (yData[i] == "NaN") || (xData[i] == "NA") || (yData[i] == "NA")) {
                continue;
            }
            dataset[index] = [xData[i], yData[i], zData[i], mutations[i], case_set[i], mutations_id[i]];
            index += 1;
        }
    } else if (type == 3) {
        for( var i = 0; i<xData.length; i++) {
	    if ((xData[i] == "NaN") || (yData[i] == "NaN") || (xData[i] == "NA") || (yData[i] == "NA")) { 
                continue;
            }
            dataset[index] = [xData[i], yData[i], zData[i], mutations[i], case_set[i], mutations_id[i]];
            index += 1;
        }
    }
}
function analyseData(xData, yData){
    var tmp_xData = [];
    var tmp_xIndex = 0;
    var tmp_yData = [];
    var tmp_yIndex = 0;
    for (var j=0; j< xData.length; j++){
        if (xData[j] != "NaN" && yData[j] != "NaN" && xData[j] != "NA" && yData[j] != "NA") {
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
        'min_x': min_x,
        'max_x': max_x,
        'edge_x': edge_x,
        'min_y': min_y,
        'max_y': max_y,
        'edge_y': edge_y
    };
}
function addBoxPlots(svg, type, xData, yData, min_x, max_x, xScale, yScale){
    if (type == 1 && data_type_copy_no.indexOf("log2") == -1 && (data_type_copy_no.indexOf("gistic") != -1 || data_type_copy_no.indexOf("cna") != -1 || data_type_copy_no.indexOf("CNA") != -1)) {
        for (var i = min_x ; i < max_x + 1; i++) {
            var top;
            var bottom;
            var quan1;
            var quan2;
            var mean;
            var IQR;
            var scaled_y_arr=[];
            var tmp_y_arr = [];
            //Find the middle line for one box plot
            var midLine = xScale(i);
            //Find the max/min y value with certain x value;
            var index_tmp_y_data_array = 0;
            for (var j = 0; j < yData.length; j++) {
                if (yData[j] != "NaN" && xData[j] != "NaN" && yData[j] != "NA" && xData[j] != "NA" && xData[j] == i) {
                    tmp_y_arr[index_tmp_y_data_array] = parseFloat(yData[j]);
                    index_tmp_y_data_array += 1;
                }
            }
            tmp_y_arr.sort(function(a,b){return a-b});
            if (tmp_y_arr.length == 0) {
                //TODO: error handle (empty dataset)
            } else if (tmp_y_arr.length == 1) {
                mean = yScale(tmp_y_arr[0]);
                svg.append("line")
                    .attr("x1", midLine-30)
                    .attr("x2", midLine+30)
                    .attr("y1", mean)
                    .attr("y2", mean)
                    .attr("stroke-width", 1)
                    .attr("stroke", "grey");
            } else {
                if (tmp_y_arr.length == 2) {
                    mean = yScale((tmp_y_arr[0] + tmp_y_arr[1])/2);
                    quan1 = bottom = yScale(tmp_y_arr[0]);
                    quan2 = top = yScale(tmp_y_arr[1]);
                    IQR = Math.abs(quan2 - quan1);
                } else {
                    var yl = tmp_y_arr.length;
                    if (yl % 2 == 0) {
                        mean = yScale((tmp_y_arr[(yl/2)-1] + tmp_y_arr[yl/2])/2);
                        if (yl % 4 == 0) {
                            quan1 = yScale((tmp_y_arr[(yl/4)-1] + tmp_y_arr[yl/4])/2);
                            quan2 = yScale((tmp_y_arr[(3*yl/4)-1] + tmp_y_arr[3*yl/4])/2);
                        } else {
                            quan1 = yScale(tmp_y_arr[Math.floor(yl/4)]);
                            quan2 = yScale(tmp_y_arr[Math.floor(3*yl/4)]);
                        }
                    } else {
                        mean = yScale(tmp_y_arr[Math.floor(yl/2)]);
                        var tmp_yl = Math.floor(yl/2) + 1;
                        if ( tmp_yl % 2 == 0) {
                            quan1 = yScale((tmp_y_arr[tmp_yl/2 - 1] + tmp_y_arr[tmp_yl/2])/2);
                            quan2 = yScale((tmp_y_arr[(3 * tmp_yl/2) - 2] + tmp_y_arr[(3*tmp_yl/2)-1])/2);
                        } else {
                            quan1 = yScale(tmp_y_arr[Math.floor(tmp_yl/2)]);
                            quan2 = yScale(tmp_y_arr[tmp_yl - 1 + Math.floor(tmp_yl/2)]);
                        }
                    }
                    for (var k = 0 ; k < tmp_y_arr.length ; k++) {
                        scaled_y_arr[k] = parseFloat(yScale(tmp_y_arr[k]));
                    }
                    scaled_y_arr.sort(function(a,b){return a-b});
                    IQR = Math.abs(quan2 - quan1);
                    var index_top = seachIndexTop(scaled_y_arr, (quan2-1.5*IQR));
                    top = scaled_y_arr[index_top];
                    var index_bottom = seachIndexBottom(scaled_y_arr, (quan1+1.5*IQR));
                    bottom = scaled_y_arr[index_bottom];
                }
                drawBoxPlots(svg, midLine, top, bottom, quan1, quan2, mean, IQR);
            }
        }
    }
}
function addAxisText(svg, type, xAxis, yAxis, min_x, max_x){
    if (type == 1 && data_type_copy_no.indexOf("log2") == -1 && ( data_type_copy_no.indexOf("gistic") != -1 || data_type_copy_no.indexOf("cna") != -1 || data_type_copy_no.indexOf("CNA") != -1)){
        var textSet = ["Homdel", "Hetloss", "Diploid", "Gain", "Amp"];
        var ticksTextSet = [];
        var tmp_ticks_text_index = 0;
        for (var i = min_x + 2; i < max_x + 3; i++) {
            ticksTextSet[tmp_ticks_text_index] = textSet[i];
            tmp_ticks_text_index += 1;
        }
        svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 520)")
            .call(xAxis.ticks(ticksTextSet.length))
            .selectAll("text")
            .data(ticksTextSet)
            .style("font-family", "sans-serif")
            .style("font-size", "11px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black")
            .text(function(d){return d});
    } else {
        svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 520)")
            .call(xAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "11px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
    }
    svg.append("g")
        .style("stroke-width", 2)
        .style("fill", "none")
        .style("stroke", "grey")
        .style("shape-rendering", "crispEdges")
        .attr("transform", "translate(0, 20)")
        .call(xAxis.orient("bottom").ticks(0));
    svg.append("g")
        .style("stroke-width", 2)
        .style("fill", "none")
        .style("stroke", "grey")
        .style("shape-rendering", "crispEdges")
        .attr("transform", "translate(100, 0)")
        .call(yAxis)
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
        .call(yAxis.orient("left").ticks(0));
}
//Drawing Functions
function drawSideBar() {
    //Plot Type
    for ( var m = 0; m < plot_type_list.length; m++) {
        $('#plot_type').append("<option value='" + plot_type_list[m][0] + "'>" + plot_type_list[m][1] + "</option>");
    }
    //Copy Number Type
    for ( var j = 0; j < genetic_profile_copy_no.length; j++ ) {
        $('#data_type_copy_no').append("<option value='" + genetic_profile_copy_no[j][0] + "'>" + genetic_profile_copy_no[j][1] + "</option>");
    }
    //mRNA Type
    for ( var k = 0; k < genetic_profile_mrna.length; k++ ) {
        $('#data_type_mrna').append("<option value='" + genetic_profile_mrna[k][0] + "'>" + genetic_profile_mrna[k][1] + "</option>");
    }
}
function drawBoxPlots(svg, midLine, topLine, bottomLine, quan1, quan2, mean, IQR) {
    //Rectangle
    svg.append("rect")
        .attr("x", midLine-40)
        .attr("y", quan2)
        .attr("width", 80)
        .attr("height", IQR)
        .attr("fill", "none")
        .attr("stroke-width", 1)
        .attr("stroke", "#BDBDBD");
    //meanLine
    svg.append("line")
        .attr("x1", midLine-40)
        .attr("x2", midLine+40)
        .attr("y1", mean)
        .attr("y2", mean)
        .attr("stroke-width", 1)
        .attr("stroke", "#BDBDBD");
    //topLine
    svg.append("line")
        .attr("x1", midLine-30)
        .attr("x2", midLine+30)
        .attr("y1", topLine)
        .attr("y2", topLine)
        .attr("stroke-width", 1)
        .attr("stroke", "#BDBDBD");
    //bottomLine
    svg.append("line")
        .attr("x1", midLine-30)
        .attr("x2", midLine+30)
        .attr("y1", bottomLine)
        .attr("y2", bottomLine)
        .attr("stroke", "#BDBDBD")
        .style("stroke-width", 1);
    //Top Whisker
    svg.append("line")
        .attr("x1", midLine)
        .attr("x2", midLine)
        .attr("y1", quan1)
        .attr("y2", bottomLine)
        .attr("stroke", "#BDBDBD")
        .attr("stroke-width", 1);
    //Bottom Whisker
    svg.append("line")
        .attr("x1", midLine)
        .attr("x2", midLine)
        .attr("y1", quan2)
        .attr("y2", topLine)
        .attr("stroke", "#BDBDBD")
        .style("stroke-width", 1);
}
function drawScatterPlots(xData, yData, zData, xLegend, yLegend, type, mutations, mutations_id, case_set) {

    var dataset = [];
    
    $('#plots_tab').empty();
    var w = 700;
    var h = 600;
    var svg = d3.select("#plots_tab")
        .append("svg")
        .attr("width", w)
        .attr("height", h);

    prepDataSet(type, xData, yData, zData, mutations, case_set, mutations_id, dataset);

    var tmp_results = analyseData(xData, yData);
    var min_x = tmp_results.min_x;
    var max_x = tmp_results.max_x;
    var edge_x = tmp_results.edge_x;
    var min_y = tmp_results.min_y;
    var max_y = tmp_results.max_y;
    var edge_y = tmp_results.edge_y;

    //Define scale functions
    //TODO: enhencement -- can't return xScale/yScale.
    if ( type == 2 ){ 
        //Fix the range for methylation view to from 0 to 1
	var xScale = d3. scale.linear()
		.domain([-0.02, 1])
		.range([100,600]);
    } else {
	var xScale = d3.scale.linear()
        	.domain([min_x - edge_x, max_x + edge_x])
        	.range([100, 600]);
    }
	    var yScale = d3.scale.linear()
        .domain([min_y - edge_y, max_y + edge_y])
        .range([520, 20]);
    //Define Axis
    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient("bottom")
    var yAxis = d3.svg.axis()
        .scale(yScale)
        .orient("left");

    addAxisText(svg, type, xAxis, yAxis, min_x, max_x);
    addBoxPlots(svg, type, xData, yData, min_x, max_x, xScale, yScale);

    //Create SVG dots
    var symbol = ["triangle-down", "diamond", "triangle-up", "square", "cross", "triangle-up", "circle"];
    var mutationTypes = ["frameshift", "nonsense", "splice", "in_frame", "nonstart", "nonstop", "missense"];
    var mutationFillTypes = ["#1C1C1C", "#1C1C1C", "#DF7401", "#DF7401", "#DF7401", "#1C1C1C", "#DF7401"];
    var gisticStrokeTypes = ["#00008B", "#00BFFF", "#000000", "#FF69B4", "#FF0000"];
    var gisticLegendText = ["Homdel", "Hetloss",  "Gain", "Amp", "Mutated", "Normal"];
    var gisticPopUpText = ["Homdel", "Hetloss", "Diploid", "Gain", "Amp"];
    var gisticLegendStrokeTypes = ["#00008B", "#00BFFF", "#FF69B4", "#FF0000", "none", "#000000"];
    var gisticLegendFillTypes = ["none", "none", "none", "none", "orange", "none"];

	
	//Sort dataset to paint the mutated plots last to make them prominent 
	var non_mutated_data = [];
	var mutated_data= [];
	var tmp_dataset = [];
	dataset.forEach (function(entry) {
		if (entry[3] != "non") {
			mutated_data.push(entry);		
		} else {
			non_mutated_data.push(entry);
		}
	});
	non_mutated_data.forEach (function(entry) {
		tmp_dataset.push(entry);
	});
	mutated_data.forEach (function(entry) {
		tmp_dataset.push(entry);
	});
	dataset = tmp_dataset;

	//----------------- Plot dots for Putative Copy No VS. mRNA view (with data noise)
    if ( type == 1 ) {
        //Define noise level
        var ramRatio = 0;
        if ((data_type_copy_no.indexOf("gistic") != -1 || data_type_copy_no.indexOf("cna") != -1 || data_type_copy_no.indexOf("CNA") != -1) && data_type_copy_no.indexOf("log2") == -1) {
            ramRatio = 20;
        }

	svg.selectAll("path")
            .data(dataset)
            .enter()
            .append("svg:path")
            .attr("transform", function(d) { return "translate(" + (xScale(d[0]) + ((Math.random() * (ramRatio)) - (ramRatio/2))) + ", " + yScale(d[1]) + ")";})
            .attr("d", d3.svg.symbol()
                .size( function(d) {
                    switch (d[2]) {
                        case "non" : return 15;
                        default : return 25;
                    }
                })
                .type( function (d) {
                    for(var i = 0; i < mutationTypes.length; i++) {
                        if (d[2] == mutationTypes[i]) {
                            return symbol[i];
                        }
                    }
                    return "circle";
                })
            )
            .attr("fill", function(d) {
                for(var i = 0; i < mutationTypes.length; i++) {
                    if (d[2] == mutationTypes[i]) {
                        return mutationFillTypes[i];
                    }
                }
                return "#00AAF8";
            })
            .attr("stroke", function(d) {
                switch (d[2]) {
                    case "non": return "#0089C6";
                    default: return "#B40404";
                }
            });
        //Making Qtips
	//Format Numbers 
        svg.selectAll('path').each(function(d, i) {
            $(this).qtip({
                content: {text: 'qtip failed'},
                events: {
                    render: function(event, api) {
                        var content = "<font size='2'>";
			if ((data_type_copy_no.indexOf("gistic") != -1 || data_type_copy_no.indexOf("CNA") != -1 || data_type_copy_no.indexOf("cna") != -1) && data_type_copy_no.indexOf("log2") == -1) {
				content += "mRNA: <strong>" + parseFloat(d[1]).toFixed(3) + "</strong><br>";
			} else {
				content += "CNA: <strong>" + parseFloat(d[0]).toFixed(3) + "</strong><br>" + 
					"mRNA: <strong>" + parseFloat(d[1]).toFixed(3) + "</strong><br>";	
			}
			content += "Case ID: <strong><a href='tumormap.do?case_id=" + d[4] + "&cancer_study_id=" + cancer_study_id + "'>" + d[4] + '</a></strong><br>';
                        if (d[3] != 'non') {  //Mutation Annotation only for mutated plots
                            content = content + "Mutation: " + "<strong>" + d[5] + "</strong>(" + d[3] + ")";
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

        //--------------- Plot dots for other views
    } else {
        svg.selectAll("path")
            .data(dataset)
            .enter()
            .append("svg:path")
            .attr("transform", function(d) { return "translate(" + xScale(d[0]) + ", " + yScale(d[1]) + ")";})
            .attr("d", d3.svg.symbol()
                .size(35)
                .type( function (d) {
                    switch (d[2]) {
                        default: return "circle";
                    }
                }
            ))
            .attr("fill", function(d) {
                switch (d[3]) {
                    case "non" : return "none";
                    default: return "orange";
                }
            })
            .attr("stroke", function(d) {
                if ((d[2] == "0")&&(d[3] != "non")) {
                    return "none";
                } else {
                    switch (d[2]) {
                        case "-2": return gisticStrokeTypes[0];
                        case "-1": return gisticStrokeTypes[1];
                        case "0": return gisticStrokeTypes[2];
                        case "1": return gisticStrokeTypes[3];
                        case "2": return gisticStrokeTypes[4];
                        default: return "black";
                    }
                }
            })
            .attr("stroke-width", function(d) {
                switch(d[3]) {
                    case "non" : return "1";
                    default: return "1.5";
                }
            });
        svg.selectAll('path').each(function(d, i) {
            $(this).qtip({
                content: {text: 'qtip failed'},
                events: {
                    render: function(event, api) {
			var content = "<font size='2'>";
			if (type == 2) {	//mrna vs. dna methylation
				content += "Methylation: <strong>" + parseFloat(d[0]).toFixed(3) + "</strong><br>" + 
						"mRNA: <strong>" + parseFloat(d[1]).toFixed(3) + "</strong><br>";
			} else if (type == 3) { //rppa vs. mrna
				content += "mRNA: <strong>" + parseFloat(d[0]).toFixed(3) + "</strong><br>" + 
						"RPPA: <strong>" + parseFloat(d[1]).toFixed(3) + "</strong><br>";
			}
			content += "Case ID: <strong><a href='tumormap.do?case_id=" + d[4] + "&cancer_study_id=" + cancer_study_id + "'>" + d[4] + '</a></strong><br>';
                        if (d[3] != 'non') {  //Mutation Annotation only for mutated plots
                            content = content + "Mutation: " + "<strong>" + d[5] + "</strong>(" + d[3] + ")" + "<br>";
                        }
                        if (d[2] != 0 && d[2] != "NaN") {
                            var tmp_index = parseInt(d[2], 10) + 2;
			    content = content + "CNA: " + "<strong>" + gisticPopUpText[tmp_index] + "</strong>";
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

    //Error Handling -- empty dataset
    var xHasData = false;
    var yHasData = false;
    for (var j = 0; j < xData.length; j++) {
        if (xData[j] != "NaN") {
            xHasData = true;
        }
    }
    for (var k = 0; k < yData.length; k++) {
        if (yData[k] != "NaN") {
            yHasData = true;
        }
    }
    if ((yHasData == false) || (xHasData == false)) {
	
        var errorTxt1 = "Currently, " + gene + " has no data for -->";
	var errorTxt2;
	var errorTxt3 = "in selected cancer study."
	if (yHasData == false) {
		errorTxt2 = yLegend;
	} else if (xHasData == false) {
		errorTxt2 = xLegend;
	}
	errorTxt2 = errorTxt2.substring(gene.length+1);
        svg.append("text")
            .attr("x", 350)
            .attr("y", 280)
	    .attr("text-anchor", "middle")
            .attr("fill", "#DF3A01")
            .text(errorTxt1)
        svg.append("text")
            .attr("x", 350)
            .attr("y", 295)
	    .attr("text-anchor", "middle")
            .attr("fill", "#DF3A01")
	    .style("font-weight", "bold")
            .text(errorTxt2)
	svg.append("text")
	    .attr("x", 350)
	    .attr("y", 310)
	    .attr("text-anchor", "middle")
	    .attr("fill", "#DF3A01")
	    .text(errorTxt3);
    } else {
        //Create the legend
        if (type == 1) {  //Legend for Mutations
            var legend = svg.selectAll(".legend")
                .data(mutationTypes)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) { return "translate(610, " + (30 + i * 15) + ")"; });
            legend.append("path")
                .attr("width", 18)
                .attr("height", 16)
                .attr("d", d3.svg.symbol()
                    .size(30)
                    .type(function(d, i) {
                        switch(i) {
                            case i: return symbol[i];
                        }
                    })
                )
                .attr("fill", function (d, i) {
                    switch (i) {
                        case i: return mutationFillTypes[i];
                    }
                })
                .attr("stroke", function (d, i) {
                    switch (i) {
                        case i: return "#B40404";
                    }
                })
            legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d, i) {
                    switch (i) {
                        case i: return mutationTypes[i];
                    }
                });
        } else {  //Legend for Gistic plus mutation annotation
            var legend = svg.selectAll(".legend")
                .data(gisticLegendText)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) { return "translate(610, " + (30 + i * 15) + ")"; })
            legend.append("path")
                .attr("width", 18)
                .attr("height", 18)
                .attr("d", d3.svg.symbol()
                    .size(40)
                    .type(function(d, i) {
                        return "circle";
                    })
                )
                .attr("fill", function (d, i) {
                    switch(i) {
                        case i: return gisticLegendFillTypes[i];
                    }
                })
                .attr("stroke", function (d, i) {
                    switch (i) {
                        case i: return gisticLegendStrokeTypes[i];
                    }
                })
                .attr("stroke-width", function (d, i) {
                    return 1.1;
                })
            legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d, i) {
                    switch (i) {
                        case i: return gisticLegendText[i];
                    }
                });
        }
    }

    //Append axis Titles
    svg.append("text")
        .attr("class", "label")
        .attr("x", 350)
        .attr("y", 580)
        .style("text-anchor", "middle")
        .style("font-weight","bold")
        .text(xLegend);
    svg.append("text")
        .attr("class", "label")
        .attr("transform", "rotate(-90)")
        .attr("x", -270)
        .attr("y", 45)
        .style("text-anchor", "middle")
        .style("font-weight","bold")
        .text(yLegend);

}



