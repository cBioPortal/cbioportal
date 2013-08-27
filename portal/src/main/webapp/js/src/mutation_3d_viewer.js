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

	// initial selection (mutation positions on the protein)
	var _initialSelection = null;

	// TODO take this as a parameter
	var callbackfun = function(applet) {/*alert('add your callback functions here');*/};

	var defaultOpts = {
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
	};

	var _options = jQuery.extend(true, {}, defaultOpts, options);

	if (jQuery.isFunction(callbackfun)) {
		_options['readyFunction'] = callbackfun;
	}

	/**
	 * Initializes the visualizer.
	 */
	function init()
	{
		// init applet
		_applet = Jmol.getApplet(name, _options);

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

		// move visualizer into its new container
		_container.append(_wrapper);
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
	 * @param chain   chain with mapped positions
	 */
	function reload(pdbId, chain)
	{
		// TODO pdbId and/or chainId may be null

		// load the corresponding pdb
		Jmol.script(_applet, "load=" + pdbId);

		var selection = [];

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

		// save current selection for a possible future restore
		_initialSelection = selection;

		var script = "ribbon ONLY;" + // show ribbon view
		             //"spin ON;" + // turn on spinning
		             "select " + selection.join(", ") + ";" + // select positions
		             "color purple;"; // color selection with a different color
		             //"selectionHalos ON;";

		// select residues on the 3D viewer & highlight them
		Jmol.script(_applet, script);
	}

	// return public functions
	return {init: init,
		show: show,
		hide: hide,
		reload: reload,
		updateContainer: updateContainer};
};
