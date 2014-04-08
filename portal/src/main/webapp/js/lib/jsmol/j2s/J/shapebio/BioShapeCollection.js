Clazz.declarePackage ("J.shapebio");
Clazz.load (["J.shape.Shape"], "J.shapebio.BioShapeCollection", ["J.constant.EnumPalette", "J.shapebio.BioShape", "J.util.ArrayUtil", "$.BSUtil", "$.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atoms = null;
this.madOn = -2;
this.madHelixSheet = 3000;
this.madTurnRandom = 800;
this.madDnaRna = 5000;
this.isActive = false;
this.bioShapes = null;
Clazz.instantialize (this, arguments);
}, J.shapebio, "BioShapeCollection", J.shape.Shape);
Clazz.overrideMethod (c$, "initModelSet", 
function () {
this.isBioShape = true;
this.atoms = this.modelSet.atoms;
this.initialize ();
});
Clazz.overrideMethod (c$, "getSizeG", 
function (group) {
var m = group;
var groupIndex = m.getGroupIndex ();
var leadAtomIndex = m.getLeadAtom ().getIndex ();
for (var i = this.bioShapes.length; --i >= 0; ) {
var bioShape = this.bioShapes[i];
for (var j = 0; j < bioShape.monomerCount; j++) {
if (bioShape.monomers[j].getGroupIndex () == groupIndex && bioShape.monomers[j].getLeadAtom ().getIndex () == leadAtomIndex) return bioShape.mads[j];
}
}
return 0;
}, "J.modelset.Group");
Clazz.overrideMethod (c$, "setShapeSizeRD", 
function (size, rd, bsSelected) {
var mad = size;
this.initialize ();
for (var i = this.bioShapes.length; --i >= 0; ) {
var bioShape = this.bioShapes[i];
if (bioShape.monomerCount > 0) bioShape.setMad (mad, bsSelected, (rd == null ? null : rd.values));
}
}, "~N,J.atomdata.RadiusData,J.util.BS");
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bsSelected) {
this.setPropBSC (propertyName, value, bsSelected);
}, "~S,~O,J.util.BS");
$_M(c$, "setPropBSC", 
function (propertyName, value, bsSelected) {
if (propertyName === "refreshTrajectories") {
var modelIndex = ((value)[0]).intValue ();
for (var i = this.bioShapes.length; --i >= 0; ) {
var b = this.bioShapes[i];
if (b.modelIndex == modelIndex) b.falsifyMesh ();
}
return;
}if (propertyName === "deleteModelAtoms") {
this.atoms = (value)[1];
var modelIndex = ((value)[2])[0];
for (var i = this.bioShapes.length; --i >= 0; ) {
var b = this.bioShapes[i];
if (b.modelIndex > modelIndex) {
b.modelIndex--;
b.leadAtomIndices = b.bioPolymer.getLeadAtomIndices ();
} else if (b.modelIndex == modelIndex) {
this.bioShapes = J.util.ArrayUtil.deleteElements (this.bioShapes, i, 1);
}}
return;
}this.initialize ();
if ("color" === propertyName) {
var pid = J.constant.EnumPalette.pidOf (value);
var colix = J.util.C.getColixO (value);
for (var i = this.bioShapes.length; --i >= 0; ) {
var bioShape = this.bioShapes[i];
if (bioShape.monomerCount > 0) bioShape.setColixBS (colix, pid, bsSelected);
}
return;
}if ("params" === propertyName) {
var n = bsSelected.length ();
var atomMap =  Clazz.newIntArray (n, 0);
for (var pt = 0, i = bsSelected.nextSetBit (0); i >= 0; i = bsSelected.nextSetBit (i + 1), pt++) atomMap[i] = pt;

for (var i = this.bioShapes.length; --i >= 0; ) this.bioShapes[i].setParams (value, atomMap, bsSelected);

return;
}if ("colorPhase" === propertyName) {
var twoColors = value;
var colixBack = J.util.C.getColixO (twoColors[0]);
var colix = J.util.C.getColixO (twoColors[1]);
for (var i = this.bioShapes.length; --i >= 0; ) {
var bioShape = this.bioShapes[i];
if (bioShape.monomerCount > 0) {
bioShape.setColixBS (colix, 0, bsSelected);
bioShape.setColixBack (colixBack, bsSelected);
}}
return;
}if ("translucency" === propertyName) {
var isTranslucent = ("translucent".equals (value));
for (var i = this.bioShapes.length; --i >= 0; ) {
var bioShape = this.bioShapes[i];
if (bioShape.monomerCount > 0) bioShape.setTranslucent (isTranslucent, bsSelected, this.translucentLevel);
}
return;
}this.setPropS (propertyName, value, bsSelected);
}, "~S,~O,J.util.BS");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return this.viewer.getAtomShapeSetState (this, this.bioShapes);
});
$_M(c$, "initialize", 
function () {
var modelCount = this.modelSet.modelCount;
var models = this.modelSet.models;
var n = this.modelSet.getBioPolymerCount ();
var shapes =  new Array (n--);
for (var i = modelCount; --i >= 0; ) for (var j = this.modelSet.getBioPolymerCountInModel (i); --j >= 0; n--) {
var bp = (models[i]).getBioPolymer (j);
shapes[n] = (this.bioShapes == null || this.bioShapes.length <= n || this.bioShapes[n] == null || this.bioShapes[n].bioPolymer !== bp ?  new J.shapebio.BioShape (this, i, bp) : this.bioShapes[n]);
}

this.bioShapes = shapes;
});
Clazz.overrideMethod (c$, "findNearestAtomIndex", 
function (xMouse, yMouse, closest, bsNot) {
for (var i = this.bioShapes.length; --i >= 0; ) this.bioShapes[i].findNearestAtomIndex (xMouse, yMouse, closest, bsNot);

}, "~N,~N,~A,J.util.BS");
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
if (this.bioShapes == null) return;
bs = J.util.BSUtil.copy (bs);
for (var i = this.modelSet.modelCount; --i >= 0; ) if (bs.get (i) && this.modelSet.isTrajectory (i)) bs.set (this.modelSet.getTrajectoryIndex (i));

for (var i = this.bioShapes.length; --i >= 0; ) {
var b = this.bioShapes[i];
b.modelVisibilityFlags = (bs.get (b.modelIndex) ? this.myVisibilityFlag : 0);
}
}, "J.util.BS");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
if (this.bioShapes == null) return;
for (var i = this.bioShapes.length; --i >= 0; ) this.bioShapes[i].setModelClickability ();

});
$_M(c$, "getMpsShapeCount", 
function () {
return this.bioShapes.length;
});
$_M(c$, "getBioShape", 
function (i) {
return this.bioShapes[i];
}, "~N");
});
