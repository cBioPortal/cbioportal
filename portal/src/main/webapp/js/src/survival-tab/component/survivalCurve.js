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

var SurvivalCurve = function() {

    //Instances
    var elem = "",
        settings = "",
        text = "",
        style = "",
        divs = "",
        vals = "";

    //Each curve will have unique ID which will be used to add/remove curve
    var curvesInfo = {};

    //qtip func
    var qtipFunc = {};

    function initCanvas() {
        $('#' + divs.curveDivId).empty();
        elem.svg = d3.select("#" + divs.curveDivId)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);
        elem.curve = elem.svg.append("g");
        //elem.dots = elem.svg.append("g"); //the invisible dots
        elem.censoredDots = elem.svg.append("g").attr("id", "crossDots");
    }

    function initAxis(_inputArr) {
        var _dataset = [];
        //tick format changed from .1% to %, requested by Niki, used in
        //Study View
        var formatAsPercentage = d3.format("%");
        $.each(_inputArr, function(index, obj) {
            var data = obj.data;
            $.each(data.getData(), function(index, d) {
                _dataset.push(d.time);
            });
        });
        elem.xScale = d3.scale.linear()
            .domain([0, d3.max(_dataset) + 0.1 * d3.max(_dataset)])
            .range([settings.chart_left, settings.chart_left + settings.chart_width]);
        elem.yScale = d3.scale.linear()
            .domain([-0.03, 1.05]) //fixed to be 0-1
            .range([settings.chart_top + settings.chart_height, settings.chart_top]);
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom")
            .tickSize(6, 0, 0);
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .tickFormat(formatAsPercentage)
            .orient("left")
            .tickSize(6, 0, 0);
    }

    function initLines() {
        elem.line = d3.svg.line()
            .interpolate("step-after")
            .x(function(d) { return elem.xScale(d.time); })
            .y(function(d) { return elem.yScale(d.survival_rate); });
    }

    //Append a virtual point for time zero if needed (no actual data point at time zero, therefore cause the graph not starting from 0)
    function appendZeroPoint(_num_at_risk) {
        var datum = {
            case_id: "",
            time: "",    //num of months
            status: "", //os: DECEASED-->1, LIVING-->0; dfs: Recurred/Progressed --> 1, Disease Free-->0
            num_at_risk: -1,
            survival_rate: 0
        };
        var _datum = jQuery.extend(true, {}, datum);
        _datum.case_id = "NA";
        _datum.time = 0;
        _datum.status = "NA";
        _datum.num_at_risk = _num_at_risk;
        _datum.survival_rate = 1;
        return _datum;
    }

    function drawLines(data, opts, _curveId) {
        if (data !== null && data.length > 0) {
            if (data[0].time !== 0) {
                data.unshift(appendZeroPoint(data[0].num_at_risk));
            }
            elem.curve = elem.svg.append("path")
                .attr('id', _curveId+"-line")
                .attr("d", elem.line(data))
                .style("fill", "none")
                .style("stroke", opts.line_color);
        }
    }

    function drawInvisiableDots(_index, data, _color) {
        elem.dots[_index].selectAll("path")
            .data(data)
            .enter()
            .append("svg:path")
            .attr("d", d3.svg.symbol()
                .size(400)
                .type("circle"))
            .attr("transform", function(d){
                return "translate(" + elem.xScale(d.time) + ", " + elem.yScale(d.survival_rate) + ")";
            })
            .attr("fill", _color)
            .style("opacity", 0);
    }

    function addQtips(_index) {
        var mouseOn = function(d) {
            var dot = d3.select(this);
            dot.transition()
                .duration(400)
                .style("opacity", .9);

            if(! $(this).data('qtip' )) {
                //TODO: need to find a better way to grab cancer study ID. 
                //QuerySession is only available after submitting query.
                //cancerStudyId is only available in study view
                //cancer_study_id and cancer_study_id_selected are only available in certain pages.
                //But they all point to same attribute.
                var cancerStudy = window.QuerySession ? window.QuerySession.getCancerStudyIds()[0] : (window.cancerStudyId || window.cancer_study_id || window.cancer_study_id_selected);
                var content = "<font size='2'>";
                content += text.qTips.id + ": " + "<strong><a href='"
                        + qtipFunc(cancerStudy, d.case_id)
                        + "' target='_blank'>" + d.case_id + "</a></strong><br>";
                content += text.qTips.estimation + ": <strong>" + (d.survival_rate * 100).toFixed(2) + "%</strong><br>";
                if (d.status === "0") { // If censored, mark it
                    content += text.qTips.censoredEvent + ": <strong>" + d.time.toFixed(2) + " </strong>months (censored)<br>";
                } else { // status is 1, means event occured
                    content += text.qTips.failureEvent + ": <strong>" + d.time.toFixed(2) + " </strong>months<br>";
                }
                content += "</font>";

                $(this).qtip(
                    {
                        content: {text: content},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow qtip-wide'},
                        show: {
                            event: "mouseover",
                            ready: true
                        },
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right'}
                    }
                );
            }
        };

        var mouseOff = function() {
            var dot = d3.select(this);
            dot.transition()
                .duration(400)
                .style("opacity", 0);
        };

        elem.dots[_index].selectAll("path").on("mouseover", mouseOn);
        elem.dots[_index].selectAll("path").on("mouseout", mouseOff);
    }

    function appendAxis(elemAxisX, elemAxisY) {
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .attr("class", "survival-curve-x-axis-class")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, " + (settings.chart_top + settings.chart_height) + ")")
            .call(elemAxisX);
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, " + settings.chart_top + ")")
            .call(elemAxisX.orient("bottom").ticks(0));
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .attr("class", "survival-curve-y-axis-class")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + settings.chart_left + ", 0)")
            .call(elemAxisY);
        elem.svg.append("g")
            .style("stroke-width", style.axis_stroke_width)
            .style("fill", "none")
            .style("stroke", style.axis_color)
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + (settings.chart_left + settings.chart_width) + ", 0)")
            .call(elemAxisY.orient("left").ticks(0));
        elem.svg.selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "11px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
    }

    function drawCensoredDots(data, opts, curveId) {
        // crossDots specifically for the curve for easier deletion
        // changed two separate lines to a single cross symbol
        var curveCrossdots = elem.censoredDots.append("g").attr("id", curveId+"-crossdots");
        curveCrossdots.selectAll("path")
            .data(data)
            .enter()
            .append("path")
            .filter(function(d){
                return d.status==="0";
            })
            .attr("transform", function(d) {
                return "translate(" + elem.xScale(d.time) + "," + elem.yScale(d.survival_rate) + ")";
            })
            .attr("d", d3.svg.symbol().type("cross")
                .size(function(d){
                    return 25;
                })
            )
            .attr("fill", opts.line_color);

    }

    function addLegends(_inputArr) {
        var legends_text = [];
        $.each(_inputArr, function(index, obj) {
            var _tmp = {};
            _tmp.text = obj.settings.legend;
            _tmp.color = obj.settings.line_color;
            legends_text.push(_tmp);
        });

        var legend = elem.svg.selectAll(".legend")
            .data(legends_text)
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) {
                return "translate(" + (settings.chart_left + settings.chart_width + 15) + ", " + (70 + i * 15) + ")";
            });

        legend.append("path")
            .attr("width", 18)
            .attr("height", 18)
            .attr("d", d3.svg.symbol()
                .size(60)
                .type(function(d) { return "square"; }))
            .attr("fill", function (d) { return d.color; })
            .attr("stroke", "black")
            .attr("stroke-width",.9);

        legend.append("text")
            .attr("x", 15)
            .attr("y", 4)
            .style("text-anchor", "front")
            .text(function(d) { return d.text; });

    }

    function appendAxisTitles(xTitle, yTitle) {
        elem.svg.append("text")
            .attr("class", "label")
            .attr("x", style.axisX_title_pos_x)
            .attr("y", style.axisX_title_pos_y)
            .style("text-anchor", "middle")
            .style("font-size", "11px")
            .style("font-weight","bold")
            .text(xTitle);
        elem.svg.append("text")
            .attr("class", "label")
            .attr("transform", "rotate(-90)")
            .attr("x", style.axisY_title_pos_x)
            .attr("y", style.axisY_title_pos_y)
            .style("text-anchor", "middle")
            .style("font-size", "11px")
            .style("font-weight","bold")
            .text(yTitle);
    }

    function addPvals(_pval) {
        elem.svg.append("text")
            .attr("class","pval")
            .attr("x", settings.pval_x)
            .attr("y", settings.pval_y)
            .attr("font-size", style.pval_font_size)
            .attr("font-style", style.pval_font_style)
            .style("text-anchor", "front")
            .text(text.pValTitle + cbio.util.toPrecision(Number(_pval), 3, 0.001));
    }

    function updatePvals(pval) {
        elem.svg.select('.pval')
            .text(text.pValTitle + cbio.util.toPrecision(Number(pval), 3, 0.0001));
    }

    function appendInfoTable(_infoTableInputArr) {
        $("#" + divs.infoTableDivId).empty();
        $("#" + divs.infoTableDivId).append("<tr>" +
            "<td style='width: 600px; text-align:left;'></td>" +
            "<td style='width: 200px;'>" + text.infoTableTitles.total_cases + "</td>" +
            "<td style='width: 200px;'>" + text.infoTableTitles.num_of_events_cases + "</td>" +
            "<td style='width: 200px;'>" + text.infoTableTitles.median + "</td>" +
            "</tr>");
        $.each(_infoTableInputArr, function(index, obj) {
            $("#" + divs.infoTableDivId).append("<tr>" +
                                                "<td>" + obj.groupName + "</td>" +
                                                "<td><b>" + obj.num_cases + "</b></td>" +
                                                "<td><b>" + obj.num_of_events_cases + "</b></td>" +
                                                "<td><b>" + obj.median + "</b></td>" +
                                                "</tr>");
        });
    }

    function appendImgConverter(_inputArr) {
        $('#' + divs.headerDivId).append("<button id='" + divs.curveDivId + "_svg_download' style='font-size:12px;'>SVG</button>");
        $('#' + divs.headerDivId).append("<button id='" + divs.curveDivId + "_pdf_download' style='font-size:12px;'>PDF</button>");
        $('#' + divs.headerDivId).append("<button id='" + divs.curveDivId + "_data_download' style='font-size:12px;'>Data</button>");
        $("#" + divs.curveDivId + "_svg_download").click(function() {
            var xmlSerializer = new XMLSerializer();
            var download_str = cbio.download.addSvgHeader(xmlSerializer.serializeToString($("#" + divs.curveDivId + " svg")[0]));
            cbio.download.clientSideDownload([download_str], "survival_study.svg", "application/svg+xml");
        });
        $("#" + divs.curveDivId + "_pdf_download").click(function() {
            var downloadOptions = {
                filename: "survival_study.pdf",
                contentType: "application/pdf",
                servletName: "svgtopdf.do"
            };
           cbio.download.initDownload($("#" + divs.curveDivId + " svg")[0], downloadOptions);
        });
        $("#" + divs.curveDivId + "_data_download").click(function() {
            var final_str = "";
            var file_name = "";
            var div_id = this.id;
            if (div_id.indexOf("os") !== -1) {
                final_str += "Overall Survival Kaplan-Meier Estimate\n";
                file_name = "os_survival_data.txt";
            } else if (div_id.indexOf("dfs") !== -1) {
                final_str += "Disease Free Survival Kaplan-Meier Estimate\n";
                file_name = "dfs_survival_data.txt";
            }
            _.each(_inputArr, function(curve_obj) {
                var _str = "\n" + curve_obj.settings.legend + "\n";
                _str += "Case ID" + "\t" + "Number at Risk" + "\t" + "Status" + "\t" + "Survival Rate" + "\t" + "Time (months)" + "\n" ;
                _.each(curve_obj.data.getData(), function(data_obj) {
                    //translate status
                    var _txt_status = "";
                    if (div_id.indexOf("os") !== -1) {
                        if (data_obj.status === "0") _txt_status = "censored";
                        else if (data_obj.status === "1") _txt_status = "deceased";
                    } else if (div_id.indexOf("dfs") !== -1) {
                        if (data_obj.status === "0") _txt_status = "censored";
                        else if (data_obj.status === "1") _txt_status = "relapsed";
                    }
                    //assemble
                    _str += data_obj.case_id + "\t" +
                            data_obj.num_at_risk + "\t" +
                            _txt_status + "\t" +
                            data_obj.survival_rate + "\t" +
                            data_obj.time + "\n";
                });
                final_str += _str;
            });
            cbio.download.clientSideDownload([final_str], file_name);
        });
    }

    function drawCurve(_inputArr, _obj){
        var data = _obj.data;
        var opts = _obj.settings;
        var _curve = {};
        _curve.id = opts.curveId;
        _curve.color = opts.line_color;
        curvesInfo[_curve.id] = _curve;
        elem.dots[_curve.id] = elem.svg.append("g").attr('id', _curve.id+"-dots"); //the invisible dots
        initLines();
        drawLines(data.getData(), opts, _curve.id);

        //First element is used to draw lines and its case_id is NA, this dot
        //will not be needed for drawing censored dots and invisible dots.
        //Then remove move it in here.
        data.getData().shift();
        drawCensoredDots(data.getData(), opts, _curve.id);
        drawInvisiableDots(_curve.id, data.getData(), opts.mouseover_color);
        addQtips(_curve.id);

        if (settings.include_legend) {
            addLegends(_inputArr);
        }
    }

    function removeCurve(_curveId){
        d3.selectAll('#' + _curveId + '-dots').remove();
        d3.selectAll('#' + _curveId + '-line').remove();
        // remove the crossdots for the curveId
        d3.selectAll('#' + _curveId + "-crossdots").remove();

        delete curvesInfo[_curveId];
        //TODO: Add redraw curve label function
    }

    function updateSettings(newSettings){
        if(newSettings){
            for(var setting in newSettings) {
                settings[setting] = newSettings[setting];
            }
        }
    }

    function updatePval(_pval) {
        if (settings.include_pvalue && !isNaN(_pval)) {
            if(elem.svg.select(".pval").empty()) {
                addPvals(_pval);
            }else{
                updatePvals(_pval);
            }
        }else{
            if(!elem.svg.select(".pval").empty()) {
                elem.svg.select(".pval").remove();
            }
        }
    }

    function addCurve(_obj){
        if(!(_obj.settings.curveId in curvesInfo)){
            drawCurve([_obj], _obj);
        }else{
            console.log("%c Error: Curve ID exists", "color:red");
        }
    }

    return {
        init: function(_inputArr, _opts) {
            //Place parameters
            elem = _opts.elem;
            settings = _opts.settings;
            text = _opts.text;
            style = _opts.style;
            divs = _opts.divs;
            vals = _opts.vals;
            qtipFunc = _opts.qtipFunc;

            //Init and Render
            var _empty_data = false;
            $.each(_inputArr, function(index, obj) {
                if (obj.data.getData().length === 0) {
                    _empty_data = true;
                    return false;
                }
            });

            if (!_empty_data) {
                    initCanvas();
                    initAxis(_inputArr);
                    appendAxis(elem.xAxis, elem.yAxis);
                    appendAxisTitles(text.xTitle, text.yTitle);
                    $.each(_inputArr, function(index, obj) {
                        drawCurve(_inputArr, obj);
                    });
                    appendImgConverter(_inputArr);
                    if (_opts.settings.include_info_table) {
                        var _infoTableInputArr = [];
                        $.each(_inputArr, function(index, obj) {
                            var _tmp = jQuery.extend(true, {}, obj.data.getStats());
                            _tmp.groupName = obj.settings.legend;
                            _infoTableInputArr.push(_tmp);
                        });
                        appendInfoTable(_infoTableInputArr);
                    }
                    if (_opts.settings.include_pvalue) {
                        addPvals(vals.pVal);
                    }
            } else {
                $("#" + divs.infoTableDivId).empty();
                $("#" + divs.infoTableDivId).append("<span style='margin: 20px; color: grey;'>Survival data not available</span>");
            }

        },
        updateView: function(data, opts){
            if(opts){
                updateSettings(opts.settings);
            }

            if(data){
                updatePval(data.pval);
            }
        },
        addCurve: addCurve,
        removeCurve: removeCurve
    };
};
