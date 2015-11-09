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
            fill: "#2986e2", 
            stroke: "#2986e2",
            stroke_width: "0",
            size: "30",
            shape: "circle" //default, may vary for different mutation types
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
            xTitleHelp: "Log2 based ratio of (pct in altered / pct in unaltered)",
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
    
    /**
     * Trigger the rendering of the plot.
     * 
     * @param plotEl: el where the plot should be rendered
     * @param model: the model with the parameters for filtering if needed
     * @param dmPresenter: instance of DataManagerPresenter (see DataManagerPresenter in pancancer_study_summary.js)
     * 
     */
	this.render = function(plotElName, model, dmPresenter, data){
		this.model = model;
		
    	//how the code should be if we get the data via presenter layer:
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
			
			var xValue = parseFloat(data[i][3].replace("<","").replace(">","")); //remove < and > from <-10 or >10 scenarios
			var yValue = -Math.log10(data[i][4]);
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
					case_id : xValue + "_" + yValue,
					qtip : "p-value: " + data[i][4] + ", log ratio: " + data[i][3],
			};
			plotData.push(_scatterPlotItem);
		}
		drawPlot(plotData, null, plotElName, plotDataAttr);
		
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
		var scatterPlot = new ScatterPlots();
		scatterPlotOptions.names.body = plotElName;
        scatterPlot.init(scatterPlotOptions, plotData, plotDataAttr, brushOn, false);            
        //scatterPlot.jointBrushCallback(scatterPlotBrushCallBack);
        //scatterPlot.jointClickCallback(scatterPlotClickCallBack);
        $("#study-view-scatter-plot-header").css('display', 'none');   
    }
}