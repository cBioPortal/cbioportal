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

/******************************************************************************************
 * Creating overall survival and disease free curves for the survival tab
 * @author Yichao Sun
 * @date Sep 2013
 *
 * This code performs the following functions:
 * 1. Calculate the survival rates for each time point using kaplan-meier estimator
 * 2. Generate the curves using d3 line charts
 ******************************************************************************************/

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
            dfs_unaltered_group = [];

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
                    if (_datum.time !== "NA") {
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
                    if (_datum.time !== "NA") {
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

        return {
            init: function(result, caseLists) {
                cntAlter(caseLists);
                setOSGroups(result, caseLists);
                setDFSGroups(result, caseLists);
                kmEstimator.calc(os_altered_group);
                kmEstimator.calc(os_unaltered_group);
                kmEstimator.calc(dfs_altered_group);
                kmEstimator.calc(dfs_unaltered_group);
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
            }
        }
    }());

    var view = (function() {
        var elem = {
                svgOS : "",
                svgDFS: "",
                xScale : "",
                yScale : "",
                xAxis : "",
                yAxis : "",
                osAlterLine: "",
                osUnalterLine: "",
                dfsAlterLine: "",
                dfsUnalterLine: "",
                osAlterDots: ""
            },
            settings = {
                canvas_width: 720,
                canvas_height: 600
            };

        function initCanvas() {
            $('#os_survival_curve').empty();
            $('#dfs_survival_curve').empty();
            elem.svgOS = d3.select("#os_survival_curve")
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
            elem.svgDFS = d3.select("#dfs_survival_curve")
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
            elem.osAlterDots = elem.svgOS.append("g");
        }

        function initAxis() {
            elem.xScale = d3.time.scale()
                .domain([0,200])
                .range([100, 600]);
            elem.yScale = d3.scale.linear()
                .domain([0,1])
                .range([500, 100]);
            elem.xAxis = d3.svg.axis()
                .scale(elem.xScale)
                .orient("bottom");
            elem.yAxis = d3.svg.axis()
                .scale(elem.yScale)
                .orient("left");
        }

        function initLines() {
            elem.osAlterLine = d3.svg.line()
                .interpolate("step-after")
                .x(function(d) { return elem.xScale(d.time); })
                .y(function(d) { return elem.yScale(d.survival_rate); });
        }

        function drawLines() {
            elem.svgOS.append("path")
                .attr("d", elem.osAlterLine(data.getOSAlteredData()))
                .style("fill", "none")
                .style("stroke", "red");
            elem.svgOS.append("path")
                .attr("d", elem.osAlterLine(data.getOSUnalteredData()))
                .style("fill", "none")
                .style("stroke", "blue");
            elem.svgDFS.append("path")
                .attr("d", elem.osAlterLine(data.getDFSAlteredData()))
                .style("fill", "none")
                .style("stroke", "red");
            elem.svgDFS.append("path")
                .attr("d", elem.osAlterLine(data.getDFSUnalteredData()))
                .style("fill", "none")
                .style("stroke", "blue");
        }

        function drawInvisiableDots() {
            elem.osAlterDots.selectAll("path")
                .data(data.getOSAlteredData())
                .enter()
                .append("svg:path")
                .attr("d", d3.svg.symbol()
                    .size(400)
                    .type("circle"))
                .attr("transform", function(d){
                    return "translate(" + elem.xScale(d.time) + ", " + elem.yScale(d.survival_rate) + ")";
                })
                .attr("fill", "grey")
                .style("opacity", 0);
        }

        function addQtips() {
            elem.osAlterDots.selectAll('path').each(
                function(d) {
                    var content = "<font size='2'>";
                    content += "Case ID: " + "<strong><a href='tumormap.do?case_id=" + d.case_id +
                        "&cancer_study_id=" + cancer_study_id + "'>" + d.case_id + "</a></strong><br>" + "</font>";

                    $(this).qtip(
                        {
                            content: {text: content},
                            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                            show: {event: "mouseover"},
                            hide: {fixed:true, delay: 100, event: "mouseout"},
                            position: {my:'left bottom',at:'top right'}
                        }
                    );

                    var mouseOn = function() {
                        var dot = d3.select(this);
                        dot.transition()
                            .duration(400)
                            .style("opacity", .6);
                    };

                    var mouseOff = function() {
                        var dot = d3.select(this);
                        dot.transition()
                            .duration(400)
                            .style("opacity", 0);
                    };

                    elem.osAlterDots.selectAll("path").on("mouseover", mouseOn);
                    elem.osAlterDots.selectAll("path").on("mouseout", mouseOff);
                }
            );
        }

        return {
            init: function() {
                initCanvas();
                initAxis();
                initLines();
                drawLines();
                drawInvisiableDots();
                addQtips();
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
                        //TODO: error
                    }
                }
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
                    view.init();
                }
            }
        }
    }
}());