/**
 * Constructor for MutationDiagram class.
 *
 * @param options   visual options object
 * @param data      a collection of Mutation models (MutationCollection)
 * @constructor
 */
function MutationDiagram(geneSymbol, options, data)
{
	// TODO after refactoring the data will be generic,
	// so we will need to process it before(or after?) passing to this function

	var self = this;

	// merge options with default options to use defaults for missing values
	self.options = jQuery.extend({}, self.defaultOpts, options);
	self.data = data;
	self.geneSymbol = geneSymbol;
}

MutationDiagram.prototype.initDiagram = function ()
{
	var self = this;

	var container = d3.select("#" + self.options.el);

	// calculate bounds of the actual plot area (excluding axis, sequence, labels, etc.)
	var bounds = {};
	bounds.width = self.options.elWidth -
			(self.options.marginLeft + self.options.marginRight);
	bounds.height = self.options.elHeight -
			(self.options.marginBottom + self.options.marginTop);
	bounds.x = self.options.marginLeft;
	bounds.y = self.options.elHeight - self.options.marginBottom;

	$.getJSON("getPfamSequence.json",
		{geneSymbol: self.geneSymbol},
		function(response) {
			self.data = self.processData(self.data, response);

			var svg = self.createSvg(container,
				self.options.elWidth,
				self.options.elHeight);

			self.drawDiagram(svg,
				bounds,
				self.options,
				self.data);
		});
};

MutationDiagram.prototype.processData = function (mutationData, sequenceData)
{
	var data = {};

	// TODO process a collection of mutation data
	data.mutations = mutationData[0].markups; // TODO replace with collection data
	data.sequence = sequenceData;

	return data;
};


