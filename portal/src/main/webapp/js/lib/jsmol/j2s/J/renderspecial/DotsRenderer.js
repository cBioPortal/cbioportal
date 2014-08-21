Clazz.declarePackage ("J.renderspecial");
Clazz.load (["J.render.ShapeRenderer"], "J.renderspecial.DotsRenderer", ["JU.V3", "J.shapespecial.Dots", "JU.C", "$.Geodesic"], function () {
c$ = Clazz.decorateAsClass (function () {
this.iShowSolid = false;
this.verticesTransformed = null;
this.screenLevel = 0;
this.screenDotCount = 0;
this.screenCoordinates = null;
this.faceMap = null;
this.dotScale = 0;
this.testRadiusAdjust = 0;
Clazz.instantialize (this, arguments);
}, J.renderspecial, "DotsRenderer", J.render.ShapeRenderer);
Clazz.overrideMethod (c$, "initRenderer", 
function () {
this.screenLevel = J.shapespecial.Dots.MAX_LEVEL;
this.screenDotCount = JU.Geodesic.getVertexCount (J.shapespecial.Dots.MAX_LEVEL);
this.verticesTransformed =  new Array (this.screenDotCount);
for (var i = this.screenDotCount; --i >= 0; ) this.verticesTransformed[i] =  new JU.V3 ();

this.screenCoordinates =  Clazz.newIntArray (3 * this.screenDotCount, 0);
});
Clazz.overrideMethod (c$, "render", 
function () {
this.render1 (this.shape);
return false;
});
Clazz.defineMethod (c$, "render1", 
function (dots) {
if (!this.iShowSolid && !this.g3d.setC (4)) return;
var sppa = Clazz.floatToInt (this.vwr.getScalePixelsPerAngstrom (true));
this.screenLevel = (this.iShowSolid || sppa > 20 ? 3 : sppa > 10 ? 2 : sppa > 5 ? 1 : 0);
if (!this.iShowSolid) this.screenLevel += this.vwr.getInt (553648143) - 3;
this.screenLevel = Math.max (Math.min (this.screenLevel, J.shapespecial.Dots.MAX_LEVEL), 0);
this.screenDotCount = JU.Geodesic.getVertexCount (this.screenLevel);
this.dotScale = this.vwr.getInt (553648144);
for (var i = this.screenDotCount; --i >= 0; ) this.tm.transformVector (JU.Geodesic.getVertexVector (i), this.verticesTransformed[i]);

var maps = dots.ec.getDotsConvexMaps ();
for (var i = dots.ec.getDotsConvexMax (); --i >= 0; ) {
var atom = this.ms.at[i];
var map = maps[i];
if (map == null || !this.isVisibleForMe (atom) || !this.g3d.isInDisplayRange (atom.sX, atom.sY)) continue;
try {
var nPoints = this.calcScreenPoints (map, dots.ec.getAppropriateRadius (i) + this.testRadiusAdjust, atom.sX, atom.sY, atom.sZ);
if (nPoints != 0) this.renderConvex (JU.C.getColixInherited (dots.colixes[i], atom.getColix ()), map, nPoints);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println ("Dots rendering error");
System.out.println (e.toString ());
} else {
throw e;
}
}
}
}, "J.shapespecial.Dots");
Clazz.defineMethod (c$, "calcScreenPoints", 
 function (visibilityMap, radius, x, y, z) {
var nPoints = 0;
var i = 0;
var scaledRadius = this.vwr.tm.scaleToPerspective (z, radius);
var iDot = Math.min (visibilityMap.size (), this.screenDotCount);
while (--iDot >= 0) {
if (!visibilityMap.get (iDot)) continue;
var vertex = this.verticesTransformed[iDot];
if (this.faceMap != null) this.faceMap[iDot] = i;
this.screenCoordinates[i++] = x + Math.round (scaledRadius * vertex.x);
this.screenCoordinates[i++] = y + Math.round (scaledRadius * vertex.y);
this.screenCoordinates[i++] = z + Math.round (scaledRadius * vertex.z);
++nPoints;
}
return nPoints;
}, "JU.BS,~N,~N,~N,~N");
Clazz.defineMethod (c$, "renderConvex", 
function (colix, map, nPoints) {
this.colix = JU.C.getColixTranslucent3 (colix, false, 0);
this.renderDots (nPoints);
}, "~N,JU.BS,~N");
Clazz.defineMethod (c$, "renderDots", 
function (nPoints) {
this.g3d.setC (this.colix);
this.g3d.drawPoints (nPoints, this.screenCoordinates, this.dotScale);
}, "~N");
});
