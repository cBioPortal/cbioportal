/**
 * Constructor for the MutationPdbPanel class.
 *
 * @param options   visual options object
 * @param data      PDB data (collection of PdbModel instances)
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
		chainPadding: 2,    // padding between chain rectangles
		/**
		 * Default chain tooltip function.
		 *
		 * @param element   target svg element (rectangle)
		 * @param datum     chain data
		 */
		chainTipFn: function (element, datum) {
			// TODO improve tip (and define backbone view?)
			var tip = datum.pdbId + ":" + datum.chain.chainId +
			          " (" + datum.chain.start + " - " + datum.chain.end + ")";

			var options = {content: {text: tip},
				hide: {fixed: true, delay: 100},
				style: {classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow'},
				position: {my:'bottom left', at:'top center'}};

			$(element).qtip(options);
		}
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// reference to the main svg element
	var _svg = null;

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
			var pdb = data.at(0);

			// create a rectangle for each chain
			_.each(pdb.chains, function(ele, idx) {
				var start = ele.start;
				var end = ele.end;

				// TODO assign a different color for each chain
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

				// bind chain data to the rectangle
				var datum = {pdbId: pdb.pdbId, chain: ele};
				rect.datum(datum);

				// add tooltip
				var addTooltip = options.chainTipFn;
				addTooltip(rect, datum);
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

		// create svg element & update its reference
		var svg = createSvg(container,
		                    _options.elWidth,
		                    _options.elHeight);

		_svg = svg;

		// draw the panel
		drawPanel(svg, _options, data, xScale);
	}

	/**
	 * Adds an event listener for specific diagram elements.
	 *
	 * @param selector  selector string for elements
	 * @param event     name of the event
	 * @param handler   event handler function
	 */
	function addListener(selector, event, handler)
	{
		// TODO define string constants for selectors?

		_svg.selectAll(selector).on(event, handler);
	}

	/**
	 * Removes an event listener for specific diagram elements.
	 *
	 * @param selector  selector string for elements
	 * @param event     name of the event
	 */
	function removeListener(selector, event)
	{
		_svg.selectAll(selector).on(event, null);
	}

	return {init: init,
		addListener: addListener,
		removeListener: removeListener};
}

