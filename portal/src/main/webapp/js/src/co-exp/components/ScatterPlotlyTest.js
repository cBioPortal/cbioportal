/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License,
 * version 3, or (at your option) any later version.
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

/**
 * Creates a scatterplot using plotly
 *
 * @param divName: where the plot has to be created
 * @param plotData: the data, provided in the plotly way. PlotData should contain an x, y and text array
 * @param plotDataAttr: contains information about the max and min x and y values
 */
var ScatterPlot = function(divName, plotData, plotDataAttr) {
    var self=this;
    var prevSpecialSelection=[], prevSelection=[];
    var traces=[];
    var dataTrace;

    // used for determining whether to filter the execute the drag callback
    var prevDraggedData={
        xMin: plotDataAttr.min_x,
        xMax: plotDataAttr.max_x,
        yMin: plotDataAttr.min_y,
        yMax: plotDataAttr.max_y
    };


    // default layout options for the plot
    var defaultPlotLayout={
        autosize: false,
        width: 400,
        height: 400,
        margin: {
            l: 50,
            r: 10,
            b: 75,
            t: 50,
            pad: 4
        },
        hovermode:'closest',
        showlegend: false,
        xaxis: {
            titlefont: {
                color: 'black',
                family: 'Verdana, Arial, sans-serif'
            },
            autorange: true,
            showgrid: true,
            zeroline: true,
            showline: false,
            autotick: true,
            showticklabels: true
        },
        yaxis: {
            titlefont: {
                color: 'black',
                family: 'Verdana, Arial, sans-serif'
            },
            autorange: true,
            showgrid: true,
            zeroline: true,
            showline: false,
            autotick: true,
            showticklabels: true
        }
    };

    // default options for traces
    var defaultTraceLayout={
        mode: 'markers',
        type: 'scatter',
        hoverinfo: 'text',          // on hover, show the content of the text array for the point
        marker:{
            opacity: 0.4,
            color: '#58ACFA',
            symbol: "circle",
            size: 6
        },
        stroke: '#58ACFA',          // not a plotly variable
        strokewidth: 10,            // not a plotly variable
        specialClickColor: 'red'    // not a plotly variable
    };

    /**
     * add / overwrite some layout options if desired
     * @param newOptions: the new options
     */
    this.addPlotLayoutOptions = function(newOptions){
        $.extend(true, defaultPlotLayout, newOptions);
    }

    /**
     * add a new trace
     * @param trace: a trace with datapoints. Should contain at least x and y arrays
     */
    this.addTrace = function(trace) {
        // create full trace by extending defaultTraceLayout with the trace
        var fullTrace = $.extend({}, defaultTraceLayout, trace);
        traces.push(fullTrace);
    }

    /**
     * draw the plot and add hover effect
     */
    this.drawPlot = function (){
        Plotly.newPlot(divName, traces, defaultPlotLayout);
        addHover();
    }

    /**
     * Based on the eventData, determine which points are in the current area. Also determine the minX, maxX, minY and maxY
     * for the datapoints to possibly prevent the callback from occurring.
     * @param eventData: contains the boundaries of the current area. However this data can have multiple formats. For example:
     *    autoscale button clicked: Object {xaxis.autorange: true, yaxis.autorange: true}
     *    zoom button clicked: Object {xaxis.range: Array[2], yaxis.range: Array[2]}
     *    drag zoom: Object {xaxis.range[0]: <value>, xaxis.range[1]: <value>, yaxis.range[0]: <value>, yaxis.range[1]: <value>}
     *    pan follows drag zoom, however, if you pan along one axis, the other axis will have not have its range defined
     * @returns {*}: the genes found in the current area and the max and min x and y vales of the dragged genes
     */
    function findDraggedGenes(eventData){
        var draggedGenes=[];
        var xVal, yVal, xMin=99, xMax=-99, yMin=99, yMax=-99;

        // if autoscale has been clicked, reset the values to their originals
        if(autoscaleButtonClicked(eventData)){
            draggedGenes = dataTrace.text;
            xMin = plotDataAttr.min_x;
            xMax = plotDataAttr.max_x;
            yMin = plotDataAttr.min_y;
            yMax = plotDataAttr.max_y;
        }

        else {
            var xLeft, xRight, yBottom, yTop;
            // when clicking the zoom in or zoom out button, the eventdata follows a different format
            if(zoomButtonClicked(eventData)){
                xLeft = eventData['xaxis.range'][0];
                xRight = eventData['xaxis.range'][1];
                yBottom = eventData['yaxis.range'][0];
                yTop = eventData['yaxis.range'][1];
            }
            // zoom by dragging or pan
            else {
                // when you drag over the edge of the plot and when you pan on only one axis, plotly doesn't set the ranges
                // in such as case, set it to the previous value
                xLeft = eventData['xaxis.range[0]'] === undefined ? prevDraggedData.xMin : eventData['xaxis.range[0]'];
                xRight = eventData['xaxis.range[1]'] === undefined ? prevDraggedData.xMax : eventData['xaxis.range[1]'];
                yBottom = eventData['yaxis.range[0]'] === undefined ? prevDraggedData.yMin : eventData['yaxis.range[0]'];
                yTop = eventData['yaxis.range[1]'] === undefined ? prevDraggedData.yMax : eventData['yaxis.range[1]'];
            }

            // loop over the array, retrieve the x and y of the points and check which points are inside the selected range
            for (var i = 0; i < dataTrace.x.length; i++) {
                xVal = dataTrace.x[i];
                yVal = dataTrace.y[i];
                // check whether the point is in the area
                if (xVal >= xLeft && xVal <= xRight && yVal >= yBottom && yVal <= yTop) {
                    // add it for filtering
                    draggedGenes.push(dataTrace.text[i]);
                    // keep track of minimum and maximum to possibly prevent filtering
                    if (xVal < xMin){
                        xMin = xVal;
                    }
                    if (xVal > xMax){
                        xMax = xVal;
                    }
                    if (yVal < yMin){
                        yMin = yVal;
                    }
                    if (yVal > yMax){
                        yMax = yVal;
                    }
                }
            }
        }

        return {
            xMin: xMin,
            xMax: xMax,
            yMin: yMin,
            yMax: yMax,
            draggedGenes: draggedGenes
        };
    }

    /**
     * Check whether autoscale is clicked
     * @param eventData: when autoscale is clicked, the eventData contains autorange for both axis
     * @returns {boolean}
     */
    function autoscaleButtonClicked(eventData){
        return eventData['xaxis.autorange'] && eventData['yaxis.autorange'];
    }

    /**
     * Check whether either zoom in or zoom out was clicked
     * @param eventData: when the zoom in or zoom out button is clicked, the eventData follows a different format
     * @returns {boolean}
     */
    function zoomButtonClicked(eventData){
        return eventData['xaxis.range'] != undefined;
    }

    /**
     * add hover functionality which increases the size of a symbol by using the stroke-width
     * a feature request for plotly has been posted to allow for easier updating of e.g. size, which would now
     * require a plotly_restyle with a complete array, as currently it's only possible to restyle complete traces
     */
    function addHover(){
        var myPlot = document.getElementById(divName);

        // on hover, find the element on which we are hovering, set the stroke and the stroke-width
        // the effect is that the symbol will seem to expand
        myPlot.on('plotly_hover', function(data){
            var curElement = getHoveredElement(data);
            curElement.style('stroke', dataTrace.stroke);
            curElement.style('stroke-width', dataTrace.strokewidth);

        })
        // on unhover, set the stroke-width to 0, which will cause the symbol to shrink back to its original size
        .on('plotly_unhover', function(data){
            var curElement = getHoveredElement(data);
            curElement.style('stroke-width', 0);
        });
    }

    /**
     * find the point element based on the hovered data's text
     * @param data, provided by plotly_hover
     * @returns the d3 element
     */
    function getHoveredElement(data){
        var geneHovered = data.points[0].data.text[data.points[0].pointNumber];
        var allPathElements = d3.select("#"+divName).selectAll(".point");
        var curElement = allPathElements.filter(function(d) {
            return d.tx==geneHovered;
        });
        return curElement;
    }

    /**
     * add click functionality
     * @param callback: callback function when click occurs
     */
    this.addClickListener = function (callback){
        var myPlot = document.getElementById(divName);
        // closest point
        myPlot.on('plotly_click', function(data){
            callback(data.points[0]);
        });
    }

    /**
     * add drag functionality
     * @param callback: callback function when drag occurs
     */
    this.addDragListener = function(callback){
        // plotly currently doesn't expose many events, so for the drag functionality
        // we need to use the plotly_relayout
        // this also fires when someone double-clicks (zoom-out)
        $('#'+divName).on('plotly_relayout', function(event,eventdata){
            // find the dragged genes and xMin, xMax, yMin and yMax for the data
            var draggedData = findDraggedGenes(eventdata);

            // if the previously dragged data's min and max coordinates are the same as the currently
            // dragged data min and max coordinates, this implies that the datapoints in the area must be the same
            // if that is the case, do not execute the callback
            if(prevDraggedData.xMax!=draggedData.xMax || prevDraggedData.xMin!=draggedData.xMin || prevDraggedData.yMax!=draggedData.yMax || prevDraggedData.yMin!=draggedData.yMin){
                // if the draggedData contains all the genes, return an empty list
                if(draggedData.draggedGenes.length == dataTrace.text.length) {
                    callback([]);
                }
                else if(draggedData.draggedGenes.length==0){
                    callback(["FilterAll"]);
                }
                else {
                    callback(draggedData.draggedGenes);
                }
            }
            prevDraggedData = draggedData;
        });
    }

    /**
     * function that does the actual dot colouring in the scatterplot
     * @param addElements: elements which should get the addColour
     * @param removeElements: elements which should get the removeColour
     * @param addColour: colour for the addElements
     * @param removeColour: colour for the removeElements
     */
    function colorDots(addElements, removeElements, addColour, removeColour){
        // find the point elements
        var allPathElements = d3.select("#"+divName).selectAll(".point");

        for(var i=0; i<addElements.length; i++){
            // find the proper element by filtering the data based on case_id
            var curElement = allPathElements.filter(function(d) {
                return d.tx==addElements[i];
            });
            curElement.style("fill", addColour);
        }

        for(var i=0; i<removeElements.length; i++){
            // find the proper element by filtering the data based on case_id
            var curElement = allPathElements.filter(function(d) {
                return d.tx==removeElements[i]
            });
            curElement.style("fill", removeColour);
        }
    }

    /**
     * colour the selection in lightgrey and restore to the marker.color
     * @param case_ids: the genes to colour
     * @param inverse: optionally provide inverse to color the not-selected to lightgrey
     */
    this.showSelection = function(case_ids, inverse){
        if(inverse){
            case_ids = _.difference(dataTrace.text, case_ids);
        }

        // determine which case identifiers should be coloured and which should have their colour restored
        // ensure this doesn't affect the specialSelection by removing these items from the selection
        var addSelect = _.difference(_.difference(case_ids, prevSelection), prevSpecialSelection);
        var removeSelect = _.difference(_.difference(prevSelection, case_ids), prevSpecialSelection);

        colorDots(addSelect, removeSelect, 'lightgrey', dataTrace.marker.color)
        // keep track of the selection
        prevSelection = case_ids.slice();
    }

    /**
     * sets case_ids to the specialClickColor
     * @param case_ids: the genes to colour
     */
    this.specialSelectItems = function(case_ids) {
        // determine which case identifiers should be coloured and which should have their colour restored
        var addSpecialSelect = _.difference(case_ids, prevSpecialSelection);
        var removeSpecialSelect = _.difference(prevSpecialSelection, case_ids);

        colorDots(addSpecialSelect, removeSpecialSelect, dataTrace.specialClickColor, dataTrace.marker.color);
        // keep track of the selection
        prevSpecialSelection = case_ids.slice();
    }

    /**
     * on init add the plotData
     */
    function init(){
        self.addTrace(plotData);
        dataTrace = traces[0];
    }

    init();
 }
