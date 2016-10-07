/*! @source http://purl.eligrey.com/github/FileSaver.js/blob/master/FileSaver.js */
var saveAs=saveAs||typeof navigator!=="undefined"&&navigator.msSaveOrOpenBlob&&navigator.msSaveOrOpenBlob.bind(navigator)||function(view){"use strict";if(typeof navigator!=="undefined"&&/MSIE [1-9]\./.test(navigator.userAgent)){return}var doc=view.document,get_URL=function(){return view.URL||view.webkitURL||view},save_link=doc.createElementNS("http://www.w3.org/1999/xhtml","a"),can_use_save_link="download"in save_link,click=function(node){var event=doc.createEvent("MouseEvents");event.initMouseEvent("click",true,false,view,0,0,0,0,0,false,false,false,false,0,null);node.dispatchEvent(event)},webkit_req_fs=view.webkitRequestFileSystem,req_fs=view.requestFileSystem||webkit_req_fs||view.mozRequestFileSystem,throw_outside=function(ex){(view.setImmediate||view.setTimeout)(function(){throw ex},0)},force_saveable_type="application/octet-stream",fs_min_size=0,arbitrary_revoke_timeout=500,revoke=function(file){var revoker=function(){if(typeof file==="string"){get_URL().revokeObjectURL(file)}else{file.remove()}};if(view.chrome){revoker()}else{setTimeout(revoker,arbitrary_revoke_timeout)}},dispatch=function(filesaver,event_types,event){event_types=[].concat(event_types);var i=event_types.length;while(i--){var listener=filesaver["on"+event_types[i]];if(typeof listener==="function"){try{listener.call(filesaver,event||filesaver)}catch(ex){throw_outside(ex)}}}},FileSaver=function(blob,name){var filesaver=this,type=blob.type,blob_changed=false,object_url,target_view,dispatch_all=function(){dispatch(filesaver,"writestart progress write writeend".split(" "))},fs_error=function(){if(blob_changed||!object_url){object_url=get_URL().createObjectURL(blob)}if(target_view){target_view.location.href=object_url}else{var new_tab=view.open(object_url,"_blank");if(new_tab==undefined&&typeof safari!=="undefined"){view.location.href=object_url}}filesaver.readyState=filesaver.DONE;dispatch_all();revoke(object_url)},abortable=function(func){return function(){if(filesaver.readyState!==filesaver.DONE){return func.apply(this,arguments)}}},create_if_not_found={create:true,exclusive:false},slice;filesaver.readyState=filesaver.INIT;if(!name){name="download"}if(can_use_save_link){object_url=get_URL().createObjectURL(blob);save_link.href=object_url;save_link.download=name;click(save_link);filesaver.readyState=filesaver.DONE;dispatch_all();revoke(object_url);return}if(view.chrome&&type&&type!==force_saveable_type){slice=blob.slice||blob.webkitSlice;blob=slice.call(blob,0,blob.size,force_saveable_type);blob_changed=true}if(webkit_req_fs&&name!=="download"){name+=".download"}if(type===force_saveable_type||webkit_req_fs){target_view=view}if(!req_fs){fs_error();return}fs_min_size+=blob.size;req_fs(view.TEMPORARY,fs_min_size,abortable(function(fs){fs.root.getDirectory("saved",create_if_not_found,abortable(function(dir){var save=function(){dir.getFile(name,create_if_not_found,abortable(function(file){file.createWriter(abortable(function(writer){writer.onwriteend=function(event){target_view.location.href=file.toURL();filesaver.readyState=filesaver.DONE;dispatch(filesaver,"writeend",event);revoke(file)};writer.onerror=function(){var error=writer.error;if(error.code!==error.ABORT_ERR){fs_error()}};"writestart progress write abort".split(" ").forEach(function(event){writer["on"+event]=filesaver["on"+event]});writer.write(blob);filesaver.abort=function(){writer.abort();filesaver.readyState=filesaver.DONE};filesaver.readyState=filesaver.WRITING}),fs_error)}),fs_error)};dir.getFile(name,{create:false},abortable(function(file){file.remove();save()}),abortable(function(ex){if(ex.code===ex.NOT_FOUND_ERR){save()}else{fs_error()}}))}),fs_error)}),fs_error)},FS_proto=FileSaver.prototype,saveAs=function(blob,name){return new FileSaver(blob,name)};FS_proto.abort=function(){var filesaver=this;filesaver.readyState=filesaver.DONE;dispatch(filesaver,"abort")};FS_proto.readyState=FS_proto.INIT=0;FS_proto.WRITING=1;FS_proto.DONE=2;FS_proto.error=FS_proto.onwritestart=FS_proto.onprogress=FS_proto.onwrite=FS_proto.onabort=FS_proto.onerror=FS_proto.onwriteend=null;return saveAs}(typeof self!=="undefined"&&self||typeof window!=="undefined"&&window||this.content);if(typeof module!=="undefined"&&module.exports){module.exports.saveAs=saveAs}else if(typeof define!=="undefined"&&define!==null&&define.amd!=null){define([],function(){return saveAs})}

if (cbio === undefined) {
  var cbio = {};
}

