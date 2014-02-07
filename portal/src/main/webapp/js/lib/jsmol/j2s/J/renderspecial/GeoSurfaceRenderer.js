Clazz.declarePackage ("J.renderspecial");
Clazz.load (["J.renderspecial.DotsRenderer", "J.util.P3i"], "J.renderspecial.GeoSurfaceRenderer", ["J.util.Geodesic"], function () {
c$ = Clazz.decorateAsClass (function () {
this.facePt1 = null;
this.facePt2 = null;
this.facePt3 = null;
Clazz.instantialize (this, arguments);
}, J.renderspecial, "GeoSurfaceRenderer", J.renderspecial.DotsRenderer);
Clazz.prepareFields (c$, function () {
this.facePt1 =  new J.util.P3i ();
this.facePt2 =  new J.util.P3i ();
this.facePt3 =  new J.util.P3i ();
});
Clazz.overrideMethod (c$, "render", 
function () {
var gs = this.shape;
this.iShowSolid = !(!this.viewer.checkMotionRendering (1113198597) && gs.ec.getDotsConvexMax () > 100);
if (!this.iShowSolid) return false;
if (!this.g3d.setColix (4)) return true;
if (this.iShowSolid && this.faceMap == null) this.faceMap =  Clazz.newIntArray (this.screenDotCount, 0);
this.render1 (gs);
return false;
});
Clazz.overrideMethod (c$, "renderConvex", 
function (colix, visibilityMap, nPoints) {
this.colix = colix;
if (this.iShowSolid) {
if (this.g3d.setColix (colix)) this.renderSurface (visibilityMap);
return;
}this.renderDots (nPoints);
}, "~N,J.util.BS,~N");
$_M(c$, "renderSurface", 
($fz = function (points) {
if (this.faceMap == null) return;
var faces = J.util.Geodesic.getFaceVertexes (this.screenLevel);
var coords = this.screenCoordinates;
var p1;
var p2;
var p3;
var mapMax = points.size ();
if (this.screenDotCount < mapMax) mapMax = this.screenDotCount;
for (var f = 0; f < faces.length; ) {
p1 = faces[f++];
p2 = faces[f++];
p3 = faces[f++];
if (p1 >= mapMax || p2 >= mapMax || p3 >= mapMax) continue;
if (!points.get (p1) || !points.get (p2) || !points.get (p3)) continue;
this.facePt1.set (coords[this.faceMap[p1]], coords[this.faceMap[p1] + 1], coords[this.faceMap[p1] + 2]);
this.facePt2.set (coords[this.faceMap[p2]], coords[this.faceMap[p2] + 1], coords[this.faceMap[p2] + 2]);
this.facePt3.set (coords[this.faceMap[p3]], coords[this.faceMap[p3] + 1], coords[this.faceMap[p3] + 2]);
this.g3d.fillTriangle3CN (this.facePt1, this.colix, p1, this.facePt2, this.colix, p2, this.facePt3, this.colix, p3);
}
}, $fz.isPrivate = true, $fz), "J.util.BS");
});
