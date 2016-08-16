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

cbio.util = (function() {

    var toPrecision = function(number, precision, threshold) {
        // round to precision significant figures
        // with threshold being the upper bound on the numbers that are
        // rewritten in exponential notation

        if (0.000001 <= number && number < threshold) {
            return number.toExponential(precision);
        }

        var ret = number.toPrecision(precision);
        //if (ret.indexOf(".")!==-1)
        //    ret = ret.replace(/\.?0+$/,'');

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
	 * Converts base 64 encoded string into an array of byte arrays.
	 *
	 * @param b64Data   base 64 encoded string
	 * @param sliceSize size of each byte array (default: 512)
	 * @returns {Array} an array of byte arrays
	 */
	function b64ToByteArrays(b64Data, sliceSize) {
		sliceSize = sliceSize || 512;

		var byteCharacters = atob(b64Data);
		var byteArrays = [];

		for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
			var slice = byteCharacters.slice(offset, offset + sliceSize);

			var byteNumbers = new Array(slice.length);
			for (var i = 0; i < slice.length; i++) {
				byteNumbers[i] = slice.charCodeAt(i);
			}

			var byteArray = new Uint8Array(byteNumbers);

			byteArrays.push(byteArray);
		}

		return byteArrays;
	}

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

    function getLinkToPatientView(cancerStudyId, patientId) {
        return "case.do?cancer_study_id=" + cancerStudyId + "&case_id=" + patientId;
    }

    function getLinkToSampleView(cancerStudyId, sampleId) {
        return "case.do?cancer_study_id=" + cancerStudyId + "&sample_id=" + sampleId;
    }

    /**
     * Adds qTip to the provided target when first time mouse enter
     *
     * @param target qTip target, could be a class name, id or any jquery acceptable element
     * @param qTipOpts qTip initialization options
     */
    function addTargetedQTip(target, qTipOpts) {
        if(target) {
	        // check if target[0] is SVG
	        if (target[0] && target[0].ownerSVGElement)
	        {
		        target = target[0];
	        }
	        // check if target[0][0] is SVG
	        else if (target[0] && target[0][0] && target[0][0].ownerSVGElement)
	        {
		        target = target[0][0];
	        }

            $(target).off('mouseenter', qTipMouseEnterHandler);
            $(target).one('mouseenter', {qTipOpts: qTipOpts}, qTipMouseEnterHandler);
        } else {
            console.error('qTip target is not defined.');
        }
    }

    function qTipMouseEnterHandler(event) {
        var opts = {
            show: {ready: true},
            hide: {fixed: true, delay: 100},
            style: {classes: 'qtip-light qtip-rounded qtip-shadow', tip: true},
            position: {my: 'top left', at: 'bottom right', viewport: $(window)}
        };

        var qTipOpts = event.data.qTipOpts;
        jQuery.extend(true, opts, qTipOpts);

        $(this).qtip(opts);
    }

    function baseMutationMapperOpts()
    {
        return {
            proxy: {
                // default pdb proxy are now configured for a separate pdb data source
                // this is for backward compatibility
                pdbProxy: {
                    options: {
                        servletName: "get3dPdb.json",
                        listJoiner: " ",
                        subService: false
                    }
                },
                // TODO for now init variant annotation data proxy with full empty data
                // (this will practically disable the genome-nexus connections until it is ready)
                variantAnnotationProxy: {
                    options: {
                        initMode: "full",
                        data: {}
                    }
                }
            }
        };
    }
    
    /**
     * Converts the given string to title case format. Also replaces each
     * underdash with a space.
     *
     * TODO: Need to remove the same function under network-visualization.js
     * @param source    source string to be converted to title case
     */
    function toTitleCase(source)
    {
        var str;

        if (source == null)
        {
            return source;
        }

        // first, trim the string
        str = source.replace(/\s+$/, "");

        // replace each underdash with a space
        str = replaceAll(str, "_", " ");

        // change to lower case
        str = str.toLowerCase();

        // capitalize starting character of each word

        var titleCase = new Array();

        titleCase.push(str.charAt(0).toUpperCase());

        for (var i = 1; i < str.length; i++)
        {
            if (str.charAt(i-1) == ' ')
            {
                titleCase.push(str.charAt(i).toUpperCase());
            }
            else
            {
                titleCase.push(str.charAt(i));
            }
        }

        return titleCase.join("");
    }

    /**
     * Replaces all occurrences of the given string in the source string.
     *
     * TODO: Need to remove the same function under network-visualization.js
     * @param source        string to be modified
     * @param toFind        string to match
     * @param toReplace     string to be replaced with the matched string
     * @return              modified version of the source string
     */
    function replaceAll(source, toFind, toReplace)
    {
        var target = source;
        var index = target.indexOf(toFind);

        while (index != -1)
        {
            target = target.replace(toFind, toReplace);
            index = target.indexOf(toFind);
        }

        return target;
    }
    
    //Get hotspot description. TODO: add type as parameter for different source of hotspot sources.
    function getHotSpotDesc() {
        //Single quote attribute is not supported in mutation view Backbone template.
        //HTML entity is not supported in patient view.
        //Another solution is to use unquoted attribute value which has been
        //supported since HTML2.0
        return "<b>Recurrent Hotspot</b><br/>" +
            "This mutated amino acid was identified as a recurrent hotspot " +
            "(statistically significant) in a population-scale cohort of " +
            "tumor samples of various cancer types using methodology based in " +
            "part on <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/26619011\" target=\"_blank\">" +
            "Chang et al., Nat Biotechnol, 2016</a>.<br/><br/>" +
            "Explore all mutations at " +
            "<a href=\"http://cancerhotspots.org/\" target=\"_blank\">http://cancerhotspots.org/</a>.";
    }
    
    /**
     * This function is used to handle outliers in the data, which will squeeze most of the data to only few bars in the bar chart.
     * It calculates boundary values from the box plot of input array, and that would enable the data to be displayed evenly.
     * @param data - The array of input data.
     * @param inArrayFlag - The option to choose boundary values from the input array.
     */
    function findExtremes(data, inArrayFlag) {

        // Copy the values, rather than operating on references to existing values
        var values = [], smallDataFlag = false;
        _.each(data, function(item){
            if($.isNumeric(item))
                values.push(Number(item));
        });

        // Then sort
        values.sort(function (a, b) {
            return a - b;
        });

        /* Then find a generous IQR. This is generous because if (values.length / 4) 
         * is not an int, then really you should average the two elements on either 
         * side to find q1.
         */
        var q1 = values[Math.floor((values.length / 4))];
        // Likewise for q3. 
        var q3 = values[(Math.ceil((values.length * (3 / 4))) > values.length - 1 ? values.length - 1 : Math.ceil((values.length * (3 / 4))))];
        var iqr = q3 - q1;
        if(values[Math.ceil((values.length * (1 / 2)))] < 0.001)
            smallDataFlag = true;
        // Then find min and max values
        var maxValue, minValue;
        if(q3 < 1){
            maxValue = Number((q3 + iqr * 1.5).toFixed(2));
            minValue = Number((q1 - iqr * 1.5).toFixed(2));
        }else{
            maxValue = Math.ceil(q3 + iqr * 1.5);
            minValue = Math.floor(q1 - iqr * 1.5);
        }
        if(minValue < values[0])minValue = values[0];
        if(maxValue > values[values.length - 1])maxValue = values[values.length - 1];
        //provide the option to choose min and max values from the input array
        if(inArrayFlag){
            var i = 0;
            if(values.indexOf(minValue) === -1){
                while(minValue > values[i] && minValue > values[i+1]){
                    i++;
                }
                minValue = values[i+1];
            }
            i = values.length - 1;
            if(values.indexOf(maxValue) === -1){
                while(maxValue < values[i] && maxValue < values[i-1]){
                    i--;
                }
                maxValue = values[i-1];
            }
        }
        
        return [minValue, maxValue, smallDataFlag];
    }
    
    return {
        toPrecision: toPrecision,
        getObjectLength: getObjectLength,
        checkNullOrUndefined: checkNullOrUndefined,
        uniqueElementsOfArray: uniqueElementsOfArray,
        arrayToAssociatedArrayIndices: arrayToAssociatedArrayIndices,
        alterAxesAttrForPDFConverter: alterAxesAttrForPDFConverter,
        lcss: lcss,
	    b64ToByteArrays: b64ToByteArrays,
        browser: detectBrowser(), // returning the browser object, not the function itself
        getWindowOrigin: getOrigin,
        sortByAttribute: sortByAttribute,
        safeProperty: safeProperty,
        autoHideOnMouseLeave: autoHideOnMouseLeave,
        swapElement: swapElement,
	    getTargetWindow: getTargetWindow,
	    getTargetDocument: getTargetDocument,
        getLinkToPatientView: getLinkToPatientView,
        getLinkToSampleView: getLinkToSampleView,
        addTargetedQTip: addTargetedQTip,
        baseMutationMapperOpts: baseMutationMapperOpts,
        toTitleCase: toTitleCase,
        getHotSpotDesc: getHotSpotDesc,
        replaceAll: replaceAll,
        findExtremes: findExtremes
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
