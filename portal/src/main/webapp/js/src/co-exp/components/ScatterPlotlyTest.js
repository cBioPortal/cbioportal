
var ScatterPlotlyTest = function(divName, plotData) {
    var self=this;
    var prevSpecialSelection=[], prevSelection=[];
    var traces=[];
    var dataTrace;

    // layout options for the plot
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

    // add / overwrite some layout options if desired
    this.addPlotLayoutOptions = function(newOptions){
        $.extend(true, defaultPlotLayout, newOptions);
    }

    // add a trace with datapoints
    this.addTrace = function(trace) {
        // create full trace by extending defaultTraceLayout with the trace
        var fullTrace = $.extend({}, defaultTraceLayout, trace);
        traces.push(fullTrace);
    }

    // draw the plot and add hover effect
    this.drawPlot = function (){
        Plotly.newPlot(divName, traces, defaultPlotLayout);
        addHover();
    }

    // workaround until there's an easy way to determine which points are visible
    function findDraggedGenes(eventData){
        // when you drag over the edge of the plot, plotly returns the range for side as undefined
        // in such as case, set it to infinity
        var xLeft = eventData['xaxis.range[0]']===undefined?-Infinity:eventData['xaxis.range[0]'];
        var xRight = eventData['xaxis.range[1]']===undefined?Infinity:eventData['xaxis.range[1]'];
        var yBottom = eventData['yaxis.range[0]']===undefined?-Infinity:eventData['yaxis.range[0]'];
        var yTop = eventData['yaxis.range[1]']===undefined?Infinity:eventData['yaxis.range[1]'];

        var draggedGenes=[];
        var x_val, y_val;

        // loop over the array, retrieve the x and y of the points and check which points are inside the selected range
        for(var i=0; i<dataTrace.x.length; i++){
            x_val = dataTrace.x[i];
            y_val = dataTrace.y[i];
            if(x_val>xLeft && x_val<xRight && y_val>yBottom && y_val<yTop){
                draggedGenes.push(dataTrace.text[i]);
            }
        }
        return draggedGenes;
    }

    // add hover functionality which increases the size of a figure by using the stroke-width
    // a feature request for plotly has been posted to allow for easier updating of e.g. size, which would now
    // require a plotly_restyle with a complete array, as currently it's only possible to restyle complete traces
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

    // find the point element based on the hovered data's text
    function getHoveredElement(data){
        var geneHovered = data.points[0].data.text[data.points[0].pointNumber];
        var allPathElements = d3.select("#"+divName).selectAll(".point");
        var curElement = allPathElements.filter(function(d) {
            return d.tx==geneHovered;
        });
        return curElement;
    }

    // add click functionality
    this.addClickListener = function (callback){
        var myPlot = document.getElementById(divName);
        // closest point
        myPlot.on('plotly_click', function(data){
            callback(data.points[0]);
        });
    }

    // add drag functionality
    this.addDragListener = function(callback){
        // plotly currently doesn't expose many events, so for the drag functionality
        // we need to use the plotly_relayout
        // this also fires when someone double-clicks (zoom-out)
        $('#'+divName).on('plotly_relayout', function(event,eventdata){

            // when zooming in find genes
            if(!eventdata['xaxis.autorange'] || !eventdata['yaxis.autorange']) {
                //var draggedGenes = findDraggedGenes(eventdata, plotData);
                var draggedGenes = findDraggedGenes(eventdata);
                // do something with the genes found
                callback(draggedGenes);
            }
            // when zooming out skip finding genes
            else{
                callback([]);
            }
        });
    }

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


    // colour the selection in lightgrey and restore to the marker.color
    // optionally provide inverse to color the not-selected to lightgrey
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

    // sets case_ids to the specialClickColor
    this.specialSelectItems = function(case_ids) {
        // determine which case identifiers should be coloured and which should have their colour restored
        var addSpecialSelect = _.difference(case_ids, prevSpecialSelection);
        var removeSpecialSelect = _.difference(prevSpecialSelection, case_ids);

        colorDots(addSpecialSelect, removeSpecialSelect, dataTrace.specialClickColor, dataTrace.marker.color);
        // keep track of the selection
        prevSpecialSelection = case_ids.slice();
    }

    // on init add the plotData
    function init(){
        self.addTrace(plotData);
        dataTrace = traces[0];
    }

    init();
 }
