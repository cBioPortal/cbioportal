/**
 * Constructor for MutationDiagram class.
 *
 * @param options TODO
 * @param data TODO
 * @constructor
 */
function MutationDiagram(options, data)
{
	var self = this;

	// merge options with default options to use defaults for missing values
	options = jQuery.extend({}, self.defaultOpts, options);

	var container = d3.select("#" + options.el);
	var svg = self.createSvg(container,
			options.elWidth,
			options.elHeight);

	// calculate bounds of the actual plot area (excluding the axis)
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

// TODO add more options for more customizable diagram:
// start position (origin position) wrt to the container,
// tick frequency (interval) on the axes,
// width & height of the actual diagram wrt the container,
// default tooltip templates?
// label positions and alignments
// max # of lollipop labels to display
// max number of axis coordinates to display
// color, font, style, etc of various elements
// sequence height (which is drawn on x axis)
MutationDiagram.prototype.defaultOpts = {
	el: "mutation_diagram_d3",
	elWidth: 720,
	elHeight: 180,
	marginLeft: 20,
	marginRight: 10,
	marginTop: 20,
	marginBottom: 40,
	labelX: null,
	labelY: "# Mutations",
	offsetY: 3,
	offsetX: 0,
	seqFillColor: "#BABDB6",
	seqHeight: 16,
	seqPadding: 8,
	regionHeight: 24,
	regionTextFillColor: "#FFFFFF",
	regionTextPadding: 5,
	lollipopFillColor: "#B40000", // TODO more than one color wrt mutation type?
	lollipopRadius: 3,
	lollipopStrokeWidth: 1,
	lollipopStrokeColor: "#BABDB6"
};

/**
 *
 * @param svg       svg container for the diagram
 * @param bounds    bounds of the diagram {width, height, x, y}
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

	// TODO draw axes

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

MutationDiagram.prototype.createSvg = function (container, width, height)
{
	var svg = container.append("svg");

	svg.attr('width', width);
	svg.attr('height', height);

	return svg;
};

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
}

MutationDiagram.prototype.drawRegion = function(svg, region, options, bounds, xScale)
{
	var width = Math.abs(xScale(region.start) - xScale(region.end));
	var height = options.regionHeight;
	var y = bounds.y + options.seqPadding;
	var x = bounds.x + xScale(region.start);

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

	// TODO text alignment (center, left, right)
	var text = svg.append('text')
		.attr('fill', options.regionTextFillColor)
		.attr('x', x + options.regionTextPadding)
		.attr('y', y + 2*height/3)
		.text(label);

	return rect;
};

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
 * Return the number of mutations at the hottest spot.
 *
 * @param data      mutation data
 * @return {number}
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

