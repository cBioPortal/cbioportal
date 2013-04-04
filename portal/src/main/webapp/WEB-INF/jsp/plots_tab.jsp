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
    String genetic_profile_ids = (String)request.getParameter("genetic_profile_ids_PROFILE_MUTATION_EXTENDED");
    String case_set_id = (String)request.getParameter("case_set_id");
    String gene_list_tmp = (String)request.getParameter("gene_list");
    String[] gene_list = gene_list_tmp.split("\\s+");
%>

<script type="text/javascript" src="js/d3.v2.min.js"></script>
<script type="text/javascript" src="js/bootstrap.min.js"></script>
<link rel="stylesheet" type="text/css" href="css/bootstrap.css">
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

<div class="section" id="plots">

Gene:
<select>
  <option>BRCA1</option>
  <option>BRCA2</option>
</select>



<script>

    //Prepare Canvas
    var w = 1000;
    var h = 1000;

    //Fetch Data
    var tmp_result;
    $.ajax({
        url: 'webservice.do?cmd=getProfileData&case_set_id=ov_tcga_all&genetic_profile_id=ov_tcga_mutations,ov_tcga_gistic,ov_tcga_mrna&gene_list=BRCA1',
        //url: 'webservice.do?cmd=getProfileData&case_set_id=gbm_tcga_all&genetic_profile_id=gbm_tcga_mutations, gbm_tcga_log2CNA,gbm_tcga_gistic,gbm_tcga_mrna,gbm_tcga_RPPA_protein_level&gene_list=BRCA1',
        type: 'get',
        dataType: 'text',
        async: false,
        success: function(data) {
            if (data != "") {
                tmp_result = data;
            } else {
                alert("ERROR!");
            }
        }
    });

    //Parse Data
    var case_set = [];
    var mutations = [];
    var gistic_copy_no = [];
    var log2cna_copy_no = [];
    var mrna = [];
    var rppa_protein_level = [];

    var tmp_sections = tmp_result.split(/\n/);
    var tmp_items = [];
    for (var k = 0; k < tmp_sections.length; k++) {
        tmp_items = tmp_sections[k].trim().split(/\s+/);
        var tmp_identifier = tmp_items[0];
        for (var j = 4; j < tmp_items.length; j++) {
            if (tmp_items[0].indexOf("GENETIC_PROFILE_ID") != -1) {
                case_set[j-4] = tmp_items[j];
            } else if (tmp_identifier.indexOf("log2CNA") != -1) {
                log2cna_copy_no[j-4] = tmp_items[j];
            } else if (tmp_identifier.indexOf("gistic") != -1) {
                gistic_copy_no[j-4] = tmp_items[j];
            } else if (tmp_identifier.indexOf("mutations") != -1) {
                mutations[j-4] = tmp_items[j];
            } else if (tmp_identifier.indexOf("mrna") != -1) {
                mrna[j-4] = tmp_items[j];
            } else if (tmp_identifier.indexOf("RPPA_protein_level") != -1) {
                rppa_protein_level[j-4] = tmp_items[j];
            }
        }
    }

    var dataset = [];
    var index = 0;
    for( var i = 0; i<case_set.length; i++) {

        //Skip NaN entries (Except for Mutations)
        if ((gistic_copy_no[i] == 'NaN') || (mrna[i] == 'NaN')) {
            continue;
        }

        //Mutations Mapping
        if (mutations[i] != "NaN") {
            mutations[i] = "type1";
        } else {
            mutations[i] = "non";
        }

        dataset[index] = [gistic_copy_no[i], mrna[i], mutations[i]];
        index += 1;
    }

//-----------------tmp-----------------------------
    //d3 min and max do NOT function well???
    var tmp_gistic = [];
    var tmp_index1 = 0;
    for (var j=0; j< gistic_copy_no.length; j++){
        if (gistic_copy_no[j] != "NaN") {
            tmp_gistic[tmp_index1] = gistic_copy_no[j];
            tmp_index1 += 1;
        }
    }
    var min_gistic = Math.min.apply(Math, tmp_gistic);
    var max_gistic = Math.max.apply(Math, tmp_gistic);

    //d3 min and max do NOT function well???
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
                     .range([padding, w-padding]);

    var yScale = d3.scale.linear()
                     .domain([min_mrna - 1, max_mrna + 1])
                     .range([h-padding, padding]);


    //Define Axis
    var xAxis = d3.svg.axis()
                      .scale(xScale)
                      .orient("bottom")
                      .ticks(5)

    var yAxis = d3.svg.axis()
                      .scale(yScale)
                      .orient("left")
                      .ticks(10);

    //Create SVG canvas
    var svg = d3.select("#plots")
                .append("svg")
                .attr("width", w)
                .attr("height", h);

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


    //Add Text to Dots
    /*
    svg.selectAll("text")
       .data(dataset)
       .enter()
       .append("text")
       .text(function(d) {
            return d[0] + "," + d[1];
       })
       .attr("x", function(d) {
            return xScale(d[0]);
       })
       .attr("y", function(d) {
            return yScale(d[1]);
       })
       .attr("font-family", "sans-serif")
       .attr("font-size", "11px")
       .attr("fill", function(d){return d[2]});
    */

    //Create SVG Axis
     var textSet = ["Homdel", "Hetloss", "Diploid", "Gain", "Amp"];
     svg.append("g")
        .attr("class","axis")
        .attr("transform", "translate(0," + (h - padding) + ")")
        .call(xAxis)
       .selectAll("text")
        .data(textSet)
        .style("text-anchor", "end")
        .attr("dx", "-.8em")
        .attr("dy", ".15em")
        .attr("transform", function(d) {return "rotate(-65)"})
        .text(function(d){return d})

     svg.append("text")
        .attr("class", "label")
        .attr("x", w/2 + padding)
        .attr("y", h - padding/2)
        .style("text-anchor", "end")
        .text("Putative copy-number alterations from GISTIC");

     svg.append("g")
        .attr("class","axis")
        .attr("transform", "translate(" + padding + ",0)")
        .call(yAxis)
       .append("text")
        .attr("class", "label")
        .attr("transform", "rotate(-90)")
        .attr("y", -40)
        .attr("x", -(h-padding)/2)
        .style("text-anchor", "end")
        .text("mRNA expression");
     svg.append("g")
        .attr("class","axis")
        .attr("transform", "translate(" + (w-padding) + ",0)")
        .call(yAxis.orient("right"))

    //Create the legend
        var legend = svg.selectAll(".legend")
           .data(mutationTypes)
           .enter().append("g")
           .attr("class", "legend")
           .attr("transform", function(d, i) { return "translate(" + (padding + 10)   + "," + (padding + i * 15) + ")"; });
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
</script>


</div>

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
