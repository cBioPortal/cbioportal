Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.renderbio.RocketsRenderer", "J.util.P3", "$.P3i"], "J.renderbio.CartoonRenderer", ["J.util.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.renderAsRockets = false;
this.renderEdges = false;
this.ladderOnly = false;
this.ptConnectScr = null;
this.ptConnect = null;
this.ring6Points = null;
this.ring6Screens = null;
this.ring5Points = null;
this.ring5Screens = null;
Clazz.instantialize (this, arguments);
}, J.renderbio, "CartoonRenderer", J.renderbio.RocketsRenderer);
Clazz.prepareFields (c$, function () {
this.ptConnectScr =  new J.util.P3i ();
this.ptConnect =  new J.util.P3 ();
this.ring6Points =  new Array (6);
this.ring6Screens =  new Array (6);
this.ring5Points =  new Array (5);
this.ring5Screens =  new Array (5);
{
this.ring6Screens[5] =  new J.util.P3i ();
for (var i = 5; --i >= 0; ) {
this.ring5Screens[i] =  new J.util.P3i ();
this.ring6Screens[i] =  new J.util.P3i ();
}
}});
Clazz.overrideMethod (c$, "renderBioShape", 
function (bioShape) {
if (this.wireframeOnly) {
this.renderStrands ();
return;
}this.newRockets = true;
if (this.wingVectors == null || this.isCarbohydrate) return;
this.getScreenControlPoints ();
if (this.isNucleic) {
this.renderNucleic ();
return;
}var val = this.viewer.getBoolean (603979818);
if (this.renderAsRockets != val) {
bioShape.falsifyMesh ();
this.renderAsRockets = val;
}val = !this.viewer.getBoolean (603979900);
if (this.renderArrowHeads != val) {
bioShape.falsifyMesh ();
this.renderArrowHeads = val;
}this.ribbonTopScreens = this.calcScreens (0.5);
this.ribbonBottomScreens = this.calcScreens (-0.5);
this.calcRopeMidPoints (this.newRockets);
if (!this.renderArrowHeads) {
this.calcScreenControlPoints (this.cordMidPoints);
this.controlPoints = this.cordMidPoints;
}this.renderRockets ();
this.viewer.freeTempPoints (this.cordMidPoints);
this.viewer.freeTempScreens (this.ribbonTopScreens);
this.viewer.freeTempScreens (this.ribbonBottomScreens);
}, "J.shapebio.BioShape");
$_M(c$, "renderNucleic", 
function () {
this.renderEdges = this.viewer.getBoolean (603979817);
this.ladderOnly = this.viewer.getBoolean (603979820);
var isTraceAlpha = this.viewer.getBoolean (603979966);
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) {
if (isTraceAlpha) {
this.ptConnectScr.set (Clazz.doubleToInt ((this.controlPointScreens[i].x + this.controlPointScreens[i + 1].x) / 2), Clazz.doubleToInt ((this.controlPointScreens[i].y + this.controlPointScreens[i + 1].y) / 2), Clazz.doubleToInt ((this.controlPointScreens[i].z + this.controlPointScreens[i + 1].z) / 2));
this.ptConnect.setT (this.controlPoints[i]);
this.ptConnect.scale (0.5);
this.ptConnect.scaleAdd2 (0.5, this.controlPoints[i + 1], this.ptConnect);
} else {
this.ptConnectScr.setT (this.controlPointScreens[i + 1]);
this.ptConnect.setT (this.controlPoints[i + 1]);
}this.renderHermiteConic (i, false);
this.colix = this.getLeadColix (i);
if (this.setBioColix (this.colix)) this.renderNucleicBaseStep (this.monomers[i], this.mads[i], this.ptConnectScr, this.ptConnect);
}
});
Clazz.overrideMethod (c$, "renderRockets", 
function () {
var lastWasSheet = false;
var lastWasHelix = false;
var previousStructure = null;
var thisStructure;
for (var i = this.monomerCount; --i >= 0; ) {
thisStructure = this.monomers[i].getProteinStructure ();
if (thisStructure !== previousStructure) {
if (this.renderAsRockets) lastWasHelix = false;
lastWasSheet = false;
}previousStructure = thisStructure;
var isHelix = this.isHelix (i);
var isSheet = this.isSheet (i);
var isHelixRocket = (this.renderAsRockets || !this.renderArrowHeads ? isHelix : false);
if (this.bsVisible.get (i)) {
if (isHelixRocket) {
} else if (isSheet || isHelix) {
if (lastWasSheet && isSheet || lastWasHelix && isHelix) {
this.renderHermiteRibbon (true, i, true);
} else {
this.renderHermiteArrowHead (i);
}} else {
this.renderHermiteConic (i, true);
}}lastWasSheet = isSheet;
lastWasHelix = isHelix;
}
if (this.renderAsRockets || !this.renderArrowHeads) this.renderCartoonRockets ();
});
$_M(c$, "renderCartoonRockets", 
($fz = function () {
this.tPending = false;
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) if (this.isHelix (i)) this.renderSpecialSegment (this.monomers[i], this.getLeadColix (i), this.mads[i]);

this.renderPending ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderNucleicBaseStep", 
($fz = function (nucleotide, thisMad, backboneScreen, ptConnect) {
if (this.renderEdges) {
this.renderLeontisWesthofEdges (nucleotide, thisMad);
return;
}nucleotide.getBaseRing6Points (this.ring6Points);
this.viewer.transformPoints (this.ring6Points, this.ring6Screens);
this.renderRing6 ();
var hasRing5 = nucleotide.maybeGetBaseRing5Points (this.ring5Points);
var stepScreen;
var stepPt;
var pt;
if (hasRing5) {
this.viewer.transformPoints (this.ring5Points, this.ring5Screens);
this.renderRing5 ();
if (this.ladderOnly) {
stepScreen = this.ring6Screens[2];
stepPt = this.ring6Points[2];
} else {
stepScreen = this.ring5Screens[3];
stepPt = this.ring5Points[3];
}} else {
pt = (this.ladderOnly ? 4 : 2);
stepScreen = this.ring6Screens[pt];
stepPt = this.ring6Points[pt];
}this.mad = (thisMad > 1 ? Clazz.doubleToInt (thisMad / 2) : thisMad);
this.g3d.fillCylinderScreen3I (3, Clazz.floatToInt (this.viewer.scaleToScreen (backboneScreen.z, this.mad)), backboneScreen, stepScreen, ptConnect, stepPt, this.mad / 2000);
if (this.ladderOnly) return;
--this.ring6Screens[5].z;
for (var i = 5; --i >= 0; ) {
--this.ring6Screens[i].z;
if (hasRing5) --this.ring5Screens[i].z;
}
for (var i = 6; --i > 0; ) this.g3d.fillCylinderScreen3I (3, 3, this.ring6Screens[i], this.ring6Screens[i - 1], this.ring6Points[i], this.ring6Points[i - 1], 0.005);

if (hasRing5) {
for (var i = 5; --i > 0; ) this.g3d.fillCylinderScreen3I (3, 3, this.ring5Screens[i], this.ring5Screens[i - 1], this.ring5Points[i], this.ring5Points[i - 1], 0.005);

} else {
this.g3d.fillCylinderScreen3I (3, 3, this.ring6Screens[5], this.ring6Screens[0], this.ring6Points[5], this.ring6Points[0], 0.005);
}}, $fz.isPrivate = true, $fz), "J.modelsetbio.NucleicMonomer,~N,J.util.P3i,J.util.P3");
$_M(c$, "renderLeontisWesthofEdges", 
($fz = function (nucleotide, thisMad) {
if (!nucleotide.getEdgePoints (this.ring6Points)) return;
this.viewer.transformPoints (this.ring6Points, this.ring6Screens);
this.renderTriangle ();
this.mad = (thisMad > 1 ? Clazz.doubleToInt (thisMad / 2) : thisMad);
this.g3d.fillCylinderScreen3I (3, 3, this.ring6Screens[0], this.ring6Screens[1], this.ring6Points[0], this.ring6Points[1], 0.005);
this.g3d.fillCylinderScreen3I (3, 3, this.ring6Screens[1], this.ring6Screens[2], this.ring6Points[1], this.ring6Points[2], 0.005);
var isTranslucent = J.util.C.isColixTranslucent (this.colix);
var tl = J.util.C.getColixTranslucencyLevel (this.colix);
var colixSugarEdge = J.util.C.getColixTranslucent3 (10, isTranslucent, tl);
var colixWatsonCrickEdge = J.util.C.getColixTranslucent3 (11, isTranslucent, tl);
var colixHoogsteenEdge = J.util.C.getColixTranslucent3 (7, isTranslucent, tl);
this.g3d.setColix (colixSugarEdge);
this.g3d.fillCylinderScreen3I (3, 3, this.ring6Screens[2], this.ring6Screens[3], this.ring6Points[2], this.ring6Points[3], 0.005);
this.g3d.setColix (colixWatsonCrickEdge);
this.g3d.fillCylinderScreen3I (3, 3, this.ring6Screens[3], this.ring6Screens[4], this.ring6Points[3], this.ring6Points[4], 0.005);
this.g3d.setColix (colixHoogsteenEdge);
this.g3d.fillCylinderScreen3I (3, 3, this.ring6Screens[4], this.ring6Screens[5], this.ring6Points[4], this.ring6Points[5], 0.005);
}, $fz.isPrivate = true, $fz), "J.modelsetbio.NucleicMonomer,~N");
$_M(c$, "renderTriangle", 
($fz = function () {
this.g3d.setNoisySurfaceShade (this.ring6Screens[2], this.ring6Screens[3], this.ring6Screens[4]);
this.g3d.fillTriangle3i (this.ring6Screens[2], this.ring6Screens[3], this.ring6Screens[4], this.ring6Points[2], this.ring6Points[3], this.ring6Points[4]);
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderRing6", 
($fz = function () {
if (this.ladderOnly) return;
this.g3d.setNoisySurfaceShade (this.ring6Screens[0], this.ring6Screens[2], this.ring6Screens[4]);
this.g3d.fillTriangle3i (this.ring6Screens[0], this.ring6Screens[2], this.ring6Screens[4], this.ring6Points[0], this.ring6Points[2], this.ring6Points[4]);
this.g3d.fillTriangle3i (this.ring6Screens[0], this.ring6Screens[1], this.ring6Screens[2], this.ring6Points[0], this.ring6Points[1], this.ring6Points[2]);
this.g3d.fillTriangle3i (this.ring6Screens[0], this.ring6Screens[4], this.ring6Screens[5], this.ring6Points[0], this.ring6Points[4], this.ring6Points[5]);
this.g3d.fillTriangle3i (this.ring6Screens[2], this.ring6Screens[3], this.ring6Screens[4], this.ring6Points[2], this.ring6Points[3], this.ring6Points[4]);
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderRing5", 
($fz = function () {
if (this.ladderOnly) return;
this.g3d.fillTriangle3i (this.ring5Screens[0], this.ring5Screens[2], this.ring5Screens[3], this.ring5Points[0], this.ring5Points[2], this.ring5Points[3]);
this.g3d.fillTriangle3i (this.ring5Screens[0], this.ring5Screens[1], this.ring5Screens[2], this.ring5Points[0], this.ring5Points[1], this.ring5Points[2]);
this.g3d.fillTriangle3i (this.ring5Screens[0], this.ring5Screens[3], this.ring5Screens[4], this.ring5Points[0], this.ring5Points[3], this.ring5Points[4]);
}, $fz.isPrivate = true, $fz));
});
