/**
 * Constructor for the MutationPdbPanel class.
 *
 * @param options   visual options object
 * @param data      PDB data
 * @param xScale    scale function for the x axis
 * @constructor
 */
function MutationPdbPanel(options, data, xScale)
{
	/**
	 * Default visual options.
	 */
	var _defaultOpts = {
		el: "#mutation_pdb_panel_d3", // id of the container
		elWidth: 740,       // width of the container
		elHeight: 60,       // height of the container (TODO this should vary by the number of chains)
		marginLeft: 40,     // left margin
		marginRight: 30,    // right margin
		marginTop: 0,       // top margin
		marginBottom: 0,    // bottom margin
		chainHeight: 6,     // height of a rectangle representing a single pdb chain
		chainPadding: 2     // padding between chain rectangles
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	/**
	 * Draws the actual content of the panel, by drawing a rectangle
	 * for each chain
	 *
	 * @param svg
	 * @param options
	 * @param data
	 * @param xScale
	 */
	function drawPanel(svg, options, data, xScale)
	{
		// TODO using only the first PDB id
		if (data.length > 0)
		{
			var pdb = data[0];

			_.each(pdb.chains, function(ele, idx) {
				// TODO ele.start & ele.end is not defined yet...
				//var start = ele.start;
				//var end = ele.end;

				// TODO replace these test values with actual ones
				var start = 100;
				var end = 200;
				var color = "#AABBCC";

				var width = Math.abs(xScale(start) - xScale(end));
				var height = options.chainHeight;
				var y = options.marginTop +
				        idx * (options.chainHeight + options.chainPadding);
				var x = xScale(start);

				var rect = svg.append('rect')
					.attr('fill', color)
					.attr('x', x)
					.attr('y', y)
					.attr('width', width)
					.attr('height', height);
			});
		}
	}

	function createSvg(container, width, height)
	{
		var svg = container.append("svg");

		svg.attr('width', width);
		svg.attr('height', height);

		return svg;
	}

	function init()
	{
		// init svg container
		var container = d3.select(_options.el);

		var svg = createSvg(container,
		                    _options.elWidth,
		                    _options.elHeight);

		drawPanel(svg, _options, data, xScale);
	}

	return {init: init};
}

