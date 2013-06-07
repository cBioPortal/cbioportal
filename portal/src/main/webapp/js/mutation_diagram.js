/**
 * Constructor for MutationDiagram class.
 *
 * @param options   general options object
 * @param data      mutation data
 * @constructor
 */
function MutationDiagram(options, data)
{
	// TODO after refactoring the data will be generic,
	// so we will need to process it before(or after?) passing to this function

	var self = this;

	// merge options with default options to use defaults for missing values
	options = jQuery.extend({}, self.defaultOpts, options);

	var container = d3.select("#" + options.el);
	var svg = self.createSvg(container,
			options.elWidth,
			options.elHeight);

	// calculate bounds of the actual plot area (excluding axis, sequence, labels, etc.)
	var bounds = {};
	bounds.width = options.elWidth - (options.marginLeft + options.marginRight);
	bounds.height = options.elHeight - (options.marginBottom + options.marginTop);
	bounds.x = options.marginLeft;
	bounds.y = options.elHeight - options.marginBottom;

	self.drawDiagram(svg,
		bounds,
		options,
		data);
}

// TODO add more options for a more customizable diagram:
// default tooltip templates?
// label positions and alignments
// max # of lollipop labels to display
// TODO use percent values instead of pixel values for some components?
MutationDiagram.prototype.defaultOpts = {
	el: "mutation_diagram_d3",  // id of the container
	elWidth: 720,               // width of the container
	elHeight: 180,              // height of the container
	marginLeft: 40,             // left margin for the plot area
	marginRight: 20,            // right margin for the plot area
	marginTop: 20,              // top margin for the plot area
	marginBottom: 60,           // bottom margin for the plot area
	labelX: "TODO",             // informative label of the x-axis
	labelXFont: "sans-serif",   // font type of the x-axis label
	labelXFontColor: "#2E3436", // font color of the x-axis label
	labelXFontSize: "12px",     // font size of x-axis label
	labelY: "# Mutations",      // informative label of the y-axis
	labelYFont: "sans-serif",   // font type of the y-axis label
	labelYFontColor: "#2E3436", // font color of the y-axis label
	labelYFontSize: "12px",     // font size of y-axis label
	offsetY: 3,                 // offset for y values
	offsetX: 0,                 // offset for x values
	seqFillColor: "#BABDB6",    // color of the sequence rectangle
	seqHeight: 16,              // height of the sequence rectangle
	seqPadding: 8,              // padding between sequence and plot area
	regionHeight: 24,           // height of a region (drawn on the sequence)
	regionFont: "sans-serif",   // font of the region text
	regionFontColor: "#FFFFFF", // font color of the region text
	regionFontSize: "12px",     // font size of the region text
	regionTextPadding: 5,       // TODO remove this, add alignment
	lollipopFillColor: "#B40000", // TODO more than one color wrt mutation type?
	lollipopRadius: 3,          // radius of the lollipop circles
	lollipopStrokeWidth: 1,     // width of the lollipop lines
	lollipopStrokeColor: "#BABDB6", // color of the lollipop line
	xAxisPadding: 15,           // padding between x-axis and the sequence
	xAxisTickInterval: 100,     // major tick interval for x-axis
	xAxisMaxTickLabel: 5,       // maximum number of visible tick label on the x-axis
	xAxisTickSize: 6,           // size of the major ticks of x-axis
	xAxisStroke: "#AAAAAA",     // color of the x-axis lines
	xAxisFont: "sans-serif",    // font type of the x-axis labels
	xAxisFontSize: "10px",      // font size of the x-axis labels
	xAxisFontColor: "#2E3436",  // font color of the x-axis labels
	yAxisPadding: 5,            // padding between y-axis and the plot area
	yAxisLabelPadding: 15,      // padding between y-axis and its label
	yAxisTicks: 3,              // number of major ticks to be displayed on the y-axis
	yAxisTickSize: 6,           // size of the major ticks of y-axis
	yAxisStroke: "#AAAAAA",     // color of the y-axis lines
	yAxisFont: "sans-serif",    // font type of the y-axis labels
	yAxisFontSize: "10px",      // font size of the y-axis labels
	yAxisFontColor: "#2E3436"   // font color of the y-axis labels
};

