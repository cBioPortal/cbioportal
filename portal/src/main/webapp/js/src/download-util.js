/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

if (cbio === undefined)
{
	var cbio = {};
}

/**
 * Singleton utility class for download related tasks.
 *
 * @author Selcuk Onur Sumer
 */
cbio.download = (function() {

	// Default client-side download options
	var _defaultOpts = {
		filename: "download.svg", // download file name
		contentType: "application/svg+xml", // download data type,
		dataType: null,      // servlet data type
		servletName: null,   // name of the data/conversion servlet (optional)
		servletParams: null, // servlet parameters (optional)
		preProcess: addSvgHeader,   // pre-process function for the provided data
		postProcess: cbio.util.b64ToByteArrays // post-process function for the data returned by the server (optional)
	};

	/**
	 * Submits the download form.
	 * This will send a request to the server.
	 *
	 * @param servletName       name of the action servlet
	 * @param servletParams     params to send with the form submit
	 * @param form              jQuery selector for the download form
	 * @deprecated use either initDownload or clientSideDownload
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
	 * @deprecated use either initDownload or clientSideDownload
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
	 *
	 * @deprecated use either initDownload or clientSideDownload
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
	 *
	 * @deprecated use either initDownload or clientSideDownload
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
	 * Initiates a client side download for the given content array.
	 *
	 * @param content   data array to download
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

		var blob = new Blob(content, {type: type});

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
		var svg = xml;

		var xmlHeader = "<?xml version='1.0'?>";
		var xmlVersion = "<?xml version=";

		// add xml header if not exist
		if(svg.indexOf(xmlVersion) == -1)
		{
			svg = xmlHeader + xml;
		}

		// add svg header if not exist
		if(svg.indexOf("svg xmlns") == -1)
		{
			svg = svg.replace(
				"<svg", "<svg xmlns='http://www.w3.org/2000/svg' version='1.1'");
		}

        // work around for Adobe Illustrator bug
        if(svg.indexOf("sans-serif") !== -1) {
            svg = svg.replace(/sans-serif/g, "verdana");    
        }
        
		return svg;
	}

	/**
	 * Initializes a client side download for the given content.
	 *
	 * @param content   data content, either string or DOM element
	 * @param options   download options (see _defaultOpts)
	 */
	function initDownload(content, options)
	{
		options = jQuery.extend(true, {}, _defaultOpts, options);

		// try to serialize only if content is not string...
		if (!_.isString(content))
		{
			content = serializeHtml(content);
		}

		if (_.isFunction(options.preProcess))
		{
			content = options.preProcess(content);
		}

		if (options.contentType.toLowerCase().indexOf("pdf") != -1)
		{
			// if no servlet params provided, use default ones for pdf...
			options.servletParams = options.servletParams || {
				filetype: "pdf_data",
				svgelement: content
			};
		} else if (options.contentType.toLowerCase().indexOf("png") != -1)
		{
		    options.servletParams = options.servletParams || {
			filetype: "png_data",
			svgelement: content
		    };
		}
		

		// check if a servlet name provided
		if (options.servletName != null)
		{
			$.ajax({url: options.servletName,
					type: "POST",
					data: options.servletParams,
					dataType: options.dataType,
					success: function(servletData){
						var downloadData = servletData;

						if (_.isFunction(options.postProcess))
						{
							downloadData = options.postProcess(servletData);
						}

						clientSideDownload(downloadData, options.filename, options.contentType);
					}
			});
		}
		else
		{
			clientSideDownload([content], options.filename, options.contentType);
		}
	}

    return {
	    submitDownload: submitDownload,
	    requestDownload: requestDownload,
	    clientSideDownload: clientSideDownload,
	    initDownload: initDownload,
	    serializeHtml: serializeHtml,
	    addSvgHeader: addSvgHeader
    };
})();
