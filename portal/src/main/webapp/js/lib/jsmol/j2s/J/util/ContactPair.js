Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.ContactPair", ["java.lang.Float", "J.script.T", "J.util.P3", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.radii = null;
this.vdws = null;
this.myAtoms = null;
this.pt = null;
this.volume = 0;
this.vdwVolume = 0;
this.score = 0;
this.d = 0;
this.chord = 0;
this.contactType = 0;
this.xVdwClash = NaN;
this.oldType = 0;
Clazz.instantialize (this, arguments);
}, J.util, "ContactPair");
Clazz.prepareFields (c$, function () {
this.radii =  Clazz.newFloatArray (2, 0);
this.vdws =  Clazz.newFloatArray (2, 0);
this.myAtoms =  new Array (2);
});
Clazz.makeConstructor (c$, 
function (atoms, i1, i2, R, r, vdwA, vdwB) {
this.radii[0] = R;
this.radii[1] = r;
this.vdws[0] = vdwA;
this.vdws[1] = vdwB;
this.myAtoms[0] = atoms[i1];
this.myAtoms[1] = atoms[i2];
var v = J.util.V3.newVsub (this.myAtoms[1], this.myAtoms[0]);
this.d = v.length ();
var f = (R - r + this.d) / (2 * this.d);
this.pt =  new J.util.P3 ();
this.pt.scaleAdd2 (f, v, this.myAtoms[0]);
this.score = this.d - vdwA - vdwB;
this.contactType = (this.score < 0 ? 1073741881 : 1649412120);
if (this.score < 0) {
this.radii[0] = R = vdwA;
this.radii[1] = r = vdwB;
}this.getVolume ();
}, "~A,~N,~N,~N,~N,~N,~N");
$_M(c$, "getVolume", 
($fz = function () {
var R = this.radii[0];
var r = this.radii[1];
this.volume = (R + r - this.d);
this.volume *= 3.141592653589793 * this.volume * (this.d * this.d + 2 * this.d * r - 3 * r * r + 2 * this.d * R + 6 * r * R - 3 * R * R) / 12 / this.d;
this.vdwVolume = (this.score > 0 ? -this.volume : this.volume);
var a = (this.d * this.d - r * r + R * R);
this.chord = Math.sqrt (4 * this.d * this.d * R * R - a * a) / this.d;
}, $fz.isPrivate = true, $fz));
$_M(c$, "setForVdwClash", 
function (isVdw) {
if (Float.isNaN (this.xVdwClash)) return false;
if (isVdw) {
this.oldType = this.contactType;
this.contactType = 1649412120;
this.radii[0] = this.vdws[0] + this.xVdwClash;
this.radii[1] = this.vdws[1] + this.xVdwClash;
} else {
this.contactType = this.oldType;
this.radii[0] = this.vdws[0];
this.radii[1] = this.vdws[1];
}this.getVolume ();
return true;
}, "~B");
$_M(c$, "switchAtoms", 
function () {
var atom = this.myAtoms[0];
this.myAtoms[0] = this.myAtoms[1];
this.myAtoms[1] = atom;
var r = this.radii[0];
this.radii[0] = this.radii[1];
this.radii[1] = r;
r = this.vdws[0];
this.vdws[0] = this.vdws[1];
this.vdws[1] = r;
});
Clazz.overrideMethod (c$, "toString", 
function () {
return "type=" + J.script.T.nameOf (this.contactType) + " " + this.myAtoms[0] + " " + this.myAtoms[1] + " dAB=" + this.d + " score=" + this.score + " chord=" + this.chord + " volume=" + this.volume;
});
});
