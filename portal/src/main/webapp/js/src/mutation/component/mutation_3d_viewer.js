/**
 * 3D Mutation Visualizer, currently built on Jmol/JSmol lib.
 *
 * @param name      name of the visualizer (applet/application name)
 * @param options   visualization (Jmol) options
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dVis = function(name, options)
{
	// main container -- html element
	var _container = null;

	// actual 3D application wrapper
	var _3dApp = null;

	// flag to indicate panel size minimization
	var _minimized = false;

	// current selection (mutation positions as Jmol script compatible strings)
	// this is a map of <color, position array> pairs
	var _selection = null;

	// map of mutation ids to corresponding residue positions
	var _highlighted = {};

	// current chain (PdbChainModel instance)
	var _chain = null;

	// current pdb id
	var _pdbId = null;

	// spin indicator (initially off)
	var _spin = "OFF";

	// used for show/hide option (workaround)
	var _prevTop = null;

	// default visualization options
	var defaultOpts = {
		// applet/application (Jmol/JSmol) options
		appOptions: {
			width: 400,
			height: 300,
			debug: false,
			color: "white"
		},
		proteinScheme: "cartoon", // default style of the protein structure
		restrictProtein: false, // restrict to protein only (hide other atoms)
		defaultColor: "xDDDDDD", // default color of the whole structure
		structureColors: { // default colors for special structures
			alphaHelix: "xFFA500",
			betaSheet: "x0000FF",
			loop: "xDDDDDD"
		}, // structure color takes effect only when corresponding flag is set
		defaultTranslucency: 5, // translucency (opacity) of the whole structure
		chainColor: "x888888", // color of the selected chain
		chainTranslucency: 0, // translucency (opacity) of the selected chain
		colorProteins: "uniform", // "uniform": single color, effective for all schemes
		                          // "bySecondaryStructure": not effective for space-filling scheme
		                          // "byAtomType": effective only for space-filling scheme
		                          // "byChain": not effective for space-filling scheme
		colorMutations: "byMutationType", // "byMutationType": use mutation colors for type
		                                  // "uniform": use a single color
		                                  // "none": do not color (use default atom colors)
		mutationColor: "x8A2BE2",  // uniform color of the mutated residues
		highlightColor: "xFFDD00", // color of the user-selected mutations
		displaySideChain: "highlighted", // highlighted: display side chain for only selected mutations
		                                 // all: display side chain for all mapped mutations
		                                 // none: do not display side chain atoms
		defaultZoom: 100, // default (unfocused) zoom level
		focusZoom: 250, // focused zoom level
		containerPadding: 10, // padding for the vis container (this is to prevent overlapping)
		// TODO minimized length is actually depends on padding values, it might be better to calculate it
		minimizedHeight: 10, // minimized height of the container (assuming this will hide everything but the title)
		// color mapper function for mutations
		mutationColorMapper: function (mutationId, pdbId, chain) {
			return "xFF0000"; // just return the default color for all
		}
	};

	// Predefined style scripts for Jmol
	var _styleScripts = {
		ballAndStick: "wireframe ONLY; wireframe 0.15; spacefill 20%;",
		spaceFilling: "spacefill ONLY; spacefill 100%;",
		ribbon: "ribbon ONLY;",
		cartoon: "cartoon ONLY;",
		trace: "trace ONLY;"
	};

	var _options = jQuery.extend(true, {}, defaultOpts, options);

	/**
	 * Initializes the visualizer.
	 */
	function init()
	{
		// TODO parametrize this initialization?

		if (cbio.util.browser.msie)
		{
			// TODO workaround: using Jmol in IE for now
			// JSmol cannot retrieve data from an external DB in IE
			// (it needs a server side component to do this)
			_3dApp = new JmolWrapper();
		}
		else
		{
			_3dApp = new JSmolWrapper();
		}

		// init app
		_3dApp.init(name, _options.appOptions);
	}

	/**
	 * Updates visualizer container.
	 *
	 * @param container html element
	 */
	function updateContainer(container)
	{
		// update reference
		_container = $(container);

		var appContainer = _container.find(".mutation-3d-vis-container");

		// set width
		appContainer.css("width", _options.appOptions.width);
		// set height (should be slightly bigger than the app height)
		appContainer.css("height", _options.appOptions.height + _options.containerPadding);
		// update app container
		_3dApp.updateContainer(appContainer);
	}

	/**
	 * Toggles the spin.
	 */
	function toggleSpin()
	{
		_spin == "ON" ? _spin = "OFF" : _spin = "ON";

		var script = "spin " + _spin + ";";

		_3dApp.script(script);
	}

	/**
	 * Reapply the visual style for the current options.
	 */
	function reapplyStyle()
	{
//		var script = "select all;" +
//		             _styleScripts[style];
		// regenerate visual style script
		var script = generateVisualStyleScript(_selection, _chain);

		// regenerate highlight script
		script = script.concat(generateHighlightScript(_highlighted));

		// convert array to a single string
		script = script.join(" ");
		_3dApp.script(script);
	}

	/**
	 * Shows the visualizer panel.
	 */
	function show()
	{
		if (_container != null)
		{
			_container.show();

			// this is a workaround. see the hide() function below for details

			var currentTop = parseInt(_container.css('top'));

			// update the top position only if it is negative
			if (currentTop < 0)
			{
				if (_prevTop != null && _prevTop > 0)
				{
					_container.css('top', _prevTop);
				}
				else
				{
					_container.css('top', 0);
				}
			}
		}
	}

	/**
	 * Hides the visualizer panel.
	 */
	function hide()
	{
		// TODO jQuery.hide function is problematic after Jmol init
		// Reloading the PDB data throws an error message (Error: Bad NPObject as private data!)
		// see https://code.google.com/p/gdata-issues/issues/detail?id=4820

		// So, the current workaround is to reposition instead of hiding
		if (_container != null)
		{
			//_container.hide();
			var currentTop = parseInt(_container.css('top'));

			if (currentTop > 0)
			{
				_prevTop = currentTop;
			}

			_container.css('top', -9999);
		}
	}

	/**
	 * Minimizes the container (only title will be shown)
	 */
	function minimize()
	{
		// minimize container
		if (_container != null)
		{
			_container.css({"overflow": "hidden",
				"height": _options.minimizedHeight});
			_minimized = true;
		}
	}

	/**
	 * Maximizes the container to its full height
	 */
	function maximize()
	{
		if (_container != null)
		{
			_container.css({"overflow": "", "height": ""});
			_minimized = false;
		}
	}

	function toggleSize()
	{
		if (_container != null)
		{
			if(_minimized)
			{
				maximize();
			}
			else
			{
				minimize();
			}
		}
	}

	function isVisible()
	{
		var top = parseInt(_container.css("top"));

		var hidden = (top == -9999) || _container.is(":hidden");

		return !hidden;
	}

	/**
	 * Reloads the protein view for the given PDB id and the chain.
	 *
	 * This function returns an array of mapping mutations (residues).
	 * If there is no mapping residue for currently visible mutations on
	 * the diagram, then function returns false. Note that this function
	 * returns without waiting the callback function to be invoked.
	 *
	 * @param pdbId     PDB id
	 * @param chain     PdbChainModel instance
	 * @param callback  function to call after reload
	 * @return  {Array} array of mapped mutation ids
	 */
	function reload(pdbId, chain, callback)
	{
		var mappedMutations = [];

		// reset highlights
		_highlighted = {};

		// pdbId and/or chainId may be null
		if (!pdbId || !chain)
		{
			// nothing to load
			return mappedMutations;
		}

		// save current pdb id & chain for a possible future access
		_chain = chain;
		_pdbId = pdbId;

		// update selection map
		mappedMutations = updateSelectionMap(pdbId, chain);

		// construct Jmol script string
		var script = [];

		script.push("load=" + pdbId + ";"); // load the corresponding pdb
		script = script.concat(generateVisualStyleScript(_selection, _chain));

		// TODO spin is currently disabled...
		//script.push("spin " + _spin + ";");

		// convert array into a string (to pass to Jmol)
		script = script.join(" ");

		// run script
		_3dApp.script(script, callback);

		return mappedMutations;
	}

	/**
	 * Refreshes the view without reloading the pdb structure.
	 * Using this function instead of reload makes things
	 * a lot faster for filtering operations.
	 *
	 * @return  {Array} array of mapped mutation ids
	 */
	function refresh()
	{
		var mappedMutations = [];

		// reset highlights
		_highlighted = {};

		// pdbId and/or chainId may be null
		if (_pdbId == null || _chain == null)
		{
			// nothing to refresh
			return mappedMutations;
		}

		// update selection map
		mappedMutations = updateSelectionMap(_pdbId, _chain);

		var script = [];

		// update visual style by using the updated selection map
		script = script.concat(generateVisualStyleScript(_selection, _chain));

		// convert array into a string (to pass to Jmol)
		script = script.join(" ");

		// run script
		_3dApp.script(script);

		return mappedMutations;
	}

	/**
	 * Updates the selection map for the current pdbId and chain
	 * by using the corresponding color mapper function.
	 *
	 * @param pdbId     PDB id
	 * @param chain     PdbChainModel instance
	 * @return {Array}  array of mapped mutation ids
	 */
	function updateSelectionMap(pdbId, chain)
	{
		var selection = {};
		var mappedMutations = [];
		var color = _options.mutationColor;

		// update the residue selection map wrt mutation color mapper
		for (var mutationId in chain.positionMap)
		{
			var position = chain.positionMap[mutationId];

			if (_.isFunction(_options.mutationColorMapper))
			{
				color = _options.mutationColorMapper(mutationId, pdbId, chain);
			}

			if (color == null)
			{
				//color = defaultOpts.mutationColor;

				// do not color at all, this automatically hides user-filtered mutations
				// TODO but this also hides unmapped mutations (if any)
				continue;
			}

			if (selection[color] == null)
			{
				// using an object instead of an array (to avoid duplicates)
				selection[color] = {};
			}

			var scriptPos = generateScriptPos(position);
			selection[color][scriptPos] = (scriptPos + ":" + chain.chainId);
			mappedMutations.push(mutationId);
		}

		// convert maps to arrays
		_.each(selection, function(value, key, list) {
			// key is a "color"
			// value is a "position script string" map
			list[key] = _.values(value);
		});

		// update selection for a possible future restore
		_selection = selection;

		return mappedMutations;
	}

	/**
	 * Centers the view onto the currently highlighted residue.
	 *
	 * @return {boolean} true if center successful, false otherwise
	 */
	function centerOnHighlighted()
	{
		// perform action if there is only one highlighted position
		if (_.size(_highlighted) != 1)
		{
			return false;
		}

		var script = [];

		_.each(_highlighted, function (position) {
			script = script.concat(generateCenterScript(position));
		});

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);

		return true;
	}

	/**
	 * Resets the current center to the default position.
	 */
	function resetCenter()
	{
		var script = [];

		// center to default position
		script.push("center;");

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);
	}

	/**
	 * Focuses on the residue corresponding to the given pileup. If there is
	 * no corresponding residue for the given pileup, this function does not
	 * perform a focus operation, and returns false.
	 *
	 * @param pileup    Pileup instance
	 * @return {boolean}    true if there there a matching residue, false o.w.
	 */
	function focus(pileup)
	{
		// no chain selected yet, terminate
		if (_chain == null)
		{
			return false;
		}

		// assuming all other mutations in the same pileup have
		// the same (or very close) mutation position.
		var id = pileup.mutations[0].mutationId;

		// get script
		var script = generateFocusScript(id);
		//script = script.concat(generateHighlightScript(id));

		// check if the script is valid
		if (script.length > 0)
		{
			// convert array to a single string
			script = script.join(" ");

			// send script string to the app
			_3dApp.script(script);
		}
		// no mapping position for this mutation on this chain
		else
		{
			// just reset focus
			resetFocus();
			return false;
		}

		return true;
	}

	/**
	 * Resets the current focus to the default position and zoom level.
	 */
	function resetFocus()
	{
		// zoom out to default zoom level, center to default position,
		// and remove all selection highlights
		var script = [];
		script.push("zoom " + _options.defaultZoom + ";"); // zoom to default zoom level
		script.push("center;"); // center to default position

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);
	}

	/**
	 * Highlights the residue corresponding to the given pileups. This
	 * function returns the number of successfully mapped residues
	 * for the given pileups (returns zero if no mapping at all).
	 *
	 * @param pileups   an array of Pileup instances
	 * @param reset     indicates whether to reset previous highlights
	 * @return {Number} number of mapped pileups (residues)
	 */
	function highlight(pileups, reset)
	{
		// no chain selected yet, terminate
		if (_chain == null)
		{
			return 0;
		}

		if (reset)
		{
			// reset all previous highlights
			_highlighted = {};
		}

		// init script generation
		var script = generateVisualStyleScript(_selection, _chain);

		var numMapped = 0;

		_.each(pileups, function(pileup, i) {
			// assuming all other mutations in the same pileup have
			// the same (or very close) mutation position.
			var id = pileup.mutations[0].mutationId;
			var position = _chain.positionMap[id];

			if (position != null)
			{
				// add position to the highlighted ones
				_highlighted[id] = position;
				numMapped++;
			}
		});

		// add highlight script string
		script = script.concat(generateHighlightScript(_highlighted));

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);

		// return number of mapped residues for the given pileups
		return numMapped;
	}

	/**
	 * Refreshes the current highlights.
	 */
	function refreshHighlight()
	{
		var script = generateHighlightScript(_highlighted);

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);
	}

	/**
	 * Remove all highlights.
	 */
	function resetHighlight()
	{
		// reset highlight map
		_highlighted = {};

		// remove all selection highlights
		var script = generateVisualStyleScript(_selection, _chain);

		// convert array to a single string
		script = script.join(" ");

		_3dApp.script(script);
	}

	/**
	 * Generates the visual style (scheme, coloring, selection, etc.) script
	 * to be sent to the 3D app.
	 *
	 * @return {Array}  script lines as an array
	 */
	function generateVisualStyleScript(selection, chain)
	{
		var script = [];

		script.push("select all;"); // select everything
		script.push(_styleScripts[_options.proteinScheme]); // show selected style view

		// do the initial (uniform) coloring

		script.push("color [" + _options.defaultColor + "];"); // set default color
		//script.push("translucent [" + _options.defaultTranslucency + "];"); // set default opacity
		script.push("select :" + chain.chainId + ";"); // select the chain
		script.push("color [" + _options.chainColor + "];"); // set chain color
		//script.push("translucent [" + _options.chainTranslucency + "];"); // set chain opacity

		// additional coloring for the selected chain
		script.push("select :" + chain.chainId + ";");

		if (_options.colorProteins == "byAtomType")
		{
			script.push("color atoms CPK;");
		}
		else if (_options.colorProteins == "bySecondaryStructure")
		{
			// color secondary structure (for the selected chain)
			script.push("select :" + chain.chainId + " and helix;"); // select alpha helices
			script.push("color [" + _options.structureColors.alphaHelix + "];"); // set color
			script.push("select :" + chain.chainId + " and sheet;"); // select beta sheets
			script.push("color [" + _options.structureColors.betaSheet + "];"); // set color
		}
		else if (_options.colorProteins == "byChain")
		{
			// min atom no within the selected chain
			var rangeMin = "@{{:" + chain.chainId + "}.atomNo.min}";
			// max atom no within the selected chain
			var rangeMax = "@{{:" + chain.chainId + "}.atomNo.max}";

			// max residue no within the selected chain
			//var rangeMin = "@{{:" + chain.chainId + "}.resNo.min}";
			// max residue no within the selected chain
			//var rangeMax = "@{{:" + chain.chainId + "}.resNo.max}";

			// select the chain
			script.push("select :" + chain.chainId + ";");

			// color the chain by rainbow coloring scheme (gradient coloring)
			script.push('color atoms property atomNo "roygb" ' +
			            'range ' + rangeMin + ' ' + rangeMax + ';');
		}

		// process mapped residues
		for (var color in selection)
		{
			script.push("select " + selection[color].join(", ") + ";"); // select positions (mutations)

			// color each residue with a mapped color (this is to sync with diagram colors)

			// use the actual mapped color
			if (_options.colorMutations == "byMutationType")
			{
				// color with corresponding mutation color
				script.push("color [" + color + "];");
			}
			// use a uniform color
			else if (_options.colorMutations == "uniform")
			{
				// color with a uniform mutation color
				script.push("color [" + _options.mutationColor + "];");
			}

			// show/hide side chains
			script = script.concat(
				generateSideChainScript(selection[color],
					_options.displaySideChain == "all"));
		}

		// TODO see if it is possible to set translucency value without specifying a color
		// ...right now ignoring _options.defaultTranslucency and _options.chainTranslucency

		// adjust structure transparency
		script.push("select all;");
		script.push("color translucent;");
		//script.push("color translucent [" + _options.defaultTranslucency + "];");
		script.push("select :" + chain.chainId + ";");
		script.push("color opaque;");

		if (_options.restrictProtein)
		{
			script.push("restrict protein;");
		}

		return script;
	}

	/**
	 * Generates the highlight script to be sent to the 3D app.
	 *
	 * @param positions mutation positions to highlight
	 * @return {Array}  script lines as an array
	 */
	function generateHighlightScript(positions)
	{
		var script = [];
		var displaySideChain = _options.displaySideChain != "none";
		var scriptPositions = [];

		// highlight the selected positions
		if (!_.isEmpty(positions))
		{
			// convert positions to script positions
			_.each(positions, function(position) {
				scriptPositions.push(generateScriptPos(position));
			});

			// add highlight color
			script.push("select (" + scriptPositions.join(", ") + ") and :" + _chain.chainId + ";");
			script.push("color [" + _options.highlightColor + "];");

			// show/hide side chains
			script = script.concat(
				generateSideChainScript(scriptPositions, displaySideChain));
		}

		return script;
	}

	/**
	 * Generates the script to show/hide the side chain for the given positions.
	 * Positions can be in the form of "666" or "666:C", both are fine.
	 *
	 * @param positions         an array of already generated script positions
	 * @param displaySideChain  flag to indicate to show/hide the side chain
	 */
	function generateSideChainScript(positions, displaySideChain)
	{
		var script = [];

		// display side chain (no effect for space-filling)
		if (!(_options.proteinScheme == "spaceFilling"))
		{
			// select the corresponding side chain and also the CA atom on the backbone
			script.push("select ((" + positions.join(", ") + ") and :" + _chain.chainId + " and sidechain) or " +
			            "((" + positions.join(", ") + ") and :" + _chain.chainId + " and *.CA);");

			if (displaySideChain)
			{
				// display the side chain with ball&stick style
				script.push("wireframe 0.15; spacefill 25%;");

				// TODO also color side chain wrt atom type (CPK)?
			}
			else
			{
				// hide the side chain
				script.push("wireframe OFF; spacefill OFF;");
			}
		}

		return script;
	}

	/**
	 * Generates the center script to be sent to the 3D app.
	 *
	 * @param position  position to center onto
	 * @return {Array}  script lines as an array
	 */
	function generateCenterScript(position)
	{
		var script = [];

		var scriptPos = generateScriptPos(position);

		// center to the selection
		script.push("center " + scriptPos + ":" + _chain.chainId + ";");

		return script;
	}

	/**
	 * Generates the focus script to be sent to the 3D app.
	 *
	 * @param mutationId    id of the mutation to highlight
	 * @return {Array}      script lines as an array
	 */
	function generateFocusScript(mutationId)
	{
		var script = [];
		var position = _chain.positionMap[mutationId];

		// check if the mutation maps on this chain
		if (position != null)
		{
			var scriptPos = generateScriptPos(position);

			// center and zoom to the selection
			script.push("zoom " + _options.focusZoom +";");
			script.push("center " + scriptPos + ":" + _chain.chainId + ";");
		}

		return script;
	}

	/**
	 * Performs the default zoom in operation.
	 * (Uses default zoom level defined by the underlying 3D visualizer)
	 */
	function zoomIn()
	{
		_3dApp.script("zoom in;");
	}

	/**
	 * Performs the default zoom out operation.
	 * (Uses default zoom value defined by the underlying 3D visualizer)
	 */
	function zoomOut()
	{
		_3dApp.script("zoom out;");
	}

	/**
	 * Zooms to default zoom level.
	 */
	function zoomActual()
	{
		_3dApp.script("zoom " + _options.defaultZoom + ";");
	}

	/**
	 * Zooms to the given zoom level.
	 *
	 * @param value desired zoom level
	 */
	function zoomTo(value)
	{
		_3dApp.script("zoom " + value + ";");
	}

	/**
	 * Generates a position string for Jmol scripting.
	 *
	 * @position object containing PDB position info
	 * @return {string} position string for Jmol
	 */
	function generateScriptPos(position)
	{
		var insertionStr = function(insertion) {
			var posStr = "";

			if (insertion != null &&
			    insertion.length > 0)
			{
				posStr += "^" + insertion;
			}

			return posStr;
		};

		var posStr = position.start.pdbPos +
		             insertionStr(position.start.insertion);

		if (position.end.pdbPos > position.start.pdbPos)
		{
			posStr += "-" + position.end.pdbPos +
			          insertionStr(position.end.insertion);
		}

		return posStr;
	}

	/**
	 * Updates the options of the 3D visualizer.
	 *
	 * @param options   new options object
	 */
	function updateOptions(options)
	{
		_options = jQuery.extend(true, {}, _options, options);
	}

	// return public functions
	return {init: init,
		show: show,
		hide: hide,
		minimize: minimize,
		maximize: maximize,
		toggleSize: toggleSize,
		isVisible: isVisible,
		reload: reload,
		refresh: refresh,
		focusOn: focus,
		center: centerOnHighlighted,
		resetCenter: resetCenter,
		highlight: highlight,
		resetHighlight: resetHighlight,
		refreshHighlight: refreshHighlight,
		zoomIn: zoomIn,
		zoomOut: zoomOut,
		zoomActual: zoomActual,
		zoomTo: zoomTo,
		resetFocus: resetFocus,
		updateContainer: updateContainer,
		toggleSpin: toggleSpin,
		reapplyStyle : reapplyStyle,
		updateOptions: updateOptions};
};
