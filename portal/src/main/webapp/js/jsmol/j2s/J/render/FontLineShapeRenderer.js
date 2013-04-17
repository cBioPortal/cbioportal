Clazz.declarePackage ("J.render");
Clazz.load (["J.render.ShapeRenderer", "J.util.P3", "$.P3i", "$.V3"], "J.render.FontLineShapeRenderer", ["java.lang.Float", "J.constant.EnumAxesMode", "J.util.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.imageFontScaling = 0;
this.atomA = null;
this.atomB = null;
this.atomC = null;
this.atomD = null;
this.font3d = null;
this.pt0 = null;
this.pt1 = null;
this.pt2 = null;
this.pointT = null;
this.pointT2 = null;
this.pointT3 = null;
this.vectorT = null;
this.vectorT2 = null;
this.vectorT3 = null;
this.tickInfo = null;
this.draw000 = true;
this.endcap = 3;
Clazz.instantialize (this, arguments);
}, J.render, "FontLineShapeRenderer", J.render.ShapeRenderer);
Clazz.prepareFields (c$, function () {
this.pt0 =  new J.util.P3i ();
this.pt1 =  new J.util.P3i ();
this.pt2 =  new J.util.P3i ();
this.pointT =  new J.util.P3 ();
this.pointT2 =  new J.util.P3 ();
this.pointT3 =  new J.util.P3 ();
this.vectorT =  new J.util.V3 ();
this.vectorT2 =  new J.util.V3 ();
this.vectorT3 =  new J.util.V3 ();
});
$_M(c$, "getDiameter", 
function (z, madOrPixels) {
var diameter;
var isMad = (madOrPixels > 20);
switch (this.exportType) {
case 1:
diameter = (isMad ? madOrPixels : Clazz.doubleToInt (Math.floor (this.viewer.unscaleToScreen (z, madOrPixels * 2) * 1000)));
break;
default:
if (isMad) {
diameter = this.viewer.scaleToScreen (z, madOrPixels);
} else {
if (this.g3d.isAntialiased ()) madOrPixels += madOrPixels;
diameter = madOrPixels;
}}
return diameter;
}, "~N,~N");
$_M(c$, "renderLine", 
function (p0, p1, diameter, pt0, pt1, drawTicks) {
pt0.set (Clazz.doubleToInt (Math.floor (p0.x)), Clazz.doubleToInt (Math.floor (p0.y)), Clazz.doubleToInt (Math.floor (p0.z)));
pt1.set (Clazz.doubleToInt (Math.floor (p1.x)), Clazz.doubleToInt (Math.floor (p1.y)), Clazz.doubleToInt (Math.floor (p1.z)));
if (diameter < 0) this.g3d.drawDottedLine (pt0, pt1);
 else this.g3d.fillCylinder (this.endcap, diameter, pt0, pt1);
if (!drawTicks || this.tickInfo == null) return;
this.atomA.screenX = pt0.x;
this.atomA.screenY = pt0.y;
this.atomA.screenZ = pt0.z;
this.atomB.screenX = pt1.x;
this.atomB.screenY = pt1.y;
this.atomB.screenZ = pt1.z;
this.drawTicks (this.atomA, this.atomB, diameter, true);
}, "J.util.P3,J.util.P3,~N,J.util.P3i,J.util.P3i,~B");
$_M(c$, "drawTicks", 
function (pt1, pt2, diameter, withLabels) {
if (Float.isNaN (this.tickInfo.first)) this.tickInfo.first = 0;
this.drawTicks2 (pt1, pt2, this.tickInfo.ticks.x, 8, diameter, (!withLabels ? null : this.tickInfo.tickLabelFormats == null ? ["%0.2f"] : this.tickInfo.tickLabelFormats));
this.drawTicks2 (pt1, pt2, this.tickInfo.ticks.y, 4, diameter, null);
this.drawTicks2 (pt1, pt2, this.tickInfo.ticks.z, 2, diameter, null);
}, "J.util.Point3fi,J.util.Point3fi,~N,~B");
$_M(c$, "drawTicks2", 
($fz = function (ptA, ptB, dx, length, diameter, formats) {
if (dx == 0) return;
if (this.g3d.isAntialiased ()) length *= 2;
this.vectorT2.set (ptB.screenX, ptB.screenY, 0);
this.vectorT.set (ptA.screenX, ptA.screenY, 0);
this.vectorT2.sub (this.vectorT);
if (this.vectorT2.length () < 50) return;
var signFactor = this.tickInfo.signFactor;
this.vectorT.setT (ptB);
this.vectorT.sub (ptA);
var d0 = this.vectorT.length ();
if (this.tickInfo.scale != null) {
if (Float.isNaN (this.tickInfo.scale.x)) {
var a = this.viewer.getUnitCellInfo (0);
if (!Float.isNaN (a)) this.vectorT.set (this.vectorT.x / a, this.vectorT.y / this.viewer.getUnitCellInfo (1), this.vectorT.z / this.viewer.getUnitCellInfo (2));
} else {
this.vectorT.set (this.vectorT.x * this.tickInfo.scale.x, this.vectorT.y * this.tickInfo.scale.y, this.vectorT.z * this.tickInfo.scale.z);
}}var d = this.vectorT.length () + 0.0001 * dx;
if (d < dx) return;
var f = dx / d * d0 / d;
this.vectorT.scale (f);
var dz = (ptB.screenZ - ptA.screenZ) / (d / dx);
d += this.tickInfo.first;
var p = (Clazz.doubleToInt (Math.floor (this.tickInfo.first / dx))) * dx - this.tickInfo.first;
this.pointT.scaleAdd2 (p / dx, this.vectorT, ptA);
p += this.tickInfo.first;
var z = ptA.screenZ;
if (diameter < 0) diameter = 1;
this.vectorT2.set (-this.vectorT2.y, this.vectorT2.x, 0);
this.vectorT2.scale (length / this.vectorT2.length ());
var ptRef = this.tickInfo.reference;
if (ptRef == null) {
this.pointT3.setT (this.viewer.getBoundBoxCenter ());
if (this.viewer.getAxesMode () === J.constant.EnumAxesMode.BOUNDBOX) {
this.pointT3.x += 1.0;
this.pointT3.y += 1.0;
this.pointT3.z += 1.0;
}} else {
this.pointT3.setT (ptRef);
}this.viewer.transformPtScr (this.pointT3, this.pt2);
var horizontal = (Math.abs (this.vectorT2.x / this.vectorT2.y) < 0.2);
var centerX = horizontal;
var centerY = !horizontal;
var rightJustify = !centerX && (this.vectorT2.x < 0);
var drawLabel = (formats != null && formats.length > 0);
var x;
var y;
var val =  new Array (1);
var i = (this.draw000 ? 0 : -1);
while (p < d) {
if (p >= this.tickInfo.first) {
this.pointT2.setT (this.pointT);
this.viewer.transformPt3f (this.pointT2, this.pointT2);
this.drawLine (Clazz.doubleToInt (Math.floor (this.pointT2.x)), Clazz.doubleToInt (Math.floor (this.pointT2.y)), Clazz.floatToInt (z), (x = Clazz.doubleToInt (Math.floor (this.pointT2.x + this.vectorT2.x))), (y = Clazz.doubleToInt (Math.floor (this.pointT2.y + this.vectorT2.y))), Clazz.floatToInt (z), diameter);
if (drawLabel && (this.draw000 || p != 0)) {
val[0] = Float.$valueOf ((p == 0 ? 0 : p * signFactor));
var s = J.util.TextFormat.sprintf (formats[i % formats.length], "f", val);
this.drawString (x, y, Clazz.floatToInt (z), 4, rightJustify, centerX, centerY, Clazz.doubleToInt (Math.floor (this.pointT2.y)), s);
}}this.pointT.add (this.vectorT);
p += dx;
z += dz;
i++;
}
}, $fz.isPrivate = true, $fz), "J.util.Point3fi,J.util.Point3fi,~N,~N,~N,~A");
$_M(c$, "drawLine", 
function (x1, y1, z1, x2, y2, z2, diameter) {
return this.drawLine2 (x1, y1, z1, x2, y2, z2, diameter);
}, "~N,~N,~N,~N,~N,~N,~N");
$_M(c$, "drawLine2", 
function (x1, y1, z1, x2, y2, z2, diameter) {
this.pt0.set (x1, y1, z1);
this.pt1.set (x2, y2, z2);
if (diameter < 0) {
this.g3d.drawDashedLine (4, 2, this.pt0, this.pt1);
return 1;
}this.g3d.fillCylinder (2, diameter, this.pt0, this.pt1);
return Clazz.doubleToInt ((diameter + 1) / 2);
}, "~N,~N,~N,~N,~N,~N,~N");
$_M(c$, "drawString", 
function (x, y, z, radius, rightJustify, centerX, centerY, yRef, sVal) {
if (sVal == null) return;
var width = this.font3d.stringWidth (sVal);
var height = this.font3d.getAscent ();
var xT = x;
if (rightJustify) xT -= Clazz.doubleToInt (radius / 2) + 2 + width;
 else if (centerX) xT -= Clazz.doubleToInt (radius / 2) + 2 + Clazz.doubleToInt (width / 2);
 else xT += Clazz.doubleToInt (radius / 2) + 2;
var yT = y;
if (centerY) yT += Clazz.doubleToInt (height / 2);
 else if (yRef == 0 || yRef < y) yT += height;
 else yT -= Clazz.doubleToInt (radius / 2);
var zT = z - radius - 2;
if (zT < 1) zT = 1;
this.g3d.drawString (sVal, this.font3d, xT, yT, zT, zT, 0);
}, "~N,~N,~N,~N,~B,~B,~B,~N,~S");
});
