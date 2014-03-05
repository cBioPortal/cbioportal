Clazz.declarePackage ("J.renderspecial");
Clazz.load (["J.render.ShapeRenderer", "J.util.BS", "$.Matrix3f", "$.Matrix4f", "$.P3", "$.P3i", "$.V3", "J.viewer.JC"], "J.renderspecial.EllipsoidsRenderer", ["java.lang.Float", "J.shapespecial.Ellipsoid", "J.util.C", "$.GData", "$.Normix", "$.Parser"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ellipsoids = null;
this.bGlobals = null;
this.bOptions = null;
this.OPTS = null;
this.fillArc = false;
this.isSet = false;
this.diameter = 0;
this.diameter0 = 0;
this.dotCount = 0;
this.dotScale = 0;
this.dx = 0;
this.eigenSignMask = 7;
this.iCutout = -1;
this.selectedOctant = -1;
this.coords = null;
this.axes = null;
this.center = null;
this.perspectiveFactor = 0;
this.bsTemp = null;
this.mat = null;
this.mTemp = null;
this.mDeriv = null;
this.matScreenToCartesian = null;
this.matScreenToEllipsoid = null;
this.matEllipsoidToScreen = null;
this.coefs = null;
this.factoredLengths = null;
this.selectedPoints = null;
this.v1 = null;
this.v2 = null;
this.v3 = null;
this.pt1 = null;
this.pt2 = null;
this.s0 = null;
this.s1 = null;
this.s2 = null;
this.screens = null;
this.points = null;
Clazz.instantialize (this, arguments);
}, J.renderspecial, "EllipsoidsRenderer", J.render.ShapeRenderer);
Clazz.prepareFields (c$, function () {
this.bGlobals =  Clazz.newBooleanArray (7, false);
this.bOptions =  Clazz.newBooleanArray (7, false);
this.OPTS = ["dots", "arcs", "axes", "fill", "ball", "arrows", "wireframe"];
this.bsTemp =  new J.util.BS ();
this.mat =  new J.util.Matrix3f ();
this.mTemp =  new J.util.Matrix3f ();
this.mDeriv =  new J.util.Matrix4f ();
this.matScreenToCartesian =  new J.util.Matrix3f ();
this.matScreenToEllipsoid =  new J.util.Matrix3f ();
this.matEllipsoidToScreen =  new J.util.Matrix3f ();
this.coefs =  Clazz.newDoubleArray (10, 0);
this.factoredLengths =  Clazz.newFloatArray (3, 0);
this.selectedPoints =  new Array (3);
this.v1 =  new J.util.V3 ();
this.v2 =  new J.util.V3 ();
this.v3 =  new J.util.V3 ();
this.pt1 =  new J.util.P3 ();
this.pt2 =  new J.util.P3 ();
this.s0 =  new J.util.P3i ();
this.s1 =  new J.util.P3i ();
this.s2 =  new J.util.P3i ();
this.screens =  new Array (38);
this.points =  new Array (6);
{
for (var i = 0; i < this.points.length; i++) this.points[i] =  new J.util.P3 ();

for (var i = 0; i < this.screens.length; i++) this.screens[i] =  new J.util.P3i ();

}});
Clazz.overrideMethod (c$, "render", 
function () {
this.isSet = false;
this.ellipsoids = this.shape;
if (!this.ellipsoids.isActive ()) return false;
var needTranslucent = false;
if (!this.isSet) this.isSet = this.setGlobals ();
if (!this.ellipsoids.atomEllipsoids.isEmpty ()) needTranslucent = new Boolean (needTranslucent | this.renderEllipsoids (this.ellipsoids.atomEllipsoids, false)).valueOf ();
if (!this.ellipsoids.simpleEllipsoids.isEmpty ()) {
needTranslucent = new Boolean (needTranslucent | this.renderEllipsoids (this.ellipsoids.simpleEllipsoids, true)).valueOf ();
}this.coords = null;
return needTranslucent;
});
$_M(c$, "setGlobals", 
($fz = function () {
this.bGlobals[1] = this.viewer.getBooleanProperty ("ellipsoidArcs");
this.bGlobals[5] = this.viewer.getBooleanProperty ("ellipsoidArrows");
this.bGlobals[2] = this.viewer.getBooleanProperty ("ellipsoidAxes");
this.bGlobals[4] = this.viewer.getBooleanProperty ("ellipsoidBall");
this.bGlobals[0] = this.viewer.getBooleanProperty ("ellipsoidDots");
this.bGlobals[3] = this.viewer.getBooleanProperty ("ellipsoidFill");
this.bGlobals[6] = !this.isExport && !this.viewer.checkMotionRendering (1113198596);
this.diameter0 = Math.round ((this.viewer.getParameter ("ellipsoidAxisDiameter")).floatValue () * 1000);
var m4 = this.viewer.getMatrixtransform ();
this.mat.setRow (0, m4.m00, m4.m01, m4.m02);
this.mat.setRow (1, m4.m10, m4.m11, m4.m12);
this.mat.setRow (2, m4.m20, m4.m21, m4.m22);
this.matScreenToCartesian.invertM (this.mat);
this.setLogic ();
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "setOptions", 
($fz = function (options) {
for (var i = 0; i < 7; i++) this.bOptions[i] = this.bGlobals[i];

if (options != null) {
options = ";" + options + ";";
for (var i = 0; i < 7; i++) {
if (J.util.Parser.isOneOf (this.OPTS[i], options)) this.bOptions[i] = true;
 else if (J.util.Parser.isOneOf ("no" + this.OPTS[i], options)) this.bOptions[i] = false;
}
}this.setLogic ();
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "setLogic", 
($fz = function () {
this.bOptions[0] = new Boolean (this.bOptions[0] & !this.bOptions[6]).valueOf ();
this.bOptions[4] = new Boolean (this.bOptions[4] & !this.bOptions[6]).valueOf ();
this.bOptions[3] = new Boolean (this.bOptions[3] & !this.bOptions[6]).valueOf ();
this.fillArc = this.bOptions[3] && !this.bOptions[4];
if (this.bOptions[4]) this.bOptions[0] = false;
if (!this.bOptions[0] && !this.bOptions[1] && !this.bOptions[4]) this.bOptions[2] = true;
if (this.bOptions[0]) {
this.bOptions[1] = false;
this.bOptions[3] = false;
this.dotScale = this.viewer.getInt (553648144);
}if (this.bOptions[0]) {
this.dotCount = (this.viewer.getParameter ("ellipsoidDotCount")).intValue ();
if (this.coords == null || this.coords.length != this.dotCount * 3) this.coords =  Clazz.newIntArray (this.dotCount * 3, 0);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "renderEllipsoids", 
($fz = function (ht, isSimple) {
var needTranslucent = false;
var atom = null;
for (var ellipsoid, $ellipsoid = ht.values ().iterator (); $ellipsoid.hasNext () && ((ellipsoid = $ellipsoid.next ()) || true);) {
if (!ellipsoid.visible) continue;
if (isSimple) {
this.colix = ellipsoid.colix;
} else {
atom = this.modelSet.atoms[ellipsoid.tensor.atomIndex1];
if (atom.screenZ <= 1 || !atom.isVisible (this.myVisibilityFlag)) continue;
this.colix = J.util.C.getColixInherited (ellipsoid.colix, atom.getColix ());
}if (!this.g3d.setColix (this.colix)) {
needTranslucent = true;
continue;
}this.viewer.transformPtScr (ellipsoid.center, this.s0);
this.renderOne (ellipsoid);
}
return needTranslucent;
}, $fz.isPrivate = true, $fz), "java.util.Map,~B");
$_M(c$, "renderOne", 
($fz = function (e) {
this.center = e.center;
var maxPt = 2;
var maxLen = 0;
for (var i = 3; --i >= 0; ) {
var f = this.factoredLengths[i] = Math.max (e.getLength (i), 0.02);
if (f > maxLen) {
maxLen = f;
maxPt = i;
}}
this.axes = e.tensor.eigenVectors;
this.setMatrices ();
this.setAxes (maxPt);
if (this.g3d.isClippedXY (this.dx + this.dx, this.s0.x, this.s0.y)) return;
this.eigenSignMask = e.tensor.eigenSignMask;
this.setOptions (e.options);
this.diameter = Clazz.floatToInt (this.viewer.scaleToScreen (this.s0.z, this.bOptions[6] ? 1 : this.diameter0));
if (e.tensor.isIsotropic) {
this.renderBall ();
return;
}if (this.bOptions[4]) {
this.renderBall ();
if (this.bOptions[1] || this.bOptions[2]) {
this.g3d.setColix (this.viewer.getColixBackgroundContrast ());
if (this.bOptions[2]) this.renderAxes ();
if (this.bOptions[1]) this.renderArcs ();
this.g3d.setColix (this.colix);
}} else {
if (this.bOptions[2]) this.renderAxes ();
if (this.bOptions[1]) this.renderArcs ();
}if (this.bOptions[0]) this.renderDots ();
if (this.bOptions[5]) this.renderArrows ();
}, $fz.isPrivate = true, $fz), "J.shapespecial.Ellipsoid");
$_M(c$, "setMatrices", 
($fz = function () {
for (var i = 0; i < 3; i++) {
this.v1.setT (this.axes[i]);
this.v1.scale (this.factoredLengths[i]);
this.mat.setColumnV (i, this.v1);
}
this.mat.invertM (this.mat);
this.matScreenToEllipsoid.mul2 (this.mat, this.matScreenToCartesian);
this.matEllipsoidToScreen.invertM (this.matScreenToEllipsoid);
this.perspectiveFactor = this.viewer.scaleToPerspective (this.s0.z, 1.0);
this.matScreenToEllipsoid.mulf (1 / this.perspectiveFactor);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setAxes", 
($fz = function (maxPt) {
for (var i = 0; i < 6; i++) {
var iAxis = J.renderspecial.EllipsoidsRenderer.axisPoints[i];
var i012 = Math.abs (iAxis) - 1;
this.points[i].scaleAdd2 (this.factoredLengths[i012] * (iAxis < 0 ? -1 : 1), this.axes[i012], this.center);
this.pt1.setT (J.renderspecial.EllipsoidsRenderer.unitAxisVectors[i]);
this.matEllipsoidToScreen.transform (this.pt1);
this.screens[i].set (Math.round (this.s0.x + this.pt1.x * this.perspectiveFactor), Math.round (this.s0.y + this.pt1.y * this.perspectiveFactor), Math.round (this.pt1.z + this.s0.z));
this.screens[i + 32].set (Math.round (this.s0.x + this.pt1.x * this.perspectiveFactor * 1.05), Math.round (this.s0.y + this.pt1.y * this.perspectiveFactor * 1.05), Math.round (this.pt1.z * 1.05 + this.s0.z));
}
this.dx = 2 + Clazz.floatToInt (this.viewer.scaleToScreen (this.s0.z, Math.round ((Float.isNaN (this.factoredLengths[maxPt]) ? 1.0 : this.factoredLengths[maxPt]) * 1000)));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "renderBall", 
($fz = function () {
this.setSelectedOctant ();
J.shapespecial.Ellipsoid.getEquationForQuadricWithCenter (this.s0.x, this.s0.y, this.s0.z, this.matScreenToEllipsoid, this.v1, this.mTemp, this.coefs, this.mDeriv);
this.g3d.fillEllipsoid (this.center, this.points, this.s0.x, this.s0.y, this.s0.z, this.dx + this.dx, this.matScreenToEllipsoid, this.coefs, this.mDeriv, this.selectedOctant, this.selectedOctant >= 0 ? this.selectedPoints : null);
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderArrows", 
($fz = function () {
for (var i = 0; i < 6; i += 2) {
var pt = (i == 0 ? 1 : i);
this.fillConeScreen (this.screens[i], this.screens[i + 1], (this.eigenSignMask & pt) != 0);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "fillConeScreen", 
($fz = function (p1, p2, isPositive) {
if (this.diameter == 0) return;
var diam = (this.diameter == 0 ? 1 : this.diameter) * 8;
this.v1.set (p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
this.v1.normalize ();
this.v1.scale (diam);
this.s1.setT (p1);
this.s2.setT (p1);
if (isPositive) {
this.s2.x -= Clazz.floatToInt (this.v1.x);
this.s2.y -= Clazz.floatToInt (this.v1.y);
this.s2.z -= Clazz.floatToInt (this.v1.z);
} else {
this.s1.x -= Clazz.floatToInt (this.v1.x);
this.s1.y -= Clazz.floatToInt (this.v1.y);
this.s1.z -= Clazz.floatToInt (this.v1.z);
}this.g3d.fillConeScreen (2, Clazz.floatToInt (diam), this.s1, this.s2, false);
this.s1.setT (p2);
this.s2.setT (p2);
if (isPositive) {
this.s2.x += Clazz.floatToInt (this.v1.x);
this.s2.y += Clazz.floatToInt (this.v1.y);
this.s2.z += Clazz.floatToInt (this.v1.z);
} else {
this.s1.x += Clazz.floatToInt (this.v1.x);
this.s1.y += Clazz.floatToInt (this.v1.y);
this.s1.z += Clazz.floatToInt (this.v1.z);
}this.g3d.fillConeScreen (2, Clazz.floatToInt (diam), this.s1, this.s2, false);
}, $fz.isPrivate = true, $fz), "J.util.P3i,J.util.P3i,~B");
$_M(c$, "renderAxes", 
($fz = function () {
if (this.bOptions[4] && this.bOptions[3]) {
this.g3d.fillCylinder (2, this.diameter, this.s0, this.selectedPoints[0]);
this.g3d.fillCylinder (2, this.diameter, this.s0, this.selectedPoints[1]);
this.g3d.fillCylinder (2, this.diameter, this.s0, this.selectedPoints[2]);
return;
}if (this.bOptions[4]) {
this.g3d.fillCylinder (2, this.diameter, this.screens[32], this.screens[33]);
this.g3d.fillCylinder (2, this.diameter, this.screens[34], this.screens[35]);
this.g3d.fillCylinder (2, this.diameter, this.screens[36], this.screens[37]);
} else {
this.g3d.fillCylinder (2, this.diameter, this.screens[0], this.screens[1]);
this.g3d.fillCylinder (2, this.diameter, this.screens[2], this.screens[3]);
this.g3d.fillCylinder (2, this.diameter, this.screens[4], this.screens[5]);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "renderDots", 
($fz = function () {
for (var i = 0; i < this.coords.length; ) {
var fx = Math.random ();
var fy = Math.random ();
fx *= (Math.random () > 0.5 ? -1 : 1);
fy *= (Math.random () > 0.5 ? -1 : 1);
var fz = Math.sqrt (1 - fx * fx - fy * fy);
if (Float.isNaN (fz)) continue;
fz = (Math.random () > 0.5 ? -1 : 1) * fz;
this.pt1.scaleAdd2 (fx * this.factoredLengths[0], this.axes[0], this.center);
this.pt1.scaleAdd2 (fy * this.factoredLengths[1], this.axes[1], this.pt1);
this.pt1.scaleAdd2 (fz * this.factoredLengths[2], this.axes[2], this.pt1);
this.viewer.transformPtScr (this.pt1, this.s1);
this.coords[i++] = this.s1.x;
this.coords[i++] = this.s1.y;
this.coords[i++] = this.s1.z;
}
this.g3d.drawPoints (this.dotCount, this.coords, this.dotScale);
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderArcs", 
($fz = function () {
if (this.g3d.drawEllipse (this.center, this.points[0], this.points[2], this.fillArc, this.bOptions[6])) {
this.g3d.drawEllipse (this.center, this.points[2], this.points[5], this.fillArc, this.bOptions[6]);
this.g3d.drawEllipse (this.center, this.points[5], this.points[0], this.fillArc, this.bOptions[6]);
return;
}for (var i = 1; i < 8; i += 2) {
var pt = i * 3;
this.renderArc (J.renderspecial.EllipsoidsRenderer.octants[pt], J.renderspecial.EllipsoidsRenderer.octants[pt + 1]);
this.renderArc (J.renderspecial.EllipsoidsRenderer.octants[pt + 1], J.renderspecial.EllipsoidsRenderer.octants[pt + 2]);
this.renderArc (J.renderspecial.EllipsoidsRenderer.octants[pt + 2], J.renderspecial.EllipsoidsRenderer.octants[pt]);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderArc", 
($fz = function (ptA, ptB) {
this.v1.setT (this.points[ptA]);
this.v1.sub (this.center);
this.v2.setT (this.points[ptB]);
this.v2.sub (this.center);
var d1 = this.v1.length ();
var d2 = this.v2.length ();
this.v1.normalize ();
this.v2.normalize ();
this.v3.cross (this.v1, this.v2);
this.pt1.setT (this.points[ptA]);
this.s1.setT (this.screens[ptA]);
var normix = J.util.Normix.get2SidedNormix (this.v3, this.bsTemp);
if (!this.fillArc && !this.bOptions[6]) this.screens[6].setT (this.s1);
for (var i = 0, pt = 0; i < 18; i++, pt += 2) {
this.pt2.scaleAdd2 (J.renderspecial.EllipsoidsRenderer.cossin[pt] * d1, this.v1, this.center);
this.pt2.scaleAdd2 (J.renderspecial.EllipsoidsRenderer.cossin[pt + 1] * d2, this.v2, this.pt2);
this.viewer.transformPtScr (this.pt2, this.s2);
if (this.fillArc) this.g3d.fillTriangle3CN (this.s0, this.colix, normix, this.s1, this.colix, normix, this.s2, this.colix, normix);
 else if (this.bOptions[6]) this.g3d.fillCylinder (2, this.diameter, this.s1, this.s2);
 else this.screens[i + 7].setT (this.s2);
this.pt1.setT (this.pt2);
this.s1.setT (this.s2);
}
if (!this.fillArc && !this.bOptions[6]) for (var i = 0; i < 18; i++) {
this.g3d.fillHermite (5, this.diameter, this.diameter, this.diameter, this.screens[i == 0 ? i + 6 : i + 5], this.screens[i + 6], this.screens[i + 7], this.screens[i == 17 ? i + 7 : i + 8]);
}
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setSelectedOctant", 
($fz = function () {
var zMin = 2147483647;
this.selectedOctant = -1;
this.iCutout = -1;
if (this.bOptions[3]) {
for (var i = 0; i < 8; i++) {
var ptA = J.renderspecial.EllipsoidsRenderer.octants[i * 3];
var ptB = J.renderspecial.EllipsoidsRenderer.octants[i * 3 + 1];
var ptC = J.renderspecial.EllipsoidsRenderer.octants[i * 3 + 2];
var z = this.screens[ptA].z + this.screens[ptB].z + this.screens[ptC].z;
if (z < zMin) {
zMin = z;
this.iCutout = i;
}}
this.s1.setT (this.selectedPoints[0] = this.screens[J.renderspecial.EllipsoidsRenderer.octants[this.iCutout * 3]]);
this.s1.add (this.selectedPoints[1] = this.screens[J.renderspecial.EllipsoidsRenderer.octants[this.iCutout * 3 + 1]]);
this.s1.add (this.selectedPoints[2] = this.screens[J.renderspecial.EllipsoidsRenderer.octants[this.iCutout * 3 + 2]]);
this.s1.scaleAdd (-3, this.s0, this.s1);
this.pt1.set (this.s1.x, this.s1.y, this.s1.z);
this.matScreenToEllipsoid.transform (this.pt1);
this.selectedOctant = J.util.GData.getScreenOctant (this.pt1);
}}, $fz.isPrivate = true, $fz));
Clazz.defineStatics (c$,
"OPT_DOTS", 0,
"OPT_ARCS", 1,
"OPT_AXES", 2,
"OPT_FILL", 3,
"OPT_BALL", 4,
"OPT_ARROWS", 5,
"OPT_WIREFRAME", 6,
"OPT_COUNT", 7,
"toRadians", 0.017453292,
"cossin",  Clazz.newFloatArray (36, 0));
{
for (var i = 5, pt = 0; i <= 90; i += 5) {
J.renderspecial.EllipsoidsRenderer.cossin[pt++] = Math.cos (i * 0.017453292);
J.renderspecial.EllipsoidsRenderer.cossin[pt++] = Math.sin (i * 0.017453292);
}
}c$.unitAxisVectors = c$.prototype.unitAxisVectors = [J.viewer.JC.axisNX, J.viewer.JC.axisX, J.viewer.JC.axisNY, J.viewer.JC.axisY, J.viewer.JC.axisNZ, J.viewer.JC.axisZ];
Clazz.defineStatics (c$,
"axisPoints", [-1, 1, -2, 2, -3, 3],
"octants", [5, 0, 3, 5, 2, 0, 4, 0, 2, 4, 3, 0, 5, 2, 1, 5, 1, 3, 4, 3, 1, 4, 1, 2]);
});
