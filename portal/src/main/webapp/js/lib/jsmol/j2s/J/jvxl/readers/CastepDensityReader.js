Clazz.declarePackage ("J.jvxl.readers");
Clazz.load (["J.jvxl.readers.VolumeFileReader"], "J.jvxl.readers.CastepDensityReader", ["java.lang.Character", "J.util.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.nFilePoints = 0;
this.nSkip = 0;
Clazz.instantialize (this, arguments);
}, J.jvxl.readers, "CastepDensityReader", J.jvxl.readers.VolumeFileReader);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.jvxl.readers.CastepDensityReader, []);
});
Clazz.overrideMethod (c$, "init2", 
function (sg, br) {
this.init2VFR (sg, br);
this.canDownsample = this.isProgressive = false;
this.isAngstroms = true;
}, "J.jvxl.readers.SurfaceGenerator,java.io.BufferedReader");
Clazz.overrideMethod (c$, "readParameters", 
function () {
this.jvxlFileHeaderBuffer =  new J.util.SB ();
while (this.readLine () != null && this.line.indexOf (".") < 0) {
}
for (var i = 0; i < 3; ++i) {
var voxelVector = this.volumetricVectors[i];
voxelVector.set (this.parseFloatStr (this.line), this.parseFloat (), this.parseFloat ());
this.readLine ();
}
this.nSurfaces = this.parseIntStr (this.readLine ());
this.readLine ();
this.voxelCounts[0] = (this.nPointsX = this.parseIntStr (this.line)) + 1;
this.voxelCounts[1] = (this.nPointsY = this.parseInt ()) + 1;
this.voxelCounts[2] = (this.nPointsZ = this.parseInt ()) + 1;
this.nFilePoints = (this.nPointsX++) * (this.nPointsY++) * (this.nPointsZ++);
this.volumetricOrigin.set (0, 0, 0);
for (var i = 0; i < 3; i++) {
this.volumetricVectors[i].scale (1 / (this.voxelCounts[i] - 1));
if (this.isAnisotropic) this.setVectorAnisotropy (this.volumetricVectors[i]);
}
while (this.readLine ().trim ().length > 0) {
}
});
Clazz.overrideMethod (c$, "gotoData", 
function (n, nPoints) {
this.nSkip = n;
}, "~N,~N");
Clazz.overrideMethod (c$, "readSurfaceData", 
function (isMapData) {
this.initializeSurfaceData ();
this.voxelData =  Clazz.newFloatArray (this.nPointsX, this.nPointsY, this.nPointsZ, 0);
this.readLine ();
var tokens = this.getTokens ();
if (this.nSkip > 0 && tokens.length < 3 + this.nSurfaces) {
for (var j = 0; j < this.nSkip; j++) for (var i = 0; i < this.nFilePoints; i++) this.readLine ();


this.nSkip = 0;
}for (var i = 0; i < this.nFilePoints; i++) {
var x = this.parseIntStr (this.line) - 1;
var y = this.parseInt () - 1;
var z = this.parseInt () - 1;
if (this.nSkip > 0) this.skipPoints (this.nSkip);
this.voxelData[x][y][z] = this.recordData (this.parseFloat ());
this.readLine ();
}
var n;
n = this.nPointsX - 1;
for (var i = 0; i < this.nPointsY; ++i) for (var j = 0; j < this.nPointsZ; ++j) this.voxelData[n][i][j] = this.voxelData[0][i][j];


n = this.nPointsY - 1;
for (var i = 0; i < this.nPointsX; ++i) for (var j = 0; j < this.nPointsZ; ++j) this.voxelData[i][n][j] = this.voxelData[i][0][j];


n = this.nPointsZ - 1;
for (var i = 0; i < this.nPointsX; ++i) for (var j = 0; j < this.nPointsY; ++j) this.voxelData[i][j][n] = this.voxelData[i][j][0];


if (isMapData && this.volumeData.hasPlane ()) {
this.volumeData.setVoxelMap ();
for (var x = 0; x < this.nPointsX; ++x) {
for (var y = 0; y < this.nPointsY; ++y) {
for (var z = 0; z < this.nPointsZ; ++z) {
var f = this.volumeData.getToPlaneParameter ();
if (this.volumeData.isNearPlane (x, y, z, f)) this.volumeData.setVoxelMapValue (x, y, z, this.voxelData[x][y][z]);
}
}
}
this.voxelData = null;
}this.volumeData.setVoxelDataAsArray (this.voxelData);
if (this.dataMin > this.params.cutoff) this.params.cutoff = 2 * this.dataMin;
}, "~B");
$_M(c$, "skipPoints", 
($fz = function (n) {
var pt = this.next[0];
for (var i = 0; i < n; i++) {
while (pt < this.line.length && Character.isWhitespace (this.line.charAt (pt++))) {
}
while (pt < this.line.length && !Character.isWhitespace (this.line.charAt (pt++))) {
}
}
this.next[0] = pt;
}, $fz.isPrivate = true, $fz), "~N");
});
