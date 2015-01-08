if (cbio === undefined)
{
	var cbio = {};
}

cbio.download = (function() {

	// TODO allow to update?
	var _pdfDataServlet = "svgtopdf.do";

	/**
	 * Submits the download form.
	 * This will send a request to the server.
	 *
	 * @param servletName       name of the action servlet
	 * @param servletParams     params to send with the form submit
	 * @param form              jQuery selector for the download form
	 */
	function submitDownload(servletName, servletParams, form)
	{
		// remove all previous input fields (if any)
		$(form).find("input").remove();

		// add new input fields
		for (var name in servletParams)
		{
			var value = servletParams[name];
			$(form).append('<input type="hidden" name="' + name + '">');
			$(form).find('input[name="' + name + '"]').val(value);
		}

		// update target servlet for the action
		$(form).attr("action", servletName);
		// submit the form
		$(form).submit();
	}

	/**
	 * Sends a download request to the hidden frame dedicated to file download.
	 *
	 * This function is implemented as a workaround to prevent JSmol crash
	 * due to window.location change after a download request.
	 *
	 * @param servletName
	 * @param servletParams
	 */
	function requestDownload(servletName, servletParams)
	{
		// TODO this is a workaround, frame download doesn't work for IE
		if (cbio.util.browser.msie)
		{
			initDownloadForm();
			submitDownload(servletName, servletParams, "#global_file_download_form");
			return;
		}

		initDownloadFrame(function() {
			var targetWindow = cbio.util.getTargetWindow("global_file_download_frame");

			targetWindow.postMessage(
				{servletName: servletName,
					servletParams: servletParams},
				getOrigin());
		});
	}

	/**
	 * Initializes the hidden download frame for the entire document.
	 * This is to isolate download requests from the main window.
	 */
	function initDownloadFrame(callback)
	{
		var frame = '<iframe id="global_file_download_frame" ' +
		            'src="file_download_frame.jsp" ' +
		            'seamless="seamless" width="0" height="0" ' +
		            'frameBorder="0" scrolling="no">' +
		            '</iframe>';

		// only initialize if the frame doesn't exist
		if ($("#global_file_download_frame").length === 0)
		{
			$(document.body).append(frame);

			// TODO a workaround to enable target frame to get ready to listen messages
			setTimeout(callback, 500);
		}
		else
		{
			callback();
		}
	}

	/**
	 * This form is initialized only for IE
	 */
	function initDownloadForm()
	{
		var form = '<form id="global_file_download_form"' +
		           'style="display:inline-block"' +
		           'action="" method="post" target="_blank">' +
		           '</form>';

		// only initialize if the form doesn't exist
		if ($("#global_file_download_form").length === 0)
		{
			$(document.body).append(form);
		}
	}

	/**
	 * Initiates a client side download for the given content.
	 *
	 * @param content   data content to download
	 * @param filename  download file name
	 * @param type      download type
	 */
	function clientSideDownload(content, filename, type)
	{
		if (type == null)
		{
			// text by default
			type = "text/plain;charset=utf-8"
		}

		if (filename == null)
		{
			filename = "download.txt";
		}

		// TODO if type is not text, we may need to do something else...
		var blob = new Blob([content], {type: type});

		saveAs(blob, filename);
	}

	/**
	 * Serializes the given html element into a string.
	 *
	 * @param element       html element
	 * @returns {string}    serialized string
	 */
	function serializeHtml(element)
	{
		// convert html element to string
		var xmlSerializer = new XMLSerializer();
		return xmlSerializer.serializeToString(element);
	}

	/**
	 * Adds missing xml and svg headers to the provided svg string
	 *
	 * @param xml   xml as a string
	 * @returns {string}    new xml string with additional headers
	 */
	function addSvgHeader(xml)
	{
		var xmlHeader = "<?xml version='1.0'?>";
		var svg = xmlHeader + xml;

		if(svg.indexOf("svg xmlns") == -1)
		{
			svg = svg.replace(
				"<svg", "<svg xmlns='http://www.w3.org/2000/svg' version='1.1'");
		}

		return svg;
	}

	/**
	 * Initiates a client side download specifically for svg file type.
	 *
	 * @param svgElement    svg element (as an html element)
	 * @param filename      download file name
	 */
	function clientSideSvgDownload(svgElement, filename)
	{
		// serialize element (convert to string) & init download
		clientSideSvgStrDownload(serializeHtml(svgElement), filename);
	}

	/**
	 * Initiates a client side download specifically for svg file type.
	 *
	 * @param svgString     svg element (as a string)
	 * @param filename      download file name
	 */
	function clientSideSvgStrDownload(svgString, filename)
	{
		// add header & init download
		clientSideDownload(addSvgHeader(svgString), filename, "application/svg+xml");
	}

	/**
	 * Initiates a client side PDF download for the given svg string.
	 *
	 * @param svgString     svg element (as a string)
	 * @param filename      download file name
	 */
	function clientSidePdfDownload(svgString, filename)
	{
		var servletParams = {filetype: "pdf_data",
			svgelement: svgString};

		// retrieve data from the server
		//$.post(_pdfDataServlet, servletParams, initFn, "binary");

		$.ajax({url: _pdfDataServlet,
			type: "POST",
			data: servletParams,
			dataType: "binary",
			success: function(pdfData){
				// TODO doesn't work for Firefox, need a proper binary data processor
				clientSideDownload(pdfData, filename, "application/pdf");
			}
		});

//		requestDownload(_pdfDataServlet,
//			{filetype: "pdf",
//				filename: filename,
//                svgelement: svgString}
//        );
	}

	/**
	 * Initiates a client side download for the given svg element.
	 *
	 * @param svgElement    svg element (DOM object)
	 * @param filename      download file name
	 * @param type          type of the download (PDF or SVG)
	 */
	function initSvgDownload(svgElement, filename, type)
	{
		initSvgStrDownload(serializeHtml(svgElement), filename, type);
	}

	/**
	 * Initiates a client side download for the given svg string.
	 *
	 * @param svgString     svg element (as a string)
	 * @param filename      download file name
	 * @param type          type of the download (PDF or SVG)
	 */
	function initSvgStrDownload(svgString, filename, type)
	{
		if (type.toLowerCase() == "svg")
		{
			clientSideSvgStrDownload(svgString, filename);
		}
		else if (type.toLowerCase() == "pdf")
		{
			clientSidePdfDownload(svgString, filename);
		}
		else
		{
			// TODO any other type?
		}
	}

    return {
	    submitDownload: submitDownload,
	    requestDownload: requestDownload,
	    clientSideDownload: clientSideDownload,
	    clientSideSvgDownload: clientSideSvgDownload,
	    clientSideSvgStrDownload: clientSideSvgStrDownload,
	    initSvgDownload: initSvgDownload,
	    initSvgStrDownload: initSvgStrDownload,
	    serializeHtml: serializeHtml,
	    addSvgHeader: addSvgHeader
    };
})();
