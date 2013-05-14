<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.servlet.GeneratePlots" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticAlterationType" %>
<%
    String cancer_study_id = (String)request.getParameter("cancer_study_id");
    String case_set_id = (String)request.getParameter("case_set_id");
    String[] gene_list = ((String)request.getParameter("gene_list")).split("\\s+");
%>
<script type="text/javascript" src="js/d3.v3.js"></script>
<script>
    var cancer_study_id = "<%out.print(cancer_study_id);%>",
            case_set_id = "<%out.print(case_set_id);%>",
            case_set_name,
            gene,
            case_set = [],
            mutations = [],
            copy_no = [],
            mrna = [],
            rppa = [],
            dna_methylation = [],
            extended_mutations = [],
            genetic_profile_mutations = [],
            genetic_profile_mrna = [],
            genetic_profile_copy_no = [],
            genetic_profile_rppa = [],
            genetic_profile_dna_methylation = [],
            result_set = new Array(5);

    for ( var i = 0 ; i< result_set.length; i++) {
        result_set[i] = new Array();
    }
    var plot_type_list = [
        ["mrna_vs_copy_no", "mRNA vs. Copy Number"],
        ["mrna_vs_dna_mythelation", "mRNA vs. DNA Methylation"],
        ["rppa_protein_level_vs_mrna", "RPPA Protein Level vs. mRNA"]
    ];
    var data_type_copy_no, data_type_mrna, xLegend, yLegend;

</script>

<script>
    $(document).ready(function(){
        var result;
        $.ajax({
            url: 'portal_meta_data.json',
            dataType: 'json',
            success: function(data) {
                result = data;
                generateFrame(result);
            },
            async:false,
        });
    });
    $(window).load (function(){
        drawSideBar();
    });
    function generateFrame(json){
        var tmpArr = [];
        $.each(json.cancer_studies.<% out.print(cancer_study_id); %>.case_sets, function(i, item_case_set){
            if (item_case_set.id == case_set_id) {
                case_set_name = item_case_set.name;
            }
        });
        $.each(json.cancer_studies.<% out.print(cancer_study_id); %>.genomic_profiles, function(j, item_genomic_profile){
            if (item_genomic_profile.alteration_type == "MUTATION_EXTENDED") {
                tmpArr = [item_genomic_profile.id, item_genomic_profile.name];
                genetic_profile_mutations.push(tmpArr);
            } else if (item_genomic_profile.alteration_type == "MRNA_EXPRESSION") {
                tmpArr = [item_genomic_profile.id, item_genomic_profile.name];
                genetic_profile_mrna.push(tmpArr);
            } else if (item_genomic_profile.alteration_type == "COPY_NUMBER_ALTERATION") {
                tmpArr = [item_genomic_profile.id, item_genomic_profile.name];
                genetic_profile_copy_no.push(tmpArr);
            } else if (item_genomic_profile.alteration_type == "METHYLATION") {
                tmpArr = [item_genomic_profile.id, item_genomic_profile.name];
                genetic_profile_dna_methylation.push(tmpArr);
            } else if (item_genomic_profile.alteration_type == "PROTEIN_ARRAY_PROTEIN_LEVEL") {
                tmpArr = [item_genomic_profile.id, item_genomic_profile.name];
                genetic_profile_rppa.push(tmpArr);
            }
        });
    }
</script>

<div class="section" id="plots">
    <table >
        <tr>
            <td>
                <table>
                    <tr><td style="border:2px solid #BDBDBD;height:300px;padding:20px;">
                        <h4>Plot Parameters</h4>
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
                        <button onclick="generateScatterPlots()" style='width:80px;height:30px;border-radius:5px;background-color:#A4A4A4;'>
                            <font style='font-size: 14px;color:white;font-weight: 7px;'> GO >> </font>
                        </button>
                    </td>
                    <tr><td style='height:250px;'></td>
                </table>
            </td>
            <td>
                <br><b><div id="img_center" style="display:inline-block; padding-left:100px;"></div></b>
                <form style="display:inline-block" action='svgtopdf.do' method='post' onsubmit="this.elements['svgelement'].value=loadSVG();">
                    <input type='hidden' name='svgelement'>
                    <input type='hidden' name='filetype' value='pdf'>
                    <input type='submit' value='PDF'>
                </form>
                <!--form style="display:inline-block" action='svgtopdf.do' method='post' onsubmit="this.elements['svgelement'].value=loadSVG();">
                    <input type='hidden' name='svgelement'>
                    <input type='hidden' name='filetype' value='png'>
                    <input type='submit' value='PNG'>
                </form-->
                <form style="display:inline-block" action='svgtopdf.do' method='post' onsubmit="this.elements['svgelement'].value=loadSVG();">
                    <input type='hidden' name='svgelement'>
                    <input type='hidden' name='filetype' value='svg'>
                    <input type='submit' value='SVG'>
                </form>
                <div id="plots_tab"></div>
            </td>
        </tr>
    </table>
