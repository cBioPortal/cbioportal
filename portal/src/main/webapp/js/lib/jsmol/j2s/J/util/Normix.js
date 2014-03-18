Clazz.declarePackage ("J.util");
Clazz.load (["J.util.Geodesic"], "J.util.Normix", ["J.util.BS"], function () {
c$ = Clazz.declareType (J.util, "Normix");
c$.getNormixCount = $_M(c$, "getNormixCount", 
function () {
if (J.util.Normix.normixCount == 0) ($t$ = J.util.Normix.normixCount = J.util.Geodesic.getVertexCount (3), J.util.Normix.prototype.normixCount = J.util.Normix.normixCount, $t$);
return J.util.Normix.normixCount;
});
c$.newVertexBitSet = $_M(c$, "newVertexBitSet", 
function () {
return J.util.BS.newN (J.util.Normix.getNormixCount ());
});
c$.getVertexVectors = $_M(c$, "getVertexVectors", 
function () {
if (J.util.Normix.vertexVectors == null) ($t$ = J.util.Normix.vertexVectors = J.util.Geodesic.getVertexVectors (), J.util.Normix.prototype.vertexVectors = J.util.Normix.vertexVectors, $t$);
return J.util.Normix.vertexVectors;
});
c$.setInverseNormixes = $_M(c$, "setInverseNormixes", 
function () {
if (J.util.Normix.inverseNormixes != null) return;
J.util.Normix.getNormixCount ();
J.util.Normix.getVertexVectors ();
($t$ = J.util.Normix.inverseNormixes =  Clazz.newShortArray (J.util.Normix.normixCount, 0), J.util.Normix.prototype.inverseNormixes = J.util.Normix.inverseNormixes, $t$);
var bsTemp =  new J.util.BS ();
for (var n = J.util.Normix.normixCount; --n >= 0; ) {
var v = J.util.Normix.vertexVectors[n];
J.util.Normix.inverseNormixes[n] = J.util.Normix.getNormix (-v.x, -v.y, -v.z, 3, bsTemp);
}
});
c$.getInverseNormix = $_M(c$, "getInverseNormix", 
function (normix) {
return J.util.Normix.inverseNormixes[normix];
}, "~N");
c$.getNeighborVertexArrays = $_M(c$, "getNeighborVertexArrays", 
($fz = function () {
if (J.util.Normix.neighborVertexesArrays == null) {
($t$ = J.util.Normix.neighborVertexesArrays = J.util.Geodesic.getNeighborVertexesArrays (), J.util.Normix.prototype.neighborVertexesArrays = J.util.Normix.neighborVertexesArrays, $t$);
}return J.util.Normix.neighborVertexesArrays;
}, $fz.isPrivate = true, $fz));
c$.getNormixV = $_M(c$, "getNormixV", 
function (v, bsTemp) {
return J.util.Normix.getNormix (v.x, v.y, v.z, 3, bsTemp);
}, "J.util.V3,J.util.BS");
c$.get2SidedNormix = $_M(c$, "get2SidedNormix", 
function (v, bsTemp) {
return ~J.util.Normix.getNormixV (v, bsTemp);
}, "J.util.V3,J.util.BS");
c$.getNormix = $_M(c$, "getNormix", 
($fz = function (x, y, z, geodesicLevel, bsConsidered) {
var champion;
var t;
if (z >= 0) {
champion = 0;
t = z - 1;
} else {
champion = 11;
t = z - (-1);
}bsConsidered.clearAll ();
bsConsidered.set (champion);
J.util.Normix.getVertexVectors ();
J.util.Normix.getNeighborVertexArrays ();
var championDist2 = x * x + y * y + t * t;
for (var lvl = 0; lvl <= geodesicLevel; ++lvl) {
var neighborVertexes = J.util.Normix.neighborVertexesArrays[lvl];
for (var offsetNeighbors = 6 * champion, i = offsetNeighbors + (champion < 12 ? 5 : 6); --i >= offsetNeighbors; ) {
var challenger = neighborVertexes[i];
if (bsConsidered.get (challenger)) continue;
bsConsidered.set (challenger);
var v = J.util.Normix.vertexVectors[challenger];
var d;
d = v.x - x;
var d2 = d * d;
if (d2 >= championDist2) continue;
d = v.y - y;
d2 += d * d;
if (d2 >= championDist2) continue;
d = v.z - z;
d2 += d * d;
if (d2 >= championDist2) continue;
champion = challenger;
championDist2 = d2;
}
}
return champion;
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,J.util.BS");
Clazz.defineStatics (c$,
"NORMIX_GEODESIC_LEVEL", 3,
"normixCount", 0,
"vertexVectors", null,
"inverseNormixes", null,
"neighborVertexesArrays", null,
"NORMIX_NULL", 9999);
});
