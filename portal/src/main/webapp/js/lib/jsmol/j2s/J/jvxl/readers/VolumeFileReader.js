Clazz.declarePackage ("J.jvxl.readers");
Clazz.load (["J.jvxl.readers.SurfaceFileReader"], "J.jvxl.readers.VolumeFileReader", ["java.lang.Float", "J.api.Interface", "J.atomdata.AtomData", "J.util.ArrayUtil", "$.Logger", "$.Parser", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.endOfData = false;
this.negativeAtomCount = false;
this.atomCount = 0;
this.nSurfaces = 0;
this.isAngstroms = false;
this.canDownsample = false;
this.downsampleRemainders = null;
this.preProcessPlanes = false;
this.nData = 0;
this.readerClosed = false;
this.downsampleFactor = 0;
this.nSkipX = 0;
this.nSkipY = 0;
this.nSkipZ = 0;
this.yzPlanesRaw = null;
this.iPlaneRaw = 0;
this.boundingBox = null;
this.isScaledAlready = false;
Clazz.instantialize (this, arguments);
}, J.jvxl.readers, "VolumeFileReader", J.jvxl.readers.SurfaceFileReader);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.jvxl.readers.VolumeFileReader, []);
});
Clazz.overrideMethod (c$, "init2", 
function (sg, br) {
this.init2VFR (sg, br);
}, "J.jvxl.readers.SurfaceGenerator,java.io.BufferedReader");
$_M(c$, "init2VFR", 
function (sg, br) {
this.init2SFR (sg, br);
this.canDownsample = this.isProgressive = this.isXLowToHigh = true;
this.jvxlData.wasCubic = true;
this.boundingBox = this.params.boundingBox;
if (this.params.qmOrbitalType == 4) {
this.hasColorData = (this.params.parameters == null || this.params.parameters[1] >= 0);
this.preProcessPlanes = true;
this.params.insideOut = !this.params.insideOut;
}}, "J.jvxl.readers.SurfaceGenerator,java.io.BufferedReader");
$_M(c$, "recordData", 
function (value) {
if (Float.isNaN (value)) return value;
if (value < this.dataMin) this.dataMin = value;
if (value > this.dataMax) this.dataMax = value;
this.dataMean += value;
this.nData++;
return value;
}, "~N");
Clazz.overrideMethod (c$, "closeReader", 
function () {
if (this.readerClosed) return;
this.readerClosed = true;
this.closeReaderSFR ();
if (this.nData == 0 || this.dataMax == -3.4028235E38) return;
this.dataMean /= this.nData;
J.util.Logger.info ("VolumeFileReader closing file: " + this.nData + " points read \ndata min/max/mean = " + this.dataMin + "/" + this.dataMax + "/" + this.dataMean);
});
Clazz.overrideMethod (c$, "readVolumeParameters", 
function (isMapData) {
this.endOfData = false;
this.nSurfaces = this.readVolumetricHeader ();
if (this.nSurfaces == 0) return false;
if (this.nSurfaces < this.params.fileIndex) {
J.util.Logger.warn ("not enough surfaces in file -- resetting params.fileIndex to " + this.nSurfaces);
this.params.fileIndex = this.nSurfaces;
}return true;
}, "~B");
Clazz.overrideMethod (c$, "readVolumeData", 
function (isMapData) {
return this.readVolumeDataVFR (isMapData);
}, "~B");
$_M(c$, "readVolumeDataVFR", 
function (isMapData) {
if (!this.gotoAndReadVoxelData (isMapData)) return false;
if (!this.vertexDataOnly) J.util.Logger.info ("JVXL read: " + this.nPointsX + " x " + this.nPointsY + " x " + this.nPointsZ + " data points");
return true;
}, "~B");
$_M(c$, "readVolumetricHeader", 
($fz = function () {
try {
this.readParameters ();
if (this.atomCount == -2147483648) return 0;
if (!this.vertexDataOnly) J.util.Logger.info ("voxel grid origin:" + this.volumetricOrigin);
var downsampleFactor = this.params.downsampleFactor;
var downsampling = (this.canDownsample && downsampleFactor > 0);
if (downsampling) {
this.downsampleRemainders =  Clazz.newIntArray (3, 0);
J.util.Logger.info ("downsample factor = " + downsampleFactor);
for (var i = 0; i < 3; ++i) {
var n = this.voxelCounts[i];
this.downsampleRemainders[i] = n % downsampleFactor;
this.voxelCounts[i] /= downsampleFactor;
this.volumetricVectors[i].scale (downsampleFactor);
J.util.Logger.info ("downsampling axis " + (i + 1) + " from " + n + " to " + this.voxelCounts[i]);
}
}if (!this.vertexDataOnly) for (var i = 0; i < 3; ++i) {
if (!this.isAngstroms) this.volumetricVectors[i].scale (0.5291772);
this.line = this.voxelCounts[i] + " " + this.volumetricVectors[i].x + " " + this.volumetricVectors[i].y + " " + this.volumetricVectors[i].z;
this.jvxlFileHeaderBuffer.append (this.line).appendC ('\n');
J.util.Logger.info ("voxel grid count/vector:" + this.line);
}
this.scaleIsosurface (this.params.scale);
this.volumeData.setVolumetricXml ();
return this.nSurfaces;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error (e.toString ());
return 0;
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "skipComments", 
function (allowBlankLines) {
var sb =  new J.util.SB ();
while (this.readLine () != null && (allowBlankLines && this.line.length == 0 || this.line.indexOf ("#") == 0)) sb.append (this.line).appendC ('\n');

return sb.toString ();
}, "~B");
$_M(c$, "readVoxelVector", 
function (voxelVectorIndex) {
this.readLine ();
var voxelVector = this.volumetricVectors[voxelVectorIndex];
if ((this.voxelCounts[voxelVectorIndex] = this.parseIntStr (this.line)) == -2147483648) this.next[0] = this.line.indexOf (" ");
voxelVector.set (this.parseFloat (), this.parseFloat (), this.parseFloat ());
if (this.isAnisotropic) this.setVectorAnisotropy (voxelVector);
}, "~N");
$_M(c$, "initializeSurfaceData", 
function () {
this.downsampleFactor = this.params.downsampleFactor;
this.nSkipX = 0;
this.nSkipY = 0;
this.nSkipZ = 0;
if (this.canDownsample && this.downsampleFactor > 0) {
this.nSkipX = this.downsampleFactor - 1;
this.nSkipY = this.downsampleRemainders[2] + (this.downsampleFactor - 1) * (this.nSkipZ = (this.nPointsZ * this.downsampleFactor + this.downsampleRemainders[2]));
this.nSkipZ = this.downsampleRemainders[1] * this.nSkipZ + (this.downsampleFactor - 1) * this.nSkipZ * (this.nPointsY * this.downsampleFactor + this.downsampleRemainders[1]);
}if (this.params.thePlane != null) {
this.params.cutoff = 0;
} else if (this.isJvxl) {
this.params.cutoff = (this.params.isBicolorMap || this.params.colorBySign ? 0.01 : 0.5);
}this.nDataPoints = 0;
this.next[0] = 0;
this.line = "";
this.jvxlNSurfaceInts = 0;
});
Clazz.overrideMethod (c$, "readSurfaceData", 
function (isMapData) {
this.readSurfaceDataVFR (isMapData);
}, "~B");
$_M(c$, "readSurfaceDataVFR", 
function (isMapData) {
this.initializeSurfaceData ();
if (this.isProgressive && !isMapData || this.isJvxl) {
this.nDataPoints = this.volumeData.setVoxelCounts (this.nPointsX, this.nPointsY, this.nPointsZ);
this.voxelData = null;
if (this.isJvxl) this.jvxlVoxelBitSet = this.getVoxelBitSet (this.nDataPoints);
} else if (isMapData && this.volumeData.hasPlane ()) {
this.volumeData.setVoxelMap ();
var f = this.volumeData.getToPlaneParameter ();
for (var x = 0; x < this.nPointsX; ++x) {
for (var y = 0; y < this.nPointsY; ++y) {
for (var z = 0; z < this.nPointsZ; ++z) {
var v = this.recordData (this.getNextVoxelValue ());
if (this.volumeData.isNearPlane (x, y, z, f)) this.volumeData.setVoxelMapValue (x, y, z, v);
if (this.nSkipX != 0) this.skipVoxels (this.nSkipX);
}
if (this.nSkipY != 0) this.skipVoxels (this.nSkipY);
}
if (this.nSkipZ != 0) this.skipVoxels (this.nSkipZ);
}
} else {
this.voxelData = J.util.ArrayUtil.newFloat3 (this.nPointsX, -1);
for (var x = 0; x < this.nPointsX; ++x) {
var plane = J.util.ArrayUtil.newFloat2 (this.nPointsY);
this.voxelData[x] = plane;
for (var y = 0; y < this.nPointsY; ++y) {
var strip =  Clazz.newFloatArray (this.nPointsZ, 0);
plane[y] = strip;
for (var z = 0; z < this.nPointsZ; ++z) {
strip[z] = this.recordData (this.getNextVoxelValue ());
if (this.nSkipX != 0) this.skipVoxels (this.nSkipX);
}
if (this.nSkipY != 0) this.skipVoxels (this.nSkipY);
}
if (this.nSkipZ != 0) this.skipVoxels (this.nSkipZ);
}
}this.volumeData.setVoxelDataAsArray (this.voxelData);
}, "~B");
$_M(c$, "getPlane", 
function (x) {
if (x == 0) this.initPlanes ();
if (this.preProcessPlanes) return this.getPlaneProcessed (x);
var plane = this.getPlane2 (x);
if (this.qpc == null) this.getPlane (plane, true);
return plane;
}, "~N");
$_M(c$, "getPlaneProcessed", 
function (x) {
var plane;
if (this.iPlaneRaw == 0) {
this.qpc = J.api.Interface.getOptionInterface ("quantum.NciCalculation");
var atomData =  new J.atomdata.AtomData ();
atomData.modelIndex = -1;
atomData.bsSelected = this.params.bsSelected;
this.sg.fillAtomData (atomData, 1);
this.qpc.setupCalculation (this.volumeData, this.sg.getBsSelected (), null, null, null, atomData.atomXyz, -1, null, null, null, null, null, null, this.params.isSquaredLinear, null, this.params.theProperty, true, null, this.params.parameters, this.params.testFlags);
this.iPlaneRaw = 1;
this.qpc.setPlanes (this.yzPlanesRaw =  Clazz.newFloatArray (4, this.yzCount, 0));
if (this.hasColorData) {
this.getPlane (this.yzPlanesRaw[0], false);
this.getPlane (this.yzPlanesRaw[1], false);
plane = this.yzPlanes[0];
for (var i = 0; i < this.yzCount; i++) plane[i] = NaN;

return plane;
}this.iPlaneRaw = -1;
}var nan = this.qpc.getNoValue ();
var x1 = this.nPointsX - 1;
switch (this.iPlaneRaw) {
case -1:
plane = this.yzPlanes[x % 2];
x1++;
break;
case 3:
plane = this.yzPlanesRaw[0];
this.yzPlanesRaw[0] = this.yzPlanesRaw[1];
this.yzPlanesRaw[1] = this.yzPlanesRaw[2];
this.yzPlanesRaw[2] = this.yzPlanesRaw[3];
this.yzPlanesRaw[3] = plane;
plane = this.yzPlanesRaw[this.iPlaneRaw];
break;
default:
this.iPlaneRaw++;
plane = this.yzPlanesRaw[this.iPlaneRaw];
}
if (x < x1) {
this.getPlane (plane, false);
this.qpc.calcPlane (x, plane = this.yzPlanes[x % 2]);
for (var i = 0; i < this.yzCount; i++) if (plane[i] != nan) this.recordData (plane[i]);

} else {
for (var i = 0; i < this.yzCount; i++) plane[i] = NaN;

}return plane;
}, "~N");
$_M(c$, "getPlane", 
($fz = function (plane, doRecord) {
try {
for (var y = 0, ptyz = 0; y < this.nPointsY; ++y) {
for (var z = 0; z < this.nPointsZ; ++z) {
var v = this.getNextVoxelValue ();
if (doRecord) this.recordData (v);
plane[ptyz++] = v;
if (this.nSkipX != 0) this.skipVoxels (this.nSkipX);
}
if (this.nSkipY != 0) this.skipVoxels (this.nSkipY);
}
if (this.nSkipZ != 0) this.skipVoxels (this.nSkipZ);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz), "~A,~B");
Clazz.overrideMethod (c$, "getValue", 
function (x, y, z, ptyz) {
if (this.boundingBox != null) {
this.volumeData.voxelPtToXYZ (x, y, z, this.ptTemp);
if (this.ptTemp.x < this.boundingBox[0].x || this.ptTemp.x > this.boundingBox[1].x || this.ptTemp.y < this.boundingBox[0].y || this.ptTemp.y > this.boundingBox[1].y || this.ptTemp.z < this.boundingBox[0].z || this.ptTemp.z > this.boundingBox[1].z) return NaN;
}return this.getValue2 (x, y, z, ptyz);
}, "~N,~N,~N,~N");
$_M(c$, "skipVoxels", 
($fz = function (n) {
for (var i = n; --i >= 0; ) this.getNextVoxelValue ();

}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getVoxelBitSet", 
function (nPoints) {
return null;
}, "~N");
$_M(c$, "getNextVoxelValue", 
function () {
var voxelValue = 0;
if (this.nSurfaces > 1 && !this.params.blockCubeData) {
for (var i = 1; i < this.params.fileIndex; i++) this.nextVoxel ();

voxelValue = this.nextVoxel ();
for (var i = this.params.fileIndex; i < this.nSurfaces; i++) this.nextVoxel ();

} else {
voxelValue = this.nextVoxel ();
}return voxelValue;
});
$_M(c$, "nextVoxel", 
function () {
var voxelValue = this.parseFloat ();
if (Float.isNaN (voxelValue)) {
while (this.readLine () != null && Float.isNaN (voxelValue = this.parseFloatStr (this.line))) {
}
if (this.line == null) {
if (!this.endOfData) J.util.Logger.warn ("end of file reading cube voxel data? nBytes=" + this.nBytes + " nDataPoints=" + this.nDataPoints + " (line):" + this.line);
this.endOfData = true;
this.line = "0 0 0 0 0 0 0 0 0 0";
}}return voxelValue;
});
Clazz.overrideMethod (c$, "gotoData", 
function (n, nPoints) {
if (!this.params.blockCubeData) return;
if (n > 0) J.util.Logger.info ("skipping " + n + " data sets, " + nPoints + " points each");
for (var i = 0; i < n; i++) this.skipData (nPoints);

}, "~N,~N");
$_M(c$, "skipData", 
function (nPoints) {
this.skipDataVFR (nPoints);
}, "~N");
$_M(c$, "skipDataVFR", 
function (nPoints) {
var iV = 0;
while (iV < nPoints) iV += this.countData (this.readLine ());

}, "~N");
$_M(c$, "countData", 
($fz = function (str) {
var count = 0;
var ich = 0;
var ichMax = str.length;
var ch;
while (ich < ichMax) {
while (ich < ichMax && ((ch = str.charAt (ich)) == ' ' || ch == '\t')) ++ich;

if (ich < ichMax) ++count;
while (ich < ichMax && ((ch = str.charAt (ich)) != ' ' && ch != '\t')) ++ich;

}
return count;
}, $fz.isPrivate = true, $fz), "~S");
c$.checkAtomLine = $_M(c$, "checkAtomLine", 
function (isXLowToHigh, isAngstroms, strAtomCount, atomLine, bs) {
if (atomLine.indexOf ("ANGSTROMS") >= 0) isAngstroms = true;
var atomCount = (strAtomCount == null ? 2147483647 : J.util.Parser.parseInt (strAtomCount));
switch (atomCount) {
case -2147483648:
atomCount = 0;
atomLine = " " + atomLine.substring (atomLine.indexOf (" ") + 1);
break;
case 2147483647:
atomCount = -2147483648;
break;
default:
var s = "" + atomCount;
atomLine = atomLine.substring (atomLine.indexOf (s) + s.length);
}
if (isAngstroms) {
if (atomLine.indexOf ("ANGSTROM") < 0) atomLine += " ANGSTROMS";
} else {
if (atomLine.indexOf ("BOHR") < 0) atomLine += " BOHR";
}atomLine = (atomCount == -2147483648 ? "" : (isXLowToHigh ? "+" : "-") + Math.abs (atomCount)) + atomLine + "\n";
bs.append (atomLine);
return isAngstroms;
}, "~B,~B,~S,~S,J.util.SB");
Clazz.overrideMethod (c$, "getSurfacePointAndFraction", 
function (cutoff, isCutoffAbsolute, valueA, valueB, pointA, edgeVector, x, y, z, vA, vB, fReturn, ptReturn) {
return this.getSPFv (cutoff, isCutoffAbsolute, valueA, valueB, pointA, edgeVector, x, y, z, vA, vB, fReturn, ptReturn);
}, "~N,~B,~N,~N,J.util.P3,J.util.V3,~N,~N,~N,~N,~N,~A,J.util.P3");
$_M(c$, "getSPFv", 
function (cutoff, isCutoffAbsolute, valueA, valueB, pointA, edgeVector, x, y, z, vA, vB, fReturn, ptReturn) {
var zero = this.getSPF (cutoff, isCutoffAbsolute, valueA, valueB, pointA, edgeVector, x, y, z, vA, vB, fReturn, ptReturn);
if (this.qpc == null || Float.isNaN (zero) || !this.hasColorData) return zero;
vA = this.marchingCubes.getLinearOffset (x, y, z, vA);
vB = this.marchingCubes.getLinearOffset (x, y, z, vB);
return this.qpc.process (vA, vB, fReturn[0]);
}, "~N,~B,~N,~N,J.util.P3,J.util.V3,~N,~N,~N,~N,~N,~A,J.util.P3");
$_M(c$, "scaleIsosurface", 
($fz = function (scale) {
if (this.isScaledAlready) return;
this.isScaledAlready = true;
if (this.isAnisotropic) this.setVolumetricAnisotropy ();
if (Float.isNaN (scale)) return;
J.util.Logger.info ("applying scaling factor of " + scale);
this.volumetricOrigin.scaleAdd2 ((1 - scale) / 2, this.volumetricVectors[0], this.volumetricOrigin);
this.volumetricOrigin.scaleAdd2 ((1 - scale) / 2, this.volumetricVectors[1], this.volumetricOrigin);
this.volumetricOrigin.scaleAdd2 ((1 - scale) / 2, this.volumetricVectors[2], this.volumetricOrigin);
this.volumetricVectors[0].scale (scale);
this.volumetricVectors[1].scale (scale);
this.volumetricVectors[2].scale (scale);
}, $fz.isPrivate = true, $fz), "~N");
});
