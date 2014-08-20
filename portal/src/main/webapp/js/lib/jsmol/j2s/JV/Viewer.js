Clazz.declarePackage ("JV");
Clazz.load (["java.lang.Enum", "javajs.api.PlatformViewer", "J.api.JmolViewer", "J.atomdata.AtomDataServer", "java.util.Hashtable", "javajs.awt.Dimension", "JU.Lst", "J.atomdata.RadiusData", "J.c.VDW", "JU.CommandHistory"], "JV.Viewer", ["java.io.Reader", "java.lang.Boolean", "$.Character", "$.Float", "JU.BS", "$.CU", "$.DF", "$.Measure", "$.P3", "$.P3i", "$.PT", "$.Rdr", "$.SB", "$.V3", "J.adapter.smarter.SmarterJmolAdapter", "J.api.Interface", "$.JmolAppConsoleInterface", "J.c.ANIM", "$.AXES", "$.FIL", "$.STER", "J.i18n.GT", "JS.SV", "$.T", "J.thread.TimeoutThread", "JU.BSUtil", "$.C", "$.Elements", "$.Escape", "$.GData", "$.JmolMolecule", "$.Logger", "$.Parser", "$.TempArray", "$.Txt", "JV.ActionManager", "$.AnimationManager", "$.ColorManager", "$.FileManager", "$.JC", "$.ModelManager", "$.SelectionManager", "$.ShapeManager", "$.StateManager", "$.StatusManager", "$.TransformManager", "JV.binding.Binding"], function () {
c$ = Clazz.decorateAsClass (function () {
this.autoExit = false;
this.haveDisplay = false;
this.isJS = false;
this.isWebGL = false;
this.isSingleThreaded = false;
this.queueOnHold = false;
this.fullName = "";
this.compiler = null;
this.definedAtomSets = null;
this.ms = null;
this.fm = null;
this.$isApplet = false;
this.isJNLP = false;
this.isSyntaxAndFileCheck = false;
this.isSyntaxCheck = false;
this.listCommands = false;
this.mustRender = false;
this.htmlName = "";
this.appletName = "";
this.insertedCommand = "";
this.gdata = null;
this.html5Applet = null;
this.actionManager = null;
this.am = null;
this.cm = null;
this.dm = null;
this.shm = null;
this.slm = null;
this.rm = null;
this.g = null;
this.sm = null;
this.tm = null;
this.syncId = "";
this.logFilePath = "";
this.allowScripting = false;
this.isPrintOnly = false;
this.$isSignedApplet = false;
this.isSignedAppletLocal = false;
this.isSilent = false;
this.multiTouch = false;
this.$noGraphicsAllowed = false;
this.useCommandThread = false;
this.commandOptions = null;
this.vwrOptions = null;
this.display = null;
this.modelAdapter = null;
this.access = null;
this.commandHistory = null;
this.mm = null;
this.stm = null;
this.scm = null;
this.eval = null;
this.tempArray = null;
this.allowArrayDotNotation = false;
this.$isPreviewOnly = false;
this.mouseEnabled = true;
this.noneSelected = false;
this.mouse = null;
this.ligandModels = null;
this.ligandModelSet = null;
this.annotationParser = null;
this.minimizer = null;
this.smilesMatcher = null;
this.jsc = null;
this.bsFrameOffsets = null;
this.frameOffsets = null;
this.motionEventNumber = 0;
this.inMotion = false;
this.refreshing = true;
this.axesAreTainted = false;
this.dimScreen = null;
this.maximumSize = 2147483647;
this.imageFontScaling = 1;
this.gRight = null;
this.isStereoSlave = false;
this.captureParams = null;
this.jsParams = null;
this.antialiased = false;
this.hoverAtomIndex = -1;
this.hoverText = null;
this.hoverEnabled = true;
this.currentCursor = 0;
this.ptTemp = null;
this.prevFrame = -2147483648;
this.prevMorphModel = 0;
this.haveJDX = false;
this.jsv = null;
this.rd = null;
this.frankOn = true;
this.scriptEditorVisible = false;
this.appConsole = null;
this.scriptEditor = null;
this.jmolpopup = null;
this.modelkitPopup = null;
this.headlessImageParams = null;
this.pm = null;
this.isTainted = true;
this.movingSelected = false;
this.showSelected = false;
this.rotateBondIndex = -1;
this.rotatePrev1 = -1;
this.rotatePrev2 = -1;
this.bsRotateBranch = null;
this.creatingImage = false;
this.outputManager = null;
this.bsUserVdws = null;
this.userVdws = null;
this.userVdwMars = null;
this.defaultVdw = null;
this.errorMessage = null;
this.errorMessageUntranslated = null;
this.currentShapeID = -1;
this.currentShapeState = null;
this.localFunctions = null;
this.privateKey = 0;
this.$isKiosk = false;
this.executor = null;
this.displayLoadErrors = true;
this.$isParallel = false;
this.actionStates = null;
this.actionStatesRedo = null;
this.stateScriptVersionInt = 0;
this.jsExporter3D = null;
this.timeouts = null;
this.chainMap = null;
this.chainList = null;
this.nmrCalculation = null;
this.logFileName = null;
Clazz.instantialize (this, arguments);
}, JV, "Viewer", J.api.JmolViewer, [J.atomdata.AtomDataServer, javajs.api.PlatformViewer]);
Clazz.prepareFields (c$, function () {
this.commandHistory =  new JU.CommandHistory ();
this.dimScreen =  new javajs.awt.Dimension (0, 0);
this.rd =  new J.atomdata.RadiusData (null, 0, null, null);
this.defaultVdw = J.c.VDW.JMOL;
this.localFunctions =  new java.util.Hashtable ();
this.privateKey = Math.random ();
this.actionStates =  new JU.Lst ();
this.actionStatesRedo =  new JU.Lst ();
this.chainMap =  new java.util.Hashtable ();
this.chainList =  new JU.Lst ();
});
Clazz.defineMethod (c$, "finalize", 
function () {
if (JU.Logger.debugging) JU.Logger.debug ("vwr finalize " + this);
Clazz.superCall (this, JV.Viewer, "finalize", []);
});
Clazz.defineMethod (c$, "hasDisplay", 
function () {
return this.haveDisplay;
});
Clazz.overrideMethod (c$, "isApplet", 
function () {
return this.$isApplet;
});
Clazz.defineMethod (c$, "setInsertedCommand", 
function (strScript) {
this.insertedCommand = strScript;
}, "~S");
Clazz.defineMethod (c$, "getActionManager", 
function () {
return this.actionManager;
});
Clazz.defineMethod (c$, "getLogFilePath", 
function () {
return this.logFilePath;
});
Clazz.defineMethod (c$, "isSignedApplet", 
function () {
return this.$isSignedApplet;
});
c$.getJmolVersion = Clazz.overrideMethod (c$, "getJmolVersion", 
function () {
return (JV.Viewer.version_date == null ? JV.Viewer.version_date = JV.JC.version + "  " + JV.JC.date : JV.Viewer.version_date);
});
c$.allocateViewer = Clazz.defineMethod (c$, "allocateViewer", 
function (display, modelAdapter, fullName, documentBase, codeBase, commandOptions, statusListener, implementedPlatform) {
var info =  new java.util.Hashtable ();
info.put ("display", display);
info.put ("adapter", modelAdapter);
info.put ("statusListener", statusListener);
info.put ("platform", implementedPlatform);
info.put ("options", commandOptions);
info.put ("fullName", fullName);
info.put ("documentBase", documentBase);
info.put ("codeBase", codeBase);
return  new JV.Viewer (info);
}, "~O,J.api.JmolAdapter,~S,java.net.URL,java.net.URL,~S,J.api.JmolStatusListener,javajs.api.GenericPlatform");
Clazz.makeConstructor (c$, 
function (info) {
Clazz.superConstructor (this, JV.Viewer, []);
this.setOptions (info);
}, "java.util.Map");
Clazz.defineMethod (c$, "getStatusManager", 
function () {
return this.sm;
});
Clazz.defineMethod (c$, "haveAccess", 
function (a) {
return this.access === a;
}, "JV.Viewer.ACCESS");
Clazz.overrideMethod (c$, "getModelAdapter", 
function () {
if (this.modelAdapter == null) this.modelAdapter =  new J.adapter.smarter.SmarterJmolAdapter ();
return this.modelAdapter;
});
Clazz.defineMethod (c$, "getSymmetryInfo", 
function (bsAtoms, xyz, op, pt, pt2, id, type) {
return this.getPropertyManager ().getSymmetryInfo (bsAtoms, xyz, op, pt, pt2, id, type);
}, "JU.BS,~S,~N,JU.P3,JU.P3,~S,~N");
Clazz.overrideMethod (c$, "getSmartsMatch", 
function (smarts, bsSelected) {
if (bsSelected == null) bsSelected = this.bsA ();
return this.getSmilesMatcher ().getSubstructureSet (smarts, this.ms.at, this.getAtomCount (), bsSelected, true, false);
}, "~S,JU.BS");
Clazz.defineMethod (c$, "getViewerOptions", 
function () {
return this.vwrOptions;
});
Clazz.defineMethod (c$, "setOptions", 
 function (info) {
this.vwrOptions = info;
if (JU.Logger.debugging) {
JU.Logger.debug ("Viewer constructor " + this);
}this.modelAdapter = info.get ("adapter");
var statusListener = info.get ("statusListener");
this.fullName = info.get ("fullName");
if (this.fullName == null) this.fullName = "";
var o = info.get ("codePath");
if (o == null) o = "../java/";
JV.Viewer.appletCodeBase = o.toString ();
JV.Viewer.appletIdiomaBase = JV.Viewer.appletCodeBase.substring (0, JV.Viewer.appletCodeBase.lastIndexOf ("/", JV.Viewer.appletCodeBase.length - 2) + 1) + "idioma";
o = info.get ("documentBase");
JV.Viewer.appletDocumentBase = (o == null ? "" : o.toString ());
o = info.get ("options");
this.commandOptions = (o == null ? "" : o.toString ());
if (info.containsKey ("debug") || this.commandOptions.indexOf ("-debug") >= 0) JU.Logger.setLogLevel (5);
if (this.$isApplet && info.containsKey ("maximumSize")) this.setMaximumSize ((info.get ("maximumSize")).intValue ());
this.isJNLP = this.checkOption2 ("isJNLP", "-jnlp");
if (this.isJNLP) JU.Logger.info ("setting JNLP mode TRUE");
this.$isSignedApplet = this.isJNLP || this.checkOption2 ("signedApplet", "-signed");
this.$isApplet = this.$isSignedApplet || this.checkOption2 ("applet", "-applet");
this.allowScripting = !this.checkOption2 ("noscripting", "-noscripting");
var i = this.fullName.indexOf ("__");
this.htmlName = (i < 0 ? this.fullName : this.fullName.substring (0, i));
this.appletName = JU.PT.split (this.htmlName + "_", "_")[0];
this.syncId = (i < 0 ? "" : this.fullName.substring (i + 2, this.fullName.length - 2));
this.access = (this.checkOption2 ("access:READSPT", "-r") ? JV.Viewer.ACCESS.READSPT : this.checkOption2 ("access:NONE", "-R") ? JV.Viewer.ACCESS.NONE : JV.Viewer.ACCESS.ALL);
this.$isPreviewOnly = info.containsKey ("previewOnly");
if (this.$isPreviewOnly) info.remove ("previewOnly");
this.isPrintOnly = this.checkOption2 ("printOnly", "-p");
o = info.get ("platform");
var platform = "unknown";
if (o == null) {
o = (this.commandOptions.contains ("platform=") ? this.commandOptions.substring (this.commandOptions.indexOf ("platform=") + 9) : "J.awt.Platform");
}if (Clazz.instanceOf (o, String)) {
platform = o;
this.isWebGL = (platform.indexOf (".awtjs.") >= 0);
this.isJS = this.isWebGL || (platform.indexOf (".awtjs2d.") >= 0);
var applet = null;
var ver = "?";
{
if(self.Jmol) {
applet = Jmol._applets[this.htmlName.split("_object")[0]];
ver = Jmol._version;
}
}if (ver != null) {
this.html5Applet = applet;
JV.Viewer.strJavaVersion = ver;
JV.Viewer.strJavaVendor = "Java2Script " + (this.isWebGL ? "(WebGL)" : "(HTML5)");
}o = J.api.Interface.getInterface (platform);
}this.apiPlatform = o;
this.display = info.get ("display");
this.isSingleThreaded = this.apiPlatform.isSingleThreaded ();
this.$noGraphicsAllowed = this.checkOption2 ("noGraphics", "-n");
this.haveDisplay = (this.isWebGL || this.display != null && !this.$noGraphicsAllowed && !this.isHeadless () && !this.checkOption2 ("isDataOnly", "\0"));
this.$noGraphicsAllowed = new Boolean (this.$noGraphicsAllowed & (this.display == null)).valueOf ();
if (this.haveDisplay) {
this.mustRender = true;
this.multiTouch = this.checkOption2 ("multiTouch", "-multitouch");
{
if (!this.isWebGL) this.display =
document.getElementById(this.display);
}} else {
this.display = null;
}this.apiPlatform.setViewer (this, this.display);
o = info.get ("graphicsAdapter");
if (o == null && !this.isWebGL) o = J.api.Interface.getOption ("g3d.Graphics3D");
this.gdata = (o == null ?  new JU.GData () : o);
this.gdata.initialize (this.apiPlatform);
this.stm =  new JV.StateManager (this);
this.cm =  new JV.ColorManager (this, this.gdata);
this.sm =  new JV.StatusManager (this);
var is4D = info.containsKey ("4DMouse");
this.tm = JV.TransformManager.getTransformManager (this, 2147483647, 0, is4D);
this.slm =  new JV.SelectionManager (this);
if (this.haveDisplay) {
this.actionManager = (this.multiTouch ? J.api.Interface.getOption ("multitouch.ActionManagerMT") :  new JV.ActionManager ());
this.actionManager.setViewer (this, this.commandOptions + "-multitouch-" + info.get ("multiTouch"));
this.mouse = this.apiPlatform.getMouseManager (this.privateKey, this.display);
if (this.multiTouch && !this.checkOption2 ("-simulated", "-simulated")) this.apiPlatform.setTransparentCursor (this.display);
}this.mm =  new JV.ModelManager (this);
this.shm =  new JV.ShapeManager (this);
this.tempArray =  new JU.TempArray ();
this.am =  new JV.AnimationManager (this);
o = info.get ("repaintManager");
if (o == null) o = (J.api.Interface.getOption ("render.RepaintManager"));
if (o != null && !o.equals ("")) (this.rm = o).set (this, this.shm);
this.initialize (true);
this.fm =  new JV.FileManager (this);
this.definedAtomSets =  new java.util.Hashtable ();
this.setJmolStatusListener (statusListener);
if (this.$isApplet) {
JU.Logger.info ("vwrOptions: \n" + JU.Escape.escapeMap (this.vwrOptions));
var path = this.vwrOptions.get ("documentLocation");
if (!this.isJS && path != null && path.startsWith ("file:/")) {
path = path.substring (0, path.substring (0, (path + "?").indexOf ("?")).lastIndexOf ("/"));
JU.Logger.info ("setting current directory to " + path);
this.cd (path);
}path = JV.Viewer.appletDocumentBase;
i = path.indexOf ("#");
if (i >= 0) path = path.substring (0, i);
i = path.lastIndexOf ("?");
if (i >= 0) path = path.substring (0, i);
i = path.lastIndexOf ("/");
if (i >= 0) path = path.substring (0, i);
JV.Viewer.jsDocumentBase = path;
this.fm.setAppletContext (JV.Viewer.appletDocumentBase);
var appletProxy = info.get ("appletProxy");
if (appletProxy != null) this.setStringProperty ("appletProxy", appletProxy);
if (this.$isSignedApplet) {
this.logFilePath = JU.PT.rep (JV.Viewer.appletCodeBase, "file://", "");
this.logFilePath = JU.PT.rep (this.logFilePath, "file:/", "");
if (this.logFilePath.indexOf ("//") >= 0) this.logFilePath = null;
 else this.isSignedAppletLocal = true;
} else if (!this.isJS) {
this.logFilePath = null;
}} else {
this.gdata.setBackgroundTransparent (this.checkOption2 ("backgroundTransparent", "-b"));
this.isSilent = this.checkOption2 ("silent", "-i");
if (this.isSilent) JU.Logger.setLogLevel (3);
this.isSyntaxAndFileCheck = this.checkOption2 ("checkLoad", "-C");
this.isSyntaxCheck = this.isSyntaxAndFileCheck || this.checkOption2 ("check", "-c");
this.listCommands = this.checkOption2 ("listCommands", "-l");
this.autoExit = this.checkOption2 ("exit", "-x");
this.cd (".");
if (this.isHeadless ()) {
this.headlessImageParams = info.get ("headlessImage");
o = info.get ("headlistMaxTimeMs");
if (o == null) o = Integer.$valueOf (60000);
this.setTimeout ("" + Math.random (), (o).intValue (), "exitJmol");
}}this.useCommandThread = !this.isHeadless () && this.checkOption2 ("useCommandThread", "-threaded");
this.setStartupBooleans ();
this.setIntProperty ("_nProcessors", JV.Viewer.nProcessors);
if (!this.isSilent) {
JU.Logger.info ("(C) 2012 Jmol Development" + "\nJmol Version: " + JV.Viewer.getJmolVersion () + "\njava.vendor: " + JV.Viewer.strJavaVendor + "\njava.version: " + JV.Viewer.strJavaVersion + "\nos.name: " + JV.Viewer.strOSName + "\nAccess: " + this.access + "\nmemory: " + this.getP ("_memory") + "\nprocessors available: " + JV.Viewer.nProcessors + "\nuseCommandThread: " + this.useCommandThread + (!this.$isApplet ? "" : "\nappletId:" + this.htmlName + (this.$isSignedApplet ? " (signed)" : "")));
}this.zap (false, true, false);
this.g.setO ("language", J.i18n.GT.getLanguage ());
this.stm.setJmolDefaults ();
JU.Elements.covalentVersion = 1;
this.allowArrayDotNotation = true;
}, "java.util.Map");
Clazz.defineMethod (c$, "setDisplay", 
function (canvas) {
this.display = canvas;
this.apiPlatform.setViewer (this, canvas);
}, "~O");
Clazz.defineMethod (c$, "newMeasurementData", 
function (id, points) {
return (J.api.Interface.getInterface ("JM.MeasurementData")).init (id, this, points);
}, "~S,JU.Lst");
Clazz.defineMethod (c$, "getDataManager", 
 function () {
return (this.dm == null ? (this.dm = (J.api.Interface.getInterface ("JV.DataManager")).set (this)) : this.dm);
});
Clazz.defineMethod (c$, "getScriptManager", 
 function () {
if (this.allowScripting && this.scm == null) {
this.scm = J.api.Interface.getInterface ("JS.ScriptManager");
if (this.scm == null) {
this.allowScripting = false;
return null;
}this.eval = this.scm.setViewer (this);
if (this.useCommandThread) this.scm.startCommandWatcher (true);
}return this.scm;
});
Clazz.defineMethod (c$, "checkOption2", 
 function (key1, key2) {
return (this.vwrOptions.containsKey (key1) || this.commandOptions.indexOf (key2) >= 0);
}, "~S,~S");
Clazz.defineMethod (c$, "isPreviewOnly", 
function () {
return this.$isPreviewOnly;
});
Clazz.defineMethod (c$, "isHeadless", 
function () {
return this.apiPlatform.isHeadless ();
});
Clazz.defineMethod (c$, "setStartupBooleans", 
 function () {
this.setBooleanProperty ("_applet", this.$isApplet);
this.setBooleanProperty ("_JSpecView".toLowerCase (), false);
this.setBooleanProperty ("_signedApplet", this.$isSignedApplet);
this.setBooleanProperty ("_headless", this.apiPlatform.isHeadless ());
this.setStringProperty ("_restrict", "\"" + this.access + "\"");
this.setBooleanProperty ("_useCommandThread", this.useCommandThread);
});
Clazz.defineMethod (c$, "noGraphicsAllowed", 
function () {
return this.$noGraphicsAllowed;
});
Clazz.defineMethod (c$, "getExportDriverList", 
function () {
return (this.haveAccess (JV.Viewer.ACCESS.ALL) ? this.g.getParameter ("exportDrivers", true) : "");
});
Clazz.defineMethod (c$, "getHtmlName", 
function () {
return this.htmlName;
});
Clazz.overrideMethod (c$, "getDisplay", 
function () {
return this.display;
});
Clazz.overrideMethod (c$, "dispose", 
function () {
this.gRight = null;
if (this.mouse != null) {
this.actionManager.dispose ();
this.mouse.dispose ();
this.mouse = null;
}this.clearScriptQueue ();
this.clearThreads ();
this.haltScriptExecution ();
if (this.scm != null) this.scm.clear (true);
this.gdata.destroy ();
if (this.jmolpopup != null) this.jmolpopup.jpiDispose ();
if (this.modelkitPopup != null) this.modelkitPopup.jpiDispose ();
try {
if (this.appConsole != null) {
this.appConsole.dispose ();
this.appConsole = null;
}if (this.scriptEditor != null) {
this.scriptEditor.dispose ();
this.scriptEditor = null;
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
});
Clazz.defineMethod (c$, "reset", 
function (includingSpin) {
this.ms.calcBoundBoxDimensions (null, 1);
this.axesAreTainted = true;
this.tm.homePosition (includingSpin);
if (this.ms.setCrystallographicDefaults ()) this.stm.setCrystallographicDefaults ();
 else this.setAxesModeMolecular (false);
this.prevFrame = -2147483648;
if (!this.tm.spinOn) this.refresh (-1, "Viewer:homePosition()");
}, "~B");
Clazz.overrideMethod (c$, "homePosition", 
function () {
this.evalString ("reset spin");
});
Clazz.defineMethod (c$, "initialize", 
function (clearUserVariables) {
this.g = this.stm.getGlobalSettings (this.g, clearUserVariables);
this.setStartupBooleans ();
this.g.setI ("_width", this.dimScreen.width);
this.g.setI ("_height", this.dimScreen.height);
if (this.haveDisplay) {
this.g.setB ("_is2D", this.isJS && !this.isWebGL);
this.g.setB ("_multiTouchClient", this.actionManager.isMTClient ());
this.g.setB ("_multiTouchServer", this.actionManager.isMTServer ());
}this.cm.resetElementColors ();
this.setObjectColor ("background", "black");
this.setObjectColor ("axis1", "red");
this.setObjectColor ("axis2", "green");
this.setObjectColor ("axis3", "blue");
this.gdata.setAmbientPercent (this.g.ambientPercent);
this.gdata.setDiffusePercent (this.g.diffusePercent);
this.gdata.setSpecular (this.g.specular);
this.gdata.setCel (this.g.celShading);
this.gdata.setCelPower (this.g.celShadingPower);
this.gdata.setSpecularPercent (this.g.specularPercent);
this.gdata.setSpecularPower (-this.g.specularExponent);
this.gdata.setPhongExponent (this.g.phongExponent);
this.gdata.setSpecularPower (this.g.specularPower);
if (this.ms != null) this.am.setAnimationOn (false);
this.am.setAnimationFps (this.g.animationFps);
this.sm.setAllowStatusReporting (this.g.statusReporting);
this.setBooleanProperty ("antialiasDisplay", this.g.antialiasDisplay);
this.setTransformManagerDefaults ();
}, "~B");
Clazz.defineMethod (c$, "saveModelOrientation", 
function () {
this.ms.saveModelOrientation (this.am.cmi, this.stm.getOrientation ());
});
Clazz.defineMethod (c$, "restoreModelOrientation", 
function (modelIndex) {
var o = this.ms.getModelOrientation (modelIndex);
if (o != null) o.restore (-1, true);
}, "~N");
Clazz.defineMethod (c$, "restoreModelRotation", 
function (modelIndex) {
var o = this.ms.getModelOrientation (modelIndex);
if (o != null) o.restore (-1, false);
}, "~N");
Clazz.defineMethod (c$, "getGLmolView", 
function () {
var tm = this.tm;
{
return {
center:tm.fixedRotationCenter,
quaternion:tm.getRotationQuaternion(),
xtrans:tm.xTranslationFraction,
ytrans:tm.yTranslationFraction,
scale:tm.scalePixelsPerAngstrom,
zoom:tm.zoomPercent,
cameraDistance:tm.cameraDistance,
pixelCount:tm.screenPixelCount,
perspective:tm.perspectiveDepth,
width:tm.width,
height:tm.height
};
}});
Clazz.defineMethod (c$, "setRotationRadius", 
function (angstroms, doAll) {
if (doAll) angstroms = this.tm.setRotationRadius (angstroms, false);
if (this.ms.setRotationRadius (this.am.cmi, angstroms)) this.g.setF ("rotationRadius", angstroms);
}, "~N,~B");
Clazz.defineMethod (c$, "setCenterBitSet", 
function (bsCenter, doScale) {
var center = (JU.BSUtil.cardinalityOf (bsCenter) > 0 ? this.ms.getAtomSetCenter (bsCenter) : null);
if (this.isJmolDataFrame ()) return;
this.tm.setNewRotationCenter (center, doScale);
}, "JU.BS,~B");
Clazz.defineMethod (c$, "setNewRotationCenter", 
function (center) {
if (this.isJmolDataFrame ()) return;
this.tm.setNewRotationCenter (center, true);
}, "JU.P3");
Clazz.defineMethod (c$, "navigate", 
function (keyWhere, modifiers) {
if (this.isJmolDataFrame ()) return;
this.tm.navigateKey (keyWhere, modifiers);
if (!this.tm.vibrationOn && keyWhere != 0) this.refresh (1, "Viewer:navigate()");
}, "~N,~N");
Clazz.defineMethod (c$, "move", 
function (eval, dRot, dZoom, dTrans, dSlab, floatSecondsTotal, fps) {
this.tm.move (eval, dRot, dZoom, dTrans, dSlab, floatSecondsTotal, fps);
this.moveUpdate (floatSecondsTotal);
}, "J.api.JmolScriptEvaluator,JU.V3,~N,JU.V3,~N,~N,~N");
Clazz.defineMethod (c$, "moveTo", 
function (eval, floatSecondsTotal, center, rotAxis, degrees, rotationMatrix, zoom, xTrans, yTrans, rotationRadius, navCenter, xNav, yNav, navDepth, cameraDepth, cameraX, cameraY) {
if (!this.haveDisplay) floatSecondsTotal = 0;
this.setTainted (true);
this.tm.moveTo (eval, floatSecondsTotal, center, rotAxis, degrees, rotationMatrix, zoom, xTrans, yTrans, rotationRadius, navCenter, xNav, yNav, navDepth, cameraDepth, cameraX, cameraY);
}, "J.api.JmolScriptEvaluator,~N,JU.P3,JU.V3,~N,JU.M3,~N,~N,~N,~N,JU.P3,~N,~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "moveUpdate", 
function (floatSecondsTotal) {
if (floatSecondsTotal > 0) this.requestRepaintAndWait ("moveUpdate");
 else if (floatSecondsTotal == 0) this.setSync ();
}, "~N");
Clazz.defineMethod (c$, "navigatePt", 
function (center) {
this.tm.setNavigatePt (center);
this.setSync ();
}, "JU.P3");
Clazz.defineMethod (c$, "navigateAxis", 
function (rotAxis, degrees) {
this.tm.navigateAxis (rotAxis, degrees);
this.setSync ();
}, "JU.V3,~N");
Clazz.defineMethod (c$, "navTranslatePercent", 
function (x, y) {
if (this.isJmolDataFrame ()) return;
this.tm.navTranslatePercentOrTo (0, x, y);
this.setSync ();
}, "~N,~N");
Clazz.defineMethod (c$, "setMouseEnabled", 
function (TF) {
this.mouseEnabled = TF;
}, "~B");
Clazz.overrideMethod (c$, "processMultitouchEvent", 
function (groupID, eventType, touchID, iData, pt, time) {
this.actionManager.processMultitouchEvent (groupID, eventType, touchID, iData, pt, time);
}, "~N,~N,~N,~N,JU.P3,~N");
Clazz.defineMethod (c$, "zoomBy", 
function (pixels) {
if (this.mouseEnabled) this.tm.zoomBy (pixels);
this.refresh (2, this.sm.syncingMouse ? "Mouse: zoomBy " + pixels : "");
}, "~N");
Clazz.defineMethod (c$, "zoomByFactor", 
function (factor, x, y) {
if (this.mouseEnabled) this.tm.zoomByFactor (factor, x, y);
this.refresh (2, !this.sm.syncingMouse ? "" : "Mouse: zoomByFactor " + factor + (x == 2147483647 ? "" : " " + x + " " + y));
}, "~N,~N,~N");
Clazz.defineMethod (c$, "rotateXYBy", 
function (degX, degY) {
if (this.mouseEnabled) this.tm.rotateXYBy (degX, degY, null);
this.refresh (2, this.sm.syncingMouse ? "Mouse: rotateXYBy " + degX + " " + degY : "");
}, "~N,~N");
Clazz.defineMethod (c$, "spinXYBy", 
function (xDelta, yDelta, speed) {
if (this.mouseEnabled) this.tm.spinXYBy (xDelta, yDelta, speed);
if (xDelta == 0 && yDelta == 0) return;
this.refresh (2, this.sm.syncingMouse ? "Mouse: spinXYBy " + xDelta + " " + yDelta + " " + speed : "");
}, "~N,~N,~N");
Clazz.defineMethod (c$, "rotateZBy", 
function (zDelta, x, y) {
if (this.mouseEnabled) this.tm.rotateZBy (zDelta, x, y);
this.refresh (2, this.sm.syncingMouse ? "Mouse: rotateZBy " + zDelta + (x == 2147483647 ? "" : " " + x + " " + y) : "");
}, "~N,~N,~N");
Clazz.defineMethod (c$, "rotateSelected", 
function (deltaX, deltaY, bsSelected) {
if (this.isJmolDataFrame ()) return;
if (this.mouseEnabled) {
this.tm.rotateXYBy (deltaX, deltaY, this.setMovableBitSet (bsSelected, false));
this.refreshMeasures (true);
}this.refresh (2, this.sm.syncingMouse ? "Mouse: rotateMolecule " + deltaX + " " + deltaY : "");
}, "~N,~N,JU.BS");
Clazz.defineMethod (c$, "setMovableBitSet", 
 function (bsSelected, checkMolecule) {
if (bsSelected == null) bsSelected = this.bsA ();
bsSelected = JU.BSUtil.copy (bsSelected);
JU.BSUtil.andNot (bsSelected, this.getMotionFixedAtoms ());
if (checkMolecule && !this.g.allowMoveAtoms) bsSelected = this.ms.getMoleculeBitSet (bsSelected);
return bsSelected;
}, "JU.BS,~B");
Clazz.defineMethod (c$, "translateXYBy", 
function (xDelta, yDelta) {
if (this.mouseEnabled) this.tm.translateXYBy (xDelta, yDelta);
this.refresh (2, this.sm.syncingMouse ? "Mouse: translateXYBy " + xDelta + " " + yDelta : "");
}, "~N,~N");
Clazz.overrideMethod (c$, "rotateFront", 
function () {
this.tm.resetRotation ();
this.refresh (1, "Viewer:rotateFront()");
});
Clazz.defineMethod (c$, "translate", 
function (xyz, x, type, bsAtoms) {
var xy = (type == '\0' ? Clazz.floatToInt (x) : type == '%' ? this.tm.percentToPixels (xyz, x) : this.tm.angstromsToPixels (x * (type == 'n' ? 10 : 1)));
if (bsAtoms != null) {
if (xy == 0) return;
this.tm.setSelectedTranslation (bsAtoms, xyz, xy);
} else {
switch (xyz) {
case 'X':
case 'x':
if (type == '\0') this.tm.translateToPercent ('x', x);
 else this.tm.translateXYBy (xy, 0);
break;
case 'Y':
case 'y':
if (type == '\0') this.tm.translateToPercent ('y', x);
 else this.tm.translateXYBy (0, xy);
break;
case 'Z':
case 'z':
if (type == '\0') this.tm.translateToPercent ('z', x);
 else this.tm.translateZBy (xy);
break;
}
}this.refresh (1, "Viewer:translate()");
}, "~S,~N,~S,JU.BS");
Clazz.overrideMethod (c$, "getZoomPercent", 
function () {
return Clazz.floatToInt (this.tm.getZoomSetting ());
});
Clazz.overrideMethod (c$, "getZoomPercentFloat", 
function () {
return this.tm.getZoomPercentFloat ();
});
Clazz.defineMethod (c$, "getMaxZoomPercent", 
function () {
return 200000;
});
Clazz.defineMethod (c$, "slabByPixels", 
function (pixels) {
this.tm.slabByPercentagePoints (pixels);
this.refresh (3, "slabByPixels");
}, "~N");
Clazz.defineMethod (c$, "depthByPixels", 
function (pixels) {
this.tm.depthByPercentagePoints (pixels);
this.refresh (3, "depthByPixels");
}, "~N");
Clazz.defineMethod (c$, "slabDepthByPixels", 
function (pixels) {
this.tm.slabDepthByPercentagePoints (pixels);
this.refresh (3, "slabDepthByPixels");
}, "~N");
Clazz.defineMethod (c$, "finalizeTransformParameters", 
function () {
this.tm.finalizeTransformParameters ();
this.gdata.setSlab (this.tm.slabValue);
this.gdata.setDepth (this.tm.depthValue);
this.gdata.setZShade (this.tm.zShadeEnabled, this.tm.zSlabValue, this.tm.zDepthValue, this.g.zShadePower);
});
Clazz.defineMethod (c$, "getScalePixelsPerAngstrom", 
function (asAntialiased) {
return this.tm.scalePixelsPerAngstrom * (asAntialiased || !this.antialiased ? 1 : 0.5);
}, "~B");
Clazz.defineMethod (c$, "setSpin", 
function (key, value) {
if (!JU.PT.isOneOf (key, ";x;y;z;fps;X;Y;Z;FPS;")) return;
var i = "x;y;z;fps;X;Y;Z;FPS".indexOf (key);
switch (i) {
case 0:
this.tm.setSpinXYZ (value, NaN, NaN);
break;
case 2:
this.tm.setSpinXYZ (NaN, value, NaN);
break;
case 4:
this.tm.setSpinXYZ (NaN, NaN, value);
break;
case 6:
default:
this.tm.setSpinFps (value);
break;
case 10:
this.tm.setNavXYZ (value, NaN, NaN);
break;
case 12:
this.tm.setNavXYZ (NaN, value, NaN);
break;
case 14:
this.tm.setNavXYZ (NaN, NaN, value);
break;
case 16:
this.tm.setNavFps (value);
break;
}
this.g.setI ((i < 10 ? "spin" : "nav") + key, value);
}, "~S,~N");
Clazz.defineMethod (c$, "getSpinState", 
function () {
return this.getStateCreator ().getSpinState (false);
});
Clazz.defineMethod (c$, "getOrientationText", 
function (type, name) {
switch (type) {
case 1313866249:
case 1073741863:
case 1112541205:
case 1112541206:
case 1112541207:
case 135270418:
return this.ms.getBoundBoxOrientation (type, this.bsA ());
case 1073742035:
return this.stm.getSavedOrientationText (name);
default:
return this.tm.getOrientationText (type);
}
}, "~N,~S");
Clazz.defineMethod (c$, "getCurrentColorRange", 
function () {
return this.cm.getPropertyColorRange ();
});
Clazz.defineMethod (c$, "setDefaultColors", 
 function (isRasmol) {
this.cm.setDefaultColors (isRasmol);
this.g.setB ("colorRasmol", isRasmol);
this.g.setO ("defaultColorScheme", (isRasmol ? "rasmol" : "jmol"));
}, "~B");
Clazz.defineMethod (c$, "getColorArgbOrGray", 
function (colix) {
return this.gdata.getColorArgbOrGray (colix);
}, "~N");
Clazz.defineMethod (c$, "setElementArgb", 
function (elementNumber, argb) {
this.g.setO ("=color " + JU.Elements.elementNameFromNumber (elementNumber), JU.Escape.escapeColor (argb));
this.cm.setElementArgb (elementNumber, argb);
}, "~N,~N");
Clazz.overrideMethod (c$, "setVectorScale", 
function (scale) {
this.g.setF ("vectorScale", scale);
this.g.vectorScale = scale;
}, "~N");
Clazz.overrideMethod (c$, "setVibrationScale", 
function (scale) {
this.tm.setVibrationScale (scale);
this.g.vibrationScale = scale;
this.g.setF ("vibrationScale", scale);
}, "~N");
Clazz.defineMethod (c$, "setVibrationOff", 
function () {
this.tm.setVibrationPeriod (0);
});
Clazz.overrideMethod (c$, "setVibrationPeriod", 
function (period) {
this.tm.setVibrationPeriod (period);
period = Math.abs (period);
this.g.vibrationPeriod = period;
this.g.setF ("vibrationPeriod", period);
}, "~N");
Clazz.defineMethod (c$, "setObjectColor", 
function (name, colorName) {
if (colorName == null || colorName.length == 0) return;
this.setObjectArgb (name, JU.CU.getArgbFromString (colorName));
}, "~S,~S");
Clazz.defineMethod (c$, "setObjectVisibility", 
function (name, b) {
var objId = JV.StateManager.getObjectIdFromName (name);
if (objId >= 0) {
this.setShapeProperty (objId, "display", b ? Boolean.TRUE : Boolean.FALSE);
}}, "~S,~B");
Clazz.defineMethod (c$, "setObjectArgb", 
function (name, argb) {
var objId = JV.StateManager.getObjectIdFromName (name);
if (objId < 0) {
if (name.equalsIgnoreCase ("axes")) {
this.setObjectArgb ("axis1", argb);
this.setObjectArgb ("axis2", argb);
this.setObjectArgb ("axis3", argb);
}return;
}this.g.objColors[objId] = argb;
switch (objId) {
case 0:
this.gdata.setBackgroundArgb (argb);
this.cm.setColixBackgroundContrast (argb);
break;
}
this.g.setO (name + "Color", JU.Escape.escapeColor (argb));
}, "~S,~N");
Clazz.defineMethod (c$, "setBackgroundImage", 
function (fileName, image) {
this.g.backgroundImageFileName = fileName;
this.gdata.setBackgroundImage (image);
}, "~S,~O");
Clazz.defineMethod (c$, "getObjectColix", 
function (objId) {
var argb = this.g.objColors[objId];
if (argb == 0) return this.getColixBackgroundContrast ();
return JU.C.getColix (argb);
}, "~N");
Clazz.defineMethod (c$, "getFontState", 
function (myType, font3d) {
return this.getStateCreator ().getFontState (myType, font3d);
}, "~S,javajs.awt.Font");
Clazz.overrideMethod (c$, "setColorBackground", 
function (colorName) {
this.setObjectColor ("background", colorName);
}, "~S");
Clazz.overrideMethod (c$, "getBackgroundArgb", 
function () {
return this.g.objColors[(0)];
});
Clazz.defineMethod (c$, "setObjectMad", 
function (iShape, name, mad) {
var objId = JV.StateManager.getObjectIdFromName (name.equalsIgnoreCase ("axes") ? "axis" : name);
if (objId < 0) return;
if (mad == -2 || mad == -4) {
var m = mad + 3;
mad = this.getObjectMad (objId);
if (mad == 0) mad = m;
}this.g.setB ("show" + name, mad != 0);
this.g.objStateOn[objId] = (mad != 0);
if (mad == 0) return;
this.g.objMad[objId] = mad;
this.setShapeSize (iShape, mad, null);
}, "~N,~S,~N");
Clazz.defineMethod (c$, "getObjectMad", 
function (objId) {
return (this.g.objStateOn[objId] ? this.g.objMad[objId] : 0);
}, "~N");
Clazz.defineMethod (c$, "setPropertyColorScheme", 
function (scheme, isTranslucent, isOverloaded) {
this.g.propertyColorScheme = scheme;
if (scheme.startsWith ("translucent ")) {
isTranslucent = true;
scheme = scheme.substring (12).trim ();
}this.cm.setPropertyColorScheme (scheme, isTranslucent, isOverloaded);
}, "~S,~B,~B");
Clazz.defineMethod (c$, "getPropertyColorScheme", 
function () {
return this.g.propertyColorScheme;
});
Clazz.defineMethod (c$, "getColixBackgroundContrast", 
function () {
return this.cm.colixBackgroundContrast;
});
Clazz.defineMethod (c$, "getSpecularState", 
function () {
return this.getStateCreator ().getSpecularState ();
});
Clazz.defineMethod (c$, "getColixAtomPalette", 
function (atom, pid) {
return this.cm.getColixAtomPalette (atom, pid);
}, "JM.Atom,~N");
Clazz.defineMethod (c$, "getColixBondPalette", 
function (bond, pid) {
return this.cm.getColixBondPalette (bond, pid);
}, "JM.Bond,~N");
Clazz.defineMethod (c$, "getColorSchemeList", 
function (colorScheme) {
return this.cm.getColorSchemeList (colorScheme);
}, "~S");
Clazz.defineMethod (c$, "setUserScale", 
function (scale) {
this.cm.setUserScale (scale);
}, "~A");
Clazz.defineMethod (c$, "getColixForPropertyValue", 
function (val) {
return this.cm.getColixForPropertyValue (val);
}, "~N");
Clazz.defineMethod (c$, "getColorPointForPropertyValue", 
function (val) {
return JU.CU.colorPtFromInt (this.gdata.getColorArgbOrGray (this.cm.getColixForPropertyValue (val)), null);
}, "~N");
Clazz.defineMethod (c$, "select", 
function (bs, isGroup, addRemove, isQuiet) {
if (isGroup) bs = this.getUndeletedGroupAtomBits (bs);
this.slm.select (bs, addRemove, isQuiet);
this.shm.setShapeSizeBs (1, 2147483647, null, null);
}, "JU.BS,~B,~N,~B");
Clazz.overrideMethod (c$, "setSelectionSet", 
function (set) {
this.select (set, false, 0, true);
}, "JU.BS");
Clazz.defineMethod (c$, "selectBonds", 
function (bs) {
this.shm.setShapeSizeBs (1, 2147483647, null, bs);
}, "JU.BS");
Clazz.defineMethod (c$, "displayAtoms", 
function (bs, isDisplay, isGroup, addRemove, isQuiet) {
if (isGroup) bs = this.getUndeletedGroupAtomBits (bs);
if (isDisplay) this.slm.display (this.ms, bs, addRemove, isQuiet);
 else this.slm.hide (this.ms, bs, addRemove, isQuiet);
}, "JU.BS,~B,~B,~N,~B");
Clazz.defineMethod (c$, "getUndeletedGroupAtomBits", 
 function (bs) {
bs = this.ms.getAtoms (1087373318, bs);
JU.BSUtil.andNot (bs, this.slm.getDeletedAtoms ());
return bs;
}, "JU.BS");
Clazz.defineMethod (c$, "reportSelection", 
function (msg) {
if (this.ms.getSelectionHaloEnabled ()) this.setTainted (true);
if (this.isScriptQueued () || this.g.debugScript) this.scriptStatus (msg);
}, "~S");
Clazz.defineMethod (c$, "clearAtomSets", 
 function () {
this.setSelectionSubset (null);
this.definedAtomSets.clear ();
});
Clazz.defineMethod (c$, "getDefinedAtomSet", 
function (name) {
var o = this.definedAtomSets.get (name.toLowerCase ());
return (Clazz.instanceOf (o, JU.BS) ? o :  new JU.BS ());
}, "~S");
Clazz.overrideMethod (c$, "selectAll", 
function () {
this.slm.selectAll (false);
});
Clazz.defineMethod (c$, "setNoneSelected", 
function (noneSelected) {
this.noneSelected = noneSelected;
}, "~B");
Clazz.defineMethod (c$, "getNoneSelected", 
function () {
return (this.noneSelected ? Boolean.TRUE : Boolean.FALSE);
});
Clazz.overrideMethod (c$, "clearSelection", 
function () {
this.slm.clearSelection (true);
this.g.setB ("hideNotSelected", false);
});
Clazz.defineMethod (c$, "setSelectionSubset", 
function (subset) {
this.slm.setSelectionSubset (subset);
}, "JU.BS");
Clazz.defineMethod (c$, "getSelectionSubset", 
function () {
return this.slm.getSelectionSubset ();
});
Clazz.defineMethod (c$, "invertSelection", 
function () {
this.slm.invertSelection ();
});
Clazz.overrideMethod (c$, "getSelectedAtoms", 
function () {
return this.bsA ();
});
Clazz.defineMethod (c$, "bsA", 
function () {
return this.slm.getSelectedAtoms ();
});
Clazz.defineMethod (c$, "getSelectedAtomsNoSubset", 
function () {
return this.slm.getSelectedAtomsNoSubset ();
});
Clazz.defineMethod (c$, "isAtomSelected", 
function (atomIndex) {
return this.slm.isAtomSelected (atomIndex);
}, "~N");
Clazz.overrideMethod (c$, "getSelectionCount", 
function () {
return this.slm.getSelectionCount ();
});
Clazz.defineMethod (c$, "setFormalCharges", 
function (formalCharge) {
this.ms.setFormalCharges (this.bsA (), formalCharge);
}, "~N");
Clazz.overrideMethod (c$, "addSelectionListener", 
function (listener) {
this.slm.addListener (listener);
}, "J.api.JmolSelectionListener");
Clazz.overrideMethod (c$, "removeSelectionListener", 
function (listener) {
this.slm.addListener (listener);
}, "J.api.JmolSelectionListener");
Clazz.defineMethod (c$, "getAtomBitSetEval", 
function (eval, atomExpression) {
return (this.allowScripting ? this.getScriptManager ().getAtomBitSetEval (eval, atomExpression) :  new JU.BS ());
}, "J.api.JmolScriptEvaluator,~O");
Clazz.defineMethod (c$, "processTwoPointGesture", 
function (touches) {
this.mouse.processTwoPointGesture (touches);
}, "~A");
Clazz.defineMethod (c$, "processMouseEvent", 
function (id, x, y, modifiers, time) {
return this.mouse.processEvent (id, x, y, modifiers, time);
}, "~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "getRubberBandSelection", 
function () {
return (this.haveDisplay ? this.actionManager.getRubberBand () : null);
});
Clazz.defineMethod (c$, "isBound", 
function (action, gesture) {
return (this.haveDisplay && this.actionManager.bnd (action, gesture));
}, "~N,~N");
Clazz.defineMethod (c$, "getCursorX", 
function () {
return (this.haveDisplay ? this.actionManager.getCurrentX () : 0);
});
Clazz.defineMethod (c$, "getCursorY", 
function () {
return (this.haveDisplay ? this.actionManager.getCurrentY () : 0);
});
Clazz.defineMethod (c$, "getDefaultDirectory", 
function () {
return this.g.defaultDirectory;
});
Clazz.defineMethod (c$, "getLocalUrl", 
function (fileName) {
return this.apiPlatform.getLocalUrl (fileName);
}, "~S");
Clazz.overrideMethod (c$, "getBufferedInputStream", 
function (fullPathName) {
return this.fm.getBufferedInputStream (fullPathName);
}, "~S");
Clazz.defineMethod (c$, "getBufferedReaderOrErrorMessageFromName", 
function (name, fullPathNameReturn, isBinary) {
return this.fm.getBufferedReaderOrErrorMessageFromName (name, fullPathNameReturn, isBinary, true);
}, "~S,~A,~B");
Clazz.defineMethod (c$, "setLoadParameters", 
 function (htParams, isAppend) {
if (htParams == null) htParams =  new java.util.Hashtable ();
htParams.put ("vwr", this);
if (this.g.atomTypes.length > 0) htParams.put ("atomTypes", this.g.atomTypes);
if (!htParams.containsKey ("lattice")) htParams.put ("lattice", this.g.ptDefaultLattice);
if (this.g.applySymmetryToBonds) htParams.put ("applySymmetryToBonds", Boolean.TRUE);
if (this.g.pdbGetHeader) htParams.put ("getHeader", Boolean.TRUE);
if (this.g.pdbSequential) htParams.put ("isSequential", Boolean.TRUE);
htParams.put ("stateScriptVersionInt", Integer.$valueOf (this.stateScriptVersionInt));
if (!htParams.containsKey ("filter")) {
var filter = this.g.defaultLoadFilter;
if (filter.length > 0) htParams.put ("filter", filter);
}var merging = (isAppend && !this.g.appendNew && this.getAtomCount () > 0);
htParams.put ("baseAtomIndex", Integer.$valueOf (isAppend ? this.getAtomCount () : 0));
htParams.put ("baseModelIndex", Integer.$valueOf (this.getAtomCount () == 0 ? 0 : this.getModelCount () + (merging ? -1 : 0)));
if (merging) htParams.put ("merging", Boolean.TRUE);
return htParams;
}, "java.util.Map,~B");
Clazz.overrideMethod (c$, "openFileAsyncSpecial", 
function (fileName, flags) {
this.getScriptManager ().openFileAsync (fileName, flags);
}, "~S,~N");
Clazz.overrideMethod (c$, "openFile", 
function (fileName) {
this.zap (true, true, false);
return this.loadModelFromFileRepaint (null, fileName, null, null);
}, "~S");
Clazz.overrideMethod (c$, "openFiles", 
function (fileNames) {
this.zap (true, true, false);
return this.loadModelFromFileRepaint (null, null, fileNames, null);
}, "~A");
Clazz.overrideMethod (c$, "openReader", 
function (fullPathName, fileName, reader) {
this.zap (true, true, false);
return this.loadModelFromFileRepaint (fullPathName, fileName, null, reader);
}, "~S,~S,java.io.Reader");
Clazz.overrideMethod (c$, "openDOM", 
function (DOMNode) {
this.zap (true, true, false);
return this.loadModelFromFileRepaint ("?", "?", null, DOMNode);
}, "~O");
Clazz.defineMethod (c$, "loadModelFromFileRepaint", 
 function (fullPathName, fileName, fileNames, reader) {
var ret = this.loadModelFromFile (fullPathName, fileName, fileNames, reader, false, null, null, null, 0, false);
this.refresh (1, "loadModelFromFileRepaint");
return ret;
}, "~S,~S,~A,~O");
Clazz.defineMethod (c$, "loadModelFromFile", 
function (fullPathName, fileName, fileNames, reader, isAppend, htParams, loadScript, sOptions, tokType, isConcat) {
if (htParams == null) htParams = this.setLoadParameters (null, isAppend);
if (isConcat) htParams.put ("concatenate", Boolean.TRUE);
var atomSetCollection;
var saveInfo = this.fm.getFileInfo ();
if (fileNames != null) {
if (loadScript == null) {
loadScript =  new JU.SB ().append ("load files");
for (var i = 0; i < fileNames.length; i++) loadScript.append (i == 0 || !isConcat ? " " : "+").append ("/*file*/$FILENAME" + (i + 1) + "$");

}if (sOptions.length () > 0) loadScript.append (" /*options*/ ").append (sOptions.toString ());
var timeBegin = System.currentTimeMillis ();
atomSetCollection = this.fm.createAtomSetCollectionFromFiles (fileNames, this.setLoadParameters (htParams, isAppend), isAppend);
var ms = System.currentTimeMillis () - timeBegin;
JU.Logger.info ("openFiles(" + fileNames.length + ") " + ms + " ms");
fileNames = htParams.get ("fullPathNames");
var fileTypes = htParams.get ("fileTypes");
var s = loadScript.toString ();
for (var i = 0; i < fileNames.length; i++) {
var fname = fileNames[i];
if (fileTypes != null && fileTypes[i] != null) fname = fileTypes[i] + "::" + fname;
s = JU.PT.rep (s, "$FILENAME" + (i + 1) + "$", JU.PT.esc (fname.$replace ('\\', '/')));
}
loadScript =  new JU.SB ().append (s);
} else if (reader == null) {
if (loadScript == null) loadScript =  new JU.SB ().append ("load /*file*/$FILENAME$");
atomSetCollection = this.openFileFull (fileName, isAppend, htParams, loadScript);
} else if (Clazz.instanceOf (reader, java.io.Reader)) {
atomSetCollection = this.fm.createAtomSetCollectionFromReader (fullPathName, fileName, reader, htParams);
} else {
atomSetCollection = this.fm.createAtomSetCollectionFromDOM (reader, htParams);
}if (tokType != 0) {
this.fm.setFileInfo (saveInfo);
return this.loadAtomDataAndReturnError (atomSetCollection, tokType);
}if (htParams.containsKey ("isData")) return atomSetCollection;
if (loadScript != null) {
var fname = htParams.get ("fullPathName");
if (fname == null) fname = "";
if (htParams.containsKey ("loadScript")) loadScript = htParams.get ("loadScript");
htParams.put ("loadScript", loadScript =  new JU.SB ().append (JU.PT.rep (loadScript.toString (), "$FILENAME$", JU.PT.esc (fname.$replace ('\\', '/')))));
}return this.createModelSetAndReturnError (atomSetCollection, isAppend, loadScript, htParams);
}, "~S,~S,~A,~O,~B,java.util.Map,JU.SB,JU.SB,~N,~B");
Clazz.defineMethod (c$, "setLigandModel", 
function (key, data) {
if (this.ligandModels == null) this.ligandModels =  new java.util.Hashtable ();
this.ligandModels.put (key, data);
}, "~S,~S");
Clazz.defineMethod (c$, "getLigandModel", 
function (id, prefix, suffix, terminator) {
if (id == null) {
if (this.ligandModelSet != null) {
var e = this.ligandModels.entrySet ().iterator ();
while (e.hasNext ()) {
var entry = e.next ();
if (Clazz.instanceOf (entry.getValue (), Boolean)) e.remove ();
}
}return null;
}var isLigand = prefix.equals ("ligand_");
if (isLigand) id = id.toUpperCase ();
if (this.ligandModelSet == null) this.ligandModelSet =  new java.util.Hashtable ();
this.ligandModelSet.put (id, Boolean.TRUE);
if (this.ligandModels == null) this.ligandModels =  new java.util.Hashtable ();
var isPng = (id.indexOf ("|") > 0);
if (isPng) id = id.substring (id.indexOf ("|") + 1);
var model = (terminator == null ? this.ligandModels.get (id) : null);
var data;
var fname = null;
if (Clazz.instanceOf (model, Boolean)) return null;
if (model == null && (terminator == null || isPng)) model = this.ligandModels.get (id + suffix);
var isError = false;
if (model == null) {
var s;
if (isLigand) {
fname = this.setLoadFormat ("#" + id, '#', false);
if (fname.length == 0) return null;
this.scriptEcho ("fetching " + fname);
s = this.getFileAsString (fname, false);
} else {
s = this.getFileAsString (prefix, false);
var pt = (terminator == null ? -1 : s.indexOf (terminator));
if (pt >= 0) s = s.substring (0, pt);
}isError = (s.indexOf ("java.") == 0);
model = s;
if (!isError) this.ligandModels.put (id + suffix, model);
}if (!isLigand) return model;
if (!isError && Clazz.instanceOf (model, String)) {
data = model;
if (data.length != 0) {
var htParams =  new java.util.Hashtable ();
htParams.put ("modelOnly", Boolean.TRUE);
model = this.getModelAdapter ().getAtomSetCollectionReader ("ligand", null, JU.Rdr.getBR (data), htParams);
isError = (Clazz.instanceOf (model, String));
if (!isError) {
model = this.getModelAdapter ().getAtomSetCollection (model);
isError = (Clazz.instanceOf (model, String));
if (fname != null && !isError) this.scriptEcho (this.getModelAdapter ().getAtomSetCollectionAuxiliaryInfo (model).get ("modelLoadNote"));
}}}if (isError) {
this.scriptEcho (model.toString ());
this.ligandModels.put (id, Boolean.FALSE);
return null;
}return model;
}, "~S,~S,~S,~S");
Clazz.defineMethod (c$, "openFileFull", 
 function (fileName, isAppend, htParams, loadScript) {
if (fileName == null) return null;
if (fileName.indexOf ("[]") >= 0) {
return null;
}var atomSetCollection;
var msg = "openFile(" + fileName + ")";
JU.Logger.startTimer (msg);
htParams = this.setLoadParameters (htParams, isAppend);
var isLoadVariable = fileName.startsWith ("@");
var haveFileData = (htParams.containsKey ("fileData"));
if (fileName.indexOf ('$') == 0) htParams.put ("smilesString", fileName.substring (1));
var isString = (fileName.equalsIgnoreCase ("string") || fileName.equals ("Jmol Model Kit"));
var strModel = null;
if (haveFileData) {
strModel = htParams.get ("fileData");
if (htParams.containsKey ("isData")) {
return this.loadInlineScript (strModel, '\0', isAppend, htParams);
}} else if (isString) {
strModel = this.ms.getInlineData (-1);
if (strModel == null) if (this.g.modelKitMode) strModel = "5\n\nC 0 0 0\nH .63 .63 .63\nH -.63 -.63 .63\nH -.63 .63 -.63\nH .63 -.63 -.63";
 else return "cannot find string data";
if (loadScript != null) htParams.put ("loadScript", loadScript =  new JU.SB ().append (JU.PT.rep (loadScript.toString (), "$FILENAME$", "data \"model inline\"\n" + strModel + "end \"model inline\"")));
}if (strModel != null) {
if (!isAppend) this.zap (true, false, false);
if (!isLoadVariable && (!haveFileData || isString)) this.getStateCreator ().getInlineData (loadScript, strModel, isAppend, this.g.defaultLoadFilter);
atomSetCollection = this.fm.createAtomSetCollectionFromString (strModel, htParams, isAppend);
} else {
atomSetCollection = this.fm.createAtomSetCollectionFromFile (fileName, htParams, isAppend);
}JU.Logger.checkTimer (msg, false);
return atomSetCollection;
}, "~S,~B,java.util.Map,JU.SB");
Clazz.overrideMethod (c$, "openStringInline", 
function (strModel) {
var ret = this.openStringInlineParamsAppend (strModel, null, false);
this.refresh (1, "openStringInline");
return ret;
}, "~S");
Clazz.defineMethod (c$, "loadInline", 
function (strModel) {
return this.loadInlineScriptRepaint (strModel, this.g.inlineNewlineChar, false);
}, "~S");
Clazz.defineMethod (c$, "loadInline", 
function (strModel, newLine) {
return this.loadInlineScriptRepaint (strModel, newLine, false);
}, "~S,~S");
Clazz.overrideMethod (c$, "loadInlineAppend", 
function (strModel, isAppend) {
return this.loadInlineScriptRepaint (strModel, '\0', isAppend);
}, "~S,~B");
Clazz.defineMethod (c$, "loadInlineScriptRepaint", 
 function (strModel, newLine, isAppend) {
var ret = this.loadInlineScript (strModel, newLine, isAppend, null);
this.refresh (1, "loadInlineScript");
return ret;
}, "~S,~S,~B");
Clazz.defineMethod (c$, "loadInline", 
function (arrayModels) {
return this.loadInline (arrayModels, false);
}, "~A");
Clazz.defineMethod (c$, "loadInline", 
function (arrayModels, isAppend) {
if (arrayModels == null || arrayModels.length == 0) return null;
var ret = this.openStringsInlineParamsAppend (arrayModels, null, isAppend);
this.refresh (1, "loadInline String[]");
return ret;
}, "~A,~B");
Clazz.defineMethod (c$, "loadInline", 
function (arrayData, isAppend) {
if (arrayData == null || arrayData.size () == 0) return null;
if (!isAppend) this.zap (true, false, false);
var list =  new JU.Lst ();
for (var i = 0; i < arrayData.size (); i++) list.addLast (arrayData.get (i));

var atomSetCollection = this.fm.createAtomSeCollectionFromArrayData (list, this.setLoadParameters (null, isAppend), isAppend);
var ret = this.createModelSetAndReturnError (atomSetCollection, isAppend, null, null);
this.refresh (1, "loadInline");
return ret;
}, "java.util.List,~B");
Clazz.defineMethod (c$, "loadInlineScript", 
 function (strModel, newLine, isAppend, htParams) {
if (strModel == null || strModel.length == 0) return null;
strModel = JV.Viewer.fixInlineString (strModel, newLine);
if (newLine.charCodeAt (0) != 0) JU.Logger.info ("loading model inline, " + strModel.length + " bytes, with newLine character " + (newLine).charCodeAt (0) + " isAppend=" + isAppend);
if (JU.Logger.debugging) JU.Logger.debug (strModel);
var datasep = this.getDataSeparator ();
var i;
if (datasep != null && datasep !== "" && (i = strModel.indexOf (datasep)) >= 0 && strModel.indexOf ("# Jmol state") < 0) {
var n = 2;
while ((i = strModel.indexOf (datasep, i + 1)) >= 0) n++;

var strModels =  new Array (n);
var pt = 0;
var pt0 = 0;
for (i = 0; i < n; i++) {
pt = strModel.indexOf (datasep, pt0);
if (pt < 0) pt = strModel.length;
strModels[i] = strModel.substring (pt0, pt);
pt0 = pt + datasep.length;
}
return this.openStringsInlineParamsAppend (strModels, htParams, isAppend);
}return this.openStringInlineParamsAppend (strModel, htParams, isAppend);
}, "~S,~S,~B,java.util.Map");
c$.fixInlineString = Clazz.defineMethod (c$, "fixInlineString", 
function (strModel, newLine) {
var i;
if (strModel.indexOf ("\\/n") >= 0) {
strModel = JU.PT.rep (strModel, "\n", "");
strModel = JU.PT.rep (strModel, "\\/n", "\n");
newLine = String.fromCharCode ( 0);
}if (newLine.charCodeAt (0) != 0 && newLine != '\n') {
var repEmpty = (strModel.indexOf ('\n') >= 0);
var len = strModel.length;
for (i = 0; i < len && strModel.charAt (i) == ' '; ++i) {
}
if (i < len && strModel.charAt (i) == newLine) strModel = strModel.substring (i + 1);
if (repEmpty) strModel = JU.PT.rep (strModel, "" + newLine, "");
 else strModel = strModel.$replace (newLine, '\n');
}return strModel;
}, "~S,~S");
Clazz.defineMethod (c$, "openStringInlineParamsAppend", 
function (strModel, htParams, isAppend) {
var br = JU.Rdr.getBR (strModel);
var type = this.getModelAdapter ().getFileTypeName (br);
if (type == null) return "unknown file type";
if (type.equals ("spt")) {
return "cannot open script inline";
}htParams = this.setLoadParameters (htParams, isAppend);
var loadScript = htParams.get ("loadScript");
var isLoadCommand = htParams.containsKey ("isData");
if (loadScript == null) loadScript =  new JU.SB ();
if (!isAppend) this.zap (true, false, false);
if (!isLoadCommand) this.getStateCreator ().getInlineData (loadScript, strModel, isAppend, this.g.defaultLoadFilter);
var atomSetCollection = this.fm.createAtomSetCollectionFromString (strModel, htParams, isAppend);
return this.createModelSetAndReturnError (atomSetCollection, isAppend, loadScript, null);
}, "~S,java.util.Map,~B");
Clazz.defineMethod (c$, "openStringsInlineParamsAppend", 
 function (arrayModels, htParams, isAppend) {
var loadScript =  new JU.SB ();
if (!isAppend) this.zap (true, false, false);
var atomSetCollection = this.fm.createAtomSeCollectionFromStrings (arrayModels, loadScript, this.setLoadParameters (htParams, isAppend), isAppend);
return this.createModelSetAndReturnError (atomSetCollection, isAppend, loadScript, null);
}, "~A,java.util.Map,~B");
Clazz.defineMethod (c$, "getInlineChar", 
function () {
return this.g.inlineNewlineChar;
});
Clazz.defineMethod (c$, "getDataSeparator", 
function () {
return this.g.getParameter ("dataseparator", true);
});
Clazz.defineMethod (c$, "createModelSetAndReturnError", 
 function (atomSetCollection, isAppend, loadScript, htParams) {
var fullPathName = this.fm.getFullPathName (false);
var fileName = this.fm.getFileName ();
var errMsg;
if (loadScript == null) {
this.setBooleanProperty ("preserveState", false);
loadScript =  new JU.SB ().append ("load \"???\"");
}if (Clazz.instanceOf (atomSetCollection, String)) {
errMsg = atomSetCollection;
this.setFileLoadStatus (J.c.FIL.NOT_LOADED, fullPathName, null, null, errMsg, null);
if (this.displayLoadErrors && !isAppend && !errMsg.equals ("#CANCELED#")) this.zapMsg (errMsg);
return errMsg;
}if (isAppend) this.clearAtomSets ();
 else if (this.g.modelKitMode && !fileName.equals ("Jmol Model Kit")) this.setModelKitMode (false);
this.setFileLoadStatus (J.c.FIL.CREATING_MODELSET, fullPathName, fileName, null, null, null);
this.pushHoldRepaintWhy ("createModelSet");
this.setErrorMessage (null, null);
try {
var bsNew =  new JU.BS ();
this.mm.createModelSet (fullPathName, fileName, loadScript, atomSetCollection, bsNew, isAppend);
if (bsNew.cardinality () > 0) {
var jmolScript = this.ms.getInfoM ("jmolscript");
if (this.ms.getMSInfoB ("doMinimize")) this.minimize (2147483647, 0, bsNew, null, 0, true, true, true, true);
 else this.addHydrogens (bsNew, false, true);
if (jmolScript != null) this.ms.getMSInfo ().put ("jmolscript", jmolScript);
}this.initializeModel (isAppend);
} catch (er) {
if (Clazz.exceptionOf (er, Error)) {
this.handleError (er, true);
errMsg = this.getShapeErrorState ();
errMsg = ("ERROR creating model: " + er + (errMsg.length == 0 ? "" : "|" + errMsg));
this.zapMsg (errMsg);
this.setErrorMessage (errMsg, null);
} else {
throw er;
}
}
this.popHoldRepaint ("createModelSet \u0001## REPAINT_IGNORE ##");
errMsg = this.getErrorMessage ();
this.setFileLoadStatus (J.c.FIL.CREATED, fullPathName, fileName, this.getModelSetName (), errMsg, htParams == null ? null : htParams.get ("async"));
if (isAppend) {
this.selectAll ();
this.setTainted (true);
this.axesAreTainted = true;
}atomSetCollection = null;
System.gc ();
return errMsg;
}, "~O,~B,JU.SB,java.util.Map");
Clazz.defineMethod (c$, "loadAtomDataAndReturnError", 
 function (atomSetCollection, tokType) {
if (Clazz.instanceOf (atomSetCollection, String)) return atomSetCollection;
this.setErrorMessage (null, null);
try {
var script = this.mm.createAtomDataSet (atomSetCollection, tokType);
switch (tokType) {
case 1146095626:
if (script != null) this.runScript (script);
break;
case 4166:
this.setStatusFrameChanged (true, true);
break;
case 1649412120:
this.shm.deleteVdwDependentShapes (null);
break;
}
} catch (er) {
if (Clazz.exceptionOf (er, Error)) {
this.handleError (er, true);
var errMsg = this.getShapeErrorState ();
errMsg = ("ERROR adding atom data: " + er + (errMsg.length == 0 ? "" : "|" + errMsg));
this.zapMsg (errMsg);
this.setErrorMessage (errMsg, null);
this.setParallel (false);
} else {
throw er;
}
}
return this.getErrorMessage ();
}, "~O,~N");
Clazz.defineMethod (c$, "getEmbeddedFileState", 
function (filename, allowCached) {
return this.fm.getEmbeddedFileState (filename, allowCached);
}, "~S,~B");
Clazz.overrideMethod (c$, "getFileAsBytes", 
function (pathName, out) {
return this.fm.getFileAsBytes (pathName, out, true);
}, "~S,JU.OC");
Clazz.defineMethod (c$, "getCurrentFileAsString", 
function () {
var filename = this.getFullPathName (false);
if (filename.equals ("string") || filename.equals ("Jmol Model Kit")) return this.ms.getInlineData (this.am.cmi);
if (filename.indexOf ("[]") >= 0) return filename;
if (filename === "JSNode") return "<DOM NODE>";
return this.getFileAsString4 (filename, -1, true, false, false);
});
Clazz.defineMethod (c$, "getFullPathName", 
function (orPrevious) {
return this.fm.getFullPathName (orPrevious);
}, "~B");
Clazz.defineMethod (c$, "getFileName", 
function () {
return this.fm.getFileName ();
});
Clazz.defineMethod (c$, "getFullPathNameOrError", 
function (filename) {
var data =  new Array (2);
this.fm.getFullPathNameOrError (filename, false, data);
return data;
}, "~S");
Clazz.overrideMethod (c$, "getFileAsString", 
function (name, checkProtected) {
return this.getFileAsString4 (name, -1, false, false, checkProtected);
}, "~S,~B");
Clazz.defineMethod (c$, "getFileAsMap", 
function (name) {
return this.fm.getFileAsMap (name);
}, "~S");
Clazz.defineMethod (c$, "getFileAsString4", 
function (name, nBytesMax, doSpecialLoad, allowBinary, checkProtected) {
if (name == null) return this.getCurrentFileAsString ();
var data =  new Array (2);
data[0] = name;
this.fm.getFileDataOrErrorAsString (data, nBytesMax, doSpecialLoad, allowBinary, checkProtected);
return data[1];
}, "~S,~N,~B,~B,~B");
Clazz.defineMethod (c$, "getFileAsStringBin", 
function (data, allowBinary) {
return this.fm.getFileDataOrErrorAsString (data, -1, false, allowBinary, !allowBinary);
}, "~A,~B");
Clazz.defineMethod (c$, "getFilePath", 
function (name, asShortName) {
return this.fm.getFilePath (name, false, asShortName);
}, "~S,~B");
Clazz.defineMethod (c$, "getFileInfo", 
function () {
return this.fm.getFileInfo ();
});
Clazz.defineMethod (c$, "setFileInfo", 
function (fileInfo) {
this.fm.setFileInfo (fileInfo);
}, "~A");
Clazz.defineMethod (c$, "autoCalculate", 
function (tokProperty) {
switch (tokProperty) {
case 1112539151:
this.ms.getSurfaceDistanceMax ();
break;
case 1112539150:
this.ms.calculateStraightness ();
break;
}
}, "~N");
Clazz.defineMethod (c$, "calculateStraightness", 
function () {
this.ms.setHaveStraightness (false);
this.ms.calculateStraightness ();
});
Clazz.defineMethod (c$, "calculateSurface", 
function (bsSelected, envelopeRadius) {
if (bsSelected == null) bsSelected = this.bsA ();
if (envelopeRadius == 3.4028235E38 || envelopeRadius == -1) this.ms.addStateScript ("calculate surfaceDistance " + (envelopeRadius == 3.4028235E38 ? "FROM" : "WITHIN"), null, bsSelected, null, "", false, true);
return this.ms.calculateSurface (bsSelected, envelopeRadius);
}, "JU.BS,~N");
Clazz.defineMethod (c$, "getStructureList", 
function () {
return this.g.getStructureList ();
});
Clazz.defineMethod (c$, "setStructureList", 
function (list, type) {
this.g.setStructureList (list, type);
this.ms.setStructureList (this.getStructureList ());
}, "~A,J.c.STR");
Clazz.defineMethod (c$, "getDefaultStructure", 
function (bsAtoms, bsAllAtoms) {
if (bsAtoms == null) bsAtoms = this.bsA ();
return this.ms.getDefaultStructure (bsAtoms, bsAllAtoms);
}, "JU.BS,JU.BS");
Clazz.defineMethod (c$, "calculateStructures", 
function (bsAtoms, asDSSP, setStructure) {
if (bsAtoms == null) bsAtoms = this.bsA ();
return this.ms.calculateStructures (bsAtoms, asDSSP, !this.am.animationOn, this.g.dsspCalcHydrogen, setStructure);
}, "JU.BS,~B,~B");
Clazz.defineMethod (c$, "getAnnotationParser", 
function () {
return (this.annotationParser == null ? (this.annotationParser = J.api.Interface.getOption ("dssx.AnnotationParser")) : this.annotationParser);
});
Clazz.overrideMethod (c$, "getSelectedAtomIterator", 
function (bsSelected, isGreaterOnly, modelZeroBased, isMultiModel) {
return this.ms.getSelectedAtomIterator (bsSelected, isGreaterOnly, modelZeroBased, false, isMultiModel);
}, "JU.BS,~B,~B,~B");
Clazz.overrideMethod (c$, "setIteratorForAtom", 
function (iterator, atomIndex, distance) {
this.ms.setIteratorForAtom (iterator, -1, atomIndex, distance, null);
}, "J.api.AtomIndexIterator,~N,~N");
Clazz.overrideMethod (c$, "setIteratorForPoint", 
function (iterator, modelIndex, pt, distance) {
this.ms.setIteratorForPoint (iterator, modelIndex, pt, distance);
}, "J.api.AtomIndexIterator,~N,JU.T3,~N");
Clazz.overrideMethod (c$, "fillAtomData", 
function (atomData, mode) {
atomData.programInfo = "Jmol Version " + JV.Viewer.getJmolVersion ();
atomData.fileName = this.getFileName ();
this.ms.fillAtomData (atomData, mode);
}, "J.atomdata.AtomData,~N");
Clazz.defineMethod (c$, "addStateScript", 
function (script, addFrameNumber, postDefinitions) {
return this.ms.addStateScript (script, null, null, null, null, addFrameNumber, postDefinitions);
}, "~S,~B,~B");
Clazz.defineMethod (c$, "getMinimizer", 
function (createNew) {
return (this.minimizer == null && createNew ? (this.minimizer = J.api.Interface.getInterface ("JM.Minimizer")).setProperty ("vwr", this) : this.minimizer);
}, "~B");
Clazz.defineMethod (c$, "getSmilesMatcher", 
function () {
return (this.smilesMatcher == null ? (this.smilesMatcher = J.api.Interface.getInterface ("JS.SmilesMatcher")) : this.smilesMatcher);
});
Clazz.defineMethod (c$, "clearModelDependentObjects", 
function () {
this.setFrameOffsets (null);
this.stopMinimization ();
this.minimizer = null;
this.smilesMatcher = null;
});
Clazz.defineMethod (c$, "zap", 
function (notify, resetUndo, zapModelKit) {
this.clearThreads ();
if (this.ms != null) {
this.ligandModelSet = null;
this.clearModelDependentObjects ();
this.fm.clear ();
this.clearRepaintManager (-1);
this.am.clear ();
this.tm.clear ();
this.slm.clear ();
this.clearAllMeasurements ();
this.clearMinimization ();
this.gdata.clear ();
this.mm.zap ();
if (this.scm != null) this.scm.clear (false);
if (this.nmrCalculation != null) this.getNMRCalculation ().setChemicalShiftReference (null, 0);
if (this.haveDisplay) {
this.mouse.clear ();
this.clearTimeouts ();
this.actionManager.clear ();
}this.stm.clear (this.g);
this.tempArray.clear ();
this.chainMap.clear ();
this.chainList.clear ();
this.cm.clear ();
this.definedAtomSets.clear ();
if (this.dm != null) this.dm.clear ();
if (resetUndo) {
if (zapModelKit && this.g.modelKitMode) {
this.openStringInlineParamsAppend ("5\n\nC 0 0 0\nH .63 .63 .63\nH -.63 -.63 .63\nH -.63 .63 -.63\nH .63 -.63 -.63", null, true);
this.setRotationRadius (5.0, true);
this.setStringProperty ("picking", "assignAtom_C");
this.setStringProperty ("picking", "assignBond_p");
}this.undoClear ();
}System.gc ();
} else {
this.mm.zap ();
}this.initializeModel (false);
if (notify) {
this.setFileLoadStatus (J.c.FIL.ZAPPED, null, (resetUndo ? "resetUndo" : this.getZapName ()), null, null, null);
}if (JU.Logger.debugging) JU.Logger.checkMemory ();
}, "~B,~B,~B");
Clazz.defineMethod (c$, "zapMsg", 
 function (msg) {
this.zap (true, true, false);
this.echoMessage (msg);
}, "~S");
Clazz.defineMethod (c$, "echoMessage", 
function (msg) {
var iShape = 30;
this.shm.loadShape (iShape);
this.setShapeProperty (iShape, "font", this.getFont3D ("SansSerif", "Plain", 9));
this.setShapeProperty (iShape, "target", "error");
this.setShapeProperty (iShape, "text", msg);
}, "~S");
Clazz.defineMethod (c$, "initializeModel", 
 function (isAppend) {
this.clearThreads ();
if (isAppend) {
this.am.initializePointers (1);
return;
}this.reset (true);
this.selectAll ();
this.rotatePrev1 = this.rotateBondIndex = -1;
this.movingSelected = false;
this.noneSelected = false;
this.hoverEnabled = true;
this.tm.setCenter ();
this.am.initializePointers (1);
if (!this.ms.getMSInfoB ("isPyMOL")) {
this.clearAtomSets ();
this.setCurrentModelIndex (0);
}this.setBackgroundModelIndex (-1);
this.setFrankOn (this.getShowFrank ());
this.startHoverWatcher (true);
this.setTainted (true);
this.finalizeTransformParameters ();
}, "~B");
Clazz.defineMethod (c$, "startHoverWatcher", 
function (tf) {
if (tf && this.inMotion || !this.haveDisplay || tf && (!this.hoverEnabled || this.am.animationOn)) return;
this.actionManager.startHoverWatcher (tf);
}, "~B");
Clazz.overrideMethod (c$, "getModelSetPathName", 
function () {
return this.mm.getModelSetPathName ();
});
Clazz.overrideMethod (c$, "getModelSetFileName", 
function () {
return this.mm.getModelSetFileName ();
});
Clazz.defineMethod (c$, "getUnitCellInfoText", 
function () {
var c = this.getCurrentUnitCell ();
return (c == null ? "not applicable" : c.getUnitCellInfo ());
});
Clazz.defineMethod (c$, "getUnitCellInfo", 
function (infoType) {
var symmetry = this.getCurrentUnitCell ();
return (symmetry == null ? NaN : symmetry.getUnitCellInfoType (infoType));
}, "~N");
Clazz.defineMethod (c$, "getV0abc", 
function (def) {
var uc = this.getCurrentUnitCell ();
return (uc == null ? null : uc.getV0abc (def));
}, "~O");
Clazz.defineMethod (c$, "getSymmetryOperation", 
function (symop, pt1, pt2, type) {
return this.ms.getSymTemp (true).getSymmetryInfoString (this.ms, this.am.cmi, symop, pt1, pt2, null, type);
}, "~N,JU.P3,JU.P3,~S");
Clazz.defineMethod (c$, "getSpaceGroupInfo", 
function (spaceGroup) {
return this.ms.getSymTemp (true).getSpaceGroupInfo (this.ms, -1, spaceGroup, 0, null, null, null, null);
}, "~S");
Clazz.defineMethod (c$, "getPolymerPointsAndVectors", 
function (bs, vList) {
this.ms.getPolymerPointsAndVectors (bs, vList, this.g.traceAlpha, this.g.sheetSmoothing);
}, "JU.BS,JU.Lst");
Clazz.overrideMethod (c$, "getModelSetName", 
function () {
return (this.ms == null ? null : this.ms.modelSetName);
});
Clazz.overrideMethod (c$, "getBondCount", 
function () {
return this.ms.bondCount;
});
Clazz.overrideMethod (c$, "haveFrame", 
function () {
return this.haveModelSet ();
});
Clazz.defineMethod (c$, "haveModelSet", 
function () {
return this.ms != null;
});
Clazz.defineMethod (c$, "getHybridizationAndAxes", 
function (atomIndex, z, x, lcaoType) {
return this.ms.getHybridizationAndAxes (atomIndex, 0, z, x, lcaoType, true, true);
}, "~N,JU.V3,JU.V3,~S");
Clazz.defineMethod (c$, "getAllAtoms", 
function () {
return this.getModelUndeletedAtomsBitSet (-1);
});
Clazz.defineMethod (c$, "getModelUndeletedAtomsBitSet", 
function (modelIndex) {
return this.excludeAtoms (this.ms.getModelAtomBitSetIncludingDeleted (modelIndex, true), false);
}, "~N");
Clazz.defineMethod (c$, "getModelUndeletedAtomsBitSetBs", 
function (bsModels) {
return this.excludeAtoms (this.ms.getModelAtomBitSetIncludingDeletedBs (bsModels), false);
}, "JU.BS");
Clazz.defineMethod (c$, "excludeAtoms", 
function (bs, ignoreSubset) {
this.slm.excludeAtoms (bs, ignoreSubset);
return bs;
}, "JU.BS,~B");
Clazz.overrideMethod (c$, "getBoundBoxCenter", 
function () {
return this.ms.getBoundBoxCenter (this.am.cmi);
});
Clazz.defineMethod (c$, "calcBoundBoxDimensions", 
function (bs, scale) {
this.ms.calcBoundBoxDimensions (bs, scale);
this.axesAreTainted = true;
}, "JU.BS,~N");
Clazz.defineMethod (c$, "calcRotationRadius", 
function (center) {
return this.ms.calcRotationRadius (this.am.cmi, center);
}, "JU.P3");
Clazz.overrideMethod (c$, "getBoundBoxCornerVector", 
function () {
return this.ms.getBoundBoxCornerVector ();
});
Clazz.defineMethod (c$, "getBoundBoxCenterX", 
function () {
return Clazz.doubleToInt (this.dimScreen.width / 2);
});
Clazz.defineMethod (c$, "getBoundBoxCenterY", 
function () {
return Clazz.doubleToInt (this.dimScreen.height / 2);
});
Clazz.overrideMethod (c$, "getModelCount", 
function () {
return (this.ms == null ? 0 : this.ms.mc);
});
Clazz.overrideMethod (c$, "getModelSetProperties", 
function () {
return this.ms.getMSProperties ();
});
Clazz.overrideMethod (c$, "getModelSetAuxiliaryInfo", 
function () {
return this.ms.getMSInfo ();
});
Clazz.overrideMethod (c$, "getModelNumber", 
function (modelIndex) {
return (modelIndex < 0 ? modelIndex : this.ms.getModelNumber (modelIndex));
}, "~N");
Clazz.defineMethod (c$, "getModelFileNumber", 
function (modelIndex) {
return (modelIndex < 0 ? 0 : this.ms.getModelFileNumber (modelIndex));
}, "~N");
Clazz.overrideMethod (c$, "getModelNumberDotted", 
function (modelIndex) {
return modelIndex < 0 ? "0" : this.ms == null ? null : this.ms.getModelNumberDotted (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelName", 
function (modelIndex) {
return this.ms == null ? null : this.ms.getModelName (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelProperties", 
function (modelIndex) {
return this.ms.getModelProperties (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelProperty", 
function (modelIndex, propertyName) {
return this.ms.getModelProperty (modelIndex, propertyName);
}, "~N,~S");
Clazz.overrideMethod (c$, "getModelAuxiliaryInfo", 
function (modelIndex) {
return this.ms.getModelAuxiliaryInfo (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelAuxiliaryInfoValue", 
function (modelIndex, keyName) {
return this.ms.getInfo (modelIndex, keyName);
}, "~N,~S");
Clazz.overrideMethod (c$, "modelGetLastVibrationIndex", 
function (modelIndex, tok) {
return this.ms.getLastVibrationVector (modelIndex, tok);
}, "~N,~N");
Clazz.overrideMethod (c$, "modelHasVibrationVectors", 
function (modelIndex) {
return (this.ms.getLastVibrationVector (modelIndex, 4166) >= 0);
}, "~N");
Clazz.overrideMethod (c$, "getChainCount", 
function () {
return this.ms.getChainCount (true);
});
Clazz.overrideMethod (c$, "getChainCountInModel", 
function (modelIndex) {
return this.ms.getChainCountInModelWater (modelIndex, false);
}, "~N");
Clazz.overrideMethod (c$, "getGroupCount", 
function () {
return this.ms.getGroupCount ();
});
Clazz.overrideMethod (c$, "getGroupCountInModel", 
function (modelIndex) {
return this.ms.getGroupCountInModel (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getPolymerCount", 
function () {
return this.ms.getBioPolymerCount ();
});
Clazz.overrideMethod (c$, "getPolymerCountInModel", 
function (modelIndex) {
return this.ms.getBioPolymerCountInModel (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getAtomCount", 
function () {
return this.ms.getAtomCount ();
});
Clazz.overrideMethod (c$, "getAtomCountInModel", 
function (modelIndex) {
return this.ms.getAtomCountInModel (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getBondCountInModel", 
function (modelIndex) {
return this.ms.getBondCountInModel (modelIndex);
}, "~N");
Clazz.defineMethod (c$, "getBondsForSelectedAtoms", 
function (bsAtoms) {
return this.ms.getBondsForSelectedAtoms (bsAtoms, this.g.bondModeOr || JU.BSUtil.cardinalityOf (bsAtoms) == 1);
}, "JU.BS");
Clazz.defineMethod (c$, "frankClicked", 
function (x, y) {
return !this.g.disablePopupMenu && this.getShowFrank () && this.shm.checkFrankclicked (x, y);
}, "~N,~N");
Clazz.defineMethod (c$, "frankClickedModelKit", 
function (x, y) {
return !this.g.disablePopupMenu && this.g.modelKitMode && x >= 0 && y >= 0 && x < 40 && y < 80;
}, "~N,~N");
Clazz.overrideMethod (c$, "findNearestAtomIndex", 
function (x, y) {
return this.findNearestAtomIndexMovable (x, y, false);
}, "~N,~N");
Clazz.defineMethod (c$, "findNearestAtomIndexMovable", 
function (x, y, mustBeMovable) {
return (this.ms == null || !this.g.atomPicking ? -1 : this.ms.findNearestAtomIndex (x, y, mustBeMovable ? this.slm.getMotionFixedAtoms () : null, this.g.minPixelSelRadius));
}, "~N,~N,~B");
Clazz.defineMethod (c$, "toCartesian", 
function (pt, ignoreOffset) {
var unitCell = this.getCurrentUnitCell ();
if (unitCell != null) unitCell.toCartesian (pt, ignoreOffset);
}, "JU.P3,~B");
Clazz.defineMethod (c$, "toFractional", 
function (pt, asAbsolute) {
var unitCell = this.getCurrentUnitCell ();
if (unitCell != null) unitCell.toFractional (pt, asAbsolute);
}, "JU.T3,~B");
Clazz.defineMethod (c$, "toUnitCell", 
function (pt, offset) {
var unitCell = this.getCurrentUnitCell ();
if (unitCell != null) unitCell.toUnitCell (pt, offset);
}, "JU.P3,JU.P3");
Clazz.defineMethod (c$, "setCurrentCage", 
function (isosurfaceId) {
var data = [isosurfaceId, null];
this.shm.getShapePropertyData (24, "unitCell", data);
this.ms.setModelCage (this.am.cmi, data[1]);
}, "~S");
Clazz.defineMethod (c$, "setCurrentCagePts", 
function (originABC, name) {
try {
this.ms.setModelCage (this.am.cmi, originABC == null ? null : J.api.Interface.getSymmetry ().getUnitCell (originABC, false, name));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}, "~A,~S");
Clazz.defineMethod (c$, "addUnitCellOffset", 
function (pt) {
var unitCell = this.getCurrentUnitCell ();
if (unitCell == null) return;
pt.add (unitCell.getCartesianOffset ());
}, "JU.P3");
Clazz.defineMethod (c$, "setAtomData", 
function (type, name, coordinateData, isDefault) {
this.ms.setAtomData (type, name, coordinateData, isDefault);
if (type == 2) this.checkCoordinatesChanged ();
this.refreshMeasures (true);
}, "~N,~S,~S,~B");
Clazz.overrideMethod (c$, "setCenterSelected", 
function () {
this.setCenterBitSet (this.bsA (), true);
});
Clazz.defineMethod (c$, "setApplySymmetryToBonds", 
function (TF) {
this.g.applySymmetryToBonds = TF;
}, "~B");
Clazz.overrideMethod (c$, "setBondTolerance", 
function (bondTolerance) {
this.g.setF ("bondTolerance", bondTolerance);
this.g.bondTolerance = bondTolerance;
}, "~N");
Clazz.overrideMethod (c$, "setMinBondDistance", 
function (minBondDistance) {
this.g.setF ("minBondDistance", minBondDistance);
this.g.minBondDistance = minBondDistance;
}, "~N");
Clazz.defineMethod (c$, "getAtomsNearPt", 
function (distance, coord) {
var bs =  new JU.BS ();
this.ms.getAtomsWithin (distance, coord, bs, -1);
return bs;
}, "~N,JU.P3");
Clazz.defineMethod (c$, "getBranchBitSet", 
function (atomIndex, atomIndexNot, allowCyclic) {
if (atomIndex < 0 || atomIndex >= this.getAtomCount ()) return  new JU.BS ();
return JU.JmolMolecule.getBranchBitSet (this.ms.at, atomIndex, this.getModelUndeletedAtomsBitSet (this.ms.at[atomIndex].mi), null, atomIndexNot, allowCyclic, true);
}, "~N,~N,~B");
Clazz.defineMethod (c$, "getAtomIndexFromAtomNumber", 
function (atomNumber) {
return this.ms.getAtomIndexFromAtomNumber (atomNumber, this.getVisibleFramesBitSet ());
}, "~N");
Clazz.overrideMethod (c$, "getElementsPresentBitSet", 
function (modelIndex) {
return this.ms.getElementsPresentBitSet (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getHeteroList", 
function (modelIndex) {
return this.ms.getHeteroList (modelIndex);
}, "~N");
Clazz.defineMethod (c$, "getFileHeader", 
function () {
return this.ms.getFileHeader (this.am.cmi);
});
Clazz.defineMethod (c$, "getFileData", 
function () {
return this.ms.getFileData (this.am.cmi);
});
Clazz.defineMethod (c$, "getCifData", 
function (modelIndex) {
var name = this.getModelFileName (modelIndex);
var data = this.getFileAsString (name, false);
return (data == null ? null : JU.Rdr.readCifData (JU.Rdr.getBR (data)));
}, "~N");
Clazz.defineMethod (c$, "getPDBHeader", 
function () {
return this.ms.getPDBHeader (this.am.cmi);
});
Clazz.defineMethod (c$, "getStateCreator", 
function () {
if (this.jsc == null) (this.jsc = J.api.Interface.getInterface ("JV.StateCreator")).setViewer (this);
return this.jsc;
});
Clazz.defineMethod (c$, "getWrappedStateScript", 
function () {
return this.getOutputManager ().getWrappedState (null, null, null, null);
});
Clazz.overrideMethod (c$, "getStateInfo", 
function () {
return this.getStateInfo3 (null, 0, 0);
});
Clazz.defineMethod (c$, "getStateInfo3", 
function (type, width, height) {
return (this.g.preserveState ? this.getStateCreator ().getStateScript (type, width, height) : "");
}, "~S,~N,~N");
Clazz.defineMethod (c$, "getStructureState", 
function () {
return this.getStateCreator ().getModelState (null, false, true);
});
Clazz.defineMethod (c$, "getProteinStructureState", 
function () {
return this.ms.getProteinStructureState (this.bsA (), false, false, 3);
});
Clazz.defineMethod (c$, "getCoordinateState", 
function (bsSelected) {
return this.getStateCreator ().getAtomicPropertyState (2, bsSelected);
}, "JU.BS");
Clazz.defineMethod (c$, "setCurrentColorRange", 
function (label) {
var data = this.getDataFloat (label);
var bs = (data == null ? null : (this.getDataManager ().getData (label))[2]);
if (bs != null && this.g.rangeSelected) bs.and (this.bsA ());
this.cm.setPropertyColorRangeData (data, bs);
}, "~S");
Clazz.defineMethod (c$, "setData", 
function (type, data, arrayCount, matchField, matchFieldColumnCount, field, fieldColumnCount) {
this.getDataManager ().setData (type, data, arrayCount, this.getAtomCount (), matchField, matchFieldColumnCount, field, fieldColumnCount);
}, "~S,~A,~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "getData", 
function (type) {
return this.getDataManager ().getData (type);
}, "~S");
Clazz.defineMethod (c$, "getDataFloat", 
function (label) {
return this.getDataManager ().getDataFloatA (label);
}, "~S");
Clazz.defineMethod (c$, "getDataFloat2D", 
function (label) {
return this.getDataManager ().getDataFloat2D (label);
}, "~S");
Clazz.defineMethod (c$, "getDataFloat3D", 
function (label) {
return this.getDataManager ().getDataFloat3D (label);
}, "~S");
Clazz.defineMethod (c$, "getDataFloatAt", 
function (label, atomIndex) {
return this.getDataManager ().getDataFloat (label, atomIndex);
}, "~S,~N");
Clazz.overrideMethod (c$, "getAltLocListInModel", 
function (modelIndex) {
return this.ms.getAltLocListInModel (modelIndex);
}, "~N");
Clazz.defineMethod (c$, "getConformation", 
function (iModel, conformationIndex, doSet) {
return this.ms.getConformation (iModel, conformationIndex, doSet);
}, "~N,~N,~B");
Clazz.defineMethod (c$, "autoHbond", 
function (bsFrom, bsTo, onlyIfHaveCalculated) {
if (bsFrom == null) bsFrom = bsTo = this.bsA ();
return this.ms.autoHbond (bsFrom, bsTo, onlyIfHaveCalculated);
}, "JU.BS,JU.BS,~B");
Clazz.overrideMethod (c$, "havePartialCharges", 
function () {
return this.ms.getPartialCharges () != null;
});
Clazz.defineMethod (c$, "getCurrentUnitCell", 
function () {
if (this.am.cai >= 0) return this.ms.getUnitCellForAtom (this.am.cai);
var m = this.am.cmi;
if (m >= 0) return this.ms.getUnitCell (m);
var models = this.getVisibleFramesBitSet ();
var ucLast = null;
for (var i = models.nextSetBit (0); i >= 0; i = models.nextSetBit (i + 1)) {
var uc = this.ms.getUnitCell (i);
if (uc == null) continue;
if (ucLast == null) {
ucLast = uc;
continue;
}if (!ucLast.unitCellEquals (uc)) return null;
}
return ucLast;
});
Clazz.defineMethod (c$, "getModelUnitCell", 
function (modelIndex) {
return this.ms.getUnitCell (modelIndex);
}, "~N");
Clazz.defineMethod (c$, "getDefaultMeasurementLabel", 
function (nPoints) {
switch (nPoints) {
case 2:
return this.g.defaultDistanceLabel;
case 3:
return this.g.defaultAngleLabel;
default:
return this.g.defaultTorsionLabel;
}
}, "~N");
Clazz.overrideMethod (c$, "getMeasurementCount", 
function () {
var count = this.getShapePropertyAsInt (6, "count");
return count <= 0 ? 0 : count;
});
Clazz.overrideMethod (c$, "getMeasurementStringValue", 
function (i) {
return "" + this.shm.getShapePropertyIndex (6, "stringValue", i);
}, "~N");
Clazz.defineMethod (c$, "getMeasurementInfoAsString", 
function () {
return this.getShapeProperty (6, "infostring");
});
Clazz.overrideMethod (c$, "getMeasurementCountPlusIndices", 
function (i) {
return this.shm.getShapePropertyIndex (6, "countPlusIndices", i);
}, "~N");
Clazz.defineMethod (c$, "setPendingMeasurement", 
function (mp) {
this.shm.loadShape (6);
this.setShapeProperty (6, "pending", mp);
}, "JM.MeasurementPending");
Clazz.defineMethod (c$, "getPendingMeasurement", 
function () {
return this.getShapeProperty (6, "pending");
});
Clazz.defineMethod (c$, "clearAllMeasurements", 
function () {
this.setShapeProperty (6, "clear", null);
});
Clazz.overrideMethod (c$, "clearMeasurements", 
function () {
this.evalString ("measures delete");
});
Clazz.defineMethod (c$, "setAnimation", 
function (tok) {
switch (tok) {
case 1073742098:
this.am.reverseAnimation ();
case 1073742096:
case 4143:
if (!this.am.animationOn) this.am.resumeAnimation ();
return;
case 20487:
if (this.am.animationOn && !this.am.animationPaused) this.am.pauseAnimation ();
return;
case 1073742037:
this.am.setAnimationNext ();
return;
case 1073742108:
this.am.setAnimationPrevious ();
return;
case 1073741942:
case 1073742126:
this.am.rewindAnimation ();
return;
case 1073741993:
this.am.setAnimationLast ();
return;
}
}, "~N");
Clazz.overrideMethod (c$, "setAnimationFps", 
function (fps) {
this.am.setAnimationFps (fps);
}, "~N");
Clazz.defineMethod (c$, "setAnimationMode", 
 function (mode) {
if (mode.equalsIgnoreCase ("once")) {
this.am.setAnimationReplayMode (J.c.ANIM.ONCE, 0, 0);
} else if (mode.equalsIgnoreCase ("loop")) {
this.am.setAnimationReplayMode (J.c.ANIM.LOOP, 1, 1);
} else if (mode.startsWith ("pal")) {
this.am.setAnimationReplayMode (J.c.ANIM.PALINDROME, 1, 1);
}}, "~S");
Clazz.defineMethod (c$, "setAnimationOn", 
function (animationOn) {
var wasAnimating = this.am.animationOn;
if (animationOn == wasAnimating) return;
this.am.setAnimationOn (animationOn);
}, "~B");
Clazz.defineMethod (c$, "setAnimationRange", 
function (modelIndex1, modelIndex2) {
this.am.setAnimationRange (modelIndex1, modelIndex2);
}, "~N,~N");
Clazz.overrideMethod (c$, "getVisibleFramesBitSet", 
function () {
return this.ms.selectDisplayedTrajectories (JU.BSUtil.copy (this.am.bsVisibleModels));
});
Clazz.defineMethod (c$, "defineAtomSets", 
function (info) {
this.definedAtomSets.putAll (info);
}, "java.util.Map");
Clazz.defineMethod (c$, "setAnimDisplay", 
function (bs) {
this.am.setDisplay (bs);
if (!this.am.animationOn) this.am.morph (this.am.currentMorphModel + 1);
}, "JU.BS");
Clazz.defineMethod (c$, "setCurrentModelIndex", 
function (modelIndex) {
if (modelIndex == -2147483648) {
this.prevFrame = -2147483648;
this.setCurrentModelIndexClear (this.am.cmi, true);
return;
}this.am.setModel (modelIndex, true);
}, "~N");
Clazz.defineMethod (c$, "getTrajectoryState", 
function () {
return this.getStateCreator ().getTrajectoryState ();
});
Clazz.defineMethod (c$, "setFrameOffsets", 
function (bsAtoms) {
this.bsFrameOffsets = bsAtoms;
this.tm.setFrameOffsets (this.frameOffsets = this.ms.getFrameOffsets (this.bsFrameOffsets));
}, "JU.BS");
Clazz.defineMethod (c$, "getFrameOffsets", 
function () {
return this.bsFrameOffsets;
});
Clazz.defineMethod (c$, "setCurrentModelIndexClear", 
function (modelIndex, clearBackground) {
this.am.setModel (modelIndex, clearBackground);
}, "~N,~B");
Clazz.overrideMethod (c$, "getDisplayModelIndex", 
function () {
return this.am.cmi;
});
Clazz.defineMethod (c$, "haveFileSet", 
function () {
return (this.getModelCount () > 1 && this.getModelNumber (2147483647) > 2000000);
});
Clazz.defineMethod (c$, "setBackgroundModelIndex", 
function (modelIndex) {
this.am.setBackgroundModelIndex (modelIndex);
this.g.setO ("backgroundModel", this.ms.getModelNumberDotted (modelIndex));
}, "~N");
Clazz.defineMethod (c$, "setFrameVariables", 
function () {
this.g.setO ("animationMode", this.am.animationReplayMode.name ());
this.g.setI ("animationFps", this.am.animationFps);
this.g.setO ("_firstFrame", this.am.getModelSpecial (-1));
this.g.setO ("_lastFrame", this.am.getModelSpecial (1));
this.g.setF ("_animTimeSec", this.am.getAnimRunTimeSeconds ());
this.g.setB ("_animMovie", this.am.isMovie);
});
Clazz.defineMethod (c$, "getInMotion", 
function (includeAnim) {
return (this.inMotion || includeAnim && this.am.animationOn);
}, "~B");
Clazz.overrideMethod (c$, "getMotionEventNumber", 
function () {
return this.motionEventNumber;
});
Clazz.overrideMethod (c$, "setInMotion", 
function (inMotion) {
if ( new Boolean (this.inMotion ^ inMotion).valueOf ()) {
this.inMotion = inMotion;
this.resizeImage (0, 0, false, false, true);
if (inMotion) {
this.startHoverWatcher (false);
++this.motionEventNumber;
} else {
this.startHoverWatcher (true);
this.refresh (3, "vwr setInMotion " + inMotion);
}}}, "~B");
Clazz.defineMethod (c$, "setRefreshing", 
 function (TF) {
this.refreshing = TF;
}, "~B");
Clazz.defineMethod (c$, "getRefreshing", 
function () {
return this.refreshing;
});
Clazz.overrideMethod (c$, "pushHoldRepaint", 
function () {
this.pushHoldRepaintWhy (null);
});
Clazz.defineMethod (c$, "pushHoldRepaintWhy", 
function (why) {
if (this.rm != null) this.rm.pushHoldRepaint (why);
}, "~S");
Clazz.overrideMethod (c$, "popHoldRepaint", 
function (why) {
if (this.rm != null) {
this.rm.popHoldRepaint (why.indexOf ("\u0001## REPAINT_IGNORE ##") < 0, why);
}}, "~S");
Clazz.overrideMethod (c$, "refresh", 
function (mode, strWhy) {
if (this.rm == null || !this.refreshing) return;
if (mode == 6 && this.getInMotion (true)) return;
if (this.isWebGL) {
if (mode == 2 || mode == 7) {
this.tm.finalizeTransformParameters ();
{
if (!this.applet) return;
this.html5Applet._refresh();
}}} else {
if (mode > 0 && mode != 7) this.rm.repaintIfReady ("refresh " + mode + " " + strWhy);
}if (mode == 7) return;
if (mode % 3 != 0 && this.sm.doSync ()) this.sm.setSync (mode == 2 ? strWhy : null);
}, "~N,~S");
Clazz.defineMethod (c$, "requestRepaintAndWait", 
function (why) {
if (!this.haveDisplay || this.rm == null) return;
this.rm.requestRepaintAndWait (why);
this.setSync ();
}, "~S");
Clazz.defineMethod (c$, "clearShapeRenderers", 
function () {
this.clearRepaintManager (-1);
});
Clazz.defineMethod (c$, "isRepaintPending", 
function () {
return (this.rm == null ? false : this.rm.isRepaintPending ());
});
Clazz.overrideMethod (c$, "notifyViewerRepaintDone", 
function () {
if (this.rm != null) this.rm.repaintDone ();
this.am.repaintDone ();
});
Clazz.defineMethod (c$, "areAxesTainted", 
function () {
var TF = this.axesAreTainted;
this.axesAreTainted = false;
return TF;
});
Clazz.defineMethod (c$, "setMaximumSize", 
 function (x) {
this.maximumSize = Math.max (x, 100);
}, "~N");
Clazz.overrideMethod (c$, "setScreenDimension", 
function (width, height) {
height = Math.min (height, this.maximumSize);
width = Math.min (width, this.maximumSize);
if (this.isStereoDouble ()) width = Clazz.doubleToInt ((width + 1) / 2);
if (this.dimScreen.width == width && this.dimScreen.height == height) return;
this.resizeImage (width, height, false, false, true);
}, "~N,~N");
Clazz.defineMethod (c$, "setStereo", 
function (isStereoSlave, gRight) {
this.isStereoSlave = isStereoSlave;
this.gRight = gRight;
}, "~B,~O");
Clazz.defineMethod (c$, "getImageFontScaling", 
function () {
return this.imageFontScaling;
});
Clazz.defineMethod (c$, "resizeImage", 
function (width, height, isImageWrite, isExport, isReset) {
if (!isImageWrite && this.creatingImage) return;
if (!isExport && !isImageWrite) this.setShapeProperty (5, "clearBoxes", null);
this.antialiased = (isReset ? this.g.antialiasDisplay && this.checkMotionRendering (603979786) : isImageWrite && !isExport ? this.g.antialiasImages : false);
this.imageFontScaling = (isReset || width <= 0 ? 1 : Clazz.doubleToInt ((this.g.zoomLarge == (height > width) ? height : width) / this.getScreenDim ())) * (this.antialiased ? 2 : 1);
if (width > 0) {
this.dimScreen.width = width;
this.dimScreen.height = height;
if (!isImageWrite) {
this.g.setI ("_width", width);
this.g.setI ("_height", height);
}} else {
width = (this.dimScreen.width == 0 ? this.dimScreen.width = 500 : this.dimScreen.width);
height = (this.dimScreen.height == 0 ? this.dimScreen.height = 500 : this.dimScreen.height);
}this.tm.setScreenParameters (width, height, isImageWrite || isReset ? this.g.zoomLarge : false, this.antialiased, false, false);
this.gdata.setWindowParameters (width, height, this.antialiased);
if (width > 0 && !isImageWrite) this.setStatusResized (width, height);
}, "~N,~N,~B,~B,~B");
Clazz.overrideMethod (c$, "getScreenWidth", 
function () {
return this.dimScreen.width;
});
Clazz.overrideMethod (c$, "getScreenHeight", 
function () {
return this.dimScreen.height;
});
Clazz.defineMethod (c$, "getScreenDim", 
function () {
return (this.g.zoomLarge == (this.dimScreen.height > this.dimScreen.width) ? this.dimScreen.height : this.dimScreen.width);
});
Clazz.overrideMethod (c$, "generateOutputForExport", 
function (params) {
return (this.$noGraphicsAllowed || this.rm == null ? null : this.getOutputManager ().getOutputFromExport (params));
}, "java.util.Map");
Clazz.defineMethod (c$, "clearRepaintManager", 
 function (iShape) {
if (this.rm != null) this.rm.clear (iShape);
}, "~N");
Clazz.defineMethod (c$, "renderScreenImageStereo", 
function (gLeft, checkStereoSlave, width, height) {
if (this.updateWindow (width, height)) {
if (!checkStereoSlave || this.gRight == null) {
this.getScreenImageBuffer (gLeft, false);
} else {
this.render1 (this.gRight, this.getImage (true, false), 0, 0);
this.render1 (gLeft, this.getImage (false, false), 0, 0);
}}if (this.captureParams != null && Boolean.FALSE !== this.captureParams.get ("captureEnabled")) {
if (System.currentTimeMillis () + 50 > (this.captureParams.get ("endTime")).longValue ()) this.captureParams.put ("captureMode", "end");
this.processWriteOrCapture (this.captureParams);
}this.notifyViewerRepaintDone ();
}, "~O,~B,~N,~N");
Clazz.defineMethod (c$, "updateJS", 
function () {
if (this.isWebGL) {
if (this.jsParams == null) {
this.jsParams =  new java.util.Hashtable ();
this.jsParams.put ("type", "JS");
}if (this.updateWindow (0, 0)) this.render ();
this.notifyViewerRepaintDone ();
} else {
if (this.isStereoSlave) return;
this.renderScreenImageStereo (this.apiPlatform.getGraphics (null), true, 0, 0);
}});
Clazz.defineMethod (c$, "updateJSView", 
 function (imodel, iatom) {
var applet = this.html5Applet;
{
applet && applet._viewSet != null && applet._atomPickedCallback(imodel, iatom);
}}, "~N,~N");
Clazz.defineMethod (c$, "updateWindow", 
 function (width, height) {
if (!this.refreshing || this.creatingImage) return false;
if (this.isTainted || this.tm.slabEnabled) this.setModelVisibility ();
this.isTainted = false;
if (this.rm != null) {
if (width != 0) this.setScreenDimension (width, height);
}return true;
}, "~N,~N");
Clazz.defineMethod (c$, "renderScreenImage", 
function (g, width, height) {
this.renderScreenImageStereo (g, false, width, height);
}, "~O,~N,~N");
Clazz.defineMethod (c$, "getImage", 
 function (isDouble, isImageWrite) {
if (this.isWebGL) return null;
var image = null;
try {
this.beginRendering (isDouble, isImageWrite);
this.render ();
this.gdata.endRendering ();
image = this.gdata.getScreenImage (isImageWrite);
} catch (er) {
if (Clazz.exceptionOf (er, Error)) {
this.gdata.getScreenImage (isImageWrite);
this.handleError (er, false);
this.setErrorMessage ("Error during rendering: " + er, null);
} else {
throw er;
}
}
return image;
}, "~B,~B");
Clazz.defineMethod (c$, "beginRendering", 
 function (isDouble, isImageWrite) {
this.gdata.beginRendering (this.tm.getStereoRotationMatrix (isDouble), this.g.translucent, isImageWrite, !this.checkMotionRendering (603979967));
}, "~B,~B");
Clazz.defineMethod (c$, "render", 
 function () {
if (this.ms == null || !this.mustRender || !this.refreshing && !this.creatingImage || this.rm == null) return;
var antialias2 = this.antialiased && this.g.antialiasTranslucent;
this.finalizeTransformParameters ();
var minMax = this.shm.finalizeAtoms (this.tm.bsSelectedAtoms, this.tm.ptOffset);
this.tm.bsSelectedAtoms = null;
if (this.isWebGL) {
this.rm.renderExport (this.gdata, this.ms, this.jsParams);
this.notifyViewerRepaintDone ();
return;
}this.rm.render (this.gdata, this.ms, true, minMax);
if (this.gdata.setPass2 (antialias2)) {
this.tm.setAntialias (antialias2);
this.rm.render (this.gdata, this.ms, false, null);
this.tm.setAntialias (this.antialiased);
}});
Clazz.defineMethod (c$, "render1", 
 function (graphic, img, x, y) {
if (graphic != null && img != null) {
this.apiPlatform.drawImage (graphic, img, x, y, this.dimScreen.width, this.dimScreen.height);
}this.gdata.releaseScreenImage ();
}, "~O,~O,~N,~N");
Clazz.overrideMethod (c$, "getScreenImageBuffer", 
function (graphic, isImageWrite) {
if (this.isWebGL) return null;
var mergeImages = (graphic == null && this.isStereoDouble ());
var imageBuffer;
if (this.tm.stereoMode.isBiColor ()) {
this.beginRendering (true, isImageWrite);
this.render ();
this.gdata.endRendering ();
this.gdata.snapshotAnaglyphChannelBytes ();
this.beginRendering (false, isImageWrite);
this.render ();
this.gdata.endRendering ();
this.gdata.applyAnaglygh (this.tm.stereoMode, this.tm.stereoColors);
imageBuffer = this.gdata.getScreenImage (isImageWrite);
} else {
imageBuffer = this.getImage (this.isStereoDouble (), isImageWrite);
}var imageBuffer2 = null;
if (mergeImages) {
imageBuffer2 = this.apiPlatform.newBufferedImage (imageBuffer, this.dimScreen.width << 1, this.dimScreen.height);
graphic = this.apiPlatform.getGraphics (imageBuffer2);
}if (graphic != null) {
if (this.isStereoDouble ()) {
this.render1 (graphic, imageBuffer, this.dimScreen.width, 0);
imageBuffer = this.getImage (false, false);
}this.render1 (graphic, imageBuffer, 0, 0);
}return (mergeImages ? imageBuffer2 : imageBuffer);
}, "~O,~B");
Clazz.overrideMethod (c$, "getImageAsBytes", 
function (type, width, height, quality, errMsg) {
return (this.isWebGL ? null : this.getOutputManager ().getImageAsBytes (type, width, height, quality, errMsg));
}, "~S,~N,~N,~N,~A");
Clazz.overrideMethod (c$, "releaseScreenImage", 
function () {
this.gdata.releaseScreenImage ();
});
Clazz.overrideMethod (c$, "evalFile", 
function (strFilename) {
return (this.allowScripting && this.getScriptManager () != null ? this.scm.evalFile (strFilename) : null);
}, "~S");
Clazz.defineMethod (c$, "getInsertedCommand", 
function () {
var s = this.insertedCommand;
this.insertedCommand = "";
if (JU.Logger.debugging && s !== "") JU.Logger.debug ("inserting: " + s);
return s;
});
Clazz.overrideMethod (c$, "script", 
function (strScript) {
return this.evalStringQuietSync (strScript, false, true);
}, "~S");
Clazz.overrideMethod (c$, "evalString", 
function (strScript) {
return this.evalStringQuietSync (strScript, false, true);
}, "~S");
Clazz.overrideMethod (c$, "evalStringQuiet", 
function (strScript) {
return this.evalStringQuietSync (strScript, true, true);
}, "~S");
Clazz.defineMethod (c$, "evalStringQuietSync", 
function (strScript, isQuiet, allowSyncScript) {
return (this.getScriptManager () == null ? null : this.scm.evalStringQuietSync (strScript, isQuiet, allowSyncScript));
}, "~S,~B,~B");
Clazz.defineMethod (c$, "clearScriptQueue", 
function () {
if (this.scm != null) this.scm.clearQueue ();
});
Clazz.defineMethod (c$, "setScriptQueue", 
 function (TF) {
this.g.useScriptQueue = TF;
if (!TF) this.clearScriptQueue ();
}, "~B");
Clazz.overrideMethod (c$, "checkHalt", 
function (str, isInsert) {
return (this.scm != null && this.scm.checkHalt (str, isInsert));
}, "~S,~B");
Clazz.overrideMethod (c$, "scriptWait", 
function (strScript) {
return this.evalWait ("JSON", strScript, "+scriptStarted,+scriptStatus,+scriptEcho,+scriptTerminated");
}, "~S");
Clazz.overrideMethod (c$, "scriptWaitStatus", 
function (strScript, statusList) {
return this.evalWait ("object", strScript, statusList);
}, "~S,~S");
Clazz.defineMethod (c$, "evalWait", 
 function (returnType, strScript, statusList) {
if (this.getScriptManager () == null) return null;
this.scm.waitForQueue ();
var doTranslateTemp = J.i18n.GT.setDoTranslate (false);
var ret = this.evalStringWaitStatusQueued (returnType, strScript, statusList, false, false, false);
J.i18n.GT.setDoTranslate (doTranslateTemp);
return ret;
}, "~S,~S,~S");
Clazz.defineMethod (c$, "evalStringWaitStatusQueued", 
function (returnType, strScript, statusList, isScriptFile, isQuiet, isQueued) {
{
if (strScript.indexOf("JSCONSOLE") == 0) {
this.html5Applet._showInfo(strScript.indexOf("CLOSE")<0); if
(strScript.indexOf("CLEAR") >= 0) this.html5Applet._clearConsole();
return null; }
}return (this.getScriptManager () == null ? null : this.scm.evalStringWaitStatusQueued (returnType, strScript, statusList, isScriptFile, isQuiet, isQueued));
}, "~S,~S,~S,~B,~B,~B");
Clazz.defineMethod (c$, "exitJmol", 
function () {
if (this.$isApplet && !this.isJNLP) return;
if (this.headlessImageParams != null) {
try {
if (this.isHeadless ()) this.outputToFile (this.headlessImageParams);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}if (JU.Logger.debugging) JU.Logger.debug ("exitJmol -- exiting");
System.out.flush ();
System.exit (0);
});
Clazz.defineMethod (c$, "scriptCheckRet", 
 function (strScript, returnContext) {
if (this.getScriptManager () == null) return null;
return this.scm.scriptCheckRet (strScript, returnContext);
}, "~S,~B");
Clazz.overrideMethod (c$, "scriptCheck", 
function (strScript) {
if (this.getScriptManager () == null) return null;
return this.scriptCheckRet (strScript, false);
}, "~S");
Clazz.overrideMethod (c$, "isScriptExecuting", 
function () {
return (this.eval != null && this.eval.isExecuting ());
});
Clazz.overrideMethod (c$, "haltScriptExecution", 
function () {
if (this.eval != null) {
this.eval.haltExecution ();
this.eval.stopScriptThreads ();
}this.setStringPropertyTok ("pathForAllFiles", 545259571, "");
this.clearTimeouts ();
});
Clazz.defineMethod (c$, "pauseScriptExecution", 
function () {
if (this.eval != null) this.eval.pauseExecution (true);
});
Clazz.defineMethod (c$, "resolveDatabaseFormat", 
function (fileName) {
if (JV.Viewer.hasDatabasePrefix (fileName)) fileName = this.setLoadFormat (fileName, fileName.charAt (0), false);
return fileName;
}, "~S");
c$.isDatabaseCode = Clazz.defineMethod (c$, "isDatabaseCode", 
function (ch) {
return (ch == '*' || ch == '$' || ch == '=' || ch == ':');
}, "~S");
c$.hasDatabasePrefix = Clazz.defineMethod (c$, "hasDatabasePrefix", 
function (fileName) {
return (fileName.length != 0 && JV.Viewer.isDatabaseCode (fileName.charAt (0)));
}, "~S");
Clazz.defineMethod (c$, "setLoadFormat", 
function (name, type, withPrefix) {
var format;
var f = name.substring (1);
switch (type) {
case '=':
if (name.startsWith ("==")) {
f = f.substring (1);
type = '#';
} else if (f.indexOf ("/") > 0) {
try {
var pt = f.indexOf ("/");
var database = f.substring (0, pt);
f = this.g.resolveDataBase (database, f.substring (pt + 1));
return (f == null ? name : f);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return name;
} else {
throw e;
}
}
}case '#':
var s = (type == '=' ? this.g.loadFormat : this.g.loadLigandFormat);
if (f.indexOf (".") > 0 && s.indexOf ("%FILE.") >= 0) s = s.substring (0, s.indexOf ("%FILE") + 5);
return JU.Txt.formatStringS (s, "FILE", f);
case '*':
if (name.startsWith ("*dom/")) {
var pt = name.lastIndexOf ("/");
f = name.substring (pt + 1);
format = (pt > 4 ? name.substring (5) : "mappings");
return JU.PT.rep (this.g.resolveDataBase ("map", f), "%TYPE", format);
} else if (name.startsWith ("*val/")) {
var ptv = name.lastIndexOf ("/");
f = name.substring (ptv + 1);
format = (ptv > 4 ? name.substring (5) : "validation/outliers/all");
return JU.PT.rep (this.g.resolveDataBase ("map", f), "%TYPE", format);
}return this.g.resolveDataBase ("pdbe", f);
case ':':
format = this.g.pubChemFormat;
if (f.equals ("")) {
try {
f = "smiles:" + this.getSmiles (this.bsA ());
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}var fl = f.toLowerCase ();
var fi = -2147483648;
try {
fi = Integer.parseInt (f);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
if (fi != -2147483648) {
f = "cid/" + fi;
} else {
if (fl.startsWith ("smiles:")) {
format += "?POST?smiles=" + f.substring (7);
f = "smiles";
} else if (f.startsWith ("cid:") || f.startsWith ("inchikey:") || f.startsWith ("cas:")) {
f = f.$replace (':', '/');
} else {
if (fl.startsWith ("name:")) f = f.substring (5);
f = "name/" + JU.PT.escapeUrl (f);
}}return JU.Txt.formatStringS (format, "FILE", f);
case '$':
if (name.startsWith ("$$")) {
f = f.substring (1);
format = JU.PT.rep (this.g.smilesUrlFormat, "&get3d=True", "");
return JU.Txt.formatStringS (format, "FILE", JU.PT.escapeUrl (f));
}if (name.equals ("$")) try {
f = this.getSmiles (this.bsA ());
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
case 'N':
case '2':
case 'I':
case 'K':
case 'S':
case 'T':
case '/':
f = JU.PT.escapeUrl (f);
switch (type) {
case 'N':
format = this.g.nihResolverFormat + "/names";
break;
case '2':
format = this.g.nihResolverFormat + "/image";
break;
case 'I':
format = this.g.nihResolverFormat + "/stdinchi";
break;
case 'K':
format = this.g.nihResolverFormat + "/inchikey";
break;
case 'S':
format = this.g.nihResolverFormat + "/stdinchikey";
break;
case 'T':
format = this.g.nihResolverFormat + "/stdinchi";
break;
case '/':
format = this.g.nihResolverFormat + "/";
break;
default:
format = this.g.smilesUrlFormat;
break;
}
return (withPrefix ? "MOL3D::" : "") + JU.Txt.formatStringS (format, "FILE", f);
case '_':
var server = JV.FileManager.fixFileNameVariables (this.g.edsUrlFormat, f);
var strCutoff = JV.FileManager.fixFileNameVariables (this.g.edsUrlCutoff, f);
return [server, strCutoff];
}
return f;
}, "~S,~S,~B");
Clazz.defineMethod (c$, "getElectronDensityLoadInfo", 
function () {
return [this.g.edsUrlFormat, this.g.edsUrlCutoff, this.g.edsUrlOptions];
});
Clazz.defineMethod (c$, "getStandardLabelFormat", 
function (type) {
switch (type) {
default:
case 0:
return "%[identify]";
case 1:
return this.g.defaultLabelXYZ;
case 2:
return this.g.defaultLabelPDB;
}
}, "~N");
Clazz.defineMethod (c$, "getAdditionalHydrogens", 
function (bsAtoms, doAll, justCarbon, vConnections) {
if (bsAtoms == null) bsAtoms = this.bsA ();
var nTotal =  Clazz.newIntArray (1, 0);
var pts = this.ms.calculateHydrogens (bsAtoms, nTotal, doAll, justCarbon, vConnections);
var points =  new Array (nTotal[0]);
for (var i = 0, pt = 0; i < pts.length; i++) if (pts[i] != null) for (var j = 0; j < pts[i].length; j++) points[pt++] = pts[i][j];


return points;
}, "JU.BS,~B,~B,JU.Lst");
Clazz.overrideMethod (c$, "setMarBond", 
function (marBond) {
this.g.bondRadiusMilliAngstroms = marBond;
this.g.setI ("bondRadiusMilliAngstroms", marBond);
this.setShapeSize (1, marBond * 2, JU.BSUtil.setAll (this.getAtomCount ()));
}, "~N");
Clazz.defineMethod (c$, "setHoverLabel", 
function (strLabel) {
this.shm.loadShape (34);
this.setShapeProperty (34, "label", strLabel);
this.hoverEnabled = (strLabel != null);
if (!this.hoverEnabled) this.startHoverWatcher (false);
}, "~S");
Clazz.defineMethod (c$, "hoverOn", 
function (atomIndex, isLabel) {
this.setStatusAtomHovered (atomIndex, this.getAtomInfoXYZ (atomIndex, false));
if (!this.hoverEnabled) return;
if (this.g.modelKitMode) {
if (this.isAtomAssignable (atomIndex)) this.highlight (JU.BSUtil.newAndSetBit (atomIndex));
this.refresh (3, "hover on atom");
return;
}if (this.eval != null && this.isScriptExecuting () || atomIndex == this.hoverAtomIndex || this.g.hoverDelayMs == 0) return;
if (!this.slm.isInSelectionSubset (atomIndex)) return;
this.shm.loadShape (34);
if (isLabel && this.ms.at[atomIndex].isVisible (512)) {
this.setShapeProperty (34, "specialLabel", J.i18n.GT._ ("Drag to move label"));
}this.setShapeProperty (34, "text", null);
this.setShapeProperty (34, "target", Integer.$valueOf (atomIndex));
this.hoverText = null;
this.hoverAtomIndex = atomIndex;
this.refresh (3, "hover on atom");
}, "~N,~B");
Clazz.defineMethod (c$, "hoverOnPt", 
function (x, y, text, id, pt) {
if (!this.hoverEnabled) return;
if (this.eval != null && this.isScriptExecuting ()) return;
this.shm.loadShape (34);
this.setShapeProperty (34, "xy", JU.P3i.new3 (x, y, 0));
this.setShapeProperty (34, "target", null);
this.setShapeProperty (34, "specialLabel", null);
this.setShapeProperty (34, "text", text);
this.hoverAtomIndex = -1;
this.hoverText = text;
if (id != null && pt != null) this.setStatusObjectHovered (id, text, pt);
this.refresh (3, "hover on point");
}, "~N,~N,~S,~S,JU.T3");
Clazz.defineMethod (c$, "hoverOff", 
function () {
try {
if (this.g.modelKitMode) this.highlight (null);
if (!this.hoverEnabled) return;
var isHover = (this.hoverText != null || this.hoverAtomIndex >= 0);
if (this.hoverAtomIndex >= 0) {
this.setShapeProperty (34, "target", null);
this.hoverAtomIndex = -1;
}if (this.hoverText != null) {
this.setShapeProperty (34, "text", null);
this.hoverText = null;
}this.setShapeProperty (34, "specialLabel", null);
if (isHover) this.refresh (3, "hover off");
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
});
Clazz.overrideMethod (c$, "setDebugScript", 
function (debugScript) {
this.g.debugScript = debugScript;
this.g.setB ("debugScript", debugScript);
if (this.eval != null) this.eval.setDebugging ();
}, "~B");
Clazz.defineMethod (c$, "clearClickCount", 
function () {
this.setTainted (true);
});
Clazz.defineMethod (c$, "setCursor", 
function (cursor) {
if (this.$isKiosk || this.currentCursor == cursor || this.multiTouch || !this.haveDisplay) return;
this.apiPlatform.setCursor (this.currentCursor = cursor, this.display);
}, "~N");
Clazz.defineMethod (c$, "setPickingMode", 
function (strMode, pickingMode) {
if (!this.haveDisplay) return;
this.showSelected = false;
var option = null;
if (strMode != null) {
var pt = strMode.indexOf ("_");
if (pt >= 0) {
option = strMode.substring (pt + 1);
strMode = strMode.substring (0, pt);
}pickingMode = JV.ActionManager.getPickingMode (strMode);
}if (pickingMode < 0) pickingMode = 1;
this.actionManager.setPickingMode (pickingMode);
this.g.setO ("picking", JV.ActionManager.getPickingModeName (this.actionManager.getAtomPickingMode ()));
if (option == null || option.length == 0) return;
option = Character.toUpperCase (option.charAt (0)) + (option.length == 1 ? "" : option.substring (1, 2));
switch (pickingMode) {
case 32:
this.setAtomPickingOption (option);
break;
case 33:
this.setBondPickingOption (option);
break;
default:
JU.Logger.error ("Bad picking mode: " + strMode + "_" + option);
}
}, "~S,~N");
Clazz.defineMethod (c$, "getPickingMode", 
function () {
return (this.haveDisplay ? this.actionManager.getAtomPickingMode () : 0);
});
Clazz.defineMethod (c$, "setPickingStyle", 
function (style, pickingStyle) {
if (!this.haveDisplay) return;
if (style != null) pickingStyle = JV.ActionManager.getPickingStyleIndex (style);
if (pickingStyle < 0) pickingStyle = 0;
this.actionManager.setPickingStyle (pickingStyle);
this.g.setO ("pickingStyle", JV.ActionManager.getPickingStyleName (this.actionManager.getPickingStyle ()));
}, "~S,~N");
Clazz.defineMethod (c$, "getDrawHover", 
function () {
return this.haveDisplay && this.g.drawHover;
});
Clazz.overrideMethod (c$, "getAtomInfo", 
function (atomOrPointIndex) {
if (this.ptTemp == null) this.ptTemp =  new JU.P3 ();
return (atomOrPointIndex >= 0 ? this.ms.getAtomInfo (atomOrPointIndex, null, this.ptTemp) : this.shm.getShapePropertyIndex (6, "pointInfo", -atomOrPointIndex));
}, "~N");
Clazz.defineMethod (c$, "getAtomInfoXYZ", 
function (atomIndex, useChimeFormat) {
if (this.ptTemp == null) this.ptTemp =  new JU.P3 ();
return this.ms.getAtomInfoXYZ (atomIndex, useChimeFormat, this.ptTemp);
}, "~N,~B");
Clazz.defineMethod (c$, "setSync", 
 function () {
if (this.sm.doSync ()) this.sm.setSync (null);
});
Clazz.overrideMethod (c$, "setJmolCallbackListener", 
function (jmolCallbackListener) {
this.sm.setJmolCallbackListener (jmolCallbackListener);
}, "J.api.JmolCallbackListener");
Clazz.overrideMethod (c$, "setJmolStatusListener", 
function (jmolStatusListener) {
this.sm.setJmolStatusListener (jmolStatusListener, null);
}, "J.api.JmolStatusListener");
Clazz.defineMethod (c$, "getStatusChanged", 
function (statusNameList) {
return (statusNameList == null ? null : this.sm.getStatusChanged (statusNameList));
}, "~S");
Clazz.defineMethod (c$, "menuEnabled", 
function () {
return (!this.g.disablePopupMenu && this.getPopupMenu () != null);
});
Clazz.defineMethod (c$, "popupMenu", 
function (x, y, type) {
if (!this.haveDisplay || !this.refreshing || this.$isPreviewOnly || this.g.disablePopupMenu) return;
switch (type) {
case 'j':
try {
this.getPopupMenu ();
this.jmolpopup.jpiShow (x, y);
} catch (e) {
JU.Logger.info (e.toString ());
this.g.disablePopupMenu = true;
}
break;
case 'a':
case 'b':
case 'm':
if (this.modelkitPopup == null && (this.modelkitPopup = this.apiPlatform.getMenuPopup (null, type)) == null) return;
this.modelkitPopup.jpiShow (x, y);
break;
}
}, "~N,~N,~S");
Clazz.defineMethod (c$, "getMenu", 
function (type) {
this.getPopupMenu ();
if (type.equals ("\0")) {
this.popupMenu (this.dimScreen.width - 120, 0, 'j');
return "OK";
}return (this.jmolpopup == null ? "" : this.jmolpopup.jpiGetMenuAsString ("Jmol version " + JV.Viewer.getJmolVersion () + "|_GET_MENU|" + type));
}, "~S");
Clazz.defineMethod (c$, "getPopupMenu", 
 function () {
if (this.jmolpopup == null) {
this.jmolpopup = (this.allowScripting ? this.apiPlatform.getMenuPopup (this.menuStructure, 'j') : null);
if (this.jmolpopup == null) {
this.g.disablePopupMenu = true;
return null;
}}return this.jmolpopup.jpiGetMenuAsObject ();
});
Clazz.overrideMethod (c$, "setMenu", 
function (fileOrText, isFile) {
if (isFile) JU.Logger.info ("Setting menu " + (fileOrText.length == 0 ? "to Jmol defaults" : "from file " + fileOrText));
if (fileOrText.length == 0) fileOrText = null;
 else if (isFile) fileOrText = this.getFileAsString (fileOrText, false);
this.getProperty ("DATA_API", "setMenu", fileOrText);
this.sm.setCallbackFunction ("menu", fileOrText);
}, "~S,~B");
Clazz.defineMethod (c$, "setStatusFrameChanged", 
function (isVib, doNotify) {
if (isVib) {
this.prevFrame = -2147483648;
}this.tm.setVibrationPeriod (NaN);
var firstIndex = this.am.firstFrameIndex;
var lastIndex = this.am.lastFrameIndex;
var isMovie = this.am.isMovie;
var modelIndex = this.am.cmi;
if (firstIndex == lastIndex && !isMovie) modelIndex = firstIndex;
var frameID = this.getModelFileNumber (modelIndex);
var currentFrame = this.am.cmi;
var fileNo = frameID;
var modelNo = frameID % 1000000;
var firstNo = (isMovie ? firstIndex : this.getModelFileNumber (firstIndex));
var lastNo = (isMovie ? lastIndex : this.getModelFileNumber (lastIndex));
var strModelNo;
if (isMovie) {
strModelNo = "" + (currentFrame + 1);
} else if (fileNo == 0) {
strModelNo = this.getModelNumberDotted (firstIndex);
if (firstIndex != lastIndex) strModelNo += " - " + this.getModelNumberDotted (lastIndex);
if (Clazz.doubleToInt (firstNo / 1000000) == Clazz.doubleToInt (lastNo / 1000000)) fileNo = firstNo;
} else {
strModelNo = this.getModelNumberDotted (modelIndex);
}if (fileNo != 0) fileNo = (fileNo < 1000000 ? 1 : Clazz.doubleToInt (fileNo / 1000000));
if (!isMovie) {
this.g.setI ("_currentFileNumber", fileNo);
this.g.setI ("_currentModelNumberInFile", modelNo);
}var currentMorphModel = this.am.currentMorphModel;
this.g.setI ("_currentFrame", currentFrame);
this.g.setI ("_morphCount", this.am.morphCount);
this.g.setF ("_currentMorphFrame", currentMorphModel);
this.g.setI ("_frameID", frameID);
this.g.setO ("_modelNumber", strModelNo);
this.g.setO ("_modelName", (modelIndex < 0 ? "" : this.getModelName (modelIndex)));
var title = (modelIndex < 0 ? "" : this.getModelTitle (modelIndex));
this.g.setO ("_modelTitle", title == null ? "" : title);
this.g.setO ("_modelFile", (modelIndex < 0 ? "" : this.ms.getModelFileName (modelIndex)));
this.g.setO ("_modelType", (modelIndex < 0 ? "" : this.ms.getModelFileType (modelIndex)));
if (currentFrame == this.prevFrame && currentMorphModel == this.prevMorphModel) return;
this.prevFrame = currentFrame;
this.prevMorphModel = currentMorphModel;
var entryName;
if (isMovie) {
entryName = "" + (currentFrame + 1);
} else {
entryName = this.getModelName (currentFrame);
var script = "" + this.getModelNumberDotted (currentFrame);
if (!entryName.equals (script)) entryName = script + ": " + entryName;
if (entryName.length > 50) entryName = entryName.substring (0, 45) + "...";
}this.sm.setStatusFrameChanged (fileNo, modelNo, (this.am.animationDirection < 0 ? -firstNo : firstNo), (this.am.currentDirection < 0 ? -lastNo : lastNo), currentFrame, currentMorphModel, entryName);
if (this.doHaveJDX ()) this.getJSV ().setModel (modelIndex);
if (this.isJS) this.updateJSView (modelIndex, -1);
}, "~B,~B");
Clazz.defineMethod (c$, "doHaveJDX", 
 function () {
return (this.haveJDX || (this.haveJDX = this.getBooleanProperty ("_JSpecView".toLowerCase ())));
});
Clazz.defineMethod (c$, "getJSV", 
function () {
if (this.jsv == null) {
this.jsv = J.api.Interface.getOption ("jsv.JSpecView");
this.jsv.setViewer (this);
}return this.jsv;
});
Clazz.defineMethod (c$, "getJDXBaseModelIndex", 
function (modelIndex) {
if (!this.doHaveJDX ()) return modelIndex;
return this.getJSV ().getBaseModelIndex (modelIndex);
}, "~N");
Clazz.defineMethod (c$, "getJspecViewProperties", 
function (myParam) {
var o = this.sm.getJspecViewProperties ("" + myParam);
if (o != null) this.haveJDX = true;
return o;
}, "~O");
Clazz.defineMethod (c$, "scriptEcho", 
function (strEcho) {
if (!JU.Logger.isActiveLevel (4)) return;
{
System.out.println(strEcho);
}this.sm.setScriptEcho (strEcho, this.isScriptQueued ());
if (this.listCommands && strEcho != null && strEcho.indexOf ("$[") == 0) JU.Logger.info (strEcho);
}, "~S");
Clazz.defineMethod (c$, "isScriptQueued", 
 function () {
return this.scm != null && this.scm.isScriptQueued ();
});
Clazz.defineMethod (c$, "notifyError", 
function (errType, errMsg, errMsgUntranslated) {
this.g.setO ("_errormessage", errMsgUntranslated);
this.sm.notifyError (errType, errMsg, errMsgUntranslated);
}, "~S,~S,~S");
Clazz.defineMethod (c$, "jsEval", 
function (strEval) {
return this.sm.jsEval (strEval);
}, "~S");
Clazz.defineMethod (c$, "setStatusAtomHovered", 
 function (atomIndex, info) {
this.g.setI ("_atomhovered", atomIndex);
this.g.setO ("hovered", JU.BSUtil.newAndSetBit (atomIndex));
this.sm.setStatusAtomHovered (atomIndex, info);
}, "~N,~S");
Clazz.defineMethod (c$, "setStatusObjectHovered", 
 function (id, info, pt) {
this.g.setO ("_objecthovered", id);
this.sm.setStatusObjectHovered (id, info, pt);
}, "~S,~S,JU.T3");
Clazz.defineMethod (c$, "setFileLoadStatus", 
 function (ptLoad, fullPathName, fileName, modelName, strError, isAsync) {
this.setErrorMessage (strError, null);
this.g.setI ("_loadPoint", ptLoad.getCode ());
var doCallback = (ptLoad !== J.c.FIL.CREATING_MODELSET);
if (doCallback) this.setStatusFrameChanged (false, false);
this.sm.setFileLoadStatus (fullPathName, fileName, modelName, strError, ptLoad.getCode (), doCallback, isAsync);
if (doCallback) {
if (this.doHaveJDX ()) this.getJSV ().setModel (this.am.cmi);
if (this.isJS) this.updateJSView (this.am.cmi, -2);
}}, "J.c.FIL,~S,~S,~S,~S,Boolean");
Clazz.defineMethod (c$, "getZapName", 
function () {
return (this.g.modelKitMode ? "Jmol Model Kit" : "zapped");
});
Clazz.defineMethod (c$, "setStatusMeasuring", 
function (status, intInfo, strMeasure, value) {
this.sm.setStatusMeasuring (status, intInfo, strMeasure, value);
}, "~S,~N,~S,~N");
Clazz.defineMethod (c$, "notifyMinimizationStatus", 
function () {
var step = this.getP ("_minimizationStep");
var ff = this.getP ("_minimizationForceField");
this.sm.notifyMinimizationStatus (this.getP ("_minimizationStatus"), Clazz.instanceOf (step, String) ? Integer.$valueOf (0) : step, this.getP ("_minimizationEnergy"), (step.toString ().equals ("0") ? Float.$valueOf (0) : this.getP ("_minimizationEnergyDiff")), ff);
});
Clazz.defineMethod (c$, "setStatusAtomPicked", 
function (atomIndex, info, map) {
if (info == null) {
info = this.g.pickLabel;
info = (info.length == 0 ? this.getAtomInfoXYZ (atomIndex, this.g.messageStyleChime) : this.ms.getAtomInfo (atomIndex, info, this.ptTemp));
}this.g.setPicked (atomIndex);
this.g.setO ("_pickinfo", info);
this.sm.setStatusAtomPicked (atomIndex, info, map);
if (atomIndex < 0) return;
var syncMode = this.sm.getSyncMode ();
if (syncMode == 1 && this.doHaveJDX ()) this.getJSV ().atomPicked (atomIndex);
if (this.isJS) this.updateJSView (this.getAtomModelIndex (atomIndex), atomIndex);
}, "~N,~S,java.util.Map");
Clazz.defineMethod (c$, "setStatusDragDropped", 
function (mode, x, y, fileName) {
if (mode == 0) {
this.g.setO ("_fileDropped", fileName);
this.g.setB ("doDrop", true);
}var handled = this.sm.setStatusDragDropped (mode, x, y, fileName);
return (!handled || this.getP ("doDrop").toString ().equals ("true"));
}, "~N,~N,~N,~S");
Clazz.defineMethod (c$, "setStatusResized", 
function (width, height) {
this.sm.setStatusResized (width, height);
}, "~N,~N");
Clazz.defineMethod (c$, "scriptStatus", 
function (strStatus) {
this.setScriptStatus (strStatus, "", 0, null);
}, "~S");
Clazz.defineMethod (c$, "scriptStatusMsg", 
function (strStatus, statusMessage) {
this.setScriptStatus (strStatus, statusMessage, 0, null);
}, "~S,~S");
Clazz.defineMethod (c$, "setScriptStatus", 
function (strStatus, statusMessage, msWalltime, strErrorMessageUntranslated) {
this.sm.setScriptStatus (strStatus, statusMessage, msWalltime, strErrorMessageUntranslated);
}, "~S,~S,~N,~S");
Clazz.defineMethod (c$, "getModelTitle", 
 function (modelIndex) {
return this.ms == null ? null : this.ms.getModelTitle (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelFileName", 
function (modelIndex) {
return this.ms == null ? null : this.ms.getModelFileName (modelIndex);
}, "~N");
Clazz.defineMethod (c$, "dialogAsk", 
function (type, fileName) {
{
return prompt(type, fileName);
}}, "~S,~S");
Clazz.overrideMethod (c$, "showUrl", 
function (urlString) {
if (urlString == null) return;
if (urlString.indexOf (":") < 0) {
var base = this.fm.getAppletDocumentBase ();
if (base === "") base = this.fm.getFullPathName (false);
if (base.indexOf ("/") >= 0) {
base = base.substring (0, base.lastIndexOf ("/") + 1);
} else if (base.indexOf ("\\") >= 0) {
base = base.substring (0, base.lastIndexOf ("\\") + 1);
}urlString = base + urlString;
}JU.Logger.info ("showUrl:" + urlString);
this.sm.showUrl (urlString);
}, "~S");
Clazz.defineMethod (c$, "setMeshCreator", 
function (meshCreator) {
this.shm.loadShape (24);
this.setShapeProperty (24, "meshCreator", meshCreator);
}, "~O");
Clazz.defineMethod (c$, "showConsole", 
function (showConsole) {
if (!this.haveDisplay) return;
try {
if (this.appConsole == null && showConsole) this.getProperty ("DATA_API", "getAppConsole", Boolean.TRUE);
this.appConsole.setVisible (true);
} catch (e) {
}
}, "~B");
Clazz.overrideMethod (c$, "getParameter", 
function (key) {
return this.getP (key);
}, "~S");
Clazz.defineMethod (c$, "getP", 
function (key) {
return this.g.getParameter (key, true);
}, "~S");
Clazz.defineMethod (c$, "getPOrNull", 
function (key) {
return this.g.getParameter (key, false);
}, "~S");
Clazz.defineMethod (c$, "unsetProperty", 
function (key) {
key = key.toLowerCase ();
if (key.equals ("all") || key.equals ("variables")) this.fm.setPathForAllFiles ("");
this.g.unsetUserVariable (key);
}, "~S");
Clazz.overrideMethod (c$, "notifyStatusReady", 
function (isReady) {
System.out.println ("Jmol applet " + this.fullName + (isReady ? " ready" : " destroyed"));
if (!isReady) this.dispose ();
this.sm.setStatusAppletReady (this.fullName, isReady);
}, "~B");
Clazz.overrideMethod (c$, "getBooleanProperty", 
function (key) {
key = key.toLowerCase ();
if (this.g.htBooleanParameterFlags.containsKey (key)) return this.g.htBooleanParameterFlags.get (key).booleanValue ();
if (key.endsWith ("p!")) {
if (this.actionManager == null) return false;
var s = this.actionManager.getPickingState ().toLowerCase ();
key = key.substring (0, key.length - 2) + ";";
return (s.indexOf (key) >= 0);
}if (key.equalsIgnoreCase ("executionPaused")) return (this.eval != null && this.eval.isPaused ());
if (key.equalsIgnoreCase ("executionStepping")) return (this.eval != null && this.eval.isStepping ());
if (key.equalsIgnoreCase ("haveBFactors")) return (this.ms.getBFactors () != null);
if (key.equalsIgnoreCase ("colorRasmol")) return this.cm.getDefaultColorRasmol ();
if (key.equalsIgnoreCase ("frank")) return this.getShowFrank ();
if (key.equalsIgnoreCase ("spinOn")) return this.tm.spinOn;
if (key.equalsIgnoreCase ("isNavigating")) return this.tm.isNavigating ();
if (key.equalsIgnoreCase ("showSelections")) return this.ms.getSelectionHaloEnabled ();
if (this.g.htUserVariables.containsKey (key)) {
var t = this.g.getUserVariable (key);
if (t.tok == 1048589) return true;
if (t.tok == 1048588) return false;
}JU.Logger.error ("vwr.getBooleanProperty(" + key + ") - unrecognized");
return false;
}, "~S");
Clazz.overrideMethod (c$, "getInt", 
function (tok) {
switch (tok) {
case 553648132:
return this.am.animationFps;
case 553648143:
return this.g.dotDensity;
case 553648144:
return this.g.dotScale;
case 553648146:
return this.g.helixStep;
case 553648151:
return this.g.meshScale;
case 553648153:
return this.g.minPixelSelRadius;
case 553648154:
return this.g.percentVdwAtom;
case 553648157:
return this.g.pickingSpinRate;
case 553648166:
return this.g.ribbonAspectRatio;
case 536870922:
return this.g.scriptDelay;
case 553648170:
return this.g.smallMoleculeMaxAtoms;
case 553648184:
return this.g.strutSpacing;
}
JU.Logger.error ("viewer.getInt(" + JS.T.nameOf (tok) + ") - not listed");
return 0;
}, "~N");
Clazz.defineMethod (c$, "getDelayMaximumMs", 
function () {
return (this.haveDisplay ? this.g.delayMaximumMs : 1);
});
Clazz.defineMethod (c$, "getHermiteLevel", 
function () {
return (this.tm.spinOn ? 0 : this.g.hermiteLevel);
});
Clazz.defineMethod (c$, "getHoverDelay", 
function () {
return (this.g.modelKitMode ? 20 : this.g.hoverDelayMs);
});
Clazz.overrideMethod (c$, "getBoolean", 
function (tok) {
switch (tok) {
case 1074790662:
return this.ms.getMSInfoB ("isPDB");
case 603979780:
return this.g.allowGestures;
case 603979784:
return this.g.allowMultiTouch;
case 603979785:
return this.g.allowRotateSelected;
case 603979792:
return this.g.appendNew;
case 603979794:
return this.g.applySymmetryToBonds;
case 603979796:
return this.g.atomPicking;
case 603979798:
return this.g.autoBond;
case 603979800:
return this.g.autoFps;
case 603979806:
return this.g.axesOrientationRasmol;
case 603979811:
return this.g.backboneSteps;
case 603979812:
return this.g.bondModeOr;
case 603979816:
return this.g.cartoonBaseEdges;
case 603979817:
return this.g.cartoonFancy;
case 603979818:
return this.g.cartoonLadders;
case 603979819:
return this.g.cartoonRibose;
case 603979820:
return this.g.cartoonRockets;
case 603979823:
return this.g.chainCaseSensitive || this.chainList.size () > 0;
case 603979825:
return this.g.debugScript;
case 603979826:
return this.g.defaultStructureDSSP;
case 603979827:
return this.g.disablePopupMenu;
case 603979828:
return this.g.displayCellParameters;
case 603979830:
return this.g.dotSurface;
case 603979829:
return this.g.dotsSelectedOnly;
case 603979833:
return this.g.drawPicking;
case 603979844:
return this.g.fontCaching;
case 603979845:
return this.g.fontScaling;
case 603979846:
return this.g.forceAutoBond;
case 603979848:
return false;
case 603979850:
return this.g.greyscaleRendering;
case 603979852:
return this.g.hbondsBackbone;
case 603979853:
return this.g.hbondsRasmol;
case 603979854:
return this.g.hbondsSolid;
case 1613758470:
return this.g.rasmolHeteroSetting;
case 603979858:
return this.g.hideNameInPopup;
case 603979864:
return this.g.highResolutionFlag;
case 1613758476:
return this.g.rasmolHydrogenSetting;
case 603979870:
return this.g.isosurfaceKey;
case 603979872:
return this.g.justifyMeasurements;
case 603979874:
return this.g.legacyAutoBonding;
case 603979875:
return this.g.legacyHAddition;
case 603979877:
return this.g.logGestures;
case 603979878:
return this.g.measureAllModels;
case 603979879:
return this.g.measurementLabels;
case 603979880:
return this.g.messageStyleChime;
case 603979883:
return this.g.modelKitMode;
case 603979887:
return this.g.navigationMode;
case 603979888:
return this.g.navigationPeriodic;
case 603979889:
return this.g.partialDots;
case 603979892:
return this.g.pdbSequential;
case 603979894:
return this.g.preserveState;
case 603979898:
return this.g.ribbonBorder;
case 603979900:
return this.g.rocketBarrels;
case 603979906:
return this.g.selectAllModels;
case 603979920:
return this.g.showHiddenSelectionHalos;
case 603979922:
return this.g.showHydrogens;
case 603979926:
return this.g.showMeasurements;
case 603979928:
return this.g.showMultipleBonds;
case 603979934:
return this.g.showTiming;
case 603979938:
return this.g.showUnitCellInfo;
case 603979937:
return this.g.showUnitCellDetails;
case 603979939:
return this.g.slabByAtom;
case 603979940:
return this.g.slabByMolecule;
case 603979944:
return this.g.smartAromatic;
case 1613758488:
return this.g.solventOn;
case 603979952:
return this.g.ssbondsBackbone;
case 603979955:
return this.g.strutsMultiple;
case 603979966:
return this.g.traceAlpha;
case 603979967:
return this.g.translucent;
case 603979968:
return this.g.twistedSheets;
case 603979972:
return this.g.vectorsCentered;
case 603979973:
return this.g.vectorSymmetry;
case 603979974:
return this.g.waitForMoveTo;
case 603979978:
return this.g.zeroBasedXyzRasmol;
}
JU.Logger.error ("viewer.getBoolean(" + JS.T.nameOf (tok) + ") - not listed");
return false;
}, "~N");
Clazz.defineMethod (c$, "allowEmbeddedScripts", 
function () {
return (this.g.allowEmbeddedScripts && !this.$isPreviewOnly);
});
Clazz.defineMethod (c$, "getDragSelected", 
function () {
return (this.g.dragSelected && !this.g.modelKitMode);
});
Clazz.defineMethod (c$, "getBondPicking", 
function () {
return (this.g.bondPicking || this.g.modelKitMode);
});
Clazz.defineMethod (c$, "useMinimizationThread", 
function () {
return (this.g.useMinimizationThread && !this.autoExit);
});
Clazz.overrideMethod (c$, "getFloat", 
function (tok) {
switch (tok) {
case 1141899265:
return this.g.particleRadius;
case 570425346:
return this.g.axesScale;
case 570425348:
return this.g.bondTolerance;
case 570425354:
return this.g.defaultTranslucent;
case 570425352:
return this.g.defaultDrawArrowScale;
case 570425355:
return this.g.dipoleScale;
case 570425356:
return this.g.drawFontSize;
case 570425358:
return this.g.exportScale;
case 570425360:
return this.g.hbondsAngleMinimum;
case 570425361:
return this.g.hbondsDistanceMaximum;
case 570425363:
return this.g.loadAtomDataTolerance;
case 570425364:
return this.g.minBondDistance;
case 1276121113:
return this.g.modulationScale;
case 570425370:
return this.g.multipleBondSpacing;
case 570425369:
return this.g.multipleBondRadiusFactor;
case 570425374:
return this.g.navigationSpeed;
case 570425382:
return this.g.pointGroupDistanceTolerance;
case 570425384:
return this.g.pointGroupLinearTolerance;
case 570425388:
return this.tm.getRotationRadius ();
case 570425392:
return this.g.sheetSmoothing;
case 570425394:
return this.g.solventProbeRadius;
case 570425403:
return this.g.starWidth;
case 570425406:
return this.g.strutDefaultRadius;
case 570425408:
return this.g.strutLengthMaximum;
case 1649410049:
return this.g.vectorScale;
case 570425412:
return this.g.vibrationPeriod;
}
JU.Logger.error ("viewer.getFloat(" + JS.T.nameOf (tok) + ") - not listed");
return 0;
}, "~N");
Clazz.overrideMethod (c$, "setStringProperty", 
function (key, value) {
if (value == null) return;
if (key.charAt (0) == '_') {
this.g.setO (key, value);
return;
}var tok = JS.T.getTokFromName (key);
switch (JS.T.getParamType (tok)) {
case 603979776:
this.setBooleanPropertyTok (key, tok, JS.SV.newV (4, value).asBoolean ());
break;
case 553648128:
this.setIntPropertyTok (key, tok, JS.SV.newV (4, value).asInt ());
break;
case 570425344:
this.setFloatPropertyTok (key, tok, JU.PT.parseFloat (value));
break;
default:
this.setStringPropertyTok (key, tok, value);
}
}, "~S,~S");
Clazz.defineMethod (c$, "setStringPropertyTok", 
 function (key, tok, value) {
switch (tok) {
case 545259521:
this.setAnimationMode (value);
return;
case 545259569:
this.g.nmrPredictFormat = value;
break;
case 545259548:
this.g.defaultDropScript = value;
break;
case 545259571:
value = this.fm.setPathForAllFiles (value);
break;
case 545259558:
this.setUnits (value, false);
return;
case 545259560:
this.g.forceField = value = ("UFF".equalsIgnoreCase (value) ? "UFF" : "MMFF");
this.minimizer = null;
break;
case 545259570:
this.g.nmrUrlFormat = value;
break;
case 545259568:
this.setUnits (value, true);
return;
case 545259566:
this.g.loadLigandFormat = value;
break;
case 545259543:
this.g.defaultLabelPDB = value;
break;
case 545259544:
this.g.defaultLabelXYZ = value;
break;
case 545259549:
this.g.defaultLoadFilter = value;
break;
case 545259567:
value = this.getOutputManager ().setLogFile (value);
if (value == null) return;
break;
case 545259559:
break;
case 545259524:
this.g.atomTypes = value;
break;
case 545259538:
break;
case 545259576:
this.g.pickLabel = value;
break;
case 545259580:
if (value.length == 2 && value.startsWith ("R")) this.g.quaternionFrame = value.substring (0, 2);
 else this.g.quaternionFrame = "" + (value.toLowerCase () + "p").charAt (0);
if (!JU.PT.isOneOf (this.g.quaternionFrame, "RC;RP;a;b;c;n;p;q;x;")) this.g.quaternionFrame = "p";
this.ms.setHaveStraightness (false);
break;
case 545259555:
this.setVdwStr (value);
return;
case 545259564:
 new J.i18n.GT (this, value);
var language = J.i18n.GT.getLanguage ();
this.modelkitPopup = null;
if (this.jmolpopup != null) {
this.jmolpopup.jpiDispose ();
this.jmolpopup = null;
this.getPopupMenu ();
}this.sm.setCallbackFunction ("language", language);
value = J.i18n.GT.getLanguage ();
break;
case 545259565:
this.g.loadFormat = value;
break;
case 545259534:
this.setObjectColor ("background", value);
return;
case 545259528:
this.setObjectColor ("axis1", value);
return;
case 545259530:
this.setObjectColor ("axis2", value);
return;
case 545259532:
this.setObjectColor ("axis3", value);
return;
case 545259536:
this.setObjectColor ("boundbox", value);
return;
case 545259586:
this.setObjectColor ("unitcell", value);
return;
case 545259578:
this.setPropertyColorScheme (value, false, false);
break;
case 545259562:
this.shm.loadShape (34);
this.setShapeProperty (34, "atomLabel", value);
break;
case 545259547:
this.g.defaultDistanceLabel = value;
break;
case 545259542:
this.g.defaultAngleLabel = value;
break;
case 545259554:
this.g.defaultTorsionLabel = value;
break;
case 545259550:
this.g.defaultLoadScript = value;
break;
case 545259522:
this.fm.setAppletProxy (value);
break;
case 545259546:
if (value == null) value = "";
value = value.$replace ('\\', '/');
this.g.defaultDirectory = value;
break;
case 545259561:
this.g.helpPath = value;
break;
case 545259552:
if (!value.equalsIgnoreCase ("RasMol") && !value.equalsIgnoreCase ("PyMOL")) value = "Jmol";
this.setDefaultsType (value);
break;
case 545259545:
this.setDefaultColors (value.equalsIgnoreCase ("rasmol"));
return;
case 545259572:
this.setPickingMode (value, 0);
return;
case 545259574:
this.setPickingStyle (value, 0);
return;
case 545259540:
break;
default:
if (key.toLowerCase ().endsWith ("callback")) {
this.sm.setCallbackFunction (key, (value.length == 0 || value.equalsIgnoreCase ("none") ? null : value));
break;
}if (!this.g.htNonbooleanParameterValues.containsKey (key.toLowerCase ())) {
this.g.setUserVariable (key, JS.SV.newV (4, value));
return;
}break;
}
this.g.setO (key, value);
}, "~S,~N,~S");
Clazz.overrideMethod (c$, "setFloatProperty", 
function (key, value) {
if (Float.isNaN (value)) return;
if (key.charAt (0) == '_') {
this.g.setF (key, value);
return;
}var tok = JS.T.getTokFromName (key);
switch (JS.T.getParamType (tok)) {
case 545259520:
this.setStringPropertyTok (key, tok, "" + value);
break;
case 603979776:
this.setBooleanPropertyTok (key, tok, value != 0);
break;
case 553648128:
this.setIntPropertyTok (key, tok, Clazz.floatToInt (value));
break;
default:
this.setFloatPropertyTok (key, tok, value);
}
}, "~S,~N");
Clazz.defineMethod (c$, "setFloatPropertyTok", 
 function (key, tok, value) {
switch (tok) {
case 570425366:
this.ms.setModulation (null, false, null, false);
this.g.modulationScale = value = Math.max (0.1, value);
this.ms.setModulation (null, true, null, false);
break;
case 570425381:
this.g.particleRadius = Math.abs (value);
break;
case 570425356:
this.g.drawFontSize = value;
break;
case 570425358:
this.g.exportScale = value;
break;
case 570425403:
this.g.starWidth = value;
break;
case 570425369:
this.g.multipleBondRadiusFactor = value;
break;
case 570425370:
this.g.multipleBondSpacing = value;
break;
case 570425393:
this.tm.setSlabRange (value);
break;
case 570425365:
this.g.minimizationCriterion = value;
break;
case 570425359:
if (this.haveDisplay) this.actionManager.setGestureSwipeFactor (value);
break;
case 570425367:
if (this.haveDisplay) this.actionManager.setMouseDragFactor (value);
break;
case 570425368:
if (this.haveDisplay) this.actionManager.setMouseWheelFactor (value);
break;
case 570425408:
this.g.strutLengthMaximum = value;
break;
case 570425406:
this.g.strutDefaultRadius = value;
break;
case 570425376:
this.setSpin ("X", Clazz.floatToInt (value));
break;
case 570425378:
this.setSpin ("Y", Clazz.floatToInt (value));
break;
case 570425380:
this.setSpin ("Z", Clazz.floatToInt (value));
break;
case 570425371:
if (Float.isNaN (value)) return;
this.setSpin ("FPS", Clazz.floatToInt (value));
break;
case 570425363:
this.g.loadAtomDataTolerance = value;
break;
case 570425360:
this.g.hbondsAngleMinimum = value;
break;
case 570425361:
this.g.hbondsDistanceMaximum = value;
break;
case 570425382:
this.g.pointGroupDistanceTolerance = value;
break;
case 570425384:
this.g.pointGroupLinearTolerance = value;
break;
case 570425357:
this.g.ellipsoidAxisDiameter = value;
break;
case 570425398:
this.setSpin ("x", Clazz.floatToInt (value));
break;
case 570425400:
this.setSpin ("y", Clazz.floatToInt (value));
break;
case 570425402:
this.setSpin ("z", Clazz.floatToInt (value));
break;
case 570425396:
this.setSpin ("fps", Clazz.floatToInt (value));
break;
case 570425352:
this.g.defaultDrawArrowScale = value;
break;
case 570425354:
this.g.defaultTranslucent = value;
break;
case 570425346:
this.setAxesScale (value);
break;
case 570425416:
this.tm.setVisualRange (value);
this.refresh (1, "set visualRange");
break;
case 570425372:
this.setNavigationDepthPercent (value);
break;
case 570425374:
this.g.navigationSpeed = value;
break;
case 570425373:
this.tm.setNavigationSlabOffsetPercent (value);
break;
case 570425350:
this.tm.setCameraDepthPercent (value, false);
this.refresh (1, "set cameraDepth");
return;
case 570425388:
this.setRotationRadius (value, true);
return;
case 570425362:
this.g.hoverDelayMs = Clazz.floatToInt (value * 1000);
break;
case 570425392:
this.g.sheetSmoothing = value;
break;
case 570425355:
value = JV.Viewer.checkFloatRange (value, -10, 10);
this.g.dipoleScale = value;
break;
case 570425404:
this.tm.setStereoDegrees (value);
break;
case 1649410049:
this.setVectorScale (value);
return;
case 570425412:
this.setVibrationPeriod (value);
return;
case 570425414:
this.setVibrationScale (value);
return;
case 570425348:
this.setBondTolerance (value);
return;
case 570425364:
this.setMinBondDistance (value);
return;
case 570425390:
this.tm.setScaleAngstromsPerInch (value);
break;
case 570425394:
value = JV.Viewer.checkFloatRange (value, 0, 10);
this.g.solventProbeRadius = value;
break;
default:
if (!this.g.htNonbooleanParameterValues.containsKey (key.toLowerCase ())) {
this.g.setUserVariable (key, JS.SV.newV (3, Float.$valueOf (value)));
return;
}}
this.g.setF (key, value);
}, "~S,~N,~N");
Clazz.overrideMethod (c$, "setIntProperty", 
function (key, value) {
if (value == -2147483648) return;
if (key.charAt (0) == '_') {
this.g.setI (key, value);
return;
}var tok = JS.T.getTokFromName (key);
switch (JS.T.getParamType (tok)) {
case 545259520:
this.setStringPropertyTok (key, tok, "" + value);
break;
case 603979776:
this.setBooleanPropertyTok (key, tok, value != 0);
break;
case 570425344:
this.setFloatPropertyTok (key, tok, value);
break;
default:
this.setIntPropertyTok (key, tok, value);
}
}, "~S,~N");
Clazz.defineMethod (c$, "setIntPropertyTok", 
 function (key, tok, value) {
switch (tok) {
case 553648138:
value = (value == 0 ? 0 : 1);
this.g.bondingVersion = JU.Elements.bondingVersion = value;
break;
case 553648137:
this.g.celShadingPower = value;
this.gdata.setCelPower (value);
break;
case 553648129:
this.gdata.setAmbientOcclusion (value);
break;
case 553648158:
this.g.platformSpeed = Math.min (Math.max (value, 0), 10);
break;
case 553648151:
this.g.meshScale = value;
break;
case 553648153:
this.g.minPixelSelRadius = value;
break;
case 553648149:
this.g.isosurfacePropertySmoothingPower = value;
break;
case 553648165:
this.g.repaintWaitMs = value;
break;
case 553648170:
this.g.smallMoleculeMaxAtoms = value;
break;
case 553648152:
this.g.minimizationSteps = value;
break;
case 553648184:
this.g.strutSpacing = value;
break;
case 553648156:
value = JV.Viewer.checkIntRange (value, 0, 1000);
this.gdata.setPhongExponent (value);
break;
case 553648146:
this.g.helixStep = value;
this.ms.setHaveStraightness (false);
break;
case 553648144:
this.g.dotScale = value;
break;
case 553648143:
this.g.dotDensity = value;
break;
case 553648140:
this.g.delayMaximumMs = value;
break;
case 553648150:
JU.Logger.setLogLevel (value);
JU.Logger.info ("logging level set to " + value);
this.g.setI ("logLevel", value);
if (this.eval != null) this.eval.setDebugging ();
return;
case 553648134:
switch (J.c.AXES.getAxesMode (value)) {
case J.c.AXES.MOLECULAR:
this.setAxesModeMolecular (true);
return;
case J.c.AXES.BOUNDBOX:
this.setAxesModeMolecular (false);
return;
case J.c.AXES.UNITCELL:
this.setAxesModeUnitCell (true);
return;
}
return;
case 553648178:
this.setStrandCount (0, value);
return;
case 553648182:
this.setStrandCount (12, value);
return;
case 553648180:
this.setStrandCount (13, value);
return;
case 553648155:
return;
case 536870922:
this.g.scriptDelay = value;
break;
case 553648176:
if (value < 0) value = JV.Viewer.checkIntRange (value, -10, -1);
 else value = JV.Viewer.checkIntRange (value, 0, 100);
this.gdata.setSpecularPower (value);
break;
case 553648172:
value = JV.Viewer.checkIntRange (-value, -10, -1);
this.gdata.setSpecularPower (value);
break;
case 553648136:
this.setMarBond (value);
return;
case 536870924:
this.setBooleanPropertyTok (key, tok, value == 1);
return;
case 553648174:
value = JV.Viewer.checkIntRange (value, 0, 100);
this.gdata.setSpecularPercent (value);
break;
case 553648142:
value = JV.Viewer.checkIntRange (value, 0, 100);
this.gdata.setDiffusePercent (value);
break;
case 553648130:
value = JV.Viewer.checkIntRange (value, 0, 100);
this.gdata.setAmbientPercent (value);
break;
case 553648186:
this.tm.zDepthToPercent (value);
break;
case 553648188:
this.tm.zSlabToPercent (value);
break;
case 554176526:
this.tm.depthToPercent (value);
break;
case 554176565:
this.tm.slabToPercent (value);
break;
case 553648190:
this.g.zShadePower = Math.max (value, 1);
break;
case 553648166:
this.g.ribbonAspectRatio = value;
break;
case 553648157:
this.g.pickingSpinRate = (value < 1 ? 1 : value);
break;
case 553648132:
this.setAnimationFps (value);
return;
case 553648154:
this.setPercentVdwAtom (value);
break;
case 553648147:
this.g.hermiteLevel = value;
break;
case 553648145:
case 553648148:
case 553648160:
case 553648159:
case 553648162:
case 553648164:
break;
default:
if (!this.g.htNonbooleanParameterValues.containsKey (key)) {
this.g.setUserVariable (key, JS.SV.newI (value));
return;
}}
this.g.setI (key, value);
}, "~S,~N,~N");
c$.checkIntRange = Clazz.defineMethod (c$, "checkIntRange", 
 function (value, min, max) {
return (value < min ? min : value > max ? max : value);
}, "~N,~N,~N");
c$.checkFloatRange = Clazz.defineMethod (c$, "checkFloatRange", 
 function (value, min, max) {
return (value < min ? min : value > max ? max : value);
}, "~N,~N,~N");
Clazz.overrideMethod (c$, "setBooleanProperty", 
function (key, value) {
if (key.charAt (0) == '_') {
this.g.setB (key, value);
return;
}var tok = JS.T.getTokFromName (key);
switch (JS.T.getParamType (tok)) {
case 545259520:
this.setStringPropertyTok (key, tok, "");
break;
case 553648128:
this.setIntPropertyTok (key, tok, value ? 1 : 0);
break;
case 570425344:
this.setFloatPropertyTok (key, tok, value ? 1 : 0);
break;
default:
this.setBooleanPropertyTok (key, tok, value);
}
}, "~S,~B");
Clazz.defineMethod (c$, "setBooleanPropertyTok", 
 function (key, tok, value) {
var doRepaint = true;
switch (tok) {
case 603979938:
this.g.showUnitCellInfo = value;
break;
case 603979937:
this.g.showUnitCellDetails = value;
break;
case 603979848:
doRepaint = false;
break;
case 603979972:
this.g.vectorsCentered = value;
break;
case 603979811:
this.g.backboneSteps = value;
break;
case 603979819:
this.g.cartoonRibose = value;
if (value && this.getBoolean (603979816)) this.setBooleanPropertyTok ("cartoonBaseEdges", 603979816, false);
break;
case 603979837:
this.g.ellipsoidArrows = value;
break;
case 603979967:
this.g.translucent = value;
break;
case 603979818:
this.g.cartoonLadders = value;
break;
case 603979968:
var b = this.g.twistedSheets;
this.g.twistedSheets = value;
if (b != value) this.checkCoordinatesChanged ();
break;
case 603979822:
this.g.celShading = value;
this.gdata.setCel (value);
break;
case 603979817:
this.g.cartoonFancy = value;
break;
case 603979934:
this.g.showTiming = value;
break;
case 603979973:
this.g.vectorSymmetry = value;
break;
case 603979870:
this.g.isosurfaceKey = value;
break;
case 603979889:
this.g.partialDots = value;
break;
case 603979874:
this.g.legacyAutoBonding = value;
break;
case 603979826:
this.g.defaultStructureDSSP = value;
break;
case 603979834:
this.g.dsspCalcHydrogen = value;
break;
case 603979782:
this.g.allowModelkit = value;
if (!value) this.setModelKitMode (false);
break;
case 603979883:
this.setModelKitMode (value);
break;
case 603979885:
this.g.multiProcessor = value && (JV.Viewer.nProcessors > 1);
break;
case 603979884:
this.g.monitorEnergy = value;
break;
case 603979853:
this.g.hbondsRasmol = value;
break;
case 603979881:
this.g.minimizationRefresh = value;
break;
case 603979882:
this.g.minimizationSilent = value;
break;
case 603979869:
if (value) {
this.$isKiosk = true;
this.g.disablePopupMenu = true;
if (this.display != null) this.apiPlatform.setTransparentCursor (this.display);
}break;
case 603979974:
this.g.waitForMoveTo = value;
break;
case 603979876:
this.g.logCommands = true;
break;
case 603979877:
this.g.logGestures = true;
break;
case 603979784:
this.g.allowMultiTouch = value;
break;
case 603979894:
this.g.preserveState = value;
this.ms.setPreserveState (value);
this.undoClear ();
break;
case 603979955:
this.g.strutsMultiple = value;
break;
case 603979842:
break;
case 603979939:
this.g.slabByAtom = value;
break;
case 603979940:
this.g.slabByMolecule = value;
break;
case 603979902:
this.g.saveProteinStructureState = value;
break;
case 603979780:
this.g.allowGestures = value;
break;
case 603979868:
this.g.imageState = value;
break;
case 603979970:
this.g.useMinimizationThread = value;
break;
case 603979781:
if (this.g.disablePopupMenu) value = false;
this.g.allowKeyStrokes = value;
break;
case 603979831:
this.g.dragSelected = value;
this.showSelected = false;
break;
case 603979924:
this.g.showKeyStrokes = value;
break;
case 603979844:
this.g.fontCaching = value;
break;
case 603979796:
this.g.atomPicking = value;
break;
case 603979814:
this.highlight (null);
this.g.bondPicking = value;
break;
case 603979906:
this.g.selectAllModels = value;
break;
case 603979880:
this.g.messageStyleChime = value;
break;
case 603979892:
this.g.pdbSequential = value;
break;
case 603979890:
this.g.pdbAddHydrogens = value;
break;
case 603979891:
this.g.pdbGetHeader = value;
break;
case 603979838:
this.g.ellipsoidAxes = value;
break;
case 603979836:
this.g.ellipsoidArcs = value;
break;
case 603979839:
this.g.ellipsoidBall = value;
break;
case 603979840:
this.g.ellipsoidDots = value;
break;
case 603979841:
this.g.ellipsoidFill = value;
break;
case 603979845:
this.g.fontScaling = value;
break;
case 603979956:
this.setSyncTarget (0, value);
break;
case 603979958:
this.setSyncTarget (1, value);
break;
case 603979976:
this.g.wireframeRotation = value;
break;
case 603979871:
this.g.isosurfacePropertySmoothing = value;
break;
case 603979833:
this.g.drawPicking = value;
break;
case 603979786:
case 603979790:
case 603979788:
this.setAntialias (tok, value);
break;
case 603979944:
this.g.smartAromatic = value;
break;
case 603979794:
this.setApplySymmetryToBonds (value);
break;
case 603979792:
this.g.appendNew = value;
break;
case 603979800:
this.g.autoFps = value;
break;
case 603979971:
JU.DF.setUseNumberLocalization (this.g.useNumberLocalization = value);
break;
case 603979918:
case 1611272202:
key = "showFrank";
this.setFrankOn (value);
break;
case 1613758488:
key = "solventProbe";
this.g.solventOn = value;
break;
case 603979948:
this.g.solventOn = value;
break;
case 603979785:
this.g.allowRotateSelected = value;
break;
case 603979783:
this.g.allowMoveAtoms = value;
this.g.allowRotateSelected = value;
this.g.dragSelected = value;
this.showSelected = false;
break;
case 536870922:
this.setIntPropertyTok ("showScript", tok, value ? 1 : 0);
return;
case 603979778:
this.g.allowEmbeddedScripts = value;
break;
case 603979888:
this.g.navigationPeriodic = value;
break;
case 603979984:
this.tm.setZShadeEnabled (value);
return;
case 603979832:
if (this.haveDisplay) this.g.drawHover = value;
break;
case 603979887:
this.setNavigationMode (value);
break;
case 603979886:
return;
case 603979860:
this.g.hideNavigationPoint = value;
break;
case 603979930:
this.g.showNavigationPointAlways = value;
break;
case 603979896:
this.setRefreshing (value);
break;
case 603979872:
this.g.justifyMeasurements = value;
break;
case 603979952:
this.g.ssbondsBackbone = value;
break;
case 603979852:
this.g.hbondsBackbone = value;
break;
case 603979854:
this.g.hbondsSolid = value;
break;
case 536870924:
this.gdata.setSpecular (value);
break;
case 603979942:
this.tm.setSlabEnabled (value);
return;
case 603979980:
this.tm.setZoomEnabled (value);
return;
case 603979864:
this.g.highResolutionFlag = value;
break;
case 603979966:
this.g.traceAlpha = value;
break;
case 603979983:
this.g.zoomLarge = value;
this.tm.setZoomHeight (this.g.zoomHeight, value);
break;
case 603979982:
this.g.zoomHeight = value;
this.tm.setZoomHeight (value, this.g.zoomLarge);
break;
case 603979873:
J.i18n.GT.setDoTranslate (value);
break;
case 603979862:
this.slm.setHideNotSelected (value);
break;
case 603979904:
this.setScriptQueue (value);
break;
case 603979830:
this.g.dotSurface = value;
break;
case 603979829:
this.g.dotsSelectedOnly = value;
break;
case 1611141171:
this.setSelectionHalos (value);
break;
case 603979910:
this.g.rasmolHydrogenSetting = value;
break;
case 603979908:
this.g.rasmolHeteroSetting = value;
break;
case 603979928:
this.g.showMultipleBonds = value;
break;
case 603979920:
this.g.showHiddenSelectionHalos = value;
break;
case 603979975:
this.tm.setWindowCentered (value);
break;
case 603979828:
this.g.displayCellParameters = value;
break;
case 603979960:
this.g.testFlag1 = value;
break;
case 603979962:
this.g.testFlag2 = value;
break;
case 603979964:
this.g.testFlag3 = value;
break;
case 603979965:
this.jmolTest ();
this.g.testFlag4 = value;
break;
case 603979898:
this.g.ribbonBorder = value;
break;
case 603979816:
this.g.cartoonBaseEdges = value;
if (value && this.getBoolean (603979819)) this.setBooleanPropertyTok ("cartoonRibose", 603979819, false);
break;
case 603979820:
this.g.cartoonRockets = value;
break;
case 603979900:
this.g.rocketBarrels = value;
break;
case 603979850:
this.gdata.setGreyscaleMode (this.g.greyscaleRendering = value);
break;
case 603979879:
this.g.measurementLabels = value;
break;
case 603979810:
this.setAxesModeMolecular (!value);
return;
case 603979804:
this.setAxesModeMolecular (value);
return;
case 603979808:
this.setAxesModeUnitCell (value);
return;
case 603979806:
this.setAxesOrientationRasmol (value);
return;
case 603979824:
this.setStringPropertyTok ("defaultcolorscheme", 545259545, value ? "rasmol" : "jmol");
return;
case 603979825:
this.setDebugScript (value);
return;
case 603979893:
this.setPerspectiveDepth (value);
return;
case 603979798:
this.setAutoBond (value);
return;
case 603979914:
this.setShowAxes (value);
return;
case 603979916:
this.setShowBbcage (value);
return;
case 603979922:
this.setShowHydrogens (value);
return;
case 603979926:
this.setShowMeasurements (value);
return;
case 603979936:
this.setShowUnitCell (value);
return;
case 603979812:
doRepaint = false;
this.g.bondModeOr = value;
break;
case 603979978:
doRepaint = false;
this.g.zeroBasedXyzRasmol = value;
this.reset (true);
break;
case 603979895:
doRepaint = false;
this.g.rangeSelected = value;
break;
case 603979878:
doRepaint = false;
this.g.measureAllModels = value;
break;
case 603979954:
doRepaint = false;
this.sm.setAllowStatusReporting (value);
break;
case 603979823:
doRepaint = false;
this.g.chainCaseSensitive = value;
break;
case 603979858:
doRepaint = false;
this.g.hideNameInPopup = value;
break;
case 603979827:
doRepaint = false;
this.g.disablePopupMenu = value;
break;
case 603979846:
doRepaint = false;
this.g.forceAutoBond = value;
break;
default:
if (!this.g.htBooleanParameterFlags.containsKey (key.toLowerCase ())) {
this.g.setUserVariable (key, JS.SV.getBoolean (value));
return;
}}
this.g.setB (key, value);
if (doRepaint) this.setTainted (true);
}, "~S,~N,~B");
Clazz.defineMethod (c$, "setModelKitMode", 
 function (value) {
if (this.actionManager == null || !this.allowScripting) return;
if (value || this.g.modelKitMode) {
this.setPickingMode (null, value ? 33 : 1);
this.setPickingMode (null, value ? 32 : 1);
}var isChange = (this.g.modelKitMode != value);
this.g.modelKitMode = value;
this.highlight (null);
if (value) {
this.setNavigationMode (false);
this.selectAll ();
this.setAtomPickingOption ("C");
this.setBondPickingOption ("p");
if (!this.$isApplet) this.popupMenu (0, 0, 'm');
if (isChange) this.sm.setCallbackFunction ("modelkit", "ON");
this.g.modelKitMode = true;
if (this.getAtomCount () == 0) this.zap (false, true, true);
} else {
this.actionManager.setPickingMode (-1);
this.setStringProperty ("pickingStyle", "toggle");
this.setBooleanProperty ("bondPicking", false);
if (isChange) this.sm.setCallbackFunction ("modelkit", "OFF");
}}, "~B");
Clazz.defineMethod (c$, "setSmilesString", 
function (s) {
if (s == null) this.g.removeParam ("_smilesString");
 else this.g.setO ("_smilesString", s);
}, "~S");
Clazz.defineMethod (c$, "removeUserVariable", 
function (key) {
this.g.removeUserVariable (key);
if (key.endsWith ("callback")) this.sm.setCallbackFunction (key, null);
}, "~S");
Clazz.defineMethod (c$, "isJmolVariable", 
function (key) {
return this.g.isJmolVariable (key);
}, "~S");
Clazz.defineMethod (c$, "jmolTest", 
 function () {
});
Clazz.defineMethod (c$, "showParameter", 
function (key, ifNotSet, nMax) {
var sv = "" + this.g.getParameterEscaped (key, nMax);
if (ifNotSet || sv.indexOf ("<not defined>") < 0) this.showString (key + " = " + sv, false);
}, "~S,~B,~N");
Clazz.defineMethod (c$, "showString", 
function (str, isPrint) {
if (this.isScriptQueued () && (!this.isSilent || isPrint) && !this.isJS) JU.Logger.warn (str);
this.scriptEcho (str);
}, "~S,~B");
Clazz.defineMethod (c$, "getAllSettings", 
function (prefix) {
return this.getStateCreator ().getAllSettings (prefix);
}, "~S");
Clazz.defineMethod (c$, "getBindingInfo", 
function (qualifiers) {
return (this.haveDisplay ? this.actionManager.getBindingInfo (qualifiers) : "");
}, "~S");
Clazz.defineMethod (c$, "getIsosurfacePropertySmoothing", 
function (asPower) {
return (asPower ? this.g.isosurfacePropertySmoothingPower : this.g.isosurfacePropertySmoothing ? 1 : 0);
}, "~B");
Clazz.defineMethod (c$, "setNavigationDepthPercent", 
function (percent) {
this.tm.setNavigationDepthPercent (percent);
this.refresh (1, "set navigationDepth");
}, "~N");
Clazz.defineMethod (c$, "getShowNavigationPoint", 
function () {
if (!this.g.navigationMode || !this.tm.canNavigate ()) return false;
return (this.tm.isNavigating () && !this.g.hideNavigationPoint || this.g.showNavigationPointAlways || this.getInMotion (true));
});
Clazz.defineMethod (c$, "getCurrentSolventProbeRadius", 
function () {
return this.g.solventOn ? this.g.solventProbeRadius : 0;
});
Clazz.defineMethod (c$, "getTestFlag", 
function (i) {
switch (i) {
case 1:
return this.g.testFlag1;
case 2:
return this.g.testFlag2;
case 3:
return this.g.testFlag3;
case 4:
return this.g.testFlag4;
}
return false;
}, "~N");
Clazz.overrideMethod (c$, "setPerspectiveDepth", 
function (perspectiveDepth) {
this.tm.setPerspectiveDepth (perspectiveDepth);
}, "~B");
Clazz.overrideMethod (c$, "setAxesOrientationRasmol", 
function (TF) {
this.g.setB ("axesOrientationRasmol", TF);
this.g.axesOrientationRasmol = TF;
this.reset (true);
}, "~B");
Clazz.defineMethod (c$, "setAxesScale", 
function (scale) {
scale = JV.Viewer.checkFloatRange (scale, -100, 100);
this.g.axesScale = scale;
this.axesAreTainted = true;
}, "~N");
Clazz.defineMethod (c$, "getAxisPoints", 
function () {
this.shm.loadShape (33);
return (this.getObjectMad (1) == 0 || this.g.axesMode !== J.c.AXES.UNITCELL || (this.getShapeProperty (33, "axesTypeXY")).booleanValue () || this.getShapeProperty (33, "origin") != null ? null : this.getShapeProperty (33, "axisPoints"));
});
Clazz.defineMethod (c$, "resetError", 
function () {
this.g.removeParam ("_errormessage");
});
Clazz.defineMethod (c$, "setAxesModeMolecular", 
 function (TF) {
this.g.axesMode = (TF ? J.c.AXES.MOLECULAR : J.c.AXES.BOUNDBOX);
this.axesAreTainted = true;
this.g.removeParam ("axesunitcell");
this.g.removeParam (TF ? "axeswindow" : "axesmolecular");
this.g.setI ("axesMode", this.g.axesMode.getCode ());
this.g.setB (TF ? "axesMolecular" : "axesWindow", true);
}, "~B");
Clazz.defineMethod (c$, "setAxesModeUnitCell", 
function (TF) {
this.g.axesMode = (TF ? J.c.AXES.UNITCELL : J.c.AXES.BOUNDBOX);
this.axesAreTainted = true;
this.g.removeParam ("axesmolecular");
this.g.removeParam (TF ? "axeswindow" : "axesunitcell");
this.g.setB (TF ? "axesUnitcell" : "axesWindow", true);
this.g.setI ("axesMode", this.g.axesMode.getCode ());
}, "~B");
Clazz.overrideMethod (c$, "getPerspectiveDepth", 
function () {
return this.tm.getPerspectiveDepth ();
});
Clazz.overrideMethod (c$, "setSelectionHalos", 
function (TF) {
if (this.ms == null || TF == this.ms.getSelectionHaloEnabled ()) return;
this.g.setB ("selectionHalos", TF);
this.shm.loadShape (8);
this.ms.setSelectionHaloEnabled (TF);
}, "~B");
Clazz.defineMethod (c$, "getSelectionHaloEnabled", 
function (isRenderer) {
var flag = this.ms.getSelectionHaloEnabled () || isRenderer && this.showSelected;
if (isRenderer) this.showSelected = false;
return flag;
}, "~B");
Clazz.defineMethod (c$, "setStrandCount", 
 function (type, value) {
value = JV.Viewer.checkIntRange (value, 0, 20);
switch (type) {
case 12:
this.g.strandCountForStrands = value;
break;
case 13:
this.g.strandCountForMeshRibbon = value;
break;
default:
this.g.strandCountForStrands = value;
this.g.strandCountForMeshRibbon = value;
break;
}
this.g.setI ("strandCount", value);
this.g.setI ("strandCountForStrands", this.g.strandCountForStrands);
this.g.setI ("strandCountForMeshRibbon", this.g.strandCountForMeshRibbon);
}, "~N,~N");
Clazz.defineMethod (c$, "getStrandCount", 
function (type) {
return (type == 12 ? this.g.strandCountForStrands : this.g.strandCountForMeshRibbon);
}, "~N");
Clazz.defineMethod (c$, "setNavigationMode", 
function (TF) {
this.g.navigationMode = TF;
this.tm.setNavigationMode (TF);
}, "~B");
Clazz.defineMethod (c$, "setTransformManagerDefaults", 
 function () {
this.tm.setCameraDepthPercent (this.g.defaultCameraDepth, true);
this.tm.setPerspectiveDepth (this.g.defaultPerspectiveDepth);
this.tm.setStereoDegrees (-5);
this.tm.setVisualRange (this.g.visualRange);
this.tm.setSpinOff ();
this.tm.setVibrationPeriod (0);
this.tm.setFrameOffsets (this.frameOffsets);
});
Clazz.overrideMethod (c$, "setAutoBond", 
function (TF) {
this.g.setB ("autobond", TF);
this.g.autoBond = TF;
}, "~B");
Clazz.defineMethod (c$, "makeConnections", 
function (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy) {
this.clearModelDependentObjects ();
this.clearMinimization ();
return this.ms.makeConnections (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy);
}, "~N,~N,~N,~N,JU.BS,JU.BS,JU.BS,~B,~B,~N");
Clazz.overrideMethod (c$, "rebond", 
function () {
this.rebondState (false);
});
Clazz.defineMethod (c$, "rebondState", 
function (isStateScript) {
this.clearModelDependentObjects ();
this.ms.deleteAllBonds ();
var isLegacy = isStateScript && this.g.legacyAutoBonding;
this.ms.autoBondBs4 (null, null, null, null, this.getMadBond (), isLegacy);
this.addStateScript ((isLegacy ? "set legacyAutoBonding TRUE;connect;set legacyAutoBonding FALSE;" : "connect;"), false, true);
}, "~B");
Clazz.overrideMethod (c$, "setPercentVdwAtom", 
function (value) {
this.g.setI ("percentVdwAtom", value);
this.g.percentVdwAtom = value;
this.rd.value = value / 100;
this.rd.factorType = J.atomdata.RadiusData.EnumType.FACTOR;
this.rd.vdwType = J.c.VDW.AUTO;
this.shm.setShapeSizeBs (0, 0, this.rd, null);
}, "~N");
Clazz.overrideMethod (c$, "getMadBond", 
function () {
return (this.g.bondRadiusMilliAngstroms * 2);
});
Clazz.overrideMethod (c$, "setShowHydrogens", 
function (TF) {
this.g.setB ("showHydrogens", TF);
this.g.showHydrogens = TF;
}, "~B");
Clazz.overrideMethod (c$, "setShowBbcage", 
function (value) {
this.setObjectMad (31, "boundbox", (value ? -4 : 0));
this.g.setB ("showBoundBox", value);
}, "~B");
Clazz.overrideMethod (c$, "getShowBbcage", 
function () {
return this.getObjectMad (4) != 0;
});
Clazz.defineMethod (c$, "setShowUnitCell", 
function (value) {
this.setObjectMad (32, "unitcell", (value ? -2 : 0));
this.g.setB ("showUnitCell", value);
}, "~B");
Clazz.defineMethod (c$, "getShowUnitCell", 
function () {
return this.getObjectMad (5) != 0;
});
Clazz.overrideMethod (c$, "setShowAxes", 
function (value) {
this.setObjectMad (33, "axes", (value ? -2 : 0));
this.g.setB ("showAxes", value);
}, "~B");
Clazz.overrideMethod (c$, "getShowAxes", 
function () {
return this.getObjectMad (1) != 0;
});
Clazz.overrideMethod (c$, "setFrankOn", 
function (TF) {
if (this.$isPreviewOnly) TF = false;
this.frankOn = TF;
this.setObjectMad (35, "frank", (TF ? 1 : 0));
}, "~B");
Clazz.defineMethod (c$, "getShowFrank", 
function () {
if (this.$isPreviewOnly || this.$isApplet && this.creatingImage) return false;
return (this.$isSignedApplet && !this.isSignedAppletLocal && !this.isJS || this.frankOn);
});
Clazz.overrideMethod (c$, "setShowMeasurements", 
function (TF) {
this.g.setB ("showMeasurements", TF);
this.g.showMeasurements = TF;
}, "~B");
Clazz.defineMethod (c$, "setUnits", 
function (units, isDistance) {
this.g.setUnits (units);
if (isDistance) {
this.g.setUnits (units);
this.setShapeProperty (6, "reformatDistances", null);
} else {
}}, "~S,~B");
Clazz.overrideMethod (c$, "setRasmolDefaults", 
function () {
this.setDefaultsType ("RasMol");
});
Clazz.overrideMethod (c$, "setJmolDefaults", 
function () {
this.setDefaultsType ("Jmol");
});
Clazz.defineMethod (c$, "setDefaultsType", 
 function (type) {
if (type.equalsIgnoreCase ("RasMol")) {
this.stm.setRasMolDefaults ();
return;
}if (type.equalsIgnoreCase ("PyMOL")) {
this.stm.setPyMOLDefaults ();
return;
}this.stm.setJmolDefaults ();
this.setIntProperty ("bondingVersion", 0);
this.shm.setShapeSizeBs (0, 0, this.rd, this.getAllAtoms ());
}, "~S");
Clazz.defineMethod (c$, "setAntialias", 
 function (tok, TF) {
switch (tok) {
case 603979786:
this.g.antialiasDisplay = TF;
break;
case 603979790:
this.g.antialiasTranslucent = TF;
break;
case 603979788:
this.g.antialiasImages = TF;
return;
}
this.resizeImage (0, 0, false, false, true);
}, "~N,~B");
Clazz.defineMethod (c$, "allocTempPoints", 
function (size) {
return this.tempArray.allocTempPoints (size);
}, "~N");
Clazz.defineMethod (c$, "freeTempPoints", 
function (tempPoints) {
this.tempArray.freeTempPoints (tempPoints);
}, "~A");
Clazz.defineMethod (c$, "allocTempScreens", 
function (size) {
return this.tempArray.allocTempScreens (size);
}, "~N");
Clazz.defineMethod (c$, "freeTempScreens", 
function (tempScreens) {
this.tempArray.freeTempScreens (tempScreens);
}, "~A");
Clazz.defineMethod (c$, "allocTempEnum", 
function (size) {
return this.tempArray.allocTempEnum (size);
}, "~N");
Clazz.defineMethod (c$, "freeTempEnum", 
function (temp) {
this.tempArray.freeTempEnum (temp);
}, "~A");
Clazz.defineMethod (c$, "getFont3D", 
function (fontFace, fontStyle, fontSize) {
return this.gdata.getFont3DFSS (fontFace, fontStyle, fontSize);
}, "~S,~S,~N");
Clazz.defineMethod (c$, "formatText", 
function (text0) {
var i;
if ((i = text0.indexOf ("@{")) < 0 && (i = text0.indexOf ("%{")) < 0) return text0;
var text = text0;
var isEscaped = (text.indexOf ("\\") >= 0);
if (isEscaped) {
text = JU.PT.rep (text, "\\%", "\1");
text = JU.PT.rep (text, "\\@", "\2");
isEscaped = !text.equals (text0);
}text = JU.PT.rep (text, "%{", "@{");
var name;
while ((i = text.indexOf ("@{")) >= 0) {
i++;
var i0 = i + 1;
var len = text.length;
i = JU.Txt.ichMathTerminator (text, i, len);
if (i >= len) return text;
name = text.substring (i0, i);
if (name.length == 0) return text;
var v = this.evaluateExpression (name);
if (Clazz.instanceOf (v, JU.P3)) v = JU.Escape.eP (v);
text = text.substring (0, i0 - 2) + v.toString () + text.substring (i + 1);
}
if (isEscaped) {
text = JU.PT.rep (text, "\2", "@");
text = JU.PT.rep (text, "\1", "%");
}return text;
}, "~S");
Clazz.defineMethod (c$, "getElementSymbol", 
function (i) {
return this.ms.getElementSymbol (i);
}, "~N");
Clazz.defineMethod (c$, "getElementNumber", 
function (i) {
return this.ms.getElementNumber (i);
}, "~N");
Clazz.overrideMethod (c$, "getAtomName", 
function (i) {
return this.ms.getAtomName (i);
}, "~N");
Clazz.overrideMethod (c$, "getAtomNumber", 
function (i) {
return this.ms.getAtomNumber (i);
}, "~N");
Clazz.overrideMethod (c$, "getAtomPoint3f", 
function (i) {
return this.ms.at[i];
}, "~N");
Clazz.overrideMethod (c$, "getAtomRadius", 
function (i) {
return this.ms.getAtomRadius (i);
}, "~N");
Clazz.overrideMethod (c$, "getAtomArgb", 
function (i) {
return this.gdata.getColorArgbOrGray (this.ms.getAtomColix (i));
}, "~N");
Clazz.overrideMethod (c$, "getAtomModelIndex", 
function (i) {
return this.ms.at[i].mi;
}, "~N");
Clazz.overrideMethod (c$, "getBondRadius", 
function (i) {
return this.ms.getBondRadius (i);
}, "~N");
Clazz.overrideMethod (c$, "getBondOrder", 
function (i) {
return this.ms.getBondOrder (i);
}, "~N");
Clazz.overrideMethod (c$, "getBondArgb1", 
function (i) {
return this.gdata.getColorArgbOrGray (this.ms.getBondColix1 (i));
}, "~N");
Clazz.overrideMethod (c$, "getBondModelIndex", 
function (i) {
return this.ms.getBondModelIndex (i);
}, "~N");
Clazz.overrideMethod (c$, "getBondArgb2", 
function (i) {
return this.gdata.getColorArgbOrGray (this.ms.getBondColix2 (i));
}, "~N");
Clazz.overrideMethod (c$, "getPolymerLeadMidPoints", 
function (modelIndex, polymerIndex) {
return this.ms.getPolymerLeadMidPoints (modelIndex, polymerIndex);
}, "~N,~N");
Clazz.defineMethod (c$, "getAtomGroupQuaternions", 
function (bsAtoms, nMax) {
return this.ms.getAtomGroupQuaternions (bsAtoms, nMax, this.getQuaternionFrame ());
}, "JU.BS,~N");
Clazz.defineMethod (c$, "setStereoMode", 
function (twoColors, stereoMode, degrees) {
this.setFloatProperty ("stereoDegrees", degrees);
this.setBooleanProperty ("greyscaleRendering", stereoMode.isBiColor ());
if (twoColors != null) this.tm.setStereoMode2 (twoColors);
 else this.tm.setStereoMode (stereoMode);
}, "~A,J.c.STER,~N");
Clazz.defineMethod (c$, "isStereoDouble", 
function () {
return this.tm.stereoMode === J.c.STER.DOUBLE;
});
Clazz.defineMethod (c$, "getChimeInfo", 
function (tok) {
return this.getPropertyManager ().getChimeInfo (tok, this.bsA ());
}, "~N");
Clazz.defineMethod (c$, "getModelFileInfo", 
function () {
return this.getPropertyManager ().getModelFileInfo (this.getVisibleFramesBitSet ());
});
Clazz.defineMethod (c$, "getModelFileInfoAll", 
function () {
return this.getPropertyManager ().getModelFileInfo (null);
});
Clazz.overrideMethod (c$, "getProperty", 
function (returnType, infoType, paramInfo) {
if (!"DATA_API".equals (returnType)) return this.getPropertyManager ().getProperty (returnType, infoType, paramInfo);
switch (("scriptCheck.........consoleText.........scriptEditor........scriptEditorState...getAppConsole.......getScriptEditor.....setMenu.............spaceGroupInfo......disablePopupMenu....defaultDirectory....getPopupMenu........shapeManager........").indexOf (infoType)) {
case 0:
return this.scriptCheckRet (paramInfo, true);
case 20:
return (this.appConsole == null ? "" : this.appConsole.getText ());
case 40:
this.showEditor (paramInfo);
return null;
case 60:
this.scriptEditorVisible = (paramInfo).booleanValue ();
return null;
case 80:
if (this.$isKiosk) {
this.appConsole = null;
} else if (Clazz.instanceOf (paramInfo, J.api.JmolAppConsoleInterface)) {
this.appConsole = paramInfo;
} else if (paramInfo != null && !(paramInfo).booleanValue ()) {
this.appConsole = null;
} else if (this.appConsole == null && paramInfo != null && (paramInfo).booleanValue ()) {
if (this.isJS) {
this.appConsole = J.api.Interface.getOption ("consolejs.AppletConsole");
}{
}if (this.appConsole != null) this.appConsole.start (this);
}this.scriptEditor = (this.isJS || this.appConsole == null ? null : this.appConsole.getScriptEditor ());
return this.appConsole;
case 100:
if (this.appConsole == null && paramInfo != null && (paramInfo).booleanValue ()) {
this.getProperty ("DATA_API", "getAppConsole", Boolean.TRUE);
this.scriptEditor = (this.appConsole == null ? null : this.appConsole.getScriptEditor ());
}return this.scriptEditor;
case 120:
if (this.jmolpopup != null) this.jmolpopup.jpiDispose ();
this.jmolpopup = null;
return this.menuStructure = paramInfo;
case 140:
return this.getSpaceGroupInfo (null);
case 160:
this.g.disablePopupMenu = true;
return null;
case 180:
return this.g.defaultDirectory;
case 200:
if (Clazz.instanceOf (paramInfo, String)) return this.getMenu (paramInfo);
return this.getPopupMenu ();
case 220:
return this.shm.getProperty (paramInfo);
}
JU.Logger.error ("ERROR in getProperty DATA_API: " + infoType);
return null;
}, "~S,~S,~O");
Clazz.defineMethod (c$, "showEditor", 
function (file_text) {
var scriptEditor = this.getProperty ("DATA_API", "getScriptEditor", Boolean.TRUE);
if (scriptEditor == null) return;
scriptEditor.show (file_text);
}, "~A");
Clazz.defineMethod (c$, "getPropertyManager", 
 function () {
if (this.pm == null) (this.pm = J.api.Interface.getInterface ("JV.PropertyManager")).setViewer (this);
return this.pm;
});
Clazz.defineMethod (c$, "getModelExtract", 
function (atomExpression, doTransform, isModelKit, type) {
return this.getPropertyManager ().getModelExtract (this.getAtomBitSet (atomExpression), doTransform, isModelKit, type);
}, "~O,~B,~B,~S");
Clazz.defineMethod (c$, "setTainted", 
function (TF) {
this.isTainted = this.axesAreTainted = (TF && (this.refreshing || this.creatingImage));
}, "~B");
Clazz.defineMethod (c$, "notifyMouseClicked", 
function (x, y, action, mode) {
var modifiers = JV.binding.Binding.getButtonMods (action);
var clickCount = JV.binding.Binding.getClickCount (action);
this.g.setI ("_mouseX", x);
this.g.setI ("_mouseY", this.dimScreen.height - y);
this.g.setI ("_mouseAction", action);
this.g.setI ("_mouseModifiers", modifiers);
this.g.setI ("_clickCount", clickCount);
return this.sm.setStatusClicked (x, this.dimScreen.height - y, action, clickCount, mode);
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "checkObjectClicked", 
function (x, y, modifiers) {
return this.shm.checkObjectClicked (x, y, modifiers, this.getVisibleFramesBitSet (), this.g.drawPicking);
}, "~N,~N,~N");
Clazz.defineMethod (c$, "checkObjectHovered", 
function (x, y) {
return (x >= 0 && this.shm != null && this.shm.checkObjectHovered (x, y, this.getVisibleFramesBitSet (), this.getBondPicking ()));
}, "~N,~N");
Clazz.defineMethod (c$, "checkObjectDragged", 
function (prevX, prevY, x, y, action) {
var iShape = 0;
switch (this.getPickingMode ()) {
case 2:
iShape = 5;
break;
case 4:
iShape = 22;
break;
}
if (this.shm.checkObjectDragged (prevX, prevY, x, y, action, this.getVisibleFramesBitSet (), iShape)) {
this.refresh (1, "checkObjectDragged");
if (iShape == 22) this.scriptEcho (this.getShapeProperty (22, "command"));
}}, "~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "rotateAxisAngleAtCenter", 
function (eval, rotCenter, rotAxis, degreesPerSecond, endDegrees, isSpin, bsSelected) {
var isOK = this.tm.rotateAxisAngleAtCenter (eval, rotCenter, rotAxis, degreesPerSecond, endDegrees, isSpin, bsSelected);
if (isOK) this.refresh (-1, "rotateAxisAngleAtCenter");
return isOK;
}, "J.api.JmolScriptEvaluator,JU.P3,JU.V3,~N,~N,~B,JU.BS");
Clazz.defineMethod (c$, "rotateAboutPointsInternal", 
function (eval, point1, point2, degreesPerSecond, endDegrees, isSpin, bsSelected, translation, finalPoints, dihedralList) {
if (this.isHeadless ()) {
if (isSpin && endDegrees == 3.4028235E38) return false;
isSpin = false;
}var isOK = this.tm.rotateAboutPointsInternal (eval, point1, point2, degreesPerSecond, endDegrees, false, isSpin, bsSelected, false, translation, finalPoints, dihedralList);
if (isOK) this.refresh (-1, "rotateAxisAboutPointsInternal");
return isOK;
}, "J.api.JmolScriptEvaluator,JU.P3,JU.P3,~N,~N,~B,JU.BS,JU.V3,JU.Lst,~A");
Clazz.defineMethod (c$, "startSpinningAxis", 
function (pt1, pt2, isClockwise) {
if (this.tm.spinOn || this.tm.navOn) {
this.tm.setSpinOff ();
this.tm.setNavOn (false);
return;
}this.tm.rotateAboutPointsInternal (null, pt1, pt2, this.g.pickingSpinRate, 3.4028235E38, isClockwise, true, null, false, null, null, null);
}, "JU.T3,JU.T3,~B");
Clazz.defineMethod (c$, "getModelDipole", 
function () {
return this.ms.getModelDipole (this.am.cmi);
});
Clazz.defineMethod (c$, "calculateMolecularDipole", 
function (bsAtoms) {
return this.ms.calculateMolecularDipole (this.am.cmi, bsAtoms);
}, "JU.BS");
Clazz.defineMethod (c$, "setDefaultLattice", 
function (p) {
if (!Float.isNaN (p.x + p.y + p.z)) this.g.ptDefaultLattice.setT (p);
this.g.setO ("defaultLattice", JU.Escape.eP (p));
}, "JU.P3");
Clazz.defineMethod (c$, "getDefaultLattice", 
function () {
return this.g.ptDefaultLattice;
});
Clazz.defineMethod (c$, "getData", 
function (atomExpression, type) {
var exp = "";
if (type.equalsIgnoreCase ("MOL") || type.equalsIgnoreCase ("SDF") || type.equalsIgnoreCase ("V2000") || type.equalsIgnoreCase ("V3000") || type.equalsIgnoreCase ("XYZVIB") || type.equalsIgnoreCase ("CD") || type.equalsIgnoreCase ("JSON")) return this.getModelExtract (atomExpression, false, false, type);
if (type.toLowerCase ().indexOf ("property_") == 0) exp = "{selected}.label(\"%{" + type + "}\")";
 else if (type.equalsIgnoreCase ("CML")) return this.getModelCml (this.getAtomBitSet (atomExpression), 2147483647, true);
 else if (type.equalsIgnoreCase ("PDB")) exp = "{selected and not hetero}.label(\"ATOM  %5i %-4a%1A%3.3n %1c%4R%1E   %8.3x%8.3y%8.3z%6.2Q%6.2b          %2e  \").lines+{selected and hetero}.label(\"HETATM%5i %-4a%1A%3.3n %1c%4R%1E   %8.3x%8.3y%8.3z%6.2Q%6.2b          %2e  \").lines";
 else if (type.equalsIgnoreCase ("XYZRN")) exp = "\"\" + {selected}.size + \"\n\n\"+{selected}.label(\"%-2e %8.3x %8.3y %8.3z %4.2[vdw] 1 [%n]%r.%a#%i\").lines";
 else if (type.startsWith ("USER:")) exp = "{selected}.label(\"" + type.substring (5) + "\").lines";
 else exp = "\"\" + {selected}.size + \"\n\n\"+{selected}.label(\"%-2e %10.5x %10.5y %10.5z\").lines";
if (!atomExpression.equals ("selected")) exp = JU.PT.rep (exp, "selected", atomExpression);
return this.evaluateExpression (exp);
}, "~S,~S");
Clazz.defineMethod (c$, "getModelCml", 
function (bs, nAtomsMax, addBonds) {
return this.getPropertyManager ().getModelCml (bs, nAtomsMax, addBonds);
}, "JU.BS,~N,~B");
Clazz.defineMethod (c$, "getPdbAtomData", 
function (bs, sb) {
return this.getPropertyManager ().getPdbAtomData (bs == null ? this.bsA () : bs, sb);
}, "JU.BS,JU.OC");
Clazz.defineMethod (c$, "isJmolDataFrame", 
function () {
return this.ms.isJmolDataFrameForModel (this.am.cmi);
});
Clazz.defineMethod (c$, "setFrameTitle", 
function (modelIndex, title) {
this.ms.setFrameTitle (JU.BSUtil.newAndSetBit (modelIndex), title);
}, "~N,~S");
Clazz.defineMethod (c$, "setFrameTitleObj", 
function (title) {
this.shm.loadShape (30);
this.ms.setFrameTitle (this.getVisibleFramesBitSet (), title);
}, "~O");
Clazz.defineMethod (c$, "getFrameTitle", 
function () {
return this.ms.getFrameTitle (this.am.cmi);
});
Clazz.defineMethod (c$, "setAtomProperty", 
function (bs, tok, iValue, fValue, sValue, values, list) {
if (tok == 1649412120) this.shm.deleteVdwDependentShapes (bs);
this.clearMinimization ();
this.ms.setAtomProperty (bs, tok, iValue, fValue, sValue, values, list);
switch (tok) {
case 1112541185:
case 1112541186:
case 1112541187:
case 1112541188:
case 1112541189:
case 1112541190:
case 1112539153:
case 1112539154:
case 1112539155:
case 1087375365:
this.refreshMeasures (true);
}
}, "JU.BS,~N,~N,~N,~S,~A,~A");
Clazz.defineMethod (c$, "checkCoordinatesChanged", 
function () {
this.ms.recalculatePositionDependentQuantities (null, null);
this.refreshMeasures (true);
});
Clazz.defineMethod (c$, "setAtomCoords", 
function (bs, tokType, xyzValues) {
if (bs.cardinality () == 0) return;
this.ms.setAtomCoords (bs, tokType, xyzValues);
this.checkMinimization ();
this.sm.setStatusAtomMoved (bs);
}, "JU.BS,~N,~O");
Clazz.defineMethod (c$, "setAtomCoordsRelative", 
function (offset, bs) {
if (bs == null) bs = this.bsA ();
if (bs.cardinality () == 0) return;
this.ms.setAtomCoordsRelative (offset, bs);
this.checkMinimization ();
this.sm.setStatusAtomMoved (bs);
}, "JU.T3,JU.BS");
Clazz.defineMethod (c$, "invertAtomCoordPt", 
function (pt, bs) {
this.ms.invertSelected (pt, null, -1, null, bs);
this.checkMinimization ();
this.sm.setStatusAtomMoved (bs);
}, "JU.P3,JU.BS");
Clazz.defineMethod (c$, "invertAtomCoordPlane", 
function (plane, bs) {
this.ms.invertSelected (null, plane, -1, null, bs);
this.checkMinimization ();
this.sm.setStatusAtomMoved (bs);
}, "JU.P4,JU.BS");
Clazz.defineMethod (c$, "invertSelected", 
function (pt, plane, iAtom, invAtoms) {
var bs = this.bsA ();
if (bs.cardinality () == 0) return;
this.ms.invertSelected (pt, plane, iAtom, invAtoms, bs);
this.checkMinimization ();
this.sm.setStatusAtomMoved (bs);
}, "JU.P3,JU.P4,~N,JU.BS");
Clazz.defineMethod (c$, "moveAtoms", 
function (mNew, rotation, translation, center, isInternal, bsAtoms, translationOnly) {
if (bsAtoms.cardinality () == 0) return;
this.ms.moveAtoms (mNew, rotation, translation, bsAtoms, center, isInternal, translationOnly);
this.checkMinimization ();
this.sm.setStatusAtomMoved (bsAtoms);
}, "JU.M3,JU.M3,JU.V3,JU.P3,~B,JU.BS,~B");
Clazz.defineMethod (c$, "moveSelected", 
function (deltaX, deltaY, deltaZ, x, y, bsSelected, isTranslation, asAtoms) {
if (deltaZ == 0) return;
if (x == -2147483648) this.rotateBondIndex = -1;
if (this.isJmolDataFrame ()) return;
if (deltaX == -2147483648) {
this.showSelected = true;
this.shm.loadShape (8);
this.refresh (6, "moveSelected");
return;
}if (deltaX == 2147483647) {
if (!this.showSelected) return;
this.showSelected = false;
this.refresh (6, "moveSelected");
return;
}if (this.movingSelected) return;
this.movingSelected = true;
this.stopMinimization ();
if (this.rotateBondIndex >= 0 && x != -2147483648) {
this.actionRotateBond (deltaX, deltaY, x, y);
} else {
bsSelected = this.setMovableBitSet (bsSelected, !asAtoms);
if (bsSelected.cardinality () != 0) {
if (isTranslation) {
var ptCenter = this.ms.getAtomSetCenter (bsSelected);
this.tm.finalizeTransformParameters ();
var f = (this.g.antialiasDisplay ? 2 : 1);
var ptScreen = this.tm.transformPt (ptCenter);
var ptScreenNew;
if (deltaZ != -2147483648) ptScreenNew = JU.P3.new3 (ptScreen.x, ptScreen.y, ptScreen.z + deltaZ + 0.5);
 else ptScreenNew = JU.P3.new3 (ptScreen.x + deltaX * f + 0.5, ptScreen.y + deltaY * f + 0.5, ptScreen.z);
var ptNew =  new JU.P3 ();
this.tm.unTransformPoint (ptScreenNew, ptNew);
ptNew.sub (ptCenter);
this.setAtomCoordsRelative (ptNew, bsSelected);
} else {
this.tm.rotateXYBy (deltaX, deltaY, bsSelected);
}}}this.refresh (2, "");
this.movingSelected = false;
}, "~N,~N,~N,~N,~N,JU.BS,~B,~B");
Clazz.defineMethod (c$, "highlightBond", 
function (index, isHover) {
if (isHover && !this.hoverEnabled) return;
var bs = null;
if (index >= 0) {
var b = this.ms.bo[index];
var i = b.getAtomIndex2 ();
if (!this.isAtomAssignable (i)) return;
bs = JU.BSUtil.newAndSetBit (i);
bs.set (b.getAtomIndex1 ());
}this.highlight (bs);
this.refresh (3, "highlightBond");
}, "~N,~B");
Clazz.defineMethod (c$, "highlight", 
function (bs) {
if (bs != null) this.shm.loadShape (8);
this.setShapeProperty (8, "highlight", bs);
}, "JU.BS");
Clazz.defineMethod (c$, "setRotateBondIndex", 
function (index) {
var haveBond = (this.rotateBondIndex >= 0);
if (!haveBond && index < 0) return;
this.rotatePrev1 = -1;
this.bsRotateBranch = null;
if (index == -2147483648) return;
this.rotateBondIndex = index;
this.highlightBond (index, false);
}, "~N");
Clazz.defineMethod (c$, "getRotateBondIndex", 
function () {
return this.rotateBondIndex;
});
Clazz.defineMethod (c$, "actionRotateBond", 
function (deltaX, deltaY, x, y) {
if (this.rotateBondIndex < 0) return;
var bsBranch = this.bsRotateBranch;
var atom1;
var atom2;
if (bsBranch == null) {
var b = this.ms.bo[this.rotateBondIndex];
atom1 = b.getAtom1 ();
atom2 = b.getAtom2 ();
this.undoMoveActionClear (atom1.i, 2, true);
var pt = JU.P3.new3 (x, y, Clazz.doubleToInt ((atom1.sZ + atom2.sZ) / 2));
this.tm.unTransformPoint (pt, pt);
if (atom2.getCovalentBondCount () == 1 || pt.distance (atom1) < pt.distance (atom2) && atom1.getCovalentBondCount () != 1) {
var a = atom1;
atom1 = atom2;
atom2 = a;
}if (JU.Measure.computeAngleABC (pt, atom1, atom2, true) > 90 || JU.Measure.computeAngleABC (pt, atom2, atom1, true) > 90) {
bsBranch = this.getBranchBitSet (atom2.i, atom1.i, true);
}if (bsBranch != null) for (var n = 0, i = atom1.getBonds ().length; --i >= 0; ) {
if (bsBranch.get (atom1.getBondedAtomIndex (i)) && ++n == 2) {
bsBranch = null;
break;
}}
if (bsBranch == null) {
bsBranch = this.ms.getMoleculeBitSetForAtom (atom1.i);
}this.bsRotateBranch = bsBranch;
this.rotatePrev1 = atom1.i;
this.rotatePrev2 = atom2.i;
} else {
atom1 = this.ms.at[this.rotatePrev1];
atom2 = this.ms.at[this.rotatePrev2];
}var v1 = JU.V3.new3 (atom2.sX - atom1.sX, atom2.sY - atom1.sY, 0);
var v2 = JU.V3.new3 (deltaX, deltaY, 0);
v1.cross (v1, v2);
var degrees = (v1.z > 0 ? 1 : -1) * v2.length ();
var bs = JU.BSUtil.copy (bsBranch);
bs.andNot (this.slm.getMotionFixedAtoms ());
this.rotateAboutPointsInternal (this.eval, atom1, atom2, 0, degrees, false, bs, null, null, null);
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "refreshMeasures", 
function (andStopMinimization) {
this.setShapeProperty (6, "refresh", null);
if (andStopMinimization) this.stopMinimization ();
}, "~B");
Clazz.defineMethod (c$, "functionXY", 
function (functionName, nX, nY) {
var data = null;
if (functionName.indexOf ("file:") == 0) data = this.getFileAsString (functionName.substring (5), false);
 else if (functionName.indexOf ("data2d_") != 0) return this.sm.functionXY (functionName, nX, nY);
nX = Math.abs (nX);
nY = Math.abs (nY);
var fdata;
if (data == null) {
fdata = this.getDataFloat2D (functionName);
if (fdata != null) return fdata;
data = "";
}fdata =  Clazz.newFloatArray (nX, nY, 0);
var f =  Clazz.newFloatArray (nX * nY, 0);
JU.Parser.parseStringInfestedFloatArray (data, null, f);
for (var i = 0, n = 0; i < nX; i++) for (var j = 0; j < nY; j++) fdata[i][j] = f[n++];


return fdata;
}, "~S,~N,~N");
Clazz.defineMethod (c$, "functionXYZ", 
function (functionName, nX, nY, nZ) {
var data = null;
if (functionName.indexOf ("file:") == 0) data = this.getFileAsString (functionName.substring (5), false);
 else if (functionName.indexOf ("data3d_") != 0) return this.sm.functionXYZ (functionName, nX, nY, nZ);
nX = Math.abs (nX);
nY = Math.abs (nY);
nZ = Math.abs (nZ);
var xyzdata;
if (data == null) {
xyzdata = this.getDataFloat3D (functionName);
if (xyzdata != null) return xyzdata;
data = "";
}xyzdata =  Clazz.newFloatArray (nX, nY, nZ, 0);
var f =  Clazz.newFloatArray (nX * nY * nZ, 0);
JU.Parser.parseStringInfestedFloatArray (data, null, f);
for (var i = 0, n = 0; i < nX; i++) for (var j = 0; j < nY; j++) for (var k = 0; k < nZ; k++) xyzdata[i][j][k] = f[n++];



return xyzdata;
}, "~S,~N,~N,~N");
Clazz.overrideMethod (c$, "extractMolData", 
function (what) {
if (what == null) {
var i = this.am.cmi;
if (i < 0) return null;
what = this.getModelNumberDotted (i);
}return this.getModelExtract (what, true, false, "V2000");
}, "~S");
Clazz.defineMethod (c$, "getNMRPredict", 
function (openURL) {
var molFile = this.getModelExtract ("selected", true, false, "V2000");
var pt = molFile.indexOf ("\n");
molFile = "Jmol " + JV.Viewer.version_date + molFile.substring (pt);
if (openURL) {
if (this.$isApplet) {
this.showUrl (this.g.nmrUrlFormat + molFile);
} else {
this.syncScript ("true", "*", 0);
this.syncScript ("H1Simulate:", ".", 0);
}return null;
}var url = this.g.nmrPredictFormat + molFile;
return this.getFileAsString (url, false);
}, "~B");
Clazz.defineMethod (c$, "getHelp", 
function (what) {
if (this.g.helpPath.indexOf ("?") < 0) {
if (what.length > 0 && what.indexOf ("?") != 0) what = "?search=" + JU.PT.rep (what, " ", "%20");
what += (what.length == 0 ? "?ver=" : "&ver=") + JV.JC.version;
} else {
what = "&" + what;
}this.showUrl (this.g.helpPath + what);
}, "~S");
Clazz.defineMethod (c$, "show2D", 
function (smiles) {
this.showUrl (this.setLoadFormat ("_" + smiles, '2', false));
}, "~S");
Clazz.defineMethod (c$, "getChemicalInfo", 
function (smiles, t) {
var info = null;
var type = '/';
switch ((t == null ? 1073742035 : t.tok)) {
case 1073741977:
type = 'I';
break;
case 1073741978:
type = 'K';
break;
case 1073742159:
type = 'T';
break;
case 1073742160:
type = 'S';
break;
case 1073742035:
type = 'N';
break;
default:
info = JS.SV.sValue (t);
}
var s = this.setLoadFormat ("_" + smiles, type, false);
if (type == '/') s += JU.PT.rep (info, " ", "%20");
return this.getFileAsString4 (s, -1, false, false, false);
}, "~S,JS.T");
Clazz.defineMethod (c$, "addCommand", 
function (command) {
if (this.autoExit || !this.haveDisplay || !this.getPreserveState ()) return;
this.commandHistory.addCommand (JU.PT.replaceAllCharacters (command, "\r\n\t", " "));
}, "~S");
Clazz.defineMethod (c$, "removeCommand", 
function () {
return this.commandHistory.removeCommand ();
});
Clazz.overrideMethod (c$, "getSetHistory", 
function (howFarBack) {
return this.commandHistory.getSetHistory (howFarBack);
}, "~N");
Clazz.defineMethod (c$, "getOutputChannel", 
function (localName, fullPath) {
return this.getOutputManager ().getOutputChannel (localName, fullPath);
}, "~S,~A");
Clazz.overrideMethod (c$, "writeTextFile", 
function (fileName, data) {
var params =  new java.util.Hashtable ();
params.put ("fileName", fileName);
params.put ("type", "txt");
params.put ("text", data);
this.outputToFile (params);
}, "~S,~S");
Clazz.overrideMethod (c$, "clipImageOrPasteText", 
function (text) {
if (!this.haveAccess (JV.Viewer.ACCESS.ALL)) return "no";
return this.getOutputManager ().clipImageOrPasteText (text);
}, "~S");
Clazz.overrideMethod (c$, "getClipboardText", 
function () {
if (!this.haveAccess (JV.Viewer.ACCESS.ALL)) return "no";
try {
return this.getOutputManager ().getClipboardText ();
} catch (er) {
if (Clazz.exceptionOf (er, Error)) {
return J.i18n.GT._ ("clipboard is not accessible -- use signed applet");
} else {
throw er;
}
}
});
Clazz.defineMethod (c$, "processWriteOrCapture", 
function (params) {
return this.getOutputManager ().processWriteOrCapture (params);
}, "java.util.Map");
Clazz.defineMethod (c$, "createZip", 
function (fileName, type, data) {
var params =  new java.util.Hashtable ();
params.put ("fileName", fileName);
params.put ("type", type);
params.put ("text", this.getStateInfo ());
if (Clazz.instanceOf (data, Array)) params.put ("scripts", data);
 else if (Clazz.instanceOf (data, JU.Lst)) params.put ("imageData", data);
return this.getOutputManager ().outputToFile (params);
}, "~S,~S,~O");
Clazz.overrideMethod (c$, "outputToFile", 
function (params) {
return this.getOutputManager ().outputToFile (params);
}, "java.util.Map");
Clazz.defineMethod (c$, "getOutputManager", 
 function () {
if (this.outputManager != null) return this.outputManager;
return (this.outputManager = J.api.Interface.getInterface ("JV.OutputManager" + (this.isJS && !this.isWebGL ? "JS" : "Awt"))).setViewer (this, this.privateKey);
});
Clazz.defineMethod (c$, "setSyncTarget", 
 function (mode, TF) {
switch (mode) {
case 0:
this.sm.syncingMouse = TF;
break;
case 1:
this.sm.syncingScripts = TF;
break;
case 2:
this.sm.syncSend (TF ? "GET_GRAPHICS" : "SET_GRAPHICS_OFF", "*", 0);
if (Float.isNaN (this.tm.stereoDegrees)) this.setFloatProperty ("stereoDegrees", -5);
if (TF) {
this.setBooleanProperty ("_syncMouse", false);
this.setBooleanProperty ("_syncScript", false);
}return;
}
if (!this.sm.syncingScripts && !this.sm.syncingMouse) this.refresh (-1, "set sync");
}, "~N,~B");
Clazz.overrideMethod (c$, "syncScript", 
function (script, applet, port) {
this.getStateCreator ().syncScript (script, applet, port);
}, "~S,~S,~N");
Clazz.overrideMethod (c$, "getModelIndexFromId", 
function (id) {
return this.ms.getModelIndexFromId (id);
}, "~S");
Clazz.defineMethod (c$, "setSyncDriver", 
function (mode) {
this.sm.setSyncDriver (mode);
}, "~N");
Clazz.defineMethod (c$, "getPartialCharges", 
function () {
return this.ms.getPartialCharges ();
});
Clazz.defineMethod (c$, "setProteinType", 
function (type, bs) {
this.ms.setProteinType (bs == null ? this.bsA () : bs, type);
}, "J.c.STR,JU.BS");
Clazz.overrideMethod (c$, "getBondPoint3f1", 
function (i) {
return this.ms.getBondAtom1 (i);
}, "~N");
Clazz.overrideMethod (c$, "getBondPoint3f2", 
function (i) {
return this.ms.getBondAtom2 (i);
}, "~N");
Clazz.defineMethod (c$, "getVanderwaalsMar", 
function (i) {
return (this.defaultVdw === J.c.VDW.USER ? this.userVdwMars[i] : JU.Elements.getVanderwaalsMar (i, this.defaultVdw));
}, "~N");
Clazz.defineMethod (c$, "getVanderwaalsMarType", 
function (atomicAndIsotopeNumber, type) {
if (type == null) type = this.defaultVdw;
 else switch (type) {
case J.c.VDW.AUTO:
case J.c.VDW.AUTO_BABEL:
case J.c.VDW.AUTO_JMOL:
case J.c.VDW.AUTO_RASMOL:
if (this.defaultVdw !== J.c.VDW.AUTO) type = this.defaultVdw;
break;
default:
break;
}
if (type === J.c.VDW.USER && this.bsUserVdws == null) type = J.c.VDW.JMOL;
return (type === J.c.VDW.USER ? this.userVdwMars[atomicAndIsotopeNumber & 127] : JU.Elements.getVanderwaalsMar (atomicAndIsotopeNumber, type));
}, "~N,J.c.VDW");
Clazz.defineMethod (c$, "setVdwStr", 
function (name) {
var type = J.c.VDW.getVdwType (name);
if (type == null) type = J.c.VDW.AUTO;
switch (type) {
case J.c.VDW.JMOL:
case J.c.VDW.BABEL:
case J.c.VDW.RASMOL:
case J.c.VDW.AUTO:
case J.c.VDW.USER:
break;
default:
type = J.c.VDW.JMOL;
}
if (type !== this.defaultVdw && type === J.c.VDW.USER && this.bsUserVdws == null) this.setUserVdw (this.defaultVdw);
this.defaultVdw = type;
this.g.setO ("defaultVDW", type.getVdwLabel ());
}, "~S");
Clazz.defineMethod (c$, "setUserVdw", 
function (mode) {
this.userVdwMars =  Clazz.newIntArray (JU.Elements.elementNumberMax, 0);
this.userVdws =  Clazz.newFloatArray (JU.Elements.elementNumberMax, 0);
this.bsUserVdws =  new JU.BS ();
if (mode === J.c.VDW.USER) mode = J.c.VDW.JMOL;
for (var i = 1; i < JU.Elements.elementNumberMax; i++) {
this.userVdwMars[i] = JU.Elements.getVanderwaalsMar (i, mode);
this.userVdws[i] = this.userVdwMars[i] / 1000;
}
}, "J.c.VDW");
Clazz.defineMethod (c$, "getDefaultVdwNameOrData", 
function (mode, type, bs) {
switch (mode) {
case -2147483648:
return this.defaultVdw.getVdwLabel ();
case 2147483647:
if ((bs = this.bsUserVdws) == null) return "";
type = J.c.VDW.USER;
break;
}
if (type == null || type === J.c.VDW.AUTO) type = this.defaultVdw;
if (type === J.c.VDW.USER && this.bsUserVdws == null) this.setUserVdw (this.defaultVdw);
return this.getDataManager ().getDefaultVdwNameOrData (type, bs);
}, "~N,J.c.VDW,JU.BS");
Clazz.defineMethod (c$, "deleteAtoms", 
function (bsAtoms, fullModels) {
var atomIndex = (bsAtoms == null ? -1 : bsAtoms.nextSetBit (0));
if (atomIndex < 0) return 0;
this.clearModelDependentObjects ();
if (!fullModels) {
this.sm.modifySend (atomIndex, this.ms.at[atomIndex].mi, 4, "deleting atom " + this.getAtomName (atomIndex));
this.ms.deleteAtoms (bsAtoms);
var n = this.slm.deleteAtoms (bsAtoms);
this.setTainted (true);
this.sm.modifySend (atomIndex, this.ms.at[atomIndex].mi, -4, "OK");
return n;
}return this.deleteModels (this.ms.at[atomIndex].mi, bsAtoms);
}, "JU.BS,~B");
Clazz.defineMethod (c$, "deleteModels", 
function (modelIndex, bsAtoms) {
this.clearModelDependentObjects ();
this.sm.modifySend (-1, modelIndex, 5, "deleting model " + this.getModelNumberDotted (modelIndex));
this.setCurrentModelIndexClear (0, false);
this.am.setAnimationOn (false);
var bsD0 = JU.BSUtil.copy (this.getDeletedAtoms ());
var bsModels = (bsAtoms == null ? JU.BSUtil.newAndSetBit (modelIndex) : this.ms.getModelBS (bsAtoms, false));
var bsDeleted = this.ms.deleteModels (bsModels);
this.slm.processDeletedModelAtoms (bsDeleted);
if (this.eval != null) this.eval.deleteAtomsInVariables (bsDeleted);
this.setAnimationRange (0, 0);
this.clearRepaintManager (-1);
this.am.clear ();
this.am.initializePointers (1);
this.setCurrentModelIndexClear (this.getModelCount () > 1 ? -1 : 0, this.getModelCount () > 1);
this.hoverAtomIndex = -1;
this.setFileLoadStatus (J.c.FIL.DELETED, null, null, null, null, null);
this.refreshMeasures (true);
if (bsD0 != null) bsDeleted.andNot (bsD0);
this.sm.modifySend (-1, modelIndex, -5, "OK");
return JU.BSUtil.cardinalityOf (bsDeleted);
}, "~N,JU.BS");
Clazz.defineMethod (c$, "deleteBonds", 
function (bsDeleted) {
var modelIndex = this.ms.getBondModelIndex (bsDeleted.nextSetBit (0));
this.sm.modifySend (-1, modelIndex, 2, "delete bonds " + JU.Escape.eBond (bsDeleted));
this.ms.deleteBonds (bsDeleted, false);
this.sm.modifySend (-1, modelIndex, -2, "OK");
}, "JU.BS");
Clazz.defineMethod (c$, "deleteModelAtoms", 
function (modelIndex, firstAtomIndex, nAtoms, bsModelAtoms) {
this.sm.modifySend (-1, modelIndex, 1, "delete atoms " + JU.Escape.eBS (bsModelAtoms));
JU.BSUtil.deleteBits (this.getFrameOffsets (), bsModelAtoms);
this.setFrameOffsets (this.getFrameOffsets ());
this.getDataManager ().deleteModelAtoms (firstAtomIndex, nAtoms, bsModelAtoms);
this.sm.modifySend (-1, modelIndex, -1, "OK");
}, "~N,~N,~N,JU.BS");
Clazz.defineMethod (c$, "getDeletedAtoms", 
function () {
return this.slm.getDeletedAtoms ();
});
Clazz.defineMethod (c$, "getQuaternionFrame", 
function () {
return this.g.quaternionFrame.charAt (this.g.quaternionFrame.length == 2 ? 1 : 0);
});
Clazz.defineMethod (c$, "calculatePointGroup", 
function () {
return this.ms.calculatePointGroup (this.bsA ());
});
Clazz.defineMethod (c$, "getPointGroupInfo", 
function (atomExpression) {
return this.ms.getPointGroupInfo (this.getAtomBitSet (atomExpression));
}, "~O");
Clazz.defineMethod (c$, "getPointGroupAsString", 
function (asDraw, type, index, scale) {
return this.ms.getPointGroupAsString (this.bsA (), asDraw, type, index, scale);
}, "~B,~S,~N,~N");
Clazz.defineMethod (c$, "loadImageData", 
function (image, nameOrError, echoName, sc) {
if (image == null) this.scriptEcho (nameOrError);
if (echoName == null) {
this.setBackgroundImage ((image == null ? null : nameOrError), image);
} else {
this.shm.loadShape (30);
this.setShapeProperty (30, "text", nameOrError);
if (image != null) this.setShapeProperty (30, "image", image);
}if (this.isJS && sc != null) {
sc.mustResumeEval = true;
this.eval.resumeEval (sc);
}}, "~O,~S,~S,JS.ScriptContext");
Clazz.defineMethod (c$, "cd", 
function (dir) {
if (dir == null) {
dir = ".";
} else if (dir.length == 0) {
this.setStringProperty ("defaultDirectory", "");
dir = ".";
}dir = this.fm.getDefaultDirectory (dir + (dir.equals ("=") ? "" : dir.endsWith ("/") ? "X.spt" : "/X.spt"));
if (dir.length > 0) this.setStringProperty ("defaultDirectory", dir);
var path = this.fm.getFilePath (dir + "/", true, false);
if (path.startsWith ("file:/")) JV.FileManager.setLocalPath (this, dir, false);
return dir;
}, "~S");
Clazz.defineMethod (c$, "setErrorMessage", 
function (errMsg, errMsgUntranslated) {
this.errorMessageUntranslated = errMsgUntranslated;
if (errMsg != null) this.eval.stopScriptThreads ();
return (this.errorMessage = errMsg);
}, "~S,~S");
Clazz.overrideMethod (c$, "getErrorMessage", 
function () {
return this.errorMessage;
});
Clazz.overrideMethod (c$, "getErrorMessageUn", 
function () {
return this.errorMessageUntranslated == null ? this.errorMessage : this.errorMessageUntranslated;
});
Clazz.defineMethod (c$, "setShapeErrorState", 
function (shapeID, state) {
this.currentShapeID = shapeID;
this.currentShapeState = state;
}, "~N,~S");
Clazz.defineMethod (c$, "getShapeErrorState", 
function () {
if (this.currentShapeID < 0) return "";
if (this.ms != null) this.shm.releaseShape (this.currentShapeID);
this.clearRepaintManager (this.currentShapeID);
return JV.JC.getShapeClassName (this.currentShapeID, false) + " " + this.currentShapeState;
});
Clazz.defineMethod (c$, "handleError", 
function (er, doClear) {
try {
if (doClear) this.zapMsg ("" + er);
this.undoClear ();
if (JU.Logger.getLogLevel () == 0) JU.Logger.setLogLevel (4);
this.setCursor (0);
this.setBooleanProperty ("refreshing", true);
this.fm.setPathForAllFiles ("");
JU.Logger.error ("vwr handling error condition: " + er + "  ");
if (!this.isJS) er.printStackTrace ();
this.notifyError ("Error", "doClear=" + doClear + "; " + er, "" + er);
} catch (e1) {
try {
JU.Logger.error ("Could not notify error " + er + ": due to " + e1);
} catch (er2) {
}
}
}, "Error,~B");
Clazz.defineMethod (c$, "getAtomicCharges", 
function () {
return this.ms.getAtomicCharges ();
});
Clazz.defineMethod (c$, "getFunctions", 
function (isStatic) {
return (isStatic ? JV.Viewer.staticFunctions : this.localFunctions);
}, "~B");
Clazz.defineMethod (c$, "removeFunction", 
function (name) {
name = name.toLowerCase ();
var $function = this.getFunction (name);
if ($function == null) return;
JV.Viewer.staticFunctions.remove (name);
this.localFunctions.remove (name);
}, "~S");
Clazz.defineMethod (c$, "getFunction", 
function (name) {
if (name == null) return null;
var $function = (JV.Viewer.isStaticFunction (name) ? JV.Viewer.staticFunctions : this.localFunctions).get (name);
return ($function == null || $function.geTokens () == null ? null : $function);
}, "~S");
c$.isStaticFunction = Clazz.defineMethod (c$, "isStaticFunction", 
 function (name) {
return name.startsWith ("static_");
}, "~S");
Clazz.defineMethod (c$, "isFunction", 
function (name) {
return (JV.Viewer.isStaticFunction (name) ? JV.Viewer.staticFunctions : this.localFunctions).containsKey (name);
}, "~S");
Clazz.defineMethod (c$, "clearFunctions", 
function () {
JV.Viewer.staticFunctions.clear ();
this.localFunctions.clear ();
});
Clazz.defineMethod (c$, "addFunction", 
function ($function) {
var name = $function.getName ();
(JV.Viewer.isStaticFunction (name) ? JV.Viewer.staticFunctions : this.localFunctions).put (name, $function);
}, "J.api.JmolScriptFunction");
Clazz.defineMethod (c$, "getFunctionCalls", 
function (selectedFunction) {
return this.getStateCreator ().getFunctionCalls (selectedFunction);
}, "~S");
Clazz.defineMethod (c$, "warn", 
function (s) {
if (!this.isPrintOnly) JU.Logger.warn (s);
}, "~S");
Clazz.defineMethod (c$, "getMoInfo", 
function (modelIndex) {
return this.ms.getMoInfo (modelIndex);
}, "~N");
Clazz.defineMethod (c$, "checkPrivateKey", 
function (privateKey) {
return privateKey == this.privateKey;
}, "~N");
Clazz.defineMethod (c$, "bindAction", 
function (desc, name) {
if (this.haveDisplay) this.actionManager.bind (desc, name);
}, "~S,~S");
Clazz.defineMethod (c$, "unBindAction", 
function (desc, name) {
if (this.haveDisplay) this.actionManager.unbindAction (desc, name);
}, "~S,~S");
Clazz.defineMethod (c$, "getPlaneIntersection", 
function (type, plane, scale, flags) {
return this.ms.getPlaneIntersection (type, plane, scale, flags, type == 1614417948 ? this.getCurrentUnitCell () : null);
}, "~N,JU.P4,~N,~N");
Clazz.defineMethod (c$, "calculateStruts", 
function (bs1, bs2) {
return this.ms.calculateStruts (bs1 == null ? this.bsA () : bs1, bs2 == null ? this.bsA () : bs2);
}, "JU.BS,JU.BS");
Clazz.defineMethod (c$, "getPreserveState", 
function () {
return (this.g.preserveState && this.scm != null);
});
Clazz.defineMethod (c$, "isKiosk", 
function () {
return this.$isKiosk;
});
Clazz.defineMethod (c$, "hasFocus", 
function () {
return (this.haveDisplay && (this.$isKiosk || this.apiPlatform.hasFocus (this.display)));
});
Clazz.defineMethod (c$, "setFocus", 
function () {
if (this.haveDisplay && !this.apiPlatform.hasFocus (this.display)) this.apiPlatform.requestFocusInWindow (this.display);
});
Clazz.defineMethod (c$, "stopMinimization", 
function () {
if (this.minimizer != null) {
this.minimizer.setProperty ("stop", null);
}});
Clazz.defineMethod (c$, "clearMinimization", 
function () {
if (this.minimizer != null) this.minimizer.setProperty ("clear", null);
});
Clazz.defineMethod (c$, "getMinimizationInfo", 
function () {
return (this.minimizer == null ? "" : this.minimizer.getProperty ("log", 0));
});
Clazz.defineMethod (c$, "checkMinimization", 
 function () {
this.refreshMeasures (true);
if (!this.g.monitorEnergy) return;
this.minimize (0, 0, this.getAllAtoms (), null, 0, false, false, true, false);
this.echoMessage (this.getP ("_minimizationForceField") + " Energy = " + this.getP ("_minimizationEnergy"));
});
Clazz.defineMethod (c$, "minimize", 
function (steps, crit, bsSelected, bsFixed, rangeFixed, addHydrogen, isOnly, isSilent, isLoad2D) {
var ff = this.g.forceField;
var bsInFrame = this.getModelUndeletedAtomsBitSetBs (this.getVisibleFramesBitSet ());
if (bsSelected == null) bsSelected = this.getModelUndeletedAtomsBitSet (this.getVisibleFramesBitSet ().length () - 1);
 else bsSelected.and (bsInFrame);
if (rangeFixed <= 0) rangeFixed = 5.0;
var bsMotionFixed = JU.BSUtil.copy (bsFixed == null ? this.slm.getMotionFixedAtoms () : bsFixed);
var haveFixed = (bsMotionFixed.cardinality () > 0);
if (haveFixed) bsSelected.andNot (bsMotionFixed);
var bsNearby = (isOnly ?  new JU.BS () : this.ms.getAtomsWithinRadius (rangeFixed, bsSelected, true, null));
bsNearby.andNot (bsSelected);
if (haveFixed) {
bsMotionFixed.and (bsNearby);
} else {
bsMotionFixed = bsNearby;
}bsMotionFixed.and (bsInFrame);
if (addHydrogen) bsSelected.or (this.addHydrogens (bsSelected, isLoad2D, isSilent));
if (bsSelected.cardinality () > 200) {
JU.Logger.error ("Too many atoms for minimization (>200)");
return;
}try {
if (!isSilent) JU.Logger.info ("Minimizing " + bsSelected.cardinality () + " atoms");
this.getMinimizer (true).minimize (steps, crit, bsSelected, bsMotionFixed, haveFixed, isSilent, ff);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.error ("Minimization error: " + e.toString ());
if (!this.isJS) e.printStackTrace ();
} else {
throw e;
}
}
}, "~N,~N,JU.BS,JU.BS,~N,~B,~B,~B,~B");
Clazz.defineMethod (c$, "setMotionFixedAtoms", 
function (bs) {
this.slm.setMotionFixedAtoms (bs);
}, "JU.BS");
Clazz.defineMethod (c$, "getMotionFixedAtoms", 
function () {
return this.slm.getMotionFixedAtoms ();
});
Clazz.defineMethod (c$, "getAtomicPropertyState", 
function (commands, type, bs, name, data) {
this.getStateCreator ().getAtomicPropertyStateBuffer (commands, type, bs, name, data);
}, "JU.SB,~N,JU.BS,~S,~A");
Clazz.defineMethod (c$, "getCenterAndPoints", 
function (atomSets, addCenter) {
return this.ms.getCenterAndPoints (atomSets, addCenter);
}, "JU.Lst,~B");
Clazz.defineMethod (c$, "writeFileData", 
function (fileName, type, modelIndex, parameters) {
return this.getOutputManager ().writeFileData (fileName, type, modelIndex, parameters);
}, "~S,~S,~N,~A");
Clazz.defineMethod (c$, "getPdbData", 
function (modelIndex, type, bsAtoms, parameters, oc, getStructure) {
return this.getPropertyManager ().getPdbData (modelIndex, type, bsAtoms == null ? this.bsA () : bsAtoms, parameters, oc, getStructure);
}, "~N,~S,JU.BS,~A,JU.OC,~B");
Clazz.defineMethod (c$, "getGroupsWithin", 
function (nResidues, bs) {
return this.ms.getGroupsWithin (nResidues, bs);
}, "~N,JU.BS");
Clazz.defineMethod (c$, "getExecutor", 
function () {
if (this.executor != null || JV.Viewer.nProcessors < 2) return this.executor;
try {
this.executor = (J.api.Interface.getInterface ("JS.ScriptParallelProcessor")).getExecutor ();
} catch (e$$) {
if (Clazz.exceptionOf (e$$, Exception)) {
var e = e$$;
{
this.executor = null;
}
} else if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
this.executor = null;
}
} else {
throw e$$;
}
}
if (this.executor == null) JU.Logger.error ("parallel processing is not available");
return this.executor;
});
Clazz.defineMethod (c$, "setShapeSize", 
function (shapeID, mad, bsSelected) {
if (bsSelected == null) bsSelected = this.bsA ();
this.shm.setShapeSizeBs (shapeID, mad, null, bsSelected);
}, "~N,~N,JU.BS");
Clazz.defineMethod (c$, "setShapeProperty", 
function (shapeID, propertyName, value) {
if (shapeID < 0) return;
this.shm.setShapePropertyBs (shapeID, propertyName, value, null);
}, "~N,~S,~O");
Clazz.defineMethod (c$, "getShapeProperty", 
function (shapeType, propertyName) {
return this.shm.getShapePropertyIndex (shapeType, propertyName, -2147483648);
}, "~N,~S");
Clazz.defineMethod (c$, "getShapePropertyAsInt", 
 function (shapeID, propertyName) {
var value = this.getShapeProperty (shapeID, propertyName);
return value == null || !(Clazz.instanceOf (value, Integer)) ? -2147483648 : (value).intValue ();
}, "~N,~S");
Clazz.defineMethod (c$, "setModelVisibility", 
function () {
if (this.shm == null) return;
this.shm.setModelVisibility ();
});
Clazz.defineMethod (c$, "resetShapes", 
function (andCreateNew) {
this.shm.resetShapes ();
if (andCreateNew) {
this.shm.loadDefaultShapes (this.ms);
this.clearRepaintManager (-1);
}}, "~B");
Clazz.defineMethod (c$, "setParallel", 
function (TF) {
return (this.$isParallel = this.g.multiProcessor && TF);
}, "~B");
Clazz.defineMethod (c$, "isParallel", 
function () {
return this.g.multiProcessor && this.$isParallel;
});
Clazz.defineMethod (c$, "getRenderableBitSet", 
function () {
return this.shm.getRenderableBitSet ();
});
Clazz.defineMethod (c$, "setAtomPickingOption", 
 function (option) {
if (this.haveDisplay) this.actionManager.setAtomPickingOption (option);
}, "~S");
Clazz.defineMethod (c$, "setBondPickingOption", 
 function (option) {
if (this.haveDisplay) this.actionManager.setBondPickingOption (option);
}, "~S");
Clazz.defineMethod (c$, "undoClear", 
function () {
this.actionStates.clear ();
this.actionStatesRedo.clear ();
});
Clazz.defineMethod (c$, "undoMoveAction", 
function (action, n) {
this.getStateCreator ().undoMoveAction (action, n);
}, "~N,~N");
Clazz.defineMethod (c$, "undoMoveActionClear", 
function (taintedAtom, type, clearRedo) {
if (!this.g.preserveState) return;
this.getStateCreator ().undoMoveActionClear (taintedAtom, type, clearRedo);
}, "~N,~N,~B");
Clazz.defineMethod (c$, "moveAtomWithHydrogens", 
function (atomIndex, deltaX, deltaY, deltaZ, bsAtoms) {
this.stopMinimization ();
if (bsAtoms == null) {
var atom = this.ms.at[atomIndex];
bsAtoms = JU.BSUtil.newAndSetBit (atomIndex);
var bonds = atom.getBonds ();
if (bonds != null) for (var i = 0; i < bonds.length; i++) {
var atom2 = bonds[i].getOtherAtom (atom);
if (atom2.getElementNumber () == 1) bsAtoms.set (atom2.i);
}
}this.moveSelected (deltaX, deltaY, deltaZ, -2147483648, -2147483648, bsAtoms, true, true);
}, "~N,~N,~N,~N,JU.BS");
Clazz.defineMethod (c$, "isAtomPDB", 
function (i) {
return this.ms.isAtomPDB (i);
}, "~N");
Clazz.defineMethod (c$, "isModelPDB", 
function (i) {
return this.ms.am[i].isBioModel;
}, "~N");
Clazz.defineMethod (c$, "isAtomAssignable", 
function (i) {
return this.ms.isAtomAssignable (i);
}, "~N");
Clazz.overrideMethod (c$, "deleteMeasurement", 
function (i) {
this.setShapeProperty (6, "delete", Integer.$valueOf (i));
}, "~N");
Clazz.defineMethod (c$, "haveModelKit", 
function () {
return this.ms.haveModelKit ();
});
Clazz.defineMethod (c$, "getModelKitStateBitSet", 
function (bs, bsDeleted) {
return this.ms.getModelKitStateBitset (bs, bsDeleted);
}, "JU.BS,JU.BS");
Clazz.overrideMethod (c$, "getSmiles", 
function (bs) {
return this.getSmilesOpt (bs, -1, -1, false, false, false, false, false);
}, "JU.BS");
Clazz.defineMethod (c$, "getSmilesOpt", 
function (bsSelected, index1, index2, explicitH, isBioSmiles, bioAllowUnmatchedRings, bioAddCrossLinks, bioAddComment) {
var atoms = this.ms.at;
if (bsSelected == null) {
if (index1 < 0 || index2 < 0) {
bsSelected = this.bsA ();
} else {
if (isBioSmiles) {
if (index1 > index2) {
var i = index1;
index1 = index2;
index2 = i;
}index1 = atoms[index1].getGroup ().firstAtomIndex;
index2 = atoms[index2].getGroup ().lastAtomIndex;
}bsSelected =  new JU.BS ();
bsSelected.setBits (index1, index2 + 1);
}}var bioComment = (bioAddComment ? JV.Viewer.getJmolVersion () + " " + this.getModelName (this.am.cmi) : null);
return this.getSmilesMatcher ().getSmiles (atoms, this.getAtomCount (), bsSelected, isBioSmiles, bioAllowUnmatchedRings, bioAddCrossLinks, bioComment, explicitH);
}, "JU.BS,~N,~N,~B,~B,~B,~B,~B");
Clazz.defineMethod (c$, "connect", 
function (connections) {
this.ms.connect (connections);
}, "~A");
Clazz.defineMethod (c$, "prompt", 
function (label, data, list, asButtons) {
return (this.$isKiosk ? "null" : this.apiPlatform.prompt (label, data, list, asButtons));
}, "~S,~S,~A,~B");
Clazz.defineMethod (c$, "initializeExporter", 
function (params) {
var isJS = params.get ("type").equals ("JS");
if (isJS) {
if (this.jsExporter3D != null) {
this.jsExporter3D.initializeOutput (this, this.privateKey, this.gdata, params);
return this.jsExporter3D;
}} else {
var fileName = params.get ("fileName");
var fullPath = params.get ("fullPath");
var out = this.getOutputChannel (fileName, fullPath);
if (out == null) return null;
params.put ("outputChannel", out);
}var export3D = J.api.Interface.getOption ("export.Export3D");
if (export3D == null) return null;
var exporter = export3D.initializeExporter (this, this.privateKey, this.gdata, params);
if (isJS && exporter != null) this.jsExporter3D = export3D;
return (exporter == null ? null : export3D);
}, "java.util.Map");
Clazz.defineMethod (c$, "getMouseEnabled", 
function () {
return this.refreshing && !this.creatingImage;
});
Clazz.overrideMethod (c$, "calcAtomsMinMax", 
function (bs, boxInfo) {
this.ms.calcAtomsMinMax (bs, boxInfo);
}, "JU.BS,JU.BoxInfo");
Clazz.defineMethod (c$, "getObjectMap", 
function (map, c) {
switch (c) {
case '{':
if (this.getScriptManager () != null) {
var m = map;
var sets = this.eval.getDefinedAtomSets ();
if (sets != null) m.putAll (sets);
JS.T.getTokensType (m, 3145728);
}return;
case '$':
case '0':
this.shm.getObjectMap (map, c == '$');
return;
}
}, "java.util.Map,~S");
Clazz.defineMethod (c$, "getPdbBondInfo", 
function (group3) {
return JV.JC.getPdbBondInfo (group3, this.g.legacyHAddition);
}, "~S");
Clazz.defineMethod (c$, "setPicked", 
function (iAtom) {
this.g.setPicked (iAtom);
}, "~N");
Clazz.overrideMethod (c$, "runScript", 
function (script) {
var outputBuffer =  new JU.SB ();
try {
if (this.getScriptManager () == null) return null;
this.eval.runScriptBuffer (script, outputBuffer);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return this.eval.getErrorMessage ();
} else {
throw e;
}
}
return outputBuffer.toString ();
}, "~S");
Clazz.defineMethod (c$, "allowSpecAtom", 
function () {
return this.ms.allowSpecAtom ();
});
Clazz.defineMethod (c$, "setFrameDelayMs", 
function (millis) {
this.ms.setFrameDelayMs (millis, this.getVisibleFramesBitSet ());
}, "~N");
Clazz.defineMethod (c$, "getFrameDelayMs", 
function (i) {
return this.ms.getFrameDelayMs (i);
}, "~N");
Clazz.defineMethod (c$, "getBaseModelBitSet", 
function () {
return this.ms.getModelAtomBitSetIncludingDeleted (this.getJDXBaseModelIndex (this.am.cmi), true);
});
Clazz.defineMethod (c$, "getTimeouts", 
function () {
return this.timeouts;
});
Clazz.defineMethod (c$, "clearTimeouts", 
function () {
if (this.timeouts != null) J.thread.TimeoutThread.clear (this.timeouts);
});
Clazz.defineMethod (c$, "setTimeout", 
function (name, mSec, script) {
if (!this.haveDisplay || this.isHeadless () || this.autoExit) return;
if (name == null) {
this.clearTimeouts ();
return;
}if (this.timeouts == null) {
this.timeouts =  new java.util.Hashtable ();
}J.thread.TimeoutThread.setTimeout (this, this.timeouts, name, mSec, script);
}, "~S,~N,~S");
Clazz.defineMethod (c$, "triggerTimeout", 
function (name) {
if (!this.haveDisplay || this.timeouts == null) return;
J.thread.TimeoutThread.trigger (this.timeouts, name);
}, "~S");
Clazz.defineMethod (c$, "clearTimeout", 
function (name) {
this.setTimeout (name, 0, null);
}, "~S");
Clazz.defineMethod (c$, "showTimeout", 
function (name) {
return (this.haveDisplay ? J.thread.TimeoutThread.showTimeout (this.timeouts, name) : "");
}, "~S");
Clazz.defineMethod (c$, "calculatePartialCharges", 
function (bsSelected) {
if (bsSelected == null || bsSelected.cardinality () == 0) bsSelected = this.getModelUndeletedAtomsBitSetBs (this.getVisibleFramesBitSet ());
this.getMinimizer (true).calculatePartialCharges (this.ms.bo, this.ms.bondCount, this.ms.at, bsSelected);
}, "JU.BS");
Clazz.defineMethod (c$, "setCurrentModelID", 
function (id) {
var modelIndex = this.am.cmi;
if (modelIndex >= 0) this.ms.setInfo (modelIndex, "modelID", id);
}, "~S");
Clazz.defineMethod (c$, "setCentroid", 
function (bs, minmax) {
this.ms.setCentroid (bs, minmax);
}, "JU.BS,~A");
Clazz.defineMethod (c$, "getPathForAllFiles", 
function () {
return this.fm.getPathForAllFiles ();
});
Clazz.defineMethod (c$, "cacheGet", 
function (key) {
return this.fm.cacheGet (key, false);
}, "~S");
Clazz.defineMethod (c$, "cacheClear", 
function () {
this.fm.cacheClear ();
this.ligandModelSet = null;
this.ligandModels = null;
this.ms.clearCache ();
});
Clazz.defineMethod (c$, "cachePut", 
function (key, data) {
JU.Logger.info ("Viewer cachePut " + key);
this.fm.cachePut (key, data);
}, "~S,~O");
Clazz.defineMethod (c$, "cacheFileByName", 
function (fileName, isAdd) {
if (fileName == null) {
this.cacheClear ();
return -1;
}return this.fm.cacheFileByNameAdd (fileName, isAdd);
}, "~S,~B");
Clazz.defineMethod (c$, "cacheList", 
function () {
return this.fm.cacheList ();
});
Clazz.defineMethod (c$, "clearThreads", 
function () {
if (this.eval != null) this.eval.stopScriptThreads ();
this.stopMinimization ();
this.tm.clearThreads ();
this.setAnimationOn (false);
});
Clazz.defineMethod (c$, "getEvalContextAndHoldQueue", 
function (jse) {
if (jse == null || !this.isJS) return null;
jse.pushContextDown ("getEvalContextAndHoldQueue");
var sc = jse.getThisContext ();
sc.setMustResume ();
sc.isJSThread = true;
this.queueOnHold = true;
return sc;
}, "J.api.JmolScriptEvaluator");
Clazz.defineMethod (c$, "checkInheritedShapes", 
function () {
this.shm.checkInheritedShapes ();
});
Clazz.overrideMethod (c$, "resizeInnerPanel", 
function (width, height) {
if (this.autoExit || !this.haveDisplay) {
this.setScreenDimension (width, height);
return this.dimScreen;
}return this.sm.resizeInnerPanel (width, height);
}, "~N,~N");
Clazz.defineMethod (c$, "getFontLineShapeState", 
function (s, myType, tickInfos) {
return this.getStateCreator ().getFontLineShapeState (s, myType, tickInfos);
}, "~S,~S,~A");
Clazz.defineMethod (c$, "getShapeSetState", 
function (atomShape, shape, monomerCount, monomers, bsSizeDefault, temp, temp2) {
this.getStateCreator ().getShapeSetState (atomShape, shape, monomerCount, monomers, bsSizeDefault, temp, temp2);
}, "J.shape.AtomShape,J.shape.Shape,~N,~A,JU.BS,java.util.Map,java.util.Map");
Clazz.defineMethod (c$, "getMeasurementState", 
function (measures, mList, measurementCount, font3d, ti) {
return this.getStateCreator ().getMeasurementState (measures, mList, measurementCount, font3d, ti);
}, "J.shape.Measures,JU.Lst,~N,javajs.awt.Font,JM.TickInfo");
Clazz.defineMethod (c$, "getBondState", 
function (shape, bsOrderSet, reportAll) {
return this.getStateCreator ().getBondState (shape, bsOrderSet, reportAll);
}, "J.shape.Shape,JU.BS,~B");
Clazz.defineMethod (c$, "getAtomShapeSetState", 
function (shape, shapes) {
return this.getStateCreator ().getAtomShapeSetState (shape, shapes);
}, "J.shape.Shape,~A");
Clazz.defineMethod (c$, "getShapeState", 
function (shape) {
return this.getStateCreator ().getShapeState (shape);
}, "J.shape.Shape");
Clazz.defineMethod (c$, "getAtomShapeState", 
function (shape) {
return this.getStateCreator ().getAtomShapeState (shape);
}, "J.shape.AtomShape");
Clazz.defineMethod (c$, "getDefaultPropertyParam", 
function (propertyID) {
return this.getPropertyManager ().getDefaultPropertyParam (propertyID);
}, "~N");
Clazz.defineMethod (c$, "getPropertyNumber", 
function (name) {
return this.getPropertyManager ().getPropertyNumber (name);
}, "~S");
Clazz.defineMethod (c$, "checkPropertyParameter", 
function (name) {
return this.getPropertyManager ().checkPropertyParameter (name);
}, "~S");
Clazz.defineMethod (c$, "extractProperty", 
function (property, args, pt) {
return this.getPropertyManager ().extractProperty (property, args, pt, null, false);
}, "~O,~O,~N");
Clazz.defineMethod (c$, "addHydrogens", 
function (bsAtoms, is2DLoad, isSilent) {
var doAll = (bsAtoms == null);
if (bsAtoms == null) bsAtoms = this.getModelUndeletedAtomsBitSet (this.getVisibleFramesBitSet ().length () - 1);
var bsB =  new JU.BS ();
if (bsAtoms.cardinality () == 0) return bsB;
var modelIndex = this.ms.at[bsAtoms.nextSetBit (0)].mi;
if (modelIndex != this.ms.mc - 1) return bsB;
var vConnections =  new JU.Lst ();
var pts = this.getAdditionalHydrogens (bsAtoms, doAll, false, vConnections);
var wasAppendNew = false;
wasAppendNew = this.g.appendNew;
if (pts.length > 0) {
this.clearModelDependentObjects ();
try {
bsB = (is2DLoad ? this.ms.addHydrogens (vConnections, pts) : this.addHydrogensInline (bsAtoms, vConnections, pts));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println (e.toString ());
} else {
throw e;
}
}
if (wasAppendNew) this.g.appendNew = true;
}if (!isSilent) this.scriptStatus (J.i18n.GT.i (J.i18n.GT._ ("{0} hydrogens added"), pts.length));
return bsB;
}, "JU.BS,~B,~B");
Clazz.defineMethod (c$, "addHydrogensInline", 
function (bsAtoms, vConnections, pts) {
if (this.getScriptManager () == null) return null;
return this.scm.addHydrogensInline (bsAtoms, vConnections, pts);
}, "JU.BS,JU.Lst,~A");
Clazz.overrideMethod (c$, "evalFunctionFloat", 
function (func, params, values) {
return (this.getScriptManager () == null ? 0 : this.eval.evalFunctionFloat (func, params, values));
}, "~O,~O,~A");
Clazz.defineMethod (c$, "evalParallel", 
function (context, shapeManager) {
this.displayLoadErrors = false;
var isOK = this.getScriptManager () != null && this.eval.evalParallel (context, (shapeManager == null ? this.shm : shapeManager));
this.displayLoadErrors = true;
return isOK;
}, "JS.ScriptContext,JV.ShapeManager");
Clazz.overrideMethod (c$, "evaluateExpression", 
function (stringOrTokens) {
return (this.getScriptManager () == null ? null : this.eval.evaluateExpression (stringOrTokens, false, false));
}, "~O");
Clazz.defineMethod (c$, "evaluateExpressionAsVariable", 
function (stringOrTokens) {
return (this.getScriptManager () == null ? null : this.eval.evaluateExpression (stringOrTokens, true, false));
}, "~O");
Clazz.defineMethod (c$, "getAtomBitSet", 
function (atomExpression) {
if (Clazz.instanceOf (atomExpression, JU.BS)) return this.excludeAtoms (atomExpression, false);
this.getScriptManager ();
return this.getAtomBitSetEval (this.eval, atomExpression);
}, "~O");
Clazz.defineMethod (c$, "getAtomBitSetVector", 
function (atomExpression) {
return (this.getScriptManager () == null ? null : this.eval.getAtomBitSetVector (this.getAtomCount (), atomExpression));
}, "~O");
Clazz.defineMethod (c$, "getContextVariables", 
function () {
return (this.getScriptManager () == null ? null : this.eval.getContextVariables ());
});
Clazz.defineMethod (c$, "getScriptContext", 
function (why) {
return (this.getScriptManager () == null ? null : this.eval.getScriptContext (why));
}, "~S");
Clazz.overrideMethod (c$, "getAtomDefs", 
function (names) {
return this.getStateCreator ().getAtomDefs (names);
}, "java.util.Map");
Clazz.defineMethod (c$, "setCGO", 
function (info) {
this.shm.loadShape (23);
this.shm.setShapePropertyBs (23, "setCGO", info, null);
}, "JU.Lst");
Clazz.defineMethod (c$, "setModelSet", 
function (modelSet) {
this.ms = this.mm.modelSet = modelSet;
}, "JM.ModelSet");
Clazz.defineMethod (c$, "setObjectProp", 
function (id, tokCommand) {
this.getScriptManager ();
if (id == null) id = "*";
return (this.eval == null ? null : this.eval.setObjectPropSafe (id, tokCommand));
}, "~S,~N");
Clazz.defineMethod (c$, "getSceneList", 
function () {
try {
return this.ms.getInfoM ("scenes");
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
});
Clazz.defineMethod (c$, "getDihedralMap", 
function (atoms) {
return this.ms.getDihedralMap (atoms);
}, "~A");
Clazz.defineMethod (c$, "setDihedrals", 
function (dihedralList, bsBranches, rate) {
if (bsBranches == null) bsBranches = this.getBsBranches (dihedralList);
this.ms.setDihedrals (dihedralList, bsBranches, rate);
}, "~A,~A,~N");
Clazz.defineMethod (c$, "getBsBranches", 
function (dihedralList) {
return this.ms.getBsBranches (dihedralList);
}, "~A");
Clazz.defineMethod (c$, "getChainID", 
function (id) {
var iboxed = this.chainMap.get (id);
if (iboxed != null) return iboxed.intValue ();
var i = id.charCodeAt (0);
if (id.length > 1) {
i = 256 + this.chainList.size ();
this.chainList.addLast (id);
}iboxed = Integer.$valueOf (i);
this.chainMap.put (iboxed, id);
this.chainMap.put (id, iboxed);
return i;
}, "~S");
Clazz.defineMethod (c$, "getChainIDStr", 
function (id) {
return this.chainMap.get (Integer.$valueOf (id));
}, "~N");
Clazz.defineMethod (c$, "getScriptQueueInfo", 
function () {
return (this.scm != null && this.scm.isQueueProcessing () ? Boolean.TRUE : Boolean.FALSE);
});
Clazz.defineMethod (c$, "getNMRCalculation", 
function () {
return (this.nmrCalculation == null ? (this.nmrCalculation = J.api.Interface.getOption ("quantum.NMRCalculation")).setViewer (this) : this.nmrCalculation);
});
Clazz.defineMethod (c$, "getDistanceUnits", 
function (s) {
if (s == null) s = this.getDefaultMeasurementLabel (2);
var pt = s.indexOf ("//");
return (pt < 0 ? this.g.measureDistanceUnits : s.substring (pt + 2));
}, "~S");
Clazz.defineMethod (c$, "calculateFormalCharges", 
function (bs) {
if (bs == null) bs = this.bsA ();
return this.ms.fixFormalCharges (bs);
}, "JU.BS");
Clazz.defineMethod (c$, "cachePngFiles", 
function () {
return (!this.getTestFlag (1));
});
Clazz.defineMethod (c$, "setModulation", 
function (bs, isOn, t1, isQ) {
if (isQ) this.g.setO ("_modt", JU.Escape.eP (t1));
this.ms.setModulation (bs == null ? this.getAllAtoms () : bs, isOn, t1, isQ);
this.refreshMeasures (true);
}, "JU.BS,~B,JU.P3,~B");
Clazz.defineMethod (c$, "checkInMotion", 
function (state) {
switch (state) {
case 0:
this.setTimeout ("_SET_IN_MOTION_", 0, null);
break;
case 1:
if (!this.inMotion) this.setTimeout ("_SET_IN_MOTION_", this.g.hoverDelayMs * 2, "!setInMotion");
break;
case 2:
this.setInMotion (true);
this.refresh (3, "timeoutThread set in motion");
break;
}
}, "~N");
Clazz.defineMethod (c$, "checkMotionRendering", 
function (tok) {
if (!this.getInMotion (true) && !this.tm.spinOn && !this.tm.vibrationOn && !this.am.animationOn) return true;
if (this.g.wireframeRotation) return false;
var n = 0;
switch (tok) {
case 1678770178:
case 1141899265:
n = 2;
break;
case 1113198596:
n = 3;
break;
case 1113198597:
n = 4;
break;
case 1113200642:
n = 5;
break;
case 1073742018:
n = 6;
break;
case 603979967:
n = 7;
break;
case 603979786:
n = 8;
break;
}
return this.g.platformSpeed >= n;
}, "~N");
Clazz.defineMethod (c$, "openExportChannel", 
function (privateKey, fileName, asWriter) {
return this.getOutputManager ().openOutputChannel (privateKey, fileName, asWriter, false);
}, "~N,~S,~B");
Clazz.overrideMethod (c$, "log", 
function (data) {
if (data != null) this.getOutputManager ().logToFile (data);
}, "~S");
Clazz.defineMethod (c$, "getLogFileName", 
function () {
return (this.logFileName == null ? "" : this.logFileName);
});
Clazz.defineMethod (c$, "getCommands", 
function (htDefine, htMore, select) {
return this.getStateCreator ().getCommands (htDefine, htMore, select);
}, "java.util.Map,java.util.Map,~S");
Clazz.defineMethod (c$, "allowCapture", 
function () {
return !this.$isApplet || this.$isSignedApplet;
});
Clazz.defineMethod (c$, "getHtml5Applet", 
function () {
return this.html5Applet;
});
Clazz.defineMethod (c$, "compileExpr", 
function (expr) {
var o = (this.getScriptManager () == null ? null : this.eval.evaluateExpression (expr, false, true));
return (Clazz.instanceOf (o, Array) ? o : [JS.T.o (4, expr)]);
}, "~S");
Clazz.defineMethod (c$, "checkSelect", 
function (h, value) {
return this.getScriptManager () != null && this.eval.checkSelect (h, value);
}, "java.util.Map,~A");
Clazz.defineMethod (c$, "getAnnotationInfo", 
function (d, match, type) {
return this.getAnnotationParser ().getAnnotationInfo (this, d, match, type, this.am.cmi);
}, "JS.SV,~S,~N");
Clazz.defineMethod (c$, "getAtomValidation", 
function (type, atom) {
return this.getAnnotationParser ().getAtomValidation (this, type, atom);
}, "~S,JM.Atom");
Clazz.pu$h(self.c$);
c$ = Clazz.declareType (JV.Viewer, "ACCESS", Enum);
Clazz.defineEnumConstant (c$, "NONE", 0, []);
Clazz.defineEnumConstant (c$, "READSPT", 1, []);
Clazz.defineEnumConstant (c$, "ALL", 2, []);
c$ = Clazz.p0p ();
{
{
self.Jmol && Jmol.extend && Jmol.extend("vwr", JV.Viewer.prototype);
}}Clazz.defineStatics (c$,
"appletDocumentBase", "",
"appletCodeBase", "",
"appletIdiomaBase", null,
"jsDocumentBase", "");
c$.strJavaVendor = c$.prototype.strJavaVendor = "Java: " + System.getProperty ("java.vendor", "j2s");
c$.strOSName = c$.prototype.strOSName = System.getProperty ("os.name", "");
c$.strJavaVersion = c$.prototype.strJavaVersion = "Java " + System.getProperty ("java.version", "");
Clazz.defineStatics (c$,
"version_date", null,
"SYNC_GRAPHICS_MESSAGE", "GET_GRAPHICS",
"SYNC_NO_GRAPHICS_MESSAGE", "SET_GRAPHICS_OFF");
c$.staticFunctions = c$.prototype.staticFunctions =  new java.util.Hashtable ();
Clazz.defineStatics (c$,
"nProcessors", 1);
{
{
}}});
