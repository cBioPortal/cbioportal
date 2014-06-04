var cbio = {};

cbio.util = (function() {

    var toPrecision = function(number, precision, threshold) {
        // round to precision significant figures
        // with threshold being the upper bound on the numbers that are
        // rewritten in exponential notation

        if (0.000001 <= number && number < threshold) {
            return number.toExponential(precision);
        }

        var ret = number.toPrecision(precision);
        if (ret.indexOf(".")!==-1)
            ret = ret.replace(/\.?0+$/,'');

        return ret;
    };

    var getObjectLength = function(object) {
        var length = 0;

        for (var i in object) {
            if (Object.prototype.hasOwnProperty.call(object, i)){
                length++;
            }
        }
        return length;
    };

    var checkNullOrUndefined = function(o) {
        return o === null || typeof o === "undefined";
    };

    // convert from array to associative array of element to index
    var arrayToAssociatedArrayIndices = function(arr, offset) {
        if (checkNullOrUndefined(offset)) offset=0;
        var aa = {};
        for (var i=0, n=arr.length; i<n; i++) {
            aa[arr[i]] = i+offset;
        }
        return aa;
    };
        
    var uniqueElementsOfArray = function(arr) {
        var ret = [];
        var aa = {};
        for (var i=0, n=arr.length; i<n; i++) {
            if (!(arr[i] in aa)) {
                ret.push(arr[i]);
                aa[arr[i]] = 1;
            }
        }
        return ret;
    };

    var alterAxesAttrForPDFConverter = function(xAxisGrp, shiftValueOnX, yAxisGrp, shiftValueOnY, rollback) {

        // To alter attributes of the input D3 SVG object (axis)
        // in order to prevent the text of the axes from moving up
        // when converting the SVG to PDF
        // (TODO: This is a temporary solution, need to debug batik library)
        //
        // @param xAxisGrp: the x axis D3 object
        // @param shiftValueOnX: increased/decreased value of the x axis' text vertical position of the text of x axis
        //                       before/after conversion
        // @param yAxisGrp: the y axis D3 object
        // @param shiftValueOnY: increased/decreased value of the y axis' text vertical position of the text of x axis
        //                       before/after conversion
        // @param rollback: the switch to control moving up/down the axes' text (true -> move up; false -> move down)
        //

        if (rollback)
        {
            shiftValueOnX = -1 * shiftValueOnX;
            shiftValueOnY = -1 * shiftValueOnY;
        }

        var xLabels = xAxisGrp
            .selectAll(".tick")
            .selectAll("text");

        var yLabels = yAxisGrp
            .selectAll(".tick")
            .selectAll("text");

        // TODO:
        // shifting axis tick labels a little bit because of
        // a bug in the PDF converter library (this is a hack!)
        var xy = parseInt(xLabels.attr("y"));
        var yy = parseInt(yLabels.attr("y"));

        xLabels.attr("y", xy + shiftValueOnX);
        yLabels.attr("y", yy + shiftValueOnY);
    };

    /**
     * Determines the longest common starting substring
     * for the given two strings
     *
     * @param str1  first string
     * @param str2  second string
     * @return {String} longest common starting substring
     */
    var lcss = function (str1, str2)
    {
        var i = 0;

        while (i < str1.length && i < str2.length)
        {
            if (str1[i] === str2[i])
            {
                i++;
            }
            else
            {
                break;
            }
        }

        return str1.substring(0, i);
    };

	/**
	 * Detects browser and its version.
	 * This function is implemented as an alternative to the deprecated jQuery.browser object.
	 *
	 * @return {object} browser information as an object
	 */
	var detectBrowser = function ()
	{
		var browser = {};
		var uagent = navigator.userAgent.toLowerCase();

		browser.firefox = /mozilla/.test(uagent) &&
		                  /firefox/.test(uagent);

		browser.mozilla = browser.firefox; // this is just an alias

		browser.chrome = /webkit/.test(uagent) &&
		                 /chrome/.test(uagent);

		browser.safari = /applewebkit/.test(uagent) &&
		                 /safari/.test(uagent) &&
		                 !/chrome/.test(uagent);

		browser.opera = /opera/.test(uagent);

		browser.msie = /msie/.test(uagent);

		browser.version = "";

		// check for IE 11
		if (!(browser.msie ||
		      browser.firefox ||
		      browser.chrome ||
		      browser.safari ||
		      browser.opera))
		{
			// TODO probably we need to update this for future IE versions
			if (/trident/.test(uagent))
			{
				browser.msie = true;
				browser.version = 11;
			}
		}

		if (browser.version === "")
		{
			for (var x in browser)
			{
				if (browser[x])
				{
					browser.version = uagent.match(new RegExp("(" + x + ")( |/)([0-9]+)"))[3];
					break;
				}
			}
		}

		return browser;
	};

	/**
	 * Retrieves the page origin from the global window object. This function is
	 * introduced to eliminate cross-browser issues (window.location.origin is
	 * undefined for IE)
	 */
	var getOrigin = function()
	{
		var origin = window.location.origin;

		if (!origin)
		{
			origin = window.location.protocol + "//" +
			         window.location.hostname +
			         (window.location.port ? ':' + window.location.port: '');
		}

		return origin;
	};
        
        var sortByAttribute = function(objs, attrName) {
            function compare(a,b) {
                if (a[attrName] < b[attrName])
                    return -1;
                if (a[attrName] > b[attrName])
                    return 1;
                return 0;
            }
            objs.sort(compare);
            return objs;
        };
        
	/**
	 * Replaces problematic characters with an underscore for the given string.
	 * Those characters cause problems with the properties of an HTML object,
	 * especially for the id and class properties.
	 *
	 * @param property  string to be modified
	 * @return {string} safe version of the given string
	 */
	var safeProperty = function(property)
	{
		return property.replace(/[^a-zA-Z0-9-]/g,'_');
	};

	/**
	 * Hides the child html element on mouse leave, and shows on
	 * mouse enter. This function is designed to hide a child
	 * element within a parent element.
	 *
	 * @param parentElement target of mouse events
	 * @param childElement  element to show/hide
	 */
	function autoHideOnMouseLeave(parentElement, childElement)
	{
		$(parentElement).mouseenter(function(evt) {
			childElement.fadeIn({complete: function() {
				$(this).css({"visibility":"visible"});
				$(this).css({"display":"inline"});
			}});
		});

		$(parentElement).mouseleave(function(evt) {
			// fade out without setting display to none
			childElement.fadeOut({complete: function() {
				// fade out uses hide() function, but it may change
				// the size of the parent element
				// so this is a workaround to prevent resize
				// due to display: "none"
				$(this).css({"visibility":"hidden"});
				$(this).css({"display":"inline"});
			}});
		});
	}

    function swapElement(array, indexA, indexB) {
        var tmp = array[indexA];
        array[indexA] = array[indexB];
        array[indexB] = tmp;
    }

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
		if (detectBrowser().msie)
		{
			initDownloadForm();
			submitDownload(servletName, servletParams, "#global_file_download_form");
			return;
		}

		initDownloadFrame(function() {
			var targetWindow = getTargetWindow("global_file_download_frame");

			targetWindow.postMessage(
				{servletName: servletName,
					servletParams: servletParams},
				getOrigin());
		});
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

    return {
        toPrecision: toPrecision,
        getObjectLength: getObjectLength,
        checkNullOrUndefined: checkNullOrUndefined,
        uniqueElementsOfArray: uniqueElementsOfArray,
        arrayToAssociatedArrayIndices: arrayToAssociatedArrayIndices,
        alterAxesAttrForPDFConverter: alterAxesAttrForPDFConverter,
        lcss: lcss,
        browser: detectBrowser(), // returning the browser object, not the function itself
        getWindowOrigin: getOrigin,
        sortByAttribute: sortByAttribute,
        safeProperty: safeProperty,
        autoHideOnMouseLeave: autoHideOnMouseLeave,
        swapElement: swapElement,
	    getTargetWindow: getTargetWindow,
	    submitDownload: submitDownload,
	    requestDownload: requestDownload,
	    getTargetDocument: getTargetDocument
    };

})();

if (!Array.prototype.forEach) {
    Array.prototype.forEach = function(fun /*, thisp*/) {
        var len = this.length >>> 0;
        if (typeof fun !== "function") {
            throw new TypeError();
        }

        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
            if (i in this) {
                fun.call(thisp, this[i], i, this);
            }
        }
    };
}
