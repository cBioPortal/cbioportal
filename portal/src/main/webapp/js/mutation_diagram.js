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
	seqFillColor: "#BABDB6",
	seqHeight: 16,
	seqPadding: 8,
	regionHeight: 24,
	lollipopFillColor: "#B40000", // TODO more than one color wrt mutation type?
	lollipopRadius: 3
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

	// TODO temp test value, data structure should change completely
	var xMax = data[0].length;
	var yMax = self.calcMaxCount(data) + 3; // TODO offset = 3

	var xScale = d3.scale.linear()
		.domain([0, xMax])
		.range([bounds.x, bounds.x + bounds.width]);

	var yScale = d3.scale.linear()
		.domain([0, yMax])
		.range([bounds.y, bounds.y - bounds.height]);

	// TODO draw axes

	// TODO draw sequence
	var sequence = self.drawSequence(svg, options, bounds);

	// TODO draw regions
	for (i = 0, size = data[0].regions.length; i < size; i++)
	{
		self.drawRegion(svg, data[0].regions[i], options, bounds, xScale);
	}

	// TODO draw lollipops
	for (var i = 0, size = data[0].markups.length; i < size; i++)
	{
		if (data[0].markups[i].type == "mutation")
		{
			var x = xScale(data[0].markups[i].start);
			var y = yScale(data[0].markups[i].metadata.count);

			svg.append('circle')
				.attr('cy', y)
				.attr('cx', x)
				.attr('r', options.lollipopRadius)
				.attr('fill', options.lollipopFillColor);
		}
	}
};

MutationDiagram.prototype.createSvg = function (container, width, height)
{
	var svg = container.append("svg");

	svg.attr('width', width);
	svg.attr('height', height);

	return svg;
};


MutationDiagram.prototype.drawRegion = function(svg, region, options, bounds, xScale)
{
	var width = Math.abs(xScale(region.start) - xScale(region.end));
	var height = options.regionHeight;
	var y = bounds.y + options.seqPadding;
	var x = bounds.x + xScale(region.start);

	return svg.append('rect')
		.attr('fill', "#E0E0E0") // TODO fill color?
		.attr('x', x)
		.attr('y', y)
		.attr('width', width)
		.attr('height', height);
};

MutationDiagram.prototype.drawSequence = function(svg, options, bounds)
{
	var x = bounds.x;
	var y = bounds.y +
	        Math.abs(options.regionHeight - options.seqHeight) / 2 +
	        options.seqPadding;
	var width = bounds.width;
	var height = options.seqHeight;

	return svg.append('rect')
		.attr('fill', options.seqFillColor)
		.attr('x', x)
		.attr('y', y)
		.attr('width', width)
		.attr('height', height);
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

