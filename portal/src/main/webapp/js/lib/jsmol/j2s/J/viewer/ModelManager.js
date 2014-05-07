Clazz.declarePackage ("J.viewer");
Clazz.load (null, "J.viewer.ModelManager", ["J.modelset.ModelLoader"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.modelSet = null;
this.modelSetPathName = null;
this.fileName = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "ModelManager");
Clazz.makeConstructor (c$, 
function (viewer) {
this.viewer = viewer;
}, "J.viewer.Viewer");
$_M(c$, "zap", 
function () {
this.modelSetPathName = this.fileName = null;
 new J.modelset.ModelLoader (this.viewer, this.viewer.getZapName (), null, null, null, null);
});
$_M(c$, "getModelSetFileName", 
function () {
return (this.fileName != null ? this.fileName : this.viewer.getZapName ());
});
$_M(c$, "getModelSetPathName", 
function () {
return this.modelSetPathName;
});
$_M(c$, "createModelSet", 
function (fullPathName, fileName, loadScript, atomSetCollection, bsNew, isAppend) {
var modelSetName = null;
if (isAppend) {
modelSetName = this.modelSet.modelSetName;
if (modelSetName.equals ("zapped")) modelSetName = null;
 else if (modelSetName.indexOf (" (modified)") < 0) modelSetName += " (modified)";
} else if (atomSetCollection == null) {
this.zap ();
} else {
this.modelSetPathName = fullPathName;
this.fileName = fileName;
}if (atomSetCollection != null) {
if (modelSetName == null) {
modelSetName = this.viewer.getModelAdapter ().getAtomSetCollectionName (atomSetCollection);
if (modelSetName != null) {
modelSetName = modelSetName.trim ();
if (modelSetName.length == 0) modelSetName = null;
}if (modelSetName == null) modelSetName = J.viewer.ModelManager.reduceFilename (fileName);
} new J.modelset.ModelLoader (this.viewer, modelSetName, loadScript, atomSetCollection, (isAppend ? this.modelSet : null), bsNew);
}if (this.modelSet.getAtomCount () == 0 && !this.modelSet.getModelSetAuxiliaryInfoBoolean ("isPyMOL")) this.zap ();
}, "~S,~S,J.util.SB,~O,J.util.BS,~B");
c$.reduceFilename = $_M(c$, "reduceFilename", 
($fz = function (fileName) {
if (fileName == null) return null;
var ichDot = fileName.indexOf ('.');
if (ichDot > 0) fileName = fileName.substring (0, ichDot);
if (fileName.length > 24) fileName = fileName.substring (0, 20) + " ...";
return fileName;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "createAtomDataSet", 
function (atomSetCollection, tokType) {
J.modelset.ModelLoader.createAtomDataSet (this.viewer, this.modelSet, tokType, atomSetCollection, this.viewer.getSelectionSet (false));
}, "~O,~N");
});
