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
		
    	//get data via presenter layer:
		//this.plotPresenter = new PlotPresenter(model, dmPresenter, geneId);
		//Get data:
	    //this.plotPresenter.getDataForPlot(function (plotData) {
			//var plotData = null;
	    	//draw plot
			//var plotEl = $("#" + plotElName)[0];
	    	//drawPlot(plotData, model, plotEl);
	    //});
		var scatterPlotDataAttr = {
			    min_x: 0,
			    max_x: 0,
			    min_y: 0,
			    max_y: 0
			};

		
		var scatterPlotArr = [];
		for (var i = 0; i < data.length; i++){
			
			var xValue = parseFloat(data[i][3].replace("<","").replace(">","")); //remove < and > from <-10 or >10 scenarios
			var yValue = -Math.log10(data[i][4]);
			if (xValue < scatterPlotDataAttr.min_x)
				scatterPlotDataAttr.min_x = xValue;
			if (xValue > scatterPlotDataAttr.max_x)
				scatterPlotDataAttr.max_x = xValue;
			if (yValue < scatterPlotDataAttr.min_y)
				scatterPlotDataAttr.min_y = yValue;
			if (yValue > scatterPlotDataAttr.max_y)
				scatterPlotDataAttr.max_y = yValue;
			
			var _scatterPlotItem = {
					x_val : xValue,
					y_val : yValue,
					case_id : xValue + "_" + yValue,
					qtip : "p-value: " + data[i][4] + ", log ratio: " + data[i][3],
			};
	        scatterPlotArr.push(_scatterPlotItem);
		}
		
		var brushOn = true;
		var scatterPlot = new ScatterPlots();
		scatterPlotOptions.names.body = plotElName;
        scatterPlot.init(scatterPlotOptions, scatterPlotArr, scatterPlotDataAttr, brushOn, false);            
        //scatterPlot.jointBrushCallback(scatterPlotBrushCallBack);
        //scatterPlot.jointClickCallback(scatterPlotClickCallBack);
        $("#study-view-scatter-plot-header").css('display', 'none');
		
	}
	    
	/**
	 * Main function using D3js methods to render the histogram.
	 * 
	 * @param plotData: the plot data formated by PlotPresenter
	 * @param model: the model with the parameters that can influence part of the look/feel
	 * @param plotEl: el where the plot should be rendered
	 */
    var drawPlot = function(plotData, model, plotEl) {
		
    	// data that you want to plot, I've used separate arrays for x and y values
    	var xdata = [5, 10, 15, 20],
    	    ydata = [3, 17, 4, 6];

    	// size and margins for the chart
    	var margin = {top: 20, right: 15, bottom: 60, left: 60}
    	  , width = 200 - margin.left - margin.right
    	  , height = 200 - margin.top - margin.bottom;

    	// x and y scales, I've used linear here but there are other options
    	// the scales translate data values to pixel values for you
    	var x = d3.scale.linear()
    	          .domain([0, d3.max(xdata)])  // the range of the values to plot
    	          .range([ 0, width ]);        // the pixel range of the x-axis

    	var y = d3.scale.linear()
    	          .domain([0, d3.max(ydata)])
    	          .range([ height, 0 ]);

    	// the chart object, includes all margins
    	var chart = d3.select(plotEl)
    	.append('svg:svg')
    	.attr('width', width + margin.right + margin.left)
    	.attr('height', height + margin.top + margin.bottom)
    	.attr('class', 'chart')

    	// the main object where the chart and axis will be drawn
    	var main = chart.append('g')
    	.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
    	.attr('width', width)
    	.attr('height', height)
    	.attr('class', 'main')   

    	// draw the x axis
    	var xAxis = d3.svg.axis()
    	.scale(x)
    	.orient('bottom');

    	main.append('g')
    	.attr('transform', 'translate(0,' + height + ')')
    	.attr('class', 'main axis date')
    	.call(xAxis);

    	// draw the y axis
    	var yAxis = d3.svg.axis()
    	.scale(y)
    	.orient('left');

    	main.append('g')
    	.attr('transform', 'translate(0,0)')
    	.attr('class', 'main axis date')
    	.call(yAxis);

    	// draw the graph object
    	var g = main.append("svg:g"); 

    	g.selectAll("scatter-dots")
    	  .data(ydata)  // using the values in the ydata array
    	  .enter().append("svg:circle")  // create a new circle for each value
    	      .attr("cy", function (d) { return y(d); } ) // translate y value to a pixel
    	      .attr("cx", function (d,i) { return x(xdata[i]); } ) // translate x value
    	      .attr("r", 10) // radius of circle
    	      .style("opacity", 0.6); // opacity of circle
	    
	    
    }
}