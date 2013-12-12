<!DOCTYPE HTML>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

		<script type="text/javascript" src="js/lib/jsmol/JSmol.min.js"></script>
		<script type="text/javascript" src="js/src/cbio-util.js"></script>

		<script type="text/javascript">

			var _applet = null;

			function _processMessage(event)
			{
				// only accept messages from the local origin
				if (cbio.util.getWindowOrigin() != event.origin)
				{
					return;
				}

				if (event.data.type == "script")
				{
					Jmol.script(_applet, event.data.content);
				}
				else if (event.data.type == "init")
				{
					// TODO this does not work...
					//_applet = Jmol.getApplet("jsmol_applet", event.data.content);
				}
			}

			function _menuCheck(event)
			{
				var state = "none";

				if ($(".jmolPopupMenu").is(":visible"))
				{
					state = "visible";
				}
				else
				{
					state = "hidden";
				}

				window.parent.postMessage({type: "menu", content: state},
				                          cbio.util.getWindowOrigin());
			}

			window.addEventListener("message", _processMessage, false);
			window.parent.postMessage({type: "ready"}, cbio.util.getWindowOrigin());

			// TODO add a click listener on the JSmol canvas to call function _menuCheck
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
					color: "white"
				};

				// TODO get applet name from parent?
				_applet = Jmol.getApplet("jsmol_applet", _appOptions);
			</script>
		</div>
	</body>
</html>