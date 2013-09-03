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
		elHeight: "auto",   // height of the container
		marginLeft: 40,     // left margin
		marginRight: 30,    // right margin
		marginTop: 0,       // top margin
		marginBottom: 0,    // bottom margin
		chainHeight: 6,     // height of a rectangle representing a single pdb chain
		chainPadding: 2,    // padding between chain rectangles
		// TODO duplicate google colors, taken from OncoprintUtils.js (default branch)
		// ...use OncoprintUtils or move colors to a general util class after merging
		colors: ["#3366cc","#dc3912","#ff9900","#109618",
			"#990099","#0099c6","#dd4477","#66aa00",
			"#b82e2e","#316395","#994499","#22aa99",
			"#aaaa11","#6633cc","#e67300","#8b0707",
			"#651067","#329262","#5574a6","#3b3eac",
			"#b77322","#16d620","#b91383","#f4359e",
			"#9c5935","#a9c413","#2a778d","#668d1c",
			"#bea413","#0c5922","#743411"],
		/**
		 * Default chain tooltip function.
		 *
		 * @param element   target svg element (rectangle)
		 * @param segment   a single segment on the chain
		 */
		chainTipFn: function (element, segment) {
			// TODO define a backbone view: PdbChainTipView
			var datum = element.datum();

			var tip = "<span class='pdb-chain-tip'>" +
			          "<b>PDB:</b> " + datum.pdbId + "<br>" +
			          "<b>Chain:</b> " + datum.chain.chainId +
			          " (" + segment.start + " - " + segment.end + ")" +
			          "</span>";

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
		// we need to count chains to calculate y value
		var count = 0;

		// TODO rank chains by length. also limit number of chains?
		data.each(function(pdb, idx) {
			// create rectangle(s) for each chain
			_.each(pdb.chains, function(ele, idx) {
				// chain datum
				var datum = {pdbId: pdb.pdbId, chain: ele};

				// assign a different color to each chain
				var color = options.colors[count % options.colors.length];

				// add a rectangle for each segment
				_.each(ele.segments, function(ele, idx) {
					var start = ele.start;
					var end = ele.end;

					var width = Math.abs(xScale(start) - xScale(end));
					var height = options.chainHeight;
					var y = options.marginTop +
					        count * (options.chainHeight + options.chainPadding);
					var x = xScale(start);

					var rect = svg.append('rect')
						.attr('fill', color)
						.attr('x', x)
						.attr('y', y)
						.attr('width', width)
						.attr('height', height);

					// bind chain data to the rectangle
					rect.datum(datum);

					// add tooltip
					var addTooltip = options.chainTipFn;
					addTooltip(rect, ele);
				});

				// increment counter
				count++;
			});
		});
	}

	/**
	 * Calculates the height of the panel wrt to provided elHeight option.
	 *
	 * @param elHeight  provided height value
	 * @return {number}
	 */
	function calcHeight(elHeight)
	{
		var height = 0;

		// if not auto, then just copy the value
		if (elHeight != "auto")
		{
			height = elHeight;
		}
		else
		{
			var chainCount = 0;

			data.each(function(pdb, idx) {
				chainCount += pdb.chains.length;
			});

			height = _options.marginTop + _options.marginBottom +
				chainCount * (_options.chainHeight + _options.chainPadding);
		}

		return height;
	}

	/**
	 * Creates the main svg element.
	 *
	 * @param container target container (html element)
	 * @param width     widht of the svg
	 * @param height    height of the svg
	 * @return {object} svg instance (D3)
	 */
	function createSvg(container, width, height)
	{
		var svg = container.append("svg");

		svg.attr('width', width);
		svg.attr('height', height);

		return svg;
	}

	/**
	 * Initializes the panel.
	 */
	function init()
	{
		// init svg container
		var container = d3.select(_options.el);

		// create svg element & update its reference
		var svg = createSvg(container,
		                    _options.elWidth,
		                    calcHeight(_options.elHeight));

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

	/**
	 * Shows the panel.
	 */
	function show()
	{
		$(_options.el).show();
	}

	/**
	 * Hides the panel.
	 */
	function hide()
	{
		$(_options.el).hide();
	}

	return {init: init,
		addListener: addListener,
		removeListener: removeListener,
		show: show,
		hide: hide};
}

