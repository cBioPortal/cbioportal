Clazz.declarePackage ("J.util");
Clazz.load (["J.util.P3", "$.P3i", "$.V3"], "J.util.BoxInfo", ["java.util.Hashtable", "J.util.JmolList", "$.Measure", "$.P4", "$.Point3fi", "$.TriangleData"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bbCorner0 = null;
this.bbCorner1 = null;
this.bbCenter = null;
this.bbVector = null;
this.bbVertices = null;
this.isScaleSet = false;
this.margin = 0;
Clazz.instantialize (this, arguments);
}, J.util, "BoxInfo");
Clazz.prepareFields (c$, function () {
this.bbCorner0 =  new J.util.P3 ();
this.bbCorner1 =  new J.util.P3 ();
this.bbCenter =  new J.util.P3 ();
this.bbVector =  new J.util.V3 ();
this.bbVertices =  new Array (8);
{
for (var i = 8; --i >= 0; ) this.bbVertices[i] =  new J.util.Point3fi ();

}{
for (var i = 0; i < 8; i++) {
J.util.BoxInfo.unitBboxPoints[i] = J.util.P3.new3 (-1, -1, -1);
J.util.BoxInfo.unitBboxPoints[i].scaleAdd2 (2, J.util.BoxInfo.unitCubePoints[i], J.util.BoxInfo.unitBboxPoints[i]);
}
}});
Clazz.makeConstructor (c$, 
function () {
this.reset ();
});
$_M(c$, "intersectPlane", 
function (plane, scale, flags) {
var v =  new J.util.JmolList ();
v.addLast (this.getCanonicalCopy (scale));
return J.util.TriangleData.intersectPlane (plane, v, flags);
}, "J.util.P4,~N,~N");
$_M(c$, "getCanonicalCopy", 
function (scale) {
return J.util.BoxInfo.getCanonicalCopy (this.bbVertices, scale);
}, "~N");
c$.getCanonicalCopy = $_M(c$, "getCanonicalCopy", 
function (bbUcPoints, scale) {
var pts =  new Array (8);
for (var i = 0; i < 8; i++) pts[J.util.BoxInfo.toCanonical[i]] = J.util.P3.newP (bbUcPoints[i]);

J.util.BoxInfo.scaleBox (pts, scale);
return pts;
}, "~A,~N");
c$.scaleBox = $_M(c$, "scaleBox", 
function (pts, scale) {
if (scale == 0 || scale == 1) return;
var center =  new J.util.P3 ();
var v =  new J.util.V3 ();
for (var i = 0; i < 8; i++) center.add (pts[i]);

center.scale (0.125);
for (var i = 0; i < 8; i++) {
v.sub2 (pts[i], center);
v.scale (scale);
pts[i].add2 (center, v);
}
}, "~A,~N");
c$.getFacesFromCriticalPoints = $_M(c$, "getFacesFromCriticalPoints", 
function (points) {
var faces =  new Array (6);
var vNorm =  new J.util.V3 ();
var vAB =  new J.util.V3 ();
var vAC =  new J.util.V3 ();
var va =  new J.util.P3 ();
var vb =  new J.util.P3 ();
var vc =  new J.util.P3 ();
var vertices =  new Array (8);
for (var i = 0; i < 8; i++) {
vertices[i] = J.util.P3.newP (points[0]);
if ((i & 1) == 1) vertices[i].add (points[1]);
if ((i & 2) == 2) vertices[i].add (points[2]);
if ((i & 4) == 4) vertices[i].add (points[3]);
}
for (var i = 0; i < 6; i++) {
va.setT (vertices[J.util.BoxInfo.facePoints[i].x]);
vb.setT (vertices[J.util.BoxInfo.facePoints[i].y]);
vc.setT (vertices[J.util.BoxInfo.facePoints[i].z]);
J.util.Measure.getPlaneThroughPoints (va, vb, vc, vNorm, vAB, vAC, faces[i] =  new J.util.P4 ());
}
return faces;
}, "~A");
c$.getCriticalPoints = $_M(c$, "getCriticalPoints", 
function (bbVertices, offset) {
var center = J.util.P3.newP (bbVertices[0]);
var a = J.util.P3.newP (bbVertices[1]);
var b = J.util.P3.newP (bbVertices[2]);
var c = J.util.P3.newP (bbVertices[4]);
a.sub (center);
b.sub (center);
c.sub (center);
if (offset != null) center.add (offset);
return [center, a, b, c];
}, "~A,J.util.Tuple3f");
$_M(c$, "getBoundBoxCenter", 
function () {
if (!this.isScaleSet) this.setBbcage (1);
return this.bbCenter;
});
$_M(c$, "getBoundBoxCornerVector", 
function () {
if (!this.isScaleSet) this.setBbcage (1);
return this.bbVector;
});
$_M(c$, "getBoundBoxPoints", 
function (isAll) {
if (!this.isScaleSet) this.setBbcage (1);
return (isAll ? [this.bbCenter, J.util.P3.newP (this.bbVector), this.bbCorner0, this.bbCorner1] : [this.bbCorner0, this.bbCorner1]);
}, "~B");
$_M(c$, "getBoundBoxVertices", 
function () {
if (!this.isScaleSet) this.setBbcage (1);
return this.bbVertices;
});
$_M(c$, "getBoundBoxInfo", 
function () {
if (!this.isScaleSet) this.setBbcage (1);
var info =  new java.util.Hashtable ();
info.put ("center", J.util.P3.newP (this.bbCenter));
info.put ("vector", J.util.V3.newV (this.bbVector));
info.put ("corner0", J.util.P3.newP (this.bbCorner0));
info.put ("corner1", J.util.P3.newP (this.bbCorner1));
return info;
});
$_M(c$, "setBoundBox", 
function (pt1, pt2, byCorner, scale) {
if (pt1 != null) {
if (scale == 0) return;
if (byCorner) {
if (pt1.distance (pt2) == 0) return;
this.bbCorner0.set (Math.min (pt1.x, pt2.x), Math.min (pt1.y, pt2.y), Math.min (pt1.z, pt2.z));
this.bbCorner1.set (Math.max (pt1.x, pt2.x), Math.max (pt1.y, pt2.y), Math.max (pt1.z, pt2.z));
} else {
if (pt2.x == 0 || pt2.y == 0 && pt2.z == 0) return;
this.bbCorner0.set (pt1.x - pt2.x, pt1.y - pt2.y, pt1.z - pt2.z);
this.bbCorner1.set (pt1.x + pt2.x, pt1.y + pt2.y, pt1.z + pt2.z);
}}this.setBbcage (scale);
}, "J.util.P3,J.util.P3,~B,~N");
$_M(c$, "reset", 
function () {
this.isScaleSet = false;
this.bbCorner0.set (3.4028235E38, 3.4028235E38, 3.4028235E38);
this.bbCorner1.set (-3.4028235E38, -3.4028235E38, -3.4028235E38);
});
$_M(c$, "setMargin", 
function (m) {
this.margin = m;
}, "~N");
$_M(c$, "addBoundBoxPoint", 
function (pt) {
this.isScaleSet = false;
J.util.BoxInfo.addPoint (pt, this.bbCorner0, this.bbCorner1, this.margin);
}, "J.util.P3");
c$.addPoint = $_M(c$, "addPoint", 
function (pt, xyzMin, xyzMax, margin) {
if (pt.x - margin < xyzMin.x) xyzMin.x = pt.x - margin;
if (pt.x + margin > xyzMax.x) xyzMax.x = pt.x + margin;
if (pt.y - margin < xyzMin.y) xyzMin.y = pt.y - margin;
if (pt.y + margin > xyzMax.y) xyzMax.y = pt.y + margin;
if (pt.z - margin < xyzMin.z) xyzMin.z = pt.z - margin;
if (pt.z + margin > xyzMax.z) xyzMax.z = pt.z + margin;
}, "J.util.P3,J.util.P3,J.util.P3,~N");
c$.addPointXYZ = $_M(c$, "addPointXYZ", 
function (x, y, z, xyzMin, xyzMax, margin) {
if (x - margin < xyzMin.x) xyzMin.x = x - margin;
if (x + margin > xyzMax.x) xyzMax.x = x + margin;
if (y - margin < xyzMin.y) xyzMin.y = y - margin;
if (y + margin > xyzMax.y) xyzMax.y = y + margin;
if (z - margin < xyzMin.z) xyzMin.z = z - margin;
if (z + margin > xyzMax.z) xyzMax.z = z + margin;
}, "~N,~N,~N,J.util.P3,J.util.P3,~N");
$_M(c$, "setBbcage", 
function (scale) {
this.isScaleSet = true;
this.bbCenter.add2 (this.bbCorner0, this.bbCorner1);
this.bbCenter.scale (0.5);
this.bbVector.sub2 (this.bbCorner1, this.bbCenter);
if (scale > 0) {
this.bbVector.scale (scale);
} else {
this.bbVector.x -= scale / 2;
this.bbVector.y -= scale / 2;
this.bbVector.z -= scale / 2;
}for (var i = 8; --i >= 0; ) {
var pt = this.bbVertices[i];
pt.setT (J.util.BoxInfo.unitBboxPoints[i]);
pt.x *= this.bbVector.x;
pt.y *= this.bbVector.y;
pt.z *= this.bbVector.z;
pt.add (this.bbCenter);
}
this.bbCorner0.setT (this.bbVertices[0]);
this.bbCorner1.setT (this.bbVertices[7]);
}, "~N");
$_M(c$, "isWithin", 
function (pt) {
if (!this.isScaleSet) this.setBbcage (1);
return (pt.x >= this.bbCorner0.x && pt.x <= this.bbCorner1.x && pt.y >= this.bbCorner0.y && pt.y <= this.bbCorner1.y && pt.z >= this.bbCorner0.z && pt.z <= this.bbCorner1.z);
}, "J.util.P3");
Clazz.defineStatics (c$,
"bbcageTickEdges", ['z', '\0', '\0', 'y', 'x', '\0', '\0', '\0', '\0', '\0', '\0', '\0'],
"uccageTickEdges", ['z', 'y', 'x', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0'],
"edges", [0, 1, 0, 2, 0, 4, 1, 3, 1, 5, 2, 3, 2, 6, 3, 7, 4, 5, 4, 6, 5, 7, 6, 7]);
c$.unitCubePoints = c$.prototype.unitCubePoints = [J.util.P3.new3 (0, 0, 0), J.util.P3.new3 (0, 0, 1), J.util.P3.new3 (0, 1, 0), J.util.P3.new3 (0, 1, 1), J.util.P3.new3 (1, 0, 0), J.util.P3.new3 (1, 0, 1), J.util.P3.new3 (1, 1, 0), J.util.P3.new3 (1, 1, 1)];
c$.facePoints = c$.prototype.facePoints = [J.util.P3i.new3 (4, 0, 6), J.util.P3i.new3 (4, 6, 5), J.util.P3i.new3 (5, 7, 1), J.util.P3i.new3 (1, 3, 0), J.util.P3i.new3 (6, 2, 7), J.util.P3i.new3 (1, 0, 5)];
Clazz.defineStatics (c$,
"toCanonical", [0, 3, 4, 7, 1, 2, 5, 6]);
c$.cubeVertexOffsets = c$.prototype.cubeVertexOffsets = [J.util.P3i.new3 (0, 0, 0), J.util.P3i.new3 (1, 0, 0), J.util.P3i.new3 (1, 0, 1), J.util.P3i.new3 (0, 0, 1), J.util.P3i.new3 (0, 1, 0), J.util.P3i.new3 (1, 1, 0), J.util.P3i.new3 (1, 1, 1), J.util.P3i.new3 (0, 1, 1)];
c$.unitBboxPoints = c$.prototype.unitBboxPoints =  new Array (8);
});
