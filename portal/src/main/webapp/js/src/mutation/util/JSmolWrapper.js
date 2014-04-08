/**
 * Utility class to initialize the 3D mutation visualizer with JSmol (HTML5)
 * instance.
 *
 * This class is designed to initialize the JSmol visualizer within
 * a separate frame (due to JSmol incompatibilities with jQuery lib)
 *
 * Note: This class is assumed to have the same interface
 * (the same signature for all public functions) with the JmolWrapper.
 *
 * @author Selcuk Onur Sumer
 */
var JSmolWrapper = function()
{
	var _appName = null;
	var _options = null;
	var _frameHtml = null;
	var _targetWindow = null;
	var _targetDocument = null;
	var _container = null;
	var _origin = cbio.util.getWindowOrigin();
	var _commandQueue = [];
	var _commandMap = {};

	var _idCounter = 0;

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

		var w = _options.width;
		// TODO this (x4) is a workaround for the menu to overflow
		var h = _options.height * 4;

		// TODO send custom opts via GET? (i.e: jsmol_frame.jsp?name=n&width=400&...)
		_frameHtml = '<iframe id="jsmol_frame" ' +
		             'src="jsmol_frame.jsp" ' +
		             'seamless="seamless" ' +
		             'width="' + w + '" ' +
		             'height="' + h + '" ' +
		             'frameBorder="0" ' +
		             'scrolling="no"></iframe>';

		// add listener to process messages coming from the iFrame

		var _processMessage = function(event)
		{
			// only accept messages from the local origin
			if (cbio.util.getWindowOrigin() != event.origin)
			{
				return;
			}

			// ready event: supposed to be fired when frame gets ready
			if (event.data.type == "ready")
			{
				if (_targetWindow)
				{
					_targetDocument = getTargetDocument("jsmol_frame");

					// TODO JSmol init doesn't work after document ready
					//var data = {type: "init", content: _options};
					//_targetWindow.postMessage(data, _origin);
				}
			}
			// menu event: supposed to be fired when JSmol menu becomes active
			else if (event.data.type == "menu")
			{
				// show or hide the overlay wrt the menu event
				if (_container)
				{
					if (event.data.content == "visible")
					{
						_container.css("overflow", "visible");
					}
					else if (event.data.content == "hidden")
					{
						_container.css("overflow", "hidden");
					}
				}
			}
			// done event: supposed to be fired when JSmol finishes executing a script
			else if (event.data.type == "done")
			{
				var command = _commandMap[event.data.scriptId];

				if (command != null)
				{
					// remove the command to prevent possible multiple executions
					delete _commandMap[event.data.scriptId];

					// check for a registered callback

					var callback = command.callback;

					if (callback &&
					    _.isFunction(callback))
					{
						// call the registered callback function
						callback();
					}
				}

				// see if there are more commands to send
				if (!_.isEmpty(_commandQueue))
				{
					// get the next command from the queue
					command = _commandQueue.shift();
					// add it to the map to access the callback when "done"
					_commandMap[command.data.scriptId] = command;
					// send the command
					_targetWindow.postMessage(command.data, _origin);
				}
			}
		};

		window.addEventListener("message", _processMessage, false);
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
			_container = container;
		}

		_targetWindow = getTargetWindow("jsmol_frame");

		if (!_targetWindow)
		{
			console.log("warning: JSmol frame cannot be initialized properly");
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
		if (_targetWindow)
		{
			_idCounter = (_idCounter + 1) % 1000000;

			var data = {type: "script",
				content: command,
				scriptId: "script_" + (_idCounter)};

			// queue the command, this prevents simultaneous JSmol script calls
			queue(data, callback);
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

	/**
	 * Adds the given data package and callback function to the command queue.
	 * If the queue is currently empty, immediately posts the message to the
	 * target window.
	 *
	 * @param data      data package to send
	 * @param callback  callback function for this command
	 */
	function queue(data, callback)
	{
		var command = {data: data, callback: callback};

		// TODO not always safe -- producer/consumer problem, may run into a deadlock...

		// immediately post message if the queue is empty
		if (_.isEmpty(_commandQueue))
		{
			// add the command to the map to access the callback when "done"
			_commandMap[command.data.scriptId] = command;
			// send the command
			_targetWindow.postMessage(data, _origin);
		}
		// add command to the queue (message will be post after a "done" event)
		else
		{
			// add the command to the queue
			_commandQueue.push(command);
		}
	}

	return {
		init: init,
		updateContainer: updateContainer,
		script: script
	};
};
