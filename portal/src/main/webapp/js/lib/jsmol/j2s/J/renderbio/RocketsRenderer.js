Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.renderbio.StrandsRenderer", "J.util.P3", "$.V3"], "J.renderbio.RocketsRenderer", ["J.constant.EnumStructure"], function () {
c$ = Clazz.decorateAsClass (function () {
this.newRockets = false;
this.renderArrowHeads = false;
this.cordMidPoints = null;
this.tPending = false;
this.proteinstructurePending = null;
this.startIndexPending = 0;
this.endIndexPending = 0;
this.screenA = null;
this.screenB = null;
this.screenC = null;
this.vtemp = null;
this.corners = null;
this.screenCorners = null;
this.pointTipOffset = null;
this.scaledWidthVector = null;
this.scaledHeightVector = null;
this.lengthVector = null;
this.pointCorner = null;
Clazz.instantialize (this, arguments);
}, J.renderbio, "RocketsRenderer", J.renderbio.StrandsRenderer);
Clazz.prepareFields (c$, function () {
this.screenA =  new J.util.P3 ();
this.screenB =  new J.util.P3 ();
this.screenC =  new J.util.P3 ();
this.vtemp =  new J.util.V3 ();
this.corners =  new Array (8);
this.screenCorners =  new Array (8);
{
for (var i = 8; --i >= 0; ) {
this.screenCorners[i] =  new J.util.P3 ();
this.corners[i] =  new J.util.P3 ();
}
}this.pointTipOffset =  new J.util.P3 ();
this.scaledWidthVector =  new J.util.V3 ();
this.scaledHeightVector =  new J.util.V3 ();
this.lengthVector =  new J.util.V3 ();
this.pointCorner =  new J.util.P3 ();
});
Clazz.overrideMethod (c$, "renderBioShape", 
function (bioShape) {
if (!(Clazz.instanceOf (bioShape.bioPolymer, J.modelsetbio.AlphaPolymer))) return;
if (this.wireframeOnly) {
this.renderStrands ();
return;
}var val = !this.viewer.getBoolean (603979900);
if (this.renderArrowHeads != val) {
bioShape.falsifyMesh ();
this.renderArrowHeads = val;
}this.calcRopeMidPoints (this.newRockets);
this.calcScreenControlPoints (this.cordMidPoints);
this.controlPoints = this.cordMidPoints;
this.renderRockets ();
this.viewer.freeTempPoints (this.cordMidPoints);
}, "J.shapebio.BioShape");
$_M(c$, "isSheet", 
function (i) {
return this.structureTypes[i] === J.constant.EnumStructure.SHEET;
}, "~N");
$_M(c$, "calcRopeMidPoints", 
function (isNewStyle) {
var midPointCount = this.monomerCount + 1;
this.cordMidPoints = this.viewer.allocTempPoints (midPointCount);
var proteinstructurePrev = null;
var point;
for (var i = 0; i < this.monomerCount; ++i) {
point = this.cordMidPoints[i];
var residue = this.monomers[i];
if (isNewStyle && this.renderArrowHeads) {
point.setT (this.controlPoints[i]);
} else if (this.isHelix (i) || !isNewStyle && this.isSheet (i)) {
var proteinstructure = residue.getProteinStructure ();
point.setT (i - 1 != proteinstructure.getMonomerIndex () ? proteinstructure.getAxisStartPoint () : proteinstructure.getAxisEndPoint ());
proteinstructurePrev = proteinstructure;
} else {
if (proteinstructurePrev != null) point.setT (proteinstructurePrev.getAxisEndPoint ());
 else {
point.setT (this.controlPoints[i]);
}proteinstructurePrev = null;
}}
point = this.cordMidPoints[this.monomerCount];
if (proteinstructurePrev != null) point.setT (proteinstructurePrev.getAxisEndPoint ());
 else {
point.setT (this.controlPoints[this.monomerCount]);
}}, "~B");
$_M(c$, "renderRockets", 
function () {
this.tPending = false;
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) {
var monomer = this.monomers[i];
if (this.isHelix (i) || this.isSheet (i)) {
this.renderSpecialSegment (monomer, this.getLeadColix (i), this.mads[i]);
} else {
this.renderPending ();
this.renderHermiteConic (i, true);
}}
this.renderPending ();
});
$_M(c$, "renderSpecialSegment", 
function (monomer, thisColix, thisMad) {
var proteinstructure = monomer.getProteinStructure ();
if (this.tPending) {
if (proteinstructure === this.proteinstructurePending && thisMad == this.mad && thisColix == this.colix && proteinstructure.getIndex (monomer) == this.endIndexPending + 1) {
++this.endIndexPending;
return;
}this.renderPending ();
}this.proteinstructurePending = proteinstructure;
this.startIndexPending = this.endIndexPending = proteinstructure.getIndex (monomer);
this.colix = thisColix;
this.mad = thisMad;
this.tPending = true;
}, "J.modelsetbio.Monomer,~N,~N");
$_M(c$, "renderPending", 
function () {
if (!this.tPending) return;
var segments = this.proteinstructurePending.getSegments ();
var tEnd = (this.endIndexPending == this.proteinstructurePending.getMonomerCount () - 1);
if (Clazz.instanceOf (this.proteinstructurePending, J.modelsetbio.Helix)) this.renderPendingRocketSegment (this.endIndexPending, segments[this.startIndexPending], segments[this.endIndexPending], segments[this.endIndexPending + 1], tEnd);
 else if (Clazz.instanceOf (this.proteinstructurePending, J.modelsetbio.Sheet)) this.renderPendingSheet (segments[this.startIndexPending], segments[this.endIndexPending], segments[this.endIndexPending + 1], tEnd);
this.tPending = false;
});
$_M(c$, "renderPendingRocketSegment", 
($fz = function (i, pointStart, pointBeforeEnd, pointEnd, tEnd) {
this.viewer.transformPt3f (pointStart, this.screenA);
this.viewer.transformPt3f (pointEnd, this.screenB);
var zMid = Clazz.doubleToInt (Math.floor ((this.screenA.z + this.screenB.z) / 2));
var diameter = Clazz.floatToInt (this.viewer.scaleToScreen (zMid, this.mad));
if (this.g3d.setColix (this.colix)) {
this.g3d.fillCylinderBits (2, diameter, this.screenA, this.screenB);
if (tEnd && this.renderArrowHeads) {
this.vtemp.sub2 (pointEnd, pointStart);
this.vtemp.normalize ();
this.screenA.scaleAdd2 (4.0, this.vtemp, pointEnd);
this.viewer.transformPt3f (this.screenA, this.screenC);
this.renderCone (i, pointEnd, this.screenA, this.screenB, this.screenC);
}if (this.startIndexPending == this.endIndexPending) return;
var t = this.screenB;
this.screenB = this.screenC;
this.screenC = t;
}}, $fz.isPrivate = true, $fz), "~N,J.util.P3,J.util.P3,J.util.P3,~B");
$_M(c$, "renderCone", 
function (i, pointBegin, pointEnd, screenPtBegin, screenPtEnd) {
var coneDiameter = (this.mad << 1) - (this.mad >> 1);
coneDiameter = Clazz.floatToInt (this.viewer.scaleToScreen (Clazz.doubleToInt (Math.floor (screenPtBegin.z)), coneDiameter));
this.g3d.fillConeSceen3f (2, coneDiameter, screenPtBegin, screenPtEnd);
}, "~N,J.util.P3,J.util.P3,J.util.P3,J.util.P3");
$_M(c$, "renderPendingSheet", 
($fz = function (pointStart, pointBeforeEnd, pointEnd, tEnd) {
if (!this.g3d.setColix (this.colix)) return;
if (tEnd && this.renderArrowHeads) {
this.drawArrowHeadBox (pointBeforeEnd, pointEnd);
this.drawBox (pointStart, pointBeforeEnd);
} else {
this.drawBox (pointStart, pointEnd);
}}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3,J.util.P3,~B");
$_M(c$, "buildBox", 
function (pointCorner, scaledWidthVector, scaledHeightVector, lengthVector) {
for (var i = 8; --i >= 0; ) {
var corner = this.corners[i];
corner.setT (pointCorner);
if ((i & 1) != 0) corner.add (scaledWidthVector);
if ((i & 2) != 0) corner.add (scaledHeightVector);
if ((i & 4) != 0) corner.add (lengthVector);
this.viewer.transformPt3f (corner, this.screenCorners[i]);
}
}, "J.util.P3,J.util.V3,J.util.V3,J.util.V3");
$_M(c$, "buildArrowHeadBox", 
function (pointCorner, scaledWidthVector, scaledHeightVector, pointTip) {
for (var i = 4; --i >= 0; ) {
var corner = this.corners[i];
corner.setT (pointCorner);
if ((i & 1) != 0) corner.add (scaledWidthVector);
if ((i & 2) != 0) corner.add (scaledHeightVector);
this.viewer.transformPt3f (corner, this.screenCorners[i]);
}
this.corners[4].setT (pointTip);
this.viewer.transformPt3f (pointTip, this.screenCorners[4]);
this.corners[5].add2 (pointTip, scaledHeightVector);
this.viewer.transformPt3f (this.corners[5], this.screenCorners[5]);
}, "J.util.P3,J.util.V3,J.util.V3,J.util.P3");
$_M(c$, "drawBox", 
function (pointA, pointB) {
var sheet = this.proteinstructurePending;
var scale = this.mad / 1000;
this.scaledWidthVector.setT (sheet.getWidthUnitVector ());
this.scaledWidthVector.scale (scale);
this.scaledHeightVector.setT (sheet.getHeightUnitVector ());
this.scaledHeightVector.scale (scale / 4);
this.pointCorner.add2 (this.scaledWidthVector, this.scaledHeightVector);
this.pointCorner.scaleAdd (-0.5, pointA);
this.lengthVector.sub2 (pointB, pointA);
this.buildBox (this.pointCorner, this.scaledWidthVector, this.scaledHeightVector, this.lengthVector);
for (var i = 0; i < 6; ++i) {
var i0 = J.renderbio.RocketsRenderer.boxFaces[i * 4];
var i1 = J.renderbio.RocketsRenderer.boxFaces[i * 4 + 1];
var i2 = J.renderbio.RocketsRenderer.boxFaces[i * 4 + 2];
var i3 = J.renderbio.RocketsRenderer.boxFaces[i * 4 + 3];
this.g3d.fillQuadrilateral (this.screenCorners[i0], this.screenCorners[i1], this.screenCorners[i2], this.screenCorners[i3]);
}
}, "J.util.P3,J.util.P3");
$_M(c$, "drawArrowHeadBox", 
function (base, tip) {
var sheet = this.proteinstructurePending;
var scale = this.mad / 1000;
this.scaledWidthVector.setT (sheet.getWidthUnitVector ());
this.scaledWidthVector.scale (scale * 1.25);
this.scaledHeightVector.setT (sheet.getHeightUnitVector ());
this.scaledHeightVector.scale (scale / 3);
this.pointCorner.add2 (this.scaledWidthVector, this.scaledHeightVector);
this.pointCorner.scaleAdd (-0.5, base);
this.pointTipOffset.setT (this.scaledHeightVector);
this.pointTipOffset.scaleAdd (-0.5, tip);
this.buildArrowHeadBox (this.pointCorner, this.scaledWidthVector, this.scaledHeightVector, this.pointTipOffset);
this.g3d.fillTriangle3f (this.screenCorners[0], this.screenCorners[1], this.screenCorners[4], true);
this.g3d.fillTriangle3f (this.screenCorners[2], this.screenCorners[3], this.screenCorners[5], true);
for (var i = 0; i < 12; i += 4) {
var i0 = J.renderbio.RocketsRenderer.arrowHeadFaces[i];
var i1 = J.renderbio.RocketsRenderer.arrowHeadFaces[i + 1];
var i2 = J.renderbio.RocketsRenderer.arrowHeadFaces[i + 2];
var i3 = J.renderbio.RocketsRenderer.arrowHeadFaces[i + 3];
this.g3d.fillQuadrilateral (this.screenCorners[i0], this.screenCorners[i1], this.screenCorners[i2], this.screenCorners[i3]);
}
}, "J.util.P3,J.util.P3");
Clazz.defineStatics (c$,
"boxFaces", [0, 1, 3, 2, 0, 2, 6, 4, 0, 4, 5, 1, 7, 5, 4, 6, 7, 6, 2, 3, 7, 3, 1, 5]);
Clazz.defineStatics (c$,
"arrowHeadFaces", [0, 1, 3, 2, 0, 4, 5, 2, 1, 4, 5, 3]);
});
