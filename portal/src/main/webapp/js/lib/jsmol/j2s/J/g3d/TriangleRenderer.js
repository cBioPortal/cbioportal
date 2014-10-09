Clazz.declarePackage ("J.g3d");
Clazz.load (["J.g3d.G3DRenderer", "JU.Rgb16"], "J.g3d.TriangleRenderer", ["JU.GData"], function () {
c$ = Clazz.decorateAsClass (function () {
this.g3d = null;
this.ax = null;
this.ay = null;
this.az = null;
this.axW = null;
this.azW = null;
this.axE = null;
this.azE = null;
this.rgb16sW = null;
this.rgb16sE = null;
this.rgb16sGouraud = null;
this.rgb16t1 = null;
this.rgb16t2 = null;
Clazz.instantialize (this, arguments);
}, J.g3d, "TriangleRenderer", null, J.g3d.G3DRenderer);
Clazz.prepareFields (c$, function () {
this.ax =  Clazz.newIntArray (3, 0);
this.ay =  Clazz.newIntArray (3, 0);
this.az =  Clazz.newIntArray (3, 0);
this.axW =  Clazz.newIntArray (64, 0);
this.azW =  Clazz.newIntArray (64, 0);
this.axE =  Clazz.newIntArray (64, 0);
this.azE =  Clazz.newIntArray (64, 0);
this.rgb16t1 =  new JU.Rgb16 ();
this.rgb16t2 =  new JU.Rgb16 ();
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "set", 
function (g3d) {
try {
this.rgb16sW =  new Array (64);
this.rgb16sE =  new Array (64);
for (var i = 64; --i >= 0; ) {
this.rgb16sW[i] =  new JU.Rgb16 ();
this.rgb16sE[i] =  new JU.Rgb16 ();
}
this.g3d = g3d;
this.rgb16sGouraud =  new Array (3);
for (var i = 3; --i >= 0; ) this.rgb16sGouraud[i] =  new JU.Rgb16 ();

} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return this;
}, "J.api.JmolRendererInterface");
Clazz.defineMethod (c$, "reallocRgb16s", 
function (rgb16s, n) {
var t =  new Array (n);
System.arraycopy (rgb16s, 0, t, 0, rgb16s.length);
for (var i = rgb16s.length; i < n; ++i) t[i] =  new JU.Rgb16 ();

return t;
}, "~A,~N");
Clazz.defineMethod (c$, "setGouraud", 
function (rgbA, rgbB, rgbC) {
this.rgb16sGouraud[0].setInt (rgbA);
this.rgb16sGouraud[1].setInt (rgbB);
this.rgb16sGouraud[2].setInt (rgbC);
}, "~N,~N,~N");
Clazz.defineMethod (c$, "drawfillTriangle", 
function (xA, yA, zA, xB, yB, zB, xC, yC, zC, useGouraud) {
this.ax[0] = xA;
this.ax[1] = xB;
this.ax[2] = xC;
this.ay[0] = yA;
this.ay[1] = yB;
this.ay[2] = yC;
this.az[0] = zA;
this.az[1] = zB;
this.az[2] = zC;
this.fillTriangleB (useGouraud);
}, "~N,~N,~N,~N,~N,~N,~N,~N,~N,~B");
Clazz.defineMethod (c$, "fillTriangleXYZ", 
function (xScreenA, yScreenA, zScreenA, xScreenB, yScreenB, zScreenB, xScreenC, yScreenC, zScreenC, useGouraud) {
this.ax[0] = xScreenA;
this.ax[1] = xScreenB;
this.ax[2] = xScreenC;
this.ay[0] = yScreenA;
this.ay[1] = yScreenB;
this.ay[2] = yScreenC;
this.az[0] = zScreenA;
this.az[1] = zScreenB;
this.az[2] = zScreenC;
this.fillTriangleB (useGouraud);
}, "~N,~N,~N,~N,~N,~N,~N,~N,~N,~B");
Clazz.defineMethod (c$, "fillTriangleP3i", 
function (screenA, screenB, screenC, useGouraud) {
this.ax[0] = screenA.x;
this.ax[1] = screenB.x;
this.ax[2] = screenC.x;
this.ay[0] = screenA.y;
this.ay[1] = screenB.y;
this.ay[2] = screenC.y;
this.az[0] = screenA.z;
this.az[1] = screenB.z;
this.az[2] = screenC.z;
this.fillTriangleB (useGouraud);
}, "JU.P3i,JU.P3i,JU.P3i,~B");
Clazz.defineMethod (c$, "fillTriangleP3f", 
function (screenA, screenB, screenC, useGouraud) {
this.ax[0] = Math.round (screenA.x);
this.ax[1] = Math.round (screenB.x);
this.ax[2] = Math.round (screenC.x);
this.ay[0] = Math.round (screenA.y);
this.ay[1] = Math.round (screenB.y);
this.ay[2] = Math.round (screenC.y);
this.az[0] = Math.round (screenA.z);
this.az[1] = Math.round (screenB.z);
this.az[2] = Math.round (screenC.z);
this.fillTriangleB (useGouraud);
}, "JU.P3,JU.P3,JU.P3,~B");
Clazz.defineMethod (c$, "fillTriangleP3if", 
function (screenA, screenB, screenC, factor, useGouraud) {
this.ax[0] = screenA.x;
this.ax[1] = screenB.x;
this.ax[2] = screenC.x;
this.ay[0] = screenA.y;
this.ay[1] = screenB.y;
this.ay[2] = screenC.y;
this.az[0] = screenA.z;
this.az[1] = screenB.z;
this.az[2] = screenC.z;
J.g3d.TriangleRenderer.adjustVertex (this.ax, factor);
J.g3d.TriangleRenderer.adjustVertex (this.ay, factor);
J.g3d.TriangleRenderer.adjustVertex (this.az, factor);
this.fillTriangleB (useGouraud);
}, "JU.P3i,JU.P3i,JU.P3i,~N,~B");
c$.adjustVertex = Clazz.defineMethod (c$, "adjustVertex", 
 function (t, factor) {
var av = (t[0] + t[1] + t[2]) / 3;
for (var i = 0; i < 3; i++) t[i] += factor * (av - t[i]);

}, "~A,~N");
Clazz.defineMethod (c$, "fillTriangleB", 
 function (useGouraud) {
if (this.az[0] <= 1 || this.az[1] <= 1 || this.az[2] <= 1) return;
var cc0 = this.g3d.clipCode3 (this.ax[0], this.ay[0], this.az[0]);
var cc1 = this.g3d.clipCode3 (this.ax[1], this.ay[1], this.az[1]);
var cc2 = this.g3d.clipCode3 (this.ax[2], this.ay[2], this.az[2]);
var isClipped = (cc0 | cc1 | cc2) != 0;
if (isClipped) {
if ((cc0 & cc1 & cc2) != 0) {
return;
}}var iMinY = 0;
if (this.ay[1] < this.ay[iMinY]) iMinY = 1;
if (this.ay[2] < this.ay[iMinY]) iMinY = 2;
var iMidY = (iMinY + 1) % 3;
var iMaxY = (iMinY + 2) % 3;
if (this.ay[iMidY] > this.ay[iMaxY]) {
var t = iMidY;
iMidY = iMaxY;
iMaxY = t;
}var yMin = this.ay[iMinY];
var yMid = this.ay[iMidY];
var yMax = this.ay[iMaxY];
var nLines = yMax - yMin + 1;
if (nLines > this.g3d.height * 3) return;
if (nLines > this.axW.length) this.reallocRasterArrays (nLines);
var gouraudW;
var gouraudE;
if (useGouraud) {
gouraudW = this.rgb16sW;
gouraudE = this.rgb16sE;
} else {
gouraudW = gouraudE = null;
}var dyMidMin = yMid - yMin;
if (dyMidMin == 0) {
if (this.ax[iMidY] < this.ax[iMinY]) {
var t = iMidY;
iMidY = iMinY;
iMinY = t;
}this.generateRaster (nLines, iMinY, iMaxY, this.axW, this.azW, 0, gouraudW);
this.generateRaster (nLines, iMidY, iMaxY, this.axE, this.azE, 0, gouraudE);
} else if (yMid == yMax) {
if (this.ax[iMaxY] < this.ax[iMidY]) {
var t = iMidY;
iMidY = iMaxY;
iMaxY = t;
}this.generateRaster (nLines, iMinY, iMidY, this.axW, this.azW, 0, gouraudW);
this.generateRaster (nLines, iMinY, iMaxY, this.axE, this.azE, 0, gouraudE);
} else {
var dxMaxMin = this.ax[iMaxY] - this.ax[iMinY];
var roundFactor;
roundFactor = JU.GData.roundInt (Clazz.doubleToInt (nLines / 2));
if (dxMaxMin < 0) roundFactor = -roundFactor;
var axSplit = this.ax[iMinY] + Clazz.doubleToInt ((dxMaxMin * dyMidMin + roundFactor) / nLines);
if (axSplit < this.ax[iMidY]) {
this.generateRaster (nLines, iMinY, iMaxY, this.axW, this.azW, 0, gouraudW);
this.generateRaster (dyMidMin + 1, iMinY, iMidY, this.axE, this.azE, 0, gouraudE);
this.generateRaster (nLines - dyMidMin, iMidY, iMaxY, this.axE, this.azE, dyMidMin, gouraudE);
} else {
this.generateRaster (dyMidMin + 1, iMinY, iMidY, this.axW, this.azW, 0, gouraudW);
this.generateRaster (nLines - dyMidMin, iMidY, iMaxY, this.axW, this.azW, dyMidMin, gouraudW);
this.generateRaster (nLines, iMinY, iMaxY, this.axE, this.azE, 0, gouraudE);
}}this.g3d.setZMargin (5);
if (useGouraud) this.fillRasterG (yMin, nLines, isClipped, this.g3d.$isPass2 ? 1 : 0);
 else this.fillRaster (yMin, nLines, isClipped, this.g3d.$isPass2 ? 1 : 0);
this.g3d.setZMargin (0);
}, "~B");
Clazz.defineMethod (c$, "reallocRasterArrays", 
 function (n) {
n = (n + 31) & -32;
this.axW =  Clazz.newIntArray (n, 0);
this.azW =  Clazz.newIntArray (n, 0);
this.axE =  Clazz.newIntArray (n, 0);
this.azE =  Clazz.newIntArray (n, 0);
this.rgb16sW = this.reallocRgb16s (this.rgb16sW, n);
this.rgb16sE = this.reallocRgb16s (this.rgb16sE, n);
}, "~N");
Clazz.defineMethod (c$, "generateRaster", 
 function (dy, iN, iS, axRaster, azRaster, iRaster, gouraud) {
var xN = this.ax[iN];
var zN = this.az[iN];
var xS = this.ax[iS];
var zS = this.az[iS];
var dx = xS - xN;
var dz = zS - zN;
var xCurrent = xN;
var xIncrement;
var width;
var errorTerm;
if (dx >= 0) {
xIncrement = 1;
width = dx;
errorTerm = 0;
} else {
xIncrement = -1;
width = -dx;
errorTerm = 1 - dy;
}var zCurrentScaled = (zN << 10) + (512);
var roundingFactor = JU.GData.roundInt (Clazz.doubleToInt (dy / 2));
if (dz < 0) roundingFactor = -roundingFactor;
var zIncrementScaled = Clazz.doubleToInt (((dz << 10) + roundingFactor) / dy);
var xMajorIncrement;
var xMajorError;
if (width <= dy) {
xMajorIncrement = 0;
xMajorError = width;
} else {
xMajorIncrement = JU.GData.roundInt (Clazz.doubleToInt (dx / dy));
xMajorError = width % dy;
}for (var y = 0, i = iRaster; y < dy; zCurrentScaled += zIncrementScaled, ++i, ++y) {
axRaster[i] = xCurrent;
azRaster[i] = zCurrentScaled >> 10;
xCurrent += xMajorIncrement;
errorTerm += xMajorError;
if (errorTerm > 0) {
xCurrent += xIncrement;
errorTerm -= dy;
}}
if (gouraud != null) {
var rgb16Base = this.rgb16t1;
rgb16Base.setRgb (this.rgb16sGouraud[iN]);
var rgb16Increment = this.rgb16t2;
rgb16Increment.diffDiv (this.rgb16sGouraud[iS], rgb16Base, dy);
for (var i = iRaster, iMax = iRaster + dy; i < iMax; ++i) gouraud[i].setAndIncrement (rgb16Base, rgb16Increment);

}}, "~N,~N,~N,~A,~A,~N,~A");
Clazz.defineMethod (c$, "fillRaster", 
 function (y, numLines, isClipped, correction) {
var i = 0;
if (y < 0) {
numLines += y;
i -= y;
y = 0;
}if (y + numLines > this.g3d.height) numLines = this.g3d.height - y;
if (isClipped) {
for (; --numLines >= correction; ++y, ++i) {
var xW = this.axW[i];
var pixelCount = this.axE[i] - xW + 1 - correction;
if (pixelCount > 0) this.g3d.plotPixelsClippedRaster (pixelCount, xW, y, this.azW[i], this.azE[i], null, null);
}
} else {
var xW;
for (; --numLines >= correction; ++y, ++i) {
var pixelCount = this.axE[i] - (xW = this.axW[i]) + 1 - correction;
if (correction == 1 && pixelCount < 0) {
pixelCount = 1;
xW--;
}if (pixelCount > 0) this.g3d.plotPixelsUnclippedRaster (pixelCount, xW, y, this.azW[i], this.azE[i], null, null);
}
}}, "~N,~N,~B,~N");
Clazz.defineMethod (c$, "fillRasterG", 
 function (y, numLines, isClipped, correction) {
var i = 0;
if (y < 0) {
numLines += y;
i -= y;
y = 0;
}if (y + numLines > this.g3d.height) numLines = this.g3d.height - y;
if (isClipped) {
for (; --numLines >= correction; ++y, ++i) {
var xW = this.axW[i];
var pixelCount = this.axE[i] - xW + 1 - correction;
if (pixelCount > 0) this.g3d.plotPixelsClippedRaster (pixelCount, xW, y, this.azW[i], this.azE[i], this.rgb16sW[i], this.rgb16sE[i]);
}
} else {
var xW;
for (; --numLines >= correction; ++y, ++i) {
var pixelCount = this.axE[i] - (xW = this.axW[i]) + 1 - correction;
if (correction == 1 && pixelCount < 0) {
pixelCount = 1;
xW--;
}if (pixelCount > 0) this.g3d.plotPixelsUnclippedRaster (pixelCount, xW, y, this.azW[i], this.azE[i], this.rgb16sW[i], this.rgb16sE[i]);
}
}}, "~N,~N,~B,~N");
Clazz.defineStatics (c$,
"DEFAULT", 64);
});
