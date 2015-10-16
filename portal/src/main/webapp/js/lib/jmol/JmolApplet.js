// JmolApplet.js -- Jmol._Applet and Jmol._Image

// BH 7/16/2012 1:50:03 PM adds server-side scripting for image
// BH 8/11/2012 11:00:01 AM adds Jmol._readyCallback for MSIE not in Quirks mode
// BH 8/12/2012 3:56:40 AM allows .min.png to be replaced by .all.png in Image file name
// BH 8/13/2012 6:16:55 PM fix for no-java message not displaying

(function (Jmol, document) {


	// _Applet -- the main, full-featured, object
	
	Jmol._Applet = function(id, Info, caption, checkOnly){
		this._jmolType = "Jmol._Applet" + (Info.isSigned ? " (signed)" : "");
		if (checkOnly)
			return this;
		this._isSigned = Info.isSigned;
		this._dataMultiplier=1;
		this._readyFunction = Info.readyFunction;
		this._ready = false; 
		this._applet = null;
		this._memoryLimit = Info.memoryLimit || 512;
		this._canScript = function(script) {return true;};
		this._savedOrientations = [];
		this._syncKeyword = "Select:";
		
		/*
		 * privileged methods
		 */
		this._initialize = function(jarPath, jarFile) {
			var doReport = false;
			if(this._jarFile) {
				var f = this._jarFile;
				if(f.indexOf("/") >= 0) {
					alert("This web page URL is requesting that the applet used be " + f + ". This is a possible security risk, particularly if the applet is signed, because signed applets can read and write files on your local machine or network.");
					var ok = prompt("Do you want to use applet " + f + "? ", "yes or no")
					if(ok == "yes") {
						jarPath = f.substring(0, f.lastIndexOf("/"));
						jarFile = f.substring(f.lastIndexOf("/") + 1);
					} else {
						doReport = true;
					}
				} else {
					jarFile = f;
				}
			}
 			this._jarPath = jarPath || ".";
			this._jarFile = (typeof(jarFile) == "string" ? jarFile : (jarFile ?  "JmolAppletSigned" : "JmolApplet") + "0.jar");
	    if (doReport)
				alert("The web page URL was ignored. Continuing using " + this._jarFile + ' in directory "' + this._jarPath + '"');
			Jmol.controls == undefined || Jmol.controls._onloadResetForms();		
		}		
		this._create(id, Info, caption);
		return this;
	}

  /*  AngelH, mar2007:
    By (re)setting these variables in the webpage before calling Jmol.getApplet(),
    a custom message can be provided (e.g. localized for user's language) when no Java is installed.
  */
	Jmol._Applet._noJavaMsg =
      "You do not have Java applets enabled in your web browser, or your browser is blocking this applet.<br />\
      Check the warning message from your browser and/or enable Java applets in<br />\
      your web browser preferences, or install the Java Runtime Environment from <a href='http://www.java.com'>www.java.com</a>";
	Jmol._Applet._noJavaMsg2 =
      "You do not have the<br />\
      Java Runtime Environment<br />\
      installed for applet support.<br />\
      Visit <a href='http://www.java.com'>www.java.com</a>";

	Jmol._Applet._setCommonMethods = function(proto) {
		proto._showInfo = Jmol._Applet.prototype._showInfo;	
		proto._search = Jmol._Applet.prototype._search;
		proto._readyCallback = Jmol._Applet.prototype._readyCallback;
	}

	Jmol._Applet._createApplet = function(applet, Info, params, myClass, script, caption) {

		if (Jmol._syncedApplets.length)
		  params.synccallback = "Jmol._mySyncCallback";
		params.java_arguments = "-Xmx" + Math.round(Info.memoryLimit || applet._memoryLimit) + "m";

		applet._initialize(Info.jarPath, Info.jarFile);

		// size is set to 100% of containers' size, but only if resizable. 
		// Note that resizability in MSIE requires: 
		// <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
		var w = (applet._containerWidth.indexOf("px") >= 0 ? applet._containerWidth : "100%");
		var h = (applet._containerHeight.indexOf("px") >= 0 ? applet._containerHeight : "100%");
		var widthAndHeight = " style=\"width:" + w + ";height:" + h + "\" ";
		var tHeader, tFooter;
		if (Jmol.featureDetection.useIEObject || Jmol.featureDetection.useHtml4Object) {
			params.archive = applet._jarFile;
			if (script)
  			params.script = script;
			params.mayscript = 'true';
			params.codebase = applet._jarPath;
			params.code = myClass + ".class";
			tHeader =
				"<object name='" + applet._id +
				"_object' id='" + applet._id + "_object' " + "\n" +
				widthAndHeight + "\n";
			tFooter = "</object>";
		}
		if (Jmol.featureDetection.useIEObject) { // use MSFT IE6 object tag with .cab file reference
			var _windowsClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
			var _windowsCabUrl = "http://java.sun.com/update/1.6.0/jinstall-6u22-windows-i586.cab";
			tHeader += " classid='" + _windowsClassId + "'\n" + " codebase='" + _windowsCabUrl + "'\n>\n";
		} else if (Jmol.featureDetection.useHtml4Object) { // use HTML4 object tag
			tHeader += " type='application/x-java-applet'\n>\n";
		} else { // use applet tag
			if (script)
  			params.script = script;
			tHeader =
				"<applet name='" + applet._id +
				"_object' id='" + applet._id + "_object' \n" +
				widthAndHeight + "\n" +
				" code='" + myClass + "'" +
				" archive='" + applet._jarFile + "' codebase='" + applet._jarPath + "'\n" +
				" mayscript='true'>\n";
			tFooter = "</applet>";
		}
		var visitJava;
		if (Jmol.featureDetection.useIEObject || Jmol.featureDetection.useHtml4Object) {
			var szX = "width:" + applet._width;
			if ( szX.indexOf("%")==-1 ) 
				szX+="px";
			var szY = "height:" + applet._height;
			if ( szY.indexOf("%")==-1 )
				szY+="px";
			visitJava = "<p style='background-color:yellow; color:black; " + szX + ";" + szY + ";" +
					// why doesn't this vertical-align work?
				"text-align:center;vertical-align:middle;'>\n" +
				Jmol._Applet._noJavaMsg + "</p>";
		} else {
			visitJava = "<table bgcolor='yellow'><tr>" +
				"<td align='center' valign='middle' " + widthAndHeight + "><font color='black'>\n" +
				Jmol._Applet._noJavaMsg2 + "</font></td></tr></table>";
		}
	
		var t = Jmol._getWrapper(applet, true) + tHeader;
 		for (var i in params)
			if(params[i])
		 		t+="  <param name='"+i+"' value='"+params[i]+"' />\n";
		t += visitJava + tFooter 
			+ Jmol._getWrapper(applet, false) 
			+ (Info.addSelectionOptions ? Jmol._getGrabberOptions(applet, caption) : "");
		if (Jmol._debugAlert)
			alert(t);
		applet._code = Jmol._documentWrite(t);
	}

  Jmol._Applet._search = function(applet, query, script) {
  		applet._showInfo(false);
		arguments.length > 1 || (query = null);
		Jmol._setQueryTerm(applet, query);
		query || (query = Jmol._getElement(applet, "query").value);
		query && (query = query.replace(/\"/g, ""));
		var database;
		if (Jmol._isDatabaseCall(query)) {
			database = query.substring(0, 1);
			query = query.substring(1);
		} else {
			database = (applet._hasOptions ? Jmol._getElement(applet, "select").value : "$");
		}
		if (database == "=" && query.length == 3)
			query = "=" + query; // this is a ligand			
		var dm = database + query;
		if (!query || dm.indexOf("?") < 0 && dm == applet._thisJmolModel) {
			return;
		}
		applet._thisJmolModel = dm;
		if (database == "$" || database == ":")
			applet._jmolFileType = "MOL";
		else if (database == "=")
			applet._jmolFileType = "PDB";
		applet._searchDatabase(query, database, script);
	}
	
	Jmol._Applet.prototype._create = function(id, Info, caption){
		Jmol._setObject(this, id, Info);
		var params = {
			syncId: ("" + Math.random()).substring(3),
			progressbar: "true",
			progresscolor: "blue",
			boxbgcolor: Info.color || "black",
			boxfgcolor: "white",
			boxmessage: "Downloading JmolApplet ...",
			script: (!Info.color ? "" : "background " + (Info.color.indexOf("#") == 0 ? "[0x" + Info.color.substring(1) + "]" : Info.color))
		};

		var availableValues = "'progressbar','progresscolor','boxbgcolor','boxfgcolor','boxmessage',\
									'messagecallback','pickcallback','animframecallback','appletreadycallback','atommovedcallback',\
									'echocallback','evalcallback','hovercallback','language','loadstructcallback','measurecallback',\
									'minimizationcallback','resizecallback','scriptcallback','statusform','statustext','statustextarea',\
									'synccallback','usecommandthread'";
		for (var i in Info)
			if(availableValues.indexOf("'" + i.toLowerCase() + "'") >= 0)
				params[i] = Info[i];
				
		function sterilizeInline(model) {
			model = model.replace(/\r|\n|\r\n/g, (model.indexOf("|") >= 0 ? "\\/n" : "|")).replace(/'/g, "&#39;");
			if(Jmol._debugAlert)
				alert("inline model:\n" + model);
			return model;
		}

		params.loadInline = (Info.inlineModel ? sterilizeInline(Info.inlineModel) : "");
		params.appletReadyCallback = "Jmol._readyCallback";//this._id + "._readyCallback";
		if (Jmol._syncedApplets.length)
		  params.synccallback = "Jmol._mySyncCallback";
		params.java_arguments = "-Xmx" + Math.round(Info.memoryLimit || this._memoryLimit) + "m";

		this._initialize(Info.jarPath, Info.jarFile);
		Jmol._Applet._createApplet(this, Info, params, "JmolApplet", null);
	}

	Jmol._Applet.prototype._readyCallback = function(id, fullid, isReady, applet) {
		if (!isReady)
			return; // ignore -- page is closing
		this._ready = true;
		var script = this._readyScript;
		this._applet = applet;
		if (this._defaultModel)
			this._search(this._defaultModel, (script ? ";" + script : ""));
		else if (script)
			this._script(script);
		else if (this._src)
			this._script('load "' + this._src + '"');
		this._readyFunction && this._readyFunction(this);
		Jmol._setReady(this);
	}
	
	Jmol._Applet.prototype._showInfo = function(tf) {
		Jmol._getElement(this, "infoheaderspan").innerHTML = this._infoHeader;
		if (this._info)
			Jmol._getElement(this, "infodiv").innerHTML = this._info;
		if ((!this._isInfoVisible) == (!tf))
			return;
		this._isInfoVisible = tf;
		// 1px does not work for MSIE
	
		var w = (tf ? "2px" : "100%");
		var h = (tf ? "2px" : "100%");
//		var w = (tf ? "2px" : this._containerWidth.indexOf("px") >= 0 ? this._containerWidth : "100%");
//		var h = (tf ? "2px" : this._containerHeight.indexOf("px") >= 0 ? this._containerHeight : "100%");
		Jmol._getElement(this, "appletdiv").style.width = w;
		Jmol._getElement(this, "appletdiv").style.height = h;
		if (this._infoObject) {
			this._infoObject._showInfo(tf);
		} else {
			Jmol._getElement(this, "infotablediv").style.display = (tf ? "block" : "none");
		}
		if (false && !tf)// -- occurring on Mac systems?)
			alert("returning to applet..." + w + " " + h);
		this._show(!tf);
	}

	Jmol._Applet.prototype._search = function(query, script){
		Jmol._Applet._search(this, query, script);
	}
	
	Jmol._Applet.prototype._loadModel = function(mol, params) {
		var script = 'load DATA "model"\n' + mol + '\nEND "model" ' + params;
		this._applet.script(script);
	}
	
	Jmol._Applet.prototype._show = function(tf) {
		var w = (!tf ? "2px" : "100%");
		var h = (!tf ? "2px" : "100%");
		//var w = (!tf ? "2px" : this._containerWidth.indexOf("px") >= 0 ? this._containerWidth : "100%");
		//var h = (!tf ? "2px" : this._containerHeight.indexOf("px") >= 0 ? this._containerHeight : "100%");
		Jmol._getElement(this, "object").style.width = w; 
		Jmol._getElement(this, "object").style.height = h; 
	}
	
	Jmol._Applet.prototype._script = function(script) {
		if (!this._ready) {
			this._readyScript || (this._readyScript = ";");
			this._readyScript += ";" + script;
			return; 
		}
		this._applet.script(script);
	}
	
	Jmol._Applet.prototype._syncScript = function(script) {
		this._applet.syncScript(script);
	}
	
	Jmol._Applet.prototype._scriptWait = function(script) {
		var Ret = this._scriptWaitAsArray(script);
		var s = "";
		for(var i = Ret.length; --i >= 0; )
			for(var j = 0, jj = Ret[i].length; j < jj; j++)
				s += Ret[i][j] + "\n";
		return s;
	}
	
	Jmol._Applet.prototype._scriptEcho = function(script) {
		// returns a newline-separated list of all echos from a script
		var Ret = this._scriptWaitAsArray(script);
		var s = "";
		for(var i = Ret.length; --i >= 0; )
			for(var j = Ret[i].length; --j >= 0; )
				if(Ret[i][j][1] == "scriptEcho")
					s += Ret[i][j][3] + "\n";
		return s.replace(/ \| /g, "\n");
	}
	
	Jmol._Applet.prototype._scriptMessage = function(script) {
		// returns a newline-separated list of all messages from a script, ending with "script completed\n"
		var Ret = this._scriptWaitAsArray(script);
		var s = "";
		for(var i = Ret.length; --i >= 0; )
			for(var j = Ret[i].length; --j >= 0; )
				if(Ret[i][j][1] == "scriptStatus")
					s += Ret[i][j][3] + "\n";
		return s.replace(/ \| /g, "\n");
	}
	
	Jmol._Applet.prototype._scriptWaitOutput = function(script) {
		var ret = "";
		try {
			if(script) {
				ret += this._applet.scriptWaitOutput(script);
			}
		} catch(e) {
		}
		return ret;
	}

	Jmol._Applet.prototype._scriptWaitAsArray = function(script) {
		var ret = "";
		try {
			this._getStatus("scriptEcho,scriptMessage,scriptStatus,scriptError");
			if(script) {
				ret += this._applet.scriptWait(script);
				ret = Jmol._evalJSON(ret, "jmolStatus");
				if( typeof ret == "object")
					return ret;
			}
		} catch(e) {
		}
		return [[ret]];
	}
	
	Jmol._Applet.prototype._getStatus = function(strStatus) {
		return Jmol._sortMessages(this._getPropertyAsArray("jmolStatus",strStatus));
	}
	
	Jmol._Applet.prototype._getPropertyAsArray = function(sKey,sValue) {
		return Jmol._evalJSON(this._getPropertyAsJSON(sKey,sValue),sKey);
	}

	Jmol._Applet.prototype._getPropertyAsString = function(sKey,sValue) {
		sValue == undefined && ( sValue = "");
		return this._applet.getPropertyAsString(sKey, sValue) + "";
	}

	Jmol._Applet.prototype._getPropertyAsJSON = function(sKey,sValue) {
		sValue == undefined && ( sValue = "");
		try {
			return (this._applet.getPropertyAsJSON(sKey, sValue) + "");
		} catch(e) {
			return "";
		}
	}

	Jmol._Applet.prototype._getPropertyAsJavaObject = function(sKey,sValue) {		
		sValue == undefined && ( sValue = "");
		return this._applet.getProperty(sKey,sValue);
	}

	
	Jmol._Applet.prototype._evaluate = function(molecularMath) {
		//carries out molecular math on a model
	
		var result = "" + this._getPropertyAsJavaObject("evaluate", molecularMath);
		var s = result.replace(/\-*\d+/, "");
		if(s == "" && !isNaN(parseInt(result)))
			return parseInt(result);
		var s = result.replace(/\-*\d*\.\d*/, "")
		if(s == "" && !isNaN(parseFloat(result)))
			return parseFloat(result);
		return result;
	}

	
	Jmol._Applet.prototype._saveOrientation = function(id) {	
		return this._savedOrientations[id] = this._getPropertyAsArray("orientationInfo","info").moveTo;
	}

	
	Jmol._Applet.prototype._restoreOrientation = function(id) {
		var s = this._savedOrientations[id];
		if(!s || s == "")
			return s = s.replace(/1\.0/, "0");
		return this._scriptWait(s);
	}

	
	Jmol._Applet.prototype._restoreOrientationDelayed = function(id,delay) {
		arguments.length < 1 && ( delay = 1);
		var s = this._savedOrientations[id];
		if(!s || s == "")
			return s = s.replace(/1\.0/, delay);
		return this._scriptWait(s);
	}

	Jmol._Applet.prototype._resizeApplet = function(size) {
		// See _jmolGetAppletSize() for the formats accepted as size [same used by jmolApplet()]
		//  Special case: an empty value for width or height is accepted, meaning no change in that dimension.
		
		/*
		 * private functions
		 */
		function _getAppletSize(size, units) {
			/* Accepts single number, 2-value array, or object with width and height as mroperties, each one can be one of:
			 percent (text string ending %), decimal 0 to 1 (percent/100), number, or text string (interpreted as nr.)
			 [width, height] array of strings is returned, with units added if specified.
			 Percent is relative to container div or element (which should have explicitly set size).
			 */
			var width, height;
			if(( typeof size) == "object" && size != null) {
				width = size[0]||size.width;
				height = size[1]||size.height;
			} else {
				width = height = size;
			}
			return [_fixDim(width, units), _fixDim(height, units)];
		}

		function _fixDim(x, units) {
			var sx = "" + x;
			return (sx.length == 0 ? (units ? "" : Jmol._allowedJmolSize[2]) 
				: sx.indexOf("%") == sx.length - 1 ? sx 
				: (x = parseFloat(x)) <= 1 && x > 0 ? x * 100 + "%" 
				: (isNaN(x = Math.floor(x)) ? Jmol._allowedJmolSize[2] 
				: x < Jmol._allowedJmolSize[0] ? Jmol._allowedJmolSize[0] 
				: x > Jmol._allowedJmolSize[1] ? Jmol._allowedJmolSize[1] 
				: x)
				+ (units ? units : "")
			);
		}
		
		var sz = _getAppletSize(size, "px");
		var d = Jmol._getElement(this, "appletinfotablediv");
		d.style.width = sz[0];
		d.style.height = sz[1];
		this._containerWidth = sz[0];
		this._containerHeight = sz[1];
	}
	
	Jmol._Applet.prototype._loadFile = function(fileName, params){
		this._showInfo(false);
		params || (params = "");
		this._thisJmolModel = "" + Math.random();
		this._script("zap;set echo middle center;echo Retrieving data...");
		if (this._isSigned) {
			this._script("load \"" + fileName + "\"" + params);
			return;
		}
		var self = this;
		Jmol._loadFileData(this, fileName, function(data){self._loadModel(data, params)});
	}
	
	Jmol._Applet.prototype._searchDatabase = function(query, database, script){
		this._showInfo(false);
		if (query.indexOf("?") >= 0) {
			Jmol._getInfoFromDatabase(this, database, query.split("?")[0]);
			return;
		}
		script || (script = Jmol._getScriptForDatabase(database));
		var dm = database + query;
		if (Jmol.db._DirectDatabaseCalls[database]) {
			this._loadFile(dm, script);
			return;
		}
		this._script("zap;set echo middle center;echo Retrieving data...");
		if (this._isSigned) {
			this._script("load \"" + dm + "\";" + script);
		} else {
			// need to do the postLoad here as well
			var self = this;
			Jmol._getRawDataFromServer(
				database,
				query,
				function(data){self._loadModel(data, ";" + script)}
			);
		}
	}
	

	// _Image -- an alternative to _Applet
	
	Jmol._Image = function(id, Info, caption, checkOnly){
		this._jmolType = "image";
		if (checkOnly)
			return this;
		this._create(id, Info, caption);
		return this;
	}

  Jmol._Image.prototype._create = function(id, Info, caption) {
  	Jmol._setObject(this, id, Info);
  	this._src || (this._src = "");
		var t = Jmol._getWrapper(this, true) 
			+ '<img id="'+id+'_image" width="' + Info.width + '" height="' + Info.height + '" src=""/>'
		 	+	Jmol._getWrapper(this, false)
			+ (Info.addSelectionOptions ? Jmol._getGrabberOptions(this, caption) : "");
		if (Jmol._debugAlert)
			alert(t);
		this._code = Jmol._documentWrite(t);
		this._ready = true;
		if (Jmol._document)
			this._readyCallback(id, null, true, null);
  }

	Jmol._Applet._setCommonMethods(Jmol._Image.prototype);

	Jmol._Image.prototype._canScript = function(script) {
		var slc = script.toLowerCase().replace(/[\",\']/g, '');
		var ipt = slc.length;
		return (script.indexOf("#alt:LOAD") >= 0 || slc.indexOf(";") < 0 && slc.indexOf("\n") < 0
		  && (slc.indexOf("script ") == 0 || slc.indexOf("load ") == 0)
		  && (slc.indexOf(".png") == ipt - 4 || slc.indexOf(".jpg") == ipt - 4));
	}

	Jmol._Image.prototype._script = function(script) {
		var slc = script.toLowerCase().replace(/[\",\']/g, '');
		// single command only
		// "script ..." or "load ..." only
		// PNG or PNGJ or JPG only
		// automatically switches to .all.png(j) from .min.png(j)
		var ipt = slc.length;
		if (slc.indexOf(";") < 0 && slc.indexOf("\n") < 0
		  && (slc.indexOf("script ") == 0 || slc.indexOf("load ") == 0)
		  && (slc.indexOf(".png") == ipt - 4 || slc.indexOf(".pngj") == ipt - 5 || slc.indexOf(".jpg") == ipt - 4)) {
			var imageFile = script.substring(script.indexOf(" ") + 1);
			ipt = imageFile.length;
			for (var i = 0; i < ipt; i++) {
				switch (imageFile.charAt(i)) {
				case " ":
					continue;
				case '"':
					imageFile = imageFile.substring(i + 1, imageFile.indexOf('"', i + 1))
					i = ipt;
					continue;
				case "'":
					imageFile = imageFile.substring(i + 1, imageFile.indexOf("'", i + 1))
					i = ipt;
					continue;
				default:
					imageFile = imageFile.substring(i)
					i = ipt;
					continue;
				}
			}
			imageFile = imageFile.replace(/\.min\.png/,".all.png")
			document.getElementById(this._id + "_image").src = imageFile
		} else if (script.indexOf("#alt:LOAD ") >= 0) {
		  imageFile = script.split("#alt:LOAD ")[1]
			if (imageFile.indexOf("??") >= 0) {
				var db = imageFile.split("??")[0];
				imageFile = prompt(imageFile.split("??")[1], "");
				if (!imageFile)
					return;
				if (!Jmol.db._DirectDatabaseCalls[imageFile.substring(0,1)])
					imageFile = db + imageFile;
			}
			this._loadFile(imageFile);
    }
	}
	
	Jmol._Image.prototype._show = function(tf) {
		Jmol._getElement(this, "appletdiv").style.display = (tf ? "block" : "none");
	}
		
	Jmol._Image.prototype._loadFile = function(fileName, params){
		this._showInfo(false);
		this._thisJmolModel = "" + Math.random();
		params = (params ? params : "");
		var database = "";
		if (Jmol._isDatabaseCall(fileName)) {
			database = fileName.substring(0, 1); 
			fileName = Jmol._getDirectDatabaseCall(fileName, false);
		} else if (fileName.indexOf("://") < 0) {
			var ref = document.location.href
			var pt = ref.lastIndexOf("/");
			fileName = ref.substring(0, pt + 1) + fileName;
		}
		
		var src = Jmol._serverUrl 
				+ "?call=getImageForFileLoad"
				+ "&file=" + escape(fileName)
				+ "&width=" + this._width
				+ "&height=" + this._height
				+ "&params=" + encodeURIComponent(params + ";frank off;");
		Jmol._getElement(this, "image").src = src;
	}

	Jmol._Image.prototype._searchDatabase = function(query, database, script){
		if (query.indexOf("?") == query.length - 1) {
			Jmol._getInfoFromDatabase(this, database, query.split("?")[0]);
			return;
		}
		this._showInfo(false);
		script || (script = Jmol._getScriptForDatabase(database));
		var src = Jmol._serverUrl 
			+ "?call=getImageFromDatabase"
			+ "&database=" + database
			+ "&query=" + query
			+ "&width=" + this._width
			+ "&height=" + this._height
			+ "&script=" + encodeURIComponent(script + ";frank off;");
		Jmol._getElement(this, "image").src = src;
	}

})(Jmol, document);
