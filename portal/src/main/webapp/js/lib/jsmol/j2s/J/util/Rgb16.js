Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.Rgb16", ["J.util.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.rScaled = 0;
this.gScaled = 0;
this.bScaled = 0;
Clazz.instantialize (this, arguments);
}, J.util, "Rgb16");
Clazz.makeConstructor (c$, 
function () {
});
c$.newI = $_M(c$, "newI", 
function (argb) {
var c =  new J.util.Rgb16 ();
c.setInt (argb);
return c;
}, "~N");
$_M(c$, "setInt", 
function (argb) {
this.rScaled = ((argb >> 8) & 0xFF00) | 0x80;
this.gScaled = ((argb) & 0xFF00) | 0x80;
this.bScaled = ((argb << 8) & 0xFF00) | 0x80;
}, "~N");
$_M(c$, "setRgb", 
function (other) {
this.rScaled = other.rScaled;
this.gScaled = other.gScaled;
this.bScaled = other.bScaled;
}, "J.util.Rgb16");
$_M(c$, "diffDiv", 
function (rgb16A, rgb16B, divisor) {
this.rScaled = Clazz.doubleToInt ((rgb16A.rScaled - rgb16B.rScaled) / divisor);
this.gScaled = Clazz.doubleToInt ((rgb16A.gScaled - rgb16B.gScaled) / divisor);
this.bScaled = Clazz.doubleToInt ((rgb16A.bScaled - rgb16B.bScaled) / divisor);
}, "J.util.Rgb16,J.util.Rgb16,~N");
$_M(c$, "setAndIncrement", 
function (base, other) {
this.rScaled = base.rScaled;
base.rScaled += other.rScaled;
this.gScaled = base.gScaled;
base.gScaled += other.gScaled;
this.bScaled = base.bScaled;
base.bScaled += other.bScaled;
}, "J.util.Rgb16,J.util.Rgb16");
$_M(c$, "getArgb", 
function () {
return (0xFF000000 | ((this.rScaled << 8) & 0x00FF0000) | (this.gScaled & 0x0000FF00) | (this.bScaled >> 8));
});
Clazz.overrideMethod (c$, "toString", 
function () {
return  new J.util.SB ().append ("Rgb16(").appendI (this.rScaled).appendC (',').appendI (this.gScaled).appendC (',').appendI (this.bScaled).append (" -> ").appendI ((this.rScaled >> 8) & 0xFF).appendC (',').appendI ((this.gScaled >> 8) & 0xFF).appendC (',').appendI ((this.bScaled >> 8) & 0xFF).appendC (')').toString ();
});
});
