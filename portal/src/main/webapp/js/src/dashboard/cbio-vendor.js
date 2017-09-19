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
      }, function() {
        def.reject();
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
    return "case.do#/patient?studyId=" + cancerStudyId + "&caseId=" + patientId;
  }

  function getLinkToSampleView(cancerStudyId, sampleId) {
    return "case.do#/patient?studyId=" + cancerStudyId + "&sampleId=" + sampleId;
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
    var q3 = values[(Math.floor(values.length * (3 / 4)))];
    var iqr = q3 - q1;

    if (values[Math.ceil((values.length * (1 / 2)))] < 0.001) {
      smallDataFlag = true;
    }
    // Then find min and max values
    var maxValue, minValue;
    if (0.001 <= q3 && q3 < 1) {
      maxValue = Number((q3 + iqr * 1.5).toFixed(3));
      minValue = Number((q1 - iqr * 1.5).toFixed(3));
    } else if(q3 < 0.001){// get IQR for very small number(<0.001)
      maxValue = Number((q3 + iqr * 1.5));
      minValue = Number((q1 - iqr * 1.5));
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

    return [minValue, maxValue, smallDataFlag, values, iqr];
  }

  function getDatahubStudiesList() {
    var DATAHUB_GIT_URL =
      'https://api.github.com/repos/cBioPortal/datahub/contents/public';
    var def = new $.Deferred();

    $.getJSON(DATAHUB_GIT_URL, function(data) {
      var studies = {};
      if (_.isArray(data)) {
        _.each(data, function(fileInfo) {
          if (_.isObject(fileInfo) &&
            fileInfo.type === 'file' &&
            _.isString(fileInfo.name)) {
            var fileName = fileInfo.name.split('.tar.gz');
            if (fileName.length > 0) {
              studies[fileName[0]] = {
                name: fileName[0],
                htmlURL: fileInfo.html_url
              };
            }
          }
        })
      }
      def.resolve(studies);
    }).fail(function(error) {
      def.reject(error);
    });
    return def.promise();
  }


  function getDecimalExponents(data){
    //Copy the values, rather than operating on references to existing values
    if (!_.isArray(data) || data.length < 1) {//if data is not an array or is empty, return data
      return data;
    }

    var values = [];
    var minZeros = 0, maxZeros = 0;
    var head, tail;
    var expoents = [];

    _.each(data, function(item) {
      if (!isNaN(item)) {
        values.push(Number(item));
      }
    });

    // Then sort
    values.sort(function(a, b) {
      return a - b;
    });

    //make sure that min and max values are greater than 0.
    for (head = 0; head < values.length; head++){
      if (values[head] > 0) {
        while (values[head] < 1) {
          values[head] *= 10;
          minZeros++;
        }
        break;
      }
    }

    for (tail = values.length - 1; tail >= 0; tail--) {
      if (values[tail] > 0) {
        while (values[tail] < 1) {
          values[tail] *= 10;
          maxZeros++;
        }
        break;
      }
    }

    if(head <= tail){
      for(var i = maxZeros;i <= minZeros; i++){
        expoents.push(-i);
      }
    }
    
    return expoents;
    
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
    makeCachedPromiseFunction: makeCachedPromiseFunction,
    getDatahubStudiesList: getDatahubStudiesList,
    getDecimalExponents: getDecimalExponents
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

'use strict';

window.EnhancedFixedDataTableSpecial = (function() {
// Data button component
  var FileGrabber = React.createClass({displayName: "FileGrabber",
    // Saves table content to a text file
    saveFile: function() {
      var formatData = this.props.content();

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

    render: function() {
      return (
        React.createElement("button", {className: "btn btn-default", onClick: this.saveFile},
          "DATA")
      );
    }
  });

// Copy button component
  var ClipboardGrabber = React.createClass({displayName: "ClipboardGrabber",
    notify: function(opts) {
      // Default settings for Copied.
      var _message = 'Copied.';
      var _type = 'success';

      if (_.isObject(opts)) {
        if (!_.isUndefined(opts.message)) {
          _message = opts.message;
        }
        if (opts.type) {
          _type = opts.type;
        }
      }
      $.notify({
        message: _message
      }, {
        type: _type,
        animate: {
          enter: 'animated fadeInDown',
          exit: 'animated fadeOutUp'
        },
        delay: 1000
      });
    },

    componentDidMount: function() {
      var client = new ZeroClipboard($("#copy-button"));
      var self = this;
      client.on("ready", function(readyEvent) {
        client.on("copy", function(event) {
          event.clipboardData.setData('text/plain', self.props.content());
        });
        client.on("aftercopy", function(event) {
          self.notify();
        });
        client.on("error", function(event) {
          // Error happened, disable Copy button notify the user.
          ZeroClipboard.destroy();
          self.notify({
            message: 'Copy button is not availble at this moment.',
            type: 'danger'
          });
          self.setState({show: false});
        });
      });
    },

    getInitialState: function() {
      var _show = true;
      var _content = this.props.content();

      // The current not official limitation is 1,000,000
      // https://github.com/zeroclipboard/zeroclipboard/issues/529
      if (!_.isString(_content) || _content.length > 1000000) {
        _show = false;
      }

      return {
        show: _show,
        formatData: ''
      };
    },

    render: function() {
      return (
        React.createElement("div", null,
          this.state.show ?
            React.createElement("button", {className: "btn btn-default", id: "copy-button"},
              "COPY") : ''
        )
      );
    }
  });

// Container of FileGrabber and ClipboardGrabber
  var DataGrabber = React.createClass({displayName: "DataGrabber",
    // Prepares table content data for download or copy button
    prepareContent: function() {
      var content = [], cols = $.extend(true, [], this.props.cols), rows = this.props.rows;

      // List fixed columns first
      cols = cols.sort(function(x, y) {
        return (x.fixed === y.fixed) ? 0 : x.fixed ? -1 : 1;
      });

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
        React.createElement("span", {className: className + (qtipFlag ? " hasQtip " : '') +
          ((field === 'alttype' && ['mutatedGene', 'cna'].indexOf(tableType) !== -1) ? (label === 'AMP' ? ' alt-type-red' : ' alt-type-blue') : ''),
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
      // add title attributes to unlabeled inputs generated by
      // .dropdownCheckbox() for accessibility, until
      // https://github.com/Nelrohd/bootstrap-dropdown-checkbox/issues/33 is
      // fixed upstream
      $('#hide_column_checklist input[type="checkbox"].checkbox-all')
        .attr('title', 'Select all');
      $('#hide_column_checklist input[type="text"].search')
        .attr('title', 'Search');
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
      // add title attributes to unlabeled inputs generated by
      // .dropdownCheckbox() for accessibility, until
      // https://github.com/Nelrohd/bootstrap-dropdown-checkbox/issues/33 is
      // fixed upstream
      $('#pin_column_checklist input[type="checkbox"].checkbox-all')
        .attr('title', 'Select all');
      $('#pin_column_checklist input[type="text"].search')
        .attr('title', 'Search');
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
        // explicitly set anchors without href for the handles, as
        // jQuery UI 1.10 otherwise adds href="#" which may confuse
        // assistive technologies
        return (
          React.createElement("div", {className: "EFDT-header-filters"},
            React.createElement("span", {id: "range-" + this.props.name}),

            React.createElement("div", {className: "rangeSlider", "data-max": this.props.max,
                "data-min": this.props.min, "data-column": this.props.name,
                "data-type": this.props.type},
              React.createElement("a", {className: "ui-slider-handle", tabIndex: "0"}),
              React.createElement("a", {className: "ui-slider-handle", tabIndex: "0"})
            )
          )
        );
      } else {
        return (
          React.createElement("div", {className: "EFDT-header-filters"},
            React.createElement("input", {className: "form-control",
              placeholder: this.props.hasOwnProperty('placeholder') ? this.props.placeholder : "Search...",
              "data-column": this.props.name,
              value: this.state.key,
              onChange: this.handleChange,
              title: "Input a keyword"})
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
                  "Showing ", this.props.filteredRowsSize, " samples",

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
            columnKey: field},
          React.createElement("span", {style: flag ? {backgroundColor: 'yellow'} : {},
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
                "data-qtip": '<b>MutSig</b><br/><i>Q-value</i>: ' + data[rowIndex].row.qval,
                alt: "MutSig"}) :
              React.createElement("img", {src: "images/gistic.png", className: "hasQtip qval-icon",
                "data-qtip": '<b>Gistic</b><br/><i>Q-value</i>: ' + data[rowIndex].row.qval,
                alt: "Gistic"})) : '',


          field === 'cases' ?
            React.createElement("input", {type: "checkbox", style: {float: 'right'},
              title: 'Select ' + data[rowIndex].row[field]
              + ' sample' + (Number(data[rowIndex].row[field]) > 1 ? 's' : '')
              + (tableType === 'mutatedGene' ? (' with ' + data[rowIndex].row.gene + ' mutation') :
                (tableType === 'cna' ? (' with ' + data[rowIndex].row.gene + ' ' + data[rowIndex].row.alttype) :
                  (tableType === 'pieLabel' ? (' in ' + data[rowIndex].row.name) : ''))),
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
        rows = this.props.filteredRows, columnsWidth = this.props.columnsWidth,
        cellShortLabels = this.props.shortLabels.cell,
        headerShortLabels = this.props.shortLabels.header,
        confirmedRowsIndex = this.props.confirmedRowsIndex,
        selectedRowIndex = this.state.selectedRowIndex,
        selectedGeneRowIndex = this.state.selectedGeneRowIndex,
        self = this;

      return (
        React.createElement("div", null,
          React.createElement(Table, {
              rowHeight: props.rowHeight ? props.rowHeight : 30,
              rowGetter: this.rowGetter,
              onScrollEnd: this.onScrollEnd,
              rowsCount: props.filteredRows.length,
              width: props.tableWidth ? props.tableWidth : 1230,
              maxHeight: props.maxHeight ? props.maxHeight : 500,
              headerHeight: props.headerHeight ? props.headerHeight : 30,
              groupHeaderHeight: props.groupHeaderHeight ? props.groupHeaderHeight : 50,
              scrollToColumn: props.goToColumn,
              isColumnResizing: false,
              onColumnResizeEndCallback: props.onColumnResizeEndCallback
            },

            props.cols.map(function(col, index) {
              var column;
              var width = col.show ? (col.width ? col.width :
                  (columnsWidth[col.name] ? columnsWidth[col.name] : 200)) : 0;

              if (props.groupHeader) {
                column = React.createElement(ColumnGroup, {
                    header:
                      React.createElement(Filter, {type: props.filters[col.name].type,
                        name: col.name,
                        max: col.max, min: col.min,
                        filter: props.filters[col.name],
                        placeholder: "Filter column",
                        onFilterKeywordChange: props.onFilterKeywordChange,
                        title: "Filter column"}
                      ),

                    key: col.name,
                    fixed: col.fixed,
                    align: "center"
                  },
                  React.createElement(Column, {
                    header:
                      React.createElement(HeaderWrapper, {cellDataKey: col.name, columnData: {
                          displayName: col.displayName,
                          sortFlag: props.sortBy === col.name,
                          sortDirArrow: props.sortDirArrow,
                          filterAll: props.filterAll,
                          type: props.filters[col.name].type
                        },
                          sortNSet: props.sortNSet,
                          filter: props.filters[col.name],
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
                    React.createElement(HeaderWrapper, {cellDataKey: col.name, columnData: {
                        displayName: col.displayName,
                        sortFlag: props.sortBy === col.name,
                        sortDirArrow: props.sortDirArrow,
                        filterAll: props.filterAll,
                        type: props.filters[col.name].type
                      },
                        sortNSet: props.sortNSet,
                        filter: props.filters[col.name],
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

    getRulerWidth: function(str, measureMethod, fontSize) {
      var rulerWidth = 0;

      //TODO: what about 0
      if (!str) {
        return 0;
      }

      str = str.toString();
      switch (measureMethod) {
      case 'jquery':
        var ruler = $("#ruler");
        ruler.css('font-size', fontSize);
        ruler.text(str);
        rulerWidth = ruler.outerWidth();
        break;
      default:
        var upperCaseLength = str.replace(/[^A-Z]/g, "").length;
        var dataLength = str.length;
        rulerWidth = upperCaseLength * (fontSize - 4) + (dataLength - upperCaseLength) * (fontSize - 6) + 15;
        break;
      }
      return rulerWidth;
    },

    //TODO: need to find way shorten this time. One possible solution is to calculate the categories for each column, and only detect the width for these categories.
    getShortLabels: function(rows, cols, columnWidth, measureMethod) {
      var cellShortLabels = [];
      var headerShortLabels = {};
      var self = this;
      _.each(rows, function(row) {
        var rowWidthObj = {};
        _.each(row, function(content, attr) {
          var _label = content;
          var _labelShort = _label;
          if (_label) {
            _label = _label.toString();
            var _labelWidth = self.getRulerWidth(_label, measureMethod, 14);

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

          if (_label) {
            _label = _label.toString();
            var _labelWidth = self.getRulerWidth(_label, measureMethod, 15);
            if (_labelWidth > columnWidth[col.name]) {
              var end = Math.floor((_label.length) * columnWidth[col.name] / _labelWidth) - 3;
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
            if (filters[col].type === "STRING") {
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
            } else if (filters[col].type === "NUMBER" || filters[col].type === 'PERCENTAGE') {
              var cell = filters[col].type === 'PERCENTAGE' ? Number(row[col].toString().replace('%', '')) : row[col];

              if (!isNaN(cell)) {
                if (hasGroupHeader) {
                  if (filters[col].min !== filters[col]._min && cell < filters[col].min) {
                    return false;
                  }
                  if (filters[col].max !== filters[col]._max && cell > filters[col].max) {
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

        if (confirmedRowsIndex.indexOf(a.index) !== -1 && confirmedRowsIndex.indexOf(b.index) === -1) {
          return -1;
        }

        if (confirmedRowsIndex.indexOf(a.index) === -1 && confirmedRowsIndex.indexOf(b.index) !== -1) {
          return 1;
        }

        if (sortBy === 'cytoband' && window.hasOwnProperty('StudyViewUtil')) {
          var _sortResult = window.StudyViewUtil.cytobanBaseSort(aVal, bVal);
          sortVal = sortDir === SortTypes.ASC ? -_sortResult : _sortResult;
        } else {

          if (type === "NUMBER") {
            aVal = (aVal && !isNaN(aVal)) ? Number(aVal) : aVal;
            bVal = (bVal && !isNaN(bVal)) ? Number(bVal) : bVal;
          }
          if (type === 'PERCENTAGE') {
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
        if (e.target.getAttribute("data-column") === "all") {
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
      state.sortBy = this.props.sortBy ? this.props.sortBy.toLowerCase() : 'cases';
      state.goToColumn = null;
      state.filterTimer = 0;
      state.sortDir = this.props.sortDir || this.SortTypes.DESC;
      state.rowClickFunc = this.rowClickCallback;
      state.selectButtonClickCallback = this.selectButtonClickCallback;
      return state;
    },

    rowClickCallback: function(selectedRows, isSelected, allSelectedRows) {
      var uniqueId = this.state.uniqueId;
      this.setState({
        selectedRows: allSelectedRows.map(function(item) {
          return item[uniqueId];
        })
      });
      if (_.isFunction(this.props.rowClickFunc)) {
        this.props.rowClickFunc(selectedRows, isSelected, allSelectedRows);
      }
    },

    selectButtonClickCallback: function() {
      var selectedRows = this.state.selectedRows;
      this.setState({
        confirmedRows: selectedRows
      });
      if (_.isFunction(this.props.selectButtonClickCallback)) {
        this.props.selectButtonClickCallback();
      }
    },

    getSelectedRowIndex: function(selectedRows) {
      var selectedRowIndex = [];
      var uniqueId = this.state.uniqueId;
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

    parseInputData: function(input, uniqueId, selectedRows, groupHeader, columnSorting) {
      var cols = [], rows = [], rowsDict = {}, attributes = input.attributes,
        data = input.data, dataLength = data.length, col, cell, i, filters = {},
        uniqueId = uniqueId || 'id', newCol,
        selectedRows = selectedRows || [],
        measureMethod = (dataLength > 100000 || !this.props.autoColumnWidth) ? 'charNum' : 'jquery',
        autoColumnWidth = this.props.autoColumnWidth,
        columnMinWidth = this.props.groupHeader ? 130 : 50; //The minimum width to at least fit in number slider.

      var selectedRowIndex = [];
      var columnsWidth = {}, self = this;

      // Gets column info from input
      var colsDict = {};
      for (i = 0; i < attributes.length; i++) {
        col = attributes[i];
        col.attr_id = col.attr_id.toLowerCase();
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
        colsDict[col.attr_id] = newCol;
        columnsWidth[col.attr_id] = 0;
      }

      // Gets data rows from input
      for (i = 0; i < dataLength; i++) {
        cell = data[i];
        cell.attr_id = cell.attr_id.toLowerCase();

        if (!colsDict.hasOwnProperty(cell.attr_id)) {
          continue;
        }

        if (!rowsDict[cell[uniqueId]]) {
          rowsDict[cell[uniqueId]] = {};
        }

        //Clean up the input data
        if (_.isUndefined(cell.attr_val)) {
          cell.attr_val = '';
        }

        if (colsDict[cell.attr_id].type === 'NUMBER') {
          rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val !== '' ? Number(cell.attr_val) : NaN;
        } else if (colsDict[cell.attr_id].type === 'STRING') {
          rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val.toString();
        } else if (colsDict[cell.attr_id].type === 'PERCENTAGE') {
          rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val.toString();
        } else {
          rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val;
        }

        if (autoColumnWidth) {
          var val = rowsDict[cell[uniqueId]][cell.attr_id];
          var rulerWidth = 0;
          if (val !== 0) {
            rulerWidth = val ? this.getRulerWidth(val, measureMethod, 14) : 0;
          }
          columnsWidth[cell.attr_id] = columnsWidth[cell.attr_id] < rulerWidth ? rulerWidth : columnsWidth[cell.attr_id];
        }
      }

      if (!autoColumnWidth) {
        _.each(cols, function(col, attr) {
          columnsWidth[col.name] = col.width ? col.width : 200;
        });
      } else {
        columnsWidth = _.object(_.map(columnsWidth, function(length, attr) {
          return [attr, length > self.props.columnMaxWidth ?
            self.props.columnMaxWidth :
            ( (length + 20) < columnMinWidth ?
              columnMinWidth : (length + 20))];
        }));
      }

      var index = 0;
      var _uniqueId = uniqueId.toLowerCase();
      _.each(rowsDict, function(item, i) {
        rowsDict[i][_uniqueId] = i;
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

        if (col.type === "NUMBER" || col.type === "PERCENTAGE") {
          var min = Number.MAX_VALUE, max = -Number.MAX_VALUE;
          for (var j = 0; j < rows.length; j++) {
            cell = col.type === "PERCENTAGE" ? Number(rows[j][col.name].replace('%')) : rows[j][col.name];
            if (!isNaN(cell)) {
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

      var shortLabels = this.getShortLabels(rows, cols, columnsWidth, measureMethod);

      return {
        cols: cols,
        rowsSize: rows.length,
        filters: filters,
        shortLabels: shortLabels,
        columnsWidth: columnsWidth,
        columnMinWidth: columnMinWidth,
        selectedRowIndex: selectedRowIndex,
        selectedRows: selectedRows,
        dataSize: dataLength,
        measureMethod: measureMethod,
        uniqueId: _uniqueId
      };
    },
    // If properties changed
    componentWillReceiveProps: function(newProps) {
      var state = this.parseInputData(newProps.input, newProps.uniqueId,
        newProps.selectedRows, newProps.groupHeader, newProps.columnSorting);
      state.confirmedRows = ['mutatedGene', 'cna'].indexOf(newProps.tableType) !== -1 ? state.selectedRows : [];
      state.filteredRows = null;
      state.filterAll = this.state.filterAll || '';
      if (newProps.sortBy && newProps.sortBy.toLowerCase() !== this.state.sortBy) {
        state.sortBy = newProps.sortBy.toLowerCase();
      } else {
        state.sortBy = this.state.sortBy || 'cases';
      }
      if (newProps.sortDir && newProps.sortDir.toLowerCase() !== this.state.sortDir) {
        state.sortDir = newProps.sortDir;
      } else {
        state.sortDir = this.state.sortDir || this.SortTypes.DESC;
      }
      state.goToColumn = null;
      state.filterTimer = 0;

      var filteredRows = this.filterRowsBy(state.filterAll, state.filters);
      var result = this.sortRowsBy(state.filters, filteredRows, state.sortBy, false);
      state.filteredRows = result.filteredRows;

      this.setState(state);
    },

    // Initializes filteredRows before first rendering
    componentWillMount: function() {
      var rows = this.rows.map(function(item, index) {
        return {
          row: item,
          index: index
        }
      });
      var result = this.sortRowsBy(this.state.filters, rows, this.state.sortBy, false);
      this.setState({
        filteredRows: result.filteredRows,
        sortBy: this.state.sortBy,
        sortDir: result.sortDir,
        filterAll: this.state.filterAll,
        filters: this.state.filters
      });
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
        var columnsWidth = this.state.columnsWidth;
        columnsWidth[key] = width;
        var shortLabels = this.getShortLabels(this.rows, cols, columnsWidth, this.state.measureMethod);
        this.setState({
          columnsWidth: columnsWidth,
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

    // Expose the current sorting settings
    getCurrentSort: function() {
      return {
        sortBy: this.state.sortBy,
        sortDir: this.state.sortDir
      };
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
              columnsWidth: this.state.columnsWidth,
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
              React.createElement("button", {className: "btn btn-default btn-xs",
                onClick: this.state.selectButtonClickCallback}, "Select" + ' ' +
                "Samples") : ''

          )
        )
      );
    }
  });

  return Main;
})();

'use strict';
window.DataManagerForIviz = (function($, _) {
  var content = {};

  // Clinical attributes will be transfered into table.
  var configs_;
  content.util = {};


  /**
   * General pick clinical attributes based on predesigned Regex
   * This filter is the same one which used in previous Google Charts Version,
   * should be revised later.
   *
   * @param {string} attr Clinical attribute ID.
   * @return {boolean} Whether input attribute passed the criteria.
   */
  content.util.isPreSelectedClinicalAttr = function(attr) {
    return attr.toLowerCase().match(/(os_survival)|(dfs_survival)|(mut_cnt_vs_cna)|(mutated_genes)|(cna_details)|(^age)|(gender)|(sex)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(sample_type)|(.*site.*)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(mutation_count)|(copy_number_alterations)/);
  };

  /**
   * Compare based on data availability.
   * Notice that: attribute with only one category will be moved to end.
   * Number of keys in this attribute is more than half numOfDatum
   * will be moved to end as well.
   *
   * @param {object} a Attribute meta item A.
   * @param {object} b Attribute meta item B.
   * @return {number} Indicator which item is selected.
   */
  content.util.compareClinicalAvailability = function(a, b) {
    if (!a.keys || !a.numOfDatum) {
      return 1;
    }
    if (!b.keys || !b.numOfDatum) {
      return -1;
    }

    var numOfKeysA = Object.keys(a.keys).length;
    var numOfKeysB = Object.keys(b.keys).length;
    if (numOfKeysA === 1 && numOfKeysB !== 1) {
      return 1;
    }
    if (numOfKeysA !== 1 && numOfKeysB === 1) {
      return -1;
    }

    if (numOfKeysA / a.numOfDatum > 0.5 && numOfKeysB / b.numOfDatum <= 0.5) {
      return 1;
    }
    if (numOfKeysA / a.numOfDatum <= 0.5 && numOfKeysB / b.numOfDatum > 0.5) {
      return -1;
    }

    return b.numOfDatum - a.numOfDatum;
  };

  /**
   * There are few steps to determine the priority.
   * Step 1: whether it is in clinAttrs_.general.priority list
   * Step 2: whether it will pass preSelectedAttr Regex check
   * Step 3: Sort the rest based on data availability. Notice that: at this
   * Step, attribute with only one category will be moved to end. Number of
   * keys in this attribute is more than half numOfDatum will be moved to end
   * as well.
   *
   * @param {array} array All clinical attributes.
   * @return {array} Sorted clinical attributes.
   */
  content.util.sortClinicalAttrs = function(array) {
    array = array.sort(function(a, b) {
      return compareClinicalAttrs(a, b);
    });
    return array;
  };

  function compareClinicalAttrs(a, b) {
    var priority = 0;

    if (content.util.isPreSelectedClinicalAttr(a.attr_id)) {
      if (content.util.isPreSelectedClinicalAttr(b.attr_id)) {
        priority = content.util.compareClinicalAvailability(a, b);
      } else {
        priority = -1;
      }
    } else if (content.util.isPreSelectedClinicalAttr(b.attr_id)) {
      priority = 1;
    } else {
      priority = 0;
    }

    if (priority !== 0) {
      return priority;
    }

    return content.util.compareClinicalAvailability(a, b);
  };

  /**
   * Sort clinical attributes by priority.
   * @param {array} array Clinical attributes.
   * @return {array} Sorted clinical attributes.
   */
  content.util.sortByClinicalPriority = function(array) {
    if (_.isArray(array)) {
      array = array.sort(function(a, b) {
        var score = iViz.priorityManager.comparePriorities(a.priority, b.priority, false);
        if (score === 0) {
          score = compareClinicalAttrs(a, b);
        }
        return score;
      });
    }
    return array;
  };

  content.util.pxStringToNumber = function(_str) {
    var result;
    if (_.isString(_str)) {
      var tmp = _str.split('px');
      if (tmp.length > 0) {
        result = Number(tmp[0]);
      }
    }
    return result;
  };

  /**
   * Finds the intersection elements between two arrays in a simple fashion.
   * Should have O(n) operations, where n is n = MIN(a.length, b.length)
   *
   * @param {array} a First array, must already be sorted
   * @param {array} b Second array, must already be sorted
   * @return {array} The interaction elements between a and b
   */
  content.util.intersection = function(a, b) {
    var result = [];
    var i = 0;
    var j = 0;
    var aL = a.length;
    var bL = b.length;
    while (i < aL && j < bL) {
      if (a[i] < b[j]) {
        ++i;
      } else if (a[i] > b[j]) {
        ++j;
      } else {
        result.push(a[i]);
        ++i;
        ++j;
      }
    }

    return result;
  };

  /**
   * Normalize clinical data type to all uppercaes.
   * If data type is not STRING or NUMBER, convert it to STRING
   *
   * @param {string} datatype
   * @return {string}
   */
  content.util.normalizeDataType = function(datatype) {
    var invalid = false;
    if (_.isString(datatype)) {
      datatype = datatype.toUpperCase();
      if (['STRING', 'NUMBER'].indexOf(datatype) === -1) {
        invalid = true;
      }
    } else {
      invalid = true;
    }
    if (invalid) {
      datatype = 'STRING';
    }
    return datatype;
  };

  content.init = function(_portalUrl, _study_cases_map) {
    var initialSetup = function() {
      var _def = new $.Deferred();
      var self = this;
      $.when(self.getSampleLists()).then(function() {
        $.when(self.getStudyToSampleToPatientdMap(), self.getConfigs()).then(function(_studyToSampleToPatientMap, _configs) {
          $.when(self.getGeneticProfiles(), self.getCaseLists(),
            self.getClinicalAttributesByStudy())
            .then(function(_geneticProfiles, _caseLists,
                           _clinicalAttributes) {
              var _result = {};
              var _patientData = [];
              var _sampleAttributes = {};
              var _patientAttributes = {};
              var _sampleData = [];
              var _hasDFS = false;
              var _hasOS = false;
              var _hasPatientAttrData = {};
              var _hasSampleAttrData = {};
              var _hasDfsStatus = false;
              var _hasDfsMonths = false;
              var _hasOsStatus = false;
              var _hasOsMonths = false;
              var _cnaCaseUIdsMap = {};
              var _sequencedCaseUIdsMap = {};
              var _cnaCaseUIDs = [];
              var _sequencedCaseUIDs = [];
              var _allCaseUIDs = [];

              iViz.priorityManager.setDefaultClinicalAttrPriorities(_configs.priority);

              $.each(_caseLists, function(studyId, caseList) {
                if (caseList.cnaSampleIds.length > 0) {
                  $.each(caseList.cnaSampleIds, function(index, sampleId) {
                    _cnaCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                  });
                }
                if (caseList.sequencedSampleIds.length > 0) {
                  $.each(caseList.sequencedSampleIds, function(index, sampleId) {
                    _sequencedCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                  });
                }
                if (caseList.allSampleIds.length > 0) {
                  $.each(caseList.allSampleIds, function(index, sampleId) {
                    _allCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                  });
                }
              });
              _cnaCaseUIDs = _cnaCaseUIDs.length > 0 ? _cnaCaseUIDs : _allCaseUIDs;
              _sequencedCaseUIDs = _sequencedCaseUIDs.length > 0 ? _sequencedCaseUIDs : _allCaseUIDs;
              _.each(_cnaCaseUIDs, function(_sampleUid) {
                _cnaCaseUIdsMap[_sampleUid] = _sampleUid;
              });
              _.each(_sequencedCaseUIDs, function(_sampleUid) {
                _sequencedCaseUIdsMap[_sampleUid] = _sampleUid;
              });

              _.each(_clinicalAttributes, function(attr) {
                if (attr.is_patient_attribute === '0') {
                  _sampleAttributes[attr.attr_id] = attr;
                } else {
                  _patientAttributes[attr.attr_id] = attr;
                }
              });

              var addAttr = function(data, group) {
                if (!_.isObject(data) || !data.attr_id || !group) {
                  return null;
                }

                var datum = {
                  attr_id: '',
                  datatype: 'STRING',
                  description: '',
                  display_name: '',
                  priority: iViz.priorityManager.getDefaultPriority(data.attr_id)
                };

                datum = _.extend(datum, data);

                if (group === 'patient') {
                  _patientAttributes[datum.attr_id] = datum;
                } else if (group === 'sample') {
                  _sampleAttributes[datum.attr_id] = datum;
                }
              };

              // Add three additional attributes for all studies.
              addAttr({
                attr_id: 'sequenced',
                display_name: 'With Mutation Data',
                description: 'If the sample has mutation data'
              }, 'sample');

              addAttr({
                attr_id: 'has_cna_data',
                display_name: 'With CNA Data',
                description: 'If the sample has CNA data'
              }, 'sample');

              addAttr({
                attr_id: 'sample_count_patient',
                display_name: '# of Samples Per Patient',
                description: ''
              }, 'patient');

              // TODO : temporary fix to show/hide charts
              // define view type from data type
              _.each(_sampleAttributes, function(_metaObj) {
                _metaObj.filter = [];
                _metaObj.keys = {};
                _metaObj.numOfDatum = 0;
                _metaObj.addChartBy = 'default';
                if (!_.isArray(_metaObj.priority)) {
                  iViz.priorityManager
                    .setClinicalAttrPriority(_metaObj.attr_id, Number(_metaObj.priority));
                  _metaObj.priority =
                    iViz.priorityManager
                      .getDefaultPriority(_metaObj.attr_id);
                }
                _metaObj.show = _metaObj.priority !== 0;
                _metaObj.attrList = [_metaObj.attr_id];
                _metaObj.datatype = content.util.normalizeDataType(_metaObj.datatype);
                if (_metaObj.datatype === 'NUMBER') {
                  _metaObj.view_type = 'bar_chart';
                  _metaObj.layout = [-1, 2, 'h'];
                } else if (_metaObj.datatype === 'STRING') {
                  _metaObj.view_type = 'pie_chart';
                  _metaObj.layout = [-1, 1];
                }
                if (configs_.tableAttrs.indexOf(_metaObj.attr_id) !== -1) {
                  _metaObj.view_type = 'table';
                  _metaObj.layout = [-1, 4];
                  _metaObj.type = 'pieLabel';
                  _metaObj.options = {
                    allCases: _caseLists.allCaseUIDs,
                    sequencedCases: _caseLists.allCaseUIDs
                  };
                }
                if (['CANCER_TYPE', 'CANCER_TYPE_DETAILED']
                    .indexOf(_metaObj.attr_id) !== -1) {
                  _metaObj.priority =
                    iViz.priorityManager
                      .getDefaultPriority(_metaObj.attr_id);
                }
              });
              _.each(_patientAttributes, function(_metaObj) {
                switch (_metaObj.attr_id) {
                  case 'DFS_STATUS':
                    _hasDfsStatus = true;
                    break;
                  case 'DFS_MONTHS':
                    _hasDfsMonths = true;
                    break;
                  case 'OS_STATUS':
                    _hasOsStatus = true;
                    break;
                  case 'OS_MONTHS':
                    _hasOsMonths = true;
                    break;
                  default :
                    break;
                }
                _metaObj.filter = [];
                _metaObj.keys = {};
                _metaObj.numOfDatum = 0;
                _metaObj.addChartBy = 'default';
                if (!_.isArray(_metaObj.priority)) {
                  iViz.priorityManager
                    .setClinicalAttrPriority(_metaObj.attr_id, Number(_metaObj.priority));
                  _metaObj.priority =
                    iViz.priorityManager
                      .getDefaultPriority(_metaObj.attr_id);
                }
                _metaObj.show = _metaObj.priority !== 0;
                _metaObj.attrList = [_metaObj.attr_id];
                _metaObj.datatype = content.util.normalizeDataType(_metaObj.datatype);
                if (_metaObj.datatype === 'NUMBER') {
                  _metaObj.view_type = 'bar_chart';
                  _metaObj.layout = [-1, 2, 'h'];
                } else if (_metaObj.datatype === 'STRING') {
                  _metaObj.view_type = 'pie_chart';
                  _metaObj.layout = [-1, 1];
                }
                if (configs_.tableAttrs.indexOf(_metaObj.attr_id) !== -1) {
                  _metaObj.view_type = 'table';
                  _metaObj.layout = [-1, 4];
                  _metaObj.type = 'pieLabel';
                  _metaObj.options = {
                    allCases: _caseLists.allCaseUIDs,
                    sequencedCases: _caseLists.allCaseUIDs
                  };
                }
              });

              if (_hasDfsStatus && _hasDfsMonths) {
                _hasDFS = true;
              }
              if (_hasOsStatus && _hasOsMonths) {
                _hasOS = true;
              }

              var _samplesToPatientMap = {};
              var _patientToSampleMap = {};

              _hasSampleAttrData.sample_uid = '';
              _hasSampleAttrData.sample_id = '';
              _hasSampleAttrData.study_id = '';
              _hasSampleAttrData.sequenced = '';
              _hasSampleAttrData.has_cna_data = '';
              _.each(_studyToSampleToPatientMap, function(_sampleToPatientMap, _studyId) {
                _.each(_sampleToPatientMap.sample_uid_to_patient_uid, function(_patientUID, _sampleUID) {
                  if (_samplesToPatientMap[_sampleUID] === undefined) {
                    _samplesToPatientMap[_sampleUID] = [_patientUID];
                  }
                  if (_patientToSampleMap[_patientUID] === undefined) {
                    _patientToSampleMap[_patientUID] = [_sampleUID];
                  } else {
                    _patientToSampleMap[_patientUID].push(_sampleUID);
                  }

                  if (_patientData[_patientUID] === undefined) {
                    // create datum for each patient
                    var _patientDatum = {};
                    _patientDatum.patient_uid = _patientUID;
                    _patientDatum.patient_id = _sampleToPatientMap.uid_to_patient[_patientUID];
                    _patientDatum.study_id = _studyId;
                    _hasPatientAttrData.patient_id = '';
                    _hasPatientAttrData.patient_uid = '';
                    _hasPatientAttrData.study_id = '';
                    _patientData[_patientUID] = _patientDatum;
                  }

                  // create datum for each sample
                  var _sampleDatum = {};
                  _sampleDatum.sample_id = _sampleToPatientMap.uid_to_sample[_sampleUID];
                  _sampleDatum.sample_uid = _sampleUID;
                  _sampleDatum.study_id = _studyId;
                  _sampleDatum.has_cna_data = 'NO';
                  _sampleDatum.sequenced = 'NO';

                  if (self.hasMutationData()) {
                    if (_sequencedCaseUIdsMap[_sampleUID] !== undefined) {
                      _sampleDatum.sequenced = 'YES';
                    }
                    _sampleDatum.mutated_genes = [];
                  }
                  if (self.hasCnaSegmentData()) {
                    if (_cnaCaseUIdsMap[_sampleUID] !== undefined) {
                      _sampleDatum.has_cna_data = 'YES';
                    }
                    _sampleDatum.cna_details = [];
                  }
                  _sampleData[_sampleUID] = _sampleDatum;
                });
              });

              // Add sample_count_patient data
              _.each(_patientData, function(datum, patientUID) {
                _hasPatientAttrData.sample_count_patient = '';
                if (_patientToSampleMap.hasOwnProperty(patientUID)) {
                  datum.sample_count_patient = _patientToSampleMap[patientUID].length.toString();
                }
              });

              // add CNA Table
              if (self.hasCnaSegmentData()) {
                _hasSampleAttrData.cna_details = '';
                var _cnaAttrMeta = {};
                _cnaAttrMeta.type = 'cna';
                _cnaAttrMeta.view_type = 'table';
                _cnaAttrMeta.layout = [-1, 4];
                _cnaAttrMeta.display_name = 'CNA Genes';
                _cnaAttrMeta.description = 'This table only shows ' +
                  '<a href="cancer_gene_list.jsp" target="_blank">' +
                  'cBioPortal cancer genes</a> in the cohort.';
                _cnaAttrMeta.attr_id = 'cna_details';
                _cnaAttrMeta.filter = [];
                _cnaAttrMeta.addChartBy = 'default';
                _cnaAttrMeta.keys = {};
                _cnaAttrMeta.numOfDatum = 0;
                _cnaAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority(_cnaAttrMeta.attr_id);
                _cnaAttrMeta.show = _cnaAttrMeta.priority !== 0;
                _cnaAttrMeta.attrList = [_cnaAttrMeta.attr_id];
                _cnaAttrMeta.options = {
                  allCases: _allCaseUIDs,
                  sequencedCases: _cnaCaseUIDs
                };
                _sampleAttributes[_cnaAttrMeta.attr_id] = _cnaAttrMeta;
              }

              // add Gene Mutation Info
              if (self.hasMutationData()) {
                _hasSampleAttrData.mutated_genes = '';
                var _mutDataAttrMeta = {};
                _mutDataAttrMeta.type = 'mutatedGene';
                _mutDataAttrMeta.view_type = 'table';
                _mutDataAttrMeta.layout = [-1, 4];
                _mutDataAttrMeta.display_name = 'Mutated Genes';
                _mutDataAttrMeta.description = 'This table shows ' +
                  '<a href="cancer_gene_list.jsp" target="_blank">' +
                  'cBioPortal cancer genes</a> ' +
                  'with 1 or more mutations, as well as any ' +
                  'gene with 2 or more mutations';
                _mutDataAttrMeta.attr_id = 'mutated_genes';
                _mutDataAttrMeta.filter = [];
                _mutDataAttrMeta.addChartBy = 'default';
                _mutDataAttrMeta.keys = {};
                _mutDataAttrMeta.numOfDatum = 0;
                _mutDataAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority(_mutDataAttrMeta.attr_id);
                _mutDataAttrMeta.show = _mutDataAttrMeta.priority !== 0;
                _mutDataAttrMeta.attrList = [_mutDataAttrMeta.attr_id];
                _mutDataAttrMeta.options = {
                  allCases: _allCaseUIDs,
                  sequencedCases: _sequencedCaseUIDs
                };
                _sampleAttributes[_mutDataAttrMeta.attr_id] = _mutDataAttrMeta;
              }

              if (_hasDFS) {
                var _dfsSurvivalAttrMeta = {};
                _dfsSurvivalAttrMeta.attr_id = 'DFS_SURVIVAL';
                _dfsSurvivalAttrMeta.datatype = 'SURVIVAL';
                _dfsSurvivalAttrMeta.view_type = 'survival';
                _dfsSurvivalAttrMeta.layout = [-1, 4];
                _dfsSurvivalAttrMeta.description = '';
                _dfsSurvivalAttrMeta.display_name = 'Disease Free Survival';
                _dfsSurvivalAttrMeta.filter = [];
                _dfsSurvivalAttrMeta.addChartBy = 'default';
                _dfsSurvivalAttrMeta.keys = {};
                _dfsSurvivalAttrMeta.numOfDatum = 0;
                _dfsSurvivalAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority('DFS_SURVIVAL', true);
                _dfsSurvivalAttrMeta.show = _dfsSurvivalAttrMeta.priority !== 0;
                _dfsSurvivalAttrMeta.attrList = ['DFS_STATUS', 'DFS_MONTHS'];
                _patientAttributes[_dfsSurvivalAttrMeta.attr_id] = _dfsSurvivalAttrMeta;
              }

              if (_hasOS) {
                var _osSurvivalAttrMeta = {};
                _osSurvivalAttrMeta.attr_id = 'OS_SURVIVAL';
                _osSurvivalAttrMeta.datatype = 'SURVIVAL';
                _osSurvivalAttrMeta.view_type = 'survival';
                _osSurvivalAttrMeta.layout = [-1, 4];
                _osSurvivalAttrMeta.description = '';
                _osSurvivalAttrMeta.display_name = 'Overall Survival';
                _osSurvivalAttrMeta.filter = [];
                _osSurvivalAttrMeta.addChartBy = 'default';
                _osSurvivalAttrMeta.keys = {};
                _osSurvivalAttrMeta.numOfDatum = 0;
                _osSurvivalAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority('OS_SURVIVAL', true);
                _osSurvivalAttrMeta.show = _osSurvivalAttrMeta.priority !== 0;
                _osSurvivalAttrMeta.attrList = ['OS_STATUS', 'OS_MONTHS'];
                _patientAttributes[_osSurvivalAttrMeta.attr_id] = _osSurvivalAttrMeta;
              }

              // add Cancer Study
              if (self.getCancerStudyIds().length > 1) {
                var _id = 'study_id';
                _patientAttributes.study_id = {
                  datatype: 'STRING',
                  description: '',
                  display_name: 'Cancer Studies',
                  attr_id: _id,
                  view_type: 'pie_chart',
                  layout: [-1, 1],
                  filter: [],
                  keys: [],
                  numOfDatum: 0,
                  priority: iViz.priorityManager.getDefaultPriority(_id),
                  show: true,
                  addChartBy: 'default',
                  attrList: [_id]
                };
                _patientAttributes.study_id.show = _patientAttributes.study_id.priority !== 0;
              }
              // add Copy Number Alterations bar chart
              // TODO : need to set priority
              if (_hasSampleAttrData.copy_number_alterations !== undefined) {
                var _id = 'copy_number_alterations';
                _sampleAttributes.copy_number_alterations = {
                  datatype: 'NUMBER',
                  description: '',
                  display_name: 'Fraction of copy number altered genome',
                  attr_id: _id,
                  view_type: 'bar_chart',
                  layout: [-1, 2, 'h'],
                  priority: iViz.priorityManager.getDefaultPriority(_id),
                  filter: [],
                  attrList: [_id],
                  keys: [],
                  numOfDatum: 0,
                  show: true,
                  addChartBy: 'default'
                };
                _sampleAttributes.copy_number_alterations.show = _sampleAttributes.copy_number_alterations.priority !== 0;
              }

              _result.groups = {
                group_mapping: {
                  patient_to_sample: _patientToSampleMap,
                  sample_to_patient: _samplesToPatientMap,
                  studyMap: _studyToSampleToPatientMap
                },
                patient: {
                  attr_meta: content.util
                    .sortByClinicalPriority(_.values(_patientAttributes)),
                  data: _patientData,
                  has_attr_data: _hasPatientAttrData
                },
                sample: {
                  attr_meta: content.util
                    .sortByClinicalPriority(_.values(_sampleAttributes)),
                  data: _sampleData,
                  has_attr_data: _hasSampleAttrData
                }
              };

              // add Mutation count vs. CNA fraction
              _hasSampleAttrData.copy_number_alterations = '';
              _hasSampleAttrData.cna_fraction = '';
              var _mutCntAttrMeta = {};
              _mutCntAttrMeta.attr_id = 'MUT_CNT_VS_CNA';
              _mutCntAttrMeta.datatype = 'SCATTER_PLOT';
              _mutCntAttrMeta.view_type = 'scatter_plot';
              _mutCntAttrMeta.layout = [-1, 4];
              _mutCntAttrMeta.description = '';
              _mutCntAttrMeta.display_name = 'Mutation Count vs. CNA';
              _mutCntAttrMeta.filter = [];
              _mutCntAttrMeta.keys = {};
              _mutCntAttrMeta.numOfDatum = 0;
              _mutCntAttrMeta.priority =
                iViz.priorityManager
                  .getDefaultPriority('MUT_CNT_VS_CNA', true);
              _mutCntAttrMeta.show = _mutCntAttrMeta.priority !== 0;
              _mutCntAttrMeta.addChartBy = 'default';
              _mutCntAttrMeta.attrList = ['mutation_count', 'cna_fraction'];
              // This attribute is used for getScatterData()
              // This should not be added into attribute meta and should be saved into main.js 
              // (Centralized place storing all data for sharing across directives)
              // This needs to be updated after merging into virtual study branch
              _mutCntAttrMeta.sequencedCaseUIdsMap = _sequencedCaseUIdsMap; 
              _sampleAttributes[_mutCntAttrMeta.attr_id] = _mutCntAttrMeta;

              // add mutation count
              _hasSampleAttrData.mutation_count = '';
              var _MutationCountMeta = {};
              _MutationCountMeta.datatype = 'NUMBER';
              _MutationCountMeta.description = '';
              _MutationCountMeta.display_name = 'Mutation Count';
              _MutationCountMeta.attr_id = 'mutation_count';
              _MutationCountMeta.view_type = 'bar_chart';
              _MutationCountMeta.layout = [-1, 2, 'h'];
              _MutationCountMeta.filter = [];
              _MutationCountMeta.keys = {};
              _MutationCountMeta.numOfDatum = 0;
              _MutationCountMeta.priority =
                iViz.priorityManager
                  .getDefaultPriority(_MutationCountMeta.attr_id);
              _MutationCountMeta.show = _MutationCountMeta.priority !== 0;
              _MutationCountMeta.addChartBy = 'default';
              _MutationCountMeta.attrList = [_MutationCountMeta.attr_id];
              // This attribute is used for getMutationCountData()
              _MutationCountMeta.sequencedCaseUIdsMap = _sequencedCaseUIdsMap;
              _sampleAttributes[_MutationCountMeta.attr_id] = _MutationCountMeta;

              _result.groups.patient.attr_meta =
                content.util
                  .sortByClinicalPriority(_.values(_patientAttributes));
              _result.groups.sample.attr_meta =
                content.util
                  .sortByClinicalPriority(_.values(_sampleAttributes));

              self.initialSetupResult = _result;
              _def.resolve(_result);
            });
        });
      });
      return _def.promise();
    };

    // Borrowed from cbioportal-client.js
    var getApiCallPromise = function(endpt, args) {
      var arg_strings = [];
      for (var k in args) {
        if (args.hasOwnProperty(k)) {
          arg_strings.push(k + '=' + [].concat(args[k]).join(','));
        }
      }
      var arg_string = arg_strings.join('&') || '?';
      return $.ajax({
        type: 'POST',
        url: window.cbioURL + endpt,
        data: arg_string,
        dataType: 'json'
      });
    };

    var getPatientClinicalData = function(self, attr_ids) {
      var def = new $.Deferred();
      var fetch_promises = [];
      var clinical_data = {};
      if (_.isArray(attr_ids)) {
        attr_ids = attr_ids.slice();
      }
      $.when(self.getClinicalAttributesByStudy())
        .then(function(attributes) {
          var studyCasesMap = self.getStudyCasesMap();
          var studyAttributesMap = {};
          if (!_.isArray(attr_ids)) {
            attr_ids = Object.keys(attributes);
          }
          _.each(attr_ids, function(_attrId) {
            var attrDetails = attributes[_attrId];
            if (attrDetails !== undefined) {
              _.each(attrDetails.study_ids, function(studyId) {
                if (studyAttributesMap[studyId] === undefined) {
                  studyAttributesMap[studyId] = [attrDetails.attr_id];
                } else {
                  studyAttributesMap[studyId].push(attrDetails.attr_id);
                }
              });
            }
          });

          fetch_promises = fetch_promises.concat(Object.keys(studyAttributesMap).map(function(_studyId) {
            var _def = new $.Deferred();
            // Bypass cBioPortal client for clinical data call.
            // Checking whether patient clinical data is available takes too much
            // time. This is temporary solution, should be replaced with
            // better solution.
            var uniqueId = _studyId + studyAttributesMap[_studyId].sort().join('') + studyCasesMap[_studyId].patients.sort().join('');
            if (self.data.clinical.patient.hasOwnProperty(uniqueId)) {
              var data = self.data.clinical.patient[uniqueId];
              for (var i = 0; i < data.length; i++) {
                data[i].attr_id = data[i].attr_id.toUpperCase();
                var attr_id = data[i].attr_id;
                clinical_data[attr_id] = clinical_data[attr_id] || [];
                clinical_data[attr_id].push(data[i]);
              }
              _def.resolve();
            } else {
              getApiCallPromise('api-legacy/clinicaldata/patients', {
                study_id: [_studyId],
                attribute_ids: studyAttributesMap[_studyId],
                patient_ids: studyCasesMap[_studyId].patients
              }).then(function(data) {
                self.data.clinical.patient[uniqueId] = data;
                for (var i = 0; i < data.length; i++) {
                  data[i].attr_id = data[i].attr_id.toUpperCase();
                  var attr_id = data[i].attr_id;
                  clinical_data[attr_id] = clinical_data[attr_id] || [];
                  clinical_data[attr_id].push(data[i]);
                }
                _def.resolve();
              }).fail(
                function() {
                  def.reject();
                });
            }
            return _def.promise();
          }));
          $.when.apply($, fetch_promises).then(function() {
            def.resolve(clinical_data);
          });
        });
      return def.promise();
    };

    var getSampleClinicalData = function(self, attr_ids) {
      var def = new $.Deferred();
      var fetch_promises = [];
      var clinical_data = {};
      if (_.isArray(attr_ids)) {
        attr_ids = attr_ids.slice();
      }
      $.when(self.getClinicalAttributesByStudy())
        .then(function(attributes) {
          var studyCasesMap = self.getStudyCasesMap();
          var studyAttributesMap = {};
          if (!_.isArray(attr_ids)) {
            attr_ids = Object.keys(attributes);
          }

          _.each(attr_ids, function(_attrId) {
            var attrDetails = attributes[_attrId];
            if (attrDetails !== undefined) {
              _.each(attrDetails.study_ids, function(studyId) {
                if (studyAttributesMap[studyId] === undefined) {
                  studyAttributesMap[studyId] = [attrDetails.attr_id];
                } else {
                  studyAttributesMap[studyId].push(attrDetails.attr_id);
                }
              });
            }
          });

          fetch_promises = fetch_promises.concat(Object.keys(studyAttributesMap)
            .map(function(_studyId) {
              var _def = new $.Deferred();
              // Bypass cBioPortal client for clinical data call.
              // Checking whether sample clinical data is available takes too much
              // time. This is temporary solution, should be replaced with
              // better solution.
              var uniqueId = _studyId + studyAttributesMap[_studyId].sort().join('') + studyCasesMap[_studyId].samples.sort().join('');
              if (self.data.clinical.sample.hasOwnProperty(uniqueId)) {
                var data = self.data.clinical.sample[uniqueId];
                for (var i = 0; i < data.length; i++) {
                  data[i].attr_id = data[i].attr_id.toUpperCase();
                  var attr_id = data[i].attr_id;
                  clinical_data[attr_id] = clinical_data[attr_id] || [];
                  clinical_data[attr_id].push(data[i]);
                }
                _def.resolve();
              } else {
                getApiCallPromise('api-legacy/clinicaldata/samples', {
                  study_id: [_studyId],
                  attribute_ids: studyAttributesMap[_studyId],
                  sample_ids: studyCasesMap[_studyId].samples
                }).then(function(data) {
                  self.data.clinical.sample[uniqueId] = data;
                  for (var i = 0; i < data.length; i++) {
                    data[i].attr_id = data[i].attr_id.toUpperCase();
                    var attr_id = data[i].attr_id;
                    clinical_data[attr_id] = clinical_data[attr_id] || [];
                    clinical_data[attr_id].push(data[i]);
                  }
                  _def.resolve();
                }).fail(
                  function() {
                    def.reject();
                  });
              }
              return _def.promise();
            }));
          $.when.apply($, fetch_promises).then(function() {
            def.resolve(clinical_data);
          });
        });
      return def.promise();
    };

    return {
      initialSetupResult: '',
      cancerStudyIds: [],
      mutationProfileIdsMap: {},
      cnaProfileIdsMap: {},
      panelSampleMap: {},
      portalUrl: _portalUrl,
      studyCasesMap: _study_cases_map,
      initialSetup: initialSetup,
      hasMutationData: function() {
        return _.keys(this.mutationProfileIdsMap).length > 0;
      },
      hasCnaSegmentData: function() {
        return _.keys(this.cnaProfileIdsMap).length > 0;
      },
      getCancerStudyIds: function() {
        if (this.cancerStudyIds.length === 0) {
          this.cancerStudyIds = _.keys(this.studyCasesMap);
        }
        return this.cancerStudyIds;
      },
      getStudyCasesMap: function() {
        return window.cbio.util.deepCopyObject(this.studyCasesMap);
      },
      data: {
        clinical: {
          sample: {},
          patient: {}
        },
        sampleLists: {
          all: {},
          sequenced: {},
          cna: {},
          lists: {}
        }
      },
      // The reason to separate style variable into individual json is
      // that the scss file can also rely on this file.
      getConfigs: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          if (_.isObject(configs_)) {
            fetch_promise.resolve(configs_);
          } else {
            $.getJSON(window.cbioResourceURL + 'configs.json')
              .then(function(data) {
                var configs = {
                  styles: {
                    vars: {}
                  }
                };
                configs = $.extend(true, configs, data);
                configs.styles.vars.width = {
                  one: content.util.pxStringToNumber(data['grid-w-1']) || 195,
                  two: content.util.pxStringToNumber(data['grid-w-2']) || 400
                };
                configs.styles.vars.height = {
                  one: content.util.pxStringToNumber(data['grid-h-1']) || 170,
                  two: content.util.pxStringToNumber(data['grid-h-2']) || 350
                };
                configs.styles.vars.chartHeader = 17;
                configs.styles.vars.borderWidth = 2;
                configs.styles.vars.scatter = {
                  width: (
                  configs.styles.vars.width.two -
                  configs.styles.vars.borderWidth) || 400,
                  height: (
                  configs.styles.vars.height.two -
                  configs.styles.vars.chartHeader -
                  configs.styles.vars.borderWidth) || 350
                };
                configs.styles.vars.survival = {
                  width: configs.styles.vars.scatter.width,
                  height: configs.styles.vars.scatter.height
                };
                configs.styles.vars.specialTables = {
                  width: configs.styles.vars.scatter.width,
                  height: configs.styles.vars.scatter.height - 25
                };
                configs.styles.vars.piechart = {
                  width: 140,
                  height: 140
                };
                configs.styles.vars.barchart = {
                  width: (
                  configs.styles.vars.width.two -
                  configs.styles.vars.borderWidth) || 400,
                  height: (
                  configs.styles.vars.height.one -
                  configs.styles.vars.chartHeader * 2 -
                  configs.styles.vars.borderWidth) || 130
                };
                configs_ = configs;
                fetch_promise.resolve(configs);
              })
              .fail(function() {
                fetch_promise.resolve();
              });
          }
        }),
      getGeneticProfiles: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _profiles = [];
          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              window.cbioportal_client
                .getGeneticProfiles({study_id: [cancer_study_id]})
                .then(function(profiles) {
                  _profiles = _profiles.concat(profiles);
                  def.resolve();
                }).fail(
                function() {
                  fetch_promise.reject();
                });
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            _.each(_profiles, function(_profile) {
              if (_profile.genetic_alteration_type === 'COPY_NUMBER_ALTERATION' && _profile.datatype === 'DISCRETE') {
                self.cnaProfileIdsMap[_profile.study_id] = _profile.id;
              } else if (_profile.genetic_alteration_type === 'MUTATION_EXTENDED' && (_profile.study_id + '_mutations_uncalled' !== _profile.id)) {
                self.mutationProfileIdsMap[_profile.study_id] = _profile.id;
              }
            });
            fetch_promise.resolve(_profiles);
          }).fail(function() {
            fetch_promise.reject();
          });
        }),
      getCaseLists: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _responseStudyCaseList = {};
          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              self.getSampleListsData(['all', 'sequenced', 'cna'], cancer_study_id)
                .done(function() {
                  var studyCaseList = {
                    sequencedSampleIds: [],
                    cnaSampleIds: [],
                    allSampleIds: []
                  };
                  if (_.isArray(self.data.sampleLists.sequenced[cancer_study_id])) {
                    studyCaseList.sequencedSampleIds = self.data.sampleLists.sequenced[cancer_study_id];
                  }
                  if (_.isArray(self.data.sampleLists.cna[cancer_study_id])) {
                    studyCaseList.cnaSampleIds = self.data.sampleLists.cna[cancer_study_id];
                  }
                  if (_.isArray(self.data.sampleLists.all[cancer_study_id])) {
                    studyCaseList.allSampleIds = self.data.sampleLists.all[cancer_study_id];
                  }
                  _responseStudyCaseList[cancer_study_id] = studyCaseList;
                  def.resolve();
                }).fail(function() {
                fetch_promise.reject();
              });
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            fetch_promise.resolve(_responseStudyCaseList);
          }).fail(function() {
            fetch_promise.reject();
          });
        }),
      getClinicalAttributesByStudy: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var clinical_attributes_set = {};
          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              window.cbioportal_client.getClinicalAttributesByStudy({
                study_id: [cancer_study_id]
              }).then(function(attrs) {
                for (var i = 0; i < attrs.length; i++) {
                  // TODO : Need to update logic incase if multiple studies
                  // have same attribute name but different properties
                  attrs[i].attr_id = attrs[i].attr_id.toUpperCase();
                  if (clinical_attributes_set[attrs[i].attr_id] === undefined) {
                    attrs[i].study_ids = [cancer_study_id];
                    clinical_attributes_set[attrs[i].attr_id] = attrs[i];
                  } else {
                    attrs[i].study_ids =
                      clinical_attributes_set[attrs[i].attr_id]
                        .study_ids.concat(cancer_study_id);
                    clinical_attributes_set[attrs[i].attr_id] = attrs[i];
                  }
                }
                def.resolve();
              }).fail(function() {
                fetch_promise.reject();
              });
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            fetch_promise.resolve(clinical_attributes_set);
          }).fail(function() {
            fetch_promise.reject();
          });
        }),
      getStudyToSampleToPatientdMap: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var study_to_sample_to_patient = {};
          var _sample_uid = 0;
          var _patient_uid = 0
          var getSamplesCall = function(cancerStudyId) {
            var def = new $.Deferred();
            window.cbioportal_client.getSamples({
              study_id: [cancerStudyId],
              sample_ids: self.studyCasesMap[cancerStudyId].samples
            }).then(function(data) {
              var patient_to_sample = {};
              var sample_to_patient = {};
              var sample_uid_to_patient_uid = {};
              var uid_to_sample = {};
              var sample_to_uid = {};
              var patient_to_uid = {};
              var uid_to_patient = {};
              var resultMap = {};
              var patientList = {};
              for (var i = 0; i < data.length; i++) {
                uid_to_sample[_sample_uid] = data[i].id;
                sample_to_uid[data[i].id] = _sample_uid.toString();
                if (patient_to_uid[data[i].patient_id] === undefined) {
                  uid_to_patient[_patient_uid] = data[i].patient_id;
                  patient_to_uid[data[i].patient_id] = _patient_uid.toString();
                  _patient_uid++;
                }
                if (!patient_to_sample.hasOwnProperty(data[i].patient_id)) {
                  patient_to_sample[data[i].patient_id] = {};
                }
                patient_to_sample[data[i].patient_id][data[i].id] = 1;
                sample_to_patient[data[i].id] = data[i].patient_id;
                sample_uid_to_patient_uid[_sample_uid] = patient_to_uid[data[i].patient_id];
                patientList[data[i].patient_id] = 1;
                _sample_uid++;
              }
              // set patient list in studyCasesMap if sample list is
              // passed in the input
              if (_.isArray(self.studyCasesMap[cancerStudyId].samples) &&
                self.studyCasesMap[cancerStudyId].samples.length > 0) {
                self.studyCasesMap[cancerStudyId].patients = Object.keys(patientList);
              }
              resultMap.uid_to_sample = uid_to_sample;
              resultMap.uid_to_patient = uid_to_patient;
              resultMap.sample_to_uid = sample_to_uid;
              resultMap.patient_to_uid = patient_to_uid;
              resultMap.sample_to_patient = sample_to_patient;
              resultMap.patient_to_sample = _.mapObject(patient_to_sample, function(item) {
                return _.keys(item);
              });
              resultMap.sample_uid_to_patient_uid = sample_uid_to_patient_uid;
              study_to_sample_to_patient[cancerStudyId] = resultMap;
              def.resolve();
            }).fail(function() {
              def.reject();
            });
            return def.promise();
          };

          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              if (!self.studyCasesMap.hasOwnProperty(cancer_study_id)) {
                self.studyCasesMap[cancer_study_id] = {};
              }
              if (_.isArray(self.studyCasesMap[cancer_study_id].samples)) {
                getSamplesCall(cancer_study_id)
                  .then(function() {
                    def.resolve();
                  })
                  .fail(function() {
                    fetch_promise.reject();
                  });
              } else {
                self.getSampleListsData(['all'], cancer_study_id)
                  .done(function() {
                    if (_.isArray(self.data.sampleLists.all[cancer_study_id])) {
                      self.studyCasesMap[cancer_study_id].samples =
                        self.data.sampleLists.all[cancer_study_id];
                    }
                    getSamplesCall(cancer_study_id)
                      .then(function() {
                        def.resolve();
                      })
                      .fail(function() {
                        fetch_promise.reject();
                      });
                  }).fail(function() {
                  fetch_promise.reject();
                });
              }
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            fetch_promise.resolve(study_to_sample_to_patient);
          });
        }),
      getSampleListsData: function(lists, studyId) {
        var def = new $.Deferred();
        var self = this;
        var promises = [];
        if (_.isArray(lists)) {
          _.each(lists, function(list) {
            var _def = new $.Deferred();
            if (list && studyId) {
              if (self.data.sampleLists.lists.hasOwnProperty(studyId)
                && _.isArray(self.data.sampleLists.lists[studyId])
                && self.data.sampleLists.lists[studyId].indexOf(studyId + '_' + list) !== -1) {
                if (!self.data.sampleLists.hasOwnProperty(list)) {
                  self.data.sampleLists[list] = {};
                }
                if (self.data.sampleLists[list].hasOwnProperty(studyId)) {
                  _def.resolve(self.data.sampleLists[list][studyId]);
                } else {
                  $.ajax({
                    url: window.cbioURL + 'api/sample-lists/' +
                    studyId + '_' + list + '/sample-ids',
                    contentType: "application/json",
                    type: 'GET'
                  }).done(function(data) {
                    self.data.sampleLists[list][studyId] = data;
                    _def.resolve(data);
                  }).fail(function() {
                    _def.reject();
                  });
                }
                promises.push(_def.promise());
              }
            }
          });
          $.when.apply($, promises)
            .then(function() {
              def.resolve();
            })
            .fail(function() {
              def.reject();
            });
        } else {
          def.reject();
        }
        return def.promise();
      },
      getSampleLists: function() {
        var def = new $.Deferred();
        var self = this;
        var fetch_promises = [];
        fetch_promises = fetch_promises.concat(self.getCancerStudyIds().map(
          function(studyId) {
            var _def = new $.Deferred();
            if (!self.data.sampleLists.hasOwnProperty('lists')) {
              self.data.sampleLists.lists = {};
            }
            if (self.data.sampleLists.lists.hasOwnProperty(studyId)) {
              _def.resolve(self.data.sampleLists.lists[studyId]);
            } else {
              $.ajax({
                url: window.cbioURL + 'api/studies/' + studyId + '/sample-lists',
                contentType: "application/json",
                type: 'GET'
              }).done(function(data) {
                self.data.sampleLists.lists[studyId] = _.pluck(data, 'sampleListId');
                _def.resolve(data);
              }).fail(function() {
                _def.reject();
              });
              return _def.promise();
            }
          }));
        $.when.apply($, fetch_promises).then(function() {
          def.resolve();
        });
        return def.promise();
      },
      getCnaFractionData: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _ajaxCnaFractionData = {};
          var cancer_study_ids = self.getCancerStudyIds();
          var _studyCasesMap = self.getStudyCasesMap();
          var fetch_promises = [];
          fetch_promises = fetch_promises.concat(cancer_study_ids.map(
            function(_studyId) {
              var _def = new $.Deferred();
              var _data = {cmd: 'get_cna_fraction', cancer_study_id: _studyId};
              if (_studyCasesMap[_studyId].samples !== undefined) {
                _data.case_ids = _studyCasesMap[_studyId].samples.join(' ');
              }
              $.ajax({
                method: 'POST',
                url: self.portalUrl + 'cna.json?',
                data: _data,
                success: function(response) {
                  if (Object.keys(response).length > 0) {
                    _ajaxCnaFractionData[_studyId] = response;
                  }
                  _def.resolve();
                },
                error: function() {
                  fetch_promise.reject();
                }
              });
              return _def.promise();
            }));
          $.when.apply($, fetch_promises).then(function() {
            fetch_promise.resolve(_ajaxCnaFractionData);
          });
        }),
      getCnaData: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _ajaxCnaData = {};
          var fetch_promises = [];
          var _cnaProfiles = self.cnaProfileIdsMap;
          var _studyCasesMap = self.getStudyCasesMap();

          fetch_promises = fetch_promises.concat(_.map(_cnaProfiles,
            function(_profileId, _studyId) {
              var _def = new $.Deferred();
              var _samples = _studyCasesMap[_studyId].samples;
              var _data = {
                cbio_genes_filter: true,
                cna_profile: _profileId
              };
              if (_samples !== undefined) {
                _data.sample_id = _samples.join(' ');
              }
              $.ajax({
                method: 'POST',
                url: self.portalUrl + 'cna.json?',
                data: _data,
                success: function(response) {
                  _ajaxCnaData[_studyId] = response;
                  _def.resolve();
                },
                error: function() {
                  fetch_promise.reject();
                }
              });
              return _def.promise();
            }));
          $.when.apply($, fetch_promises).then(function() {
            fetch_promise.resolve(_ajaxCnaData);
          });
        }),
      getMutationCount: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var fetch_promises = [];
          var _ajaxMutationCountData = {};
          $.when(self.getGeneticProfiles()).then(function(_profiles) {
            var _mutationProfiles = _.filter(_profiles, function(_profile) {
              return _profile.study_id + '_mutations' === _profile.id;
            });
            var _studyCasesMap = self.getStudyCasesMap();
            fetch_promises = fetch_promises.concat(_mutationProfiles.map(
              function(_mutationProfile) {
                var _def = new $.Deferred();
                var _samples = _studyCasesMap[_mutationProfile.study_id].samples;
                var _data = {
                  cmd: 'count_mutations',
                  mutation_profile: _mutationProfile.id
                };
                if (_samples !== undefined) {
                  _data.case_ids = _samples.join(' ');
                }
                $.ajax({
                  method: 'POST',
                  url: self.portalUrl + 'mutations.json?',
                  data: _data,
                  success: function(response) {
                    if (Object.keys(response).length > 0) {
                      _ajaxMutationCountData[_mutationProfile.study_id] = response;
                    }
                    _def.resolve();
                  },
                  error: function() {
                    fetch_promise.reject();
                  }
                });
                return _def.promise();
              }));
            $.when.apply($, fetch_promises).then(function() {
              fetch_promise.resolve(_ajaxMutationCountData);
            });
          });
        }),
      getMutData: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var fetch_promises = [];
          var _mutDataStudyIdArr = [];
          var _mutationProfiles = self.mutationProfileIdsMap;
          var _studyCasesMap = self.getStudyCasesMap();
          fetch_promises = fetch_promises.concat(_.map(_mutationProfiles,
            function(_mutationProfileId, _studyId) {
              var _def = new $.Deferred();
              var _samples = _studyCasesMap[_studyId].samples;
              var _data = {
                cmd: 'get_smg',
                mutation_profile: _mutationProfileId
              };
              if (_samples !== undefined) {
                _data.case_list = _samples.join(' ');
              }
              $.ajax({
                method: 'POST',
                url: self.portalUrl + 'mutations.json?',
                data: _data,
                success: function(response) {
                  _.each(response, function(element) {
                    _.extend(element, {study_id: _studyId});
                  });
                  _mutDataStudyIdArr = _mutDataStudyIdArr.concat(response);
                  _def.resolve();
                },
                error: function() {
                  fetch_promise.reject();
                }
              });
              return _def.promise();
            }));
          $.when.apply($, fetch_promises).then(function() {
            fetch_promise.resolve(_mutDataStudyIdArr);
          }, function() {
            fetch_promise.reject();
          });
        }),
      getSampleClinicalData: function(attribute_ids) {
        return getSampleClinicalData(this, attribute_ids);
      },
      getPatientClinicalData: function(attribute_ids) {
        return getPatientClinicalData(this, attribute_ids);
      },
      getClinicalData: function(attribute_ids, isPatientAttributes) {
        return isPatientAttributes ? this.getPatientClinicalData(attribute_ids) :
          this.getSampleClinicalData(attribute_ids);
      },
      getAllGenePanelSampleIds: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _map = {};
          var asyncAjaxCalls = [];
          var responses = [];
          _.each(self.getCancerStudyIds(), function(_studyId) {
            asyncAjaxCalls.push(
              $.ajax({
                url: window.cbioURL + 'api-legacy/genepanel/data',
                contentType: 'application/json',
                data: ['profile_id=' + _studyId + '_mutations', 'genes='].join('&'),
                type: 'GET',
                success: function(_res) {
                  responses.push(_res);
                }
              })
            );
          });
          $.when.apply($, asyncAjaxCalls).done(function() {
            var _panelMetaArr = _.flatten(responses);
            _.each(_panelMetaArr, function(_panelMeta) {
              _map[_panelMeta.stableId] = {};
              var _sorted = (_panelMeta.samples).sort();
              _map[_panelMeta.stableId].samples = _sorted;
              _map[_panelMeta.stableId].sel_samples = _sorted;
            });
            fetch_promise.resolve(_map);
          }).fail(function() {
            fetch_promise.reject();
          });
        }
      ),
      getGenePanelMap: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          self.getAllGenePanelSampleIds().then(function(_panelSampleMap) {
            self.panelSampleMap = _panelSampleMap;
            var asyncAjaxCalls = [];
            var responses = [];
            _.each(Object.keys(_panelSampleMap), function(_panelId) {
              asyncAjaxCalls.push(
                $.ajax({
                  url: window.cbioURL + 'api-legacy/genepanel',
                  contentType: 'application/json',
                  data: {panel_id: _panelId},
                  type: 'GET',
                  success: function(_res) {
                    responses.push(_res);
                  }
                })
              );
            });
            $.when.apply($, asyncAjaxCalls).done(function() {
              var _panelMetaArr = _.map(responses, function(responseArr) {
                return responseArr[0];
              });
              var _map = {};
              _.each(_panelMetaArr, function(_panelMeta) {
                _.each(_panelMeta.genes, function(_gene) {
                  if (!_map.hasOwnProperty(_gene.hugoGeneSymbol)) {
                    _map[_gene.hugoGeneSymbol] = {};
                    _map[_gene.hugoGeneSymbol].panel_id = [];
                    _map[_gene.hugoGeneSymbol].sample_num = 0;
                  }
                  _map[_gene.hugoGeneSymbol].panel_id.push(_panelMeta.stableId);
                  _map[_gene.hugoGeneSymbol].sample_num += _panelSampleMap[_panelMeta.stableId].samples.length;
                });
              });
              fetch_promise.resolve(_map);
            }).fail(function() {
              fetch_promise.reject();
            });
          }, function() {
            fetch_promise.reject();
          });
        }
      ),
      updateGenePanelMap: function(_map, _selectedSampleIds) {
        var _self = this;
        if (typeof _selectedSampleIds === 'undefined') {
          return _map;
        }
        // update panel sample count map
        _selectedSampleIds = _selectedSampleIds.sort();
        _.each(Object.keys(_self.panelSampleMap), function(_panelId) {
          _self.panelSampleMap[_panelId].sel_samples =
            content.util.intersection(_self.panelSampleMap[_panelId].samples, _selectedSampleIds);
        });
        _.each(Object.keys(_map), function(_gene) {
          var _sampleNumPerGene = 0;
          _.each(_map[_gene].panel_id, function(_panelId) {
            _sampleNumPerGene += _self.panelSampleMap[_panelId].sel_samples.length;
          });
          _map[_gene].sample_num = _sampleNumPerGene;
        });
        return _map;
      }
    };
  };

  return content;
})(window.$, window._);

window.cbioportal_client = (function() {
  var raw_service = (function() {
    var getApiCallPromise = function(endpt, args, type) {
      var arg_strings = [];
      for (var k in args) {
        if (args.hasOwnProperty(k)) {
          arg_strings.push(k + '=' + [].concat(args[k]).join(","));
        }
      }
      var arg_string = arg_strings.join("&") || "?";
      return $.ajax({
        type: type || "POST",
        url: window.cbioURL + endpt,
        data: arg_string,
        dataType: "json"
      });
    };
    var functionNameToEndpointProperties = {
      'CancerTypes':{ endpoint: 'api-legacy/cancertypes' },
      'SampleClinicalData': { endpoint: 'api-legacy/clinicaldata/samples' },
      'PatientClinicalData': { endpoint: 'api-legacy/clinicaldata/patients' },
      'SampleClinicalAttributes': { endpoint: 'api-legacy/clinicalattributes/samples' },
      'PatientClinicalAttributes': { endpoint: 'api-legacy/clinicalattributes/patients' },
      'ClinicalAttributes': {endpoint: 'api-legacy/clinicalattributes'},
      'Genes': { endpoint: 'api-legacy/genes' },
      'GeneticProfiles': { endpoint: 'api-legacy/geneticprofiles' },
      'SampleLists': { endpoint: 'api-legacy/samplelists' },
      'SampleListsMeta': { endpoint: 'api-legacy/samplelists', args: {metadata: true } },
      'Patients': { endpoint: 'api-legacy/patients' },
      'GeneticProfileData': { endpoint: 'api-legacy/geneticprofiledata' },
      'Samples': { endpoint: 'api-legacy/samples' },
      'Studies': { endpoint: 'api-legacy/studies' },
      'MutationCounts': { endpoint: 'api-legacy/mutation_count'},
      'GenePanels': {endpoint: 'api-legacy/genepanel', type: 'GET'}
    };
    var ret = {};
    for (var fn_name in functionNameToEndpointProperties) {
      if (functionNameToEndpointProperties.hasOwnProperty(fn_name)) {
        ret['get'+fn_name] = (function(props) {
          return function(args) {
            return getApiCallPromise(props.endpoint, $.extend(true, {}, args, props.args), props.type);
          };
        })(functionNameToEndpointProperties[fn_name]);
      }
    }
    return ret;
  })();
  function Index(key) {
    var map = {};
    var stringSetDifference = function (A, B) {
      // In A and not in B
      var in_A_not_in_B = {};
      var i, _len;
      for (i = 0, _len = A.length; i < _len; i++) {
        in_A_not_in_B[A[i]] = true;
      }
      for (i = 0, _len = B.length; i < _len; i++) {
        in_A_not_in_B[B[i]] = false;
      }
      var ret = [];
      for (i = 0, _len = A.length; i < _len; i++) {
        if (in_A_not_in_B[A[i]]) {
          ret.push(A[i]);
        }
      }
      return ret;
    };
    this.clear = function(data, args) {
      var i;
      var _len = data.length;
      // Clear existing data for touched keys
      for (i = 0; i < _len; i++) {
        var datum_key = key(data[i], args);
        map[datum_key] = [];
      }
    };
    this.addData = function (data, args, append) {
      var i;
      var _len = data.length;
      if (!append) {
        // Clear existing data for touched keys
        for (i = 0; i < _len; i++) {
          var datum_key = key(data[i], args);
          map[datum_key] = [];
        }
      }
      // Add data
      for (i = 0; i < _len; i++) {
        var d = data[i];
        map[key(d, args)].push(d);
      }
    };
    this.getData = function (keys, datumFilter) {
      keys = keys || Object.keys(map);
      keys = [].concat(keys);
      var i, datum;
      var ret = [], _len = keys.length;
      for (i = 0; i < _len; i++) {
        datum = map[keys[i]];
        if (typeof datum !== 'undefined' && (!datumFilter || datumFilter(datum))) {
          ret = ret.concat(datum);
        }
      }
      return ret;
    };

    this.missingKeys = function (keys) {
      return stringSetDifference([].concat(keys), Object.keys(map));
    };
  };
  var keyCombinations = function(key_list_list, list_of_lists) {
    var ret = [[]];
    for (var i=0; i<key_list_list.length; i++) {
      var intermediate_ret = [];
      var key_list = key_list_list[i];
      for (var j=0; j<key_list.length; j++) {
        for (var k=0; k<ret.length; k++) {
          intermediate_ret.push(ret[k].concat([(list_of_lists ? [].concat(key_list[j]) : key_list[j])]));
        }
      }
      ret = intermediate_ret;
    }
    return ret;
  };
  function HierIndex(key) {
    var fully_loaded = {};
    var map = {};
    this.markFullyLoaded = function(key) {
      var curr_obj = fully_loaded;
      for (var i=0; i<key.length; i++) {
        if (curr_obj === true) {
          break;
        }
        var sub_key = key[i];
        if (i < key.length - 1) {
          curr_obj[sub_key] = curr_obj[sub_key] || {};
          curr_obj = curr_obj[sub_key];
        } else {
          curr_obj[sub_key] = true;
        }
      }
    };
    this.isFullyLoaded = function(key) {
      var curr_obj = fully_loaded;
      for (var i=0; i<key.length; i++) {
        if (curr_obj === true || typeof curr_obj === "undefined") {
          break;
        }
        var sub_key = key[i];
        curr_obj = curr_obj[sub_key];
      }
      return (curr_obj === true);
    };
    this.addData = function(data, args) {
      var i, _len=data.length, j;
      // Clear existing data for touched keys, and initialize map locations
      for (i = 0; i < _len; i++) {
        var datum_key = key(data[i], args);
        var map_entry = map;
        for (j = 0; j < datum_key.length; j++) {
          var sub_key = datum_key[j];
          if (j < datum_key.length - 1) {
            map_entry[sub_key] = map_entry[sub_key] || {};
          } else {
            map_entry[sub_key] = [];
          }
          map_entry = map_entry[sub_key];
        }
      }
      // Add data
      for (i = 0; i < _len; i++) {
        var datum_key = key(data[i], args);
        var map_entry = map;
        for (j = 0; j < datum_key.length; j++) {
          map_entry = map_entry[datum_key[j]];
        }
        map_entry.push(data[i]);
      }
    };

    var flatten = function(list_of_lists){
      //console.log("flatten: " + ret.length);
      var chunk_size = 60000;
      var n_chunks = Math.ceil(list_of_lists.length/chunk_size);
      var flattened = [];
      //first round of flattening, in chunks of chunkSize to avoid stack size problems in concat.apply:
      for (var k=0; k<n_chunks; k++) {
        flattened.push([].concat.apply([], list_of_lists.slice(k*chunk_size, (k+1)*chunk_size)));
      }
      //final round, flattening the lists of lists to a single list:
      return [].concat.apply([], flattened);
    };

    this.getData = function (key_list_list) {
      var intermediate = [map];
      var ret = [];
      var i, j, k;
      key_list_list = key_list_list || [];
      var key_list_index = 0;
      while (intermediate.length > 0) {
        var tmp_intermediate = [];
        for (i = 0; i<intermediate.length; i++) {
          var obj = intermediate[i];
          if (Object.prototype.toString.call(obj) === '[object Array]') {
            ret.push(obj);
          } else {
            var keys = (key_list_index < key_list_list.length && key_list_list[key_list_index]) || Object.keys(obj);
            for (k = 0; k<keys.length; k++) {
              if (obj.hasOwnProperty(keys[k])) {
                tmp_intermediate.push(obj[keys[k]]);
              }
            }
          }
        }
        intermediate = tmp_intermediate;
        key_list_index += 1;
      }
      //flatten, if data is found (when not found, it means it is a missing key)
      if (ret.length > 0) {
        ret = flatten(ret);
      }
      return ret;
    };
    this.missingKeys = function(key_list_list) {
      // TODO: implement this without slow reference to getData
      var missing_keys = key_list_list.map(function() { return {}; });
      var j, k;
      var key_combinations = keyCombinations(key_list_list, true);
      for (k = 0; k<key_combinations.length; k++) {
        if (this.getData(key_combinations[k]).length === 0) {
          for (j=0; j<key_combinations[k].length; j++) {
            missing_keys[j][key_combinations[k][j]] = true;
          }
        }
      }
      return missing_keys.map(function(o) { return Object.keys(o);});
    };
  };

  var makeOneIndexService = function(arg_name, indexKeyFn, service_fn_name) {
    return (function() {
      var index = new Index(indexKeyFn);
      var loaded_all = false;
      return function(args) {
        args = args || {};
        var def = new $.Deferred();
        try {
          if (args.hasOwnProperty(arg_name)) {
            var missing_keys = index.missingKeys(args[arg_name]);
            if (missing_keys.length > 0) {
              var webservice_args = {};
              webservice_args[arg_name] = missing_keys;
              raw_service[service_fn_name](webservice_args).then(function(data) {
                index.addData(data);
                def.resolve(index.getData(args[arg_name]));
              }).fail(function() {
                def.reject();
              });
            } else {
              def.resolve(index.getData(args[arg_name]));
            }
          } else {
            if (!loaded_all) {
              raw_service[service_fn_name]({}).then(function(data) {
                index.addData(data);
                loaded_all = true;
                def.resolve(index.getData());
              }).fail(function() {
                def.reject();
              });
            } else {
              def.resolve(index.getData());
            }
          }
        } catch (err) {
          def.reject();
        }
        return def.promise();
      }
    })();
  };
  var makeTwoIndexService = function(arg_name1, indexKeyFn1, index1_always_add, arg_name2, indexKeyFn2, index2_always_add, service_fn_name) {
    return (function() {
      var index1 = new Index(indexKeyFn1);
      var index2 = new Index(indexKeyFn2);
      var loaded_all = false;
      return function(args) {
        args = args || {};
        var def = new $.Deferred();
        try {
          if (args.hasOwnProperty(arg_name1)) {
            var missing_keys = index1.missingKeys(args[arg_name1]);
            if (missing_keys.length > 0) {
              var webservice_args = {};
              webservice_args[arg_name1] = missing_keys;
              raw_service[service_fn_name](webservice_args).then(function(data) {
                index1.addData(data);
                if (index2_always_add) {
                  index2.addData(data);
                }
                def.resolve(index1.getData(args[arg_name1]));
              }).fail(function() {
                def.reject();
              });
            } else {
              def.resolve(index1.getData(args[arg_name1]));
            }
          } else if (args.hasOwnProperty(arg_name2)) {
            var missing_keys = index2.missingKeys(args[arg_name2]);
            if (missing_keys.length > 0) {
              var webservice_args = {};
              webservice_args[arg_name2] = missing_keys;
              raw_service[service_fn_name](webservice_args).then(function(data) {
                index2.addData(data);
                if (index1_always_add) {
                  index1.addData(data);
                }
                def.resolve(index2.getData(args[arg_name2]));
              }).fail(function() {
                def.reject();
              });
            } else {
              def.resolve(index2.getData(args[arg_name2]));
            }
          } else {
            if (!loaded_all) {
              raw_service[service_fn_name]({}).then(function(data) {
                index1.addData(data);
                index2.addData(data);
                loaded_all = true;
                def.resolve(index1.getData());
              }).fail(function() {
                def.reject();
              });
            } else {
              def.resolve(index1.getData());
            }
          }
        } catch (err) {
          def.reject();
        }
        return def.promise();
      }
    })();
  };

  var makeHierIndexService = function(arg_names, indexing_attrs, service_fn_name) {
    return (function() {
      var index = new HierIndex(function(d) {
        var ret = [];
        for (var i=0; i<indexing_attrs.length; i++) {
          ret.push(d[indexing_attrs[i]]);
        }
        return ret;
      });
      return function(args) {
        var def = new $.Deferred();
        try {
          var arg_list_list = arg_names.map(function (a) {
            return args[a];
          });
          while (typeof arg_list_list[arg_list_list.length - 1] === "undefined") {
            arg_list_list.pop();
          }
          if (arg_list_list.length < arg_names.length) {
            var missing_arg_set_list = arg_list_list.map(function (a) {
              return {};
            });
            var key_combs = keyCombinations(arg_list_list);
            var missing_key_combs = [];
            for (var i = 0; i < key_combs.length; i++) {
              if (!index.isFullyLoaded(key_combs[i])) {
                missing_key_combs.push(key_combs[i]);
                for (var j = 0; j < key_combs[i].length; j++) {
                  missing_arg_set_list[j][key_combs[i][j]] = true;
                }
              }
            }
            missing_arg_set_list = missing_arg_set_list.map(function (o) {
              return Object.keys(o);
            });
            if (missing_arg_set_list[0].length > 0) {
              var webservice_args = {};
              for (var i = 0; i < missing_arg_set_list.length; i++) {
                webservice_args[arg_names[i]] = missing_arg_set_list[i];
              }
              raw_service[service_fn_name](webservice_args).then(function (data) {
                for (var j = 0; j < missing_key_combs.length; j++) {
                  index.markFullyLoaded(missing_key_combs[j]);
                }
                index.addData(data);
                def.resolve(index.getData(arg_list_list));
              }).fail(function() {
                def.reject();
              });
            } else {
              def.resolve(index.getData(arg_list_list));
            }
          } else {
            var missing_keys = index.missingKeys(arg_list_list);
            if (missing_keys[0].length > 0) {
              var webservice_args = {};
              for (var i = 0; i < missing_keys.length; i++) {
                webservice_args[arg_names[i]] = missing_keys[i];
              }
              raw_service[service_fn_name](webservice_args).then(function (data) {
                index.addData(data);
                def.resolve(index.getData(arg_list_list));
              }).fail(function() {
                def.reject();
              });
            } else {
              def.resolve(index.getData(arg_list_list));
            }
          }
        } catch (err) {
          def.reject();
        }
        return def.promise();
      };
    })();
  };
  // TODO: abstract this correctly so there isn't more than one index, more than one index service maker?
  var enforceRequiredArguments = function(fnPtr, list_of_arg_combinations) {
    return function(args) {
      args = args || {};
      var matches_one = false;
      for (var i = 0; i < list_of_arg_combinations.length; i++) {
        var combination = list_of_arg_combinations[i];
        var matches_combo = true;
        for (var j = 0; j < combination.length; j++) {
          matches_combo = matches_combo && args.hasOwnProperty(combination[j]);
        }
        if (matches_combo) {
          matches_one = true;
          break;
        }
      }
      if (!matches_one) {
        var def = new $.Deferred();
        var msg = "Given arguments not acceptable; need a combination in the following list: "
        msg += list_of_arg_combinations.map(function(arg_combo) { return arg_combo.join(","); }).join(";");
        def.reject({msg: msg});
        return def.promise();
      } else {
        return fnPtr(args);
      }
    };
  }

  var cached_service = {
    getCancerTypes: enforceRequiredArguments(makeOneIndexService('cancer_type_ids', function(d) { return d.id;}, 'getCancerTypes'), [[], ["cancer_type_ids"]]),
    getGenes: enforceRequiredArguments(makeOneIndexService('hugo_gene_symbols', function(d) { return d.hugo_gene_symbol;}, 'getGenes'), [[],["hugo_gene_symbols"]]),
    getStudies: enforceRequiredArguments(makeOneIndexService('study_ids', function(d) { return d.id;}, 'getStudies'), [[], ["study_ids"]]),
    getGenePanelsByPanelId: enforceRequiredArguments(makeOneIndexService('panel_id', function(d) { return d.stableId;}, 'getGenePanels'), [["panel_id"]]),
    getGeneticProfiles: enforceRequiredArguments(makeTwoIndexService('study_id', function(d) { return d.study_id;}, false, 'genetic_profile_ids', function(d) {return d.id; }, true, 'getGeneticProfiles'), [["study_id"],["genetic_profile_ids"]]),
    getSampleLists: enforceRequiredArguments(makeTwoIndexService('study_id', function(d) { return d.study_id;}, false, 'sample_list_ids', function(d) {return d.id; }, true, 'getSampleLists'), [["study_id"], ["sample_list_ids"]]),
    getSampleClinicalData: enforceRequiredArguments(makeHierIndexService(['study_id', 'attribute_ids', 'sample_ids'], ['study_id', 'attr_id', 'sample_id'], 'getSampleClinicalData'), [["study_id","attribute_ids"], ["study_id","attribute_ids","sample_ids"]]),
    getPatientClinicalData: enforceRequiredArguments(makeHierIndexService(['study_id', 'attribute_ids', 'patient_ids'], ['study_id', 'attr_id', 'patient_id'], 'getPatientClinicalData'), [["study_id","attribute_ids"], ["study_id","attribute_ids","patient_ids"]]),
    getPatients: enforceRequiredArguments(makeHierIndexService(['study_id', 'patient_ids'], ['study_id', 'id'], 'getPatients'), [["study_id"], ["study_id","patient_ids"]]),
    getSamples: enforceRequiredArguments(makeHierIndexService(['study_id', 'sample_ids'], ['study_id', 'id'], 'getSamples'), [["study_id"], ["study_id", "sample_ids"]]),
    getSamplesByPatient: enforceRequiredArguments(makeHierIndexService(['study_id', 'patient_ids'], ['study_id', 'patient_id'], 'getSamples'), [["study_id"], ["study_id", "patient_ids"]]),
    getGeneticProfileDataBySample: enforceRequiredArguments(makeHierIndexService(['genetic_profile_ids', 'genes', 'sample_ids'], ['genetic_profile_id', 'hugo_gene_symbol', 'sample_id'], 'getGeneticProfileData'), [["genetic_profile_ids","genes"], ["genetic_profile_ids","genes","sample_ids"]]),
    getGeneticProfileDataBySampleList: enforceRequiredArguments(makeHierIndexService(['genetic_profile_ids', 'genes', 'sample_list_id'], ['genetic_profile_id', 'hugo_gene_symbol', 'sample_list_id'], 'getGeneticProfileData'), [["genetic_profile_ids","genes"], ["genetic_profile_ids","genes","sample_list_id"]]),
    getSampleClinicalAttributes: enforceRequiredArguments(function(args) {
      return raw_service.getSampleClinicalAttributes(args);
    }, [["study_id"], ["study_id","sample_ids"]]),
    getPatientClinicalAttributes: enforceRequiredArguments(function(args) {
      return raw_service.getPatientClinicalAttributes(args);
    }, [["study_id"], ["study_id", "patient_ids"]]),
    getClinicalAttributesByStudy: enforceRequiredArguments(function(args) {
      return raw_service.getClinicalAttributes(args);
    }, [["study_id"]]),
    getClinicalAttributes: enforceRequiredArguments(makeOneIndexService('attr_ids', function(d) { return d.attr_id; }, 'getClinicalAttributes'), [[], ["attr_ids"], ["study_id"]]),
    getMutationCounts: raw_service.getMutationCounts,
  };
  return cached_service;
})();
