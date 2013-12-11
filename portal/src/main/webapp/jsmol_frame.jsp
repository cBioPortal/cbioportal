<html>
	<body>
		<script type="text/javascript" src="js/lib/jsmol/JSmol.min.js"></script>

		<style type="text/css">
			#jsmol_container {
				/* TODO this a workaround, and might not be safe */
				margin: -6px;
			}
		</style>

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
				var _applet = Jmol.getApplet("jsmol_applet", _appOptions);
			</script>
		</div>

		<script type="text/javascript">
			var _processMessage = function(event)
			{
				// TODO check event.origin for security
				// we have many different domains, use an external list of safe domains?

				// only accept messages from the local origin
				if (window.location.origin != event.origin)
				{
					return;
				}


				if (event.data.type == "script")
				{
					Jmol.script(_applet, event.data.content);
				}
			};

			window.addEventListener("message", _processMessage, false);
        </script>

	</body>
</html>