/**
 * Draws the mutation diagram.
 *
 * @param svg       svg container for the diagram
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param options   options object
 * @param data      data to visualize
 */
MutationDiagram.prototype.drawDiagram = function (svg, bounds, options, data)
{
	var self = this;

	// TODO data structure should change completely
	var xMax = data[0].length + options.offsetX;
	var yMax = self.calcMaxCount(data) + options.offsetY;

	var xScale = d3.scale.linear()
		.domain([0, xMax])
		.range([bounds.x, bounds.x + bounds.width]);

	var yScale = d3.scale.linear()
		.domain([0, yMax])
		.range([bounds.y, bounds.y - bounds.height]);

	// draw x-axis
	self.drawXAxis(svg, xScale, xMax, options, bounds);

	// draw y-axis
	self.drawYAxis(svg, yScale, yMax, options, bounds);
	self.drawYAxisLabel(svg, options, bounds);

	// draw lollipop lines
	for (var i = 0; i < data[0].markups.length; i++)
	{
		var mutation = data[0].markups[i];

		// TODO this check should be unnecessary after changing data structure
		if (mutation.type === "mutation")
		{
			self.drawLollipopLine(svg, mutation, options, bounds, xScale, yScale);
		}
	}

	// draw lollipop circles
	for (var i = 0; i < data[0].markups.length; i++)
	{
		var mutation = data[0].markups[i];

		// TODO this check should be unnecessary after changing data structure
		if (mutation.type === "mutation")
		{
			self.drawLollipopCircle(svg, mutation, options, xScale, yScale);
		}
	}

	// draw sequence
	var sequence = self.drawSequence(svg, options, bounds);

	// draw regions
	for (var i = 0, size = data[0].regions.length; i < size; i++)
	{
		self.drawRegion(svg, data[0].regions[i], options, bounds, xScale);
	}
};

/**
 * Creates the main svg (graphical) component.
 *
 * @param container main container (div, etc.)
 * @param width     width of the svg area
 * @param height    height of the svg area
 * @return
 */
MutationDiagram.prototype.createSvg = function (container, width, height)
{
	var svg = container.append("svg");

	svg.attr('width', width);
	svg.attr('height', height);

	return svg;
};

/**
 * Draws the x-axis on the bottom side of the plot area.
 *
 * @param svg       svg to append the axis
 * @param xScale    scale function for the y-axis
 * @param xMax      max y value for the axis
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return          svg group containing all the axis components
 */
MutationDiagram.prototype.drawXAxis = function(svg, xScale, xMax, options, bounds)
{
	var self = this;

	// helper function to calculate display interval for tick labels
	var calcDisplayInterval = function(interval, maxValue, maxLabelCount) {
		var displayInterval = maxValue / (maxLabelCount - 1);

		if (displayInterval % interval > interval / 2)
		{
			displayInterval += + interval - (displayInterval % interval);
		}
		else
		{
			displayInterval -= (displayInterval % interval);
		}

		if (displayInterval < interval)
		{
			displayInterval = interval;
		}

		return displayInterval;
	};

	// determine tick values
	var tickValues = [];

	var value = 0;
	var interval = options.xAxisTickInterval;

	while (value < xMax - interval / 2)
	{
		tickValues.push(value);
		value += interval;
	}

	tickValues.push(xMax);

	// formatter to hide labels
	var formatter = function(value) {
		var displayInterval = calcDisplayInterval(interval,
			xMax,
			options.xAxisMaxTickLabel);

		// always display max value
		if (value == xMax)
		{
			return value + " aa";
		}
		// display value if its multiple of display interval
		else if (value % displayInterval == 0)
		{
			return value;
		}
		// hide remaining labels
		else
		{
			return '';
		}
	};

	var tickSize = options.xAxisTickSize;

	var xAxis = d3.svg.axis()
		.scale(xScale)
		.orient("bottom")
		.tickValues(tickValues)
		.tickFormat(formatter)
		.tickSubdivide(true)
		.tickSize(tickSize, tickSize/2, 0);

	// calculate y-coordinate of the axis
	var position = bounds.y + options.regionHeight + options.xAxisPadding;

	// append axis
	var axis = svg.append("g")
		.attr("class", "mut-dia-x-axis")
		.attr("transform", "translate(0," + position + ")")
		.call(xAxis);

	// format axis
	self.formatAxis(".mut-dia-x-axis",
		options.xAxisStroke,
		options.xAxisFont,
		options.xAxisFontSize,
		options.xAxisFontColor);

	return axis;
};

