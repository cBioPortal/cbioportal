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
		numChains: 5,       // max number of chains (rows) to be displayed initially
		marginLeft: 40,     // left margin
		marginRight: 30,    // right margin
		marginTop: 0,       // top margin
		marginBottom: 0,    // bottom margin
		chainHeight: 6,     // height of a rectangle representing a single pdb chain
		chainPadding: 2,    // padding between chain rectangles
		labelY: false,      // informative label of the y-axis (false means "do not draw")
		labelYFont: "sans-serif",   // font type of the y-axis label
		labelYFontColor: "#2E3436", // font color of the y-axis label
		labelYFontSize: "12px",     // font size of y-axis label
		labelYFontWeight: "normal", // font weight of y-axis label
		labelYPaddingRight: 15, // padding between y-axis and its label
		labelYPaddingTop: 20, // padding between y-axis and its label
		// TODO duplicate google colors, taken from OncoprintUtils.js
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
		 */
		chainTipFn: function (element) {
			// TODO define a backbone view: PdbChainTipView
			var datum = element.datum();

			var tip = "<span class='pdb-chain-tip'>" +
			          "<b>PDB id:</b> " + datum.pdbId + "<br>" +
			          "<b>Chain:</b> " + datum.chain.chainId +
			          //" (" + alignment.uniprotFrom + " - " + alignment.uniprotTo + ")" +
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

	// total number of chains (for the given PDB data)
	var _chainCount = calcChainCount(data);

	// collapse indicator (initially true)
	var _collapsed = true;

	/**
	 * Draws the actual content of the panel, by drawing a rectangle
	 * for each chain
	 *
	 * @param svg       svg element (D3)
	 * @param options   visual options object
	 * @param data      PDB data (collection of PdbModel instances)
	 * @param xScale    scale function for the x-axis
	 */
	function drawPanel(svg, options, data, xScale)
	{
		// we need to count chains to calculate y value
		var count = 0;

		// TODO define a rule to rank (sort) chains
		data.each(function(pdb, idx) {

			// create rectangle(s) for each chain
			pdb.chains.each(function(ele, idx) {
				// chain datum
				var datum = {pdbId: pdb.pdbId, chain: ele};

				// assign a different color to each chain
				var color = options.colors[count % options.colors.length];

				// add rectangle(s) for the chain
				// TODO color code special characters
				if (ele.alignments.length > 0)
				{
					var start = ele.alignments.at(0).uniprotFrom;
					var end = start + ele.alignmentSummary.length;

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
					addTooltip(rect);
				}

				// increment counter
				count++;
			});
		});
	}

	/**
	 * Draws the label of the y-axis.
	 *
	 * @param svg       svg to append the label element
	 * @param options   general options object
	 * @return {object} text label (svg element)
	 */
	function drawYAxisLabel(svg, options)
	{
		// TODO this needs to be tuned to fit

		// set x, y of the label to the top left

		var x = options.marginLeft -
		        options.labelYPaddingRight;

		var y =  options.marginTop +
		         options.labelYPaddingTop;

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
	}

	/**
	 * Calculates total number of chains for the given PDB data.
	 *
	 * @param data      PDB data (collection of PdbModel instances)
	 * @return {number} total number of chains
	 */
	function calcChainCount(data)
	{
		var chainCount = 0;

		data.each(function(pdb, idx) {
			chainCount += pdb.chains.length;
		});

		return chainCount;
	}

	/**
	 * Calculates the full height of the panel wrt to provided elHeight option.
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
			height = _options.marginTop + _options.marginBottom +
				_chainCount * (_options.chainHeight + _options.chainPadding);
		}

		return height;
	}

	/**
	 * Calculates the collapsed height of the panel wrt to provided
	 * maxChain option.
	 *
	 * @param maxChain  maximum number of chains to be displayed
	 * @return {number} calculated collapsed height
	 */
	function calcCollapsedHeight(maxChain)
	{
		var height = 0;

		if (maxChain < _chainCount)
		{
			height = _options.marginTop +
			         maxChain * (_options.chainHeight + _options.chainPadding);
		}
		// total number of chains is less than max, set to full height
		else
		{
			height = calcHeight("auto");
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
		                    calcCollapsedHeight(_options.numChains));

		_svg = svg;

		// draw the panel
		drawPanel(svg, _options, data, xScale);

		// draw the labels
		if (_options.labelY != false)
		{
			drawYAxisLabel(svg, _options);
		}
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
	function showPanel()
	{
		$(_options.el).show();
	}

	/**
	 * Hides the panel.
	 */
	function hidePanel()
	{
		$(_options.el).hide();
	}

	/**
	 * Resizes the panel height to show only a limited number of chains.
	 */
	function collapsePanel()
	{
		// resize to collapsed height
		var collapsedHeight = calcCollapsedHeight(_options.numChains);
		_svg.transition().duration(1000).attr("height", collapsedHeight);
		_collapsed = true;
	}

	/**
	 * Resizes the panel to its full height (to show all chains).
	 */
	function expandPanel()
	{
		// resize to full size
		var fullHeight = calcHeight(_options.elHeight);
		_svg.transition().duration(1000).attr("height", fullHeight);
		_collapsed = false;
	}

	/**
	 * Expands/Collapses the panel.
	 */
	function toggleHeight()
	{
		if (_collapsed)
		{
			expandPanel();
		}
		else
		{
			collapsePanel();
		}
	}

	return {init: init,
		addListener: addListener,
		removeListener: removeListener,
		show: showPanel,
		hide: hidePanel,
		toggleHeight: toggleHeight};
}