cbio.util = (function() {
  var deepCopyObject = function(obj) {
    return $.extend(true, ($.isArray(obj) ? [] : {}), obj);
  };
  var objectValues = function(obj) {
    return Object.keys(obj).map(function(key) {
      return obj[key];
    });
  };
  var objectKeyDifference = function(from, by) {
    var ret = {};
    var from_keys = Object.keys(from);
    for (var i = 0; i < from_keys.length; i++) {
      if (!by[from_keys[i]]) {
        ret[from_keys[i]] = true;
      }
    }
    return ret;
  };
  var objectKeyValuePairs = function(obj) {
    return Object.keys(obj).map(function(key) {
      return [key, obj[key]];
    });
  };
  var objectKeyUnion = function(list_of_objs) {
    var union = {};
    for (var i = 0; i < list_of_objs.length; i++) {
      var keys = Object.keys(list_of_objs[i]);
      for (var j = 0; j < keys.length; j++) {
        union[keys[j]] = true;
      }
    }
    return union;
  };
  var objectKeyIntersection = function(list_of_objs) {
    var intersection = {};
    for (var i = 0; i < list_of_objs.length; i++) {
      if (i === 0) {
        var keys = Object.keys(list_of_objs[0]);
        for (var j = 0; j < keys.length; j++) {
          intersection[keys[j]] = true;
        }
      } else {
        var obj = list_of_objs[i];
        var keys = Object.keys(intersection);
        for (var j = 0; j < keys.length; j++) {
          if (!obj[keys[j]]) {
            delete intersection[keys[j]];
          }
        }
      }
    }
    return intersection;
  };
  var stringListToObject = function(list) {
    var ret = {};
    for (var i = 0; i < list.length; i++) {
      ret[list[i]] = true;
    }
    return ret;
  };
  var stringListDifference = function(from, by) {
    return Object.keys(
      objectKeyDifference(
        stringListToObject(from),
        stringListToObject(by)));
  };
  var stringListUnion = function(list_of_string_lists) {
    return Object.keys(
      objectKeyUnion(
        list_of_string_lists.map(function(string_list) {
          return stringListToObject(string_list);
        })
      ));
  };
  var stringListUnique = function(list) {
    return Object.keys(stringListToObject(list));
  };
  var flatten = function(list_of_lists) {
    return list_of_lists.reduce(function(a, b) {
      return a.concat(b);
    }, []);
  };

  var makeCachedPromiseFunction = function(fetcher) {
    // In: fetcher, a function that takes a promise as an argument, and resolves it with the desired data
    // Out: a function which returns a promise that resolves with the desired data, deep copied
    //  The idea is that the fetcher is only ever called once, even if the output function
    //  of this method is called again while it's still waiting.
    var fetch_promise = new $.Deferred();
    var fetch_initiated = false;
    return function() {
      var def = new $.Deferred();
      if (!fetch_initiated) {
        fetch_initiated = true;
        fetcher(this, fetch_promise);
      }
      fetch_promise.then(function(data) {
        def.resolve(deepCopyObject(data));
      });
      return def.promise();
    };
  };

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
      if (Object.prototype.hasOwnProperty.call(object, i)) {
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
    if (checkNullOrUndefined(offset)) {
      offset = 0;
    }
    var aa = {};
    for (var i = 0, n = arr.length; i < n; i++) {
      aa[arr[i]] = i + offset;
    }
    return aa;
  };

  var uniqueElementsOfArray = function(arr) {
    var ret = [];
    var aa = {};
    for (var i = 0, n = arr.length; i < n; i++) {
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

    if (rollback) {
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
  var lcss = function(str1, str2) {
    var i = 0;

    while (i < str1.length && i < str2.length) {
      if (str1[i] === str2[i]) {
        i++;
      }
      else {
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
  var detectBrowser = function() {
    var browser = {};
    var uagent = navigator.userAgent.toLowerCase();

    browser.firefox = /mozilla/.test(uagent) &&
      /firefox/.test(uagent);

    browser.mozilla = browser.firefox; // this is just an alias

    browser.chrome = /webkit/.test(uagent) &&
      /chrome/.test(uagent);

    browser.safari = /applewebkit/.test(uagent) &&
      /safari/.test(uagent) && !/chrome/.test(uagent);

    browser.opera = /opera/.test(uagent);

    browser.msie = /msie/.test(uagent);

    browser.version = "";

    // check for IE 11
    if (!(browser.msie ||
      browser.firefox ||
      browser.chrome ||
      browser.safari ||
      browser.opera)) {
      // TODO probably we need to update this for future IE versions
      if (/trident/.test(uagent)) {
        browser.msie = true;
        browser.version = 11;
      }
    }

    if (browser.version === "") {
      for (var x in browser) {
        if (browser[x]) {
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
  var getOrigin = function() {
    var origin = window.location.origin;

    if (!origin) {
      origin = window.location.protocol + "//" +
        window.location.hostname +
        (window.location.port ? ':' + window.location.port : '');
    }

    return origin;
  };

  var sortByAttribute = function(objs, attrName) {
    function compare(a, b) {
      if (a[attrName] < b[attrName]) {
        return -1;
      }
      if (a[attrName] > b[attrName]) {
        return 1;
      }
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
  var safeProperty = function(property) {
    return property.replace(/[^a-zA-Z0-9-]/g, '_');
  };

  /**
   * Hides the child html element on mouse leave, and shows on
   * mouse enter. This function is designed to hide a child
   * element within a parent element.
   *
   * @param parentElement target of mouse events
   * @param childElement  element to show/hide
   */
  function autoHideOnMouseLeave(parentElement, childElement) {
    $(parentElement).mouseenter(function(evt) {
      childElement.fadeIn({
        complete: function() {
          $(this).css({"visibility": "visible"});
          $(this).css({"display": "inline"});
        }
      });
    });

    $(parentElement).mouseleave(function(evt) {
      // fade out without setting display to none
      childElement.fadeOut({
        complete: function() {
          // fade out uses hide() function, but it may change
          // the size of the parent element
          // so this is a workaround to prevent resize
          // due to display: "none"
          $(this).css({"visibility": "hidden"});
          $(this).css({"display": "inline"});
        }
      });
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
  function getTargetWindow(id) {
    var frame = document.getElementById(id);
    var targetWindow = frame;

    if (frame.contentWindow) {
      targetWindow = frame.contentWindow;
    }

    return targetWindow;
  }

  /**
   * Returns the content document for the given target frame.
   *
   * @param id    id of the target frame
   */
  function getTargetDocument(id) {
    var frame = document.getElementById(id);
    var targetDocument = frame.contentDocument;

    if (!targetDocument && frame.contentWindow) {
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
    if (target) {
      // check if target[0] is SVG
      if (target[0] && target[0].ownerSVGElement) {
        target = target[0];
      }
      // check if target[0][0] is SVG
      else if (target[0] && target[0][0] && target[0][0].ownerSVGElement) {
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

  function baseMutationMapperOpts() {
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
  function toTitleCase(source) {
    var str;

    if (source == null) {
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

    for (var i = 1; i < str.length; i++) {
      if (str.charAt(i - 1) == ' ') {
        titleCase.push(str.charAt(i).toUpperCase());
      }
      else {
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
  function replaceAll(source, toFind, toReplace) {
    var target = source;
    var index = target.indexOf(toFind);

    while (index != -1) {
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
    _.each(data, function(item) {
      if ($.isNumeric(item)) {
        values.push(Number(item));
      }
    });

    // Then sort
    values.sort(function(a, b) {
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
    if (values[Math.ceil((values.length * (1 / 2)))] < 0.001) {
      smallDataFlag = true;
    }
    // Then find min and max values
    var maxValue, minValue;
    if (q3 < 1) {
      maxValue = Number((q3 + iqr * 1.5).toFixed(2));
      minValue = Number((q1 - iqr * 1.5).toFixed(2));
    } else {
      maxValue = Math.ceil(q3 + iqr * 1.5);
      minValue = Math.floor(q1 - iqr * 1.5);
    }
    if (minValue < values[0]) {
      minValue = values[0];
    }
    if (maxValue > values[values.length - 1]) {
      maxValue = values[values.length - 1];
    }
    //provide the option to choose min and max values from the input array
    if (inArrayFlag) {
      var i = 0;
      if (values.indexOf(minValue) === -1) {
        while (minValue > values[i] && minValue > values[i + 1]) {
          i++;
        }
        minValue = values[i + 1];
      }
      i = values.length - 1;
      if (values.indexOf(maxValue) === -1) {
        while (maxValue < values[i] && maxValue < values[i - 1]) {
          i--;
        }
        maxValue = values[i - 1];
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
    findExtremes: findExtremes,
    deepCopyObject: deepCopyObject,
    makeCachedPromiseFunction: makeCachedPromiseFunction
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

// http://bootstrap-notify.remabledesigns.com/
function Notification() {

    // default settings
    var settings = {
        message_type: "success", //success, warning, danger, info
        allow_dismiss: false,
        newest_on_top: false,
        placement_from: "top",
        placement_align: "right",
        spacing: 10,
        delay: 5000,
        timer: 1000,
        custom_class:"geneAddedNotification"
    };

    // create a notification
    this.createNotification = function(notificationMessage, options) {
        //if the options isn’t null extend defaults with user options.
        if (options) $.extend(settings, options);

        // create the notification
        $.notify({
            message: notificationMessage,
        }, {
            // settings
            element: 'body',
            type: settings.message_type,
            allow_dismiss: settings.allow_dismiss,
            newest_on_top: settings.newest_on_top,
            showProgressbar: false,
            placement: {
                from: settings.placement_from,
                align: settings.placement_align
            },
            spacing: settings.spacing,
            z_index: 1031,
            delay: settings.delay,
            timer: settings.timer,
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            template: '<div data-notify="container" class="col-xs-11 col-sm-3 alert alert-{0} '+settings.custom_class+'" role="alert">' +
            '<button type="button" style="display: none" aria-hidden="true" class="close" data-notify="dismiss" >×</button>' +
            '<span data-notify="icon"></span> ' +
            '<span data-notify="title">{1}</span> ' +
            '<span data-notify="message">{2}</span>' +
            '<div class="progress" data-notify="progressbar">' +
            '<div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div>' +
            '</div>' +
            '<a href="{3}" target="{4}" data-notify="url"></a>' +
            '</div>'
        });


    }
}

// based on gene-symbol-validator.js
//function GeneValidator(geneAreaId, emptyAreaMessage, updateGeneCallback){
function GeneValidator(geneAreaId, geneModel){
    var self = this;
    var nrOfNotifications=0;

    var showNotification=true;

    this.init = function(){
        console.log(new Date() + " init called for "+geneAreaId);
        // create a debounced validator
        var debouncedValidation = _.debounce(this.validateGenes, 1000);
        $(geneAreaId).bind('input propertychange', debouncedValidation);
    }

    this.validateGenes = function(callback, show){
        console.log(new Date() + " validating genes in "+geneAreaId);

        // store whether to show notifications
        showNotification=(show===undefined)?true:show;

        // clear all existing notifications
        if(showNotification) clearAllNotifications();

        // clean the textArea string, removing doubles and non-word characters (except -)
        var genesStr = geneModel.getCleanGeneString(",");

        var genes = [];
        var allValid = true;

        $.post(window.cbioURL + 'CheckGeneSymbol.json', { 'genes': genesStr })
            .done(function(symbolResults) {
                // If the number of genes is more than 100, show an error
                if(symbolResults.length > 100) {
                    addNotification("<b>You have entered more than 100 genes.</b><br>Please enter fewer genes for better performance", "danger");
                    allValid=false;
                }

                // handle each symbol found
                for(var j=0; j < symbolResults.length; j++) {
                    var valid = handleSymbol(symbolResults[j])
                    if(!valid) {
                        allValid = false;
                    }
                }
            })
            .fail(function(xhr,  textStatus, errorThrown){
                addNotification("There was a problem: "+errorThrown, "danger");
                allValid=false;
            })
            .always(function(){
                // if not all valid, focus on the gene array for focusin trigger
                if(!allValid) $(geneAreaId).focus();
                // in case a submit was pressed, use the callback
                if($.isFunction(callback)) callback(allValid);
            });
    }

    // return whether there are any active notifications
    this.noActiveNotifications = function(){
        return nrOfNotifications===0;
    }

    this.replaceAreaValue = function(geneName, newValue){
        var regexp = new RegExp("\\b"+geneName+"\\b","g");
        var genesStr = geneModel.getCleanGeneString();
        geneModel.set("geneString", genesStr.replace(regexp, newValue).trim());
    }

    // create a notification of a certain type
    function addNotification(message, message_type){
        notificationSettings.message_type = message_type;
        new Notification().createNotification(message, notificationSettings);
        nrOfNotifications = $(".alert").length;
    }

    function clearAllNotifications(){
        // select the notifications of interest
        // kill their animations to prevent them from blocking space, destroy any qtips remaining and call click to
        // make the notifications disappear
        $(".geneValidationNotification").css("animation-iteration-count", "0");
        $(".geneValidationNotification").qtip("destroy");
        $(".geneValidationNotification").find("button").click();
        nrOfNotifications=0;
    }

    // handle one symbol
    function handleSymbol(aResult){
        var valid = false;

        // 1 symbol
        if(aResult.symbols.length == 1) {
            if(aResult.symbols[0].toUpperCase() != aResult.name.toUpperCase() && showNotification)
                handleSynonyms(aResult);
            else
                valid=true;
        }
        else if(aResult.symbols.length > 1 && showNotification)
            handleMultiple(aResult)
        else if(showNotification)
            handleSymbolNotFound(aResult);

        return valid;
    }

    // case where we're dealing with an ambiguous gene symbol
    function handleMultiple(aResult){
        var gene = aResult.name;
        var symbols = aResult.symbols;

        var tipText = "Ambiguous gene symbol. Click on one of the alternatives to replace it.";
        var notificationHTML="<span>Ambiguous gene symbol - "+gene+" ";

        // create the dropdown
        var nameSelect = $("<select id="+gene+">").addClass("geneSelectBox").attr("name", gene);
        $("<option>").attr("value", "").html("select a symbol").appendTo(nameSelect);
        for(var k=0; k < symbols.length; k++) {
            var aSymbol = symbols[k];
            // add class and data-notify to allow us to dismiss the notification
            var anOption = $("<option class='close' data-notify='dismiss'>").attr("value", aSymbol).html(aSymbol);
            anOption.appendTo(nameSelect);
        }

        notificationHTML+=nameSelect.prop('outerHTML')+"</span>";
        addNotification(notificationHTML, "warning");

        // when the dropdown is changed
        $("#"+gene).change(function() {
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("name"), $(this).attr("value"));

            // destroy the qtip if it's still there
            $(this).qtip("destroy");

            // emulate a click on the selected child to dismiss the notification
            this.children[this.selectedIndex].click();
        });

        addQtip(gene, tipText);
    }


    // case when the symbol has synonyms
    function handleSynonyms(aResult){
        var gene = aResult.name;
        var trueSymbol = aResult.symbols[0];
        var tipText = "'" + gene + "' is a synonym for '" + trueSymbol + "'. "
            + "Click here to replace it with the official symbol.";

        var notificationHTML=$("<span>Symbol synonym found - "+gene + ":" + trueSymbol+"</span>");
        notificationHTML.attr({
                'id': gene,
                'symbol': trueSymbol,
                'class':'close',
                'data-notify':'dismiss'
            });

        addNotification(notificationHTML.prop('outerHTML'), "warning");

        // add click event to our span
        // due to the class and data-notify, the click also removes the notification
        $("#"+gene).click(function(){
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("id"), $(this).attr("symbol"));

            // destroy the qtip if it's still here
            $(this).qtip("destroy");
        });

        addQtip(gene, tipText);
    }

    // case when the symbol was not found
    function handleSymbolNotFound(aResult){
        var gene = aResult.name;
        var tipText = "Could not find gene symbol "+gene+". Click to remove it from the gene list.";

        var notificationHTML=$("<span>Symbol not found - "+gene+"</span>");
        notificationHTML.attr({
            'id': gene,
            'class':'close',
            'data-notify':'dismiss'
        });

        addNotification(notificationHTML.prop('outerHTML'), "warning");

        // add click event to our span
        // due to the class and data-notify, the click also removes the notification
        $("#"+gene).click(function(){
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("id"), "");

            // destroy the qtip if it's still here
            $(this).qtip("destroy");
        });

        addQtip(gene, tipText);
    }

    // add a qtip to some identifier
    function addQtip(id, tipText){
        $("#"+id).qtip({
            content: {text: tipText},
            position: {my: 'top center', at: 'bottom center', viewport: $(window)},
            style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
        });
    }


    // notification settings
    var notificationSettings = {
        message_type: "warning",
        custom_class: "geneValidationNotification",
        allow_dismiss: true,
        spacing: 10,
        delay: 0,
        timer: 0
    };

    // when new object is created, called init();
    this.init();
}
function QueryByGeneUtil() {

    // add the field
    function addFormField(formId, itemName, itemValue) {
        $('<input>').attr({
            type: 'hidden',
            value: itemValue,
            name: itemName
        }).appendTo(formId)
    }

    // fields required for the study-view and their defaults to be able to query
    this.addStudyViewFields = function (cancerStudyId, mutationProfileId, cnaProfileId) {
        var formId = "#iviz-form";
        addFormField(formId, "gene_set_choice", "user-defined-list");
        addFormField(formId, "gene_list", QueryByGeneTextArea.getGenes());

        addFormField(formId, "cancer_study_list", cancerStudyId);
        addFormField(formId, "Z_SCORE_THRESHOLD", 2.0);
        addFormField(formId, "genetic_profile_ids_PROFILE_MUTATION_EXTENDED", mutationProfileId);
        addFormField(formId, "genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION", cnaProfileId);
        addFormField(formId, "clinical_param_selection", null);
        addFormField(formId, "data_priority", 0);
        addFormField(formId, "tab_index", "tab_visualize");
        addFormField(formId, "Action", "Submit");
    }
}


var GenelistModel = Backbone.Model.extend({
    defaults: {
        geneString: ""
    },

    isEmptyModel: function(){
       return this.get("geneString").length==0;
    },

    getCleanGeneString: function(delim){
        delim = delim || " ";
        return this.getCleanGeneArray().join(delim);
    },

    getCleanGeneArray: function(){
        return $.unique(this.removeEmptyElements(this.get("geneString").toUpperCase().split(/[^a-zA-Z0-9-]/))).reverse();
    },

    removeEmptyElements: function (array){
        return array.filter(function(el){ return el !== "" });
    }
});

var QueryByGeneTextArea  = (function() {
    var geneModel = new GenelistModel();
    var areaId;
    var updateGeneCallBack;
    var geneValidator;
    var emptyAreaText = "query genes - click to expand";

    // when the textarea does not have focus, the text shown in the (smaller) textarea
    // is gene1, gene2 and x more
    function createFocusOutText(){
        var geneList = geneModel.getCleanGeneArray();
        var focusOutText = geneList[0];
        var stringLength = focusOutText.length;

        // build the string to be shown
        for(var i=1; i<geneList.length; i++){
            stringLength+=geneList[i].length+2;
            // if the string length is bigger than 15 characters add the "and x more"
            if(stringLength>15) {
                focusOutText+= " and "+(geneList.length-i)+" more";
                break;
            }
            focusOutText+=", "+geneList[i];
        }
        return focusOutText;
    }

    // set the textarea text when focus is lost (and no notifications are open)
    function setFocusOutText(){
        var focusOutText=emptyAreaText;
        // if there are genes build the focusText
        if(!geneModel.isEmptyModel()) focusOutText = createFocusOutText();
        setFocusoutColour();
        $(areaId).val(focusOutText);
    }

    // if the geneList is empty, we use a gray colour, otherwise black
    function setFocusoutColour(){
        if(!geneModel.isEmptyModel()) $(areaId).css("color", "black");
        else $(areaId).css("color", "darkgrey");
    }

    // when the textarea has focus, the contents is the geneList's contents separated by spaces
    function setFocusInText(){
        $(areaId).css("color", "black");
        $(areaId).val(geneModel.getCleanGeneString());
    }

    function isEmpty(){
        return geneModel.isEmptyModel();
    }

    function getGenes(){
        return geneModel.getCleanGeneString();
    }

    // addRemoveGene is used when someone clicks on a gene in a table (StudyViewInitTables)
    function addRemoveGene (gene){
        var geneList = geneModel.getCleanGeneArray();

        // if the gene is not yet in the list, add it and create a notification
        if(geneList.indexOf(gene)==-1) {
            geneList.push(gene);
            geneModel.set("geneString", geneModel.getCleanGeneString()+" "+gene);
            new Notification().createNotification(gene+" added to your query");
        }
        // if the gene is in the list, remove it and create a notification
        else{
            var index = geneList.indexOf(gene);
            geneList.splice(index, 1);
            geneModel.set("geneString", geneList.join(" "));
            new Notification().createNotification(gene+" removed from your query");
        }
        // if there are active notifications, the textarea is still expanded and the contents
        // should reflect this
        if(geneValidator.noActiveNotifications()) setFocusOutText();
        else setFocusInText();

        // update the highlighting in the tables
        //if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
    }

    // used by the focusOut event and by the updateTextArea
    function setFocusOut(){
        // if there are no active notifications and the textarea does not have focus
        if(geneValidator.noActiveNotifications() && !$(areaId).is(":focus")){
            // switch from focusIn to focusOut and set the focus out text
            $(areaId).switchClass("expandFocusIn", "expandFocusOut", 500);
            setFocusOutText();
        }

        // update the gene tables for highlighting
        if(updateGeneCallBack != undefined) updateGeneCallBack(geneModel.getCleanGeneArray());
    }

    function validateGenes(callback){
        geneValidator.validateGenes(callback, false);
    }

    function updateTextArea(){
        // set display text - this will not fire the input propertychange
        $(areaId).val(geneModel.get("geneString"));
        setFocusOut();
    }

    function updateModel(){
        // check whether the model actually has to be updated
        if(geneModel.get("geneString")!=$(areaId).val()) {
            geneModel.set("geneString", $(areaId).val());
        }
    }

    // initialise events
    function initEvents(){
        // when user types in the textarea, update the model
        $(areaId).bind('input propertychange', updateModel);

        // when the model is changed, update the textarea
        geneModel.on("change", updateTextArea);

        // add the focusin event
        $(areaId).focusin(function () {
            $(this).switchClass("expandFocusOut", "expandFocusIn", 500);
            setFocusInText();
        });

        // add the focusout event
        $(areaId).focusout(function () {
            setFocusOut();
        });

        // create the gene validator
        geneValidator = new GeneValidator(areaId, geneModel);
    }


    function init(areaIdP, updateGeneCallBackP){
        areaId = areaIdP;
        updateGeneCallBack = updateGeneCallBackP;
        setFocusOutText();
        initEvents();
    }

    return{
        init: init,
        addRemoveGene: addRemoveGene,
        getGenes: getGenes,
        isEmpty: isEmpty,
        validateGenes: validateGenes
    }

})();


'use strict';

window.EnhancedFixedDataTableSpecial = (function() {
// Data button component
  var FileGrabber = React.createClass({displayName: "FileGrabber",
    // Saves table content to a text file
    saveFile: function() {
      var formatData = this.state.formatData || this.props.content();
      this.state.formatData = formatData;

      var blob = new Blob([formatData], {type: 'text/plain'});
      var fileName = this.props.downloadFileName ? this.props.downloadFileName : "data.txt";

      var downloadLink = document.createElement("a");
      downloadLink.download = fileName;
      downloadLink.innerHTML = "Download File";
      if (window.webkitURL) {
        // Chrome allows the link to be clicked
        // without actually adding it to the DOM.
        downloadLink.href = window.webkitURL.createObjectURL(blob);
      }
      else {
        // Firefox requires the link to be added to the DOM
        // before it can be clicked.
        downloadLink.href = window.URL.createObjectURL(blob);
        downloadLink.onclick = function(event) {
          document.body.removeChild(event.target);
        };
        downloadLink.style.display = "none";
        document.body.appendChild(downloadLink);
      }

      downloadLink.click();
    },

    getInitialState: function() {
      return {
        formatData: ''
      };
    },

    render: function() {
      return (
        React.createElement("button", {className: "btn btn-default", onClick: this.saveFile},
          "DATA")
      );
    }
  });

// Copy button component
  var ClipboardGrabber = React.createClass({displayName: "ClipboardGrabber",
    click: function() {
      if (!this.state.formatData) {
        var client = new ZeroClipboard($("#copy-button")), content = this.props.content();
        this.state.formatData = content;
        client.on("ready", function(readyEvent) {
          client.on("copy", function(event) {
            event.clipboardData.setData('text/plain', content);
          });
        });
      }
      this.notify();
    },

    notify: function() {
      $.notify({
        message: 'Copied.'
      }, {
        type: 'success',
        animate: {
          enter: 'animated fadeInDown',
          exit: 'animated fadeOutUp'
        },
        delay: 1000
      });
    },

    getInitialState: function() {
      return {
        formatData: ''
      };
    },

    render: function() {
      return (
        React.createElement("button", {className: "btn btn-default", id: "copy-button",
            onClick: this.click},
          "COPY")
      );
    }
  });

// Container of FileGrabber and ClipboardGrabber
  var DataGrabber = React.createClass({displayName: "DataGrabber",
    // Prepares table content data for download or copy button
    prepareContent: function() {
      var content = [], cols = this.props.cols, rows = this.props.rows;

      _.each(cols, function(e) {
        content.push((e.displayName || 'Unknown'), '\t');
      });
      content.pop();

      _.each(rows, function(row) {
        content.push('\r\n');
        _.each(cols, function(col) {
          content.push(row[col.name], '\t');
        });
        content.pop();
      });
      return content.join('');
    },

    render: function() {
      var getData = this.props.getData;
      if (getData === "NONE") {
        return React.createElement("div", null);
      }

      var content = this.prepareContent;

      return (
        React.createElement("div", null,
          React.createElement("div", {className: "EFDT-download-btn EFDT-top-btn"},

            getData != "COPY" ? React.createElement(FileGrabber, {content: content,
              downloadFileName: this.props.downloadFileName}) :
              React.createElement("div", null)

          ),
          React.createElement("div", {className: "EFDT-download-btn EFDT-top-btn"},

            getData != "DOWNLOAD" ? React.createElement(ClipboardGrabber, {content: content}) :
              React.createElement("div", null)

          )
        )
      );
    }
  });

// Wrapper of qTip for string
// Generates qTip when string length is larger than 20
  var QtipWrapper = React.createClass({displayName: "QtipWrapper",
    render: function() {
      var label = this.props.label, qtipFlag = false;
      var shortLabel = this.props.shortLabel;
      var className = this.props.className || '';
      var field = this.props.field;
      var color = this.props.color || '';
      var tableType = this.props.tableType || '';

      if (label && shortLabel && label.toString().length > shortLabel.toString().length) {
        qtipFlag = true;
      }
      return (
        React.createElement("span", {className: className + (qtipFlag?" hasQtip " : '') +
          ((field === 'altType' && ['mutatedGene', 'cna'].indexOf(tableType) !== -1) ? (label === 'AMP' ? ' alt-type-red' : ' alt-type-blue') : ''),
            "data-qtip": label},

          (field === 'name' && tableType === 'pieLabel') ? (
            React.createElement("svg", {width: "15", height: "10"},
              React.createElement("g", null,
                React.createElement("rect", {height: "10", width: "10", fill: color})
              )
            )
          ) : '',

          shortLabel
        )
      );
    }
  });

// Column show/hide component
  var ColumnHider = React.createClass({displayName: "ColumnHider",
    tableCols: [],// For the checklist

    // Updates column show/hide settings
    hideColumns: function(list) {
      var cols = this.props.cols, filters = this.props.filters;
      for (var i = 0; i < list.length; i++) {
        cols[i].show = list[i].isChecked;
        if (this.props.hideFilter) {
          filters[cols[i].name].hide = !cols[i].show;
        }
      }
      this.props.updateCols(cols, filters);
    },

    // Prepares tableCols
    componentWillMount: function() {
      var cols = this.props.cols;
      var colsL = cols.length;
      for (var i = 0; i < colsL; i++) {
        this.tableCols.push({
          id: cols[i].name,
          label: cols[i].displayName,
          isChecked: cols[i].show
        });
      }
    },

    componentDidMount: function() {
      var hideColumns = this.hideColumns;

      // Dropdown checklist
      $('#hide_column_checklist')
        .dropdownCheckbox({
          data: this.tableCols,
          autosearch: true,
          title: "Show / Hide Columns",
          hideHeader: false,
          showNbSelected: true
        })
        // Handles dropdown checklist event
        .on("change", function() {
          var list = ($("#hide_column_checklist").dropdownCheckbox("items"));
          hideColumns(list);
        });
    },

    render: function() {
      return (
        React.createElement("div", {id: "hide_column_checklist", className: "EFDT-top-btn"})
      );
    }
  });

// Choose fixed columns component
  var PinColumns = React.createClass({displayName: "PinColumns",
    tableCols: [],// For the checklist

    // Updates fixed column settings
    pinColumns: function(list) {
      var cols = this.props.cols;
      for (var i = 0; i < list.length; i++) {
        cols[i].fixed = list[i].isChecked;
      }
      this.props.updateCols(cols, this.props.filters);
    },

    // Prepares tableCols
    componentWillMount: function() {
      var cols = this.props.cols;
      var colsL = cols.length;
      for (var i = 0; i < colsL; i++) {
        this.tableCols.push({
          id: cols[i].name,
          label: cols[i].displayName,
          isChecked: cols[i].fixed
        });
      }
    },

    componentDidMount: function() {
      var pinColumns = this.pinColumns;

      // Dropdown checklist
      $("#pin_column_checklist")
        .dropdownCheckbox({
          data: this.tableCols,
          autosearch: true,
          title: "Choose Fixed Columns",
          hideHeader: false,
          showNbSelected: true
        })
        // Handles dropdown checklist event
        .on("change", function() {
          var list = ($("#pin_column_checklist").dropdownCheckbox("items"));
          pinColumns(list);
        });
    },

    render: function() {
      return (
        React.createElement("div", {id: "pin_column_checklist", className: "EFDT-top-btn"})
      );
    }
  });

// Column scroller component
  var ColumnScroller = React.createClass({displayName: "ColumnScroller",
    // Scrolls to user selected column
    scrollToColumn: function(e) {
      var name = e.target.value, cols = this.props.cols, index, colsL = cols.length;
      for (var i = 0; i < colsL; i++) {
        if (name === cols[i].name) {
          index = i;
          break;
        }
      }
      this.props.updateGoToColumn(index);
    },

    render: function() {
      return (
        React.createElement(Chosen, {"data-placeholder": "Column Scroller",
            onChange: this.scrollToColumn},

          this.props.cols.map(function(col) {
            return (
              React.createElement("option", {title: col.displayName, value: col.name},
                React.createElement(QtipWrapper, {label: col.displayName})
              )
            );
          })

        )
      );
    }
  });

// Filter component
  var Filter = React.createClass({displayName: "Filter",
    getInitialState: function() {
      return {key: ''};
    },
    handleChange: function(event) {
      this.setState({key: event.target.value});
      this.props.onFilterKeywordChange(event);
    },
    componentWillUpdate: function() {
      if (this.props.type === 'STRING') {
        if (!_.isUndefined(this.props.filter) && this.props.filter.key !== this.state.key && this.props.filter.key === '' && this.props.filter.reset) {
          this.state.key = '';
          this.props.filter.reset = false;
        }
      }
    },
    render: function() {
      if (this.props.type === "NUMBER" || this.props.type === "PERCENTAGE") {
        return (
          React.createElement("div", {className: "EFDT-header-filters"},
            React.createElement("span", {id: "range-"+this.props.name}),

            React.createElement("div", {className: "rangeSlider", "data-max": this.props.max,
              "data-min": this.props.min, "data-column": this.props.name,
              "data-type": this.props.type})
          )
        );
      } else {
        return (
          React.createElement("div", {className: "EFDT-header-filters"},
            React.createElement("input", {className: "form-control",
              placeholder: this.props.hasOwnProperty('placeholder')?this.props.placeholder:"Search...",
              "data-column": this.props.name,
              value: this.state.key,
              onChange: this.handleChange})
          )
        );
      }
    }
  });

// Table prefix component
// Contains components above the main part of table
  var TablePrefix = React.createClass({displayName: "TablePrefix",
    render: function() {
      return (
        React.createElement("div", null,
          React.createElement("div", null,

            this.props.hider ?
              React.createElement("div", {className: "EFDT-show-hide"},
                React.createElement(ColumnHider, {cols: this.props.cols,
                  filters: this.props.filters,
                  hideFilter: this.props.hideFilter,
                  updateCols: this.props.updateCols})
              ) :
              "",


            this.props.fixedChoose ?
              React.createElement("div", {className: "EFDT-fixed-choose"},
                React.createElement(PinColumns, {cols: this.props.cols,
                  filters: this.props.filters,
                  updateCols: this.props.updateCols})
              ) :
              "",

            React.createElement("div", {className: "EFDT-download"},
              React.createElement(DataGrabber, {cols: this.props.cols, rows: this.props.rows,
                downloadFileName: this.props.downloadFileName,
                getData: this.props.getData})
            ),

            this.props.resultInfo ?
              React.createElement("div", {className: "EFDT-result-info"},
                React.createElement("span", {className: "EFDT-result-info-content"},
                  "Showing ", this.props.filteredRowsSize, " cases",

                  this.props.filteredRowsSize !== this.props.rowsSize ?
                    React.createElement("span", null, ' (filtered from ' + this.props.rowsSize + ') ',
                      React.createElement("span", {className: "EFDT-header-filters-reset",
                        onClick: this.props.onResetFilters}, "Reset")
                    )
                    : ''

                )
              ) :
              ""

          ),
          React.createElement("div", null
          )
        )
      );
    }
  });

// Wrapper for the header rendering
  var HeaderWrapper = React.createClass({displayName: "HeaderWrapper",
    render: function() {
      var columnData = this.props.columnData;
      var shortLabel = this.props.shortLabel;
      return (
        React.createElement("div", {className: "EFDT-header"},
          React.createElement("span", {className: "EFDT-header-sort", href: "#",
              onClick: this.props.sortNSet.bind(null, this.props.cellDataKey)},
            React.createElement(QtipWrapper, {label: columnData.displayName,
              shortLabel: shortLabel,
              className: 'EFDT-header-sort-content'}),
            columnData.sortFlag ?
              React.createElement("div", {
                className: columnData.sortDirArrow + ' EFDT-header-sort-icon'})
              : ""
          )
        )
      );
    }
  });

  var CustomizeCell = React.createClass({displayName: "CustomizeCell",
    selectRow: function(rowIndex) {
      this.props.selectRow(rowIndex);
    },
    selectGene: function(rowIndex) {
      this.props.selectGene(rowIndex);
    },
    enterPieLabel: function(data) {
      this.props.pieLabelMouseEnterFunc(data);
    },
    leavePieLabel: function(data) {
      this.props.pieLabelMouseLeaveFunc(data);
    },
    render: function() {
      var Cell = FixedDataTable.Cell;
      var rowIndex = this.props.rowIndex, data = this.props.data, field = this.props.field, filterAll = this.props.filterAll;
      var flag = (data[rowIndex][field] && filterAll.length > 0) ?
        (data[rowIndex][field].toLowerCase().indexOf(filterAll.toLowerCase()) >= 0) : false;
      var shortLabels = this.props.shortLabels;
      var tableType = this.props.tableType;
      var confirmedRowsIndex = this.props.confirmedRowsIndex;
      return (
        React.createElement(Cell, {onFocus: this.onFocus, className: 'EFDT-cell EFDT-cell-full' +
          (this.props.selectedRowIndex.indexOf(data[rowIndex].index) != -1 ? ' row-selected' : ''),
            },
          React.createElement("span", {style: flag ? {backgroundColor:'yellow'} : {},
              onClick: field === 'gene' ? this.selectGene.bind(this, data[rowIndex].index) : '',
              onMouseEnter: (tableType === 'pieLabel' && _.isFunction(this.props.pieLabelMouseEnterFunc) && field === 'name') ? this.enterPieLabel.bind(this, data[rowIndex].row) : '',
              onMouseLeave: (tableType === 'pieLabel' && _.isFunction(this.props.pieLabelMouseLeaveFunc) && field === 'name') ? this.leavePieLabel.bind(this, data[rowIndex].row) : '',
              "data-qtip": field === 'gene' ? ('Click ' + data[rowIndex].row[field] + ' to ' + ( this.props.selectedGeneRowIndex.indexOf(data[rowIndex].index) === -1 ? 'add to ' : ' remove from ' ) + 'your query') : '',
              className: (field === 'gene' ? 'gene hasQtip' : '') +
              ((field === 'gene' && this.props.selectedGeneRowIndex.indexOf(data[rowIndex].index) != -1) ? ' gene-selected' : '')},
            React.createElement(QtipWrapper, {label: data[rowIndex].row[field],
              shortLabel: shortLabels[data[rowIndex].index][field],
              field: field,
              tableType: tableType,
              color: data[rowIndex].row.color})
          ),

          field === 'gene' && data[rowIndex].row.qval ?
            (tableType === 'mutatedGene' ?
              React.createElement("img", {src: "images/mutsig.png", className: "hasQtip qval-icon",
                "data-qtip": '<b>MutSig</b><br/><i>Q-value</i>: ' + data[rowIndex].row.qval}) :
              React.createElement("img", {src: "images/gistic.png", className: "hasQtip qval-icon",
                "data-qtip": '<b>Gistic</b><br/><i>Q-value</i>: ' + data[rowIndex].row.qval})) : '',


          field === 'cases' ?
            React.createElement("input", {type: "checkbox", style: {float: 'right'},
              title: 'Select ' + data[rowIndex].row[field]
              + ' sample' + (Number(data[rowIndex].row[field]) > 1 ? 's':'')
              + (tableType === 'mutatedGene' ? (' with ' + data[rowIndex].row.gene + ' mutation') :
                (tableType === 'cna' ? (' with ' + data[rowIndex].row.gene + ' ' + data[rowIndex].row.altType) :
                  (tableType === 'pieLabel' ? (' in ' + data[rowIndex].row.name)  : ''))),
              checked: this.props.selectedRowIndex.indexOf(data[rowIndex].index) != -1,
              disabled: this.props.confirmedRowsIndex.indexOf(data[rowIndex].index) !== -1,
              onChange: this.selectRow.bind(this, data[rowIndex].index)}) : ''

        )
      );
    }
  });

// Main part table component
// Uses FixedDataTable library
  var TableMainPart = React.createClass({displayName: "TableMainPart",
    // Creates Qtip
    createQtip: function() {
      $('.EFDT-table .hasQtip').one('mouseenter', function() {
        $(this).qtip({
          content: {text: $(this).attr('data-qtip')},
          hide: {fixed: true, delay: 100},
          show: {ready: true},
          style: {classes: 'qtip-light qtip-rounded qtip-shadow', tip: true},
          position: {my: 'center left', at: 'center right', viewport: $(window)}
        });
      });
    },

    // Creates Qtip after first rendering
    componentDidMount: function() {
      this.createQtip();
    },

    // Creates Qtip after update rendering
    componentDidUpdate: function() {
      this.createQtip();
    },

    // Creates Qtip after page scrolling
    onScrollEnd: function() {
      $(".qtip").remove();
      this.createQtip();
    },

    // Destroys Qtip before update rendering
    componentWillUpdate: function() {
      //console.log('number of elments which has "hasQtip" as class name: ', $('.hasQtip').size());
      //console.log('number of elments which has "hasQtip" as class name under class EFDT: ', $('.EFDT-table .hasQtip').size());

      $('.EFDT-table .hasQtip')
        .each(function() {
          $(this).qtip('destroy', true);
        });
    },

    getDefaultProps: function() {
      return {
        selectedRowIndex: []
      };
    },

    getInitialState: function() {
      return {
        selectedRowIndex: this.props.selectedRowIndex,
        selectedGeneRowIndex: this.props.selectedGeneRowIndex
      }
    },

    selectRow: function(rowIndex) {
      var selectedRowIndex = this.state.selectedRowIndex;
      var selected = false;
      var rows = this.props.rows;
      if ((_.intersection(selectedRowIndex, [rowIndex])).length > 0) {
        selectedRowIndex = _.without(selectedRowIndex, rowIndex);
      } else {
        selectedRowIndex.push(rowIndex);
        selected = true;
      }

      if (_.isFunction(this.props.rowClickFunc)) {
        var selectedData = selectedRowIndex.map(function(item) {
          return rows[item];
        });
        this.props.rowClickFunc(rows[rowIndex], selected, selectedData);
      }

      this.setState({
        selectedRowIndex: selectedRowIndex
      });
    },

    selectGene: function(rowIndex) {
      var selectedGeneRowIndex = this.state.selectedGeneRowIndex;
      var selected = false;
      var rows = this.props.rows;
      if ((_.intersection(selectedGeneRowIndex, [rowIndex])).length > 0) {
        selectedGeneRowIndex = _.without(selectedGeneRowIndex, rowIndex);
      } else {
        selectedGeneRowIndex.push(rowIndex);
        selected = true;
      }

      if (_.isFunction(this.props.geneClickFunc)) {
        this.props.geneClickFunc(rows[rowIndex], selected);
      }

      this.setState({
        selectedGeneRowIndex: selectedGeneRowIndex
      });
    },

    // If properties changed
    componentWillReceiveProps: function(newProps) {
      if (newProps.selectedRowIndex !== this.state.selectedRowIndex) {
        this.setState({
          selectedRowIndex: newProps.selectedRowIndex
        });
      }
      if (newProps.selectedGeneRowIndex !== this.state.selectedGeneRowIndex) {
        this.setState({
          selectedGeneRowIndex: newProps.selectedGeneRowIndex
        });
      }
    },

    // FixedDataTable render function
    render: function() {
      var Table = FixedDataTable.Table, Column = FixedDataTable.Column,
        ColumnGroup = FixedDataTable.ColumnGroup, props = this.props,
        rows = this.props.filteredRows, columnWidths = this.props.columnWidths,
        cellShortLabels = this.props.shortLabels.cell,
        headerShortLabels = this.props.shortLabels.header,
        confirmedRowsIndex=this.props.confirmedRowsIndex,
        selectedRowIndex = this.state.selectedRowIndex,
        selectedGeneRowIndex = this.state.selectedGeneRowIndex,
        self = this;

      return (
        React.createElement("div", null,
          React.createElement(Table, {
              rowHeight: props.rowHeight?props.rowHeight:30,
              rowGetter: this.rowGetter,
              onScrollEnd: this.onScrollEnd,
              rowsCount: props.filteredRows.length,
              width: props.tableWidth?props.tableWidth:1230,
              maxHeight: props.maxHeight?props.maxHeight:500,
              headerHeight: props.headerHeight?props.headerHeight:30,
              groupHeaderHeight: props.groupHeaderHeight?props.groupHeaderHeight:50,
              scrollToColumn: props.goToColumn,
              isColumnResizing: false,
              onColumnResizeEndCallback: props.onColumnResizeEndCallback
            },

            props.cols.map(function(col, index) {
              var column;
              var width = col.show ? (col.width ? col.width :
                (columnWidths[col.name] ? columnWidths[col.name] : 200)) : 0;

              if (props.groupHeader) {
                column = React.createElement(ColumnGroup, {
                    header:
                      React.createElement(Filter, {type: props.filters[col.name].type, name: col.name,
                        max: col.max, min: col.min, filter: props.filters[col.name],
                        placeholder: "Filter column",
                        onFilterKeywordChange: props.onFilterKeywordChange}
                      ),

                    key: col.name,
                    fixed: col.fixed,
                    align: "center"
                  },
                  React.createElement(Column, {
                    header:
                      React.createElement(HeaderWrapper, {cellDataKey: col.name, columnData: {displayName:col.displayName,sortFlag:props.sortBy === col.name,
                        sortDirArrow:props.sortDirArrow,filterAll:props.filterAll,type:props.filters[col.name].type},
                        sortNSet: props.sortNSet, filter: props.filters[col.name],
                        shortLabel: headerShortLabels[col.name]}
                      ),

                    cell: React.createElement(CustomizeCell, {data: rows, field: col.name,
                      filterAll: props.filterAll, shortLabels: cellShortLabels,
                      tableType: props.tableType,
                      selectRow: self.selectRow,
                      selectGene: self.selectGene,
                      selectedRowIndex: selectedRowIndex,
                      selectedGeneRowIndex: selectedGeneRowIndex,
                      pieLabelMouseEnterFunc: props.pieLabelMouseEnterFunc,
                      pieLabelMouseLeaveFunc: props.pieLabelMouseLeaveFunc}
                    ),
                    width: width,
                    fixed: col.fixed,
                    allowCellsRecycling: true,
                    isResizable: props.isResizable,
                    columnKey: col.name,
                    key: col.name}
                  )
                )
              } else {
                column = React.createElement(Column, {
                  header:
                    React.createElement(HeaderWrapper, {cellDataKey: col.name, columnData: {displayName:col.displayName,sortFlag:props.sortBy === col.name,
                      sortDirArrow:props.sortDirArrow,filterAll:props.filterAll,type:props.filters[col.name].type},
                      sortNSet: props.sortNSet, filter: props.filters[col.name],
                      shortLabel: headerShortLabels[col.name]}
                    ),

                  cell: React.createElement(CustomizeCell, {data: rows, field: col.name,
                    filterAll: props.filterAll,
                    shortLabels: cellShortLabels,
                    tableType: props.tableType,
                    selectRow: self.selectRow,
                    selectGene: self.selectGene,
                    selectedRowIndex: selectedRowIndex,
                    selectedGeneRowIndex: selectedGeneRowIndex,
                    confirmedRowsIndex: confirmedRowsIndex,
                    pieLabelMouseEnterFunc: props.pieLabelMouseEnterFunc,
                    pieLabelMouseLeaveFunc: props.pieLabelMouseLeaveFunc}
                  ),
                  width: width,
                  fixed: col.fixed,
                  allowCellsRecycling: true,
                  columnKey: col.name,
                  key: col.name,
                  isResizable: props.isResizable}
                )
              }
              return (
                column
              );
            })

          )
        )
      );
    }
  });

// Root component
  var Main = React.createClass({displayName: "Main",
    SortTypes: {
      ASC: 'ASC',
      DESC: 'DESC'
    },

    rows: null,

    getColumnWidth: function(cols, rows, measureMethod, columnMinWidth) {
      var columnWidth = {};
      var self = this;
      if (self.props.autoColumnWidth) {
        var rulerWidth = 0;
        _.each(rows, function(row) {
          _.each(row, function(data, attr) {
            if (data) {
              data = data.toString();
              if (!columnWidth.hasOwnProperty(attr)) {
                columnWidth[attr] = 0;
              }
              switch (measureMethod) {
                case 'jquery':
                  var ruler = $("#ruler");
                  ruler.css('font-size', '14px');
                  ruler.text(data);
                  rulerWidth = ruler.outerWidth();
                  break;
                default:
                  var upperCaseLength = data.replace(/[^A-Z]/g, "").length;
                  var dataLength = data.length;
                  rulerWidth = upperCaseLength * 10 + (dataLength - upperCaseLength) * 8 + 15;
                  break;
              }

              columnWidth[attr] = columnWidth[attr] < rulerWidth ? rulerWidth : columnWidth[attr];
            }
          });
        });

        //20px is the padding.
        columnWidth = _.object(_.map(columnWidth, function(length, attr) {
          return [attr, length > self.props.columnMaxWidth ?
            self.props.columnMaxWidth :
            ( (length + 20) < columnMinWidth ?
              columnMinWidth : (length + 20))];
        }));
      } else {
        _.each(cols, function(col, attr) {
          columnWidth[col.name] = col.width ? col.width : 200;
        });
      }
      return columnWidth;
    },

    getShortLabels: function(rows, cols, columnWidth, measureMethod) {
      var cellShortLabels = [];
      var headerShortLabels = {};

      _.each(rows, function(row) {
        var rowWidthObj = {};
        _.each(row, function(content, attr) {
          var _label = content;
          var _labelShort = _label;
          var _labelWidth;
          if (_label) {
            _label = _label.toString();
            switch (measureMethod) {
              case 'jquery':
                var ruler = $('#ruler');
                ruler.text(_label);
                ruler.css('font-size', '14px');
                _labelWidth = ruler.outerWidth();
                break;
              default:
                var upperCaseLength = _label.replace(/[^A-Z]/g, "").length;
                var dataLength = _label.length;
                _labelWidth = upperCaseLength * 10 + (dataLength - upperCaseLength) * 8 + 15;
                break;
            }
            if (_labelWidth > columnWidth[attr]) {
              var end = Math.floor(_label.length * columnWidth[attr] / _labelWidth) - 3;
              _labelShort = _label.substring(0, end) + '...';
            } else {
              _labelShort = _label;
            }
          }
          rowWidthObj[attr] = _labelShort;
        });
        cellShortLabels.push(rowWidthObj);
      });

      _.each(cols, function(col) {
        if (!col.hasOwnProperty('show') || col.show) {
          var _label = col.displayName;
          var _shortLabel = '';
          var _labelWidth = _label.toString().length * 8 + 20;

          if (_label) {
            _label = _label.toString();
            switch (measureMethod) {
              case 'jquery':
                var ruler = $('#ruler');
                ruler.text(_label);
                ruler.css('font-size', '14px');
                ruler.css('font-weight', 'bold');
                _labelWidth = ruler.outerWidth() + 20;
                break;
              default:
                var upperCaseLength = _label.replace(/[^A-Z]/g, "").length;
                var dataLength = _label.length;
                _labelWidth = upperCaseLength * 10 + (dataLength - upperCaseLength) * 8 + 20;
                break;
            }
            if (_labelWidth > columnWidth[col.name]) {
              var end = Math.floor(_label.length * columnWidth[col.name] / _labelWidth) - 3;
              _shortLabel = _label.substring(0, end) + '...';
            } else {
              _shortLabel = _label;
            }
          }
          headerShortLabels[col.name] = _shortLabel;
        }
      });

      return {
        cell: cellShortLabels,
        header: headerShortLabels
      };
    },
    // Filters rows by selected column
    filterRowsBy: function(filterAll, filters) {
      var rows = this.rows.slice();
      var hasGroupHeader = this.props.groupHeader;
      var filterRowsStartIndex = [];
      var filteredRows = _.filter(rows, function(row, index) {
        var allFlag = false; // Current row contains the global keyword
        for (var col in filters) {
          if (!filters[col].hide) {
            if (filters[col].type == "STRING") {
              if (!row[col] && hasGroupHeader) {
                if (filters[col].key.length > 0) {
                  return false;
                }
              } else {
                if (hasGroupHeader && row[col].toLowerCase().indexOf(filters[col].key.toLowerCase()) < 0) {
                  return false;
                }
                if (row[col] && row[col].toLowerCase().indexOf(filterAll.toLowerCase()) >= 0) {
                  allFlag = true;
                }
              }
            } else if (filters[col].type === "NUMBER" || filters[col].type == 'PERCENTAGE') {
              var cell = _.isUndefined(row[col]) ? row[col] : Number(row[col].toString().replace('%', ''));
              if (!isNaN(cell)) {
                if (hasGroupHeader) {
                  if (filters[col].min !== filters[col]._min && Number(cell) < filters[col].min) {
                    return false;
                  }
                  if (filters[col].max !== filters[col]._max && Number(cell) > filters[col].max) {
                    return false;
                  }
                }
                if (row[col] && row[col].toString().toLowerCase().indexOf(filterAll.toLowerCase()) >= 0) {
                  allFlag = true;
                }
              }
            }
          }
        }
        if (allFlag) {
          filterRowsStartIndex.push(index);
        }
        return allFlag;
      });

      filteredRows = filteredRows.map(function(item, index) {
        return {
          row: item,
          index: filterRowsStartIndex[index]
        }
      });
      return filteredRows;
    },

    // Sorts rows by selected column
    sortRowsBy: function(filters, filteredRows, sortBy, switchDir) {
      var type = filters[sortBy].type, sortDir = this.state.sortDir,
        SortTypes = this.SortTypes, confirmedRowsIndex = this.getSelectedRowIndex(this.state.confirmedRows);
      if (switchDir) {
        if (sortBy === this.state.sortBy) {
          sortDir = this.state.sortDir === SortTypes.ASC ? SortTypes.DESC : SortTypes.ASC;
        } else {
          sortDir = SortTypes.DESC;
        }
      }

      filteredRows.sort(function(a, b) {
        var sortVal = 0, aVal = a.row[sortBy], bVal = b.row[sortBy];

        if(confirmedRowsIndex.indexOf(a.index) !== -1 && confirmedRowsIndex.indexOf(b.index) === -1) {
          return -1;
        }

        if(confirmedRowsIndex.indexOf(a.index) === -1 && confirmedRowsIndex.indexOf(b.index) !== -1) {
          return 1;
        }

        if (sortBy === 'cytoband' && window.hasOwnProperty('StudyViewUtil')) {
          var _sortResult = window.StudyViewUtil.cytobanBaseSort(aVal, bVal);
          sortVal = sortDir === SortTypes.ASC ? -_sortResult : _sortResult;
        } else {

          if (type == "NUMBER") {
            aVal = (aVal && !isNaN(aVal)) ? Number(aVal) : aVal;
            bVal = (bVal && !isNaN(bVal)) ? Number(bVal) : bVal;
          }
          if (type == 'PERCENTAGE') {
            aVal = aVal ? Number(aVal.replace('%', '')) : aVal;
            bVal = bVal ? Number(bVal.replace('%', '')) : bVal;
          }
          if (typeof aVal != "undefined" && !isNaN(aVal) && typeof bVal != "undefined" && !isNaN(bVal)) {
            if (aVal > bVal) {
              sortVal = 1;
            }
            if (aVal < bVal) {
              sortVal = -1;
            }

            if (sortDir === SortTypes.ASC) {
              sortVal = sortVal * -1;
            }
          } else if (typeof aVal != "undefined" && typeof bVal != "undefined") {
            if (!isNaN(aVal)) {
              sortVal = -1;
            } else if (!isNaN(bVal)) {
              sortVal = 1;
            }
            else {
              if (aVal > bVal) {
                sortVal = 1;
              }
              if (aVal < bVal) {
                sortVal = -1;
              }

              if (sortDir === SortTypes.ASC) {
                sortVal = sortVal * -1;
              }
            }
          } else if (aVal) {
            sortVal = -1;
          }
          else {
            sortVal = 1;
          }
        }
        return -sortVal;
      });

      return {filteredRows: filteredRows, sortDir: sortDir};
    },

    // Sorts and sets state
    sortNSet: function(sortBy) {
      var result = this.sortRowsBy(this.state.filters, this.state.filteredRows, sortBy, true);
      this.setState({
        filteredRows: result.filteredRows,
        sortBy: sortBy,
        sortDir: result.sortDir
      });
    },

    // Filters, sorts and sets state
    filterSortNSet: function(filterAll, filters, sortBy) {
      var filteredRows = this.filterRowsBy(filterAll, filters);
      var result = this.sortRowsBy(filters, filteredRows, sortBy, false);
      this.setState({
        filteredRows: result.filteredRows,
        sortBy: sortBy,
        sortDir: result.sortDir,
        filterAll: filterAll,
        filters: filters
      });
    },

    // Operations when filter keyword changes
    onFilterKeywordChange: function(e) {
      ++this.state.filterTimer;

      //Disable event pooling in react, see https://goo.gl/1mq6qI
      e.persist();

      var self = this;
      var id = setTimeout(function() {
        var filterAll = self.state.filterAll, filters = self.state.filters;
        if (e.target.getAttribute("data-column") == "all") {
          filterAll = e.target.value;
        } else {
          filters[e.target.getAttribute("data-column")].key = e.target.value;
        }
        self.filterSortNSet(filterAll, filters, self.state.sortBy);
        --self.state.filterTimer;
      }, 500);

      if (this.state.filterTimer > 1) {
        clearTimeout(id);
        --self.state.filterTimer;
      }
    },

    // Operations when filter range changes
    onFilterRangeChange: function(column, min, max) {
      ++this.state.filterTimer;

      var self = this;
      var id = setTimeout(function() {
        var filters = self.state.filters;
        filters[column].min = min;
        filters[column].max = max;
        self.filterSortNSet(self.state.filterAll, filters, self.state.sortBy);
        --self.state.filterTimer;
      }, 500);

      if (this.state.filterTimer > 1) {
        clearTimeout(id);
        --self.state.filterTimer;
      }
    },

    // Operations when reset all filters
    onResetFilters: function() {
      var filters = this.state.filters;
      _.each(filters, function(filter) {
        if (!_.isUndefined(filter._key)) {
          filter.key = filter._key;
        }
        if (!_.isUndefined(filter._min)) {
          filter.min = filter._min;
        }
        if (!_.isUndefined(filter._max)) {
          filter.max = filter._max;
        }
        filter.reset = true;
      });
      if (this.props.groupHeader) {
        this.registerSliders();
      }
      this.filterSortNSet('', filters, this.state.sortBy);
    },

    updateCols: function(cols, filters) {
      var filteredRows = this.filterRowsBy(this.state.filterAll, filters);
      var result = this.sortRowsBy(filters, filteredRows, this.state.sortBy, false);
      this.setState({
        cols: cols,
        filteredRows: result.filteredRows,
        filters: filters
      });
      if (this.props.groupHeader) {
        this.registerSliders();
      }
    },

    updateGoToColumn: function(val) {
      this.setState({
        goToColumn: val
      });
    },

    registerSliders: function() {
      var onFilterRangeChange = this.onFilterRangeChange;
      $('.rangeSlider')
        .each(function() {
          var min = Math.floor(Number($(this).attr('data-min')) * 100) / 100, max = (Math.ceil(Number($(this).attr('data-max')) * 100)) / 100,
            column = $(this).attr('data-column'), diff = max - min, step = 1;
          var type = $(this).attr('data-type');

          if (diff < 0.01) {
            step = 0.001;
          } else if (diff < 0.1) {
            step = 0.01;
          } else if (diff < 2) {
            step = 0.1;
          }

          $(this).slider({
            range: true,
            min: min,
            max: max,
            step: step,
            values: [min, max],
            change: function(event, ui) {
              $("#range-" + column).text(ui.values[0] + " to " + ui.values[1]);
              onFilterRangeChange(column, ui.values[0], ui.values[1]);
            }
          });
          if (type === 'PERCENTAGE') {
            $("#range-" + column).text(min + "% to " + max + '%');
          } else {
            $("#range-" + column).text(min + " to " + max);
          }
        });
    },
    // Processes input data, and initializes table states
    getInitialState: function() {
      var state = this.parseInputData(this.props.input, this.props.uniqueId,
        this.props.selectedRows, this.props.groupHeader, this.props.columnSorting);

      state.confirmedRows = ['mutatedGene', 'cna'].indexOf(this.props.tableType) !== -1 ? state.selectedRows : [];
      state.filteredRows = null;
      state.filterAll = "";
      state.sortBy = this.props.sortBy || 'cases';
      state.goToColumn = null;
      state.filterTimer = 0;
      state.sortDir = this.props.sortDir || this.SortTypes.DESC;
      state.rowClickFunc = this.rowClickCallback;
      state.selectButtonClickCallback = this.selectButtonClickCallback;
      return state;
    },

    rowClickCallback: function(selectedRows, isSelected, allSelectedRows) {
      var uniqueId = this.props.uniqueId;
      this.setState({
        selectedRows: allSelectedRows.map(function(item) {
          return item[uniqueId];
        })
      });
      if(_.isFunction(this.props.rowClickFunc)) {
        this.props.rowClickFunc(selectedRows, isSelected, allSelectedRows);
      }
    },

    selectButtonClickCallback: function() {
      var selectedRows = this.state.selectedRows;
      this.setState({
        confirmedRows: selectedRows
      });
      if(_.isFunction(this.props.selectButtonClickCallback)) {
        this.props.selectButtonClickCallback();
      }
    },

    getSelectedRowIndex: function(selectedRows) {
      var selectedRowIndex = [];
      var uniqueId = this.props.uniqueId;
      _.each(this.rows, function(row, index) {
        if (selectedRows.indexOf(row[uniqueId]) !== -1) {
          selectedRowIndex.push(index);
        }
      })
      return selectedRowIndex;
    },

    getSelectedGeneRowIndex: function(selectedGene) {
      var selectedGeneRowIndex = [];
      _.each(this.rows, function(row, index) {
        if (selectedGene.indexOf(row.gene) !== -1) {
          selectedGeneRowIndex.push(index);
        }
      })
      return selectedGeneRowIndex;
    },

    // Initializes filteredRows before first rendering
    componentWillMount: function() {
      this.filterSortNSet(this.state.filterAll, this.state.filters, this.state.sortBy);
    },

    parseInputData: function(input, uniqueId, selectedRows, groupHeader, columnSorting) {
      var cols = [], rows = [], rowsDict = {}, attributes = input.attributes,
        data = input.data, dataLength = data.length, col, cell, i, filters = {},
        uniqueId = uniqueId || 'id', newCol,
        selectedRows = selectedRows || [],
        measureMethod = (dataLength > 100000 || !this.props.autoColumnWidth) ? 'charNum' : 'jquery',
        columnMinWidth = groupHeader ? 130 : 50; //The minimum width to at least fit in number slider.
      var selectedRowIndex = [];

      // Gets column info from input
      var colsDict = {};
      for (i = 0; i < attributes.length; i++) {
        col = attributes[i];
        newCol = {
          displayName: col.display_name,
          name: col.attr_id,
          type: col.datatype,
          fixed: false,
          show: true
        };

        if (col.hasOwnProperty('column_width')) {
          newCol.width = col.column_width;
        }

        if (_.isBoolean(col.show)) {
          newCol.show = col.show;
        }

        if (_.isBoolean(col.fixed)) {
          newCol.fixed = col.fixed;
        }

        cols.push(newCol);
        colsDict[col.attr_id] = i;
      }

      // Gets data rows from input
      for (i = 0; i < dataLength; i++) {
        cell = data[i];
        if (!rowsDict[cell[uniqueId]]) {
          rowsDict[cell[uniqueId]] = {};
        }
        rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val;
      }

      var index = 0;
      _.each(rowsDict, function(item, i) {
        rowsDict[i][uniqueId] = i;
        rows.push(rowsDict[i]);
        if (selectedRows.indexOf(i) !== -1) {
          selectedRowIndex.push(index);
        }
        ++index;
      });

      // Gets the range of number type features
      for (i = 0; i < cols.length; i++) {
        col = cols[i];
        var _filter = {
          type: col.type,
          hide: !col.show
        };

        if (col.type == "NUMBER" || col.type == "PERCENTAGE") {
          var min = Number.MAX_VALUE, max = -Number.MAX_VALUE;
          for (var j = 0; j < rows.length; j++) {
            cell = _.isUndefined(rows[j][col.name]) ? rows[j][col.name] : rows[j][col.name].toString().replace('%');
            if (typeof cell != "undefined" && !isNaN(cell)) {
              cell = Number(cell);
              max = cell > max ? cell : max;
              min = cell < min ? cell : min;
            }
          }
          if (max === -Number.MAX_VALUE || min === Number.MIN_VALUE) {
            _filter.key = '';
            _filter._key = '';
          } else {
            col.max = max;
            col.min = min;
            _filter.min = min;
            _filter.max = max;
            _filter._min = min;
            _filter._max = max;
          }
        } else {
          _filter.key = '';
          _filter._key = '';
        }
        filters[col.name] = _filter;
      }

      if (columnSorting) {
        cols = _.sortBy(cols, function(obj) {
          if (!_.isUndefined(obj.displayName)) {
            return obj.displayName;
          } else {
            return obj.name;
          }
        });
      }
      this.rows = rows;

      var columnWidths = this.getColumnWidth(cols, rows, measureMethod, columnMinWidth);
      var shortLabels = this.getShortLabels(rows, cols, columnWidths, measureMethod);

      return {
        cols: cols,
        rowsSize: rows.length,
        filters: filters,
        shortLabels: shortLabels,
        columnWidths: columnWidths,
        columnMinWidth: columnMinWidth,
        selectedRowIndex: selectedRowIndex,
        selectedRows: selectedRows,
        dataSize: dataLength,
        measureMethod: measureMethod
      };
    },
    // If properties changed
    componentWillReceiveProps: function(newProps) {
      var state = this.parseInputData(newProps.input, newProps.uniqueId,
        newProps.selectedRows, newProps.groupHeader, newProps.columnSorting);
      state.confirmedRows = ['mutatedGene', 'cna'].indexOf(newProps.tableType) !== -1 ? state.selectedRows : [];
      state.filteredRows = null;
      state.filterAll = this.state.filterAll || '';
      state.sortBy = this.props.sortBy || 'cases';
      state.sortDir = this.props.sortDir || this.SortTypes.DESC;
      state.goToColumn = null;
      state.filterTimer = 0;

      var filteredRows = this.filterRowsBy(state.filterAll, state.filters);
      var result = this.sortRowsBy(state.filters, filteredRows, state.sortBy, false);
      state.filteredRows = result.filteredRows;

      this.setState(state);
    },

    //Will be triggered if the column width has been changed
    onColumnResizeEndCallback: function(width, key) {
      var foundMatch = false;
      var cols = this.state.cols;

      _.each(cols, function(col, attr) {
        if (col.name === key) {
          col.width = width;
          foundMatch = true;
        }
      });
      if (foundMatch) {
        var columnWidths = this.state.columnWidths;
        columnWidths[key] = width;
        var shortLabels = this.getShortLabels(this.rows, cols, columnWidths, this.state.measureMethod);
        this.setState({
          columnWidths: columnWidths,
          shortLabels: shortLabels,
          cols: cols
        });
      }
    },

    // Activates range sliders after first rendering
    componentDidMount: function() {
      if (this.props.groupHeader) {
        this.registerSliders();
      }
    },

    // Sets default properties
    getDefaultProps: function() {
      return {
        filter: "NONE",
        download: "NONE",
        showHide: false,
        hideFilter: true,
        scroller: false,
        resultInfo: true,
        groupHeader: true,
        downloadFileName: 'data.txt',
        autoColumnWidth: true,
        columnMaxWidth: 300,
        columnSorting: true,
        tableType: 'mutatedGene',
        selectedRows: [],
        selectedGene: [],
        sortBy: 'cases',
        sortDir: 'DESC',
        isResizable: false
      };
    },

    render: function() {
      var sortDirArrow = this.state.sortDir === this.SortTypes.DESC ? 'fa fa-sort-desc' : 'fa fa-sort-asc';
      var selectedGeneRowIndex = this.getSelectedGeneRowIndex(this.props.selectedGene);
      var selectedRowIndex = this.getSelectedRowIndex(this.state.selectedRows);
      var confirmedRowsIndex = this.getSelectedRowIndex(this.state.confirmedRows);
      return (
        React.createElement("div", {className: "EFDT-table"},
          React.createElement("div", {className: "EFDT-table-prefix"},
            React.createElement(TablePrefix, {cols: this.state.cols, rows: this.rows,
              onFilterKeywordChange: this.onFilterKeywordChange,
              onResetFilters: this.onResetFilters,
              filters: this.state.filters,
              updateCols: this.updateCols,
              updateGoToColumn: this.updateGoToColumn,
              scroller: this.props.scroller,
              filter: this.props.filter,
              hideFilter: this.props.hideFilter,
              getData: this.props.download,
              downloadFileName: this.props.downloadFileName,
              hider: this.props.showHide,
              fixedChoose: this.props.fixedChoose,
              resultInfo: this.props.resultInfo,
              rowsSize: this.state.rowsSize,
              filteredRowsSize: this.state.filteredRows.length}
            )
          ),
          React.createElement("div", {className: "EFDT-tableMain"},
            React.createElement(TableMainPart, {cols: this.state.cols,
              rows: this.rows,
              filteredRows: this.state.filteredRows,
              filters: this.state.filters,
              sortNSet: this.sortNSet,
              onFilterKeywordChange: this.onFilterKeywordChange,
              goToColumn: this.state.goToColumn,
              sortBy: this.state.sortBy,
              sortDirArrow: sortDirArrow,
              filterAll: this.state.filterAll,
              filter: this.props.filter,
              rowHeight: this.props.rowHeight,
              tableWidth: this.props.tableWidth,
              maxHeight: this.props.maxHeight,
              headerHeight: this.props.headerHeight,
              groupHeaderHeight: this.props.groupHeaderHeight,
              groupHeader: this.props.groupHeader,
              tableType: this.props.tableType,
              confirmedRowsIndex: confirmedRowsIndex,
              shortLabels: this.state.shortLabels,
              columnWidths: this.state.columnWidths,
              rowClickFunc: this.state.rowClickFunc,
              geneClickFunc: this.props.geneClickFunc,
              selectButtonClickCallback: this.state.selectButtonClickCallback,
              pieLabelMouseEnterFunc: this.props.pieLabelMouseEnterFunc,
              pieLabelMouseLeaveFunc: this.props.pieLabelMouseLeaveFunc,
              selectedRowIndex: selectedRowIndex,
              selectedGeneRowIndex: selectedGeneRowIndex,
              isResizable: this.props.isResizable,
              onColumnResizeEndCallback: this.onColumnResizeEndCallback}
            )
          ),
          React.createElement("div", {className: "EFDT-filter"},

            (this.props.filter === "ALL" || this.props.filter === "GLOBAL") ?
              React.createElement(Filter, {type: "STRING", name: "all",
                onFilterKeywordChange: this.onFilterKeywordChange}) :
              React.createElement("div", null)

          ),
          React.createElement("div", {className: "EFDT-finish-selection-button"},

            (['mutatedGene', 'cna'].indexOf(this.props.tableType) !== -1 && this.state.selectedRows.length > 0 && this.state.confirmedRows.length !== this.state.selectedRows.length ) ?
              React.createElement("button", {className: "btn btn-default btn-xs", onClick: this.state.selectButtonClickCallback}, "Select Samples") : ''

          )
        )
      );
    }
  });

  return Main;
})();
