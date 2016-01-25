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
 */
var ScatterPlot = function(divName, plotData) {
    var self=this;
    var prevSpecialSelection=[], prevSelection=[];
    var traces=[];
    var dataTrace;

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
        dragmode: 'select', // box select
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
     * adds a drag listener to the plotly plot
     * @param callback
     */
    this.addDragListener = function(callback) {
        var graphDiv = document.getElementById(divName);
        addDoubleClick(callback);

        graphDiv.on('plotly_selected', function (eventData) {
            // workaround for a bug: if you click in the graph while the drag-mode is enabled,
            // eventData is undefined
            if(eventData!=undefined) {
                var draggedData = [];
                // find the gene name of the selected point
                eventData.points.forEach(function (pt) {
                    draggedData.push(dataTrace.text[pt.pointNumber]);
                });

                // if all points are selected clear the filter
                if (draggedData.length == dataTrace.text.length) {
                    callback([]);
                }
                // if no points are selected, filter everything
                else if (draggedData.length == 0) {
                    callback(["FilterAll"]);
                }
                // otherwise filer using the found gene names
                else {
                    callback(draggedData);
                }
            }
        });
    }

    /**
     * add functionality for filter clearing on double-click
     * This is a workaround until plotly allows you to make the distinction
     * @param callback
     */
    function addDoubleClick(callback){
        var clickCount = 0;
        var myPlot = document.getElementById(divName);
        var singleClickTimer;
        myPlot.addEventListener('click', function() {
            clickCount++;
            // if the clickCount is 1, set a timer which resets the clickCount to 0
            // if within the 400ms there is another click, we assume it's a double-click and clear the filter
            // by callin the callback with an empty array
            if (clickCount === 1) {
                singleClickTimer = setTimeout(function() {
                    clickCount = 0;
                }, 400);
            } else if (clickCount === 2) {
                clearTimeout(singleClickTimer);
                clickCount = 0;
                callback([]);
            }
        });
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
