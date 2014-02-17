Clazz.declarePackage ("J.shapebio");
Clazz.load (["J.shapebio.BioShapeCollection"], "J.shapebio.Backbone", ["java.lang.Float", "J.util.BS"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bsSelected = null;
Clazz.instantialize (this, arguments);
}, J.shapebio, "Backbone", J.shapebio.BioShapeCollection);
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapebio.Backbone, "initShape", []);
this.madOn = 1;
this.madHelixSheet = 1500;
this.madTurnRandom = 500;
this.madDnaRna = 2000;
this.isActive = true;
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bsSelected) {
if ("bitset" === propertyName) {
this.bsSelected = value;
return;
}this.setPropBSC (propertyName, value, bsSelected);
}, "~S,~O,J.util.BS");
Clazz.overrideMethod (c$, "setShapeSizeRD", 
function (size, rd, bsSelected) {
var mad = size;
this.initialize ();
var useThisBsSelected = (this.bsSelected != null);
if (useThisBsSelected) bsSelected = this.bsSelected;
for (var iShape = this.bioShapes.length; --iShape >= 0; ) {
var bioShape = this.bioShapes[iShape];
if (bioShape.monomerCount == 0) continue;
var bondSelectionModeOr = this.viewer.getBoolean (603979812);
var atomIndices = bioShape.bioPolymer.getLeadAtomIndices ();
var isVisible = (mad != 0);
if (bioShape.bsSizeSet == null) bioShape.bsSizeSet =  new J.util.BS ();
bioShape.isActive = true;
for (var i = bioShape.monomerCount - 1; --i >= 0; ) {
var index1 = atomIndices[i];
var index2 = atomIndices[i + 1];
var isAtom1 = bsSelected.get (index1);
var isAtom2 = bsSelected.get (index2);
if (isAtom1 && isAtom2 || useThisBsSelected && isAtom1 || bondSelectionModeOr && (isAtom1 || isAtom2)) {
bioShape.monomers[i].setShapeVisibility (this.myVisibilityFlag, isVisible);
var atomA = this.modelSet.atoms[index1];
if (rd != null) {
if (Float.isNaN (rd.values[index1]) || Float.isNaN (rd.values[index2])) continue;
mad = Clazz.floatToShort ((rd.values[index1] + rd.values[index2]) * 1000);
isVisible = (mad != 0);
}var atomB = this.modelSet.atoms[index2];
var wasVisible = (bioShape.mads[i] != 0);
if (wasVisible != isVisible) {
atomA.addDisplayedBackbone (this.myVisibilityFlag, isVisible);
atomB.addDisplayedBackbone (this.myVisibilityFlag, isVisible);
}bioShape.mads[i] = mad;
bioShape.bsSizeSet.setBitTo (i, isVisible);
bioShape.bsSizeDefault.setBitTo (i, mad == -1);
}}
}
if (useThisBsSelected) this.bsSelected = null;
}, "~N,J.atomdata.RadiusData,J.util.BS");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
if (this.bioShapes == null) return;
for (var iShape = this.bioShapes.length; --iShape >= 0; ) {
var bioShape = this.bioShapes[iShape];
var atomIndices = bioShape.bioPolymer.getLeadAtomIndices ();
for (var i = bioShape.monomerCount; --i >= 0; ) {
var atom = this.modelSet.atoms[atomIndices[i]];
if (atom.getNBackbonesDisplayed () > 0 && !this.modelSet.isAtomHidden (i)) atom.setClickable (this.myVisibilityFlag);
}
}
});
});
