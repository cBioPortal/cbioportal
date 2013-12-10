/**
 * Utility class to initialize the 3D mutation visualizer with JSmol (HTML5)
 * instance.
 *
 * @constructor
 */
var JSmolWrapper = function()
{
	// default options
	var _defaultOpts = {
		use: "HTML5",
		j2sPath: "js/lib/jsmol/j2s",
		//defaultModel: "$dopamine",
		disableJ2SLoadMonitor: true,
		disableInitialConsole: true
	};

	/**
	 * Initializes the visualizer.
	 *
	 * @param name      name of the application
	 * @param options   app options
	 */
	function init(name, options)
	{
		// TODO create a hidden iframe, with the new options
	}

	/**
	 * Updates the container of the visualizer object.
	 *
	 * @param container container selector
	 */
	function updateContainer(container)
	{
		// TODO put the frame inside the given container and make it visible
		container.empty();
		// TODO move this into init!
		container.append(
			'<iframe src="jsmol_frame.jsp" ' +
			'seamless="seamless" ' +
			'width="400" ' +
			'height="300"></iframe>');
	}

	/**
	 * Runs the given command as a script on the 3D visualizer object.
	 * @param command
	 */
	function script(command)
	{
		// TODO send the command to the iframe...
		// (use jquery-postmessage-plugin or postmassage function directly)
	}

	return {
		init: init,
		updateContainer: updateContainer,
		script: script
	};
};
