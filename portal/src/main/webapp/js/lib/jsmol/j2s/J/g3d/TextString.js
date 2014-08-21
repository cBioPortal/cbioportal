Clazz.declarePackage ("J.g3d");
Clazz.load (["JU.P3i"], "J.g3d.TextString", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.text = null;
this.font = null;
this.argb = 0;
this.bgargb = 0;
Clazz.instantialize (this, arguments);
}, J.g3d, "TextString", JU.P3i);
Clazz.defineMethod (c$, "setText", 
function (text, font, argb, bgargb, x, y, z) {
this.text = text;
this.font = font;
this.argb = argb;
this.bgargb = bgargb;
this.x = x;
this.y = y;
this.z = z;
}, "~S,javajs.awt.Font,~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "toString", 
function () {
return Clazz.superCall (this, J.g3d.TextString, "toString", []) + " " + this.text;
});
});