// TODO add more options for a more customizable diagram:
// default tooltip templates?
// max # of lollipop labels to display
// TODO use percent values instead of pixel values for some components?
MutationDiagram.prototype.defaultOpts = {
	el: "mutation_diagram_d3",  // id of the container
	elWidth: 720,               // width of the container
	elHeight: 180,              // height of the container
	marginLeft: 40,             // left margin for the plot area
	marginRight: 20,            // right margin for the plot area
	marginTop: 30,              // top margin for the plot area
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
	regionTextAnchor: "middle", // text anchor (alignment) for the region label
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

	var xMax = data.sequence.sequenceLength + options.offsetX;
	var yMax = self.calcMaxCount(data) + options.offsetY;
	var regions = data.sequence.regions;
	var mutations = data.mutations; // TODO this is still not refactored
	var seqTooltip = data.sequence.identifier + ", " +
	               data.sequence.description + " (" + data.sequence.sequenceLength + "aa)";

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

	// group for lollipop lines (lines should be drawn first)
	var gLine = svg.append("g").attr("class", "mut-dia-lollipop-lines");
	// group for lollipop circles (circles later)
	var gCircle = svg.append("g").attr("class", "mut-dia-lollipop-circles");

	// draw lollipop lines
	for (var i = 0; i < mutations.length; i++)
	{
		var mutation = mutations[i];

		// TODO this check may be redundant after refactoring data structure
		if (mutation.type === "mutation")
		{
			self.drawLollipop(gCircle, gLine, mutation, options, bounds, xScale, yScale);
		}
	}

	// draw sequence
	var sequence = self.drawSequence(svg, options, bounds);
	// add a regular tooltip (not qtip)
	sequence.attr("title", seqTooltip);

	// draw regions
	for (var i = 0, size = regions.length; i < size; i++)
	{
		self.drawRegion(svg, regions[i], options, bounds, xScale);
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
	// set x, y of the label as the middle of the y-axis

	var x = bounds.x -
		options.yAxisPadding -
		options.yAxisTickSize -
		options.yAxisLabelPadding;

	var y =  bounds.y - (bounds.height / 2);

	// append label
	var label = svg.append("text")
		.attr("fill", options.labelYFontColor)
		.attr("text-anchor", "middle")
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
 * Draws the lollipop circle and its line (from sequence to the lollipop circle)
 * on the plot area.
 *
 * @param circles   circle group (svg element) to append the lollipop circle
 * @param lines     line group (svg element) to append the lollipop lines
 * @param mutation  mutation data
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 * @return          object (lollipop circle & line as svg elements)
 */
MutationDiagram.prototype.drawLollipop = function (circles, lines, mutation, options, bounds, xScale, yScale)
{
	var self = this;

	// TODO may change after refactoring data structure
	var count = mutation.metadata.count;
	var start = mutation.start;
	var label = mutation.metadata.label;
	var title = "<b>" + count + " mutations</b>" +
	            "<br/>Amino Acid Change: " + label;

	var x = xScale(start);
	var y = yScale(count);

	var circle = circles.append('circle')
		.attr('cx', x)
		.attr('cy', y)
		.attr('r', options.lollipopRadius)
		.attr('fill', options.lollipopFillColor);

	self.addPlotTooltip(circle, title);

	var line = lines.append('line')
		.attr('x1', x)
		.attr('y1', y)
		.attr('x2', x)
		.attr('y2', self.calcSequenceBounds(bounds, options).y)
		.attr('stroke', options.lollipopStrokeColor)
		.attr('stroke-width', options.lollipopStrokeWidth);

	return {"circle": circle, "line": line};
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
 * @return          region rectangle & its text (as an svg group element)
 */
MutationDiagram.prototype.drawRegion = function(svg, region, options, bounds, xScale)
{
	var self = this;

	var start = region.start;
	var end = region.end;
	var label = region.text;
	var color = region.color;
	var tooltip = region.identifier + " " +
	              region.type.toLowerCase() + ", " +
	              region.description +
	              " (" + start + " - " + end + ")";

	var width = Math.abs(xScale(start) - xScale(end));
	var height = options.regionHeight;
	var y = bounds.y + options.seqPadding;
	var x = xScale(start);

	// group region and its label
	var group = svg.append("g")
		.attr("class", "mut-dia-region")
		.attr("transform", "translate(" + x + "," + y +")");

	var rect = group.append('rect')
		.attr('fill', color)
		.attr('x', 0)
		.attr('y', 0)
		.attr('width', width)
		.attr('height', height);

	var xText = width/2;

	if (options.regionTextAnchor === "start")
	{
		xText = 0;
	}
	else if (options.regionTextAnchor === "end")
	{
		xText = width;
	}

	// TODO truncate or hide label if it is too long to fit
	var text = group.append('text')
		.attr("text-anchor", options.regionTextAnchor)
		.attr("fill", options.regionFontColor)
		.attr("x", xText)
		.attr("y", 2*height/3)
		.attr("class", "mut-dia-region-text")
		.style("font-size", options.regionFontSize)
		.style("font-family", options.regionFont)
		.text(label);

	// add tooltip (both to the text and the rect)
	self.addRegionTooltip(rect, tooltip);
	self.addRegionTooltip(text, tooltip);

	return group;
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
	// TODO refactor this after refactoring data structure!!
	// this is the old method to find max count

	var maxCount = 0;
	var size = data.mutations.length;

	for (var i = 0; i < size; i++)
	{
		if ((data.mutations[i].type == "mutation") &&
		    (parseInt(data.mutations[i].metadata.count) >= maxCount))
		{
			maxCount = data.mutations[i].metadata.count;
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

   MutationDiagram.prototype.addPlotTooltip = function(element, txt)
{
	// TODO make the tooltip customizable (allow to pass options)
	$(element).qtip({
		content: {text: '<font size="2">'+txt+'</font>'},
		hide: {fixed: true, delay: 100 },
		style: {classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
		position: {my:'bottom center',at:'top center'}
	});
};

MutationDiagram.prototype.addRegionTooltip = function(element, txt)
{
	// TODO make the tooltip customizable (allow to pass options)
	$(element).qtip({
		content: {text: '<font size="2">'+txt+'</font>'},
		hide: {fixed: true, delay: 100 },
		style: {classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
		position: {my:'bottom left',at:'top center'}
	});
};