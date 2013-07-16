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

var mutations = [],
    mutations_id = [],
    copy_no = [],
    gistic_copy_no = [],
    log2_copy_no = [],
    mrna = [],
    rppa = [],
    dna_methylation = [],
    genetic_profile_mutations = [],
    genetic_profile_mrna = [],
    genetic_profile_copy_no = [],
    genetic_profile_rppa = [],
    genetic_profile_dna_methylation = [];

function PlotsConfig() {
    this.has_mrna = false;
    this.has_dna_methylation = false;
    this.has_rppa = false;
    this.has_copy_no = false;
    this.copy_no_type = null;
    this.mrna_type = null;
    this.dna_methylation_type = null;
    this.rppa_type = null;
    this.mutations_type = null;
    this.plotsType = {
        COPY_NUMBER: "1",
        METHYLATION: "2",
        RPPA: "3"
    };
    this.plot_type_list = [
        ["mrna_vs_copy_no", "mRNA vs. Copy Number"],
        ["mrna_vs_dna_mythelation", "mRNA vs. DNA Methylation"],
        ["rppa_protein_level_vs_mrna", "RPPA Protein Level vs. mRNA"]
    ];
}

var PlotsData = (function() {
    var gene;
    var case_set = [];
    var setGene = function(input_gene) {
        gene = input_gene;
    };
    var getGene = function(){
        return gene;
    };
    var setCaseSet = function(input_case_set) {
        case_set = input_case_set;
    }
    var getCaseSet = function() {
        return case_set;
    }
    var getSingleCase = function(index) {
        return case_set[index];
    }
    var setSingleCase = function(case_id, index) {
        case_set[index] = case_id;
    }
    var getCaseSetLength = function() {
        return case_set.length;
    }
    return {
        setGene: setGene,
        getGene: getGene,
        setCaseSet: setCaseSet,
        getCaseSet: getCaseSet,
        setSingleCase: setSingleCase,
        getSingleCase: getSingleCase,
        getCaseSetLength: getCaseSetLength,
    };
}());
var PlotsUtil = (function(){
    var findIndex = function(Str, Exp) {
        for (var i = 0; i< Exp.length; i++) {
            if ( Str == Exp[i][0] ) {
                return i;
            }
        }
        return -1;
    };
    var searchIndexBottom = function(arr, ele) {
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
    var searchIndexTop = function(arr, ele) {
        for( var i = 0; i < arr.length; i++) {
            if (ele <= arr[i]) {
                return i;
            } else {
                continue;
            }
        }
        return arr.length - 1;
    };
    var copyData = function(desArray, oriArray) {
        desArray.length = 0;
        var desArrayIndex = 0;
        for ( var tmpIndex = 0; tmpIndex < oriArray.length; tmpIndex ++ ){
            if (oriArray[tmpIndex] != "" && oriArray[tmpIndex] != null ) {
                desArray[desArrayIndex] = oriArray[tmpIndex];
                desArrayIndex += 1;
            }
        }
    };
    var toggleVisibilityShow = function(elemId) {
        var e = document.getElementById(elemId);
        e.style.display = 'block';
    };
    var toggleVisibilityHide = function(elemId) {
        var e = document.getElementById(elemId);
        e.style.display = 'none';
    };
    return {
        findIndex: findIndex,
        searchIndexBottom: searchIndexBottom,
        searchIndexTop: searchIndexTop,
        copyData: copyData,
        toggleVisibilityShow: toggleVisibilityShow,
        toggleVisibilityHide: toggleVisibilityHide
    };
}());

function FetchPlotsDataUtil() {}
FetchPlotsDataUtil.prototype.fetchFrameData = function() {
    var tmpArr = [];
    var tmp_lines = [];
    var tmp_columns = [];
    tmpArr.length = 0;
    tmp_lines.length = 0;
    tmp_columns.length = 0;
    genetic_profile_copy_no.length = 0;
    genetic_profile_rppa.length = 0;
    genetic_profile_dna_methylation.length = 0;
    genetic_profile_mrna.length = 0;
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
                            plotsConfig.has_mrna = true;
                        } else if (tmp_columns[4] == "COPY_NUMBER_ALTERATION") {
                            tmpArr = [tmp_columns[0], tmp_columns[1]];
                            genetic_profile_copy_no.push(tmpArr);
                            plotsConfig.has_copy_no = true;
                        } else if (tmp_columns[4] == "METHYLATION") {
                            tmpArr = [tmp_columns[0], tmp_columns[1]];
                            genetic_profile_dna_methylation.push(tmpArr);
                            plotsConfig.has_dna_methylation = true;
                        } else if (tmp_columns[4] == "PROTEIN_ARRAY_PROTEIN_LEVEL") {
                            tmpArr = [tmp_columns[0], tmp_columns[1]];
                            genetic_profile_rppa.push(tmpArr);
                            plotsConfig.has_rppa = true;
                        }
                    }
                }
            } else {
                alert("ERROR Fetching Data.");
            }
        }
    });
};

