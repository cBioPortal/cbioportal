/**
 * 3D Mutation Visualizer, currently based on Jmol applet.
 *
 * @param name      name of the visualizer (applet/application name)
 * @param options   visualization (Jmol) options
 * @constructor
 */
var Mutation3dVis = function(name, options)
{
	// main container -- html element
	var _container = null;

	// wrapper, created by the Jmol lib -- html element
	var _wrapper = null;

	// Jmol applet reference
	var _applet = null;

	// current selection (mutation positions on the protein)
	var _selection = null;

	// current chain (PdbChainModel instance)
	var _chain = null;

	// spin indicator (initially off)
	var _spin = "OFF";

	// selected style (default: cartoon)
	var _style = "cartoon";

	// default visualization options
	var defaultOpts = {
		// applet/application (Jmol) options
		appOptions: {
			width: 400,
			height: 300,
			debug: false,
			color: "white",
			//use: "HTML5",
			//j2sPath: "js/jsmol/j2s",
			//script: "load ="+pdbid+";",
			//defaultModel: "$dopamine",
			jarPath: "js/lib/jmol/",
			jarFile: "JmolAppletSigned.jar",
			disableJ2SLoadMonitor: true,
			disableInitialConsole: true
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
		// init applet
		_applet = Jmol.getApplet(name, _options.appOptions);

		// update wrapper reference
		// TODO the wrapper id depends on the JMol implementation
		_wrapper = $("#" + name + "_appletinfotablediv");
		_wrapper.hide();
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
		// move visualizer into its new container
		appContainer.append(_wrapper);
	}

	/**
	 * Toggles the spin.
	 */
	function toggleSpin()
	{
		_spin == "ON" ? _spin = "OFF" : _spin = "ON";

		var script = "spin " + _spin + ";";

		Jmol.script(_applet, script);
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

		Jmol.script(_applet, script);
	}

	/**
	 * Shows the visualizer panel.
	 */
	function show()
	{
		if (_wrapper != null)
		{
			_wrapper.show();
		}

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

		if (_wrapper != null)
		{
			//_wrapper.hide();
		}

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
	 * @param pdbId   PDB id
	 * @param chain   PdbChainModel instance
	 */
	function reload(pdbId, chain)
	{
		// pdbId and/or chainId may be null
		if (!pdbId || !chain)
		{
			// nothing to load
			return;
		}

		var selection = [];
		var color = _options.mutationColor;

		// TODO focus on the current segment instead of the chain?

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

				// do not color at all, this results in hiding user-filtered mutations...
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

		for (color in selection)
		{
			script.push("select " + selection[color].join(", ") + ";"); // select positions (mutations)
			script.push("color [" + color + "];"); // color with corresponding mutation color
		}

		script.push("spin " + _spin + ";"); // set spin

		script = script.join(" ");

		// run script
		Jmol.script(_applet, script);
	}

	/**
	 * Focuses on the residue corresponding to the given pileup
	 *
	 * @param pileup    Pileup instance
	 */
	function focus(pileup)
	{
		// no chain selected yet, terminate
		if (!_chain)
		{
			return;
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

			Jmol.script(_applet, script);
		}
		// no mapping position for this mutation on this chain
		else
		{
			// just reset focus
			resetFocus();
		}
	}

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

		Jmol.script(_applet, script);
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
		isVisible: isVisible,
		reload: reload,
		focusOn: focus,
		resetFocus: resetFocus,
		updateContainer: updateContainer,
		toggleSpin: toggleSpin,
		changeStyle : changeStyle,
		updateOptions: updateOptions};
};
