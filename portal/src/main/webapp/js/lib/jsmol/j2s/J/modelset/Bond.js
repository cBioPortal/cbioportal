Clazz.declarePackage ("J.modelset");
Clazz.load (["J.util.BS", "$.JmolEdge", "J.viewer.JC"], "J.modelset.Bond", ["J.util.BSUtil", "$.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atom1 = null;
this.atom2 = null;
this.mad = 0;
this.colix = 0;
this.shapeVisibilityFlags = 0;
Clazz.instantialize (this, arguments);
}, J.modelset, "Bond", J.util.JmolEdge);
Clazz.makeConstructor (c$, 
function (atom1, atom2, order, mad, colix) {
Clazz.superConstructor (this, J.modelset.Bond, []);
this.atom1 = atom1;
this.atom2 = atom2;
this.colix = colix;
this.setOrder (order);
this.setMad (mad);
}, "J.modelset.Atom,J.modelset.Atom,~N,~N,~N");
$_M(c$, "setMad", 
function (mad) {
this.mad = mad;
this.setShapeVisibility (mad != 0);
}, "~N");
$_M(c$, "setShapeVisibilityFlags", 
function (shapeVisibilityFlags) {
this.shapeVisibilityFlags = shapeVisibilityFlags;
}, "~N");
$_M(c$, "getShapeVisibilityFlags", 
function () {
return this.shapeVisibilityFlags;
});
$_M(c$, "setShapeVisibility", 
function (isVisible) {
var wasVisible = ((this.shapeVisibilityFlags & J.modelset.Bond.myVisibilityFlag) != 0);
if (wasVisible == isVisible) return;
this.atom1.addDisplayedBond (J.modelset.Bond.myVisibilityFlag, isVisible);
this.atom2.addDisplayedBond (J.modelset.Bond.myVisibilityFlag, isVisible);
if (isVisible) this.shapeVisibilityFlags |= J.modelset.Bond.myVisibilityFlag;
 else this.shapeVisibilityFlags &= ~J.modelset.Bond.myVisibilityFlag;
}, "~B");
$_M(c$, "getIdentity", 
function () {
return (this.index + 1) + " " + this.getOrderNumberAsString () + " " + this.atom1.getInfo () + " -- " + this.atom2.getInfo () + " " + this.atom1.distance (this.atom2);
});
Clazz.overrideMethod (c$, "isCovalent", 
function () {
return (this.order & 1023) != 0;
});
Clazz.overrideMethod (c$, "isHydrogen", 
function () {
return J.modelset.Bond.isOrderH (this.order);
});
c$.isOrderH = $_M(c$, "isOrderH", 
function (order) {
return (order & 30720) != 0;
}, "~N");
$_M(c$, "isStereo", 
function () {
return (this.order & 1024) != 0;
});
$_M(c$, "isPartial", 
function () {
return (this.order & 224) != 0;
});
$_M(c$, "isAromatic", 
function () {
return (this.order & 512) != 0;
});
$_M(c$, "setPaletteID", 
function (pid) {
}, "~N");
$_M(c$, "getEnergy", 
function () {
return 0;
});
$_M(c$, "getValence", 
function () {
return (!this.isCovalent () ? 0 : this.isPartial () || this.is (515) ? 1 : this.order & 7);
});
$_M(c$, "deleteAtomReferences", 
function () {
if (this.atom1 != null) this.atom1.deleteBond (this);
if (this.atom2 != null) this.atom2.deleteBond (this);
this.atom1 = this.atom2 = null;
});
$_M(c$, "setColix", 
function (colix) {
this.colix = colix;
}, "~N");
$_M(c$, "setTranslucent", 
function (isTranslucent, translucentLevel) {
this.colix = J.util.C.getColixTranslucent3 (this.colix, isTranslucent, translucentLevel);
}, "~B,~N");
$_M(c$, "setOrder", 
function (order) {
if (this.atom1.getElementNumber () == 16 && this.atom2.getElementNumber () == 16) order |= 256;
if (order == 512) order = 515;
this.order = order | (this.order & 131072);
}, "~N");
$_M(c$, "getAtom1", 
function () {
return this.atom1;
});
$_M(c$, "getAtom2", 
function () {
return this.atom2;
});
Clazz.overrideMethod (c$, "getAtomIndex1", 
function () {
return this.atom1.index;
});
Clazz.overrideMethod (c$, "getAtomIndex2", 
function () {
return this.atom2.index;
});
$_M(c$, "getRadius", 
function () {
return this.mad / 2000;
});
Clazz.overrideMethod (c$, "getCovalentOrder", 
function () {
return J.util.JmolEdge.getCovalentBondOrder (this.order);
});
$_M(c$, "getOrderName", 
function () {
return J.util.JmolEdge.getBondOrderNameFromOrder (this.order);
});
$_M(c$, "getOrderNumberAsString", 
function () {
return J.util.JmolEdge.getBondOrderNumberFromOrder (this.order);
});
$_M(c$, "getColix1", 
function () {
return J.util.C.getColixInherited (this.colix, this.atom1.colixAtom);
});
$_M(c$, "getColix2", 
function () {
return J.util.C.getColixInherited (this.colix, this.atom2.colixAtom);
});
$_M(c$, "getOtherAtom", 
function (thisAtom) {
return (this.atom1 === thisAtom ? this.atom2 : this.atom2 === thisAtom ? this.atom1 : null);
}, "J.modelset.Atom");
$_M(c$, "setIndex", 
function (i) {
this.index = i;
}, "~N");
$_M(c$, "is", 
function (bondType) {
return (this.order & -131073) == bondType;
}, "~N");
Clazz.overrideMethod (c$, "getOtherAtomNode", 
function (thisAtom) {
return (this.atom1 === thisAtom ? this.atom2 : this.atom2 === thisAtom ? this.atom1 : null);
}, "J.util.JmolNode");
Clazz.overrideMethod (c$, "toString", 
function () {
return this.atom1 + " - " + this.atom2;
});
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
this.associatedAtoms = null;
Clazz.instantialize (this, arguments);
}, J.modelset.Bond, "BondSet", J.util.BS);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.modelset.Bond.BondSet, []);
});
$_M(c$, "getAssociatedAtoms", 
function () {
return this.associatedAtoms;
});
Clazz.makeConstructor (c$, 
function (a) {
Clazz.superConstructor (this, J.modelset.Bond.BondSet, []);
J.util.BSUtil.copy2 (a, this);
}, "J.util.BS");
Clazz.makeConstructor (c$, 
function (a, b) {
this.construct (a);
this.associatedAtoms = b;
}, "J.util.BS,~A");
c$ = Clazz.p0p ();
c$.myVisibilityFlag = c$.prototype.myVisibilityFlag = J.viewer.JC.getShapeVisibilityFlag (1);
});
