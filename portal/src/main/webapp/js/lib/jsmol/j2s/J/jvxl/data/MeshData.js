Clazz.declarePackage ("J.jvxl.data");
Clazz.load (["J.util.MeshSurface"], "J.jvxl.data.MeshData", ["java.lang.Float", "java.util.Arrays", "J.util.ArrayUtil", "$.BS", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.setsSuccessful = false;
this.vertexIncrement = 1;
this.polygonColorData = null;
if (!Clazz.isClassDefined ("J.jvxl.data.MeshData.SSet")) {
J.jvxl.data.MeshData.$MeshData$SSet$ ();
}
if (!Clazz.isClassDefined ("J.jvxl.data.MeshData.SortSet")) {
J.jvxl.data.MeshData.$MeshData$SortSet$ ();
}
Clazz.instantialize (this, arguments);
}, J.jvxl.data, "MeshData", J.util.MeshSurface);
$_M(c$, "addVertexCopy", 
function (vertex, value, assocVertex) {
if (assocVertex < 0) this.vertexIncrement = -assocVertex;
return this.addVCVal (vertex, value);
}, "J.util.P3,~N,~N");
$_M(c$, "getSurfaceSet", 
function () {
return (this.surfaceSet == null ? this.getSurfaceSetForLevel (0) : this.surfaceSet);
});
$_M(c$, "getSurfaceSetForLevel", 
($fz = function (level) {
if (level == 0) {
this.surfaceSet =  new Array (100);
this.nSets = 0;
}this.setsSuccessful = true;
for (var i = 0; i < this.polygonCount; i++) if (this.polygonIndexes[i] != null) {
if (this.bsSlabDisplay != null && !this.bsSlabDisplay.get (i)) continue;
var p = this.polygonIndexes[i];
var pt0 = this.findSet (p[0]);
var pt1 = this.findSet (p[1]);
var pt2 = this.findSet (p[2]);
if (pt0 < 0 && pt1 < 0 && pt2 < 0) {
this.createSet (p[0], p[1], p[2]);
continue;
}if (pt0 == pt1 && pt1 == pt2) continue;
if (pt0 >= 0) {
this.surfaceSet[pt0].set (p[1]);
this.surfaceSet[pt0].set (p[2]);
if (pt1 >= 0 && pt1 != pt0) this.mergeSets (pt0, pt1);
if (pt2 >= 0 && pt2 != pt0 && pt2 != pt1) this.mergeSets (pt0, pt2);
continue;
}if (pt1 >= 0) {
this.surfaceSet[pt1].set (p[0]);
this.surfaceSet[pt1].set (p[2]);
if (pt2 >= 0 && pt2 != pt1) this.mergeSets (pt1, pt2);
continue;
}this.surfaceSet[pt2].set (p[0]);
this.surfaceSet[pt2].set (p[1]);
}
var n = 0;
for (var i = 0; i < this.nSets; i++) if (this.surfaceSet[i] != null) n++;

var temp =  new Array (this.surfaceSet.length);
n = 0;
for (var i = 0; i < this.nSets; i++) if (this.surfaceSet[i] != null) temp[n++] = this.surfaceSet[i];

this.nSets = n;
this.surfaceSet = temp;
if (!this.setsSuccessful && level < 2) this.getSurfaceSetForLevel (level + 1);
if (level == 0) {
this.sortSurfaceSets ();
this.setVertexSets (false);
}return this.surfaceSet;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "sortSurfaceSets", 
($fz = function () {
var sets =  new Array (this.nSets);
for (var i = 0; i < this.nSets; i++) sets[i] = Clazz.innerTypeInstance (J.jvxl.data.MeshData.SSet, this, null, this.surfaceSet[i]);

java.util.Arrays.sort (sets, Clazz.innerTypeInstance (J.jvxl.data.MeshData.SortSet, this, null));
for (var i = 0; i < this.nSets; i++) this.surfaceSet[i] = sets[i].bs;

}, $fz.isPrivate = true, $fz));
$_M(c$, "setVertexSets", 
function (onlyIfNull) {
if (this.surfaceSet == null) return;
var nNull = 0;
for (var i = 0; i < this.nSets; i++) {
if (this.surfaceSet[i] != null && this.surfaceSet[i].cardinality () == 0) this.surfaceSet[i] = null;
if (this.surfaceSet[i] == null) nNull++;
}
if (nNull > 0) {
var bsNew =  new Array (this.nSets - nNull);
for (var i = 0, n = 0; i < this.nSets; i++) if (this.surfaceSet[i] != null) bsNew[n++] = this.surfaceSet[i];

this.surfaceSet = bsNew;
this.nSets -= nNull;
} else if (onlyIfNull) {
return;
}this.vertexSets =  Clazz.newIntArray (this.vertexCount, 0);
for (var i = 0; i < this.nSets; i++) for (var j = this.surfaceSet[i].nextSetBit (0); j >= 0; j = this.surfaceSet[i].nextSetBit (j + 1)) this.vertexSets[j] = i;


}, "~B");
$_M(c$, "findSet", 
($fz = function (vertex) {
for (var i = 0; i < this.nSets; i++) if (this.surfaceSet[i] != null && this.surfaceSet[i].get (vertex)) return i;

return -1;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "createSet", 
($fz = function (v1, v2, v3) {
var i;
for (i = 0; i < this.nSets; i++) if (this.surfaceSet[i] == null) break;

if (i == this.surfaceSet.length) this.surfaceSet = J.util.ArrayUtil.ensureLength (this.surfaceSet, this.surfaceSet.length + 100);
this.surfaceSet[i] =  new J.util.BS ();
this.surfaceSet[i].set (v1);
this.surfaceSet[i].set (v2);
this.surfaceSet[i].set (v3);
if (i == this.nSets) this.nSets++;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "mergeSets", 
($fz = function (a, b) {
this.surfaceSet[a].or (this.surfaceSet[b]);
this.surfaceSet[b] = null;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "invalidateSurfaceSet", 
function (i) {
for (var j = this.surfaceSet[i].nextSetBit (0); j >= 0; j = this.surfaceSet[i].nextSetBit (j + 1)) this.vertexValues[j] = NaN;

this.surfaceSet[i] = null;
}, "~N");
c$.checkCutoff = $_M(c$, "checkCutoff", 
function (iA, iB, iC, vertexValues) {
if (iA < 0 || iB < 0 || iC < 0) return false;
var val1 = vertexValues[iA];
var val2 = vertexValues[iB];
var val3 = vertexValues[iC];
return (val1 >= 0 && val2 >= 0 && val3 >= 0 || val1 <= 0 && val2 <= 0 && val3 <= 0);
}, "~N,~N,~N,~A");
$_M(c$, "calculateVolumeOrArea", 
function (thisSet, isArea, getSets) {
if (getSets || this.nSets == 0) this.getSurfaceSet ();
var justOne = (thisSet >= -1);
var n = (justOne || this.nSets == 0 ? 1 : this.nSets);
var v =  Clazz.newDoubleArray (n, 0);
var vAB =  new J.util.V3 ();
var vAC =  new J.util.V3 ();
var vTemp =  new J.util.V3 ();
for (var i = this.polygonCount; --i >= 0; ) {
if (!this.setABC (i)) continue;
var iSet = (this.nSets == 0 ? 0 : this.vertexSets[this.iA]);
if (thisSet >= 0 && iSet != thisSet) continue;
if (isArea) {
vAB.sub2 (this.vertices[this.iB], this.vertices[this.iA]);
vAC.sub2 (this.vertices[this.iC], this.vertices[this.iA]);
vTemp.cross (vAB, vAC);
v[justOne ? 0 : iSet] += vTemp.length ();
} else {
vAB.setT (this.vertices[this.iB]);
vAC.setT (this.vertices[this.iC]);
vTemp.cross (vAB, vAC);
vAC.setT (this.vertices[this.iA]);
v[justOne ? 0 : iSet] += vAC.dot (vTemp);
}}
var factor = (isArea ? 2 : 6);
for (var i = 0; i < n; i++) v[i] /= factor;

if (justOne) return Float.$valueOf (v[0]);
return v;
}, "~N,~B,~B");
$_M(c$, "updateInvalidatedVertices", 
function (bs) {
bs.clearAll ();
for (var i = 0, ipt = 0; i < this.vertexCount; i += this.vertexIncrement, ipt++) if (Float.isNaN (this.vertexValues[i])) bs.set (i);

}, "J.util.BS");
$_M(c$, "invalidateVertices", 
function (bsInvalid) {
for (var i = bsInvalid.nextSetBit (0); i >= 0; i = bsInvalid.nextSetBit (i + 1)) this.vertexValues[i] = NaN;

}, "J.util.BS");
c$.$MeshData$SSet$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.bs = null;
this.n = 0;
Clazz.instantialize (this, arguments);
}, J.jvxl.data.MeshData, "SSet");
Clazz.makeConstructor (c$, 
function (a) {
this.bs = a;
this.n = a.cardinality ();
}, "J.util.BS");
c$ = Clazz.p0p ();
};
c$.$MeshData$SortSet$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.jvxl.data.MeshData, "SortSet", null, java.util.Comparator);
Clazz.overrideMethod (c$, "compare", 
function (a, b) {
return (a.n > b.n ? -1 : a.n < b.n ? 1 : 0);
}, "J.jvxl.data.MeshData.SSet,J.jvxl.data.MeshData.SSet");
c$ = Clazz.p0p ();
};
Clazz.defineStatics (c$,
"MODE_GET_VERTICES", 1,
"MODE_GET_COLOR_INDEXES", 2,
"MODE_PUT_SETS", 3,
"MODE_PUT_VERTICES", 4);
});
