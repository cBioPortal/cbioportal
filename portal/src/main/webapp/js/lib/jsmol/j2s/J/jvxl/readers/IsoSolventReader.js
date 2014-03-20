Clazz.declarePackage ("J.jvxl.readers");
Clazz.load (["J.jvxl.readers.AtomDataReader", "J.util.P3", "$.P4", "$.V3"], "J.jvxl.readers.IsoSolventReader", ["java.lang.Float", "java.util.Hashtable", "J.jvxl.data.MeshData", "J.util.BS", "$.BSUtil", "$.JmolList", "$.Logger", "$.Measure", "$.MeshSurface", "$.P3i"], function () {
c$ = Clazz.decorateAsClass (function () {
this.cavityRadius = 0;
this.envelopeRadius = 0;
this.dots = null;
this.doCalculateTroughs = false;
this.isCavity = false;
this.isPocket = false;
this.solventRadius = 0;
this.iter = null;
this.bsSurfacePoints = null;
this.bsSurfaceDone = null;
this.bsLocale = null;
this.htEdges = null;
this.vEdges = null;
this.vFaces = null;
this.vTemp = null;
this.plane = null;
this.ptTemp2 = null;
this.ptS1 = null;
this.ptS2 = null;
this.vTemp2 = null;
this.vTemp3 = null;
this.dPX = 0;
this.p = null;
this.maxRadius = 0;
this.bsAtomMinMax = null;
this.isSurfacePoint = false;
this.iAtomSurface = 0;
if (!Clazz.isClassDefined ("J.jvxl.readers.IsoSolventReader.Edge")) {
J.jvxl.readers.IsoSolventReader.$IsoSolventReader$Edge$ ();
}
if (!Clazz.isClassDefined ("J.jvxl.readers.IsoSolventReader.Face")) {
J.jvxl.readers.IsoSolventReader.$IsoSolventReader$Face$ ();
}
Clazz.instantialize (this, arguments);
}, J.jvxl.readers, "IsoSolventReader", J.jvxl.readers.AtomDataReader);
Clazz.prepareFields (c$, function () {
this.vTemp =  new J.util.V3 ();
this.plane =  new J.util.P4 ();
this.ptTemp2 =  new J.util.P3 ();
this.ptS1 =  new J.util.P3 ();
this.ptS2 =  new J.util.P3 ();
this.vTemp2 =  new J.util.V3 ();
this.vTemp3 =  new J.util.V3 ();
this.p =  new J.util.P3 ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.jvxl.readers.IsoSolventReader, []);
});
Clazz.overrideMethod (c$, "init", 
function (sg) {
this.initADR (sg);
}, "J.jvxl.readers.SurfaceGenerator");
Clazz.overrideMethod (c$, "readVolumeParameters", 
function (isMapData) {
this.setup (isMapData);
this.initializeVolumetricData ();
if (this.isProgressive) {
this.volumeData.setUnitVectors ();
this.volumeData.getYzCount ();
this.bsAtomMinMax =  new Array (this.nPointsX);
this.getAtomMinMax (null, this.bsAtomMinMax);
this.voxelSource =  Clazz.newIntArray (this.volumeData.nPoints, 0);
}return true;
}, "~B");
Clazz.overrideMethod (c$, "setup", 
function (isMapData) {
this.setup2 ();
if (this.contactPair == null) {
this.cavityRadius = this.params.cavityRadius;
this.envelopeRadius = this.params.envelopeRadius;
this.solventRadius = this.params.solventRadius;
this.point = this.params.point;
this.isCavity = (this.params.isCavity && this.meshDataServer != null);
this.isPocket = (this.params.pocket != null && this.meshDataServer != null);
this.doCalculateTroughs = (!isMapData && this.atomDataServer != null && !this.isCavity && this.solventRadius > 0 && (this.dataType == 1195 || this.dataType == 1203));
this.doUseIterator = this.doCalculateTroughs;
this.getAtoms (this.params.bsSelected, this.doAddHydrogens, true, false, false, true, false, NaN);
if (this.isCavity || this.isPocket) this.dots = this.meshDataServer.calculateGeodesicSurface (this.bsMySelected, this.envelopeRadius);
this.setHeader ("solvent/molecular surface", this.params.calculationType);
if (this.havePlane || !isMapData) {
var minPtsPerAng = 0;
this.setRanges (this.params.solvent_ptsPerAngstrom, this.params.solvent_gridMax, minPtsPerAng);
this.volumeData.getYzCount ();
this.margin = this.volumeData.maxGrid * 2.0;
}if (this.bsNearby != null) this.bsMySelected.or (this.bsNearby);
} else if (!isMapData) {
this.setVolumeData ();
}if (!this.doCalculateTroughs) {
if (isMapData) {
this.precalculateVoxelData = false;
this.volumeData.sr = this;
} else if (!this.isCavity) {
this.isProgressive = this.isXLowToHigh = true;
}}if (this.thisAtomSet == null) this.thisAtomSet = J.util.BSUtil.setAll (this.myAtomCount);
}, "~B");
Clazz.overrideMethod (c$, "generateCube", 
function () {
if (this.isCavity && this.params.theProperty != null) return;
this.getMaxRadius ();
if (this.isCavity && this.dataType != 1205 && this.dataType != 1206) {
this.params.vertexSource = null;
this.newVoxelDataCube ();
this.resetVoxelData (3.4028235E38);
this.markSphereVoxels (this.cavityRadius, this.params.distance);
this.generateSolventCavity ();
this.resetVoxelData (3.4028235E38);
this.markSphereVoxels (0, NaN);
} else {
this.voxelSource =  Clazz.newIntArray (this.volumeData.nPoints, 0);
this.generateSolventCube ();
}this.unsetVoxelData ();
var info = this.params.slabInfo;
if (info != null) for (var i = 0; i < info.size (); i++) if ((info.get (i)[2]).booleanValue () && Clazz.instanceOf (info.get (i)[0], J.util.P4)) {
this.volumeData.capData (info.get (i)[0], this.params.cutoff);
info.remove (i--);
}
});
Clazz.overrideMethod (c$, "getSurfacePointAndFraction", 
function (cutoff, isCutoffAbsolute, valueA, valueB, pointA, edgeVector, x, y, z, vA0, vB0, fReturn, ptReturn) {
var vA = this.marchingCubes.getLinearOffset (x, y, z, vA0);
var vB = this.marchingCubes.getLinearOffset (x, y, z, vB0);
this.isSurfacePoint = (this.bsSurfaceVoxels != null && (this.bsSurfaceVoxels.get (vA) || this.bsSurfaceVoxels.get (vB)));
if (J.jvxl.readers.IsoSolventReader.testLinear || this.voxelSource == null || this.voxelSource[vA] == 0 || this.voxelSource[vA] != this.voxelSource[vB]) return this.getSPF (cutoff, isCutoffAbsolute, valueA, valueB, pointA, edgeVector, x, y, z, vA, vB, fReturn, ptReturn);
var iAtom = Math.abs (valueA < valueB ? this.voxelSource[vA] : this.voxelSource[vB]);
this.iAtomSurface = this.atomIndex[iAtom - 1];
var fraction = fReturn[0] = J.util.MeshSurface.getSphericalInterpolationFraction ((this.voxelSource[vA] < 0 ? this.solventRadius : this.atomRadius[this.voxelSource[vA] - 1]), valueA, valueB, edgeVector.length ());
ptReturn.scaleAdd2 (fraction, edgeVector, pointA);
var diff = valueB - valueA;
return valueA + fraction * diff;
}, "~N,~B,~N,~N,J.util.P3,J.util.V3,~N,~N,~N,~N,~N,~A,J.util.P3");
Clazz.overrideMethod (c$, "addVertexCopy", 
function (vertexXYZ, value, assocVertex) {
var i = this.addVC (vertexXYZ, value, assocVertex);
if (i < 0) return i;
if (this.isSurfacePoint) this.bsSurfacePoints.set (i);
if (this.params.vertexSource != null) this.params.vertexSource[i] = this.iAtomSurface;
return i;
}, "J.util.P3,~N,~N");
Clazz.overrideMethod (c$, "selectPocket", 
function (doExclude) {
if (this.meshDataServer != null) this.meshDataServer.fillMeshData (this.meshData, 1, null);
var v = this.meshData.vertices;
var nVertices = this.meshData.vertexCount;
var vv = this.meshData.vertexValues;
var nDots = this.dots.length;
for (var i = 0; i < nVertices; i++) {
for (var j = 0; j < nDots; j++) {
if (this.dots[j].distance (v[i]) < this.envelopeRadius) {
vv[i] = NaN;
continue;
}}
}
this.meshData.getSurfaceSet ();
var nSets = this.meshData.nSets;
var pocketSet = J.util.BSUtil.newBitSet (nSets);
var ss;
for (var i = 0; i < nSets; i++) if ((ss = this.meshData.surfaceSet[i]) != null) for (var j = ss.nextSetBit (0); j >= 0; j = ss.nextSetBit (j + 1)) if (Float.isNaN (this.meshData.vertexValues[j])) {
pocketSet.set (i);
break;
}

for (var i = 0; i < nSets; i++) if (this.meshData.surfaceSet[i] != null && pocketSet.get (i) == doExclude) this.meshData.invalidateSurfaceSet (i);

this.updateSurfaceData ();
if (!doExclude) this.meshData.surfaceSet = null;
if (this.meshDataServer != null) {
this.meshDataServer.fillMeshData (this.meshData, 3, null);
this.meshData =  new J.jvxl.data.MeshData ();
}}, "~B");
Clazz.overrideMethod (c$, "postProcessVertices", 
function () {
this.setVertexSource ();
if (this.doCalculateTroughs && this.bsSurfacePoints != null) {
var bsAll =  new J.util.BS ();
var bsSurfaces = this.meshData.getSurfaceSet ();
var bsSources = null;
var volumes = (this.isPocket ? null : this.meshData.calculateVolumeOrArea (-2147483648, false, false));
var minVolume = (1.5 * 3.141592653589793 * Math.pow (this.solventRadius, 3));
var maxVolume = 0;
var maxIsNegative = false;
if (volumes != null && !this.isCavity) for (var i = 0; i < this.meshData.nSets; i++) {
var v = volumes[i];
if (Math.abs (v) > maxVolume) {
maxVolume = Math.abs (v);
maxIsNegative = (v < 0);
}}
var factor = (maxIsNegative ? -1 : 1);
for (var i = 0; i < this.meshData.nSets; i++) {
var bss = bsSurfaces[i];
if (bss.intersects (this.bsSurfacePoints)) {
if (volumes == null || volumes[i] * factor > minVolume) if (this.params.vertexSource != null) {
var bs =  new J.util.BS ();
if (bsSources == null) bsSources =  new Array (bsSurfaces.length);
for (var j = bss.nextSetBit (0); j >= 0; j = bss.nextSetBit (j + 1)) {
var iatom = this.params.vertexSource[j];
if (iatom < 0) continue;
if (bsAll.get (iatom)) {
this.meshData.invalidateSurfaceSet (i);
break;
}bs.set (iatom);
}
bsAll.or (bs);
continue;
}}this.meshData.invalidateSurfaceSet (i);
}
this.updateSurfaceData ();
if (this.meshDataServer != null) {
this.meshDataServer.fillMeshData (this.meshData, 3, null);
this.meshData =  new J.jvxl.data.MeshData ();
}}if (this.params.thePlane != null && this.params.slabInfo == null) this.params.addSlabInfo (J.util.MeshSurface.getSlabWithinRange (-100, 0));
});
$_M(c$, "generateSolventCavity", 
($fz = function () {
var bs = J.util.BSUtil.newBitSet (this.nPointsX * this.nPointsY * this.nPointsZ);
var i = 0;
var nDots = this.dots.length;
var n = 0;
var d;
var r2 = this.envelopeRadius;
for (var x = 0; x < this.nPointsX; ++x) for (var y = 0; y < this.nPointsY; ++y) {
out : for (var z = 0; z < this.nPointsZ; ++z, ++i) if ((d = this.voxelData[x][y][z]) < 3.4028235E38 && d >= this.cavityRadius) {
this.volumeData.voxelPtToXYZ (x, y, z, this.ptXyzTemp);
for (var j = 0; j < nDots; j++) {
if (this.dots[j].distance (this.ptXyzTemp) < r2) continue out;
}
bs.set (i);
n++;
}
}

J.util.Logger.info ("cavities include " + n + " voxel points");
this.atomRadius =  Clazz.newFloatArray (n, 0);
this.atomXyz =  new Array (n);
for (var x = 0, ipt = 0, apt = 0; x < this.nPointsX; ++x) for (var y = 0; y < this.nPointsY; ++y) for (var z = 0; z < this.nPointsZ; ++z) if (bs.get (ipt++)) {
this.volumeData.voxelPtToXYZ (x, y, z, (this.atomXyz[apt] =  new J.util.P3 ()));
this.atomRadius[apt++] = this.voxelData[x][y][z];
}


this.myAtomCount = this.firstNearbyAtom = n;
this.thisAtomSet = J.util.BSUtil.setAll (this.myAtomCount);
}, $fz.isPrivate = true, $fz));
$_M(c$, "generateSolventCube", 
($fz = function () {
if (this.dataType == 1205) return;
this.params.vertexSource =  Clazz.newIntArray (this.volumeData.nPoints, 0);
this.bsSurfaceDone =  new J.util.BS ();
this.bsSurfaceVoxels =  new J.util.BS ();
this.bsSurfacePoints =  new J.util.BS ();
if (this.doCalculateTroughs) {
this.iter = this.atomDataServer.getSelectedAtomIterator (this.bsMySelected, true, false, false);
this.vEdges =  new J.util.JmolList ();
this.bsLocale =  new Array (this.myAtomCount);
this.htEdges =  new java.util.Hashtable ();
this.getEdges ();
J.util.Logger.info (this.vEdges.size () + " edges");
this.vFaces =  new J.util.JmolList ();
this.getFaces ();
J.util.Logger.info (this.vFaces.size () + " faces");
this.bsLocale = null;
this.htEdges = null;
this.iter.release ();
this.iter = null;
this.newVoxelDataCube ();
this.resetVoxelData (3.4028235E38);
this.markFaceVoxels (true);
this.markToroidVoxels ();
this.vEdges = null;
this.markFaceVoxels (false);
this.vFaces = null;
} else {
this.newVoxelDataCube ();
this.resetVoxelData (3.4028235E38);
}this.markSphereVoxels (0, this.doCalculateTroughs ? 3.4028235E38 : this.params.distance);
this.noFaceSpheres = null;
this.validSpheres = null;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getEdges", 
($fz = function () {
for (var iatomA = 0; iatomA < this.myAtomCount; iatomA++) this.bsLocale[iatomA] =  new J.util.BS ();

var dist2 = this.solventRadius + this.maxRadius;
for (var iatomA = 0; iatomA < this.myAtomCount; iatomA++) {
var ptA = this.atomXyz[iatomA];
var rA = this.atomRadius[iatomA] + this.solventRadius;
this.atomDataServer.setIteratorForAtom (this.iter, this.atomIndex[iatomA], rA + dist2);
while (this.iter.hasNext ()) {
var iB = this.iter.next ();
var iatomB = this.myIndex[iB];
if (iatomA >= this.firstNearbyAtom && iatomB >= this.firstNearbyAtom) continue;
var ptB = this.atomXyz[iatomB];
var rB = this.atomRadius[iatomB] + this.solventRadius;
var dAB = ptA.distance (ptB);
if (dAB >= rA + rB) continue;
var edge = Clazz.innerTypeInstance (J.jvxl.readers.IsoSolventReader.Edge, this, null, iatomA, iatomB);
this.vEdges.addLast (edge);
this.bsLocale[iatomA].set (iatomB);
this.bsLocale[iatomB].set (iatomA);
this.htEdges.put (edge.toString (), edge);
}
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "findEdge", 
function (i, j) {
return this.htEdges.get (i < j ? i + "_" + j : j + "_" + i);
}, "~N,~N");
$_M(c$, "getFaces", 
($fz = function () {
var bs =  new J.util.BS ();
this.validSpheres =  new J.util.BS ();
this.noFaceSpheres = J.util.BSUtil.setAll (this.myAtomCount);
for (var i = this.vEdges.size (); --i >= 0; ) {
var edge = this.vEdges.get (i);
var ia = edge.ia;
var ib = edge.ib;
bs.clearAll ();
bs.or (this.bsLocale[ia]);
bs.and (this.bsLocale[ib]);
for (var ic = bs.nextSetBit (ib + 1); ic >= 0; ic = bs.nextSetBit (ic + 1)) {
if (this.getSolventPoints (ia, ib, ic)) {
var f;
var isOK = false;
if (this.validateFace (f = Clazz.innerTypeInstance (J.jvxl.readers.IsoSolventReader.Face, this, null, ia, ib, ic, edge, this.ptS1))) {
this.vFaces.addLast (f);
isOK = true;
}if (this.validateFace (f = Clazz.innerTypeInstance (J.jvxl.readers.IsoSolventReader.Face, this, null, ia, ib, ic, edge, this.ptS2))) {
this.vFaces.addLast (f);
isOK = true;
}if (isOK) {
this.noFaceSpheres.clear (ia);
this.noFaceSpheres.clear (ib);
this.noFaceSpheres.clear (ic);
}}}
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "getSolventPoints", 
($fz = function (ia, ib, ic) {
var dPS = this.getPointP (ia, ib);
var ptC = this.atomXyz[ic];
var rCS = this.atomRadius[ic] + this.solventRadius;
var dCT = J.util.Measure.distanceToPlane (this.plane, ptC);
if (Math.abs (dCT) >= rCS * 0.9) return false;
var dST = Math.sqrt (rCS * rCS - dCT * dCT);
this.ptTemp.scaleAdd2 (-dCT, this.vTemp, ptC);
var dpT = this.p.distance (this.ptTemp);
var dsp2 = (dPS * dPS);
var cosTheta = (dsp2 + dpT * dpT - dST * dST) / (2 * dPS * dpT);
if (Math.abs (cosTheta) >= 0.99) return false;
var vXS = this.vTemp2;
vXS.setT (this.ptTemp);
vXS.sub (this.p);
vXS.normalize ();
this.dPX = (dPS * cosTheta);
this.ptTemp.scaleAdd2 (this.dPX, vXS, this.p);
vXS.cross (this.vTemp, vXS);
vXS.normalize ();
vXS.scale ((Math.sqrt (1 - cosTheta * cosTheta) * dPS));
this.ptS1.setT (this.ptTemp);
this.ptS1.add (vXS);
this.ptS2.setT (this.ptTemp);
this.ptS2.sub (vXS);
return true;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "validateFace", 
($fz = function (f) {
var dist2 = this.solventRadius + this.maxRadius;
this.atomDataServer.setIteratorForPoint (this.iter, this.modelIndex, f.pS, dist2);
f.isValid = true;
while (this.iter.hasNext ()) {
var ia = this.iter.next ();
var iatom = this.myIndex[ia];
if (iatom == f.ia || iatom == f.ib || iatom == f.ic) continue;
var d = this.atomData.atomXyz[ia].distance (f.pS);
if (d < this.atomData.atomRadius[ia] + this.solventRadius) {
f.isValid = false;
break;
}}
f.setEdges ();
if (!f.isValid) return false;
for (var k = 0; k < 3; k++) {
this.validSpheres.set (f.edges[k].ia);
this.validSpheres.set (f.edges[k].ib);
}
f.edges = null;
return true;
}, $fz.isPrivate = true, $fz), "J.jvxl.readers.IsoSolventReader.Face");
$_M(c$, "markFaceVoxels", 
($fz = function (firstPass) {
var bsThisPass =  new J.util.BS ();
for (var fi = this.vFaces.size (); --fi >= 0; ) {
var f = this.vFaces.get (fi);
if (!f.isValid) continue;
this.setGridLimitsForAtom (f.pS, this.solventRadius, this.pt0, this.pt1);
this.volumeData.voxelPtToXYZ (this.pt0.x, this.pt0.y, this.pt0.z, this.ptXyzTemp);
var ptA = this.atomXyz[f.ia];
var ptB = this.atomXyz[f.ib];
var ptC = this.atomXyz[f.ic];
var ptS = f.pS;
for (var i = this.pt0.x; i < this.pt1.x; i++, this.ptXyzTemp.scaleAdd2 (1, this.volumetricVectors[0], this.ptY0)) {
this.ptY0.setT (this.ptXyzTemp);
for (var j = this.pt0.y; j < this.pt1.y; j++, this.ptXyzTemp.scaleAdd2 (1, this.volumetricVectors[1], this.ptZ0)) {
this.ptZ0.setT (this.ptXyzTemp);
for (var k = this.pt0.z; k < this.pt1.z; k++, this.ptXyzTemp.add (this.volumetricVectors[2])) {
var value = this.solventRadius - this.ptXyzTemp.distance (ptS);
var v = this.voxelData[i][j][k];
var ipt = this.volumeData.getPointIndex (i, j, k);
if (firstPass && value > 0) this.bsSurfaceDone.set (ipt);
if (J.util.Measure.isInTetrahedron (this.ptXyzTemp, ptA, ptB, ptC, ptS, this.plane, this.vTemp, this.vTemp2, this.vTemp3, false)) {
if (!firstPass ? !this.bsSurfaceDone.get (ipt) && value < 0 && value > -this.volumeData.maxGrid * 1.8 && (value > v) == bsThisPass.get (ipt) : (value > 0 && (v < 0 || v == 3.4028235E38 || (value > v) == bsThisPass.get (ipt)))) {
bsThisPass.set (ipt);
this.setVoxel (i, j, k, ipt, value);
if (this.voxelSource != null) this.voxelSource[ipt] = -1 - f.ia;
if (value > 0) {
this.bsSurfaceVoxels.set (ipt);
}}}}
}
}
}
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "markToroidVoxels", 
($fz = function () {
var ptA0 =  new J.util.P3i ();
var ptB0 =  new J.util.P3i ();
var ptA1 =  new J.util.P3i ();
var ptB1 =  new J.util.P3i ();
for (var ei = this.vEdges.size (); --ei >= 0; ) {
var edge = this.vEdges.get (ei);
var ia = edge.ia;
var ib = edge.ib;
var ptA = this.atomXyz[ia];
var ptB = this.atomXyz[ib];
var rAS = this.atomRadius[ia] + this.solventRadius;
var rBS = this.atomRadius[ib] + this.solventRadius;
var dAB = ptB.distance (ptA);
this.setGridLimitsForAtom (ptA, this.atomRadius[ia] + this.solventRadius, ptA0, ptA1);
this.setGridLimitsForAtom (ptB, this.atomRadius[ib] + this.solventRadius, ptB0, ptB1);
J.jvxl.readers.IsoSolventReader.mergeLimits (ptA0, ptB0, this.pt0, null);
J.jvxl.readers.IsoSolventReader.mergeLimits (ptA1, ptB1, null, this.pt1);
this.volumeData.voxelPtToXYZ (this.pt0.x, this.pt0.y, this.pt0.z, this.ptXyzTemp);
for (var i = this.pt0.x; i < this.pt1.x; i++, this.ptXyzTemp.scaleAdd2 (1, this.volumetricVectors[0], this.ptY0)) {
this.ptY0.setT (this.ptXyzTemp);
for (var j = this.pt0.y; j < this.pt1.y; j++, this.ptXyzTemp.scaleAdd2 (1, this.volumetricVectors[1], this.ptZ0)) {
this.ptZ0.setT (this.ptXyzTemp);
for (var k = this.pt0.z; k < this.pt1.z; k++, this.ptXyzTemp.add (this.volumetricVectors[2])) {
var dVS = this.checkSpecialVoxel (ptA, rAS, ptB, rBS, dAB, this.ptXyzTemp);
if (Float.isNaN (dVS)) continue;
var value = this.solventRadius - dVS;
if (value < this.voxelData[i][j][k]) {
var ipt = this.volumeData.getPointIndex (i, j, k);
this.setVoxel (i, j, k, ipt, value);
if (this.voxelSource != null) this.voxelSource[ipt] = -1 - ia;
}}
}
}
}
this.validSpheres.or (this.noFaceSpheres);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "unsetVoxelData", 
function () {
if (!this.havePlane) {
this.unsetVoxelData2 ();
return;
}if (this.isProgressive) for (var i = 0; i < this.yzCount; i++) {
if (this.thisPlane[i] < 0.001) {
} else {
this.thisPlane[i] = 0.001;
}}
 else for (var x = 0; x < this.nPointsX; ++x) for (var y = 0; y < this.nPointsY; ++y) for (var z = 0; z < this.nPointsZ; ++z) if (this.voxelData[x][y][z] < 0.001) {
} else {
this.voxelData[x][y][z] = 0.001;
}


});
$_M(c$, "getMaxRadius", 
function () {
this.maxRadius = 0;
for (var iAtom = 0; iAtom < this.myAtomCount; iAtom++) {
var rA = this.atomRadius[iAtom];
if (rA > this.maxRadius) this.maxRadius = rA;
}
});
c$.mergeLimits = $_M(c$, "mergeLimits", 
($fz = function (ptA, ptB, pt0, pt1) {
if (pt0 != null) {
pt0.x = Math.min (ptA.x, ptB.x);
pt0.y = Math.min (ptA.y, ptB.y);
pt0.z = Math.min (ptA.z, ptB.z);
}if (pt1 != null) {
pt1.x = Math.max (ptA.x, ptB.x);
pt1.y = Math.max (ptA.y, ptB.y);
pt1.z = Math.max (ptA.z, ptB.z);
}}, $fz.isPrivate = true, $fz), "J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i");
$_M(c$, "checkSpecialVoxel", 
($fz = function (ptA, rAS, ptB, rBS, dAB, ptV) {
var dAV = ptA.distance (ptV);
var dBV = ptB.distance (ptV);
var dVS;
var f = rAS / dAV;
if (f > 1) {
this.p.set (ptA.x + (ptV.x - ptA.x) * f, ptA.y + (ptV.y - ptA.y) * f, ptA.z + (ptV.z - ptA.z) * f);
if (ptB.distance (this.p) >= rBS) return NaN;
dVS = this.solventDistance (rAS, rBS, dAB, dAV, dBV);
return (J.jvxl.readers.IsoSolventReader.voxelIsInTrough (dVS, rAS * rAS, rBS, dAB, dAV) ? dVS : NaN);
}if ((f = rBS / dBV) > 1) {
this.p.set (ptB.x + (ptV.x - ptB.x) * f, ptB.y + (ptV.y - ptB.y) * f, ptB.z + (ptV.z - ptB.z) * f);
if (ptA.distance (this.p) >= rAS) return NaN;
dVS = this.solventDistance (rBS, rAS, dAB, dBV, dAV);
return (J.jvxl.readers.IsoSolventReader.voxelIsInTrough (dVS, rBS * rBS, rAS, dAB, dBV) ? dVS : NaN);
}return NaN;
}, $fz.isPrivate = true, $fz), "J.util.P3,~N,J.util.P3,~N,~N,J.util.P3");
c$.voxelIsInTrough = $_M(c$, "voxelIsInTrough", 
($fz = function (dXC, rAC2, rBC, dAB, dAX) {
var cosACBf = (rAC2 + rBC * rBC - dAB * dAB) / rBC;
var cosACXf = (rAC2 + dXC * dXC - dAX * dAX) / dXC;
return (cosACBf < cosACXf);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N");
$_M(c$, "solventDistance", 
($fz = function (rAS, rBS, dAB, dAV, dBV) {
var dAV2 = dAV * dAV;
var rAS2 = rAS * rAS;
var dAB2 = dAB * dAB;
var angleVAB = Math.acos ((dAV2 + dAB2 - dBV * dBV) / (2 * dAV * dAB));
var angleBAS = Math.acos ((dAB2 + rAS2 - rBS * rBS) / (2 * dAB * rAS));
var dVS = Math.sqrt (rAS2 + dAV2 - 2 * rAS * dAV * Math.cos (angleBAS - angleVAB));
return dVS;
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N");
$_M(c$, "getPointP", 
function (ia, ib) {
var ptA = this.atomXyz[ia];
var ptB = this.atomXyz[ib];
var rAS = this.atomRadius[ia] + this.solventRadius;
var rBS = this.atomRadius[ib] + this.solventRadius;
this.vTemp.setT (ptB);
this.vTemp.sub (ptA);
var dAB = this.vTemp.length ();
this.vTemp.normalize ();
var rAS2 = rAS * rAS;
var dAB2 = dAB * dAB;
var cosAngleBAS = (dAB2 + rAS2 - rBS * rBS) / (2 * dAB * rAS);
var angleBAS = Math.acos (cosAngleBAS);
this.p.scaleAdd2 ((cosAngleBAS * rAS), this.vTemp, ptA);
J.util.Measure.getPlaneThroughPoint (this.p, this.vTemp, this.plane);
return Math.sin (angleBAS) * rAS;
}, "~N,~N");
Clazz.overrideMethod (c$, "getValueAtPoint", 
function (pt, getSource) {
if (this.contactPair != null) return pt.distance (this.contactPair.myAtoms[1]) - this.contactPair.radii[1];
var value = 3.4028235E38;
for (var iAtom = 0; iAtom < this.firstNearbyAtom; iAtom++) {
var r = pt.distance (this.atomXyz[iAtom]) - this.atomRadius[iAtom] - this.solventRadius;
if (r < value) value = r;
}
return (value == 3.4028235E38 ? NaN : value);
}, "J.util.P3,~B");
Clazz.overrideMethod (c$, "getPlane", 
function (x) {
if (this.yzCount == 0) {
this.initPlanes ();
}this.thisX = x;
this.thisPlane = this.yzPlanes[x % 2];
if (this.contactPair == null) {
this.resetPlane (3.4028235E38);
this.thisAtomSet = this.bsAtomMinMax[x];
this.markSphereVoxels (0, this.params.distance);
this.unsetVoxelData ();
} else {
this.markPlaneVoxels (this.contactPair.myAtoms[0], this.contactPair.radii[0]);
}return this.thisPlane;
}, "~N");
c$.$IsoSolventReader$Edge$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.ia = 0;
this.ib = 0;
this.nFaces = 0;
this.nInvalid = 0;
this.aFaces = null;
Clazz.instantialize (this, arguments);
}, J.jvxl.readers.IsoSolventReader, "Edge");
Clazz.makeConstructor (c$, 
function (a, b) {
this.ia = Math.min (a, b);
this.ib = Math.max (a, b);
}, "~N,~N");
$_M(c$, "addFace", 
function (a) {
if (a == null) {
this.nInvalid++;
return;
}if (this.aFaces == null) this.aFaces =  new J.util.JmolList ();
this.aFaces.addLast (a);
this.nFaces++;
}, "J.jvxl.readers.IsoSolventReader.Face");
Clazz.overrideMethod (c$, "toString", 
function () {
return this.ia + "_" + this.ib;
});
c$ = Clazz.p0p ();
};
c$.$IsoSolventReader$Face$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.ia = 0;
this.ib = 0;
this.ic = 0;
this.isValid = false;
this.pS = null;
this.edges = null;
Clazz.instantialize (this, arguments);
}, J.jvxl.readers.IsoSolventReader, "Face");
Clazz.prepareFields (c$, function () {
this.edges =  new Array (3);
});
Clazz.makeConstructor (c$, 
function (a, b, c, d, e) {
this.ia = a;
this.ib = b;
this.ic = c;
this.pS = J.util.P3.newP (e);
this.edges[0] = d;
}, "~N,~N,~N,J.jvxl.readers.IsoSolventReader.Edge,J.util.P3");
$_M(c$, "setEdges", 
function () {
if (this.edges[1] == null) {
this.edges[1] = this.b$["J.jvxl.readers.IsoSolventReader"].findEdge (this.ib, this.ic);
this.edges[2] = this.b$["J.jvxl.readers.IsoSolventReader"].findEdge (this.ic, this.ia);
}var a = (this.isValid ? this : null);
for (var b = 0; b < 3; b++) this.edges[b].addFace (a);

});
c$ = Clazz.p0p ();
};
Clazz.defineStatics (c$,
"testLinear", false);
});