/**
 * Draws the y-axis on the left side of the plot area.
 *
 * @param svg       svg to append the axis
 * @param yScale    scale function for the y-axis
 * @param yMax      max y value for the axis
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return          svg group containing all the axis components
 */
MutationDiagram.prototype.drawYAxis = function(svg, yScale, yMax, options, bounds)
{
	var self = this;

	// determine tick values
	var tickValues = [];

	var value = 0;
	var interval = yMax / (options.yAxisTicks - 1);

	while (value < yMax)
	{
		tickValues.push(value);
		value += interval;
	}

	tickValues.push(yMax);

	// formatter to hide all except first and last
	var formatter = function(value) {
		if (value == yMax || value == 0)
		{
			return value;
		}
		else
		{
			return '';
		}
	};

	var tickSize = options.yAxisTickSize;

	var yAxis = d3.svg.axis()
		.scale(yScale)
		.orient("left")
		.tickValues(tickValues)
		.tickFormat(formatter)
		.tickSubdivide(true)
		.tickSize(tickSize, tickSize/2, 0);

	// calculate y-coordinate of the axis
	var position = bounds.x - options.yAxisPadding;

	// append axis
	var axis = svg.append("g")
		.attr("class", "mut-dia-y-axis")
		.attr("transform", "translate(" + position + ",0)")
		.call(yAxis);

	// format axis
	self.formatAxis(".mut-dia-y-axis",
		options.yAxisStroke,
		options.yAxisFont,
		options.yAxisFontSize,
		options.yAxisFontColor);

	return axis;
};

MutationDiagram.prototype.drawYAxisLabel = function(svg, options, bounds)
{
	// calculate y-coordinate of the axis
	var x = bounds.x -
		options.yAxisPadding -
		options.yAxisTickSize -
		options.yAxisLabelPadding;

	var y =  bounds.y;

	// TODO align center...
	// append label
	var label = svg.append("text")
		.attr("fill", options.labelYFontColor)
		.attr("x", x)
		.attr("y", y)
		.attr("class", "mut-dia-y-axis-label")
		.attr("transform", "rotate(270, " + x + "," + y +")")
		.style("font-family", options.labelYFont)
		.style("font-size", options.labelYFontSize)
		.text(options.labelY);
};

/**
 * Formats the style of the plot axis defined by the given selector.
 *
 * @param axisSelector  selector for the axis components
 * @param stroke        line color of the axis
 * @param font          font type of the axis value labels
 * @param fontSize      font size of the axis value labels
 * @param fontColor     font color of the axis value labels
 */
MutationDiagram.prototype.formatAxis = function(axisSelector, stroke, font, fontSize, fontColor)
{
	var selector = d3.selectAll(axisSelector + ' line');

	selector.style("fill", "none")
		.style("stroke", stroke)
		.style("shape-rendering", "crispEdges");

	selector = d3.selectAll(axisSelector + ' path');

	selector.style("fill", "none")
		.style("stroke", stroke)
		.style("shape-rendering", "crispEdges");

	selector = d3.selectAll(axisSelector + ' text');

	selector.attr("fill", fontColor)
		.style("font-family", font)
		.style("font-size", fontSize);
};

/**
 * Draws the lollipop circle on the plot area.
 *
 * @param svg       svg to append the circle
 * @param mutation  mutation data
 * @param options   general options object
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 * @return          lollipop circle (svg element)
 */
