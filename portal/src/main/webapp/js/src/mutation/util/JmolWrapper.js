/**
 * Utility class to initialize the 3D mutation visualizer with Jmol (Java)
 * instance.
 *
 * Note: This class is assumed to have the same interface
 * (the same signature for all public functions) with the JSmolWrapper.
 *
 * @author Selcuk Onur Sumer
 */
var JmolWrapper = function()
{
	// Jmol applet reference
	var _applet = null;

	// wrapper, created by the Jmol lib -- html element
	var _wrapper = null;

	// default options (parameters required to init with Java applet)
	var defaultOpts = {
		//defaultModel: "$dopamine",
		jarPath: "js/lib/jmol/",
		jarFile: "JmolAppletSigned.jar",
		disableJ2SLoadMonitor: true,
		disableInitialConsole: true
	};

	var _options = null;

	/**
	 * Initializes the visualizer.
	 *
	 * @param name      name of the application
	 * @param options   app options
	 */
	function init(name, options)
	{
		_options = jQuery.extend(true, {}, defaultOpts, options);

		// init applet
		_applet = Jmol.getApplet(name, _options);

		// update wrapper reference
		// TODO the wrapper id depends on the JMol implementation
		_wrapper = $("#" + name + "_appletinfotablediv");
		_wrapper.hide();
	}

	/**
	 * Updates the container of the visualizer object.
	 *
	 * @param container container selector
	 */
	function updateContainer(container)
	{
		// move visualizer into its new container
		if (_wrapper != null)
		{
			container.append(_wrapper);
			_wrapper.show();
		}
	}

	/**
	 * Runs the given command as a script on the 3D visualizer object.
	 *
	 * @param command   command to send
	 * @param callback  function to call after execution of the script
	 */
	function script(command, callback)
	{
		// run Jmol script
		Jmol.script(_applet, command);

		// call the callback function after script completed
		if(_.isFunction(callback))
		{
			callback();
		}
	}

	return {
		init: init,
		updateContainer: updateContainer,
		script: script
	};

};
