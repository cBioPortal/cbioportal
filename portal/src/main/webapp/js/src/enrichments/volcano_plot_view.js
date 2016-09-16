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
 * Class to render the d3js volcano plot, coupling it to a dataTable
 * such that the dataTable displays the selections made in the plot
 * and vice-versa.
 */
function VolcanoPlot() {

    var self = this;
    self.scatterPlot = null;

    /**
     * Trigger the rendering of the plot.
     *
     * @param orTable: the enrichmentsTabTable, which contains the miniOnca, originalData, etc.
     */
    this.render = function(orTable){
        self.orTable = orTable;
        self.miniOnco = orTable.miniOnco;

        var plotDataAttr = {
            min_x: 99,
            max_x: -99,
            min_y: 99,
            max_y: -99,
            abs_max_x: 0,
            abs_max_y: 0
        };

        // prepare the data
        var dataTrace = createDataTrace(orTable.originalData, plotDataAttr)

        //draw the plot with the prepared x,y coordinates data:
        drawPlot(dataTrace, orTable.plot_div, plotDataAttr);
    }

    /**
     *
     * @param element: the current element of the data array
     * @param convertToLog: whether the data may have to be converted to log
     * @returns: the log ratio to be used
     */
    function getXValue(element, convertToLog){
        var xVal;
        // if the data may require log ratio calculation
        if (self.orTable.requiresLogRatioCalculation()){
            // check whether the data is already LOG-VALUE. If it is not, divide and calculate ratio
            if(convertToLog){
                xVal = Math.log2(element["mean of alteration in altered group"] / element["mean of alteration in unaltered group"])
            }
            // else we're already in log space; subtract.
            else{
                xVal = element["mean of alteration in altered group"] - element["mean of alteration in unaltered group"];
            }
        }
        // if the data already has "Log Ratio", use that
        else{
            xVal = parseFloat(element["Log Ratio"].replace("<","").replace(">","")); //remove < and > from <-10 or >10 scenarios
        }
        return xVal;
    }

    /**
     * create data trace based on data and fill plotDataAttr with min and max values
     * @param data
     * @param plotDataAttr
     * @returns {{x: Array, y: Array, text: Array}}
     */
    function createDataTrace(data, plotDataAttr){
        var xValues=[], yValues=[], labels=[];
        var convertToLog = !self.orTable.hasLogData();

        //build up plots x,y coordinates list based on data columns "Log Ratio" and "p-Value" received:
        //TODO this could be made generic to support also other data with different column names (perhaps add a presenter layer that makes the translation of the specific column names to the generic ones to be supported here)
        for (var i = 0; i < data.length; i++){
            var xValue = getXValue(data[i], convertToLog);
            var yValue = -Math.log10(data[i]["p-Value"]);
            if (xValue < plotDataAttr.min_x) {
                plotDataAttr.min_x = xValue;
            }
            if (xValue > plotDataAttr.max_x){
                plotDataAttr.max_x = xValue;
            }
            if (yValue < plotDataAttr.min_y){
                plotDataAttr.min_y = yValue;
            }
            if (yValue > plotDataAttr.max_y) {
                plotDataAttr.max_y = yValue;
            }

            xValues.push(xValue);
            yValues.push(yValue);
            labels.push(data[i]["Gene"]);
        }

        // store abs max values
        if(Math.abs(plotDataAttr.min_x)>Math.abs(plotDataAttr.max_x)){
            plotDataAttr.abs_max_x = Math.abs(plotDataAttr.min_x);
        }
        else{
            plotDataAttr.abs_max_x = Math.abs(plotDataAttr.max_x);
        }

        if(Math.abs(plotDataAttr.min_y)>Math.abs(plotDataAttr.max_y)){
            plotDataAttr.abs_max_y = Math.abs(plotDataAttr.min_y);
        }
        else{
            plotDataAttr.abs_max_y = Math.abs(plotDataAttr.max_y);
        }

        var dataTrace = {
            x: xValues,
            y: yValues,
            text: labels
        }
        return dataTrace;
    }

    /**
     * extra layout options: title axis and second y-axis
     */
    function addPlotLayoutOptions(){
        // let xLabel depend on whether we've guessed the data is in log-space.
        var xLabel = self.orTable.assumedLogSpace()?'mean alt. group - mean unalt. group':'log Ratio';
        
        var axisOptions = {
            width: 330,
            height: 330,
            xaxis:{
                title: xLabel
            },
            yaxis:{
                title: '-log10 p-value'
            },
            yaxis2:{
                title: 'significance &#8594;', // &#8594; is code for -->
                titlefont: {
                    color: 'black',
                },
                showgrid: false,
                showticklabels: false,
                zeroline: false,
                showline: false,
                autotick: false,
                ticks: '',
                overlaying: 'y',
                side: 'right',
            }
        }
        
        self.scatterPlot.addPlotLayoutOptions(axisOptions);
    }

    /**
     * for IE compatibility
     * @param x
     * @returns {number}
     */
    Math.log10 = function (x) {
        return Math.log(x) / Math.LN10;
    };

    Math.log2 = function (x) {
        return Math.log(x) / Math.LN2;
    };

    /**
     * creates the scatterplot and adds all the necessary components
     * @param dataTrace: data formatted in the ploty way
     * @param plotElName: element where the plot should be added
     * @param plotDataAttr: contains information about max and min x and y values
     */
    function drawPlot(dataTrace, plotElName, plotDataAttr) {
        var axisMargin=1.25;
        var minX = -1*plotDataAttr.abs_max_x;
        var maxX = plotDataAttr.abs_max_x;
        var pVal = -Math.log10(enrichmentsTabSettings.settings.p_val_threshold);
        var extraTraceText1 = '&#8592; mutual exclusivity';
        var extraTraceText2 = 'co-occurrence  &#8594;';

        // use a different label
        if(self.orTable.requiresLogRatioCalculation()){
            extraTraceText1 = '&#8592; under-expressed';
            extraTraceText2 = 'over-expressed  &#8594;'
        }

        // create the plot and add extra layout options for the axis
        self.scatterPlot = new ScatterPlot(plotElName, dataTrace);
        addPlotLayoutOptions();

        // create pVal trace (the dotted line in the plot) and add it
        var pValTrace = {
            x:[minX*axisMargin, maxX*axisMargin],
            y:[pVal, pVal],
            mode: 'lines',
            hoverinfo: 'none',
            marker:{
                color: 'black'
            },
            line: {
                dash: 'dot',
                width: 1
            }
        };
        self.scatterPlot.addTrace(pValTrace);

        // create mutual exclusivity trace and add it
        // markers are set to invisible as a workaround to improve the text's positioning
        var mutualExclusivityTrace = {
            x:[minX*axisMargin, (minX)*0.75, 0],
            y:[0, 0, 0],
            text: ['', extraTraceText1, ''], // &#8592; is code for <--
            textposition: 'bottom',
            textfont: {
                color: 'rgb(180, 4, 4)',
            },
            mode: 'lines+markers+text',
            type: 'scatter',
            hoverinfo: 'none',
            line: {
                color: 'rgb(180, 4, 4)'
            },
            marker: {
                color: 'rgba(0, 0, 0, 0)'
            }
        };
        self.scatterPlot.addTrace(mutualExclusivityTrace);

        // create co-occurrence trace and add it
        // markers are set to invisible as a workaround to improve the text's positioning
        var coOccurrenceTrace = {
            x:[0, (maxX)*0.75, maxX*axisMargin],
            y:[0, 0, 0],
            text: ['', extraTraceText2, ''], // &#8594; is code for  -->
            textposition: 'bottom',
            textfont: {
                color: 'rgb(59, 124, 59)',
            },
            mode: 'lines+markers+text',
            hoverinfo: 'none',
            line: {
                color: 'rgb(59, 124, 59)'
            },
            marker: {
                color: 'rgba(0, 0, 0, 0)'
            }
        };
        self.scatterPlot.addTrace(coOccurrenceTrace);

        // draw the plot
        self.scatterPlot.drawPlot();

        // add the click and draglisteners with their callbacks
        if(self.orTable.supportsMiniOnco()) {
            self.scatterPlot.addClickListener(handlePlotClickCallback);
        }
        self.scatterPlot.addDragListener(handlePlotDraggedCallback);
    }

    /**
     * show the miniOnce for the clicked gene
     * @param pointClicked: point, which contains the gene as text, which should be shown in the mini-onco
     */
    function handlePlotClickCallback(pointClicked){
        var geneClicked = pointClicked.data.text[pointClicked.pointNumber]
        self.miniOnco.render(geneClicked)
    }

    /**
     * search the table for the dragged genes
     * @param genesDragged: genes to search for in the table
     */
    function handlePlotDraggedCallback(genesDragged){
        self.orTable.searchTable(genesDragged);
    }

    /**
     * This function will give the plot items corresponding to the specialSelectedItems list
     * a special selection color. This is different from the normal selection
     * color.
     *
     * @param specialSelectedItems: list of item IDs of the items that should get a *special*  color
     */
    this.specialSelectItems = function (specialSelectedItems) {
        self.scatterPlot.specialSelectItems(specialSelectedItems);
    }

    /**
     * This function can be used to show a *normal* selection made via an external source,
     * e.g. via a filter in the dataTable coupled to this plot.
     *
     * @param items: list of item IDs of the items that should get the *normal* selection style
     */
    this.selectItems = function(items) {
        self.scatterPlot.showSelection(items, true);
    }

}
