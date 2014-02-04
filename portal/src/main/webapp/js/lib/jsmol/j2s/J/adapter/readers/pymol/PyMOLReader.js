Clazz.declarePackage ("J.adapter.readers.pymol");
Clazz.load (["J.adapter.readers.pdb.PdbReader", "J.api.PymolAtomReader", "J.util.BS", "$.P3"], "J.adapter.readers.pymol.PyMOLReader", ["java.lang.Boolean", "java.util.Hashtable", "J.adapter.readers.pymol.PickleReader", "$.PyMOL", "$.PyMOLScene", "J.adapter.smarter.Atom", "$.Bond", "$.Structure", "J.constant.EnumStructure", "J.util.BSUtil", "$.BoxInfo", "$.JmolList", "$.Logger", "$.TextFormat", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.allowSurface = true;
this.doResize = false;
this.doCache = false;
this.isStateScript = false;
this.sourcePNGJ = false;
this.atomCount0 = 0;
this.$atomCount = 0;
this.stateCount = 0;
this.structureCount = 0;
this.isHidden = false;
this.bsStructureDefined = null;
this.bsBytesExcluded = null;
this.atomMap = null;
this.ssMapSeq = null;
this.pymolScene = null;
this.xyzMin = null;
this.xyzMax = null;
this.nModels = 0;
this.logging = false;
this.reps = null;
this.isMovie = false;
this.pymolFrame = 0;
this.allStates = false;
this.totalAtomCount = 0;
this.pymolVersion = 0;
this.trajectoryStep = null;
this.trajectoryPtr = 0;
this.objectName = null;
this.volumeData = null;
this.mapObjects = null;
this.haveMeasurements = false;
this.frames = null;
this.uniqueSettings = null;
this.atoms = null;
this.haveScenes = false;
this.baseAtomIndex = 0;
this.baseModelIndex = 0;
this.sceneOrder = null;
this.bondCount = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.pymol, "PyMOLReader", J.adapter.readers.pdb.PdbReader, J.api.PymolAtomReader);
Clazz.prepareFields (c$, function () {
this.bsStructureDefined =  new J.util.BS ();
this.xyzMin = J.util.P3.new3 (1e6, 1e6, 1e6);
this.xyzMax = J.util.P3.new3 (-1000000.0, -1000000.0, -1000000.0);
this.reps =  new Array (23);
});
Clazz.overrideMethod (c$, "setup", 
function (fullPath, htParams, reader) {
this.isBinary = this.mustFinalizeModelSet = true;
this.setupASCR (fullPath, htParams, reader);
}, "~S,java.util.Map,~O");
$_M(c$, "initializeReader", 
function () {
this.baseAtomIndex = (this.htParams.get ("baseAtomIndex")).intValue ();
this.baseModelIndex = (this.htParams.get ("baseModelIndex")).intValue ();
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("noAutoBond", Boolean.TRUE);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("pdbNoHydrogens", Boolean.TRUE);
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("isPyMOL", Boolean.TRUE);
if (this.isTrajectory) this.trajectorySteps =  new J.util.JmolList ();
this.isStateScript = this.htParams.containsKey ("isStateScript");
this.sourcePNGJ = this.htParams.containsKey ("sourcePNGJ");
this.doResize = this.checkFilterKey ("DORESIZE");
this.allowSurface = !this.checkFilterKey ("NOSURFACE");
this.doCache = this.checkFilterKey ("DOCACHE");
if (this.doCache && this.sourcePNGJ) this.doCache = false;
 else if (this.sourcePNGJ && !this.doCache) this.sourcePNGJ = false;
if (this.doCache) this.bsBytesExcluded =  new J.util.BS ();
this.logging = false;
Clazz.superCall (this, J.adapter.readers.pymol.PyMOLReader, "initializeReader", []);
});
Clazz.overrideMethod (c$, "processBinaryDocument", 
function (doc) {
var reader =  new J.adapter.readers.pymol.PickleReader (doc, this.viewer);
var map = reader.getMap (this.logging);
reader = null;
this.process (map);
}, "J.api.JmolDocument");
Clazz.overrideMethod (c$, "setAdditionalAtomParameters", 
function (atom) {
}, "J.adapter.smarter.Atom");
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
this.finalizeReaderPDB ();
this.atomSetCollection.setTensors ();
});
Clazz.overrideMethod (c$, "finalizeModelSet", 
function () {
this.pymolScene.setReaderObjects ();
if (this.haveMeasurements) {
this.appendLoadNote (this.viewer.getMeasurementInfoAsString ());
this.setLoadNote ();
}if (this.haveScenes) {
var scenes =  new Array (this.sceneOrder.size ());
for (var i = scenes.length; --i >= 0; ) scenes[i] = this.sceneOrder.get (i);

var info = this.viewer.getModelSetAuxiliaryInfo ();
info.put ("scenes", scenes);
}this.viewer.setTrajectoryBs (J.util.BSUtil.newBitSet2 (this.baseModelIndex, this.viewer.modelSet.modelCount));
if (!this.isStateScript) this.pymolScene.setFrameObject (0, null);
if (this.bsBytesExcluded != null) {
var nExcluded = this.bsBytesExcluded.cardinality ();
var bytes0 = this.viewer.getFileAsBytes (this.filePath, null);
var bytes =  Clazz.newByteArray (bytes0.length - nExcluded, 0);
for (var i = this.bsBytesExcluded.nextClearBit (0), n = bytes0.length, pt = 0; i < n; i = this.bsBytesExcluded.nextClearBit (i + 1)) bytes[pt++] = bytes0[i];

bytes0 = null;
var fileName = this.filePath;
this.viewer.cachePut (fileName, bytes);
}});
$_M(c$, "process", 
($fz = function (map) {
this.pymolVersion = (map.get ("version")).intValue ();
this.appendLoadNote ("PyMOL version: " + this.pymolVersion);
var settings = J.adapter.readers.pymol.PyMOLReader.getMapList (map, "settings");
this.sceneOrder = J.adapter.readers.pymol.PyMOLReader.getMapList (map, "scene_order");
this.haveScenes = this.getFrameScenes (map);
var file = this.listAt (settings, 440);
if (file != null) J.util.Logger.info ("PyMOL session file: " + file.get (2));
this.setUniqueSettings (J.adapter.readers.pymol.PyMOLReader.getMapList (map, "unique_settings"));
this.pymolScene =  new J.adapter.readers.pymol.PyMOLScene (this, this.viewer, settings, this.uniqueSettings, this.pymolVersion, this.haveScenes, this.baseAtomIndex, this.baseModelIndex, this.doCache, this.filePath);
this.logging = (this.viewer.getLogFileName ().length > 0);
var names = J.adapter.readers.pymol.PyMOLReader.getMapList (map, "names");
for (var e, $e = map.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var name = e.getKey ();
J.util.Logger.info (name);
if (name.equals ("names")) {
for (var i = 1; i < names.size (); i++) {
var obj = this.listAt (names, i);
J.util.Logger.info ("  " + J.adapter.readers.pymol.PyMOLReader.stringAt (obj, 0));
}
}}
if (this.logging) {
if (this.logging) this.viewer.log ("$CLEAR$");
for (var e, $e = map.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var name = e.getKey ();
if (!"names".equals (name)) {
this.viewer.log ("\n===" + name + "===");
this.viewer.log (J.util.TextFormat.simpleReplace (e.getValue ().toString (), "[", "\n["));
}}
this.viewer.log ("\n===names===");
for (var i = 1; i < names.size (); i++) {
this.viewer.log ("");
var list = names.get (i);
this.viewer.log (" =" + list.get (0).toString () + "=");
try {
this.viewer.log (J.util.TextFormat.simpleReplace (list.toString (), "[", "\n["));
} catch (e) {
}
}
}this.addColors (J.adapter.readers.pymol.PyMOLReader.getMapList (map, "colors"), this.pymolScene.globalSetting (214) != 0);
this.allStates = (this.pymolScene.globalSetting (49) != 0);
this.pymolFrame = Clazz.floatToInt (this.pymolScene.globalSetting (194));
this.getAtomAndStateCount (names);
this.pymolScene.setStateCount (this.stateCount);
var pymolState = Clazz.floatToInt (this.pymolScene.globalSetting (193));
if (!this.isMovie) this.pymolScene.setFrameObject (4115, (this.allStates ? Integer.$valueOf (-1) : Integer.$valueOf (pymolState - 1)));
this.appendLoadNote ("frame=" + this.pymolFrame + " state=" + pymolState + " all_states=" + this.allStates);
if (!this.isStateScript && this.doResize) {
var width = 0;
var height = 0;
try {
width = J.adapter.readers.pymol.PyMOLReader.intAt (J.adapter.readers.pymol.PyMOLReader.getMapList (map, "main"), 0);
height = J.adapter.readers.pymol.PyMOLReader.intAt (J.adapter.readers.pymol.PyMOLReader.getMapList (map, "main"), 1);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
var note;
if (width > 0 && height > 0) {
note = "PyMOL dimensions width=" + width + " height=" + height;
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("perferredWidthHeight", [width, height]);
this.viewer.resizeInnerPanel (width, height);
} else {
note = "PyMOL dimensions?";
}this.appendLoadNote (note);
}var mov;
if (!this.isStateScript && !this.allStates && (mov = J.adapter.readers.pymol.PyMOLReader.getMapList (map, "movie")) != null) {
var frameCount = J.adapter.readers.pymol.PyMOLReader.intAt (mov, 0);
if (frameCount > 0) this.processMovie (mov, frameCount);
}if (this.totalAtomCount == 0) this.atomSetCollection.newAtomSet ();
if (this.allStates && this.desiredModelNumber == -2147483648) {
} else if (this.isMovie) {
switch (this.desiredModelNumber) {
case -2147483648:
break;
default:
this.desiredModelNumber = this.frames[(this.desiredModelNumber > 0 && this.desiredModelNumber <= this.frames.length ? this.desiredModelNumber : this.pymolFrame) - 1];
this.pymolScene.setFrameObject (4115, Integer.$valueOf (this.desiredModelNumber - 1));
break;
}
} else if (this.desiredModelNumber == 0) {
this.desiredModelNumber = pymolState;
} else {
}var n = names.size ();
for (var j = 0; j < this.stateCount; j++) {
if (!this.doGetModel (++this.nModels, null)) continue;
this.model (this.nModels);
this.pymolScene.currentAtomSetIndex = this.atomSetCollection.getCurrentAtomSetIndex ();
if (this.isTrajectory) {
this.trajectoryStep =  new Array (this.totalAtomCount);
this.trajectorySteps.addLast (this.trajectoryStep);
this.trajectoryPtr = 0;
}for (var i = 1; i < n; i++) this.processObject (this.listAt (names, i), true, j);

}
for (var i = 1; i < n; i++) this.processObject (this.listAt (names, i), false, 0);

this.pymolScene.setReaderObjectInfo (null, 0, null, false, null, null, null);
if (this.mapObjects != null && this.allowSurface) this.processMeshes ();
if (this.isTrajectory) {
this.appendLoadNote ("PyMOL trajectories read: " + this.trajectorySteps.size ());
this.atomSetCollection.finalizeTrajectoryAs (this.trajectorySteps, null);
}this.processDefinitions ();
this.processSelectionsAndScenes (map);
this.pymolScene.finalizeVisibility ();
if (!this.isStateScript) {
this.viewer.initialize (true);
this.addJmolScript (this.pymolScene.getViewScript (J.adapter.readers.pymol.PyMOLReader.getMapList (map, "view")).toString ());
}if (this.$atomCount == 0) this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("dataOnly", Boolean.TRUE);
this.pymolScene.offsetObjects ();
}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "getFrameScenes", 
($fz = function (map) {
if (this.sceneOrder == null) return false;
var scenes = map.get ("scene_dict");
for (var i = 0; i < this.sceneOrder.size (); i++) {
var name = J.adapter.readers.pymol.PyMOLReader.stringAt (this.sceneOrder, i);
var thisScene = J.adapter.readers.pymol.PyMOLReader.getMapList (scenes, name);
if (thisScene == null || thisScene.get (2) == null) this.sceneOrder.remove (i--);
}
return (this.sceneOrder != null && this.sceneOrder.size () != 0);
}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "setUniqueSettings", 
($fz = function (list) {
this.uniqueSettings =  new java.util.Hashtable ();
var max = 0;
if (list != null && list.size () != 0) {
for (var i = list.size (); --i >= 0; ) {
var atomSettings = list.get (i);
var id = J.adapter.readers.pymol.PyMOLReader.intAt (atomSettings, 0);
if (id > max) max = id;
var mySettings = atomSettings.get (1);
for (var j = mySettings.size (); --j >= 0; ) {
var setting = mySettings.get (j);
var uid = (id << 10) + J.adapter.readers.pymol.PyMOLReader.intAt (setting, 0);
this.uniqueSettings.put (Integer.$valueOf (uid), setting);
J.util.Logger.info ("PyMOL unique setting " + id + " " + setting);
}
}
}return max;
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "addColors", 
($fz = function (colors, isClamped) {
if (colors == null || colors.size () == 0) return;
for (var i = colors.size (); --i >= 0; ) {
var c = this.listAt (colors, i);
J.adapter.readers.pymol.PyMOL.addColor (c.get (1), isClamped ? J.adapter.readers.pymol.PyMOLScene.colorSettingClamped (c) : J.adapter.readers.pymol.PyMOLScene.colorSetting (c));
}
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~B");
$_M(c$, "getAtomAndStateCount", 
($fz = function (names) {
var n = 0;
for (var i = 1; i < names.size (); i++) {
var execObject = this.listAt (names, i);
var type = J.adapter.readers.pymol.PyMOLReader.intAt (execObject, 4);
if (!this.checkObject (execObject)) continue;
if (type == 1) {
var pymolObject = this.listAt (execObject, 5);
var states = this.listAt (pymolObject, 4);
var ns = states.size ();
if (ns > this.stateCount) this.stateCount = ns;
var nAtoms = this.listAt (pymolObject, 7).size ();
for (var j = 0; j < ns; j++) {
var state = this.listAt (states, j);
var idxToAtm = this.listAt (state, 3);
if (idxToAtm == null) {
this.isTrajectory = false;
} else {
var m = idxToAtm.size ();
n += m;
if (this.isTrajectory && m != nAtoms) this.isTrajectory = false;
}}
}}
this.totalAtomCount = n;
J.util.Logger.info ("PyMOL total atom count = " + this.totalAtomCount);
J.util.Logger.info ("PyMOL state count = " + this.stateCount);
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "checkObject", 
($fz = function (execObject) {
this.objectName = J.adapter.readers.pymol.PyMOLReader.stringAt (execObject, 0);
this.isHidden = (J.adapter.readers.pymol.PyMOLReader.intAt (execObject, 2) != 1);
return (this.objectName.indexOf ("_") != 0);
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "processMovie", 
($fz = function (mov, frameCount) {
var movie =  new java.util.Hashtable ();
movie.put ("frameCount", Integer.$valueOf (frameCount));
movie.put ("currentFrame", Integer.$valueOf (this.pymolFrame - 1));
var haveCommands = false;
var haveViews = false;
var haveFrames = false;
var list = this.listAt (mov, 4);
for (var i = list.size (); --i >= 0; ) if (J.adapter.readers.pymol.PyMOLReader.intAt (list, i) != 0) {
this.frames =  Clazz.newIntArray (list.size (), 0);
for (var j = this.frames.length; --j >= 0; ) this.frames[j] = J.adapter.readers.pymol.PyMOLReader.intAt (list, j) + 1;

movie.put ("frames", this.frames);
haveFrames = true;
break;
}
var cmds = this.listAt (mov, 5);
var cmd;
for (var i = cmds.size (); --i >= 0; ) if ((cmd = J.adapter.readers.pymol.PyMOLReader.stringAt (cmds, i)) != null && cmd.length > 1) {
cmds = J.adapter.readers.pymol.PyMOLReader.fixMovieCommands (cmds);
if (cmds != null) {
movie.put ("commands", cmds);
haveCommands = true;
break;
}}
var views = this.listAt (mov, 6);
var view;
for (var i = views.size (); --i >= 0; ) if ((view = this.listAt (views, i)) != null && view.size () >= 12 && view.get (1) != null) {
haveViews = true;
views = J.adapter.readers.pymol.PyMOLReader.fixMovieViews (views);
if (views != null) {
movie.put ("views", views);
break;
}}
this.appendLoadNote ("PyMOL movie frameCount = " + frameCount);
if (haveFrames && !haveCommands && !haveViews) {
this.isMovie = true;
this.pymolScene.setReaderObjectInfo (null, 0, null, false, null, null, null);
this.pymolScene.setFrameObject (1073742032, movie);
} else {
}}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N");
c$.fixMovieViews = $_M(c$, "fixMovieViews", 
($fz = function (views) {
return views;
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
c$.fixMovieCommands = $_M(c$, "fixMovieCommands", 
($fz = function (cmds) {
return cmds;
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "processObject", 
($fz = function (execObject, moleculeOnly, iState) {
if (execObject == null) return;
var type = J.adapter.readers.pymol.PyMOLReader.intAt (execObject, 4);
var startLen = execObject.get (execObject.size () - 1);
if ((type == 1) != moleculeOnly || !this.checkObject (execObject)) return;
var pymolObject = this.listAt (execObject, 5);
var stateSettings = null;
if (type == 1) {
var states = this.listAt (pymolObject, 4);
var state = this.listAt (states, iState);
var idxToAtm = this.listAt (state, 3);
if (iState > 0 && (idxToAtm == null || idxToAtm.size () == 0)) return;
stateSettings = this.listAt (state, 7);
} else if (iState > 0) {
return;
}J.util.Logger.info ("PyMOL model " + (this.nModels) + " Object " + this.objectName + (this.isHidden ? " (hidden)" : " (visible)"));
var objectHeader = this.listAt (pymolObject, 0);
var parentGroupName = (execObject.size () < 8 ? null : J.adapter.readers.pymol.PyMOLReader.stringAt (execObject, 6));
if (" ".equals (parentGroupName)) parentGroupName = null;
this.pymolScene.setReaderObjectInfo (this.objectName, type, parentGroupName, this.isHidden, this.listAt (objectHeader, 8), stateSettings, (moleculeOnly ? "_" + (iState + 1) : ""));
var bsAtoms = null;
var doExclude = (this.bsBytesExcluded != null);
var msg = null;
switch (type) {
default:
msg = "" + type;
break;
case -1:
break;
case 1:
doExclude = false;
bsAtoms = this.processMolecule (pymolObject, iState);
break;
case 4:
doExclude = false;
this.processMeasure (pymolObject);
break;
case 3:
case 2:
this.processMap (pymolObject, type == 3, false);
break;
case 8:
this.processGadget (pymolObject);
break;
case 12:
if (parentGroupName == null) parentGroupName = "";
break;
case 6:
msg = "CGO";
this.processCGO (pymolObject);
break;
case 11:
msg = "ALIGNEMENT";
break;
case 9:
msg = "CALCULATOR";
break;
case 5:
msg = "CALLBACK";
break;
case 10:
msg = "SLICE";
break;
case 7:
msg = "SURFACE";
break;
}
if (parentGroupName != null || bsAtoms != null) {
var group = this.pymolScene.addGroup (execObject, parentGroupName, type);
if (bsAtoms != null) bsAtoms = group.addGroupAtoms (bsAtoms);
}if (doExclude) {
var i0 = J.adapter.readers.pymol.PyMOLReader.intAt (startLen, 0);
var len = J.adapter.readers.pymol.PyMOLReader.intAt (startLen, 1);
this.bsBytesExcluded.setBits (i0, i0 + len);
J.util.Logger.info ("cached PSE file excludes PyMOL object type " + type + " name=" + this.objectName + " len=" + len);
}if (msg != null) J.util.Logger.error ("Unprocessed object type " + msg + " " + this.objectName);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~B,~N");
$_M(c$, "processCGO", 
($fz = function (pymolObject) {
if (this.isStateScript) return;
if (this.isHidden) return;
var data = this.listAt (this.listAt (pymolObject, 2), 0);
var color = J.adapter.readers.pymol.PyMOL.getRGB (J.adapter.readers.pymol.PyMOLReader.intAt (this.listAt (pymolObject, 0), 2));
var name = this.pymolScene.addCGO (data, color);
if (name != null) this.appendLoadNote ("CGO " + name);
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "processGadget", 
($fz = function (pymolObject) {
if (this.objectName.endsWith ("_e_pot")) this.processMap (pymolObject, true, true);
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "processMap", 
($fz = function (pymolObject, isObject, isGadget) {
if (isObject) {
if (this.sourcePNGJ) return;
if (this.isHidden && !isGadget) return;
if (this.mapObjects == null) this.mapObjects =  new J.util.JmolList ();
this.mapObjects.addLast (pymolObject);
} else {
if (this.volumeData == null) this.volumeData =  new java.util.Hashtable ();
this.volumeData.put (this.objectName, pymolObject);
if (!this.isHidden && !this.isStateScript) this.pymolScene.addIsosurface (this.objectName);
}pymolObject.addLast (this.objectName);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~B,~B");
$_M(c$, "processMeasure", 
($fz = function (pymolObject) {
if (this.isStateScript) return;
if (this.isHidden) return;
J.util.Logger.info ("PyMOL measure " + this.objectName);
var measure = this.listAt (this.listAt (pymolObject, 2), 0);
var pt;
var nCoord = (Clazz.instanceOf (measure.get (pt = 1), J.util.JmolList) ? 2 : Clazz.instanceOf (measure.get (pt = 4), J.util.JmolList) ? 3 : Clazz.instanceOf (measure.get (pt = 6), J.util.JmolList) ? 4 : 0);
if (nCoord == 0) return;
var setting = this.listAt (pymolObject, 0);
var bsReps = J.adapter.readers.pymol.PyMOLReader.getBsReps (this.listAt (setting, 3));
var list = this.listAt (measure, pt);
var offsets = this.listAt (measure, 8);
var haveLabels = (measure.size () > 8);
var color = J.adapter.readers.pymol.PyMOLReader.intAt (setting, 2);
if (this.pymolScene.addMeasurements (null, nCoord, list, bsReps, color, offsets, haveLabels)) this.haveMeasurements = true;
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "processMolecule", 
($fz = function (pymolObject, iState) {
var states = this.listAt (pymolObject, 4);
var state = this.listAt (states, iState);
var idxToAtm = this.listAt (state, 3);
var n = (idxToAtm == null ? 0 : idxToAtm.size ());
if (n == 0) return null;
this.$atomCount = this.atomCount0 = this.atomSetCollection.getAtomCount ();
var nAtoms = J.adapter.readers.pymol.PyMOLReader.intAt (pymolObject, 3);
if (nAtoms == 0) return null;
this.ssMapSeq =  new java.util.Hashtable ();
if (iState == 0) this.processMolCryst (this.listAt (pymolObject, 10));
var bonds = this.getBondList (this.listAt (pymolObject, 6));
var pymolAtoms = this.listAt (pymolObject, 7);
this.atomMap =  Clazz.newIntArray (nAtoms, 0);
var bsAtoms = this.pymolScene.setAtomMap (this.atomMap, this.atomCount0);
for (var i = 0; i < 23; i++) this.reps[i] = J.util.BS.newN (1000);

var coords = this.listAt (state, 2);
var labelPositions = this.listAt (state, 8);
if (iState == 0 || !this.isTrajectory) for (var idx = 0; idx < n; idx++) {
var a = this.addAtom (pymolAtoms, J.adapter.readers.pymol.PyMOLReader.intAt (idxToAtm, idx), idx, coords, labelPositions, bsAtoms, iState);
if (a != null) this.trajectoryStep[this.trajectoryPtr++] = a;
}
this.addBonds (bonds);
this.addMolStructures ();
this.atoms = this.atomSetCollection.getAtoms ();
if (!this.isStateScript) this.createShapeObjects ();
this.ssMapSeq = null;
J.util.Logger.info ("reading " + (this.$atomCount - this.atomCount0) + " atoms");
J.util.Logger.info ("----------");
return bsAtoms;
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N");
$_M(c$, "processMolCryst", 
($fz = function (cryst) {
if (cryst == null || cryst.size () == 0) return;
var l = this.listAt (this.listAt (cryst, 0), 0);
var a = this.listAt (this.listAt (cryst, 0), 1);
this.setUnitCell (this.floatAt (l, 0), this.floatAt (l, 1), this.floatAt (l, 2), this.floatAt (a, 0), this.floatAt (a, 1), this.floatAt (a, 2));
this.setSpaceGroupName (J.adapter.readers.pymol.PyMOLReader.stringAt (cryst, 1));
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "getBondList", 
($fz = function (bonds) {
var bondList =  new J.util.JmolList ();
var asSingle = (this.pymolScene.booleanSetting (64) ? 0 : 65536);
var n = bonds.size ();
for (var i = 0; i < n; i++) {
var b = this.listAt (bonds, i);
var order = J.adapter.readers.pymol.PyMOLReader.intAt (b, 2);
if (order < 1 || order > 3) order = 1;
var ia = J.adapter.readers.pymol.PyMOLReader.intAt (b, 0);
var ib = J.adapter.readers.pymol.PyMOLReader.intAt (b, 1);
var bond =  new J.adapter.smarter.Bond (ia, ib, order | asSingle);
bond.uniqueID = (b.size () > 6 && J.adapter.readers.pymol.PyMOLReader.intAt (b, 6) != 0 ? J.adapter.readers.pymol.PyMOLReader.intAt (b, 5) : -1);
bondList.addLast (bond);
}
return bondList;
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "addAtom", 
($fz = function (pymolAtoms, apt, icoord, coords, labelPositions, bsState, iState) {
this.atomMap[apt] = -1;
var a = this.listAt (pymolAtoms, apt);
var seqNo = J.adapter.readers.pymol.PyMOLReader.intAt (a, 0);
var chainID = J.adapter.readers.pymol.PyMOLReader.stringAt (a, 1);
var altLoc = J.adapter.readers.pymol.PyMOLReader.stringAt (a, 2);
var insCode = " ";
var name = J.adapter.readers.pymol.PyMOLReader.stringAt (a, 6);
var group3 = J.adapter.readers.pymol.PyMOLReader.stringAt (a, 5);
if (group3.length > 3) group3 = group3.substring (0, 3);
if (group3.equals (" ")) group3 = "UNK";
var sym = J.adapter.readers.pymol.PyMOLReader.stringAt (a, 7);
if (sym.equals ("A")) sym = "C";
var isHetero = (J.adapter.readers.pymol.PyMOLReader.intAt (a, 19) != 0);
var ichain = this.viewer.getChainID (chainID);
var atom = this.processAtom ( new J.adapter.smarter.Atom (), name, altLoc.charAt (0), group3, ichain, seqNo, insCode.charAt (0), isHetero, sym);
if (!this.filterPDBAtom (atom, this.fileAtomIndex++)) return null;
icoord *= 3;
var x = this.floatAt (coords, icoord);
var y = this.floatAt (coords, ++icoord);
var z = this.floatAt (coords, ++icoord);
J.util.BoxInfo.addPointXYZ (x, y, z, this.xyzMin, this.xyzMax, 0);
if (this.isTrajectory && iState > 0) return null;
var isNucleic = (J.adapter.readers.pymol.PyMOLReader.nucleic.indexOf (group3) >= 0);
if (bsState != null) bsState.set (this.$atomCount);
var label = J.adapter.readers.pymol.PyMOLReader.stringAt (a, 9);
var ssType = J.adapter.readers.pymol.PyMOLReader.stringAt (a, 10);
if (seqNo >= -1000 && (!ssType.equals (" ") || name.equals ("CA") || isNucleic)) {
var bs = this.ssMapSeq.get (ssType);
if (bs == null) this.ssMapSeq.put (ssType, bs =  new J.util.BS ());
bs.set (seqNo - -1000);
ssType += ichain;
bs = this.ssMapSeq.get (ssType);
if (bs == null) this.ssMapSeq.put (ssType, bs =  new J.util.BS ());
bs.set (seqNo - -1000);
}atom.bfactor = this.floatAt (a, 14);
atom.foccupancy = this.floatAt (a, 15);
atom.radius = this.floatAt (a, 16);
if (atom.radius == 0) atom.radius = 1;
atom.partialCharge = this.floatAt (a, 17);
var formalCharge = J.adapter.readers.pymol.PyMOLReader.intAt (a, 18);
var bsReps = J.adapter.readers.pymol.PyMOLReader.getBsReps (this.listAt (a, 20));
var atomColor = J.adapter.readers.pymol.PyMOLReader.intAt (a, 21);
var serNo = J.adapter.readers.pymol.PyMOLReader.intAt (a, 22);
var cartoonType = J.adapter.readers.pymol.PyMOLReader.intAt (a, 23);
var flags = J.adapter.readers.pymol.PyMOLReader.intAt (a, 24);
var bonded = (J.adapter.readers.pymol.PyMOLReader.intAt (a, 25) != 0);
var uniqueID = (a.size () > 40 && J.adapter.readers.pymol.PyMOLReader.intAt (a, 40) == 1 ? J.adapter.readers.pymol.PyMOLReader.intAt (a, 32) : -1);
atom.vib = J.util.V3.new3 (uniqueID, cartoonType, NaN);
if (a.size () > 46) {
var data = J.adapter.readers.pymol.PyMOLScene.floatsAt (a, 41,  Clazz.newFloatArray (8, 0), 6);
this.atomSetCollection.setAnisoBorU (atom, data, 12);
}this.pymolScene.setAtomColor (atomColor);
this.processAtom2 (atom, serNo, x, y, z, formalCharge);
if (!bonded) this.pymolScene.bsNonbonded.set (this.$atomCount);
if (!label.equals (" ")) {
this.pymolScene.bsLabeled.set (this.$atomCount);
var labelOffset = this.listAt (labelPositions, apt);
this.pymolScene.addLabel (this.$atomCount, uniqueID, atomColor, labelOffset, label);
}if (this.isHidden) this.pymolScene.bsHidden.set (this.$atomCount);
if (isNucleic) this.pymolScene.bsNucleic.set (this.$atomCount);
for (var i = 0; i < 21; i++) if (bsReps.get (i)) this.reps[i].set (this.$atomCount);

if (atom.elementSymbol.equals ("H")) this.pymolScene.bsHydrogen.set (this.$atomCount);
if ((flags & J.adapter.readers.pymol.PyMOL.FLAG_NOSURFACE) != 0) this.pymolScene.bsNoSurface.set (this.$atomCount);
this.atomMap[apt] = this.$atomCount++;
return null;
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N,~N,J.util.JmolList,J.util.JmolList,J.util.BS,~N");
$_M(c$, "addBonds", 
($fz = function (bonds) {
var n = bonds.size ();
for (var i = 0; i < n; i++) {
var bond = bonds.get (i);
bond.atomIndex1 = this.atomMap[bond.atomIndex1];
bond.atomIndex2 = this.atomMap[bond.atomIndex2];
if (bond.atomIndex1 < 0 || bond.atomIndex2 < 0) continue;
this.pymolScene.setUniqueBond (this.bondCount++, bond.uniqueID);
this.atomSetCollection.addBond (bond);
}
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "addMolStructures", 
($fz = function () {
this.addMolSS ("H", J.constant.EnumStructure.HELIX);
this.addMolSS ("S", J.constant.EnumStructure.SHEET);
this.addMolSS ("L", J.constant.EnumStructure.TURN);
this.addMolSS (" ", J.constant.EnumStructure.NONE);
}, $fz.isPrivate = true, $fz));
$_M(c$, "addMolSS", 
($fz = function (ssType, type) {
if (this.ssMapSeq.get (ssType) == null) return;
var istart = -1;
var iend = -1;
var ichain = 0;
var atoms = this.atomSetCollection.getAtoms ();
var bsSeq = null;
var bsAtom = this.pymolScene.getSSMapAtom (ssType);
var n = this.$atomCount + 1;
var seqNo = -1;
var thischain = 0;
var imodel = -1;
var thisModel = -1;
for (var i = this.atomCount0; i < n; i++) {
if (i == this.$atomCount) {
thischain = 0;
} else {
seqNo = atoms[i].sequenceNumber;
thischain = atoms[i].chainID;
thisModel = atoms[i].atomSetIndex;
}if (thischain != ichain || thisModel != imodel) {
ichain = thischain;
imodel = thisModel;
bsSeq = this.ssMapSeq.get (ssType + thischain);
--i;
if (istart < 0) continue;
} else if (bsSeq != null && seqNo >= -1000 && bsSeq.get (seqNo - -1000)) {
iend = i;
if (istart < 0) istart = i;
continue;
} else if (istart < 0) {
continue;
}if (type !== J.constant.EnumStructure.NONE) {
var pt = this.bsStructureDefined.nextSetBit (istart);
if (pt >= 0 && pt <= iend) continue;
this.bsStructureDefined.setBits (istart, iend + 1);
var structure =  new J.adapter.smarter.Structure (imodel, type, type, type.toString (), ++this.structureCount, type === J.constant.EnumStructure.SHEET ? 1 : 0);
var a = atoms[istart];
var b = atoms[iend];
var i0 = this.atomSetCollection.getAtomSetAtomIndex (thisModel);
structure.set (a.chainID, a.sequenceNumber, a.insertionCode, b.chainID, b.sequenceNumber, b.insertionCode, istart - i0, iend - i0);
this.atomSetCollection.addStructure (structure);
}bsAtom.setBits (istart, iend + 1);
istart = -1;
}
}, $fz.isPrivate = true, $fz), "~S,J.constant.EnumStructure");
$_M(c$, "createShapeObjects", 
($fz = function () {
this.pymolScene.createShapeObjects (this.reps, this.allowSurface && !this.isHidden, this.atomCount0, this.$atomCount);
}, $fz.isPrivate = true, $fz));
$_M(c$, "processMeshes", 
($fz = function () {
this.viewer.cachePut (this.pymolScene.surfaceInfoName, this.volumeData);
for (var i = this.mapObjects.size (); --i >= 0; ) {
var obj = this.mapObjects.get (i);
var objName = obj.get (obj.size () - 1).toString ();
var isMep = objName.endsWith ("_e_pot");
var mapName;
var tok;
if (isMep) {
tok = 1073742016;
var root = objName.substring (0, objName.length - 3);
mapName = root + "map";
var isosurfaceName = this.pymolScene.getObjectID (root + "chg");
if (isosurfaceName == null) continue;
obj.addLast (isosurfaceName);
this.pymolScene.mepList += ";" + isosurfaceName + ";";
} else {
tok = 1073742018;
mapName = J.adapter.readers.pymol.PyMOLReader.stringAt (this.listAt (this.listAt (obj, 2), 0), 1);
}var surface = this.volumeData.get (mapName);
if (surface == null) continue;
obj.addLast (mapName);
this.volumeData.put (objName, obj);
this.volumeData.put ("__pymolSurfaceData__", obj);
if (!this.isStateScript) this.pymolScene.addMesh (tok, obj, objName, isMep);
this.appendLoadNote ("PyMOL object " + objName + " references map " + mapName);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "processDefinitions", 
($fz = function () {
var s = this.viewer.getAtomDefs (this.pymolScene.setAtomDefs ());
if (s.length > 2) s = s.substring (0, s.length - 2);
this.appendLoadNote (s);
}, $fz.isPrivate = true, $fz));
$_M(c$, "processSelectionsAndScenes", 
($fz = function (map) {
if (!this.pymolScene.needSelections ()) return;
var htObjNames = J.adapter.readers.pymol.PyMOLScene.listToMap (J.adapter.readers.pymol.PyMOLReader.getMapList (map, "names"));
if (this.haveScenes) {
var scenes = map.get ("scene_dict");
this.finalizeSceneData ();
var htSecrets = J.adapter.readers.pymol.PyMOLScene.listToMap (J.adapter.readers.pymol.PyMOLReader.getMapList (map, "selector_secrets"));
for (var i = 0; i < this.sceneOrder.size (); i++) {
var name = J.adapter.readers.pymol.PyMOLReader.stringAt (this.sceneOrder, i);
var thisScene = J.adapter.readers.pymol.PyMOLReader.getMapList (scenes, name);
if (thisScene == null) continue;
this.pymolScene.buildScene (name, thisScene, htObjNames, htSecrets);
this.appendLoadNote ("scene: " + name);
}
}this.pymolScene.setCarveSets (htObjNames);
}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "finalizeSceneData", 
($fz = function () {
var cartoonTypes =  Clazz.newIntArray (this.$atomCount, 0);
var uniqueIDs =  Clazz.newIntArray (this.$atomCount, 0);
var sequenceNumbers =  Clazz.newIntArray (this.$atomCount, 0);
var newChain =  Clazz.newBooleanArray (this.$atomCount, false);
var radii =  Clazz.newFloatArray (this.$atomCount, 0);
var lastAtomChain = -2147483648;
var lastAtomSet = -2147483648;
for (var i = 0; i < this.$atomCount; i++) {
cartoonTypes[i] = this.getCartoonType (i);
uniqueIDs[i] = this.getUniqueID (i);
sequenceNumbers[i] = this.getSequenceNumber (i);
radii[i] = this.getVDW (i);
if (lastAtomChain != this.atoms[i].chainID || lastAtomSet != this.atoms[i].atomSetIndex) {
newChain[i] = true;
lastAtomChain = this.atoms[i].chainID;
lastAtomSet = this.atoms[i].atomSetIndex;
}}
this.pymolScene.setAtomInfo (uniqueIDs, cartoonTypes, sequenceNumbers, newChain, radii);
}, $fz.isPrivate = true, $fz));
c$.intAt = $_M(c$, "intAt", 
($fz = function (list, i) {
return (list.get (i)).intValue ();
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N");
c$.stringAt = $_M(c$, "stringAt", 
($fz = function (list, i) {
var s = list.get (i).toString ();
return (s.length == 0 ? " " : s);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N");
c$.getMapList = $_M(c$, "getMapList", 
($fz = function (map, key) {
return map.get (key);
}, $fz.isPrivate = true, $fz), "java.util.Map,~S");
c$.getBsReps = $_M(c$, "getBsReps", 
($fz = function (list) {
var bsReps =  new J.util.BS ();
var n = Math.min (list.size (), 21);
for (var i = 0; i < n; i++) {
if (J.adapter.readers.pymol.PyMOLReader.intAt (list, i) == 1) bsReps.set (i);
}
return bsReps;
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "floatAt", 
($fz = function (a, i) {
return J.adapter.readers.pymol.PyMOLScene.floatAt (a, i);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N");
$_M(c$, "listAt", 
($fz = function (list, i) {
return J.adapter.readers.pymol.PyMOLScene.listAt (list, i);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N");
Clazz.overrideMethod (c$, "getUniqueID", 
function (iAtom) {
return Clazz.floatToInt (this.atoms[iAtom].vib.x);
}, "~N");
Clazz.overrideMethod (c$, "getCartoonType", 
function (iAtom) {
return Clazz.floatToInt (this.atoms[iAtom].vib.y);
}, "~N");
Clazz.overrideMethod (c$, "getVDW", 
function (iAtom) {
return this.atoms[iAtom].radius;
}, "~N");
Clazz.overrideMethod (c$, "getSequenceNumber", 
function (iAtom) {
return this.atoms[iAtom].sequenceNumber;
}, "~N");
Clazz.overrideMethod (c$, "compareAtoms", 
function (iPrev, i) {
return this.atoms[iPrev].chainID != this.atoms[i].chainID;
}, "~N,~N");
Clazz.defineStatics (c$,
"MIN_RESNO", -1000,
"nucleic", " A C G T U ADE THY CYT GUA URI DA DC DG DT DU ");
});
