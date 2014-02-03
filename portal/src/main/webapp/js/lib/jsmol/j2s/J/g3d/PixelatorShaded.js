Clazz.declarePackage ("J.g3d");
Clazz.load (["J.g3d.Pixelator"], "J.g3d.PixelatorShaded", null, function () {
c$ = Clazz.declareType (J.g3d, "PixelatorShaded", J.g3d.Pixelator);
Clazz.overrideMethod (c$, "addPixel", 
function (offset, z, p) {
if (z > this.g.zDepth) return;
if (z <= this.g.zDepth && z >= this.g.zSlab) {
var pR = p & 0xFF;
var pG = (p & 0xFF00) >> 8;
var pB = (p & 0xFF0000) >> 16;
var pA = (p & 0xFF000000);
var f = (this.g.zDepth - z) / (this.g.zDepth - this.g.zSlab);
if (this.g.zShadePower > 1) {
for (var i = 0; i < this.g.zShadePower; i++) f *= f;

}pR = this.g.zShadeR + Clazz.floatToInt (f * (pR - this.g.zShadeR));
pG = this.g.zShadeG + Clazz.floatToInt (f * (pG - this.g.zShadeG));
pB = this.g.zShadeB + Clazz.floatToInt (f * (pB - this.g.zShadeB));
p = (pB << 16) | (pG << 8) | pR | pA;
}this.addPixel1 (offset, z, p);
}, "~N,~N,~N");
});
