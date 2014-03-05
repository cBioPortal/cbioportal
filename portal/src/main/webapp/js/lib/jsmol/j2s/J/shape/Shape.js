Clazz.declarePackage ("J.shape");
Clazz.load (null, "J.shape.Shape", ["J.constant.EnumPalette", "J.util.C", "$.Logger", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.modelSet = null;
this.gdata = null;
this.shapeID = 0;
this.myVisibilityFlag = 0;
this.translucentLevel = 0;
this.translucentAllowed = true;
this.isBioShape = false;
this.bsSizeSet = null;
this.bsColixSet = null;
Clazz.instantialize (this, arguments);
}, J.shape, "Shape");
$_M(c$, "getViewer", 
function () {
return this.viewer;
});
$_M(c$, "initializeShape", 
function (viewer, g3d, modelSet, shapeID) {
this.viewer = viewer;
this.gdata = g3d;
this.shapeID = shapeID;
this.myVisibilityFlag = J.viewer.JC.getShapeVisibilityFlag (shapeID);
this.setModelSet (modelSet);
this.initShape ();
}, "J.viewer.Viewer,J.util.GData,J.modelset.ModelSet,~N");
$_M(c$, "getSize", 
function (atomIndex) {
return 0;
}, "~N");
$_M(c$, "getSizeG", 
function (group) {
return 0;
}, "J.modelset.Group");
$_M(c$, "setModelSet", 
function (modelSet) {
this.modelSet = modelSet;
this.initModelSet ();
}, "J.modelset.ModelSet");
$_M(c$, "initModelSet", 
function () {
});
$_M(c$, "initShape", 
function () {
});
$_M(c$, "merge", 
function (shape) {
}, "J.shape.Shape");
$_M(c$, "setShapeSizeRD", 
function (size, rd, bsSelected) {
if (rd == null) this.setSize (size, bsSelected);
 else this.setSizeRD (rd, bsSelected);
}, "~N,J.atomdata.RadiusData,J.util.BS");
$_M(c$, "setSize", 
function (size, bsSelected) {
}, "~N,J.util.BS");
$_M(c$, "setSizeRD", 
function (rd, bsSelected) {
}, "J.atomdata.RadiusData,J.util.BS");
$_M(c$, "getPropertyData", 
function (property, data) {
return false;
}, "~S,~A");
$_M(c$, "setPropS", 
function (propertyName, value, bsSelected) {
if (propertyName === "setProperties") {
if (bsSelected == null) bsSelected = this.viewer.getSelectionSet (false);
var propertyList = value;
while (propertyList.size () > 0) {
var data = propertyList.remove (0);
this.setProperty ((data[0]).intern (), data[1], bsSelected);
}
return;
}if (propertyName === "translucentLevel") {
this.translucentLevel = (value).floatValue ();
return;
}if (propertyName === "refreshTrajectories") {
return;
}J.util.Logger.warn ("unassigned " + J.viewer.JC.shapeClassBases[this.shapeID] + " + shape setProperty:" + propertyName + ":" + value);
}, "~S,~O,J.util.BS");
$_M(c$, "getProperty", 
function (property, index) {
return null;
}, "~S,~N");
$_M(c$, "getIndexFromName", 
function (thisID) {
return -1;
}, "~S");
$_M(c$, "wasClicked", 
function (x, y) {
return false;
}, "~N,~N");
$_M(c$, "findNearestAtomIndex", 
function (xMouse, yMouse, closest, bsNot) {
}, "~N,~N,~A,J.util.BS");
$_M(c$, "checkBoundsMinMax", 
function (pointMin, pointMax) {
}, "J.util.P3,J.util.P3");
$_M(c$, "setModelClickability", 
function () {
});
$_M(c$, "checkObjectClicked", 
function (x, y, modifiers, bsVisible, drawPicking) {
return null;
}, "~N,~N,~N,J.util.BS,~B");
$_M(c$, "checkObjectHovered", 
function (x, y, bsVisible) {
return false;
}, "~N,~N,J.util.BS");
$_M(c$, "checkObjectDragged", 
function (prevX, prevY, x, y, dragAction, bsVisible) {
return false;
}, "~N,~N,~N,~N,~N,J.util.BS");
$_M(c$, "coordinateInRange", 
function (x, y, vertex, dmin2, ptXY) {
this.viewer.transformPtScr (vertex, ptXY);
var d2 = (x - ptXY.x) * (x - ptXY.x) + (y - ptXY.y) * (y - ptXY.y);
return (d2 < dmin2 ? d2 : -1);
}, "~N,~N,J.util.P3,~N,J.util.P3i");
$_M(c$, "getColixI", 
function (colix, paletteID, atomIndex) {
return this.getColixA (colix, paletteID, this.modelSet.atoms[atomIndex]);
}, "~N,~N,~N");
$_M(c$, "getColixA", 
function (colix, paletteID, atom) {
return (colix == 2 ? this.viewer.getColixAtomPalette (atom, paletteID) : colix);
}, "~N,~N,J.modelset.Atom");
$_M(c$, "getColixB", 
function (colix, pid, bond) {
return (colix == 2 ? this.viewer.getColixBondPalette (bond, pid) : colix);
}, "~N,~N,J.modelset.Bond");
$_M(c$, "getShapeDetail", 
function () {
return null;
});
$_M(c$, "setVisibilityFlags", 
function (bs) {
}, "J.util.BS");
c$.getColix = $_M(c$, "getColix", 
function (colixes, i, atom) {
return J.util.C.getColixInherited ((colixes == null || i >= colixes.length ? 0 : colixes[i]), atom.getColix ());
}, "~A,~N,J.modelset.Atom");
c$.getFontCommand = $_M(c$, "getFontCommand", 
function (type, font) {
if (font == null) return "";
return "font " + type + " " + font.getInfo ();
}, "~S,J.util.JmolFont");
c$.getColorCommandUnk = $_M(c$, "getColorCommandUnk", 
function (type, colix, translucentAllowed) {
return J.shape.Shape.getColorCommand (type, J.constant.EnumPalette.UNKNOWN.id, colix, translucentAllowed);
}, "~S,~N,~B");
c$.getColorCommand = $_M(c$, "getColorCommand", 
function (type, pid, colix, translucentAllowed) {
if (pid == J.constant.EnumPalette.UNKNOWN.id && colix == 0) return "";
var s = (pid == J.constant.EnumPalette.UNKNOWN.id && colix == 0 ? "" : (translucentAllowed ? J.shape.Shape.getTranslucentLabel (colix) + " " : "") + (pid != J.constant.EnumPalette.UNKNOWN.id && !J.constant.EnumPalette.isPaletteVariable (pid) ? J.constant.EnumPalette.getPaletteName (pid) : J.shape.Shape.encodeColor (colix)));
return "color " + type + " " + s;
}, "~S,~N,~N,~B");
c$.encodeColor = $_M(c$, "encodeColor", 
function (colix) {
return (J.util.C.isColixColorInherited (colix) ? "none" : J.util.C.getHexCode (colix));
}, "~N");
c$.getTranslucentLabel = $_M(c$, "getTranslucentLabel", 
function (colix) {
return (J.util.C.isColixTranslucent (colix) ? "translucent " + J.util.C.getColixTranslucencyFractional (colix) : "opaque");
}, "~N");
c$.appendCmd = $_M(c$, "appendCmd", 
function (s, cmd) {
if (cmd.length == 0) return;
s.append ("  ").append (cmd).append (";\n");
}, "J.util.SB,~S");
Clazz.defineStatics (c$,
"RADIUS_MAX", 4);
});
