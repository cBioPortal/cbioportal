Clazz.declarePackage ("J.jvxl.calc");
Clazz.load (["J.util.P3", "java.util.Hashtable"], "J.jvxl.calc.MarchingSquares", ["java.lang.Float", "J.util.ArrayUtil", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.surfaceReader = null;
this.volumeData = null;
this.nContourSegments = 0;
this.contourType = 0;
this.thisContour = 0;
this.valueMin = 0;
this.valueMax = 0;
this.pointA = null;
this.pointB = null;
this.contourFromZero = true;
this.contoursDiscrete = null;
this.contourVertexCount = 0;
this.contourVertexes = null;
this.contourPlaneMinimumValue = 0;
this.contourPlaneMaximumValue = 0;
this.contourValuesUsed = null;
this.ptTemp = null;
this.triangleCount = 0;
this.triangles = null;
this.htPts = null;
if (!Clazz.isClassDefined ("J.jvxl.calc.MarchingSquares.Triangle")) {
J.jvxl.calc.MarchingSquares.$MarchingSquares$Triangle$ ();
}
Clazz.instantialize (this, arguments);
}, J.jvxl.calc, "MarchingSquares");
Clazz.prepareFields (c$, function () {
this.pointA =  new J.util.P3 ();
this.pointB =  new J.util.P3 ();
this.contourVertexes =  new Array (1000);
this.ptTemp =  new J.util.P3 ();
this.triangles =  new Array (1000);
this.htPts =  new java.util.Hashtable ();
});
Clazz.makeConstructor (c$, 
function (surfaceReader, volumeData, thePlane, contoursDiscrete, nContours, thisContour, contourFromZero) {
this.surfaceReader = surfaceReader;
this.volumeData = volumeData;
this.thisContour = thisContour;
this.contoursDiscrete = contoursDiscrete;
this.contourFromZero = contourFromZero;
if (contoursDiscrete == null) {
var i = 0;
this.nContourSegments = (nContours == 0 ? 9 : nContours) + i;
if (this.nContourSegments > 100) this.nContourSegments = 100;
} else {
nContours = contoursDiscrete.length;
this.nContourSegments = nContours;
this.contourFromZero = false;
}}, "J.jvxl.api.VertexDataServer,J.jvxl.data.VolumeData,J.util.P4,~A,~N,~N,~B");
$_M(c$, "getContourType", 
function () {
return this.contourType;
});
$_M(c$, "setMinMax", 
function (valueMin, valueMax) {
this.valueMin = valueMin;
this.valueMax = valueMax;
}, "~N,~N");
$_M(c$, "addContourVertex", 
function (vertexXYZ, value) {
if (this.contourVertexCount == this.contourVertexes.length) this.contourVertexes = J.util.ArrayUtil.doubleLength (this.contourVertexes);
var vPt = this.surfaceReader.addVertexCopy (vertexXYZ, value, -2);
this.contourVertexes[this.contourVertexCount++] =  new J.jvxl.calc.MarchingSquares.ContourVertex (vertexXYZ);
return vPt;
}, "J.util.P3,~N");
$_M(c$, "setContourData", 
function (i, value) {
this.contourVertexes[i].setValue (value);
}, "~N,~N");
$_M(c$, "getContourValues", 
function () {
return this.contourValuesUsed;
});
$_M(c$, "calcContourPoint", 
function (cutoff, valueA, valueB, pt) {
return this.volumeData.calculateFractionalPoint (cutoff, this.pointA, this.pointB, valueA, valueB, pt);
}, "~N,~N,~N,J.util.P3");
$_M(c$, "addTriangle", 
function (iA, iB, iC, check, check2) {
if (this.triangleCount == this.triangles.length) this.triangles = J.util.ArrayUtil.doubleLength (this.triangles);
this.triangles[this.triangleCount++] = Clazz.innerTypeInstance (J.jvxl.calc.MarchingSquares.Triangle, this, null, iA, iB, iC, check, check2);
return 0;
}, "~N,~N,~N,~N,~N");
$_M(c$, "generateContourData", 
function (haveData, zeroOffset) {
J.util.Logger.info ("generateContours: " + this.nContourSegments + " segments");
this.getVertexValues (haveData);
this.createContours (this.valueMin, this.valueMax, zeroOffset);
this.addAllTriangles ();
return this.contourVertexCount;
}, "~B,~N");
$_M(c$, "getVertexValues", 
($fz = function (haveData) {
this.contourPlaneMinimumValue = 3.4028235E38;
this.contourPlaneMaximumValue = -3.4028235E38;
for (var i = 0; i < this.contourVertexCount; i++) {
var c = this.contourVertexes[i];
var value;
if (haveData) {
value = c.value;
} else {
value = this.volumeData.lookupInterpolatedVoxelValue (c, false);
c.setValue (value);
}if (value < this.contourPlaneMinimumValue) this.contourPlaneMinimumValue = value;
if (value > this.contourPlaneMaximumValue) this.contourPlaneMaximumValue = value;
}
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "createContours", 
($fz = function (min, max, zeroOffset) {
var diff = max - min;
this.contourValuesUsed =  Clazz.newFloatArray (this.nContourSegments, 0);
for (var i = this.triangleCount; --i >= 0; ) this.triangles[i].check = 0;

var minCutoff = -3.4028235E38;
var cutoff = minCutoff;
for (var i = 0; i < this.nContourSegments; i++) {
cutoff = (this.contoursDiscrete != null ? this.contoursDiscrete[i] : this.contourFromZero ? min + (i * 1 / this.nContourSegments) * diff : i == 0 ? -3.4028235E38 : i == this.nContourSegments - 1 ? 3.4028235E38 : min + ((i - 1) * 1 / (this.nContourSegments - 1)) * diff);
if (this.contoursDiscrete == null && Math.abs (cutoff) < zeroOffset) cutoff = (cutoff < 0 ? -zeroOffset : zeroOffset);
this.contourValuesUsed[i] = cutoff;
J.util.Logger.info ("#contour " + (i + 1) + " " + cutoff);
var n = 0;
this.htPts.clear ();
for (var ii = this.triangleCount; --ii >= 0; ) {
if (this.triangles[ii].isValid) this.triangles[ii].checkContour (i, cutoff);
 else n++;
}
if (this.thisContour > 0) {
if (i + 1 == this.thisContour) minCutoff = cutoff;
} else {
}}
if (this.contoursDiscrete != null) {
minCutoff = this.contoursDiscrete[0];
}this.valueMin = this.contourValuesUsed[0];
this.valueMax = (this.contourValuesUsed.length == 0 ? this.valueMin : this.contourValuesUsed[this.contourValuesUsed.length - 1]);
return true;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "getMinMax", 
function () {
return [this.valueMin, this.valueMax];
});
$_M(c$, "addAllTriangles", 
($fz = function () {
for (var i = 0; i < this.triangleCount; i++) if (this.triangles[i].isValid) {
var t = this.triangles[i];
this.surfaceReader.addTriangleCheck (t.pts[0], t.pts[1], t.pts[2], t.check, t.contourIndex, false, -1);
}
}, $fz.isPrivate = true, $fz));
c$.$MarchingSquares$Triangle$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.pts = null;
this.check = 0;
this.isValid = true;
this.contourIndex = 0;
Clazz.instantialize (this, arguments);
}, J.jvxl.calc.MarchingSquares, "Triangle");
Clazz.makeConstructor (c$, 
function (a, b, c, d, e) {
this.pts = [a, b, c];
this.check = d;
this.contourIndex = e;
}, "~N,~N,~N,~N,~N");
$_M(c$, "intercept", 
($fz = function (a, b) {
var c = this.pts[a];
var d = this.pts[(a + 1) % 3];
if (c == 2147483647 || d == 2147483647) return -1;
var e = (c < d ? c + "_" + d : d + "_" + c);
if (this.b$["J.jvxl.calc.MarchingSquares"].htPts.containsKey (e)) return this.b$["J.jvxl.calc.MarchingSquares"].htPts.get (e).intValue ();
var f = this.b$["J.jvxl.calc.MarchingSquares"].contourVertexes[c].value;
var g = this.b$["J.jvxl.calc.MarchingSquares"].contourVertexes[d].value;
var h = -1;
if (f != g) {
var i = (b - f) / (g - f);
if (i >= 0 && i <= 1) {
this.b$["J.jvxl.calc.MarchingSquares"].pointA.setT (this.b$["J.jvxl.calc.MarchingSquares"].contourVertexes[c]);
this.b$["J.jvxl.calc.MarchingSquares"].pointB.setT (this.b$["J.jvxl.calc.MarchingSquares"].contourVertexes[d]);
b = this.b$["J.jvxl.calc.MarchingSquares"].calcContourPoint (b, f, g, this.b$["J.jvxl.calc.MarchingSquares"].ptTemp);
if (!Float.isNaN (b)) {
h = this.b$["J.jvxl.calc.MarchingSquares"].addContourVertex (this.b$["J.jvxl.calc.MarchingSquares"].ptTemp, b);
if (h < 0) return -1;
this.b$["J.jvxl.calc.MarchingSquares"].contourVertexes[h].setValue (b);
} else {
}}}this.b$["J.jvxl.calc.MarchingSquares"].htPts.put (e, Integer.$valueOf (h));
return h;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "checkContour", 
function (a, b) {
var c = this.intercept (0, b);
var d = this.intercept (1, b);
var e = this.intercept (2, b);
var f = 0;
if (c >= 0) {
f += 1;
}if (d >= 0) {
f += 2;
}if (e >= 0) {
f += 4;
}switch (f) {
case 3:
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (this.pts[0], c, d, 2 | (this.check & 1), a);
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (c, this.pts[1], d, 4 | (this.check & 3), a);
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (this.pts[0], d, this.pts[2], (this.check & 6), a);
break;
case 5:
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (this.pts[0], c, e, 2 | (this.check & 5), a);
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (c, this.pts[1], e, 4 | (this.check & 1), a);
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (e, this.pts[1], this.pts[2], (this.check & 6), a);
break;
case 6:
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (this.pts[0], this.pts[1], e, (this.check & 5), a);
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (e, this.pts[1], d, 4 | (this.check & 2), a);
this.b$["J.jvxl.calc.MarchingSquares"].addTriangle (e, d, this.pts[2], 1 | (this.check & 6), a);
break;
default:
return;
}
this.isValid = false;
}, "~N,~N");
c$ = Clazz.p0p ();
};
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
this.value = 0;
Clazz.instantialize (this, arguments);
}, J.jvxl.calc.MarchingSquares, "ContourVertex", J.util.P3);
Clazz.makeConstructor (c$, 
function (a) {
Clazz.superConstructor (this, J.jvxl.calc.MarchingSquares.ContourVertex, []);
this.setT (a);
}, "J.util.P3");
$_M(c$, "setValue", 
function (a) {
this.value = a;
}, "~N");
c$ = Clazz.p0p ();
Clazz.defineStatics (c$,
"CONTOUR_POINT", -1,
"VERTEX_POINT", -2,
"EDGE_POINT", -3,
"nContourMax", 100,
"defaultContourCount", 9);
});
