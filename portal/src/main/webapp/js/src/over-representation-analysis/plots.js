/**
 * Created by suny1 on 5/28/15.
 */
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/******************************************************************************************
 * Creating RPPA plots for "enrichments" tab
 * @author Yichao Sun
 *
 * This code performs the following functions:
 * 1. Generates boxplots for altered and unaltered group using D3 when user expanding any row in the table
 * 2. Annotates the plots with caseId, alteration details, specific value
 ******************************************************************************************/

var orPlots = (function() {

    var div_id, gene, profile_type, profile_id, profile_name, p_value;
    var dotsArr = [], xAxisTextSet = ["Altered", "Unaltered"];

    var elem = {
            svg : "",
            xScale : "",
            yScale : "",
            xAxis : "",
            yAxis : "",
            dotsGroup : ""   //Group of single Dots
        }, settings = {
            canvas_width: 720,
            canvas_height: 600,
            dots_fill_color: "#58ACFA",
            dots_stroke_color: "#0174DF"
        };

    function data_process(result) {

        var oncoprintData = or_util.sortOncoprintData(PortalDataColl.getOncoprintData());

        dotsArr = [];
        dotsArr.length = 0;
        $.each(Object.keys(result[gene]), function(index, _sampleId) {
            var _obj = result[gene][_sampleId];
            var _datum = {};
            if (!isNaN(_obj[profile_id])) {

                if ($.inArray(_sampleId, window.PortalGlobals.getAlteredSampleIdArray()) !== -1) { //sample is altered
                    _datum.x_val = 0;
                } else { //sample is unaltered
                    _datum.x_val = 1;
                }

                //if rna seq data, apply log 10
                if (profile_id.indexOf("rna_seq") !== -1 && _datum.y_val !== 0) _datum.y_val = Math.log(parseFloat(_obj[profile_id]) + 1.0) / Math.log(2);
                else _datum.y_val = parseFloat(_obj[profile_id]);

                _datum.case_id = _sampleId;
                if ($.inArray(_sampleId, window.PortalGlobals.getAlteredSampleIdArray()) !== -1) { //sample is altered

                    $.each(oncoprintData, function(outer_index, outer_obj) {
                        $.each(outer_obj.values, function(inner_key, inner_obj) {
                            if (_sampleId === inner_obj.sample) {
                                var _str = "";
                                if (inner_obj.hasOwnProperty("mutation")) {
                                    _str += " MUT;";
                                }
                                if (inner_obj.hasOwnProperty("cna")) {
                                    if (inner_obj.cna === "AMPLIFIED") {
                                        _str += " AMP;";
                                    } else if (inner_obj.cna === "GAINED") {
                                        _str += " GAIN;";
                                    } else if (inner_obj.cna === "HEMIZYGOUSLYDELETED") {
                                        _str += " HETLOSS;";
                                    } else if (inner_obj.cna === "HOMODELETED") {
                                        _str += " HOMDEL;";
                                    }
                                }
                                if (inner_obj.hasOwnProperty("mrna")) {
                                    if (inner_obj.mrna === "UPREGULATED") {
                                        _str += " UP;";
                                    } else if (inner_obj.mrna === "DOWNREGULATED") {
                                        _str += " DOWN;";
                                    }
                                }
                                if (inner_obj.hasOwnProperty("rppa")) {
                                    if (inner_obj.rppa === "UPREGULATED") {
                                        _str += " RPPA-UP;";
                                    } else if (inner_obj.rppa === "DOWNREGULATED") {
                                        _str += " RPPA-DOWN;";
                                    }
                                }
                                if (_str !== "") {
                                    _str = inner_obj.gene + ":" + _str;
                                    _datum.alteration = _str;
                                }
                            }
                        });
                    });
                }
                dotsArr.push(_datum);
            }
        });
        $.each(dotsArr, function(_index, obj) {
            console.log(obj.y_val);
        });

        generate_plots();
    };

    var generate_plots = function() {

        $("#" + div_id).empty();

        //attach headers & download button
        var _title = "Boxplots of " + profile_name + " data for altered and unaltered cases " +
            "<button id='" + div_id + "_enrichments_pdf_download'>PDF</button>";
        $("#" + div_id).append(_title);
        $("#" + div_id + "_enrichments_pdf_download").click(function() {
            var downloadOptions = {
                filename: "enrichments-plots.pdf",
                contentType: "application/pdf",
                servletName: "svgtopdf.do"
            };
            cbio.download.initDownload($("#" + div_id + " svg")[0], downloadOptions);
        });

        //init canvas
        elem.svg = d3.select("#" + div_id)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);

        //init axis scales
        elem.xScale = d3.scale.linear() //x axis scale
            .domain([-0.7, 1.7])
            .range([100, 600]);
        var _yValArr = []; //y axis scale
        $.each(dotsArr, function(index, val){
            _yValArr.push(val.y_val);
        });
        var _results = or_util.analyse_data(_yValArr);
        elem.yScale = d3.scale.linear()
            .domain([_results.min, _results.max])
            .range([520, 20]);
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom");
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .orient("left");

        //Draw axis
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 520)")
            .attr("class", "rppa-plots-x-axis-class")
            .call(elem.xAxis.ticks(xAxisTextSet.length))
            .selectAll("text")
            .data(xAxisTextSet)
            .style("font-family", "sans-serif")
            .style("font-size", "13px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black")
            .text(function(d){ return d; });
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 20)")
            .call(elem.xAxis.orient("bottom").ticks(0));
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(100, 0)")
            .attr("class", "rppa-plots-y-axis-class")
            .call(elem.yAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "13px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(600, 0)")
            .call(elem.yAxis.orient("left").ticks(0));

        //Append Axis Titles
        var axisTitleGroup = elem.svg.append("svg:g");
        axisTitleGroup.append("text")
            .attr("class", "rppa-plots-x-axis-title")
            .attr("x", 350)
            .attr("y", 580)
            .style("text-anchor", "middle")
            .style("font-size", "13px")
            .text("Query: " + window.PortalGlobals.getGeneListString() + " (p-Value: " + p_value + ")");
        axisTitleGroup.append("text")
            .attr("class", "rppa-plots-y-axis-title")
            .attr("transform", "rotate(-90)")
            .attr("x", -270)
            .attr("y", 45)
            .style("text-anchor", "middle")
            .style("font-size", "13px")
            .text(gene + ", " + profile_name);



        //box plots
        var boxPlotsElem = elem.svg.append("svg:g");
        for (var i = 0 ; i < 2; i++) {  //Just 0(altered) and 1(unaltered)
            var top;
            var bottom;
            var quan1;
            var quan2;
            var mean;
            var IQR;
            var scaled_y_arr = [];
            var tmp_y_arr = [];
            //Find the middle (vertical) line for one box plot
            var midLine = elem.xScale(i);
            //Find the max/min y value with certain x value;
            $.each(dotsArr, function (index, value) {
                if (value.x_val === i) {
                    tmp_y_arr.push(parseFloat(value.y_val));
                }
            });
            tmp_y_arr.sort(function (a, b) {
                return a - b;
            });
            if (tmp_y_arr.length === 0) { //Deal with individual data sub group
                //Skip: do nothing
            } else if (tmp_y_arr.length === 1) {
                mean = elem.yScale(tmp_y_arr[0]);
                boxPlotsElem.append("line")
                    .attr("x1", midLine - 30)
                    .attr("x2", midLine + 30)
                    .attr("y1", mean)
                    .attr("y2", mean)
                    .attr("stroke-width", 1)
                    .attr("stroke", "grey");
            } else {
                if (tmp_y_arr.length === 2) {
                    mean = elem.yScale((tmp_y_arr[0] + tmp_y_arr[1]) / 2);
                    quan1 = bottom = elem.yScale(tmp_y_arr[0]);
                    quan2 = top = elem.yScale(tmp_y_arr[1]);
                    IQR = Math.abs(quan2 - quan1);
                } else {
                    var yl = tmp_y_arr.length;
                    if (yl % 2 === 0) {
                        mean = elem.yScale((tmp_y_arr[(yl / 2) - 1] + tmp_y_arr[yl / 2]) / 2);
                        if (yl % 4 === 0) {
                            quan1 = elem.yScale((tmp_y_arr[(yl / 4) - 1] + tmp_y_arr[yl / 4]) / 2);
                            quan2 = elem.yScale((tmp_y_arr[(3 * yl / 4) - 1] + tmp_y_arr[3 * yl / 4]) / 2);
                        } else {
                            quan1 = elem.yScale(tmp_y_arr[Math.floor(yl / 4)]);
                            quan2 = elem.yScale(tmp_y_arr[Math.floor(3 * yl / 4)]);
                        }
                    } else {
                        mean = elem.yScale(tmp_y_arr[Math.floor(yl / 2)]);
                        var tmp_yl = Math.floor(yl / 2) + 1;
                        if (tmp_yl % 2 === 0) {
                            quan1 = elem.yScale((tmp_y_arr[tmp_yl / 2 - 1] + tmp_y_arr[tmp_yl / 2]) / 2);
                            quan2 = elem.yScale((tmp_y_arr[(3 * tmp_yl / 2) - 2] + tmp_y_arr[(3 * tmp_yl / 2) - 1]) / 2);
                        } else {
                            quan1 = elem.yScale(tmp_y_arr[Math.floor(tmp_yl / 2)]);
                            quan2 = elem.yScale(tmp_y_arr[tmp_yl - 1 + Math.floor(tmp_yl / 2)]);
                        }
                    }
                    for (var k = 0; k < tmp_y_arr.length; k++) {
                        scaled_y_arr[k] = parseFloat(elem.yScale(tmp_y_arr[k]));
                    }
                    scaled_y_arr.sort(function (a, b) {
                        return a - b;
                    });
                    IQR = Math.abs(quan2 - quan1);
                    var index_top = or_util.searchIndexTop(scaled_y_arr, (quan2 - 1.5 * IQR));
                    top = scaled_y_arr[index_top];
                    var index_bottom = or_util.searchIndexBottom(scaled_y_arr, (quan1 + 1.5 * IQR));
                    bottom = scaled_y_arr[index_bottom];
                }
                boxPlotsElem.append("rect")
                    .attr("x", midLine - 60)
                    .attr("y", quan2)
                    .attr("width", 120)
                    .attr("height", IQR)
                    .attr("fill", "none")
                    .attr("stroke-width", 1)
                    .attr("stroke", "#BDBDBD");
                boxPlotsElem.append("line")
                    .attr("x1", midLine - 60)
                    .attr("x2", midLine + 60)
                    .attr("y1", mean)
                    .attr("y2", mean)
                    .attr("stroke-width", 3)
                    .attr("stroke", "#BDBDBD");
                boxPlotsElem.append("line")
                    .attr("x1", midLine - 40)
                    .attr("x2", midLine + 40)
                    .attr("y1", top)
                    .attr("y2", top)
                    .attr("stroke-width", 1)
                    .attr("stroke", "#BDBDBD");
                boxPlotsElem.append("line")
                    .attr("x1", midLine - 40)
                    .attr("x2", midLine + 40)
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

        //draw dots
        elem.dotsGroup = elem.svg.append("svg:g");
        elem.dotsGroup.selectAll("path").remove();
        var ramRatio = 80;  //Noise
        elem.dotsGroup.selectAll("path")
            .data(dotsArr)
            .enter()
            .append("svg:path")
            .attr("transform", function(d){
                return "translate(" + (elem.xScale(d.x_val) + (Math.random() * ramRatio - ramRatio/2)) +
                       ", " + elem.yScale(d.y_val) + ")";
            })
            .attr("d", d3.svg.symbol()
                .size(20)
                .type("circle"))
            .attr("fill", settings.dots_fill_color)
            .attr("stroke", settings.dots_stroke_color)
            .attr("stroke-width", "1.2");

        //add qtips
        elem.dotsGroup.selectAll('path').each(
            function(d) {
                var content = "<font size='2'>";
                content += "<strong><a href='"
                    +cbio.util.getLinkToSampleView(cancer_study_id,d.case_id)
                    + "' target = '_blank'>" + d.case_id + "</a></strong><br>";
                if (profile_type === orAnalysis.profile_type.mrna) {
                    content += "mRNA expression: "
                } else if (profile_type === orAnalysis.profile_type.protein_exp) {
                    content += "RPPA score: "
                }
                content += "<strong>" + parseFloat(d.y_val).toFixed(3) + "</strong><br>";
                if (d.hasOwnProperty("alteration")) {
                    content += "Alteration(s): " + d.alteration;
                }
                content = content + "</font>";

                $(this).qtip(
                    {
                        content: {text: content},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right', viewport: $(window)}
                    }
                );

                var mouseOn = function() {
                    var dot = d3.select(this);
                    dot.transition()
                        .ease("elastic")
                        .duration(600)
                        .delay(100)
                        .attr("d", d3.svg.symbol().size(200).type("circle"));
                };

                var mouseOff = function() {
                    var dot = d3.select(this);
                    dot.transition()
                        .ease("elastic")//TODO: default d3 symbol is circle (coincidence!)
                        .duration(600)
                        .delay(100)
                        .attr("d", d3.svg.symbol().size(20).type("circle"));
                };
                elem.dotsGroup.selectAll("path").on("mouseover", mouseOn);
                elem.dotsGroup.selectAll("path").on("mouseout", mouseOff);
            }
        );

    };


    return {
        init: function(_div_id, _gene, _profile_type, _profile_id, _profile_name, _p_value) {

            div_id = _div_id;
            gene = _gene;
            profile_type = _profile_type;
            if (_profile_type === orAnalysis.profile_type.protein_exp) {
                profile_id = _profile_id.substring(0, _profile_id.length - 8);
            } else {
                profile_id = _profile_id;
            }
            profile_name = _profile_name;
            if (_p_value.indexOf("up1") !== -1) p_value = _p_value.replace("<img src=\"images/up1.png\"/>",  "");
            if (_p_value.indexOf("down1") !== -1) p_value = _p_value.replace("<img src=\"images/down1.png\"/>",  "");

            var params_get_profile_data = {
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene_list: gene,
                genetic_profile_id: profile_id,
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey()
            }
            $.post("getProfileData.json", params_get_profile_data, data_process, "json");

        }
    };

}());