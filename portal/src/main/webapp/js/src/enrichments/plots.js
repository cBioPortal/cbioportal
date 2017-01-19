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
 * @author suny1@mskcc.org
 *
 * This code performs the following functions:
 * 1. Generates boxplots for altered and unaltered group using D3 when user expanding any row in the table
 * 2. Annotates the plots with caseId, alteration details, specific value
 ******************************************************************************************/

var enrichmentsTabPlots = (function() {

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
        canvas_width: 350,
        canvas_height: 350,
        dots_fill_color: "#58ACFA",
        dots_stroke_color: "#0174DF"
    };

    function data_process(result) {

	window.QuerySession.getOncoprintSampleGenomicEventData().then(function(data_by_line) {
		var order = {};
		var sample_ids = window.QuerySession.getSampleIds();
		for (var i=0; i<sample_ids.length; i++) {
			order[sample_ids[i]] = i;
		}
		var oncoprintData = _.sortBy(_.flatten(data_by_line.map(function(line) { return line.oncoprint_data; })), function(d) { return order[d.sample];});
		dotsArr = [];
		dotsArr.length = 0;
		window.QuerySession.getAlteredSamples().then(function(altered_sample_ids) {
			$.each(Object.keys(result[gene]), function(index, _sampleId) {
			    var _obj = result[gene][_sampleId];
			    var _datum = {};
			    _datum.alteration = "";
			    if (!isNaN(_obj[profile_id]) && _obj[profile_id] !== 'NaN' && _obj[profile_id] !== '') {
				if ($.inArray(_sampleId, altered_sample_ids) !== -1) { //sample is altered
				    _datum.x_val = 0;
				} else { //sample is unaltered
				    _datum.x_val = 1;
				}

				//if rna seq data, apply log 10
				if (profile_id.indexOf("rna_seq") !== -1 && _obj[profile_id] !== '0') _datum.y_val = Math.log(parseFloat(_obj[profile_id]) + 1.0) / Math.log(2);
				else _datum.y_val = parseFloat(_obj[profile_id]);

				_datum.case_id = _sampleId;
				if ($.inArray(_sampleId, altered_sample_ids) !== -1) { //sample is altered

					//iterate over items (alteration info for each gene in each sample):
				    $.each(oncoprintData, function(inner_key, inner_obj) {
				    	//if sample is the current sample, then analyze alterations:
					    if (_sampleId === inner_obj.sample) {
					    	var _str = "";
							if (typeof inner_obj.disp_mut !== "undefined") {
							    _str += " MUT;";
							}
							if (typeof inner_obj.disp_cna !== "undefined") {
							    if (inner_obj.disp_cna === "amp") {
								_str += " AMP;";
							    } else if (inner_obj.disp_cna === "gain") {
								_str += " GAIN;";
							    } else if (inner_obj.disp_cna === "hetloss") {
								_str += " HETLOSS;";
							    } else if (inner_obj.disp_cna === "homdel") {
								_str += " HOMDEL;";
							    }
							}
							if (typeof inner_obj.disp_mrna !== "undefined") {
							    if (inner_obj.disp_mrna === "up") {
								_str += " UP;";
							    } else if (inner_obj.disp_mrna === "down") {
								_str += " DOWN;";
							    }
							}
							if (typeof inner_obj.disp_prot !== "undefined") {
							    if (inner_obj.disp_prot === "up") {
								_str += " RPPA-UP;";
							    } else if (inner_obj.disp_prot === "down") {
								_str += " RPPA-DOWN;";
							    }
							}
							if (_str !== "") {
							    _str = inner_obj.gene + ":" + _str;
							    //record all alterations found for this sample:
							    _datum.alteration += _str;
							}
					    }
				    });
				}
				dotsArr.push(_datum);
			    }
			});
			generate_plots();
		});
	});

    };

    var generate_plots = function() {

        $("#" + div_id).empty();

        //attach headers & download button
        var _title = "Boxplots of " + profile_name + " data for altered and unaltered cases " +
            "<button id='" + div_id + "_enrichments_pdf_download'>PDF</button>" +
            "<button id='" + div_id + "_enrichments_svg_download'>SVG</button>" +
            "<button id='" + div_id + "_enrichments_data_download'>Data</button>";
        $("#" + div_id).append(_title);
        $("#" + div_id + "_enrichments_pdf_download").click(function() {
            var downloadOptions = {
                filename: "enrichments-plots.pdf",
                contentType: "application/pdf",
                servletName: "svgtopdf.do"
            };
            cbio.download.initDownload($("#" + div_id + " svg")[0], downloadOptions);
        });
        $("#" + div_id + "_enrichments_svg_download").click(function() {
            var xmlSerializer = new XMLSerializer();
            var download_str = cbio.download.addSvgHeader(xmlSerializer.serializeToString($("#" + div_id + " svg")[0]));
            cbio.download.clientSideDownload([download_str], "enrichments-plots.svg", "application/svg+xml");
        });
        $("#" + div_id + "_enrichments_data_download").click(function() {
            cbio.download.clientSideDownload([get_tab_delimited_data()], "enrichments-plots-data.txt");
        });

        function get_tab_delimited_data() {
            var result_str = "Sample Id" + "\t" + gene + ", " + profile_name + "\t" + "Alteration" + "\n";
            _.each(dotsArr, function(dot) {
                if (dot.hasOwnProperty("alteration")) {
                    result_str += dot.case_id + "\t" + dot.y_val + "\t" + dot.alteration + "\n";
                } else {
                    result_str += dot.case_id + "\t" + dot.y_val + "\t" + "Non" + "\n";
                }
            });
            return result_str;
        }

        //init canvas
        elem.svg = d3.select("#" + div_id)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);

        //init axis scales
        elem.xScale = d3.scale.linear() //x axis scale
            .domain([-0.7, 1.7])
            .range([80, 280]);
        var _yValArr = []; //y axis scale
        $.each(dotsArr, function(index, val){
            _yValArr.push(val.y_val);
        });
        var _results = enrichmentsTabUtil.analyse_data(_yValArr);
        elem.yScale = d3.scale.linear()
            .domain([_results.min, _results.max])
            .range([220, 20]);
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
            .attr("transform", "translate(0, 220)")
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
            .attr("transform", "translate(80, 0)")
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
            .attr("transform", "translate(280, 0)")
            .call(elem.yAxis.orient("left").ticks(0));

        //Append Axis Titles
        var axisTitleGroup = elem.svg.append("svg:g");
        axisTitleGroup.append("text")
            .attr("class", "rppa-plots-x-axis-title")
            .attr("x", 180)
            .attr("y", 270)
            .style("text-anchor", "middle")
            .style("font-size", "13px")
            .text("Query: " + window.QuerySession.getQueryGenes().join(" ") + " (p-Value: " + cbio.util.toPrecision(p_value, 3, 0.01) + ")");
        axisTitleGroup.append("text")
            .attr("class", "rppa-plots-y-axis-title")
            .attr("transform", "rotate(-90)")
            .attr("x", -140)
            .attr("y", 25)
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
                    .attr("x1", midLine - 10)
                    .attr("x2", midLine + 10)
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
                    var index_top = enrichmentsTabUtil.searchIndexTop(scaled_y_arr, (quan2 - 1.5 * IQR));
                    top = scaled_y_arr[index_top];
                    var index_bottom = enrichmentsTabUtil.searchIndexBottom(scaled_y_arr, (quan1 + 1.5 * IQR));
                    bottom = scaled_y_arr[index_bottom];
                }
                boxPlotsElem.append("rect")
                    .attr("x", midLine - 30)
                    .attr("y", quan2)
                    .attr("width", 60)
                    .attr("height", IQR)
                    .attr("fill", "none")
                    .attr("stroke-width", 1)
                    .attr("stroke", "#BDBDBD");
                boxPlotsElem.append("line")
                    .attr("x1", midLine - 30)
                    .attr("x2", midLine + 30)
                    .attr("y1", mean)
                    .attr("y2", mean)
                    .attr("stroke-width", 3)
                    .attr("stroke", "#BDBDBD");
                boxPlotsElem.append("line")
                    .attr("x1", midLine - 15)
                    .attr("x2", midLine + 15)
                    .attr("y1", top)
                    .attr("y2", top)
                    .attr("stroke-width", 1)
                    .attr("stroke", "#BDBDBD");
                boxPlotsElem.append("line")
                    .attr("x1", midLine - 15)
                    .attr("x2", midLine + 15)
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
        var ramRatio = 40;  //Noise
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
                    +cbio.util.getLinkToSampleView(window.QuerySession.getCancerStudyIds()[0],d.case_id)
                    + "' target = '_blank'>" + d.case_id + "</a></strong><br>";
                if (profile_type === enrichmentsTabSettings.profile_type.mrna) {
                    content += "mRNA expression: ";
                } else if (profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                    if ($("#" + enrichmentsTabSettings.ids.sub_tab_protein_exp + enrichmentsTabSettings.postfix.protein_exp_sub_tab_profile_selection_dropdown_menu).val().indexOf("ms_abundance") !== -1) {
                        content += "Mass spec: ";
                    } else if ($("#" + enrichmentsTabSettings.ids.sub_tab_protein_exp + enrichmentsTabSettings.postfix.protein_exp_sub_tab_profile_selection_dropdown_menu).val().indexOf("rppa") !== -1){
                        content += "RPPA score: ";
                    }
                }
                content += "<strong>" + parseFloat(d.y_val).toFixed(3) + "</strong><br>";
                if (d.hasOwnProperty("alteration")) {
                    content += "Alteration(s): " + d.alteration;
                }
                content = content + "</font>";
                //make qtip for an element on first mouseenter:
                cbio.util.addTargetedQTip($(this), { content: {text: content} });
            }
        );

        //Add nice resize effect when item is hovered:
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

    };


    return {
        init: function(_div_id, _gene, _profile_type, _profile_id, _profile_name, _p_value) {

            div_id = _div_id;
            gene = _gene;
            profile_type = _profile_type;
            profile_id = _profile_id;
            profile_name = _profile_name;
            if (_p_value.toString().indexOf("up1") !== -1) p_value = _p_value.replace("<img src=\"images/up1.png\"/>",  "");
            if (_p_value.toString().indexOf("down1") !== -1) p_value = _p_value.replace("<img src=\"images/down1.png\"/>",  "");
            else p_value = _p_value;

            var params_get_profile_data = {
                cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
                gene_list: gene,
                genetic_profile_id: profile_id,
                case_set_id: window.QuerySession.getCaseSetId(),
                case_ids_key: window.QuerySession.getCaseIdsKey()
            }
            $.post("getProfileData.json", params_get_profile_data, data_process, "json");

        }
    };

}());