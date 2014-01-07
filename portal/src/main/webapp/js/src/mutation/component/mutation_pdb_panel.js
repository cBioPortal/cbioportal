/**
 * Constructor for the MutationPdbPanel class.
 *
 * @param options   visual options object
 * @param data      PDB data (collection of PdbModel instances)
 * @param proxy     PDB data proxy
 * @param xScale    scale function for the x axis
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationPdbPanel(options, data, proxy, xScale)
{
	/**
	 * Default visual options.
	 */
	var _defaultOpts = {
		el: "#mutation_pdb_panel_d3", // id of the container
		elWidth: 740,       // width of the container
		elHeight: "auto",   // height of the container
		numRows: [5, 10, 20], // number of rows to be to be displayed for each expand request
		marginLeft: 45,     // left margin
		marginRight: 30,    // right margin
		marginTop: 2,       // top margin
		marginBottom: 0,    // bottom margin
		chainHeight: 6,     // height of a rectangle representing a single pdb chain
		chainPadding: 3,    // padding between chain rectangles
		labelY: "Chains",   // informative label of the y-axis (false means "do not draw")
		labelYFont: "sans-serif",   // font type of the y-axis label
		labelYFontColor: "#2E3436", // font color of the y-axis label
		labelYFontSize: "12px",     // font size of y-axis label
		labelYFontWeight: "normal", // font weight of y-axis label
		labelYPaddingRightH: 45, // padding between y-axis and its label (horizontal alignment)
		labelYPaddingTopH: 7,    // padding between y-axis and its label (horizontal alignment)
		labelYPaddingRightV: 25, // padding between y-axis and its label (vertical alignment)
		labelYPaddingTopV: 20,   // padding between y-axis and its label (vertical alignment)
		chainBorderColor: "#666666", // border color of the chain rectangles
		chainBorderWidth: 0.5,       // border width of the chain rectangles
		highlightBorderColor: "#FF9900", // color of the highlight rect border
		highlightBorderWidth: 2.0,       // width of the highlight rect border
		colors: ["#3366cc"],  // rectangle colors
		/**
		 * Default chain tooltip function.
		 *
		 * @param element   target svg element (rectangle)
		 */
		chainTipFn: function (element) {
			var datum = element.datum();

			proxy.getPdbInfo(datum.pdbId, function(pdbInfo) {

				// init tip view
				var tipView = new PdbChainTipView({model: {
					pdbId: datum.pdbId,
					pdbInfo: pdbInfo,
					chain: datum.chain
				}});

				var content = tipView.compileTemplate();

				var options = {content: {text: content},
					hide: {fixed: true, delay: 100},
					style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'},
					position: {my:'bottom left', at:'top center'}};

				$(element).qtip(options);
			});
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

	// default chain group svg element
	var _defaultChainGroup = null;

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
					var color = options.colors[idx % options.colors.length];
					//datum.color = color;

					var y = options.marginTop +
					        (rowStart + rowIdx) * (options.chainHeight + options.chainPadding);

					var gChain = drawChainRectangles(svg, chain, color, options, xScale, y);
					gChain.datum(datum);

					// set the first drawn chain as the default chain
					if (_defaultChainGroup == null)
					{
						_defaultChainGroup = gChain;
					}

					// add tooltip
					var addTooltip = options.chainTipFn;
					addTooltip(gChain);

					// increment chain counter
					count++;
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

		// init the segmentor for the merged alignment object
		var segmentor = new MergedAlignmentSegmentor(chain.mergedAlignment);

		// iterate all segments for this merged alignment
		while (segmentor.hasNextSegment())
		{
			var segment = segmentor.getNextSegment();

			var width = Math.abs(xScale(segment.start) - xScale(segment.end));
			var x = xScale(segment.start);

			// draw a line (instead of a rectangle) for an alignment gap
			if (segment.type == PdbDataUtil.ALIGNMENT_GAP)
			{
				var line = gChain.append('line')
					.attr('stroke', options.chainBorderColor)
					.attr('stroke-width', options.chainBorderWidth)
					.attr('x1', x)
					.attr('y1', y + height/2)
					.attr('x2', x + width)
					.attr('y2', y + height/2);
			}
			// draw a rectangle for any other segment type
			else
			{
				var rect = gChain.append('rect')
					.attr('fill', color)
					.attr('opacity', chain.mergedAlignment.score)
					.attr('stroke', options.chainBorderColor)
					.attr('stroke-width', options.chainBorderWidth)
					.attr('x', x)
					.attr('y', y)
					.attr('width', width)
					.attr('height', height);
			}
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
		// default (vertical) orientation
		var x = options.marginLeft - options.labelYPaddingRightV;
		var y =  options.marginTop + options.labelYPaddingTopV;
		var textAnchor = "middle";
		var rotation = "rotate(270, " + x + "," + y +")";

		// horizontal orientation for small number of rows
		if (_rowData.length < options.numRows[0])
		{
			x = options.marginLeft - options.labelYPaddingRightH;
			y = options.marginTop + options.labelYPaddingTopH;
			textAnchor = "start";
			rotation = "rotate(0, " + x + "," + y +")";
		}

		// append label
		var label = svg.append("text")
			.attr("fill", options.labelYFontColor)
			.attr("text-anchor", textAnchor)
			.attr("x", x)
			.attr("y", y)
			.attr("class", "pdb-panel-y-axis-label")
			.attr("transform", rotation)
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

				// if there is no available space in any row,
				// then insert the chain to the next row
				if (!inserted)
				{
					var newAllocation = [];
					newAllocation.push(datum);
					rows.push(newAllocation);
				}
			}
		});

		// sort alignments in each row by start position (lowest comes first)
