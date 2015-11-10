var scatterPlotOptions = {
        canvas: {  //position of components
            width: 400,
            height: 400,
            xLeft: 80,     //The left/starting point for x axis
            xRight: 380,   //The right/ending point for x axis
            yTop: 10,      //The top/ending point for y axis
            yBottom: 320   //The bottom/starting point for y axis
        },
        style: { //Default style setting
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
            div: "study-view-scatter-plot",
            header: "study-view-scatter-plot-header",
            body: "",
            loading_img: "study-view-scatter-plot-loading-img",
            control_panel: "study-view-scatter-plot-control-panel",
            log_scale_x: "study-view-scatter-plot-log-scale-x",
            log_scale_y: "study-view-scatter-plot-log-scale-y",
            download_pdf: "study-view-scatter-plot-pdf",
            download_svg: "study-view-scatter-plot-svg"         
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
        text: {
            xTitle: "log Ratio",
            yTitle: "-log10 p-value",
            title: "Volcano plot of log Ratio vs -log10 p-value",
            fileName: "",
            xTitleHelp: "log2 based ratio of (pct in altered / pct in unaltered)",
            yTitleHelp: "-log10 p-value, derived from Fisher Exact Test"
        },
        legends: []
    };


/**
 * Class to render the d3js volcano plot.
 */
function VolcanoPlot()
{
	
	// Some semi-global utilities
    // Here are some options that we will use in this view
    var width = 200;
    var height = 200;
    var paddingLeft = 10;
    var paddingRight = 10;
    var paddingTop = 10;
    var histBottom = 400;
    var fontFamily = "sans-serif";
    var animationDuration = 1000;
    var maxStudyBarWidth = 30;
    
    var self = this;
    self.scatterPlot = null; 
    
    /**
     * Trigger the rendering of the plot.
     * 
     * @param plotEl: el where the plot should be rendered
     * @param model: the model with the parameters for filtering if needed
     * @param dmPresenter: instance of DataManagerPresenter (see DataManagerPresenter in pancancer_study_summary.js)
     * 
     */
	this.render = function(plotElName, model, dmPresenter, data, dataTable){
		
		this.model = model;
		this.dataTable = dataTable;
    	//how the code could be if we get the data via presenter layer:
		//this.plotPresenter = new PlotPresenter(model, dmPresenter, geneId);
		//Get data:
	    //this.plotPresenter.getDataForPlot(function (plotData) {
			//var plotData = null;
	    	//draw plot
	    	//drawPlot(plotData, model, plotElName);
	    //});
		var plotDataAttr = {
			    min_x: 0,
			    max_x: 0,
			    min_y: 0,
			    max_y: 0
			};

		
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
					case_id : data[i]["Gene"], // this ID is passed below to scatterPlotBrushCallBack on brushended
					qtip : "p-value: " + data[i]["p-Value"] + ", log ratio: " + data[i]["Log Ratio"],
			};
			plotData.push(_scatterPlotItem);
		}
		drawPlot(plotData, null, plotElName, plotDataAttr);
		
	}
	
	this.specialSelectItems = function (selectedGenes, totalList) {
		self.scatterPlot.specialSelectItems(selectedGenes, totalList);
	}
	
	this.showRemainingItems = function(remainingGenes) {
		self.scatterPlot.showRemainingItems(remainingGenes);
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
		self.scatterPlot = new ScatterPlots();
		scatterPlotOptions.names.body = plotElName;
		self.scatterPlot.init(scatterPlotOptions, plotData, plotDataAttr, brushOn, false);            
		self.scatterPlot.jointBrushCallback(scatterPlotBrushCallBack);
        //scatterPlot.jointClickCallback(scatterPlotBrushCallBack);  //Option, but jointClickCallback not yet implemented (also not really needed yet). Would have to port some code from study-view/component/ScatterPlot.js
        $("#study-view-scatter-plot-header").css('display', 'none');   
    }
    
    
    var scatterPlotBrushCallBack = function(brushedCaseIds) {
    	//The ^ and $ pattern is to avoid scenarios where we have genes with similar names such as A1, A11 being matched by A1. 
    	//Only first one (A1) should be matched in this case.
    	var searchExpression = "^" + brushedCaseIds.join("$|^") + "$";
    	
    	//nb: one option could be to show all data if brushedCaseIds.length is 0, i.e. special case where user clicks on empty region of plot.
    	//    But for this, the brushended() function of ScatterPlots also needs slight adjustment in the selection_mode: "fade_unselected" scenario.
    	
    	self.dataTable.DataTable().column( 0 ).search(
    			searchExpression,
    	        true,
    	        true
    	    ).draw();
    }
    
}