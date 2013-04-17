Clazz.declarePackage ("J.g3d");
Clazz.load (["J.util.P3", "$.V3"], "J.g3d.HermiteRenderer", ["J.util.JmolList", "$.P3i", "$.Point3fi"], function () {
c$ = Clazz.decorateAsClass (function () {
this.g3d = null;
this.pLeft = null;
this.pRight = null;
this.sLeft = null;
this.sRight = null;
this.pTopLeft = null;
this.pTopRight = null;
this.pBotLeft = null;
this.pBotRight = null;
this.a1 = null;
this.a2 = null;
this.b1 = null;
this.b2 = null;
this.c1 = null;
this.c2 = null;
this.d1 = null;
this.d2 = null;
this.depth1 = null;
this.needToFill = null;
this.T1 = null;
this.T2 = null;
Clazz.instantialize (this, arguments);
}, J.g3d, "HermiteRenderer");
Clazz.prepareFields (c$, function () {
this.pLeft =  new Array (16);
this.pRight =  new Array (16);
this.sLeft =  Clazz.newFloatArray (16, 0);
this.sRight =  Clazz.newFloatArray (16, 0);
this.pTopLeft =  new Array (16);
this.pTopRight =  new Array (16);
this.pBotLeft =  new Array (16);
this.pBotRight =  new Array (16);
{
for (var i = 16; --i >= 0; ) {
this.pLeft[i] =  new J.util.P3i ();
this.pRight[i] =  new J.util.P3i ();
this.pTopLeft[i] =  new J.util.P3 ();
this.pTopRight[i] =  new J.util.P3 ();
this.pBotLeft[i] =  new J.util.P3 ();
this.pBotRight[i] =  new J.util.P3 ();
}
}this.a1 =  new J.util.P3 ();
this.a2 =  new J.util.P3 ();
this.b1 =  new J.util.P3 ();
this.b2 =  new J.util.P3 ();
this.c1 =  new J.util.P3 ();
this.c2 =  new J.util.P3 ();
this.d1 =  new J.util.P3 ();
this.d2 =  new J.util.P3 ();
this.depth1 =  new J.util.V3 ();
this.needToFill =  Clazz.newBooleanArray (16, false);
this.T1 =  new J.util.V3 ();
this.T2 =  new J.util.V3 ();
});
Clazz.makeConstructor (c$, 
function (g3d) {
this.g3d = g3d;
}, "J.api.JmolRendererInterface");
$_M(c$, "renderHermiteRope", 
function (fill, tension, diameterBeg, diameterMid, diameterEnd, p0, p1, p2, p3) {
if (p0.z == 1 || p1.z == 1 || p2.z == 1 || p3.z == 1) return;
if (this.g3d.isClippedZ (p1.z) || this.g3d.isClippedZ (p2.z)) return;
var x1 = p1.x;
var y1 = p1.y;
var z1 = p1.z;
var x2 = p2.x;
var y2 = p2.y;
var z2 = p2.z;
var xT1 = Clazz.doubleToInt (((x2 - p0.x) * tension) / 8);
var yT1 = Clazz.doubleToInt (((y2 - p0.y) * tension) / 8);
var zT1 = Clazz.doubleToInt (((z2 - p0.z) * tension) / 8);
var xT2 = Clazz.doubleToInt (((p3.x - x1) * tension) / 8);
var yT2 = Clazz.doubleToInt (((p3.y - y1) * tension) / 8);
var zT2 = Clazz.doubleToInt (((p3.z - z1) * tension) / 8);
this.sLeft[0] = 0;
this.pLeft[0].setT (p1);
this.sRight[0] = 1;
this.pRight[0].setT (p2);
var sp = 0;
var n = 0;
var dDiameterFirstHalf = 0;
var dDiameterSecondHalf = 0;
if (fill) {
dDiameterFirstHalf = 2 * (diameterMid - diameterBeg);
dDiameterSecondHalf = 2 * (diameterEnd - diameterMid);
}do {
var a = this.pLeft[sp];
var b = this.pRight[sp];
var dx = b.x - a.x;
if (dx >= -1 && dx <= 1) {
var dy = b.y - a.y;
if (dy >= -1 && dy <= 1) {
n++;
var s = this.sLeft[sp];
if (fill) {
var d = (s < 0.5 ? diameterBeg + Clazz.floatToInt (dDiameterFirstHalf * s) : diameterMid + Clazz.floatToInt (dDiameterSecondHalf * (s - 0.5)));
this.g3d.fillSphereI (d, a);
} else {
this.g3d.plotPixelClippedP3i (a);
}--sp;
continue;
}}var s = (this.sLeft[sp] + this.sRight[sp]) / 2;
var s2 = s * s;
var s3 = s2 * s;
var h1 = 2 * s3 - 3 * s2 + 1;
var h2 = -2 * s3 + 3 * s2;
var h3 = s3 - 2 * s2 + s;
var h4 = s3 - s2;
if (sp >= 15) break;
var pMid = this.pRight[sp + 1];
pMid.x = Clazz.doubleToInt (h1 * x1 + h2 * x2 + h3 * xT1 + h4 * xT2);
pMid.y = Clazz.doubleToInt (h1 * y1 + h2 * y2 + h3 * yT1 + h4 * yT2);
pMid.z = Clazz.doubleToInt (h1 * z1 + h2 * z2 + h3 * zT1 + h4 * zT2);
this.pRight[sp + 1] = this.pRight[sp];
this.sRight[sp + 1] = this.sRight[sp];
this.pRight[sp] = pMid;
this.sRight[sp] = s;
++sp;
this.pLeft[sp].setT (pMid);
this.sLeft[sp] = s;
} while (sp >= 0);
}, "~B,~N,~N,~N,~N,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i");
$_M(c$, "renderHermiteRibbon", 
function (fill, border, tension, p0, p1, p2, p3, p4, p5, p6, p7, aspectRatio, fillType) {
if (p0.z == 1 || p1.z == 1 || p2.z == 1 || p3.z == 1 || p4.z == 1 || p5.z == 1 || p6.z == 1 || p7.z == 1) return;
if (!fill) {
tension = Math.abs (tension);
this.renderParallelPair (fill, tension, p0, p1, p2, p3, p4, p5, p6, p7);
return;
}var isRev = (tension < 0);
if (isRev) tension = -tension;
var ratio = 1 / aspectRatio;
var x1 = p1.x;
var y1 = p1.y;
var z1 = p1.z;
var x2 = p2.x;
var y2 = p2.y;
var z2 = p2.z;
var xT1 = Clazz.doubleToInt (((x2 - p0.x) * tension) / 8);
var yT1 = Clazz.doubleToInt (((y2 - p0.y) * tension) / 8);
var zT1 = Clazz.doubleToInt (((z2 - p0.z) * tension) / 8);
var xT2 = Clazz.doubleToInt (((p3.x - x1) * tension) / 8);
var yT2 = Clazz.doubleToInt (((p3.y - y1) * tension) / 8);
var zT2 = Clazz.doubleToInt (((p3.z - z1) * tension) / 8);
J.util.Point3fi.set2 (this.pTopLeft[0], p1);
J.util.Point3fi.set2 (this.pTopRight[0], p2);
var x5 = p5.x;
var y5 = p5.y;
var z5 = p5.z;
var x6 = p6.x;
var y6 = p6.y;
var z6 = p6.z;
var xT5 = Clazz.doubleToInt (((x6 - p4.x) * tension) / 8);
var yT5 = Clazz.doubleToInt (((y6 - p4.y) * tension) / 8);
var zT5 = Clazz.doubleToInt (((z6 - p4.z) * tension) / 8);
var xT6 = Clazz.doubleToInt (((p7.x - x5) * tension) / 8);
var yT6 = Clazz.doubleToInt (((p7.y - y5) * tension) / 8);
var zT6 = Clazz.doubleToInt (((p7.z - z5) * tension) / 8);
J.util.Point3fi.set2 (this.pBotLeft[0], p5);
J.util.Point3fi.set2 (this.pBotRight[0], p6);
this.sLeft[0] = 0;
this.sRight[0] = 1;
this.needToFill[0] = true;
var sp = 0;
var closeEnd = false;
do {
var a = this.pTopLeft[sp];
var b = this.pTopRight[sp];
var dxTop = b.x - a.x;
var dxTop2 = dxTop * dxTop;
if (dxTop2 < 10) {
var dyTop = b.y - a.y;
var dyTop2 = dyTop * dyTop;
if (dyTop2 < 10) {
var c = this.pBotLeft[sp];
var d = this.pBotRight[sp];
var dxBot = d.x - c.x;
var dxBot2 = dxBot * dxBot;
if (dxBot2 < 8) {
var dyBot = d.y - c.y;
var dyBot2 = dyBot * dyBot;
if (dyBot2 < 8) {
if (border) {
this.g3d.fillSphere (3, a);
this.g3d.fillSphere (3, c);
}if (this.needToFill[sp]) {
if (aspectRatio > 0) {
this.setDepth (this.depth1, c, a, b, ratio);
J.g3d.HermiteRenderer.setPoint (this.a1, a, this.depth1, 1);
J.g3d.HermiteRenderer.setPoint (this.a2, a, this.depth1, -1);
J.g3d.HermiteRenderer.setPoint (this.b1, b, this.depth1, 1);
J.g3d.HermiteRenderer.setPoint (this.b2, b, this.depth1, -1);
J.g3d.HermiteRenderer.setPoint (this.c1, c, this.depth1, 1);
J.g3d.HermiteRenderer.setPoint (this.c2, c, this.depth1, -1);
J.g3d.HermiteRenderer.setPoint (this.d1, d, this.depth1, 1);
J.g3d.HermiteRenderer.setPoint (this.d2, d, this.depth1, -1);
this.g3d.fillQuadrilateral (this.a1, this.b1, this.d1, this.c1);
this.g3d.fillQuadrilateral (this.a2, this.b2, this.d2, this.c2);
this.g3d.fillQuadrilateral (this.a1, this.b1, this.b2, this.a2);
this.g3d.fillQuadrilateral (this.c1, this.d1, this.d2, this.c2);
closeEnd = true;
} else {
if (fillType == 0) {
if (isRev) this.g3d.fillQuadrilateral (c, d, b, a);
 else this.g3d.fillQuadrilateral (a, b, d, c);
} else {
if (isRev) {
if (fillType != J.g3d.HermiteRenderer.isFront (a, b, d)) this.g3d.fillTriangle3f (a, b, d, false);
if (fillType != J.g3d.HermiteRenderer.isFront (a, d, c)) this.g3d.fillTriangle3f (a, d, c, false);
} else {
if (fillType == J.g3d.HermiteRenderer.isFront (a, b, d)) this.g3d.fillTriangle3f (a, b, d, false);
if (fillType == J.g3d.HermiteRenderer.isFront (a, d, c)) this.g3d.fillTriangle3f (a, d, c, false);
}}}this.needToFill[sp] = false;
}if (dxTop2 + dyTop2 < 2 && dxBot2 + dyBot2 < 2) {
--sp;
continue;
}}}}}var s = (this.sLeft[sp] + this.sRight[sp]) / 2;
var s2 = s * s;
var s3 = s2 * s;
var h1 = 2 * s3 - 3 * s2 + 1;
var h2 = -2 * s3 + 3 * s2;
var h3 = s3 - 2 * s2 + s;
var h4 = s3 - s2;
if (sp >= 15) break;
var spNext = sp + 1;
var pMidTop = this.pTopRight[spNext];
pMidTop.x = (h1 * x1 + h2 * x2 + h3 * xT1 + h4 * xT2);
pMidTop.y = (h1 * y1 + h2 * y2 + h3 * yT1 + h4 * yT2);
pMidTop.z = (h1 * z1 + h2 * z2 + h3 * zT1 + h4 * zT2);
var pMidBot = this.pBotRight[spNext];
pMidBot.x = (h1 * x5 + h2 * x6 + h3 * xT5 + h4 * xT6);
pMidBot.y = (h1 * y5 + h2 * y6 + h3 * yT5 + h4 * yT6);
pMidBot.z = (h1 * z5 + h2 * z6 + h3 * zT5 + h4 * zT6);
this.pTopRight[spNext] = this.pTopRight[sp];
this.pTopRight[sp] = pMidTop;
this.pBotRight[spNext] = this.pBotRight[sp];
this.pBotRight[sp] = pMidBot;
this.sRight[spNext] = this.sRight[sp];
this.sRight[sp] = s;
this.needToFill[spNext] = this.needToFill[sp];
this.pTopLeft[spNext].setT (pMidTop);
this.pBotLeft[spNext].setT (pMidBot);
this.sLeft[spNext] = s;
++sp;
} while (sp >= 0);
if (closeEnd) {
this.a1.z += 1;
this.c1.z += 1;
this.c2.z += 1;
this.a2.z += 1;
this.g3d.fillQuadrilateral (this.a1, this.c1, this.c2, this.a2);
}}, "~B,~B,~N,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,~N,~N");
c$.isFront = $_M(c$, "isFront", 
($fz = function (a, b, c) {
J.g3d.HermiteRenderer.vAB.sub2 (b, a);
J.g3d.HermiteRenderer.vAC.sub2 (c, a);
J.g3d.HermiteRenderer.vAB.cross (J.g3d.HermiteRenderer.vAB, J.g3d.HermiteRenderer.vAC);
return (J.g3d.HermiteRenderer.vAB.z < 0 ? -1 : 1);
}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3,J.util.P3");
$_M(c$, "renderParallelPair", 
($fz = function (fill, tension, p0, p1, p2, p3, p4, p5, p6, p7) {
var endPoints = [p2, p1, p6, p5];
var points =  new J.util.JmolList ();
var whichPoint = 0;
var numTopStrandPoints = 2;
var numPointsPerSegment = 5.0;
var interval = (1.0 / numPointsPerSegment);
var currentInt = 0.0;
var x1 = p1.x;
var y1 = p1.y;
var z1 = p1.z;
var x2 = p2.x;
var y2 = p2.y;
var z2 = p2.z;
var xT1 = Clazz.doubleToInt (((x2 - p0.x) * tension) / 8);
var yT1 = Clazz.doubleToInt (((y2 - p0.y) * tension) / 8);
var zT1 = Clazz.doubleToInt (((z2 - p0.z) * tension) / 8);
var xT2 = Clazz.doubleToInt (((p3.x - x1) * tension) / 8);
var yT2 = Clazz.doubleToInt (((p3.y - y1) * tension) / 8);
var zT2 = Clazz.doubleToInt (((p3.z - z1) * tension) / 8);
this.sLeft[0] = 0;
this.pLeft[0].setT (p1);
this.sRight[0] = 1;
this.pRight[0].setT (p2);
var sp = 0;
for (var strands = 2; strands > 0; strands--) {
if (strands == 1) {
x1 = p5.x;
y1 = p5.y;
z1 = p5.z;
x2 = p6.x;
y2 = p6.y;
z2 = p6.z;
xT1 = Clazz.doubleToInt (((x2 - p4.x) * tension) / 8);
yT1 = Clazz.doubleToInt (((y2 - p4.y) * tension) / 8);
zT1 = Clazz.doubleToInt (((z2 - p4.z) * tension) / 8);
xT2 = Clazz.doubleToInt (((p7.x - x1) * tension) / 8);
yT2 = Clazz.doubleToInt (((p7.y - y1) * tension) / 8);
zT2 = Clazz.doubleToInt (((p7.z - z1) * tension) / 8);
this.sLeft[0] = 0;
this.pLeft[0].setT (p5);
this.sRight[0] = 1;
this.pRight[0].setT (p6);
sp = 0;
}points.addLast (endPoints[whichPoint++]);
currentInt = interval;
do {
var a = this.pLeft[sp];
var b = this.pRight[sp];
var dx = b.x - a.x;
var dy = b.y - a.y;
var dist2 = dx * dx + dy * dy;
if (dist2 <= 2) {
var s = this.sLeft[sp];
this.g3d.fillSphereI (3, a);
if (s < 1.0 - currentInt) {
var temp =  new J.util.P3i ();
temp.setT (a);
points.addLast (temp);
currentInt += interval;
if (strands == 2) {
numTopStrandPoints++;
}}--sp;
} else {
var s = (this.sLeft[sp] + this.sRight[sp]) / 2;
var s2 = s * s;
var s3 = s2 * s;
var h1 = 2 * s3 - 3 * s2 + 1;
var h2 = -2 * s3 + 3 * s2;
var h3 = s3 - 2 * s2 + s;
var h4 = s3 - s2;
if (sp >= 15) break;
var pMid = this.pRight[sp + 1];
pMid.x = Clazz.doubleToInt (h1 * x1 + h2 * x2 + h3 * xT1 + h4 * xT2);
pMid.y = Clazz.doubleToInt (h1 * y1 + h2 * y2 + h3 * yT1 + h4 * yT2);
pMid.z = Clazz.doubleToInt (h1 * z1 + h2 * z2 + h3 * zT1 + h4 * zT2);
this.pRight[sp + 1] = this.pRight[sp];
this.sRight[sp + 1] = this.sRight[sp];
this.pRight[sp] = pMid;
this.sRight[sp] = s;
++sp;
this.pLeft[sp].setT (pMid);
this.sLeft[sp] = s;
}} while (sp >= 0);
points.addLast (endPoints[whichPoint++]);
}
var size = points.size ();
for (var top = 0; top < numTopStrandPoints && (top + numTopStrandPoints) < size; top++) this.g3d.drawLineAB (points.get (top), points.get (top + numTopStrandPoints));

}, $fz.isPrivate = true, $fz), "~B,~N,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i");
$_M(c$, "setDepth", 
($fz = function (depth, c, a, b, ratio) {
this.T1.sub2 (a, c);
this.T1.scale (ratio);
this.T2.sub2 (a, b);
depth.cross (this.T1, this.T2);
depth.scale (this.T1.length () / depth.length ());
}, $fz.isPrivate = true, $fz), "J.util.V3,J.util.P3,J.util.P3,J.util.P3,~N");
c$.setPoint = $_M(c$, "setPoint", 
($fz = function (a1, a, depth, direction) {
a1.setT (a);
if (direction == 1) a1.add (depth);
 else a1.sub (depth);
}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3,J.util.V3,~N");
c$.vAB = c$.prototype.vAB =  new J.util.V3 ();
c$.vAC = c$.prototype.vAC =  new J.util.V3 ();
});
