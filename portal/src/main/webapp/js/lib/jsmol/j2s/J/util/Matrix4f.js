Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.Matrix4f", ["java.lang.ArrayIndexOutOfBoundsException", "J.util.Tuple3f"], function () {
c$ = Clazz.decorateAsClass (function () {
this.m00 = 0;
this.m01 = 0;
this.m02 = 0;
this.m03 = 0;
this.m10 = 0;
this.m11 = 0;
this.m12 = 0;
this.m13 = 0;
this.m20 = 0;
this.m21 = 0;
this.m22 = 0;
this.m23 = 0;
this.m30 = 0;
this.m31 = 0;
this.m32 = 0;
this.m33 = 0;
Clazz.instantialize (this, arguments);
}, J.util, "Matrix4f", null, java.io.Serializable);
c$.newA = $_M(c$, "newA", 
function (v) {
var m =  new J.util.Matrix4f ();
m.m00 = v[0];
m.m01 = v[1];
m.m02 = v[2];
m.m03 = v[3];
m.m10 = v[4];
m.m11 = v[5];
m.m12 = v[6];
m.m13 = v[7];
m.m20 = v[8];
m.m21 = v[9];
m.m22 = v[10];
m.m23 = v[11];
m.m30 = v[12];
m.m31 = v[13];
m.m32 = v[14];
m.m33 = v[15];
return m;
}, "~A");
c$.newM = $_M(c$, "newM", 
function (m1) {
var m =  new J.util.Matrix4f ();
m.m00 = m1.m00;
m.m01 = m1.m01;
m.m02 = m1.m02;
m.m03 = m1.m03;
m.m10 = m1.m10;
m.m11 = m1.m11;
m.m12 = m1.m12;
m.m13 = m1.m13;
m.m20 = m1.m20;
m.m21 = m1.m21;
m.m22 = m1.m22;
m.m23 = m1.m23;
m.m30 = m1.m30;
m.m31 = m1.m31;
m.m32 = m1.m32;
m.m33 = m1.m33;
return m;
}, "J.util.Matrix4f");
c$.newMV = $_M(c$, "newMV", 
function (m1, t) {
var m =  new J.util.Matrix4f ();
m.setMV (m1, t);
return m;
}, "J.util.Matrix3f,J.util.V3");
$_M(c$, "setMV", 
function (m1, t) {
this.setM3 (m1);
this.setTranslation (t);
}, "J.util.Matrix3f,J.util.V3");
$_M(c$, "setAA", 
function (a1) {
this.setFromAxisAngle (a1.x, a1.y, a1.z, a1.angle);
}, "J.util.AxisAngle4f");
$_M(c$, "setFromAxisAngle", 
($fz = function (x, y, z, angle) {
var n = Math.sqrt (x * x + y * y + z * z);
n = 1 / n;
x *= n;
y *= n;
z *= n;
var c = Math.cos (angle);
var s = Math.sin (angle);
var omc = 1.0 - c;
this.m00 = (c + x * x * omc);
this.m11 = (c + y * y * omc);
this.m22 = (c + z * z * omc);
var tmp1 = x * y * omc;
var tmp2 = z * s;
this.m01 = (tmp1 - tmp2);
this.m10 = (tmp1 + tmp2);
tmp1 = x * z * omc;
tmp2 = y * s;
this.m02 = (tmp1 + tmp2);
this.m20 = (tmp1 - tmp2);
tmp1 = y * z * omc;
tmp2 = x * s;
this.m12 = (tmp1 - tmp2);
this.m21 = (tmp1 + tmp2);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
$_M(c$, "setM", 
function (m1) {
this.m00 = m1.m00;
this.m01 = m1.m01;
this.m02 = m1.m02;
this.m03 = m1.m03;
this.m10 = m1.m10;
this.m11 = m1.m11;
this.m12 = m1.m12;
this.m13 = m1.m13;
this.m20 = m1.m20;
this.m21 = m1.m21;
this.m22 = m1.m22;
this.m23 = m1.m23;
this.m30 = m1.m30;
this.m31 = m1.m31;
this.m32 = m1.m32;
this.m33 = m1.m33;
}, "J.util.Matrix4f");
Clazz.overrideMethod (c$, "toString", 
function () {
return "[\n  [" + this.m00 + "\t" + this.m01 + "\t" + this.m02 + "\t" + this.m03 + "]" + "\n  [" + this.m10 + "\t" + this.m11 + "\t" + this.m12 + "\t" + this.m13 + "]" + "\n  [" + this.m20 + "\t" + this.m21 + "\t" + this.m22 + "\t" + this.m23 + "]" + "\n  [" + this.m30 + "\t" + this.m31 + "\t" + this.m32 + "\t" + this.m33 + "] ]";
});
$_M(c$, "setIdentity", 
function () {
this.m00 = 1.0;
this.m01 = 0.0;
this.m02 = 0.0;
this.m03 = 0.0;
this.m10 = 0.0;
this.m11 = 1.0;
this.m12 = 0.0;
this.m13 = 0.0;
this.m20 = 0.0;
this.m21 = 0.0;
this.m22 = 1.0;
this.m23 = 0.0;
this.m30 = 0.0;
this.m31 = 0.0;
this.m32 = 0.0;
this.m33 = 1.0;
});
$_M(c$, "setElement", 
function (row, column, value) {
if (row == 0) if (column == 0) this.m00 = value;
 else if (column == 1) this.m01 = value;
 else if (column == 2) this.m02 = value;
 else if (column == 3) this.m03 = value;
 else throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
 else if (row == 1) if (column == 0) this.m10 = value;
 else if (column == 1) this.m11 = value;
 else if (column == 2) this.m12 = value;
 else if (column == 3) this.m13 = value;
 else throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
 else if (row == 2) if (column == 0) this.m20 = value;
 else if (column == 1) this.m21 = value;
 else if (column == 2) this.m22 = value;
 else if (column == 3) this.m23 = value;
 else throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
 else if (row == 3) if (column == 0) this.m30 = value;
 else if (column == 1) this.m31 = value;
 else if (column == 2) this.m32 = value;
 else if (column == 3) this.m33 = value;
 else throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
 else throw  new ArrayIndexOutOfBoundsException ("row must be 0 to 2 and is " + row);
}, "~N,~N,~N");
$_M(c$, "getElement", 
function (row, column) {
if (row == 0) if (column == 0) return this.m00;
 else if (column == 1) return this.m01;
 else if (column == 2) return this.m02;
 else if (column == 3) return this.m03;
 else throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
 else if (row == 1) if (column == 0) return this.m10;
 else if (column == 1) return this.m11;
 else if (column == 2) return this.m12;
 else if (column == 3) return this.m13;
 else throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
 else if (row == 2) if (column == 0) return this.m20;
 else if (column == 1) return this.m21;
 else if (column == 2) return this.m22;
 else if (column == 3) return this.m23;
 else throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
 else if (row == 3) if (column == 0) return this.m30;
 else if (column == 1) return this.m31;
 else if (column == 2) return this.m32;
 else if (column == 3) return this.m33;
 else throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
 else throw  new ArrayIndexOutOfBoundsException ("row must be 0 to 3 and is " + row);
}, "~N,~N");
$_M(c$, "get", 
function (trans) {
trans.x = this.m03;
trans.y = this.m13;
trans.z = this.m23;
}, "J.util.V3");
$_M(c$, "getRotationScale", 
function (m1) {
m1.m00 = this.m00;
m1.m01 = this.m01;
m1.m02 = this.m02;
m1.m10 = this.m10;
m1.m11 = this.m11;
m1.m12 = this.m12;
m1.m20 = this.m20;
m1.m21 = this.m21;
m1.m22 = this.m22;
}, "J.util.Matrix3f");
$_M(c$, "setRotationScale", 
function (m1) {
this.m00 = m1.m00;
this.m01 = m1.m01;
this.m02 = m1.m02;
this.m10 = m1.m10;
this.m11 = m1.m11;
this.m12 = m1.m12;
this.m20 = m1.m20;
this.m21 = m1.m21;
this.m22 = m1.m22;
}, "J.util.Matrix3f");
$_M(c$, "setRow", 
function (row, v) {
if (row == 0) {
this.m00 = v[0];
this.m01 = v[1];
this.m02 = v[2];
this.m03 = v[3];
} else if (row == 1) {
this.m10 = v[0];
this.m11 = v[1];
this.m12 = v[2];
this.m13 = v[3];
} else if (row == 2) {
this.m20 = v[0];
this.m21 = v[1];
this.m22 = v[2];
this.m23 = v[3];
} else if (row == 3) {
this.m30 = v[0];
this.m31 = v[1];
this.m32 = v[2];
this.m33 = v[3];
} else {
throw  new ArrayIndexOutOfBoundsException ("row must be 0 to 3 and is " + row);
}}, "~N,~A");
$_M(c$, "getRow", 
function (row, v) {
if (row == 0) {
v[0] = this.m00;
v[1] = this.m01;
v[2] = this.m02;
v[3] = this.m03;
} else if (row == 1) {
v[0] = this.m10;
v[1] = this.m11;
v[2] = this.m12;
v[3] = this.m13;
} else if (row == 2) {
v[0] = this.m20;
v[1] = this.m21;
v[2] = this.m22;
v[3] = this.m23;
} else if (row == 3) {
v[0] = this.m30;
v[1] = this.m31;
v[2] = this.m32;
v[3] = this.m33;
} else {
throw  new ArrayIndexOutOfBoundsException ("row must be 0 to 3 and is " + row);
}}, "~N,~A");
$_M(c$, "setColumn4", 
function (column, x, y, z, w) {
if (column == 0) {
this.m00 = x;
this.m10 = y;
this.m20 = z;
this.m30 = w;
} else if (column == 1) {
this.m01 = x;
this.m11 = y;
this.m21 = z;
this.m31 = w;
} else if (column == 2) {
this.m02 = x;
this.m12 = y;
this.m22 = z;
this.m32 = w;
} else if (column == 3) {
this.m03 = x;
this.m13 = y;
this.m23 = z;
this.m33 = w;
} else {
throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
}}, "~N,~N,~N,~N,~N");
$_M(c$, "setColumn", 
function (column, v) {
if (column == 0) {
this.m00 = v[0];
this.m10 = v[1];
this.m20 = v[2];
this.m30 = v[3];
} else if (column == 1) {
this.m01 = v[0];
this.m11 = v[1];
this.m21 = v[2];
this.m31 = v[3];
} else if (column == 2) {
this.m02 = v[0];
this.m12 = v[1];
this.m22 = v[2];
this.m32 = v[3];
} else if (column == 3) {
this.m03 = v[0];
this.m13 = v[1];
this.m23 = v[2];
this.m33 = v[3];
} else {
throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
}}, "~N,~A");
$_M(c$, "getColumn", 
function (column, v) {
if (column == 0) {
v[0] = this.m00;
v[1] = this.m10;
v[2] = this.m20;
v[3] = this.m30;
} else if (column == 1) {
v[0] = this.m01;
v[1] = this.m11;
v[2] = this.m21;
v[3] = this.m31;
} else if (column == 2) {
v[0] = this.m02;
v[1] = this.m12;
v[2] = this.m22;
v[3] = this.m32;
} else if (column == 3) {
v[0] = this.m03;
v[1] = this.m13;
v[2] = this.m23;
v[3] = this.m33;
} else {
throw  new ArrayIndexOutOfBoundsException ("column must be 0 to 3 and is " + column);
}}, "~N,~A");
$_M(c$, "sub", 
function (m1) {
this.m00 -= m1.m00;
this.m01 -= m1.m01;
this.m02 -= m1.m02;
this.m03 -= m1.m03;
this.m10 -= m1.m10;
this.m11 -= m1.m11;
this.m12 -= m1.m12;
this.m13 -= m1.m13;
this.m20 -= m1.m20;
this.m21 -= m1.m21;
this.m22 -= m1.m22;
this.m23 -= m1.m23;
this.m30 -= m1.m30;
this.m31 -= m1.m31;
this.m32 -= m1.m32;
this.m33 -= m1.m33;
}, "J.util.Matrix4f");
$_M(c$, "transpose", 
function () {
var tmp = this.m01;
this.m01 = this.m10;
this.m10 = tmp;
tmp = this.m02;
this.m02 = this.m20;
this.m20 = tmp;
tmp = this.m03;
this.m03 = this.m30;
this.m30 = tmp;
tmp = this.m12;
this.m12 = this.m21;
this.m21 = tmp;
tmp = this.m13;
this.m13 = this.m31;
this.m31 = tmp;
tmp = this.m23;
this.m23 = this.m32;
this.m32 = tmp;
});
$_M(c$, "invertM", 
function (m1) {
this.setM (m1);
this.invert ();
}, "J.util.Matrix4f");
$_M(c$, "invert", 
function () {
var s = this.determinant ();
if (s == 0.0) return;
s = 1 / s;
this.set (this.m11 * (this.m22 * this.m33 - this.m23 * this.m32) + this.m12 * (this.m23 * this.m31 - this.m21 * this.m33) + this.m13 * (this.m21 * this.m32 - this.m22 * this.m31), this.m21 * (this.m02 * this.m33 - this.m03 * this.m32) + this.m22 * (this.m03 * this.m31 - this.m01 * this.m33) + this.m23 * (this.m01 * this.m32 - this.m02 * this.m31), this.m31 * (this.m02 * this.m13 - this.m03 * this.m12) + this.m32 * (this.m03 * this.m11 - this.m01 * this.m13) + this.m33 * (this.m01 * this.m12 - this.m02 * this.m11), this.m01 * (this.m13 * this.m22 - this.m12 * this.m23) + this.m02 * (this.m11 * this.m23 - this.m13 * this.m21) + this.m03 * (this.m12 * this.m21 - this.m11 * this.m22), this.m12 * (this.m20 * this.m33 - this.m23 * this.m30) + this.m13 * (this.m22 * this.m30 - this.m20 * this.m32) + this.m10 * (this.m23 * this.m32 - this.m22 * this.m33), this.m22 * (this.m00 * this.m33 - this.m03 * this.m30) + this.m23 * (this.m02 * this.m30 - this.m00 * this.m32) + this.m20 * (this.m03 * this.m32 - this.m02 * this.m33), this.m32 * (this.m00 * this.m13 - this.m03 * this.m10) + this.m33 * (this.m02 * this.m10 - this.m00 * this.m12) + this.m30 * (this.m03 * this.m12 - this.m02 * this.m13), this.m02 * (this.m13 * this.m20 - this.m10 * this.m23) + this.m03 * (this.m10 * this.m22 - this.m12 * this.m20) + this.m00 * (this.m12 * this.m23 - this.m13 * this.m22), this.m13 * (this.m20 * this.m31 - this.m21 * this.m30) + this.m10 * (this.m21 * this.m33 - this.m23 * this.m31) + this.m11 * (this.m23 * this.m30 - this.m20 * this.m33), this.m23 * (this.m00 * this.m31 - this.m01 * this.m30) + this.m20 * (this.m01 * this.m33 - this.m03 * this.m31) + this.m21 * (this.m03 * this.m30 - this.m00 * this.m33), this.m33 * (this.m00 * this.m11 - this.m01 * this.m10) + this.m30 * (this.m01 * this.m13 - this.m03 * this.m11) + this.m31 * (this.m03 * this.m10 - this.m00 * this.m13), this.m03 * (this.m11 * this.m20 - this.m10 * this.m21) + this.m00 * (this.m13 * this.m21 - this.m11 * this.m23) + this.m01 * (this.m10 * this.m23 - this.m13 * this.m20), this.m10 * (this.m22 * this.m31 - this.m21 * this.m32) + this.m11 * (this.m20 * this.m32 - this.m22 * this.m30) + this.m12 * (this.m21 * this.m30 - this.m20 * this.m31), this.m20 * (this.m02 * this.m31 - this.m01 * this.m32) + this.m21 * (this.m00 * this.m32 - this.m02 * this.m30) + this.m22 * (this.m01 * this.m30 - this.m00 * this.m31), this.m30 * (this.m02 * this.m11 - this.m01 * this.m12) + this.m31 * (this.m00 * this.m12 - this.m02 * this.m10) + this.m32 * (this.m01 * this.m10 - this.m00 * this.m11), this.m00 * (this.m11 * this.m22 - this.m12 * this.m21) + this.m01 * (this.m12 * this.m20 - this.m10 * this.m22) + this.m02 * (this.m10 * this.m21 - this.m11 * this.m20));
this.mul (s);
});
$_M(c$, "determinant", 
function () {
return (this.m00 * this.m11 - this.m01 * this.m10) * (this.m22 * this.m33 - this.m23 * this.m32) - (this.m00 * this.m12 - this.m02 * this.m10) * (this.m21 * this.m33 - this.m23 * this.m31) + (this.m00 * this.m13 - this.m03 * this.m10) * (this.m21 * this.m32 - this.m22 * this.m31) + (this.m01 * this.m12 - this.m02 * this.m11) * (this.m20 * this.m33 - this.m23 * this.m30) - (this.m01 * this.m13 - this.m03 * this.m11) * (this.m20 * this.m32 - this.m22 * this.m30) + (this.m02 * this.m13 - this.m03 * this.m12) * (this.m20 * this.m31 - this.m21 * this.m30);
});
$_M(c$, "setM3", 
function (m1) {
this.m00 = m1.m00;
this.m01 = m1.m01;
this.m02 = m1.m02;
this.m03 = 0.0;
this.m10 = m1.m10;
this.m11 = m1.m11;
this.m12 = m1.m12;
this.m13 = 0.0;
this.m20 = m1.m20;
this.m21 = m1.m21;
this.m22 = m1.m22;
this.m23 = 0.0;
this.m30 = 0.0;
this.m31 = 0.0;
this.m32 = 0.0;
this.m33 = 1.0;
}, "J.util.Matrix3f");
$_M(c$, "setA", 
function (m, i) {
this.m00 = m[i++];
this.m01 = m[i++];
this.m02 = m[i++];
this.m03 = m[i++];
this.m10 = m[i++];
this.m11 = m[i++];
this.m12 = m[i++];
this.m13 = m[i++];
this.m20 = m[i++];
this.m21 = m[i++];
this.m22 = m[i++];
this.m23 = m[i++];
this.m30 = m[i++];
this.m31 = m[i++];
this.m32 = m[i++];
this.m33 = m[i++];
}, "~A,~N");
$_M(c$, "setTranslation", 
function (trans) {
this.m03 = trans.x;
this.m13 = trans.y;
this.m23 = trans.z;
}, "J.util.V3");
$_M(c$, "rotX", 
function (angle) {
var c = Math.cos (angle);
var s = Math.sin (angle);
this.m00 = 1.0;
this.m01 = 0.0;
this.m02 = 0.0;
this.m03 = 0.0;
this.m10 = 0.0;
this.m11 = c;
this.m12 = -s;
this.m13 = 0.0;
this.m20 = 0.0;
this.m21 = s;
this.m22 = c;
this.m23 = 0.0;
this.m30 = 0.0;
this.m31 = 0.0;
this.m32 = 0.0;
this.m33 = 1.0;
}, "~N");
$_M(c$, "rotY", 
function (angle) {
var c = Math.cos (angle);
var s = Math.sin (angle);
this.m00 = c;
this.m01 = 0.0;
this.m02 = s;
this.m03 = 0.0;
this.m10 = 0.0;
this.m11 = 1.0;
this.m12 = 0.0;
this.m13 = 0.0;
this.m20 = -s;
this.m21 = 0.0;
this.m22 = c;
this.m23 = 0.0;
this.m30 = 0.0;
this.m31 = 0.0;
this.m32 = 0.0;
this.m33 = 1.0;
}, "~N");
$_M(c$, "rotZ", 
function (angle) {
var c = Math.cos (angle);
var s = Math.sin (angle);
this.m00 = c;
this.m01 = -s;
this.m02 = 0.0;
this.m03 = 0.0;
this.m10 = s;
this.m11 = c;
this.m12 = 0.0;
this.m13 = 0.0;
this.m20 = 0.0;
this.m21 = 0.0;
this.m22 = 1.0;
this.m23 = 0.0;
this.m30 = 0.0;
this.m31 = 0.0;
this.m32 = 0.0;
this.m33 = 1.0;
}, "~N");
$_M(c$, "mul", 
($fz = function (scalar) {
this.m00 *= scalar;
this.m01 *= scalar;
this.m02 *= scalar;
this.m03 *= scalar;
this.m10 *= scalar;
this.m11 *= scalar;
this.m12 *= scalar;
this.m13 *= scalar;
this.m20 *= scalar;
this.m21 *= scalar;
this.m22 *= scalar;
this.m23 *= scalar;
this.m30 *= scalar;
this.m31 *= scalar;
this.m32 *= scalar;
this.m33 *= scalar;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "mulM4", 
function (m1) {
this.mul2 (this, m1);
}, "J.util.Matrix4f");
$_M(c$, "mul2", 
function (m1, m2) {
this.set (m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20 + m1.m03 * m2.m30, m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21 + m1.m03 * m2.m31, m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22 + m1.m03 * m2.m32, m1.m00 * m2.m03 + m1.m01 * m2.m13 + m1.m02 * m2.m23 + m1.m03 * m2.m33, m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20 + m1.m13 * m2.m30, m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21 + m1.m13 * m2.m31, m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22 + m1.m13 * m2.m32, m1.m10 * m2.m03 + m1.m11 * m2.m13 + m1.m12 * m2.m23 + m1.m13 * m2.m33, m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20 + m1.m23 * m2.m30, m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21 + m1.m23 * m2.m31, m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22 + m1.m23 * m2.m32, m1.m20 * m2.m03 + m1.m21 * m2.m13 + m1.m22 * m2.m23 + m1.m23 * m2.m33, m1.m30 * m2.m00 + m1.m31 * m2.m10 + m1.m32 * m2.m20 + m1.m33 * m2.m30, m1.m30 * m2.m01 + m1.m31 * m2.m11 + m1.m32 * m2.m21 + m1.m33 * m2.m31, m1.m30 * m2.m02 + m1.m31 * m2.m12 + m1.m32 * m2.m22 + m1.m33 * m2.m32, m1.m30 * m2.m03 + m1.m31 * m2.m13 + m1.m32 * m2.m23 + m1.m33 * m2.m33);
}, "J.util.Matrix4f,J.util.Matrix4f");
Clazz.overrideMethod (c$, "equals", 
function (o) {
if (!(Clazz.instanceOf (o, J.util.Matrix4f))) return false;
var m = o;
return (this.m00 == m.m00 && this.m01 == m.m01 && this.m02 == m.m02 && this.m03 == m.m03 && this.m10 == m.m10 && this.m11 == m.m11 && this.m12 == m.m12 && this.m13 == m.m13 && this.m20 == m.m20 && this.m21 == m.m21 && this.m22 == m.m22 && this.m23 == m.m23 && this.m30 == m.m30 && this.m31 == m.m31 && this.m32 == m.m32 && this.m33 == m.m33);
}, "~O");
Clazz.overrideMethod (c$, "hashCode", 
function () {
return J.util.Tuple3f.floatToIntBits0 (this.m00) ^ J.util.Tuple3f.floatToIntBits0 (this.m01) ^ J.util.Tuple3f.floatToIntBits0 (this.m02) ^ J.util.Tuple3f.floatToIntBits0 (this.m03) ^ J.util.Tuple3f.floatToIntBits0 (this.m10) ^ J.util.Tuple3f.floatToIntBits0 (this.m11) ^ J.util.Tuple3f.floatToIntBits0 (this.m12) ^ J.util.Tuple3f.floatToIntBits0 (this.m13) ^ J.util.Tuple3f.floatToIntBits0 (this.m20) ^ J.util.Tuple3f.floatToIntBits0 (this.m21) ^ J.util.Tuple3f.floatToIntBits0 (this.m22) ^ J.util.Tuple3f.floatToIntBits0 (this.m23) ^ J.util.Tuple3f.floatToIntBits0 (this.m30) ^ J.util.Tuple3f.floatToIntBits0 (this.m31) ^ J.util.Tuple3f.floatToIntBits0 (this.m32) ^ J.util.Tuple3f.floatToIntBits0 (this.m33);
});
$_M(c$, "transformT2", 
function (vec, vecOut) {
vecOut.set (this.m00 * vec.x + this.m01 * vec.y + this.m02 * vec.z + this.m03 * vec.w, this.m10 * vec.x + this.m11 * vec.y + this.m12 * vec.z + this.m13 * vec.w, this.m20 * vec.x + this.m21 * vec.y + this.m22 * vec.z + this.m23 * vec.w, this.m30 * vec.x + this.m31 * vec.y + this.m32 * vec.z + this.m33 * vec.w);
}, "J.util.Tuple4f,J.util.Tuple4f");
$_M(c$, "transform4", 
function (vec) {
this.transformT2 (vec, vec);
}, "J.util.Tuple4f");
$_M(c$, "transform2", 
function (point, pointOut) {
try {
pointOut.set (this.m00 * point.x + this.m01 * point.y + this.m02 * point.z + this.m03, this.m10 * point.x + this.m11 * point.y + this.m12 * point.z + this.m13, this.m20 * point.x + this.m21 * point.y + this.m22 * point.z + this.m23);
} catch (e) {
if (Clazz.exceptionOf (e, NullPointerException)) {
} else {
throw e;
}
}
}, "J.util.Tuple3f,J.util.Tuple3f");
$_M(c$, "transform", 
function (point) {
this.transform2 (point, point);
}, "J.util.Tuple3f");
$_M(c$, "transformV2", 
function (normal, normalOut) {
normalOut.set (this.m00 * normal.x + this.m01 * normal.y + this.m02 * normal.z, this.m10 * normal.x + this.m11 * normal.y + this.m12 * normal.z, this.m20 * normal.x + this.m21 * normal.y + this.m22 * normal.z);
}, "J.util.V3,J.util.V3");
$_M(c$, "transformV", 
function (normal) {
this.transformV2 (normal, normal);
}, "J.util.V3");
$_M(c$, "setZero", 
function () {
this.m00 = 0.0;
this.m01 = 0.0;
this.m02 = 0.0;
this.m03 = 0.0;
this.m10 = 0.0;
this.m11 = 0.0;
this.m12 = 0.0;
this.m13 = 0.0;
this.m20 = 0.0;
this.m21 = 0.0;
this.m22 = 0.0;
this.m23 = 0.0;
this.m30 = 0.0;
this.m31 = 0.0;
this.m32 = 0.0;
this.m33 = 0.0;
});
$_M(c$, "set", 
($fz = function (m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33) {
this.m00 = m00;
this.m01 = m01;
this.m02 = m02;
this.m03 = m03;
this.m10 = m10;
this.m11 = m11;
this.m12 = m12;
this.m13 = m13;
this.m20 = m20;
this.m21 = m21;
this.m22 = m22;
this.m23 = m23;
this.m30 = m30;
this.m31 = m31;
this.m32 = m32;
this.m33 = m33;
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N,~N,~N,~N,~N,~N,~N,~N,~N,~N,~N,~N");
});
