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

	var container = d3.select("#" + options.el);
	var svg = self.createSvg(container,
			options.elWidth,
			options.elHeight);

	self.drawDiagram(svg,
		{width: 700, height: 150, x: 10, y: 10},
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
	labelX: null,
	labelY: "# Mutations"
};

/**
 *
 * @param svg       svg container for the diagram
 * @param bounds    bounds of the diagram {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param options   options object
 * @param data      data to visualize
 */
MutationDiagram.prototype.drawDiagram = function (svg, bounds, options, data) {
	var self = this;

	// TODO temp test value, data structure should change completely
	var xMax = data[0].length;
	var yMax = self.calcMaxCount(data) + 3; // TODO offset = 3

	var xScale = d3.scale.linear()
		.domain([0, xMax])
		.range([bounds.x, bounds.x + bounds.width]);

	var yScale = d3.scale.linear()
		.domain([0, yMax])
		.range([bounds.y + bounds.height, bounds.y]);

	// TODO draw axes

	// TODO draw sequence & regions

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
				.attr('r', 3);
		}
	}
};

MutationDiagram.prototype.createSvg = function (container, width, height) {
	var svg = container.append("svg");

	// use default values if necessary

	if (width == undefined)
	{
		width = this.defaultOpts.elWidth;
	}

	if (height == undefined)
	{
		height = this.defaultOpts.elHeight;
	}

	svg.attr('width', width);
	svg.attr('height', height);

	return svg;
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
}
