Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.Modulation", ["java.lang.Float", "J.util.Escape", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.qCoefs = null;
this.a1 = 0;
this.a2 = 0;
this.center = 0;
this.left = 0;
this.right = 0;
this.axis = '\0';
this.type = 0;
this.utens = null;
Clazz.instantialize (this, arguments);
}, J.util, "Modulation");
Clazz.makeConstructor (c$, 
function (axis, type, params, utens, qCoefs) {
if (J.util.Logger.debuggingHigh) J.util.Logger.debug ("MOD create " + J.util.Escape.eP (qCoefs) + " axis=" + axis + " type=" + type + " params=" + params + " utens=" + utens);
this.axis = axis;
this.type = type;
this.utens = utens;
this.qCoefs = qCoefs;
switch (type) {
case 0:
case 2:
case 4:
this.a1 = params.x;
this.a2 = params.y;
break;
case 1:
case 3:
this.center = params.x;
var width = params.y;
if (width > 1) width = 1;
this.left = this.center - width / 2;
this.right = this.center + width / 2;
if (this.left < 0) this.left += 1;
if (this.right > 1) this.right -= 1;
if (this.left >= this.right && this.left - this.right < 0.01) this.left = this.right + 0.01;
this.a1 = 2 * params.z / params.y;
break;
}
}, "~S,~N,J.util.P3,~S,J.util.P3");
$_M(c$, "apply", 
function (ms, offset) {
var x = this.qCoefs.dot (ms.x456) + this.qCoefs.x * offset;
var v = 0;
switch (this.type) {
case 0:
case 2:
case 4:
var theta = 6.283185307179586 * x;
if (this.a1 != 0) v += this.a1 * Math.cos (theta);
if (this.a2 != 0) v += this.a2 * Math.sin (theta);
if (J.util.Logger.debuggingHigh) J.util.Logger.debug ("MOD " + ms.id + " " + J.util.Escape.eP (this.qCoefs) + " axis=" + this.axis + " v=" + v + " ccos,csin=" + this.a1 + "," + this.a2 + " / theta=" + theta);
break;
case 3:
x -= Math.floor (x);
ms.vOcc = (this.range (x) ? 1 : 0);
ms.vOcc0 = NaN;
return;
case 1:
x -= Math.floor (x);
if (!this.range (x)) return;
if (this.left > this.right) {
if (x < this.left && this.left < this.center) x += 1;
 else if (x > this.right && this.right > this.center) x -= 1;
}v = this.a1 * (x - this.center);
break;
}
switch (this.axis) {
case 'x':
ms.x += v;
break;
case 'y':
ms.y += v;
break;
case 'z':
ms.z += v;
break;
case 'U':
ms.addUTens (this.utens, v);
break;
default:
if (Float.isNaN (ms.vOcc)) ms.vOcc = 0;
ms.vOcc += v;
}
}, "J.util.ModulationSet,~N");
$_M(c$, "range", 
($fz = function (x4) {
return (this.left < this.right ? this.left <= x4 && x4 <= this.right : this.left <= x4 || x4 <= this.right);
}, $fz.isPrivate = true, $fz), "~N");
Clazz.defineStatics (c$,
"TWOPI", 6.283185307179586,
"TYPE_DISP_FOURIER", 0,
"TYPE_DISP_SAWTOOTH", 1,
"TYPE_OCC_FOURIER", 2,
"TYPE_OCC_CRENEL", 3,
"TYPE_U_FOURIER", 4);
});
