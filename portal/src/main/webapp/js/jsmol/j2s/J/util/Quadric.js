Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.Quadric", ["J.util.Eigen"], function () {
c$ = Clazz.decorateAsClass (function () {
this.lengths = null;
this.vectors = null;
this.isThermalEllipsoid = true;
this.$scale = 1;
Clazz.instantialize (this, arguments);
}, J.util, "Quadric");
$_M(c$, "scale", 
function (f) {
for (var i = 0; i < 3; i++) this.lengths[i] *= f;

}, "~N");
Clazz.overrideMethod (c$, "toString", 
function () {
return (this.vectors == null ? "" + this.lengths[0] : this.vectors[0] + "\t" + this.lengths[0] + "\n" + this.vectors[1] + "\t" + this.lengths[1] + "\n" + this.vectors[2] + "\t" + this.lengths[2] + "\n");
});
$_M(c$, "fromVectors", 
function (vectors, lengths, isThermal) {
this.vectors = vectors;
this.lengths = lengths;
this.isThermalEllipsoid = isThermal;
return this;
}, "~A,~A,~B");
$_M(c$, "fromBCart", 
function (bcart) {
this.isThermalEllipsoid = true;
this.lengths =  Clazz.newFloatArray (3, 0);
this.vectors =  new Array (3);
J.util.Quadric.getAxesForEllipsoid (bcart, this.vectors, this.lengths);
for (var i = 0; i < 3; i++) this.lengths[i] *= J.util.Quadric.ONE_OVER_ROOT2_PI;

return this;
}, "~A");
$_M(c$, "rotate", 
function (mat) {
if (this.vectors != null) for (var i = 0; i < 3; i++) mat.transformV (this.vectors[i]);

}, "J.util.Matrix4f");
$_M(c$, "setSize", 
function (size) {
this.$scale = (this.isThermalEllipsoid ? J.util.Quadric.getRadius (size) : size < 1 ? 0 : size / 100.0);
}, "~N");
c$.getAxesForEllipsoid = $_M(c$, "getAxesForEllipsoid", 
function (coef, unitVectors, lengths) {
var mat =  Clazz.newDoubleArray (3, 3, 0);
mat[0][0] = coef[0];
mat[1][1] = coef[1];
mat[2][2] = coef[2];
mat[0][1] = mat[1][0] = coef[3] / 2;
mat[0][2] = mat[2][0] = coef[4] / 2;
mat[1][2] = mat[2][1] = coef[5] / 2;
J.util.Eigen.getUnitVectors (mat, unitVectors, lengths);
}, "~A,~A,~A");
c$.setEllipsoidMatrix = $_M(c$, "setEllipsoidMatrix", 
function (unitAxes, lengths, vTemp, mat) {
for (var i = 0; i < 3; i++) {
vTemp.setT (unitAxes[i]);
vTemp.scale (lengths[i]);
mat.setColumnV (i, vTemp);
}
mat.invertM (mat);
return mat;
}, "~A,~A,J.util.V3,J.util.Matrix3f");
c$.getEquationForQuadricWithCenter = $_M(c$, "getEquationForQuadricWithCenter", 
function (x, y, z, mToElliptical, vTemp, mTemp, coef, mDeriv) {
vTemp.set (x, y, z);
mToElliptical.transform (vTemp);
var f = 1 - vTemp.dot (vTemp);
mTemp.transposeM (mToElliptical);
mTemp.transform (vTemp);
mTemp.mul (mToElliptical);
coef[0] = mTemp.m00 / f;
coef[1] = mTemp.m11 / f;
coef[2] = mTemp.m22 / f;
coef[3] = mTemp.m01 * 2 / f;
coef[4] = mTemp.m02 * 2 / f;
coef[5] = mTemp.m12 * 2 / f;
coef[6] = -2 * vTemp.x / f;
coef[7] = -2 * vTemp.y / f;
coef[8] = -2 * vTemp.z / f;
coef[9] = -1;
if (mDeriv == null) return;
mDeriv.setIdentity ();
mDeriv.m00 = (2 * coef[0]);
mDeriv.m11 = (2 * coef[1]);
mDeriv.m22 = (2 * coef[2]);
mDeriv.m01 = mDeriv.m10 = coef[3];
mDeriv.m02 = mDeriv.m20 = coef[4];
mDeriv.m12 = mDeriv.m21 = coef[5];
mDeriv.m03 = coef[6];
mDeriv.m13 = coef[7];
mDeriv.m23 = coef[8];
}, "~N,~N,~N,J.util.Matrix3f,J.util.V3,J.util.Matrix3f,~A,J.util.Matrix4f");
c$.getQuardricZ = $_M(c$, "getQuardricZ", 
function (x, y, coef, zroot) {
var b_2a = (coef[4] * x + coef[5] * y + coef[8]) / coef[2] / 2;
var c_a = (coef[0] * x * x + coef[1] * y * y + coef[3] * x * y + coef[6] * x + coef[7] * y - 1) / coef[2];
var f = b_2a * b_2a - c_a;
if (f < 0) return false;
f = Math.sqrt (f);
zroot[0] = (-b_2a - f);
zroot[1] = (-b_2a + f);
return true;
}, "~N,~N,~A,~A");
c$.getOctant = $_M(c$, "getOctant", 
function (pt) {
var i = 0;
if (pt.x < 0) i += 1;
if (pt.y < 0) i += 2;
if (pt.z < 0) i += 4;
return i;
}, "J.util.P3");
c$.getRadius = $_M(c$, "getRadius", 
function (prob) {
return J.util.Quadric.crtval[prob < 1 ? 0 : prob > 99 ? 98 : prob - 1];
}, "~N");
c$.ONE_OVER_ROOT2_PI = c$.prototype.ONE_OVER_ROOT2_PI = (Math.sqrt (0.5) / 3.141592653589793);
Clazz.defineStatics (c$,
"crtval", [0.3389, 0.4299, 0.4951, 0.5479, 0.5932, 0.6334, 0.6699, 0.7035, 0.7349, 0.7644, 0.7924, 0.8192, 0.8447, 0.8694, 0.8932, 0.9162, 0.9386, 0.9605, 0.9818, 1.0026, 1.0230, 1.0430, 1.0627, 1.0821, 1.1012, 1.1200, 1.1386, 1.1570, 1.1751, 1.1932, 1.2110, 1.2288, 1.2464, 1.2638, 1.2812, 1.2985, 1.3158, 1.3330, 1.3501, 1.3672, 1.3842, 1.4013, 1.4183, 1.4354, 1.4524, 1.4695, 1.4866, 1.5037, 1.5209, 1.5382, 1.5555, 1.5729, 1.5904, 1.6080, 1.6257, 1.6436, 1.6616, 1.6797, 1.6980, 1.7164, 1.7351, 1.7540, 1.7730, 1.7924, 1.8119, 1.8318, 1.8519, 1.8724, 1.8932, 1.9144, 1.9360, 1.9580, 1.9804, 2.0034, 2.0269, 2.0510, 2.0757, 2.1012, 2.1274, 2.1544, 2.1824, 2.2114, 2.2416, 2.2730, 2.3059, 2.3404, 2.3767, 2.4153, 2.4563, 2.5003, 2.5478, 2.5997, 2.6571, 2.7216, 2.7955, 2.8829, 2.9912, 3.1365, 3.3682]);
});
