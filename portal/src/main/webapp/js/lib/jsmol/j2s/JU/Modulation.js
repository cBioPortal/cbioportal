Clazz.declarePackage ("JU");
Clazz.load (null, "JU.Modulation", ["java.lang.Float", "java.util.Hashtable", "JU.Escape", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.qCoefs = null;
this.a1 = 0;
this.a2 = 0;
this.center = 0;
this.left = 0;
this.right = 0;
this.axis = '\0';
this.type = '\0';
this.params = null;
this.utens = null;
Clazz.instantialize (this, arguments);
}, JU, "Modulation");
Clazz.makeConstructor (c$, 
function (axis, type, params, utens, qCoefs) {
if (JU.Logger.debuggingHigh) JU.Logger.debug ("MOD create " + JU.Escape.e (qCoefs) + " axis=" + axis + " type=" + type + " params=" + JU.Escape.e (params) + " utens=" + utens);
this.axis = axis;
this.type = type;
this.utens = utens;
this.params = params;
this.qCoefs = qCoefs;
switch (type) {
case 'f':
case 'o':
case 'u':
this.a1 = params[0];
this.a2 = params[1];
break;
case 's':
case 'c':
this.center = params[0];
var width = params[1];
if (width > 1) width = 1;
this.left = this.center - width / 2;
this.right = this.center + width / 2;
if (this.left < 0) this.left += 1;
if (this.right > 1) this.right -= 1;
if (this.left >= this.right && this.left - this.right < 0.01) this.left = this.right + 0.01;
this.a1 = 2 * params[2] / params[1];
break;
}
}, "~S,~S,~A,~S,~A");
Clazz.defineMethod (c$, "apply", 
function (ms, t) {
var v = 0;
var nt = 0;
for (var i = this.qCoefs.length; --i >= 0; ) nt += this.qCoefs[i] * t[i][0];

switch (this.type) {
case 'f':
case 'o':
case 'u':
var theta = 6.283185307179586 * nt;
if (this.a1 != 0) v += this.a1 * Math.sin (theta);
if (this.a2 != 0) v += this.a2 * Math.cos (theta);
if (JU.Logger.debuggingHigh) JU.Logger.info ("MOD " + ms.id + " " + JU.Escape.e (this.qCoefs) + " axis=" + this.axis + " v=" + v + " csin,ccos=" + this.a1 + "," + this.a2 + " / theta=" + theta);
break;
case 'c':
nt -= Math.floor (nt);
ms.vOcc = (this.range (nt) ? 1 : 0);
ms.vOcc0 = NaN;
return;
case 's':
nt -= Math.floor (nt);
if (!this.range (nt)) return;
if (this.left > this.right) {
if (nt < this.left && this.left < this.center) nt += 1;
 else if (nt > this.right && this.right > this.center) nt -= 1;
}v = this.a1 * (nt - this.center);
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
}, "JU.ModulationSet,~A");
Clazz.defineMethod (c$, "range", 
 function (x4) {
return (this.left < this.right ? this.left <= x4 && x4 <= this.right : this.left <= x4 || x4 <= this.right);
}, "~N");
Clazz.defineMethod (c$, "getInfo", 
function () {
var info =  new java.util.Hashtable ();
info.put ("type", "" + this.type + this.axis);
info.put ("params", this.params);
info.put ("qCoefs", this.qCoefs);
if (this.utens != null) info.put ("Utens", this.utens);
return info;
});
Clazz.defineStatics (c$,
"TWOPI", 6.283185307179586,
"TYPE_DISP_FOURIER", 'f',
"TYPE_DISP_SAWTOOTH", 's',
"TYPE_OCC_FOURIER", 'o',
"TYPE_OCC_CRENEL", 'c',
"TYPE_U_FOURIER", 'u');
});
