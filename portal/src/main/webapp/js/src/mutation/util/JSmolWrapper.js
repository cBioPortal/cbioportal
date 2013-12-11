/**
 * Utility class to initialize the 3D mutation visualizer with JSmol (HTML5)
 * instance.
 *
 * This class is designed to initialize the JSmol visualizer within
 * a separate frame (due to JSmol incompatibilities with jQuery lib)
 *
 * @constructor
 */
var JSmolWrapper = function()
{
	var _appName = null;
	var _options = null;
	var _frameHtml = null;
	var _targetWindow = null;
	var _origin = cbio.util.getWindowOrigin();

	// default options
	var defaultOpts = {
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
		// init vars
		_appName = name;
		_options = jQuery.extend(true, {}, defaultOpts, options);
		// TODO send custom opts via GET? (i.e: jsmol_frame.jsp?name=n&width=400&...)
		_frameHtml = '<iframe id="jsmol_frame" ' +
		             'src="jsmol_frame.jsp" ' +
		             'seamless="seamless" ' +
		             'width="400" ' +
		             'height="300" ' +
		             'frameBorder="0" ' +
		             'scrolling="no"></iframe>';
	}

	/**
	 * Updates the container of the visualizer object.
	 *
	 * @param container container selector
	 */
	function updateContainer(container)
	{
		// init the iFrame for the given container
		if (container && _frameHtml)
		{
			container.empty();
			container.append(_frameHtml);
		}

		_targetWindow = getTargetWindow("jsmol_frame");
		var targetDocument = getTargetDocument("jsmol_frame");

		if (_targetWindow)
		{
			// TODO custom init doesn't work, send init opts as get params?
			var data = {type: "init", content: _options};
			//_targetWindow.postMessage(data, _domain);
		}
		else
		{
			console.log("warning: JSmol frame cannot be initialized properly");
		}
	}

	/**
	 * Runs the given command as a script on the 3D visualizer object.
	 *
	 * @param command   command to send
	 */
	function script(command)
	{
		if (_targetWindow)
		{
			var data = {type: "script", content: command};
			_targetWindow.postMessage(data, _origin);
		}
	}

	/**
	 * Returns the content window for the given target frame.
	 *
	 * @param id    id of the target frame
	 */
	function getTargetWindow(id)
	{
		var frame = document.getElementById(id);
		var targetWindow = frame;

		if (frame.contentWindow)
		{
			targetWindow = frame.contentWindow;
		}

		return targetWindow;
	}

	/**
	 * Returns the content document for the given target frame.
	 *
	 * @param id    id of the target frame
	 */
	function getTargetDocument(id)
	{
		var frame = document.getElementById(id);
		var targetDocument = frame.contentDocument;

		if (!targetDocument && frame.contentWindow)
		{
			targetDocument = frame.contentWindow.document;
		}

		return targetDocument;
	}

	return {
		init: init,
		updateContainer: updateContainer,
		script: script
	};
};
