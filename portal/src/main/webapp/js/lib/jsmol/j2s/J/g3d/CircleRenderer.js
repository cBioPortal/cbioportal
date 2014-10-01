Clazz.declarePackage ("J.g3d");
Clazz.load (["J.g3d.G3DRenderer"], "J.g3d.CircleRenderer", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.g3d = null;
this.xCenter = 0;
this.yCenter = 0;
this.zCenter = 0;
this.sizeCorrection = 0;
Clazz.instantialize (this, arguments);
}, J.g3d, "CircleRenderer", null, J.g3d.G3DRenderer);
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "set", 
function (g3d) {
try {
this.g3d = g3d;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return this;
}, "J.api.JmolRendererInterface");
Clazz.defineMethod (c$, "plotCircleCenteredClipped", 
function (xCenter, yCenter, zCenter, diameter) {
if (this.g3d.isClippedXY (diameter, xCenter, yCenter)) return;
var r = Clazz.doubleToInt (diameter / 2);
this.sizeCorrection = 1 - (diameter & 1);
this.xCenter = xCenter;
this.yCenter = yCenter;
this.zCenter = zCenter;
var x = r;
var y = 0;
var xChange = 1 - 2 * r;
var yChange = 1;
var radiusError = 0;
while (x >= y) {
this.plot8CircleCenteredClipped (x, y);
++y;
radiusError += yChange;
yChange += 2;
if (2 * radiusError + xChange > 0) {
--x;
radiusError += xChange;
xChange += 2;
}}
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "plotCircleCenteredUnclipped", 
function (xCenter, yCenter, zCenter, diameter) {
var r = Clazz.doubleToInt (diameter / 2);
this.sizeCorrection = 1 - (diameter & 1);
this.xCenter = xCenter;
this.yCenter = yCenter;
this.zCenter = zCenter;
var x = r;
var y = 0;
var xChange = 1 - 2 * r;
var yChange = 1;
var radiusError = 0;
while (x >= y) {
this.plot8CircleCenteredUnclipped (x, y);
++y;
radiusError += yChange;
yChange += 2;
if (2 * radiusError + xChange > 0) {
--x;
radiusError += xChange;
xChange += 2;
}}
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "plotFilledCircleCenteredClipped", 
function (xCenter, yCenter, zCenter, diameter) {
var r = Clazz.doubleToInt (diameter / 2);
this.sizeCorrection = 1 - (diameter & 1);
this.xCenter = xCenter;
this.yCenter = yCenter;
this.zCenter = zCenter;
var x = r;
var y = 0;
var xChange = 1 - 2 * r;
var yChange = 1;
var radiusError = 0;
while (x >= y) {
this.plot8FilledCircleCenteredClipped (x, y);
++y;
radiusError += yChange;
yChange += 2;
if (2 * radiusError + xChange > 0) {
--x;
radiusError += xChange;
xChange += 2;
}}
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "plotFilledCircleCenteredUnclipped", 
function (xCenter, yCenter, zCenter, diameter) {
var r = Clazz.doubleToInt (diameter / 2);
this.xCenter = xCenter;
this.yCenter = yCenter;
this.zCenter = zCenter;
var x = r;
var y = 0;
var xChange = 1 - 2 * r;
var yChange = 1;
var radiusError = 0;
while (x >= y) {
this.plot8FilledCircleCenteredUnclipped (x, y);
++y;
radiusError += yChange;
yChange += 2;
if (2 * radiusError + xChange > 0) {
--x;
radiusError += xChange;
xChange += 2;
}}
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "plot8CircleCenteredClipped", 
 function (dx, dy) {
this.g3d.plotPixelClippedXYZ (this.xCenter + dx - this.sizeCorrection, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter + dx - this.sizeCorrection, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter - dx, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter - dx, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter + dy - this.sizeCorrection, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter + dy - this.sizeCorrection, this.yCenter - dx, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter - dy, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter - dy, this.yCenter - dx, this.zCenter);
}, "~N,~N");
Clazz.defineMethod (c$, "plot8CircleCenteredUnclipped", 
 function (dx, dy) {
this.g3d.plotPixelUnclipped (this.xCenter + dx - this.sizeCorrection, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter + dx - this.sizeCorrection, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter - dx, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter - dx, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter + dy - this.sizeCorrection, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter + dy - this.sizeCorrection, this.yCenter - dx, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter - dy, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter - dy, this.yCenter - dx, this.zCenter);
}, "~N,~N");
Clazz.defineMethod (c$, "plot8FilledCircleCenteredClipped", 
 function (dx, dy) {
this.g3d.plotPixelsClipped (2 * dx + 1 - this.sizeCorrection, this.xCenter - dx, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelsClipped (2 * dx + 1 - this.sizeCorrection, this.xCenter - dx, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelsClipped (2 * dy + 1 - this.sizeCorrection, this.xCenter - dy, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelsClipped (2 * dy + 1 - this.sizeCorrection, this.xCenter - dy, this.yCenter - dx, this.zCenter);
}, "~N,~N");
Clazz.defineMethod (c$, "plot8FilledCircleCenteredUnclipped", 
 function (dx, dy) {
this.g3d.plotPixelsUnclippedCount (2 * dx + 1 - this.sizeCorrection, this.xCenter - dx, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelsUnclippedCount (2 * dx + 1 - this.sizeCorrection, this.xCenter - dx, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelsUnclippedCount (2 * dy + 1 - this.sizeCorrection, this.xCenter - dy, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelsUnclippedCount (2 * dy + 1 - this.sizeCorrection, this.xCenter - dy, this.yCenter - dx, this.zCenter);
}, "~N,~N");
});
