<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.servlet.GeneratePlots" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticAlterationType" %>

<script type="text/javascript" src="js/d3.v2.min.js"></script>
<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css">

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

<%
    String cancer_study_id = (String)request.getParameter("cancer_study_id");
    String case_set_id = (String)request.getParameter("case_set_id");
    String[] gene_list = ((String)request.getParameter("gene_list")).split("\\s+");
%>

<div class="section" id="plots">
    <table class='table table-bordered'>
        <tr>
            <td>
                <br>
                <h4> Plot Parameters</h4>
                <br>
                <label class='control-label'><b>Gene</b></label>
                <select id='selected_gene'>
                    <%
                        out.println("<option selected='selected' value='" + gene_list[0] + "'> " + gene_list[0] + " </option>");
                        for (int i=1; i<gene_list.length; i++){
                            out.println("<option value='" + gene_list[i] + "'>" + gene_list[i] + "</option>");
                        }
                    %>
                </select>
                <br><br>
                <label class='control-label'><b>Plot Type</b></label>
                <select id='plot_type'></select>
                <br><br>
                <label class='control-label'><b>Data Type</b></label>
                mRNA
                <br><select id='data_type_mrna'></select><br>
                Copy Number
                <br><select id='data_type_copy_no'></select>
                <br><br><br>
                <button class='btn btn-info' onclick="drawScattersPlots()"> Refresh >> </button>
            </td>
            <td>
                <div id="plots_tab"></div>
            </td>
        </tr>
    </table>
</div>

<script>

var case_set = [];

var result_set_index = [
    "mutations_index",
    "gistic_index",
    "log2cna_index",
    "cna_index",
    "cna1_index",
    "cna_rae_index",
    "cna_consensus_index",
    "rppa_protein_level_index",
    "methylation_hm27_index",
    "mrna_index",
    "mrna_median_index",
    "mrna_median_Zscores_index",
    "mrna_merged_median_Zscores_index",
    "mrna_U133_index",
    "rna_seq_v2_mrna_index",
    "rna_seq_v2_mrna_median_Zscores_index",
    "mirna_index",
    "mirna_median_Zscores_index",
    "mrna_zbynorm_index",
    "mrna_znormal_index",
    "mrna_outliers_index",
    "mrna_outlier_index"
];

var genetic_profile_types = [
    "mutations",
    "gistic",
    "log2CNA",
    "cna",
    "CNA",
    "cna_rae",
    "cna_consensus",
    "RPPA_protein_level",
    "methylation_hm27",
    "mrna",
    "mrna_median",
    "mrna_median_Zscores",
    "mrna_merged_median_Zscores",
    "mrna_U133",
    "rna_seq_v2_mrna",
    "rna_seq_v2_mrna_median_Zscores",
    "mirna",
    "mirna_median_Zscores",
    "mrna_zbynorm",
    "mrna_znormal",
    "mrna_outliers",
    "mrna_outlier"
];

var genetic_profile_types_description = [
    "Mutations",
    "Putative copy-number alterations from GISTIC",
    "Log2 copy-number values",
    "Putative copy-number alterations (RAE)",
    "Putative copy-number alterations from discretization",
    "Putative copy-number alterations (RAE, 203 cases)",
    "Putative copy-number alterations (Consensus, GBM Pathways, 206 cases)",
    "RPPA protein/phosphoprotein level",
    "Methylation (HM27)",
    "mRNA expression (microarray)",
    "mRNA expression (all genes)",
    "mRNA Expression z-Scores (microarray)",
    "mRNA/miRNA expression Z-scores (all genes)",
    "mRNA expression (U133 microarray only)",
    "mRNA expression (RNA Seq V2 RSEM)",
    "mRNA Expression z-Scores (RNA Seq V2 RSEM)",
    "microRNA expression",
    "microRNA expression Z-scores",
    "mRNA Expression Z-Scores vs Normals",
    "mRNA Z-scores vs normal fat",
    "mRNA Expression Outliers",
    "mRNA outliers"
];

var result_set = new Array(genetic_profile_types.length);
for (var i = 0; i<genetic_profile_types.length; i++ ) {
    result_set[i] = new Array();
}