MutationDiagram.prototype.drawLollipopCircle = function (svg, mutation, options, xScale, yScale)
{
	var self = this;

	var x = xScale(mutation.start);
	var y = yScale(mutation.metadata.count);

	return svg.append('circle')
		.attr('cx', x)
		.attr('cy', y)
		.attr('r', options.lollipopRadius)
		.attr('fill', options.lollipopFillColor);
};

/**
 * Draws a lollipop line (from sequence to the lollipop circle)
 *
 * @param svg       svg to append the line
 * @param mutation  mutation data
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 * @return          lollipop line (svg element)
 */
MutationDiagram.prototype.drawLollipopLine = function (svg, mutation, options, bounds, xScale, yScale)
{
	var self = this;

	var x = xScale(mutation.start);
	var y = yScale(mutation.metadata.count);

	return svg.append('line')
		.attr('x1', x)
		.attr('y1', y)
		.attr('x2', x)
		.attr('y2', self.calcSequenceBounds(bounds, options).y)
		.attr('stroke', options.lollipopStrokeColor)
		.attr('stroke-width', options.lollipopStrokeWidth);
};

/**
 * Draws the given region on the sequence.
 *
 * @param svg       target svg to append region rectangle
 * @param region    region data
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @return          region rectangle (svg element)
 */
MutationDiagram.prototype.drawRegion = function(svg, region, options, bounds, xScale)
{
	var width = Math.abs(xScale(region.start) - xScale(region.end));
	var height = options.regionHeight;
	var y = bounds.y + options.seqPadding;
	var x = xScale(region.start);

	var label = region.text;

	// TODO this data structure should change...
	var regionMetadata = region.metadata;
	var tooltip = regionMetadata.identifier + " " +
	                  regionMetadata.type.toLowerCase() + ", " +
	                  regionMetadata.description +
	                  " (" + region.start + " - " +
	                  region.end + ")";

	var rect = svg.append('rect')
		.attr('fill', region.colour) // TODO change to color?
		.attr('x', x)
		.attr('y', y)
		.attr('width', width)
		.attr('height', height);

	// TODO text alignment (center, left, right) & truncate/hide if too long to fit
	var text = svg.append('text')
		.attr("fill", options.regionFontColor)
		.attr("x", x + options.regionTextPadding)
		.attr("y", y + 2*height/3)
		.attr("class", "mut-dia-region-text")
		.style("font-size", options.regionFontSize)
		.style("font-family", options.regionFont)
		.text(label);

	return rect;
};

/**
 * Draws the sequence just below the plot area.
 *
 * @param svg       target svg to append sequence rectangle
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return          sequence rectangle (svg element)
 */
MutationDiagram.prototype.drawSequence = function(svg, options, bounds)
{
	var seqBounds = this.calcSequenceBounds(bounds, options);

	return svg.append('rect')
		.attr('fill', options.seqFillColor)
		.attr('x', seqBounds.x)
		.attr('y', seqBounds.y)
		.attr('width', seqBounds.width)
		.attr('height', seqBounds.height);
};

/**
 * Returns the number of mutations at the hottest spot.
 *
 * @param data  mutation data
 * @return      number of mutations at the hottest spot
 */
MutationDiagram.prototype.calcMaxCount = function(data)
{
	var maxCount = 0;
	var i = 0;
	var size = 0;

	// TODO data structure should change, this is the old method to find max count
	for (i = 0, size = data[0].markups.length; i < size; i++)
	{
		if ((data[0].markups[i].type == "mutation") &&
		    (parseInt(data[0].markups[i].metadata.count) >= maxCount))
		{
			maxCount = data[0].markups[i].metadata.count;
		}
	}

	return maxCount;
};

/**
 * Calculates the bounds of the sequence.
 *
 * @param bounds    bounds of the plot area
 * @param options   diagram options
 */
MutationDiagram.prototype.calcSequenceBounds = function (bounds, options)
{
	var x = bounds.x;
	var y = bounds.y +
	        Math.abs(options.regionHeight - options.seqHeight) / 2 +
	        options.seqPadding;
	var width = bounds.width;
	var height = options.seqHeight;

	return {x: x,
		y: y,
		width: width,
		height: height};
};
