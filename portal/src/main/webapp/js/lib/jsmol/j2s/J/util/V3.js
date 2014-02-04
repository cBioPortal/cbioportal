Clazz.declarePackage ("J.util");
Clazz.load (["J.util.Tuple3f"], "J.util.V3", null, function () {
c$ = Clazz.declareType (J.util, "V3", J.util.Tuple3f);
c$.newV = $_M(c$, "newV", 
function (t) {
return J.util.V3.new3 (t.x, t.y, t.z);
}, "J.util.Tuple3f");
c$.newVsub = $_M(c$, "newVsub", 
function (t1, t2) {
return J.util.V3.new3 (t1.x - t2.x, t1.y - t2.y, t1.z - t2.z);
}, "J.util.Tuple3f,J.util.Tuple3f");
c$.new3 = $_M(c$, "new3", 
function (x, y, z) {
var v =  new J.util.V3 ();
v.x = x;
v.y = y;
v.z = z;
return v;
}, "~N,~N,~N");
$_M(c$, "cross", 
function (v1, v2) {
this.set (v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);
}, "J.util.V3,J.util.V3");
$_M(c$, "angle", 
function (v1) {
var xx = this.y * v1.z - this.z * v1.y;
var yy = this.z * v1.x - this.x * v1.z;
var zz = this.x * v1.y - this.y * v1.x;
var cross = Math.sqrt (xx * xx + yy * yy + zz * zz);
return Math.abs (Math.atan2 (cross, this.dot (v1)));
}, "J.util.V3");
});
