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
        stroke: black;
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
                  <label class='control-label'>Gene</label>
                  <select id='selected_gene'>
                    <%
                        out.println("<option selected='selected' value='" + gene_list[0] + "'> " + gene_list[0] + " </option>");
                        for (int i=1; i<gene_list.length; i++){
                            out.println("<option value='" + gene_list[i] + "'>" + gene_list[i] + "</option>");
                        }
                    %>
                  </select>
                  <button onclick="drawPlots()"> Go >> </button>
            </td>
            <td>
                <div id="plots_tab"></div>
            </td>
        </tr>
    </table>
</div>

<script>

    var case_set = [];
    var mutations = [];
    var gistic = [];
    var log2cna = [];
    var cna = [];
    var cna1 = []; //CNA
    var cna_rae = [];
    var cna_consensus = [];
    var rppa_protein_level = [];
    var methylation_hm27 = [];
    var mrna = [];
    var mrna_median = [];
    var mrna_median_Zscores = [];
    var mrna_merged_median_Zscores = [];
    var mrna_U133 = [];
    var rna_seq_v2_mrna = [];
    var rna_seq_v2_mrna_median_Zscores = [];
    var mirna = [];
    var mirna_median_Zscores = [];
    var mrna_zbynorm = [];
    var mrna_znormal = [];
    var mrna_outliers = [];
    var mrna_outlier = [];

    var genetic_profile_types = [
                                    "mutations",
                                    "gistic",
                                    "log2CNA",
                                    //"cna",
                                    //"CNA",
                                    //"cna_rae",
                                    //"cna_consensus",
                                    "RPPA_protein_level",
                                    "methylation_hm27",
                                    "mrna",
                                    //"mrna_median",
                                    //"mrna_median_Zscores",
                                    //"mrna_merged_median_Zscores",
                                    //"mrna_U133",
                                    //"rna_seq_v2_mrna",
                                    //"rna_seq_v2_mrna_median_Zscores",
                                    //"mirna",
                                    //"mirna_median_Zscores",
                                    //"mrna_zbynorm",
                                    //"mrna_znormal",
                                    //"mrna_outliers",
                                    //"mrna_outlier"
                                ];

    var genetic_profile_types_description = [
                                    "Mutations",
                                    "Putative copy-number alterations from GISTIC",
                                    "Log2 copy-number values",
                                    //"Putative copy-number alterations (RAE)",
                                    //"Putative copy-number alterations from discretization",
                                    //"Putative copy-number alterations (RAE, 203 cases)",
                                    //"Putative copy-number alterations (Consensus, GBM Pathways, 206 cases)",
                                    "RPPA protein/phosphoprotein level",
                                    "Methylation (HM27)",
                                    "mRNA expression (microarray)",
                                    //"mRNA expression (all genes)",
                                    //"mRNA Expression z-Scores (microarray)",
                                    //"mRNA/miRNA expression Z-scores (all genes)",
                                    //"mRNA expression (U133 microarray only)",
                                    //"mRNA expression (RNA Seq V2 RSEM)",
                                    //"mRNA Expression z-Scores (RNA Seq V2 RSEM)",
                                    //"microRNA expression",
                                    //"microRNA expression Z-scores",
                                    //"mRNA Expression Z-Scores vs Normals",
                                    //"mRNA Z-scores vs normal fat",
                                    //"mRNA Expression Outliers",
                                    //"mRNA outliers"
                                ];

    function drawPlots() {
        var profile_data = [];
        var tmp_case_set = [];
        var tmp_profile_data = [];
        var tmp_sections = [];
        $('#plots_tab').empty();
        var url = "webservice.do?cmd=getProfileData&case_set_id=<% out.print(case_set_id); %>&gene_list=" + document.getElementById('selected_gene').value + "&genetic_profile_id=";
        for ( var j = 0 ; j < genetic_profile_types.length ; j++ ) {
            $.ajax({
                url: url + "<% out.print(cancer_study_id); %>_" + genetic_profile_types[j],
                type: 'get',
                dataType: 'text',
                async: false,
                success: function(data) {
                    if (data != "") {
                        if ( data.indexOf("No genetic profile available") == -1) {
                            tmp_sections = data.split(/\n/);

                            //Get Case Set
                            tmp_case_set = String(tmp_sections[3]).trim().split(/\s+/);
                            for (var caseSetIndex = 0 ; caseSetIndex < tmp_case_set.length; caseSetIndex ++ ) {
                                case_set[caseSetIndex] = tmp_case_set[caseSetIndex + 2];
                            }

                            //Get profile data
                            tmp_profile_data = String(tmp_sections[4]).trim().split(/\s+/);
                            for (var profileDataIndex = 0 ; profileDataIndex < tmp_profile_data.length; profileDataIndex ++ ) {
                                profile_data[profileDataIndex] = tmp_profile_data[profileDataIndex + 2];
                            }

                            //Recognize profile type
                            var profile_type_identifier = tmp_sections[1];
                            if (profile_type_identifier.indexOf("Mutations") != -1) {
                                copyData(mutations, profile_data);
                            } else if (profile_type_identifier.indexOf("Putative copy-number alterations from GISTIC") != -1) {
                                copyData(gistic, profile_data);
                            } else if (profile_type_identifier.indexOf("Log2 copy-number values") != -1) {
                                copyData(log2cna, profile_data);
                            } else if (profile_type_identifier.indexOf("RPPA protein/phosphoprotein level") != -1) {
                                copyData(rppa_protein_level, profile_data);
                            } else if (profile_type_identifier.indexOf("Methylation (HM27)") != -1) {
                                copyData(methylation_hm27, profile_data);
                            } else if (profile_type_identifier.indexOf("mRNA expression (microarray)") != -1) {
                                copyData(mrna, profile_data);
                            }
                        }
                    } else {
                        alert("ERROR Fetching Data.");
                    }
                }
            });
        }
        drawScattersPlots();
    }


    function drawScattersPlots() {

        //Prepare Canvas
        var w = 800;
        var h = 800;

        //Create SVG canvas
        var svg = d3.select("#plots_tab")
                    .append("svg")
                    .attr("width", w)
                    .attr("height", h);

        //Create data set
        var dataset=[];
        var index = 0;
        for( var i = 0; i<case_set.length; i++) {

            //Skip NaN entries (Except for Mutations)
            if ((gistic[i] == "NaN") || (mrna[i] == "NaN")) {
                continue;
            }

            //Mutations Mapping
            if (mutations[i] != "NaN") {
                mutations[i] = "type1";
            } else {
                mutations[i] = "non";
            }
            dataset[index] = [gistic[i], mrna[i], mutations[i]];
            index += 1;
        }

    //-----------------tmp------//d3 min and max do NOT function well???
        var tmp_gistic = [];
        var tmp_index1 = 0;
        for (var j=0; j< gistic.length; j++){
            if (gistic[j] != "NaN") {
                tmp_gistic[tmp_index1] = gistic[j];
                tmp_index1 += 1;
            }
        }
        var min_gistic = Math.min.apply(Math, tmp_gistic);
        var max_gistic = Math.max.apply(Math, tmp_gistic);

        var tmp_mrna = [];
        var tmp_index2 = 0;
        for (var j=0; j< mrna.length; j++){
            if (mrna[j] != "NaN") {
                tmp_mrna[tmp_index2] = mrna[j];
                tmp_index2 += 1;
            }
        }
        var min_mrna = Math.min.apply(Math, tmp_mrna);
        var max_mrna = Math.max.apply(Math, tmp_mrna);
    //-------------------tmp---------------------------

        //Define scale functions
        var padding = 200;
        var xScale = d3.scale.linear()
                         .domain([min_gistic - 0.5, max_gistic + 0.5])
                         .range([80, 680]);
        var yScale = d3.scale.linear()
                         .domain([min_mrna - 1, max_mrna + 1])
                         .range([680, 80]);

        //Define Axis
        var xAxis = d3.svg.axis()
                          .scale(xScale)
                          .orient("bottom")
                          .ticks(5)

        var yAxis = d3.svg.axis()
                          .scale(yScale)
                          .orient("left")
                          .ticks(10);

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

        //Create SVG Axis
         var textSet = ["Homdel", "Hetloss", "Diploid", "Gain", "Amp"];
         svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(0, 680)")
            .call(xAxis)
           .selectAll("text")
            .data(textSet)
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", function(d) {return "rotate(-65)"})
            .text(function(d){return d})

         svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(0, 80)")
            .call(xAxis.orient("top").ticks(0));

         svg.append("text")
            .attr("class", "label")
            .attr("x", 550)
            .attr("y", 750)
            .style("text-anchor", "end")
            .text("Putative copy-number alterations from GISTIC");

         svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(80, 0)")
            .call(yAxis);

         svg.append("g")
            .attr("class","axis")
            .attr("transform", "translate(680, 0)")
            .call(yAxis.orient("right"))
           .append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("y", 50)
            .attr("x", -300)
            .style("text-anchor", "end")
            .text("mRNA expression");

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
