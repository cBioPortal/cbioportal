/**
 * Constructor for MutationDiagram class.
 *
 * @param geneSymbol    hugo gene symbol
 * @param options       visual options object
 * @param data          collection of Mutation models (MutationCollection)
 * @constructor
 */
function MutationDiagram(geneSymbol, options, data)
{
	var self = this;

	// event listeners
	self.listeners = {};

	// merge options with default options to use defaults for missing values
	self.options = jQuery.extend(true, {}, self.defaultOpts, options);

	self.rawData = data; // data returned by server
	self.geneSymbol = geneSymbol; // hugo gene symbol

	// init other class members as null, will be assigned later
	self.svg = null;    // svg element (d3)
	self.bounds = null; // bounds of the plot area
	self.data = null;   // processed data
	self.gCircle = null; // svg group for lollipop circles
	self.gLine = null;   // svg group for lollipop lines
	self.gLabel = null;  // svg group for lollipop labels
	self.xScale = null;  // scale function for x-axis
	self.yScale = null;  // scale function for y-axis
	self.topLabel = null;   // label on top-left corner of the diagram
	self.xAxisLabel = null; // label for x-axis
	self.yAxisLabel = null; // label for y-axis

}

// TODO use percent values instead of pixel values for some components?
MutationDiagram.prototype.defaultOpts = {
	el: "#mutation_diagram_d3", // id of the container
	elWidth: 740,               // width of the container
	elHeight: 180,              // height of the container
	marginLeft: 40,             // left margin for the plot area
	marginRight: 30,            // right margin for the plot area
	marginTop: 30,              // top margin for the plot area
	marginBottom: 60,           // bottom margin for the plot area
	labelTop: "",                 // informative label on top of the diagram (false means "do not draw")
	labelTopFont: "sans-serif",   // font type of the top label
	labelTopFontColor: "#2E3436", // font color of the top label
	labelTopFontSize: "12px",     // font size of the top label
	labelTopFontWeight: "bold",   // font weight of the top label
	labelTopMargin: 2,            // left margin for the top label
	labelX: false,              // informative label of the x-axis (false means "do not draw")
	labelXFont: "sans-serif",   // font type of the x-axis label
	labelXFontColor: "#2E3436", // font color of the x-axis label
	labelXFontSize: "12px",     // font size of x-axis label
	labelXFontWeight: "normal", // font weight of x-axis label
	labelY: "# Mutations",      // informative label of the y-axis (false means "do not draw")
	labelYFont: "sans-serif",   // font type of the y-axis label
	labelYFontColor: "#2E3436", // font color of the y-axis label
	labelYFontSize: "12px",     // font size of y-axis label
	labelYFontWeight: "normal", // font weight of y-axis label
	minLengthX: 0,              // min value of the largest x value to show
	minLengthY: 5,              // min value of the largest y value to show
	seqFillColor: "#BABDB6",    // color of the sequence rectangle
	seqHeight: 14,              // height of the sequence rectangle
	seqPadding: 5,              // padding between sequence and plot area
	regionHeight: 24,           // height of a region (drawn on the sequence)
	regionFont: "sans-serif",   // font of the region text
	regionFontColor: "#FFFFFF", // font color of the region text
	regionFontSize: "12px",     // font size of the region text
	regionTextAnchor: "middle", // text anchor (alignment) for the region label
	showRegionText: true,       // show/hide region text
	lollipopLabelCount: 1,          // max number of lollipop labels to display
	lollipopLabelThreshold: 2,      // y-value threshold: circles below this value won't be labeled
	lollipopFont: "sans-serif",     // font of the lollipop label
	lollipopFontColor: "#2E3436",   // font color of the lollipop label
	lollipopFontSize: "10px",       // font size of the lollipop label
	lollipopTextAnchor: "auto",     // text anchor (alignment) for the lollipop label
	lollipopTextPadding: 5,         // padding between the label and the circle
	lollipopTextAngle: 0,           // rotation angle for the lollipop label
//	lollipopFillColor: "#B40000",
	lollipopFillColor: {            // color of the lollipop circle
		missense_mutation: "#008000",
		nonsense_mutation: "#FF0000",
		nonstop_mutation: "#FF0000",
		frame_shift_del: "#FF0000",
		frame_shift_ins: "#FF0000",
		in_frame_ins: "#000000",
		in_frame_del: "#000000",
		splice_site: "#FF0000",
		other: "#808080",       // all other mutation types
		default: "#800080"      // default is used when there is a tie
	},
	lollipopBorderColor: "#BABDB6",
	lollipopBorderWidth: 0.5,
	lollipopRadius: 3,              // radius of the lollipop circles
	lollipopHighlightRadius: 6,     // radius of the highlighted lollipop circles
	lollipopStrokeWidth: 1,         // width of the lollipop lines
	lollipopStrokeColor: "#BABDB6", // color of the lollipop line
	xAxisPadding: 10,           // padding between x-axis and the sequence
	xAxisTickIntervals: [       // valid major tick intervals for x-axis
		100, 200, 400, 500, 1000, 2000, 5000, 10000, 20000, 50000
	],
	xAxisTicks: 8,              // maximum number of major ticks for x-axis
								// (a major tick may not be labeled if it is too close to the max)
	xAxisTickSize: 6,           // size of the major ticks of x-axis
	xAxisStroke: "#AAAAAA",     // color of the x-axis lines
	xAxisFont: "sans-serif",    // font type of the x-axis labels
	xAxisFontSize: "10px",      // font size of the x-axis labels
	xAxisFontColor: "#2E3436",  // font color of the x-axis labels
	yAxisPadding: 5,            // padding between y-axis and the plot area
	yAxisLabelPadding: 15,      // padding between y-axis and its label
	yAxisTicks: 10,             // maximum number of major ticks for y-axis
	yAxisTickIntervals: [       // valid major tick intervals for y-axis
		1, 2, 5, 10, 20, 50, 100, 200, 500
	],
	yAxisTickSize: 6,           // size of the major ticks of y-axis
	yAxisStroke: "#AAAAAA",     // color of the y-axis lines
	yAxisFont: "sans-serif",    // font type of the y-axis labels
	yAxisFontSize: "10px",      // font size of the y-axis labels
	yAxisFontColor: "#2E3436",  // font color of the y-axis labels
	/**
	 * Default lollipop tooltip function.
	 *
	 * @param element   target svg element (lollipop circle)
	 * @param pileup    a pileup model instance
	 */
	lollipopTipFn: function (element, pileup) {
		var mutationStr = pileup.count > 1 ? "mutations" : "mutation";

		var model = {count: pileup.count,
			label: pileup.label};

		var tooltipView = new LollipopTipView({model: model});
		var content = tooltipView.compileTemplate();

		var options = {content: {text: content},
			hide: {fixed: true, delay: 100},
			style: {classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow'},
			position: {my:'bottom left', at:'top center'}};

		$(element).qtip(options);
	},
	/**
	 * Default region tooltip function.
	 *
	 * @param element   target svg element (region rectangle)
	 * @param region    a JSON object representing the region
	 */
	regionTipFn: function (element, region) {
		// TODO extract to a backbone view?
		var text = region.metadata.identifier + " " +
		           region.type.toLowerCase() + ", " +
		           region.metadata.description +
		           " (" + region.metadata.start + " - " + region.metadata.end + ")";

		var options = {content: {text: '<span class="diagram-region-tip">'+text+'</span>'},
			hide: {fixed: true, delay: 100},
			style: {classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow'},
			position: {my:'bottom left', at:'top center'}};

		$(element).qtip(options);
	}
};

/**
 * Initializes the diagram with the given sequence data.
 * If no sequence data is provided, then tries to retrieve
 * the data from the default servlet.
 *
 * @param sequenceData  sequence data as a JSON object
 */
MutationDiagram.prototype.initDiagram = function(sequenceData)
{
	var self = this;

	var container = d3.select(self.options.el);

	// calculate bounds of the actual plot area (excluding axis, sequence, labels, etc.)
	var bounds = {};
	bounds.width = self.options.elWidth -
			(self.options.marginLeft + self.options.marginRight);
	bounds.height = self.options.elHeight -
			(self.options.marginBottom + self.options.marginTop);
	bounds.x = self.options.marginLeft;
	bounds.y = self.options.elHeight - self.options.marginBottom;

	// save a reference for future access
	self.bounds = bounds;

	// helper function for actual initialization
	var init = function(sequenceData) {

		// create a data object
		var data = {};
		data.pileups = self.processData(self.rawData);
		data.sequence = sequenceData;

		// save a reference for future access
		self.data = data;

		// init svg container
		var svg = self.createSvg(container,
				self.options.elWidth,
				self.options.elHeight);

		// save a reference for future access
		self.svg = svg;

		// draw the whole diagram
		self.drawDiagram(svg,
				bounds,
				self.options,
				data);
	};

	// if no sequence data is provided, try to get it from the servlet
	if (!sequenceData)
	{
		$.getJSON("getPfamSequence.json",
			{geneSymbol: self.geneSymbol},
			function(data) {
				init(data);
			});
	}
	// if data is already there just init the diagram
	else
	{
		init(sequenceData);
	}
};

/**
 * Converts the mutation data returned from the server into
 * a list of Pileup instances.
 *
 * @param mutationData  list (MutationCollection) of mutations
 * @return {Array}      a list of pileup mutations
 */
MutationDiagram.prototype.processData = function (mutationData)
{
	var self = this;

	// helper function to generate a label by joining all unique
	// protein change information in the given array of mutations
	var generateLabel = function(mutations)
	{
		var mutationSet = {};

		// create a set of protein change labels
		// (this is to eliminate duplicates)
		for (var i = 0; i < mutations.length; i++)
		{
			if (mutations[i].proteinChange != null &&
			    mutations[i].proteinChange.length > 0)
			{
				mutationSet[mutations[i].proteinChange] = mutations[i].proteinChange;
			}
		}

		// convert to array & sort
		var mutationArray = [];

		for (var key in mutationSet)
		{
			mutationArray.push(key);
		}

		mutationArray.sort();

		// find longest common starting substring
		// (this is to truncate redundant starting substring)

		var startStr = "";

		if (mutationArray.length > 1)
		{
			startStr = cbio.util.lcss(mutationArray[0],
				mutationArray[mutationArray.length - 1]);

//			 console.log(mutationArray[0] + " n " +
//			             mutationArray[mutationArray.length - 1] + " = " +
//			             startStr);
		}

		// generate the string
		var label = startStr;

		for (var i = 0; i < mutationArray.length; i++)
		{
			label += mutationArray[i].substring(startStr.length) + "/";
		}

		// remove the last slash
		return label.substring(0, label.length - 1);
	};

	// create a map of mutations (key is the mutation location)
	var mutations = {};

	for (var i=0; i < mutationData.length; i++)
	{
		var mutation = mutationData.at(i);

		var proteinChange = mutation.proteinChange;

		var location = proteinChange.match(/[0-9]+/);

		if (location != null)
		{
			if (mutations[location] == null)
			{
				mutations[location] = [];
			}

			mutations[location].push(mutation);
		}
	}

	// convert map into an array of piled mutation objects
	var pileupList = [];

	for (var key in mutations)
	{
		var pileup = {};

		pileup.mutations = mutations[key];
		pileup.count = mutations[key].length;
		pileup.location = parseInt(key);
		pileup.label = generateLabel(mutations[key]);

		pileupList.push(new Pileup(pileup));
	}

	// sort (descending) the list wrt mutation count
	pileupList.sort(function(a, b) {
		var diff = b.count - a.count;

		// if equal, then compare wrt position (for consistency)
		if (diff == 0)
		{
			diff = b.location - a.location;
		}

		return diff;
	});

	return pileupList;
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
	var sequenceLength = parseInt(data.sequence["length"]);

	var xMax = Math.max(sequenceLength, options.minLengthX);
	var yMax = Math.max(self.calcMaxCount(data.pileups), options.minLengthY);
	var regions = data.sequence.regions;
	var pileups = data.pileups;
	var seqTooltip = data.sequence.metadata.identifier + ", " +
	               data.sequence.metadata.description + " (" + sequenceLength + "aa)";

	var xScale = d3.scale.linear()
		.domain([0, xMax])
		.range([bounds.x, bounds.x + bounds.width]);

	self.xScale = xScale;

	var yScale = d3.scale.linear()
		.domain([0, yMax])
		.range([bounds.y, bounds.y - bounds.height]);

	self.yScale = yScale;

	// draw x-axis
	self.drawXAxis(svg, xScale, xMax, options, bounds);

	if (options.labelX != false)
	{
		//TODO self.xAxisLabel = self.drawXAxisLabel(svg, options, bounds);
	}

	// draw y-axis
	self.drawYAxis(svg, yScale, yMax, options, bounds);

	if (options.labelY != false)
	{
		self.yAxisLabel = self.drawYAxisLabel(svg, options, bounds);
	}

	if (options.topLabel != false)
	{
		self.topLabel = self.drawTopLabel(svg, options, bounds);
	}

	// draw the plot area content
	self.drawPlot(svg,
		pileups,
		options,
		bounds,
		xScale,
		yScale);

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
 * Draw lollipop lines, circles and labels on the plot area
 * for the provided mutations (pileups).
 *
 * @param svg       svg container for the diagram
 * @param pileups   array of mutations (pileups)
 * @param options   options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 */
MutationDiagram.prototype.drawPlot = function(svg, pileups, options, bounds, xScale, yScale)
{
	var self = this;

	// group for lollipop labels (draw labels first)
	var gText = self.gLabel;
	if (gText === null)
	{
		gText = svg.append("g").attr("class", "mut-dia-lollipop-labels");
		self.gLabel = gText;
	}

	// group for lollipop lines (lines should be drawn before circles)
	var gLine = self.gLine;
	if (gLine === null)
	{
		gLine = svg.append("g").attr("class", "mut-dia-lollipop-lines");
		self.gLine = gLine;
	}

	// group for lollipop circles (circles should be drawn later)
	var gCircle = self.gCircle;
	if (gCircle === null)
	{
		gCircle = svg.append("g").attr("class", "mut-dia-lollipop-circles");
		self.gCircle = gCircle;
	}

	// draw lollipop lines and circles
	for (var i = 0; i < pileups.length; i++)
	{
		self.drawLollipop(gCircle,
				gLine,
				pileups[i],
				options,
				bounds,
				xScale,
				yScale);
	}

	// draw lollipop labels
	self.drawLollipopLabels(gText, pileups, options, xScale, yScale);
};

/**
 * Creates the main svg (graphical) component.
 *
 * @param container main container (div, etc.)
 * @param width     width of the svg area
 * @param height    height of the svg area
 * @return {object} svg component
 */
MutationDiagram.prototype.createSvg = function (container, width, height)
{
	var svg = container.append("svg");

	svg.attr('width', width);
	svg.attr('height', height);

	return svg;
};

// helper function to calculate major tick interval for the axis
/**
 * Calculates major tick interval for the given possible interval values,
 * maximum value on the axis, and the desired maximum tick count.
 *
 * @param intervals     possible interval values
 * @param maxValue      highest value on the axis
 * @param maxTickCount  desired maximum tick count
 * @return {number}     interval value
 */
MutationDiagram.prototype.calcTickInterval = function(intervals, maxValue, maxTickCount)
{
	var interval = -1;

	for (var i=0; i < intervals.length; i++)
	{
		interval = intervals[i];
		var count = maxValue / interval;

		//if (Math.round(count) <= maxLabelCount)
		if (count < maxTickCount - 1)
		{
			break;
		}
	}

	return interval;
};

/**
 * Calculates all tick values for the given max and interval values.
 *
 * @param maxValue  maximum value for the axis
 * @param interval  interval (increment) value
 * @return {Array}  an array of all tick values
 */
MutationDiagram.prototype.getTickValues = function(maxValue, interval)
{
	// determine tick values
	var tickValues = [];
	var value = 0;

	while (value < maxValue)
	{
		tickValues.push(value);
		// use half interval value for generating minor ticks
		// TODO change back to full value when there is a fix for d3 minor ticks
		value += interval / 2;
	}

	// add the max value in any case
	tickValues.push(maxValue);

	return tickValues;
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
 * @return {object} svg group containing all the axis components
 */
MutationDiagram.prototype.drawXAxis = function(svg, xScale, xMax, options, bounds)
{
	var self = this;

	var interval = self.calcTickInterval(options.xAxisTickIntervals,
		xMax,
		options.xAxisTicks);

	var tickValues = self.getTickValues(xMax, interval);

	// formatter to hide labels
	var formatter = function(value) {
//		var displayInterval = calcDisplayInterval(interval,
//			xMax,
//			options.xAxisMaxTickLabel);

		// always display max value
		if (value == xMax)
		{
			return value + " aa";
		}
		// do not display minor values
		// (this is custom implementation of minor ticks,
		// minor ticks don't work properly for custom values)
		else if (value % interval != 0)
		{
			return "";
		}
		// display major tick value if its not too close to the max value
		else if (xMax - value > interval / 3)
		{
			return value;
		}
		// hide remaining labels
		else
		{
			return "";
		}
	};

	var tickSize = options.xAxisTickSize;

	var xAxis = d3.svg.axis()
		.scale(xScale)
		.orient("bottom")
		.tickValues(tickValues)
		.tickFormat(formatter)
		//.tickSubdivide(true) TODO minor ticks have a problem with custom values
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
 * @return {object} svg group containing all the axis components
 */
MutationDiagram.prototype.drawYAxis = function(svg, yScale, yMax, options, bounds)
{
	var self = this;

	var interval = self.calcTickInterval(options.yAxisTickIntervals,
		yMax,
		options.yAxisTicks);

	// passing 2 * interval to avoid non-integer values
	// (this is also related to minor tick issue)
	var tickValues = self.getTickValues(yMax, 2 * interval);

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
		//.tickSubdivide(true) TODO minor ticks have a problem with custom values
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

/**
 * Draws the label of the y-axis.
 *
 * @param svg       svg to append the label element
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return {object} text label (svg element)
 */
MutationDiagram.prototype.drawTopLabel = function(svg, options, bounds)
{
	// set x, y of the label as the middle of the top left margins
	var x = options.labelTopMargin;
	var y = options.marginTop / 2;

	// append label
	var label = svg.append("text")
		.attr("fill", options.labelTopFontColor)
		.attr("text-anchor", "start")
		.attr("x", x)
		.attr("y", y)
		.attr("class", "mut-dia-top-label")
		.style("font-family", options.labelTopFont)
		.style("font-size", options.labelTopFontSize)
		.style("font-weight", options.labelTopFontWeight)
		.text(options.labelTop);

	return label;
};

/**
 * Draws the label of the y-axis.
 *
 * @param svg       svg to append the label element
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return {object} text label (svg element)
 */
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
		.style("font-weight", options.labelYFontWeight)
		.text(options.labelY);

	return label;
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
 * @param pileup    list (array) of mutations (pileup) at a specific location
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 * @return {object} lollipop circle & line as svg elements
 */
MutationDiagram.prototype.drawLollipop = function (circles, lines, pileup, options, bounds, xScale, yScale)
{
	var self = this;

	var count = pileup.count;
	var start = pileup.location;

	var x = xScale(start);
	var y = yScale(count);

	var circle = circles.append('circle')
		.attr('cx', x)
		.attr('cy', y)
		.attr('r', options.lollipopRadius)
		.attr('fill', self.getLollipopFillColor(options, pileup))
		.attr('stroke', options.lollipopBorderColor)
		.attr('stroke-width', options.lollipopBorderWidth);

	// bind pileup data with the lollipop circle
	circle.datum(pileup);

	var addTooltip = options.lollipopTipFn;
	addTooltip(circle, pileup);

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
 * Returns the fill color of the lollipop circle for the given pileup
 * of mutations.
 *
 * @param options   general options object
 * @param pileup    list (array) of mutations (pileup) at a specific location
 * @return {String} fill color
 */
MutationDiagram.prototype.getLollipopFillColor = function(options, pileup)
{
	var self = this;
	var color = options.lollipopFillColor;
	var value;

	if (_.isFunction(color))
	{
		value = color();
	}
	// check if the color is fixed
	else if (typeof color === "string")
	{
		value = color;
	}
	// assuming color is a map (an object)
	else
	{
		var types = PileupUtil.getMutationTypeArray(pileup);

		if (types.length > 1 &&
		    types[0].count == types[1].count)
		{
			value = color.default;
		}
		else if (color[types[0].type] == undefined)
		{
			value = color.other;
		}
		else
		{
			value = color[types[0].type];
		}
	}

	return value;
};

/**
 * Put labels over the lollipop circles. The number of labels to be displayed is defined
 * by options.lollipopLabelCount.
 *
 * @param labels        text group (svg element) for labels
 * @param pileups       array of mutations (pileups)
 * @param options       general options object
 * @param xScale        scale function for the x-axis
 * @param yScale        scale function for the y-axis
 */
MutationDiagram.prototype.drawLollipopLabels = function (labels, pileups, options, xScale, yScale)
{
	// helper function to adjust text position to prevent overlapping with the y-axis
	var getTextAnchor = function(text, textAnchor)
	{
		var anchor = textAnchor;

		// adjust if necessary and (if it is set to auto only)
		if (anchor.toLowerCase() == "auto")
		{
			// calculate distance of the label to the y-axis (assuming the anchor will be "middle")
			var distance = text.attr("x") - (text.node().getComputedTextLength() / 2);

			// adjust label to prevent overlapping with the y-axis
			if (distance < options.marginLeft)
			{
				anchor = "start";
			}
			else
			{
				anchor = "middle";
			}
		}

		return anchor;
	};

	var count = options.lollipopLabelCount;
	var maxAllowedTie = 2; // TODO refactor as an option?

	// do not show any label if there are too many ties
	// exception: if there is only one mutation then display the label in any case
	if (pileups.length > 1)
	{
		var max = pileups[0].count;

		// at the end of this loop, numberOfTies will be the number of points with
		// max y-value (number of tied points)
		for (var numberOfTies = 0; numberOfTies < pileups.length; numberOfTies++)
		{
			if (pileups[numberOfTies].count < max)
			{
				break;
			}
		}

		// do not display any label if there are too many ties
		if (count < numberOfTies &&
		    numberOfTies > maxAllowedTie)
		{
			count = 0;
		}

	}

	// show (lollipopLabelCount) label(s)
	for (var i = 0;
	     i < count && i < pileups.length;
	     i++)
	{
		// check for threshold value
		if (pileups.length > 1 &&
		    pileups[i].count < options.lollipopLabelThreshold)
		{
			// do not processes remaining values below threshold
			// (assuming mutations array is sorted)
			break;
		}

		var x = xScale(pileups[i].location);
		var y = yScale(pileups[i].count) -
		        (options.lollipopTextPadding + options.lollipopRadius);

		// init text
		var text = labels.append('text')
			.attr("fill", options.lollipopFontColor)
			.attr("x", x)
			.attr("y", y)
			.attr("class", "mut-dia-lollipop-text")
			.attr("transform", "rotate(" + options.lollipopTextAngle + ", " + x + "," + y +")")
			.style("font-size", options.lollipopFontSize)
			.style("font-family", options.lollipopFont)
			.text(pileups[i].label);

		// adjust anchor
		var textAnchor = getTextAnchor(text, options.lollipopTextAnchor);
		text.attr("text-anchor", textAnchor);
	}
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
 * @return {object} region rectangle & its text (as an svg group element)
 */
MutationDiagram.prototype.drawRegion = function(svg, region, options, bounds, xScale)
{
	var self = this;

	var start = region.metadata.start;
	var end = region.metadata.end;
	var label = region.text;
	var color = region.colour;

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

	var addTooltip = options.regionTipFn;

	// add tooltip to the rect
	addTooltip(rect, region);

	if (options.showRegionText)
	{
		var text = self.drawRegionText(label, group, options, width);

		// add tooltip if the text fits
		if (text)
		{
			// add tooltip to the text
			addTooltip(text, region);
		}
	}

	return group;
};

/**
 * Draws the text for the given svg group (which represents the region).
 * Returns null if neither the text nor its truncated version fits
 * into the region rectangle.
 *
 * @param label     text contents
 * @param group     target svg group to append the text
 * @param options   general options object
 * @param width     width of the region rectangle
 * @return {object} region text (svg element)
 */
MutationDiagram.prototype.drawRegionText = function(label, group, options, width)
{
	var xText = width/2;
	var height = options.regionHeight;

	if (options.regionTextAnchor === "start")
	{
		xText = 0;
	}
	else if (options.regionTextAnchor === "end")
	{
		xText = width;
	}

	// truncate or hide label if it is too long to fit
	var fits = true;

	// init text
	var text = group.append('text')
		.style("font-size", options.regionFontSize)
		.style("font-family", options.regionFont)
		.text(label)
		.attr("text-anchor", options.regionTextAnchor)
		.attr("fill", options.regionFontColor)
		.attr("x", xText)
		.attr("y", 2*height/3)
		.attr("class", "mut-dia-region-text");

	// check if the text fits into the region rectangle
	// adjust it if necessary
	if (text.node().getComputedTextLength() > width)
	{
		// truncate text if not fits
		label = label.substring(0,3) + "..";
		text.text(label);

		// check if truncated version fits
		if (text.node().getComputedTextLength() > width)
		{
			// remove if the truncated version doesn't fit either
			text.remove();
			text = null;
		}
	}

	return text;
};

/**
 * Draws the sequence just below the plot area.
 *
 * @param svg       target svg to append sequence rectangle
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return {object} sequence rectangle (svg element)
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
 * @param mutations array of piled up mutation data
 * @return {Number} number of mutations at the hottest spot
 */
MutationDiagram.prototype.calcMaxCount = function(mutations)
{
	var maxCount = -1;
//
//	for (var i = 0; i < mutations.length; i++)
//	{
//		if (mutations[i].count >= maxCount)
//		{
//			maxCount = mutations[i].count;
//		}
//	}
//
//	return maxCount;

	// assuming the list is sorted (descending)
	if (mutations.length > 0)
	{
		maxCount = mutations[0].count;
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

/**
 * Updates the plot area of the diagram for the given set of mutation data.
 * This function assumes that the provided mutation data is a subset
 * of the original data. Therefore this function only modifies the plot area
 * elements (lollipops, labels, etc.)
 *
 * If the number of mutations provided in mutationData is less than the number
 * mutation in the original data set, this function returns true to indicate
 * the provided data set is a subset of the original data. If the number of
 * mutations is the same, then returns false.
 *
 * @param mutationData  a collection of mutations
 * @return {boolean}    true if the diagram is filtered, false otherwise
 */
MutationDiagram.prototype.updatePlot = function(mutationData)
{
	var self = this;
	var pileups = this.processData(mutationData);

	// select all plot area elements
	var labels = self.gLabel.selectAll("text");
	var lines = self.gLine.selectAll("line");
	var circles = self.gCircle.selectAll("circle");

	// remove all plot elements (no animation)
	labels.remove();
	lines.remove();
	circles.remove();

	// TODO alternative animated version:
	// fade out and then remove all
//	labels.transition()
//		.style("opacity", 0)
//		.duration(1000)
//		.each("end", function() {
//			$(this).remove();
//		});
//
//	lines.transition()
//		.style("opacity", 0)
//		.duration(1000)
//		.each("end", function() {
//			$(this).remove();
//		});
//
//	circles.transition()
//		.style("opacity", 0)
//		.duration(1000)
//		.each("end", function() {
//			$(this).remove();
//		});

	// for the alternative animated version
	// this call should also be delayed to have a nicer effect

	// re-draw plot area contents for new data
	self.drawPlot(self.svg,
	              pileups,
	              self.options,
	              self.bounds,
	              self.xScale,
	              self.yScale);

	// also re-add listeners
	for (var selector in self.listeners)
	{
		var target = self.svg.selectAll(selector);

		for (var event in self.listeners[selector])
		{
			target.on(event,
				self.listeners[selector][event]);
		}
	}

	var filtered = false;

	if (mutationData.length < self.rawData.length)
	{
		filtered = true;
	}

	return filtered;
};

/**
 * Resets the plot area back to its initial state.
 */
MutationDiagram.prototype.resetPlot = function()
{
	var self = this;

	self.updatePlot(self.rawData);
};

/**
 * Updates the text of the top label.
 *
 * @param text  new text to set as the label value
 */
MutationDiagram.prototype.updateTopLabel = function(text)
{
	var self = this;

	// if no text value is passed used gene symbol to update the value
	if (text == undefined || text == null)
	{
		text = "";
	}

	self.topLabel.text(text);
};

/**
 * Adds an event listener for specific diagram elements.
 *
 * @param selector  selector string for elements
 * @param event     name of the event
 * @param handler   event handler function
 */
MutationDiagram.prototype.addListener = function(selector, event, handler)
{
	var self = this;

	// TODO define string constants for selectors?

	self.svg.selectAll(selector).on(event, handler);

	// save the listener for future reference
	if (self.listeners[selector] == null)
	{
		self.listeners[selector] = {};
	}

	self.listeners[selector][event] = handler;

};

/**
 * Removes an event listener for specific diagram elements.
 *
 * @param selector  selector string for elements
 * @param event     name of the event
 */
MutationDiagram.prototype.removeListener = function(selector, event)
{
	var self = this;

	self.svg.selectAll(selector).on(event, null);

	// remove listener from the map
	if (self.listeners[selector] &&
	    self.listeners[selector][event])
	{
		delete self.listeners[selector][event];
	}
};

/**
 * Resets all highlighted circles back to their original state.
 */
MutationDiagram.prototype.clearHighlights = function()
{
	var self = this;
	var circles = self.gCircle.selectAll("circle");

	circles.attr("r", self.options.lollipopRadius);
};

/**
 * Highlights a single circle. This function assumes that the provided
 * selector is a selector for one of the SVG circle elements on the
 * diagram.
 *
 * @param selector  selector for a specific circle element
 */
MutationDiagram.prototype.highlight = function(selector)
{
	var self = this;
	var circle = d3.select(selector);

	circle.transition()
		.ease("elastic")
		.duration(600)
		.delay(100)
		.attr("r", self.options.lollipopHighlightRadius);
};