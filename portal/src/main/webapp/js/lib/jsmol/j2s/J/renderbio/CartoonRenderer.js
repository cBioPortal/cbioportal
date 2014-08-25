Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.renderbio.RocketsRenderer", "JU.P3", "$.P3i"], "J.renderbio.CartoonRenderer", ["JU.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.renderAsRockets = false;
this.renderEdges = false;
this.ladderOnly = false;
this.$renderRibose = false;
this.ptConnectScr = null;
this.ptConnect = null;
this.rPt = null;
this.rScr = null;
this.rPt5 = null;
this.rScr5 = null;
this.basePt = null;
this.baseScreen = null;
Clazz.instantialize (this, arguments);
}, J.renderbio, "CartoonRenderer", J.renderbio.RocketsRenderer);
Clazz.prepareFields (c$, function () {
this.ptConnectScr =  new JU.P3i ();
this.ptConnect =  new JU.P3 ();
this.rPt =  new Array (10);
this.rScr =  new Array (10);
this.rPt5 =  new Array (5);
this.rScr5 =  new Array (5);
});
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
}var val = this.vwr.getBoolean (603979820);
if (this.renderAsRockets != val) {
bioShape.falsifyMesh ();
this.renderAsRockets = val;
}val = !this.vwr.getBoolean (603979900);
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
this.vwr.freeTempPoints (this.cordMidPoints);
this.vwr.freeTempScreens (this.ribbonTopScreens);
this.vwr.freeTempScreens (this.ribbonBottomScreens);
}, "J.shapebio.BioShape");
Clazz.defineMethod (c$, "renderNucleic", 
function () {
this.renderEdges = this.vwr.getBoolean (603979816);
this.ladderOnly = this.vwr.getBoolean (603979818);
this.$renderRibose = this.vwr.getBoolean (603979819);
var isTraceAlpha = this.vwr.getBoolean (603979966);
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) {
if (isTraceAlpha) {
this.ptConnectScr.set (Clazz.doubleToInt ((this.controlPointScreens[i].x + this.controlPointScreens[i + 1].x) / 2), Clazz.doubleToInt ((this.controlPointScreens[i].y + this.controlPointScreens[i + 1].y) / 2), Clazz.doubleToInt ((this.controlPointScreens[i].z + this.controlPointScreens[i + 1].z) / 2));
this.ptConnect.ave (this.controlPoints[i], this.controlPoints[i + 1]);
} else {
this.ptConnectScr.setT (this.controlPointScreens[i + 1]);
this.ptConnect.setT (this.controlPoints[i + 1]);
}this.renderHermiteConic (i, false, 4);
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
thisStructure = this.monomers[i].getStructure ();
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
this.renderHermiteConic (i, true, 7);
}}lastWasSheet = isSheet;
lastWasHelix = isHelix;
}
if (this.renderAsRockets || !this.renderArrowHeads) this.renderCartoonRockets ();
});
Clazz.defineMethod (c$, "renderCartoonRockets", 
 function () {
this.tPending = false;
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) if (this.isHelix (i)) this.renderSpecialSegment (this.monomers[i], this.getLeadColix (i), this.mads[i]);

this.renderPending ();
});
Clazz.defineMethod (c$, "renderNucleicBaseStep", 
 function (nucleotide, thisMad, backboneScreen, backbonePt) {
if (this.rScr[0] == null) {
for (var i = 10; --i >= 0; ) this.rScr[i] =  new JU.P3i ();

for (var i = 5; --i >= 0; ) this.rScr5[i] =  new JU.P3i ();

this.baseScreen =  new JU.P3i ();
this.basePt =  new JU.P3 ();
this.rPt[9] =  new JU.P3 ();
}if (this.renderEdges) {
this.renderLeontisWesthofEdges (nucleotide, thisMad);
return;
}nucleotide.getBaseRing6Points (this.rPt);
this.tm.transformPoints (6, this.rPt, this.rScr);
if (!this.ladderOnly) this.renderRing6 ();
var stepScreen;
var stepPt;
var pt;
var hasRing5 = nucleotide.maybeGetBaseRing5Points (this.rPt5);
if (hasRing5) {
if (this.ladderOnly) {
stepScreen = this.rScr[2];
stepPt = this.rPt[2];
} else {
this.tm.transformPoints (5, this.rPt5, this.rScr5);
this.renderRing5 ();
stepScreen = this.rScr5[3];
stepPt = this.rPt5[3];
}} else {
pt = (this.ladderOnly ? 4 : 2);
stepScreen = this.rScr[pt];
stepPt = this.rPt[pt];
}this.mad = (thisMad > 1 ? Clazz.doubleToInt (thisMad / 2) : thisMad);
var r = this.mad / 2000;
var w = Clazz.floatToInt (this.vwr.tm.scaleToScreen (backboneScreen.z, this.mad));
if (this.ladderOnly || !this.$renderRibose) this.g3d.fillCylinderScreen3I (3, w, backboneScreen, stepScreen, backbonePt, stepPt, r);
if (this.ladderOnly) return;
this.drawEdges (this.rScr, this.rPt, 6);
if (hasRing5) this.drawEdges (this.rScr5, this.rPt5, 5);
 else this.renderEdge (this.rScr, this.rPt, 0, 5);
if (this.$renderRibose) {
this.baseScreen.setT (stepScreen);
this.basePt.setT (stepPt);
nucleotide.getRiboseRing5Points (this.rPt);
var c = this.rPt[9];
c.set (0, 0, 0);
for (var i = 0; i < 5; i++) c.add (this.rPt[i]);

c.scale (0.2);
this.tm.transformPoints (10, this.rPt, this.rScr);
this.renderRibose ();
this.renderEdge (this.rScr, this.rPt, 2, 5);
this.renderEdge (this.rScr, this.rPt, 3, 6);
this.renderEdge (this.rScr, this.rPt, 6, 7);
this.renderEdge (this.rScr, this.rPt, 7, 8);
this.renderCyl (this.rScr[0], this.baseScreen, this.rPt[0], this.basePt);
this.drawEdges (this.rScr, this.rPt, 5);
}}, "JM.NucleicMonomer,~N,JU.P3i,JU.P3");
Clazz.defineMethod (c$, "drawEdges", 
 function (scr, pt, n) {
for (var i = n; --i >= 0; ) scr[i].z--;

for (var i = n; --i > 0; ) this.renderEdge (scr, pt, i, i - 1);

}, "~A,~A,~N");
Clazz.defineMethod (c$, "renderLeontisWesthofEdges", 
 function (nucleotide, thisMad) {
if (!nucleotide.getEdgePoints (this.rPt)) return;
this.tm.transformPoints (6, this.rPt, this.rScr);
this.renderTriangle (this.rScr, this.rPt, 2, 3, 4, true);
this.mad = (thisMad > 1 ? Clazz.doubleToInt (thisMad / 2) : thisMad);
this.renderEdge (this.rScr, this.rPt, 0, 1);
this.renderEdge (this.rScr, this.rPt, 1, 2);
var isTranslucent = JU.C.isColixTranslucent (this.colix);
var tl = JU.C.getColixTranslucencyLevel (this.colix);
var colixSugarEdge = JU.C.getColixTranslucent3 (10, isTranslucent, tl);
var colixWatsonCrickEdge = JU.C.getColixTranslucent3 (11, isTranslucent, tl);
var colixHoogsteenEdge = JU.C.getColixTranslucent3 (7, isTranslucent, tl);
this.g3d.setC (colixSugarEdge);
this.renderEdge (this.rScr, this.rPt, 2, 3);
this.g3d.setC (colixWatsonCrickEdge);
this.renderEdge (this.rScr, this.rPt, 3, 4);
this.g3d.setC (colixHoogsteenEdge);
this.renderEdge (this.rScr, this.rPt, 4, 5);
}, "JM.NucleicMonomer,~N");
Clazz.defineMethod (c$, "renderEdge", 
 function (scr, pt, i, j) {
this.renderCyl (scr[i], scr[j], pt[i], pt[j]);
}, "~A,~A,~N,~N");
Clazz.defineMethod (c$, "renderCyl", 
 function (s1, s2, p1, p2) {
this.g3d.fillCylinderScreen3I (3, 3, s1, s2, p1, p2, 0.005);
}, "JU.P3i,JU.P3i,JU.P3,JU.P3");
Clazz.defineMethod (c$, "renderTriangle", 
 function (scr, pt, i, j, k, doShade) {
if (doShade) this.g3d.setNoisySurfaceShade (scr[i], scr[j], scr[k]);
this.g3d.fillTriangle3i (scr[i], scr[j], scr[k], pt[i], pt[j], pt[k]);
}, "~A,~A,~N,~N,~N,~B");
Clazz.defineMethod (c$, "renderRing6", 
 function () {
this.renderTriangle (this.rScr, this.rPt, 0, 2, 4, true);
this.renderTriangle (this.rScr, this.rPt, 0, 1, 2, false);
this.renderTriangle (this.rScr, this.rPt, 0, 4, 5, false);
this.renderTriangle (this.rScr, this.rPt, 2, 3, 4, false);
});
Clazz.defineMethod (c$, "renderRing5", 
 function () {
this.renderTriangle (this.rScr5, this.rPt5, 0, 1, 2, false);
this.renderTriangle (this.rScr5, this.rPt5, 0, 2, 3, false);
this.renderTriangle (this.rScr5, this.rPt5, 0, 3, 4, false);
});
Clazz.defineMethod (c$, "renderRibose", 
 function () {
this.renderTriangle (this.rScr, this.rPt, 0, 1, 9, true);
this.renderTriangle (this.rScr, this.rPt, 1, 2, 9, true);
this.renderTriangle (this.rScr, this.rPt, 2, 3, 9, true);
this.renderTriangle (this.rScr, this.rPt, 3, 4, 9, true);
this.renderTriangle (this.rScr, this.rPt, 4, 0, 9, true);
});
});
