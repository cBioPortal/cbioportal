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

	// current chain (PdbChainModel instance)
	var _chain = null;

	// spin indicator (initially off)
	var _spin = "OFF";

	// selected style (default: cartoon)
	var _style = "cartoon";

	// default visualization options
	var defaultOpts = {
		// applet/application (Jmol/JSmol) options
		appOptions: {
			width: 400,
			height: 300,
			debug: false,
			color: "white"
		},
		defaultColor: "xDDDDDD", // default color of ribbons
		translucency: 5, // translucency (opacity) of the default color
		chainColor: "x888888", // color of the selected chain
		mutationColor: "xFF0000", // color of the mutated residues (can also be a function)
		highlightColor: "xFFDD00", // color of the user-selected mutations
		defaultZoom: 100, // default (unfocused) zoom level
		focusZoom: 250, // focused zoom level
		containerPadding: 10, // padding for the vis container (this is to prevent overlapping)
		// TODO minimized length is actually depends on padding values, it might be better to calculate it
		minimizedHeight: 10 // minimized height of the container (assuming this will hide everything but the title)
	};

	// Predefined style scripts for Jmol
	var styleScripts = {
		ballAndStick: "wireframe ONLY; wireframe 0.15; spacefill 20%;",
		ribbon: "ribbon ONLY;",
		cartoon: "cartoon ONLY;"
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

		var appContainer = _container.find("#mutation_3d_visualizer");

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
	 * Changes the style of the visualizer.
	 *
	 * @param style name of the style
	 */
	function changeStyle(style)
	{
		// update selected style
		_style = style;

		var script = "select all;" +
		             styleScripts[style];

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
			_container.css('top', 0);
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
			_container.css("overflow", "hidden");
			_container.css("height", _options.minimizedHeight);
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
			_container.css("overflow", "");
			_container.css("height", "");
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
		var top = _container.css("top").replace("px", "");

		var hidden = (top < 0) || _container.is(":hidden");

		return !hidden;
	}

	/**
	 * Reloads the protein view for the given PDB id
	 * and the chain.
	 *
	 * @param pdbId     PDB id
	 * @param chain     PdbChainModel instance
	 * @param callback  function to call after reload
	 */
	function reload(pdbId, chain, callback)
	{
		// pdbId and/or chainId may be null
		if (!pdbId || !chain)
		{
			// nothing to load
			return;
		}

		var selection = {};
		var color = _options.mutationColor;

		// color code the mutated positions (residues)
		for (var mutationId in chain.positionMap)
		{
			var position = chain.positionMap[mutationId];

			if (_.isFunction(_options.mutationColor))
			{
				color = _options.mutationColor(mutationId, pdbId, chain);
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
				selection[color] = [];
			}

			// TODO remove duplicates from the array (use another data structure such as a map)
			selection[color].push(generateScriptPos(position) + ":" + chain.chainId);
		}

		// save current chain & selection for a possible future restore
		_selection = selection;
		_chain = chain;

		// construct Jmol script string
		var script = [];
		script.push("load=" + pdbId + ";"); // load the corresponding pdb
		script.push("select all;"); // select everything
		script.push(styleScripts[_style]); // show selected style view
		script.push("color [" + _options.defaultColor + "] "); // set default color
		script.push("translucent [" + _options.translucency + "];"); // set default opacity
		script.push("select :" + chain.chainId + ";"); // select the chain
		script.push("color [" + _options.chainColor + "];"); // set chain color

		// color each residue with a mapped color (this is to sync with diagram colors)
		for (color in selection)
		{
			script.push("select " + selection[color].join(", ") + ";"); // select positions (mutations)
			script.push("color [" + color + "];"); // color with corresponding mutation color
		}

		script.push("spin " + _spin + ";"); // set spin

		// convert array into a string (to pass to Jmol)
		script = script.join(" ");

		// run script
		_3dApp.script(script, callback);
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
		if (!_chain)
		{
			return false;
		}

		// assuming all other mutations in the same pileup have
		// the same (or very close) mutation position.
		var id = pileup.mutations[0].mutationId;

		var position = _chain.positionMap[id];

		// check if the mutation maps on this chain
		if (position)
		{
			var scriptPos = generateScriptPos(position);

			// TODO turn on selection halos for the highlighted position?

			var script = [];
			// center and zoom to the selection
			script.push("zoom " + _options.focusZoom +";");
			script.push("center " + scriptPos + ":" + _chain.chainId + ";");
			// reset previous highlights
			for (var color in _selection)
			{
				script.push("select " + _selection[color].join(", ") + ";"); // select positions (mutations)
				script.push("color [" + color + "];"); // color with corresponding mutation color
			}
			// highlight the focused position
		    script.push("select " + scriptPos + ":" + _chain.chainId + ";");
			script.push("color [" + _options.highlightColor + "];");

			script = script.join(" ");

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

		for (var color in _selection)
		{
			script.push("select " + _selection[color].join(", ") + ";"); // select positions (mutations)
			script.push("color [" + color + "];"); // color with corresponding mutation color
		}

		script = script.join(" ");

		_3dApp.script(script);
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
	 * Generates a position string for Jmol scripting.
	 *
	 * @position object containing PDB position info
	 * @return {string} position string for Jmol
	 */
	function generateScriptPos(position)
	{
		var posStr = position.start.pdbPos;

		if (position.end.pdbPos > position.start.pdbPos)
		{
			posStr += "-" + position.end.pdbPos;
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
		focusOn: focus,
		zoomIn: zoomIn,
		zoomOut: zoomOut,
		zoomActual: zoomActual,
		resetFocus: resetFocus,
		updateContainer: updateContainer,
		toggleSpin: toggleSpin,
		changeStyle : changeStyle,
		updateOptions: updateOptions};
};