function arraymove(arr, fromIndex, toIndex) {
    element = arr[fromIndex];
    arr.splice(fromIndex, 1);
    arr.splice(toIndex, 0, element);
}

FetchPlotsDataUtil.prototype.fetchPlotsData = function() {
    var result_set = new Array(5);
    for ( var i = 0 ; i< result_set.length; i++) {
        result_set[i] = new Array();
    }
    //Get relevant parameters
    PlotsData.setGene(document.getElementById("genes").value);
    plotsConfig.copy_no_type = document.getElementById("data_type_copy_no").value;
    plotsConfig.mrna_type = document.getElementById("data_type_mrna").value;

    //plotsConfig.mutations_type = genetic_profile_mutations[0][0];
    plotsConfig.mutations_type = cancer_study_id + "_mutations";
    plotsConfig.rppa_type = document.getElementById("data_type_rppa").value;
    plotsConfig.dna_methylation_type = document.getElementById("data_type_dna_methylation").value;

    //Calling web APIs
    var url_base = "";
    if (case_set_id === "-1") {
        url_base = "webservice.do?cmd=getProfileData&case_ids_key=" +
            case_ids_key + "&gene_list=" + PlotsData.getGene() + "&genetic_profile_id=";
    } else {
        url_base = "webservice.do?cmd=getProfileData&case_set_id=" +
            case_set_id + "&gene_list=" + PlotsData.getGene() + "&genetic_profile_id=";
    }
    var types = [
        plotsConfig.mutations_type, //mutations
        plotsConfig.copy_no_type, //copy no
        plotsConfig.mrna_type, //mrna
        plotsConfig.rppa_type, //rppa
        plotsConfig.dna_methylation_type //dna methylation
    ];
    for (var i = 0; i < types.length; i++) {
        $.ajax({
            url: url_base + types[i],
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
                            PlotsData.setSingleCase(tmp_case_set[j+2], j);
                        }
                        //Get profile data (Filter the headers)
                        var profile_data = [];
                        var tmp_profile_data = String(tmp_sections[4]).trim().split(/\s+/);
                        for (var profileDataIndex = 0; profileDataIndex < tmp_profile_data.length; profileDataIndex++) {
                            profile_data[profileDataIndex] = tmp_profile_data[profileDataIndex + 2];
                        }
                        //Fill in Result Data Set
                        PlotsUtil.copyData(result_set[i], profile_data);
                    }
                } else {
                    alert("ERROR Fetching Data.");
                }
            }
        });
    }

    //Distributing Data
    PlotsUtil.copyData(mutations, result_set[0]);
    PlotsUtil.copyData(copy_no, result_set[1]);
    PlotsUtil.copyData(mrna, result_set[2]);
    PlotsUtil.copyData(rppa, result_set[3]);
    PlotsUtil.copyData(dna_methylation, result_set[4]);

    if (plotsConfig.copy_no_type.indexOf("gistic") != -1) {
        PlotsUtil.copyData(gistic_copy_no, copy_no);
    } else if (plotsConfig.copy_no_type.indexOf("log2") != -1) {
        PlotsUtil.copyData(log2_copy_no, copy_no);
    }

    //Map Mutations --- replace mutation ID w/ mutations type
    var mutationMap = {};
    for (var i = 0; i < PlotsData.getCaseSetLength(); i++) {
        mutationMap[PlotsData.getSingleCase(i)] = "non";
    }
    var url = "";
    if (case_set_id === "-1") {
        url = "webservice.do?cmd=getMutationData&case_ids_key=" +
            case_ids_key + "&gene_list=" + PlotsData.getGene() +
            "&genetic_profile_id=" + cancer_study_id + "_mutations";
    } else {
        url = "webservice.do?cmd=getMutationData&case_set_id=" +
            case_set_id + "&gene_list=" + PlotsData.getGene() +
            "&genetic_profile_id=" + cancer_study_id + "_mutations";
    }
    $.ajax({
        url: url,
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
    for (var k = 0; k < PlotsData.getCaseSetLength(); k++) {
        mutations_id[k] = mutations[k]; //Keep mutations ID info
        mutations[k] = mutationMap[PlotsData.getSingleCase(k)];
    }
}

//View Controllers
function ViewController() {}
ViewController.prototype.initView = function() {
    $('#img_center').hide();
    $('#plots_box').hide();
    fetchPlotsDataUtil.fetchFrameData();
    drawSideBar();
    this.updateSideBar();
    fetchPlotsDataUtil.fetchPlotsData();
    var tmp_axis_title_result = fetchAxisTitle();
    $('#plots_box').empty();
    drawScatterPlots(copy_no, mrna, mutations, tmp_axis_title_result[0], tmp_axis_title_result[1], plotsConfig.plotsType.COPY_NUMBER, mutations, mutations_id, PlotsData.getCaseSet());
    $('#img_center_text').empty();
    $('#img_center_text').append(PlotsData.getGene() + ": mRNA Expression v. CNA ");
    setTimeout(function() {
        $('#div-loading-image').empty();
        $('#plots_box').show();
        $('#img_center').show();
    }, 500);
}
ViewController.prototype.updateScatterPlots = function() {
    fetchPlotsDataUtil.fetchPlotsData();
    var axisTitles = fetchAxisTitle();
    var xLegend = axisTitles[0];
    var yLegend = axisTitles[1];
    var tmp_plot_type = document.getElementById("plot_type").value;
    if (tmp_plot_type == plotsConfig.plot_type_list[0][0]) {    //"mrna_vs_copy_no"
        this.updateSideBar();
        drawScatterPlots(copy_no, mrna, mutations, xLegend, yLegend, plotsConfig.plotsType.COPY_NUMBER, mutations, mutations_id, PlotsData.getCaseSet());
        $('#img_center_text').empty();
        $('#img_center_text').append(PlotsData.getGene() + ": mRNA Expression v. CNA ");
    } else if (tmp_plot_type == plotsConfig.plot_type_list[1][0]) {  //"mrna_vs_dna_mythelation"
        this.updateSideBar();
        drawScatterPlots(dna_methylation, mrna, gistic_copy_no, xLegend, yLegend, plotsConfig.plotsType.METHYLATION, mutations, mutations_id, PlotsData.getCaseSet());
        $('#img_center_text').empty();
        $('#img_center_text').append(PlotsData.getGene() + ": mRNA Expression v. DNA Methylation ");
    } else if (tmp_plot_type == plotsConfig.plot_type_list[2][0]) {  //"rppa_protein_level_vs_mrna"
        this.updateSideBar();
        drawScatterPlots(mrna, rppa, gistic_copy_no, xLegend, yLegend, plotsConfig.plotsType.RPPA, mutations, mutations_id, PlotsData.getCaseSet());
        $('#img_center_text').empty();
        $('#img_center_text').append(PlotsData.getGene() + ": RPPA protein level v. mRNA Expression ");
    }
}
ViewController.prototype.updateView = function() {
    $('#img_center').hide();
    $('#plots_box').hide();
    $('#div-loading-image').append("<img style='padding:200px;' src='images/ajax-loader.gif'>");
    setTimeout(function() {
        viewController.updateSideBar();
        viewController.updateScatterPlots();
        $('#div-loading-image').empty();
        $('#plots_box').show();
        $('#img_center').show();
    }, 500);
}
ViewController.prototype.updatePlots = function() {
    $('#img_center').hide();
    $('#plots_box').hide();
    $('#div-loading-image').append("<img style='padding:200px;' src='images/ajax-loader.gif'>");
    setTimeout(function() {
        viewController.updateScatterPlots();
        $('#div-loading-image').empty();
        $('#plots_box').show();
        $('#img_center').show();
    }, 500);
}
ViewController.prototype.updateSideBar = function() {  // hide/show menu item based on user selection
    var currentPlotType = $('#plot_type').val();
    if (currentPlotType.indexOf("copy_no") != -1) {
        PlotsUtil.toggleVisibilityShow("mrna_dropdown");
        PlotsUtil.toggleVisibilityShow("copy_no_dropdown");
        PlotsUtil.toggleVisibilityHide("dna_methylation_dropdown");
        PlotsUtil.toggleVisibilityHide("rppa_dropdown");
    } else if (currentPlotType.indexOf("dna_mythelation") != -1) {
        PlotsUtil.toggleVisibilityShow("mrna_dropdown");
        PlotsUtil.toggleVisibilityHide("copy_no_dropdown");
        PlotsUtil.toggleVisibilityShow("dna_methylation_dropdown");
        PlotsUtil.toggleVisibilityHide("rppa_dropdown");
    } else if (currentPlotType.indexOf("rppa") != -1) {
        PlotsUtil.toggleVisibilityShow("mrna_dropdown");
        PlotsUtil.toggleVisibilityHide("copy_no_dropdown");
        PlotsUtil.toggleVisibilityHide("dna_methylation_dropdown");
        PlotsUtil.toggleVisibilityShow("rppa_dropdown");
    }
}


//Utils functions for Plotting
function prepDataSet(type, xData, yData, zData, mutations, case_set, mutations_id, dataset) {
    dataset.length = 0;
    var index = 0;
    if (type == plotsConfig.plotsType.COPY_NUMBER) {
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
    } else if (type == plotsConfig.plotsType.METHYLATION) {
        var tmp_count = 0;
        for (var j = 0; j < xData.length; j++) {
            if (xData[j] != "NaN" && yData[j] != "NaN"){
                tmp_count ++;
            }
        }
        for ( var i = 0; i<xData.length; i++) {
            if ((xData[i] == "NaN") || (xData[i] == "NA") ||
                (yData[i] == "NaN") || (yData[i] == "NA") ||
                (zData[i] == "NaN") || (zData[i] == "NA")) {
                continue;
            }
            dataset[index] = [xData[i], yData[i], zData[i], mutations[i], case_set[i], mutations_id[i]];
            index += 1;
        }
    } else if (type == plotsConfig.plotsType.RPPA) {
        for ( var i = 0; i<xData.length; i++) {
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
    var posCount = min_x;
    if (isDiscretized(type)) {
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
                var midLine = xScale(posCount);
                posCount = posCount + 1;
                mean = yScale(tmp_y_arr[0]);
                svg.append("line")
                    .attr("x1", midLine-30)
                    .attr("x2", midLine+30)
                    .attr("y1", mean)
                    .attr("y2", mean)
                    .attr("stroke-width", 1)
                    .attr("stroke", "grey");
            } else {
                var midLine = xScale(posCount);
                posCount = posCount + 1;
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
                    var index_top = PlotsUtil.searchIndexTop(scaled_y_arr, (quan2-1.5*IQR));
                    top = scaled_y_arr[index_top];
                    var index_bottom = PlotsUtil.searchIndexBottom(scaled_y_arr, (quan1+1.5*IQR));
                    bottom = scaled_y_arr[index_bottom];
                }
                drawBoxPlots(svg, midLine, top, bottom, quan1, quan2, mean, IQR);
            }
        }
    }
}
function isDiscretized(type) {
    return (type == plotsConfig.plotsType.COPY_NUMBER &&
        plotsConfig.copy_no_type.indexOf("log2") == -1 &&
        (plotsConfig.copy_no_type.indexOf("gistic") != -1 || plotsConfig.copy_no_type.indexOf("cna") != -1 || plotsConfig.copy_no_type.indexOf("CNA") != -1));
}
function addAxisText(svg, type, xAxis, yAxis, min_x, max_x, textSet){
    if (isDiscretized(type)) {
        svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 520)")
            .attr('class', "plots-x-axis-class")
            .call(xAxis.ticks(textSet.length))
            .selectAll("text")
            .data(textSet)
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
            .attr('class', "plots-x-axis-class")
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
        .attr('class', "plots-y-axis-class")
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
function addAxisTitle(svg, xLegend, yLegend) {
    //Append axis Titles
    var axisTitleGroup = svg.append("svg:g");
    axisTitleGroup.append("text")
        .attr("class", "label")
        .attr("x", 350)
        .attr("y", 580)
        .style("text-anchor", "middle")
        .style("font-weight","bold")
        .text(xLegend);
    axisTitleGroup.append("text")
        .attr("class", "label")
        .attr("transform", "rotate(-90)")
        .attr("x", -270)
        .attr("y", 45)
        .style("text-anchor", "middle")
        .style("font-weight","bold")
        .text(yLegend);
}

function loadSVG() {

    var mySVG = document.getElementById("plots_box");
    var svgDoc = mySVG.getElementsByTagName("svg");
    var xmlSerializer = new XMLSerializer();
    var xml = xmlSerializer.serializeToString(svgDoc[0]);

    //Quick fix for the Batik display bug
    //TODO: debug/modify the library
    xml = xml.replace(/<text y="9" x="0" dy=".71em"/g, "<text y=\"19\" x=\"0\" dy=\".71em\"");
    xml = xml.replace(/<text x="-9" y="0" dy=".32em"/g, "<text x=\"-9\" y=\"3\" dy=\".32em\"");

    return xml;

}

function fetchAxisTitle() {
    var xLegend = "";
    var yLegend = "";
    var tmp_plot_type = document.getElementById("plot_type").value;
    if (tmp_plot_type == plotsConfig.plot_type_list[0][0]) {    //"mrna_vs_copy_no"
        var tmp_xLegend_index = PlotsUtil.findIndex(document.getElementById("data_type_copy_no").value, genetic_profile_copy_no);
        var tmp_yLegend_index = PlotsUtil.findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);
        xLegend = PlotsData.getGene() + ", " + genetic_profile_copy_no[tmp_xLegend_index][1];
        yLegend = PlotsData.getGene() + ", " + genetic_profile_mrna[tmp_yLegend_index][1];
    } else if (tmp_plot_type == plotsConfig.plot_type_list[1][0]) {  //"mrna_vs_dna_mythelation"
        var tmp_xLegend_index = PlotsUtil.findIndex(document.getElementById("data_type_dna_methylation").value, genetic_profile_dna_methylation);
        var tmp_yLegend_index = PlotsUtil.findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);
        xLegend = PlotsData.getGene() + ", " + genetic_profile_dna_methylation[tmp_xLegend_index][1];
        yLegend = PlotsData.getGene() + ", " + genetic_profile_mrna[tmp_yLegend_index][1];
    } else if (tmp_plot_type == plotsConfig.plot_type_list[2][0]) {  //"rppa_protein_level_vs_mrna"
        var tmp_xLegend_index = PlotsUtil.findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);
        var tmp_yLegend_index = PlotsUtil.findIndex(document.getElementById("data_type_dna_methylation").value, genetic_profile_dna_methylation);
        xLegend = PlotsData.getGene() + ", " + genetic_profile_mrna[tmp_xLegend_index][1];
        yLegend = PlotsData.getGene() + ", " + genetic_profile_rppa[tmp_yLegend_index][1];
    }
    return [xLegend, yLegend];
}

