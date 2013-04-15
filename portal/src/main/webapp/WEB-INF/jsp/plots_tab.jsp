<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.servlet.GeneratePlots" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticAlterationType" %>

<script type="text/javascript" src="js/d3.v2.min.js"></script>
<!--link rel="stylesheet" type="text/css" href="css/bootstrap.min.css"-->

<style type="text/css">
    .axis path,
    .axis line {
        fill: none;
        stroke: grey;
        stroke-width:2;
        shape-rendering: crispEdges;
    }

    .axis text {
        font-family: sans-serif;
        font-size: 11px;
    }
</style>

<script>
    var gene;
    var case_set = [];
    var mutations = [];
    var copy_no = [];
    var mrna = [];
    var rppa = [];
    var dna_methylation = [];
    var result_set = new Array(5);
    for ( var i = 0 ; i< result_set.length; i++) {
        result_set[i] = new Array();
    }
    var genetic_profile_mutations = [
        "mutations", "Mutations"
    ];
    var genetic_profile_mrna = [
        ["mrna", "mRNA expression (microarray)"],
        ["mrna_median", "mRNA expression (all genes)"],
        ["mrna_median_Zscores", "mRNA Expression z-Scores (microarray)"],
        ["mrna_merged_median_Zscores", "mRNA/miRNA expression Z-scores (all genes)"],
        ["mrna_U133", "mRNA expression (U133 microarray only)"],
        ["rna_seq_v2_mrna", "mRNA expression (RNA Seq V2 RSEM)"],
        ["rna_seq_v2_mrna_median_Zscores", "mRNA Expression z-Scores (RNA Seq V2 RSEM)"],
        ["mirna", "microRNA expression"],
        ["mirna_median_Zscores", "microRNA expression Z-scores"],
        ["mrna_zbynorm", "mRNA Expression Z-Scores vs Normals"],
        ["mrna_znormal", "mRNA Z-scores vs normal fat"],
        ["mrna_outliers", "mRNA Expression Outliers"],
        ["mrna_outlier", "mRNA outliers"]
    ];
    var genetic_profile_copy_no = [
        ["gistic", "Putative copy-number alterations from GISTIC"],
        ["log2CNA", "Log2 copy-number values"],
        ["cna", "Putative copy-number alterations (RAE)"]
    ];
    var genetic_profile_rppa = [
        "RPPA_protein_level", "RPPA protein/phosphoprotein level"
    ];
    var genetic_profile_dna_methylation = [
        "methylation_hm27", "Methylation (HM27)"
    ];
    var plot_type_list = [
        ["mrna_vs_copy_no", "mRNA vs. Copy Number"],
        ["mrna_vs_dna_mythelation", "mRNA vs. DNA Methylation"],
        ["rppa_protein_level_vs_mrna", "RPPA Protein Level vs. mRNA"]
    ];

    var data_type_copy_no;
    var data_type_mrna;
    var xLegend;
    var yLegend;
</script>

<%
    String cancer_study_id = (String)request.getParameter("cancer_study_id");
    String case_set_id = (String)request.getParameter("case_set_id");
    String[] gene_list = ((String)request.getParameter("gene_list")).split("\\s+");
%>

<div class="section" id="plots">
    <table >
        <tr>
            <td>
                <table>
                    <tr><td style="border:2px solid #BDBDBD;width:320px;padding-left:20px;">
                        <h4 style='margin-top:-57px;'><font style="background-color: white">&nbsp;&nbsp;Plot Parameters&nbsp;&nbsp;</font></h4>
                        <br>
                        <b>Gene</b><br>
                        <select id='genes'>
                            <%
                                for (int i=0; i<gene_list.length; i++){
                                    out.println("<option value='" + gene_list[i] + "'>" + gene_list[i] + "</option>");
                                }
                            %>
                        </select>
                        <br><br>
                        <b>Plot Type</b><br>
                        <select id='plot_type'></select>
                        <br><br>
                        <b>Data Type</b><br>
                        <br>
                        mRNA
                        <br><select id='data_type_mrna'></select><br>
                        Copy Number
                        <br><select id='data_type_copy_no'></select>
                        <br><br><br>
                        <button onclick="generateScatterPlots()" style='width:80px;height:30px;border-radius:15px;'>
                            <font style='font-size: 14px;color:white;font-weight: 5px;'> GO >> </font>
                        </button>
                    </td>
                    <tr><td style='height:150px;'></td>
                </table>

            </td>
            <td>
                <div id="plots_tab"></div>
            </td>
        </tr>
    </table>
    <br>
</div>

