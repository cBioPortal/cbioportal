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

 /** 
 *
 * Basic Scatter Plots Component. 
 *
 * @param: options -- includes style, canvas(position), elem(svg), text(titles, etc.)
 * @param: dataArr -- Json object from data proxy (x value, y value, qtip content, case id, etc.)
 * @param: dataAttr -- attributes of input data object (max, min, etc.)
 *
 * @output: a simple scatter plot 
 * 
 * @author: Yichao S
 * @date: Jan 2014
 *
 */

var ScatterPlots = (function() {

    var style = {},
        canvas = {}, //positions
        elem = {},
        text = {},
        names = {}, //ids
        dataArr = [],
        dataAttr = [];

    var axis_edge = 0.1;
        log_scale_threshold = 0.17677669529;

    function initSettings(options, _dataAttr) { //Init with options
        style = jQuery.extend(true, {}, options.style);
        canvas = jQuery.extend(true, {}, options.canvas);
        elem = jQuery.extend(true, {}, options.elem);
        text = jQuery.extend(true, {}, options.text);
        names = jQuery.extend(true, {}, options.names);
        dataAttr = jQuery.extend(true, {}, _dataAttr);
    }

    function convertData(_dataArr) {
        dataArr.length = 0;
        //convert json to array, and filter out null 
        $.each(_dataArr, function(index, obj) {
            if (!isNaN(parseFloat(obj.x_val)) && !isNaN(parseFloat(obj.y_val))) {
                dataArr.push(obj);
            }
        });
    }

    function initSvgCanvas(divName) {
        elem.svg = d3.select("#" + divName).append("svg")
            .attr("width", canvas.width)
            .attr("height", canvas.height);
        elem.dotsGroup = elem.svg.append("svg:g");
        elem.axisGroup = elem.svg.append("svg:g");
        elem.axisTitleGroup = elem.svg.append("svg:g");
    }

    function initScaleX() {
        var _edge_x = (dataAttr.max_x - dataAttr.min_x) * axis_edge;
        elem.xScale = d3.scale.linear()
            .domain([dataAttr.min_x - _edge_x, dataAttr.max_x + _edge_x])
            .range([canvas.xLeft, canvas.xRight]);
    }

    function initScaleY() {
        var _edge_y = (dataAttr.max_y - dataAttr.min_y) * axis_edge;
        elem.yScale = d3.scale.linear()
            .domain([dataAttr.min_y - _edge_y, dataAttr.max_y + _edge_y])
            .range([canvas.yBottom, canvas.yTop]);
    }

    function initAxisX() {
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom")
            .tickSize(6, 0, 0);
    }

    function initAxisY() {
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .orient("left")
            .tickSize(6, 0, 0);
    }

    function generateAxisX() {
        d3.select("#" + names.body).select(".coexp-x-axis").remove();
        elem.axisGroup.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("class", "coexp-x-axis")
            .attr("transform", "translate(0, " + canvas.yBottom + ")")
            .call(elem.xAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "11px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.axisGroup.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, " + canvas.yTop + ")")
            .call(elem.xAxis.orient("bottom").ticks(0));

    }

    function generateAxisY() {
        d3.select("#" + names.body).select(".coexp-y-axis").remove();
        elem.axisGroup.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("class", "coexp-y-axis")
            .attr("transform", "translate(" + canvas.xLeft + ", 0)")
            .call(elem.yAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "11px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.axisGroup.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + canvas.xRight + ", 0)")
            .call(elem.yAxis.orient("left").ticks(0));
    }

    function appendAxisTitleX(_applyLogScale) {
        d3.select("#" + names.body).select(".coexp-title-x").remove();
        if (_applyLogScale) {
            text.xTitle = text.xTitle + " (log2)";
        } else {
            text.xTitle = text.xTitle.replace(" (log2)", "");
        }
        elem.axisTitleGroup.append("text")
            .attr("x", canvas.xLeft + (canvas.xRight - canvas.xLeft) / 2)
            .attr("y", canvas.yBottom + 40)
            .style("text-anchor", "middle")
            .style("font-size", "12px")
            .style("font-weight", "bold") 
            .attr("class", "coexp-title-x")
            .text(text.xTitle);
    }

    function appendAxisTitleY(_applyLogScale) {
        d3.select("#" + names.body).select(".coexp-title-y").remove();
        if (_applyLogScale) {
            text.yTitle = text.yTitle + " (log2)";
        } else {
            text.yTitle = text.yTitle.replace(" (log2)", "");
        }
        elem.axisTitleGroup.append("text")
            .attr("transform", "rotate(-90)")
            .attr("x", (canvas.xLeft - canvas.xRight) / 2 - canvas.yTop)
            .attr("y", 45)
            .style("text-anchor", "middle")
            .style("font-size", "12px")
            .style("font-weight", "bold")
            .attr("class", "coexp-title-y") 
            .text(text.yTitle);
    }

    function drawPlots() {
        elem.dotsGroup.selectAll("path").remove();
        elem.dotsGroup.selectAll("path")
            .data(dataArr)
            .enter()
            .append("svg:path")
            .attr("transform", function(d){
                //Remember current positions for the later transition animation
                $(this).attr("x_val", d.x_val);
                $(this).attr("y_val", d.y_val);
                $(this).attr("x_pos", elem.xScale(d.x_val)); 
                $(this).attr("y_pos", elem.yScale(d.y_val));
                return "translate(" + elem.xScale(d.x_val) + ", " + elem.yScale(d.y_val) + ")";
            })
            .attr("d", d3.svg.symbol()
                .size(style.size)
                .type(style.shape))
            .attr("fill", style.fill)
            .attr("stroke", style.stroke)
            .attr("stroke-width", style.stroke_width);
    }

    function addQtips() {
        elem.dotsGroup.selectAll('path').each(
            function(d) {
                $(this).qtip(
                    {
                        content: {text: d.qtip},
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right'}
                    }
                );

            }
        );
        //Hover Animation
        var mouseOn = function() {
            var dot = d3.select(this);
            dot.transition()
                .ease("elastic")
                .duration(600)
                .delay(100)
                .attr("d", d3.svg.symbol().size(style.size * 10).type(style.shape));
        };
        var mouseOff = function() {
            var dot = d3.select(this);
            dot.transition()
                .ease("elastic")
                .duration(600)
                .delay(100)
                .attr("d", d3.svg.symbol().size(style.size).type(style.shape));
        };
        elem.dotsGroup.selectAll("path").on("mouseover", mouseOn);
        elem.dotsGroup.selectAll("path").on("mouseout", mouseOff);
    }

    function updatePlotsLogScale(_axis, _applyLogScale) {
        elem.dotsGroup.selectAll("path")
            .transition().duration(300)
            .attr("transform", function() {
                if (_applyLogScale) {
                    if (_axis === "x") {
                        if (parseFloat(d3.select(this).attr("x_val")) <= log_scale_threshold) {
                            var _post_x = elem.xScale(Math.log(log_scale_threshold) / Math.log(2));
                        } else {
                            var _post_x = elem.xScale(Math.log(d3.select(this).attr("x_val")) / Math.log(2));
                        }
                        var _post_y = d3.select(this).attr("y_pos");
                    } else if (_axis === "y") {
                        var _post_x = d3.select(this).attr("x_pos");
                        if (parseFloat(d3.select(this).attr("y_val")) <= log_scale_threshold) {
                            var _post_y = elem.yScale(Math.log(log_scale_threshold) / Math.log(2));
                        } else {
                            var _post_y = elem.yScale(Math.log(d3.select(this).attr("y_val")) / Math.log(2));
                        }

                    }
                    d3.select(this).attr("x_pos", _post_x);
                    d3.select(this).attr("y_pos", _post_y);
                    return "translate(" + _post_x + ", " + _post_y + ")";
                } else {
                    if (_axis === "x") {
                        var _post_x = elem.xScale(d3.select(this).attr("x_val"));
                        var _post_y = d3.select(this).attr("y_pos");
                    } else if (_axis === "y") {
                        var _post_x = d3.select(this).attr("x_pos");
                        var _post_y = elem.yScale(d3.select(this).attr("y_val"));
                    }
                    d3.select(this).attr("x_pos", _post_x);
                    d3.select(this).attr("y_pos", _post_y);
                    return "translate(" + _post_x + ", " + _post_y + ")";
                }
            });
    }

    function updateAxisScaleX() {
        var _min_x, _max_x, _edge_x;
        if (dataAttr.min_x <= log_scale_threshold) {
            _min_x = Math.log(log_scale_threshold) / Math.log(2);
        } else {
            _min_x = Math.log(dataAttr.min_x) / Math.log(2);
        }
        _max_x = Math.log(dataAttr.max_x) / Math.log(2);
        _edge_x = (_max_x - _min_x) * axis_edge;
        elem.xScale = d3.scale.linear()
            .domain([_min_x - _edge_x, _max_x + _edge_x])
            .range([canvas.xLeft, canvas.xRight]);

    }

    function updateAxisScaleY() {
        var _min_y, _max_y, _edge_y;
        if (dataAttr.min_y <= log_scale_threshold) {
            _min_y = Math.log(log_scale_threshold) / Math.log(2);
        } else {
            _min_y = Math.log(dataAttr.min_y) / Math.log(2);
        }
        _max_y = Math.log(dataAttr.max_y) / Math.log(2);
        _edge_y = (_max_y - _min_y) * axis_edge;
        elem.yScale = d3.scale.linear()
            .domain([_min_y - _edge_y, _max_y + _edge_y])
            .range([canvas.yBottom, canvas.yTop]);
    }

    return {
        init: function(options, _dataArr, _dataAttr) {    //Init with options
            initSettings(options, _dataAttr);
            convertData(_dataArr);
            initSvgCanvas(names.body);
            initScaleX();
            initScaleY();
            initAxisX();
            initAxisY();
            generateAxisX();
            generateAxisY();
            appendAxisTitleX(false);
            appendAxisTitleY(false);
            drawPlots();
            addQtips();
        },
        // !!! Log Scale are only used by using RNA Seq Profile
        updateScaleX: function(_divName) {   //_applyLogScale: boolean, true for apply scale, false for  original value)
            var _applyLogScale = document.getElementById(_divName).checked;
            if (_applyLogScale) {
                updateAxisScaleX();
            } else {
                initScaleX();
            }
            initAxisX();
            generateAxisX();
            appendAxisTitleX(_applyLogScale);
            updatePlotsLogScale("x", _applyLogScale);
        },
        updateScaleY: function(_divName) {   //_applyLogScale: boolean, true for apply scale, false for  original value)
            var _applyLogScale = document.getElementById(_divName).checked;
            if (_applyLogScale) {
                updateAxisScaleY();
            } else {
                initScaleY();
            }
            initAxisY();
            generateAxisY();
            appendAxisTitleY(_applyLogScale);
            updatePlotsLogScale("y", _applyLogScale);
        }
    }

}());


