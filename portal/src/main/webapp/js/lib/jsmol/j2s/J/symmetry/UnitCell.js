Clazz.declarePackage ("J.symmetry");
Clazz.load (["J.util.SimpleUnitCell", "$.P3", "J.viewer.JC"], "J.symmetry.UnitCell", ["java.lang.Float", "J.util.BoxInfo", "$.Escape", "$.Matrix4f", "$.Tensor"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vertices = null;
this.cartesianOffset = null;
this.fractionalOffset = null;
this.allFractionalRelative = false;
this.unitCellMultiplier = null;
Clazz.instantialize (this, arguments);
}, J.symmetry, "UnitCell", J.util.SimpleUnitCell);
Clazz.prepareFields (c$, function () {
this.cartesianOffset =  new J.util.P3 ();
this.fractionalOffset =  new J.util.P3 ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.symmetry.UnitCell, []);
});
c$.newP = $_M(c$, "newP", 
function (points) {
var c =  new J.symmetry.UnitCell ();
var parameters = [-1, 0, 0, 0, 0, 0, points[1].x, points[1].y, points[1].z, points[2].x, points[2].y, points[2].z, points[3].x, points[3].y, points[3].z];
c.set (parameters);
c.allFractionalRelative = true;
c.calcUnitcellVertices ();
c.setCartesianOffset (points[0]);
return c;
}, "~A");
c$.newA = Clazz.overrideMethod (c$, "newA", 
function (notionalUnitcell) {
var c =  new J.symmetry.UnitCell ();
c.set (notionalUnitcell);
c.calcUnitcellVertices ();
return c;
}, "~A");
$_M(c$, "setOrientation", 
function (mat) {
if (mat == null) return;
var m =  new J.util.Matrix4f ();
m.setM3 (mat);
this.matrixFractionalToCartesian.mul2 (m, this.matrixFractionalToCartesian);
this.matrixCartesianToFractional.invertM (this.matrixFractionalToCartesian);
this.calcUnitcellVertices ();
}, "J.util.Matrix3f");
$_M(c$, "toUnitCell", 
function (pt, offset) {
if (this.matrixCartesianToFractional == null) return;
if (offset == null) {
this.matrixCartesianToFractional.transform (pt);
this.unitize (pt);
this.matrixFractionalToCartesian.transform (pt);
} else {
this.matrixCtoFAbsolute.transform (pt);
this.unitize (pt);
pt.add (offset);
this.matrixFtoCAbsolute.transform (pt);
}}, "J.util.P3,J.util.P3");
$_M(c$, "unitize", 
function (pt) {
switch (this.dimension) {
case 3:
pt.z = J.symmetry.UnitCell.toFractionalX (pt.z);
case 2:
pt.y = J.symmetry.UnitCell.toFractionalX (pt.y);
case 1:
pt.x = J.symmetry.UnitCell.toFractionalX (pt.x);
}
}, "J.util.P3");
$_M(c$, "setAllFractionalRelative", 
function (TF) {
this.allFractionalRelative = TF;
}, "~B");
$_M(c$, "setOffset", 
function (pt) {
if (pt == null) return;
if (pt.x >= 100 || pt.y >= 100) {
this.unitCellMultiplier = J.util.P3.newP (pt);
return;
}if (pt.x == 0 && pt.y == 0 && pt.z == 0) this.unitCellMultiplier = null;
this.fractionalOffset.setT (pt);
this.matrixCartesianToFractional.m03 = -pt.x;
this.matrixCartesianToFractional.m13 = -pt.y;
this.matrixCartesianToFractional.m23 = -pt.z;
this.cartesianOffset.setT (pt);
this.matrixFractionalToCartesian.m03 = 0;
this.matrixFractionalToCartesian.m13 = 0;
this.matrixFractionalToCartesian.m23 = 0;
this.matrixFractionalToCartesian.transform (this.cartesianOffset);
this.matrixFractionalToCartesian.m03 = this.cartesianOffset.x;
this.matrixFractionalToCartesian.m13 = this.cartesianOffset.y;
this.matrixFractionalToCartesian.m23 = this.cartesianOffset.z;
if (this.allFractionalRelative) {
this.matrixCtoFAbsolute.setM (this.matrixCartesianToFractional);
this.matrixFtoCAbsolute.setM (this.matrixFractionalToCartesian);
}}, "J.util.P3");
$_M(c$, "setCartesianOffset", 
function (origin) {
this.cartesianOffset.setT (origin);
this.matrixFractionalToCartesian.m03 = this.cartesianOffset.x;
this.matrixFractionalToCartesian.m13 = this.cartesianOffset.y;
this.matrixFractionalToCartesian.m23 = this.cartesianOffset.z;
this.fractionalOffset.setT (this.cartesianOffset);
this.matrixCartesianToFractional.m03 = 0;
this.matrixCartesianToFractional.m13 = 0;
this.matrixCartesianToFractional.m23 = 0;
this.matrixCartesianToFractional.transform (this.fractionalOffset);
this.matrixCartesianToFractional.m03 = -this.fractionalOffset.x;
this.matrixCartesianToFractional.m13 = -this.fractionalOffset.y;
this.matrixCartesianToFractional.m23 = -this.fractionalOffset.z;
if (this.allFractionalRelative) {
this.matrixCtoFAbsolute.setM (this.matrixCartesianToFractional);
this.matrixFtoCAbsolute.setM (this.matrixFractionalToCartesian);
}}, "J.util.Tuple3f");
$_M(c$, "setMinMaxLatticeParameters", 
function (minXYZ, maxXYZ) {
if (maxXYZ.x <= 555 && maxXYZ.y >= 555) {
var pt =  new J.util.P3 ();
J.util.SimpleUnitCell.ijkToPoint3f (maxXYZ.x, pt, 0);
minXYZ.x = Clazz.floatToInt (pt.x);
minXYZ.y = Clazz.floatToInt (pt.y);
minXYZ.z = Clazz.floatToInt (pt.z);
J.util.SimpleUnitCell.ijkToPoint3f (maxXYZ.y, pt, 1);
maxXYZ.x = Clazz.floatToInt (pt.x);
maxXYZ.y = Clazz.floatToInt (pt.y);
maxXYZ.z = Clazz.floatToInt (pt.z);
}switch (this.dimension) {
case 1:
minXYZ.y = 0;
maxXYZ.y = 1;
case 2:
minXYZ.z = 0;
maxXYZ.z = 1;
}
}, "J.util.P3i,J.util.P3i");
$_M(c$, "dumpInfo", 
function (isFull) {
return "a=" + this.a + ", b=" + this.b + ", c=" + this.c + ", alpha=" + this.alpha + ", beta=" + this.beta + ", gamma=" + this.gamma + (isFull ? "\nfractional to cartesian: " + this.matrixFractionalToCartesian + "\ncartesian to fractional: " + this.matrixCartesianToFractional : "");
}, "~B");
$_M(c$, "getVertices", 
function () {
return this.vertices;
});
$_M(c$, "getCartesianOffset", 
function () {
return this.cartesianOffset;
});
$_M(c$, "getFractionalOffset", 
function () {
return this.fractionalOffset;
});
$_M(c$, "getTensor", 
function (parBorU) {
if (parBorU == null) return null;
if (parBorU[0] == 0) {
var f = parBorU[7];
var eigenValues = [f, f, f];
return J.util.Tensor.getTensorFromEigenVectors (J.symmetry.UnitCell.unitVectors, eigenValues, "iso", "Uiso=" + f);
}var Bcart =  Clazz.newDoubleArray (6, 0);
var ortepType = Clazz.floatToInt (parBorU[6]);
if (ortepType == 12) {
Bcart[0] = parBorU[0] * 19.739208802178716;
Bcart[1] = parBorU[1] * 19.739208802178716;
Bcart[2] = parBorU[2] * 19.739208802178716;
Bcart[3] = parBorU[3] * 19.739208802178716 * 2;
Bcart[4] = parBorU[4] * 19.739208802178716 * 2;
Bcart[5] = parBorU[5] * 19.739208802178716 * 2;
parBorU[7] = (parBorU[0] + parBorU[1] + parBorU[3]) / 3;
} else {
var isFractional = (ortepType == 4 || ortepType == 5 || ortepType == 8 || ortepType == 9);
var cc = 2 - (ortepType % 2);
var dd = (ortepType == 8 || ortepType == 9 || ortepType == 10 ? 19.739208802178716 : ortepType == 4 || ortepType == 5 ? 0.25 : ortepType == 2 || ortepType == 3 ? Math.log (2) : 1);
var B11 = parBorU[0] * dd * (isFractional ? this.a_ * this.a_ : 1);
var B22 = parBorU[1] * dd * (isFractional ? this.b_ * this.b_ : 1);
var B33 = parBorU[2] * dd * (isFractional ? this.c_ * this.c_ : 1);
var B12 = parBorU[3] * dd * (isFractional ? this.a_ * this.b_ : 1) * cc;
var B13 = parBorU[4] * dd * (isFractional ? this.a_ * this.c_ : 1) * cc;
var B23 = parBorU[5] * dd * (isFractional ? this.b_ * this.c_ : 1) * cc;
parBorU[7] = Math.pow (B11 / 19.739208802178716 / this.a_ / this.a_ * B22 / 19.739208802178716 / this.b_ / this.b_ * B33 / 19.739208802178716 / this.c_ / this.c_, 0.3333);
Bcart[0] = this.a * this.a * B11 + this.b * this.b * this.cosGamma * this.cosGamma * B22 + this.c * this.c * this.cosBeta * this.cosBeta * B33 + this.a * this.b * this.cosGamma * B12 + this.b * this.c * this.cosGamma * this.cosBeta * B23 + this.a * this.c * this.cosBeta * B13;
Bcart[1] = this.b * this.b * this.sinGamma * this.sinGamma * B22 + this.c * this.c * this.cA_ * this.cA_ * B33 + this.b * this.c * this.cA_ * this.sinGamma * B23;
Bcart[2] = this.c * this.c * this.cB_ * this.cB_ * B33;
Bcart[3] = 2 * this.b * this.b * this.cosGamma * this.sinGamma * B22 + 2 * this.c * this.c * this.cA_ * this.cosBeta * B33 + this.a * this.b * this.sinGamma * B12 + this.b * this.c * (this.cA_ * this.cosGamma + this.sinGamma * this.cosBeta) * B23 + this.a * this.c * this.cA_ * B13;
Bcart[4] = 2 * this.c * this.c * this.cB_ * this.cosBeta * B33 + this.b * this.c * this.cosGamma * B23 + this.a * this.c * this.cB_ * B13;
Bcart[5] = 2 * this.c * this.c * this.cA_ * this.cB_ * B33 + this.b * this.c * this.cB_ * this.sinGamma * B23;
}return J.util.Tensor.getTensorFromThermalEquation (Bcart, J.util.Escape.eAF (parBorU));
}, "~A");
$_M(c$, "getCanonicalCopy", 
function (scale) {
var pts =  new Array (8);
for (var i = 0; i < 8; i++) {
pts[i] = J.util.P3.newP (J.util.BoxInfo.unitCubePoints[i]);
this.matrixFractionalToCartesian.transform (pts[i]);
}
return J.util.BoxInfo.getCanonicalCopy (pts, scale);
}, "~N");
c$.toFractionalX = $_M(c$, "toFractionalX", 
($fz = function (x) {
x = (x - Math.floor (x));
if (x > 0.9999 || x < 0.0001) x = 0;
return x;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "calcUnitcellVertices", 
($fz = function () {
if (this.matrixFractionalToCartesian == null) return;
this.matrixCtoFAbsolute = J.util.Matrix4f.newM (this.matrixCartesianToFractional);
this.matrixFtoCAbsolute = J.util.Matrix4f.newM (this.matrixFractionalToCartesian);
this.vertices =  new Array (8);
for (var i = 8; --i >= 0; ) {
this.vertices[i] =  new J.util.P3 ();
this.matrixFractionalToCartesian.transform2 (J.util.BoxInfo.unitCubePoints[i], this.vertices[i]);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "checkDistance", 
function (f1, f2, distance, dx, iRange, jRange, kRange, ptOffset) {
var p1 = J.util.P3.newP (f1);
this.toCartesian (p1, true);
for (var i = -iRange; i <= iRange; i++) for (var j = -jRange; j <= jRange; j++) for (var k = -kRange; k <= kRange; k++) {
ptOffset.set (f2.x + i, f2.y + j, f2.z + k);
this.toCartesian (ptOffset, true);
var d = p1.distance (ptOffset);
if (dx > 0 ? Math.abs (d - distance) <= dx : d <= distance && d > 0.1) {
ptOffset.set (i, j, k);
return true;
}}


return false;
}, "J.util.P3,J.util.P3,~N,~N,~N,~N,~N,J.util.P3");
$_M(c$, "getUnitCellMultiplier", 
function () {
return this.unitCellMultiplier;
});
$_M(c$, "getUnitCellVectors", 
function () {
var m = this.matrixFractionalToCartesian;
return [J.util.P3.newP (this.cartesianOffset), J.util.P3.new3 (m.m00, m.m10, m.m20), J.util.P3.new3 (m.m01, m.m11, m.m21), J.util.P3.new3 (m.m02, m.m12, m.m22)];
});
$_M(c$, "isSameAs", 
function (uc) {
if (uc.notionalUnitcell.length != this.notionalUnitcell.length) return false;
for (var i = this.notionalUnitcell.length; --i >= 0; ) if (this.notionalUnitcell[i] != uc.notionalUnitcell[i] && !(Float.isNaN (this.notionalUnitcell[i]) && Float.isNaN (uc.notionalUnitcell[i]))) return false;

if (this.fractionalOffset.distanceSquared (uc.fractionalOffset) != 0) return false;
return true;
}, "J.symmetry.UnitCell");
Clazz.defineStatics (c$,
"twoP2", 19.739208802178716);
c$.unitVectors = c$.prototype.unitVectors = [J.viewer.JC.axisX, J.viewer.JC.axisY, J.viewer.JC.axisZ];
});