//		_.each(rows, function(allocation, idx) {
//			allocation.sort(function(a, b){
//				return (a.chain.mergedAlignment.uniprotFrom -
//				        b.chain.mergedAlignment.uniprotFrom);
//			});
//		});

		return rows;
	}

	/**
	 * Returns the group svg element for the default chain.
	 *
	 * @return chain datum for the default chain.
	 */
	function getDefaultChainGroup()
	{
		return _defaultChainGroup;
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
		var max = -1;

		// try to find the first value within the level array
		// which is bigger than the total number of rows
		for (var i=0; i < expansionLevels.length; i++)
		{

			if (expansionLevels[i] > totalNumRows)
			{
				max = i;
				break;
			}
		}

		// if the total number of rows is bigger than all values
		// than max should be the highest available level
		if (max == -1)
		{
			max = expansionLevels.length - 1;
		}

		return max;
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
				rowCount * (_options.chainHeight + _options.chainPadding) -
				(_options.chainPadding / 2); // no need for the full padding for the last row
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
				maxChain * (_options.chainHeight + _options.chainPadding) -
				(_options.chainPadding / 2); // no need for full padding for the last row
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
		var nextLevel = (_expansion + 1) % (_maxExpansionLevel + 1);

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

	/**
	 * Highlights a group of chain rectangles by drawing an outline
	 * border around the bounding box of all group elements.
	 *
	 * @param chainGroup    a group of rectangles representing the pdb chain
	 */
	function highlight(chainGroup)
	{
		// calculate the bounding box
		var bbox = boundingBox(chainGroup);

		// remove the previous selection rectangle(s)
		_svg.selectAll(".pdb-selection-rectangle").remove();

		// add the selection rectangle
		var rect = _svg.append('rect')
			.attr('class', "pdb-selection-rectangle")
			.attr('fill', "none")
			.attr('stroke', _options.highlightBorderColor)
			.attr('stroke-width', _options.highlightBorderWidth)
			.attr('x', bbox.x)
			.attr('y', bbox.y)
			.attr('width', bbox.width)
			.attr('height', bbox.height);

		// ...alternatively we can just use a yellowish color
		// to highlight the whole background
	}

	function boundingBox(rectGroup)
	{
		var left = Infinity;
		var right = -1;
		var y = -1;
		var height = -1;

		rectGroup.selectAll("rect").each(function(datum, idx) {
			var rect = d3.select(this);
			// assuming height and y are the same for all rects
			y = parseFloat(rect.attr("y"));
			height = parseFloat(rect.attr("height"));

			var x = parseFloat(rect.attr("x"));
			var width = parseFloat(rect.attr("width"));

			if (x < left)
			{
				left = x;
			}

			if (x + width > right)
			{
				right = x + width;
			}
		});

		return {x: left,
			y: y,
			width: right - left,
			height: height};
	}

	return {init: init,
		addListener: addListener,
		removeListener: removeListener,
		getDefaultChainGroup: getDefaultChainGroup,
		show: showPanel,
		hide: hidePanel,
		toggleHeight: toggleHeight,
		hasMoreChains: hasMoreChains,
		highlight: highlight};
}

