Clazz.declarePackage ("J.util");
Clazz.load (["J.util.ArrayUtil"], "J.util.Geodesic", ["java.lang.NullPointerException", "$.Short", "java.util.Hashtable", "J.util.V3"], function () {
c$ = Clazz.declareType (J.util, "Geodesic");
c$.getNeighborVertexesArrays = $_M(c$, "getNeighborVertexesArrays", 
function () {
if (J.util.Geodesic.vertexCounts == null) J.util.Geodesic.createGeodesic (3);
return J.util.Geodesic.neighborVertexesArrays;
});
c$.getVertexCount = $_M(c$, "getVertexCount", 
function (level) {
if (J.util.Geodesic.vertexCounts == null) J.util.Geodesic.createGeodesic (3);
return J.util.Geodesic.vertexCounts[level];
}, "~N");
c$.getVertexVectors = $_M(c$, "getVertexVectors", 
function () {
if (J.util.Geodesic.vertexCounts == null) J.util.Geodesic.createGeodesic (3);
return J.util.Geodesic.vertexVectors;
});
c$.getVertexVector = $_M(c$, "getVertexVector", 
function (i) {
return J.util.Geodesic.vertexVectors[i];
}, "~N");
c$.getFaceVertexes = $_M(c$, "getFaceVertexes", 
function (level) {
return J.util.Geodesic.faceVertexesArrays[level];
}, "~N");
c$.createGeodesic = $_M(c$, "createGeodesic", 
function (lvl) {
if (lvl < J.util.Geodesic.currentLevel) return;
($t$ = J.util.Geodesic.currentLevel = lvl, J.util.Geodesic.prototype.currentLevel = J.util.Geodesic.currentLevel, $t$);
var v =  Clazz.newShortArray (lvl + 1, 0);
($t$ = J.util.Geodesic.neighborVertexesArrays = J.util.ArrayUtil.newShort2 (lvl + 1), J.util.Geodesic.prototype.neighborVertexesArrays = J.util.Geodesic.neighborVertexesArrays, $t$);
($t$ = J.util.Geodesic.faceVertexesArrays = J.util.ArrayUtil.newShort2 (lvl + 1), J.util.Geodesic.prototype.faceVertexesArrays = J.util.Geodesic.faceVertexesArrays, $t$);
($t$ = J.util.Geodesic.vertexVectors =  new Array (12), J.util.Geodesic.prototype.vertexVectors = J.util.Geodesic.vertexVectors, $t$);
J.util.Geodesic.vertexVectors[0] = J.util.V3.new3 (0, 0, J.util.Geodesic.halfRoot5);
for (var i = 0; i < 5; ++i) {
J.util.Geodesic.vertexVectors[i + 1] = J.util.V3.new3 (Math.cos (i * 1.2566371), Math.sin (i * 1.2566371), 0.5);
J.util.Geodesic.vertexVectors[i + 6] = J.util.V3.new3 (Math.cos (i * 1.2566371 + 0.62831855), Math.sin (i * 1.2566371 + 0.62831855), -0.5);
}
J.util.Geodesic.vertexVectors[11] = J.util.V3.new3 (0, 0, ($t$ = - J.util.Geodesic.halfRoot5, J.util.Geodesic.prototype.halfRoot5 = J.util.Geodesic.halfRoot5, $t$));
for (var i = 12; --i >= 0; ) J.util.Geodesic.vertexVectors[i].normalize ();

J.util.Geodesic.faceVertexesArrays[0] = J.util.Geodesic.faceVertexesIcosahedron;
J.util.Geodesic.neighborVertexesArrays[0] = J.util.Geodesic.neighborVertexesIcosahedron;
v[0] = 12;
for (var i = 0; i < lvl; ++i) J.util.Geodesic.quadruple (i, v);

($t$ = J.util.Geodesic.vertexCounts = v, J.util.Geodesic.prototype.vertexCounts = J.util.Geodesic.vertexCounts, $t$);
}, "~N");
c$.quadruple = $_M(c$, "quadruple", 
($fz = function (level, counts) {
($t$ = J.util.Geodesic.htVertex =  new java.util.Hashtable (), J.util.Geodesic.prototype.htVertex = J.util.Geodesic.htVertex, $t$);
var oldVertexCount = J.util.Geodesic.vertexVectors.length;
var oldFaceVertexes = J.util.Geodesic.faceVertexesArrays[level];
var oldFaceVertexesLength = oldFaceVertexes.length;
var oldFaceCount = Clazz.doubleToInt (oldFaceVertexesLength / 3);
var oldEdgesCount = oldVertexCount + oldFaceCount - 2;
var newVertexCount = oldVertexCount + oldEdgesCount;
var newFaceCount = 4 * oldFaceCount;
($t$ = J.util.Geodesic.vertexVectors = J.util.ArrayUtil.arrayCopyObject (J.util.Geodesic.vertexVectors, newVertexCount), J.util.Geodesic.prototype.vertexVectors = J.util.Geodesic.vertexVectors, $t$);
var newFacesVertexes =  Clazz.newShortArray (3 * newFaceCount, 0);
J.util.Geodesic.faceVertexesArrays[level + 1] = newFacesVertexes;
var neighborVertexes =  Clazz.newShortArray (6 * newVertexCount, 0);
J.util.Geodesic.neighborVertexesArrays[level + 1] = neighborVertexes;
for (var i = neighborVertexes.length; --i >= 0; ) neighborVertexes[i] = -1;

counts[level + 1] = newVertexCount;
($t$ = J.util.Geodesic.vertexNext = oldVertexCount, J.util.Geodesic.prototype.vertexNext = J.util.Geodesic.vertexNext, $t$);
var iFaceNew = 0;
for (var i = 0; i < oldFaceVertexesLength; ) {
var iA = oldFaceVertexes[i++];
var iB = oldFaceVertexes[i++];
var iC = oldFaceVertexes[i++];
var iAB = J.util.Geodesic.getVertex (iA, iB);
var iBC = J.util.Geodesic.getVertex (iB, iC);
var iCA = J.util.Geodesic.getVertex (iC, iA);
newFacesVertexes[iFaceNew++] = iA;
newFacesVertexes[iFaceNew++] = iAB;
newFacesVertexes[iFaceNew++] = iCA;
newFacesVertexes[iFaceNew++] = iB;
newFacesVertexes[iFaceNew++] = iBC;
newFacesVertexes[iFaceNew++] = iAB;
newFacesVertexes[iFaceNew++] = iC;
newFacesVertexes[iFaceNew++] = iCA;
newFacesVertexes[iFaceNew++] = iBC;
newFacesVertexes[iFaceNew++] = iCA;
newFacesVertexes[iFaceNew++] = iAB;
newFacesVertexes[iFaceNew++] = iBC;
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iAB, iA);
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iAB, iCA);
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iAB, iBC);
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iAB, iB);
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iBC, iB);
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iBC, iCA);
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iBC, iC);
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iCA, iC);
J.util.Geodesic.addNeighboringVertexes (neighborVertexes, iCA, iA);
}
if (true) {
var vertexCount = J.util.Geodesic.vertexVectors.length;
if (iFaceNew != newFacesVertexes.length) throw  new NullPointerException ();
if (J.util.Geodesic.vertexNext != newVertexCount) throw  new NullPointerException ();
for (var i = 0; i < 12; ++i) {
for (var j = 0; j < 5; ++j) {
var neighbor = neighborVertexes[i * 6 + j];
if (neighbor < 0) throw  new NullPointerException ();
if (neighbor >= vertexCount) throw  new NullPointerException ();
if (neighborVertexes[i * 6 + 5] != -1) throw  new NullPointerException ();
}
}
for (var i = 72; i < neighborVertexes.length; ++i) {
var neighbor = neighborVertexes[i];
if (neighbor < 0) throw  new NullPointerException ();
if (neighbor >= vertexCount) throw  new NullPointerException ();
}
for (var i = 0; i < newVertexCount; ++i) {
var neighborCount = 0;
for (var j = neighborVertexes.length; --j >= 0; ) if (neighborVertexes[j] == i) ++neighborCount;

if ((i < 12 && neighborCount != 5) || (i >= 12 && neighborCount != 6)) throw  new NullPointerException ();
var faceCount = 0;
for (var j = newFacesVertexes.length; --j >= 0; ) if (newFacesVertexes[j] == i) ++faceCount;

if ((i < 12 && faceCount != 5) || (i >= 12 && faceCount != 6)) throw  new NullPointerException ();
}
}($t$ = J.util.Geodesic.htVertex = null, J.util.Geodesic.prototype.htVertex = J.util.Geodesic.htVertex, $t$);
}, $fz.isPrivate = true, $fz), "~N,~A");
c$.addNeighboringVertexes = $_M(c$, "addNeighboringVertexes", 
($fz = function (neighborVertexes, v1, v2) {
for (var i = v1 * 6, iMax = i + 6; i < iMax; ++i) {
if (neighborVertexes[i] == v2) return;
if (neighborVertexes[i] < 0) {
neighborVertexes[i] = v2;
for (var j = v2 * 6, jMax = j + 6; j < jMax; ++j) {
if (neighborVertexes[j] == v1) return;
if (neighborVertexes[j] < 0) {
neighborVertexes[j] = v1;
return;
}}
}}
throw  new NullPointerException ();
}, $fz.isPrivate = true, $fz), "~A,~N,~N");
c$.getVertex = $_M(c$, "getVertex", 
($fz = function (v1, v2) {
if (v1 > v2) {
var t = v1;
v1 = v2;
v2 = t;
}var hashKey = Integer.$valueOf ((v1 << 16) + v2);
var iv = J.util.Geodesic.htVertex.get (hashKey);
if (iv != null) {
return iv.shortValue ();
}var newVertexVector = J.util.V3.newV (J.util.Geodesic.vertexVectors[v1]);
J.util.Geodesic.vertexVectors[J.util.Geodesic.vertexNext] = newVertexVector;
newVertexVector.add (J.util.Geodesic.vertexVectors[v2]);
newVertexVector.scale (0.5);
newVertexVector.normalize ();
J.util.Geodesic.htVertex.put (hashKey, Short.$valueOf (J.util.Geodesic.vertexNext));
return ($t$ = J.util.Geodesic.vertexNext ++, J.util.Geodesic.prototype.vertexNext = J.util.Geodesic.vertexNext, $t$);
}, $fz.isPrivate = true, $fz), "~N,~N");
c$.halfRoot5 = c$.prototype.halfRoot5 = (0.5 * Math.sqrt (5));
Clazz.defineStatics (c$,
"oneFifth", 1.2566371,
"oneTenth", 0.62831855,
"faceVertexesIcosahedron", [0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 5, 0, 5, 1, 1, 6, 2, 2, 7, 3, 3, 8, 4, 4, 9, 5, 5, 10, 1, 6, 1, 10, 7, 2, 6, 8, 3, 7, 9, 4, 8, 10, 5, 9, 11, 6, 10, 11, 7, 6, 11, 8, 7, 11, 9, 8, 11, 10, 9],
"neighborVertexesIcosahedron", [1, 2, 3, 4, 5, -1, 0, 5, 10, 6, 2, -1, 0, 1, 6, 7, 3, -1, 0, 2, 7, 8, 4, -1, 0, 3, 8, 9, 5, -1, 0, 4, 9, 10, 1, -1, 1, 10, 11, 7, 2, -1, 2, 6, 11, 8, 3, -1, 3, 7, 11, 9, 4, -1, 4, 8, 11, 10, 5, -1, 5, 9, 11, 6, 1, -1, 6, 7, 8, 9, 10, -1],
"standardLevel", 3,
"maxLevel", 3,
"vertexCounts", null,
"vertexVectors", null,
"faceVertexesArrays", null,
"neighborVertexesArrays", null,
"currentLevel", 0,
"vertexNext", 0,
"htVertex", null,
"VALIDATE", true);
});
