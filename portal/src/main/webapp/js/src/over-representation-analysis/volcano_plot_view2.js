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
     * @param plotElName: el where the plot should be rendered
     * @param data: data from dataTable, but in original form (json)
     * @param dataTable: dataTable item to be updated when selections are made in plot
     * @param miniOnco: miniOnco item to be updated when plot item is clicked
     */

    this.render = function(orTable){
        self.orTable = orTable;
        self.miniOnco = orTable.miniOnco;

        var plotDataAttr = {
            min_x: 0,
            max_x: 0,
            min_y: 0,
            max_y: 0,
            abs_max_x: 0,
            abs_max_y: 0
        };

        // prepare the data
        var dataTrace = createDataTrace(orTable.originalData, plotDataAttr)

        //draw the plot with the prepared x,y coordinates data:
        drawPlot(dataTrace, null, orTable.plot_div, plotDataAttr);
    }

    // create data trace based on data and fill plotDataAttr with min and max values
    function createDataTrace(data, plotDataAttr){
        var xValues=[], yValues=[], labels=[];

        //build up plots x,y coordinates list based on data columns "Log Ratio" and "p-Value" received:
        //TODO this could be made generic to support also other data with different column names (perhaps add a presenter layer that makes the translation of the specific column names to the generic ones to be supported here)
        for (var i = 0; i < data.length; i++){
            var xValue = parseFloat(data[i]["Log Ratio"].replace("<","").replace(">","")); //remove < and > from <-10 or >10 scenarios
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

    function addPlotLayoutOptions(){
        var axisOptions = {
            xaxis:{
                title: 'log Ratio'
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

    // for IE compatibility
    Math.log10 = function (x) {
        return Math.log(x) / Math.LN10;
    };

    function drawPlot(dataTrace, model, plotElName, plotDataOptions) {

        var axisMargin=1.25;
        var minX = -1*plotDataOptions.abs_max_x;
        var maxX = plotDataOptions.abs_max_x;
        var pVal = -Math.log10(orAnalysis.settings.p_val_threshold);

        // create the plot and add extra layout options for the axis
        self.scatterPlot = new ScatterPlotlyTest(plotElName, dataTrace);
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
            text: ['', '&#8592; mutual exclusivity', ''], // &#8592; is code for <--
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

        // create co-occurence trace and add it
        // markers are set to invisible as a workaround to improve the text's positioning
        var coOccurrenceTrace = {
            x:[0, (maxX)*0.75, maxX*axisMargin],
            y:[0, 0, 0],
            text: ['', 'co-occurrence  &#8594;', ''], // &#8594; is code for  -->
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
        self.scatterPlot.addClickListener(handlePlotClickCallback);
        self.scatterPlot.addDragListener(handlePlotDraggedCallback);
    }

    // show the miniOnce for the clicked gene
    function handlePlotClickCallback(pointClicked){
        var geneClicked = pointClicked.data.text[pointClicked.pointNumber]
        self.miniOnco.render(geneClicked)
    }

    // search the table for the dragged genes
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
