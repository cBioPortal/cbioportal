Clazz.declarePackage ("J.shapebio");
Clazz.load (["J.shape.AtomShape", "J.modelset.Atom", "J.modelsetbio.NucleicMonomer", "J.viewer.JC"], "J.shapebio.BioShape", ["java.lang.Float", "J.constant.EnumPalette", "$.EnumStructure", "J.util.ArrayUtil", "$.BS", "$.C", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.modelIndex = 0;
this.modelVisibilityFlags = 0;
this.shape = null;
this.bioPolymer = null;
this.meshes = null;
this.meshReady = null;
this.colixesBack = null;
this.monomers = null;
this.wingVectors = null;
this.leadAtomIndices = null;
this.hasBfactorRange = false;
this.bfactorMin = 0;
this.bfactorMax = 0;
this.range = 0;
this.floatRange = 0;
Clazz.instantialize (this, arguments);
}, J.shapebio, "BioShape", J.shape.AtomShape);
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bsSelected) {
this.setPropAS (propertyName, value, bsSelected);
}, "~S,~O,J.util.BS");
Clazz.overrideMethod (c$, "getMonomers", 
function () {
return this.monomers;
});
Clazz.makeConstructor (c$, 
function (shape, modelIndex, bioPolymer) {
Clazz.superConstructor (this, J.shapebio.BioShape, []);
this.shape = shape;
this.modelIndex = modelIndex;
this.bioPolymer = bioPolymer;
this.isActive = shape.isActive;
this.bsSizeDefault =  new J.util.BS ();
this.monomerCount = bioPolymer.monomerCount;
if (this.monomerCount > 0) {
this.colixes =  Clazz.newShortArray (this.monomerCount, 0);
this.paletteIDs =  Clazz.newByteArray (this.monomerCount, 0);
this.mads =  Clazz.newShortArray (this.monomerCount + 1, 0);
this.monomers = bioPolymer.getGroups ();
this.meshReady =  Clazz.newBooleanArray (this.monomerCount, false);
this.meshes =  new Array (this.monomerCount);
this.wingVectors = bioPolymer.getWingVectors ();
this.leadAtomIndices = bioPolymer.getLeadAtomIndices ();
}}, "J.shapebio.BioShapeCollection,~N,J.modelsetbio.BioPolymer");
$_M(c$, "calcBfactorRange", 
function () {
this.bfactorMin = this.bfactorMax = this.monomers[0].getLeadAtom ().getBfactor100 ();
for (var i = this.monomerCount; --i > 0; ) {
var bfactor = this.monomers[i].getLeadAtom ().getBfactor100 ();
if (bfactor < this.bfactorMin) this.bfactorMin = bfactor;
 else if (bfactor > this.bfactorMax) this.bfactorMax = bfactor;
}
this.range = this.bfactorMax - this.bfactorMin;
this.floatRange = this.range;
this.hasBfactorRange = true;
});
$_M(c$, "calcMeanPositionalDisplacement", 
function (bFactor100) {
return Clazz.doubleToShort (Math.sqrt (bFactor100 / 7895.6835208714865) * 1000);
}, "~N");
Clazz.overrideMethod (c$, "findNearestAtomIndex", 
function (xMouse, yMouse, closest, bsNot) {
this.bioPolymer.findNearestAtomIndex (xMouse, yMouse, closest, this.mads, this.shape.myVisibilityFlag, bsNot);
}, "~N,~N,~A,J.util.BS");
$_M(c$, "setMad", 
function (mad, bsSelected, values) {
if (this.monomerCount < 2) return;
this.isActive = true;
if (this.bsSizeSet == null) this.bsSizeSet =  new J.util.BS ();
var flag = this.shape.myVisibilityFlag;
for (var i = this.monomerCount; --i >= 0; ) {
var leadAtomIndex = this.leadAtomIndices[i];
if (bsSelected.get (leadAtomIndex)) {
if (values != null && leadAtomIndex < values.length) {
if (Float.isNaN (values[leadAtomIndex])) continue;
mad = Clazz.floatToShort (values[leadAtomIndex] * 2000);
}var isVisible = ((this.mads[i] = this.getMad (i, mad)) > 0);
this.bsSizeSet.setBitTo (i, isVisible);
this.monomers[i].setShapeVisibility (flag, isVisible);
this.shape.atoms[leadAtomIndex].setShapeVisibility (flag, isVisible);
this.falsifyNearbyMesh (i);
}}
if (this.monomerCount > 1) this.mads[this.monomerCount] = this.mads[this.monomerCount - 1];
}, "~N,J.util.BS,~A");
$_M(c$, "getMad", 
($fz = function (groupIndex, mad) {
this.bsSizeDefault.setBitTo (groupIndex, mad == -1 || mad == -2);
if (mad >= 0) return mad;
switch (mad) {
case -1:
case -2:
if (mad == -1 && this.shape.madOn >= 0) return this.shape.madOn;
switch (this.monomers[groupIndex].getProteinStructureType ()) {
case J.constant.EnumStructure.SHEET:
case J.constant.EnumStructure.HELIX:
return this.shape.madHelixSheet;
case J.constant.EnumStructure.DNA:
case J.constant.EnumStructure.RNA:
return this.shape.madDnaRna;
default:
return this.shape.madTurnRandom;
}
case -3:
{
if (!this.hasBfactorRange) this.calcBfactorRange ();
var atom = this.monomers[groupIndex].getLeadAtom ();
var bfactor100 = atom.getBfactor100 ();
var scaled = bfactor100 - this.bfactorMin;
if (this.range == 0) return 0;
var percentile = scaled / this.floatRange;
if (percentile < 0 || percentile > 1) J.util.Logger.error ("Que ha ocurrido? " + percentile);
return Clazz.floatToShort ((1750 * percentile) + 250);
}case -4:
{
var atom = this.monomers[groupIndex].getLeadAtom ();
return (2 * this.calcMeanPositionalDisplacement (atom.getBfactor100 ()));
}}
J.util.Logger.error ("unrecognized setMad(" + mad + ")");
return 0;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "falsifyMesh", 
function () {
if (this.meshReady == null) return;
for (var i = 0; i < this.monomerCount; i++) this.meshReady[i] = false;

});
$_M(c$, "falsifyNearbyMesh", 
($fz = function (index) {
if (this.meshReady == null) return;
this.meshReady[index] = false;
if (index > 0) this.meshReady[index - 1] = false;
if (index < this.monomerCount - 1) this.meshReady[index + 1] = false;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setColixBS", 
function (colix, pid, bsSelected) {
this.isActive = true;
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
for (var i = this.monomerCount; --i >= 0; ) {
var atomIndex = this.leadAtomIndices[i];
if (bsSelected.get (atomIndex)) {
this.colixes[i] = this.shape.getColixI (colix, pid, atomIndex);
if (this.colixesBack != null && this.colixesBack.length > i) this.colixesBack[i] = 0;
this.paletteIDs[i] = pid;
this.bsColixSet.setBitTo (i, this.colixes[i] != 0);
}}
}, "~N,~N,J.util.BS");
$_M(c$, "setColixes", 
function (atomColixes, bsSelected) {
this.isActive = true;
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
for (var i = this.monomerCount; --i >= 0; ) {
var atomIndex = this.leadAtomIndices[i];
if (bsSelected.get (atomIndex) && i < this.colixes.length && atomIndex < atomColixes.length) {
this.colixes[i] = this.shape.getColixI (atomColixes[atomIndex], J.constant.EnumPalette.UNKNOWN.id, atomIndex);
if (this.colixesBack != null && i < this.colixesBack.length) this.colixesBack[i] = 0;
this.paletteIDs[i] = J.constant.EnumPalette.UNKNOWN.id;
this.bsColixSet.set (i);
}}
}, "~A,J.util.BS");
$_M(c$, "setParams", 
function (data, atomMap, bsSelected) {
if (this.monomerCount == 0) return;
var c = data[0];
var atrans = data[1];
this.isActive = true;
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
var n = atomMap.length;
for (var i = this.monomerCount; --i >= 0; ) {
var atomIndex = this.leadAtomIndices[i];
if (bsSelected.get (atomIndex) && i < this.colixes.length && atomIndex < n) {
var pt = atomMap[atomIndex];
var colix = (c == null ? 0 : c[pt]);
var f = (atrans == null ? 0 : atrans[pt]);
if (f > 0.01) colix = J.util.C.getColixTranslucent3 (colix, true, f);
this.colixes[i] = this.shape.getColixI (colix, J.constant.EnumPalette.UNKNOWN.id, atomIndex);
if (this.colixesBack != null && i < this.colixesBack.length) this.colixesBack[i] = 0;
this.paletteIDs[i] = J.constant.EnumPalette.UNKNOWN.id;
this.bsColixSet.set (i);
}}
}, "~A,~A,J.util.BS");
$_M(c$, "setColixBack", 
function (colix, bsSelected) {
for (var i = this.monomerCount; --i >= 0; ) {
var atomIndex = this.leadAtomIndices[i];
if (bsSelected.get (atomIndex)) {
if (this.colixesBack == null) this.colixesBack =  Clazz.newShortArray (this.colixes.length, 0);
if (this.colixesBack.length < this.colixes.length) this.colixesBack = J.util.ArrayUtil.ensureLengthShort (this.colixesBack, this.colixes.length);
this.colixesBack[i] = colix;
}}
}, "~N,J.util.BS");
$_M(c$, "setTranslucent", 
function (isTranslucent, bsSelected, translucentLevel) {
this.isActive = true;
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
for (var i = this.monomerCount; --i >= 0; ) if (bsSelected.get (this.leadAtomIndices[i])) {
this.colixes[i] = J.util.C.getColixTranslucent3 (this.colixes[i], isTranslucent, translucentLevel);
if (this.colixesBack != null && this.colixesBack.length > i) this.colixesBack[i] = J.util.C.getColixTranslucent3 (this.colixesBack[i], isTranslucent, translucentLevel);
this.bsColixSet.setBitTo (i, this.colixes[i] != 0);
}
}, "~B,J.util.BS,~N");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
if (!this.isActive || this.wingVectors == null) return;
var isNucleicPolymer = Clazz.instanceOf (this.bioPolymer, J.modelsetbio.NucleicPolymer);
for (var i = this.monomerCount; --i >= 0; ) {
if (this.mads[i] <= 0) continue;
var iAtom = this.leadAtomIndices[i];
if (this.monomers[i].chain.model.modelSet.isAtomHidden (iAtom)) continue;
this.shape.atoms[iAtom].setClickable (J.shapebio.BioShape.ALPHA_CARBON_VISIBILITY_FLAG);
if (isNucleicPolymer) (this.monomers[i]).setModelClickability ();
}
});
c$.ALPHA_CARBON_VISIBILITY_FLAG = c$.prototype.ALPHA_CARBON_VISIBILITY_FLAG = J.modelsetbio.NucleicMonomer.CARTOON_VISIBILITY_FLAG | J.modelset.Atom.BACKBONE_VISIBILITY_FLAG | J.viewer.JC.getShapeVisibilityFlag (10) | J.viewer.JC.getShapeVisibilityFlag (12) | J.viewer.JC.getShapeVisibilityFlag (13) | J.viewer.JC.getShapeVisibilityFlag (14);
Clazz.defineStatics (c$,
"eightPiSquared100", 7895.6835208714865);
});
