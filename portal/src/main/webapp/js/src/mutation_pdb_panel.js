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
		numRows: [5, 10, 20], // number of rows to be to be displayed for each expand request
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
		// opacity wrt segment type
		opacity: {
			"regular": 1.0,
			"*": 0.4, // Gap
			" ": 0.6, // Mismatch
			"+": 0.8, // Similar
			"-": 0.6
		},
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
			          " (" + datum.chain.mergedAlignment.uniprotFrom +
			          " - " + datum.chain.mergedAlignment.uniprotTo + ")" +
			          "</span>";

			var options = {content: {text: tip},
				hide: {fixed: true, delay: 100},
				style: {classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow'},
				position: {my:'bottom left', at:'top center'}};

			$(element).qtip(options);
		}
	};

	// event listeners
	var _listeners = {};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// reference to the main svg element
	var _svg = null;

	// row data (allocation of chains wrt rows)
	var _rowData = null;

	// expansion level indicator (initially 0)
	var _expansion = 0;

	// max expansion level:
	// assume numRows = [0, 100, 500], and number of total rows = 20
	// in this case max level should be 2 (although numRows has 3 elements)
	var _maxExpansionLevel = null;

	// number of total rectangles drawn (initially 0)
	// this number is being updated after each panel expand
	var _rectCount = 0;

	// indicator for an expansion level whether the rectangles drawn
	var _levelDrawn = [];

	/**
	 * Draws the actual content of the panel, by drawing a rectangle
	 * for each chain
	 *
	 * @param svg       svg element (D3)
	 * @param options   visual options object
	 * @param data      row data
	 * @param xScale    scale function for the x-axis
	 * @param rowStart  starting index for the first row
	 */
	function drawPanel(svg, options, data, xScale, rowStart)
	{
		// chain counter
		var count = _rectCount;

		// add a rectangle group for each chain
		_.each(data, function(allocation, rowIdx) {
			_.each(allocation, function(datum, idx) {
				var chain = datum.chain;

				// create the rectangle group
				if (chain.alignments.length > 0)
				{
					// assign a different color to each chain
					var color = options.colors[count % options.colors.length];
					datum.color = color;

					var y = options.marginTop +
					        (rowStart + rowIdx) * (options.chainHeight + options.chainPadding);

					var gChain = drawChainRectangles(svg, chain, color, options, xScale, y);
					gChain.datum(datum);

					// add tooltip
					var addTooltip = options.chainTipFn;
					addTooltip(gChain);

					// increment chain counter
					count++;

					// TODO also add tooltip for specific rectangles in the group?
				}
			});
		});

		// update global rectangle counter in the end
		_rectCount = count;
	}

	/**
	 * Draws a group of rectangles for a specific chain.
	 *
	 * @param svg       svg element (D3)
	 * @param chain     a PdbChainModel instance
	 * @param color     rectangle color
	 * @param options   visual options object
	 * @param xScale    scale function for the x-axis
	 * @param y         y coordinate of the rectangle group
	 * @return {object} group for the chain (svg element)
	 */
	function drawChainRectangles(svg, chain, color, options, xScale, y)
	{
		var gChain = svg.append("g").attr("class", "pdb-chain-group");
		var height = options.chainHeight;
		var segmentor = new MergedAlignmentSegmentor(chain.mergedAlignment);

		while (segmentor.hasNextSegment())
		{
			var segment = segmentor.getNextSegment();

			// do not draw gaps at all
			if (segment.type == PdbDataUtil.ALIGNMENT_GAP)
			{
				continue;
			}

			var width = Math.abs(xScale(segment.start) - xScale(segment.end));

			var x = xScale(segment.start);

			var rect = gChain.append('rect')
				.attr('fill', color)
				.attr('opacity', chain.mergedAlignment.score)
				.attr('x', x)
				.attr('y', y)
				.attr('width', width)
				.attr('height', height);
		}

		return gChain;
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
			.attr("class", "pdb-panel-y-axis-label")
			.attr("transform", "rotate(270, " + x + "," + y +")")
			.style("font-family", options.labelYFont)
			.style("font-size", options.labelYFontSize)
			.style("font-weight", options.labelYFontWeight)
			.text(options.labelY);

		return label;
	}

	/**
	 * Create row data by allocating position for each chain.
	 * A row may have multiple chains if there is no overlap
	 * between chains.
	 *
	 * @param chainData an array of <pdb id, PdbChainModel> pairs
	 * @return {Array}  a 2D array of chain allocation
	 */
	function allocateRows(chainData)
	{
		var rows = [];

		_.each(chainData, function(datum, idx) {
			var chain = datum.chain;

			if (chain.alignments.length > 0)
			{
				var inserted = false;

				// find the first available row for this chain
				for (var i=0; i < rows.length; i++)
				{
					var row = rows[i];
					var conflict = false;

					// check for conflict for this row
					for (var j=0; j < row.length; j++)
					{
						if (overlaps(chain, row[j].chain))
						{
							// set the flag, and break the loop
							conflict = true;
							break;
						}
					}

					// if there is space available in this row,
					// insert the chain into the current row
					if (!conflict)
					{
						// insert the chain, set the flag, and break the loop
						row.push(datum);
						inserted = true;
						break;
					}
				}

				// if no there is no available space in any row,
				// then insert the chain to the next row
				if (!inserted)
				{
					var newAllocation = [];
					newAllocation.push(datum);
					rows.push(newAllocation);
				}
			}
		});

		return rows;
	}

	/**
	 * Returns the chain datum (<pdb id, PdbChainModel> pair)
	 * for the default chain.
	 *
	 * @return chain datum for the default chain.
	 */
	function getDefaultChainDatum()
	{
		var datum = null;

		if (_rowData)
		{
			// TODO return the left most chain instead?
			// ...i.e: min uniprotFrom in _rowData[0]
			datum = _rowData[0][0];
		}

		return datum;
	}

	/**
	 * Checks if the given two chain alignments (positions) overlaps
	 * with each other.
	 *
	 * @param chain1    first chain
	 * @param chain2    second chain
	 * @return {boolean}    true if intersects, false if distinct
	 */
	function overlaps(chain1, chain2)
	{
		var overlap = true;

		if (chain1.mergedAlignment.uniprotFrom >= chain2.mergedAlignment.uniprotTo ||
		    chain2.mergedAlignment.uniprotFrom >= chain1.mergedAlignment.uniprotTo)
		{
			// no conflict
			overlap = false;
		}

		return overlap;
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
	 * Calculates the max expansion level for the given data.
	 *
	 * @param totalNumRows      total number of rows
	 * @param expansionLevels   expansion level array
	 *                          (number of rows to be displayed for each level)
	 * @return {number}     max level for the current data
	 */
	function calcMaxExpansionLevel(totalNumRows, expansionLevels)
	{
		var max = 0;

		while (expansionLevels.length > max &&
		       totalNumRows > expansionLevels[max])
		{
			max++;
		}

		// base level should be 1
		return max + 1;
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
		var rowCount = _rowData.length;

		// if not auto, then just copy the value
		if (elHeight != "auto")
		{
			height = elHeight;
		}
		else
		{
			height = _options.marginTop + _options.marginBottom +
				rowCount * (_options.chainHeight + _options.chainPadding);
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
		var rowCount = _rowData.length;

		if (maxChain < rowCount)
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
		// generate row data (one row may contain more than one chain)
		_rowData = allocateRows(PdbDataUtil.getSortedChainData(data));
		_maxExpansionLevel = calcMaxExpansionLevel(_rowData.length, _options.numRows);

		// init svg container
		var container = d3.select(_options.el);

		// number of rows to be shown initially
		var numRows = _options.numRows[0];

		// create svg element & update its reference
		var svg = createSvg(container,
		                    _options.elWidth,
		                    calcCollapsedHeight(numRows));

		_svg = svg;

		// (partially) draw the panel
		drawPanel(svg, _options, _rowData.slice(0, numRows), xScale, 0);
		_levelDrawn[0] = true;

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

		// save the listener for future reference
		if (_listeners[selector] == null)
		{
			_listeners[selector] = {};
		}

		_listeners[selector][event] = handler;
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

		// remove listener from the map
		if (_listeners[selector] &&
		    _listeners[selector][event])
		{
			delete _listeners[selector][event];
		}
	}

	/**
	 * Reapplies current listeners to the diagram. This function should be
	 * called while adding new diagram elements after initialization.
	 */
	function reapplyListeners()
	{
		for (var selector in _listeners)
		{
			var target = _svg.selectAll(selector);

			for (var event in _listeners[selector])
			{
				target.on(event, _listeners[selector][event]);
			}
		}
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
	function resizePanel(index)
	{
		// resize to collapsed height
		var collapsedHeight = calcCollapsedHeight(_options.numRows[index]);
		_svg.transition().duration(1000).attr("height", collapsedHeight);
	}

	/**
	 * Resizes the panel to its full height (to show all chains).
	 */
	function expandPanel()
	{
		// resize to full size
		var fullHeight = calcHeight(_options.elHeight);
		_svg.transition().duration(1000).attr("height", fullHeight);
	}

	/**
	 * Expands/Collapses the panel.
	 */
	function toggleHeight()
	{
		// do not try to draw any further levels than max level
		// (no rectangle to draw beyond max level)
		var nextLevel = (_expansion + 1) % _maxExpansionLevel;

		// draw the rectangles if not drawn yet
		if (!_levelDrawn[nextLevel])
		{
			// draw rectangles for the next level
			drawPanel(_svg,
			          _options,
			          _rowData.slice(_options.numRows[_expansion], _options.numRows[nextLevel]),
			          xScale,
			          _options.numRows[_expansion]);

			// also reapply the listeners for the new elements
			reapplyListeners();

			// mark the indicator for the next level
			_levelDrawn[nextLevel] = true;
		}

		// update expansion level
		_expansion = nextLevel;

		// resize panel
		resizePanel(nextLevel);
	}

	/**
	 * Checks if there are more chains (more rows) to show. This function
	 * returns true if the number of total rows exceeds the initial number
	 * of rows to be displayed (which is determined by numRows option).
	 *
	 * @return {boolean} true if there are more rows to show, false otherwise
	 */
	function hasMoreChains()
	{
		return (_rowData.length > _options.numRows[0]);
	}

	return {init: init,
		addListener: addListener,
		removeListener: removeListener,
		getDefaultChainDatum: getDefaultChainDatum,
		show: showPanel,
		hide: hidePanel,
		toggleHeight: toggleHeight,
		hasMoreChains: hasMoreChains};
}

