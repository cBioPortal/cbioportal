Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.renderbio.BioShapeRenderer"], "J.renderbio.BackboneRenderer", ["JU.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.isDataFrame = false;
Clazz.instantialize (this, arguments);
}, J.renderbio, "BackboneRenderer", J.renderbio.BioShapeRenderer);
Clazz.overrideMethod (c$, "renderBioShape", 
function (bioShape) {
var showSteps = this.vwr.getBoolean (603979811) && bioShape.bioPolymer.isNucleic ();
this.isDataFrame = this.vwr.ms.isJmolDataFrameForModel (bioShape.modelIndex);
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) {
var atomA = this.ms.at[this.leadAtomIndices[i]];
var cA = this.colixes[i];
this.mad = this.mads[i];
this.drawSegment (atomA, this.ms.at[this.leadAtomIndices[i + 1]], cA, this.colixes[i + 1], 100);
if (showSteps) {
var g = this.monomers[i];
var bps = g.getBasePairs ();
if (bps != null) {
for (var j = bps.size (); --j >= 0; ) {
var iAtom = bps.get (j).getPartnerAtom (g);
if (iAtom > i) this.drawSegment (atomA, this.ms.at[iAtom], cA, cA, 1000);
}
}}}
}, "J.shapebio.BioShape");
Clazz.defineMethod (c$, "drawSegment", 
 function (atomA, atomB, colixA, colixB, max) {
if (atomA.getNBackbonesDisplayed () == 0 || atomB.getNBackbonesDisplayed () == 0 || this.ms.isAtomHidden (atomB.i) || !this.isDataFrame && atomA.distanceSquared (atomB) > max) return;
colixA = JU.C.getColixInherited (colixA, atomA.getColix ());
colixB = JU.C.getColixInherited (colixB, atomB.getColix ());
if (!this.isExport && !this.isPass2 && !this.setBioColix (colixA) && !this.setBioColix (colixB)) return;
var xA = atomA.sX;
var yA = atomA.sY;
var zA = atomA.sZ;
var xB = atomB.sX;
var yB = atomB.sY;
var zB = atomB.sZ;
var mad = this.mad;
if (max == 1000) mad = mad >> 1;
if (mad < 0) {
this.g3d.drawLine (colixA, colixB, xA, yA, zA, xB, yB, zB);
} else {
var width = Clazz.floatToInt (this.exportType == 1 ? mad : this.vwr.tm.scaleToScreen (Clazz.doubleToInt ((zA + zB) / 2), mad));
this.g3d.fillCylinderXYZ (colixA, colixB, 3, width, xA, yA, zA, xB, yB, zB);
}}, "JM.Atom,JM.Atom,~N,~N,~N");
});