//Drawing Functions
function drawSideBar() {
    //Plot Type
    if ( plotsConfig.has_mrna && plotsConfig.has_copy_no) {
        $('#plot_type').append("<option value='" + plotsConfig.plot_type_list[0][0] + "'>" + plotsConfig.plot_type_list[0][1] + "</option>");
    }
    if ( plotsConfig.has_mrna && plotsConfig.has_dna_methylation) {
        $('#plot_type').append("<option value='" + plotsConfig.plot_type_list[1][0] + "'>" + plotsConfig.plot_type_list[1][1] + "</option>");
    }
    if ( plotsConfig.has_mrna && plotsConfig.has_rppa) {
        $('#plot_type').append("<option value='" + plotsConfig.plot_type_list[2][0] + "'>" + plotsConfig.plot_type_list[2][1] + "</option>");
    }

    //Data Type
    //Lay out all data types
    var j = 0;
    for ( j = 0; j < genetic_profile_copy_no.length; j++ ) {
        $('#data_type_copy_no').append("<option value='" + genetic_profile_copy_no[j][0] + "'>" + genetic_profile_copy_no[j][1] + "</option>");
    }
    for ( j = 0; j < genetic_profile_mrna.length; j++ ) {
        $('#data_type_mrna').append("<option value='" + genetic_profile_mrna[j][0] + "'>" + genetic_profile_mrna[j][1] + "</option>");
    }
    for ( j = 0; j < genetic_profile_dna_methylation.length; j++ ) {
        $('#data_type_dna_methylation').append("<option value='" + genetic_profile_dna_methylation[j][0] + "'>" + genetic_profile_dna_methylation[j][1] + "</option>");
    }
    for ( j = 0; j < genetic_profile_rppa.length; j++ ) {
        $('#data_type_rppa').append("<option value='" + genetic_profile_rppa[j][0] + "'>" + genetic_profile_rppa[j][1] + "</option>");
    }

    //Set Default mRNA settings: user selection, RNA Seq V2 (w/o z-scores), RNA Seq(w/o zscores), Z-scores
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
    $.each(geneticProfiles.split(/\s+/), function(index, value){
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

    //Set default copy no settings: user selection, discretized(gistic, rae), continuous
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
        if (this.value === userSelectedMrnaProfile){
            $(this).prop('selected', true);
            return false;
        }
    });

}



