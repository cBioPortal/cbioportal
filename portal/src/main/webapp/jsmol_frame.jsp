<!DOCTYPE HTML>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

		<script type="text/javascript" src="js/lib/jsmol/JSmol.min.js"></script>
		<script type="text/javascript" src="js/src/cbio-util.js"></script>

		<script type="text/javascript">

			var _applet = null;

			function _sendMessage(data)
			{
				window.parent.postMessage(data, cbio.util.getWindowOrigin());
			}

			function _processMessage(event)
			{
				// only accept messages from the local origin
				if (cbio.util.getWindowOrigin() != event.origin)
				{
					return;
				}

				if (event.data.type == "script")
				{
					// run the script on the JSmol object
					Jmol.scriptWait(_applet, event.data.content);

					// send a message to parent window to indicate that execution is completed
					_sendMessage({type: "done", scriptId: event.data.scriptId});
				}
				else if (event.data.type == "init")
				{
					// TODO init does not work after document ready (JSmol bug)...
					//_applet = Jmol.getApplet("jsmol_applet", event.data.content);
				}
			}

			function _menuCheck(event)
			{
				// TODO this delay is a workaround to wait for the menu to close
				// delay check for a small amount of time
				setTimeout(function(){
					var state = "none";

					if ($(".jmolPopupMenu").is(":visible"))
					{
						state = "visible";
					}
					else
					{
						state = "hidden";
					}

					_sendMessage({type: "menu", content: state});
				}, 10);
			}

			window.addEventListener("message", _processMessage, false);
			_sendMessage({type: "ready"});

		</script>

		<style type="text/css">
			#jsmol_container {
				/* TODO this a workaround, and might not be safe for all browsers */
				margin: -6px;
			}
		</style>
	</head>

	<body>
		<div id="jsmol_container">
			<script type="text/javascript">

				// TODO make these customizable (postMessage doesn't work at init, use GET params?)
				var _appOptions = {
					use: "HTML5",
					j2sPath: "js/lib/jsmol/j2s",
					disableJ2SLoadMonitor: true,
					disableInitialConsole: true,
					width: 400,
					height: 300,
					debug: false,
					color: "white",
					readyFunction: function() {
						$("html").click(_menuCheck);
						$("canvas").mousedown(_menuCheck);
					}
				};

				// TODO get applet name from parent?
				_applet = Jmol.getApplet("jsmol_applet", _appOptions);
			</script>
		</div>
	</body>
</html>