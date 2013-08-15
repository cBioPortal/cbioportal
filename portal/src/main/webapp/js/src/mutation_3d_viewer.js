var Mutation3dVis = function(name, options)
{
	var _container = null;
	var _wrapper = null;
	var _applet = null;

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

	function init()
	{
		_applet = Jmol.getApplet(name, _options);
		// TODO the wrapper id depends on the JMol implementation
		_wrapper = $("#" + name + "_appletinfotablediv");
		_wrapper.hide();
	}

	function updateContainer(container)
	{
		_container = $(container);
		_container.append(_wrapper);
	}

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

	function reload(pdbId)
	{
		Jmol.script(_applet, "load=" + pdbId);
	}

	return {init: init,
		show: show,
		hide: hide,
		reload: reload,
		updateContainer: updateContainer};
};
