Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.Tuple3d", ["java.lang.Double"], function () {
c$ = Clazz.decorateAsClass (function () {
this.x = 0;
this.y = 0;
this.z = 0;
Clazz.instantialize (this, arguments);
}, J.util, "Tuple3d", null, java.io.Serializable);
Clazz.makeConstructor (c$, 
function () {
});
$_M(c$, "set", 
function (x, y, z) {
this.x = x;
this.y = y;
this.z = z;
}, "~N,~N,~N");
$_M(c$, "setA", 
function (t) {
this.x = t[0];
this.y = t[1];
this.z = t[2];
}, "~A");
$_M(c$, "setT", 
function (t1) {
this.x = t1.x;
this.y = t1.y;
this.z = t1.z;
}, "J.util.Tuple3d");
$_M(c$, "add2", 
function (t1, t2) {
this.x = t1.x + t2.x;
this.y = t1.y + t2.y;
this.z = t1.z + t2.z;
}, "J.util.Tuple3d,J.util.Tuple3d");
$_M(c$, "add", 
function (t1) {
this.x += t1.x;
this.y += t1.y;
this.z += t1.z;
}, "J.util.Tuple3d");
$_M(c$, "sub2", 
function (t1, t2) {
this.x = t1.x - t2.x;
this.y = t1.y - t2.y;
this.z = t1.z - t2.z;
}, "J.util.Tuple3d,J.util.Tuple3d");
$_M(c$, "sub", 
function (t1) {
this.x -= t1.x;
this.y -= t1.y;
this.z -= t1.z;
}, "J.util.Tuple3d");
$_M(c$, "scale", 
function (s) {
this.x *= s;
this.y *= s;
this.z *= s;
}, "~N");
$_M(c$, "scaleAdd", 
function (s, t1, t2) {
this.x = s * t1.x + t2.x;
this.y = s * t1.y + t2.y;
this.z = s * t1.z + t2.z;
}, "~N,J.util.Tuple3d,J.util.Tuple3d");
Clazz.overrideMethod (c$, "hashCode", 
function () {
var xbits = J.util.Tuple3d.doubleToLongBits0 (this.x);
var ybits = J.util.Tuple3d.doubleToLongBits0 (this.y);
var zbits = J.util.Tuple3d.doubleToLongBits0 (this.z);
return (xbits ^ (xbits >> 32) ^ ybits ^ (ybits >> 32) ^ zbits ^ (zbits >> 32));
});
c$.doubleToLongBits0 = $_M(c$, "doubleToLongBits0", 
function (d) {
return (d == 0 ? 0 : Double.doubleToLongBits (d));
}, "~N");
Clazz.overrideMethod (c$, "equals", 
function (t1) {
if (!(Clazz.instanceOf (t1, J.util.Tuple3d))) return false;
var t2 = t1;
return (this.x == t2.x && this.y == t2.y && this.z == t2.z);
}, "~O");
Clazz.overrideMethod (c$, "toString", 
function () {
return "(" + this.x + ", " + this.y + ", " + this.z + ")";
});
});
