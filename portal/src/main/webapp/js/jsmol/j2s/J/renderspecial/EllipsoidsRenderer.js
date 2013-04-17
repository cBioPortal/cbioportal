Clazz.declarePackage ("J.renderspecial");
Clazz.load (["J.render.ShapeRenderer", "J.util.BS", "$.Matrix3f", "$.Matrix4f", "$.P3", "$.P3i", "$.V3", "J.viewer.JC"], "J.renderspecial.EllipsoidsRenderer", ["java.lang.Float", "J.shape.Shape", "J.util.Normix", "$.Quadric"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ellipsoids = null;
this.drawDots = false;
this.drawArcs = false;
this.drawAxes = false;
this.drawFill = false;
this.drawBall = false;
this.wireframeOnly = false;
this.dotCount = 0;
this.coords = null;
this.axes = null;
this.factoredLengths = null;
this.diameter = 0;
this.diameter0 = 0;
this.selectedOctant = -1;
this.selectedPoints = null;
this.iCutout = -1;
this.mat = null;
this.mTemp = null;
this.mDeriv = null;
this.matScreenToCartesian = null;
this.matScreenToEllipsoid = null;
this.matEllipsoidToScreen = null;
this.coef = null;
this.v1 = null;
this.v2 = null;
this.v3 = null;
this.pt1 = null;
this.pt2 = null;
this.s0 = null;
this.s1 = null;
this.s2 = null;
this.dotScale = 0;
this.isSet = false;
this.screens = null;
this.points = null;
this.dx = 0;
this.perspectiveFactor = 0;
this.center = null;
this.fillArc = false;
this.bsTemp = null;
Clazz.instantialize (this, arguments);
}, J.renderspecial, "EllipsoidsRenderer", J.render.ShapeRenderer);
Clazz.prepareFields (c$, function () {
this.factoredLengths =  Clazz.newFloatArray (3, 0);
this.selectedPoints =  new Array (3);
this.mat =  new J.util.Matrix3f ();
this.mTemp =  new J.util.Matrix3f ();
this.mDeriv =  new J.util.Matrix4f ();
this.matScreenToCartesian =  new J.util.Matrix3f ();
this.matScreenToEllipsoid =  new J.util.Matrix3f ();
this.matEllipsoidToScreen =  new J.util.Matrix3f ();
this.coef =  Clazz.newDoubleArray (10, 0);
this.v1 =  new J.util.V3 ();
this.v2 =  new J.util.V3 ();
this.v3 =  new J.util.V3 ();
this.pt1 =  new J.util.P3 ();
this.pt2 =  new J.util.P3 ();
this.s0 =  new J.util.P3i ();
this.s1 =  new J.util.P3i ();
this.s2 =  new J.util.P3i ();
this.screens =  new Array (32);
this.points =  new Array (6);
{
for (var i = 0; i < this.points.length; i++) this.points[i] =  new J.util.P3 ();

for (var i = 0; i < this.screens.length; i++) this.screens[i] =  new J.util.P3i ();

}this.bsTemp =  new J.util.BS ();
});
Clazz.overrideMethod (c$, "render", 
function () {
this.isSet = false;
this.ellipsoids = this.shape;
if (this.ellipsoids.madset == null && !this.ellipsoids.haveEllipsoids) return false;
var needTranslucent = false;
var atoms = this.modelSet.atoms;
for (var i = this.modelSet.getAtomCount (); --i >= 0; ) {
var atom = atoms[i];
if (!atom.isVisible (this.myVisibilityFlag)) continue;
if (atom.screenZ <= 1) continue;
var ellipsoid2 = atom.getEllipsoid ();
if (ellipsoid2 == null) continue;
for (var j = 0; j < ellipsoid2.length; j++) {
if (ellipsoid2[j] == null || this.ellipsoids.madset[j] == null || this.ellipsoids.madset[j][i] == 0) continue;
this.colix = J.shape.Shape.getColix (this.ellipsoids.colixset[j], i, atom);
if (this.g3d.setColix (this.colix)) this.render1 (atom, ellipsoid2[j]);
 else needTranslucent = true;
}
}
if (this.ellipsoids.haveEllipsoids) {
var e = this.ellipsoids.htEllipsoids.values ().iterator ();
while (e.hasNext ()) {
var ellipsoid = e.next ();
if (ellipsoid.visible && ellipsoid.isValid) {
if (this.g3d.setColix (this.colix = ellipsoid.colix)) this.renderEllipsoid (ellipsoid);
 else needTranslucent = true;
}}
}this.coords = null;
return needTranslucent;
});
$_M(c$, "renderEllipsoid", 
($fz = function (ellipsoid) {
if (!this.isSet) this.isSet = this.setGlobals ();
this.axes = ellipsoid.axes;
for (var i = 0; i < 3; i++) this.factoredLengths[i] = ellipsoid.lengths[i];

this.viewer.transformPtScr (ellipsoid.center, this.s0);
this.setMatrices ();
this.center = ellipsoid.center;
this.setAxes ();
this.renderOne (this.s0.z, true);
}, $fz.isPrivate = true, $fz), "J.shapespecial.Ellipsoids.Ellipsoid");
$_M(c$, "setGlobals", 
($fz = function () {
this.wireframeOnly = (this.viewer.getWireframeRotation () && this.viewer.getInMotion ());
this.drawAxes = this.viewer.getBooleanProperty ("ellipsoidAxes");
this.drawArcs = this.viewer.getBooleanProperty ("ellipsoidArcs");
this.drawBall = this.viewer.getBooleanProperty ("ellipsoidBall") && !this.wireframeOnly;
this.drawDots = this.viewer.getBooleanProperty ("ellipsoidDots") && !this.wireframeOnly;
this.drawFill = this.viewer.getBooleanProperty ("ellipsoidFill") && !this.wireframeOnly;
this.fillArc = this.drawFill && !this.drawBall;
this.diameter0 = Math.round ((this.viewer.getParameter ("ellipsoidAxisDiameter")).floatValue () * 1000);
if (this.drawBall) this.drawDots = false;
if (!this.drawDots && !this.drawArcs && !this.drawBall) this.drawAxes = true;
if (this.drawDots) {
this.drawArcs = false;
this.drawFill = false;
this.dotScale = this.viewer.getDotScale ();
}if (this.drawDots) {
this.dotCount = (this.viewer.getParameter ("ellipsoidDotCount")).intValue ();
if (this.coords == null || this.coords.length != this.dotCount * 3) this.coords =  Clazz.newIntArray (this.dotCount * 3, 0);
}var m4 = this.viewer.getMatrixtransform ();
this.mat.setRow (0, m4.m00, m4.m01, m4.m02);
this.mat.setRow (1, m4.m10, m4.m11, m4.m12);
this.mat.setRow (2, m4.m20, m4.m21, m4.m22);
this.matScreenToCartesian.invertM (this.mat);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "render1", 
($fz = function (atom, ellipsoid) {
if (!this.isSet) this.isSet = this.setGlobals ();
this.s0.set (atom.screenX, atom.screenY, atom.screenZ);
var isOK = true;
for (var i = 3; --i >= 0; ) {
this.factoredLengths[i] = ellipsoid.lengths[i] * ellipsoid.$scale;
if (Float.isNaN (this.factoredLengths[i])) isOK = false;
 else if (this.factoredLengths[i] < 0.02) this.factoredLengths[i] = 0.02;
}
this.axes = ellipsoid.vectors;
if (this.axes == null) {
this.axes = J.renderspecial.EllipsoidsRenderer.unitVectors;
}this.setMatrices ();
this.center = atom;
this.setAxes ();
if (this.g3d.isClippedXY (this.dx + this.dx, atom.screenX, atom.screenY)) return;
this.renderOne (atom.screenZ, isOK);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,J.util.Quadric");
$_M(c$, "renderOne", 
($fz = function (screenZ, isOK) {
this.diameter = this.viewer.scaleToScreen (screenZ, this.wireframeOnly ? 1 : this.diameter0);
if (!isOK || this.drawBall) {
this.renderBall ();
if (!isOK) return;
if (this.drawArcs || this.drawAxes) {
this.g3d.setColix (this.viewer.getColixBackgroundContrast ());
if (this.drawAxes) this.renderAxes ();
if (this.drawArcs) this.renderArcs ();
this.g3d.setColix (this.colix);
}} else {
if (this.drawAxes) this.renderAxes ();
if (this.drawArcs) this.renderArcs ();
}if (this.drawDots) this.renderDots ();
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "setMatrices", 
($fz = function () {
J.util.Quadric.setEllipsoidMatrix (this.axes, this.factoredLengths, this.v1, this.mat);
this.matScreenToEllipsoid.mul2 (this.mat, this.matScreenToCartesian);
this.matEllipsoidToScreen.invertM (this.matScreenToEllipsoid);
this.perspectiveFactor = this.viewer.scaleToPerspective (this.s0.z, 1.0);
this.matScreenToEllipsoid.mulf (1 / this.perspectiveFactor);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setAxes", 
($fz = function () {
for (var i = 0; i < 6; i++) {
var iAxis = J.renderspecial.EllipsoidsRenderer.axisPoints[i];
var i012 = Math.abs (iAxis) - 1;
this.points[i].scaleAdd2 (this.factoredLengths[i012] * (iAxis < 0 ? -1 : 1), this.axes[i012], this.center);
this.pt1.setT (J.renderspecial.EllipsoidsRenderer.unitAxisVectors[i]);
this.matEllipsoidToScreen.transform (this.pt1);
this.screens[i].set (Math.round (this.s0.x + this.pt1.x * this.perspectiveFactor), Math.round (this.s0.y + this.pt1.y * this.perspectiveFactor), Math.round (this.pt1.z + this.s0.z));
}
this.dx = 2 + this.viewer.scaleToScreen (this.s0.z, Math.round ((Float.isNaN (this.factoredLengths[2]) ? 1.0 : this.factoredLengths[2]) * 1000));
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderBall", 
($fz = function () {
this.setSelectedOctant ();
J.util.Quadric.getEquationForQuadricWithCenter (this.s0.x, this.s0.y, this.s0.z, this.matScreenToEllipsoid, this.v1, this.mTemp, this.coef, this.mDeriv);
this.g3d.fillEllipsoid (this.center, this.points, this.s0.x, this.s0.y, this.s0.z, this.dx + this.dx, this.matScreenToEllipsoid, this.coef, this.mDeriv, this.selectedOctant, this.selectedOctant >= 0 ? this.selectedPoints : null);
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderAxes", 
($fz = function () {
if (this.drawBall && this.drawFill) {
this.g3d.fillCylinder (2, this.diameter, this.s0, this.selectedPoints[0]);
this.g3d.fillCylinder (2, this.diameter, this.s0, this.selectedPoints[1]);
this.g3d.fillCylinder (2, this.diameter, this.s0, this.selectedPoints[2]);
return;
}this.g3d.fillCylinder (2, this.diameter, this.screens[0], this.screens[1]);
this.g3d.fillCylinder (2, this.diameter, this.screens[2], this.screens[3]);
this.g3d.fillCylinder (2, this.diameter, this.screens[4], this.screens[5]);
}, $fz.isPrivate = true, $fz));
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
if (this.g3d.drawEllipse (this.center, this.points[0], this.points[2], this.fillArc, this.wireframeOnly)) {
this.g3d.drawEllipse (this.center, this.points[2], this.points[5], this.fillArc, this.wireframeOnly);
this.g3d.drawEllipse (this.center, this.points[5], this.points[0], this.fillArc, this.wireframeOnly);
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
if (!this.fillArc && !this.wireframeOnly) this.screens[6].setT (this.s1);
for (var i = 0, pt = 0; i < 18; i++, pt += 2) {
this.pt2.scaleAdd2 (J.renderspecial.EllipsoidsRenderer.cossin[pt] * d1, this.v1, this.center);
this.pt2.scaleAdd2 (J.renderspecial.EllipsoidsRenderer.cossin[pt + 1] * d2, this.v2, this.pt2);
this.viewer.transformPtScr (this.pt2, this.s2);
if (this.fillArc) this.g3d.fillTriangle3CN (this.s0, this.colix, normix, this.s1, this.colix, normix, this.s2, this.colix, normix);
 else if (this.wireframeOnly) this.g3d.fillCylinder (2, this.diameter, this.s1, this.s2);
 else this.screens[i + 7].setT (this.s2);
this.pt1.setT (this.pt2);
this.s1.setT (this.s2);
}
if (!this.fillArc && !this.wireframeOnly) for (var i = 0; i < 18; i++) {
this.g3d.fillHermite (5, this.diameter, this.diameter, this.diameter, this.screens[i == 0 ? i + 6 : i + 5], this.screens[i + 6], this.screens[i + 7], this.screens[i == 17 ? i + 7 : i + 8]);
}
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setSelectedOctant", 
($fz = function () {
var zMin = 2147483647;
this.selectedOctant = -1;
this.iCutout = -1;
if (this.drawFill) {
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
this.selectedOctant = J.util.Quadric.getOctant (this.pt1);
}}, $fz.isPrivate = true, $fz));
Clazz.defineStatics (c$,
"toRadians", 0.017453292,
"cossin",  Clazz.newFloatArray (36, 0));
{
for (var i = 5, pt = 0; i <= 90; i += 5) {
J.renderspecial.EllipsoidsRenderer.cossin[pt++] = Math.cos (i * 0.017453292);
J.renderspecial.EllipsoidsRenderer.cossin[pt++] = Math.sin (i * 0.017453292);
}
}Clazz.defineStatics (c$,
"axisPoints", [-1, 1, -2, 2, -3, 3],
"octants", [5, 0, 3, 5, 2, 0, 4, 0, 2, 4, 3, 0, 5, 2, 1, 5, 1, 3, 4, 3, 1, 4, 1, 2]);
c$.unitVectors = c$.prototype.unitVectors = [J.viewer.JC.axisX, J.viewer.JC.axisY, J.viewer.JC.axisZ];
c$.unitAxisVectors = c$.prototype.unitAxisVectors = [J.viewer.JC.axisNX, J.viewer.JC.axisX, J.viewer.JC.axisNY, J.viewer.JC.axisY, J.viewer.JC.axisNZ, J.viewer.JC.axisZ];
});
