Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.Measure", ["java.lang.Float", "J.util.Eigen", "$.Escape", "$.JmolList", "$.Logger", "$.P3", "$.P4", "$.Quaternion", "$.V3", "J.viewer.JC"], function () {
c$ = Clazz.declareType (J.util, "Measure");
c$.computeAngle = $_M(c$, "computeAngle", 
function (pointA, pointB, pointC, vectorBA, vectorBC, asDegrees) {
vectorBA.sub2 (pointA, pointB);
vectorBC.sub2 (pointC, pointB);
var angle = vectorBA.angle (vectorBC);
return (asDegrees ? angle / 0.017453292 : angle);
}, "J.util.Tuple3f,J.util.Tuple3f,J.util.Tuple3f,J.util.V3,J.util.V3,~B");
c$.computeAngleABC = $_M(c$, "computeAngleABC", 
function (pointA, pointB, pointC, asDegrees) {
var vectorBA =  new J.util.V3 ();
var vectorBC =  new J.util.V3 ();
return J.util.Measure.computeAngle (pointA, pointB, pointC, vectorBA, vectorBC, asDegrees);
}, "J.util.Tuple3f,J.util.Tuple3f,J.util.Tuple3f,~B");
c$.computeTorsion = $_M(c$, "computeTorsion", 
function (p1, p2, p3, p4, asDegrees) {
var ijx = p1.x - p2.x;
var ijy = p1.y - p2.y;
var ijz = p1.z - p2.z;
var kjx = p3.x - p2.x;
var kjy = p3.y - p2.y;
var kjz = p3.z - p2.z;
var klx = p3.x - p4.x;
var kly = p3.y - p4.y;
var klz = p3.z - p4.z;
var ax = ijy * kjz - ijz * kjy;
var ay = ijz * kjx - ijx * kjz;
var az = ijx * kjy - ijy * kjx;
var cx = kjy * klz - kjz * kly;
var cy = kjz * klx - kjx * klz;
var cz = kjx * kly - kjy * klx;
var ai2 = 1 / (ax * ax + ay * ay + az * az);
var ci2 = 1 / (cx * cx + cy * cy + cz * cz);
var ai = Math.sqrt (ai2);
var ci = Math.sqrt (ci2);
var denom = ai * ci;
var cross = ax * cx + ay * cy + az * cz;
var cosang = cross * denom;
if (cosang > 1) {
cosang = 1;
}if (cosang < -1) {
cosang = -1;
}var torsion = Math.acos (cosang);
var dot = ijx * cx + ijy * cy + ijz * cz;
var absDot = Math.abs (dot);
torsion = (dot / absDot > 0) ? torsion : -torsion;
return (asDegrees ? torsion / 0.017453292 : torsion);
}, "J.util.Tuple3f,J.util.Tuple3f,J.util.Tuple3f,J.util.Tuple3f,~B");
c$.computeHelicalAxis = $_M(c$, "computeHelicalAxis", 
function (id, tokType, a, b, dq) {
var vab =  new J.util.V3 ();
vab.sub2 (b, a);
var theta = dq.getTheta ();
var n = dq.getNormal ();
var v_dot_n = vab.dot (n);
if (Math.abs (v_dot_n) < 0.0001) v_dot_n = 0;
if (tokType == 1073741854) {
if (v_dot_n != 0) n.scale (v_dot_n);
return n;
}var va_prime_d =  new J.util.V3 ();
va_prime_d.cross (vab, n);
if (va_prime_d.dot (va_prime_d) != 0) va_prime_d.normalize ();
var vda =  new J.util.V3 ();
var vcb = J.util.V3.newV (n);
if (v_dot_n == 0) v_dot_n = 1.4E-45;
vcb.scale (v_dot_n);
vda.sub2 (vcb, vab);
vda.scale (0.5);
va_prime_d.scale (theta == 0 ? 0 : (vda.length () / Math.tan (theta / 2 / 180 * 3.141592653589793)));
var r = J.util.V3.newV (va_prime_d);
if (theta != 0) r.add (vda);
if (tokType == 1666189314) return r;
var pt_a_prime = J.util.P3.newP (a);
pt_a_prime.sub (r);
if (tokType == 135266320) {
return pt_a_prime;
}if (v_dot_n != 1.4E-45) n.scale (v_dot_n);
var pt_b_prime = J.util.P3.newP (pt_a_prime);
pt_b_prime.add (n);
theta = J.util.Measure.computeTorsion (a, pt_a_prime, pt_b_prime, b, true);
if (Float.isNaN (theta) || r.length () < 0.0001) theta = dq.getThetaDirectedV (n);
if (tokType == 135266305) return Float.$valueOf (theta);
if (tokType == 135176) return "draw ID \"" + id + "\" VECTOR " + J.util.Escape.eP (pt_a_prime) + " " + J.util.Escape.eP (n) + " color " + (theta < 0 ? "{255.0 200.0 0.0}" : "{255.0 0.0 128.0}");
if (tokType == 1746538509) return "measure " + J.util.Escape.eP (a) + J.util.Escape.eP (pt_a_prime) + J.util.Escape.eP (pt_b_prime) + J.util.Escape.eP (b);
var residuesPerTurn = Math.abs (theta == 0 ? 0 : 360 / theta);
var pitch = Math.abs (v_dot_n == 1.4E-45 ? 0 : n.length () * (theta == 0 ? 1 : 360 / theta));
switch (tokType) {
case 135266306:
return [pt_a_prime, n, r, J.util.P3.new3 (theta, pitch, residuesPerTurn)];
case 1073742001:
return [J.util.Escape.eP (pt_a_prime), J.util.Escape.eP (n), J.util.Escape.eP (r), J.util.Escape.eP (J.util.P3.new3 (theta, pitch, residuesPerTurn))];
default:
return null;
}
}, "~S,~N,J.util.P3,J.util.P3,J.util.Quaternion");
c$.getPlaneThroughPoints = $_M(c$, "getPlaneThroughPoints", 
function (pointA, pointB, pointC, vNorm, vAB, vAC, plane) {
var w = J.util.Measure.getNormalThroughPoints (pointA, pointB, pointC, vNorm, vAB, vAC);
plane.set (vNorm.x, vNorm.y, vNorm.z, w);
}, "J.util.P3,J.util.P3,J.util.P3,J.util.V3,J.util.V3,J.util.V3,J.util.P4");
c$.getPlaneThroughPoint = $_M(c$, "getPlaneThroughPoint", 
function (pt, normal, plane) {
plane.set (normal.x, normal.y, normal.z, -normal.dot (pt));
}, "J.util.P3,J.util.V3,J.util.P4");
c$.distanceToPlane = $_M(c$, "distanceToPlane", 
function (plane, pt) {
return (plane == null ? NaN : (plane.x * pt.x + plane.y * pt.y + plane.z * pt.z + plane.w) / Math.sqrt (plane.x * plane.x + plane.y * plane.y + plane.z * plane.z));
}, "J.util.P4,J.util.P3");
c$.distanceToPlaneD = $_M(c$, "distanceToPlaneD", 
function (plane, d, pt) {
return (plane == null ? NaN : (plane.x * pt.x + plane.y * pt.y + plane.z * pt.z + plane.w) / d);
}, "J.util.P4,~N,J.util.P3");
c$.distanceToPlaneV = $_M(c$, "distanceToPlaneV", 
function (norm, w, pt) {
return (norm == null ? NaN : (norm.x * pt.x + norm.y * pt.y + norm.z * pt.z + w) / Math.sqrt (norm.x * norm.x + norm.y * norm.y + norm.z * norm.z));
}, "J.util.V3,~N,J.util.P3");
c$.calcNormalizedNormal = $_M(c$, "calcNormalizedNormal", 
function (pointA, pointB, pointC, vNormNorm, vAB, vAC) {
vAB.sub2 (pointB, pointA);
vAC.sub2 (pointC, pointA);
vNormNorm.cross (vAB, vAC);
vNormNorm.normalize ();
}, "J.util.P3,J.util.P3,J.util.P3,J.util.V3,J.util.V3,J.util.V3");
c$.getDirectedNormalThroughPoints = $_M(c$, "getDirectedNormalThroughPoints", 
function (pointA, pointB, pointC, ptRef, vNorm, vAB, vAC) {
var nd = J.util.Measure.getNormalThroughPoints (pointA, pointB, pointC, vNorm, vAB, vAC);
if (ptRef != null) {
var pt0 = J.util.P3.newP (pointA);
pt0.add (vNorm);
var d = pt0.distance (ptRef);
pt0.setT (pointA);
pt0.sub (vNorm);
if (d > pt0.distance (ptRef)) {
vNorm.scale (-1);
nd = -nd;
}}return nd;
}, "J.util.P3,J.util.P3,J.util.P3,J.util.P3,J.util.V3,J.util.V3,J.util.V3");
c$.getNormalThroughPoints = $_M(c$, "getNormalThroughPoints", 
function (pointA, pointB, pointC, vNorm, vAB, vAC) {
J.util.Measure.calcNormalizedNormal (pointA, pointB, pointC, vNorm, vAB, vAC);
vAB.setT (pointA);
return -vAB.dot (vNorm);
}, "J.util.P3,J.util.P3,J.util.P3,J.util.V3,J.util.V3,J.util.V3");
c$.getPlaneProjection = $_M(c$, "getPlaneProjection", 
function (pt, plane, ptProj, vNorm) {
var dist = J.util.Measure.distanceToPlane (plane, pt);
vNorm.set (plane.x, plane.y, plane.z);
vNorm.normalize ();
vNorm.scale (-dist);
ptProj.setT (pt);
ptProj.add (vNorm);
}, "J.util.P3,J.util.P4,J.util.P3,J.util.V3");
c$.getNormalFromCenter = $_M(c$, "getNormalFromCenter", 
function (ptCenter, ptA, ptB, ptC, isOutward, normal) {
var vAB =  new J.util.V3 ();
var vAC =  new J.util.V3 ();
var d = J.util.Measure.getNormalThroughPoints (ptA, ptB, ptC, normal, vAB, vAC);
var isReversed = (J.util.Measure.distanceToPlaneV (normal, d, ptCenter) > 0);
if (isReversed == isOutward) normal.scale (-1.0);
return !isReversed;
}, "J.util.P3,J.util.P3,J.util.P3,J.util.P3,~B,J.util.V3");
c$.getNormalToLine = $_M(c$, "getNormalToLine", 
function (pointA, pointB, vNormNorm) {
vNormNorm.sub2 (pointA, pointB);
vNormNorm.cross (vNormNorm, J.viewer.JC.axisY);
vNormNorm.normalize ();
if (Float.isNaN (vNormNorm.x)) vNormNorm.set (1, 0, 0);
}, "J.util.P3,J.util.P3,J.util.V3");
c$.getBisectingPlane = $_M(c$, "getBisectingPlane", 
function (pointA, vAB, ptTemp, vTemp, plane) {
ptTemp.scaleAdd2 (0.5, vAB, pointA);
vTemp.setT (vAB);
vTemp.normalize ();
J.util.Measure.getPlaneThroughPoint (ptTemp, vTemp, plane);
}, "J.util.P3,J.util.V3,J.util.P3,J.util.V3,J.util.P4");
c$.projectOntoAxis = $_M(c$, "projectOntoAxis", 
function (point, axisA, axisUnitVector, vectorProjection) {
vectorProjection.sub2 (point, axisA);
var projectedLength = vectorProjection.dot (axisUnitVector);
point.setT (axisUnitVector);
point.scaleAdd (projectedLength, axisA);
vectorProjection.sub2 (point, axisA);
}, "J.util.P3,J.util.P3,J.util.V3,J.util.V3");
c$.calcBestAxisThroughPoints = $_M(c$, "calcBestAxisThroughPoints", 
function (points, axisA, axisUnitVector, vectorProjection, nTriesMax) {
var nPoints = points.length;
axisA.setT (points[0]);
axisUnitVector.sub2 (points[nPoints - 1], axisA);
axisUnitVector.normalize ();
J.util.Measure.calcAveragePointN (points, nPoints, axisA);
var nTries = 0;
while (nTries++ < nTriesMax && J.util.Measure.findAxis (points, nPoints, axisA, axisUnitVector, vectorProjection) > 0.001) {
}
var tempA = J.util.P3.newP (points[0]);
J.util.Measure.projectOntoAxis (tempA, axisA, axisUnitVector, vectorProjection);
axisA.setT (tempA);
}, "~A,J.util.P3,J.util.V3,J.util.V3,~N");
c$.findAxis = $_M(c$, "findAxis", 
function (points, nPoints, axisA, axisUnitVector, vectorProjection) {
var sumXiYi =  new J.util.V3 ();
var vTemp =  new J.util.V3 ();
var pt =  new J.util.P3 ();
var ptProj =  new J.util.P3 ();
var a = J.util.V3.newV (axisUnitVector);
var sum_Xi2 = 0;
var sum_Yi2 = 0;
for (var i = nPoints; --i >= 0; ) {
pt.setT (points[i]);
ptProj.setT (pt);
J.util.Measure.projectOntoAxis (ptProj, axisA, axisUnitVector, vectorProjection);
vTemp.sub2 (pt, ptProj);
sum_Yi2 += vTemp.lengthSquared ();
vTemp.cross (vectorProjection, vTemp);
sumXiYi.add (vTemp);
sum_Xi2 += vectorProjection.lengthSquared ();
}
var m = J.util.V3.newV (sumXiYi);
m.scale (1 / sum_Xi2);
vTemp.cross (m, axisUnitVector);
axisUnitVector.add (vTemp);
axisUnitVector.normalize ();
vTemp.setT (axisUnitVector);
vTemp.sub (a);
return vTemp.length ();
}, "~A,~N,J.util.P3,J.util.V3,J.util.V3");
c$.calcAveragePoint = $_M(c$, "calcAveragePoint", 
function (pointA, pointB, pointC) {
pointC.set ((pointA.x + pointB.x) / 2, (pointA.y + pointB.y) / 2, (pointA.z + pointB.z) / 2);
}, "J.util.P3,J.util.P3,J.util.P3");
c$.calcAveragePointN = $_M(c$, "calcAveragePointN", 
function (points, nPoints, averagePoint) {
averagePoint.setT (points[0]);
for (var i = 1; i < nPoints; i++) averagePoint.add (points[i]);

averagePoint.scale (1 / nPoints);
}, "~A,~N,J.util.P3");
c$.getCenterAndPoints = $_M(c$, "getCenterAndPoints", 
function (vPts) {
var n = vPts.size ();
var pts =  new Array (n + 1);
pts[0] =  new J.util.P3 ();
if (n > 0) {
for (var i = 0; i < n; i++) {
pts[0].add (pts[i + 1] = vPts.get (i));
}
pts[0].scale (1 / n);
}return pts;
}, "J.util.JmolList");
c$.getTransformMatrix4 = $_M(c$, "getTransformMatrix4", 
function (ptsA, ptsB, m, centerA) {
var cptsA = J.util.Measure.getCenterAndPoints (ptsA);
var cptsB = J.util.Measure.getCenterAndPoints (ptsB);
var retStddev =  Clazz.newFloatArray (2, 0);
var q = J.util.Measure.calculateQuaternionRotation ([cptsA, cptsB], retStddev, true);
var v = J.util.V3.newV (cptsB[0]);
v.sub (cptsA[0]);
m.setMV (q.getMatrix (), v);
if (centerA != null) centerA.setT (cptsA[0]);
return retStddev[1];
}, "J.util.JmolList,J.util.JmolList,J.util.Matrix4f,J.util.P3");
c$.calculateQuaternionRotation = $_M(c$, "calculateQuaternionRotation", 
function (centerAndPoints, retStddev, doReport) {
retStddev[1] = NaN;
var q =  new J.util.Quaternion ();
if (centerAndPoints[0].length == 1 || centerAndPoints[0].length != centerAndPoints[1].length) return q;
var n = centerAndPoints[0].length - 1;
if (doReport) for (var i = 1; i <= n; i++) {
var aij = centerAndPoints[0][i];
var bij = centerAndPoints[1][i];
if (Clazz.instanceOf (aij, J.modelset.Atom) && Clazz.instanceOf (bij, J.modelset.Atom)) J.util.Logger.info (" atom 1 " + (aij).getInfo () + "\tatom 2 " + (bij).getInfo ());
 else break;
}
if (n < 2) return q;
var Sxx = 0;
var Sxy = 0;
var Sxz = 0;
var Syx = 0;
var Syy = 0;
var Syz = 0;
var Szx = 0;
var Szy = 0;
var Szz = 0;
var ptA =  new J.util.P3 ();
var ptB =  new J.util.P3 ();
for (var i = n + 1; --i >= 1; ) {
var aij = centerAndPoints[0][i];
var bij = centerAndPoints[1][i];
ptA.setT (aij);
ptA.sub (centerAndPoints[0][0]);
ptB.setT (bij);
ptB.sub (centerAndPoints[0][1]);
Sxx += ptA.x * ptB.x;
Sxy += ptA.x * ptB.y;
Sxz += ptA.x * ptB.z;
Syx += ptA.y * ptB.x;
Syy += ptA.y * ptB.y;
Syz += ptA.y * ptB.z;
Szx += ptA.z * ptB.x;
Szy += ptA.z * ptB.y;
Szz += ptA.z * ptB.z;
}
retStddev[0] = J.util.Measure.getRmsd (centerAndPoints, q);
var N =  Clazz.newDoubleArray (4, 4, 0);
N[0][0] = Sxx + Syy + Szz;
N[0][1] = N[1][0] = Syz - Szy;
N[0][2] = N[2][0] = Szx - Sxz;
N[0][3] = N[3][0] = Sxy - Syx;
N[1][1] = Sxx - Syy - Szz;
N[1][2] = N[2][1] = Sxy + Syx;
N[1][3] = N[3][1] = Szx + Sxz;
N[2][2] = -Sxx + Syy - Szz;
N[2][3] = N[3][2] = Syz + Szy;
N[3][3] = -Sxx - Syy + Szz;
var eigen = J.util.Eigen.newM (N);
var v = eigen.getEigenvectorsFloatTransposed ()[3];
q = J.util.Quaternion.newP4 (J.util.P4.new4 (v[1], v[2], v[3], v[0]));
retStddev[1] = J.util.Measure.getRmsd (centerAndPoints, q);
return q;
}, "~A,~A,~B");
c$.getRmsd = $_M(c$, "getRmsd", 
function (centerAndPoints, q) {
var sum2 = 0;
var ptsA = centerAndPoints[0];
var ptsB = centerAndPoints[1];
var cA = ptsA[0];
var cB = ptsB[0];
var n = ptsA.length - 1;
var ptAnew =  new J.util.P3 ();
for (var i = n + 1; --i >= 1; ) {
ptAnew.setT (ptsA[i]);
ptAnew.sub (cA);
q.transformP2 (ptAnew, ptAnew).add (cB);
sum2 += ptAnew.distanceSquared (ptsB[i]);
}
return Math.sqrt (sum2 / n);
}, "~A,J.util.Quaternion");
c$.transformPoints = $_M(c$, "transformPoints", 
function (vPts, m4, center) {
var v =  new J.util.JmolList ();
for (var i = 0; i < vPts.size (); i++) {
var pt = J.util.P3.newP (vPts.get (i));
pt.sub (center);
m4.transform2 (pt, pt);
pt.add (center);
v.addLast (pt);
}
return v;
}, "J.util.JmolList,J.util.Matrix4f,J.util.P3");
c$.isInTetrahedron = $_M(c$, "isInTetrahedron", 
function (pt, ptA, ptB, ptC, ptD, plane, vTemp, vTemp2, vTemp3, fullyEnclosed) {
J.util.Measure.getPlaneThroughPoints (ptC, ptD, ptA, vTemp, vTemp2, vTemp3, plane);
var b = (J.util.Measure.distanceToPlane (plane, pt) >= 0);
J.util.Measure.getPlaneThroughPoints (ptA, ptD, ptB, vTemp, vTemp2, vTemp3, plane);
if (b != (J.util.Measure.distanceToPlane (plane, pt) >= 0)) return false;
J.util.Measure.getPlaneThroughPoints (ptB, ptD, ptC, vTemp, vTemp2, vTemp3, plane);
if (b != (J.util.Measure.distanceToPlane (plane, pt) >= 0)) return false;
J.util.Measure.getPlaneThroughPoints (ptA, ptB, ptC, vTemp, vTemp2, vTemp3, plane);
var d = J.util.Measure.distanceToPlane (plane, pt);
if (fullyEnclosed) return (b == (d >= 0));
var d1 = J.util.Measure.distanceToPlane (plane, ptD);
return d1 * d <= 0 || Math.abs (d1) > Math.abs (d);
}, "J.util.P3,J.util.P3,J.util.P3,J.util.P3,J.util.P3,J.util.P4,J.util.V3,J.util.V3,J.util.V3,~B");
c$.getIntersectionPP = $_M(c$, "getIntersectionPP", 
function (plane1, plane2) {
var a1 = plane1.x;
var b1 = plane1.y;
var c1 = plane1.z;
var d1 = plane1.w;
var a2 = plane2.x;
var b2 = plane2.y;
var c2 = plane2.z;
var d2 = plane2.w;
var norm1 = J.util.V3.new3 (a1, b1, c1);
var norm2 = J.util.V3.new3 (a2, b2, c2);
var nxn =  new J.util.V3 ();
nxn.cross (norm1, norm2);
var ax = Math.abs (nxn.x);
var ay = Math.abs (nxn.y);
var az = Math.abs (nxn.z);
var x;
var y;
var z;
var diff;
var type = (ax > ay ? (ax > az ? 1 : 3) : ay > az ? 2 : 3);
switch (type) {
case 1:
x = 0;
diff = (b1 * c2 - b2 * c1);
if (Math.abs (diff) < 0.01) return null;
y = (c1 * d2 - c2 * d1) / diff;
z = (b2 * d1 - d2 * b1) / diff;
break;
case 2:
diff = (a1 * c2 - a2 * c1);
if (Math.abs (diff) < 0.01) return null;
x = (c1 * d2 - c2 * d1) / diff;
y = 0;
z = (a2 * d1 - d2 * a1) / diff;
break;
case 3:
default:
diff = (a1 * b2 - a2 * b1);
if (Math.abs (diff) < 0.01) return null;
x = (b1 * d2 - b2 * d1) / diff;
y = (a2 * d1 - d2 * a1) / diff;
z = 0;
}
var list =  new J.util.JmolList ();
list.addLast (J.util.P3.new3 (x, y, z));
nxn.normalize ();
list.addLast (nxn);
return list;
}, "J.util.P4,J.util.P4");
c$.getIntersection = $_M(c$, "getIntersection", 
function (pt1, v, plane, ptRet, tempNorm, vTemp) {
J.util.Measure.getPlaneProjection (pt1, plane, ptRet, tempNorm);
tempNorm.set (plane.x, plane.y, plane.z);
tempNorm.normalize ();
if (v == null) v = J.util.V3.newV (tempNorm);
var l_dot_n = v.dot (tempNorm);
if (Math.abs (l_dot_n) < 0.01) return null;
vTemp.setT (ptRet);
vTemp.sub (pt1);
ptRet.scaleAdd2 (vTemp.dot (tempNorm) / l_dot_n, v, pt1);
return ptRet;
}, "J.util.P3,J.util.V3,J.util.P4,J.util.P3,J.util.V3,J.util.V3");
Clazz.defineStatics (c$,
"radiansPerDegree", (0.017453292519943295));
});
