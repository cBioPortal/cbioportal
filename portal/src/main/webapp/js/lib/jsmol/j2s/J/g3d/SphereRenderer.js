Clazz.declarePackage ("J.g3d");
Clazz.load (["J.util.P3", "$.Shader"], "J.g3d.SphereRenderer", ["J.util.GData"], function () {
c$ = Clazz.decorateAsClass (function () {
this.g3d = null;
this.shader = null;
this.minX = 0;
this.maxX = 0;
this.minY = 0;
this.maxY = 0;
this.minZ = 0;
this.maxZ = 0;
this.x = 0;
this.y = 0;
this.z = 0;
this.diameter = 0;
this.tScreened = false;
this.shades = null;
this.zroot = null;
this.mat = null;
this.coef = null;
this.mDeriv = null;
this.selectedOctant = 0;
this.octantPoints = null;
this.planeShade = 0;
this.zbuf = null;
this.width = 0;
this.height = 0;
this.depth = 0;
this.slab = 0;
this.offsetPbufBeginLine = 0;
this.addAllPixels = false;
this.ptTemp = null;
this.planeShades = null;
this.dxyz = null;
this.z0 = 0;
Clazz.instantialize (this, arguments);
}, J.g3d, "SphereRenderer");
Clazz.prepareFields (c$, function () {
this.zroot =  Clazz.newDoubleArray (2, 0);
this.ptTemp =  new J.util.P3 ();
this.planeShades =  Clazz.newIntArray (3, 0);
this.dxyz =  Clazz.newFloatArray (3, 3, 0);
});
Clazz.makeConstructor (c$, 
function (g3d) {
this.g3d = g3d;
this.shader = g3d.shader;
}, "J.g3d.Graphics3D");
$_M(c$, "render", 
function (shades, tScreened, diameter, x, y, z, mat, coef, mDeriv, selectedOctant, octantPoints, addAllPixels) {
if (z == 1) return;
this.width = this.g3d.width;
this.height = this.g3d.height;
if (diameter > 49) diameter &= -2;
if (this.g3d.isClippedXY (diameter, x, y)) return;
var radius = (diameter + 1) >> 1;
this.minX = x - radius;
this.maxX = x + radius;
this.minY = y - radius;
this.maxY = y + radius;
this.slab = this.g3d.slab;
this.depth = this.g3d.depth;
this.minZ = z - radius;
this.maxZ = z + radius;
if (this.maxZ < this.slab || this.minZ > this.depth) return;
this.shader.nOut = this.shader.nIn = 0;
this.zbuf = this.g3d.zbuf;
this.addAllPixels = addAllPixels;
this.offsetPbufBeginLine = this.width * y + x;
this.x = x;
this.y = y;
this.z = z;
this.diameter = diameter;
this.tScreened = tScreened;
this.shades = shades;
this.mat = mat;
if (mat != null) {
this.coef = coef;
this.mDeriv = mDeriv;
this.selectedOctant = selectedOctant;
this.octantPoints = octantPoints;
}if (mat != null || diameter > 128) {
this.renderLarge ();
if (mat != null) {
this.mat = null;
this.coef = null;
this.mDeriv = null;
this.octantPoints = null;
}} else {
var ss = this.getSphereShape (diameter);
if (this.minX < 0 || this.maxX >= this.width || this.minY < 0 || this.maxY >= this.height || this.minZ < this.slab || z > this.depth) this.renderShapeClipped (ss);
 else this.renderShapeUnclipped (ss);
}this.shades = null;
this.zbuf = null;
}, "~A,~B,~N,~N,~N,~N,J.util.Matrix3f,~A,J.util.Matrix4f,~N,~A,~B");
$_M(c$, "getSphereShape", 
($fz = function (diameter) {
var ss;
return ((ss = this.shader.sphereShapeCache[diameter - 1]) == null ? this.createSphereShape (diameter) : ss);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "createSphereShape", 
($fz = function (diameter) {
var countSE = 0;
var oddDiameter = (diameter & 1) != 0;
var radiusF = diameter / 2.0;
var radiusF2 = radiusF * radiusF;
var radius = Clazz.doubleToInt ((diameter + 1) / 2);
var y = oddDiameter ? 0 : 0.5;
for (var i = 0; i < radius; ++i, ++y) {
var y2 = y * y;
var x = oddDiameter ? 0 : 0.5;
for (var j = 0; j < radius; ++j, ++x) {
var x2 = x * x;
var z2 = radiusF2 - y2 - x2;
if (z2 >= 0) ++countSE;
}
}
var sphereShape =  Clazz.newIntArray (countSE, 0);
var offset = 0;
y = oddDiameter ? 0 : 0.5;
for (var i = 0; i < radius; ++i, ++y) {
var y2 = y * y;
var x = oddDiameter ? 0 : 0.5;
for (var j = 0; j < radius; ++j, ++x) {
var x2 = x * x;
var z2 = radiusF2 - y2 - x2;
if (z2 >= 0) {
var z = Math.sqrt (z2);
var height = Clazz.floatToInt (z);
var shadeIndexSE = this.shader.getShadeN (x, y, z, radiusF);
var shadeIndexSW = this.shader.getShadeN (-x, y, z, radiusF);
var shadeIndexNE = this.shader.getShadeN (x, -y, z, radiusF);
var shadeIndexNW = this.shader.getShadeN (-x, -y, z, radiusF);
var packed = (height | (shadeIndexSE << 7) | (shadeIndexSW << 13) | (shadeIndexNE << 19) | (shadeIndexNW << 25));
sphereShape[offset++] = packed;
}}
sphereShape[offset - 1] |= 0x80000000;
}
return this.shader.sphereShapeCache[diameter - 1] = sphereShape;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "renderShapeUnclipped", 
($fz = function (sphereShape) {
var offsetSphere = 0;
var evenSizeCorrection = 1 - (this.diameter & 1);
var offsetSouthCenter = this.offsetPbufBeginLine;
var offsetNorthCenter = offsetSouthCenter - evenSizeCorrection * this.width;
var nLines = Clazz.doubleToInt ((this.diameter + 1) / 2);
if (!this.tScreened) {
do {
var offsetSE = offsetSouthCenter;
var offsetSW = offsetSouthCenter - evenSizeCorrection;
var offsetNE = offsetNorthCenter;
var offsetNW = offsetNorthCenter - evenSizeCorrection;
var packed;
do {
packed = sphereShape[offsetSphere++];
var zPixel = this.z - (packed & 0x7F);
if (zPixel < this.zbuf[offsetSE]) this.g3d.addPixel (offsetSE, zPixel, this.shades[((packed >> 7) & 0x3F)]);
if (zPixel < this.zbuf[offsetSW]) this.g3d.addPixel (offsetSW, zPixel, this.shades[((packed >> 13) & 0x3F)]);
if (zPixel < this.zbuf[offsetNE]) this.g3d.addPixel (offsetNE, zPixel, this.shades[((packed >> 19) & 0x3F)]);
if (zPixel < this.zbuf[offsetNW]) this.g3d.addPixel (offsetNW, zPixel, this.shades[((packed >> 25) & 0x3F)]);
++offsetSE;
--offsetSW;
++offsetNE;
--offsetNW;
} while (packed >= 0);
offsetSouthCenter += this.width;
offsetNorthCenter -= this.width;
} while (--nLines > 0);
return;
}var flipflopSouthCenter = (this.x ^ this.y) & 1;
var flipflopNorthCenter = flipflopSouthCenter ^ evenSizeCorrection;
var flipflopSE = flipflopSouthCenter;
var flipflopSW = flipflopSouthCenter ^ evenSizeCorrection;
var flipflopNE = flipflopNorthCenter;
var flipflopNW = flipflopNorthCenter ^ evenSizeCorrection;
var flipflopsCenter = flipflopSE | (flipflopSW << 1) | (flipflopNE << 2) | (flipflopNW << 3);
do {
var offsetSE = offsetSouthCenter;
var offsetSW = offsetSouthCenter - evenSizeCorrection;
var offsetNE = offsetNorthCenter;
var offsetNW = offsetNorthCenter - evenSizeCorrection;
var packed;
var flipflops = (flipflopsCenter = ~flipflopsCenter);
do {
packed = sphereShape[offsetSphere++];
var zPixel = this.z - (packed & 0x7F);
if ((flipflops & 1) != 0 && zPixel < this.zbuf[offsetSE]) this.g3d.addPixel (offsetSE, zPixel, this.shades[((packed >> 7) & 0x3F)]);
if ((flipflops & 2) != 0 && zPixel < this.zbuf[offsetSW]) this.g3d.addPixel (offsetSW, zPixel, this.shades[((packed >> 13) & 0x3F)]);
if ((flipflops & 4) != 0 && zPixel < this.zbuf[offsetNE]) this.g3d.addPixel (offsetNE, zPixel, this.shades[((packed >> 19) & 0x3F)]);
if ((flipflops & 8) != 0 && zPixel < this.zbuf[offsetNW]) this.g3d.addPixel (offsetNW, zPixel, this.shades[((packed >> 25) & 0x3F)]);
++offsetSE;
--offsetSW;
++offsetNE;
--offsetNW;
flipflops = ~flipflops;
} while (packed >= 0);
offsetSouthCenter += this.width;
offsetNorthCenter -= this.width;
} while (--nLines > 0);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "renderShapeClipped", 
($fz = function (sphereShape) {
var offsetSphere = 0;
var evenSizeCorrection = 1 - (this.diameter & 1);
var offsetSouthCenter = this.offsetPbufBeginLine;
var offsetNorthCenter = offsetSouthCenter - evenSizeCorrection * this.width;
var nLines = Clazz.doubleToInt ((this.diameter + 1) / 2);
var ySouth = this.y;
var yNorth = this.y - evenSizeCorrection;
var randu = (this.x << 16) + (this.y << 1) ^ 0x33333333;
var flipflopSouthCenter = (this.x ^ this.y) & 1;
var flipflopNorthCenter = flipflopSouthCenter ^ evenSizeCorrection;
var flipflopSE = flipflopSouthCenter;
var flipflopSW = flipflopSouthCenter ^ evenSizeCorrection;
var flipflopNE = flipflopNorthCenter;
var flipflopNW = flipflopNorthCenter ^ evenSizeCorrection;
var flipflopsCenter = flipflopSE | (flipflopSW << 1) | (flipflopNE << 2) | (flipflopNW << 3);
do {
var tSouthVisible = ySouth >= 0 && ySouth < this.height;
var tNorthVisible = yNorth >= 0 && yNorth < this.height;
var offsetSE = offsetSouthCenter;
var offsetSW = offsetSouthCenter - evenSizeCorrection;
var offsetNE = offsetNorthCenter;
var offsetNW = offsetNorthCenter - evenSizeCorrection;
var packed;
var flipflops = (flipflopsCenter = ~flipflopsCenter);
var xEast = this.x;
var xWest = this.x - evenSizeCorrection;
do {
var tWestVisible = xWest >= 0 && xWest < this.width;
var tEastVisible = xEast >= 0 && xEast < this.width;
packed = sphereShape[offsetSphere++];
var isCore;
var zOffset = packed & 0x7F;
var zPixel;
if (this.z < this.slab) {
zPixel = this.z + zOffset;
isCore = (zPixel >= this.slab);
} else {
zPixel = this.z - zOffset;
isCore = (zPixel < this.slab);
}if (isCore) zPixel = this.slab;
if (zPixel >= this.slab && zPixel <= this.depth) {
if (tSouthVisible) {
if (tEastVisible && (this.addAllPixels || (flipflops & 1) != 0) && zPixel < this.zbuf[offsetSE]) {
var i = (isCore ? J.g3d.SphereRenderer.SHADE_SLAB_CLIPPED - 3 + ((randu >> 7) & 0x07) : (packed >> 7) & 0x3F);
this.g3d.addPixel (offsetSE, zPixel, this.shades[i]);
}if (tWestVisible && (this.addAllPixels || (flipflops & 2) != 0) && zPixel < this.zbuf[offsetSW]) {
var i = (isCore ? J.g3d.SphereRenderer.SHADE_SLAB_CLIPPED - 3 + ((randu >> 13) & 0x07) : (packed >> 13) & 0x3F);
this.g3d.addPixel (offsetSW, zPixel, this.shades[i]);
}}if (tNorthVisible) {
if (tEastVisible && (!this.tScreened || (flipflops & 4) != 0) && zPixel < this.zbuf[offsetNE]) {
var i = (isCore ? J.g3d.SphereRenderer.SHADE_SLAB_CLIPPED - 3 + ((randu >> 19) & 0x07) : (packed >> 19) & 0x3F);
this.g3d.addPixel (offsetNE, zPixel, this.shades[i]);
}if (tWestVisible && (!this.tScreened || (flipflops & 8) != 0) && zPixel < this.zbuf[offsetNW]) {
var i = (isCore ? J.g3d.SphereRenderer.SHADE_SLAB_CLIPPED - 3 + ((randu >> 25) & 0x07) : (packed >> 25) & 0x3F);
this.g3d.addPixel (offsetNW, zPixel, this.shades[i]);
}}}++offsetSE;
--offsetSW;
++offsetNE;
--offsetNW;
++xEast;
--xWest;
flipflops = ~flipflops;
if (isCore) randu = ((randu << 16) + (randu << 1) + randu) & 0x7FFFFFFF;
} while (packed >= 0);
offsetSouthCenter += this.width;
offsetNorthCenter -= this.width;
++ySouth;
--yNorth;
} while (--nLines > 0);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "renderLarge", 
($fz = function () {
if (this.mat != null) {
if (this.shader.ellipsoidShades == null) this.shader.createEllipsoidShades ();
if (this.octantPoints != null) this.setPlaneDerivatives ();
} else if (!this.shader.sphereShadingCalculated) this.shader.calcSphereShading ();
this.renderQuadrant (-1, -1);
this.renderQuadrant (-1, 1);
this.renderQuadrant (1, -1);
this.renderQuadrant (1, 1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderQuadrant", 
($fz = function (xSign, ySign) {
var radius = Clazz.doubleToInt (this.diameter / 2);
var t = this.x + radius * xSign;
var xStatus = (this.x < 0 ? -1 : this.x < this.width ? 0 : 1) + (t < 0 ? -2 : t < this.width ? 0 : 2);
if (xStatus == -3 || xStatus == 3) return;
t = this.y + radius * ySign;
var yStatus = (this.y < 0 ? -1 : this.y < this.height ? 0 : 1) + (t < 0 ? -2 : t < this.height ? 0 : 2);
if (yStatus == -3 || yStatus == 3) return;
var unclipped = (this.mat == null && xStatus == 0 && yStatus == 0 && this.z - radius >= this.slab && this.z <= this.depth);
if (unclipped) this.renderQuadrantUnclipped (radius, xSign, ySign);
 else this.renderQuadrantClipped (radius, xSign, ySign);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "renderQuadrantUnclipped", 
($fz = function (radius, xSign, ySign) {
var r2 = radius * radius;
var dDivisor = radius * 2 + 1;
var flipflopBeginLine = ((this.x ^ this.y) & 1) == 0;
var lineIncrement = (ySign < 0 ? -this.width : this.width);
var ptLine = this.offsetPbufBeginLine;
for (var i = 0, i2 = 0; i2 <= r2; i2 += i + (++i), ptLine += lineIncrement) {
var offset = ptLine;
var flipflop = (flipflopBeginLine = !flipflopBeginLine);
var s2 = r2 - i2;
var z0 = this.z - radius;
var y8 = Clazz.doubleToInt (((i * ySign + radius) << 8) / dDivisor);
for (var j = 0, j2 = 0; j2 <= s2; j2 += j + (++j), offset += xSign) {
if (this.addAllPixels || (flipflop = !flipflop)) {
if (this.zbuf[offset] <= z0) continue;
var k = Clazz.doubleToInt (Math.sqrt (s2 - j2));
z0 = this.z - k;
if (this.zbuf[offset] <= z0) continue;
var x8 = Clazz.doubleToInt (((j * xSign + radius) << 8) / dDivisor);
this.g3d.addPixel (offset, z0, this.shades[this.shader.sphereShadeIndexes[((y8 << 8) + x8)]]);
}}
}
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "renderQuadrantClipped", 
($fz = function (radius, xSign, ySign) {
var isEllipsoid = (this.mat != null);
var checkOctant = (this.selectedOctant >= 0);
var r2 = radius * radius;
var dDivisor = radius * 2 + 1;
var lineIncrement = (ySign < 0 ? -this.width : this.width);
var ptLine = this.offsetPbufBeginLine;
var randu = (this.x << 16) + (this.y << 1) ^ 0x33333333;
var yCurrent = this.y;
var y8 = 0;
var iShade = 0;
for (var i = 0, i2 = 0; i2 <= r2; i2 += i + (++i), ptLine += lineIncrement, yCurrent += ySign) {
if (yCurrent < 0) {
if (ySign < 0) return;
continue;
}if (yCurrent >= this.height) {
if (ySign > 0) return;
continue;
}var s2 = r2 - (isEllipsoid ? 0 : i2);
var xCurrent = this.x;
if (!isEllipsoid) {
y8 = Clazz.doubleToInt (((i * ySign + radius) << 8) / dDivisor);
}randu = ((randu << 16) + (randu << 1) + randu) & 0x7FFFFFFF;
var iRoot = -1;
var mode = 1;
var offset = ptLine;
for (var j = 0, j2 = 0; j2 <= s2; j2 += j + (++j), offset += xSign, xCurrent += xSign) {
if (xCurrent < 0) {
if (xSign < 0) break;
continue;
}if (xCurrent >= this.width) {
if (xSign > 0) break;
continue;
}if (this.tScreened && (((xCurrent ^ yCurrent) & 1) != 0)) continue;
var zPixel;
if (isEllipsoid) {
if (!J.g3d.SphereRenderer.getQuardricZ (xCurrent, yCurrent, this.coef, this.zroot)) {
if (iRoot >= 0) {
break;
}continue;
}iRoot = (this.z < this.slab ? 1 : 0);
zPixel = Clazz.doubleToInt (this.zroot[iRoot]);
if (zPixel == 0) zPixel = this.z;
mode = 2;
this.z0 = zPixel;
if (checkOctant) {
this.ptTemp.set (xCurrent - this.x, yCurrent - this.y, zPixel - this.z);
this.mat.transform (this.ptTemp);
var thisOctant = J.util.GData.getScreenOctant (this.ptTemp);
if (thisOctant == this.selectedOctant) {
iShade = this.getPlaneShade (xCurrent, yCurrent, this.zroot);
zPixel = Clazz.doubleToInt (this.zroot[0]);
mode = 3;
}var isCore = (this.z < this.slab ? zPixel >= this.slab : zPixel < this.slab);
if (isCore) {
this.z0 = zPixel = this.slab;
mode = 0;
}}if (zPixel < this.slab || zPixel > this.depth || this.zbuf[offset] <= this.z0) continue;
} else {
var zOffset = Clazz.doubleToInt (Math.sqrt (s2 - j2));
zPixel = this.z + (this.z < this.slab ? zOffset : -zOffset);
var isCore = (this.z < this.slab ? zPixel >= this.slab : zPixel < this.slab);
if (isCore) {
zPixel = this.slab;
mode = 0;
}if (zPixel < this.slab || zPixel > this.depth || this.zbuf[offset] <= zPixel) continue;
}switch (mode) {
case 0:
iShade = (J.g3d.SphereRenderer.SHADE_SLAB_CLIPPED - 3 + ((randu >> 8) & 0x07));
randu = ((randu << 16) + (randu << 1) + randu) & 0x7FFFFFFF;
mode = 1;
break;
case 2:
iShade = this.shader.getEllipsoidShade (xCurrent, yCurrent, this.zroot[iRoot], radius, this.mDeriv);
break;
case 3:
this.g3d.clearPixel (offset, this.z0);
break;
default:
var x8 = Clazz.doubleToInt (((j * xSign + radius) << 8) / dDivisor);
iShade = this.shader.sphereShadeIndexes[(y8 << 8) + x8];
break;
}
this.g3d.addPixel (offset, zPixel, this.shades[iShade]);
}
randu = ((randu + xCurrent + yCurrent) | 1) & 0x7FFFFFFF;
}
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
c$.getQuardricZ = $_M(c$, "getQuardricZ", 
($fz = function (x, y, coef, zroot) {
var b_2a = (coef[4] * x + coef[5] * y + coef[8]) / coef[2] / 2;
var c_a = (coef[0] * x * x + coef[1] * y * y + coef[3] * x * y + coef[6] * x + coef[7] * y - 1) / coef[2];
var f = b_2a * b_2a - c_a;
if (f < 0) return false;
f = Math.sqrt (f);
zroot[0] = (-b_2a - f);
zroot[1] = (-b_2a + f);
return true;
}, $fz.isPrivate = true, $fz), "~N,~N,~A,~A");
$_M(c$, "setPlaneDerivatives", 
($fz = function () {
this.planeShade = -1;
for (var i = 0; i < 3; i++) {
var dx = this.dxyz[i][0] = this.octantPoints[i].x - this.x;
var dy = this.dxyz[i][1] = this.octantPoints[i].y - this.y;
var dz = this.dxyz[i][2] = this.octantPoints[i].z - this.z;
this.planeShades[i] = this.shader.getShadeIndex (dx, dy, -dz);
if (dx == 0 && dy == 0) {
this.planeShade = this.planeShades[i];
return;
}}
}, $fz.isPrivate = true, $fz));
$_M(c$, "getPlaneShade", 
($fz = function (xCurrent, yCurrent, zroot) {
if (this.planeShade >= 0) return this.planeShade;
var iMin = 3;
var dz;
var zMin = 3.4028235E38;
for (var i = 0; i < 3; i++) {
if ((dz = this.dxyz[i][2]) == 0) continue;
var ptz = this.z + (-this.dxyz[i][0] * (xCurrent - this.x) - this.dxyz[i][1] * (yCurrent - this.y)) / dz;
if (ptz < zMin) {
zMin = ptz;
iMin = i;
}}
if (iMin == 3) {
iMin = 0;
zMin = this.z;
}zroot[0] = zMin;
return this.planeShades[iMin];
}, $fz.isPrivate = true, $fz), "~N,~N,~A");
Clazz.defineStatics (c$,
"maxOddSizeSphere", 49,
"maxSphereDiameter", 1000,
"maxSphereDiameter2", 2000);
c$.SHADE_SLAB_CLIPPED = c$.prototype.SHADE_SLAB_CLIPPED = J.util.Shader.shadeIndexNormal - 5;
});
