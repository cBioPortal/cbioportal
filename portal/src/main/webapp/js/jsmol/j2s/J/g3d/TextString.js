Clazz.declarePackage ("J.g3d");
Clazz.load (["J.util.P3i"], "J.g3d.TextString", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.text = null;
this.font = null;
this.argb = 0;
this.bgargb = 0;
Clazz.instantialize (this, arguments);
}, J.g3d, "TextString", J.util.P3i);
$_M(c$, "setText", 
function (text, font, argb, bgargb, x, y, z) {
this.text = text;
this.font = font;
this.argb = argb;
this.bgargb = bgargb;
this.x = x;
this.y = y;
this.z = z;
}, "~S,J.util.JmolFont,~N,~N,~N,~N,~N");
$_M(c$, "toString", 
function () {
return Clazz.superCall (this, J.g3d.TextString, "toString", []) + " " + this.text;
});
});