</div>

<script>
var urls;
function fetchData() {
    gene = document.getElementById("genes").value;
    data_type_copy_no = document.getElementById("data_type_copy_no").value;
    data_type_mrna = document.getElementById("data_type_mrna").value;
    var url_base = "webservice.do?cmd=getProfileData&case_set_id=" + case_set_id + "&gene_list=" + gene + "&genetic_profile_id=";
    urls = [
        url_base + genetic_profile_mutations[0][0], //mutations
        url_base + data_type_copy_no, //copy no
        url_base + data_type_mrna, //mrna
        url_base + genetic_profile_rppa[0][0], //rppa
        url_base + genetic_profile_dna_methylation[0][0] //dna methylation
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
                        for (var j = 0 ; j < tmp_case_set.length - 2 ; j ++ ) {
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
    //Map Mutations
    var mutationMap = {};
    for (var i = 0; i<case_set.length; i++ ) {
        mutationMap[case_set[i]] = "non";
    }
    $.ajax({
        url: "webservice.do?cmd=getMutationData&case_set_id=" + case_set_id + "&gene_list=" + gene + "&genetic_profile_id=" + cancer_study_id + "_mutations",
        type: 'get',
        dataType: 'text',
        async: false,
        success: function(data) {
            if (data != "") {
                if ( data.indexOf("No genetic profile available") == -1) {  //Profile Exists
                    tmp_lines = data.split(/\n/);
                    //Over-write mutation type
                    for (var j = 3 ; j < tmp_lines.length; j ++ ) {
                        var tmp_columns = tmp_lines[j].split(/\s+/);
                        mutationMap[tmp_columns[2]] = tmp_columns[5];
                    }
                }
            } else {
                alert("ERROR Fetching Extended Mutation Data.");
            }
        }
    });
    for (var k = 0; k< case_set.length; k++ ){
        mutations[k] = mutationMap[case_set[k]];
    }
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
    drawScatterPlots(copy_no, mrna, mutations, tmp_axis_title_result[0], tmp_axis_title_result[1], 1, mutations);
    $('#img_center').empty();
    $('#img_center').append(gene + ": mRNA Expression v. CNA ");
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
        drawScatterPlots(copy_no, mrna, mutations, xLegend, yLegend, 1, mutations);
        $('#img_center').empty();
        $('#img_center').append(gene + ": mRNA Expression v. CNA ");
    } else if (tmp_plot_type == plot_type_list[1][0]) {  //"mrna_vs_dna_mythelation"
        drawScatterPlots(dna_methylation, mrna, copy_no, xLegend, yLegend, 2, mutations);
        $('#img_center').empty();
        $('#img_center').append(gene + ": mRNA Expression v. DNA Methylation ");
    } else if (tmp_plot_type == plot_type_list[2][0]) {  //"rppa_protein_level_vs_mrna"
        drawScatterPlots(mrna, rppa, copy_no, xLegend, yLegend, 3, mutations);
        $('#img_center').empty();
        $('#img_center').append(gene + ": RPPA protein level v. mRNA Expression ");
    }
}



/**
 * Draw sccatter plots
 *
 * @param xData, yData --> types varies depends on plots type
 * @param zData --> Annotation for points depends on plots type
 * @param xLegend, yLegend --> Title for x&y axis
 * @param type
 *        1--> mrna_vs_copy_no
 *        2--> mrna_vs_dna_mythelation
 *        3--> rppa_protein_level_vs_mrna
 * @param mutations --> Annotate mutation and GISTIC CNA together, extra dimension
 *
 */

var dataset = [];
var tmp_dot = [];
function drawScatterPlots(xData, yData, zData, xLegend, yLegend, type, mutations) {
//    var dataset = [];
    //Create Canvas
    $('#plots_tab').empty();
    var w = 700;
    var h = 600;
    var svg = d3.select("#plots_tab")
            .append("svg")
            .attr("width", w)
            .attr("height", h);
    //Prepare DataSet
    var index = 4;
    if (type == 1) {
        for( var i = 0; i<xData.length; i++) {
            //Skip NaN entries
            if ((xData[i] == "NaN") || (yData[i] == "NaN")) {
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
            dataset[index] = [xData[i], yData[i], zData[i], mutations[i]];
            index += 1;
        }
    } else if (type == 2) {
        for( var i = 0; i<xData.length; i++) {
            //Skip NaN entries
            if ((xData[i] == "NaN") || (yData[i] == "NaN")) {
                continue;
            }
            dataset[index] = [xData[i], yData[i], zData[i], mutations[i]];
            index += 1;
        }
    } else if (type == 3) {
        for( var i = 0; i<xData.length; i++) {
            //Skip NaN entries
            if ((xData[i] == "NaN") || (yData[i] == "NaN")) {
                continue;
            }
            dataset[index] = [xData[i], yData[i], zData[i], mutations[i]];
            index += 1;
        }
    }

    // --- Analysis Data --- find max, min, define edge(display purpose only)
    var tmp_xData = [];
    var tmp_xIndex = 0;
    var tmp_yData = [];
    var tmp_yIndex = 0;
    for (var j=0; j< xData.length; j++){
        if (xData[j] != "NaN" && yData[j] != "NaN") {
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

    //----Create SVG Axis (Duplicate: can NOT use CSS style here since batik won't take it for PDF convertion.)

    //---------------Add extra text axis annotation for GISTIC
    if (type == 1 && data_type_copy_no.indexOf("gistic") != -1) {
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

    //Create SVG dots
    var symbol = ["triangle-down", "diamond", "triangle-up", "square", "cross", "triangle-up", "circle"];
    var mutationTypes = ["frameshift", "nonsense", "splice", "in_frame", "nonstart", "nonstop", "missense"];
    var mutationStrokeTypes = ["#DF0101", "#DF0101", "#DF0101", "#DF0101", "#DF0101", "#DF0101", "#DF0101"];
    var mutationFillTypes = ["#1C1C1C", "#1C1C1C", "#FF8000", "#FF8000", "#FF8000", "#1C1C1C", "#FF8000"];
    var gisticStrokeTypes = ["#00008B", "#00BFFF", "#000000", "#FF69B4", "#FF0000"];
    var gisticLegendText = ["Homdel", "Hetloss",  "Gain", "Amp", "Mutated", "Normal"];
    var gisticLegendStrokeTypes = ["#00008B", "#00BFFF", "#FF69B4", "#FF0000", "none", "#000000"];
    var gisticLegendFillTypes = ["none", "none", "none", "none", "orange", "none"];

    //----------------- Plot dots for GISTIC v mRNA view (with data noise)
    if ( type == 1 ) {
        //Define noise level
        var ramRatio = 0;
        if (data_type_copy_no.indexOf("gistic") != -1) {
            ramRatio = 20;
        }
        svg.selectAll("path")
                .data(dataset)
                .enter()
                .append("svg:path")
                .attr("transform", function(d) { return "translate(" + (xScale(d[0]) + ((Math.random() * (ramRatio)) - (ramRatio/2))) + ", " + yScale(d[1]) + ")";})
                .attr("d", d3.svg.symbol()
                        .size(30)
                        .type( function (d) {
                            switch (d[2]) {
                                case mutationTypes[0] : return symbol[0];
                                case mutationTypes[1] : return symbol[1];
                                case mutationTypes[2] : return symbol[2];
                                case mutationTypes[3] : return symbol[3];
                                case mutationTypes[4] : return symbol[4];
                                case mutationTypes[5] : return symbol[5];
                                case mutationTypes[6] : return symbol[6];
                                default: return "circle";
                            }
                        }
                ))
                .attr("fill", function(d) {
                    switch (d[2]) {
                        case mutationTypes[0]: return mutationFillTypes[0];
                        case mutationTypes[1]: return mutationFillTypes[1];
                        case mutationTypes[2]: return mutationFillTypes[2];
                        case mutationTypes[3]: return mutationFillTypes[3];
                        case mutationTypes[4]: return mutationFillTypes[4];
                        case mutationTypes[5]: return mutationFillTypes[5];
                        case mutationTypes[6]: return mutationFillTypes[6];
                        default: return "none";
                    }
                })
                .attr("stroke", function(d) {
                    switch (d[2]) {
                        case mutationTypes[0]: return mutationStrokeTypes[0];
                        case mutationTypes[1]: return mutationStrokeTypes[1];
                        case mutationTypes[2]: return mutationStrokeTypes[2];
                        case mutationTypes[3]: return mutationStrokeTypes[3];
                        case mutationTypes[4]: return mutationStrokeTypes[4];
                        case mutationTypes[5]: return mutationStrokeTypes[5];
                        case mutationTypes[6]: return mutationStrokeTypes[6];
                        default: return "#2E9AFE";
                    }
                })
                .attr("stroke-width", function(d) {
                    return "1";
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
                    var result;
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
        var errorTxt1 = "An error occurred processing your request.";
        var errorTxt2 = "It maybe that your gene/case set combination has no data for this data type. ";
        var errorTxt3 = "If you believe this is an error, please contact us cbioportal@cbio.mskcc.org.";
        svg.append("text")
                .attr("x", 220)
                .attr("y", 280)
                .attr("fill", "#DF3A01")
                .text(errorTxt1)
        svg.append("text")
                .attr("x", 120)
                .attr("y", 295)
                .attr("fill", "#DF3A01")
                .text(errorTxt2)
        svg.append("text")
                .attr("x", 130)
                .attr("y", 310)
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
                            case i: return mutationStrokeTypes[i];
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
            .attr("x", 500)
            .attr("y", 580)
            .style("text-anchor", "end")
            .style("font-weight","bold")
            .text(gene + " , " + xLegend);
    svg.append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("x", -150)
            .attr("y", 60)
            .style("text-anchor", "end")
            .style("font-weight","bold")
            .text(gene + " , " + yLegend);

    //If it is GISTIC and mrna view, add Box plots
    if (type == 1 && data_type_copy_no.indexOf("gistic") != -1) {
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
                if (yData[j] != "NaN" && xData[j] != "NaN" && xData[j] == i) {
                    tmp_y_arr[index_tmp_y_data_array] = parseFloat(yData[j]);
                    index_tmp_y_data_array += 1;
                }
            }
            tmp_y_arr.sort(function(a,b){return a-b});
            if (tmp_y_arr.length == 0) {
                //TODO: error handle (empty dataset)
            } else if (tmp_y_arr.length == 1) {
                mean = yScale(tmp_y_arr[0]);
                var meanLine = svg.append("line")
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

function drawBoxPlots(svg, midLine, top, bottom, quan1, quan2, mean, IQR) {
    //Rectangle
    svg.append("rect")
            .attr("x", midLine-40)
            .attr("y", quan2)
            .attr("width", 80)
            .attr("height", IQR)
            .attr("fill", "none")
            .attr("stroke-width", 0.5)
            .attr("stroke", "grey");
    //meanLine
    svg.append("line")
            .attr("x1", midLine-40)
            .attr("x2", midLine+40)
            .attr("y1", mean)
            .attr("y2", mean)
            .attr("stroke-width", 1)
            .attr("stroke", "grey");
    //topLine
    svg.append("line")
            .attr("x1", midLine-30)
            .attr("x2", midLine+30)
            .attr("y1", top)
            .attr("y2", top)
            .attr("stroke-width", 0.5)
            .attr("stroke", "grey");
    //bottomLine
    svg.append("line")
            .attr("x1", midLine-30)
            .attr("x2", midLine+30)
            .attr("y1", bottom)
            .attr("y2", bottom)
            .attr("stroke", "grey")
            .style("stroke-width", 0.5);
    //Top Whisker
    svg.append("line")
            .attr("x1", midLine)
            .attr("x2", midLine)
            .attr("y1", quan1)
            .attr("y2", bottom)
        //.style("stroke-dasharray", ("3, 3"))
            .attr("stroke", "grey")
            .attr("stroke-width", 0.5);
    //Bottom Whisker
    svg.append("line")
            .attr("x1", midLine)
            .attr("x2", midLine)
            .attr("y1", quan2)
            .attr("y2", top)
        //.style("stroke-dasharray", ("3, 3"))
            .attr("stroke", "grey")
            .style("stroke-width", 0.5);
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


function median(v) {
    var half = Math.floor(v.length/2);
    if(v.length % 2)
        return v[half];
    else
        return (parseFloat(v[half-1]) + parseFloat(v[half]))/2;
}

function loadSVG() {
    var mySVG = document.getElementById("plots_tab");
    var svgDoc = mySVG.getElementsByTagName("svg");
    var tmp1 = new XMLSerializer();
    var tmp2 = tmp1.serializeToString(svgDoc[0]);
    return tmp2;
}

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
