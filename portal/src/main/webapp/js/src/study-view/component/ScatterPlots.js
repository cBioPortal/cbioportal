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
 * @param: dataArr -- Json object from data proxy (x value, y value, qtip content, sample id, etc.)
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
        dataAttr = {},
        brushedCases = [];

    var axis_edge = 0.1;

    var updateBrushCallback = "",
        clickCallback = "",
        brushOn = false;

    var axisXLogFlag = false,
        axisYLogFlag = false;

    var shiftKeyDown = false;
    
    var hasZeroX = false,
        zeroMappedLogValX = 0,
        minValX = 100000;

    var hasZeroY = false,
        zeroMappedLogValY = 0,
        minValY = 100000;

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
                if (obj.x_val !== 0 && minValX > obj.x_val) {
                    minValX = obj.x_val;
                } else if(obj.x_val === 0) {
                    hasZeroX = true;
                }
                if (obj.y_val!== 0 && minValY > obj.y_val) {
                    minValY = obj.y_val;
                } else if(obj.y_val === 0) {
                    hasZeroY = true;
                }
                dataArr.push(obj);
            }
        });
        
        if(hasZeroX) {
            zeroMappedLogValX = parseInt(Math.log(minValX) / Math.log(10)) -1;
        }
        
        if(hasZeroY) {
            zeroMappedLogValY = parseInt(Math.log(minValY) / Math.log(10)) -0.5;
        }
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
            brushOn = true;
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
            .ticks(5)
            .tickSize(6, 0, 0);
    }

    function initAxisY() {
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .orient("left")
            .ticks(5)
            .tickSize(6, 0, 0);
    }
    
    function initLogAxisY() {
        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .orient("left")
            .ticks(5)
            .tickSize(6, 0, 0)
            .tickFormat(function(v) {
                if(hasZeroY && v === zeroMappedLogValY) {
                    return 0;
                }else {
                    return parseInt(Math.pow(10, v));
                }
            });
    }
    
    function initLogAxisX() {
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom")
            .ticks(5)
            .tickSize(6, 0, 0)
            .tickFormat(function(v) {
                if(hasZeroX && v === zeroMappedLogValX) {
                    return 0;
                }else {
                    return cbio.util.toPrecision(Math.pow(10, v),1,0.001);
                }
            });
    }

    function generateAxisX() {
        d3.select("#" + names.body).select(".plots-x-axis").remove();
        elem.axisGroup.append("g")
            .style("stroke-width", 1)
            .style("fill", "none")
            .style("stroke", "black")
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
            .style("stroke-width", 1)
            .style("fill", "none")
            .style("stroke", "black")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, " + canvas.yTop + ")")
            .call(elem.xAxis.orient("bottom").ticks(0));
    }

    function generateAxisY() {
        d3.select("#" + names.body).select(".plots-y-axis").remove();
        elem.axisGroup.append("g")
            .style("stroke-width", 1)
            .style("fill", "none")
            .style("stroke", "black")
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
            .style("stroke-width", 1)
            .style("fill", "none")
            .style("stroke", "black")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(" + canvas.xRight + ", 0)")
            .call(elem.yAxis.orient("left").ticks(0));
    }

    function appendAxisTitleX(_applyLogScale) {
        d3.select("#" + names.body).select(".plots-title-x").remove();
        d3.select("#" + names.body).select(".plots-title-x-help").remove();
        d3.select("#" + names.body).select("#plots-title-x-checkbox").remove();
        var _xTitle = "",
            _checked = "";
        if (_applyLogScale) {
            _xTitle = text.xTitle + " (log)";
        } else {
            _xTitle = text.xTitle;
        }
        elem.axisTitleGroup.append("text")
            .attr("x", canvas.xLeft + (canvas.xRight - canvas.xLeft) / 2 - 15)
            .attr("y", canvas.yBottom + 60)
            .style("text-anchor", "middle")
            .style("font-size", "11px")
            .style("font-weight", "bold") 
            .attr("class", "plots-title-x")
            .text(_xTitle);
        elem.axisTitleGroup.append("svg:image")
            .attr("xlink:href", "images/info.png")
            .attr("class", "plots-title-x-help")
            .attr("x", canvas.xLeft + (canvas.xRight - canvas.xLeft) / 2 - _xTitle.length / 2 * 8 - 15)
            .attr("y", canvas.yBottom + 48)
            .attr("width", "14")
            .attr("height", "14");
        elem.svg.select(".plots-title-x-help").each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=1>" + text.xTitleHelp + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'right bottom', at:'top left', viewport: $(window)}
                    }
                );
            }
        );

        if(axisXLogFlag) {
            _checked = "checked";
        }else {
            _checked = "";
        }
        
        elem.axisTitleGroup.append("svg:foreignObject")
            .attr("id", "plots-title-x-checkbox")
            .attr("x", canvas.xLeft + (canvas.xRight - canvas.xLeft) / 2 + _xTitle.length / 2 * 8 - 35)
            .attr("y", canvas.yBottom + 48)
            .attr("width", "50")
            .attr("height", "15")
            .append("xhtml:body")
            .style({"font-size": "11px", "margin": "0"})
            .html("<input id='study-view-scatter-plot-log-scale-x' type='checkbox' style='float:left' "+_checked+"/><span style='float:left; margin-top: 2px; font-size-adjust: 0.5;'>Log</span>");
        $("#study-view-scatter-plot-log-scale-x").change(function() {
            if($(this).prop("checked")){
                axisXLogFlag = true;
            }else {
                axisXLogFlag = false;
            }
            updateScaleX();
        });
    }

    function appendAxisTitleY() {
        d3.select("#" + names.body).select(".plots-title-y").remove();
        d3.select("#" + names.body).select(".plots-title-y-help").remove();
        d3.select("#" + names.body).select("#plots-title-y-checkbox").remove();
        var _yTitle = "",
            _checked = "";
//        if (_applyLogScale) {
//            _yTitle = text.yTitle + " (log)";
//        } else {
            _yTitle = text.yTitle;
//        }
        elem.axisTitleGroup.append("text")
            .attr("transform", "rotate(-90)")
            .attr("x", (canvas.yTop - canvas.yBottom) / 2 - canvas.yTop)
            .attr("y", canvas.xLeft - 60)
            .style("text-anchor", "middle")
            .style("font-size", "11px")
            .style("font-weight", "bold")
            .attr("class", "plots-title-y") 
            .text(_yTitle);
        elem.axisTitleGroup.append("svg:image")
            .attr("xlink:href", "images/info.png")
            .attr("class", "plots-title-y-help")
            .attr("x", canvas.xLeft - 72)
            .attr("y", canvas.yBottom - (canvas.yBottom - canvas.yTop) / 2 + _yTitle.length / 2 * 8 - 5)
            .attr("width", "14")
            .attr("height", "14");
        elem.svg.select(".plots-title-y-help").each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=1>" + text.yTitleHelp + "</font>"},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'right bottom', at:'top left', viewport: $(window)}
                    }
                );
            }
        );
        
        if(axisYLogFlag) {
            _checked = "checked";
        }else {
            _checked = "";
        }
        
        elem.axisTitleGroup.append("svg:foreignObject")
            .attr("id", "plots-title-y-checkbox")
            .attr("transform", "rotate(-90)")
            .attr("x", (canvas.yTop - canvas.yBottom) / 2 + _yTitle.length / 2 * 8 - 15)
            .attr("y", canvas.xLeft - 72)
            .attr("width", "50")
            .attr("height", "15")
            .append("xhtml:body")
            .style({"font-size": "11px", "margin": "0"})
            .html("<input id='study-view-scatter-plot-log-scale-y' type='checkbox' style='float:left' "+_checked+"/><span style='float:left; margin-top: 2px; font-size-adjust: 0.5;'>Log</span>");
        
        $("#study-view-scatter-plot-log-scale-y").change(function() {
            if($(this).prop("checked")){
                axisYLogFlag = true;
            }else {
                axisYLogFlag = false;
            }
            updateScaleY();
        });
    }

    function drawPlots(_dataArr) {
        elem.dotsGroup.selectAll("path").remove();
        elem.dotsGroup.selectAll("path")
            .data(_dataArr)
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
            });
        
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
    
    function addListeners() {
        //This code is oringinally coming from Onur. Listening shiftKey down and 
        //shiftKey up.
        $(window).on("keydown", function(event) {
            if (event.keyCode === 16)
            {
                shiftKeyDown = true;
            }
        });

        $(window).on("keyup", function(event) {
            if (event.keyCode === 16)
            {
                shiftKeyDown = false;
            }
        });
    }
    
    function addQtips() {
        elem.dotsGroup.selectAll('path').each(
            function(d) {
                $(this).qtip(
                    {
                        content: {text: d.qtip},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'right bottom', at:'top left', viewport: $(window)}
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
            $(this).css('cursor','pointer');
        };
        var mouseOff = function(d) {
            var dot = d3.select(this);
            var size = getMouseoutPointSize(this);
            
            dot.transition()
                .ease("elastic")
                .duration(600)
                .delay(100)
                .attr("d", d3.svg.symbol().size(size).type(style.shape));
        
//            elem.dotsGroup.selectAll("path").each(function(d) {
//                changePointSize(this);
//            });
        };
        //Click has three status: 1. Click; 2. ShiftClick; 3. Both
        var click = function(){
            d3.event.preventDefault();
            if(shiftKeyDown){
                shiftclicked(this);
            }else{
                clicked(this);
            }
        };
        
        elem.dotsGroup.selectAll("path").attr('pointer-events', 'all').on("mouseover", mouseOn);
        elem.dotsGroup.selectAll("path").attr('pointer-events', 'all').on("mouseout", mouseOff);
        elem.dotsGroup.selectAll("path").attr('pointer-events', 'all').on("click", click);
    }
    
    function getMouseoutPointSize(_element) {
        var _clickType = pointClickType(_element);
        
        switch(_clickType){
            case 'none':
                return style.size;
                break;
            default:
                return style.size;
        }
    }
    function changePointSize(_element) {
        var _clickType = pointClickType(_element);
        
        switch(_clickType){
            case 'clicked':
                $(_element).attr("d", d3.svg.symbol().size(style.size).type(style.shape));
                break;
            case 'shiftClicked':
                $(_element).attr("d", d3.svg.symbol().size(style.size).type(style.shape));
                break;
            case 'both':
                $(_element).attr("d", d3.svg.symbol().size(style.size).type(style.shape));
                break;
            
            //default: withOutClick
            default:
                $(_element).attr("d", d3.svg.symbol().size(style.size).type(style.shape));
        }
    }
    //Added in Study View especially
    function changeClickStyle(_element){
        var _clickType = pointClickType(_element);

        switch(_clickType){
            case 'clicked':
                $(_element).attr('stroke-width','3')
                            .attr('fill',style.fill)
                            .attr('stroke','red');
//                            .attr('opacity','1');
                break;
            case 'shiftClicked':
                $(_element).attr('stroke-width','0')
                            .attr('fill','red')
                            .attr('stroke','red');
//                            .attr('opacity','1');
                break;
            case 'both':
                $(_element).attr('stroke-width','3')
                            .attr('fill','red')
                            .attr('stroke',style.stroke);
//                            .attr('opacity','1');
                break;

            //default: withOutClick
            default:
//                if(brushedCases.length !== 0){
//                    $(_element).attr('stroke-width','0')
//                            .attr('fill',style.fill)
//                            .attr('stroke',style.stroke)
//                            .attr('opacity','0.6');
//                }else {
                    $(_element).attr('stroke-width','0')
                            .attr('fill',style.fill)
                            .attr('stroke',style.stroke);
//                            .attr('opacity','1');
//                }
        } 
    }
    
    //Added in Study View especially
    function pointClickType(_element){
        var _clickType,
            _attr = $(_element).attr('clicked');
        if(typeof _attr === 'undefined' || _attr === false)
            _clickType = 'none';
        else
            _clickType = _attr;
        return _clickType;
    }
    
    //This functions has been modified from original template.
    function clicked(_element){
        //Only allow click one point, return string to distinguish
        //the Click function and Shift Click function
        var _clickedCase = '',
            _attrType = pointClickType(_element);
        //UPdate click point style
        if(_attrType === 'both'){
            $(_element).attr('clicked','shiftClicked');
        }else if(_attrType === 'clicked'){
            $(_element).removeAttr('clicked');
        }else{
            elem.dotsGroup.selectAll("path").each(function(d) {
                var _subAttrType = pointClickType(this);
                if(_subAttrType === 'both'){
                    $(this).attr('clicked','shiftClicked');
                }else if(_subAttrType === 'clicked'){
                    $(this).removeAttr('clicked');
                }
                changeClickStyle(this);
            });
            if(_attrType === 'shiftClicked'){
                $(_element).attr('clicked','both');
            }else{
                $(_element).attr('clicked','clicked');
            }
        }
        changeClickStyle(_element);
        
        //Find the clicked point ID
        brushedCases.length = 0;
        elem.dotsGroup.selectAll("path").each(function(d) {
            var _subTyleAttr = pointClickType(this);
            if (_subTyleAttr === 'clicked' || _subTyleAttr === 'both') {
                _clickedCase = d.case_id;
            }
            if (_subTyleAttr === 'shiftClicked' || _subTyleAttr === 'both'){
                brushedCases.push(d.case_id);
            }
            changeClickStyle(this);
        });
        clickCallback(_clickedCase);
    }
    
    //Added in Study View especially
    function shiftclicked(_element){
        var _shiftClickedCases = [],
            _attrType = pointClickType(_element);
        
        //UPdate click point style
        if(_attrType === 'shiftClicked' || _attrType === 'both'){
            $(_element).removeAttr('clicked');
        }else if(_attrType === 'clicked'){
            $(_element).attr('clicked','shiftClicked');
        }else{
            $(_element).attr('clicked','shiftClicked');
        }
        
        elem.dotsGroup.selectAll("path").each(function(d) {
            var _subAttrType = pointClickType(this);
            if (_subAttrType === 'both' || _subAttrType === 'shiftClicked') {
                _shiftClickedCases.push(d.case_id);
                if(_subAttrType === 'both'){
                    $(this).attr('clicked','shiftClicked');
                }
            }else if(_subAttrType === 'clicked' ){
                $(this).attr('clicked','shiftClicked');
                _shiftClickedCases.push(d.case_id);
            }
        });
        
        brushedCases = _shiftClickedCases;
        
        elem.dotsGroup.selectAll("path").each(function(d) {
            changeClickStyle(this);
        });
            
        clickCallback(brushedCases);
    }
    
    //This functions has been modified from original template.
    function brushended(event) {
        var _brushedCases = [],
            _totalHighlightIds = [];
        
        var extent = elem.brush.extent();
        
        elem.dotsGroup.selectAll("path").each(function(d) {
            var _x = $(this).attr("x_val"),
                _y = $(this).attr("y_val");
        
            if (_x > extent[0][0] && _x < extent[1][0] &&
                        _y > extent[0][1] && _y < extent[1][1]) {
                _totalHighlightIds.push(d.case_id);
            }     
        });
        
        
        
        if(_totalHighlightIds.length > 0) {
            _totalHighlightIds = [];
            _brushedCases.length = 0;

            if(shiftKeyDown){
                elem.dotsGroup.selectAll("path").each(function(d) {
                    var _attrType = pointClickType(this),
                        _x = $(this).attr("x_val"),
                        _y = $(this).attr("y_val");    

                    if (_x > extent[0][0] && _x < extent[1][0] &&
                        _y > extent[0][1] && _y < extent[1][1]) {
                        if(_attrType === 'shiftClicked'){
                            $(this).removeAttr('clicked');
                        }else{
                            $(this).attr('clicked','shiftClicked');
                            _brushedCases.push(d.case_id);
                        }
                    }
                    if(_attrType === 'clicked'){
                        $(this).attr('clicked','shiftClicked');
                        _brushedCases.push(d.case_id);
                    }

                    changeClickStyle(this);
                    _attrType = pointClickType(this);
                    if(_attrType !== 'none'){
                        _totalHighlightIds.push(d.case_id);
                    }
                });

            }else{
                elem.dotsGroup.selectAll("path").each(function(d) {
                    var _attrType = pointClickType(this),
                        _x = $(this).attr("x_val"),
                        _y = $(this).attr("y_val");    

                    if (_x > extent[0][0] && _x < extent[1][0] &&
                        _y > extent[0][1] && _y < extent[1][1]) {
                        //TODO: does not work with log scale applied scenario
                        $(this).attr('clicked','shiftClicked');
                        _brushedCases.push(d.case_id);
                    }else{
                        if(_attrType !== 'none'){
                            $(this).removeAttr('clicked');
                        }
                    }
                    changeClickStyle(this);
                    _totalHighlightIds = _brushedCases;
                });
            }
            brushedCases = _totalHighlightIds;

            updateBrushCallback(_totalHighlightIds);
        }
        
        d3.select(".brush").call(elem.brush.clear());
    }

    function updatePlotsLogScale(_axis, _applyLogScale) {
        elem.dotsGroup.selectAll("path")
            .transition().duration(300)
            .attr("transform", function() {
                if (_applyLogScale) {
                    if (_axis === "x") {
                        var _post_x_val = "";
                        
                        if(d3.select(this).attr("x_val") === '0') {
                            _post_x_val = zeroMappedLogValX;
                        }else {
                            _post_x_val = Math.log(d3.select(this).attr("x_val")) / Math.log(10);
                        }
                        var _post_x = elem.xScale(_post_x_val);
                        var _post_y = d3.select(this).attr("y_pos");
                        
                        d3.select(this).attr("x_val",Math.log(d3.select(this).attr("x_val")) / Math.log(10));
                        axisXLogFlag = true;
                        
                    } else if (_axis === "y") {
                        var _post_x = d3.select(this).attr("x_pos");
                        var _post_y_val = "";
                        
                        if(d3.select(this).attr("y_val") === '0') {
                            _post_y_val = zeroMappedLogValY;
                        }else {
                            _post_y_val = Math.log(d3.select(this).attr("y_val")) / Math.log(10);
                        }
                        var _post_y = elem.yScale(_post_y_val);
                        d3.select(this).attr("y_val",_post_y_val);
                        axisYLogFlag = true;
                    }
                    d3.select(this).attr("x_pos", _post_x);
                    d3.select(this).attr("y_pos", _post_y);
                    return "translate(" + _post_x + ", " + _post_y + ")";
                } else {
                    if (_axis === "x") {
                        var _post_x_val = "";
                        if(hasZeroX && d3.select(this).attr("x_val") === zeroMappedLogValX.toString()) {
                            _post_x_val = 0;
                        }else {
                            _post_x_val = Math.pow(10,d3.select(this).attr("x_val"));
                        }
                        d3.select(this).attr("x_val",_post_x_val);
                        var _post_x = elem.xScale(d3.select(this).attr("x_val"));
                        var _post_y = d3.select(this).attr("y_pos");
                        axisXLogFlag = false;
                    } else if (_axis === "y") {
                        var _post_y_val = "";
                        if(hasZeroY && d3.select(this).attr("y_val") === zeroMappedLogValY.toString()) {
                            _post_y_val = 0;
                        }else {
                            _post_y_val = Math.pow(10,d3.select(this).attr("y_val"));
                        }
                        d3.select(this).attr("y_val",_post_y_val);
                        var _post_x = d3.select(this).attr("x_pos");
                        var _post_y = elem.yScale(d3.select(this).attr("y_val"));
                        axisYLogFlag = false;
                    }
                    d3.select(this).attr("x_pos", _post_x);
                    d3.select(this).attr("y_pos", _post_y);
                    return "translate(" + _post_x + ", " + _post_y + ")";
                }
            });
    }

    function updateAxisScaleX() {
        var _min_x, _max_x, _edge_x;
        if(hasZeroX) {
            _min_x = zeroMappedLogValX;
        } else{
            _min_x = Math.log(dataAttr.min_x) / Math.log(10);
        }
        _max_x = Math.log(dataAttr.max_x) / Math.log(10);
        _edge_x = (_max_x - _min_x) * axis_edge;
        elem.xScale = d3.scale.linear()
            .domain([_min_x - _edge_x, _max_x + _edge_x])
            .range([canvas.xLeft, canvas.xRight]);
    }

    function updateAxisScaleY() {
        var _min_y, _max_y, _edge_y;
        _max_y = Math.log(dataAttr.max_y) / Math.log(10);
        if(hasZeroY) {
            _min_y = zeroMappedLogValY;
        } else{
            _min_y = Math.log(dataAttr.min_y) / Math.log(10);
        }
        _edge_y = (_max_y - _min_y) * axis_edge;
        elem.yScale = d3.scale.linear()
            .domain([_min_y - _edge_y, _max_y + _edge_y])
            .range([canvas.yBottom, canvas.yTop]);
    }
    
    function updateBrush() {
        elem.brush.x(elem.xScale);
        elem.brush.y(elem.yScale);
    }
    
    function log2Value(_value){
        
        if(typeof log_scale_threshold !== 'undefined'){
            if(_value <= log_scale_threshold){
                _value = Math.log(log_scale_threshold) / Math.log(2);
            }else{
                _value = Math.log(_value) / Math.log(2);
            }
        }else{
            _value = Math.log(_value) / Math.log(2);
        }
        return _value;
    }
    
    function log10Value(_value){
        
        if(typeof log_scale_threshold !== 'undefined'){
            if(_value <= log_scale_threshold){
                _value = Math.log(log_scale_threshold) / Math.log(10);
            }else{
                _value = Math.log(_value) / Math.log(10);
            }
        }else{
            _value = Math.log(_value) / Math.log(10);
        }
        return _value;
    }
    
    function updateScaleX() {
//        var _applyLogScale = document.getElementById(_divName).checked;
            
        if (axisXLogFlag) {
            updateAxisScaleX();
            initLogAxisX();
        } else {
            initScaleX();
            initAxisX();
        }

        if(brushOn){
            updateBrush();
        }
        generateAxisX();
        appendAxisTitleX();
        updatePlotsLogScale("x", axisXLogFlag);
    }
    
    function updateScaleY() {   //_applyLogScale: boolean, true for apply scale, false for  original value)
        if (axisYLogFlag) {
            updateAxisScaleY();
            initLogAxisY();
        } else {
            initScaleY();
            initAxisY();
        }
        if(brushOn)
            updateBrush();
        generateAxisY();
        appendAxisTitleY();
        updatePlotsLogScale("y", axisYLogFlag);
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
            drawPlots(dataArr);
            drawLegends();
            addListeners();
            addQtips();
        },
        // !!! Log Scale are only used by using RNA Seq Profile
        updateScaleX: updateScaleX,
        
        updateScaleY: updateScaleY,
        setAxisXLogFlag: function(_flag) {
            axisXLogFlag = _flag;
        },
        setAxisYLogFlag: function(_flag) {
            axisYLogFlag = _flag;
        },
        getBrushedCases: function() {
            return brushedCases;
        },
        getHighlightCases: function() {
            var _highLightCases = [];
            
            elem.dotsGroup.selectAll("path").each(function(d) {
                var _attrType = pointClickType(this);
                
                if(_attrType === 'shiftClicked'){
                    _highLightCases.push(d.case_id);
                }
            });
            return _highLightCases;
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
                drawPlots(dataArr);
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
        jointClickCallback: function(_clickCallback) {
            clickCallback = _clickCallback;
        },
        
        //This functions has been modified from original template.
        updateStyle: function(_datumArr) {
            var _tmpDataArr=[];
            var dataCopy = jQuery.extend(true,[],dataArr);
            
            if(axisXLogFlag && axisYLogFlag){
                for(var i=0; i<dataCopy.length; i++){
                    if(dataCopy[i].x_val === 0) {
                        dataCopy[i].x_val = zeroMappedLogValX;
                    }else {
                        dataCopy[i].x_val = log10Value(dataCopy[i].x_val);
                    }
                    if(dataCopy[i].y_val === 0) {
                        dataCopy[i].y_val = zeroMappedLogValY;
                    }else {
                        dataCopy[i].y_val = log10Value(dataCopy[i].y_val);
                    }
                }
            }else if(axisXLogFlag){
                for(var i=0; i<dataCopy.length; i++){
                    if(dataCopy[i].x_val === 0) {
                        dataCopy[i].x_val = zeroMappedLogValX;
                    }else {
                        dataCopy[i].x_val = log10Value(dataCopy[i].x_val);
                    }
                }
            }else if(axisYLogFlag){
                for(var i=0; i<dataCopy.length; i++){
                    if(dataCopy[i].y_val === 0) {
                        dataCopy[i].y_val = zeroMappedLogValY;
                    }else {
                        dataCopy[i].y_val = log10Value(dataCopy[i].y_val);
                    }
                }
            }
            
            for(var j=0 ; j< _datumArr.length ; j++){
                if(_datumArr[j].fill !== 'red') {
                    for(var i=0 ; i< dataCopy.length ; i++){
                        if(_datumArr[j].case_id === dataCopy[i].case_id){
                            _tmpDataArr.push(dataCopy[i]);
                            break;
                        }
                    }
                }
            }
            
            for(var j=0 ; j< _datumArr.length ; j++){
                if(_datumArr[j].fill === 'red') {
                    for(var i=0 ; i< dataCopy.length ; i++){
                        if(_datumArr[j].case_id === dataCopy[i].case_id ){
                            _tmpDataArr.push(dataCopy[i]);
                            break;
                        }
                    }
                }
            }
            dataCopy = _tmpDataArr;
            drawPlots(dataCopy);
            addQtips();
            
            var _caseIdList = [];
            $.each(_datumArr, function(index, obj) {
                _caseIdList.push(_datumArr[index].case_id);
            });
            elem.dotsGroup.selectAll("path").each(function(d) {
                if (_caseIdList.indexOf(d.case_id) !== -1) {
                    var _index = _caseIdList.indexOf(d.case_id);
                    $(this).attr("fill", _datumArr[_index].fill);
                    $(this).attr("stroke", _datumArr[_index].stroke);
//                    $(this).attr("opacity", _datumArr[_index].opacity);
                    $(this).attr("d", d3.svg.symbol().size(_datumArr[_index].size).type(style.shape));
                    $(this).attr("stroke-width", _datumArr[_index].strokeWidth);
                    if(_datumArr[_index].fill === style.fill && _datumArr[_index].stroke === 'red'){
                        $(this).attr("clicked", 'clicked');
                    }else if(_datumArr[_index].fill === 'red' && _datumArr[_index].stroke === 'red'){
                        $(this).attr("clicked", 'shiftClicked');
                    }else if(_datumArr[_index].fill === 'red' && _datumArr[_index].stroke === style.stroke){
                        $(this).attr("clicked", 'both');
                    }
                }
            });            
        }

    };
};