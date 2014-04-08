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
		maxHeight: 200,     // max height of the container
		numRows: [Infinity], // number of rows to be to be displayed for each expand request
		marginLeft: 45,     // left margin
		marginRight: 30,    // right margin
		marginTop: 2,       // top margin
		marginBottom: 0,    // bottom margin
		chainHeight: 6,     // height of a rectangle representing a single pdb chain
		chainPadding: 3,    // padding between chain rectangles
		labelY: ["PDB", "Chains"],  // label of the y-axis. use array for multi lines, false: "do not draw"
		labelYFont: "sans-serif",   // font type of the y-axis label
		labelYFontColor: "#2E3436", // font color of the y-axis label
		labelYFontSize: "12px",     // font size of y-axis label
		labelYFontWeight: "normal", // font weight of y-axis label
		labelYPaddingRightH: 45, // padding between y-axis and its label (horizontal alignment)
		labelYPaddingTopH: 7,    // padding between y-axis and its label (horizontal alignment)
		labelYPaddingRightV: 25, // padding between y-axis and its label (vertical alignment)
		labelYPaddingTopV: 20,   // padding between y-axis and its label (vertical alignment)
		labelAlignThreshold: 5,  // threshold to determine horizontal or vertical alignment
		chainBorderColor: "#666666", // border color of the chain rectangles
		chainBorderWidth: 0.5,       // border width of the chain rectangles
		highlightBorderColor: "#FF9900", // color of the highlight rect border
		highlightBorderWidth: 2.0,       // width of the highlight rect border
		colors: ["#3366cc"],  // rectangle colors
		animationDuration: 1000, // transition duration (in ms) used for resize animations
		/**
		 * Default chain tooltip function.
		 *
		 * @param element   target svg element (rectangle)
		 */
		chainTipFn: function (element) {
			var datum = element.datum();

			proxy.getPdbInfo(datum.pdbId, function(pdbInfo) {
				var summary = null;

				if (pdbInfo)
				{
					summary = PdbDataUtil.generatePdbInfoSummary(
						pdbInfo[datum.pdbId], datum.chain.chainId);
				}

				// init tip view
				var tipView = new PdbChainTipView({model: {
					pdbId: datum.pdbId,
					pdbInfo: summary.title,
					molInfo: summary.molecule,
					chain: datum.chain
				}});

				var content = tipView.compileTemplate();

				var options = {content: {text: content},
					hide: {fixed: true, delay: 100},
					style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'},
					position: {my:'bottom left', at:'top center',viewport: $(window)}};

				$(element).qtip(options);
			});
		},
		/**
		 * Default y-axis help tooltip function.
		 *
		 * @param element   target svg element (help icon)
		 */
		yHelpTipFn: function (element) {
			var content = _.template(
				$("#mutation_details_pdb_help_tip_template").html());

			var options = {content: {text: content},
				hide: {fixed: true, delay: 100},
				style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow qtip-wide'},
				position: {my:'bottom left', at:'top center',viewport: $(window)}};

			$(element).qtip(options);
		}
	};

	// event listeners
	var _listeners = {};

	// custom event dispatcher
	var _dispatcher = {};
	_.extend(_dispatcher, Backbone.Events);

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

	// <pdbId:chainId> to <chain group (svg element)> map
	var _chainMap = {};

	// <pdbId:chainId> to <row index> map
	var _rowMap = {};

	// previous height before auto collapse
	var _levelHeight = 0;

	// currently highlighted chain
	var _highlighted = null;

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
					_chainMap[PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId)] = gChain;

					// set the first drawn chain as the default chain
					if (_defaultChainGroup == null)
					{
						_defaultChainGroup = gChain;
					}

					// increment chain counter
					count++;
				}
			});
		});

		// update global rectangle counter in the end
		_rectCount = count;

		// add chain tooltips
		addChainTooltips(data, options);
	}

	/**
	 * Adds tooltips to the chain rectangles.
	 *
	 * @param data      row data containing pdb and chain information
	 * @param options   visual options object
	 */
	function addChainTooltips(data, options)
	{
		// this is to prevent chain tooltip functions to send
		// too many separate requests to the server

		var pdbIds = [];
		var chains = [];

		// collect pdb ids and chains
		_.each(data, function(allocation, rowIdx) {
			_.each(allocation, function(datum, idx) {
				pdbIds.push(datum.pdbId);
				chains.push(_chainMap[
					PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId)]);
			});
		});

		// this caches pdb info before adding the tooltips
		proxy.getPdbInfo(pdbIds.join(" "), function(data) {
			// add tooltip to the chain groups
			_.each(chains, function(chain, idx) {
				var addTooltip = options.chainTipFn;
				addTooltip(chain);
			});
		});
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
		var gChain = svg.append("g")
			.attr("class", "pdb-chain-group")
			.attr("opacity", 1);

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

				// store initial position for future use
				// TODO this is not a good way of using datum
				line.datum({initPos: {x: x, y: (y + height/2)}});
			}
			// draw a rectangle for any other segment type
			else
			{
				var rect = gChain.append('rect')
					.attr('fill', color)
					.attr('opacity', chain.mergedAlignment.identityPerc)
					.attr('stroke', options.chainBorderColor)
					.attr('stroke-width', options.chainBorderWidth)
					.attr('x', x)
					.attr('y', y)
					.attr('width', width)
					.attr('height', height);

				// store initial position for future use
				// TODO this is not a good way of using datum
				rect.datum({initPos: {x: x, y: y}});
			}
		}

		return gChain;
	}

	/**
	 * Draws the label of the y-axis.
	 *
	 * @param svg       svg to append the label element
	 * @param options   general options object
	 * @return {object} label group (svg element)
	 */
	function drawYAxisLabel(svg, options)
	{
		// default (vertical) orientation
		var x = options.marginLeft - options.labelYPaddingRightV;
		var y =  options.marginTop + options.labelYPaddingTopV;
		var textAnchor = "middle";
		var rotation = "rotate(270, " + x + "," + y +")";
		var orient = "vertical";

		// horizontal orientation for small number of rows
		if (_rowData.length < options.labelAlignThreshold)
		{
			x = options.marginLeft - options.labelYPaddingRightH;
			y = options.marginTop + options.labelYPaddingTopH;
			textAnchor = "start";
			rotation = "rotate(0, " + x + "," + y +")";
			orient = "horizontal";
		}

		var gLabel = svg.append("g")
			.attr("class", "pdb-panel-y-axis-label-group")
			.attr("opacity", 1);

		// append label
		var label = gLabel.append("text")
			.attr("fill", options.labelYFontColor)
			.attr("text-anchor", textAnchor)
			.attr("x", x)
			.attr("y", y)
			.attr("class", "pdb-panel-y-axis-label")
			.attr("transform", rotation)
			.style("font-family", options.labelYFont)
			.style("font-size", options.labelYFontSize)
			.style("font-weight", options.labelYFontWeight);

		// for an array, create multi-line label
		if (_.isArray(options.labelY))
		{
			_.each(options.labelY, function(text, idx) {
				var dy = (idx == 0) ? 0 : 10;

				// TODO this is an adjustment to fit the help icon image
				var dx = (idx == 0 && orient == "vertical") ? -5 : 0;

				label.append('tspan')
					.attr('x', x + dx)
					.attr('dy', dy).
					text(text);
			});
		}
		// regular string, just set the text
		else
		{
			label.text(options.labelY);
		}

		var help = drawYAxisHelp(gLabel, x, y, orient, options);

		var addTooltip = options.yHelpTipFn;
		addTooltip(help);

		return label;
	}

	/**
	 *
	 * @param svg       svg to append the label element
	 * @param labelX    x coord of y-axis label
	 * @param labelY    y coord of y-axis label
	 * @param orient    orientation of the label (vertical or horizontal)
	 * @param options   general options object
	 * @return {object} help image (svg element)
	 */
	function drawYAxisHelp(svg, labelX, labelY, orient, options)
	{
		// TODO all these values are fine tuned for the label "PDB Chains",
		// ...setting another label text would probably mess things up
		var w = 12;
		var h = 12;
		var x = labelX - w + 2;
		var y = options.marginTop;

		if (orient == "horizontal")
		{
			x = options.marginLeft - w - 5;
			y = labelY - h + 2;
		}

		return svg.append("svg:image")
			.attr("xlink:href", "images/help.png")
			.attr("class", "pdb-panel-y-axis-help")
			.attr("x", x)
			.attr("y", y)
			.attr("width", w)
			.attr("height", h);
	}

	/**
	 * Returns the group svg element for the default chain.
	 *
	 * @return chain group for the default chain.
	 */
	function getDefaultChainGroup()
	{
		return _defaultChainGroup;
	}

	/**
	 * Returns the group svg element for the given pdb id
	 * and chain id pair.
	 *
	 * @param pdbId
	 * @param chainId
	 * @return chain group for the specified chain.
	 */
	function getChainGroup(pdbId, chainId)
	{
		return _chainMap[pdbId + ":" + chainId];
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
	 * @param maxChain  maximum number of rows to be displayed
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
		// TODO get pdbRowData (or uniprot id?) as a model parameter
		// generate row data (one row may contain more than one chain)
		_rowData = PdbDataUtil.allocateChainRows(data);
		_maxExpansionLevel = calcMaxExpansionLevel(_rowData.length, _options.numRows);

		// selecting using jQuery node to support both string and jQuery selector values
		var node = $(_options.el)[0];
		var container = d3.select(node);

		// number of rows to be shown initially
		var numRows = _options.numRows[0];
		_levelHeight = calcCollapsedHeight(numRows);

		// create svg element & update its reference
		var svg = createSvg(container,
		                    _options.elWidth,
		                    _levelHeight);

		_svg = svg;

		// (partially) draw the panel
		drawPanel(svg, _options, _rowData.slice(0, numRows), xScale, 0);
		_levelDrawn[0] = true;

		// draw the labels
		if (_options.labelY != false)
		{
			drawYAxisLabel(svg, _options);
		}

		// build row map
		_rowMap = buildRowMap(_rowData);

		// add default listeners
		addDefaultListeners();
	}

	/**
	 * Builds a map of <pdbId:chainId>, <row index> pairs
	 * for the given row data.
	 *
	 * @param rowData   rows of chain data
	 * @return {Object} <pdbId:chainId> to <row index> map
	 */
	function buildRowMap(rowData)
	{
		var map = {};

		// add a rectangle group for each chain
		_.each(rowData, function(allocation, rowIdx) {
			_.each(allocation, function(datum, idx) {
				map[PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId)] = rowIdx;
			});
		});

		return map;
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

	function addDefaultListeners()
	{
		addListener(".pdb-chain-group", "click", function(datum, index) {
			// highlight the selected chain on the pdb panel
			highlight(d3.select(this));
		});
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
	 *
	 * @param index level index
	 */
	function resizePanel(index)
	{
		// resize to collapsed height
		var collapsedHeight = calcCollapsedHeight(_options.numRows[index]);
		var prevHeight = _svg.attr("height");

		dispatchResizeStartEvent(collapsedHeight, prevHeight);

		_svg.transition()
			.duration(_options.animationDuration)
			.attr("height", collapsedHeight)
			.each("end", function() {
				dispatchResizeEndEvent(collapsedHeight, prevHeight);
			});

		_levelHeight = collapsedHeight;
	}

	/**
	 * Resizes the panel to its full height (to show all chains).
	 */
	function expandPanel()
	{
		// resize to full size
		var fullHeight = calcHeight(_options.elHeight);
		_svg.transition().duration(_options.animationDuration).attr("height", fullHeight);
	}

	/**
	 * Expands/Collapses the panel.
	 */
	function toggleHeight()
	{
		 var nextLevel = drawNextLevel();

		// resize panel
		resizePanel(nextLevel);
	}

	/**
	 * Draws the next level of rectangles.
	 *
	 * @return {Number} next level number
	 */
	function drawNextLevel()
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

		return nextLevel;
	}

	/**
	 * Expands the panel to a specific level.
	 *
	 * @param level
	 */
	function expandToLevel(level)
	{
		var nextLevel = -1;

		// expand until desired level
		for (var i = _expansion;
		     i < level && i < _maxExpansionLevel;
		     i++)
		{
			nextLevel = drawNextLevel();
		}

		// if already expanded (or beyond) that level,
		// no need to update or resize
		if (nextLevel !== -1)
		{
			// resize panel
			resizePanel(nextLevel);
		}
	}

	/**
	 * Expands the panel to the level of the specified chain.
	 *
	 * @param pdbId
	 * @param chainId
	 */
	function expandToChainLevel(pdbId, chainId)
	{
		var chainLevel = -1;
		var chainRow = _rowMap[PdbDataUtil.chainKey(pdbId, chainId)];

		for (var i=0; i < _options.numRows.length; i++)
		{
			if (chainRow < _options.numRows[i])
			{
				chainLevel = i;
				break;
			}
		}

		// TODO chainLevel is beyond the visible levels, expand all?
		if (chainLevel !== -1)
		{
			expandToLevel(chainLevel);
		}
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
		// update the reference
		_highlighted = chainGroup;

		// calculate the bounding box
		var bbox = boundingBox(chainGroup);

		// remove the previous selection rectangle(s)
		_svg.selectAll(".pdb-selection-rectangle-group").remove();
		var gRect = _svg.append('g')
			.attr('class', "pdb-selection-rectangle-group")
			.attr('opacity', 0);

		// add the selection rectangle
		var rect = gRect.append('rect')
			.attr('fill', "none")
			.attr('stroke', _options.highlightBorderColor)
			.attr('stroke-width', _options.highlightBorderWidth)
			.attr('x', bbox.x)
			.attr('y', bbox.y)
			.attr('width', bbox.width)
			.attr('height', bbox.height);

		gRect.transition().duration(_options.animationDuration).attr('opacity', 1);

		// store initial position for future use
		// TODO this is not a good way of using datum
		rect.datum({initPos: {x: bbox.x, y: bbox.y}});

		// ...alternatively we can just use a yellowish color
		// to highlight the whole background

		// trigger corresponding event
		_dispatcher.trigger(
			MutationDetailsEvents.PANEL_CHAIN_SELECTED,
			chainGroup);
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

	/**
	 * Collapses the view to the currently highlighted chain group
	 *
	 * @param callback  function to invoke after the transition
	 */
	function minimizeToHighlighted(callback)
	{
		if (_highlighted != null)
		{
			minimizeToChain(_highlighted, callback);
		}
	}

	/**
	 * Collapses the view to the given chain group by hiding
	 * everything other than the given chain. Also reduces
	 * the size of the diagram to fit only a single row.
	 *
	 * @param chainGroup    chain group (svg element)
	 * @param callback      function to invoke after the transition
	 */
	function minimizeToChain(chainGroup, callback)
	{
		var duration = _options.animationDuration;

		// 3 transitions in parallel:

		// TODO shifting all chains causes problems with multiple transitions
		// 1) shift all chains up, such that selected chain will be on top
		//shiftToChain(chainGroup);

		// 1) reposition the given chain..
		moveToFirstRow(chainGroup, callback);

		//..and the selection rectangle if the chain is highlighted
		if (chainGroup == _highlighted)
		{
			moveToFirstRow(_svg.selectAll(".pdb-selection-rectangle-group"));
		}

		// 2) fade-out all chains (except selected) and labels
		fadeOutOthers(chainGroup);

		// 3) resize the panel to a single row size
		var collapsedHeight = calcCollapsedHeight(1);
		var prevHeight = _svg.attr("height");

		dispatchResizeStartEvent(collapsedHeight, prevHeight);

		_svg.transition().duration(duration)
			.attr("height", collapsedHeight)
			.each("end", function(){
				dispatchResizeEndEvent(collapsedHeight, prevHeight);
			});
	}

	/**
	 * Shift all the chain rectangles, such that the given chain
	 * will be in the first row.
	 *
	 * @param chainGroup    chain group (svg element)
	 * @param callback      function to invoke after the transition
	 */
	function shiftToChain(chainGroup, callback)
	{
		var duration = _options.animationDuration;
		var datum = chainGroup.datum();
		var key = PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId);
		var chainRow = _rowMap[key];

		// if chains are not at their original positions, then shift value should be different
//		var shift = chainRow * (_options.chainHeight + _options.chainPadding);

		// calculate shift value relative to the current position of the given chain group
		var shift = 0;

		chainGroup.selectAll("rect").each(function(datum, idx) {
			var rect = d3.select(this);
			shift = parseInt(rect.attr("y")) - _options.marginTop;
		});

		var shiftFn = function(target, d, attr) {
			var ele = d3.select(target);
			return (parseInt(ele.attr(attr)) - shift);
		};

		// shift up every chain on the y-axis
		yShiftRect(".pdb-chain-group rect", shiftFn, duration);
		yShiftRect(".pdb-selection-rectangle-group rect", shiftFn, duration);
		yShiftLine(".pdb-chain-group line", shiftFn, duration);

		// TODO it is better to bind this to a d3 transition
		// ..safest way is to call after the selected chain's transition ends
		setTimeout(callback, duration + 50);
	}

	/**
	 * Moves the given chainGroup to the first row.
	 *
	 * @param chainGroup
	 * @param callback
	 */
	function moveToFirstRow(chainGroup, callback)
	{
		var duration = _options.animationDuration;

		// first row coordinates...
		// (we can also use the default chain coordinates)
		var y = _options.marginTop;
		var height = _options.chainHeight;

		// move chain group rectangles and lines
		chainGroup.selectAll("line")
			.transition().duration(duration)
			.attr('y1', y + height/2)
			.attr('y2', y + height/2);

		chainGroup.selectAll("rect")
			.transition().duration(duration)
			.attr('y', y)
			.each("end", function() {
                if (_.isFunction(callback)) {
					callback();
				}
			});
	}

	/**
	 * Fades out all other element except the ones in
	 * the given chain group.
	 *
	 * @param chainGroup    chain group to exclude from fade out
	 * @param callback      function to invoke after the transition
	 */
	function fadeOutOthers(chainGroup, callback)
	{
		var duration = _options.animationDuration;
		var datum = chainGroup.datum();
		var key = PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId);

		// fade out all chain rectangles but the given
		_svg.selectAll(".pdb-chain-group")
			.transition().duration(duration)
			.attr("opacity", function(datum) {
				if (PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId) === key) {
					// do not hide the provided chain
					return 1;
				} else {
					// hide all the others
					return 0;
				}
			});

		// also fade out selection rectangle if the given chain is not selected
		_svg.selectAll(".pdb-selection-rectangle-group")
			.transition().duration(duration)
			.attr("opacity", function(datum) {
				if (_highlighted == chainGroup) {
					// do not hide the selection rectangle
					return 1;
				} else {
					// hide the selection rectangle
					return 0;
				}
			});

		_svg.select(".pdb-panel-y-axis-label-group")
			.transition().duration(duration)
			.attr("opacity", 0)
			.each("end", function() {
				if (_.isFunction(callback)) {
					callback();
				}
			});
	}

	function yShiftLine(selector, shiftFn, duration)
	{
		_svg.selectAll(selector)
			.transition().duration(duration)
			.attr("y1", function(d) {
				return shiftFn(this, d, "y1");
			})
			.attr("y2", function(d) {
				return shiftFn(this, d, "y2");
			});
	}

	function yShiftRect(selector, shiftFn, duration)
	{
		_svg.selectAll(selector)
			.transition().duration(duration)
			.attr("y", function(d) {
				return shiftFn(this, d, "y");
			});
	}

	/**
	 * Reverses the changes back to the state before calling
	 * the minimizeToChain function.
	 *
	 * @param callback  function to invoke after the transition
	 */
	function restoreToFull(callback)
	{
		var duration = _options.animationDuration;

		// put everything back to its original position
		restoreChainPositions();

		// fade-in hidden elements
		fadeInAll();

		var prevHeight = _svg.attr("height");

		dispatchResizeStartEvent(_levelHeight, prevHeight);

		// restore to previous height
		_svg.transition().duration(duration)
			.attr("height", _levelHeight)
			.each("end", function(){
				if (_.isFunction(callback)) {
					callback();
				}
				dispatchResizeEndEvent(_levelHeight, prevHeight);
			});
	}

	/**
	 * Restores all chains back to their initial positions.
	 */
	function restoreChainPositions(callback)
	{
		var duration = _options.animationDuration;

		var shiftFn = function(target, d, attr) {
			return d.initPos.y;
		};

		// put everything back to its original position
		yShiftRect(".pdb-chain-group rect", shiftFn, duration);
		yShiftLine(".pdb-chain-group line", shiftFn, duration);
		yShiftRect(".pdb-selection-rectangle-group rect", shiftFn, duration);

		// TODO it is better to bind this to a d3 transition
		// ..safest way is to call after the selected chain's transition ends
		setTimeout(callback, duration + 50);
	}

	/**
	 * Fades in all hidden components.
	 */
	function fadeInAll(callback)
	{
		var duration = _options.animationDuration;

		// fade-in hidden elements

		_svg.selectAll(".pdb-chain-group")
			.transition().duration(duration)
			.attr("opacity", 1);

		_svg.selectAll(".pdb-selection-rectangle-group")
			.transition().duration(duration)
			.attr("opacity", 1);

		_svg.selectAll(".pdb-panel-y-axis-label-group")
			.transition().duration(duration)
			.attr("opacity", 1)
			.each("end", function(){
				if (_.isFunction(callback)) {
					callback();
				}
			});
	}

	function getHighlighted()
	{
		return _highlighted;
	}

	function dispatchResizeStartEvent(newHeight, prevHeight)
	{
		_dispatcher.trigger(
			MutationDetailsEvents.PDB_PANEL_RESIZE_STARTED,
			newHeight, prevHeight, _options.maxHeight);
	}

	function dispatchResizeEndEvent(newHeight, prevHeight)
	{
		_dispatcher.trigger(
			MutationDetailsEvents.PDB_PANEL_RESIZE_ENDED,
			newHeight, prevHeight, _options.maxHeight);
	}

	return {init: init,
		addListener: addListener,
		removeListener: removeListener,
		getChainGroup: getChainGroup,
		getDefaultChainGroup: getDefaultChainGroup,
		show: showPanel,
		hide: hidePanel,
		toggleHeight: toggleHeight,
		expandToChainLevel: expandToChainLevel,
		minimizeToChain: minimizeToChain,
		minimizeToHighlighted: minimizeToHighlighted,
		restoreToFull: restoreToFull,
		restoreChainPositions: restoreChainPositions,
		fadeInAll: fadeInAll,
		hasMoreChains: hasMoreChains,
		highlight: highlight,
		getHighlighted: getHighlighted,
		dispatcher: _dispatcher};
}

