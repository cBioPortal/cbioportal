Clazz.declarePackage ("J.render");
Clazz.load (["J.render.FontLineShapeRenderer", "J.util.P3"], "J.render.CageRenderer", ["J.util.BoxInfo", "$.Point3fi"], function () {
c$ = Clazz.decorateAsClass (function () {
this.screens = null;
this.tickEdges = null;
this.isSlab = false;
this.isPolymer = false;
this.pt = null;
Clazz.instantialize (this, arguments);
}, J.render, "CageRenderer", J.render.FontLineShapeRenderer);
Clazz.prepareFields (c$, function () {
this.screens =  new Array (8);
{
for (var i = 8; --i >= 0; ) this.screens[i] =  new J.util.P3 ();

}this.pt =  new J.util.P3 ();
});
$_M(c$, "renderCage", 
function (mad, vertices, axisPoints, firstLine, allowedEdges0, allowedEdges1, scale) {
this.g3d.setColix (this.colix);
var fls = this.shape;
this.imageFontScaling = this.viewer.getImageFontScaling ();
this.font3d = this.g3d.getFont3DScaled (fls.font3d, this.imageFontScaling);
var zSum = 0;
for (var i = 8; --i >= 0; ) {
this.pt.setT (vertices[i]);
if (scale != 1) {
this.pt.sub (vertices[0]);
this.pt.scaleAdd2 (scale, this.pt, vertices[0]);
}this.viewer.transformPtNoClip (this.pt, this.screens[i]);
zSum += this.screens[i].z;
}
var diameter = this.getDiameter (Clazz.doubleToInt (Math.floor (zSum / 8)), mad);
var axisPt = 2;
var edge = String.fromCharCode (0);
allowedEdges0 &= (this.isPolymer ? 0x1 : this.isSlab ? 0x55 : 0xFF);
allowedEdges1 &= (this.isPolymer ? 0x10 : this.isSlab ? 0x55 : 0xFF);
for (var i = firstLine * 2; i < 24; i += 2) {
var edge0 = J.util.BoxInfo.edges[i];
var edge1 = J.util.BoxInfo.edges[i + 1];
if (axisPoints != null && edge0 == 0) this.viewer.transformPtNoClip (axisPoints[axisPt--], this.screens[0]);
if ((allowedEdges0 & (1 << edge0)) == 0 || (allowedEdges1 & (1 << edge1)) == 0) continue;
var drawTicks = (fls.tickInfos != null && ((edge = this.tickEdges[i >> 1])).charCodeAt (0) != 0);
if (drawTicks) {
if (this.atomA == null) {
this.atomA =  new J.util.Point3fi ();
this.atomB =  new J.util.Point3fi ();
}this.atomA.setT (vertices[edge0]);
this.atomB.setT (vertices[edge1]);
var start = 0;
if (Clazz.instanceOf (this.shape, J.shape.Bbcage)) switch (edge) {
case 'x':
start = this.atomA.x;
break;
case 'y':
start = this.atomA.y;
break;
case 'z':
start = this.atomA.z;
break;
}
this.tickInfo = fls.tickInfos["xyz".indexOf (edge) + 1];
if (this.tickInfo == null) this.tickInfo = fls.tickInfos[0];
if (this.tickInfo == null) drawTicks = false;
 else this.tickInfo.first = start;
}this.renderLine (this.screens[edge0], this.screens[edge1], diameter, this.pt0i, this.pt1i, drawTicks);
}
}, "~N,~A,~A,~N,~N,~N,~N");
});
