<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<!DOCTYPE HTML>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

	<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
	<script type="text/javascript" src="js/src/cbio-util.js?<%=GlobalProperties.getAppVersion()%>"></script>

	<script type="text/javascript">
		function _processMessage(event)
		{
			// only accept messages from the local origin
			if (cbio.util.getWindowOrigin() != event.origin)
			{
				return;
			}

			var data = event.data;

			var name = data.servletName;
			var params = data.servletParams;

			// submit the download form with the provided parameters
			cbio.util.submitDownload(name, params, ".file-download-form-container form");
		}

		window.addEventListener("message", _processMessage, false);

	</script>
</head>

<body>
	<div class="file-download-form-container">
		<form style="display:inline-block"
		      action=""
		      method="post"
		      class="file-download-form">
		</form>
	</div>
</body>
</html>