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
 * @param: options -- includes customized style, canvas(position), elem(svg), text(titles, etc.)
 * @param: dataArr -- Json object from data proxy (x value, y value, qtip content, case id, etc.)
 * @param: dataAttr -- attributes of input data object (max, min, etc.)
 * @param: brushOn -- signal for turning on the brush feature or not
 *
 * @interface: updateScaleX -- pass the ID of a checkbox, to apply log scale on x axis
 * @interface: updateScaleY -- pass the ID of a checkbox, to apply log scale on y axis
 * @interface: updateMutations -- pass the ID of a checkbox, to show/hide mutation from plots
 * @interface: jointBrushCallback -- pass a function to be set as a callback function  
 *                                   whenever the brush behavior occur
 * @interface: updateStyle -- pass an array of datum (fields: case_id, fill, stroke), 
 *                            the corresponding plots style will be upated
 *
 * @output: a simple scatter plot 
 * 
 * @author: Yichao S
 * @date: Jan 2014
 *
 */

var ScatterPlots = function() {

    var style = {},
        canvas = {}, //positions
        elem = {},
        text = {},
        names = {}, //ids
        legends = [],
        dataArr = [],
        dataAttr = {};

    var axis_edge = 0.1;
        log_scale_threshold = 0.17677669529;

    var updateBrushCallback = "";

    function initSettings(options, _dataAttr) { //Init with options
        style = jQuery.extend(true, {}, options.style);
        canvas = jQuery.extend(true, {}, options.canvas);
        elem = jQuery.extend(true, {}, options.elem);
        text = jQuery.extend(true, {}, options.text);
        names = jQuery.extend(true, {}, options.names);
        legends = options.legends;
        dataAttr = jQuery.extend(true, {}, _dataAttr);
    }

    function convertData(_dataArr) {
        dataArr.length = 0;
        //convert json to array, and filter out null 
        $.each(_dataArr, function(index, obj) {
            if (!isNaN(parseFloat(obj.x_val)) && !isNaN(parseFloat(obj.y_val))) { //filter nan data points
                dataArr.push(obj);
            }
        });
    }

    function initSvgCanvas(divName, _brushOn) {
        elem.svg = d3.select("#" + divName).append("svg")
            .attr("width", canvas.width)
            .attr("height", canvas.height)
            .attr('pointer-events', 'all');

        if (_brushOn) {
            //Init the brush (before init the dots!)
            elem.brush = d3.svg.brush()
                .x(elem.xScale)
                .y(elem.yScale)
                .extent([[0, 0], [0, 0]])
                .on("brushend", brushended);
            elem.svg.append("g")
                .attr("class", "brush")
                .call(elem.brush);
        }
      
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
        d3.select("#" + names.body).select(".plots-x-axis").remove();
        elem.axisGroup.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("class", "plots-x-axis")
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
        d3.select("#" + names.body).select(".plots-y-axis").remove();
        elem.axisGroup.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("class", "plots-y-axis")
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
        d3.select("#" + names.body).select(".plots-title-x").remove();
        d3.select("#" + names.body).select(".plots-title-x-help").remove();
        var _xTitle = "";
        if (_applyLogScale) {
            _xTitle = text.xTitle + " (log2)";
        } else {
            _xTitle = text.xTitle;
        }
        elem.axisTitleGroup.append("text")
            .attr("x", canvas.xLeft + (canvas.xRight - canvas.xLeft) / 2)
            .attr("y", canvas.yBottom + 60)
            .style("text-anchor", "middle")
            .style("font-size", "12px")
            .style("font-weight", "bold") 
            .attr("class", "plots-title-x")
            .text(_xTitle);
        elem.axisTitleGroup.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", "plots-title-x-help")
            .attr("x", canvas.xLeft + (canvas.xRight - canvas.xLeft) / 2 + _xTitle.length / 2 * 8)
            .attr("y", canvas.yBottom + 48)
            .attr("width", "16")
            .attr("height", "16");
        elem.svg.select(".plots-title-x-help").each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=1>" + text.xTitleHelp + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right'}
                    }
                );
            }
        );
    }

    function appendAxisTitleY(_applyLogScale) {
        d3.select("#" + names.body).select(".plots-title-y").remove();
        d3.select("#" + names.body).select(".plots-title-y-help").remove();
        var _yTitle = "";
        if (_applyLogScale) {
            _yTitle = text.yTitle + " (log2)";
        } else {
            _yTitle = text.yTitle;
        }
        elem.axisTitleGroup.append("text")
            .attr("transform", "rotate(-90)")
            .attr("x", (canvas.yTop - canvas.yBottom) / 2 - canvas.yTop)
            .attr("y", canvas.xLeft - 60)
            .style("text-anchor", "middle")
            .style("font-size", "12px")
            .style("font-weight", "bold")
            .attr("class", "plots-title-y") 
            .text(_yTitle);
        elem.axisTitleGroup.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", "plots-title-y-help")
            .attr("x", canvas.xLeft - 72)
            .attr("y", canvas.yBottom - (canvas.yBottom - canvas.yTop) / 2 - _yTitle.length / 2 * 8 - 20)
            .attr("width", "16")
            .attr("height", "16");
        elem.svg.select(".plots-title-y-help").each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=1>" + text.yTitleHelp + "</font>"},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'right bottom',at:'top left'}
                    }
                );
            }
        );
    }

    function drawPlots() {
        elem.dotsGroup.selectAll("path").remove();
        elem.dotsGroup.selectAll("path")
            .data(dataArr)
            .enter()
            .append("svg:path")
            .attr("transform", function(d) {
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
            .attr("fill", function(d) {
                if (d.fill === null || d.fill === "" || typeof d.fill === "undefined") {
                    return style.fill;
                } else {
                    return d.fill;
                }
            })
            .attr("stroke", function(d) {
                if (d.stroke === null || d.stroke === "" || typeof d.stroke === "undefined") {
                    return style.stroke;
                } else {
                    return d.stroke;
                }
            })
            .attr("stroke-width", style.stroke_width)
            .attr("z-index", "100");
    }

    function hideMutations() { //remove special styles for mutated cases
        elem.dotsGroup.selectAll("path").remove();
        elem.dotsGroup.selectAll("path")
            .data(dataArr)
            .enter()
            .append("svg:path")
            .attr("transform", function(d) {
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
            .attr("fill", function(d) {
                return style.fill;
            })
            .attr("stroke", function(d) {
                return style.stroke;
            })
            .attr("stroke-width", style.stroke_width);
        elem.svg.selectAll(".legend").remove();
    }

    function drawLegends() {
        //separate long legends into two lines
        var _legends = [];
        $.each(legends, function(index, obj) {
            var firstWord = obj.text.substr(0, obj.text.indexOf(" "));
            var secondWord = obj.text.substr(obj.text.indexOf(" "), obj.text.length);
            if (firstWord.length <= 9) {
                _legends.push(obj);
            } else {
                var _tmp_obj_1 = {};
                _tmp_obj_1.size = obj.size;
                _tmp_obj_1.shape = obj.shape;
                _tmp_obj_1.fill = obj.fill;
                _tmp_obj_1.stroke = obj.stroke;
                _tmp_obj_1.stroke_width = obj.stroke_width;
                _tmp_obj_1.text = firstWord;
                _legends.push(_tmp_obj_1);
                var _tmp_obj_2 = {};
                _tmp_obj_2.size = "0"; //invisible
                _tmp_obj_2.shape = obj.shape;
                _tmp_obj_2.fill = obj.fill;
                _tmp_obj_2.stroke = obj.stroke;
                _tmp_obj_2.stroke_width = obj.stroke_width;
                _tmp_obj_2.text = secondWord;
                _legends.push(_tmp_obj_2);
            }
        });

        var legend = elem.svg.selectAll(".legend")
            .data(_legends)
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) {
                return "translate(" + (canvas.xRight + 10) + ", " + (24 + i * 14) + ")";
            })
        
        legend.append("path")
            .attr("d", d3.svg.symbol()
                .size(function(d) { return d.size; })
                .type(function(d) { return d.shape; }))
            .attr("fill", function (d) { return d.fill; })
            .attr("stroke", function (d) { return d.stroke; })
            .attr("stroke-width", function(d) { return d.stroke_width; });

        legend.append("text")
            .attr("dx", ".75em")
            .attr("dy", ".35em")
            .attr("text-anchor", "front")
            .style("font-size", "11")
            .text(function(d) { 
                return d.text; 
            });

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
        var mouseOff = function(d) {
            var dot = d3.select(this);
            dot.transition()
                .ease("elastic")
                .duration(600)
                .delay(100)
                .attr("d", d3.svg.symbol().size(style.size).type(style.shape));
        };
        elem.dotsGroup.selectAll("path").attr('pointer-events', 'all').on("mouseover", mouseOn);
        elem.dotsGroup.selectAll("path").attr('pointer-events', 'all').on("mouseout", mouseOff);
    }

    function brushended() {
        var brushedCases = [];
        brushedCases.length = 0;
        var extent = elem.brush.extent();
        elem.dotsGroup.selectAll("path").each(function(d) {
            if (d.x_val > extent[0][0] && d.x_val < extent[1][0] &&
                d.y_val > extent[0][1] && d.y_val < extent[1][1]) {
                //TODO: does not work with log scale applied scenario
                $(this).attr("stroke", "red");
                brushedCases.push(d.case_id);
            } else {
                if(d.stroke === null || d.stroke === "" || typeof d.stroke === "undefined") {
                    $(this).attr("stroke", style.stroke);    
                }
                $(this).attr("stroke", d.stroke);
            }
        });
        d3.select(".brush").call(elem.brush.clear());
        updateBrushCallback(brushedCases);
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
        init: function(_options, _dataArr, _dataAttr, _brushOn) {    //Init with options
            initSettings(_options, _dataAttr);
            convertData(_dataArr);
            initScaleX();
            initScaleY();
            initSvgCanvas(names.body, _brushOn);
            initAxisX();
            initAxisY();
            generateAxisX();
            generateAxisY();
            appendAxisTitleX(false);
            appendAxisTitleY(false);
            drawPlots();
            drawLegends();
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
        },
        updateMutations: function(_divName, _divName_x_scale, _divName_y_scale) {
            var _showMutations = document.getElementById(_divName).checked;
            
            //Get applying log scale status
            var _applyLogScale_x = false;
            var _applyLogScale_y = false;
            if(!(document.getElementById(_divName_x_scale) === null) && 
               !(document.getElementById(_divName_y_scale) === null)) { 
               //for studies wihtout log scale option
                _applyLogScale_x = document.getElementById(_divName_x_scale).checked;
                _applyLogScale_y = document.getElementById(_divName_y_scale).checked;
            }  

            if (_showMutations) {
                drawPlots();
                drawLegends();
            } else {
                hideMutations();
            }
            //Reapply log scale (lost during re-draw dots)
            updatePlotsLogScale("x", _applyLogScale_x);
            updatePlotsLogScale("y", _applyLogScale_y);
            addQtips();
        },
        updateTitleHelp: function(_divName_x_scale, _divName_y_scale) {
            //Get applying log scale status
            var _applyLogScale_x = false;
            var _applyLogScale_y = false;
            if(!(document.getElementById(_divName_x_scale) === null) && 
               !(document.getElementById(_divName_y_scale) === null)) { 
               //for studies wihtout log scale option
                _applyLogScale_x = document.getElementById(_divName_x_scale).checked;
                _applyLogScale_y = document.getElementById(_divName_y_scale).checked;
            }  
            appendAxisTitleX(_applyLogScale_x);
            appendAxisTitleY(_applyLogScale_y);
        },
        jointBrushCallback: function(_refreshCallback) {
            updateBrushCallback = _refreshCallback;
        },
        updateStyle: function(_datumArr) {
            var _caseIdList = [];
            $.each(_datumArr, function(index, obj) {
                _caseIdList.push(_datumArr[index].case_id);
            });
            elem.dotsGroup.selectAll("path").each(function(d) {
                if (_caseIdList.indexOf(d.case_id) !== -1) {
                    var _index = _caseIdList.indexOf(d.case_id);
                    $(this).attr("fill", _datumArr[_index].fill);
                    $(this).attr("stroke", _datumArr[_index].fill);
                }
            });
        }

    }
}



