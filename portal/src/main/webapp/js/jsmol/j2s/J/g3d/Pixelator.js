Clazz.declarePackage ("J.g3d");
Clazz.load (null, "J.g3d.Pixelator", ["J.g3d.Graphics3D"], function () {
c$ = Clazz.decorateAsClass (function () {
this.g = null;
Clazz.instantialize (this, arguments);
}, J.g3d, "Pixelator");
Clazz.makeConstructor (c$, 
function (graphics3d) {
this.g = graphics3d;
}, "J.g3d.Graphics3D");
$_M(c$, "clearPixel", 
function (offset, z) {
if (!this.g.$isPass2 && this.g.zbuf[offset] > z) this.g.zbuf[offset] = 2147483647;
}, "~N,~N");
$_M(c$, "addPixel", 
function (offset, z, p) {
this.addPixel1 (offset, z, p);
}, "~N,~N,~N");
$_M(c$, "addPixel1", 
function (offset, z, p) {
if (!this.g.$isPass2) {
this.g.zbuf[offset] = z;
this.g.pbuf[offset] = p;
return;
}var zT = this.g.zbufT[offset];
if (z < zT) {
var argb = this.g.pbufT[offset];
if (!this.g.translucentCoverOnly && argb != 0 && zT - z > this.g.zMargin) J.g3d.Graphics3D.mergeBufferPixel (this.g.pbuf, offset, argb, this.g.bgcolor);
this.g.zbufT[offset] = z;
this.g.pbufT[offset] = p & this.g.translucencyMask;
} else if (z == zT) {
} else if (!this.g.translucentCoverOnly && z - zT > this.g.zMargin) {
J.g3d.Graphics3D.mergeBufferPixel (this.g.pbuf, offset, p & this.g.translucencyMask, this.g.bgcolor);
}}, "~N,~N,~N");
});
