Clazz.declarePackage ("J.g3d");
c$ = Clazz.decorateAsClass (function () {
this.g3d = null;
this.xCenter = 0;
this.yCenter = 0;
this.zCenter = 0;
this.sizeCorrection = 0;
Clazz.instantialize (this, arguments);
}, J.g3d, "CircleRenderer");
Clazz.makeConstructor (c$, 
function (g3d) {
this.g3d = g3d;
}, "J.g3d.Graphics3D");
$_M(c$, "plotCircleCenteredClipped", 
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
$_M(c$, "plotCircleCenteredUnclipped", 
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
$_M(c$, "plotFilledCircleCenteredClipped", 
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
$_M(c$, "plotFilledCircleCenteredUnclipped", 
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
$_M(c$, "plot8CircleCenteredClipped", 
($fz = function (dx, dy) {
this.g3d.plotPixelClippedXYZ (this.xCenter + dx - this.sizeCorrection, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter + dx - this.sizeCorrection, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter - dx, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter - dx, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter + dy - this.sizeCorrection, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter + dy - this.sizeCorrection, this.yCenter - dx, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter - dy, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelClippedXYZ (this.xCenter - dy, this.yCenter - dx, this.zCenter);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "plot8CircleCenteredUnclipped", 
($fz = function (dx, dy) {
this.g3d.plotPixelUnclipped (this.xCenter + dx - this.sizeCorrection, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter + dx - this.sizeCorrection, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter - dx, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter - dx, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter + dy - this.sizeCorrection, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter + dy - this.sizeCorrection, this.yCenter - dx, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter - dy, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelUnclipped (this.xCenter - dy, this.yCenter - dx, this.zCenter);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "plot8FilledCircleCenteredClipped", 
($fz = function (dx, dy) {
this.g3d.plotPixelsClipped (2 * dx + 1 - this.sizeCorrection, this.xCenter - dx, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelsClipped (2 * dx + 1 - this.sizeCorrection, this.xCenter - dx, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelsClipped (2 * dy + 1 - this.sizeCorrection, this.xCenter - dy, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelsClipped (2 * dy + 1 - this.sizeCorrection, this.xCenter - dy, this.yCenter - dx, this.zCenter);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "plot8FilledCircleCenteredUnclipped", 
($fz = function (dx, dy) {
this.g3d.plotPixelsUnclippedCount (2 * dx + 1 - this.sizeCorrection, this.xCenter - dx, this.yCenter + dy - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelsUnclippedCount (2 * dx + 1 - this.sizeCorrection, this.xCenter - dx, this.yCenter - dy, this.zCenter);
this.g3d.plotPixelsUnclippedCount (2 * dy + 1 - this.sizeCorrection, this.xCenter - dy, this.yCenter + dx - this.sizeCorrection, this.zCenter);
this.g3d.plotPixelsUnclippedCount (2 * dy + 1 - this.sizeCorrection, this.xCenter - dy, this.yCenter - dx, this.zCenter);
}, $fz.isPrivate = true, $fz), "~N,~N");