<script>

function fetchData() {
    gene = document.getElementById("genes").value;
    data_type_copy_no = document.getElementById("data_type_copy_no").value;
    data_type_mrna = document.getElementById("data_type_mrna").value;

    var url_base = "webservice.do?cmd=getProfileData&case_set_id=<% out.print(case_set_id); %>&gene_list=" + gene + "&genetic_profile_id=";
    var urls = [
        url_base + "<% out.print(cancer_study_id); %>_" + genetic_profile_mutations[0], //mutations
        url_base + "<% out.print(cancer_study_id); %>_" + data_type_copy_no, //copy no
        url_base + "<% out.print(cancer_study_id); %>_" + data_type_mrna, //mrna
        url_base + "<% out.print(cancer_study_id); %>_" + genetic_profile_rppa[0], //rppa
        url_base + "<% out.print(cancer_study_id); %>_" + genetic_profile_dna_methylation[0] //dna methylation
    ];

    for (var i = 0; i < urls.length; i++) {
        $.ajax({
            url: urls[i],
            type: 'get',
            dataType: 'text',
            async: false,
            success: function(data) {
                if (data != "") {
                    if ( data.indexOf("No genetic profile available") == -1) {  //Profile Exists
                        tmp_sections = data.split(/\n/);
                        //Get Case Set
                        tmp_case_set = String(tmp_sections[3]).trim().split(/\s+/);
                        for (var j = 0 ; j < tmp_case_set.length; j ++ ) {
                            case_set[j] = tmp_case_set[j + 2];
                        }
                        //Get profile data (Filter the headers)
                        var profile_data = [];
                        var tmp_profile_data = String(tmp_sections[4]).trim().split(/\s+/);
                        for (var profileDataIndex = 0 ; profileDataIndex < tmp_profile_data.length; profileDataIndex ++ ) {
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
    //Distribute Data
    copyData(mutations, result_set[0]);
    copyData(copy_no, result_set[1]);
    copyData(mrna, result_set[2]);
    copyData(rppa, result_set[3]);
    copyData(dna_methylation, result_set[4]);
}

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
    fetchData();
    var tmp_axis_title_result = findAxisTitle();
    drawScatterPlots(copy_no, mrna, mutations, tmp_axis_title_result[0], tmp_axis_title_result[1], 1);
}

function findIndex(Str, Exp) {
    for (var i = 0; i< Exp.length; i++) {
        if ( Str == Exp[i][0] ) {
            return i;
        }
    }
    return -1;
}

function findAxisTitle() {
    var tmp_result = [];
    var xLegend;
    var yLegend;
    var tmp_plot_type = document.getElementById("plot_type").value;
    if (tmp_plot_type == plot_type_list[0][0]) {    //"mrna_vs_copy_no"
        var tmp_xLegend_index = findIndex(document.getElementById("data_type_copy_no").value, genetic_profile_copy_no);;
        var tmp_yLegend_index = findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);
        xLegend = genetic_profile_copy_no[tmp_xLegend_index][1];
        yLegend = genetic_profile_mrna[tmp_yLegend_index][1];
    } else if (tmp_plot_type == plot_type_list[1][0]) {  //"mrna_vs_dna_mythelation"
        var tmp_yLegend_index = findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);
        xLegend = genetic_profile_dna_methylation[1];
        yLegend = genetic_profile_mrna[tmp_yLegend_index][1];
    } else if (tmp_plot_type == plot_type_list[2][0]) {  //"rppa_protein_level_vs_mrna"
        var tmp_xLegend_index = findIndex(document.getElementById("data_type_mrna").value, genetic_profile_mrna);;
        xLegend = genetic_profile_mrna[tmp_xLegend_index][1];
        yLegend = genetic_profile_rppa[1];
    }
    tmp_result = [xLegend, yLegend];
    return tmp_result;
}

function generateScatterPlots() {
    fetchData();
    var axisTitles = [];
    axisTitles = findAxisTitle();
    var xLegend = axisTitles[0];
    var yLegend = axisTitles[1];
    var tmp_plot_type = document.getElementById("plot_type").value;
    if (tmp_plot_type == plot_type_list[0][0]) {    //"mrna_vs_copy_no"
        drawScatterPlots(copy_no, mrna, mutations, xLegend, yLegend, 1);
    } else if (tmp_plot_type == plot_type_list[1][0]) {  //"mrna_vs_dna_mythelation"
        drawScatterPlots(dna_methylation, mrna, copy_no, xLegend, yLegend, 2);
    } else if (tmp_plot_type == plot_type_list[2][0]) {  //"rppa_protein_level_vs_mrna"
        drawScatterPlots(mrna, rppa, copy_no, xLegend, yLegend, 3);
    }
}

function drawScatterPlots(xData, yData, zData, xLegend, yLegend, type) {
    //Create Canvas
    $('#plots_tab').empty();
    var w = 700;
    var h = 600;
    var svg = d3.select("#plots_tab")
            .append("svg")
            .attr("width", w)
            .attr("height", h);
    //Prepare DataSet
    var dataset = [];

    if (type == 1) {    //mrna_vs_copy_no
        var index = 0;
        for( var i = 0; i<xData.length; i++) {
            //Skip NaN entries
            if ((xData[i] == "NaN") || (yData[i] == "NaN")) {
                continue;
            }
            //TODO: Mutations Mapping
            if (zData[i] != "NaN") {
                zData[i] = "type1";
            } else {
                zData[i] = "non";
            }
            dataset[index] = [xData[i], yData[i], zData[i]];
            index += 1;
        }
    } else if (type == 2) {  // mrna_vs_dna_mythelation
        var index = 0;
        for( var i = 0; i<xData.length; i++) {
            //Skip NaN entries
            if ((xData[i] == "NaN") || (yData[i] == "NaN")) {
                continue;
            }
            dataset[index] = [xData[i], yData[i], zData[i]];
            index += 1;
        }
    } else if (type == 3) {  //rppa_protein_level_vs_mrna
        var index = 0;
        for( var i = 0; i<xData.length; i++) {
            //Skip NaN entries
            if ((xData[i] == "NaN") || (yData[i] == "NaN")) {
                continue;
            }
            dataset[index] = [xData[i], yData[i], zData[i]];
            index += 1;
        }
    }

    //-----------------tmp------//d3 min and max do NOT function well???
    var tmp_xData = [];
    var tmp_xIndex = 0;
    for (var j=0; j< xData.length; j++){
        if (xData[j] != "NaN") {
            tmp_xData[tmp_xIndex] = xData[j];
            tmp_xIndex += 1;
        }
    }
    var min_x = Math.min.apply(Math, tmp_xData);
    var max_x = Math.max.apply(Math, tmp_xData);
    var edge_x = (max_x - min_x) * 0.2;

    var tmp_yData = [];
    var tmp_yIndex = 0;
    for (var j=0; j< yData.length; j++){
        if (yData[j] != "NaN") {
            tmp_yData[tmp_yIndex] = yData[j];
            tmp_yIndex += 1;
        }
    }
    var min_y = Math.min.apply(Math, tmp_yData);
    var max_y = Math.max.apply(Math, tmp_yData);
    var edge_y = (max_y - min_y) * 0.2;
    //-------------------tmp---------------------------

    //Define scale functions
    var padding = 200;
    var xScale = d3.scale.linear()
            .domain([min_x - edge_x, max_x + edge_x])
            .range([100, 600]);
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

    //Create SVG Axis
    //Axis Annotation for GISTIC
    if (type == 1 && data_type_copy_no == "gistic") {
        var textSet = ["Homdel", "Hetloss", "Diploid", "Gain", "Amp"];
        var ticksTextSet = [];
        var tmp_ticks_text_index = 0;
        for (var i = min_x + 2; i < max_x + 3; i++) {
            ticksTextSet[tmp_ticks_text_index] = textSet[i];
            tmp_ticks_text_index += 1;
        }
        svg.append("g")
                .attr("class","axis")
                .attr("transform", "translate(0, 520)")
                .call(xAxis.ticks(ticksTextSet.length))
                .selectAll("text")
                .data(ticksTextSet)
                .style("text-anchor", "end")
                .attr("dx", "-.8em")
                .attr("dy", ".15em")
                .attr("transform", function(d) {return "rotate(-65)"})
                .text(function(d){return d})
    } else {
        svg.append("g")
                .attr("class","axis")
                .attr("transform", "translate(0, 520)")
                .call(xAxis);
    }

    svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(0, 20)")
            .call(xAxis.orient("bottom").ticks(0));

    svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(100, 0)")
            .call(yAxis)

    svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(600, 0)")
            .call(yAxis.orient("left").ticks(0));

    //Create SVG dots
    var symbol = d3.scale.ordinal().range(["circle", "triangle-up", "cross", "diamond", "square"]);
    var mutationTypes = ["non", "type1"];
    var gisticTypes = ["Homdel", "Hetloss", "Diploid", "Gain", "Amp"];
    var fillTypes = ["none", "#F78181", "#04B4AE", "#585858", "#FFBF00"];
    var strokeTypes = ["#2E9AFE", "#F78181", "#04B4AE", "#585858", "#FFBF00"];

    svg.selectAll("path")
            .data(dataset)
            .enter()
            .append("svg:path")
            .attr("transform", function(d) { return "translate(" + (xScale(d[0]) + (Math.random() * (20))) + ", " + yScale(d[1]) + ")";})
            .attr("d", d3.svg.symbol()
                    .size(40)
                    .type( function (d) {
                        switch (d[2]) {
                            case mutationTypes[0]: return symbol(0);
                            case mutationTypes[1]: return symbol(1);
                            default: return "circle";
                        }
                    }
            ))
            .attr("fill", function(d) {
                switch (d[2]) {
                    //Mutations
                    case mutationTypes[0]: return fillTypes[0];
                    case mutationTypes[1]: return fillTypes[1];
                    default: return "none";
                }
            })
            .attr("stroke", function(d) {
                switch (d[2]) {
                    case mutationTypes[0]: return strokeTypes[0];
                    case mutationTypes[1]: return strokeTypes[1];
                    case "-2": return strokeTypes[0];
                    case "-1": return strokeTypes[1];
                    case "0": return strokeTypes[2];
                    case "1": return strokeTypes[3];
                    case "2": return strokeTypes[4];
                    default: return "black";
                }
            })
            .attr("stroke-width", function(d) {
                if (type == 1) {
                    return "1";
                } else {
                    return "1.2";
                }
            });

    //Create the legend
    if (type == 1) {  //Legend for Mutations
        var legend = svg.selectAll(".legend")
                .data(mutationTypes)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) { return "translate(110, " + (30 + i * 15) + ")"; });
        legend.append("path")
                .attr("width", 18)
                .attr("height", 18)
                .attr("d", d3.svg.symbol()
                        .size(40)
                        .type(function(d, i) {
                            switch(i) {
                                case 0: return symbol(0);
                                case 1: return symbol(1);
                            };
                        })
                )
                .attr("fill", function (d, i) {
                    switch (i) {
                        case 0: return fillTypes[0];
                        case 1: return fillTypes[1];
                    }
                })
                .attr("stroke", function (d, i) {
                    switch (i) {
                        case 0: return strokeTypes[0];
                        case 1: return strokeTypes[1];
                    }
                })
        legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d, i) {
                    switch (i) {
                        case 0: return mutationTypes[0];
                        case 1: return mutationTypes[1];
                    }
                });
    } else {  //Legend for Gistic Copy Number
        var legend = svg.selectAll(".legend")
                .data(gisticTypes)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) { return "translate(110, " + (30 + i * 15) + ")"; });
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
                    return "none";
                })
                .attr("stroke", function (d, i) {
                    switch (i) {
                        case 0: return strokeTypes[0];
                        case 1: return strokeTypes[1];
                        case 2: return strokeTypes[2];
                        case 3: return strokeTypes[3];
                        case 4: return strokeTypes[4];
                    }
                })
                .attr("stroke-width", function (d, i) {
                    return 1.2;
                })
        legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d, i) {
                    switch (i) {
                        case 0: return gisticTypes[0];
                        case 1: return gisticTypes[1];
                        case 2: return gisticTypes[2];
                        case 3: return gisticTypes[3];
                        case 4: return gisticTypes[4];
                    }
                });
    }
    //Axis Titles
    svg.append("text")
            .attr("class", "label")
            .attr("x", 500)
            .attr("y", 580)
            .style("text-anchor", "end")
            .style("font-weight","bold")
            .text(xLegend);
    svg.append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("x", -150)
            .attr("y", 60)
            .style("text-anchor", "end")
            .style("font-weight","bold")
            .text(yLegend);
}

function copyData( desArray, oriArray) {
    var desArrayIndex = 0;
    for ( var tmpIndex = 0; tmpIndex < oriArray.length; tmpIndex ++ ){
        if (oriArray[tmpIndex] != "" && oriArray[tmpIndex] != null ) {
            desArray[desArrayIndex] = oriArray[tmpIndex];
            desArrayIndex += 1;
        }
    }
}
window.onload=drawSideBar();
</script>

<%!
    public int countProfiles (ArrayList<GeneticProfile> profileList, GeneticAlterationType type) {
        int counter = 0;
        for (int i = 0; i < profileList.size(); i++) {
            GeneticProfile profile = profileList.get(i);
            if (profile.getGeneticAlterationType() == type) {
                counter++;
            }
        }
        return counter;
    }
%>
