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
 * Creating RPPA plots for "protein change" tab
 * @author Yichao Sun
 *
 * This code performs the following functions:
 * 1. Generates RPPA plots using D3 when user expanding any row in the table
 * 2. Annotates the plots with caseId, alteration details, specific RPPA value
 ******************************************************************************************/

var rppaPlots = (function() {

    var data = (function() {

        var alteredCaseList =  [],
            unalteredCaseList =  [],
            alteredCases = [],
            unalteredCases = [];

        function setArrayData(proteinArrayData, alterationsObj) {
            alteredCases.length = 0;
            unalteredCases.length = 0;
            for (var key in proteinArrayData) {
                if (proteinArrayData.hasOwnProperty(key)) {
                    var _tmp = {
                        "caseId": key,
                        "value": proteinArrayData[key],
                        "alterations": alterationsObj[key]
                    };
                    if (alteredCaseList.indexOf(key) !== -1) {
                        alteredCases.push(_tmp);
                    } else {
                        unalteredCases.push(_tmp);
                    }
                }
            }
        }

        function init(proteinArrayData, alterationsObj) {
            setArrayData(proteinArrayData, alterationsObj);
        }

        return {
            setCaseLists: function(caseListsJSON) {
                alteredCaseList.length = 0;
                unalteredCaseList.length = 0;
                for (var key in caseListsJSON) {
                    if (caseListsJSON.hasOwnProperty(key)) {
                        if (caseListsJSON[key] === "altered") {
                            alteredCaseList.push(key);
                        } else if (caseListsJSON[key] === "unaltered") {
                            unalteredCaseList.push(key);
                        }
                    }
                }
            },
            init: init,
            getAlteredCases: function() {
                return alteredCases;
            },
            getUnAlteredCases: function() {
                return unalteredCases;
            }
        }

    }());

    var view = (function() {

        var xLabel = "",
            yLabel = "",
            title = "",
            divName = "",
            xAxisTextSet = ["Altered", "Unaltered"],
            singleDot = {
                xVal: "",  //0 --> altered; 1 --> unaltered
                yVal: "",
                caseId: ""
            },
            dotsArr = [],
            elem = {
                svg : "",
                xScale : "",
                yScale : "",
                xAxis : "",
                yAxis : "",
                dotsGroup : ""   //Group of single Dots
            },
            settings = {
                canvas_width: 720,
                canvas_height: 600,
                dots_fill_color: "#58ACFA",
                dots_stroke_color: "#0174DF"
            };

        function pDataInit() {
            dotsArr.length = 0;
            $.each(data.getAlteredCases(), function(index, val){
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.xVal = 0;
                _singleDot.yVal = val.value;
                _singleDot.caseId = val.caseId;
                //Convert alteration JSON to string
                var _altStr = "<table style='border: 1px solid darkgrey;border-collapse:collapse;'>";
                var p = val.alterations;
                for (var key in p) {
                    if (p.hasOwnProperty(key) && (p[key] !== "")) {
                        var str = p[key];
                        var last = str.lastIndexOf(';');
                        var butLast = str.substring(0, last).replace(/;/g, ", ");
                        var res = butLast + str.substring(last).replace(";", "");
                        _altStr += "<tr style='border: 1px solid darkgrey'><td style='border: 1px solid darkgrey'>" +
                            key + "</td><td>" + res + "</td></tr>";
                    }
                }
                _altStr += "</table>";
                _singleDot.annotation = _altStr;
                dotsArr.push(_singleDot);
            });
            $.each(data.getUnAlteredCases(), function(index, val){
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.xVal = 1;
                _singleDot.yVal = val.value;
                _singleDot.caseId = val.caseId;
                dotsArr.push(_singleDot);
            });
        }

        function initCanvas() {
            elem.svg = d3.select("#" + divName)
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
        }

        function drawAxis() {
            //Init
            elem.xScale = d3.scale.linear()
                .domain([-0.7, 1.7])
                .range([100, 600]);

            var _yValArr = [];
            $.each(dotsArr, function(index, val){
                _yValArr.push(val.yVal);
            });
            var _results = util.analyseData(_yValArr);
            elem.yScale = d3.scale.linear()
                .domain([_results.min, _results.max])
                .range([520, 20]);
            elem.xAxis = d3.svg.axis()
                .scale(elem.xScale)
                .orient("bottom")
            elem.yAxis = d3.svg.axis()
                .scale(elem.yScale)
                .orient("left");
            //Draw
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
                .text(function(d){return d});
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
                .text(xLabel);
            axisTitleGroup.append("text")
                .attr("class", "rppa-plots-y-axis-title")
                .attr("transform", "rotate(-90)")
                .attr("x", -270)
                .attr("y", 45)
                .style("text-anchor", "middle")
                .style("font-size", "13px")
                .text(yLabel);
        }

        function drawPlots() {
            elem.dotsGroup = elem.svg.append("svg:g");
            elem.dotsGroup.selectAll("path").remove();
            var ramRatio = 80;  //Noise
            elem.dotsGroup.selectAll("path")
                .data(dotsArr)
                .enter()
                .append("svg:path")
                .attr("transform", function(d){
                    return "translate(" + (elem.xScale(d.xVal) + (Math.random() * ramRatio - ramRatio/2)) + ", " + elem.yScale(d.yVal) + ")";
                })
                .attr("d", d3.svg.symbol()
                    .size(20)
                    .type("circle"))
                .attr("fill", settings.dots_fill_color)
                .attr("stroke", settings.dots_stroke_color)
                .attr("stroke-width", "1.2");
        }

        function appendHeader() {
            $("#" + divName).empty();
            $("#" + divName).append(title);
            var pdfConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank' " +
                "onsubmit=\"this.elements['svgelement'].value=loadRPPASVG('" + divName + "');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='rppa-plots.pdf'>" +
                "<input type='submit' value='PDF'></form>";
            $("#" + divName).append(pdfConverterForm);
        }

        function drawBoxPlot() {
            var boxPlotsElem = elem.svg.append("svg:g");
            for (var i = 0 ; i < 2; i++) {  //Just 0(altered) and 1(unaltered)
                var top;
                var bottom;
                var quan1;
                var quan2;
                var mean;
                var IQR;
                var scaled_y_arr=[];
                var tmp_y_arr = [];
                //Find the middle (vertical) line for one box plot
                var midLine = elem.xScale(i);
                //Find the max/min y value with certain x value;
                $.each(dotsArr, function(index, value) {
                    if (value.xVal === i) {
                        tmp_y_arr.push(parseFloat(value.yVal));
                    }
                });
                tmp_y_arr.sort(function(a, b) { return a - b });
                //Deal with individual data sub group
                if (tmp_y_arr.length === 0) {
                    //Skip: do nothing
                } else if (tmp_y_arr.length === 1) {
                    mean = elem.yScale(tmp_y_arr[0]);
                    boxPlotsElem.append("line")
                        .attr("x1", midLine-30)
                        .attr("x2", midLine+30)
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
                            mean = elem.yScale((tmp_y_arr[(yl / 2)-1] + tmp_y_arr[yl / 2]) / 2);
                            if (yl % 4 === 0) {
                                quan1 = elem.yScale((tmp_y_arr[(yl / 4)-1] + tmp_y_arr[yl / 4]) / 2);
                                quan2 = elem.yScale((tmp_y_arr[(3*yl / 4)-1] + tmp_y_arr[3 * yl / 4]) / 2);
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
                        for (var k = 0 ; k < tmp_y_arr.length ; k++) {
                            scaled_y_arr[k] = parseFloat(elem.yScale(tmp_y_arr[k]));
                        }
                        scaled_y_arr.sort(function(a,b) { return a-b });
                        IQR = Math.abs(quan2 - quan1);
                        var index_top = util.searchIndexTop(scaled_y_arr, (quan2 - 1.5 * IQR));
                        top = scaled_y_arr[index_top];
                        var index_bottom = util.searchIndexBottom(scaled_y_arr, (quan1 + 1.5 * IQR));
                        bottom = scaled_y_arr[index_bottom];
                    }

                    //D3 Drawing
                    boxPlotsElem.append("rect")
                        .attr("x", midLine-60)
                        .attr("y", quan2)
                        .attr("width", 120)
                        .attr("height", IQR)
                        .attr("fill", "none")
                        .attr("stroke-width", 1)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsElem.append("line")
                        .attr("x1", midLine-60)
                        .attr("x2", midLine+60)
                        .attr("y1", mean)
                        .attr("y2", mean)
                        .attr("stroke-width", 3)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsElem.append("line")
                        .attr("x1", midLine-40)
                        .attr("x2", midLine+40)
                        .attr("y1", top)
                        .attr("y2", top)
                        .attr("stroke-width", 1)
                        .attr("stroke", "#BDBDBD");
                    boxPlotsElem.append("line")
                        .attr("x1", midLine-40)
                        .attr("x2", midLine+40)
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
        }

        function addQtips() {
            elem.dotsGroup.selectAll('path').each(
                function(d) {
                    var content = "<font size='2'>";
                    content += "Case ID: " + "<strong><a href='"
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            + "' target = '_blank'>" + d.caseId + "</a></strong><br>";
                    content += "RPPA score: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.hasOwnProperty("annotation")) {
                        content += "Altertions: <strong>" + d.annotation + "</strong>";
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
        }

        function init() {
            pDataInit();
            appendHeader();
            initCanvas();
            drawAxis();
            drawBoxPlot();
            drawPlots();
            addQtips();
        }

        return {
            setAttr: function(_xLabel, _yLabel, _title, _divName) {
                xLabel = _xLabel;
                yLabel = _yLabel;
                title = _title;
                divName = _divName;
            },
            init: init
        }

    }());

    var util = (function() {

        function analyseData(inputArr) {    //pDataX, pDataY: array of single dot objects
            var min = Math.min.apply(Math, inputArr);
            var max = Math.max.apply(Math, inputArr);
            var edge = (max - min) * 0.1;
            min -= edge;
            max += edge;

            return {
                min: min,
                max: max
            };
        }

        function searchIndexBottom(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
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

        function searchIndexTop(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
                if (ele <= arr[i]) {
                    return i;
                } else {
                    continue;
                }
            }
            return arr.length - 1;
        }

        return {
            analyseData: analyseData,
            searchIndexBottom: searchIndexBottom,
            searchIndexTop: searchIndexTop
        }

    }());

    function generatePlots(proteinArrayId, alterationsObj) {
        var paramsGetProteinArrayData = {
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
            case_set_id: window.PortalGlobals.getCaseSetId(),
            case_ids_key: window.PortalGlobals.getCaseIdsKey(),
            protein_array_id: proteinArrayId
        };
        $.post("getProteinArrayData.json", paramsGetProteinArrayData, getProfileDataCallBack(alterationsObj), "json");
    }

    function getProfileDataCallBack(alterationsObj) {
        return function(result) {
            data.init(result, alterationsObj);
            view.init();
        }
    }

    return {
        init: function(xLabel, yLabel, title, divName, caseLists, proteinArrayId, alterationsObj) {
            //Set all the parameters
            data.setCaseLists(caseLists);
            view.setAttr(xLabel, yLabel, title, divName);
            //Get data from server and drawing
            generatePlots(proteinArrayId, alterationsObj);
        }
    }

}());

function loadRPPASVG(divName) {
    var docSVG = document.getElementById(divName);
    var svgDoc = docSVG.getElementsByTagName("svg");
    var xmlSerializer = new XMLSerializer();
    var xmlString = xmlSerializer.serializeToString(svgDoc[0]);
    return xmlString;
}