function drawBoxPlots(svg, midLine, topLine, bottomLine, quan1, quan2, mean, IQR) {
    var boxPlotsGroup = svg.append("svg:g");
    //Rectangle
    boxPlotsGroup.append("rect")
        .attr("x", midLine-40)
        .attr("y", quan2)
        .attr("width", 80)
        .attr("height", IQR)
        .attr("fill", "none")
        .attr("stroke-width", 1)
        .attr("stroke", "#BDBDBD");
    //meanLine
    boxPlotsGroup.append("line")
        .attr("x1", midLine-40)
        .attr("x2", midLine+40)
        .attr("y1", mean)
        .attr("y2", mean)
        .attr("stroke-width", 1)
        .attr("stroke", "#BDBDBD");
    //topLine
    boxPlotsGroup.append("line")
        .attr("x1", midLine-30)
        .attr("x2", midLine+30)
        .attr("y1", topLine)
        .attr("y2", topLine)
        .attr("stroke-width", 1)
        .attr("stroke", "#BDBDBD");
    //bottomLine
    boxPlotsGroup.append("line")
        .attr("x1", midLine-30)
        .attr("x2", midLine+30)
        .attr("y1", bottomLine)
        .attr("y2", bottomLine)
        .attr("stroke", "#BDBDBD")
        .style("stroke-width", 1);
    //Top Whisker
    boxPlotsGroup.append("line")
        .attr("x1", midLine)
        .attr("x2", midLine)
        .attr("y1", quan1)
        .attr("y2", bottomLine)
        .attr("stroke", "#BDBDBD")
        .attr("stroke-width", 1);
    //Bottom Whisker
    boxPlotsGroup.append("line")
        .attr("x1", midLine)
        .attr("x2", midLine)
        .attr("y1", quan2)
        .attr("y2", topLine)
        .attr("stroke", "#BDBDBD")
        .style("stroke-width", 1);
}
function drawScatterPlots(xData, yData, zData, xLegend, yLegend, type, mutations, mutations_id, case_set) {

    var dataset = [];


    $('#plots_box').empty();
    var w = 700;
    var h = 600;
    var svg = d3.select("#plots_box")
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
    if ( type == plotsConfig.plotsType.METHYLATION && plotsConfig.dna_methylation_type.indexOf("hm27") != -1 ){
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
        .orient("bottom");
    var yAxis = d3.svg.axis()
        .scale(yScale)
        .orient("left");


    //Create SVG dots
    var symbol = ["triangle-down", "diamond", "triangle-up", "square", "cross", "triangle-up", "circle"];
    var mutationTypes = ["frameshift", "nonsense", "splice", "in_frame", "nonstart", "nonstop", "missense"];
    var mutationFillTypes = ["#1C1C1C", "#1C1C1C", "#A4A4A4", "#DF7401", "#DF7401", "#1C1C1C", "#DF7401"];

    var gisticStrokeTypes = ["#00008B", "#00BFFF", "#000000", "#FF69B4", "#FF0000"];
    
    var gisticLegendText = ["Amp", "Gain", "Diploid", "Hetloss", "Homdel", "Mutated"];
    var gisticLegendStrokeTypes = ["#FF0000", "#FF69B4", "#000000", "#00BFFF", "#00008B", "none"];
    var gisticLegendFillTypes = ["none", "none", "none", "none", "none", "orange"];
    
    var gisticPopUpText = ["Homdel", "Hetloss", "Diploid", "Gain", "Amp"];


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
    var textSet = [];
    if ( type == plotsConfig.plotsType.COPY_NUMBER ) {

        //Define noise level
        if (isDiscretized(type)) {
            var ramRatio = 20;

            //Strictly order by gistic val : min -> max
            //TODO: improve
            var subDataSet = {
                Homdel : [],
                Hetloss : [],
                Diploid : [],
                Gain : [],
                Amp : []
            };

            for (var i = 0; i < dataset.length; i++) {
                var gisticVal = dataset[i][0];
                if (gisticVal === "-2") {
                    subDataSet.Homdel.push(dataset[i]);
                } else if (gisticVal === "-1") {
                    subDataSet.Hetloss.push(dataset[i]);
                } else if (gisticVal === "0") {
                    subDataSet.Diploid.push(dataset[i]);
                } else if (gisticVal === "1") {
                    subDataSet.Gain.push(dataset[i]);
                } else if (gisticVal === "2") {
                    subDataSet.Amp.push(dataset[i]);
                }
            }

            var countSubDataSets = 0;
            $.each(subDataSet, function(key, value) {
                if (subDataSet[key].length !== 0) {
                    countSubDataSets = countSubDataSets + 1;
                    textSet.push(key);
                }
            });

            //Redefine the axis
            if (countSubDataSets === 1) {
                xScale = d3.scale.linear()
                    .domain([(min_x - 0.2), (min_x + 0.2)])
                    .range([100, 600]);
            } else if (countSubDataSets === 2) {
                xScale = d3.scale.linear()
                    .domain([(min_x - 0.8), (min_x + countSubDataSets - 1 + 0.8)])
                    .range([100, 600]);
            } else if (countSubDataSets === 3) {
                xScale = d3.scale.linear()
                    .domain([min_x - 0.8, (min_x + countSubDataSets - 1) + 0.8])
                    .range([100, 600]);
            } else if (countSubDataSets === 4) {
                xScale = d3.scale.linear()
                    .domain([min_x - 0.6, (min_x + countSubDataSets - 1) + 0.6])
                    .range([100, 600]);
            } else if (countSubDataSets === 5) {
                xScale = d3.scale.linear()
                    .domain([min_x - 0.6, (min_x + countSubDataSets - 1) + 0.6])
                    .range([100, 600]);
            }
            xAxis = d3.svg.axis()
                .scale(xScale)
                .orient("bottom")

            addBoxPlots(svg, type, xData, yData, min_x, max_x, xScale, yScale);

            var dotsGroup = svg.append("svg:g");
            var posVal = min_x;
            $.each(subDataSet, function(key, value) {
                if (value.length !== 0) {
                    var subDotsGrp = dotsGroup.append("svg:g");
                    subDotsGrp.selectAll("path")
                        .data(value)
                        .enter()
                        .append("svg:path")
                        .attr("transform", function(d) { return "translate(" + (xScale(posVal) +
                            ((Math.random() * (ramRatio)) - (ramRatio/2))) +
                            ", " + yScale(d[1]) + ")";})
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
                        })
                        .attr("stroke-width", 1.2);

                    posVal += 1;
                }
            });
        } else {
            var dotsGroup = svg.append("svg:g");
            dotsGroup.selectAll("path")
                .data(dataset)
                .enter()
                .append("svg:path")
                .attr("transform", function(d) { return "translate(" + xScale(d[0]) +
                      ", " + yScale(d[1]) + ")";})
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
                })
                .attr("stroke-width", 1.2);
        }


        //Making Qtips
        dotsGroup.selectAll('path').each(function(d, i) {
            $(this).qtip({
                content: {text: 'qtip failed'},
                events: {
                    render: function(event, api) {
                        var content = "<font size='2'>";
                        if ((plotsConfig.copy_no_type.indexOf("gistic") != -1 || plotsConfig.copy_no_type.indexOf("CNA") != -1 || plotsConfig.copy_no_type.indexOf("cna") != -1) && plotsConfig.copy_no_type.indexOf("log2") == -1) {
                            content += "mRNA: <strong>" + parseFloat(d[1]).toFixed(3) + "</strong><br>";
                        } else {
                            content += "CNA: <strong>" + parseFloat(d[0]).toFixed(3) + "</strong><br>" +
                                "mRNA: <strong>" + parseFloat(d[1]).toFixed(3) + "</strong><br>";
                        }
                        content += "Case ID: <strong><a href='tumormap.do?case_id=" + d[4] + "&cancer_study_id=" + cancer_study_id + "'>" + d[4] + '</a></strong><br>';
                        if (d[3] != 'non') {  //Mutation Annotation only for mutated plots
                            var formattedMutationTypes = d[5].replace(",", ", ");
                            content = content + "Mutation: " + "<strong>" + formattedMutationTypes + "</strong>";
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
        var dotsGroup = svg.append("svg:g");
        dotsGroup.selectAll("path")
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
                    case "non" : return "yellow";
                    default: return "orange";
                }
            })
            .attr("fill-opacity", function(d) {
                switch (d[3]) {
                    case "non": return 0.0;
                    default: return 1.0;
                }
            })
            .attr("stroke", function(d) {
                    switch (d[2]) {
                        case "-2": return gisticStrokeTypes[0];
                        case "-1": return gisticStrokeTypes[1];
                        case "0": return gisticStrokeTypes[2];
                        case "1": return gisticStrokeTypes[3];
                        case "2": return gisticStrokeTypes[4];
                        default: return "black";
                    }
            })
            .attr("stroke-width", function(d) {
                if (d[2] === "0") {
                    return "1";
                } else {
                    switch(d[3]) {
                        case "non" : return "1";
                        default: return "1.2";
                    }
                }
	        });

        dotsGroup.selectAll('path').each(function(d, i) {
            $(this).qtip({
                content: {text: 'qtip failed'},
                events: {
                    render: function(event, api) {
                        var content = "<font size='2'>";
                        if (type == plotsConfig.plotsType.METHYLATION) {    //mrna vs. dna methylation
                            content += "Methylation: <strong>" + parseFloat(d[0]).toFixed(3) + "</strong><br>" +
                                "mRNA: <strong>" + parseFloat(d[1]).toFixed(3) + "</strong><br>";
                        } else if (type == plotsConfig.plotsType.RPPA) { //rppa vs. mrna
                            content += "mRNA: <strong>" + parseFloat(d[0]).toFixed(3) + "</strong><br>" +
                                "RPPA: <strong>" + parseFloat(d[1]).toFixed(3) + "</strong><br>";
                        }
                        content += "Case ID: <strong><a href='tumormap.do?case_id=" + d[4] + "&cancer_study_id=" + cancer_study_id + "'>" + d[4] + '</a></strong><br>';
                        if (d[3] != 'non') {  //Mutation Annotation only for mutated plots
                            var formattedMutationTypes = d[5].replace(",", ", ");
                            content = content + "Mutation: " + "<strong>" + formattedMutationTypes + "</strong>" + "<br>";
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

    addAxisText(svg, type, xAxis, yAxis, min_x, max_x, textSet);

    //Error Handling -- empty dataset
    var xHasData = false;
    var yHasData = false;
    var combineHasData = false;
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
    for (var m = 0; m < xData.length; m++) {
        if (xData[m] != "NaN" && yData[m] != "NaN") {
            combineHasData = true;
        }
    }
    if ((yHasData == false) || (xHasData == false) || (combineHasData == false)) {

        var errorTxt2 = PlotsData.getGene();
        var errorTxt3 = "in the selected cancer study."
        if (yHasData == false) {
            errorTxt1 = "There is no " +
                        yLegend.substring(PlotsData.getGene().length+1) +
                        " data for";
        } else if (xHasData == false) {
            errorTxt1 = "There is no " +
                        xLegend.substring(PlotsData.getGene().length+1) +
                        " data for";
        } else if (combineHasData == false) {
            errorTxt1 = "There is no data for the selected data type combination for";
        }
        //Get rid of "RPPA" in error message for rppa view
        errorTxt1 = errorTxt1.replace("RPPA", "");

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
        if (type == plotsConfig.plotsType.COPY_NUMBER) {  //Legend for Mutations
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
                .attr("stroke-width", 1.2);
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
    addAxisTitle(svg, xLegend, yLegend);
}

var viewController = new ViewController();
var plotsConfig = new PlotsConfig();
var fetchPlotsDataUtil = new FetchPlotsDataUtil();