function drawSideBar() {
    var profile_data = [];
    var tmp_case_set = [];
    var tmp_profile_data = [];
    var tmp_sections = [];

    var url = "webservice.do?cmd=getProfileData&case_set_id=<% out.print(case_set_id); %>&gene_list=" + document.getElementById('selected_gene').value + "&genetic_profile_id=";
    for ( var j = 0 ; j < genetic_profile_types.length ; j++ ) {
        $.ajax({
            url: url + "<% out.print(cancer_study_id); %>_" + genetic_profile_types[j],
            type: 'get',
            dataType: 'text',
            async: false,
            success: function(data) {
                if (data != "") {
                    if ( data.indexOf("No genetic profile available") == -1) {  //Profile Exists
                        tmp_sections = data.split(/\n/);
                        //Get Case Set
                        tmp_case_set = String(tmp_sections[3]).trim().split(/\s+/);
                        for (var caseSetIndex = 0 ; caseSetIndex < tmp_case_set.length; caseSetIndex ++ ) {
                            case_set[caseSetIndex] = tmp_case_set[caseSetIndex + 2];
                        }
                        //Get profile data (Filter the headers)
                        tmp_profile_data = String(tmp_sections[4]).trim().split(/\s+/);
                        for (var profileDataIndex = 0 ; profileDataIndex < tmp_profile_data.length; profileDataIndex ++ ) {
                            profile_data[profileDataIndex] = tmp_profile_data[profileDataIndex + 2];
                        }
                        //Fill in Result Data Set
                        var profile_type_identifier = tmp_sections[1];
                        var tmp_result_set_index = findIndex(profile_type_identifier.substring(13), genetic_profile_types_description);
                        if ( tmp_result_set_index != -1 ) {
                            copyData(result_set[tmp_result_set_index], profile_data);
                            drawDataType(profile_type_identifier, tmp_result_set_index);
                        }
                    }
                } else {
                    alert("ERROR Fetching Data.");
                }
            }
        });
    }
    drawPlotType();
    drawScattersPlots();
}

var has_copy_no = false;
var has_mrna = false;
var has_dna_mythelation = false;
var has_rppa = false;

function drawDataType(inputStr, i) {
    if ((inputStr.indexOf("mRNA") != -1) || (inputStr.indexOf("microRNA") != -1)) {
        has_mrna = true;
        $('#data_type_mrna').append("<option value='" + genetic_profile_types[i] + "'>" + genetic_profile_types_description[i] + "</option>");
    } else if (inputStr.indexOf("copy-number") != -1) {
        has_copy_no = true;
        $('#data_type_copy_no').append("<option value='" + genetic_profile_types[i] + "'>" + genetic_profile_types_description[i] + "</option>");
    } else if (inputStr.indexOf("RPPA") != -1) {
        has_rppa = true;
    } else if (inputStr.indexOf("Methylation") != -1) {
        has_dna_mythelation = true;
    }
}

function drawPlotType() {
    if (has_copy_no == true && has_mrna == true) {
        $('#plot_type').append("<option value='mrna_vs_copy_no' selected='selected'>mRNA vs. Copy Number</option>");
    }
    if (has_dna_mythelation == true && has_mrna == true) {
        $('#plot_type').append("<option value='mrna_vs_dna_mythelation'>mRNA vs. DNA Methylation</option>");
    }
    if (has_rppa == true && has_mrna == true) {
        $('#plot_type').append("<option value='rppa_protein_level_vs_mrna'>RPPA Protein Level vs. mRNA</option>");
    }
}

