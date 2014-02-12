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

/****************************************************************************************************
 * Creating overall survival and disease free curves for the survival tab
 * @author Yichao Sun
 * @date Sep 2013
 *
 * This code performs the following functions:
 * 1. Calculate the survival rates for each time point using kaplan-meier estimator
 * 2. Generate the curves using d3 line charts w/ mouse over for each time point
 * 3. Display basic information of main query: gene set, nubmer of cases
 * 4. Calculate interested values from the curve: p-value(log-rank test), median, 0.95lcl, 0.95ucl
 *
 ****************************************************************************************************/

var survivalCurves = (function() {

    var util = (function() {

        function sortByAttribute(objs, attrName) {
            function compare(a,b) {
                if (a[attrName] < b[attrName])
                    return -1;
                if (a[attrName] > b[attrName])
                    return 1;
                return 0;
            }
            objs.sort(compare);
            return objs;
        }

        return {
            sortByAttribute: sortByAttribute
        }

    }());

    var data  = (function() {

        var datum = {
                case_id: "",
                time: "",    //num of months
                status: "", //os: DECEASED-->1, LIVING-->0; dfs: Recurred/Progressed --> 1, Disease Free-->0
                num_at_risk: -1,
                survival_rate: 0
            },
            os_altered_group = [],
            os_unaltered_group = [],
            dfs_altered_group = [],
            dfs_unaltered_group = [],
            stat_datum = {
                pVal: 0,
                num_altered_cases: 0,
                num_unaltered_cases: 0,
                num_of_events_altered_cases: 0,
                num_of_events_unaltered_cases: 0,
                altered_median: 0,
                unaltered_median: 0
            },
            stat_values = {};

        var totalAlter = 0,
            totalUnalter = 0;

        //Count the total number of altered and unaltered cases
        function cntAlter(caseLists) {
            for (var key in caseLists) {
                if (caseLists[key] === "altered") totalAlter += 1;
                else if (caseLists[key] === "unaltered") totalUnalter += 1;
            }
        }

        //Settle the overall survival datum group
        //order by time, filtered NA cases and add on num of risk for each time point
        function setOSGroups(result, caseLists) {
            var _totalAlter = 0,
                _totalUnalter = 0;

            for (var caseId in result) {
                if (result.hasOwnProperty(caseId) && (result[caseId] !== "")) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.case_id = result[caseId].case_id;
                    _datum.time = result[caseId].os_months;
                    _datum.status = result[caseId].os_status;
                    if (_datum.time !== "NA" && _datum.status !== "NA") {
                        if (caseLists[caseId] === "altered") {
                            os_altered_group.push(_datum);
                            _totalAlter += 1;
                        } else if (caseLists[caseId] === "unaltered") {
                            os_unaltered_group.push(_datum);
                            _totalUnalter += 1;
                        }
                    }
                }
            }
            util.sortByAttribute(os_altered_group, "time");
            util.sortByAttribute(os_unaltered_group, "time");

            for (var i in os_altered_group) {
                os_altered_group[i].num_at_risk = _totalAlter;
                _totalAlter += -1;
            }
            for (var i in os_unaltered_group) {
                os_unaltered_group[i].num_at_risk = _totalUnalter;
                _totalUnalter += -1;
            }
        }

        //Settle the disease free survival datum group
        //order by time, filtered NA cases and add on num of risk for each time point
        function setDFSGroups(result, caseLists) {
            var _totalAlter = 0,
                _totalUnalter = 0;

            for (var caseId in result) {
                if (result.hasOwnProperty(caseId) && (result[caseId] !== "")) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.case_id = result[caseId].case_id;
                    _datum.time = result[caseId].dfs_months;
                    _datum.status = result[caseId].dfs_status;
                    if (_datum.time !== "NA" && _datum.status !== "NA") {
                        if (caseLists[caseId] === "altered") {
                            dfs_altered_group.push(_datum);
                            _totalAlter += 1;
                        } else if (caseLists[caseId] === "unaltered") {
                            dfs_unaltered_group.push(_datum);
                            _totalUnalter += 1;
                        }
                    }
                }
            }

            util.sortByAttribute(dfs_altered_group, "time");
            util.sortByAttribute(dfs_unaltered_group, "time");

            for (var i in dfs_altered_group) {
                dfs_altered_group[i].num_at_risk = _totalAlter;
                _totalAlter += -1;
            }
            for (var i in dfs_unaltered_group) {
                dfs_unaltered_group[i].num_at_risk = _totalUnalter;
                _totalUnalter += -1;
            }
        }

        function calcOS() {
            kmEstimator.calc(os_altered_group);
            kmEstimator.calc(os_unaltered_group);
            var _os_stat_datum = jQuery.extend(true, {}, stat_datum);
            if (os_altered_group.length !== 0) {
                _os_stat_datum.num_altered_cases = os_altered_group.length;
            } else {
                _os_stat_datum.num_altered_cases = "NA";
            }
            if (os_unaltered_group.length !== 0) {
                _os_stat_datum.num_unaltered_cases = os_unaltered_group.length;
            } else {
                _os_stat_datum.num_unaltered_cases = "NA";
            }
            _os_stat_datum.num_of_events_altered_cases = countEvents(os_altered_group);
            _os_stat_datum.num_of_events_unaltered_cases = countEvents(os_unaltered_group);
            _os_stat_datum.altered_median = calcMedian(os_altered_group);
            _os_stat_datum.unaltered_median = calcMedian(os_unaltered_group);
            logRankTest.calc(os_altered_group, os_unaltered_group, "os");
            stat_values["os"] = _os_stat_datum;
        }

        function calcDFS() {
            kmEstimator.calc(dfs_altered_group);
            kmEstimator.calc(dfs_unaltered_group);
            var _dfs_stat_datum = jQuery.extend(true, {}, stat_datum);
            if (dfs_altered_group.length !== 0) {
                _dfs_stat_datum.num_altered_cases = dfs_altered_group.length;
            } else {
                _dfs_stat_datum.num_altered_cases = "NA";
            }
            if (dfs_unaltered_group.length !== 0) {
                _dfs_stat_datum.num_unaltered_cases = dfs_unaltered_group.length;
            } else {
                _dfs_stat_datum.num_unaltered_cases = "NA";
            }
            _dfs_stat_datum.num_of_events_altered_cases = countEvents(dfs_altered_group);
            _dfs_stat_datum.num_of_events_unaltered_cases = countEvents(dfs_unaltered_group);
            _dfs_stat_datum.altered_median = calcMedian(dfs_altered_group);
            _dfs_stat_datum.unaltered_median = calcMedian(dfs_unaltered_group);
            logRankTest.calc(dfs_altered_group, dfs_unaltered_group, "dfs");
            stat_values["dfs"] = _dfs_stat_datum;
        }

        function countEvents(inputArr) {
            if (inputArr.length !== 0) {
                var _cnt = 0;
                for (var i in inputArr) {
                    if (inputArr[i].status === "1") {
                        _cnt += 1;
                    }
                }
                return _cnt;
            }
            return "NA";
        }

        function calcMedian(inputArr) {
            if (inputArr.length !== 0) {
                var _mIndex = 0;
                for (var i in inputArr) {
                    if (inputArr[i].survival_rate <= 0.5) {
                        _mIndex = i;
                        break;
                    } else {
                        continue;
                    }
                }
                return parseFloat(inputArr[_mIndex].time).toFixed(2);
            }
            return "NA";
        }

        return {
            pValCallBack: function(pVal, type) {
                pVal = parseFloat(pVal).toFixed(6);
                if (type === "os") {
                    stat_values.os.pVal = pVal;
                    view.initOS();
                    view.generateOS();
                } else if (type === "dfs") {
                    stat_values.dfs.pVal = pVal;
                    view.initDFS();
                    view.generateDFS();
                }
            },
            init: function(result, caseLists) {
                cntAlter(caseLists);
                setOSGroups(result, caseLists);
                setDFSGroups(result, caseLists);
                if (os_altered_group.length === 0 && os_unaltered_group.length === 0 &&
                    dfs_altered_group.length === 0 && dfs_unaltered_group.length === 0) {
                    //$('#tab-survival').hide();
                    var tab = $('#tabs a').filter(function(){
                        return $(this).text() == "Survival";
                    }).parent();
                    tab.hide();
                } else {
                    if (os_altered_group.length !== 0 || os_unaltered_group.length !== 0) {
                        calcOS();
                    } else {
                        view.errorMsg("os");
                    }
                    if (dfs_altered_group.length !== 0 || dfs_unaltered_group.length !== 0) {
                        calcDFS();
                    } else {
                        view.errorMsg("dfs");
                    }
                }
            },
            getOSAlteredData: function() {
                return os_altered_group;
            },
            getOSUnalteredData: function() {
                return os_unaltered_group;
            },
            getDFSAlteredData: function() {
                return dfs_altered_group;
            },
            getDFSUnalteredData: function() {
                return dfs_unaltered_group;
            },
            getOsStats: function() {
                return stat_values.os;
            },
            getDfsStats: function() {
                return stat_values.dfs;
            }
        }
    }());

    var view = (function() {
        var elem = {
                svgOS : "",
                svgDFS: "",
                xScale : "",
                yScale : "",
                xAxisOS : "",
                yAxisOS : "",
                xAxisDFS : "",
                yAxisDFS : "",
                line: "",
                osAlterDots: "",
                osUnalterDots: "",
                dfsAlterDots: "",
                dfsUnalterDots: "",
                osAlterCensoredDots: "",
                osUnalterCensoredDots: "",
                dfsAlterCensoredDots: "",
                dfsUnalterCensoredDots: ""
            },
            settings = {
                canvas_width: 1000,
                canvas_height: 620,
                altered_line_color: "red",
                unaltered_line_color: "blue",
                altered_mouseover_color: "#F5BCA9",
                unaltered_mouseover_color: "#81BEF7"
            },
            text = {
                glyph1: "Cases with Alteration(s) in Query Gene(s)",
                glyph2: "Cases without Alteration(s) in Query Gene(s)",
                xTitle_os: "Months Survival",
                yTitle_os: "Surviving",
                xTitle_dfs: "Months Disease Free",
                yTitle_dfs: "Disease Free"
            },
            style = {
                censored_sign_size: 5,
                axis_stroke_width: 1,
                axisX_title_pos_x: 380,
                axisX_title_pos_y: 600,
                axisY_title_pos_x: -270,
                axisY_title_pos_y: 45,
                axis_color: "black"
            };


        function initOSCanvas() {
            $('#os_survival_curve').empty();
            elem.svgOS = d3.select("#os_survival_curve")
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
            elem.osAlterDots = elem.svgOS.append("g");
            elem.osUnalterDots = elem.svgOS.append("g");
            elem.osAlterCensoredDots = elem.svgOS.append("g");
            elem.osUnalterCensoredDots = elem.svgOS.append("g");
        }

        function initDFSCanvas() {
            $('#dfs_survival_curve').empty();
            elem.svgDFS = d3.select("#dfs_survival_curve")
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
            elem.dfsAlterDots = elem.svgDFS.append("g");
            elem.dfsUnalterDots = elem.svgDFS.append("g");
            elem.dfsAlterCensoredDots = elem.svgDFS.append("g");
            elem.dfsUnalterCensoredDots = elem.svgDFS.append("g");
        }

        function initAxis() {
            var _dataset = [];
            var formatAsPercentage = d3.format(".1%");
            _dataset.push(d3.max(data.getOSAlteredData(), function(d) { return d.time; }));
            _dataset.push(d3.max(data.getOSUnalteredData(), function(d) { return d.time; }));
            _dataset.push(d3.max(data.getDFSAlteredData(), function(d) { return d.time; }));
            _dataset.push(d3.max(data.getDFSUnalteredData(), function(d) { return d.time; }));
            elem.xScale = d3.scale.linear()
                .domain([0, d3.max(_dataset) + 0.1 * d3.max(_dataset)])
                .range([100, 700]);
            elem.yScale = d3.scale.linear()
                .domain([-0.03, 1.05]) //fixed to be 0-1
                .range([550, 50]);
            elem.xAxisOS = d3.svg.axis()
                .scale(elem.xScale)
                .orient("bottom")
                .tickSize(6, 0, 0);
            elem.yAxisOS = d3.svg.axis()
                .scale(elem.yScale)
                .tickFormat(formatAsPercentage)
                .orient("left")
                .tickSize(6, 0, 0);
            elem.xAxisDFS = d3.svg.axis()
                .scale(elem.xScale)
                .orient("bottom")
                .tickSize(6, 0, 0);
            elem.yAxisDFS = d3.svg.axis()
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

        function drawOSLines() {
            var _os_altered_data = data.getOSAlteredData();
            var _os_unaltered_data = data.getOSUnalteredData();
            if (_os_altered_data !== null) {
                if (_os_altered_data[0].time !== 0) {
                    _os_altered_data.unshift(appendZeroPoint(_os_altered_data[0].num_at_risk));
                }
                elem.svgOS.append("path")
                    .attr("d", elem.line(_os_altered_data))
                    .style("fill", "none")
                    .style("stroke", settings.altered_line_color);
            }
            if (_os_unaltered_data !== null) {
                if (_os_unaltered_data[0].time !== 0) {
                    _os_unaltered_data.unshift(appendZeroPoint(_os_unaltered_data[0].num_at_risk));
                }
                elem.svgOS.append("path")
                    .attr("d", elem.line(_os_unaltered_data))
                    .style("fill", "none")
                    .style("stroke", settings.unaltered_line_color);
            }
        }

        function drawDFSLines() {
            var _dfs_altered_data = data.getDFSAlteredData();
            var _dfs_unaltered_data = data.getDFSUnalteredData();
            if (_dfs_altered_data !== null) {
                if (_dfs_altered_data[0].time !== 0) {
                    _dfs_altered_data.unshift(appendZeroPoint(_dfs_altered_data[0].num_at_risk));
                }
                elem.svgDFS.append("path")
                    .attr("d", elem.line(_dfs_altered_data))
                    .style("fill", "none")
                    .style("stroke", settings.altered_line_color);
            }
            if (_dfs_unaltered_data !== null) {
                if (_dfs_unaltered_data[0].time !== 0) {
                    _dfs_unaltered_data.unshift(appendZeroPoint(_dfs_unaltered_data[0].num_at_risk));
                }
                elem.svgDFS.append("path")
                    .attr("d", elem.line(_dfs_unaltered_data))
                    .style("fill", "none")
                    .style("stroke", settings.unaltered_line_color);
            }
        }

        function drawInvisiableDots(svg, color, data) {
            svg.selectAll("path")
                .data(data)
                .enter()
                .append("svg:path")
                .attr("d", d3.svg.symbol()
                    .size(400)
                    .type("circle"))
                .attr("transform", function(d){
                    return "translate(" + elem.xScale(d.time) + ", " + elem.yScale(d.survival_rate) + ")";
                })
                .attr("fill", color)
                .style("opacity", 0);
        }

        function addQtips(svg, type) {
            svg.selectAll('path').each(
                function(d) {
                    var content = "<font size='2'>";
                    content += "Case id: " + "<strong><a href='tumormap.do?case_id=" + d.case_id +
                        "&cancer_study_id=" + cancer_study_id + "' target='_blank'>" + d.case_id + "</a></strong><br>";
                    if (type === "os") {
                        content += "Survival estimate: <strong>" + (d.survival_rate * 100).toFixed(2) + "%</strong><br>";
                    } else if (type === "dfs") {
                        content += "Disease free estimate: <strong>" + (d.survival_rate * 100).toFixed(2) + "%</strong><br>";
                    }
                    if (d.status === "0") { // If censored, mark it
                        content += "Time of last observation: <strong>" + d.time.toFixed(2) + " </strong>months (censored)<br>";
                    } else {
                        if (type === "os") {
                            content += "Time of death: <strong>" + d.time.toFixed(2) + " </strong>months<br>";
                        } else if (type === "dfs") {
                            content += "Time of relapse: <strong>" + d.time.toFixed(2) + " </strong>months<br>";
                        }
                    }
                    content += "</font>";


                    $(this).qtip(
                        {
                            content: {text: content},
                            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow qtip-wide'},
                            show: {event: "mouseover"},
                            hide: {fixed:true, delay: 100, event: "mouseout"},
                            position: {my:'left bottom',at:'top right'}
                        }
                    );

                    var mouseOn = function() {
                        var dot = d3.select(this);
                        dot.transition()
                            .duration(400)
                            .style("opacity", .9);
                    };

                    var mouseOff = function() {
                        var dot = d3.select(this);
                        dot.transition()
                            .duration(400)
                            .style("opacity", 0);
                    };

                    svg.selectAll("path").on("mouseover", mouseOn);
                    svg.selectAll("path").on("mouseout", mouseOff);
                }
            );
        }

        function appendAxis(svg, elemAxisX, elemAxisY) {
            svg.append("g")
                .style("stroke-width", style.axis_stroke_width)
                .style("fill", "none")
                .style("stroke", style.axis_color)
                .attr("class", "survival-curve-x-axis-class")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 550)")
                .call(elemAxisX);
            svg.append("g")
                .style("stroke-width", style.axis_stroke_width)
                .style("fill", "none")
                .style("stroke", style.axis_color)
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 50)")
                .call(elemAxisX.orient("bottom").ticks(0));
            svg.append("g")
                .style("stroke-width", style.axis_stroke_width)
                .style("fill", "none")
                .style("stroke", style.axis_color)
                .attr("class", "survival-curve-y-axis-class")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(100, 0)")
                .call(elemAxisY);
            svg.append("g")
                .style("stroke-width", style.axis_stroke_width)
                .style("fill", "none")
                .style("stroke", style.axis_color)
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(700, 0)")
                .call(elemAxisY.orient("left").ticks(0));
            svg.selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "11px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black");

        }

        function drawCensoredDots(svg, data, color) {
            svg.selectAll("path")
                .data(data)
                .enter()
                .append("line")
                .attr("x1", function(d) {return elem.xScale(d.time)})
                .attr("x2", function(d) {return elem.xScale(d.time)})
                .attr("y1", function(d) {return elem.yScale(d.survival_rate) + style.censored_sign_size})
                .attr("y2", function(d) {return elem.yScale(d.survival_rate) - style.censored_sign_size})
                .attr("stroke", color)
                .style("opacity", function(d) {
                    if (d.status === "1") {
                        return 0; //hidden
                    } else if (d.status === "0") { //censored
                        return 1;
                    }
                });
            svg.selectAll("path")
                .data(data)
                .enter()
                .append("line")
                .attr("x1", function(d) {return elem.xScale(d.time) + style.censored_sign_size})
                .attr("x2", function(d) {return elem.xScale(d.time) - style.censored_sign_size})
                .attr("y1", function(d) {return elem.yScale(d.survival_rate)})
                .attr("y2", function(d) {return elem.yScale(d.survival_rate)})
                .attr("stroke", color)
                .style("opacity", function(d) {
                    if (d.status === "1") {
                        return 0; //hidden
                    } else if (d.status === "0") { //censored
                        return 1;
                    }
                });
        }

        function addLegends(svg) {
            var _legends_text = [
                {
                    text: text.glyph1,                //altered
                    color: settings.altered_line_color
                },
                {
                    text: text.glyph2,
                    color: settings.unaltered_line_color
                }
            ];

            var legend = svg.selectAll(".legend")
                .data(_legends_text)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) {
                    return "translate(715, " + (70 + i * 15) + ")";
                })

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
                .text(function(d) { return d.text });

        }

        function appendAxisTitles(svg, xTitle, yTitle) {
            svg.append("text")
                .attr("class", "label")
                .attr("x", style.axisX_title_pos_x)
                .attr("y", style.axisX_title_pos_y)
                .style("text-anchor", "middle")
                .style("font-weight","bold")
                .text(xTitle);
            svg.append("text")
                .attr("class", "label")
                .attr("transform", "rotate(-90)")
                .attr("x", style.axisY_title_pos_x)
                .attr("y", style.axisY_title_pos_y)
                .style("text-anchor", "middle")
                .style("font-weight","bold")
                .text(yTitle);
        }

        function addPvals(svg, pVal) {
            svg.append("text")
                .attr("x", 710)
                .attr("y", 110)
                .style("text-anchor", "front")
                .text("Logrank Test P-Value: " + pVal);
        }

        function appendInfo(divName, vals, type) {
            var _m_title = "";
            var _events_title = "";
            if (type === "os") {
                _m_title = "median months survival";
                _events_title = "#cases deceased";
            } else if (type === "dfs") {
                _m_title = "median months disease free";
                _events_title = "#cases relapsed";
            }
            $("#" + divName).empty();
            $("#" + divName).append("<table class='survival_stats'>" +
                "<tr><td></td><td>#total cases</td><td>" + _events_title + "</td><td>" + _m_title + "</td></tr>" +
                "<tr>" +
                "<td style='width: 300px; text-align:left;'>Cases with Alteration(s) in Query Gene(s)</td>" +
                "<td><b>" + vals.num_altered_cases + "</b></td>" +
                "<td><b>" + vals.num_of_events_altered_cases + "</b></td>" +
                "<td><b>" + vals.altered_median + "</b></td>" +
                "</tr><tr>" +
                "<td style='text-align:left;'>Cases without Alteration(s) in Query Gene(s)</td>" +
                "<td><b>" + vals.num_unaltered_cases + "</b></td>" +
                "<td><b>" + vals.num_of_events_unaltered_cases + "</b></td>" +
                "<td><b>" + vals.unaltered_median + "</b></td>" +
                "</table>");
        }

        function appendImgConverter(divId, svgId) {
            var pdfConverterForm = "<form class='img_buttons' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=loadSurvivalCurveSVG('" + svgId + "');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='survival_study.pdf'>" +
                "<input type='submit' value='PDF'></form>";
            $('#' + divId).append(pdfConverterForm);
            var svgConverterForm = "<form class='img_buttons' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=loadSurvivalCurveSVG('" + svgId + "');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' name='filename' value='survival_study.svg'>" +
                "<input type='submit' value='SVG'></form>";
            $('#' + divId).append(svgConverterForm);
        }

        return {
            initOS: function() {
                initOSCanvas();
                initAxis();
                initLines();
            },
            initDFS: function() {
                initDFSCanvas();
                initAxis();
                initLines();
            },
            generateOS: function() {
                drawOSLines();
                drawInvisiableDots(elem.osAlterDots, settings.altered_mouseover_color, data.getOSAlteredData());
                drawInvisiableDots(elem.osUnalterDots, settings.unaltered_mouseover_color, data.getOSUnalteredData());
                drawCensoredDots(elem.osAlterCensoredDots, data.getOSAlteredData(), settings.altered_line_color);
                drawCensoredDots(elem.osUnalterCensoredDots, data.getOSUnalteredData(), settings.unaltered_line_color);
                addQtips(elem.osAlterDots, "os");
                addQtips(elem.osUnalterDots, "os");
                appendAxis(elem.svgOS, elem.xAxisOS, elem.yAxisOS);
                appendAxisTitles(elem.svgOS, text.xTitle_os, text.yTitle_os);
                addLegends(elem.svgOS);
                addPvals(elem.svgOS, data.getOsStats().pVal);
                appendInfo("os_stat_table", data.getOsStats(), "os");
                appendImgConverter("os_header", "os_survival_curve");
            },
            generateDFS: function() {
                drawDFSLines();
                drawInvisiableDots(elem.dfsAlterDots, settings.altered_mouseover_color, data.getDFSAlteredData());
                drawInvisiableDots(elem.dfsUnalterDots, settings.unaltered_mouseover_color, data.getDFSUnalteredData());
                drawCensoredDots(elem.dfsAlterCensoredDots, data.getDFSAlteredData(), settings.altered_line_color);
                drawCensoredDots(elem.dfsUnalterCensoredDots, data.getDFSUnalteredData(), settings.unaltered_line_color);
                addQtips(elem.dfsAlterDots, "dfs");
                addQtips(elem.dfsUnalterDots, "dfs");
                appendAxis(elem.svgDFS, elem.xAxisDFS, elem.yAxisDFS);
                appendAxisTitles(elem.svgDFS, text.xTitle_dfs, text.yTitle_dfs);
                addLegends(elem.svgDFS);
                addPvals(elem.svgDFS, data.getDfsStats().pVal);
                appendInfo("dfs_stat_table", data.getDfsStats(), "dfs");
                appendImgConverter("dfs_header", "dfs_survival_curve");
            },
            errorMsg: function(type) {
                var errMsg = "<p style='margin-left:100px;'><br><br>Data not available.</p>";
                if (type === "os") {
                    $("#os_stat_table").append(errMsg);
                } else if (type === "dfs") {
                    $("#dfs_stat_table").append(errMsg);
                }

            }
        }
    }());

    var kmEstimator = (function() {

        return {
            calc: function(inputGrp) { //calculate the survival rate for each time point
                //each item in the input already has fields: time, num at risk, event/status(0-->censored)
                var _prev_value = 1;  //buffer for the previous value
                for (var i in inputGrp) {
                    var _case = inputGrp[i];
                    if (_case.status === "1") {
                        _case.survival_rate = _prev_value * ((_case.num_at_risk - 1) / _case.num_at_risk) ;
                        _prev_value = _case.survival_rate;
                    } else if (_case.status === "0") {
                        _case.survival_rate = _prev_value; //survival rate remain the same if the event is "censored"
                    } else {
                        //TODO: error handling
                    }
                }
            }
        }
    }());

    var logRankTest = (function() {

        var datum = {
                time: "",    //num of months
                num_of_failure_1: 0,
                num_of_failure_2: 0,
                num_at_risk_1: 0,
                num_at_risk_2: 0,
                expectation: 0, //(n1j / (n1j + n2j)) * (m1j + m2j)
                variance: 0
            },
            mergedArr = [];
        //os: DECEASED-->1, LIVING-->0; dfs: Recurred/Progressed --> 1, Disease Free-->0
        function mergeGrps(inputGrp1, inputGrp2) {
            var _ptr_1 = 0; //index indicator/pointer for group1
            var _ptr_2 = 0; //index indicator/pointer for group2

            while(_ptr_1 < inputGrp1.length && _ptr_2 < inputGrp2.length) { //Stop when either pointer reach the end of the array
                if (inputGrp1[_ptr_1].time < inputGrp2[_ptr_2].time) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.time = inputGrp1[_ptr_1].time;
                    if (inputGrp1[_ptr_1].status === "1") {
                        _datum.num_of_failure_1 = 1;
                        _datum.num_at_risk_1 = inputGrp1[_ptr_1].num_at_risk;
                        _datum.num_at_risk_2 = inputGrp2[_ptr_2].num_at_risk;
                        _ptr_1 += 1;
                    } else {
                        _ptr_1 += 1;
                        continue;
                    }
                } else if (inputGrp1[_ptr_1].time > inputGrp2[_ptr_2].time) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.time = inputGrp2[_ptr_2].time;
                    if (inputGrp2[_ptr_2].status === "1") {
                        _datum.num_of_failure_2 = 1;
                        _datum.num_at_risk_1 = inputGrp1[_ptr_1].num_at_risk;
                        _datum.num_at_risk_2 = inputGrp2[_ptr_2].num_at_risk;
                        _ptr_2 += 1;
                    } else {
                        _ptr_2 += 1;
                        continue;
                    }
                } else { //events occur at the same time point
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.time = inputGrp1[_ptr_1].time;
                    if (inputGrp1[_ptr_1].status === "1" || inputGrp2[_ptr_2].status === "1") {
                        if (inputGrp1[_ptr_1].status === "1") {
                            _datum.num_of_failure_1 = 1;
                        }
                        if (inputGrp2[_ptr_2].status === "1") {
                            _datum.num_of_failure_2 = 1;
                        }
                        _datum.num_at_risk_1 = inputGrp1[_ptr_1].num_at_risk;
                        _datum.num_at_risk_2 = inputGrp2[_ptr_2].num_at_risk;
                        _ptr_1 += 1;
                        _ptr_2 += 1;
                    } else {
                        _ptr_1 += 1;
                        _ptr_2 += 1;
                        continue;
                    }
                }
                mergedArr.push(_datum);
            }
        }

        function calcExpection() {
            for (var i in mergedArr) {
                var _item = mergedArr[i];
                _item.expectation = (_item.num_at_risk_1 / (_item.num_at_risk_1 + _item.num_at_risk_2)) * (_item.num_of_failure_1 + _item.num_of_failure_2);
            }
        }

        function calcVariance() {
            for (var i in mergedArr) {
                var _item = mergedArr[i];
                var _num_of_failures = _item.num_of_failure_1 + _item.num_of_failure_2;
                var _num_at_risk = _item.num_at_risk_1 + _item.num_at_risk_2;
                _item.variance = ( _num_of_failures * (_num_at_risk - _num_of_failures) * _item.num_at_risk_1 * _item.num_at_risk_2) / ((_num_at_risk * _num_at_risk) * (_num_at_risk - 1));
            }
        }

        function calcPval(type) {
            var O1 = 0, E1 = 0, V = 0;
            for (var i in mergedArr) {
                var _item = mergedArr[i];
                O1 += _item.num_of_failure_1;
                E1 += _item.expectation;
                V += _item.variance;
            }
            var chi_square_score = (O1 - E1) * (O1 - E1) / V;
            $.post( "calcPval.do", { chi_square_score: chi_square_score })
                .done( function(_data) {
                    data.pValCallBack(_data, type);
                });
        }

        return {
            calc: function(inputGrp1, inputGrp2, type) {
                mergedArr.length = 0;
                mergeGrps(inputGrp1, inputGrp2);
                calcExpection();
                calcVariance();
                calcPval(type);
            }
        }
    }());

    var confidenceIntervals = (function() {

        var arr = [],
            n = 0,
            mean = 0,
            sd = 0,
            se = 0,
            lcl = 0,
            ucl = 0;

        function adaptor(inputArr) {
            var _index = 0;
            for(var i in inputArr) {
                if (inputArr[i].status === "1")  {
                    arr[_index] = inputArr[i].time;
                    _index += 1;
                }
            }
            n = arr.length;
        }

        function calcMean() {
            var _sum = 0;
            for(var i in arr) { _sum += arr[i]; }
            mean = _sum / n;
        }

        function calcStandardDeviation() {
            var _sum = 0;
            for(var i in arr) { _sum += (arr[i] - mean) * (arr[i] - mean); }
            sd = Math.sqrt(_sum / (n-1));
        }

        function calcStandardError() {
            se = sd / Math.sqrt(n);
        }

        function calcControlLimits() {  //apply 0.95 percentage
            lcl = mean - 1.962 * se;
            ucl = mean + 1.962 * se;
            return {
                "lcl": lcl,
                "ucl": ucl
            };
        }

        return {
            calc: function(inputArr) {
                n = 0;
                arr.length = 0;
                adaptor(inputArr);
                calcMean();
                calcStandardDeviation();
                calcStandardError();
                calcControlLimits();
            }
        }

    }());

    return {
        init: function(caseLists) {
            var paramsGetSurvivalData = {
                case_set_id: case_set_id,
                case_ids_key: case_ids_key,
                cancer_study_id: cancer_study_id
            };
            $.post("getSurvivalData.json", paramsGetSurvivalData, getResultInit(caseLists), "json");

            function getResultInit(caseLists) {
                return function(result) {
                    data.init(result, caseLists);
                }
            }
        }
    }
}());

function loadSurvivalCurveSVG(svgId) {
    return $("#" + svgId).html();
}