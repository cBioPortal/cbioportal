Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.Hermite", ["J.util.P3", "$.V3"], function () {
c$ = Clazz.declareType (J.util, "Hermite");
c$.getHermiteList = $_M(c$, "getHermiteList", 
function (tension, p0, p1, p2, p3, p4, list, index0, n, isPt) {
var nPoints = n + 1;
var fnPoints = n - 1;
var x1 = p1.x;
var y1 = p1.y;
var z1 = p1.z;
var x2 = p2.x;
var y2 = p2.y;
var z2 = p2.z;
var xT1 = ((x2 - p0.x) * tension) / 8;
var yT1 = ((y2 - p0.y) * tension) / 8;
var zT1 = ((z2 - p0.z) * tension) / 8;
var xT2 = ((p3.x - x1) * tension) / 8;
var yT2 = ((p3.y - y1) * tension) / 8;
var zT2 = ((p3.z - z1) * tension) / 8;
var xT3 = ((p4.x - x2) * tension) / 8;
var yT3 = ((p4.y - y2) * tension) / 8;
var zT3 = ((p4.z - z2) * tension) / 8;
list[index0] = p1;
for (var i = 0; i < nPoints; i++) {
var s = i / fnPoints;
if (i == nPoints - 1) {
x1 = x2;
y1 = y2;
z1 = z2;
x2 = p3.x;
y2 = p3.y;
z2 = p3.z;
xT1 = xT2;
yT1 = yT2;
zT1 = zT2;
xT2 = xT3;
yT2 = yT3;
zT2 = zT3;
s -= 1;
}var s2 = s * s;
var s3 = s2 * s;
var h1 = 2 * s3 - 3 * s2 + 1;
var h2 = -2 * s3 + 3 * s2;
var h3 = s3 - 2 * s2 + s;
var h4 = s3 - s2;
var x = (h1 * x1 + h2 * x2 + h3 * xT1 + h4 * xT2);
var y = (h1 * y1 + h2 * y2 + h3 * yT1 + h4 * yT2);
var z = (h1 * z1 + h2 * z2 + h3 * zT1 + h4 * zT2);
if (isPt) list[index0 + i] = J.util.P3.new3 (x, y, z);
 else list[index0 + i] = J.util.V3.new3 (x, y, z);
}
}, "~N,J.util.Tuple3f,J.util.Tuple3f,J.util.Tuple3f,J.util.Tuple3f,J.util.Tuple3f,~A,~N,~N,~B");
});