function drawScattersPlots() {

    //Create Canvas
    $('#plots_tab').empty();
    var w = 800;
    var h = 800;
    var svg = d3.select("#plots_tab")
            .append("svg")
            .attr("width", w)
            .attr("height", h);

    //Prepare DataSet
    var mrna_type = document.getElementById('data_type_mrna').value;
    var copy_no_type = document.getElementById('data_type_copy_no').value;
    var tmp_mrna_index = findIndex(mrna_type, genetic_profile_types);
    var tmp_copy_no_index = findIndex(copy_no_type, genetic_profile_types);
    var tmp_rppa_index = findIndex("rppa_protein_level_index", result_set_index);
    var tmp_mythelation_index = findIndex("methylation_hm27_index", result_set_index);
    var tmp_mutation_index = findIndex("mutations_index", result_set_index);

    var xData = [];
    var yData = [];
    var zData = [];
    var dataset = [];
    var xLegend;
    var yLegend;

    var plot_type = document.getElementById('plot_type').value;

    if (plot_type == "mrna_vs_copy_no") {
        copyData(xData, result_set[tmp_copy_no_index]);
        copyData(yData, result_set[tmp_mrna_index]);
        copyData(zData, result_set[tmp_mutation_index]);
        var index = 0;
        for( var i = 0; i<case_set.length; i++) {
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
        xLegend = genetic_profile_types_description[tmp_copy_no_index];
        yLegend = genetic_profile_types_description[tmp_mrna_index];
    } else if (plot_type == "mrna_vs_dna_mythelation") {
        xData = result_set[tmp_mythelation_index];
        yData = result_set[tmp_mrna_index];
    } else if (plot_type == "rppa_protein_level_vs_mrna") {
        xData = result_set[tmp_mrna_index];
        yData = result_set[tmp_rppa_index];
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
    var edge_x = (max_x - min_x) * 0.1;

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
    var edge_y = (max_y - min_y) * 0.1;
    //-------------------tmp---------------------------

    //Define scale functions
    var padding = 200;
    var xScale = d3.scale.linear()
            .domain([min_x - edge_x, max_x + edge_x])
            .range([80, 680]);
    var yScale = d3.scale.linear()
            .domain([min_y - edge_y, max_y + edge_y])
            .range([680, 80]);
    //Define Axis
    var xAxis = d3.svg.axis()
            .scale(xScale)
            .orient("bottom")
    //.ticks(5)

    var yAxis = d3.svg.axis()
            .scale(yScale)
            .orient("left");
    //.ticks(10);
    //Create SVG Axis

    //Axis Annotation for GISTIC
    if (copy_no_type == "gistic") {
        var textSet = ["Homdel", "Hetloss", "Diploid", "Gain", "Amp"];
        var ticksTextSet = [];
        var tmp_ticks_text_index = 0;
        for (var i = min_x + 2; i = max_x + 3; i++) {
            ticksTextSet[tmp_ticks_text_index] = textSet[i];
            tmp_ticks_text_index += 1;
        }
        svg.append("g")
                .attr("class","axis")
                .attr("transform", "translate(0, 680)")
                .call(xAxis.ticks(ticksTextSet.length))
                .selectAll("text")
                .data(ticksTextSet)
                .style("text-anchor", "end")
                .attr("dx", "-.8em")
                .attr("dy", ".15em")
                .attr("transform", function(d) {return "rotate(-65)"})
                .text(function(d){return d})
        document.write(copy_no_type);
        document.write(ticksTextSet);
    } else {
        svg.append("g")
                .attr("class","axis")
                .attr("transform", "translate(0, 680)")
                .call(xAxis);
    }

    svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(0, 80)")
            .call(xAxis.orient("top").ticks(0));

    svg.append("text")
            .attr("class", "label")
            .attr("x", 550)
            .attr("y", 750)
            .style("text-anchor", "end")
            .text(xLegend);

    svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(80, 0)")
            .call(yAxis)
            .append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("y", -40)
            .attr("x", -300)
            .style("text-anchor", "end")
            .text(yLegend);

    svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(680, 0)")
            .call(yAxis.orient("right").ticks(0));

    //Create SVG dots
    var symbol = d3.scale.ordinal().range(["circle", "triangle-up"]);
    var mutationTypes = ["non", "type1"];
    var fillTypes = ["none", "#358FE8", "grey"];
    var strokeTypes = ["#9EA2A6", "#2E7CC9", "green"];

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
                        }
                    }
            ))
            .attr("fill", function(d) {
                switch (d[2]) {
                    case mutationTypes[0]: return fillTypes[0];
                    case mutationTypes[1]: return fillTypes[1];
                }
            })
            .attr("stroke", function(d) {
                switch (d[2]) {
                    case mutationTypes[0]: return strokeTypes[0];
                    case mutationTypes[1]: return strokeTypes[1];
                }
            });

    //Create the legend
    var legend = svg.selectAll(".legend")
            .data(mutationTypes)
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) { return "translate(100, " + (100 + i * 15) + ")"; });
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

}

function copyData( desArray, oriArray) {
    for ( var tmpIndex = 0; tmpIndex < oriArray.length; tmpIndex ++ ){
        if (oriArray[tmpIndex] != "" && oriArray[tmpIndex] != null ) {
            desArray[tmpIndex] = oriArray[tmpIndex];
        }
    }
}

function findIndex(Str, Exp) {
    for (var i = 0; i< Exp.length; i++) {
        if ( Str == Exp[i] ) {
            return i;
        }
    }
    return -1;
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
