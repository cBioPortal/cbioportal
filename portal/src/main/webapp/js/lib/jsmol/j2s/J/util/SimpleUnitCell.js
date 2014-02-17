Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.SimpleUnitCell", ["java.lang.Float", "J.util.ArrayUtil", "$.Matrix4f", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.notionalUnitcell = null;
this.matrixCartesianToFractional = null;
this.matrixFractionalToCartesian = null;
this.na = 0;
this.nb = 0;
this.nc = 0;
this.a = 0;
this.b = 0;
this.c = 0;
this.alpha = 0;
this.beta = 0;
this.gamma = 0;
this.cosAlpha = 0;
this.sinAlpha = 0;
this.cosBeta = 0;
this.sinBeta = 0;
this.cosGamma = 0;
this.sinGamma = 0;
this.volume = 0;
this.cA_ = 0;
this.cB_ = 0;
this.a_ = 0;
this.b_ = 0;
this.c_ = 0;
this.dimension = 0;
this.matrixCtoFAbsolute = null;
this.matrixFtoCAbsolute = null;
Clazz.instantialize (this, arguments);
}, J.util, "SimpleUnitCell");
$_M(c$, "isSupercell", 
function () {
return (this.na > 1 || this.nb > 1 || this.nc > 1);
});
c$.isValid = $_M(c$, "isValid", 
function (parameters) {
return (parameters != null && (parameters[0] > 0 || parameters.length > 14 && !Float.isNaN (parameters[14])));
}, "~A");
Clazz.makeConstructor (c$, 
function () {
});
c$.newA = $_M(c$, "newA", 
function (parameters) {
var c =  new J.util.SimpleUnitCell ();
c.set (parameters);
return c;
}, "~A");
$_M(c$, "set", 
function (parameters) {
if (!J.util.SimpleUnitCell.isValid (parameters)) return;
this.notionalUnitcell = J.util.ArrayUtil.arrayCopyF (parameters, parameters.length);
this.a = parameters[0];
this.b = parameters[1];
this.c = parameters[2];
this.alpha = parameters[3];
this.beta = parameters[4];
this.gamma = parameters[5];
this.na = Math.max (1, parameters.length >= 25 && !Float.isNaN (parameters[22]) ? Clazz.floatToInt (parameters[22]) : 1);
this.nb = Math.max (1, parameters.length >= 25 && !Float.isNaN (parameters[23]) ? Clazz.floatToInt (parameters[23]) : 1);
this.nc = Math.max (1, parameters.length >= 25 && !Float.isNaN (parameters[24]) ? Clazz.floatToInt (parameters[24]) : 1);
if (this.a <= 0) {
var va = J.util.V3.new3 (parameters[6], parameters[7], parameters[8]);
var vb = J.util.V3.new3 (parameters[9], parameters[10], parameters[11]);
var vc = J.util.V3.new3 (parameters[12], parameters[13], parameters[14]);
this.a = va.length ();
this.b = vb.length ();
this.c = vc.length ();
if (this.a == 0) return;
if (this.b == 0) this.b = this.c = -1;
 else if (this.c == 0) this.c = -1;
this.alpha = (this.b < 0 || this.c < 0 ? 90 : vb.angle (vc) / 0.017453292);
this.beta = (this.c < 0 ? 90 : va.angle (vc) / 0.017453292);
this.gamma = (this.b < 0 ? 90 : va.angle (vb) / 0.017453292);
if (this.c < 0) {
var n = J.util.ArrayUtil.arrayCopyF (parameters, -1);
if (this.b < 0) {
vb.set (0, 0, 1);
vb.cross (vb, va);
if (vb.length () < 0.001) vb.set (0, 1, 0);
vb.normalize ();
n[9] = vb.x;
n[10] = vb.y;
n[11] = vb.z;
}if (this.c < 0) {
vc.cross (va, vb);
vc.normalize ();
n[12] = vc.x;
n[13] = vc.y;
n[14] = vc.z;
}parameters = n;
}}this.a *= this.na;
if (this.b <= 0) {
this.b = this.c = 1;
this.dimension = 1;
} else if (this.c <= 0) {
this.c = 1;
this.b *= this.nb;
this.dimension = 2;
} else {
this.b *= this.nb;
this.c *= this.nc;
this.dimension = 3;
}this.cosAlpha = Math.cos (0.017453292 * this.alpha);
this.sinAlpha = Math.sin (0.017453292 * this.alpha);
this.cosBeta = Math.cos (0.017453292 * this.beta);
this.sinBeta = Math.sin (0.017453292 * this.beta);
this.cosGamma = Math.cos (0.017453292 * this.gamma);
this.sinGamma = Math.sin (0.017453292 * this.gamma);
var unitVolume = Math.sqrt (this.sinAlpha * this.sinAlpha + this.sinBeta * this.sinBeta + this.sinGamma * this.sinGamma + 2.0 * this.cosAlpha * this.cosBeta * this.cosGamma - 2);
this.volume = this.a * this.b * this.c * unitVolume;
this.cA_ = (this.cosAlpha - this.cosBeta * this.cosGamma) / this.sinGamma;
this.cB_ = unitVolume / this.sinGamma;
this.a_ = this.b * this.c * this.sinAlpha / this.volume;
this.b_ = this.a * this.c * this.sinBeta / this.volume;
this.c_ = this.a * this.b * this.sinGamma / this.volume;
if (parameters.length > 21 && !Float.isNaN (parameters[21])) {
var scaleMatrix =  Clazz.newFloatArray (16, 0);
for (var i = 0; i < 16; i++) {
var f;
switch (i % 4) {
case 0:
f = this.na;
break;
case 1:
f = this.nb;
break;
case 2:
f = this.nc;
break;
default:
f = 1;
break;
}
scaleMatrix[i] = parameters[6 + i] * f;
}
this.matrixCartesianToFractional = J.util.Matrix4f.newA (scaleMatrix);
this.matrixFractionalToCartesian =  new J.util.Matrix4f ();
this.matrixFractionalToCartesian.invertM (this.matrixCartesianToFractional);
} else if (parameters.length > 14 && !Float.isNaN (parameters[14])) {
var m = this.matrixFractionalToCartesian =  new J.util.Matrix4f ();
m.setColumn4 (0, parameters[6] * this.na, parameters[7] * this.na, parameters[8] * this.na, 0);
m.setColumn4 (1, parameters[9] * this.nb, parameters[10] * this.nb, parameters[11] * this.nb, 0);
m.setColumn4 (2, parameters[12] * this.nc, parameters[13] * this.nc, parameters[14] * this.nc, 0);
m.setColumn4 (3, 0, 0, 0, 1);
this.matrixCartesianToFractional =  new J.util.Matrix4f ();
this.matrixCartesianToFractional.invertM (this.matrixFractionalToCartesian);
} else {
var m = this.matrixFractionalToCartesian =  new J.util.Matrix4f ();
m.setColumn4 (0, this.a, 0, 0, 0);
m.setColumn4 (1, (this.b * this.cosGamma), (this.b * this.sinGamma), 0, 0);
m.setColumn4 (2, (this.c * this.cosBeta), (this.c * (this.cosAlpha - this.cosBeta * this.cosGamma) / this.sinGamma), (this.volume / (this.a * this.b * this.sinGamma)), 0);
m.setColumn4 (3, 0, 0, 0, 1);
this.matrixCartesianToFractional =  new J.util.Matrix4f ();
this.matrixCartesianToFractional.invertM (this.matrixFractionalToCartesian);
}this.matrixCtoFAbsolute = this.matrixCartesianToFractional;
this.matrixFtoCAbsolute = this.matrixFractionalToCartesian;
}, "~A");
$_M(c$, "toSupercell", 
function (fpt) {
fpt.x /= this.na;
fpt.y /= this.nb;
fpt.z /= this.nc;
return fpt;
}, "J.util.P3");
$_M(c$, "toCartesian", 
function (pt, isAbsolute) {
if (this.matrixFractionalToCartesian != null) (isAbsolute ? this.matrixFtoCAbsolute : this.matrixFractionalToCartesian).transform (pt);
}, "J.util.Tuple3f,~B");
$_M(c$, "toFractional", 
function (pt, isAbsolute) {
if (this.matrixCartesianToFractional == null) return;
(isAbsolute ? this.matrixCtoFAbsolute : this.matrixCartesianToFractional).transform (pt);
}, "J.util.Tuple3f,~B");
$_M(c$, "isPolymer", 
function () {
return (this.dimension == 1);
});
$_M(c$, "isSlab", 
function () {
return (this.dimension == 2);
});
$_M(c$, "getNotionalUnitCell", 
function () {
return this.notionalUnitcell;
});
$_M(c$, "getUnitCellAsArray", 
function (vectorsOnly) {
var m = this.matrixFractionalToCartesian;
return (vectorsOnly ? [m.m00, m.m10, m.m20, m.m01, m.m11, m.m21, m.m02, m.m12, m.m22] : [this.a, this.b, this.c, this.alpha, this.beta, this.gamma, m.m00, m.m10, m.m20, m.m01, m.m11, m.m21, m.m02, m.m12, m.m22, this.dimension, this.volume]);
}, "~B");
$_M(c$, "getInfo", 
function (infoType) {
switch (infoType) {
case 0:
return this.a;
case 1:
return this.b;
case 2:
return this.c;
case 3:
return this.alpha;
case 4:
return this.beta;
case 5:
return this.gamma;
case 6:
return this.dimension;
}
return NaN;
}, "~N");
c$.ijkToPoint3f = $_M(c$, "ijkToPoint3f", 
function (nnn, cell, c) {
c -= 5;
cell.x = Clazz.doubleToInt (nnn / 100) + c;
cell.y = Clazz.doubleToInt ((nnn % 100) / 10) + c;
cell.z = (nnn % 10) + c;
}, "~N,J.util.P3,~N");
Clazz.defineStatics (c$,
"toRadians", 0.017453292,
"INFO_DIMENSIONS", 6,
"INFO_GAMMA", 5,
"INFO_BETA", 4,
"INFO_ALPHA", 3,
"INFO_C", 2,
"INFO_B", 1,
"INFO_A", 0);
});
