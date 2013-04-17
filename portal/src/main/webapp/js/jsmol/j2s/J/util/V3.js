Clazz.declarePackage ("J.util");
Clazz.load (["J.util.Tuple3f"], "J.util.V3", null, function () {
c$ = Clazz.declareType (J.util, "V3", J.util.Tuple3f);
c$.newV = $_M(c$, "newV", 
function (t) {
var v =  new J.util.V3 ();
v.x = t.x;
v.y = t.y;
v.z = t.z;
return v;
}, "J.util.Tuple3f");
c$.new3 = $_M(c$, "new3", 
function (x, y, z) {
var v =  new J.util.V3 ();
v.x = x;
v.y = y;
v.z = z;
return v;
}, "~N,~N,~N");
$_M(c$, "lengthSquared", 
function () {
return this.x * this.x + this.y * this.y + this.z * this.z;
});
$_M(c$, "length", 
function () {
return Math.sqrt (this.lengthSquared ());
});
$_M(c$, "cross", 
function (v1, v2) {
this.set (v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);
}, "J.util.V3,J.util.V3");
$_M(c$, "dot", 
function (v) {
return this.x * v.x + this.y * v.y + this.z * v.z;
}, "J.util.V3");
$_M(c$, "normalize", 
function () {
var d = this.length ();
this.x /= d;
this.y /= d;
this.z /= d;
});
$_M(c$, "angle", 
function (v1) {
var xx = this.y * v1.z - this.z * v1.y;
var yy = this.z * v1.x - this.x * v1.z;
var zz = this.x * v1.y - this.y * v1.x;
var cross = Math.sqrt (xx * xx + yy * yy + zz * zz);
return Math.abs (Math.atan2 (cross, this.dot (v1)));
}, "J.util.V3");
});
