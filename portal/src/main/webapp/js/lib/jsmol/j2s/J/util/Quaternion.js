Clazz.declarePackage ("J.util");
Clazz.load (["J.util.P4"], "J.util.Quaternion", ["java.lang.Float", "J.util.AxisAngle4f", "$.Escape", "$.Logger", "$.Matrix3f", "$.P3", "$.TextFormat", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.q0 = 0;
this.q1 = 0;
this.q2 = 0;
this.q3 = 0;
this.mat = null;
Clazz.instantialize (this, arguments);
}, J.util, "Quaternion");
Clazz.makeConstructor (c$, 
function () {
this.q0 = 1;
});
c$.newQ = $_M(c$, "newQ", 
function (q) {
var q1 =  new J.util.Quaternion ();
q1.set (q);
return q1;
}, "J.util.Quaternion");
c$.newVA = $_M(c$, "newVA", 
function (pt, theta) {
var q =  new J.util.Quaternion ();
q.setTA (pt, theta);
return q;
}, "J.util.Tuple3f,~N");
c$.newM = $_M(c$, "newM", 
function (mat) {
var q =  new J.util.Quaternion ();
q.setM (mat);
return q;
}, "J.util.Matrix3f");
c$.newAA = $_M(c$, "newAA", 
function (a) {
var q =  new J.util.Quaternion ();
q.setAA (a);
return q;
}, "J.util.AxisAngle4f");
c$.newP4 = $_M(c$, "newP4", 
function (pt) {
var q =  new J.util.Quaternion ();
q.setP4 (pt);
return q;
}, "J.util.P4");
c$.new4 = $_M(c$, "new4", 
function (q0, q1, q2, q3) {
var q =  new J.util.Quaternion ();
if (q0 < -1) {
q.q0 = -1;
return q;
}if (q0 > 1) {
q.q0 = 1;
return q;
}q.q0 = q0;
q.q1 = q1;
q.q2 = q2;
q.q3 = q3;
return q;
}, "~N,~N,~N,~N");
$_M(c$, "set", 
function (q) {
this.q0 = q.q0;
this.q1 = q.q1;
this.q2 = q.q2;
this.q3 = q.q3;
}, "J.util.Quaternion");
$_M(c$, "setP4", 
($fz = function (pt) {
var factor = (pt == null ? 0 : pt.distance (J.util.Quaternion.qZero));
if (factor == 0) {
this.q0 = 1;
return;
}this.q0 = pt.w / factor;
this.q1 = pt.x / factor;
this.q2 = pt.y / factor;
this.q3 = pt.z / factor;
}, $fz.isPrivate = true, $fz), "J.util.P4");
$_M(c$, "setTA", 
function (pt, theta) {
if (pt.x == 0 && pt.y == 0 && pt.z == 0) {
this.q0 = 1;
return;
}var fact = (Math.sin (theta / 2 * 0.017453292519943295) / Math.sqrt (pt.x * pt.x + pt.y * pt.y + pt.z * pt.z));
this.q0 = (Math.cos (theta / 2 * 0.017453292519943295));
this.q1 = (pt.x * fact);
this.q2 = (pt.y * fact);
this.q3 = (pt.z * fact);
}, "J.util.Tuple3f,~N");
$_M(c$, "setAA", 
function (a) {
var aa = J.util.AxisAngle4f.newAA (a);
if (aa.angle == 0) aa.y = 1;
var m3 =  new J.util.Matrix3f ();
m3.setAA (aa);
this.setM (m3);
}, "J.util.AxisAngle4f");
$_M(c$, "setM", 
function (mat) {
this.mat = mat;
var trace = mat.m00 + mat.m11 + mat.m22;
var temp;
var w;
var x;
var y;
var z;
if (trace >= 0.5) {
w = Math.sqrt (1.0 + trace);
x = (mat.m21 - mat.m12) / w;
y = (mat.m02 - mat.m20) / w;
z = (mat.m10 - mat.m01) / w;
} else if ((temp = mat.m00 + mat.m00 - trace) >= 0.5) {
x = Math.sqrt (1.0 + temp);
w = (mat.m21 - mat.m12) / x;
y = (mat.m10 + mat.m01) / x;
z = (mat.m20 + mat.m02) / x;
} else if ((temp = mat.m11 + mat.m11 - trace) >= 0.5 || mat.m11 > mat.m22) {
y = Math.sqrt (1.0 + temp);
w = (mat.m02 - mat.m20) / y;
x = (mat.m10 + mat.m01) / y;
z = (mat.m21 + mat.m12) / y;
} else {
z = Math.sqrt (1.0 + mat.m22 + mat.m22 - trace);
w = (mat.m10 - mat.m01) / z;
x = (mat.m20 + mat.m02) / z;
y = (mat.m21 + mat.m12) / z;
}this.q0 = (w * 0.5);
this.q1 = (x * 0.5);
this.q2 = (y * 0.5);
this.q3 = (z * 0.5);
}, "J.util.Matrix3f");
$_M(c$, "setRef", 
function (qref) {
if (qref == null) {
this.mul (this.getFixFactor ());
return;
}if (this.dot (qref) >= 0) return;
this.q0 *= -1;
this.q1 *= -1;
this.q2 *= -1;
this.q3 *= -1;
}, "J.util.Quaternion");
c$.getQuaternionFrame = $_M(c$, "getQuaternionFrame", 
function (center, x, xy) {
var vA = J.util.V3.newV (x);
var vB = J.util.V3.newV (xy);
if (center != null) {
vA.sub (center);
vB.sub (center);
}return J.util.Quaternion.getQuaternionFrameV (vA, vB, null, false);
}, "J.util.P3,J.util.Tuple3f,J.util.Tuple3f");
c$.getQuaternionFrameV = $_M(c$, "getQuaternionFrameV", 
function (vA, vB, vC, yBased) {
if (vC == null) {
vC =  new J.util.V3 ();
vC.cross (vA, vB);
if (yBased) vA.cross (vB, vC);
}var vBprime =  new J.util.V3 ();
vBprime.cross (vC, vA);
vA.normalize ();
vBprime.normalize ();
vC.normalize ();
var mat =  new J.util.Matrix3f ();
mat.setColumnV (0, vA);
mat.setColumnV (1, vBprime);
mat.setColumnV (2, vC);
var q = J.util.Quaternion.newM (mat);
return q;
}, "J.util.V3,J.util.V3,J.util.V3,~B");
$_M(c$, "getMatrix", 
function () {
if (this.mat == null) this.setMatrix ();
return this.mat;
});
$_M(c$, "setMatrix", 
($fz = function () {
this.mat =  new J.util.Matrix3f ();
this.mat.m00 = this.q0 * this.q0 + this.q1 * this.q1 - this.q2 * this.q2 - this.q3 * this.q3;
this.mat.m01 = 2 * this.q1 * this.q2 - 2 * this.q0 * this.q3;
this.mat.m02 = 2 * this.q1 * this.q3 + 2 * this.q0 * this.q2;
this.mat.m10 = 2 * this.q1 * this.q2 + 2 * this.q0 * this.q3;
this.mat.m11 = this.q0 * this.q0 - this.q1 * this.q1 + this.q2 * this.q2 - this.q3 * this.q3;
this.mat.m12 = 2 * this.q2 * this.q3 - 2 * this.q0 * this.q1;
this.mat.m20 = 2 * this.q1 * this.q3 - 2 * this.q0 * this.q2;
this.mat.m21 = 2 * this.q2 * this.q3 + 2 * this.q0 * this.q1;
this.mat.m22 = this.q0 * this.q0 - this.q1 * this.q1 - this.q2 * this.q2 + this.q3 * this.q3;
}, $fz.isPrivate = true, $fz));
$_M(c$, "add", 
function (x) {
return J.util.Quaternion.newVA (this.getNormal (), this.getTheta () + x);
}, "~N");
$_M(c$, "mul", 
function (x) {
return (x == 1 ? J.util.Quaternion.new4 (this.q0, this.q1, this.q2, this.q3) : J.util.Quaternion.newVA (this.getNormal (), this.getTheta () * x));
}, "~N");
$_M(c$, "mulQ", 
function (p) {
return J.util.Quaternion.new4 (this.q0 * p.q0 - this.q1 * p.q1 - this.q2 * p.q2 - this.q3 * p.q3, this.q0 * p.q1 + this.q1 * p.q0 + this.q2 * p.q3 - this.q3 * p.q2, this.q0 * p.q2 + this.q2 * p.q0 + this.q3 * p.q1 - this.q1 * p.q3, this.q0 * p.q3 + this.q3 * p.q0 + this.q1 * p.q2 - this.q2 * p.q1);
}, "J.util.Quaternion");
$_M(c$, "div", 
function (p) {
return this.mulQ (p.inv ());
}, "J.util.Quaternion");
$_M(c$, "divLeft", 
function (p) {
return this.inv ().mulQ (p);
}, "J.util.Quaternion");
$_M(c$, "dot", 
function (q) {
return this.q0 * q.q0 + this.q1 * q.q1 + this.q2 * q.q2 + this.q3 * q.q3;
}, "J.util.Quaternion");
$_M(c$, "inv", 
function () {
return J.util.Quaternion.new4 (this.q0, -this.q1, -this.q2, -this.q3);
});
$_M(c$, "negate", 
function () {
return J.util.Quaternion.new4 (-this.q0, -this.q1, -this.q2, -this.q3);
});
$_M(c$, "getFixFactor", 
($fz = function () {
return (this.q0 < 0 || this.q0 == 0 && (this.q1 < 0 || this.q1 == 0 && (this.q2 < 0 || this.q2 == 0 && this.q3 < 0)) ? -1 : 1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getVector", 
function (i) {
return this.getVectorS (i, 1);
}, "~N");
$_M(c$, "getVectorS", 
($fz = function (i, scale) {
if (i == -1) {
scale *= this.getFixFactor ();
return J.util.V3.new3 (this.q1 * scale, this.q2 * scale, this.q3 * scale);
}if (this.mat == null) this.setMatrix ();
var v =  new J.util.V3 ();
this.mat.getColumnV (i, v);
if (scale != 1) v.scale (scale);
return v;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "getNormal", 
function () {
var v = J.util.Quaternion.getRawNormal (this);
v.scale (this.getFixFactor ());
return v;
});
c$.getRawNormal = $_M(c$, "getRawNormal", 
($fz = function (q) {
var v = J.util.V3.new3 (q.q1, q.q2, q.q3);
if (v.length () == 0) return J.util.V3.new3 (0, 0, 1);
v.normalize ();
return v;
}, $fz.isPrivate = true, $fz), "J.util.Quaternion");
$_M(c$, "getTheta", 
function () {
return (Math.acos (Math.abs (this.q0)) * 2 * 180 / 3.141592653589793);
});
$_M(c$, "getThetaRadians", 
function () {
return (Math.acos (Math.abs (this.q0)) * 2);
});
$_M(c$, "getNormalDirected", 
function (v0) {
var v = this.getNormal ();
if (v.x * v0.x + v.y * v0.y + v.z * v0.z < 0) {
v.scale (-1);
}return v;
}, "J.util.V3");
$_M(c$, "get3dProjection", 
function (v3d) {
v3d.set (this.q1, this.q2, this.q3);
return v3d;
}, "J.util.V3");
$_M(c$, "getThetaDirected", 
function (axisAngle) {
var theta = this.getTheta ();
var v = this.getNormal ();
if (axisAngle.x * this.q1 + axisAngle.y * this.q2 + axisAngle.z * this.q3 < 0) {
v.scale (-1);
theta = -theta;
}axisAngle.set (v.x, v.y, v.z, theta);
return axisAngle;
}, "J.util.P4");
$_M(c$, "getThetaDirectedV", 
function (vector) {
var theta = this.getTheta ();
var v = this.getNormal ();
if (vector.x * this.q1 + vector.y * this.q2 + vector.z * this.q3 < 0) {
v.scale (-1);
theta = -theta;
}return theta;
}, "J.util.V3");
$_M(c$, "toPoint4f", 
function () {
return J.util.P4.new4 (this.q1, this.q2, this.q3, this.q0);
});
$_M(c$, "toAxisAngle4f", 
function () {
var theta = 2 * Math.acos (Math.abs (this.q0));
var sinTheta2 = Math.sin (theta / 2);
var v = this.getNormal ();
if (sinTheta2 < 0) {
v.scale (-1);
theta = 3.141592653589793 - theta;
}return J.util.AxisAngle4f.newVA (v, theta);
});
$_M(c$, "transformPt", 
function (pt) {
if (this.mat == null) this.setMatrix ();
var ptNew = J.util.P3.newP (pt);
this.mat.transform (ptNew);
return ptNew;
}, "J.util.P3");
$_M(c$, "transformP2", 
function (pt, ptNew) {
if (this.mat == null) this.setMatrix ();
this.mat.transform2 (pt, ptNew);
return ptNew;
}, "J.util.Tuple3f,J.util.Tuple3f");
$_M(c$, "transform", 
function (v) {
if (this.mat == null) this.setMatrix ();
var vNew = J.util.V3.newV (v);
this.mat.transform (vNew);
return vNew;
}, "J.util.V3");
$_M(c$, "leftDifference", 
function (q2) {
var q2adjusted = (this.dot (q2) < 0 ? q2.negate () : q2);
return this.inv ().mulQ (q2adjusted);
}, "J.util.Quaternion");
$_M(c$, "rightDifference", 
function (q2) {
var q2adjusted = (this.dot (q2) < 0 ? q2.negate () : q2);
return this.mulQ (q2adjusted.inv ());
}, "J.util.Quaternion");
$_M(c$, "getInfo", 
function () {
var axis = this.toAxisAngle4f ();
return J.util.TextFormat.sprintf ("%10.6f%10.6f%10.6f%10.6f  %6.2f  %10.5f %10.5f %10.5f", "F", [[this.q0, this.q1, this.q2, this.q3, (axis.angle * 180 / 3.141592653589793), axis.x, axis.y, axis.z]]);
});
$_M(c$, "draw", 
function (prefix, id, ptCenter, scale) {
var strV = " VECTOR " + J.util.Escape.eP (ptCenter) + " ";
if (scale == 0) scale = 1;
return "draw " + prefix + "x" + id + strV + J.util.Escape.eP (this.getVectorS (0, scale)) + " color red\n" + "draw " + prefix + "y" + id + strV + J.util.Escape.eP (this.getVectorS (1, scale)) + " color green\n" + "draw " + prefix + "z" + id + strV + J.util.Escape.eP (this.getVectorS (2, scale)) + " color blue\n";
}, "~S,~S,J.util.P3,~N");
Clazz.overrideMethod (c$, "toString", 
function () {
return "{" + this.q1 + " " + this.q2 + " " + this.q3 + " " + this.q0 + "}";
});
c$.div = $_M(c$, "div", 
function (data1, data2, nMax, isRelative) {
var n;
if (data1 == null || data2 == null || (n = Math.min (data1.length, data2.length)) == 0) return null;
if (nMax > 0 && n > nMax) n = nMax;
var dqs =  new Array (n);
for (var i = 0; i < n; i++) {
if (data1[i] == null || data2[i] == null) return null;
dqs[i] = (isRelative ? data1[i].divLeft (data2[i]) : data1[i].div (data2[i]));
}
return dqs;
}, "~A,~A,~N,~B");
c$.sphereMean = $_M(c$, "sphereMean", 
function (data, retStddev, criterion) {
if (data == null || data.length == 0) return  new J.util.Quaternion ();
if (retStddev == null) retStddev =  Clazz.newFloatArray (1, 0);
if (data.length == 1) {
retStddev[0] = 0;
return J.util.Quaternion.newQ (data[0]);
}var diff = 3.4028235E38;
var lastStddev = 3.4028235E38;
var qMean = J.util.Quaternion.simpleAverage (data);
var maxIter = 100;
var iter = 0;
while (diff > criterion && lastStddev != 0 && iter < maxIter) {
qMean = J.util.Quaternion.newMean (data, qMean);
retStddev[0] = J.util.Quaternion.stdDev (data, qMean);
diff = Math.abs (retStddev[0] - lastStddev);
lastStddev = retStddev[0];
J.util.Logger.info (++iter + " sphereMean " + qMean + " stddev=" + lastStddev + " diff=" + diff);
}
return qMean;
}, "~A,~A,~N");
c$.simpleAverage = $_M(c$, "simpleAverage", 
($fz = function (ndata) {
var mean = J.util.V3.new3 (0, 0, 1);
var v = ndata[0].getNormal ();
mean.add (v);
for (var i = ndata.length; --i >= 0; ) mean.add (ndata[i].getNormalDirected (mean));

mean.sub (v);
mean.normalize ();
var f = 0;
for (var i = ndata.length; --i >= 0; ) f += Math.abs (ndata[i].get3dProjection (v).dot (mean));

if (f != 0) mean.scale (f / ndata.length);
f = Math.sqrt (1 - mean.lengthSquared ());
if (Float.isNaN (f)) f = 0;
return J.util.Quaternion.newP4 (J.util.P4.new4 (mean.x, mean.y, mean.z, f));
}, $fz.isPrivate = true, $fz), "~A");
c$.newMean = $_M(c$, "newMean", 
($fz = function (data, mean) {
var sum =  new J.util.V3 ();
var v;
var q;
var dq;
for (var i = data.length; --i >= 0; ) {
q = data[i];
dq = q.div (mean);
v = dq.getNormal ();
v.scale (dq.getTheta ());
sum.add (v);
}
sum.scale (1 / data.length);
var dqMean = J.util.Quaternion.newVA (sum, sum.length ());
return dqMean.mulQ (mean);
}, $fz.isPrivate = true, $fz), "~A,J.util.Quaternion");
c$.stdDev = $_M(c$, "stdDev", 
($fz = function (data, mean) {
var sum = 0;
var sum2 = 0;
var n = data.length;
for (var i = n; --i >= 0; ) {
var dq = data[i].div (mean);
var theta = dq.getTheta ();
sum += theta;
sum2 += theta * theta;
}
sum2 = sum2 - sum * sum / n;
if (sum2 < 0) sum2 = 0;
return Math.sqrt (sum2 / (n - 1));
}, $fz.isPrivate = true, $fz), "~A,J.util.Quaternion");
$_M(c$, "getEulerZYZ", 
function () {
var rA;
var rB;
var rG;
rA = Math.atan2 (2 * (this.q2 * this.q3 - this.q0 * this.q1), 2 * (this.q1 * this.q3 + this.q0 * this.q2));
rB = Math.acos (this.q3 * this.q3 - this.q2 * this.q2 - this.q1 * this.q1 + this.q0 * this.q0);
rG = Math.atan2 (2 * (this.q2 * this.q3 + this.q0 * this.q1), 2 * (this.q0 * this.q2 - this.q1 * this.q3));
return [(rA / 0.017453292519943295), (rB / 0.017453292519943295), (rG / 0.017453292519943295)];
});
$_M(c$, "getEulerZXZ", 
function () {
var rA;
var rB;
var rG;
rA = Math.atan2 (2 * (this.q1 * this.q3 + this.q0 * this.q2), 2 * (this.q0 * this.q1 - this.q2 * this.q3));
rB = Math.acos (this.q3 * this.q3 - this.q2 * this.q2 - this.q1 * this.q1 + this.q0 * this.q0);
rG = Math.atan2 (2 * (this.q1 * this.q3 - this.q0 * this.q2), 2 * (this.q2 * this.q3 + this.q0 * this.q1));
return [(rA / 0.017453292519943295), (rB / 0.017453292519943295), (rG / 0.017453292519943295)];
});
c$.qZero = c$.prototype.qZero =  new J.util.P4 ();
Clazz.defineStatics (c$,
"RAD_PER_DEG", 0.017453292519943295);
});
