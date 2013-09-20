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
		mutationColor: "xFF0000", // color of the selected mutations
		containerPadding: 25 // padding for the vis container (this is to prevent overlapping)
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
		}
	}

	/**
	 * Hides the visualizer panel.
	 */
	function hide()
	{
		if (_wrapper != null)
		{
			_wrapper.hide();
		}

		if (_container != null)
		{
			_container.hide();
		}
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
		// TODO pdbId and/or chainId may be null

		// load the corresponding pdb
		Jmol.script(_applet, "load=" + pdbId);

		var selection = [];

		// TODO focus on the current segment instead of the chain?

		// highlight the positions (residues)
		for (var mutationId in chain.positionMap)
		{
			var pdbPos = chain.positionMap[mutationId];
			var posStr = pdbPos.start;

			if (pdbPos.end > pdbPos.start)
			{
				posStr += "-" + pdbPos.end;
			}

			selection.push(posStr + ":" + chain.chainId);
		}

		// save current chain & selection for a possible future restore
		_selection = selection;
		_chain = chain;

		// select residues on the 3D viewer & highlight them

		var script = "ribbon ONLY;" + // show ribbon view
		             //"spin ON;" + // turn on spinning
		             "select all;" + // select everything
		             "color [" + _options.defaultColor + "] " + // set default color
		             "translucent [" + _options.translucency + "];" + // set default opacity
		             "select :" + chain.chainId + ";" + // select the chain
		             "color [" + _options.chainColor + "];" + // set chain color
		             "select " + selection.join(", ") + ";" + // select positions (mutations)
		             "color red;"; // highlight selected area

		Jmol.script(_applet, script);
	}

	// return public functions
	return {init: init,
		show: show,
		hide: hide,
		reload: reload,
		updateContainer: updateContainer};
};
