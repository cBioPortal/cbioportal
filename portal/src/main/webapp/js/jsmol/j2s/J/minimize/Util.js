Clazz.declarePackage ("J.minimize");
Clazz.load (null, "J.minimize.Util", ["java.lang.Double", "java.util.Random"], function () {
c$ = Clazz.declareType (J.minimize, "Util");
c$.sub = $_M(c$, "sub", 
function (a, b, result) {
result.set (a[0] - b[0], a[1] - b[1], a[2] - b[2]);
}, "~A,~A,J.util.Vector3d");
c$.putCoord = $_M(c$, "putCoord", 
function (v, c) {
c[0] = v.x;
c[1] = v.y;
c[2] = v.z;
}, "J.util.Vector3d,~A");
c$.distance2 = $_M(c$, "distance2", 
function (a, b) {
var dx = a[0] - b[0];
var dy = a[1] - b[1];
var dz = a[2] - b[2];
return (dx * dx + dy * dy + dz * dz);
}, "~A,~A");
c$.distance2V = $_M(c$, "distance2V", 
function (a, b) {
var dx = a.x - b.x;
var dy = a.y - b.y;
var dz = a.z - b.z;
return (dx * dx + dy * dy + dz * dz);
}, "J.util.Vector3d,J.util.Vector3d");
c$.getAngleRadiansABC = $_M(c$, "getAngleRadiansABC", 
function (a, b, c) {
var ab2 = J.minimize.Util.distance2 (a, b);
var bc2 = J.minimize.Util.distance2 (b, c);
var ac2 = J.minimize.Util.distance2 (a, c);
return (J.minimize.Util.isNearZero2 (ab2, 1e-3) || J.minimize.Util.isNearZero2 (bc2, 1e-3) ? 0 : Math.acos ((ab2 + bc2 - ac2) / 2 / Math.sqrt (ab2 * bc2)));
}, "~A,~A,~A");
c$.isApprox = $_M(c$, "isApprox", 
function (a, b, precision) {
return (J.minimize.Util.distance2V (a, b) <= precision * precision * Math.min (a.lengthSquared (), b.lengthSquared ()));
}, "J.util.Vector3d,J.util.Vector3d,~N");
c$.canBeSquared = $_M(c$, "canBeSquared", 
function (x) {
if (x == 0) return true;
return ((x = Math.abs (x)) < 1.0E150 && x > 1.0E-150);
}, "~N");
c$.isNegligible = $_M(c$, "isNegligible", 
function (a, b) {
return J.minimize.Util.isNegligible3 (a, b, 1e-11);
}, "~N,~N");
c$.isFinite = $_M(c$, "isFinite", 
function (a) {
return !Double.isInfinite (a) && !Double.isNaN (a);
}, "~N");
c$.isNegligible3 = $_M(c$, "isNegligible3", 
function (a, b, precision) {
return (Math.abs (a) <= precision * Math.abs (b));
}, "~N,~N,~N");
c$.isNear = $_M(c$, "isNear", 
function (a, b) {
return J.minimize.Util.isNear3 (a, b, 2e-6);
}, "~N,~N");
c$.isNear3 = $_M(c$, "isNear3", 
function (a, b, epsilon) {
return (Math.abs (a - b) < epsilon);
}, "~N,~N,~N");
c$.isNearZero = $_M(c$, "isNearZero", 
function (a) {
return J.minimize.Util.isNearZero2 (a, 2e-6);
}, "~N");
c$.isNearZero2 = $_M(c$, "isNearZero2", 
function (a, epsilon) {
return (Math.abs (a) < epsilon);
}, "~N,~N");
c$.canBeNormalized = $_M(c$, "canBeNormalized", 
function (a) {
if (a.x == 0.0 && a.y == 0.0 && a.z == 0.0) return false;
return (J.minimize.Util.canBeSquared (a.x) && J.minimize.Util.canBeSquared (a.y) && J.minimize.Util.canBeSquared (a.z));
}, "J.util.Vector3d");
c$.pointPlaneAngleRadians = $_M(c$, "pointPlaneAngleRadians", 
function (a, b, c, d, v1, v2, norm, fixTheta) {
v1.sub2 (b, c);
v2.sub2 (b, d);
norm.cross (v1, v2);
v2.add (v1);
v1.sub2 (b, a);
var angleA_CD = (fixTheta ? J.minimize.Util.vectorAngleRadians (v2, v1) : 3.141592653589793);
var angleNorm = J.minimize.Util.vectorAngleRadians (norm, v1);
if (angleNorm > 1.5707963267948966) angleNorm = 3.141592653589793 - angleNorm;
var val = 1.5707963267948966 + (angleA_CD > 1.5707963267948966 ? -angleNorm : angleNorm);
return val;
}, "J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,~B");
c$.vectorAngleRadians = $_M(c$, "vectorAngleRadians", 
($fz = function (v1, v2) {
var l1 = v1.length ();
var l2 = v2.length ();
return (J.minimize.Util.isNearZero (l1) || J.minimize.Util.isNearZero (l2) ? 0 : Math.acos (v1.dot (v2) / (l1 * l2)));
}, $fz.isPrivate = true, $fz), "J.util.Vector3d,J.util.Vector3d");
c$.getTorsionAngleRadians = $_M(c$, "getTorsionAngleRadians", 
function (a, b, c, d, r1, r2, r3) {
J.minimize.Util.sub (b, a, r1);
J.minimize.Util.sub (c, b, r2);
r2.normalize ();
r1.cross (r1, r2);
J.minimize.Util.sub (d, c, r3);
r3.cross (r2, r3);
var p1dotp2 = r1.dot (r3);
r1.cross (r3, r1);
var theta = Math.atan2 (-r2.dot (r1), p1dotp2);
return theta;
}, "~A,~A,~A,~A,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d");
c$.restorativeForceAndDistance = $_M(c$, "restorativeForceAndDistance", 
function (a, b, vab) {
vab.sub2 (a, b);
var rab = vab.length ();
if (rab < 0.1) {
J.minimize.Util.randomizeUnitVector (vab);
rab = 0.1;
}vab.normalize ();
a.setT (vab);
a.scale (-1);
b.setT (vab);
return rab;
}, "J.util.Vector3d,J.util.Vector3d,J.util.Vector3d");
c$.randomizeUnitVector = $_M(c$, "randomizeUnitVector", 
($fz = function (v) {
var ptr =  new java.util.Random ();
var l;
do {
v.set (ptr.nextFloat () - 0.5, ptr.nextFloat () - 0.5, ptr.nextFloat () - 0.5);
l = v.lengthSquared ();
} while ((l > 1.0) || (l < 1e-4));
v.normalize ();
}, $fz.isPrivate = true, $fz), "J.util.Vector3d");
c$.restorativeForceAndAngleRadians = $_M(c$, "restorativeForceAndAngleRadians", 
function (i, j, k) {
i.sub (j);
k.sub (j);
var length1 = i.length ();
var length2 = k.length ();
if (J.minimize.Util.isNearZero (length1) || J.minimize.Util.isNearZero (length2)) {
i.set (0, 0, 0);
j.set (0, 0, 0);
k.set (0, 0, 0);
return 0.0;
}var inverse_length_v1 = 1.0 / length1;
var inverse_length_v2 = 1.0 / length2;
i.scale (inverse_length_v1);
k.scale (inverse_length_v2);
j.cross (i, k);
var length = j.length ();
if (J.minimize.Util.isNearZero (length)) {
i.set (0, 0, 0);
j.set (0, 0, 0);
k.set (0, 0, 0);
return 0.0;
}j.scale (1 / length);
var costheta = i.dot (k);
var theta;
if (costheta > 1.0) {
theta = 0.0;
costheta = 1.0;
} else if (costheta < -1.0) {
theta = 3.141592653589793;
costheta = -1.0;
} else {
theta = Math.acos (costheta);
}i.cross (i, j);
i.normalize ();
j.cross (k, j);
j.normalize ();
i.scale (-inverse_length_v1);
j.scale (inverse_length_v2);
k.setT (j);
j.add (i);
j.scale (-1);
return theta;
}, "J.util.Vector3d,J.util.Vector3d,J.util.Vector3d");
c$.restorativeForceAndOutOfPlaneAngleRadians = $_M(c$, "restorativeForceAndOutOfPlaneAngleRadians", 
function (i, j, k, l, an, bn, cn) {
i.sub2 (i, j);
k.sub2 (k, j);
l.sub2 (l, j);
var length_ji = i.length ();
var length_jk = k.length ();
var length_jl = l.length ();
if (J.minimize.Util.isNearZero (length_ji) || J.minimize.Util.isNearZero (length_jk) || J.minimize.Util.isNearZero (length_jl)) {
i.set (0, 0, 0);
j.set (0, 0, 0);
k.set (0, 0, 0);
l.set (0, 0, 0);
return 0.0;
}i.normalize ();
k.normalize ();
l.normalize ();
var cos_theta = i.dot (k);
var theta = Math.acos (cos_theta);
if (J.minimize.Util.isNearZero (theta) || J.minimize.Util.isNearZero (Math.abs (theta - 3.141592653589793))) {
i.set (0, 0, 0);
j.set (0, 0, 0);
k.set (0, 0, 0);
l.set (0, 0, 0);
return 0.0;
}var csc_theta = 1 / Math.sin (theta);
an.cross (i, k);
bn.cross (k, l);
cn.cross (l, i);
var sin_dl = an.dot (l) * csc_theta;
var dl = Math.asin (sin_dl);
var cos_dl = Math.cos (dl);
if (cos_dl < 0.0001 || J.minimize.Util.isNearZero (dl) || J.minimize.Util.isNearZero (Math.abs (dl - 3.141592653589793))) {
i.set (0, 0, 0);
j.set (0, 0, 0);
k.set (0, 0, 0);
l.set (0, 0, 0);
return dl;
}l.scaleAdd (-sin_dl / csc_theta, l, an);
l.scale (csc_theta / length_jl);
j.setT (i);
i.scaleAdd (-cos_theta, k, i);
i.scaleAdd (-sin_dl * csc_theta, i, bn);
i.scale (csc_theta / length_ji);
k.scaleAdd (-cos_theta, j, k);
k.scaleAdd (-sin_dl * csc_theta, k, cn);
k.scale (csc_theta / length_jk);
j.setT (i);
j.add (k);
j.add (l);
j.scale (-1);
return dl;
}, "J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d");
c$.restorativeForceAndTorsionAngleRadians = $_M(c$, "restorativeForceAndTorsionAngleRadians", 
function (i, j, k, l) {
i.sub2 (j, i);
j.sub2 (k, j);
k.sub2 (l, k);
var len_ij = i.length ();
var len_jk = j.length ();
var len_kl = k.length ();
if (J.minimize.Util.isNearZero (len_ij) || J.minimize.Util.isNearZero (len_jk) || J.minimize.Util.isNearZero (len_kl)) {
i.set (0, 0, 0);
j.set (0, 0, 0);
k.set (0, 0, 0);
l.set (0, 0, 0);
return 0.0;
}var ang = J.minimize.Util.vectorAngleRadians (i, j);
var sin_j = Math.sin (ang);
var cos_j = Math.cos (ang);
ang = J.minimize.Util.vectorAngleRadians (j, k);
var sin_k = Math.sin (ang);
var cos_k = Math.cos (ang);
i.normalize ();
j.normalize ();
k.normalize ();
i.cross (i, j);
l.cross (j, k);
k.cross (i, l);
var theta = -Math.atan2 (k.dot (j), i.dot (l));
i.scale (1. / len_ij / sin_j / sin_j);
l.scale (-1.0 / len_kl / sin_k / sin_k);
j.setT (i);
j.scale (-len_ij / len_jk * cos_j - 1.);
k.setT (l);
k.scale (-len_kl / len_jk * cos_k);
j.sub (k);
k.setT (i);
k.add (j);
k.add (l);
k.scale (-1);
return theta;
}, "J.util.Vector3d,J.util.Vector3d,J.util.Vector3d,J.util.Vector3d");
Clazz.defineStatics (c$,
"RAD_TO_DEG", (57.29577951308232),
"DEG_TO_RAD", (0.017453292519943295),
"max_squarable_double", 1e150,
"min_squarable_double", 1e-150);
});
