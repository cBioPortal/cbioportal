// JmolApi.js -- Jmol user functions  Bob Hanson hansonr@stolaf.edu

// along with this file you need at least JmolCore.js and JmolApplet.js. Also, if you want buttons, JmolControls.js
// in that order. Then include JmolApi.js. 

// default settings are below. Generally you would do something like this:

// jmol = "jmol"
// Info = {.....your settings if not default....}
// Jmol.jmolButton(jmol,....)
// jmol = Jmol.getApplet(jmol, Info)
// Jmol.script(jmol,"....")
// Jmol.jmolLink(jmol,....)
// etc. 
// first parameter is always the applet id, either the string "jmol" or the object defined by Jmol.getApplet()
// no need for waiting to start giving script commands. You can also define a callback function as part of Info.

// see JmolCore.js for details

// BH 8/12/2012 5:15:11 PM added Jmol.getAppletHtml()

(function (Jmol) {

	Jmol.getVersion = function(){return Jmol._jmolInfo.version};

	Jmol.getApplet = function(id, Info, checkOnly) {
	
	// note that the variable name the return is assigned to MUST match the first parameter in quotes
	// applet = Jmol.getApplet("applet", Info)

		checkOnly || (checkOnly = false);
		Info || (Info = {});
		var DefaultInfo = {
			color: "#FFFFFF", // applet object background color, as for older jmolSetBackgroundColor(s)
			width: 300,
			height: 300,
			addSelectionOptions: false,
			serverURL: "http://chemapps.stolaf.edu/jmol/jmolcd.php",
			defaultModel: "",
			script: null,
			src: null,
			readyFunction: null,
			use: "Java noWebGL noHTML5 noImage",//remove "no" to enables other options (order important)
			jarPath: ".",
			jarFile: "JmolApplet0.jar",
			isSigned: false,
			debug: false
		};	 
		Jmol._addDefaultInfo(Info, DefaultInfo);
		Jmol._debugAlert = Info.debug;	
		Info.serverURL && (Jmol._serverUrl = Info.serverURL);
		var model = (checkOnly ? null : Info.defaultModel);
		var applet = null;
		
		if (Info.use) {
		
		// better idea....
		// order matters; must have JAVA, WEBGL, HTML5, or IMAGE in some order, separated by a single space
		
		  var List = Info.use.toUpperCase().split(" ");
		  var javaAllowed = false;
		  for (var i = 0; i < List.length; i++) {
		    switch (List[i]) {
		    case "JAVA":
		    	javaAllowed = true;
		    	if (Jmol.featureDetection.supportsJava())
						applet = new Jmol._Applet(id, Info, null, checkOnly);
					break;
		    case "WEBGL":
					Info.useWebGlIfAvailable = true;
					applet = Jmol._getCanvas(id, Info, checkOnly);
		      break;
		    case "HTML5":
					Info.useWebGlIfAvailable = false;
					applet = Jmol._getCanvas(id, Info, checkOnly);
		      break;
		    case "IMAGE":
					applet = new Jmol._Image(id, Info, null, checkOnly);
					break;
		    }
		    if (applet != null)
		    	break;		  
		  }
		  if (applet == null) {
		  	if (checkOnly || !javaAllowed)
		  		applet = {_jmolType : "none" };
		  	else
	 		  	applet = new Jmol._Applet(id, Info, null, false);
			}
			
		} else {

			// early idea
			
			if (!Info.useNoApplet && !Info.useImageOnly 
				&& (navigator.javaEnabled() || Info.useJmolOnly)) {
			
			// Jmol applet, signed or unsigned
			
				applet = new Jmol._Applet(id, Info, null, checkOnly);
			} 
			if (applet == null) {
				if (!Info.useJmolOnly && !Info.useImageOnly) {
					applet = Jmol._getCanvas(id, Info, checkOnly);
				} 
				if (applet == null)
					applet = new Jmol._Image(id, Info, null, checkOnly);
			}
		}
		// keyed to both its string id and itself
		return (checkOnly ? applet : Jmol._registerApplet(id, applet));
	}

	Jmol.getJMEApplet = function(id, Info, linkedApplet) {
	
	// requires JmolJME.js and JME.jar
	// note that the variable name the return is assigned to MUST match the first parameter in quotes
	// jme = Jmol.getJMEApplet("jme", Info)

		Info || (Info = {});
		var DefaultInfo = {
			width: 300,
			height: 300,
			jarPath: ".",
			jarFile: "JME.jar",
			options: "autoez"
			// see http://www2.chemie.uni-erlangen.de/services/fragment/editor/jme_functions.html
			// rbutton, norbutton - show / hide R button
			// hydrogens, nohydrogens - display / hide hydrogens
			// query, noquery - enable / disable query features
			// autoez, noautoez - automatic generation of SMILES with E,Z stereochemistry
			// nocanonize - SMILES canonicalization and detection of aromaticity supressed
			// nostereo - stereochemistry not considered when creating SMILES
			// reaction, noreaction - enable / disable reaction input
			// multipart - possibility to enter multipart structures
			// number - possibility to number (mark) atoms
			// depict - the applet will appear without editing butons,this is used for structure display only
		};		
		Jmol._addDefaultInfo(Info, DefaultInfo);
		return Jmol._registerApplet(id, new Jmol._JMEApplet(id, Info, linkedApplet));
	}

	Jmol.getJSVApplet = function(id, Info) {
	
	// requires JmolJSV.js and either JSpecViewApplet.jar or JSpecViewAppletSigned.jar
	// note that the variable name the return is assigned to MUST match the first parameter in quotes
	// applet = Jmol.getJSVApplet("applet", Info)

		Info || (Info = {});
		var DefaultInfo = {
			width: 500,
			height: 300,
			debug: false,
			jarPath: ".",
			jarFile: "JSpecViewApplet.jar",
			isSigned: false,
			initParams: null,
			readyFunction: null,
			script: null
		};
		Jmol._addDefaultInfo(Info, DefaultInfo);
		return Jmol._registerApplet(id, new Jmol._JSVApplet(id, Info, null));
	}

  Jmol.getAppletHtml = function(applet) {
    return applet._code;
	}
		
	Jmol.script = function(applet, script) {	
		applet._script(script);
	}
	
	Jmol.scriptWait = function(applet, script) {
		return applet._scriptWait(script);
	}
	
	Jmol.scriptEcho = function(applet, script) {
		return applet._scriptEcho(script);
	}
	
	Jmol.scriptMessage = function(applet, script) {
		return applet._scriptMessage(script);
	}
	
	Jmol.scriptWaitOutput = function(applet, script) {
		return applet._scriptWait(script);
	}
	
	Jmol.scriptWaitAsArray = function(applet, script) {
		return applet._scriptWait(script);
	}
	
	Jmol.getStatus = function(applet,strStatus) {
		return applet._getStatus(strStatus);
	}
	
	Jmol.getPropertyAsArray = function(applet,sKey,sValue) {
		return applet._getPropertyAsArray(sKey,sValue);
	}

	Jmol.getPropertyAsString = function(applet,sKey,sValue) {
		return applet._getPropertyAsString(sKey,sValue);
	}

	Jmol.getPropertyAsJSON = function(applet,sKey,sValue) {
		return applet._getPropertyAsJSON(sKey,sValue);
	}

	Jmol.getPropertyAsJavaObject = function(applet,sKey,sValue) {
		return applet._getPropertyAsJavaObject(sKey,sValue);
	}
	
	Jmol.evaluate = function(applet,molecularMath) {
		return applet._evaluate(molecularMath);
	}
	
	Jmol.saveOrientation = function(applet,id) {
		return applet._saveOrientation(id);
	}
	
	Jmol.restoreOrientation = function(applet,id) {
		return applet._restoreOrientation(id);
	}
	
	Jmol.restoreOrientationDelayed = function(applet,id,delay) {
		return applet._restoreOrientationDelayed(id,delay);
	}
	
	Jmol.resizeApplet = function(applet,size) {
		return applet._resizeApplet(size);
	}

	Jmol.search = function(applet, query, script) {
		applet._search(query, script);
	}

	Jmol.loadFile = function(applet, fileName, params){
		applet._loadFile(fileName, params);
	}

	Jmol.say = function(msg) {
		alert(msg);
	}

	Jmol.setInfo = function(applet, info, isShown) {
		applet._info = info;
		if (arguments.length > 2)
			applet._showInfo(isShown);
	}

	Jmol.getInfo = function(applet) {
		return applet._info;
	}

	Jmol.showInfo = function(applet, tf) {
		applet._showInfo(tf);
	}


	

//////////// controls and HTML /////////////


	Jmol.jmolBr = function() {
		return Jmol._documentWrite("<br />");
	}

	Jmol.jmolButton = function(appletOrId, script, label, id, title) {
		return Jmol.controls._getButton(appletOrId, script, label, id, title);
	}
	
	Jmol.jmolCheckbox = function(appletOrId, scriptWhenChecked, scriptWhenUnchecked,
			labelHtml, isChecked, id, title) {
		return Jmol.controls._getCheckbox(appletOrId, scriptWhenChecked, scriptWhenUnchecked,
			labelHtml, isChecked, id, title);
	}


	Jmol.jmolCommandInput = function(appletOrId, label, size, id, title) {
		return Jmol.controls._getCommandInput(appletOrId, label, size, id, title);
	}
		
	Jmol.jmolHtml = function(html) {
		return Jmol._documentWrite(html);
	}
	
	Jmol.jmolLink = function(appletOrId, script, label, id, title) {
		return Jmol.controls._getLink(appletOrId, script, label, id, title);
	}

	Jmol.jmolMenu = function(appletOrId, arrayOfMenuItems, size, id, title) {
		return Jmol.controls._getMenu(appletOrId, arrayOfMenuItems, size, id, title);
	}

	Jmol.jmolRadio = function(appletOrId, script, labelHtml, isChecked, separatorHtml, groupName, id, title) {
		return Jmol.controls._getRadio(appletOrId, script, labelHtml, isChecked, separatorHtml, groupName, id, title);
	}

	Jmol.jmolRadioGroup = function (appletOrId, arrayOfRadioButtons, separatorHtml, groupName, id, title) {
		return Jmol.controls._getRadioGroup(appletOrId, arrayOfRadioButtons, separatorHtml, groupName, id, title);
	}

	Jmol.setCheckboxGroup = function(chkMaster,chkBox) {
		Jmol.controls._cbSetCheckboxGroup(chkMaster, chkBox);
	}
	
	Jmol.setDocument = function(doc) {
		
		// If doc is null or 0, Jmol.getApplet() will still return an Object, but the HTML will
		// put in applet._code and not written to the page. This can be nice, because then you 
		// can still refer to the applet, but place it on the page after the controls are made. 
		//
		// This really isn't necessary, though, because there is a simpler way: Just define the 
		// applet variable like this:
		//
		// jmolApplet0 = "jmolApplet0"
		//
		// and then, in the getApplet command, use
		//
		// jmolapplet0 = Jmol.getApplet(jmolApplet0,....)
		// 
		// prior to this, "jmolApplet0" will suffice, and after it, the Object will work as well
		// in any button creation 
		//		 
		//  Bob Hanson 25.04.2012
		
		Jmol._document = doc;
	}

	Jmol.setXHTML = function(id) {
		Jmol._isXHTML = true;
		Jmol._XhtmlElement = null;
		Jmol._XhtmlAppendChild = false;
		if (id){
			Jmol._XhtmlElement = document.getElementById(id);
			Jmol._XhtmlAppendChild = true;
		}
	}

	////////////////////////////////////////////////////////////////
	// Cascading Style Sheet Class support
	////////////////////////////////////////////////////////////////
	
	// BH 4/25 -- added text option. setAppletCss(null, "style=\"xxxx\"")
	// note that since you must add the style keyword, this can be used to add any attribute to these tags, not just css. 
	
	Jmol.setAppletCss = function(cssClass, text) {
		cssClass != null && (Jmol.controls._appletCssClass = cssClass);
		Jmol.controls._appletCssText = text ? text + " " : cssClass ? "class=\"" + cssClass + "\" " : "";
	}
	
	Jmol.setButtonCss = function(cssClass, text) {
		cssClass != null && (Jmol.controls._buttonCssClass = cssClass);
		Jmol.controls._buttonCssText = text ? text + " " : cssClass ? "class=\"" + cssClass + "\" " : "";
	}
	
	Jmol.setCheckboxCss = function(cssClass, text) {
		cssClass != null && (Jmol.controls._checkboxCssClass = cssClass);
		Jmol.controls._checkboxCssText = text ? text + " " : cssClass ? "class=\"" + cssClass + "\" " : "";
	}
	
	Jmol.setRadioCss = function(cssClass, text) {
		cssClass != null && (Jmol.controls._radioCssClass = cssClass);
		Jmol.controls._radioCssText = text ? text + " " : cssClass ? "class=\"" + cssClass + "\" " : "";
	}
	
	Jmol.setLinkCss = function(cssClass, text) {
		cssClass != null && (Jmol.controls._linkCssClass = cssClass);
		Jmol.controls._linkCssText = text ? text + " " : cssClass ? "class=\"" + cssClass + "\" " : "";
	}
	
	Jmol.setMenuCss = function(cssClass, text) {
		cssClass != null && (Jmol.controls._menuCssClass = cssClass);
		Jmol.controls._menuCssText = text ? text + " ": cssClass ? "class=\"" + cssClass + "\" " : "";
	}

  Jmol.setAppletSync = function(applets, commands, isJmolJSV) {
    Jmol._syncedApplets = applets;   // an array of appletIDs
    Jmol._syncedCommands = commands; // an array of commands; one or more may be null 
    Jmol._syncedReady = {};
    Jmol._isJmolJSVSync = isJmolJSV;
	}
	
	/*
	Jmol._grabberOptions = [
	  ["$", "NCI(small molecules)"],
	  [":", "PubChem(small molecules)"],
	  ["=", "RCSB(macromolecules)"]
	];
	*/
	
	Jmol.setGrabberOptions = function(options) {
	  Jmol._grabberOptions = options;
	}

	
})(Jmol);
