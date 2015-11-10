/**
 * Class to render the d3js volcano plot, coupling it to a dataTable 
 * such that the dataTable displays the selections made in the plot 
 * and vice-versa.
 */
function VolcanoPlot() {	

	var self = this;
    self.scatterPlot = null; 

    var scatterPlotOptions = {
	    canvas: {  //size and relative position of plot
	        width: 400,
	        height: 400,
	        xLeft: 80,     //The left/starting point for x axis
	        xRight: 380,   //The right/ending point for x axis
	        yTop: 10,      //The top/ending point for y axis
	        yBottom: 320   //The bottom/starting point for y axis
	    },
	    style: { //style settings
	        fill: "#58ACFA", //light blue
	        stroke: "#0174DF", //dark blue
	        stroke_width: "1.2",
	        size: "20",
	        shape: "circle", //default, may vary for different mutation types
	        opacity: 0.4,
	        selection_mode: "fade_unselected",  //if not set, selection will be shown by placing red border on items
	        special_select_color: "lime" //needed when calling scatterPlot.specialSelectItems (as is the case here)
	    },
	    names: { 
	        body: "", //set on the fly via this.render() to plotElName parameter value
	    },
	    elem: {
	        svg: "",
	        xScale: "",
	        yScale: "",
	        xAxis: "",
	        yAxis: "",
	        dotsGroup: "",
	        axisGroup: "",
	        axisTitleGroup: "",
	        brush: ""
	    },
	    text: { //axis labels:
	        xTitle: "log Ratio",
	        yTitle: "-log10 p-value",
	        xTitleHelp: "log2 based ratio of (pct in altered / pct in unaltered)",
	        yTitleHelp: "-log10 p-value, derived from Fisher Exact Test"
	    },
	    legends: []
	};    
    
    
    /**
     * Trigger the rendering of the plot.
     * 
     * @param plotElName: el where the plot should be rendered
     * @param data: data from dataTable, but in original form (json)
     * @param dataTable: dataTable item to be updated when selections are made in plot
     */
	this.render = function(plotElName, data, dataTable){
		
		this.model = model;
		this.dataTable = dataTable;
		var plotDataAttr = {
			    min_x: 0,
			    max_x: 0,
			    min_y: 0,
			    max_y: 0
			};

		//build up plots x,y coordinates list based on data columns "Log Ratio" and "p-Value" received: 
		//TODO this could be made generic to support also other data with different column names (perhaps add a presenter layer that makes the translation of the specific column names to the generic ones to be supported here)
		var plotData = [];
		for (var i = 0; i < data.length; i++){
			
			var xValue = parseFloat(data[i]["Log Ratio"].replace("<","").replace(">","")); //remove < and > from <-10 or >10 scenarios
			var yValue = -Math.log10(data[i]["p-Value"]);
			if (xValue < plotDataAttr.min_x)
				plotDataAttr.min_x = xValue;
			if (xValue > plotDataAttr.max_x)
				plotDataAttr.max_x = xValue;
			if (yValue < plotDataAttr.min_y)
				plotDataAttr.min_y = yValue;
			if (yValue > plotDataAttr.max_y)
				plotDataAttr.max_y = yValue;
			
			var _scatterPlotItem = {
					x_val : xValue,
					y_val : yValue,
					case_id : data[i]["Gene"], // case_id is the "item ID". This item ID is passed below to scatterPlotBrushCallBack on brushended
					qtip : "p-value: " + cbio.util.toPrecision(parseFloat(data[i]["p-Value"]), 3, 0.01) + 
						   ", log ratio: " + parseFloat(data[i]["Log Ratio"]).toFixed(2),
			};
			//add to list:
			plotData.push(_scatterPlotItem);
		}
		//draw the plot with the prepared x,y coordinates data:
		drawPlot(plotData, null, plotElName, plotDataAttr);
		
	}
	
	/**
     * This function will give the plot items corresponding to the specialSelectedItems list 
     * a special selection color. This is different from the normal selection
     * color. 
     * 
     * @param specialSelectedItems: list of item IDs of the items that should get a *special*  color
     * @param totalList: total list of item IDs that are part of the current *normal* selection
     */
	this.specialSelectItems = function (specialSelectedItems, totalList) {
		self.scatterPlot.specialSelectItems(specialSelectedItems, totalList);
	}
	
	/**
     * This function can be used to show a *normal* selection made via an external source,
     * e.g. via a filter in the dataTable coupled to this plot. 
     * 
     * @param items: list of item IDs of the items that should get the *normal* selection style
     */
	this.selectItems = function(items) {
		self.scatterPlot.showSelection(items);
	}
	
	    
	/**
	 * Main function using D3js methods to render the histogram.
	 * 
	 * @param plotData: the plot data formated by PlotPresenter
	 * @param model: the model with the parameters that can influence part of the look/feel
	 * @param plotEl: el where the plot should be rendered
	 * @param plotDataAttr: object containing info on data size (x and y min/max)
	 */
    var drawPlot = function(plotData, model, plotElName, plotDataAttr) {
		
    	var brushOn = true;
    	var drawCoExpInfo = false; //we don't want this...i.e. is not a co-expression plot in this case
    	//reuse the ScatterPlots object from co-exp/components/ScatterPlots.js:
		self.scatterPlot = new ScatterPlots();
		scatterPlotOptions.names.body = plotElName;
		//initialize with style options and the data to plot: 
		self.scatterPlot.init(scatterPlotOptions, plotData, plotDataAttr, brushOn, drawCoExpInfo);            
		self.scatterPlot.jointBrushCallback(scatterPlotBrushCallBack);
        //scatterPlot.jointClickCallback(scatterPlotBrushCallBack);  //Option, but jointClickCallback not yet implemented (also not really needed yet). Would have to port some code from study-view/component/ScatterPlot.js
    }
    
    /**
     * Callback function for brushended event. This function will ensure the dataTable in this.dataTable 
     * is updated according to the items selected (brushedItemIds). 
     * 
     * @param brushedItemIds: the item IDs selected by the brush action. 
     */
    var scatterPlotBrushCallBack = function(brushedItemIds) {
    	//The ^ and $ pattern is to avoid scenarios where we have genes with similar names such as A1, A11 being matched by A1. 
    	//Only first one (A1) should be matched in this case.
    	var searchExpression = "^" + brushedItemIds.join("$|^") + "$";
    	
    	//nb: one option could devise an expression to show all data if brushedItemIds.length is 0, i.e. the special case where user clicks on empty region of plot.
    	//    But for this, the brushended() function of ScatterPlots also needs slight adjustment in the selection_mode: "fade_unselected" scenario.
    	
    	//apply search expression to the dataTable: 
    	self.dataTable.DataTable().column( 0 ).search(
    			searchExpression,
    	        true,
    	        true
    	    ).draw();
    }
    
}