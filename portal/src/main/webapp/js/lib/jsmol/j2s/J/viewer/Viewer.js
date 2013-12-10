Clazz.declarePackage ("J.viewer");
Clazz.load (["java.lang.Enum", "J.api.JmolViewer", "J.atomdata.AtomDataServer", "java.util.Hashtable", "J.atomdata.RadiusData", "J.i18n.GT", "J.util.CommandHistory", "$.Dimension", "$.JmolList"], "J.viewer.Viewer", ["java.io.BufferedReader", "$.StringReader", "java.lang.Boolean", "$.Character", "$.Double", "$.Float", "J.adapter.smarter.SmarterJmolAdapter", "J.api.Interface", "J.constant.EnumAxesMode", "$.EnumFileStatus", "$.EnumStereoMode", "$.EnumVdw", "J.io.CifDataReader", "$.JmolBinary", "J.modelset.Group", "J.script.SV", "$.T", "J.thread.TimeoutThread", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.C", "$.ColorUtil", "$.Elements", "$.Escape", "$.GData", "$.JmolMolecule", "$.Logger", "$.Measure", "$.P3", "$.P3i", "$.Parser", "$.SB", "$.TempArray", "$.TextFormat", "$.V3", "J.viewer.ActionManager", "$.AnimationManager", "$.ColorManager", "$.DataManager", "$.FileManager", "$.JC", "$.ModelManager", "$.SelectionManager", "$.ShapeManager", "$.StateManager", "$.StatusManager", "$.TransformManager", "J.viewer.binding.Binding"], function () {
c$ = Clazz.decorateAsClass (function () {
this.autoExit = false;
this.haveDisplay = false;
this.isJS = false;
this.isWebGL = false;
this.isSingleThreaded = false;
this.queueOnHold = false;
this.fullName = "";
this.appletDocumentBase = "";
this.appletCodeBase = "";
this.compiler = null;
this.definedAtomSets = null;
this.modelSet = null;
this.fileManager = null;
this.$isApplet = false;
this.isSyntaxAndFileCheck = false;
this.isSyntaxCheck = false;
this.listCommands = false;
this.mustRender = false;
this.htmlName = "";
this.insertedCommand = "";
this.gdata = null;
this.applet = null;
this.actionManager = null;
this.animationManager = null;
this.colorManager = null;
this.dataManager = null;
this.shapeManager = null;
this.selectionManager = null;
this.repaintManager = null;
this.global = null;
this.statusManager = null;
this.transformManager = null;
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
this.viewerOptions = null;
this.display = null;
this.modelAdapter = null;
this.access = null;
this.commandHistory = null;
this.symmetry = null;
this.smilesMatcher = null;
this.modelManager = null;
this.stateManager = null;
this.scriptManager = null;
this.eval = null;
this.tempArray = null;
this.$isPreviewOnly = false;
this.mouse = null;
this.mouseEnabled = true;
this.noneSelected = false;
this.ligandModels = null;
this.ligandModelSet = null;
this.sc = null;
this.bsFrameOffsets = null;
this.frameOffsets = null;
this.motionEventNumber = 0;
this.inMotion = false;
this.refreshing = true;
this.axesAreTainted = false;
this.dimScreen = null;
this.maximumSize = 2147483647;
this.imageFontScaling = 1;
this.antialiasDisplay = false;
this.hoverAtomIndex = -1;
this.hoverText = null;
this.hoverEnabled = true;
this.currentCursor = 0;
this.prevFrame = -2147483648;
this.haveJDX = false;
this.jsv = null;
this.language = null;
this.rd = null;
this.frankOn = true;
this.scriptEditorVisible = false;
this.appConsole = null;
this.scriptEditor = null;
this.jmolpopup = null;
this.modelkitPopup = null;
this.headlessImage = null;
this.pm = null;
this.isTainted = true;
this.movingSelected = false;
this.showSelected = false;
this.rotateBondIndex = -1;
this.rotatePrev1 = -1;
this.rotatePrev2 = -1;
this.bsRotateBranch = null;
this.creatingImage = false;
this.errorMessage = null;
this.errorMessageUntranslated = null;
this.currentShapeID = -1;
this.currentShapeState = null;
this.localFunctions = null;
this.privateKey = 0;
this.$isKiosk = false;
this.minimizer = null;
this.executor = null;
this.displayLoadErrors = true;
this.$isParallel = false;
this.actionStates = null;
this.actionStatesRedo = null;
this.stateScriptVersionInt = 0;
this.jsExporter3D = null;
this.htPdbBondInfo = null;
this.timeouts = null;
this.chainMap = null;
this.chainList = null;
this.nmrCalculation = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "Viewer", J.api.JmolViewer, J.atomdata.AtomDataServer);
Clazz.prepareFields (c$, function () {
this.commandHistory =  new J.util.CommandHistory ();
this.dimScreen =  new J.util.Dimension ();
this.language = J.i18n.GT.getLanguage ();
this.rd =  new J.atomdata.RadiusData (null, 0, null, null);
this.localFunctions =  new java.util.Hashtable ();
this.privateKey = Math.random ();
this.actionStates =  new J.util.JmolList ();
this.actionStatesRedo =  new J.util.JmolList ();
this.chainMap =  new java.util.Hashtable ();
this.chainList =  new J.util.JmolList ();
});
$_M(c$, "finalize", 
function () {
if (J.util.Logger.debugging) J.util.Logger.debug ("viewer finalize " + this);
Clazz.superCall (this, J.viewer.Viewer, "finalize", []);
});
Clazz.overrideMethod (c$, "isApplet", 
function () {
return this.$isApplet;
});
$_M(c$, "setInsertedCommand", 
function (strScript) {
this.insertedCommand = strScript;
}, "~S");
$_M(c$, "getLogFilePath", 
function () {
return this.logFilePath;
});
$_M(c$, "isSignedApplet", 
function () {
return this.$isSignedApplet;
});
c$.getJmolVersion = Clazz.overrideMethod (c$, "getJmolVersion", 
function () {
return (J.viewer.Viewer.version_date == null ? ($t$ = J.viewer.Viewer.version_date = J.viewer.JC.version + "  " + J.viewer.JC.date, J.viewer.Viewer.prototype.version_date = J.viewer.Viewer.version_date, $t$) : J.viewer.Viewer.version_date);
});
c$.allocateViewer = $_M(c$, "allocateViewer", 
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
return  new J.viewer.Viewer (info);
}, "~O,J.api.JmolAdapter,~S,java.net.URL,java.net.URL,~S,J.api.JmolStatusListener,J.api.ApiPlatform");
Clazz.makeConstructor (c$, 
function (info) {
Clazz.superConstructor (this, J.viewer.Viewer, []);
this.setOptions (info);
}, "java.util.Map");
$_M(c$, "getGlobalSettings", 
function () {
return this.global;
});
$_M(c$, "getStatusManager", 
function () {
return this.statusManager;
});
$_M(c$, "isRestricted", 
function (a) {
return this.access === a;
}, "J.viewer.Viewer.ACCESS");
Clazz.overrideMethod (c$, "getModelAdapter", 
function () {
if (this.modelAdapter == null) this.modelAdapter =  new J.adapter.smarter.SmarterJmolAdapter ();
return this.modelAdapter;
});
$_M(c$, "getSymmetry", 
function () {
if (this.symmetry == null) this.symmetry = J.api.Interface.getOptionInterface ("symmetry.Symmetry");
return this.symmetry;
});
$_M(c$, "getSymmetryInfo", 
function (bsAtoms, xyz, op, pt, pt2, id, type) {
return this.getPropertyManager ().getSymmetryInfo (bsAtoms, xyz, op, pt, pt2, id, type);
}, "J.util.BS,~S,~N,J.util.P3,J.util.P3,~S,~N");
$_M(c$, "getSmilesMatcher", 
function () {
if (this.smilesMatcher == null) {
this.smilesMatcher = J.api.Interface.getOptionInterface ("smiles.SmilesMatcher");
}return this.smilesMatcher;
});
Clazz.overrideMethod (c$, "getSmartsMatch", 
function (smarts, bsSelected) {
if (bsSelected == null) bsSelected = this.getSelectionSet (false);
return this.getSmilesMatcher ().getSubstructureSet (smarts, this.modelSet.atoms, this.getAtomCount (), bsSelected, true, false);
}, "~S,J.util.BS");
$_M(c$, "getViewerOptions", 
function () {
return this.viewerOptions;
});
$_M(c$, "setOptions", 
($fz = function (info) {
this.viewerOptions = info;
if (J.util.Logger.debugging) {
J.util.Logger.debug ("Viewer constructor " + this);
}this.modelAdapter = info.get ("adapter");
var statusListener = info.get ("statusListener");
this.fullName = info.get ("fullName");
if (this.fullName == null) this.fullName = "";
var o = info.get ("codeBase");
this.appletCodeBase = (o == null ? "" : o.toString ());
o = info.get ("documentBase");
this.appletDocumentBase = (o == null ? "" : o.toString ());
o = info.get ("options");
this.commandOptions = (o == null ? "" : o.toString ());
if (info.containsKey ("debug") || this.commandOptions.indexOf ("-debug") >= 0) J.util.Logger.setLogLevel (5);
this.$isSignedApplet = this.checkOption2 ("signedApplet", "-signed");
this.$isApplet = this.$isSignedApplet || this.checkOption2 ("applet", "-applet");
this.allowScripting = !this.checkOption2 ("noscripting", "-noscripting");
var i = this.fullName.indexOf ("__");
this.htmlName = (i < 0 ? this.fullName : this.fullName.substring (0, i));
this.syncId = (i < 0 ? "" : this.fullName.substring (i + 2, this.fullName.length - 2));
if (this.$isApplet) {
{
if(typeof Jmol != "undefined") this.applet =
Jmol._applets[this.htmlName.split("_object")[0]];
}if (info.containsKey ("maximumSize")) this.setMaximumSize ((info.get ("maximumSize")).intValue ());
}this.access = (this.checkOption2 ("access:READSPT", "-r") ? J.viewer.Viewer.ACCESS.READSPT : this.checkOption2 ("access:NONE", "-R") ? J.viewer.Viewer.ACCESS.NONE : J.viewer.Viewer.ACCESS.ALL);
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
o = J.api.Interface.getInterface (platform);
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
if (o == null && !this.isWebGL) o = J.api.Interface.getInterface ("J.g3d.Graphics3D");
this.gdata = (o == null ?  new J.util.GData () : o);
this.gdata.initialize (this.apiPlatform);
this.stateManager =  new J.viewer.StateManager (this);
this.colorManager =  new J.viewer.ColorManager (this, this.gdata);
this.statusManager =  new J.viewer.StatusManager (this);
this.transformManager =  new J.viewer.TransformManager (this, 2147483647, 0);
this.selectionManager =  new J.viewer.SelectionManager (this);
if (this.haveDisplay) {
this.actionManager = (this.multiTouch ? J.api.Interface.getOptionInterface ("multitouch.ActionManagerMT") :  new J.viewer.ActionManager ());
this.actionManager.setViewer (this, this.commandOptions + "-multitouch-" + info.get ("multiTouch"));
this.mouse = this.apiPlatform.getMouseManager (this, this.actionManager);
if (this.multiTouch && !this.checkOption2 ("-simulated", "-simulated")) this.apiPlatform.setTransparentCursor (this.display);
}this.modelManager =  new J.viewer.ModelManager (this);
this.shapeManager =  new J.viewer.ShapeManager (this);
this.tempArray =  new J.util.TempArray ();
this.dataManager =  new J.viewer.DataManager (this);
this.animationManager =  new J.viewer.AnimationManager (this);
o = info.get ("repaintManager");
if (o == null) o = (J.api.Interface.getOptionInterface ("render.RepaintManager"));
if (o != null && !o.equals ("")) (this.repaintManager = o).set (this, this.shapeManager);
this.initialize (true);
this.fileManager =  new J.viewer.FileManager (this);
this.definedAtomSets =  new java.util.Hashtable ();
this.setJmolStatusListener (statusListener);
if (this.$isApplet) {
J.util.Logger.info ("viewerOptions: \n" + J.util.Escape.escapeMap (this.viewerOptions));
($t$ = J.viewer.Viewer.jsDocumentBase = this.appletDocumentBase, J.viewer.Viewer.prototype.jsDocumentBase = J.viewer.Viewer.jsDocumentBase, $t$);
i = J.viewer.Viewer.jsDocumentBase.indexOf ("#");
if (i >= 0) ($t$ = J.viewer.Viewer.jsDocumentBase = J.viewer.Viewer.jsDocumentBase.substring (0, i), J.viewer.Viewer.prototype.jsDocumentBase = J.viewer.Viewer.jsDocumentBase, $t$);
i = J.viewer.Viewer.jsDocumentBase.lastIndexOf ("?");
if (i >= 0) ($t$ = J.viewer.Viewer.jsDocumentBase = J.viewer.Viewer.jsDocumentBase.substring (0, i), J.viewer.Viewer.prototype.jsDocumentBase = J.viewer.Viewer.jsDocumentBase, $t$);
i = J.viewer.Viewer.jsDocumentBase.lastIndexOf ("/");
if (i >= 0) ($t$ = J.viewer.Viewer.jsDocumentBase = J.viewer.Viewer.jsDocumentBase.substring (0, i), J.viewer.Viewer.prototype.jsDocumentBase = J.viewer.Viewer.jsDocumentBase, $t$);
this.fileManager.setAppletContext (this.appletDocumentBase);
var appletProxy = info.get ("appletProxy");
if (appletProxy != null) this.setStringProperty ("appletProxy", appletProxy);
if (this.$isSignedApplet) {
this.logFilePath = J.util.TextFormat.simpleReplace (this.appletCodeBase, "file://", "");
this.logFilePath = J.util.TextFormat.simpleReplace (this.logFilePath, "file:/", "");
if (this.logFilePath.indexOf ("//") >= 0) this.logFilePath = null;
 else this.isSignedAppletLocal = true;
} else {
this.logFilePath = null;
}} else {
this.gdata.setBackgroundTransparent (this.checkOption2 ("backgroundTransparent", "-b"));
this.isSilent = this.checkOption2 ("silent", "-i");
if (this.isSilent) J.util.Logger.setLogLevel (3);
this.isSyntaxAndFileCheck = this.checkOption2 ("checkLoad", "-C");
this.isSyntaxCheck = this.isSyntaxAndFileCheck || this.checkOption2 ("check", "-c");
this.listCommands = this.checkOption2 ("listCommands", "-l");
this.autoExit = this.checkOption2 ("exit", "-x");
this.cd (".");
if (this.isHeadless ()) {
this.headlessImage = info.get ("headlessImage");
o = info.get ("headlistMaxTimeMs");
if (o == null) o = Integer.$valueOf (60000);
this.setTimeout ("" + Math.random (), (o).intValue (), "exitJmol");
}}this.useCommandThread = !this.isHeadless () && this.checkOption2 ("useCommandThread", "-threaded");
this.setStartupBooleans ();
this.setIntProperty ("_nProcessors", J.viewer.Viewer.nProcessors);
o = info.get ("menuFile");
if (o != null) this.getProperty ("DATA_API", "setMenu", this.getFileAsString (o));
if (!this.isSilent) {
J.util.Logger.info ("(C) 2012 Jmol Development" + "\nJmol Version: " + J.viewer.Viewer.getJmolVersion () + "\njava.vendor: " + J.viewer.Viewer.strJavaVendor + "\njava.version: " + J.viewer.Viewer.strJavaVersion + "\nos.name: " + J.viewer.Viewer.strOSName + "\nAccess: " + this.access + "\nmemory: " + this.getParameter ("_memory") + "\nprocessors available: " + J.viewer.Viewer.nProcessors + "\nuseCommandThread: " + this.useCommandThread + (!this.$isApplet ? "" : "\nappletId:" + this.htmlName + (this.$isSignedApplet ? " (signed)" : "")));
}this.zap (false, true, false);
this.global.setS ("language", J.i18n.GT.getLanguage ());
this.stateManager.setJmolDefaults ();
}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "setDisplay", 
function (canvas) {
this.display = canvas;
this.apiPlatform.setViewer (this, canvas);
}, "~O");
$_M(c$, "getScriptManager", 
($fz = function () {
if (this.allowScripting && this.scriptManager == null) {
this.scriptManager = J.api.Interface.getOptionInterface ("script.ScriptManager");
this.scriptManager.setViewer (this);
this.eval = this.scriptManager.getEval ();
if (this.useCommandThread) this.scriptManager.startCommandWatcher (true);
}return this.scriptManager;
}, $fz.isPrivate = true, $fz));
$_M(c$, "checkOption2", 
($fz = function (key1, key2) {
return (this.viewerOptions.containsKey (key1) || this.commandOptions.indexOf (key2) >= 0);
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "isPreviewOnly", 
function () {
return this.$isPreviewOnly;
});
$_M(c$, "isHeadless", 
function () {
return this.apiPlatform.isHeadless ();
});
$_M(c$, "setStartupBooleans", 
($fz = function () {
this.setBooleanProperty ("_applet", this.$isApplet);
this.setBooleanProperty ("_jspecview", false);
this.setBooleanProperty ("_signedApplet", this.$isSignedApplet);
this.setBooleanProperty ("_headless", this.apiPlatform.isHeadless ());
this.setStringProperty ("_restrict", "\"" + this.access + "\"");
this.setBooleanProperty ("_useCommandThread", this.useCommandThread);
}, $fz.isPrivate = true, $fz));
$_M(c$, "noGraphicsAllowed", 
function () {
return this.$noGraphicsAllowed;
});
$_M(c$, "getExportDriverList", 
function () {
return (this.isRestricted (J.viewer.Viewer.ACCESS.ALL) ? this.global.getParameter ("exportDrivers") : "");
});
$_M(c$, "getHtmlName", 
function () {
return this.htmlName;
});
Clazz.overrideMethod (c$, "getDisplay", 
function () {
return this.display;
});
$_M(c$, "clearMouse", 
function () {
this.mouse.clear ();
});
$_M(c$, "disposeMouse", 
function () {
this.mouse.dispose ();
this.mouse = null;
});
Clazz.overrideMethod (c$, "handleOldJvm10Event", 
function (id, x, y, modifiers, time) {
return this.mouse.handleOldJvm10Event (id, x, y, modifiers, time);
}, "~N,~N,~N,~N,~N");
$_M(c$, "reset", 
function (includingSpin) {
this.modelSet.calcBoundBoxDimensions (null, 1);
this.axesAreTainted = true;
this.transformManager.homePosition (includingSpin);
if (this.modelSet.setCrystallographicDefaults ()) this.stateManager.setCrystallographicDefaults ();
 else this.setAxesModeMolecular (false);
this.prevFrame = -2147483648;
if (!this.getSpinOn ()) this.refresh (-1, "Viewer:homePosition()");
}, "~B");
Clazz.overrideMethod (c$, "homePosition", 
function () {
this.evalString ("reset spin");
});
$_M(c$, "getAppletInfo", 
function () {
var info =  new java.util.Hashtable ();
info.put ("htmlName", this.htmlName);
info.put ("syncId", this.syncId);
info.put ("fullName", this.fullName);
if (this.$isApplet) {
info.put ("documentBase", this.appletDocumentBase);
info.put ("codeBase", this.appletCodeBase);
info.put ("registry", this.statusManager.getRegistryInfo ());
}info.put ("version", J.viewer.JC.version);
info.put ("date", J.viewer.JC.date);
info.put ("javaVendor", J.viewer.Viewer.strJavaVendor);
info.put ("javaVersion", J.viewer.Viewer.strJavaVersion + (!this.isJS ? "" : this.isWebGL ? "(WebGL)" : "(HTML5)"));
info.put ("operatingSystem", J.viewer.Viewer.strOSName);
return info;
});
$_M(c$, "initialize", 
function (clearUserVariables) {
this.global = this.stateManager.getGlobalSettings (this.global, clearUserVariables);
this.setStartupBooleans ();
this.global.setI ("_width", this.dimScreen.width);
this.global.setI ("_height", this.dimScreen.height);
if (this.haveDisplay) {
this.global.setB ("_is2D", this.isJS && !this.isWebGL);
this.global.setB ("_multiTouchClient", this.actionManager.isMTClient ());
this.global.setB ("_multiTouchServer", this.actionManager.isMTServer ());
}this.colorManager.resetElementColors ();
this.setObjectColor ("background", "black");
this.setObjectColor ("axis1", "red");
this.setObjectColor ("axis2", "green");
this.setObjectColor ("axis3", "blue");
this.gdata.setAmbientPercent (this.global.ambientPercent);
this.gdata.setDiffusePercent (this.global.diffusePercent);
this.gdata.setSpecular (this.global.specular);
this.gdata.setCel (this.global.celShading);
this.gdata.setSpecularPercent (this.global.specularPercent);
this.gdata.setSpecularPower (-this.global.specularExponent);
this.gdata.setPhongExponent (this.global.phongExponent);
this.gdata.setSpecularPower (this.global.specularPower);
if (this.modelSet != null) this.animationManager.setAnimationOn (false);
this.animationManager.setAnimationFps (this.global.animationFps);
this.statusManager.setAllowStatusReporting (this.global.statusReporting);
this.setBooleanProperty ("antialiasDisplay", this.global.antialiasDisplay);
this.setTransformManagerDefaults ();
}, "~B");
$_M(c$, "listSavedStates", 
function () {
return this.stateManager.listSavedStates ();
});
$_M(c$, "saveOrientation", 
function (saveName, pymolView) {
this.stateManager.saveOrientation (saveName, pymolView);
}, "~S,~A");
$_M(c$, "saveScene", 
function (saveName, scene) {
this.stateManager.saveScene (saveName, scene);
}, "~S,java.util.Map");
$_M(c$, "restoreScene", 
function (saveName, timeSeconds) {
this.stateManager.restoreScene (saveName, timeSeconds);
}, "~S,~N");
$_M(c$, "restoreOrientation", 
function (saveName, timeSeconds) {
return this.stateManager.restoreOrientation (saveName, timeSeconds, true);
}, "~S,~N");
$_M(c$, "restoreRotation", 
function (saveName, timeSeconds) {
this.stateManager.restoreOrientation (saveName, timeSeconds, false);
}, "~S,~N");
$_M(c$, "saveModelOrientation", 
function () {
this.modelSet.saveModelOrientation (this.animationManager.currentModelIndex, this.stateManager.getOrientation ());
});
$_M(c$, "getOrientation", 
function () {
return this.stateManager.getOrientation ();
});
$_M(c$, "restoreModelOrientation", 
function (modelIndex) {
var o = this.modelSet.getModelOrientation (modelIndex);
if (o != null) o.restore (-1, true);
}, "~N");
$_M(c$, "restoreModelRotation", 
function (modelIndex) {
var o = this.modelSet.getModelOrientation (modelIndex);
if (o != null) o.restore (-1, false);
}, "~N");
$_M(c$, "saveBonds", 
function (saveName) {
this.stateManager.saveBonds (saveName);
}, "~S");
$_M(c$, "restoreBonds", 
function (saveName) {
this.clearModelDependentObjects ();
return this.stateManager.restoreBonds (saveName);
}, "~S");
$_M(c$, "saveState", 
function (saveName) {
this.stateManager.saveState (saveName);
}, "~S");
$_M(c$, "deleteSavedState", 
function (saveName) {
this.stateManager.deleteSaved ("State_" + saveName);
}, "~S");
$_M(c$, "getSavedState", 
function (saveName) {
return this.stateManager.getSavedState (saveName);
}, "~S");
$_M(c$, "saveStructure", 
function (saveName) {
this.stateManager.saveStructure (saveName);
}, "~S");
$_M(c$, "getSavedStructure", 
function (saveName) {
return this.stateManager.getSavedStructure (saveName);
}, "~S");
$_M(c$, "saveCoordinates", 
function (saveName, bsSelected) {
this.stateManager.saveCoordinates (saveName, bsSelected);
}, "~S,J.util.BS");
$_M(c$, "getSavedCoordinates", 
function (saveName) {
return this.stateManager.getSavedCoordinates (saveName);
}, "~S");
$_M(c$, "saveSelection", 
function (saveName) {
this.stateManager.saveSelection (saveName, this.getSelectionSet (false));
this.stateManager.restoreSelection (saveName);
}, "~S");
$_M(c$, "restoreSelection", 
function (saveName) {
return this.stateManager.restoreSelection (saveName);
}, "~S");
$_M(c$, "getMatrixtransform", 
function () {
return this.transformManager.getMatrixtransform ();
});
$_M(c$, "getRotationQuaternion", 
function () {
return this.transformManager.getRotationQuaternion ();
});
$_M(c$, "setRotationRadius", 
function (angstroms, doAll) {
if (doAll) angstroms = this.transformManager.setRotationRadius (angstroms, false);
if (this.modelSet.setRotationRadius (this.animationManager.currentModelIndex, angstroms)) this.global.setF ("rotationRadius", angstroms);
}, "~N,~B");
$_M(c$, "getRotationCenter", 
function () {
return this.transformManager.getRotationCenter ();
});
$_M(c$, "setCenterAt", 
function (relativeTo, pt) {
if (this.isJmolDataFrame ()) return;
this.transformManager.setCenterAt (relativeTo, pt);
}, "~S,J.util.P3");
$_M(c$, "setCenterBitSet", 
function (bsCenter, doScale) {
var center = (J.util.BSUtil.cardinalityOf (bsCenter) > 0 ? this.getAtomSetCenter (bsCenter) : null);
if (this.isJmolDataFrame ()) return;
this.transformManager.setNewRotationCenter (center, doScale);
}, "J.util.BS,~B");
$_M(c$, "setNewRotationCenter", 
function (center) {
if (this.isJmolDataFrame ()) return;
this.transformManager.setNewRotationCenter (center, true);
}, "J.util.P3");
$_M(c$, "getNavigationCenter", 
function () {
return this.transformManager.getNavigationCenter ();
});
$_M(c$, "getNavigationDepthPercent", 
function () {
return this.transformManager.getNavigationDepthPercent ();
});
$_M(c$, "navigate", 
function (keyWhere, modifiers) {
if (this.isJmolDataFrame ()) return;
this.transformManager.navigateKey (keyWhere, modifiers);
if (!this.transformManager.vibrationOn && keyWhere != 0) this.refresh (1, "Viewer:navigate()");
}, "~N,~N");
$_M(c$, "getNavigationOffset", 
function () {
return this.transformManager.getNavigationOffset ();
});
$_M(c$, "getNavigationOffsetPercent", 
function (XorY) {
return this.transformManager.getNavigationOffsetPercent (XorY);
}, "~S");
$_M(c$, "isNavigating", 
function () {
return this.transformManager.isNavigating ();
});
$_M(c$, "isInPosition", 
function (axis, degrees) {
return this.transformManager.isInPosition (axis, degrees);
}, "J.util.V3,~N");
$_M(c$, "move", 
function (eval, dRot, dZoom, dTrans, dSlab, floatSecondsTotal, fps) {
this.transformManager.move (eval, dRot, dZoom, dTrans, dSlab, floatSecondsTotal, fps);
this.moveUpdate (floatSecondsTotal);
}, "J.api.JmolScriptEvaluator,J.util.V3,~N,J.util.V3,~N,~N,~N");
$_M(c$, "stopMotion", 
function () {
this.transformManager.stopMotion ();
});
$_M(c$, "setRotationMatrix", 
function (rotationMatrix) {
this.transformManager.setRotation (rotationMatrix);
}, "J.util.Matrix3f");
$_M(c$, "moveTo", 
function (eval, floatSecondsTotal, center, rotAxis, degrees, rotationMatrix, zoom, xTrans, yTrans, rotationRadius, navCenter, xNav, yNav, navDepth, cameraDepth, cameraX, cameraY) {
if (!this.haveDisplay) floatSecondsTotal = 0;
this.setTainted (true);
this.transformManager.moveTo (eval, floatSecondsTotal, center, rotAxis, degrees, rotationMatrix, zoom, xTrans, yTrans, rotationRadius, navCenter, xNav, yNav, navDepth, cameraDepth, cameraX, cameraY);
}, "J.api.JmolScriptEvaluator,~N,J.util.P3,J.util.V3,~N,J.util.Matrix3f,~N,~N,~N,~N,J.util.P3,~N,~N,~N,~N,~N,~N");
$_M(c$, "moveUpdate", 
function (floatSecondsTotal) {
if (floatSecondsTotal > 0) this.requestRepaintAndWait ("moveUpdate");
 else if (floatSecondsTotal == 0) this.setSync ();
}, "~N");
$_M(c$, "getMoveToText", 
function (timespan) {
return this.transformManager.getMoveToText (timespan, false);
}, "~N");
$_M(c$, "navigateList", 
function (eval, list) {
if (this.isJmolDataFrame ()) return;
this.transformManager.navigateList (eval, list);
}, "J.api.JmolScriptEvaluator,J.util.JmolList");
$_M(c$, "navigatePt", 
function (center) {
this.transformManager.setNavigatePt (center);
this.setSync ();
}, "J.util.P3");
$_M(c$, "navigateAxis", 
function (rotAxis, degrees) {
this.transformManager.navigateAxis (rotAxis, degrees);
this.setSync ();
}, "J.util.V3,~N");
$_M(c$, "navTranslatePercent", 
function (x, y) {
if (this.isJmolDataFrame ()) return;
this.transformManager.navTranslatePercentOrTo (0, x, y);
this.setSync ();
}, "~N,~N");
$_M(c$, "setMouseEnabled", 
function (TF) {
this.mouseEnabled = TF;
}, "~B");
Clazz.overrideMethod (c$, "processMultitouchEvent", 
function (groupID, eventType, touchID, iData, pt, time) {
this.actionManager.processMultitouchEvent (groupID, eventType, touchID, iData, pt, time);
}, "~N,~N,~N,~N,J.util.P3,~N");
$_M(c$, "zoomBy", 
function (pixels) {
if (this.mouseEnabled) this.transformManager.zoomBy (pixels);
this.refresh (2, this.statusManager.syncingMouse ? "Mouse: zoomBy " + pixels : "");
}, "~N");
$_M(c$, "zoomByFactor", 
function (factor, x, y) {
if (this.mouseEnabled) this.transformManager.zoomByFactor (factor, x, y);
this.refresh (2, !this.statusManager.syncingMouse ? "" : "Mouse: zoomByFactor " + factor + (x == 2147483647 ? "" : " " + x + " " + y));
}, "~N,~N,~N");
$_M(c$, "rotateXYBy", 
function (xDelta, yDelta) {
if (this.mouseEnabled) this.transformManager.rotateXYBy (xDelta, yDelta, null);
this.refresh (2, this.statusManager.syncingMouse ? "Mouse: rotateXYBy " + xDelta + " " + yDelta : "");
}, "~N,~N");
$_M(c$, "spinXYBy", 
function (xDelta, yDelta, speed) {
if (this.mouseEnabled) this.transformManager.spinXYBy (xDelta, yDelta, speed);
if (xDelta == 0 && yDelta == 0) return;
this.refresh (2, this.statusManager.syncingMouse ? "Mouse: spinXYBy " + xDelta + " " + yDelta + " " + speed : "");
}, "~N,~N,~N");
$_M(c$, "rotateZBy", 
function (zDelta, x, y) {
if (this.mouseEnabled) this.transformManager.rotateZBy (zDelta, x, y);
this.refresh (2, this.statusManager.syncingMouse ? "Mouse: rotateZBy " + zDelta + (x == 2147483647 ? "" : " " + x + " " + y) : "");
}, "~N,~N,~N");
$_M(c$, "rotateSelected", 
function (deltaX, deltaY, bsSelected) {
if (this.isJmolDataFrame ()) return;
if (this.mouseEnabled) {
this.transformManager.rotateXYBy (deltaX, deltaY, this.setMovableBitSet (bsSelected, false));
this.refreshMeasures (true);
}this.refresh (2, this.statusManager.syncingMouse ? "Mouse: rotateMolecule " + deltaX + " " + deltaY : "");
}, "~N,~N,J.util.BS");
$_M(c$, "setMovableBitSet", 
($fz = function (bsSelected, checkMolecule) {
if (bsSelected == null) bsSelected = this.getSelectionSet (false);
bsSelected = J.util.BSUtil.copy (bsSelected);
J.util.BSUtil.andNot (bsSelected, this.getMotionFixedAtoms ());
if (checkMolecule && !this.global.allowMoveAtoms) bsSelected = this.modelSet.getMoleculeBitSet (bsSelected);
return bsSelected;
}, $fz.isPrivate = true, $fz), "J.util.BS,~B");
$_M(c$, "translateXYBy", 
function (xDelta, yDelta) {
if (this.mouseEnabled) this.transformManager.translateXYBy (xDelta, yDelta);
this.refresh (2, this.statusManager.syncingMouse ? "Mouse: translateXYBy " + xDelta + " " + yDelta : "");
}, "~N,~N");
Clazz.overrideMethod (c$, "rotateFront", 
function () {
this.transformManager.rotateFront ();
this.refresh (1, "Viewer:rotateFront()");
});
Clazz.overrideMethod (c$, "rotateX", 
function (angleRadians) {
this.transformManager.rotateX (angleRadians);
this.refresh (1, "Viewer:rotateX()");
}, "~N");
Clazz.overrideMethod (c$, "rotateY", 
function (angleRadians) {
this.transformManager.rotateY (angleRadians);
this.refresh (1, "Viewer:rotateY()");
}, "~N");
Clazz.overrideMethod (c$, "rotateZ", 
function (angleRadians) {
this.transformManager.rotateZ (angleRadians);
this.refresh (1, "Viewer:rotateZ()");
}, "~N");
Clazz.overrideMethod (c$, "rotateXDeg", 
function (angleDegrees) {
this.rotateX (angleDegrees * 0.017453292);
}, "~N");
Clazz.overrideMethod (c$, "rotateYDeg", 
function (angleDegrees) {
this.rotateY (angleDegrees * 0.017453292);
}, "~N");
$_M(c$, "translate", 
function (xyz, x, type, bsAtoms) {
var xy = (type == '\0' ? Clazz.floatToInt (x) : type == '%' ? this.transformManager.percentToPixels (xyz, x) : this.transformManager.angstromsToPixels (x * (type == 'n' ? 10 : 1)));
if (bsAtoms != null) {
if (xy == 0) return;
this.transformManager.setSelectedTranslation (bsAtoms, xyz, xy);
} else {
switch (xyz) {
case 'X':
case 'x':
if (type == '\0') this.transformManager.translateToPercent ('x', x);
 else this.transformManager.translateXYBy (xy, 0);
break;
case 'Y':
case 'y':
if (type == '\0') this.transformManager.translateToPercent ('y', x);
 else this.transformManager.translateXYBy (0, xy);
break;
case 'Z':
case 'z':
if (type == '\0') this.transformManager.translateToPercent ('z', x);
 else this.transformManager.translateZBy (xy);
break;
}
}this.refresh (1, "Viewer:translate()");
}, "~S,~N,~S,J.util.BS");
$_M(c$, "getTranslationXPercent", 
function () {
return this.transformManager.getTranslationXPercent ();
});
$_M(c$, "getTranslationYPercent", 
function () {
return this.transformManager.getTranslationYPercent ();
});
$_M(c$, "getTranslationZPercent", 
function () {
return this.transformManager.getTranslationZPercent ();
});
$_M(c$, "getTranslationScript", 
function () {
return this.transformManager.getTranslationScript ();
});
$_M(c$, "getZShadeStart", 
function () {
return this.transformManager.getZShadeStart ();
});
$_M(c$, "isWindowCentered", 
function () {
return this.transformManager.isWindowCentered ();
});
Clazz.overrideMethod (c$, "getZoomPercent", 
function () {
return Clazz.floatToInt (this.getZoomSetting ());
});
$_M(c$, "getZoomSetting", 
function () {
return this.transformManager.getZoomSetting ();
});
Clazz.overrideMethod (c$, "getZoomPercentFloat", 
function () {
return this.transformManager.getZoomPercentFloat ();
});
$_M(c$, "getMaxZoomPercent", 
function () {
return 200000;
});
$_M(c$, "slabReset", 
function () {
this.transformManager.slabReset ();
});
$_M(c$, "getZoomEnabled", 
function () {
return this.transformManager.zoomEnabled;
});
$_M(c$, "getSlabEnabled", 
function () {
return this.transformManager.slabEnabled;
});
$_M(c$, "slabByPixels", 
function (pixels) {
this.transformManager.slabByPercentagePoints (pixels);
this.refresh (3, "slabByPixels");
}, "~N");
$_M(c$, "depthByPixels", 
function (pixels) {
this.transformManager.depthByPercentagePoints (pixels);
this.refresh (3, "depthByPixels");
}, "~N");
$_M(c$, "slabDepthByPixels", 
function (pixels) {
this.transformManager.slabDepthByPercentagePoints (pixels);
this.refresh (3, "slabDepthByPixels");
}, "~N");
$_M(c$, "slabInternal", 
function (plane, isDepth) {
this.transformManager.slabInternal (plane, isDepth);
}, "J.util.P4,~B");
$_M(c$, "slabToPercent", 
function (percentSlab) {
this.transformManager.slabToPercent (percentSlab);
}, "~N");
$_M(c$, "depthToPercent", 
function (percentDepth) {
this.transformManager.depthToPercent (percentDepth);
}, "~N");
$_M(c$, "setSlabDepthInternal", 
function (isDepth) {
this.transformManager.setSlabDepthInternal (isDepth);
}, "~B");
$_M(c$, "zValueFromPercent", 
function (zPercent) {
return this.transformManager.zValueFromPercent (zPercent);
}, "~N");
Clazz.overrideMethod (c$, "getUnscaledTransformMatrix", 
function () {
return this.transformManager.getUnscaledTransformMatrix ();
});
$_M(c$, "finalizeTransformParameters", 
function () {
this.transformManager.finalizeTransformParameters ();
this.gdata.setSlab (this.transformManager.slabValue);
this.gdata.setDepth (this.transformManager.depthValue);
this.gdata.setZShade (this.transformManager.zShadeEnabled, this.transformManager.zSlabValue, this.transformManager.zDepthValue, this.global.zShadePower);
});
$_M(c$, "rotatePoint", 
function (pt, ptRot) {
this.transformManager.rotatePoint (pt, ptRot);
}, "J.util.P3,J.util.P3");
$_M(c$, "transformPt", 
function (pointAngstroms) {
return this.transformManager.transformPoint (pointAngstroms);
}, "J.util.P3");
$_M(c$, "transformPtVib", 
function (pointAngstroms, vibrationVector) {
return this.transformManager.transformPointVib (pointAngstroms, vibrationVector);
}, "J.util.P3,J.util.Vibration");
$_M(c$, "transformPtScr", 
function (pointAngstroms, pointScreen) {
this.transformManager.transformPointScr (pointAngstroms, pointScreen);
}, "J.util.P3,J.util.P3i");
$_M(c$, "transformPtNoClip", 
function (pointAngstroms, pt) {
this.transformManager.transformPointNoClip2 (pointAngstroms, pt);
}, "J.util.P3,J.util.P3");
$_M(c$, "transformPt3f", 
function (pointAngstroms, pointScreen) {
this.transformManager.transformPoint2 (pointAngstroms, pointScreen);
}, "J.util.P3,J.util.P3");
$_M(c$, "transformPoints", 
function (pointsAngstroms, pointsScreens) {
this.transformManager.transformPoints (pointsAngstroms.length, pointsAngstroms, pointsScreens);
}, "~A,~A");
$_M(c$, "transformVector", 
function (vectorAngstroms, vectorTransformed) {
this.transformManager.transformVector (vectorAngstroms, vectorTransformed);
}, "J.util.V3,J.util.V3");
$_M(c$, "unTransformPoint", 
function (pointScreen, pointAngstroms) {
this.transformManager.unTransformPoint (pointScreen, pointAngstroms);
}, "J.util.P3,J.util.P3");
$_M(c$, "getScalePixelsPerAngstrom", 
function (asAntialiased) {
return this.transformManager.scalePixelsPerAngstrom * (asAntialiased || !this.antialiasDisplay ? 1 : 0.5);
}, "~B");
$_M(c$, "scaleToScreen", 
function (z, milliAngstroms) {
return this.transformManager.scaleToScreen (z, milliAngstroms);
}, "~N,~N");
$_M(c$, "unscaleToScreen", 
function (z, screenDistance) {
return this.transformManager.unscaleToScreen (z, screenDistance);
}, "~N,~N");
$_M(c$, "scaleToPerspective", 
function (z, sizeAngstroms) {
return this.transformManager.scaleToPerspective (z, sizeAngstroms);
}, "~N,~N");
$_M(c$, "setSpin", 
function (key, value) {
if (!J.util.Parser.isOneOf (key, "x;y;z;fps;X;Y;Z;FPS")) return;
var i = "x;y;z;fps;X;Y;Z;FPS".indexOf (key);
switch (i) {
case 0:
this.transformManager.setSpinXYZ (value, NaN, NaN);
break;
case 2:
this.transformManager.setSpinXYZ (NaN, value, NaN);
break;
case 4:
this.transformManager.setSpinXYZ (NaN, NaN, value);
break;
case 6:
default:
this.transformManager.setSpinFps (value);
break;
case 10:
this.transformManager.setNavXYZ (value, NaN, NaN);
break;
case 12:
this.transformManager.setNavXYZ (NaN, value, NaN);
break;
case 14:
this.transformManager.setNavXYZ (NaN, NaN, value);
break;
case 16:
this.transformManager.setNavFps (value);
break;
}
this.global.setI ((i < 10 ? "spin" : "nav") + key, value);
}, "~S,~N");
$_M(c$, "getSpinState", 
function () {
return this.getStateCreator ().getSpinState (false);
});
$_M(c$, "setSpinOn", 
function (spinOn) {
if (spinOn) this.transformManager.setSpinOn ();
 else this.transformManager.setSpinOff ();
}, "~B");
$_M(c$, "getSpinOn", 
function () {
return this.transformManager.getSpinOn ();
});
$_M(c$, "setNavOn", 
function (navOn) {
this.transformManager.setNavOn (navOn);
}, "~B");
$_M(c$, "getNavOn", 
function () {
return this.transformManager.getNavOn ();
});
$_M(c$, "setNavXYZ", 
function (x, y, z) {
this.transformManager.setNavXYZ (Clazz.floatToInt (x), Clazz.floatToInt (y), Clazz.floatToInt (z));
}, "~N,~N,~N");
$_M(c$, "getOrientationText", 
function (type, name) {
switch (type) {
case 1313866247:
case 1073741863:
case 1112541205:
case 1112541206:
case 1112541207:
case 135270417:
return this.modelSet.getBoundBoxOrientation (type, this.getSelectionSet (false));
case 1073742035:
return this.stateManager.getSavedOrientationText (name);
default:
return this.transformManager.getOrientationText (type);
}
}, "~N,~S");
$_M(c$, "getOrientationInfo", 
function () {
return this.transformManager.getOrientationInfo ();
});
$_M(c$, "getMatrixRotate", 
function () {
return this.transformManager.getMatrixRotate ();
});
$_M(c$, "getAxisAngle", 
function (axisAngle) {
this.transformManager.getAxisAngle (axisAngle);
}, "J.util.AxisAngle4f");
$_M(c$, "getTransformText", 
function () {
return this.transformManager.getTransformText ();
});
$_M(c$, "getRotation", 
function (matrixRotation) {
this.transformManager.getRotation (matrixRotation);
}, "J.util.Matrix3f");
$_M(c$, "getCurrentColorRange", 
function () {
return this.colorManager.getPropertyColorRange ();
});
$_M(c$, "setDefaultColors", 
($fz = function (isRasmol) {
this.colorManager.setDefaultColors (isRasmol);
this.global.setB ("colorRasmol", isRasmol);
this.global.setS ("defaultColorScheme", (isRasmol ? "rasmol" : "jmol"));
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getColorArgbOrGray", 
function (colix) {
return this.gdata.getColorArgbOrGray (colix);
}, "~N");
$_M(c$, "setRubberbandArgb", 
function (argb) {
this.colorManager.setRubberbandArgb (argb);
}, "~N");
$_M(c$, "getColixRubberband", 
function () {
return this.colorManager.colixRubberband;
});
$_M(c$, "setElementArgb", 
function (elementNumber, argb) {
this.global.setS ("=color " + J.util.Elements.elementNameFromNumber (elementNumber), J.util.Escape.escapeColor (argb));
this.colorManager.setElementArgb (elementNumber, argb);
}, "~N,~N");
Clazz.overrideMethod (c$, "setVectorScale", 
function (scale) {
this.global.setF ("vectorScale", scale);
this.global.vectorScale = scale;
}, "~N");
$_M(c$, "isVibrationOn", 
function () {
return this.transformManager.vibrationOn;
});
Clazz.overrideMethod (c$, "setVibrationScale", 
function (scale) {
this.transformManager.setVibrationScale (scale);
this.global.vibrationScale = scale;
this.global.setF ("vibrationScale", scale);
}, "~N");
$_M(c$, "setVibrationOff", 
function () {
this.transformManager.setVibrationPeriod (0);
});
Clazz.overrideMethod (c$, "setVibrationPeriod", 
function (period) {
this.transformManager.setVibrationPeriod (period);
period = Math.abs (period);
this.global.vibrationPeriod = period;
this.global.setF ("vibrationPeriod", period);
}, "~N");
$_M(c$, "setObjectColor", 
function (name, colorName) {
if (colorName == null || colorName.length == 0) return;
this.setObjectArgb (name, J.util.ColorUtil.getArgbFromString (colorName));
}, "~S,~S");
$_M(c$, "setObjectVisibility", 
function (name, b) {
var objId = J.viewer.StateManager.getObjectIdFromName (name);
if (objId >= 0) {
this.setShapeProperty (objId, "display", b ? Boolean.TRUE : Boolean.FALSE);
}}, "~S,~B");
$_M(c$, "setObjectArgb", 
function (name, argb) {
var objId = J.viewer.StateManager.getObjectIdFromName (name);
if (objId < 0) {
if (name.equalsIgnoreCase ("axes")) {
this.setObjectArgb ("axis1", argb);
this.setObjectArgb ("axis2", argb);
this.setObjectArgb ("axis3", argb);
}return;
}this.global.objColors[objId] = argb;
switch (objId) {
case 0:
this.gdata.setBackgroundArgb (argb);
this.colorManager.setColixBackgroundContrast (argb);
break;
}
this.global.setS (name + "Color", J.util.Escape.escapeColor (argb));
}, "~S,~N");
$_M(c$, "setBackgroundImage", 
function (fileName, image) {
this.global.backgroundImageFileName = fileName;
this.gdata.setBackgroundImage (image);
}, "~S,~O");
$_M(c$, "getObjectArgb", 
function (objId) {
return this.global.objColors[objId];
}, "~N");
$_M(c$, "getObjectColix", 
function (objId) {
var argb = this.getObjectArgb (objId);
if (argb == 0) return this.getColixBackgroundContrast ();
return J.util.C.getColix (argb);
}, "~N");
$_M(c$, "getFontState", 
function (myType, font3d) {
return this.getStateCreator ().getFontState (myType, font3d);
}, "~S,J.util.JmolFont");
Clazz.overrideMethod (c$, "setColorBackground", 
function (colorName) {
this.setObjectColor ("background", colorName);
}, "~S");
Clazz.overrideMethod (c$, "getBackgroundArgb", 
function () {
return this.getObjectArgb (0);
});
$_M(c$, "setObjectMad", 
function (iShape, name, mad) {
var objId = J.viewer.StateManager.getObjectIdFromName (name.equalsIgnoreCase ("axes") ? "axis" : name);
if (objId < 0) return;
if (mad == -2 || mad == -4) {
var m = mad + 3;
mad = this.getObjectMad (objId);
if (mad == 0) mad = m;
}this.global.setB ("show" + name, mad != 0);
this.global.objStateOn[objId] = (mad != 0);
if (mad == 0) return;
this.global.objMad[objId] = mad;
this.setShapeSize (iShape, mad, null);
}, "~N,~S,~N");
$_M(c$, "getObjectMad", 
function (objId) {
return (this.global.objStateOn[objId] ? this.global.objMad[objId] : 0);
}, "~N");
$_M(c$, "setPropertyColorScheme", 
function (scheme, isTranslucent, isOverloaded) {
this.global.propertyColorScheme = scheme;
if (scheme.startsWith ("translucent ")) {
isTranslucent = true;
scheme = scheme.substring (12).trim ();
}this.colorManager.setPropertyColorScheme (scheme, isTranslucent, isOverloaded);
}, "~S,~B,~B");
$_M(c$, "getPropertyColorScheme", 
function () {
return this.global.propertyColorScheme;
});
$_M(c$, "getColixBackgroundContrast", 
function () {
return this.colorManager.colixBackgroundContrast;
});
$_M(c$, "getSpecularState", 
function () {
return this.getStateCreator ().getSpecularState ();
});
$_M(c$, "getColixAtomPalette", 
function (atom, pid) {
return this.colorManager.getColixAtomPalette (atom, pid);
}, "J.modelset.Atom,~N");
$_M(c$, "getColixBondPalette", 
function (bond, pid) {
return this.colorManager.getColixBondPalette (bond, pid);
}, "J.modelset.Bond,~N");
$_M(c$, "getColorSchemeList", 
function (colorScheme) {
return this.colorManager.getColorSchemeList (colorScheme);
}, "~S");
$_M(c$, "setUserScale", 
function (scale) {
this.colorManager.setUserScale (scale);
}, "~A");
$_M(c$, "getColixForPropertyValue", 
function (val) {
return this.colorManager.getColixForPropertyValue (val);
}, "~N");
$_M(c$, "getColorPointForPropertyValue", 
function (val) {
return J.util.ColorUtil.colorPointFromInt2 (this.gdata.getColorArgbOrGray (this.colorManager.getColixForPropertyValue (val)));
}, "~N");
$_M(c$, "select", 
function (bs, isGroup, addRemove, isQuiet) {
if (isGroup) bs = this.getUndeletedGroupAtomBits (bs);
this.selectionManager.select (bs, addRemove, isQuiet);
this.shapeManager.setShapeSizeBs (1, 2147483647, null, null);
}, "J.util.BS,~B,~N,~B");
Clazz.overrideMethod (c$, "setSelectionSet", 
function (set) {
this.select (set, false, 0, true);
}, "J.util.BS");
$_M(c$, "selectBonds", 
function (bs) {
this.shapeManager.setShapeSizeBs (1, 2147483647, null, bs);
}, "J.util.BS");
$_M(c$, "displayAtoms", 
function (bs, isDisplay, isGroup, addRemove, isQuiet) {
if (isGroup) bs = this.getUndeletedGroupAtomBits (bs);
if (isDisplay) this.selectionManager.display (this.modelSet, bs, addRemove, isQuiet);
 else this.selectionManager.hide (this.modelSet, bs, addRemove, isQuiet);
}, "J.util.BS,~B,~B,~N,~B");
$_M(c$, "getUndeletedGroupAtomBits", 
($fz = function (bs) {
bs = this.getAtomBits (1087373318, bs);
J.util.BSUtil.andNot (bs, this.selectionManager.getDeletedAtoms ());
return bs;
}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "getHiddenSet", 
function () {
return this.selectionManager.getHiddenSet ();
});
$_M(c$, "isSelected", 
function (atomIndex) {
return this.selectionManager.isSelected (atomIndex);
}, "~N");
$_M(c$, "isInSelectionSubset", 
function (atomIndex) {
return this.selectionManager.isInSelectionSubset (atomIndex);
}, "~N");
$_M(c$, "reportSelection", 
function (msg) {
if (this.modelSet.getSelectionHaloEnabled ()) this.setTainted (true);
if (this.isScriptQueued () || this.global.debugScript) this.scriptStatus (msg);
}, "~S");
$_M(c$, "getAtomSetCenter", 
function (bs) {
return this.modelSet.getAtomSetCenter (bs);
}, "J.util.BS");
$_M(c$, "clearAtomSets", 
($fz = function () {
this.setSelectionSubset (null);
this.definedAtomSets.clear ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "getDefinedAtomSet", 
function (name) {
var o = this.definedAtomSets.get (name.toLowerCase ());
return (Clazz.instanceOf (o, J.util.BS) ? o :  new J.util.BS ());
}, "~S");
Clazz.overrideMethod (c$, "selectAll", 
function () {
this.selectionManager.selectAll (false);
});
$_M(c$, "setNoneSelected", 
function (noneSelected) {
this.noneSelected = noneSelected;
}, "~B");
$_M(c$, "getNoneSelected", 
function () {
return (this.noneSelected ? Boolean.TRUE : Boolean.FALSE);
});
Clazz.overrideMethod (c$, "clearSelection", 
function () {
this.selectionManager.clearSelection (true);
this.global.setB ("hideNotSelected", false);
});
$_M(c$, "setSelectionSubset", 
function (subset) {
this.selectionManager.setSelectionSubset (subset);
}, "J.util.BS");
$_M(c$, "getSelectionSubset", 
function () {
return this.selectionManager.getSelectionSubset ();
});
$_M(c$, "invertSelection", 
function () {
this.selectionManager.invertSelection ();
});
$_M(c$, "getSelectionSet", 
function (includeDeleted) {
return this.selectionManager.getSelectionSet (includeDeleted);
}, "~B");
$_M(c$, "setSelectedAtom", 
function (atomIndex, TF) {
this.selectionManager.setSelectedAtom (atomIndex, TF);
}, "~N,~B");
$_M(c$, "isAtomSelected", 
function (atomIndex) {
return this.selectionManager.isAtomSelected (atomIndex);
}, "~N");
Clazz.overrideMethod (c$, "getSelectionCount", 
function () {
return this.selectionManager.getSelectionCount ();
});
$_M(c$, "setFormalCharges", 
function (formalCharge) {
this.modelSet.setFormalCharges (this.getSelectionSet (false), formalCharge);
}, "~N");
Clazz.overrideMethod (c$, "addSelectionListener", 
function (listener) {
this.selectionManager.addListener (listener);
}, "J.api.JmolSelectionListener");
Clazz.overrideMethod (c$, "removeSelectionListener", 
function (listener) {
this.selectionManager.addListener (listener);
}, "J.api.JmolSelectionListener");
$_M(c$, "getAtomBitSetEval", 
function (eval, atomExpression) {
if (!this.allowScripting) {
System.out.println ("viewer.getAtomBitSetEval not allowed");
return  new J.util.BS ();
}return this.getScriptManager ().getAtomBitSetEval (eval, atomExpression);
}, "J.api.JmolScriptEvaluator,~O");
Clazz.overrideMethod (c$, "setModeMouse", 
function (modeMouse) {
if (modeMouse == -1) {
if (this.mouse != null) {
this.mouse.dispose ();
this.mouse = null;
}this.clearScriptQueue ();
this.clearThreads ();
this.haltScriptExecution ();
if (this.scriptManager != null) this.scriptManager.clear (true);
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
}}, "~N");
$_M(c$, "getRubberBandSelection", 
function () {
return (this.haveDisplay ? this.actionManager.getRubberBand () : null);
});
$_M(c$, "isBound", 
function (action, gesture) {
return (this.haveDisplay && this.actionManager.isBound (action, gesture));
}, "~N,~N");
$_M(c$, "getCursorX", 
function () {
return (this.haveDisplay ? this.actionManager.getCurrentX () : 0);
});
$_M(c$, "getCursorY", 
function () {
return (this.haveDisplay ? this.actionManager.getCurrentY () : 0);
});
$_M(c$, "getDefaultDirectory", 
function () {
return this.global.defaultDirectory;
});
Clazz.overrideMethod (c$, "getBufferedInputStream", 
function (fullPathName) {
return this.fileManager.getBufferedInputStream (fullPathName);
}, "~S");
$_M(c$, "getBufferedReaderOrErrorMessageFromName", 
function (name, fullPathNameReturn, isBinary) {
return this.fileManager.getBufferedReaderOrErrorMessageFromName (name, fullPathNameReturn, isBinary, true);
}, "~S,~A,~B");
$_M(c$, "setLoadParameters", 
($fz = function (htParams, isAppend) {
if (htParams == null) htParams =  new java.util.Hashtable ();
htParams.put ("viewer", this);
if (this.global.atomTypes.length > 0) htParams.put ("atomTypes", this.global.atomTypes);
if (!htParams.containsKey ("lattice")) htParams.put ("lattice", this.global.getDefaultLattice ());
if (this.global.applySymmetryToBonds) htParams.put ("applySymmetryToBonds", Boolean.TRUE);
if (this.global.pdbGetHeader) htParams.put ("getHeader", Boolean.TRUE);
if (this.global.pdbSequential) htParams.put ("isSequential", Boolean.TRUE);
htParams.put ("stateScriptVersionInt", Integer.$valueOf (this.stateScriptVersionInt));
if (!htParams.containsKey ("filter")) {
var filter = this.getDefaultLoadFilter ();
if (filter.length > 0) htParams.put ("filter", filter);
}var merging = (isAppend && !this.global.appendNew && this.getAtomCount () > 0);
htParams.put ("baseAtomIndex", Integer.$valueOf (isAppend ? this.getAtomCount () : 0));
htParams.put ("baseModelIndex", Integer.$valueOf (this.getAtomCount () == 0 ? 0 : this.getModelCount () + (merging ? -1 : 0)));
if (merging) htParams.put ("merging", Boolean.TRUE);
return htParams;
}, $fz.isPrivate = true, $fz), "java.util.Map,~B");
Clazz.overrideMethod (c$, "openFileAsyncPDB", 
function (fileName, pdbCartoons) {
this.getStateCreator ().openFileAsync (fileName, pdbCartoons);
}, "~S,~B");
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
$_M(c$, "loadModelFromFileRepaint", 
($fz = function (fullPathName, fileName, fileNames, reader) {
var ret = this.loadModelFromFile (fullPathName, fileName, fileNames, reader, false, null, null, 0);
this.refresh (1, "loadModelFromFileRepaint");
return ret;
}, $fz.isPrivate = true, $fz), "~S,~S,~A,~O");
$_M(c$, "loadModelFromFile", 
function (fullPathName, fileName, fileNames, reader, isAppend, htParams, loadScript, tokType) {
if (htParams == null) htParams = this.setLoadParameters (null, isAppend);
var atomSetCollection;
var saveInfo = this.fileManager.getFileInfo ();
if (fileNames != null) {
if (loadScript == null) {
loadScript =  new J.util.SB ().append ("load files");
for (var i = 0; i < fileNames.length; i++) loadScript.append (" /*file*/$FILENAME" + (i + 1) + "$");

}var timeBegin = System.currentTimeMillis ();
atomSetCollection = this.fileManager.createAtomSetCollectionFromFiles (fileNames, this.setLoadParameters (htParams, isAppend), isAppend);
var ms = System.currentTimeMillis () - timeBegin;
var msg = "";
for (var i = 0; i < fileNames.length; i++) msg += (i == 0 ? "" : ",") + fileNames[i];

J.util.Logger.info ("openFiles(" + fileNames.length + ") " + ms + " ms");
fileNames = htParams.get ("fullPathNames");
var fileTypes = htParams.get ("fileTypes");
var s = loadScript.toString ();
for (var i = 0; i < fileNames.length; i++) {
var fname = fileNames[i];
if (fileTypes != null && fileTypes[i] != null) fname = fileTypes[i] + "::" + fname;
s = J.util.TextFormat.simpleReplace (s, "$FILENAME" + (i + 1) + "$", J.util.Escape.eS (fname.$replace ('\\', '/')));
}
loadScript =  new J.util.SB ().append (s);
} else if (reader == null) {
if (loadScript == null) loadScript =  new J.util.SB ().append ("load /*file*/$FILENAME$");
atomSetCollection = this.openFileFull (fileName, isAppend, htParams, loadScript);
} else if (Clazz.instanceOf (reader, java.io.Reader)) {
atomSetCollection = this.fileManager.createAtomSetCollectionFromReader (fullPathName, fileName, reader, htParams);
} else {
atomSetCollection = this.fileManager.createAtomSetCollectionFromDOM (reader, htParams);
}if (tokType != 0) {
this.fileManager.setFileInfo (saveInfo);
return this.loadAtomDataAndReturnError (atomSetCollection, tokType);
}if (htParams.containsKey ("isData")) return atomSetCollection;
if (loadScript != null) {
var fname = htParams.get ("fullPathName");
if (fname == null) fname = "";
if (htParams.containsKey ("loadScript")) loadScript = htParams.get ("loadScript");
htParams.put ("loadScript", loadScript =  new J.util.SB ().append (J.util.TextFormat.simpleReplace (loadScript.toString (), "$FILENAME$", J.util.Escape.eS (fname.$replace ('\\', '/')))));
}return this.createModelSetAndReturnError (atomSetCollection, isAppend, loadScript, htParams);
}, "~S,~S,~A,~O,~B,java.util.Map,J.util.SB,~N");
$_M(c$, "setLigandModel", 
function (key, data) {
if (this.ligandModels == null) this.ligandModels =  new java.util.Hashtable ();
this.ligandModels.put (key, data);
}, "~S,~S");
$_M(c$, "getLigandModel", 
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
var model = this.ligandModels.get (id);
var data;
var fname = null;
if (Clazz.instanceOf (model, Boolean)) return null;
if (model == null) model = this.ligandModels.get (id + suffix);
var isError = false;
if (model == null) {
var s;
if (isLigand) {
fname = this.setLoadFormat ("#" + id, '#', false);
if (fname.length == 0) return null;
this.scriptEcho ("fetching " + fname);
s = this.getFileAsString (fname);
} else {
s = this.getFileAsString (prefix);
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
model = this.getModelAdapter ().getAtomSetCollectionReader ("ligand", null, J.io.JmolBinary.getBufferedReaderForString (data), htParams);
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
$_M(c$, "openFileFull", 
($fz = function (fileName, isAppend, htParams, loadScript) {
if (fileName == null) return null;
if (fileName.indexOf ("[]") >= 0) {
return null;
}var atomSetCollection;
var msg = "openFile(" + fileName + ")";
J.util.Logger.startTimer (msg);
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
strModel = this.modelSet.getInlineData (-1);
if (strModel == null) if (this.global.modelKitMode) strModel = "5\n\nC 0 0 0\nH .63 .63 .63\nH -.63 -.63 .63\nH -.63 .63 -.63\nH .63 -.63 -.63";
 else return "cannot find string data";
if (loadScript != null) htParams.put ("loadScript", loadScript =  new J.util.SB ().append (J.util.TextFormat.simpleReplace (loadScript.toString (), "$FILENAME$", "data \"model inline\"\n" + strModel + "end \"model inline\"")));
}if (strModel != null) {
if (!isAppend) this.zap (true, false, false);
atomSetCollection = this.fileManager.createAtomSetCollectionFromString (strModel, loadScript, htParams, isAppend, isLoadVariable || haveFileData && !isString);
} else {
atomSetCollection = this.fileManager.createAtomSetCollectionFromFile (fileName, htParams, isAppend);
}J.util.Logger.checkTimer (msg, false);
return atomSetCollection;
}, $fz.isPrivate = true, $fz), "~S,~B,java.util.Map,J.util.SB");
Clazz.overrideMethod (c$, "openStringInline", 
function (strModel) {
var ret = this.openStringInlineParamsAppend (strModel, null, false);
this.refresh (1, "openStringInline");
return ret;
}, "~S");
$_M(c$, "loadInline", 
function (strModel) {
return this.loadInlineScriptRepaint (strModel, this.global.inlineNewlineChar, false);
}, "~S");
$_M(c$, "loadInline", 
function (strModel, newLine) {
return this.loadInlineScriptRepaint (strModel, newLine, false);
}, "~S,~S");
$_M(c$, "loadInline", 
function (strModel, isAppend) {
return this.loadInlineScriptRepaint (strModel, '\0', isAppend);
}, "~S,~B");
$_M(c$, "loadInlineScriptRepaint", 
($fz = function (strModel, newLine, isAppend) {
var ret = this.loadInlineScript (strModel, newLine, isAppend, null);
this.refresh (1, "loadInlineScript");
return ret;
}, $fz.isPrivate = true, $fz), "~S,~S,~B");
$_M(c$, "loadInline", 
function (arrayModels) {
return this.loadInline (arrayModels, false);
}, "~A");
$_M(c$, "loadInline", 
function (arrayModels, isAppend) {
if (arrayModels == null || arrayModels.length == 0) return null;
var ret = this.openStringsInlineParamsAppend (arrayModels, null, isAppend);
this.refresh (1, "loadInline String[]");
return ret;
}, "~A,~B");
$_M(c$, "loadInline", 
function (arrayData, isAppend) {
if (arrayData == null || arrayData.size () == 0) return null;
if (!isAppend) this.zap (true, false, false);
var atomSetCollection = this.fileManager.createAtomSeCollectionFromArrayData (arrayData, this.setLoadParameters (null, isAppend), isAppend);
var ret = this.createModelSetAndReturnError (atomSetCollection, isAppend, null, null);
this.refresh (1, "loadInline");
return ret;
}, "java.util.List,~B");
$_M(c$, "loadInlineScript", 
($fz = function (strModel, newLine, isAppend, htParams) {
if (strModel == null || strModel.length == 0) return null;
strModel = J.viewer.Viewer.fixInlineString (strModel, newLine);
if (newLine.charCodeAt (0) != 0) J.util.Logger.info ("loading model inline, " + strModel.length + " bytes, with newLine character " + (newLine).charCodeAt (0) + " isAppend=" + isAppend);
if (J.util.Logger.debugging) J.util.Logger.debug (strModel);
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
}, $fz.isPrivate = true, $fz), "~S,~S,~B,java.util.Map");
c$.fixInlineString = $_M(c$, "fixInlineString", 
function (strModel, newLine) {
var i;
if (strModel.indexOf ("\\/n") >= 0) {
strModel = J.util.TextFormat.simpleReplace (strModel, "\n", "");
strModel = J.util.TextFormat.simpleReplace (strModel, "\\/n", "\n");
newLine = String.fromCharCode ( 0);
}if (newLine.charCodeAt (0) != 0 && newLine != '\n') {
var repEmpty = (strModel.indexOf ('\n') >= 0);
var len = strModel.length;
for (i = 0; i < len && strModel.charAt (i) == ' '; ++i) {
}
if (i < len && strModel.charAt (i) == newLine) strModel = strModel.substring (i + 1);
if (repEmpty) strModel = J.util.TextFormat.simpleReplace (strModel, "" + newLine, "");
 else strModel = strModel.$replace (newLine, '\n');
}return strModel;
}, "~S,~S");
$_M(c$, "openStringInlineParamsAppend", 
function (strModel, htParams, isAppend) {
var br =  new java.io.BufferedReader ( new java.io.StringReader (strModel));
var type = this.getModelAdapter ().getFileTypeName (br);
if (type == null) return "unknown file type";
if (type.equals ("spt")) {
return "cannot open script inline";
}htParams = this.setLoadParameters (htParams, isAppend);
var loadScript = htParams.get ("loadScript");
var isLoadCommand = htParams.containsKey ("isData");
if (loadScript == null) loadScript =  new J.util.SB ();
if (!isAppend) this.zap (true, false, false);
var atomSetCollection = this.fileManager.createAtomSetCollectionFromString (strModel, loadScript, htParams, isAppend, isLoadCommand);
return this.createModelSetAndReturnError (atomSetCollection, isAppend, loadScript, null);
}, "~S,java.util.Map,~B");
$_M(c$, "openStringsInlineParamsAppend", 
($fz = function (arrayModels, htParams, isAppend) {
var loadScript =  new J.util.SB ();
if (!isAppend) this.zap (true, false, false);
var atomSetCollection = this.fileManager.createAtomSeCollectionFromStrings (arrayModels, loadScript, this.setLoadParameters (htParams, isAppend), isAppend);
return this.createModelSetAndReturnError (atomSetCollection, isAppend, loadScript, null);
}, $fz.isPrivate = true, $fz), "~A,java.util.Map,~B");
$_M(c$, "getInlineChar", 
function () {
return this.global.inlineNewlineChar;
});
$_M(c$, "getDataSeparator", 
function () {
return this.global.getParameter ("dataseparator");
});
$_M(c$, "createModelSetAndReturnError", 
($fz = function (atomSetCollection, isAppend, loadScript, htParams) {
var fullPathName = this.fileManager.getFullPathName ();
var fileName = this.fileManager.getFileName ();
var errMsg;
if (loadScript == null) {
this.setBooleanProperty ("preserveState", false);
loadScript =  new J.util.SB ().append ("load \"???\"");
}if (Clazz.instanceOf (atomSetCollection, String)) {
errMsg = atomSetCollection;
this.setFileLoadStatus (J.constant.EnumFileStatus.NOT_LOADED, fullPathName, null, null, errMsg, null);
if (this.displayLoadErrors && !isAppend && !errMsg.equals ("#CANCELED#")) this.zapMsg (errMsg);
return errMsg;
}if (isAppend) this.clearAtomSets ();
 else if (this.global.modelKitMode && !fileName.equals ("Jmol Model Kit")) this.setModelKitMode (false);
this.setFileLoadStatus (J.constant.EnumFileStatus.CREATING_MODELSET, fullPathName, fileName, null, null, null);
this.pushHoldRepaint ("createModelSet");
this.setErrorMessage (null, null);
try {
var bsNew =  new J.util.BS ();
this.modelManager.createModelSet (fullPathName, fileName, loadScript, atomSetCollection, bsNew, isAppend);
if (bsNew.cardinality () > 0) {
var jmolScript = this.modelSet.getModelSetAuxiliaryInfoValue ("jmolscript");
if (this.modelSet.getModelSetAuxiliaryInfoBoolean ("doMinimize")) this.minimize (2147483647, 0, bsNew, null, 0, true, true, true);
 else this.addHydrogens (bsNew, false, true);
if (jmolScript != null) this.modelSet.getModelSetAuxiliaryInfo ().put ("jmolscript", jmolScript);
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
this.setFileLoadStatus (J.constant.EnumFileStatus.CREATED, fullPathName, fileName, this.getModelSetName (), errMsg, htParams == null ? null : htParams.get ("async"));
if (isAppend) {
this.selectAll ();
this.setTainted (true);
this.axesAreTainted = true;
}atomSetCollection = null;
System.gc ();
return errMsg;
}, $fz.isPrivate = true, $fz), "~O,~B,J.util.SB,java.util.Map");
$_M(c$, "loadAtomDataAndReturnError", 
($fz = function (atomSetCollection, tokType) {
if (Clazz.instanceOf (atomSetCollection, String)) return atomSetCollection;
this.setErrorMessage (null, null);
try {
this.modelManager.createAtomDataSet (atomSetCollection, tokType);
switch (tokType) {
case 4166:
this.setStatusFrameChanged (true);
break;
case 1649412120:
this.shapeManager.deleteVdwDependentShapes (null);
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
}, $fz.isPrivate = true, $fz), "~O,~N");
Clazz.overrideMethod (c$, "getEmbeddedFileState", 
function (filename) {
return this.fileManager.getEmbeddedFileState (filename);
}, "~S");
Clazz.overrideMethod (c$, "getFileAsBytes", 
function (pathName, osb) {
return this.fileManager.getFileAsBytes (pathName, osb, true);
}, "~S,J.io.OutputStringBuilder");
$_M(c$, "getCurrentFileAsString", 
function () {
var filename = this.getFullPathName ();
if (filename.equals ("string") || filename.equals ("Jmol Model Kit")) return this.modelSet.getInlineData (this.getCurrentModelIndex ());
if (filename.indexOf ("[]") >= 0) return filename;
if (filename === "JSNode") return "<DOM NODE>";
var pathName = this.modelManager.getModelSetPathName ();
if (pathName == null) return null;
return this.getFileAsString4 (pathName, -1, true, false);
});
$_M(c$, "getFullPathName", 
function () {
return this.fileManager.getFullPathName ();
});
$_M(c$, "getFileName", 
function () {
return this.fileManager.getFileName ();
});
$_M(c$, "getFullPathNameOrError", 
function (filename) {
return this.fileManager.getFullPathNameOrError (filename);
}, "~S");
Clazz.overrideMethod (c$, "getFileAsString", 
function (name) {
return this.getFileAsString4 (name, -1, false, false);
}, "~S");
$_M(c$, "getFileAsString4", 
function (name, nBytesMax, doSpecialLoad, allowBinary) {
if (name == null) return this.getCurrentFileAsString ();
var data =  new Array (2);
data[0] = name;
this.fileManager.getFileDataOrErrorAsString (data, nBytesMax, doSpecialLoad, allowBinary);
return data[1];
}, "~S,~N,~B,~B");
$_M(c$, "getFileAsStringBin", 
function (data) {
return this.fileManager.getFileDataOrErrorAsString (data, -1, false, true);
}, "~A");
$_M(c$, "getFilePath", 
function (name, asShortName) {
return this.fileManager.getFilePath (name, false, asShortName);
}, "~S,~B");
$_M(c$, "getFileInfo", 
function () {
return this.fileManager.getFileInfo ();
});
$_M(c$, "setFileInfo", 
function (fileInfo) {
this.fileManager.setFileInfo (fileInfo);
}, "~A");
$_M(c$, "autoCalculate", 
function (tokProperty) {
switch (tokProperty) {
case 1112539151:
this.modelSet.getSurfaceDistanceMax ();
break;
case 1112539150:
this.modelSet.calculateStraightness ();
break;
}
}, "~N");
$_M(c$, "getSurfaceDistanceMax", 
function () {
return this.modelSet.getSurfaceDistanceMax ();
});
$_M(c$, "calculateStraightness", 
function () {
this.modelSet.setHaveStraightness (false);
this.modelSet.calculateStraightness ();
});
$_M(c$, "calculateSurface", 
function (bsSelected, envelopeRadius) {
if (bsSelected == null) bsSelected = this.getSelectionSet (false);
if (envelopeRadius == 3.4028235E38 || envelopeRadius == -1) this.addStateScriptRet ("calculate surfaceDistance " + (envelopeRadius == 3.4028235E38 ? "FROM" : "WITHIN"), null, bsSelected, null, "", false, true);
return this.modelSet.calculateSurface (bsSelected, envelopeRadius);
}, "J.util.BS,~N");
$_M(c$, "getStructureList", 
function () {
return this.global.getStructureList ();
});
$_M(c$, "setStructureList", 
function (list, type) {
this.global.setStructureList (list, type);
this.modelSet.setStructureList (this.getStructureList ());
}, "~A,J.constant.EnumStructure");
$_M(c$, "getDefaultStructure", 
function (bsAtoms, bsAllAtoms) {
if (bsAtoms == null) bsAtoms = this.getSelectionSet (false);
return this.modelSet.getDefaultStructure (bsAtoms, bsAllAtoms);
}, "J.util.BS,J.util.BS");
$_M(c$, "calculateStructures", 
function (bsAtoms, asDSSP, setStructure) {
if (bsAtoms == null) bsAtoms = this.getSelectionSet (false);
return this.modelSet.calculateStructures (bsAtoms, asDSSP, this.global.dsspCalcHydrogen, setStructure);
}, "J.util.BS,~B,~B");
Clazz.overrideMethod (c$, "getSelectedAtomIterator", 
function (bsSelected, isGreaterOnly, modelZeroBased, isMultiModel) {
return this.modelSet.getSelectedAtomIterator (bsSelected, isGreaterOnly, modelZeroBased, false, isMultiModel);
}, "J.util.BS,~B,~B,~B");
Clazz.overrideMethod (c$, "setIteratorForAtom", 
function (iterator, atomIndex, distance) {
this.modelSet.setIteratorForAtom (iterator, -1, atomIndex, distance, null);
}, "J.api.AtomIndexIterator,~N,~N");
Clazz.overrideMethod (c$, "setIteratorForPoint", 
function (iterator, modelIndex, pt, distance) {
this.modelSet.setIteratorForPoint (iterator, modelIndex, pt, distance);
}, "J.api.AtomIndexIterator,~N,J.util.P3,~N");
Clazz.overrideMethod (c$, "fillAtomData", 
function (atomData, mode) {
atomData.programInfo = "Jmol Version " + J.viewer.Viewer.getJmolVersion ();
atomData.fileName = this.getFileName ();
this.modelSet.fillAtomData (atomData, mode);
}, "J.atomdata.AtomData,~N");
$_M(c$, "addStateScript", 
function (script, addFrameNumber, postDefinitions) {
return this.addStateScriptRet (script, null, null, null, null, addFrameNumber, postDefinitions);
}, "~S,~B,~B");
$_M(c$, "addStateScriptRet", 
function (script1, bsBonds, bsAtoms1, bsAtoms2, script2, addFrameNumber, postDefinitions) {
return this.modelSet.addStateScript (script1, bsBonds, bsAtoms1, bsAtoms2, script2, addFrameNumber, postDefinitions);
}, "~S,J.util.BS,J.util.BS,J.util.BS,~S,~B,~B");
$_M(c$, "getEchoStateActive", 
function () {
return this.modelSet.getEchoStateActive ();
});
$_M(c$, "setEchoStateActive", 
function (TF) {
this.modelSet.setEchoStateActive (TF);
}, "~B");
$_M(c$, "clearModelDependentObjects", 
($fz = function () {
this.setFrameOffsets (null);
this.stopMinimization ();
this.minimizer = null;
if (this.smilesMatcher != null) {
this.smilesMatcher = null;
}if (this.symmetry != null) {
this.symmetry = null;
}}, $fz.isPrivate = true, $fz));
$_M(c$, "zap", 
function (notify, resetUndo, zapModelKit) {
this.clearThreads ();
if (this.modelSet != null) {
this.ligandModelSet = null;
this.clearModelDependentObjects ();
this.fileManager.clear ();
this.clearRepaintManager (-1);
this.animationManager.clear ();
this.transformManager.clear ();
this.selectionManager.clear ();
this.clearAllMeasurements ();
this.clearMinimization ();
this.gdata.clear ();
this.modelManager.zap ();
if (this.scriptManager != null) this.scriptManager.clear (false);
if (this.nmrCalculation != null) this.getNMRCalculation ().setChemicalShiftReference (null, 0);
if (this.haveDisplay) {
this.mouse.clear ();
this.clearTimeouts ();
this.actionManager.clear ();
}this.stateManager.clear (this.global);
this.tempArray.clear ();
this.chainMap.clear ();
this.chainList.clear ();
this.colorManager.clear ();
this.definedAtomSets.clear ();
this.dataManager.clear ();
if (resetUndo) {
if (zapModelKit && this.global.modelKitMode) {
this.openStringInlineParamsAppend ("5\n\nC 0 0 0\nH .63 .63 .63\nH -.63 -.63 .63\nH -.63 .63 -.63\nH .63 -.63 -.63", null, true);
this.setRotationRadius (5.0, true);
this.setStringProperty ("picking", "assignAtom_C");
this.setStringProperty ("picking", "assignBond_p");
}this.undoClear ();
}System.gc ();
} else {
this.modelManager.zap ();
}this.initializeModel (false);
if (notify) {
this.setFileLoadStatus (J.constant.EnumFileStatus.ZAPPED, null, (resetUndo ? "resetUndo" : this.getZapName ()), null, null, null);
}if (J.util.Logger.debugging) J.util.Logger.checkMemory ();
}, "~B,~B,~B");
$_M(c$, "zapMsg", 
($fz = function (msg) {
this.zap (true, true, false);
this.echoMessage (msg);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "echoMessage", 
function (msg) {
var iShape = 30;
this.loadShape (iShape);
this.setShapeProperty (iShape, "font", this.getFont3D ("SansSerif", "Plain", 9));
this.setShapeProperty (iShape, "target", "error");
this.setShapeProperty (iShape, "text", msg);
}, "~S");
$_M(c$, "initializeModel", 
($fz = function (isAppend) {
this.clearThreads ();
if (isAppend) {
this.animationManager.initializePointers (1);
return;
}this.reset (true);
this.selectAll ();
this.rotatePrev1 = this.rotateBondIndex = -1;
this.movingSelected = false;
this.noneSelected = false;
this.hoverEnabled = true;
this.transformManager.setCenter ();
this.animationManager.initializePointers (1);
if (!this.modelSet.getModelSetAuxiliaryInfoBoolean ("isPyMOL")) {
this.clearAtomSets ();
this.setCurrentModelIndex (0);
}this.setBackgroundModelIndex (-1);
this.setFrankOn (this.getShowFrank ());
this.startHoverWatcher (true);
this.setTainted (true);
this.finalizeTransformParameters ();
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "startHoverWatcher", 
function (tf) {
if (!this.haveDisplay || tf && (!this.hoverEnabled || this.animationManager.animationOn)) return;
this.actionManager.startHoverWatcher (tf);
}, "~B");
Clazz.overrideMethod (c$, "getModelSetName", 
function () {
if (this.modelSet == null) return null;
return this.modelSet.modelSetName;
});
Clazz.overrideMethod (c$, "getModelSetFileName", 
function () {
return this.modelManager.getModelSetFileName ();
});
$_M(c$, "getUnitCellInfoText", 
function () {
return this.modelSet.getUnitCellInfoText ();
});
$_M(c$, "getUnitCellInfo", 
function (infoType) {
var symmetry = this.getCurrentUnitCell ();
if (symmetry == null) return NaN;
return symmetry.getUnitCellInfoType (infoType);
}, "~N");
$_M(c$, "getSpaceGroupInfo", 
function (spaceGroup) {
return this.modelSet.getSymTemp (true).getSpaceGroupInfo (this.modelSet, -1, spaceGroup, 0, null, null, null);
}, "~S");
$_M(c$, "getPolymerPointsAndVectors", 
function (bs, vList) {
this.modelSet.getPolymerPointsAndVectors (bs, vList, this.global.traceAlpha, this.global.sheetSmoothing);
}, "J.util.BS,J.util.JmolList");
$_M(c$, "getModelSetProperty", 
function (strProp) {
return this.modelSet.getModelSetProperty (strProp);
}, "~S");
$_M(c$, "getModelSetAuxiliaryInfoValue", 
function (strKey) {
return this.modelSet.getModelSetAuxiliaryInfoValue (strKey);
}, "~S");
Clazz.overrideMethod (c$, "getModelSetPathName", 
function () {
return this.modelManager.getModelSetPathName ();
});
$_M(c$, "getModelSetTypeName", 
function () {
return this.modelSet.getModelSetTypeName ();
});
Clazz.overrideMethod (c$, "haveFrame", 
function () {
return this.haveModelSet ();
});
$_M(c$, "haveModelSet", 
function () {
return this.modelSet != null;
});
$_M(c$, "clearBfactorRange", 
function () {
this.modelSet.clearBfactorRange ();
});
$_M(c$, "getHybridizationAndAxes", 
function (atomIndex, z, x, lcaoType) {
return this.modelSet.getHybridizationAndAxes (atomIndex, 0, z, x, lcaoType, true, true);
}, "~N,J.util.V3,J.util.V3,~S");
$_M(c$, "getMoleculeBitSet", 
function (atomIndex) {
return this.modelSet.getMoleculeBitSetForAtom (atomIndex);
}, "~N");
$_M(c$, "getModelUndeletedAtomsBitSet", 
function (modelIndex) {
var bs = this.modelSet.getModelAtomBitSetIncludingDeleted (modelIndex, true);
this.excludeAtoms (bs, false);
return bs;
}, "~N");
$_M(c$, "getModelBitSet", 
function (atomList, allTrajectories) {
return this.modelSet.getModelBitSet (atomList, allTrajectories);
}, "J.util.BS,~B");
$_M(c$, "getModelUndeletedAtomsBitSetBs", 
function (bsModels) {
var bs = this.modelSet.getModelAtomBitSetIncludingDeletedBs (bsModels);
this.excludeAtoms (bs, false);
return bs;
}, "J.util.BS");
$_M(c$, "excludeAtoms", 
function (bs, ignoreSubset) {
this.selectionManager.excludeAtoms (bs, ignoreSubset);
}, "J.util.BS,~B");
$_M(c$, "getModelSet", 
function () {
return this.modelSet;
});
$_M(c$, "getBoundBoxCommand", 
function (withOptions) {
return this.modelSet.getBoundBoxCommand (withOptions);
}, "~B");
$_M(c$, "setBoundBox", 
function (pt1, pt2, byCorner, scale) {
this.modelSet.setBoundBox (pt1, pt2, byCorner, scale);
}, "J.util.P3,J.util.P3,~B,~N");
Clazz.overrideMethod (c$, "getBoundBoxCenter", 
function () {
return this.modelSet.getBoundBoxCenter (this.animationManager.currentModelIndex);
});
$_M(c$, "getAverageAtomPoint", 
function () {
return this.modelSet.getAverageAtomPoint ();
});
$_M(c$, "calcBoundBoxDimensions", 
function (bs, scale) {
this.modelSet.calcBoundBoxDimensions (bs, scale);
this.axesAreTainted = true;
}, "J.util.BS,~N");
$_M(c$, "getBoxInfo", 
function (bs, scale) {
return this.modelSet.getBoxInfo (bs, scale);
}, "J.util.BS,~N");
$_M(c$, "calcRotationRadius", 
function (center) {
return this.modelSet.calcRotationRadius (this.animationManager.currentModelIndex, center);
}, "J.util.P3");
$_M(c$, "calcRotationRadiusBs", 
function (bs) {
return this.modelSet.calcRotationRadiusBs (bs);
}, "J.util.BS");
Clazz.overrideMethod (c$, "getBoundBoxCornerVector", 
function () {
return this.modelSet.getBoundBoxCornerVector ();
});
$_M(c$, "getBoundBoxVertices", 
function () {
return this.modelSet.getBboxVertices ();
});
$_M(c$, "getBoundBoxInfo", 
function () {
return this.modelSet.getBoundBoxInfo ();
});
$_M(c$, "getBoundBoxModels", 
function () {
return this.modelSet.getBoundBoxModels ();
});
$_M(c$, "getBoundBoxCenterX", 
function () {
return Clazz.doubleToInt (this.dimScreen.width / 2);
});
$_M(c$, "getBoundBoxCenterY", 
function () {
return Clazz.doubleToInt (this.dimScreen.height / 2);
});
Clazz.overrideMethod (c$, "getModelCount", 
function () {
return this.modelSet.modelCount;
});
$_M(c$, "getModelInfoAsString", 
function () {
return this.modelSet.getModelInfoAsString ();
});
$_M(c$, "getSymmetryInfoAsString", 
function () {
return this.modelSet.getSymmetryInfoAsString ();
});
$_M(c$, "getSymmetryOperation", 
function (spaceGroup, symop, pt1, pt2, labelOnly) {
return this.modelSet.getSymmetryOperation (this.animationManager.currentModelIndex, spaceGroup, symop, pt1, pt2, null, labelOnly);
}, "~S,~N,J.util.P3,J.util.P3,~B");
Clazz.overrideMethod (c$, "getModelSetProperties", 
function () {
return this.modelSet.getModelSetProperties ();
});
Clazz.overrideMethod (c$, "getModelSetAuxiliaryInfo", 
function () {
return this.modelSet.getModelSetAuxiliaryInfo ();
});
Clazz.overrideMethod (c$, "getModelNumber", 
function (modelIndex) {
return (modelIndex < 0 ? modelIndex : this.modelSet.getModelNumber (modelIndex));
}, "~N");
$_M(c$, "getModelFileNumber", 
function (modelIndex) {
return (modelIndex < 0 ? 0 : this.modelSet.getModelFileNumber (modelIndex));
}, "~N");
Clazz.overrideMethod (c$, "getModelNumberDotted", 
function (modelIndex) {
return modelIndex < 0 ? "0" : this.modelSet == null ? null : this.modelSet.getModelNumberDotted (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelName", 
function (modelIndex) {
return this.modelSet == null ? null : this.modelSet.getModelName (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelProperties", 
function (modelIndex) {
return this.modelSet.getModelProperties (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelProperty", 
function (modelIndex, propertyName) {
return this.modelSet.getModelProperty (modelIndex, propertyName);
}, "~N,~S");
$_M(c$, "getModelFileInfo", 
function () {
return this.getPropertyManager ().getModelFileInfo (this.getVisibleFramesBitSet ());
});
$_M(c$, "getModelFileInfoAll", 
function () {
return this.getPropertyManager ().getModelFileInfo (null);
});
Clazz.overrideMethod (c$, "getModelAuxiliaryInfo", 
function (modelIndex) {
return this.modelSet.getModelAuxiliaryInfo (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getModelAuxiliaryInfoValue", 
function (modelIndex, keyName) {
return this.modelSet.getModelAuxiliaryInfoValue (modelIndex, keyName);
}, "~N,~S");
$_M(c$, "getModelNumberIndex", 
function (modelNumber, useModelNumber, doSetTrajectory) {
return this.modelSet.getModelNumberIndex (modelNumber, useModelNumber, doSetTrajectory);
}, "~N,~B,~B");
$_M(c$, "modelSetHasVibrationVectors", 
function () {
return this.modelSet.modelSetHasVibrationVectors ();
});
Clazz.overrideMethod (c$, "modelGetLastVibrationIndex", 
function (modelIndex, tok) {
return this.modelSet.getLastVibrationVector (modelIndex, tok);
}, "~N,~N");
Clazz.overrideMethod (c$, "modelHasVibrationVectors", 
function (modelIndex) {
return (this.modelSet.getLastVibrationVector (modelIndex, 4166) >= 0);
}, "~N");
Clazz.overrideMethod (c$, "getChainCount", 
function () {
return this.modelSet.getChainCount (true);
});
Clazz.overrideMethod (c$, "getChainCountInModel", 
function (modelIndex) {
return this.modelSet.getChainCountInModel (modelIndex, false);
}, "~N");
$_M(c$, "getChainCountInModelWater", 
function (modelIndex, countWater) {
return this.modelSet.getChainCountInModel (modelIndex, countWater);
}, "~N,~B");
Clazz.overrideMethod (c$, "getGroupCount", 
function () {
return this.modelSet.getGroupCount ();
});
Clazz.overrideMethod (c$, "getGroupCountInModel", 
function (modelIndex) {
return this.modelSet.getGroupCountInModel (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getPolymerCount", 
function () {
return this.modelSet.getBioPolymerCount ();
});
Clazz.overrideMethod (c$, "getPolymerCountInModel", 
function (modelIndex) {
return this.modelSet.getBioPolymerCountInModel (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getAtomCount", 
function () {
return this.modelSet.getAtomCount ();
});
Clazz.overrideMethod (c$, "getAtomCountInModel", 
function (modelIndex) {
return this.modelSet.getAtomCountInModel (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getBondCount", 
function () {
return this.modelSet.bondCount;
});
Clazz.overrideMethod (c$, "getBondCountInModel", 
function (modelIndex) {
return this.modelSet.getBondCountInModel (modelIndex);
}, "~N");
$_M(c$, "getBondsForSelectedAtoms", 
function (bsAtoms) {
return this.modelSet.getBondsForSelectedAtoms (bsAtoms, this.global.bondModeOr || J.util.BSUtil.cardinalityOf (bsAtoms) == 1);
}, "J.util.BS");
$_M(c$, "frankClicked", 
function (x, y) {
return !this.global.disablePopupMenu && this.getShowFrank () && this.shapeManager.checkFrankclicked (x, y);
}, "~N,~N");
$_M(c$, "frankClickedModelKit", 
function (x, y) {
return !this.global.disablePopupMenu && this.global.modelKitMode && x >= 0 && y >= 0 && x < 40 && y < 80;
}, "~N,~N");
Clazz.overrideMethod (c$, "findNearestAtomIndex", 
function (x, y) {
return this.findNearestAtomIndexMovable (x, y, false);
}, "~N,~N");
$_M(c$, "findNearestAtomIndexMovable", 
function (x, y, mustBeMovable) {
return (this.modelSet == null || !this.global.atomPicking ? -1 : this.modelSet.findNearestAtomIndex (x, y, mustBeMovable ? this.selectionManager.getMotionFixedAtoms () : null, this.global.minPixelSelRadius));
}, "~N,~N,~B");
$_M(c$, "findAtomsInRectangle", 
function (rect) {
return this.modelSet.findAtomsInRectangle (rect, this.getVisibleFramesBitSet ());
}, "J.util.Rectangle");
$_M(c$, "toCartesian", 
function (pt, asAbsolute) {
var unitCell = this.getCurrentUnitCell ();
if (unitCell != null) unitCell.toCartesian (pt, asAbsolute);
}, "J.util.P3,~B");
$_M(c$, "toFractional", 
function (pt, asAbsolute) {
var unitCell = this.getCurrentUnitCell ();
if (unitCell != null) unitCell.toFractional (pt, asAbsolute);
}, "J.util.P3,~B");
$_M(c$, "toUnitCell", 
function (pt, offset) {
var unitCell = this.getCurrentUnitCell ();
if (unitCell != null) unitCell.toUnitCell (pt, offset);
}, "J.util.P3,J.util.P3");
$_M(c$, "setCurrentCage", 
function (isosurfaceId) {
var data = [isosurfaceId, null];
this.shapeManager.getShapePropertyData (24, "unitCell", data);
this.modelSet.setModelCage (this.getCurrentModelIndex (), data[1]);
}, "~S");
$_M(c$, "setCurrentCagePts", 
function (points) {
this.modelSet.setModelCage (this.getCurrentModelIndex (), this.getSymmetry ().getUnitCell (points));
}, "~A");
$_M(c$, "setCurrentUnitCellOffset", 
function (ijk) {
this.modelSet.setUnitCellOffset (this.animationManager.currentModelIndex, null, ijk);
}, "~N");
$_M(c$, "setCurrentUnitCellOffsetPt", 
function (pt) {
this.modelSet.setUnitCellOffset (this.animationManager.currentModelIndex, pt, 0);
}, "J.util.P3");
$_M(c$, "addUnitCellOffset", 
function (pt) {
var unitCell = this.getCurrentUnitCell ();
if (unitCell == null) return;
pt.add (unitCell.getCartesianOffset ());
}, "J.util.P3");
$_M(c$, "setAtomData", 
function (type, name, coordinateData, isDefault) {
this.modelSet.setAtomData (type, name, coordinateData, isDefault);
if (type == 2) this.checkCoordinatesChanged ();
this.refreshMeasures (true);
}, "~N,~S,~S,~B");
Clazz.overrideMethod (c$, "setCenterSelected", 
function () {
this.setCenterBitSet (this.getSelectionSet (false), true);
});
$_M(c$, "setApplySymmetryToBonds", 
function (TF) {
this.global.applySymmetryToBonds = TF;
}, "~B");
Clazz.overrideMethod (c$, "setBondTolerance", 
function (bondTolerance) {
this.global.setF ("bondTolerance", bondTolerance);
this.global.bondTolerance = bondTolerance;
}, "~N");
Clazz.overrideMethod (c$, "setMinBondDistance", 
function (minBondDistance) {
this.global.setF ("minBondDistance", minBondDistance);
this.global.minBondDistance = minBondDistance;
}, "~N");
$_M(c$, "getAtomIndices", 
function (bs) {
return this.modelSet.getAtomIndices (bs);
}, "J.util.BS");
$_M(c$, "getAtomBits", 
function (tokType, specInfo) {
return this.modelSet.getAtomBits (tokType, specInfo);
}, "~N,~O");
$_M(c$, "getSequenceBits", 
function (specInfo, bs) {
return this.modelSet.getSequenceBits (specInfo, bs);
}, "~S,J.util.BS");
$_M(c$, "getAtomsNearPt", 
function (distance, coord) {
var bs =  new J.util.BS ();
this.modelSet.getAtomsWithin (distance, coord, bs, -1);
return bs;
}, "~N,J.util.P3");
$_M(c$, "getAtomsNearPts", 
function (distance, points, bsInclude) {
return this.modelSet.getAtomsWithinBs (distance, points, bsInclude);
}, "~N,~A,J.util.BS");
$_M(c$, "getAtomsNearPlane", 
function (distance, plane) {
return this.modelSet.getAtomsWithin (distance, plane);
}, "~N,J.util.P4");
$_M(c$, "getAtomsWithinRadius", 
function (distance, bs, withinAllModels, rd) {
return this.modelSet.getAtomsWithinRD (distance, bs, withinAllModels, rd);
}, "~N,J.util.BS,~B,J.atomdata.RadiusData");
$_M(c$, "getAtomsConnected", 
function (min, max, intType, bs) {
return this.modelSet.getAtomsConnected (min, max, intType, bs);
}, "~N,~N,~N,J.util.BS");
$_M(c$, "getBranchBitSet", 
function (atomIndex, atomIndexNot, allowCyclic) {
if (atomIndex < 0 || atomIndex >= this.getAtomCount ()) return  new J.util.BS ();
return J.util.JmolMolecule.getBranchBitSet (this.modelSet.atoms, atomIndex, this.getModelUndeletedAtomsBitSet (this.modelSet.atoms[atomIndex].modelIndex), null, atomIndexNot, allowCyclic, true);
}, "~N,~N,~B");
$_M(c$, "getAtomIndexFromAtomNumber", 
function (atomNumber) {
return this.modelSet.getAtomIndexFromAtomNumber (atomNumber, this.getVisibleFramesBitSet ());
}, "~N");
Clazz.overrideMethod (c$, "getElementsPresentBitSet", 
function (modelIndex) {
return this.modelSet.getElementsPresentBitSet (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "getHeteroList", 
function (modelIndex) {
return this.modelSet.getHeteroList (modelIndex);
}, "~N");
$_M(c$, "getVisibleSet", 
function () {
return this.modelSet.getVisibleSet ();
});
$_M(c$, "getClickableSet", 
function () {
return this.modelSet.getClickableSet ();
});
$_M(c$, "calcSelectedGroupsCount", 
function () {
this.modelSet.calcSelectedGroupsCount (this.getSelectionSet (false));
});
$_M(c$, "calcSelectedMonomersCount", 
function () {
this.modelSet.calcSelectedMonomersCount (this.getSelectionSet (false));
});
$_M(c$, "calcSelectedMoleculesCount", 
function () {
this.modelSet.calcSelectedMoleculesCount (this.getSelectionSet (false));
});
$_M(c$, "getFileHeader", 
function () {
return this.modelSet.getFileHeader (this.animationManager.currentModelIndex);
});
$_M(c$, "getFileData", 
function () {
return this.modelSet.getFileData (this.animationManager.currentModelIndex);
});
$_M(c$, "getCifData", 
function (modelIndex) {
var name = this.getModelFileName (modelIndex);
var data = this.getFileAsString (name);
if (data == null) return null;
return J.io.CifDataReader.readCifData ( new java.io.BufferedReader ( new java.io.StringReader (data)));
}, "~N");
$_M(c$, "getPDBHeader", 
function () {
return this.modelSet.getPDBHeader (this.animationManager.currentModelIndex);
});
$_M(c$, "getAuxiliaryInfo", 
function (atomExpression) {
return this.modelSet.getAuxiliaryInfo (this.getModelBitSet (this.getAtomBitSet (atomExpression), false));
}, "~O");
$_M(c$, "getChimeInfo", 
function (tok) {
return this.getPropertyManager ().getChimeInfo (tok, this.getSelectionSet (false));
}, "~N");
$_M(c$, "getStateCreator", 
function () {
if (this.sc == null) (this.sc = J.api.Interface.getOptionInterface ("viewer.StateCreator")).setViewer (this, this.privateKey);
return this.sc;
});
$_M(c$, "getWrappedState", 
function (fileName, scripts, isImage, asJmolZip, width, height) {
return this.getStateCreator ().getWrappedState (fileName, scripts, isImage, asJmolZip, width, height);
}, "~S,~A,~B,~B,~N,~N");
Clazz.overrideMethod (c$, "getStateInfo", 
function () {
return this.getStateInfo3 (null, 0, 0);
});
$_M(c$, "getStateInfo3", 
function (type, width, height) {
return (this.global.preserveState ? this.getStateCreator ().getStateScript (type, width, height) : "");
}, "~S,~N,~N");
$_M(c$, "getStructureState", 
function () {
return this.getStateCreator ().getModelState (null, false, true);
});
$_M(c$, "getProteinStructureState", 
function () {
return this.modelSet.getProteinStructureState (this.getSelectionSet (false), false, false, 3);
});
$_M(c$, "getCoordinateState", 
function (bsSelected) {
return this.getStateCreator ().getAtomicPropertyState (2, bsSelected);
}, "J.util.BS");
$_M(c$, "setCurrentColorRange", 
function (label) {
var data = this.getDataFloat (label);
var bs = (data == null ? null : (this.dataManager.getData (label))[2]);
if (bs != null && this.global.rangeSelected) bs.and (this.getSelectionSet (false));
this.setCurrentColorRangeData (data, bs);
}, "~S");
$_M(c$, "setCurrentColorRangeData", 
function (data, bs) {
this.colorManager.setPropertyColorRangeData (data, bs, this.global.propertyColorScheme);
}, "~A,J.util.BS");
$_M(c$, "setCurrentColorRange", 
function (min, max) {
this.colorManager.setPropertyColorRange (min, max);
}, "~N,~N");
$_M(c$, "setData", 
function (type, data, arrayCount, matchField, matchFieldColumnCount, field, fieldColumnCount) {
this.dataManager.setData (type, data, arrayCount, this.getAtomCount (), matchField, matchFieldColumnCount, field, fieldColumnCount);
}, "~S,~A,~N,~N,~N,~N,~N");
$_M(c$, "getData", 
function (type) {
return this.dataManager.getData (type);
}, "~S");
$_M(c$, "getDataFloat", 
function (label) {
return this.dataManager.getDataFloatA (label);
}, "~S");
$_M(c$, "getDataFloat2D", 
function (label) {
return this.dataManager.getDataFloat2D (label);
}, "~S");
$_M(c$, "getDataFloat3D", 
function (label) {
return this.dataManager.getDataFloat3D (label);
}, "~S");
$_M(c$, "getDataFloatAt", 
function (label, atomIndex) {
return this.dataManager.getDataFloat (label, atomIndex);
}, "~S,~N");
Clazz.overrideMethod (c$, "getAltLocListInModel", 
function (modelIndex) {
return this.modelSet.getAltLocListInModel (modelIndex);
}, "~N");
$_M(c$, "setConformation", 
function () {
return this.modelSet.setConformation (this.getSelectionSet (false));
});
$_M(c$, "getConformation", 
function (iModel, conformationIndex, doSet) {
return this.modelSet.getConformation (iModel, conformationIndex, doSet);
}, "~N,~N,~B");
$_M(c$, "autoHbond", 
function (bsFrom, bsTo, onlyIfHaveCalculated) {
if (bsFrom == null) bsFrom = bsTo = this.getSelectionSet (false);
return this.modelSet.autoHbond (bsFrom, bsTo, onlyIfHaveCalculated);
}, "J.util.BS,J.util.BS,~B");
Clazz.overrideMethod (c$, "havePartialCharges", 
function () {
return this.modelSet.getPartialCharges () != null;
});
$_M(c$, "getCurrentUnitCell", 
function () {
if (this.animationManager.currentModelIndex >= 0) return this.modelSet.getUnitCell (this.animationManager.currentModelIndex);
var models = this.getVisibleFramesBitSet ();
var ucLast = null;
for (var i = models.nextSetBit (0); i >= 0; i = models.nextSetBit (i + 1)) {
var uc = this.modelSet.getUnitCell (i);
if (uc == null) continue;
if (ucLast == null) {
ucLast = uc;
continue;
}if (!ucLast.unitCellEquals (uc)) return null;
}
return ucLast;
});
$_M(c$, "getModelUnitCell", 
function (modelIndex) {
return this.modelSet.getUnitCell (modelIndex);
}, "~N");
$_M(c$, "getDefaultMeasurementLabel", 
function (nPoints) {
switch (nPoints) {
case 2:
return this.global.defaultDistanceLabel;
case 3:
return this.global.defaultAngleLabel;
default:
return this.global.defaultTorsionLabel;
}
}, "~N");
Clazz.overrideMethod (c$, "getMeasurementCount", 
function () {
var count = this.getShapePropertyAsInt (6, "count");
return count <= 0 ? 0 : count;
});
Clazz.overrideMethod (c$, "getMeasurementStringValue", 
function (i) {
var str = "" + this.getShapePropertyIndex (6, "stringValue", i);
return str;
}, "~N");
$_M(c$, "getMeasurementInfo", 
function () {
return this.getShapeProperty (6, "info");
});
$_M(c$, "getMeasurementInfoAsString", 
function () {
return this.getShapeProperty (6, "infostring");
});
Clazz.overrideMethod (c$, "getMeasurementCountPlusIndices", 
function (i) {
var List = this.getShapePropertyIndex (6, "countPlusIndices", i);
return List;
}, "~N");
$_M(c$, "setPendingMeasurement", 
function (measurementPending) {
this.setShapeProperty (6, "pending", measurementPending);
}, "J.modelset.MeasurementPending");
$_M(c$, "getPendingMeasurement", 
function () {
return this.getShapeProperty (6, "pending");
});
$_M(c$, "clearAllMeasurements", 
function () {
this.setShapeProperty (6, "clear", null);
});
Clazz.overrideMethod (c$, "clearMeasurements", 
function () {
this.evalString ("measures delete");
});
$_M(c$, "setAnimation", 
function (tok) {
switch (tok) {
case 1073742098:
this.animationManager.reverseAnimation ();
case 1073742096:
case 266287:
if (!this.animationManager.animationOn) this.animationManager.resumeAnimation ();
return;
case 20487:
if (this.animationManager.animationOn && !this.animationManager.animationPaused) this.animationManager.pauseAnimation ();
return;
case 1073742037:
this.animationManager.setAnimationNext ();
return;
case 1073742108:
this.animationManager.setAnimationPrevious ();
return;
case 1073741942:
case 1073742126:
this.animationManager.rewindAnimation ();
return;
case 1073741993:
this.animationManager.setAnimationLast ();
return;
}
}, "~N");
$_M(c$, "setAnimationDirection", 
function (direction) {
this.animationManager.setAnimationDirection (direction);
}, "~N");
$_M(c$, "getAnimationDirection", 
function () {
return this.animationManager.animationDirection;
});
$_M(c$, "getAnimationInfo", 
function () {
return this.getStateCreator ().getInfo (this.animationManager);
});
Clazz.overrideMethod (c$, "setAnimationFps", 
function (fps) {
if (fps < 1) fps = 1;
if (fps > 50) fps = 50;
this.global.setI ("animationFps", fps);
this.animationManager.setAnimationFps (fps);
}, "~N");
$_M(c$, "setAnimationReplayMode", 
function (replayMode, firstFrameDelay, lastFrameDelay) {
this.animationManager.setAnimationReplayMode (replayMode, firstFrameDelay, lastFrameDelay);
}, "J.constant.EnumAnimationMode,~N,~N");
$_M(c$, "getAnimationReplayMode", 
function () {
return this.animationManager.animationReplayMode;
});
$_M(c$, "setAnimationOn", 
function (animationOn) {
var wasAnimating = this.animationManager.animationOn;
if (animationOn == wasAnimating) return;
this.animationManager.setAnimationOn (animationOn);
}, "~B");
$_M(c$, "setAnimationRange", 
function (modelIndex1, modelIndex2) {
this.animationManager.setAnimationRange (modelIndex1, modelIndex2);
}, "~N,~N");
Clazz.overrideMethod (c$, "getVisibleFramesBitSet", 
function () {
return this.modelSet.selectDisplayedTrajectories (J.util.BSUtil.copy (this.animationManager.bsVisibleModels));
});
$_M(c$, "isAnimationOn", 
function () {
return this.animationManager.animationOn;
});
$_M(c$, "setMovie", 
function (info) {
this.animationManager.setMovie (info);
}, "java.util.Map");
$_M(c$, "setAnimMorphCount", 
function (n) {
this.animationManager.setMorphCount (n);
}, "~N");
$_M(c$, "isMovie", 
function () {
return this.animationManager.isMovie;
});
$_M(c$, "getFrameCount", 
function () {
return this.animationManager.getFrameCount ();
});
$_M(c$, "defineAtomSets", 
function (info) {
this.definedAtomSets.putAll (info);
}, "java.util.Map");
$_M(c$, "morph", 
function (frame) {
this.animationManager.morph (frame);
}, "~N");
$_M(c$, "setAnimDisplay", 
function (bs) {
this.animationManager.setDisplay (bs);
if (!this.isAnimationOn ()) this.animationManager.morph (this.animationManager.currentMorphModel + 1);
}, "J.util.BS");
$_M(c$, "setCurrentModelIndex", 
function (modelIndex) {
if (modelIndex == -2147483648) {
this.prevFrame = -2147483648;
this.setCurrentModelIndexClear (this.animationManager.currentModelIndex, true);
return;
}this.animationManager.setModel (modelIndex, true);
}, "~N");
$_M(c$, "setTrajectory", 
function (modelIndex) {
this.modelSet.setTrajectory (modelIndex);
}, "~N");
$_M(c$, "setTrajectoryBs", 
function (bsModels) {
this.modelSet.setTrajectoryBs (bsModels);
}, "J.util.BS");
$_M(c$, "isTrajectory", 
function (modelIndex) {
return this.modelSet.isTrajectory (modelIndex);
}, "~N");
$_M(c$, "getBitSetTrajectories", 
function () {
return this.modelSet.getBitSetTrajectories ();
});
$_M(c$, "getTrajectoryState", 
function () {
return this.getStateCreator ().getTrajectoryState ();
});
$_M(c$, "setFrameOffset", 
function (modelIndex) {
this.transformManager.setFrameOffset (modelIndex);
}, "~N");
$_M(c$, "setFrameOffsets", 
function (bsAtoms) {
this.bsFrameOffsets = bsAtoms;
this.transformManager.setFrameOffsets (this.frameOffsets = this.modelSet.getFrameOffsets (this.bsFrameOffsets));
}, "J.util.BS");
$_M(c$, "getFrameOffsets", 
function () {
return this.bsFrameOffsets;
});
$_M(c$, "setCurrentModelIndexClear", 
function (modelIndex, clearBackground) {
this.animationManager.setModel (modelIndex, clearBackground);
}, "~N,~B");
$_M(c$, "getCurrentModelIndex", 
function () {
return this.animationManager.currentModelIndex;
});
Clazz.overrideMethod (c$, "getDisplayModelIndex", 
function () {
return this.animationManager.currentModelIndex;
});
$_M(c$, "haveFileSet", 
function () {
return (this.getModelCount () > 1 && this.getModelNumber (2147483647) > 2000000);
});
$_M(c$, "setBackgroundModelIndex", 
function (modelIndex) {
this.animationManager.setBackgroundModelIndex (modelIndex);
this.global.setS ("backgroundModel", this.modelSet.getModelNumberDotted (modelIndex));
}, "~N");
$_M(c$, "setFrameVariables", 
function () {
this.global.setS ("_firstFrame", this.animationManager.getModelSpecial (-1));
this.global.setS ("_lastFrame", this.animationManager.getModelSpecial (1));
this.global.setF ("_animTimeSec", this.animationManager.getAnimRunTimeSeconds ());
this.global.setB ("_animMovie", this.animationManager.isMovie);
});
$_M(c$, "getInMotion", 
function (includeAnim) {
return (this.inMotion || includeAnim && this.animationManager.animationOn);
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
this.refresh (3, "viewer setInMotion " + inMotion);
}}}, "~B");
$_M(c$, "setRefreshing", 
($fz = function (TF) {
this.refreshing = TF;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getRefreshing", 
function () {
return this.refreshing;
});
$_M(c$, "pushHoldRepaint", 
function () {
this.pushHoldRepaint (null);
});
$_M(c$, "pushHoldRepaint", 
function (why) {
if (this.repaintManager != null) this.repaintManager.pushHoldRepaint (why);
}, "~S");
Clazz.overrideMethod (c$, "popHoldRepaint", 
function (why) {
if (this.repaintManager != null) {
this.repaintManager.popHoldRepaint (why.indexOf ("\u0001## REPAINT_IGNORE ##") < 0, why);
}}, "~S");
Clazz.overrideMethod (c$, "refresh", 
function (mode, strWhy) {
if (this.repaintManager == null || !this.refreshing) return;
if (mode == 6 && this.getInMotion (true)) return;
{
if (typeof Jmol == "undefined") return; if (!this.isWebGL) {
if (mode == 7)return; if (mode > 0)
this.repaintManager.repaintIfReady(); } else if (mode == 2 ||
mode == 7) {
this.transformManager.finalizeTransformParameters(); if
(Jmol._refresh) Jmol._refresh(this.applet, mode, strWhy,
[this.transformManager.fixedRotationCenter,
this.transformManager.getRotationQuaternion(),
this.transformManager.xTranslationFraction,
this.transformManager.yTranslationFraction,
this.transformManager.modelRadius,
this.transformManager.scalePixelsPerAngstrom,
this.transformManager.zoomPercent ]); if (mode == 7)return; }
}if (mode % 3 != 0 && this.statusManager.doSync ()) this.statusManager.setSync (mode == 2 ? strWhy : null);
}, "~N,~S");
$_M(c$, "requestRepaintAndWait", 
function (why) {
if (!this.haveDisplay || this.repaintManager == null) return;
this.repaintManager.requestRepaintAndWait (why);
this.setSync ();
}, "~S");
$_M(c$, "clearShapeRenderers", 
function () {
this.clearRepaintManager (-1);
});
$_M(c$, "isRepaintPending", 
function () {
return (this.repaintManager == null ? false : this.repaintManager.isRepaintPending ());
});
Clazz.overrideMethod (c$, "notifyViewerRepaintDone", 
function () {
if (this.repaintManager != null) this.repaintManager.repaintDone ();
this.animationManager.repaintDone ();
});
$_M(c$, "areAxesTainted", 
function () {
var TF = this.axesAreTainted;
this.axesAreTainted = false;
return TF;
});
$_M(c$, "setMaximumSize", 
($fz = function (x) {
this.maximumSize = Math.max (x, 100);
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "setScreenDimension", 
function (width, height) {
height = Math.min (height, this.maximumSize);
width = Math.min (width, this.maximumSize);
if (this.isStereoDouble ()) width = Clazz.doubleToInt ((width + 1) / 2);
if (this.dimScreen.width == width && this.dimScreen.height == height) return;
this.resizeImage (width, height, false, false, true);
}, "~N,~N");
$_M(c$, "getImageFontScaling", 
function () {
return this.imageFontScaling;
});
$_M(c$, "resizeImage", 
function (width, height, isImageWrite, isExport, isReset) {
if (!isImageWrite && this.creatingImage) return;
if (!isExport && !isImageWrite) this.setShapeProperty (5, "clearBoxes", null);
this.antialiasDisplay = (isReset ? this.global.antialiasDisplay && this.checkMotionRendering (603979786) : isImageWrite && !isExport ? this.global.antialiasImages : false);
this.imageFontScaling = (isReset || width <= 0 ? 1 : Clazz.doubleToInt ((this.global.zoomLarge == (height > width) ? height : width) / this.getScreenDim ())) * (this.antialiasDisplay ? 2 : 1);
if (width > 0) {
this.dimScreen.width = width;
this.dimScreen.height = height;
if (!isImageWrite) {
this.global.setI ("_width", width);
this.global.setI ("_height", height);
this.setStatusResized (width, height);
}} else {
width = (this.dimScreen.width == 0 ? this.dimScreen.width = 500 : this.dimScreen.width);
height = (this.dimScreen.height == 0 ? this.dimScreen.height = 500 : this.dimScreen.height);
}this.transformManager.setScreenParameters (width, height, isImageWrite || isReset ? this.global.zoomLarge : false, this.antialiasDisplay, false, false);
this.gdata.setWindowParameters (width, height, this.antialiasDisplay);
}, "~N,~N,~B,~B,~B");
Clazz.overrideMethod (c$, "getScreenWidth", 
function () {
return this.dimScreen.width;
});
Clazz.overrideMethod (c$, "getScreenHeight", 
function () {
return this.dimScreen.height;
});
$_M(c$, "getScreenDim", 
function () {
return (this.global.zoomLarge == (this.dimScreen.height > this.dimScreen.width) ? this.dimScreen.height : this.dimScreen.width);
});
Clazz.overrideMethod (c$, "generateOutputForExport", 
function (type, fileName, width, height) {
if (this.$noGraphicsAllowed || this.repaintManager == null) return null;
return this.getStateCreator ().generateOutputForExport (type, fileName, width, height);
}, "~S,~A,~N,~N");
$_M(c$, "clearRepaintManager", 
($fz = function (iShape) {
if (this.repaintManager != null) this.repaintManager.clear (iShape);
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "renderScreenImageStereo", 
function (gLeft, gRight, width, height) {
if (this.updateWindow (width, height)) {
if (gRight == null) {
this.getScreenImageBuffer (gLeft, false);
} else {
this.render1 (gRight, this.getImage (true, false), 0, 0);
this.render1 (gLeft, this.getImage (false, false), 0, 0);
}}this.notifyViewerRepaintDone ();
}, "~O,~O,~N,~N");
$_M(c$, "updateJS", 
function (width, height) {
{
if (!this.isWebGL) {
this.renderScreenImageStereo(this.apiPlatform.context, null,
width, height); return; } if (this.updateWindow(width,
height)){ this.render(); } this.notifyViewerRepaintDone();
}}, "~N,~N");
$_M(c$, "updateWindow", 
($fz = function (width, height) {
if (!this.refreshing || this.creatingImage) return false;
if (this.isTainted || this.getSlabEnabled ()) this.setModelVisibility ();
this.isTainted = false;
if (this.repaintManager != null) {
if (width != 0) this.setScreenDimension (width, height);
}return true;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "renderScreenImage", 
function (g, width, height) {
this.renderScreenImageStereo (g, null, width, height);
}, "~O,~N,~N");
$_M(c$, "getImage", 
($fz = function (isDouble, isImageWrite) {
{
if (this.isWebGL)return null;
}var image = null;
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
}, $fz.isPrivate = true, $fz), "~B,~B");
$_M(c$, "beginRendering", 
($fz = function (isDouble, isImageWrite) {
this.gdata.beginRendering (this.transformManager.getStereoRotationMatrix (isDouble), this.global.translucent, isImageWrite, !this.checkMotionRendering (603979967));
}, $fz.isPrivate = true, $fz), "~B,~B");
$_M(c$, "isAntialiased", 
function () {
return this.antialiasDisplay;
});
$_M(c$, "render", 
($fz = function () {
if (this.modelSet == null || !this.mustRender || !this.refreshing && !this.creatingImage || this.repaintManager == null) return;
var antialias2 = this.antialiasDisplay && this.global.antialiasTranslucent;
this.finalizeTransformParameters ();
this.shapeManager.finalizeAtoms (this.transformManager.bsSelectedAtoms, this.transformManager.ptOffset);
var minMax = this.shapeManager.transformAtoms ();
this.transformManager.bsSelectedAtoms = null;
{
if (this.isWebGL) { this.repaintManager.renderExport("JS",
this.gdata, this.modelSet, null);
this.notifyViewerRepaintDone(); return; }
}this.repaintManager.render (this.gdata, this.modelSet, true, minMax);
if (this.gdata.setPass2 (antialias2)) {
this.transformManager.setAntialias (antialias2);
this.repaintManager.render (this.gdata, this.modelSet, false, null);
this.transformManager.setAntialias (this.antialiasDisplay);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "render1", 
($fz = function (graphic, img, x, y) {
if (graphic != null && img != null) {
this.apiPlatform.drawImage (graphic, img, x, y, this.dimScreen.width, this.dimScreen.height);
}this.gdata.releaseScreenImage ();
}, $fz.isPrivate = true, $fz), "~O,~O,~N,~N");
Clazz.overrideMethod (c$, "getScreenImageBuffer", 
function (graphic, isImageWrite) {
{
if (this.isWebGL)return null
}{
var mergeImages = (graphic == null && this.isStereoDouble ());
var imageBuffer;
if (this.transformManager.stereoMode.isBiColor ()) {
this.beginRendering (true, isImageWrite);
this.render ();
this.gdata.endRendering ();
this.gdata.snapshotAnaglyphChannelBytes ();
this.beginRendering (false, isImageWrite);
this.render ();
this.gdata.endRendering ();
this.gdata.applyAnaglygh (this.transformManager.stereoMode, this.transformManager.stereoColors);
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
}}, "~O,~B");
Clazz.overrideMethod (c$, "getImageAs", 
function (type, quality, width, height, fileName, os) {
{
if (this.isWebGL)return null
}return this.getImageAsWithComment (type, quality, width, height, fileName, null, os, "");
}, "~S,~N,~N,~N,~S,java.io.OutputStream");
$_M(c$, "getImageAsWithComment", 
function (type, quality, width, height, fileName, scripts, os, comment) {
{
if (this.isWebGL)return null
}return this.getStateCreator ().getImageAsWithComment (type, quality, width, height, fileName, scripts, os, comment);
}, "~S,~N,~N,~N,~S,~A,java.io.OutputStream,~S");
Clazz.overrideMethod (c$, "releaseScreenImage", 
function () {
this.gdata.releaseScreenImage ();
});
Clazz.overrideMethod (c$, "evalFile", 
function (strFilename) {
if (!this.allowScripting) return null;
var ptWait = strFilename.indexOf (" -noqueue");
if (ptWait >= 0) {
return this.evalStringWaitStatusQueued ("String", strFilename.substring (0, ptWait), "", true, false, false);
}return this.getScriptManager ().addScript (strFilename, true, false);
}, "~S");
$_M(c$, "getInsertedCommand", 
function () {
var s = this.insertedCommand;
this.insertedCommand = "";
if (J.util.Logger.debugging && s !== "") J.util.Logger.debug ("inserting: " + s);
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
$_M(c$, "evalStringQuietSync", 
function (strScript, isQuiet, allowSyncScript) {
return (this.getScriptManager () == null ? null : this.scriptManager.evalStringQuietSync (strScript, isQuiet, allowSyncScript));
}, "~S,~B,~B");
$_M(c$, "clearScriptQueue", 
function () {
if (this.scriptManager != null) this.scriptManager.clearQueue ();
});
$_M(c$, "setScriptQueue", 
($fz = function (TF) {
this.global.useScriptQueue = TF;
if (!TF) this.clearScriptQueue ();
}, $fz.isPrivate = true, $fz), "~B");
Clazz.overrideMethod (c$, "checkHalt", 
function (str, isInsert) {
return (this.scriptManager != null && this.scriptManager.checkHalt (str, isInsert));
}, "~S,~B");
Clazz.overrideMethod (c$, "scriptWait", 
function (strScript) {
return this.evalWait ("JSON", strScript, "+scriptStarted,+scriptStatus,+scriptEcho,+scriptTerminated");
}, "~S");
Clazz.overrideMethod (c$, "scriptWaitStatus", 
function (strScript, statusList) {
return this.evalWait ("object", strScript, statusList);
}, "~S,~S");
$_M(c$, "evalWait", 
($fz = function (returnType, strScript, statusList) {
if (this.getScriptManager () == null) return null;
this.scriptManager.waitForQueue ();
var doTranslateTemp = J.i18n.GT.setDoTranslate (false);
var ret = this.evalStringWaitStatusQueued (returnType, strScript, statusList, false, false, false);
J.i18n.GT.setDoTranslate (doTranslateTemp);
return ret;
}, $fz.isPrivate = true, $fz), "~S,~S,~S");
$_M(c$, "evalStringWaitStatusQueued", 
function (returnType, strScript, statusList, isScriptFile, isQuiet, isQueued) {
{
if (strScript.indexOf("JSCONSOLE") == 0) {
this.applet._showInfo(true); return null; }
}if (this.getScriptManager () == null) return null;
return this.scriptManager.evalStringWaitStatusQueued (returnType, strScript, statusList, isScriptFile, isQuiet, isQueued);
}, "~S,~S,~S,~B,~B,~B");
$_M(c$, "exitJmol", 
function () {
if (this.$isApplet) return;
if (this.headlessImage != null) {
try {
var p = this.headlessImage;
if (this.isHeadless ()) this.createImage (p[0], p[1], null, (p[2]).intValue (), (p[3]).intValue (), (p[4]).intValue ());
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}if (J.util.Logger.debugging) J.util.Logger.debug ("exitJmol -- exiting");
System.out.flush ();
System.exit (0);
});
$_M(c$, "scriptCheckRet", 
($fz = function (strScript, returnContext) {
if (this.getScriptManager () == null) return null;
return this.scriptManager.scriptCheckRet (strScript, returnContext);
}, $fz.isPrivate = true, $fz), "~S,~B");
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
$_M(c$, "pauseScriptExecution", 
function () {
if (this.eval != null) this.eval.pauseExecution (true);
});
$_M(c$, "getDefaultLoadFilter", 
function () {
return this.global.defaultLoadFilter;
});
$_M(c$, "getDefaultLoadScript", 
function () {
return this.global.defaultLoadScript;
});
$_M(c$, "resolveDatabaseFormat", 
function (fileName) {
if (J.viewer.Viewer.hasDatabasePrefix (fileName)) fileName = this.setLoadFormat (fileName, fileName.charAt (0), false);
return fileName;
}, "~S");
c$.isDatabaseCode = $_M(c$, "isDatabaseCode", 
function (ch) {
return (ch == '$' || ch == '=' || ch == ':');
}, "~S");
c$.hasDatabasePrefix = $_M(c$, "hasDatabasePrefix", 
function (fileName) {
return (fileName.length != 0 && J.viewer.Viewer.isDatabaseCode (fileName.charAt (0)));
}, "~S");
$_M(c$, "setLoadFormat", 
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
f = this.global.resolveDataBase (database, f.substring (pt + 1));
return (f == null ? name : f);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return name;
} else {
throw e;
}
}
}case '#':
var s = (type == '=' ? this.global.loadFormat : this.global.loadLigandFormat);
if (f.indexOf (".") > 0 && s.indexOf ("%FILE.") >= 0) s = s.substring (0, s.indexOf ("%FILE") + 5);
return J.util.TextFormat.formatStringS (s, "FILE", f);
case ':':
format = this.global.pubChemFormat;
var fl = f.toLowerCase ();
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
} else if (fl.startsWith ("cid:")) {
f = "cid/" + f.substring (4);
} else {
if (fl.startsWith ("name:")) f = f.substring (5);
if (fl.startsWith ("cas:")) f = f.substring (4);
f = "name/" + J.util.Escape.escapeUrl (f);
}}return J.util.TextFormat.formatStringS (format, "FILE", f);
case '$':
if (name.startsWith ("$$")) {
f = f.substring (1);
format = J.util.TextFormat.simpleReplace (this.global.smilesUrlFormat, "&get3d=True", "");
return J.util.TextFormat.formatStringS (format, "FILE", J.util.Escape.escapeUrl (f));
}case 'N':
case '2':
case 'I':
case 'K':
case '/':
f = J.util.Escape.escapeUrl (f);
switch (type) {
case 'N':
format = this.global.nihResolverFormat + "/names";
break;
case '2':
format = this.global.nihResolverFormat + "/image";
break;
case 'I':
format = this.global.nihResolverFormat + "/stdinchi";
break;
case 'K':
format = this.global.nihResolverFormat + "/inchikey";
break;
case '/':
format = this.global.nihResolverFormat + "/";
break;
default:
format = this.global.smilesUrlFormat;
break;
}
return (withPrefix ? "MOL3D::" : "") + J.util.TextFormat.formatStringS (format, "FILE", f);
case '_':
var server = J.viewer.FileManager.fixFileNameVariables (this.global.edsUrlFormat, f);
var strCutoff = J.viewer.FileManager.fixFileNameVariables (this.global.edsUrlCutoff, f);
return [server, strCutoff];
}
return f;
}, "~S,~S,~B");
$_M(c$, "getElectronDensityLoadInfo", 
function () {
return [this.global.edsUrlFormat, this.global.edsUrlCutoff, this.global.edsUrlOptions];
});
$_M(c$, "getStandardLabelFormat", 
function (type) {
switch (type) {
default:
case 0:
return "%[identify]";
case 1:
return this.global.defaultLabelXYZ;
case 2:
return this.global.defaultLabelPDB;
}
}, "~N");
$_M(c$, "getAdditionalHydrogens", 
function (bsAtoms, doAll, justCarbon, vConnections) {
if (bsAtoms == null) bsAtoms = this.getSelectionSet (false);
var nTotal =  Clazz.newIntArray (1, 0);
var pts = this.modelSet.calculateHydrogens (bsAtoms, nTotal, doAll, justCarbon, vConnections);
var points =  new Array (nTotal[0]);
for (var i = 0, pt = 0; i < pts.length; i++) if (pts[i] != null) for (var j = 0; j < pts[i].length; j++) points[pt++] = pts[i][j];


return points;
}, "J.util.BS,~B,~B,J.util.JmolList");
Clazz.overrideMethod (c$, "setMarBond", 
function (marBond) {
this.global.bondRadiusMilliAngstroms = marBond;
this.global.setI ("bondRadiusMilliAngstroms", marBond);
this.setShapeSize (1, marBond * 2, J.util.BSUtil.setAll (this.getAtomCount ()));
}, "~N");
$_M(c$, "setHoverLabel", 
function (strLabel) {
this.loadShape (34);
this.setShapeProperty (34, "label", strLabel);
this.hoverEnabled = (strLabel != null);
if (!this.hoverEnabled) this.startHoverWatcher (false);
}, "~S");
$_M(c$, "hoverOn", 
function (atomIndex, isLabel) {
this.setStatusAtomHovered (atomIndex, this.getAtomInfoXYZ (atomIndex, false));
if (!this.hoverEnabled) return;
if (this.global.modelKitMode) {
if (this.isAtomAssignable (atomIndex)) this.highlight (J.util.BSUtil.newAndSetBit (atomIndex));
this.refresh (3, "hover on atom");
return;
}if (this.eval != null && this.isScriptExecuting () || atomIndex == this.hoverAtomIndex || this.global.hoverDelayMs == 0) return;
if (!this.isInSelectionSubset (atomIndex)) return;
this.loadShape (34);
if (isLabel && this.modelSet.atoms[atomIndex].isShapeVisible (J.viewer.JC.getShapeVisibilityFlag (5))) {
this.setShapeProperty (34, "specialLabel", J.i18n.GT._ ("Drag to move label"));
}this.setShapeProperty (34, "text", null);
this.setShapeProperty (34, "target", Integer.$valueOf (atomIndex));
this.hoverText = null;
this.hoverAtomIndex = atomIndex;
this.refresh (3, "hover on atom");
}, "~N,~B");
$_M(c$, "hoverOnPt", 
function (x, y, text, id, pt) {
if (!this.hoverEnabled) return;
if (this.eval != null && this.isScriptExecuting ()) return;
this.loadShape (34);
this.setShapeProperty (34, "xy", J.util.P3i.new3 (x, y, 0));
this.setShapeProperty (34, "target", null);
this.setShapeProperty (34, "specialLabel", null);
this.setShapeProperty (34, "text", text);
this.hoverAtomIndex = -1;
this.hoverText = text;
if (id != null && pt != null) this.setStatusObjectHovered (id, text, pt);
this.refresh (3, "hover on point");
}, "~N,~N,~S,~S,J.util.P3");
$_M(c$, "hoverOff", 
function () {
try {
if (this.global.modelKitMode) this.highlight (null);
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
$_M(c$, "getBfactor100Hi", 
function () {
return this.modelSet.getBfactor100Hi ();
});
$_M(c$, "getColix", 
function (object) {
return J.util.C.getColixO (object);
}, "~O");
Clazz.overrideMethod (c$, "setDebugScript", 
function (debugScript) {
this.global.debugScript = debugScript;
this.global.setB ("debugScript", debugScript);
if (this.eval != null) this.eval.setDebugging ();
}, "~B");
$_M(c$, "clearClickCount", 
function () {
this.setTainted (true);
});
$_M(c$, "getCursor", 
function () {
return this.currentCursor;
});
$_M(c$, "setCursor", 
function (cursor) {
if (this.$isKiosk || this.currentCursor == cursor || this.multiTouch || !this.haveDisplay) return;
this.apiPlatform.setCursor (this.currentCursor = cursor, this.display);
}, "~N");
$_M(c$, "setPickingMode", 
function (strMode, pickingMode) {
if (!this.haveDisplay) return;
this.showSelected = false;
var option = null;
if (strMode != null) {
var pt = strMode.indexOf ("_");
if (pt >= 0) {
option = strMode.substring (pt + 1);
strMode = strMode.substring (0, pt);
}pickingMode = J.viewer.ActionManager.getPickingMode (strMode);
}if (pickingMode < 0) pickingMode = 1;
this.actionManager.setPickingMode (pickingMode);
this.global.setS ("picking", J.viewer.ActionManager.getPickingModeName (this.actionManager.getAtomPickingMode ()));
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
J.util.Logger.error ("Bad picking mode: " + strMode + "_" + option);
}
}, "~S,~N");
$_M(c$, "getPickingMode", 
function () {
return (this.haveDisplay ? this.actionManager.getAtomPickingMode () : 0);
});
$_M(c$, "setPickingStyle", 
function (style, pickingStyle) {
if (!this.haveDisplay) return;
if (style != null) pickingStyle = J.viewer.ActionManager.getPickingStyleIndex (style);
if (pickingStyle < 0) pickingStyle = 0;
this.actionManager.setPickingStyle (pickingStyle);
this.global.setS ("pickingStyle", J.viewer.ActionManager.getPickingStyleName (this.actionManager.getPickingStyle ()));
}, "~S,~N");
$_M(c$, "getDrawHover", 
function () {
return this.haveDisplay && this.global.drawHover;
});
Clazz.overrideMethod (c$, "getAtomInfo", 
function (atomOrPointIndex) {
return (atomOrPointIndex >= 0 ? this.modelSet.getAtomInfo (atomOrPointIndex, null) : this.shapeManager.getShapePropertyIndex (6, "pointInfo", -atomOrPointIndex));
}, "~N");
$_M(c$, "getAtomInfoXYZ", 
function (atomIndex, useChimeFormat) {
return this.modelSet.getAtomInfoXYZ (atomIndex, useChimeFormat);
}, "~N,~B");
$_M(c$, "setSync", 
($fz = function () {
if (this.statusManager.doSync ()) this.statusManager.setSync (null);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "setJmolCallbackListener", 
function (jmolCallbackListener) {
this.statusManager.setJmolCallbackListener (jmolCallbackListener);
}, "J.api.JmolCallbackListener");
Clazz.overrideMethod (c$, "setJmolStatusListener", 
function (jmolStatusListener) {
this.statusManager.setJmolStatusListener (jmolStatusListener, null);
}, "J.api.JmolStatusListener");
$_M(c$, "getMessageQueue", 
function () {
return this.statusManager.getMessageQueue ();
});
$_M(c$, "getStatusChanged", 
function (statusNameList) {
return (statusNameList == null ? null : this.statusManager.getStatusChanged (statusNameList));
}, "~S");
$_M(c$, "menuEnabled", 
function () {
return (!this.global.disablePopupMenu && this.getPopupMenu () != null);
});
$_M(c$, "popupMenu", 
function (x, y, type) {
if (!this.haveDisplay || !this.refreshing || this.$isPreviewOnly || this.global.disablePopupMenu) return;
switch (type) {
case 'j':
try {
this.getPopupMenu ();
this.jmolpopup.jpiShow (x, y);
} catch (e) {
this.global.disablePopupMenu = true;
}
break;
case 'a':
case 'b':
case 'm':
this.modelkitPopup = this.apiPlatform.getMenuPopup (this, null, type);
if (this.modelkitPopup != null) this.modelkitPopup.jpiShow (x, y);
break;
}
}, "~N,~N,~S");
$_M(c$, "getMenu", 
function (type) {
this.getPopupMenu ();
if (type.equals ("\0")) {
this.popupMenu (this.dimScreen.width - 120, 0, 'j');
return "OK";
}return (this.jmolpopup == null ? "" : this.jmolpopup.jpiGetMenuAsString ("Jmol version " + J.viewer.Viewer.getJmolVersion () + "|_GET_MENU|" + type));
}, "~S");
$_M(c$, "getPopupMenu", 
($fz = function () {
if (this.jmolpopup == null) {
this.jmolpopup = (this.allowScripting ? this.apiPlatform.getMenuPopup (this, this.menuStructure, 'j') : null);
if (this.jmolpopup == null) {
this.global.disablePopupMenu = true;
return null;
}}return this.jmolpopup.jpiGetMenuAsObject ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setMenu", 
function (fileOrText, isFile) {
if (isFile) J.util.Logger.info ("Setting menu " + (fileOrText.length == 0 ? "to Jmol defaults" : "from file " + fileOrText));
if (fileOrText.length == 0) fileOrText = null;
 else if (isFile) fileOrText = this.getFileAsString (fileOrText);
this.getProperty ("DATA_API", "setMenu", fileOrText);
this.statusManager.setCallbackFunction ("menu", fileOrText);
}, "~S,~B");
$_M(c$, "setStatusFrameChanged", 
function (isVib) {
if (isVib) {
this.prevFrame = -2147483648;
}var frameNo = this.animationManager.getCurrentModelIndex ();
this.transformManager.setVibrationPeriod (NaN);
var firstIndex = this.animationManager.firstFrameIndex;
var lastIndex = this.animationManager.lastFrameIndex;
var isMovie = this.isMovie ();
var modelIndex = this.animationManager.currentModelIndex;
if (firstIndex == lastIndex && !isMovie) modelIndex = firstIndex;
var frameID = this.getModelFileNumber (modelIndex);
var currentFrame = this.animationManager.getCurrentModelIndex ();
var fileNo = frameID;
var modelNo = frameID % 1000000;
var firstNo = (isMovie ? firstIndex : this.getModelFileNumber (firstIndex));
var lastNo = (isMovie ? lastIndex : this.getModelFileNumber (lastIndex));
var strModelNo;
if (isMovie) {
strModelNo = "" + (frameNo + 1);
} else if (fileNo == 0) {
strModelNo = this.getModelNumberDotted (firstIndex);
if (firstIndex != lastIndex) strModelNo += " - " + this.getModelNumberDotted (lastIndex);
if (Clazz.doubleToInt (firstNo / 1000000) == Clazz.doubleToInt (lastNo / 1000000)) fileNo = firstNo;
} else {
strModelNo = this.getModelNumberDotted (modelIndex);
}if (fileNo != 0) fileNo = (fileNo < 1000000 ? 1 : Clazz.doubleToInt (fileNo / 1000000));
if (!isMovie) {
this.global.setI ("_currentFileNumber", fileNo);
this.global.setI ("_currentModelNumberInFile", modelNo);
}this.global.setI ("_currentFrame", currentFrame);
this.global.setI ("_morphCount", this.animationManager.morphCount);
this.global.setF ("_currentMorphFrame", this.animationManager.currentMorphModel);
this.global.setI ("_frameID", frameID);
this.global.setS ("_modelNumber", strModelNo);
this.global.setS ("_modelName", (modelIndex < 0 ? "" : this.getModelName (modelIndex)));
this.global.setS ("_modelTitle", (modelIndex < 0 ? "" : this.getModelTitle (modelIndex)));
this.global.setS ("_modelFile", (modelIndex < 0 ? "" : this.modelSet.getModelFileName (modelIndex)));
this.global.setS ("_modelType", (modelIndex < 0 ? "" : this.modelSet.getModelFileType (modelIndex)));
if (currentFrame == this.prevFrame) return;
this.prevFrame = currentFrame;
var entryName;
if (isMovie) {
entryName = "" + (this.animationManager.getCurrentModelIndex () + 1);
} else {
entryName = this.getModelName (frameNo);
var script = "" + this.getModelNumberDotted (frameNo);
if (!entryName.equals (script)) entryName = script + ": " + entryName;
if (entryName.length > 50) entryName = entryName.substring (0, 45) + "...";
}this.statusManager.setStatusFrameChanged (frameNo, fileNo, modelNo, (this.animationManager.animationDirection < 0 ? -firstNo : firstNo), (this.animationManager.currentDirection < 0 ? -lastNo : lastNo), currentFrame, entryName);
if (this.doHaveJDX ()) this.getJSV ().setModel (modelIndex);
}, "~B");
$_M(c$, "doHaveJDX", 
($fz = function () {
return (this.haveJDX || (this.haveJDX = this.getBooleanProperty ("_jspecview")));
}, $fz.isPrivate = true, $fz));
$_M(c$, "getJSV", 
($fz = function () {
if (this.jsv == null) {
this.jsv = J.api.Interface.getOptionInterface ("viewer.JSpecView");
this.jsv.setViewer (this);
}return this.jsv;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getJDXBaseModelIndex", 
function (modelIndex) {
if (!this.doHaveJDX ()) return modelIndex;
return this.getJSV ().getBaseModelIndex (modelIndex);
}, "~N");
$_M(c$, "getJspecViewProperties", 
function (myParam) {
var o = this.statusManager.getJspecViewProperties ("" + myParam);
if (o != null) this.haveJDX = true;
return o;
}, "~O");
$_M(c$, "scriptEcho", 
function (strEcho) {
if (!J.util.Logger.isActiveLevel (4)) return;
{
System.out.println(strEcho);
}this.statusManager.setScriptEcho (strEcho, this.isScriptQueued ());
if (this.listCommands && strEcho != null && strEcho.indexOf ("$[") == 0) J.util.Logger.info (strEcho);
}, "~S");
$_M(c$, "isScriptQueued", 
($fz = function () {
return this.scriptManager != null && this.scriptManager.isScriptQueued ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "notifyError", 
function (errType, errMsg, errMsgUntranslated) {
this.global.setS ("_errormessage", errMsgUntranslated);
this.statusManager.notifyError (errType, errMsg, errMsgUntranslated);
}, "~S,~S,~S");
$_M(c$, "jsEval", 
function (strEval) {
return this.statusManager.jsEval (strEval);
}, "~S");
$_M(c$, "setStatusAtomHovered", 
function (atomIndex, info) {
this.global.setI ("_atomhovered", atomIndex);
this.statusManager.setStatusAtomHovered (atomIndex, info);
}, "~N,~S");
$_M(c$, "setStatusObjectHovered", 
function (id, info, pt) {
this.global.setS ("_objecthovered", id);
this.statusManager.setStatusObjectHovered (id, info, pt);
}, "~S,~S,J.util.P3");
$_M(c$, "setFileLoadStatus", 
($fz = function (ptLoad, fullPathName, fileName, modelName, strError, isAsync) {
this.setErrorMessage (strError, null);
this.global.setI ("_loadPoint", ptLoad.getCode ());
var doCallback = (ptLoad !== J.constant.EnumFileStatus.CREATING_MODELSET);
this.statusManager.setFileLoadStatus (fullPathName, fileName, modelName, strError, ptLoad.getCode (), doCallback, isAsync);
if (doCallback && this.doHaveJDX ()) this.getJSV ().setModel (this.getCurrentModelIndex ());
}, $fz.isPrivate = true, $fz), "J.constant.EnumFileStatus,~S,~S,~S,~S,Boolean");
$_M(c$, "getZapName", 
function () {
return (this.global.modelKitMode ? "Jmol Model Kit" : "zapped");
});
$_M(c$, "setStatusMeasuring", 
function (status, intInfo, strMeasure, value) {
this.statusManager.setStatusMeasuring (status, intInfo, strMeasure, value);
}, "~S,~N,~S,~N");
$_M(c$, "notifyMinimizationStatus", 
function () {
var step = this.getParameter ("_minimizationStep");
var ff = this.getParameter ("_minimizationForceField");
this.statusManager.notifyMinimizationStatus (this.getParameter ("_minimizationStatus"), Clazz.instanceOf (step, String) ? Integer.$valueOf (0) : step, this.getParameter ("_minimizationEnergy"), (step.toString ().equals ("0") ? Float.$valueOf (0) : this.getParameter ("_minimizationEnergyDiff")), ff);
});
$_M(c$, "setStatusAtomPicked", 
function (atomIndex, info) {
if (info == null) {
info = this.global.pickLabel;
if (info.length == 0) info = this.getAtomInfoXYZ (atomIndex, this.global.messageStyleChime);
 else info = this.modelSet.getAtomInfo (atomIndex, info);
}this.global.setPicked (atomIndex);
this.global.setS ("_pickinfo", info);
this.statusManager.setStatusAtomPicked (atomIndex, info);
var syncMode = this.statusManager.getSyncMode ();
if (syncMode != 1 || !this.doHaveJDX ()) return;
this.getJSV ().atomPicked (atomIndex);
}, "~N,~S");
$_M(c$, "setStatusResized", 
function (width, height) {
this.statusManager.setStatusResized (width, height);
}, "~N,~N");
$_M(c$, "scriptStatus", 
function (strStatus) {
this.setScriptStatus (strStatus, "", 0, null);
}, "~S");
$_M(c$, "scriptStatusMsg", 
function (strStatus, statusMessage) {
this.setScriptStatus (strStatus, statusMessage, 0, null);
}, "~S,~S");
$_M(c$, "setScriptStatus", 
function (strStatus, statusMessage, msWalltime, strErrorMessageUntranslated) {
this.statusManager.setScriptStatus (strStatus, statusMessage, msWalltime, strErrorMessageUntranslated);
}, "~S,~S,~N,~S");
$_M(c$, "getModelTitle", 
($fz = function (modelIndex) {
return this.modelSet == null ? null : this.modelSet.getModelTitle (modelIndex);
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "getModelFileName", 
function (modelIndex) {
return this.modelSet == null ? null : this.modelSet.getModelFileName (modelIndex);
}, "~N");
$_M(c$, "dialogAsk", 
function (type, fileName) {
{
return prompt(type, fileName);
}}, "~S,~S");
Clazz.overrideMethod (c$, "showUrl", 
function (urlString) {
if (urlString == null) return;
if (urlString.indexOf (":") < 0) {
var base = this.fileManager.getAppletDocumentBase ();
if (base === "") base = this.fileManager.getFullPathName ();
if (base.indexOf ("/") >= 0) {
base = base.substring (0, base.lastIndexOf ("/") + 1);
} else if (base.indexOf ("\\") >= 0) {
base = base.substring (0, base.lastIndexOf ("\\") + 1);
}urlString = base + urlString;
}J.util.Logger.info ("showUrl:" + urlString);
this.statusManager.showUrl (urlString);
}, "~S");
$_M(c$, "setMeshCreator", 
function (meshCreator) {
this.loadShape (24);
this.setShapeProperty (24, "meshCreator", meshCreator);
}, "~O");
$_M(c$, "showConsole", 
function (showConsole) {
if (!this.haveDisplay) return;
try {
if (this.appConsole == null && showConsole) this.getProperty ("DATA_API", "getAppConsole", Boolean.TRUE);
this.appConsole.setVisible (true);
} catch (e) {
}
}, "~B");
$_M(c$, "clearConsole", 
function () {
this.statusManager.clearConsole ();
});
$_M(c$, "getParameterEscaped", 
function (key) {
return this.global.getParameterEscaped (key, 0);
}, "~S");
Clazz.overrideMethod (c$, "getParameter", 
function (key) {
return this.global.getParameter (key);
}, "~S");
$_M(c$, "getOrSetNewVariable", 
function (key, doSet) {
return this.global.getOrSetNewVariable (key, doSet);
}, "~S,~B");
$_M(c$, "setUserVariable", 
function (name, value) {
return this.global.setUserVariable (name, value);
}, "~S,J.script.SV");
$_M(c$, "unsetProperty", 
function (key) {
key = key.toLowerCase ();
if (key.equals ("all") || key.equals ("variables")) this.fileManager.setPathForAllFiles ("");
this.global.unsetUserVariable (key);
}, "~S");
$_M(c$, "getVariableList", 
function () {
return this.global.getVariableList ();
});
Clazz.overrideMethod (c$, "getBooleanProperty", 
function (key) {
key = key.toLowerCase ();
if (this.global.htBooleanParameterFlags.containsKey (key)) return this.global.htBooleanParameterFlags.get (key).booleanValue ();
if (key.endsWith ("p!")) {
if (this.actionManager == null) return false;
var s = this.actionManager.getPickingState ().toLowerCase ();
key = key.substring (0, key.length - 2) + ";";
return (s.indexOf (key) >= 0);
}if (key.equalsIgnoreCase ("__appletReady")) {
this.statusManager.setStatusAppletReady (this.fullName, true);
return true;
}if (key.equalsIgnoreCase ("__appletDestroyed")) {
this.statusManager.setStatusAppletReady (this.htmlName, false);
return true;
}if (key.equalsIgnoreCase ("executionPaused")) return (this.eval != null && this.eval.isPaused ());
if (key.equalsIgnoreCase ("executionStepping")) return (this.eval != null && this.eval.isStepping ());
if (key.equalsIgnoreCase ("haveBFactors")) return (this.modelSet.getBFactors () != null);
if (key.equalsIgnoreCase ("colorRasmol")) return this.colorManager.getDefaultColorRasmol ();
if (key.equalsIgnoreCase ("frank")) return this.getShowFrank ();
if (key.equalsIgnoreCase ("spinOn")) return this.getSpinOn ();
if (key.equalsIgnoreCase ("isNavigating")) return this.isNavigating ();
if (key.equalsIgnoreCase ("showSelections")) return this.modelSet.getSelectionHaloEnabled ();
if (this.global.htUserVariables.containsKey (key)) {
var t = this.global.getUserVariable (key);
if (t.tok == 1048589) return true;
if (t.tok == 1048588) return false;
}J.util.Logger.error ("viewer.getBooleanProperty(" + key + ") - unrecognized");
return false;
}, "~S");
Clazz.overrideMethod (c$, "getInt", 
function (tok) {
switch (tok) {
case 553648132:
return this.animationManager.animationFps;
case 553648143:
return this.global.dotDensity;
case 553648144:
return this.global.dotScale;
case 553648146:
return this.global.helixStep;
case 553648151:
return this.global.meshScale;
case 553648153:
return this.global.minPixelSelRadius;
case 553648154:
return this.global.percentVdwAtom;
case 553648157:
return this.global.pickingSpinRate;
case 553648158:
return this.global.platformSpeed;
case 553648166:
return this.global.ribbonAspectRatio;
case 536870922:
return this.global.scriptDelay;
case 553648170:
return this.global.smallMoleculeMaxAtoms;
case 553648184:
return this.global.strutSpacing;
}
J.util.Logger.error ("viewer.getInt(" + J.script.T.nameOf (tok) + ") - not listed");
return 0;
}, "~N");
$_M(c$, "getDelayMaximumMs", 
function () {
return (this.haveDisplay ? this.global.delayMaximumMs : 1);
});
$_M(c$, "getHermiteLevel", 
function () {
return (this.getSpinOn () ? 0 : this.global.hermiteLevel);
});
$_M(c$, "getHoverDelay", 
function () {
return (this.global.modelKitMode ? 20 : this.global.hoverDelayMs);
});
Clazz.overrideMethod (c$, "getBoolean", 
function (tok) {
switch (tok) {
case 1074790662:
return this.modelSet.getModelSetAuxiliaryInfoBoolean ("isPDB");
case 603979780:
return this.global.allowGestures;
case 603979784:
return this.global.allowMultiTouch;
case 603979785:
return this.global.allowRotateSelected;
case 603979792:
return this.global.appendNew;
case 603979794:
return this.global.applySymmetryToBonds;
case 603979796:
return this.global.atomPicking;
case 603979798:
return this.global.autoBond;
case 603979800:
return this.global.autoFps;
case 603979806:
return this.global.axesOrientationRasmol;
case 603979812:
return this.global.bondModeOr;
case 603979817:
return this.global.cartoonBaseEdges;
case 603979819:
return this.global.cartoonFancy;
case 603979820:
return this.global.cartoonLadders;
case 603979818:
return this.global.cartoonRockets;
case 603979822:
return this.global.chainCaseSensitive || this.chainList.size () > 0;
case 603979824:
return this.global.debugScript;
case 603979825:
return this.global.defaultStructureDSSP;
case 603979826:
return this.global.disablePopupMenu;
case 603979828:
return this.global.displayCellParameters;
case 603979830:
return this.global.dotSurface;
case 603979829:
return this.global.dotsSelectedOnly;
case 603979833:
return this.global.drawPicking;
case 603979835:
return this.global.dynamicMeasurements;
case 603979844:
return this.global.fontCaching;
case 603979845:
return this.global.fontScaling;
case 603979846:
return this.global.forceAutoBond;
case 603979848:
return this.global.fractionalRelative;
case 603979850:
return this.global.greyscaleRendering;
case 603979852:
return this.global.hbondsBackbone;
case 603979853:
return this.global.hbondsRasmol;
case 603979854:
return this.global.hbondsSolid;
case 1613758470:
return this.global.rasmolHeteroSetting;
case 603979858:
return this.global.hideNameInPopup;
case 603979864:
return this.global.highResolutionFlag;
case 1613758476:
return this.global.rasmolHydrogenSetting;
case 603979870:
return this.global.isosurfaceKey;
case 603979872:
return this.global.justifyMeasurements;
case 603979874:
return this.global.legacyAutoBonding;
case 603979875:
return this.global.legacyHAddition;
case 603979877:
return this.global.logGestures;
case 603979878:
return this.global.measureAllModels;
case 603979879:
return this.global.measurementLabels;
case 603979880:
return this.global.messageStyleChime;
case 603979883:
return this.global.modelKitMode;
case 603979887:
return this.global.navigationMode;
case 603979888:
return this.global.navigationPeriodic;
case 603979889:
return this.global.partialDots;
case 603979892:
return this.global.pdbSequential;
case 603979894:
return this.global.preserveState;
case 603979898:
return this.global.ribbonBorder;
case 603979900:
return this.global.rocketBarrels;
case 603979906:
return this.global.selectAllModels;
case 603979920:
return this.global.showHiddenSelectionHalos;
case 603979922:
return this.global.showHydrogens;
case 603979926:
return this.global.showMeasurements;
case 603979928:
return this.global.showMultipleBonds;
case 603979934:
return this.global.showTiming;
case 603979938:
return this.global.slabByAtom;
case 603979940:
return this.global.slabByMolecule;
case 603979944:
return this.global.smartAromatic;
case 1613758488:
return this.global.solventOn;
case 603979952:
return this.global.ssbondsBackbone;
case 603979955:
return this.global.strutsMultiple;
case 603979966:
return this.global.traceAlpha;
case 603979967:
return this.global.translucent;
case 603979968:
return this.global.twistedSheets;
case 603979973:
return this.global.vectorSymmetry;
case 603979974:
return this.global.waitForMoveTo;
case 603979978:
return this.global.zeroBasedXyzRasmol;
}
J.util.Logger.error ("viewer.getBoolean(" + J.script.T.nameOf (tok) + ") - not listed");
return false;
}, "~N");
$_M(c$, "allowEmbeddedScripts", 
function () {
return (this.global.allowEmbeddedScripts && !this.$isPreviewOnly);
});
$_M(c$, "getDragSelected", 
function () {
return (this.global.dragSelected && !this.global.modelKitMode);
});
$_M(c$, "getBondPicking", 
function () {
return (this.global.bondPicking || this.global.modelKitMode);
});
$_M(c$, "useMinimizationThread", 
function () {
return (this.global.useMinimizationThread && !this.autoExit);
});
Clazz.overrideMethod (c$, "getFloat", 
function (tok) {
switch (tok) {
case 570425346:
return this.global.axesScale;
case 570425348:
return this.global.bondTolerance;
case 570425354:
return this.global.defaultTranslucent;
case 570425352:
return this.global.defaultDrawArrowScale;
case 570425355:
return this.global.dipoleScale;
case 570425356:
return this.global.drawFontSize;
case 570425358:
return this.global.exportScale;
case 570425360:
return this.global.hbondsAngleMinimum;
case 570425361:
return this.global.hbondsDistanceMaximum;
case 570425363:
return this.global.loadAtomDataTolerance;
case 570425364:
return this.global.minBondDistance;
case 570425369:
return this.global.multipleBondSpacing;
case 570425368:
return this.global.multipleBondRadiusFactor;
case 570425374:
return this.global.navigationSpeed;
case 570425382:
return this.global.pointGroupDistanceTolerance;
case 570425384:
return this.global.pointGroupLinearTolerance;
case 570425388:
return this.transformManager.getRotationRadius ();
case 570425392:
return this.global.sheetSmoothing;
case 570425394:
return this.global.solventProbeRadius;
case 570425403:
return this.global.starScale;
case 570425406:
return this.global.strutDefaultRadius;
case 570425408:
return this.global.strutLengthMaximum;
case 1649410049:
return this.global.vectorScale;
case 570425412:
return this.global.vibrationPeriod;
}
J.util.Logger.error ("viewer.getFloat(" + J.script.T.nameOf (tok) + ") - not listed");
return 0;
}, "~N");
Clazz.overrideMethod (c$, "setStringProperty", 
function (key, value) {
if (value == null) return;
if (key.charAt (0) == '_') {
this.global.setS (key, value);
return;
}var tok = J.script.T.getTokFromName (key);
switch (J.script.T.getParamType (tok)) {
case 603979776:
this.setBooleanPropertyTok (key, tok, J.script.SV.newVariable (4, value).asBoolean ());
break;
case 553648128:
this.setIntPropertyTok (key, tok, J.script.SV.newVariable (4, value).asInt ());
break;
case 570425344:
this.setFloatPropertyTok (key, tok, J.util.Parser.parseFloatStr (value));
break;
default:
this.setStringPropertyTok (key, tok, value);
}
}, "~S,~S");
$_M(c$, "setStringPropertyTok", 
($fz = function (key, tok, value) {
switch (tok) {
case 545259569:
this.global.nmrPredictFormat = value;
break;
case 545259548:
this.global.defaultDropScript = value;
break;
case 545259571:
value = this.fileManager.setPathForAllFiles (value);
break;
case 545259558:
this.setUnits (value, false);
return;
case 545259560:
this.global.forceField = value = ("UFF".equalsIgnoreCase (value) ? "UFF" : "MMFF");
this.minimizer = null;
break;
case 545259570:
this.global.nmrUrlFormat = value;
break;
case 545259568:
this.setUnits (value, true);
return;
case 545259566:
this.global.loadLigandFormat = value;
break;
case 545259543:
this.global.defaultLabelPDB = value;
break;
case 545259544:
this.global.defaultLabelXYZ = value;
break;
case 545259549:
this.global.defaultLoadFilter = value;
break;
case 545259567:
value = this.getStateCreator ().setLogFile (value);
if (value == null) return;
break;
case 545259559:
break;
case 545259524:
this.global.atomTypes = value;
break;
case 545259538:
break;
case 545259576:
this.global.pickLabel = value;
break;
case 545259580:
if (value.length == 2 && value.startsWith ("R")) this.global.quaternionFrame = value.substring (0, 2);
 else this.global.quaternionFrame = "" + (value.toLowerCase () + "p").charAt (0);
if (!J.util.Parser.isOneOf (this.global.quaternionFrame, "RC;RP;a;b;c;n;p;q;x;")) this.global.quaternionFrame = "p";
this.modelSet.setHaveStraightness (false);
break;
case 545259555:
this.setDefaultVdw (value);
return;
case 545259564:
 new J.i18n.GT (value);
this.language = J.i18n.GT.getLanguage ();
this.modelkitPopup = null;
if (this.jmolpopup != null) {
this.jmolpopup.jpiDispose ();
this.jmolpopup = null;
this.getPopupMenu ();
}this.statusManager.setCallbackFunction ("language", this.language);
value = J.i18n.GT.getLanguage ();
break;
case 545259565:
this.global.loadFormat = value;
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
this.setShapeProperty (34, "atomLabel", value);
break;
case 545259547:
this.global.defaultDistanceLabel = value;
break;
case 545259542:
this.global.defaultAngleLabel = value;
break;
case 545259554:
this.global.defaultTorsionLabel = value;
break;
case 545259550:
this.global.defaultLoadScript = value;
break;
case 545259522:
this.fileManager.setAppletProxy (value);
break;
case 545259546:
if (value == null) value = "";
value = value.$replace ('\\', '/');
this.global.defaultDirectory = value;
break;
case 545259561:
this.global.helpPath = value;
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
this.statusManager.setCallbackFunction (key, (value.length == 0 || value.equalsIgnoreCase ("none") ? null : value));
break;
}if (!this.global.htNonbooleanParameterValues.containsKey (key.toLowerCase ())) {
this.global.setUserVariable (key, J.script.SV.newVariable (4, value));
return;
}break;
}
this.global.setS (key, value);
}, $fz.isPrivate = true, $fz), "~S,~N,~S");
Clazz.overrideMethod (c$, "setFloatProperty", 
function (key, value) {
if (Float.isNaN (value)) return;
if (key.charAt (0) == '_') {
this.global.setF (key, value);
return;
}var tok = J.script.T.getTokFromName (key);
switch (J.script.T.getParamType (tok)) {
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
$_M(c$, "setFloatPropertyTok", 
($fz = function (key, tok, value) {
switch (tok) {
case 570425356:
this.global.drawFontSize = value;
break;
case 570425358:
this.global.exportScale = value;
break;
case 570425403:
this.global.starScale = value;
break;
case 570425368:
this.global.multipleBondRadiusFactor = value;
break;
case 570425369:
this.global.multipleBondSpacing = value;
break;
case 570425393:
this.transformManager.setSlabRange (value);
break;
case 570425365:
this.global.minimizationCriterion = value;
break;
case 570425359:
if (this.haveDisplay) this.actionManager.setGestureSwipeFactor (value);
break;
case 570425366:
if (this.haveDisplay) this.actionManager.setMouseDragFactor (value);
break;
case 570425367:
if (this.haveDisplay) this.actionManager.setMouseWheelFactor (value);
break;
case 570425408:
this.global.strutLengthMaximum = value;
break;
case 570425406:
this.global.strutDefaultRadius = value;
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
case 570425370:
if (Float.isNaN (value)) return;
this.setSpin ("FPS", Clazz.floatToInt (value));
break;
case 570425363:
this.global.loadAtomDataTolerance = value;
break;
case 570425360:
this.global.hbondsAngleMinimum = value;
break;
case 570425361:
this.global.hbondsDistanceMaximum = value;
break;
case 570425382:
this.global.pointGroupDistanceTolerance = value;
break;
case 570425384:
this.global.pointGroupLinearTolerance = value;
break;
case 570425357:
this.global.ellipsoidAxisDiameter = value;
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
this.global.defaultDrawArrowScale = value;
break;
case 570425354:
this.global.defaultTranslucent = value;
break;
case 570425346:
this.setAxesScale (value);
break;
case 570425416:
this.transformManager.setVisualRange (value);
this.refresh (1, "set visualRange");
break;
case 570425371:
this.setNavigationDepthPercent (value);
break;
case 570425374:
this.global.navigationSpeed = value;
break;
case 570425372:
this.transformManager.setNavigationSlabOffsetPercent (value);
break;
case 570425350:
this.transformManager.setCameraDepthPercent (value, false);
this.refresh (1, "set cameraDepth");
return;
case 570425388:
this.setRotationRadius (value, true);
return;
case 570425362:
this.global.hoverDelayMs = Clazz.floatToInt (value * 1000);
break;
case 570425392:
this.global.sheetSmoothing = value;
break;
case 570425355:
value = J.viewer.Viewer.checkFloatRange (value, -10, 10);
this.global.dipoleScale = value;
break;
case 570425404:
this.transformManager.setStereoDegrees (value);
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
this.transformManager.setScaleAngstromsPerInch (value);
break;
case 570425394:
value = J.viewer.Viewer.checkFloatRange (value, 0, 10);
this.global.solventProbeRadius = value;
break;
default:
if (!this.global.htNonbooleanParameterValues.containsKey (key.toLowerCase ())) {
this.global.setUserVariable (key, J.script.SV.newVariable (3, Float.$valueOf (value)));
return;
}}
this.global.setF (key, value);
}, $fz.isPrivate = true, $fz), "~S,~N,~N");
Clazz.overrideMethod (c$, "setIntProperty", 
function (key, value) {
if (value == -2147483648) return;
if (key.charAt (0) == '_') {
this.global.setI (key, value);
return;
}var tok = J.script.T.getTokFromName (key);
switch (J.script.T.getParamType (tok)) {
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
$_M(c$, "setIntPropertyTok", 
($fz = function (key, tok, value) {
switch (tok) {
case 553648158:
this.global.platformSpeed = Math.min (Math.max (value, 0), 10);
break;
case 553648151:
this.global.meshScale = value;
break;
case 553648153:
this.global.minPixelSelRadius = value;
break;
case 553648149:
this.global.isosurfacePropertySmoothingPower = value;
break;
case 553648165:
this.global.repaintWaitMs = value;
break;
case 553648170:
this.global.smallMoleculeMaxAtoms = value;
break;
case 553648152:
this.global.minimizationSteps = value;
break;
case 553648184:
this.global.strutSpacing = value;
break;
case 553648156:
value = J.viewer.Viewer.checkIntRange (value, 0, 1000);
this.gdata.setPhongExponent (value);
break;
case 553648146:
this.global.helixStep = value;
this.modelSet.setHaveStraightness (false);
break;
case 553648144:
this.global.dotScale = value;
break;
case 553648143:
this.global.dotDensity = value;
break;
case 553648138:
this.global.delayMaximumMs = value;
break;
case 553648150:
J.util.Logger.setLogLevel (value);
J.util.Logger.info ("logging level set to " + value);
this.global.setI ("logLevel", value);
if (this.eval != null) this.eval.setDebugging ();
return;
case 553648134:
switch (J.constant.EnumAxesMode.getAxesMode (value)) {
case J.constant.EnumAxesMode.MOLECULAR:
this.setAxesModeMolecular (true);
return;
case J.constant.EnumAxesMode.BOUNDBOX:
this.setAxesModeMolecular (false);
return;
case J.constant.EnumAxesMode.UNITCELL:
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
this.global.scriptDelay = value;
break;
case 553648176:
if (value < 0) value = J.viewer.Viewer.checkIntRange (value, -10, -1);
 else value = J.viewer.Viewer.checkIntRange (value, 0, 100);
this.gdata.setSpecularPower (value);
break;
case 553648172:
value = J.viewer.Viewer.checkIntRange (-value, -10, -1);
this.gdata.setSpecularPower (value);
break;
case 553648136:
this.setMarBond (value);
return;
case 536870924:
this.setBooleanPropertyTok (key, tok, value == 1);
return;
case 553648174:
value = J.viewer.Viewer.checkIntRange (value, 0, 100);
this.gdata.setSpecularPercent (value);
break;
case 553648142:
value = J.viewer.Viewer.checkIntRange (value, 0, 100);
this.gdata.setDiffusePercent (value);
break;
case 553648130:
value = J.viewer.Viewer.checkIntRange (value, 0, 100);
this.gdata.setAmbientPercent (value);
break;
case 553648186:
this.transformManager.zDepthToPercent (value);
break;
case 553648188:
this.transformManager.zSlabToPercent (value);
break;
case 554176526:
this.transformManager.depthToPercent (value);
break;
case 554176565:
this.transformManager.slabToPercent (value);
break;
case 553648190:
this.global.zShadePower = Math.max (value, 1);
break;
case 553648166:
this.global.ribbonAspectRatio = value;
break;
case 553648157:
this.global.pickingSpinRate = (value < 1 ? 1 : value);
break;
case 553648132:
this.setAnimationFps (value);
break;
case 553648154:
this.setPercentVdwAtom (value);
break;
case 553648147:
this.global.hermiteLevel = value;
break;
case 553648145:
case 553648148:
case 553648160:
case 553648159:
case 553648162:
case 553648164:
break;
default:
if (!this.global.htNonbooleanParameterValues.containsKey (key)) {
this.global.setUserVariable (key, J.script.SV.newScriptVariableInt (value));
return;
}}
this.global.setI (key, value);
}, $fz.isPrivate = true, $fz), "~S,~N,~N");
c$.checkIntRange = $_M(c$, "checkIntRange", 
($fz = function (value, min, max) {
return (value < min ? min : value > max ? max : value);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
c$.checkFloatRange = $_M(c$, "checkFloatRange", 
($fz = function (value, min, max) {
return (value < min ? min : value > max ? max : value);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
Clazz.overrideMethod (c$, "setBooleanProperty", 
function (key, value) {
if (key.charAt (0) == '_') {
this.global.setB (key, value);
return;
}var tok = J.script.T.getTokFromName (key);
switch (J.script.T.getParamType (tok)) {
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
$_M(c$, "setBooleanPropertyTok", 
($fz = function (key, tok, value) {
var doRepaint = true;
switch (tok) {
case 603979837:
this.global.ellipsoidArrows = value;
break;
case 603979967:
this.global.translucent = value;
break;
case 603979820:
this.global.cartoonLadders = value;
break;
case 603979968:
var b = this.global.twistedSheets;
this.global.twistedSheets = value;
if (b != value) this.checkCoordinatesChanged ();
break;
case 603979821:
this.global.celShading = value;
this.gdata.setCel (value);
break;
case 603979819:
this.global.cartoonFancy = value;
break;
case 603979934:
this.global.showTiming = value;
break;
case 603979973:
this.global.vectorSymmetry = value;
break;
case 603979870:
this.global.isosurfaceKey = value;
break;
case 603979889:
this.global.partialDots = value;
break;
case 603979874:
this.global.legacyAutoBonding = value;
break;
case 603979825:
this.global.defaultStructureDSSP = value;
break;
case 603979834:
this.global.dsspCalcHydrogen = value;
break;
case 603979782:
this.global.allowModelkit = value;
if (!value) this.setModelKitMode (false);
break;
case 603979883:
this.setModelKitMode (value);
break;
case 603979885:
this.global.multiProcessor = value && (J.viewer.Viewer.nProcessors > 1);
break;
case 603979884:
this.global.monitorEnergy = value;
break;
case 603979853:
this.global.hbondsRasmol = value;
break;
case 603979881:
this.global.minimizationRefresh = value;
break;
case 603979882:
this.global.minimizationSilent = value;
break;
case 603979969:
this.global.useArcBall = value;
break;
case 603979869:
if (value) {
this.$isKiosk = true;
this.global.disablePopupMenu = true;
if (this.display != null) this.apiPlatform.setTransparentCursor (this.display);
}break;
case 603979974:
this.global.waitForMoveTo = value;
break;
case 603979876:
this.global.logCommands = true;
break;
case 603979877:
this.global.logGestures = true;
break;
case 603979784:
this.global.allowMultiTouch = value;
break;
case 603979894:
this.global.preserveState = value;
this.modelSet.setPreserveState (value);
this.undoClear ();
break;
case 603979955:
this.global.strutsMultiple = value;
break;
case 603979842:
break;
case 603979938:
this.global.slabByAtom = value;
break;
case 603979940:
this.global.slabByMolecule = value;
break;
case 603979902:
this.global.saveProteinStructureState = value;
break;
case 603979780:
this.global.allowGestures = value;
break;
case 603979868:
this.global.imageState = value;
break;
case 603979970:
this.global.useMinimizationThread = value;
break;
case 603979781:
if (this.global.disablePopupMenu) value = false;
this.global.allowKeyStrokes = value;
break;
case 603979831:
this.global.dragSelected = value;
this.showSelected = false;
break;
case 603979924:
this.global.showKeyStrokes = value;
break;
case 603979844:
this.global.fontCaching = value;
break;
case 603979796:
this.global.atomPicking = value;
break;
case 603979814:
this.highlight (null);
this.global.bondPicking = value;
break;
case 603979906:
this.global.selectAllModels = value;
break;
case 603979880:
this.global.messageStyleChime = value;
break;
case 603979892:
this.global.pdbSequential = value;
break;
case 603979890:
this.global.pdbAddHydrogens = value;
break;
case 603979891:
this.global.pdbGetHeader = value;
break;
case 603979838:
this.global.ellipsoidAxes = value;
break;
case 603979836:
this.global.ellipsoidArcs = value;
break;
case 603979839:
this.global.ellipsoidBall = value;
break;
case 603979840:
this.global.ellipsoidDots = value;
break;
case 603979841:
this.global.ellipsoidFill = value;
break;
case 603979845:
this.global.fontScaling = value;
break;
case 603979956:
this.setSyncTarget (0, value);
break;
case 603979958:
this.setSyncTarget (1, value);
break;
case 603979976:
this.global.wireframeRotation = value;
break;
case 603979871:
this.global.isosurfacePropertySmoothing = value;
break;
case 603979833:
this.global.drawPicking = value;
break;
case 603979786:
case 603979790:
case 603979788:
this.setAntialias (tok, value);
break;
case 603979944:
this.global.smartAromatic = value;
break;
case 603979794:
this.setApplySymmetryToBonds (value);
break;
case 603979792:
this.setAppendNew (value);
break;
case 603979800:
this.global.autoFps = value;
break;
case 603979972:
J.util.TextFormat.setUseNumberLocalization (this.global.useNumberLocalization = value);
break;
case 1611272202:
key = "showFrank";
this.setFrankOn (value);
break;
case 603979918:
this.setFrankOn (value);
break;
case 1613758488:
key = "solventProbe";
this.global.solventOn = value;
break;
case 603979948:
this.global.solventOn = value;
break;
case 603979835:
this.setDynamicMeasurements (value);
break;
case 603979785:
this.global.allowRotateSelected = value;
break;
case 603979783:
this.global.allowMoveAtoms = value;
this.global.allowRotateSelected = value;
this.global.dragSelected = value;
this.showSelected = false;
break;
case 536870922:
this.setIntPropertyTok ("showScript", tok, value ? 1 : 0);
return;
case 603979778:
this.global.allowEmbeddedScripts = value;
break;
case 603979888:
this.global.navigationPeriodic = value;
break;
case 603979984:
this.transformManager.setZShadeEnabled (value);
return;
case 603979832:
if (this.haveDisplay) this.global.drawHover = value;
break;
case 603979887:
this.setNavigationMode (value);
break;
case 603979886:
return;
case 603979860:
this.global.hideNavigationPoint = value;
break;
case 603979930:
this.global.showNavigationPointAlways = value;
break;
case 603979896:
this.setRefreshing (value);
break;
case 603979872:
this.global.justifyMeasurements = value;
break;
case 603979952:
this.global.ssbondsBackbone = value;
break;
case 603979852:
this.global.hbondsBackbone = value;
break;
case 603979854:
this.global.hbondsSolid = value;
break;
case 536870924:
this.gdata.setSpecular (value);
break;
case 603979942:
this.transformManager.setSlabEnabled (value);
return;
case 603979980:
this.transformManager.setZoomEnabled (value);
return;
case 603979864:
this.global.highResolutionFlag = value;
break;
case 603979966:
this.global.traceAlpha = value;
break;
case 603979983:
this.global.zoomLarge = value;
this.transformManager.setZoomHeight (this.global.zoomHeight, value);
break;
case 603979982:
this.global.zoomHeight = value;
this.transformManager.setZoomHeight (value, this.global.zoomLarge);
break;
case 603979873:
J.i18n.GT.setDoTranslate (value);
break;
case 603979862:
this.selectionManager.setHideNotSelected (value);
break;
case 603979904:
this.setScriptQueue (value);
break;
case 603979830:
this.global.dotSurface = value;
break;
case 603979829:
this.global.dotsSelectedOnly = value;
break;
case 1611141171:
this.setSelectionHalos (value);
break;
case 603979910:
this.global.rasmolHydrogenSetting = value;
break;
case 603979908:
this.global.rasmolHeteroSetting = value;
break;
case 603979928:
this.global.showMultipleBonds = value;
break;
case 603979920:
this.global.showHiddenSelectionHalos = value;
break;
case 603979975:
this.transformManager.setWindowCentered (value);
break;
case 603979828:
this.global.displayCellParameters = value;
break;
case 603979960:
this.global.testFlag1 = value;
break;
case 603979962:
this.global.testFlag2 = value;
break;
case 603979964:
this.global.testFlag3 = value;
break;
case 603979965:
this.jmolTest ();
this.global.testFlag4 = value;
break;
case 603979898:
this.global.ribbonBorder = value;
break;
case 603979817:
this.global.cartoonBaseEdges = value;
break;
case 603979818:
this.global.cartoonRockets = value;
break;
case 603979900:
this.global.rocketBarrels = value;
break;
case 603979850:
this.gdata.setGreyscaleMode (this.global.greyscaleRendering = value);
break;
case 603979879:
this.global.measurementLabels = value;
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
case 603979823:
this.setStringPropertyTok ("defaultcolorscheme", 545259545, value ? "rasmol" : "jmol");
return;
case 603979824:
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
this.global.bondModeOr = value;
break;
case 603979978:
doRepaint = false;
this.global.zeroBasedXyzRasmol = value;
this.reset (true);
break;
case 603979895:
doRepaint = false;
this.global.rangeSelected = value;
break;
case 603979878:
doRepaint = false;
this.global.measureAllModels = value;
break;
case 603979954:
doRepaint = false;
this.statusManager.setAllowStatusReporting (value);
break;
case 603979822:
doRepaint = false;
this.global.chainCaseSensitive = value;
break;
case 603979858:
doRepaint = false;
this.global.hideNameInPopup = value;
break;
case 603979826:
doRepaint = false;
this.global.disablePopupMenu = value;
break;
case 603979846:
doRepaint = false;
this.global.forceAutoBond = value;
break;
case 603979848:
doRepaint = false;
this.global.fractionalRelative = value;
break;
default:
if (!this.global.htBooleanParameterFlags.containsKey (key.toLowerCase ())) {
this.global.setUserVariable (key, J.script.SV.getBoolean (value));
return;
}}
this.global.setB (key, value);
if (doRepaint) this.setTainted (true);
}, $fz.isPrivate = true, $fz), "~S,~N,~B");
$_M(c$, "setModelKitMode", 
($fz = function (value) {
if (this.actionManager == null || !this.allowScripting) return;
if (value || this.global.modelKitMode) {
this.setPickingMode (null, value ? 33 : 1);
this.setPickingMode (null, value ? 32 : 1);
}var isChange = (this.global.modelKitMode != value);
this.global.modelKitMode = value;
this.highlight (null);
if (value) {
this.setNavigationMode (false);
this.selectAll ();
this.setAtomPickingOption ("C");
this.setBondPickingOption ("p");
if (!this.$isApplet) this.popupMenu (0, 0, 'm');
if (isChange) this.statusManager.setCallbackFunction ("modelkit", "ON");
this.global.modelKitMode = true;
if (this.getAtomCount () == 0) this.zap (false, true, true);
} else {
this.actionManager.setPickingMode (-1);
this.setStringProperty ("pickingStyle", "toggle");
this.setBooleanProperty ("bondPicking", false);
if (isChange) this.statusManager.setCallbackFunction ("modelkit", "OFF");
}}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getLanguage", 
function () {
return this.language;
});
$_M(c$, "setSmilesString", 
function (s) {
if (s == null) this.global.removeParam ("_smilesString");
 else this.global.setS ("_smilesString", s);
}, "~S");
$_M(c$, "removeUserVariable", 
function (key) {
this.global.removeUserVariable (key);
if (key.endsWith ("callback")) this.statusManager.setCallbackFunction (key, null);
}, "~S");
$_M(c$, "isJmolVariable", 
function (key) {
return this.global.isJmolVariable (key);
}, "~S");
$_M(c$, "jmolTest", 
($fz = function () {
}, $fz.isPrivate = true, $fz));
$_M(c$, "showParameter", 
function (key, ifNotSet, nMax) {
var sv = "" + this.global.getParameterEscaped (key, nMax);
if (ifNotSet || sv.indexOf ("<not defined>") < 0) this.showString (key + " = " + sv, false);
}, "~S,~B,~N");
$_M(c$, "showString", 
function (str, isPrint) {
if (this.isScriptQueued () && (!this.isSilent || isPrint) && !this.isJS) J.util.Logger.warn (str);
this.scriptEcho (str);
}, "~S,~B");
$_M(c$, "getAllSettings", 
function (prefix) {
return this.getStateCreator ().getAllSettings (prefix);
}, "~S");
$_M(c$, "getBindingInfo", 
function (qualifiers) {
return (this.haveDisplay ? this.actionManager.getBindingInfo (qualifiers) : "");
}, "~S");
$_M(c$, "getIsosurfacePropertySmoothing", 
function (asPower) {
return (asPower ? this.global.isosurfacePropertySmoothingPower : this.global.isosurfacePropertySmoothing ? 1 : 0);
}, "~B");
$_M(c$, "setNavigationDepthPercent", 
function (percent) {
this.transformManager.setNavigationDepthPercent (percent);
this.refresh (1, "set navigationDepth");
}, "~N");
$_M(c$, "getShowNavigationPoint", 
function () {
if (!this.global.navigationMode || !this.transformManager.canNavigate ()) return false;
return (this.isNavigating () && !this.global.hideNavigationPoint || this.global.showNavigationPointAlways || this.getInMotion (true));
});
$_M(c$, "getCurrentSolventProbeRadius", 
function () {
return this.global.solventOn ? this.global.solventProbeRadius : 0;
});
$_M(c$, "getTestFlag", 
function (i) {
switch (i) {
case 1:
return this.global.testFlag1;
case 2:
return this.global.testFlag2;
case 3:
return this.global.testFlag3;
case 4:
return this.global.testFlag4;
}
return false;
}, "~N");
Clazz.overrideMethod (c$, "setPerspectiveDepth", 
function (perspectiveDepth) {
this.transformManager.setPerspectiveDepth (perspectiveDepth);
}, "~B");
Clazz.overrideMethod (c$, "setAxesOrientationRasmol", 
function (TF) {
this.global.setB ("axesOrientationRasmol", TF);
this.global.axesOrientationRasmol = TF;
this.reset (true);
}, "~B");
$_M(c$, "setAxesScale", 
function (scale) {
scale = J.viewer.Viewer.checkFloatRange (scale, -100, 100);
this.global.axesScale = scale;
this.axesAreTainted = true;
}, "~N");
$_M(c$, "getAxisPoints", 
function () {
return (this.getObjectMad (1) == 0 || this.getAxesMode () !== J.constant.EnumAxesMode.UNITCELL || (this.getShapeProperty (31, "axesTypeXY")).booleanValue () || this.getShapeProperty (31, "origin") != null ? null : this.getShapeProperty (31, "axisPoints"));
});
$_M(c$, "resetError", 
function () {
this.global.removeParam ("_errormessage");
});
$_M(c$, "setAxesModeMolecular", 
($fz = function (TF) {
this.global.axesMode = (TF ? J.constant.EnumAxesMode.MOLECULAR : J.constant.EnumAxesMode.BOUNDBOX);
this.axesAreTainted = true;
this.global.removeParam ("axesunitcell");
this.global.removeParam (TF ? "axeswindow" : "axesmolecular");
this.global.setI ("axesMode", this.global.axesMode.getCode ());
this.global.setB (TF ? "axesMolecular" : "axesWindow", true);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "setAxesModeUnitCell", 
function (TF) {
this.global.axesMode = (TF ? J.constant.EnumAxesMode.UNITCELL : J.constant.EnumAxesMode.BOUNDBOX);
this.axesAreTainted = true;
this.global.removeParam ("axesmolecular");
this.global.removeParam (TF ? "axeswindow" : "axesunitcell");
this.global.setB (TF ? "axesUnitcell" : "axesWindow", true);
this.global.setI ("axesMode", this.global.axesMode.getCode ());
}, "~B");
$_M(c$, "getAxesMode", 
function () {
return this.global.axesMode;
});
Clazz.overrideMethod (c$, "getPerspectiveDepth", 
function () {
return this.transformManager.getPerspectiveDepth ();
});
Clazz.overrideMethod (c$, "setSelectionHalos", 
function (TF) {
if (this.modelSet == null || TF == this.modelSet.getSelectionHaloEnabled ()) return;
this.global.setB ("selectionHalos", TF);
this.loadShape (8);
this.modelSet.setSelectionHaloEnabled (TF);
}, "~B");
$_M(c$, "getSelectionHaloEnabled", 
function (isRenderer) {
var flag = this.modelSet.getSelectionHaloEnabled () || isRenderer && this.showSelected;
if (isRenderer) this.showSelected = false;
return flag;
}, "~B");
$_M(c$, "setStrandCount", 
($fz = function (type, value) {
value = J.viewer.Viewer.checkIntRange (value, 0, 20);
switch (type) {
case 12:
this.global.strandCountForStrands = value;
break;
case 13:
this.global.strandCountForMeshRibbon = value;
break;
default:
this.global.strandCountForStrands = value;
this.global.strandCountForMeshRibbon = value;
break;
}
this.global.setI ("strandCount", value);
this.global.setI ("strandCountForStrands", this.global.strandCountForStrands);
this.global.setI ("strandCountForMeshRibbon", this.global.strandCountForMeshRibbon);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "getStrandCount", 
function (type) {
return (type == 12 ? this.global.strandCountForStrands : this.global.strandCountForMeshRibbon);
}, "~N");
$_M(c$, "setNavigationMode", 
($fz = function (TF) {
this.global.navigationMode = TF;
this.transformManager.setNavigationMode (TF);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "setTransformManagerDefaults", 
($fz = function () {
this.transformManager.setCameraDepthPercent (this.global.defaultCameraDepth, true);
this.transformManager.setPerspectiveDepth (this.global.defaultPerspectiveDepth);
this.transformManager.setStereoDegrees (-5);
this.transformManager.setVisualRange (this.global.visualRange);
this.transformManager.setSpinOff ();
this.transformManager.setVibrationPeriod (0);
this.transformManager.setFrameOffsets (this.frameOffsets);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getCameraFactors", 
function () {
return this.transformManager.getCameraFactors ();
});
$_M(c$, "getCameraDepth", 
function () {
return this.transformManager.getCameraDepth ();
});
$_M(c$, "getLoadState", 
function (htParams) {
return this.getStateCreator ().getLoadState (htParams);
}, "java.util.Map");
Clazz.overrideMethod (c$, "setAutoBond", 
function (TF) {
this.global.setB ("autobond", TF);
this.global.autoBond = TF;
}, "~B");
$_M(c$, "makeConnections", 
function (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy) {
this.clearModelDependentObjects ();
this.clearMinimization ();
return this.modelSet.makeConnections (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy);
}, "~N,~N,~N,~N,J.util.BS,J.util.BS,J.util.BS,~B,~B,~N");
Clazz.overrideMethod (c$, "rebond", 
function () {
this.rebondState (false);
});
$_M(c$, "rebondState", 
function (isStateScript) {
this.clearModelDependentObjects ();
this.modelSet.deleteAllBonds ();
var isLegacy = isStateScript && this.global.legacyAutoBonding;
this.modelSet.autoBondBs4 (null, null, null, null, this.getMadBond (), isLegacy);
this.addStateScript ((isLegacy ? "set legacyAutoBonding TRUE;connect;set legacyAutoBonding FALSE;" : "connect;"), false, true);
}, "~B");
$_M(c$, "setPdbConectBonding", 
function (isAuto, isStateScript) {
this.clearModelDependentObjects ();
this.modelSet.deleteAllBonds ();
var bsExclude =  new J.util.BS ();
this.modelSet.setPdbConectBonding (0, 0, bsExclude);
if (isAuto) {
var isLegacy = isStateScript && this.global.legacyAutoBonding;
this.modelSet.autoBondBs4 (null, null, bsExclude, null, this.getMadBond (), isLegacy);
this.addStateScript ((isLegacy ? "set legacyAutoBonding TRUE;connect PDB AUTO;set legacyAutoBonding FALSE;" : "connect PDB auto;"), false, true);
return;
}this.addStateScript ("connect PDB;", false, true);
}, "~B,~B");
Clazz.overrideMethod (c$, "setPercentVdwAtom", 
function (value) {
this.global.setI ("percentVdwAtom", value);
this.global.percentVdwAtom = value;
this.rd.value = value / 100;
this.rd.factorType = J.atomdata.RadiusData.EnumType.FACTOR;
this.rd.vdwType = J.constant.EnumVdw.AUTO;
this.setShapeSizeRD (0, this.rd, null);
}, "~N");
$_M(c$, "getDefaultRadiusData", 
function () {
return this.rd;
});
Clazz.overrideMethod (c$, "getMadBond", 
function () {
return (this.global.bondRadiusMilliAngstroms * 2);
});
$_M(c$, "getMarBond", 
function () {
return this.global.bondRadiusMilliAngstroms;
});
$_M(c$, "getModeMultipleBond", 
function () {
return this.global.modeMultipleBond;
});
Clazz.overrideMethod (c$, "setShowHydrogens", 
function (TF) {
this.global.setB ("showHydrogens", TF);
this.global.showHydrogens = TF;
}, "~B");
Clazz.overrideMethod (c$, "setShowBbcage", 
function (value) {
this.setObjectMad (32, "boundbox", (value ? -4 : 0));
this.global.setB ("showBoundBox", value);
}, "~B");
Clazz.overrideMethod (c$, "getShowBbcage", 
function () {
return this.getObjectMad (4) != 0;
});
$_M(c$, "setShowUnitCell", 
function (value) {
this.setObjectMad (33, "unitcell", (value ? -2 : 0));
this.global.setB ("showUnitCell", value);
}, "~B");
$_M(c$, "getShowUnitCell", 
function () {
return this.getObjectMad (5) != 0;
});
Clazz.overrideMethod (c$, "setShowAxes", 
function (value) {
this.setObjectMad (31, "axes", (value ? -2 : 0));
this.global.setB ("showAxes", value);
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
$_M(c$, "getShowFrank", 
function () {
if (this.$isPreviewOnly || this.$isApplet && this.creatingImage) return false;
return (!this.isJS && this.$isSignedApplet && !this.isSignedAppletLocal || this.frankOn);
});
Clazz.overrideMethod (c$, "setShowMeasurements", 
function (TF) {
this.global.setB ("showMeasurements", TF);
this.global.showMeasurements = TF;
}, "~B");
$_M(c$, "setUnits", 
function (units, isDistance) {
this.global.setUnits (units);
if (isDistance) {
this.global.setUnits (units);
this.setShapeProperty (6, "reformatDistances", null);
} else {
}}, "~S,~B");
$_M(c$, "getMeasureDistanceUnits", 
function () {
return this.global.measureDistanceUnits;
});
$_M(c$, "getEnergyUnits", 
function () {
return this.global.energyUnits;
});
$_M(c$, "setAppendNew", 
function (value) {
this.global.appendNew = value;
}, "~B");
Clazz.overrideMethod (c$, "setRasmolDefaults", 
function () {
this.setDefaultsType ("RasMol");
});
Clazz.overrideMethod (c$, "setJmolDefaults", 
function () {
this.setDefaults ();
});
$_M(c$, "setDefaultsType", 
($fz = function (type) {
if (type.equalsIgnoreCase ("RasMol")) {
this.stateManager.setRasMolDefaults ();
return;
}if (type.equalsIgnoreCase ("PyMOL")) {
this.stateManager.setPyMOLDefaults ();
return;
}this.setDefaults ();
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "setDefaults", 
($fz = function () {
this.setShapeSizeRD (0, this.rd, this.getModelUndeletedAtomsBitSet (-1));
}, $fz.isPrivate = true, $fz));
$_M(c$, "setAntialias", 
($fz = function (tok, TF) {
switch (tok) {
case 603979786:
this.global.antialiasDisplay = TF;
break;
case 603979790:
this.global.antialiasTranslucent = TF;
break;
case 603979788:
this.global.antialiasImages = TF;
return;
}
this.resizeImage (0, 0, false, false, true);
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "allocTempPoints", 
function (size) {
return this.tempArray.allocTempPoints (size);
}, "~N");
$_M(c$, "freeTempPoints", 
function (tempPoints) {
this.tempArray.freeTempPoints (tempPoints);
}, "~A");
$_M(c$, "allocTempScreens", 
function (size) {
return this.tempArray.allocTempScreens (size);
}, "~N");
$_M(c$, "freeTempScreens", 
function (tempScreens) {
this.tempArray.freeTempScreens (tempScreens);
}, "~A");
$_M(c$, "allocTempEnum", 
function (size) {
return this.tempArray.allocTempEnum (size);
}, "~N");
$_M(c$, "freeTempEnum", 
function (temp) {
this.tempArray.freeTempEnum (temp);
}, "~A");
$_M(c$, "getFont3D", 
function (fontFace, fontStyle, fontSize) {
return this.gdata.getFont3DFSS (fontFace, fontStyle, fontSize);
}, "~S,~S,~N");
$_M(c$, "formatText", 
function (text0) {
var i;
if ((i = text0.indexOf ("@{")) < 0 && (i = text0.indexOf ("%{")) < 0) return text0;
var text = text0;
var isEscaped = (text.indexOf ("\\") >= 0);
if (isEscaped) {
text = J.util.TextFormat.simpleReplace (text, "\\%", "\1");
text = J.util.TextFormat.simpleReplace (text, "\\@", "\2");
isEscaped = !text.equals (text0);
}text = J.util.TextFormat.simpleReplace (text, "%{", "@{");
var name;
while ((i = text.indexOf ("@{")) >= 0) {
i++;
var i0 = i + 1;
var len = text.length;
i = J.util.TextFormat.ichMathTerminator (text, i, len);
if (i >= len) return text;
name = text.substring (i0, i);
if (name.length == 0) return text;
var v = this.evaluateExpression (name);
if (Clazz.instanceOf (v, J.util.P3)) v = J.util.Escape.eP (v);
text = text.substring (0, i0 - 2) + v.toString () + text.substring (i + 1);
}
if (isEscaped) {
text = J.util.TextFormat.simpleReplace (text, "\2", "@");
text = J.util.TextFormat.simpleReplace (text, "\1", "%");
}return text;
}, "~S");
$_M(c$, "getElementSymbol", 
function (i) {
return this.modelSet.getElementSymbol (i);
}, "~N");
$_M(c$, "getElementNumber", 
function (i) {
return this.modelSet.getElementNumber (i);
}, "~N");
Clazz.overrideMethod (c$, "getAtomName", 
function (i) {
return this.modelSet.getAtomName (i);
}, "~N");
Clazz.overrideMethod (c$, "getAtomNumber", 
function (i) {
return this.modelSet.getAtomNumber (i);
}, "~N");
$_M(c$, "getAtomGroupQuaternions", 
function (bsAtoms, nMax) {
return this.modelSet.getAtomGroupQuaternions (bsAtoms, nMax, this.getQuaternionFrame ());
}, "J.util.BS,~N");
$_M(c$, "getAtomQuaternion", 
function (i) {
return this.modelSet.getQuaternion (i, this.getQuaternionFrame ());
}, "~N");
Clazz.overrideMethod (c$, "getAtomPoint3f", 
function (i) {
return this.modelSet.atoms[i];
}, "~N");
$_M(c$, "getAtomPointVector", 
function (bs) {
return this.modelSet.getAtomPointVector (bs);
}, "J.util.BS");
Clazz.overrideMethod (c$, "getAtomRadius", 
function (i) {
return this.modelSet.getAtomRadius (i);
}, "~N");
Clazz.overrideMethod (c$, "getAtomArgb", 
function (i) {
return this.gdata.getColorArgbOrGray (this.modelSet.getAtomColix (i));
}, "~N");
Clazz.overrideMethod (c$, "getAtomModelIndex", 
function (i) {
return this.modelSet.atoms[i].modelIndex;
}, "~N");
Clazz.overrideMethod (c$, "getBondRadius", 
function (i) {
return this.modelSet.getBondRadius (i);
}, "~N");
Clazz.overrideMethod (c$, "getBondOrder", 
function (i) {
return this.modelSet.getBondOrder (i);
}, "~N");
$_M(c$, "assignAromaticBonds", 
function () {
this.modelSet.assignAromaticBonds ();
});
$_M(c$, "resetAromatic", 
function () {
this.modelSet.resetAromatic ();
});
Clazz.overrideMethod (c$, "getBondArgb1", 
function (i) {
return this.gdata.getColorArgbOrGray (this.modelSet.getBondColix1 (i));
}, "~N");
Clazz.overrideMethod (c$, "getBondModelIndex", 
function (i) {
return this.modelSet.getBondModelIndex (i);
}, "~N");
Clazz.overrideMethod (c$, "getBondArgb2", 
function (i) {
return this.gdata.getColorArgbOrGray (this.modelSet.getBondColix2 (i));
}, "~N");
Clazz.overrideMethod (c$, "getPolymerLeadMidPoints", 
function (modelIndex, polymerIndex) {
return this.modelSet.getPolymerLeadMidPoints (modelIndex, polymerIndex);
}, "~N,~N");
$_M(c$, "setStereoMode", 
function (twoColors, stereoMode, degrees) {
this.setFloatProperty ("stereoDegrees", degrees);
this.setBooleanProperty ("greyscaleRendering", stereoMode.isBiColor ());
if (twoColors != null) this.transformManager.setStereoMode2 (twoColors);
 else this.transformManager.setStereoMode (stereoMode);
}, "~A,J.constant.EnumStereoMode,~N");
$_M(c$, "isStereoDouble", 
function () {
return this.transformManager.stereoMode === J.constant.EnumStereoMode.DOUBLE;
});
Clazz.overrideMethod (c$, "getOperatingSystemName", 
function () {
return J.viewer.Viewer.strOSName + (!this.isJS ? "" : this.isWebGL ? "(WebGL)" : "(HTML5)");
});
Clazz.overrideMethod (c$, "getJavaVendor", 
function () {
return J.viewer.Viewer.strJavaVendor;
});
Clazz.overrideMethod (c$, "getJavaVersion", 
function () {
return J.viewer.Viewer.strJavaVersion;
});
$_M(c$, "getGraphicsData", 
function () {
return this.gdata;
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
this.getStateCreator ().showEditor (paramInfo);
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
{
this.appConsole = J.api.Interface
.getOptionInterface("consolejs.AppletConsole"); if
(this.appConsole != null) { this.appConsole.start(this);
return this.appConsole; }
}if (this.appConsole != null) this.appConsole.start (this);
}this.scriptEditor = (this.appConsole == null ? null : this.appConsole.getScriptEditor ());
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
this.global.disablePopupMenu = true;
return null;
case 180:
return this.global.defaultDirectory;
case 200:
if (Clazz.instanceOf (paramInfo, String)) return this.getMenu (paramInfo);
return this.getPopupMenu ();
case 220:
return this.shapeManager.getProperty (paramInfo);
}
J.util.Logger.error ("ERROR in getProperty DATA_API: " + infoType);
return null;
}, "~S,~S,~O");
$_M(c$, "getPropertyManager", 
($fz = function () {
if (this.pm == null) (this.pm = J.api.Interface.getOptionInterface ("viewer.PropertyManager")).setViewer (this);
return this.pm;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getModelExtract", 
function (atomExpression, doTransform, isModelKit, type) {
return this.getPropertyManager ().getModelExtract (this.getAtomBitSet (atomExpression), doTransform, isModelKit, type);
}, "~O,~B,~B,~S");
$_M(c$, "setTainted", 
function (TF) {
this.isTainted = this.axesAreTainted = (TF && (this.refreshing || this.creatingImage));
}, "~B");
$_M(c$, "notifyMouseClicked", 
function (x, y, action, mode) {
var modifiers = J.viewer.binding.Binding.getButtonMods (action);
var clickCount = J.viewer.binding.Binding.getClickCount (action);
this.global.setI ("_mouseX", x);
this.global.setI ("_mouseY", this.dimScreen.height - y);
this.global.setI ("_mouseAction", action);
this.global.setI ("_mouseModifiers", modifiers);
this.global.setI ("_clickCount", clickCount);
return this.statusManager.setStatusClicked (x, this.dimScreen.height - y, action, clickCount, mode);
}, "~N,~N,~N,~N");
$_M(c$, "checkObjectClicked", 
function (x, y, modifiers) {
return this.shapeManager.checkObjectClicked (x, y, modifiers, this.getVisibleFramesBitSet (), this.global.drawPicking);
}, "~N,~N,~N");
$_M(c$, "checkObjectHovered", 
function (x, y) {
return (x >= 0 && this.shapeManager != null && this.shapeManager.checkObjectHovered (x, y, this.getVisibleFramesBitSet (), this.getBondPicking ()));
}, "~N,~N");
$_M(c$, "checkObjectDragged", 
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
if (this.shapeManager.checkObjectDragged (prevX, prevY, x, y, action, this.getVisibleFramesBitSet (), iShape)) {
this.refresh (1, "checkObjectDragged");
if (iShape == 22) this.scriptEcho (this.getShapeProperty (22, "command"));
}}, "~N,~N,~N,~N,~N");
$_M(c$, "rotateAxisAngleAtCenter", 
function (eval, rotCenter, rotAxis, degreesPerSecond, endDegrees, isSpin, bsSelected) {
var isOK = this.transformManager.rotateAxisAngleAtCenter (eval, rotCenter, rotAxis, degreesPerSecond, endDegrees, isSpin, bsSelected);
if (isOK) this.refresh (-1, "rotateAxisAngleAtCenter");
return isOK;
}, "J.api.JmolScriptEvaluator,J.util.P3,J.util.V3,~N,~N,~B,J.util.BS");
$_M(c$, "rotateAboutPointsInternal", 
function (eval, point1, point2, degreesPerSecond, endDegrees, isSpin, bsSelected, translation, finalPoints, dihedralList) {
var isOK = this.transformManager.rotateAboutPointsInternal (eval, point1, point2, degreesPerSecond, endDegrees, false, isSpin, bsSelected, false, translation, finalPoints, dihedralList);
if (isOK) this.refresh (-1, "rotateAxisAboutPointsInternal");
return isOK;
}, "J.api.JmolScriptEvaluator,J.util.P3,J.util.P3,~N,~N,~B,J.util.BS,J.util.V3,J.util.JmolList,~A");
$_M(c$, "startSpinningAxis", 
function (pt1, pt2, isClockwise) {
if (this.getSpinOn () || this.getNavOn ()) {
this.setSpinOn (false);
this.setNavOn (false);
return;
}this.transformManager.rotateAboutPointsInternal (null, pt1, pt2, this.global.pickingSpinRate, 3.4028235E38, isClockwise, true, null, false, null, null, null);
}, "J.util.P3,J.util.P3,~B");
$_M(c$, "getModelDipole", 
function () {
return this.modelSet.getModelDipole (this.animationManager.currentModelIndex);
});
$_M(c$, "calculateMolecularDipole", 
function () {
return this.modelSet.calculateMolecularDipole (this.animationManager.currentModelIndex);
});
$_M(c$, "getAtomIdentityInfo", 
function (atomIndex, info) {
this.modelSet.getAtomIdentityInfo (atomIndex, info);
}, "~N,java.util.Map");
$_M(c$, "setDefaultLattice", 
function (ptLattice) {
this.global.setDefaultLattice (ptLattice);
this.global.setS ("defaultLattice", J.util.Escape.eP (ptLattice));
}, "J.util.P3");
$_M(c$, "getDefaultLattice", 
function () {
return this.global.getDefaultLattice ();
});
$_M(c$, "getTaintedAtoms", 
function (type) {
return this.modelSet.getTaintedAtoms (type);
}, "~N");
$_M(c$, "setTaintedAtoms", 
function (bs, type) {
this.modelSet.setTaintedAtoms (bs, type);
}, "J.util.BS,~N");
$_M(c$, "getData", 
function (atomExpression, type) {
var exp = "";
if (type.equalsIgnoreCase ("MOL") || type.equalsIgnoreCase ("SDF") || type.equalsIgnoreCase ("V2000") || type.equalsIgnoreCase ("V3000") || type.equalsIgnoreCase ("XYZVIB") || type.equalsIgnoreCase ("CD")) return this.getModelExtract (atomExpression, false, false, type);
if (type.toLowerCase ().indexOf ("property_") == 0) exp = "{selected}.label(\"%{" + type + "}\")";
 else if (type.equalsIgnoreCase ("CML")) return this.getModelCml (this.getAtomBitSet (atomExpression), 2147483647, true);
 else if (type.equalsIgnoreCase ("PDB")) exp = "{selected and not hetero}.label(\"ATOM  %5i %-4a%1A%3.3n %1c%4R%1E   %8.3x%8.3y%8.3z%6.2Q%6.2b          %2e  \").lines+{selected and hetero}.label(\"HETATM%5i %-4a%1A%3.3n %1c%4R%1E   %8.3x%8.3y%8.3z%6.2Q%6.2b          %2e  \").lines";
 else if (type.equalsIgnoreCase ("XYZRN")) exp = "\"\" + {selected}.size + \"\n\n\"+{selected}.label(\"%-2e %8.3x %8.3y %8.3z %4.2[vdw] 1 [%n]%r.%a#%i\").lines";
 else if (type.startsWith ("USER:")) exp = "{selected}.label(\"" + type.substring (5) + "\").lines";
 else exp = "\"\" + {selected}.size + \"\n\n\"+{selected}.label(\"%-2e %10.5x %10.5y %10.5z\").lines";
if (!atomExpression.equals ("selected")) exp = J.util.TextFormat.simpleReplace (exp, "selected", atomExpression);
return this.evaluateExpression (exp);
}, "~S,~S");
$_M(c$, "getModelCml", 
function (bs, nAtomsMax, addBonds) {
return this.modelSet.getModelCml (bs, nAtomsMax, addBonds);
}, "J.util.BS,~N,~B");
$_M(c$, "getHelixData", 
function (bs, tokType) {
return this.modelSet.getHelixData (bs, tokType);
}, "J.util.BS,~N");
$_M(c$, "getPdbAtomData", 
function (bs, sb) {
return this.modelSet.getPdbAtomData (bs == null ? this.getSelectionSet (true) : bs, sb);
}, "J.util.BS,J.io.OutputStringBuilder");
$_M(c$, "isJmolDataFrameForModel", 
function (modelIndex) {
return this.modelSet.isJmolDataFrameForModel (modelIndex);
}, "~N");
$_M(c$, "isJmolDataFrame", 
function () {
return this.modelSet.isJmolDataFrameForModel (this.animationManager.currentModelIndex);
});
$_M(c$, "getJmolDataFrameIndex", 
function (modelIndex, type) {
return this.modelSet.getJmolDataFrameIndex (modelIndex, type);
}, "~N,~S");
$_M(c$, "setJmolDataFrame", 
function (type, modelIndex, dataIndex) {
this.modelSet.setJmolDataFrame (type, modelIndex, dataIndex);
}, "~S,~N,~N");
$_M(c$, "setFrameTitle", 
function (modelIndex, title) {
this.modelSet.setFrameTitle (J.util.BSUtil.newAndSetBit (modelIndex), title);
}, "~N,~S");
$_M(c$, "setFrameTitleObj", 
function (title) {
this.loadShape (30);
this.modelSet.setFrameTitle (this.getVisibleFramesBitSet (), title);
}, "~O");
$_M(c$, "getFrameTitle", 
function () {
return this.modelSet.getFrameTitle (this.animationManager.currentModelIndex);
});
$_M(c$, "getJmolFrameType", 
function (modelIndex) {
return this.modelSet.getJmolFrameType (modelIndex);
}, "~N");
$_M(c$, "getJmolDataSourceFrame", 
function (modelIndex) {
return this.modelSet.getJmolDataSourceFrame (modelIndex);
}, "~N");
$_M(c$, "setAtomProperty", 
function (bs, tok, iValue, fValue, sValue, values, list) {
if (tok == 1649412120) this.shapeManager.deleteVdwDependentShapes (bs);
this.clearMinimization ();
this.modelSet.setAtomProperty (bs, tok, iValue, fValue, sValue, values, list);
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
}, "J.util.BS,~N,~N,~N,~S,~A,~A");
$_M(c$, "checkCoordinatesChanged", 
function () {
this.modelSet.recalculatePositionDependentQuantities (null, null);
this.refreshMeasures (true);
});
$_M(c$, "setAtomCoord", 
function (atomIndex, x, y, z) {
this.modelSet.setAtomCoord (atomIndex, x, y, z);
}, "~N,~N,~N,~N");
$_M(c$, "setAtomCoordRelative", 
function (atomIndex, x, y, z) {
this.modelSet.setAtomCoordRelative (atomIndex, x, y, z);
}, "~N,~N,~N,~N");
$_M(c$, "setAtomCoords", 
function (bs, tokType, xyzValues) {
if (bs.cardinality () == 0) return;
this.modelSet.setAtomCoords (bs, tokType, xyzValues);
this.checkMinimization ();
this.statusManager.setStatusAtomMoved (bs);
}, "J.util.BS,~N,~O");
$_M(c$, "setAtomCoordsRelative", 
function (offset, bs) {
if (bs == null) bs = this.getSelectionSet (false);
if (bs.cardinality () == 0) return;
this.modelSet.setAtomCoordsRelative (offset, bs);
this.checkMinimization ();
this.statusManager.setStatusAtomMoved (bs);
}, "J.util.Tuple3f,J.util.BS");
$_M(c$, "invertAtomCoordPt", 
function (pt, bs) {
this.modelSet.invertSelected (pt, null, -1, null, bs);
this.checkMinimization ();
this.statusManager.setStatusAtomMoved (bs);
}, "J.util.P3,J.util.BS");
$_M(c$, "invertAtomCoordPlane", 
function (plane, bs) {
this.modelSet.invertSelected (null, plane, -1, null, bs);
this.checkMinimization ();
this.statusManager.setStatusAtomMoved (bs);
}, "J.util.P4,J.util.BS");
$_M(c$, "invertSelected", 
function (pt, plane, iAtom, invAtoms) {
var bs = this.getSelectionSet (false);
if (bs.cardinality () == 0) return;
this.modelSet.invertSelected (pt, plane, iAtom, invAtoms, bs);
this.checkMinimization ();
this.statusManager.setStatusAtomMoved (bs);
}, "J.util.P3,J.util.P4,~N,J.util.BS");
$_M(c$, "moveAtoms", 
function (mNew, matrixRotate, translation, center, isInternal, bsAtoms) {
if (bsAtoms.cardinality () == 0) return;
this.modelSet.moveAtoms (mNew, matrixRotate, translation, bsAtoms, center, isInternal);
this.checkMinimization ();
this.statusManager.setStatusAtomMoved (bsAtoms);
}, "J.util.Matrix3f,J.util.Matrix3f,J.util.V3,J.util.P3,~B,J.util.BS");
$_M(c$, "moveSelected", 
function (deltaX, deltaY, deltaZ, x, y, bsSelected, isTranslation, asAtoms) {
if (deltaZ == 0) return;
if (x == -2147483648) this.rotateBondIndex = -1;
if (this.isJmolDataFrame ()) return;
if (deltaX == -2147483648) {
this.showSelected = true;
this.loadShape (8);
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
var ptCenter = this.getAtomSetCenter (bsSelected);
this.transformManager.finalizeTransformParameters ();
var f = (this.global.antialiasDisplay ? 2 : 1);
var ptScreen = this.transformPt (ptCenter);
var ptScreenNew;
if (deltaZ != -2147483648) ptScreenNew = J.util.P3.new3 (ptScreen.x, ptScreen.y, ptScreen.z + deltaZ + 0.5);
 else ptScreenNew = J.util.P3.new3 (ptScreen.x + deltaX * f + 0.5, ptScreen.y + deltaY * f + 0.5, ptScreen.z);
var ptNew =  new J.util.P3 ();
this.unTransformPoint (ptScreenNew, ptNew);
ptNew.sub (ptCenter);
this.setAtomCoordsRelative (ptNew, bsSelected);
} else {
this.transformManager.rotateXYBy (deltaX, deltaY, bsSelected);
}}}this.refresh (2, "");
this.movingSelected = false;
}, "~N,~N,~N,~N,~N,J.util.BS,~B,~B");
$_M(c$, "highlightBond", 
function (index, isHover) {
if (isHover && !this.hoverEnabled) return;
var bs = null;
if (index >= 0) {
var b = this.modelSet.bonds[index];
var i = b.getAtomIndex2 ();
if (!this.isAtomAssignable (i)) return;
bs = J.util.BSUtil.newAndSetBit (i);
bs.set (b.getAtomIndex1 ());
}this.highlight (bs);
this.refresh (3, "highlightBond");
}, "~N,~B");
$_M(c$, "highlight", 
function (bs) {
if (bs != null) this.loadShape (8);
this.setShapeProperty (8, "highlight", bs);
}, "J.util.BS");
$_M(c$, "setRotateBondIndex", 
function (index) {
var haveBond = (this.rotateBondIndex >= 0);
if (!haveBond && index < 0) return;
this.rotatePrev1 = -1;
this.bsRotateBranch = null;
if (index == -2147483648) return;
this.rotateBondIndex = index;
this.highlightBond (index, false);
}, "~N");
$_M(c$, "getRotateBondIndex", 
function () {
return this.rotateBondIndex;
});
$_M(c$, "actionRotateBond", 
function (deltaX, deltaY, x, y) {
if (this.rotateBondIndex < 0) return;
var bsBranch = this.bsRotateBranch;
var atom1;
var atom2;
if (bsBranch == null) {
var b = this.modelSet.bonds[this.rotateBondIndex];
atom1 = b.getAtom1 ();
atom2 = b.getAtom2 ();
this.undoMoveActionClear (atom1.index, 2, true);
var pt = J.util.P3.new3 (x, y, Clazz.doubleToInt ((atom1.screenZ + atom2.screenZ) / 2));
this.transformManager.unTransformPoint (pt, pt);
if (atom2.getCovalentBondCount () == 1 || pt.distance (atom1) < pt.distance (atom2) && atom1.getCovalentBondCount () != 1) {
var a = atom1;
atom1 = atom2;
atom2 = a;
}if (J.util.Measure.computeAngleABC (pt, atom1, atom2, true) > 90 || J.util.Measure.computeAngleABC (pt, atom2, atom1, true) > 90) {
bsBranch = this.getBranchBitSet (atom2.index, atom1.index, true);
}if (bsBranch != null) for (var n = 0, i = atom1.getBonds ().length; --i >= 0; ) {
if (bsBranch.get (atom1.getBondedAtomIndex (i)) && ++n == 2) {
bsBranch = null;
break;
}}
if (bsBranch == null) {
bsBranch = this.getMoleculeBitSet (atom1.index);
}this.bsRotateBranch = bsBranch;
this.rotatePrev1 = atom1.index;
this.rotatePrev2 = atom2.index;
} else {
atom1 = this.modelSet.atoms[this.rotatePrev1];
atom2 = this.modelSet.atoms[this.rotatePrev2];
}var v1 = J.util.V3.new3 (atom2.screenX - atom1.screenX, atom2.screenY - atom1.screenY, 0);
var v2 = J.util.V3.new3 (deltaX, deltaY, 0);
v1.cross (v1, v2);
var degrees = (v1.z > 0 ? 1 : -1) * v2.length ();
var bs = J.util.BSUtil.copy (bsBranch);
bs.andNot (this.selectionManager.getMotionFixedAtoms ());
this.rotateAboutPointsInternal (this.eval, atom1, atom2, 0, degrees, false, bs, null, null, null);
}, "~N,~N,~N,~N");
$_M(c$, "refreshMeasures", 
function (andStopMinimization) {
this.setShapeProperty (6, "refresh", null);
if (andStopMinimization) this.stopMinimization ();
}, "~B");
$_M(c$, "setDynamicMeasurements", 
function (TF) {
this.global.dynamicMeasurements = TF;
}, "~B");
$_M(c$, "functionXY", 
function (functionName, nX, nY) {
var data = null;
if (functionName.indexOf ("file:") == 0) data = this.getFileAsString (functionName.substring (5));
 else if (functionName.indexOf ("data2d_") != 0) return this.statusManager.functionXY (functionName, nX, nY);
nX = Math.abs (nX);
nY = Math.abs (nY);
var fdata;
if (data == null) {
fdata = this.getDataFloat2D (functionName);
if (fdata != null) return fdata;
data = "";
}fdata =  Clazz.newFloatArray (nX, nY, 0);
var f =  Clazz.newFloatArray (nX * nY, 0);
J.util.Parser.parseStringInfestedFloatArray (data, null, f);
for (var i = 0, n = 0; i < nX; i++) for (var j = 0; j < nY; j++) fdata[i][j] = f[n++];


return fdata;
}, "~S,~N,~N");
$_M(c$, "functionXYZ", 
function (functionName, nX, nY, nZ) {
var data = null;
if (functionName.indexOf ("file:") == 0) data = this.getFileAsString (functionName.substring (5));
 else if (functionName.indexOf ("data3d_") != 0) return this.statusManager.functionXYZ (functionName, nX, nY, nZ);
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
J.util.Parser.parseStringInfestedFloatArray (data, null, f);
for (var i = 0, n = 0; i < nX; i++) for (var j = 0; j < nY; j++) for (var k = 0; k < nZ; k++) xyzdata[i][j][k] = f[n++];



return xyzdata;
}, "~S,~N,~N,~N");
Clazz.overrideMethod (c$, "extractMolData", 
function (what) {
if (what == null) {
var i = this.getCurrentModelIndex ();
if (i < 0) return null;
what = this.getModelNumberDotted (i);
}return this.getModelExtract (what, true, false, "V2000");
}, "~S");
$_M(c$, "getNMRPredict", 
function (openURL) {
var molFile = this.getModelExtract ("selected", true, false, "V2000");
var pt = molFile.indexOf ("\n");
molFile = "Jmol " + J.viewer.Viewer.version_date + molFile.substring (pt);
if (openURL) {
if (this.$isApplet) {
this.showUrl (this.global.nmrUrlFormat + molFile);
} else {
this.syncScript ("true", "*", 0);
this.syncScript ("JSpecView:", ".", 0);
}return null;
}var url = this.global.nmrPredictFormat + molFile;
return this.getFileAsString (url);
}, "~B");
$_M(c$, "getHelp", 
function (what) {
if (this.global.helpPath.indexOf ("?") < 0) {
if (what.length > 0 && what.indexOf ("?") != 0) what = "?search=" + J.util.TextFormat.simpleReplace (what, " ", "%20");
what += (what.length == 0 ? "?ver=" : "&ver=") + J.viewer.JC.version;
} else {
what = "&" + what;
}this.showUrl (this.global.helpPath + what);
}, "~S");
$_M(c$, "show2D", 
function (smiles) {
this.showUrl (this.setLoadFormat ("_" + smiles, '2', false));
}, "~S");
$_M(c$, "getChemicalInfo", 
function (smiles, type, info) {
var s = this.setLoadFormat ("_" + smiles, type, false);
if (type == '/') s += J.util.TextFormat.simpleReplace (info, " ", "%20");
return this.getFileAsString4 (s, -1, false, false);
}, "~S,~S,~S");
$_M(c$, "addCommand", 
function (command) {
if (this.autoExit || !this.haveDisplay || !this.getPreserveState ()) return;
this.commandHistory.addCommand (J.util.TextFormat.replaceAllCharacters (command, "\r\n\t", " "));
}, "~S");
$_M(c$, "removeCommand", 
function () {
return this.commandHistory.removeCommand ();
});
Clazz.overrideMethod (c$, "getSetHistory", 
function (howFarBack) {
return this.commandHistory.getSetHistory (howFarBack);
}, "~N");
$_M(c$, "getOutputStream", 
function (localName, fullPath) {
return this.getStateCreator ().getOutputStream (localName, fullPath);
}, "~S,~A");
Clazz.overrideMethod (c$, "writeTextFile", 
function (fileName, data) {
this.createImage (fileName, "txt", data, -2147483648, 0, 0);
}, "~S,~S");
Clazz.overrideMethod (c$, "clipImage", 
function (text) {
if (!this.isRestricted (J.viewer.Viewer.ACCESS.ALL)) return "no";
var c;
try {
c = this.getImageCreator ();
return c.clipImage (this, text);
} catch (er) {
if (Clazz.exceptionOf (er, Error)) {
return J.i18n.GT._ ("clipboard is not accessible -- use signed applet");
} else {
throw er;
}
}
}, "~S");
$_M(c$, "createImageSet", 
function (fileName, type, text, bytes, scripts, quality, width, height, bsFrames, nVibes, fullPath) {
return this.getStateCreator ().createImageSet (fileName, type, text, bytes, scripts, quality, width, height, bsFrames, nVibes, fullPath);
}, "~S,~S,~S,~A,~A,~N,~N,~N,J.util.BS,~N,~A");
$_M(c$, "createZip", 
function (fileName, type, stateInfo, scripts) {
return this.getStateCreator ().createImage (fileName, type, stateInfo, null, scripts, -2147483648, -1, -1);
}, "~S,~S,~S,~A");
$_M(c$, "createImage", 
function (fileName, type, text_or_bytes, quality, width, height) {
var text = (Clazz.instanceOf (text_or_bytes, String) ? text_or_bytes : null);
var bytes = (Clazz.instanceOf (text_or_bytes, Array) ? text_or_bytes : null);
return this.getStateCreator ().createImage (fileName, type, text, bytes, null, quality, width, height);
}, "~S,~S,~O,~N,~N,~N");
$_M(c$, "createImage", 
function (fileName, type, text, bytes, quality, width, height) {
return this.getStateCreator ().createImage (fileName, type, text, bytes, null, quality, width, height);
}, "~S,~S,~S,~A,~N,~N,~N");
$_M(c$, "getImageCreator", 
function () {
return (J.api.Interface.getOptionInterface (this.isJS && !this.isWebGL ? "exportjs.JSImageCreator" : "export.image.AwtImageCreator")).setViewer (this, this.privateKey);
});
$_M(c$, "setSyncTarget", 
($fz = function (mode, TF) {
switch (mode) {
case 0:
this.statusManager.syncingMouse = TF;
break;
case 1:
this.statusManager.syncingScripts = TF;
break;
case 2:
this.statusManager.syncSend (TF ? "GET_GRAPHICS" : "SET_GRAPHICS_OFF", "*", 0);
if (Float.isNaN (this.transformManager.stereoDegrees)) this.setFloatProperty ("stereoDegrees", -5);
if (TF) {
this.setBooleanProperty ("_syncMouse", false);
this.setBooleanProperty ("_syncScript", false);
}return;
}
if (!this.statusManager.syncingScripts && !this.statusManager.syncingMouse) this.refresh (-1, "set sync");
}, $fz.isPrivate = true, $fz), "~N,~B");
Clazz.overrideMethod (c$, "syncScript", 
function (script, applet, port) {
this.getStateCreator ().syncScript (script, applet, port);
}, "~S,~S,~N");
$_M(c$, "getModelIndexFromId", 
function (id) {
return this.modelSet.getModelIndexFromId (id);
}, "~S");
$_M(c$, "setSyncDriver", 
function (mode) {
this.statusManager.setSyncDriver (mode);
}, "~N");
$_M(c$, "getPartialCharges", 
function () {
return this.modelSet.getPartialCharges ();
});
$_M(c$, "getAtomicPotentials", 
function (isMep, bsSelected, bsIgnore, fileName) {
var potentials =  Clazz.newFloatArray (this.getAtomCount (), 0);
var m = J.api.Interface.getOptionInterface ("quantum.MlpCalculation");
var data = (fileName == null ? null : this.getFileAsString (fileName));
m.assignPotentials (this.modelSet.atoms, potentials, this.getSmartsMatch ("a", bsSelected), this.getSmartsMatch ("/noAromatic/[$(C=O),$(O=C),$(NC=O)]", bsSelected), bsIgnore, data);
return potentials;
}, "~B,J.util.BS,J.util.BS,~S");
$_M(c$, "setProteinType", 
function (type, bs) {
this.modelSet.setProteinType (bs == null ? this.getSelectionSet (false) : bs, type);
}, "J.constant.EnumStructure,J.util.BS");
Clazz.overrideMethod (c$, "getBondPoint3f1", 
function (i) {
return this.modelSet.getBondAtom1 (i);
}, "~N");
Clazz.overrideMethod (c$, "getBondPoint3f2", 
function (i) {
return this.modelSet.getBondAtom2 (i);
}, "~N");
$_M(c$, "getVibration", 
function (atomIndex) {
return this.modelSet.getVibration (atomIndex, false);
}, "~N");
$_M(c$, "getVanderwaalsMar", 
function (i) {
return (this.dataManager.defaultVdw === J.constant.EnumVdw.USER ? this.dataManager.userVdwMars[i] : J.util.Elements.getVanderwaalsMar (i, this.dataManager.defaultVdw));
}, "~N");
$_M(c$, "getVanderwaalsMarType", 
function (atomicAndIsotopeNumber, type) {
if (type == null) type = this.dataManager.defaultVdw;
 else switch (type) {
case J.constant.EnumVdw.USER:
if (this.dataManager.bsUserVdws == null) type = this.dataManager.defaultVdw;
 else return this.dataManager.userVdwMars[atomicAndIsotopeNumber & 127];
break;
case J.constant.EnumVdw.AUTO:
case J.constant.EnumVdw.JMOL:
case J.constant.EnumVdw.BABEL:
case J.constant.EnumVdw.RASMOL:
if (this.dataManager.defaultVdw !== J.constant.EnumVdw.AUTO) type = this.dataManager.defaultVdw;
break;
}
return (J.util.Elements.getVanderwaalsMar (atomicAndIsotopeNumber, type));
}, "~N,J.constant.EnumVdw");
$_M(c$, "setDefaultVdw", 
function (type) {
var vType = J.constant.EnumVdw.getVdwType (type);
if (vType == null) vType = J.constant.EnumVdw.AUTO;
this.dataManager.setDefaultVdw (vType);
this.global.setS ("defaultVDW", this.getDefaultVdwTypeNameOrData (-2147483648, null));
}, "~S");
$_M(c$, "getDefaultVdwTypeNameOrData", 
function (iMode, vType) {
return this.dataManager.getDefaultVdwNameOrData (iMode, vType, null);
}, "~N,J.constant.EnumVdw");
$_M(c$, "deleteAtoms", 
function (bs, fullModels) {
var atomIndex = (bs == null ? -1 : bs.nextSetBit (0));
if (atomIndex < 0) return 0;
this.clearModelDependentObjects ();
if (!fullModels) {
this.statusManager.modifySend (atomIndex, this.modelSet.atoms[atomIndex].modelIndex, 4);
this.modelSet.deleteAtoms (bs);
var n = this.selectionManager.deleteAtoms (bs);
this.setTainted (true);
this.statusManager.modifySend (atomIndex, this.modelSet.atoms[atomIndex].modelIndex, -4);
return n;
}this.statusManager.modifySend (-1, this.modelSet.atoms[atomIndex].modelIndex, 5);
this.setCurrentModelIndexClear (0, false);
this.animationManager.setAnimationOn (false);
var bsD0 = J.util.BSUtil.copy (this.getDeletedAtoms ());
var bsDeleted = this.modelSet.deleteModels (bs);
this.selectionManager.processDeletedModelAtoms (bsDeleted);
this.setAnimationRange (0, 0);
if (this.eval != null) this.eval.deleteAtomsInVariables (bsDeleted);
this.clearRepaintManager (-1);
this.animationManager.clear ();
this.animationManager.initializePointers (1);
this.setCurrentModelIndexClear (this.getModelCount () > 1 ? -1 : 0, this.getModelCount () > 1);
this.hoverAtomIndex = -1;
this.setFileLoadStatus (J.constant.EnumFileStatus.DELETED, null, null, null, null, null);
this.refreshMeasures (true);
if (bsD0 != null) bsDeleted.andNot (bsD0);
this.statusManager.modifySend (-1, this.modelSet.atoms[atomIndex].modelIndex, -5);
return J.util.BSUtil.cardinalityOf (bsDeleted);
}, "J.util.BS,~B");
$_M(c$, "deleteBonds", 
function (bsDeleted) {
this.modelSet.deleteBonds (bsDeleted, false);
}, "J.util.BS");
$_M(c$, "deleteModelAtoms", 
function (firstAtomIndex, nAtoms, bsDeleted) {
this.selectionManager.deleteModelAtoms (bsDeleted);
J.util.BSUtil.deleteBits (this.getFrameOffsets (), bsDeleted);
this.setFrameOffsets (this.getFrameOffsets ());
this.dataManager.deleteModelAtoms (firstAtomIndex, nAtoms, bsDeleted);
}, "~N,~N,J.util.BS");
$_M(c$, "getDeletedAtoms", 
function () {
return this.selectionManager.getDeletedAtoms ();
});
$_M(c$, "getQuaternionFrame", 
function () {
return this.global.quaternionFrame.charAt (this.global.quaternionFrame.length == 2 ? 1 : 0);
});
$_M(c$, "calculatePointGroup", 
function () {
return this.modelSet.calculatePointGroup (this.getSelectionSet (false));
});
$_M(c$, "getPointGroupInfo", 
function (atomExpression) {
return this.modelSet.getPointGroupInfo (this.getAtomBitSet (atomExpression));
}, "~O");
$_M(c$, "getPointGroupAsString", 
function (asDraw, type, index, scale) {
return this.modelSet.getPointGroupAsString (this.getSelectionSet (false), asDraw, type, index, scale);
}, "~B,~S,~N,~N");
$_M(c$, "loadImage", 
function (pathName, echoName) {
this.fileManager.loadImage (pathName, echoName);
}, "~S,~S");
$_M(c$, "loadImageData", 
function (image, nameOrError, echoName, sc) {
if (image == null) J.util.Logger.info (nameOrError);
if (echoName == null) {
this.setBackgroundImage ((image == null ? null : nameOrError), image);
} else {
this.setShapeProperty (30, "text", nameOrError);
if (image != null) this.setShapeProperty (30, "image", image);
}if (sc != null) {
sc.mustResumeEval = true;
this.eval.resumeEval (sc);
}}, "~O,~S,~S,J.script.ScriptContext");
$_M(c$, "cd", 
function (dir) {
if (dir == null) {
dir = ".";
} else if (dir.length == 0) {
this.setStringProperty ("defaultDirectory", "");
dir = ".";
}dir = this.fileManager.getDefaultDirectory (dir + (dir.equals ("=") ? "" : dir.endsWith ("/") ? "X.spt" : "/X.spt"));
if (dir.length > 0) this.setStringProperty ("defaultDirectory", dir);
var path = this.fileManager.getFilePath (dir + "/", true, false);
if (path.startsWith ("file:/")) J.viewer.FileManager.setLocalPath (this, dir, false);
return dir;
}, "~S");
$_M(c$, "setErrorMessage", 
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
$_M(c$, "setShapeErrorState", 
function (shapeID, state) {
this.currentShapeID = shapeID;
this.currentShapeState = state;
}, "~N,~S");
$_M(c$, "getShapeErrorState", 
function () {
if (this.currentShapeID < 0) return "";
if (this.modelSet != null) this.shapeManager.releaseShape (this.currentShapeID);
this.clearRepaintManager (this.currentShapeID);
return J.viewer.JC.getShapeClassName (this.currentShapeID, false) + " " + this.currentShapeState;
});
$_M(c$, "handleError", 
function (er, doClear) {
try {
if (doClear) this.zapMsg ("" + er);
this.undoClear ();
if (J.util.Logger.getLogLevel () == 0) J.util.Logger.setLogLevel (4);
this.setCursor (0);
this.setBooleanProperty ("refreshing", true);
this.fileManager.setPathForAllFiles ("");
J.util.Logger.error ("viewer handling error condition: " + er + "  ");
if (!this.isJS) er.printStackTrace ();
this.notifyError ("Error", "doClear=" + doClear + "; " + er, "" + er);
} catch (e1) {
try {
J.util.Logger.error ("Could not notify error " + er + ": due to " + e1);
} catch (er2) {
}
}
}, "Error,~B");
$_M(c$, "getAtomicCharges", 
function () {
return this.modelSet.getAtomicCharges ();
});
$_M(c$, "getFunctions", 
function (isStatic) {
return (isStatic ? J.viewer.Viewer.staticFunctions : this.localFunctions);
}, "~B");
$_M(c$, "removeFunction", 
function (name) {
var $function = this.getFunction (name);
if ($function == null) return;
J.viewer.Viewer.staticFunctions.remove (name);
this.localFunctions.remove (name);
}, "~S");
$_M(c$, "getFunction", 
function (name) {
if (name == null) return null;
var $function = (J.viewer.Viewer.isStaticFunction (name) ? J.viewer.Viewer.staticFunctions : this.localFunctions).get (name);
return ($function == null || $function.geTokens () == null ? null : $function);
}, "~S");
c$.isStaticFunction = $_M(c$, "isStaticFunction", 
($fz = function (name) {
return name.startsWith ("static_");
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "isFunction", 
function (name) {
return (J.viewer.Viewer.isStaticFunction (name) ? J.viewer.Viewer.staticFunctions : this.localFunctions).containsKey (name);
}, "~S");
$_M(c$, "clearFunctions", 
function () {
J.viewer.Viewer.staticFunctions.clear ();
this.localFunctions.clear ();
});
$_M(c$, "addFunction", 
function ($function) {
var name = $function.getName ();
(J.viewer.Viewer.isStaticFunction (name) ? J.viewer.Viewer.staticFunctions : this.localFunctions).put (name, $function);
}, "J.api.JmolScriptFunction");
$_M(c$, "getFunctionCalls", 
function (selectedFunction) {
return this.getStateCreator ().getFunctionCalls (selectedFunction);
}, "~S");
$_M(c$, "showMessage", 
function (s) {
if (!this.isPrintOnly) J.util.Logger.warn (s);
}, "~S");
$_M(c$, "getMoInfo", 
function (modelIndex) {
return this.modelSet.getMoInfo (modelIndex);
}, "~N");
Clazz.overrideMethod (c$, "checkPrivateKey", 
function (privateKey) {
return privateKey == this.privateKey;
}, "~N");
$_M(c$, "bindAction", 
function (desc, name) {
if (this.haveDisplay) this.actionManager.bindAction (desc, name);
}, "~S,~S");
$_M(c$, "unBindAction", 
function (desc, name) {
if (this.haveDisplay) this.actionManager.unbindAction (desc, name);
}, "~S,~S");
$_M(c$, "getMouseInfo", 
function () {
return (this.haveDisplay ? this.actionManager.getMouseInfo () : null);
});
$_M(c$, "getFrontPlane", 
function () {
return this.transformManager.getFrontPlane ();
});
$_M(c$, "getPlaneIntersection", 
function (type, plane, scale, flags) {
return this.modelSet.getPlaneIntersection (type, plane, scale, flags, this.animationManager.currentModelIndex);
}, "~N,J.util.P4,~N,~N");
$_M(c$, "calculateStruts", 
function (bs1, bs2) {
return this.modelSet.calculateStruts (bs1 == null ? this.getSelectionSet (false) : bs1, bs2 == null ? this.getSelectionSet (false) : bs2);
}, "J.util.BS,J.util.BS");
$_M(c$, "getPreserveState", 
function () {
return (this.global.preserveState && this.scriptManager != null);
});
$_M(c$, "isKiosk", 
function () {
return this.$isKiosk;
});
$_M(c$, "hasFocus", 
function () {
return (this.haveDisplay && (this.$isKiosk || this.apiPlatform.hasFocus (this.display)));
});
$_M(c$, "setFocus", 
function () {
if (this.haveDisplay && !this.apiPlatform.hasFocus (this.display)) this.apiPlatform.requestFocusInWindow (this.display);
});
$_M(c$, "getMinimizer", 
function (createNew) {
if (this.minimizer == null && createNew) {
this.minimizer = J.api.Interface.getOptionInterface ("minimize.Minimizer");
this.minimizer.setProperty ("viewer", this);
}return this.minimizer;
}, "~B");
$_M(c$, "stopMinimization", 
function () {
if (this.minimizer != null) {
this.minimizer.setProperty ("stop", null);
}});
$_M(c$, "clearMinimization", 
function () {
if (this.minimizer != null) this.minimizer.setProperty ("clear", null);
});
$_M(c$, "getMinimizationInfo", 
function () {
return (this.minimizer == null ? "" : this.minimizer.getProperty ("log", 0));
});
$_M(c$, "checkMinimization", 
($fz = function () {
this.refreshMeasures (true);
if (!this.global.monitorEnergy) return;
this.minimize (0, 0, this.getModelUndeletedAtomsBitSet (-1), null, 0, false, true, false);
this.echoMessage (this.getParameter ("_minimizationForceField") + " Energy = " + this.getParameter ("_minimizationEnergy"));
}, $fz.isPrivate = true, $fz));
$_M(c$, "minimize", 
function (steps, crit, bsSelected, bsFixed, rangeFixed, addHydrogen, isSilent, isLoad2D) {
var ff = this.global.forceField;
var bsInFrame = this.getModelUndeletedAtomsBitSetBs (this.getVisibleFramesBitSet ());
if (bsSelected == null) bsSelected = this.getModelUndeletedAtomsBitSet (this.getVisibleFramesBitSet ().length () - 1);
 else bsSelected.and (bsInFrame);
if (rangeFixed <= 0) rangeFixed = 5.0;
var bsMotionFixed = J.util.BSUtil.copy (bsFixed == null ? this.selectionManager.getMotionFixedAtoms () : bsFixed);
var haveFixed = (bsMotionFixed.cardinality () > 0);
if (haveFixed) bsSelected.andNot (bsMotionFixed);
var bsNearby = this.getAtomsWithinRadius (rangeFixed, bsSelected, true, null);
bsNearby.andNot (bsSelected);
if (haveFixed) {
bsMotionFixed.and (bsNearby);
} else {
bsMotionFixed = bsNearby;
}bsMotionFixed.and (bsInFrame);
if (addHydrogen) bsSelected.or (this.addHydrogens (bsSelected, isLoad2D, isSilent));
if (bsSelected.cardinality () > 200) {
J.util.Logger.error ("Too many atoms for minimization (>200)");
return;
}try {
if (!isSilent) J.util.Logger.info ("Minimizing " + bsSelected.cardinality () + " atoms");
this.getMinimizer (true).minimize (steps, crit, bsSelected, bsMotionFixed, haveFixed, isSilent, ff);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("Minimization error: " + e.toString ());
if (!this.isJS) e.printStackTrace ();
} else {
throw e;
}
}
}, "~N,~N,J.util.BS,J.util.BS,~N,~B,~B,~B");
$_M(c$, "setMotionFixedAtoms", 
function (bs) {
this.selectionManager.setMotionFixedAtoms (bs);
}, "J.util.BS");
$_M(c$, "getMotionFixedAtoms", 
function () {
return this.selectionManager.getMotionFixedAtoms ();
});
$_M(c$, "rotateArcBall", 
function (x, y, factor) {
this.transformManager.rotateArcBall (x, y, factor);
this.refresh (2, this.statusManager.syncingMouse ? "Mouse: rotateArcBall " + x + " " + y + " " + factor : "");
}, "~N,~N,~N");
$_M(c$, "getAtomicPropertyState", 
function (commands, type, bs, name, data) {
this.getStateCreator ().getAtomicPropertyStateBuffer (commands, type, bs, name, data);
}, "J.util.SB,~N,J.util.BS,~S,~A");
$_M(c$, "getCenterAndPoints", 
function (atomSets, addCenter) {
return this.modelSet.getCenterAndPoints (atomSets, addCenter);
}, "J.util.JmolList,~B");
$_M(c$, "writeFileData", 
function (fileName, type, modelIndex, parameters) {
return this.getStateCreator ().writeFileData (fileName, type, modelIndex, parameters);
}, "~S,~S,~N,~A");
$_M(c$, "getPdbData", 
function (modelIndex, type, parameters) {
return this.modelSet.getPdbData (modelIndex, type, this.getSelectionSet (false), parameters, null);
}, "~N,~S,~A");
$_M(c$, "getGroupsWithin", 
function (nResidues, bs) {
return this.modelSet.getGroupsWithin (nResidues, bs);
}, "~N,J.util.BS");
$_M(c$, "getExecutor", 
function () {
if (this.executor != null || J.viewer.Viewer.nProcessors < 2) return this.executor;
try {
this.executor = (J.api.Interface.getOptionInterface ("parallel.ScriptParallelProcessor")).getExecutor ();
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
if (this.executor == null) J.util.Logger.error ("parallel processing is not available");
return this.executor;
});
$_M(c$, "getShapeInfo", 
function () {
return this.shapeManager.getShapeInfo ();
});
$_M(c$, "togglePickingLabel", 
function (bs) {
if (bs == null) bs = this.getSelectionSet (false);
this.loadShape (5);
this.shapeManager.setShapePropertyBs (5, "toggleLabel", null, bs);
}, "J.util.BS");
$_M(c$, "loadShape", 
function (shapeID) {
this.shapeManager.loadShape (shapeID);
}, "~N");
$_M(c$, "setShapeSize", 
function (shapeID, mad, bsSelected) {
if (bsSelected == null) bsSelected = this.getSelectionSet (false);
this.shapeManager.setShapeSizeBs (shapeID, mad, null, bsSelected);
}, "~N,~N,J.util.BS");
$_M(c$, "setShapeSizeRD", 
function (shapeID, rd, bsAtoms) {
this.shapeManager.setShapeSizeBs (shapeID, 0, rd, bsAtoms);
}, "~N,J.atomdata.RadiusData,J.util.BS");
$_M(c$, "setShapeProperty", 
function (shapeID, propertyName, value) {
if (shapeID < 0) return;
this.shapeManager.setShapePropertyBs (shapeID, propertyName, value, null);
}, "~N,~S,~O");
$_M(c$, "getShapeProperty", 
function (shapeType, propertyName) {
return this.shapeManager.getShapePropertyIndex (shapeType, propertyName, -2147483648);
}, "~N,~S");
$_M(c$, "getShapePropertyData", 
function (shapeType, propertyName, data) {
return this.shapeManager.getShapePropertyData (shapeType, propertyName, data);
}, "~N,~S,~A");
$_M(c$, "getShapePropertyIndex", 
function (shapeType, propertyName, index) {
return this.shapeManager.getShapePropertyIndex (shapeType, propertyName, index);
}, "~N,~S,~N");
$_M(c$, "getShapePropertyAsInt", 
($fz = function (shapeID, propertyName) {
var value = this.getShapeProperty (shapeID, propertyName);
return value == null || !(Clazz.instanceOf (value, Integer)) ? -2147483648 : (value).intValue ();
}, $fz.isPrivate = true, $fz), "~N,~S");
$_M(c$, "setModelVisibility", 
function () {
if (this.shapeManager == null) return;
this.shapeManager.setModelVisibility ();
});
$_M(c$, "resetShapes", 
function (andCreateNew) {
this.shapeManager.resetShapes ();
if (andCreateNew) {
this.shapeManager.loadDefaultShapes (this.modelSet);
this.clearRepaintManager (-1);
}}, "~B");
$_M(c$, "setAtomLabel", 
function (value, i) {
this.shapeManager.setAtomLabel (value, i);
}, "~S,~N");
$_M(c$, "deleteShapeAtoms", 
function (value, bs) {
this.shapeManager.deleteShapeAtoms (value, bs);
}, "~A,J.util.BS");
$_M(c$, "resetBioshapes", 
function (bsAllAtoms) {
this.shapeManager.resetBioshapes (bsAllAtoms);
}, "J.util.BS");
$_M(c$, "getAtomShapeValue", 
function (tok, group, atomIndex) {
return this.shapeManager.getAtomShapeValue (tok, group, atomIndex);
}, "~N,J.modelset.Group,~N");
$_M(c$, "mergeShapes", 
function (newShapes) {
this.shapeManager.mergeShapes (newShapes);
}, "~A");
$_M(c$, "getShapeManager", 
function () {
return this.shapeManager;
});
$_M(c$, "setParallel", 
function (TF) {
return (this.$isParallel = this.global.multiProcessor && TF);
}, "~B");
$_M(c$, "isParallel", 
function () {
return this.global.multiProcessor && this.$isParallel;
});
$_M(c$, "getRenderableBitSet", 
function () {
return this.shapeManager.getRenderableBitSet ();
});
$_M(c$, "setAtomPickingOption", 
($fz = function (option) {
if (this.haveDisplay) this.actionManager.setAtomPickingOption (option);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "setBondPickingOption", 
($fz = function (option) {
if (this.haveDisplay) this.actionManager.setBondPickingOption (option);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "undoClear", 
function () {
this.actionStates.clear ();
this.actionStatesRedo.clear ();
});
$_M(c$, "undoMoveAction", 
function (action, n) {
this.getStateCreator ().undoMoveAction (action, n);
}, "~N,~N");
$_M(c$, "undoMoveActionClear", 
function (taintedAtom, type, clearRedo) {
if (!this.global.preserveState) return;
this.getStateCreator ().undoMoveActionClear (taintedAtom, type, clearRedo);
}, "~N,~N,~B");
$_M(c$, "assignBond", 
function (bondIndex, type) {
try {
var bsAtoms = this.modelSet.setBondOrder (bondIndex, type);
if (bsAtoms == null || type == '0') this.refresh (3, "setBondOrder");
 else this.addHydrogens (bsAtoms, false, true);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("assignBond failed");
} else {
throw e;
}
}
}, "~N,~S");
$_M(c$, "assignAtom", 
function (atomIndex, pt, type) {
if (type.equals ("X")) this.setRotateBondIndex (-1);
if (this.modelSet.atoms[atomIndex].modelIndex != this.modelSet.modelCount - 1) return;
this.clearModelDependentObjects ();
if (pt == null) {
this.statusManager.modifySend (atomIndex, this.modelSet.atoms[atomIndex].modelIndex, 1);
var atomCount = this.modelSet.getAtomCount ();
this.modelSet.assignAtom (atomIndex, type, true);
if (!J.util.Parser.isOneOf (type, ";Mi;Pl;X;")) this.modelSet.setAtomNamesAndNumbers (atomIndex, -atomCount, null);
this.statusManager.modifySend (atomIndex, this.modelSet.atoms[atomIndex].modelIndex, -1);
this.refresh (3, "assignAtom");
return;
}var atom = this.modelSet.atoms[atomIndex];
var bs = J.util.BSUtil.newAndSetBit (atomIndex);
var pts = [pt];
var vConnections =  new J.util.JmolList ();
vConnections.addLast (atom);
var modelIndex = atom.modelIndex;
this.statusManager.modifySend (atomIndex, modelIndex, 3);
try {
bs = this.addHydrogensInline (bs, vConnections, pts);
atomIndex = bs.nextSetBit (0);
this.modelSet.assignAtom (atomIndex, type, false);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
this.modelSet.setAtomNamesAndNumbers (atomIndex, -1, null);
this.statusManager.modifySend (atomIndex, modelIndex, -3);
}, "~N,J.util.P3,~S");
$_M(c$, "assignConnect", 
function (index, index2) {
this.clearModelDependentObjects ();
var connections = J.util.ArrayUtil.newFloat2 (1);
connections[0] = [index, index2];
var modelIndex = this.modelSet.atoms[index].modelIndex;
this.statusManager.modifySend (index, modelIndex, 2);
this.modelSet.connect (connections);
this.modelSet.assignAtom (index, ".", true);
this.modelSet.assignAtom (index2, ".", true);
this.statusManager.modifySend (index, modelIndex, -2);
this.refresh (3, "assignConnect");
}, "~N,~N");
$_M(c$, "moveAtomWithHydrogens", 
function (atomIndex, deltaX, deltaY, deltaZ, bsAtoms) {
this.stopMinimization ();
if (bsAtoms == null) {
var atom = this.modelSet.atoms[atomIndex];
bsAtoms = J.util.BSUtil.newAndSetBit (atomIndex);
var bonds = atom.getBonds ();
if (bonds != null) for (var i = 0; i < bonds.length; i++) {
var atom2 = bonds[i].getOtherAtom (atom);
if (atom2.getElementNumber () == 1) bsAtoms.set (atom2.index);
}
}this.moveSelected (deltaX, deltaY, deltaZ, -2147483648, -2147483648, bsAtoms, true, true);
}, "~N,~N,~N,~N,J.util.BS");
c$.getInlineData = $_M(c$, "getInlineData", 
function (loadScript, strModel, isAppend) {
J.viewer.DataManager.getInlineData (loadScript, strModel, isAppend, null);
}, "J.util.SB,~S,~B");
$_M(c$, "isAtomPDB", 
function (i) {
return this.modelSet.isAtomPDB (i);
}, "~N");
$_M(c$, "isModelPDB", 
function (i) {
return this.modelSet.models[i].isBioModel;
}, "~N");
$_M(c$, "isAtomAssignable", 
function (i) {
return this.modelSet.isAtomAssignable (i);
}, "~N");
Clazz.overrideMethod (c$, "deleteMeasurement", 
function (i) {
this.setShapeProperty (6, "delete", Integer.$valueOf (i));
}, "~N");
$_M(c$, "haveModelKit", 
function () {
return this.modelSet.haveModelKit ();
});
$_M(c$, "getModelKitStateBitSet", 
function (bs, bsDeleted) {
return this.modelSet.getModelKitStateBitset (bs, bsDeleted);
}, "J.util.BS,J.util.BS");
$_M(c$, "getSmiles", 
function (index1, index2, bsSelected, isBioSmiles, allowUnmatchedRings, addCrossLinks, addComment) {
var atoms = this.modelSet.atoms;
if (bsSelected == null) {
if (index1 < 0 || index2 < 0) {
bsSelected = this.getSelectionSet (true);
} else {
if (isBioSmiles) {
if (index1 > index2) {
var i = index1;
index1 = index2;
index2 = i;
}index1 = atoms[index1].getGroup ().firstAtomIndex;
index2 = atoms[index2].getGroup ().lastAtomIndex;
}bsSelected =  new J.util.BS ();
bsSelected.setBits (index1, index2 + 1);
}}var comment = (addComment ? J.viewer.Viewer.getJmolVersion () + " " + this.getModelName (this.getCurrentModelIndex ()) : null);
return this.getSmilesMatcher ().getSmiles (atoms, this.getAtomCount (), bsSelected, isBioSmiles, allowUnmatchedRings, addCrossLinks, comment);
}, "~N,~N,J.util.BS,~B,~B,~B,~B");
$_M(c$, "connect", 
function (connections) {
this.modelSet.connect (connections);
}, "~A");
$_M(c$, "prompt", 
function (label, data, list, asButtons) {
return (this.$isKiosk ? "null" : this.apiPlatform.prompt (label, data, list, asButtons));
}, "~S,~S,~A,~B");
$_M(c$, "getColorEncoder", 
function (colorScheme) {
return this.colorManager.getColorEncoder (colorScheme);
}, "~S");
$_M(c$, "displayBonds", 
function (bs, isDisplay) {
this.modelSet.displayBonds (bs, isDisplay);
}, "J.modelset.Bond.BondSet,~B");
$_M(c$, "getModelAtomProperty", 
function (atom, text) {
return this.modelSet.getModelAtomProperty (atom, text);
}, "J.modelset.Atom,~S");
$_M(c$, "setStateScriptVersion", 
function (version) {
if (version != null) {
var tokens = J.util.Parser.getTokens (version.$replace ('.', ' ').$replace ('_', ' '));
try {
var main = J.util.Parser.parseInt (tokens[0]);
var sub = J.util.Parser.parseInt (tokens[1]);
var minor = J.util.Parser.parseInt (tokens[2]);
if (minor == -2147483648) minor = 0;
if (main != -2147483648 && sub != -2147483648) {
this.stateScriptVersionInt = main * 10000 + sub * 100 + minor;
this.global.legacyAutoBonding = (this.stateScriptVersionInt < 110924);
this.global.legacyHAddition = (this.stateScriptVersionInt < 130117);
return;
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}this.setBooleanProperty ("legacyautobonding", false);
this.stateScriptVersionInt = 2147483647;
}, "~S");
$_M(c$, "initializeExporter", 
function (type, fileName) {
if (this.jsExporter3D != null) {
this.jsExporter3D.initializeOutput (type, this, this.privateKey, this.gdata, null);
return this.jsExporter3D;
}var isJS = type.equals ("JS");
var output = (fileName == null ?  new J.util.SB () : fileName);
var export3D = null;
try {
var export3Dclass = Class.forName (isJS ? "J.exportjs.Export3D" : "J.export.Export3D");
export3D = export3Dclass.newInstance ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
var exporter = export3D.initializeExporter (type, this, this.privateKey, this.gdata, output);
if (isJS && exporter != null) this.jsExporter3D = export3D;
return (exporter == null ? null : export3D);
}, "~S,~S");
$_M(c$, "setPrivateKeyForShape", 
function (iShape) {
this.setShapeProperty (iShape, "privateKey", Double.$valueOf (this.privateKey));
}, "~N");
$_M(c$, "getMouseEnabled", 
function () {
return this.refreshing && !this.creatingImage;
});
$_M(c$, "setZslabPoint", 
function (pt) {
this.transformManager.setZslabPoint (pt);
}, "J.util.P3");
Clazz.overrideMethod (c$, "calcAtomsMinMax", 
function (bs, boxInfo) {
this.modelSet.calcAtomsMinMax (bs, boxInfo);
}, "J.util.BS,J.util.BoxInfo");
$_M(c$, "getObjectMap", 
function (map, c) {
switch (c) {
case '{':
if (this.getScriptManager () != null) {
var m = map;
var sets = this.eval.getDefinedAtomSets ();
if (sets != null) m.putAll (sets);
J.script.T.getTokensType (m, 3145728);
}return;
case '$':
case '0':
this.shapeManager.getObjectMap (map, c == '$');
return;
}
}, "java.util.Map,~S");
$_M(c$, "getPdbBondInfo", 
function (group3) {
if (this.htPdbBondInfo == null) this.htPdbBondInfo =  new java.util.Hashtable ();
var info = this.htPdbBondInfo.get (group3);
if (info != null) return info;
info = J.viewer.JC.getPdbBondInfo (J.modelset.Group.lookupGroupID (group3), this.global.legacyHAddition);
this.htPdbBondInfo.put (group3, info);
return info;
}, "~S");
$_M(c$, "setPicked", 
function (iAtom) {
this.global.setPicked (iAtom);
}, "~N");
Clazz.overrideMethod (c$, "runScript", 
function (script) {
var outputBuffer =  new J.util.SB ();
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
$_M(c$, "allowSpecAtom", 
function () {
return this.modelSet.allowSpecAtom ();
});
$_M(c$, "setFrameDelayMs", 
function (millis) {
this.modelSet.setFrameDelayMs (millis, this.getVisibleFramesBitSet ());
}, "~N");
$_M(c$, "getFrameDelayMs", 
function (i) {
return this.modelSet.getFrameDelayMs (i);
}, "~N");
$_M(c$, "getBaseModelBitSet", 
function () {
return this.modelSet.getModelAtomBitSetIncludingDeleted (this.getJDXBaseModelIndex (this.getCurrentModelIndex ()), true);
});
$_M(c$, "getTimeouts", 
function () {
return this.timeouts;
});
$_M(c$, "clearTimeouts", 
function () {
if (this.timeouts != null) J.thread.TimeoutThread.clear (this.timeouts);
});
$_M(c$, "setTimeout", 
function (name, mSec, script) {
if (!this.haveDisplay || this.isHeadless () || this.autoExit) return;
if (name == null) {
this.clearTimeouts ();
return;
}if (this.timeouts == null) {
this.timeouts =  new java.util.Hashtable ();
}J.thread.TimeoutThread.setTimeout (this, this.timeouts, name, mSec, script);
}, "~S,~N,~S");
$_M(c$, "triggerTimeout", 
function (name) {
if (!this.haveDisplay || this.timeouts == null) return;
J.thread.TimeoutThread.trigger (this.timeouts, name);
}, "~S");
$_M(c$, "clearTimeout", 
function (name) {
this.setTimeout (name, 0, null);
}, "~S");
$_M(c$, "showTimeout", 
function (name) {
return (this.haveDisplay ? J.thread.TimeoutThread.showTimeout (this.timeouts, name) : "");
}, "~S");
$_M(c$, "calculatePartialCharges", 
function (bsSelected) {
if (bsSelected == null || bsSelected.cardinality () == 0) bsSelected = this.getModelUndeletedAtomsBitSetBs (this.getVisibleFramesBitSet ());
this.getMinimizer (true).calculatePartialCharges (this.modelSet.bonds, this.modelSet.bondCount, this.modelSet.atoms, bsSelected);
}, "J.util.BS");
$_M(c$, "setCurrentModelID", 
function (id) {
var modelIndex = this.getCurrentModelIndex ();
if (modelIndex >= 0) this.modelSet.setModelAuxiliaryInfo (modelIndex, "modelID", id);
}, "~S");
$_M(c$, "setCentroid", 
function (bs, minmax) {
this.modelSet.setCentroid (bs, minmax);
}, "J.util.BS,~A");
$_M(c$, "getPathForAllFiles", 
function () {
return this.fileManager.getPathForAllFiles ();
});
$_M(c$, "cacheGet", 
function (key) {
return this.fileManager.cacheGet (key, false);
}, "~S");
$_M(c$, "cacheClear", 
function () {
this.fileManager.cacheClear ();
});
Clazz.overrideMethod (c$, "cachePut", 
function (key, data) {
J.util.Logger.info ("Viewer cachePut " + key);
this.fileManager.cachePut (key, data);
}, "~S,~O");
Clazz.overrideMethod (c$, "cacheFileByName", 
function (fileName, isAdd) {
return this.fileManager.cacheFileByNameAdd (fileName, isAdd);
}, "~S,~B");
$_M(c$, "cacheList", 
function () {
return this.fileManager.cacheList ();
});
$_M(c$, "clearThreads", 
function () {
if (this.eval != null) this.eval.stopScriptThreads ();
this.stopMinimization ();
this.setVibrationOff ();
this.setSpinOn (false);
this.setNavOn (false);
this.setAnimationOn (false);
});
$_M(c$, "getEvalContextAndHoldQueue", 
function (jse) {
if (jse == null || !this.isJS) return null;
jse.pushContextDown ("getEvalContextAndHoldQueue");
var sc = jse.getThisContext ();
var sc0 = sc;
while (sc0 != null) {
sc0.mustResumeEval = true;
sc0 = sc0.parentContext;
}
sc.isJSThread = true;
this.queueOnHold = true;
return sc;
}, "J.api.JmolScriptEvaluator");
$_M(c$, "checkInheritedShapes", 
function () {
this.shapeManager.checkInheritedShapes ();
});
Clazz.overrideMethod (c$, "resizeInnerPanel", 
function (width, height) {
if (this.autoExit || !this.haveDisplay) {
this.setScreenDimension (width, height);
return this.dimScreen;
}return this.statusManager.resizeInnerPanel (width, height);
}, "~N,~N");
$_M(c$, "getFontLineShapeState", 
function (s, myType, tickInfos) {
return this.getStateCreator ().getFontLineShapeState (s, myType, tickInfos);
}, "~S,~S,~A");
$_M(c$, "getShapeSetState", 
function (atomShape, shape, monomerCount, monomers, bsSizeDefault, temp, temp2) {
this.getStateCreator ().getShapeSetState (atomShape, shape, monomerCount, monomers, bsSizeDefault, temp, temp2);
}, "J.shape.AtomShape,J.shape.Shape,~N,~A,J.util.BS,java.util.Map,java.util.Map");
$_M(c$, "getMeasurementState", 
function (measures, mList, measurementCount, font3d, ti) {
return this.getStateCreator ().getMeasurementState (measures, mList, measurementCount, font3d, ti);
}, "J.shape.Measures,J.util.JmolList,~N,J.util.JmolFont,J.modelset.TickInfo");
$_M(c$, "getBondState", 
function (shape, bsOrderSet, reportAll) {
return this.getStateCreator ().getBondState (shape, bsOrderSet, reportAll);
}, "J.shape.Shape,J.util.BS,~B");
$_M(c$, "getAtomShapeSetState", 
function (shape, shapes) {
return this.getStateCreator ().getAtomShapeSetState (shape, shapes);
}, "J.shape.Shape,~A");
$_M(c$, "getShapeState", 
function (shape) {
return this.getStateCreator ().getShapeState (shape);
}, "J.shape.Shape");
$_M(c$, "getAtomShapeState", 
function (shape) {
return this.getStateCreator ().getAtomShapeState (shape);
}, "J.shape.AtomShape");
$_M(c$, "getDefaultPropertyParam", 
function (propertyID) {
return this.getPropertyManager ().getDefaultPropertyParam (propertyID);
}, "~N");
$_M(c$, "getPropertyNumber", 
function (name) {
return this.getPropertyManager ().getPropertyNumber (name);
}, "~S");
$_M(c$, "checkPropertyParameter", 
function (name) {
return this.getPropertyManager ().checkPropertyParameter (name);
}, "~S");
$_M(c$, "extractProperty", 
function (property, args, pt) {
return this.getPropertyManager ().extractProperty (property, args, pt);
}, "~O,~A,~N");
$_M(c$, "addHydrogens", 
function (bsAtoms, is2DLoad, isSilent) {
var doAll = (bsAtoms == null);
if (bsAtoms == null) bsAtoms = this.getModelUndeletedAtomsBitSet (this.getVisibleFramesBitSet ().length () - 1);
var bsB =  new J.util.BS ();
if (bsAtoms.cardinality () == 0) return bsB;
var modelIndex = this.modelSet.atoms[bsAtoms.nextSetBit (0)].modelIndex;
if (modelIndex != this.modelSet.modelCount - 1) return bsB;
var vConnections =  new J.util.JmolList ();
var pts = this.getAdditionalHydrogens (bsAtoms, doAll, false, vConnections);
var wasAppendNew = false;
wasAppendNew = this.global.appendNew;
if (pts.length > 0) {
this.clearModelDependentObjects ();
try {
bsB = (is2DLoad ? this.modelSet.addHydrogens (vConnections, pts) : this.addHydrogensInline (bsAtoms, vConnections, pts));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println (e.toString ());
} else {
throw e;
}
}
if (wasAppendNew) this.setAppendNew (true);
}if (!isSilent) this.scriptStatus (J.i18n.GT._ ("{0} hydrogens added", pts.length));
return bsB;
}, "J.util.BS,~B,~B");
$_M(c$, "addHydrogensInline", 
($fz = function (bsAtoms, vConnections, pts) {
if (this.getScriptManager () == null) return null;
return this.eval.addHydrogensInline (bsAtoms, vConnections, pts);
}, $fz.isPrivate = true, $fz), "J.util.BS,J.util.JmolList,~A");
Clazz.overrideMethod (c$, "evalFunctionFloat", 
function (func, params, values) {
return (this.getScriptManager () == null ? 0 : this.eval.evalFunctionFloat (func, params, values));
}, "~O,~O,~A");
$_M(c$, "evalParallel", 
function (context, shapeManager) {
this.displayLoadErrors = false;
var isOK = this.getScriptManager () != null && this.eval.evaluateParallel (context, (shapeManager == null ? this.shapeManager : shapeManager));
this.displayLoadErrors = true;
return isOK;
}, "J.script.ScriptContext,J.viewer.ShapeManager");
Clazz.overrideMethod (c$, "evaluateExpression", 
function (stringOrTokens) {
if (this.getScriptManager () == null) return null;
return this.eval.evaluateExpression (stringOrTokens, false);
}, "~O");
$_M(c$, "evaluateExpressionAsVariable", 
function (stringOrTokens) {
if (this.getScriptManager () == null) return null;
return this.eval.evaluateExpression (stringOrTokens, true);
}, "~O");
$_M(c$, "getAtomBitSet", 
function (atomExpression) {
if (Clazz.instanceOf (atomExpression, J.util.BS)) return atomExpression;
this.getScriptManager ();
return this.getAtomBitSetEval (this.eval, atomExpression);
}, "~O");
$_M(c$, "getAtomBitSetVector", 
function (atomExpression) {
if (this.getScriptManager () == null) return null;
return this.eval.getAtomBitSetVector (this.getAtomCount (), atomExpression);
}, "~O");
$_M(c$, "getContextVariables", 
function () {
if (this.getScriptManager () == null) return null;
return this.eval.getContextVariables ();
});
$_M(c$, "getScriptContext", 
function (why) {
return (this.getScriptManager () == null ? null : this.eval.getScriptContext (why));
}, "~S");
Clazz.overrideMethod (c$, "getAtomDefs", 
function (names) {
return this.getStateCreator ().getAtomDefs (names);
}, "java.util.Map");
$_M(c$, "createModels", 
function (n) {
this.modelSet.createModels (n);
}, "~N");
$_M(c$, "setCGO", 
function (info) {
this.shapeManager.loadShape (23);
this.shapeManager.setShapePropertyBs (23, "setCGO", info, null);
}, "J.util.JmolList");
$_M(c$, "setFrame", 
function (i) {
this.animationManager.setFrame (i - 1);
}, "~N");
$_M(c$, "movePyMOL", 
function (eval, floatSecondsTotal, pymolView) {
this.transformManager.moveToPyMOL (eval, floatSecondsTotal, pymolView);
return true;
}, "J.api.JmolScriptEvaluator,~N,~A");
$_M(c$, "getCamera", 
function () {
return this.transformManager.camera;
});
$_M(c$, "setModelSet", 
function (modelSet) {
this.modelSet = this.modelManager.modelSet = modelSet;
}, "J.modelset.ModelSet");
$_M(c$, "setObjectProp", 
function (id, tokCommand) {
this.getScriptManager ();
if (id == null) id = "*";
return (this.eval == null ? null : this.eval.setObjectPropSafe (id, tokCommand, -1));
}, "~S,~N");
$_M(c$, "getSceneList", 
function () {
try {
return this.getModelSetAuxiliaryInfoValue ("scenes");
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
});
$_M(c$, "setBondParameters", 
function (modelIndex, i, bsBonds, rad, pymolValence, argb, trans) {
this.modelSet.setBondParametersBS (modelIndex, i, bsBonds, rad, pymolValence, argb, trans);
}, "~N,~N,J.util.BS,~N,~N,~N,~N");
$_M(c$, "getDihedralMap", 
function (atoms) {
return this.modelSet.getDihedralMap (atoms);
}, "~A");
$_M(c$, "setDihedrals", 
function (dihedralList, bsBranches, rate) {
this.modelSet.setDihedrals (dihedralList, bsBranches, rate);
}, "~A,~A,~N");
$_M(c$, "getBsBranches", 
function (dihedralList) {
return this.modelSet.getBsBranches (dihedralList);
}, "~A");
$_M(c$, "getChainID", 
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
$_M(c$, "getChainIDStr", 
function (id) {
return this.chainMap.get (Integer.$valueOf (id));
}, "~N");
$_M(c$, "getScriptQueueInfo", 
function () {
return (this.scriptManager != null && this.scriptManager.isQueueProcessing () ? Boolean.TRUE : Boolean.FALSE);
});
$_M(c$, "getNMRCalculation", 
function () {
return (this.nmrCalculation == null ? (this.nmrCalculation = J.api.Interface.getOptionInterface ("quantum.NMRCalculation")).setViewer (this) : this.nmrCalculation);
});
$_M(c$, "getDistanceUnits", 
function (s) {
if (s == null) s = this.getDefaultMeasurementLabel (2);
var pt = s.indexOf ("//");
return (pt < 0 ? this.getMeasureDistanceUnits () : s.substring (pt + 2));
}, "~S");
$_M(c$, "calculateFormalCharges", 
function (bs) {
if (bs == null) bs = this.getSelectionSet (false);
return this.modelSet.fixFormalCharges (bs);
}, "J.util.BS");
$_M(c$, "cachePngFiles", 
function () {
return (!this.getTestFlag (1));
});
$_M(c$, "setModulation", 
function (isOn, t1, t2, isThread) {
var t = (t1 == null ? 2147483647 : t1[0]);
if (t2 == 2147483647) {
if (!isThread) this.animationManager.setModulationPlay (2147483647, 0);
if (t1 != null) this.global.setI ("_modt", t1[0]);
this.modelSet.setModulation (this.getSelectionSet (false), isOn, t);
} else {
this.animationManager.setModulationPlay (t, t2);
}this.refreshMeasures (true);
}, "~B,~A,~N,~B");
$_M(c$, "setModulationFps", 
function (fps) {
this.animationManager.setModulationFps (fps);
}, "~N");
$_M(c$, "checkInMotion", 
function (state) {
switch (state) {
case 0:
this.setTimeout ("_SET_IN_MOTION_", 0, null);
break;
case 1:
if (!this.inMotion) this.setTimeout ("_SET_IN_MOTION_", this.global.hoverDelayMs * 2, "!setInMotion");
break;
case 2:
this.setInMotion (true);
this.refresh (3, "timeoutThread set in motion");
break;
}
}, "~N");
$_M(c$, "checkMotionRendering", 
function (tok) {
if (!this.getInMotion (true) && !this.transformManager.spinOn && !this.transformManager.vibrationOn && !this.animationManager.animationOn) return true;
if (this.global.wireframeRotation) return false;
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
return this.global.platformSpeed >= n;
}, "~N");
$_M(c$, "openOutputChannel", 
function (privateKey, fileName, asWriter) {
return (!this.isRestricted (J.viewer.Viewer.ACCESS.ALL) ? null : this.getFileAdapter ().openOutputChannel (privateKey, this.fileManager, fileName, asWriter));
}, "~N,~S,~B");
$_M(c$, "openFileInputStream", 
function (privateKey, fileName) {
return this.getFileAdapter ().openFileInputStream (privateKey, fileName);
}, "~N,~S");
$_M(c$, "getAbsolutePath", 
function (privateKey, fileName) {
return this.getFileAdapter ().getAbsolutePath (privateKey, fileName);
}, "~N,~S");
$_M(c$, "getFileLength", 
function (privateKey, fileName) {
return this.getFileAdapter ().getFileLength (privateKey, fileName);
}, "~N,~S");
$_M(c$, "openLogFile", 
function (privateKey, logFileName, asAppend) {
return this.getFileAdapter ().openLogFile (privateKey, logFileName, asAppend);
}, "~N,~S,~B");
Clazz.overrideMethod (c$, "log", 
function (data) {
if (data != null) this.getStateCreator ().logToFile (data);
}, "~S");
$_M(c$, "getLogFileName", 
function () {
return this.getStateCreator ().getLogFileName ();
});
Clazz.pu$h ();
c$ = Clazz.declareType (J.viewer.Viewer, "ACCESS", Enum);
Clazz.defineEnumConstant (c$, "NONE", 0, []);
Clazz.defineEnumConstant (c$, "READSPT", 1, []);
Clazz.defineEnumConstant (c$, "ALL", 2, []);
c$ = Clazz.p0p ();
Clazz.defineStatics (c$,
"jsDocumentBase", "");
c$.strJavaVendor = c$.prototype.strJavaVendor = System.getProperty ("java.vendor", "j2s");
c$.strOSName = c$.prototype.strOSName = System.getProperty ("os.name", "j2s");
c$.strJavaVersion = c$.prototype.strJavaVersion = System.getProperty ("java.version", "0.0");
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
