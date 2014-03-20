Clazz.declarePackage ("J.shapespecial");
Clazz.load (null, "J.shapespecial.Dipole", ["J.util.C", "$.Escape", "$.P3", "$.SB", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.thisID = "";
this.mad = 0;
this.colix = 0;
this.type = 0;
this.origin = null;
this.center = null;
this.vector = null;
this.dipoleInfo = "";
this.dipoleValue = 0;
this.isUserValue = false;
this.offsetSide = 0;
this.offsetAngstroms = 0;
this.offsetPercent = 0;
this.visibilityFlags = 0;
this.modelIndex = 0;
this.visible = false;
this.noCross = false;
this.haveAtoms = false;
this.isValid = false;
this.atoms = null;
this.coords = null;
this.bond = null;
Clazz.instantialize (this, arguments);
}, J.shapespecial, "Dipole");
Clazz.prepareFields (c$, function () {
this.atoms =  new Array (2);
this.coords =  new Array (2);
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.makeConstructor (c$, 
function (modelIndex, thisID, dipoleInfo, colix, mad, visible) {
this.modelIndex = modelIndex;
this.thisID = thisID;
this.dipoleInfo = dipoleInfo;
this.colix = colix;
this.mad = mad;
this.visible = visible;
this.type = 0;
}, "~N,~S,~S,~N,~N,~B");
$_M(c$, "setTranslucent", 
function (isTranslucent, translucentLevel) {
this.colix = J.util.C.getColixTranslucent3 (this.colix, isTranslucent, translucentLevel);
}, "~B,~N");
$_M(c$, "set", 
function (thisID, dipoleInfo, atoms, dipoleValue, mad, offsetAngstroms, offsetPercent, offsetSide, origin, vector) {
this.thisID = thisID;
this.dipoleInfo = dipoleInfo;
this.dipoleValue = dipoleValue;
this.mad = mad;
this.offsetAngstroms = offsetAngstroms;
this.offsetPercent = offsetPercent;
this.offsetSide = offsetSide;
this.vector = J.util.V3.newV (vector);
this.origin = J.util.P3.newP (origin);
this.haveAtoms = (atoms[0] != null);
if (this.haveAtoms) {
this.atoms[0] = atoms[0];
this.atoms[1] = atoms[1];
this.centerDipole ();
} else {
this.center = null;
}}, "~S,~S,~A,~N,~N,~N,~N,~N,J.util.P3,J.util.V3");
$_M(c$, "set", 
($fz = function (pt1, pt2) {
this.coords[0] = J.util.P3.newP (pt1);
this.coords[1] = J.util.P3.newP (pt2);
this.isValid = (this.coords[0].distance (this.coords[1]) > 0.1);
if (this.dipoleValue < 0) {
this.origin = J.util.P3.newP (pt2);
this.vector = J.util.V3.newV (pt1);
this.dipoleValue = -this.dipoleValue;
} else {
this.origin = J.util.P3.newP (pt1);
this.vector = J.util.V3.newV (pt2);
}this.dipoleInfo = "" + this.origin + this.vector;
this.vector.sub (this.origin);
if (this.dipoleValue == 0) this.dipoleValue = this.vector.length ();
 else this.vector.scale (this.dipoleValue / this.vector.length ());
this.type = 1;
}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3");
$_M(c$, "set", 
function (value) {
var d = this.dipoleValue;
this.dipoleValue = value;
if (value == 0) this.isValid = false;
if (this.vector == null) return;
this.vector.scale (this.dipoleValue / this.vector.length ());
if (d * this.dipoleValue < 0) this.origin.sub (this.vector);
}, "~N");
$_M(c$, "set", 
function (pt1, pt2, value) {
this.dipoleValue = value;
this.atoms[0] = null;
this.set (pt1, pt2);
}, "J.util.P3,J.util.P3,~N");
$_M(c$, "set", 
function (pt1, dipole) {
this.set (dipole.length ());
var pt2 = J.util.P3.newP (pt1);
pt2.add (dipole);
this.set (pt1, pt2);
this.type = 5;
}, "J.util.P3,J.util.V3");
$_M(c$, "set", 
function (atom1, atom2, value) {
this.set (value);
this.set (atom1, atom2);
this.offsetSide = 0.4;
this.mad = 5;
this.atoms[0] = atom1;
this.atoms[1] = atom2;
this.haveAtoms = true;
this.centerDipole ();
}, "J.modelset.Atom,J.modelset.Atom,~N");
$_M(c$, "centerDipole", 
function () {
this.isValid = (this.atoms[0] !== this.atoms[1] && this.dipoleValue != 0);
if (!this.isValid) return;
var f = this.atoms[0].distance (this.atoms[1]) / (2 * this.dipoleValue) - 0.5;
this.origin.scaleAdd2 (f, this.vector, this.atoms[0]);
this.center =  new J.util.P3 ();
this.center.scaleAdd2 (0.5, this.vector, this.origin);
this.bond = this.atoms[0].getBond (this.atoms[1]);
this.type = (this.bond == null ? 2 : 3);
});
$_M(c$, "isBondType", 
function () {
return (this.type == 2 || this.type == 3);
});
$_M(c$, "getShapeState", 
function () {
if (!this.isValid) return "";
var s =  new J.util.SB ();
s.append ("dipole ID ").append (this.thisID);
if (this.haveAtoms) s.append (" ({").appendI (this.atoms[0].getIndex ()).append (" ").appendI (this.atoms[1].getIndex ()).append ("})");
 else if (this.coords[0] == null) return "";
 else s.append (" ").append (J.util.Escape.eP (this.coords[0])).append (" ").append (J.util.Escape.eP (this.coords[1]));
if (this.isUserValue) s.append (" value ").appendF (this.dipoleValue);
if (this.mad != 5) s.append (" width ").appendF (this.mad / 1000);
if (this.offsetAngstroms != 0) s.append (" offset ").appendF (this.offsetAngstroms);
 else if (this.offsetPercent != 0) s.append (" offset ").appendI (this.offsetPercent);
if (this.offsetSide != 0.4) s.append (" offsetSide ").appendF (this.offsetSide);
if (this.noCross) s.append (" nocross");
if (!this.visible) s.append (" off");
s.append (";\n");
return s.toString ();
});
Clazz.defineStatics (c$,
"DIPOLE_TYPE_UNKNOWN", 0,
"DIPOLE_TYPE_POINTS", 1,
"DIPOLE_TYPE_ATOMS", 2,
"DIPOLE_TYPE_BOND", 3,
"DIPOLE_TYPE_MOLECULAR", 4,
"DIPOLE_TYPE_POINTVECTOR", 5);
});
